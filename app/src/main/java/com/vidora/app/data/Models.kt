package com.vidora.app.data

import org.json.JSONObject

/** A single video. All fields are plain values so the type can be cached and persisted. */
data class Video(
    val id: String,
    val title: String,
    val channelId: String = "",
    val channelTitle: String = "",
    val description: String = "",
    val thumbnail: String = "",
    val publishedAt: String = "",
    val duration: String = "",
    val views: Long = 0L
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("channelId", channelId)
        put("channelTitle", channelTitle)
        put("description", description)
        put("thumbnail", thumbnail)
        put("publishedAt", publishedAt)
        put("duration", duration)
        put("views", views)
    }

    companion object {
        fun fromJson(o: JSONObject): Video = Video(
            id = o.optString("id"),
            title = o.optString("title"),
            channelId = o.optString("channelId"),
            channelTitle = o.optString("channelTitle"),
            description = o.optString("description"),
            thumbnail = o.optString("thumbnail"),
            publishedAt = o.optString("publishedAt"),
            duration = o.optString("duration"),
            views = o.optLong("views")
        )
    }
}

data class Channel(
    val id: String,
    val title: String,
    val description: String = "",
    val avatar: String = "",
    val banner: String = "",
    val subscribers: Long = 0L,
    val videoCount: Long = 0L,
    val uploadsPlaylist: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("description", description)
        put("avatar", avatar)
        put("banner", banner)
        put("subscribers", subscribers)
        put("videoCount", videoCount)
        put("uploadsPlaylist", uploadsPlaylist)
    }

    companion object {
        fun fromJson(o: JSONObject): Channel = Channel(
            id = o.optString("id"),
            title = o.optString("title"),
            description = o.optString("description"),
            avatar = o.optString("avatar"),
            banner = o.optString("banner"),
            subscribers = o.optLong("subscribers"),
            videoCount = o.optLong("videoCount"),
            uploadsPlaylist = o.optString("uploadsPlaylist")
        )
    }
}

data class Playlist(
    val id: String,
    val title: String,
    val thumbnail: String = "",
    val channelTitle: String = "",
    val itemCount: Int = 0,
    val description: String = ""
)

/** A playlist the user created on this device. */
data class UserPlaylist(
    val id: String,
    val name: String,
    val videos: List<Video> = emptyList()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        val arr = org.json.JSONArray()
        videos.forEach { arr.put(it.toJson()) }
        put("videos", arr)
    }

    companion object {
        fun fromJson(o: JSONObject): UserPlaylist {
            val arr = o.optJSONArray("videos") ?: org.json.JSONArray()
            val list = ArrayList<Video>(arr.length())
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { list.add(Video.fromJson(it)) }
            }
            return UserPlaylist(
                id = o.optString("id"),
                name = o.optString("name"),
                videos = list
            )
        }
    }
}

/** One page of video results, with the token needed to ask for the next page. */
data class VideoPage(val items: List<Video>, val nextPageToken: String?)
