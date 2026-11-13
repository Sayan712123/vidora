package com.vidora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vidora.app.data.AppGraph
import com.vidora.app.data.Video
import com.vidora.app.data.VideoPage
import com.vidora.app.ui.components.ChannelAvatar
import com.vidora.app.ui.components.ErrorBlock
import com.vidora.app.ui.components.ScreenTopBar
import com.vidora.app.ui.components.SkeletonBlock
import com.vidora.app.ui.components.VideoFeed
import com.vidora.app.ui.icons.IconCheck
import com.vidora.app.util.compactCount
import com.vidora.app.util.subscriberLabel

@Composable
fun ChannelScreen(
    channelId: String,
    onBack: () -> Unit,
    onVideoClick: (Video) -> Unit
) {
    val store = AppGraph.store
    val repo = AppGraph.repo

    var channel by remember(channelId) { mutableStateOf(repo.cachedChannel(channelId)) }
    var error by remember(channelId) { mutableStateOf<String?>(null) }
    var aboutExpanded by remember(channelId) { mutableStateOf(false) }
    var reloadToken by remember(channelId) { mutableStateOf(0) }

    LaunchedEffect(channelId, reloadToken) {
        error = null
        try {
            val loaded = repo.channel(channelId)
            if (loaded == null) error = "This channel could not be found." else channel = loaded
        } catch (t: Throwable) {
            error = t.message ?: "Could not load this channel."
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTopBar(title = channel?.title ?: "Channel", onBack = onBack)

        if (error != null && channel == null) {
            ErrorBlock(message = error.orEmpty(), onRetry = { reloadToken++ })
            return@Column
        }

        val current = channel
        if (current == null) {
            Column(modifier = Modifier.padding(16.dp)) {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f),
                    radius = 14
                )
                Spacer(Modifier.height(14.dp))
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(18.dp),
                    radius = 5
                )
            }
            return@Column
        }

        VideoFeed(
            key = "channel-" + channelId,
            fetch = { token ->
                val uploads = current.uploadsPlaylist
                if (uploads.isBlank()) VideoPage(emptyList(), null)
                else repo.channelVideos(uploads, token)
            },
            onVideoClick = onVideoClick,
            compact = true,
            emptyTitle = "No uploads found",
            emptyMessage = "This channel has no public videos right now.",
            header = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (current.banner.isNotBlank()) {
                            AsyncImage(
                                model = current.banner,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChannelAvatar(url = current.avatar, name = current.title, size = 62)
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = current.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = buildString {
                                    append(subscriberLabel(current.subscribers))
                                    if (current.videoCount > 0) {
                                        append(" \u00B7 ")
                                        append(compactCount(current.videoCount))
                                        append(" videos")
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val subscribed = store.isSubscribed(current.id)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (subscribed) MaterialTheme.colorScheme.surfaceVariant
                                    else MaterialTheme.colorScheme.primary
                                )
                                .clickable { store.toggleSubscription(current) }
                                .padding(horizontal = 18.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (subscribed) {
                                IconCheck(MaterialTheme.colorScheme.onSurfaceVariant, size = 15.dp)
                                Spacer(Modifier.width(5.dp))
                            }
                            Text(
                                text = if (subscribed) "Subscribed" else "Subscribe",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (subscribed) MaterialTheme.colorScheme.onSurfaceVariant
                                        else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    if (current.description.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable { aboutExpanded = !aboutExpanded }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = current.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (aboutExpanded) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (aboutExpanded) "Show less" else "About",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Videos",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                    )
                }
            }
        )
    }
}
