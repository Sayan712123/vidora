package com.vidora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vidora.app.data.AppGraph
import com.vidora.app.data.Video
import com.vidora.app.ui.components.ChannelAvatar
import com.vidora.app.ui.components.EmptyState
import com.vidora.app.ui.components.VideoFeed
import com.vidora.app.ui.components.VidoraWordmark

/**
 * Subscriptions are stored on the device. The feed is the uploads playlist of the
 * channel the user is currently looking at, so switching channels is instant and costs
 * one quota unit per page.
 */
@Composable
fun SubscriptionsScreen(
    onVideoClick: (Video) -> Unit,
    onChannelClick: (String) -> Unit
) {
    val store = AppGraph.store
    val repo = AppGraph.repo
    val subs = store.subscriptions

    var activeChannelId by remember(subs.size) {
        mutableStateOf(subs.firstOrNull()?.id.orEmpty())
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VidoraWordmark(modifier = Modifier.weight(1f))
        }

        if (subs.isEmpty()) {
            EmptyState(
                title = "No subscriptions yet",
                message = "Open a channel and tap Subscribe. Your channels show up here, " +
                    "newest first.",
                icon = "\u2605"
            )
            return@Column
        }

        // Channel rail
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(subs, key = { "sub-" + it.id }) { channel ->
                val active = channel.id == activeChannelId
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(66.dp)
                        .clickable { activeChannelId = channel.id }
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (active) 58.dp else 54.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else Color.Transparent
                            )
                            .padding(3.dp)
                    ) {
                        ChannelAvatar(
                            url = channel.avatar,
                            name = channel.title,
                            size = if (active) 52 else 48
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = channel.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (active) MaterialTheme.colorScheme.onBackground
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        val active = subs.firstOrNull { it.id == activeChannelId } ?: subs.first()
        if (active.uploadsPlaylist.isBlank()) {
            // The stored channel was saved before its uploads playlist was known.
            EmptyState(
                title = active.title,
                message = "Open the channel page once to load its uploads, then come back.",
                icon = "\u25B6"
            )
        } else {
            VideoFeed(
                key = "subs-" + active.id,
                fetch = { token -> repo.channelVideos(active.uploadsPlaylist, token) },
                onVideoClick = onVideoClick,
                onChannelClick = onChannelClick,
                compact = true,
                emptyTitle = "No new videos",
                emptyMessage = active.title + " has not published anything public yet."
            )
        }
    }
}
