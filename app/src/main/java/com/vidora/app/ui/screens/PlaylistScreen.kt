package com.vidora.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vidora.app.data.AppGraph
import com.vidora.app.data.Video
import com.vidora.app.ui.components.EmptyState
import com.vidora.app.ui.components.ScreenTopBar
import com.vidora.app.ui.components.VideoFeed
import com.vidora.app.ui.components.VideoRow

/** A playlist that lives on YouTube: channel uploads, curated lists, search results. */
@Composable
fun PlaylistScreen(
    playlistId: String,
    onBack: () -> Unit,
    onVideoClick: (Video) -> Unit,
    onChannelClick: (String) -> Unit
) {
    val repo = AppGraph.repo
    var title by remember(playlistId) { mutableStateOf("Playlist") }
    var subtitle by remember(playlistId) { mutableStateOf("") }

    LaunchedEffect(playlistId) {
        runCatching { repo.playlist(playlistId) }.getOrNull()?.let { playlist ->
            title = playlist.title
            subtitle = buildString {
                if (playlist.channelTitle.isNotBlank()) append(playlist.channelTitle)
                if (playlist.itemCount > 0) {
                    if (isNotEmpty()) append(" \u00B7 ")
                    append(playlist.itemCount).append(" videos")
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTopBar(title = title, subtitle = subtitle, onBack = onBack)
        VideoFeed(
            key = "playlist-" + playlistId,
            fetch = { token -> repo.playlistVideos(playlistId, token) },
            onVideoClick = onVideoClick,
            onChannelClick = onChannelClick,
            compact = true,
            emptyTitle = "This playlist is empty",
            emptyMessage = "No playable videos were found in this list."
        )
    }
}

/** A playlist the user built inside Vidora, stored on the device. */
@Composable
fun MyPlaylistScreen(
    playlistId: String,
    onBack: () -> Unit,
    onVideoClick: (Video) -> Unit
) {
    val store = AppGraph.store
    val playlist = store.playlist(playlistId)
    val videos = playlist?.videos.orEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTopBar(
            title = playlist?.name ?: "Playlist",
            subtitle = if (videos.isEmpty()) "" else videos.size.toString() + " videos",
            onBack = onBack
        )
        if (videos.isEmpty()) {
            EmptyState(
                title = "Nothing saved here yet",
                message = "Open a video and use Playlist to add it to this list."
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(videos, key = { it.id }) { video ->
                    VideoRow(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onMoreClick = { store.removeFromPlaylist(playlistId, it.id) }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}
