package com.vidora.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vidora.app.BuildConfig
import org.json.JSONArray
import java.util.UUID

/**
 * Everything that is personal to this device: theme, watch history, subscriptions,
 * playlists, "Watch later" and recent searches. Held in one SharedPreferences file as
 * small JSON blobs - no database, no background work, cheap on low-end hardware.
 */
class UserStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vidora_user", Context.MODE_PRIVATE)

    var themeMode by mutableStateOf(prefs.getString(KEY_THEME, "system") ?: "system")
        private set

    var region by mutableStateOf(
        prefs.getString(KEY_REGION, BuildConfig.DEFAULT_REGION).orEmpty()
            .ifBlank { "IN" }
    )
        private set

    var history by mutableStateOf(readVideos(KEY_HISTORY))
        private set

    var subscriptions by mutableStateOf(readChannels(KEY_SUBS))
        private set

    var playlists by mutableStateOf(readPlaylists())
        private set

    var watchLater by mutableStateOf(readVideos(KEY_WATCH_LATER))
        private set

    var recentSearches by mutableStateOf(
        prefs.getString(KEY_SEARCHES, "").orEmpty()
            .split("\u001F")
            .filter { it.isNotBlank() }
    )
        private set

    // ------------------------------------------------------------------- theme

    fun setTheme(mode: String) {
        themeMode = mode
        prefs.edit().putString(KEY_THEME, mode).apply()
    }

    fun setRegion(code: String) {
        region = code
        prefs.edit().putString(KEY_REGION, code).apply()
    }

    // ----------------------------------------------------------------- history

    /** Most recent first, de-duplicated, capped so the file stays small. */
    fun addToHistory(video: Video) {
        val updated = ArrayList<Video>(history.size + 1)
        updated.add(video)
        history.forEach { if (it.id != video.id) updated.add(it) }
        history = updated.take(120)
        writeVideos(KEY_HISTORY, history)
    }

    fun removeFromHistory(videoId: String) {
        history = history.filterNot { it.id == videoId }
        writeVideos(KEY_HISTORY, history)
    }

    fun clearHistory() {
        history = emptyList()
        writeVideos(KEY_HISTORY, history)
    }

    // ----------------------------------------------------------- subscriptions

    fun isSubscribed(channelId: String): Boolean =
        subscriptions.any { it.id == channelId }

    fun toggleSubscription(channel: Channel) {
        subscriptions = if (isSubscribed(channel.id)) {
            subscriptions.filterNot { it.id == channel.id }
        } else {
            listOf(channel) + subscriptions
        }
        writeChannels(KEY_SUBS, subscriptions)
    }

    // --------------------------------------------------------------- watch later

    fun isInWatchLater(videoId: String): Boolean = watchLater.any { it.id == videoId }

    fun toggleWatchLater(video: Video) {
        watchLater = if (isInWatchLater(video.id)) {
            watchLater.filterNot { it.id == video.id }
        } else {
            listOf(video) + watchLater
        }
        writeVideos(KEY_WATCH_LATER, watchLater)
    }

    fun clearWatchLater() {
        watchLater = emptyList()
        writeVideos(KEY_WATCH_LATER, watchLater)
    }

    // ---------------------------------------------------------------- playlists

    fun createPlaylist(name: String): UserPlaylist {
        val playlist = UserPlaylist(id = UUID.randomUUID().toString(), name = name.trim())
        playlists = playlists + playlist
        writePlaylists()
        return playlist
    }

    fun deletePlaylist(id: String) {
        playlists = playlists.filterNot { it.id == id }
        writePlaylists()
    }

    fun addToPlaylist(playlistId: String, video: Video) {
        playlists = playlists.map { playlist ->
            if (playlist.id != playlistId || playlist.videos.any { it.id == video.id }) {
                playlist
            } else {
                playlist.copy(videos = playlist.videos + video)
            }
        }
        writePlaylists()
    }

    fun removeFromPlaylist(playlistId: String, videoId: String) {
        playlists = playlists.map { playlist ->
            if (playlist.id != playlistId) playlist
            else playlist.copy(videos = playlist.videos.filterNot { it.id == videoId })
        }
        writePlaylists()
    }

    fun playlist(id: String): UserPlaylist? = playlists.firstOrNull { it.id == id }

    // ------------------------------------------------------------------ searches

    fun addSearch(term: String) {
        val clean = term.trim()
        if (clean.length < 2) return
        recentSearches = (listOf(clean) + recentSearches.filterNot { it.equals(clean, true) }).take(10)
        prefs.edit().putString(KEY_SEARCHES, recentSearches.joinToString("\u001F")).apply()
    }

    fun removeSearch(term: String) {
        recentSearches = recentSearches.filterNot { it == term }
        prefs.edit().putString(KEY_SEARCHES, recentSearches.joinToString("\u001F")).apply()
    }

    fun clearSearches() {
        recentSearches = emptyList()
        prefs.edit().remove(KEY_SEARCHES).apply()
    }

    // ------------------------------------------------------------------- helpers

    private fun readVideos(key: String): List<Video> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            val list = ArrayList<Video>(arr.length())
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { list.add(Video.fromJson(it)) }
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun writeVideos(key: String, videos: List<Video>) {
        val arr = JSONArray()
        videos.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(key, arr.toString()).apply()
    }

    private fun readChannels(key: String): List<Channel> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            val list = ArrayList<Channel>(arr.length())
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { list.add(Channel.fromJson(it)) }
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun writeChannels(key: String, channels: List<Channel>) {
        val arr = JSONArray()
        channels.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(key, arr.toString()).apply()
    }

    private fun readPlaylists(): List<UserPlaylist> {
        val raw = prefs.getString(KEY_PLAYLISTS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            val list = ArrayList<UserPlaylist>(arr.length())
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { list.add(UserPlaylist.fromJson(it)) }
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun writePlaylists() {
        val arr = JSONArray()
        playlists.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY_PLAYLISTS, arr.toString()).apply()
    }

    private companion object {
        const val KEY_THEME = "theme_mode"
        const val KEY_REGION = "region"
        const val KEY_HISTORY = "history"
        const val KEY_SUBS = "subscriptions"
        const val KEY_PLAYLISTS = "playlists"
        const val KEY_WATCH_LATER = "watch_later"
        const val KEY_SEARCHES = "recent_searches"
    }
}
