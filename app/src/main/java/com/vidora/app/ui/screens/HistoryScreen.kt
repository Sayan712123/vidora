package com.vidora.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vidora.app.data.AppGraph
import com.vidora.app.data.Video
import com.vidora.app.ui.components.EmptyState
import com.vidora.app.ui.components.ScreenTopBar
import com.vidora.app.ui.components.VideoRow

/** Watch history, newest first, stored on the device only. */
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onVideoClick: (Video) -> Unit,
    onChannelClick: (String) -> Unit
) {
    val store = AppGraph.store
    val history = store.history

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTopBar(
            title = "History",
            subtitle = if (history.isEmpty()) "" else history.size.toString() + " videos",
            onBack = onBack,
            trailing = {
                if (history.isNotEmpty()) {
                    Text(
                        text = "Clear all",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { store.clearHistory() }
                            .padding(8.dp)
                    )
                }
            }
        )

        if (history.isEmpty()) {
            EmptyState(
                title = "No watch history",
                message = "Videos you open in Vidora appear here so you can find them again.",
                icon = "\u21BB"
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(history, key = { "h-" + it.id }) { video ->
                    VideoRow(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onChannelClick = onChannelClick,
                        onMoreClick = { store.removeFromHistory(it.id) }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

/** Videos the user saved for later, newest first. */
@Composable
fun WatchLaterScreen(
    onBack: () -> Unit,
    onVideoClick: (Video) -> Unit,
    onChannelClick: (String) -> Unit
) {
    val store = AppGraph.store
    val saved = store.watchLater

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTopBar(
            title = "Watch later",
            subtitle = if (saved.isEmpty()) "" else saved.size.toString() + " videos",
            onBack = onBack
        )

        if (saved.isEmpty()) {
            EmptyState(
                title = "Nothing saved for later",
                message = "Tap Save on any video and it will wait for you here.",
                icon = "\u2606"
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(saved, key = { "w-" + it.id }) { video ->
                    VideoRow(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onChannelClick = onChannelClick,
                        onMoreClick = { store.toggleWatchLater(it) }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}
