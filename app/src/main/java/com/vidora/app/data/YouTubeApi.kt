package com.vidora.app.data

import android.util.LruCache
import com.vidora.app.BuildConfig
import com.vidora.app.util.formatIsoDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiException(message: String) : Exception(message)

/**
 * Thin client for the official YouTube Data API v3.
 *
 * Read-only: the app only ever calls list endpoints (videos, search, channels,
 * playlistItems, playlists) and plays media through the official embedded player.
 */
object YouTubeApi {

    private const val PLACEHOLDER = "YOUR_YOUTUBE_API_KEY"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // Small in-memory caches keep scrolling smooth and save quota units.
    private val videoCache = LruCache<String, Video>(240)
    private val channelCache = LruCache<String, Channel>(48)

    fun hasApiKey(): Boolean =
        BuildConfig.YOUTUBE_API_KEY.isNotBlank() && BuildConfig.YOUTUBE_API_KEY != PLACEHOLDER

    fun cachedVideo(id: String): Video? = videoCache.get(id)

    fun cachedChannel(id: String): Channel? = channelCache.get(id)

    private suspend fun get(endpoint: String, params: Map<String, String>): JSONObject =
        withContext(Dispatchers.IO) {
            if (!hasApiKey()) {
                throw ApiException(
                    "No API key yet. Add YOUTUBE_API_KEY to local.properties (see README) and rebuild."
                )
            }
            val builder = HttpUrl.Builder()
                .scheme("https")
                .host("www.googleapis.com")
                .addPathSegment("youtube")
                .addPathSegment("v3")
                .addPathSegment(endpoint)
                .addQueryParameter("key", BuildConfig.YOUTUBE_API_KEY)

            params.forEach { (key, value) ->
                if (value.isNotBlank()) builder.addQueryParameter(key, value)
            }

            val request = Request.Builder()
                .url(builder.build())
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val apiMessage = runCatching {
                        JSONObject(body).optJSONObject("error")?.optString("message")
                    }.getOrNull().orEmpty()
                    throw ApiException(friendlyError(response.code, apiMessage))
                }
                runCatching { JSONObject(body) }.getOrElse {
                    throw ApiException("The service returned an unexpected response.")
                }
            }
        }

    private fun friendlyError(code: Int, apiMessage: String): String = when (code) {
        400 -> "Request rejected. Check that the API key is valid and the YouTube Data API v3 is enabled. " +
            apiMessage
        403 -> "YouTube refused the request: quota exceeded, key restricted, or the item is private. " +
            apiMessage
        404 -> "Not found. The video, channel or playlist may have been removed."
        in 500..599 -> "YouTube is having trouble right now. Try again in a moment."
        else -> if (apiMessage.isBlank()) "Network error (HTTP $code)." else apiMessage
    }

    // ---------------------------------------------------------------- trending

    suspend fun trending(region: String, pageToken: String? = null): VideoPage {
        val json = get(
            "videos",
            mapOf(
                "part" to "snippet,contentDetails,statistics",
                "chart" to "mostPopular",
                "regionCode" to region,
                "maxResults" to "20",
                "pageToken" to pageToken.orEmpty()
            )
        )
        val items = parseVideoArray(json.optJSONArray("items"))
        return VideoPage(items, json.optString("nextPageToken").takeIf { it.isNotBlank() })
    }

    // ------------------------------------------------------------------ search

    /** Costs 100 quota units plus 1 for the metadata enrichment, so results carry real durations. */
    suspend fun searchVideos(query: String, pageToken: String? = null): VideoPage {
        val json = get(
            "search",
            mapOf(
                "part" to "snippet",
                "q" to query,
                "type" to "video",
                "maxResults" to "20",
                "videoEmbeddable" to "true",
                "pageToken" to pageToken.orEmpty()
            )
        )
        val arr = json.optJSONArray("items") ?: JSONArray()
        val ids = ArrayList<String>(arr.length())
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            val id = item.optJSONObject("id")?.optString("videoId").orEmpty()
            if (id.isNotBlank()) ids.add(id)
        }
        val detailed = videosByIds(ids)
        val byId = detailed.associateBy { it.id }
        val items = ids.mapNotNull { byId[it] }
        return VideoPage(items, json.optString("nextPageToken").takeIf { it.isNotBlank() })
    }

    suspend fun searchChannels(query: String): List<Channel> {
        val json = get(
            "search",
            mapOf(
                "part" to "snippet",
                "q" to query,
                "type" to "channel",
                "maxResults" to "15"
            )
        )
        val arr = json.optJSONArray("items") ?: JSONArray()
        val result = ArrayList<Channel>(arr.length())
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            val id = item.optJSONObject("id")?.optString("channelId").orEmpty()
            val snippet = item.optJSONObject("snippet") ?: continue
            if (id.isBlank()) continue
            result.add(
                Channel(
                    id = id,
                    title = snippet.optString("title"),
                    description = snippet.optString("description"),
                    avatar = snippet.thumbnailUrl()
                )
            )
        }
        return result
    }

    // ------------------------------------------------------------------ videos

    suspend fun videosByIds(ids: List<String>): List<Video> {
        if (ids.isEmpty()) return emptyList()
        val result = ArrayList<Video>(ids.size)
        ids.chunked(50).forEach { chunk ->
            val json = get(
                "videos",
                mapOf(
                    "part" to "snippet,contentDetails,statistics",
                    "id" to chunk.joinToString(","),
                    "maxResults" to "50"
                )
            )
            result.addAll(parseVideoArray(json.optJSONArray("items")))
        }
        val byId = result.associateBy { it.id }
        return ids.mapNotNull { byId[it] }
    }

    suspend fun video(id: String): Video? {
        videoCache.get(id)?.let { return it }
        return videosByIds(listOf(id)).firstOrNull()
    }

    // ---------------------------------------------------------------- channels

    suspend fun channel(id: String): Channel? {
        channelCache.get(id)?.let { return it }
        val json = get(
            "channels",
            mapOf(
                "part" to "snippet,statistics,contentDetails,brandingSettings",
                "id" to id
            )
        )
        val item = json.optJSONArray("items")?.optJSONObject(0) ?: return null
        val snippet = item.optJSONObject("snippet")
        val stats = item.optJSONObject("statistics")
        val uploads = item.optJSONObject("contentDetails")
            ?.optJSONObject("relatedPlaylists")
            ?.optString("uploads")
            .orEmpty()
        val banner = item.optJSONObject("brandingSettings")
            ?.optJSONObject("image")
            ?.optString("bannerExternalUrl")
            .orEmpty()
        val channel = Channel(
            id = id,
            title = snippet?.optString("title").orEmpty(),
            description = snippet?.optString("description").orEmpty(),
            avatar = snippet?.thumbnails()?.optJSONObject("high")?.optString("url")
                ?: snippet?.thumbnails()?.optJSONObject("default")?.optString("url").orEmpty(),
            banner = banner,
            subscribers = stats?.optString("subscriberCount")?.toLongOrNull() ?: 0L,
            videoCount = stats?.optString("videoCount")?.toLongOrNull() ?: 0L,
            uploadsPlaylist = uploads
        )
        channelCache.put(id, channel)
        return channel
    }

    /** Uploads are read through the channel's uploads playlist: 1 quota unit per page. */
    suspend fun channelVideos(uploadsPlaylistId: String, pageToken: String? = null): VideoPage {
        return playlistItems(uploadsPlaylistId, pageToken)
    }

    // --------------------------------------------------------------- playlists

    suspend fun playlistItems(playlistId: String, pageToken: String? = null): VideoPage {
        val json = get(
            "playlistItems",
            mapOf(
                "part" to "snippet,contentDetails",
                "playlistId" to playlistId,
                "maxResults" to "20",
                "pageToken" to pageToken.orEmpty()
            )
        )
        val arr = json.optJSONArray("items") ?: JSONArray()
        val ids = ArrayList<String>(arr.length())
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            val id = item.optJSONObject("snippet")
                ?.optJSONObject("resourceId")
                ?.optString("videoId")
                .orEmpty()
            if (id.isNotBlank()) ids.add(id)
        }
        val byId = videosByIds(ids).associateBy { it.id }
        // Keep the playlist's own ordering, and fall back to the item snippet for
        // videos that no longer resolve (deleted or private entries).
        val items = ArrayList<Video>(arr.length())
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            val snippet = item.optJSONObject("snippet") ?: continue
            val id = snippet.optJSONObject("resourceId")?.optString("videoId").orEmpty()
            if (id.isBlank()) continue
            val rich = byId[id]
            items.add(
                rich ?: Video(
                    id = id,
                    title = snippet.optString("title"),
                    channelId = snippet.optString("videoOwnerChannelId"),
                    channelTitle = snippet.optString("videoOwnerChannelTitle"),
                    description = snippet.optString("description"),
                    thumbnail = snippet.thumbnailUrl(),
                    publishedAt = snippet.optString("publishedAt")
                )
            )
        }
        return VideoPage(items, json.optString("nextPageToken").takeIf { it.isNotBlank() })
    }

    suspend fun playlist(id: String): Playlist? {
        val json = get(
            "playlists",
            mapOf(
                "part" to "snippet,contentDetails",
                "id" to id,
                "maxResults" to "1"
            )
        )
        val item = json.optJSONArray("items")?.optJSONObject(0) ?: return null
        val snippet = item.optJSONObject("snippet") ?: return null
        return Playlist(
            id = id,
            title = snippet.optString("title"),
            thumbnail = snippet.thumbnailUrl(),
            channelTitle = snippet.optString("channelTitle"),
            itemCount = item.optJSONObject("contentDetails")?.optInt("itemCount") ?: 0,
            description = snippet.optString("description")
        )
    }

    // ----------------------------------------------------------------- parsing

    private fun parseVideoArray(arr: JSONArray?): List<Video> {
        if (arr == null) return emptyList()
        val list = ArrayList<Video>(arr.length())
        for (i in 0 until arr.length()) {
            parseVideo(arr.optJSONObject(i))?.let { list.add(it) }
        }
        return list
    }

    private fun parseVideo(item: JSONObject?): Video? {
        if (item == null) return null
        val id = item.optString("id")
        val snippet = item.optJSONObject("snippet") ?: return null
        if (id.isBlank()) return null
        val video = Video(
            id = id,
            title = snippet.optString("title"),
            channelId = snippet.optString("channelId"),
            channelTitle = snippet.optString("channelTitle"),
            description = snippet.optString("description"),
            thumbnail = snippet.thumbnailUrl(),
            publishedAt = snippet.optString("publishedAt"),
            duration = formatIsoDuration(
                item.optJSONObject("contentDetails")?.optString("duration").orEmpty()
            ),
            views = item.optJSONObject("statistics")?.optString("viewCount")?.toLongOrNull() ?: 0L
        )
        videoCache.put(id, video)
        return video
    }

    private fun JSONObject.thumbnails(): JSONObject? = optJSONObject("thumbnails")

    private fun JSONObject.thumbnailUrl(): String {
        val t = thumbnails() ?: return ""
        return t.optJSONObject("high")?.optString("url")
            ?: t.optJSONObject("medium")?.optString("url")
            ?: t.optJSONObject("standard")?.optString("url")
            ?: t.optJSONObject("default")?.optString("url")
                .orEmpty()
    }
}
