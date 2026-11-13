package com.vidora.app.data

/**
 * Single entry point the UI talks to. Keeps the API client and the local store
 * together and gives the screens one place to fetch from.
 */
class Repository(val store: UserStore) {

    fun hasApiKey(): Boolean = YouTubeApi.hasApiKey()

    suspend fun homeFeed(pageToken: String? = null): VideoPage =
        YouTubeApi.trending(store.region, pageToken)

    suspend fun search(query: String, pageToken: String? = null): VideoPage =
        YouTubeApi.searchVideos(query, pageToken)

    suspend fun searchChannels(query: String): List<Channel> =
        YouTubeApi.searchChannels(query)

    suspend fun video(id: String): Video? = YouTubeApi.video(id)

    suspend fun channel(id: String): Channel? = YouTubeApi.channel(id)

    suspend fun channelVideos(uploadsPlaylistId: String, pageToken: String? = null): VideoPage =
        YouTubeApi.channelVideos(uploadsPlaylistId, pageToken)

    suspend fun playlist(id: String): Playlist? = YouTubeApi.playlist(id)

    suspend fun playlistVideos(id: String, pageToken: String? = null): VideoPage =
        YouTubeApi.playlistItems(id, pageToken)

    fun cachedVideo(id: String): Video? = YouTubeApi.cachedVideo(id)

    fun cachedChannel(id: String): Channel? = YouTubeApi.cachedChannel(id)
}

/** Minimal service locator - initialised once from [com.vidora.app.VidoraApplication]. */
object AppGraph {
    lateinit var store: UserStore
        private set
    lateinit var repo: Repository
        private set

    fun init(store: UserStore) {
        this.store = store
        this.repo = Repository(store)
    }
}
