package com.vidora.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vidora.app.data.AppGraph
import com.vidora.app.data.Channel
import com.vidora.app.data.Video
import com.vidora.app.ui.components.ChannelAvatar
import com.vidora.app.ui.components.ErrorBlock
import com.vidora.app.ui.components.PlayerPlaceholder
import com.vidora.app.ui.components.SectionHeader
import com.vidora.app.ui.components.VideoRow
import com.vidora.app.ui.components.YouTubePlayer
import com.vidora.app.ui.icons.IconBack
import com.vidora.app.ui.icons.IconBookmark
import com.vidora.app.ui.icons.IconCheck
import com.vidora.app.ui.icons.IconPlus
import com.vidora.app.ui.icons.IconShare
import com.vidora.app.util.relativeTime
import com.vidora.app.util.subscriberLabel
import com.vidora.app.util.viewLabel

@Composable
fun PlayerScreen(
    videoId: String,
    onBack: () -> Unit,
    onVideoClick: (Video) -> Unit,
    onChannelClick: (String) -> Unit
) {
    val store = AppGraph.store
    val repo = AppGraph.repo
    val context = LocalContext.current

    var video by remember(videoId) { mutableStateOf(repo.cachedVideo(videoId)) }
    var error by remember(videoId) { mutableStateOf<String?>(null) }
    var related by remember(videoId) { mutableStateOf<List<Video>>(emptyList()) }
    var descriptionExpanded by remember(videoId) { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    LaunchedEffect(videoId) {
        error = null
        try {
            val loaded = repo.video(videoId)
            if (loaded == null) {
                error = "This video is unavailable. It may have been removed or made private."
            } else {
                video = loaded
                // Watching a video is what puts it in history.
                store.addToHistory(loaded)
                related = runCatching {
                    repo.search(loaded.title.take(60)).items.filterNot { it.id == loaded.id }
                }.getOrDefault(emptyList())
            }
        } catch (t: Throwable) {
            error = t.message ?: "Could not load this video."
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ------------------------------------------------------------- top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                IconBack(MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Now playing",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        if (error != null && video == null) {
            ErrorBlock(message = error.orEmpty(), onRetry = {})
            return@Column
        }

        // -------------------------------------------------------------- player
        if (video == null) {
            PlayerPlaceholder()
        } else {
            YouTubePlayer(videoId = videoId)
        }

        val current = video

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (current != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text = current.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = buildString {
                                val views = viewLabel(current.views)
                                if (views.isNotBlank()) append(views)
                                val when_ = relativeTime(current.publishedAt)
                                if (when_.isNotBlank()) {
                                    if (isNotEmpty()) append(" \u00B7 ")
                                    append(when_)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // --------------------------------------------------- action row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ActionChip(
                            label = if (store.isInWatchLater(current.id)) "Saved" else "Save",
                            active = store.isInWatchLater(current.id),
                            modifier = Modifier.weight(1f)
                        ) {
                            store.toggleWatchLater(current)
                        }
                        ActionChip(
                            label = "Playlist",
                            active = false,
                            modifier = Modifier.weight(1f)
                        ) {
                            showPlaylistDialog = true
                        }
                        ActionChip(
                            label = "Share",
                            active = false,
                            modifier = Modifier.weight(1f)
                        ) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    current.title + "\nhttps://www.youtube.com/watch?v=" + current.id
                                )
                            }
                            context.startActivity(Intent.createChooser(intent, "Share video"))
                        }
                    }
                }

                // ------------------------------------------------ channel strip
                if (current.channelId.isNotBlank()) {
                    item {
                        val channel = repo.cachedChannel(current.channelId)
                        val subscribed = store.isSubscribed(current.channelId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onChannelClick(current.channelId) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChannelAvatar(
                                    url = channel?.avatar.orEmpty(),
                                    name = current.channelTitle,
                                    size = 40
                                )
                                Spacer(Modifier.width(11.dp))
                                Column {
                                    Text(
                                        text = current.channelTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = subscriberLabel(channel?.subscribers ?: 0L),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            SubscribeButton(
                                subscribed = subscribed,
                                onClick = {
                                    val target = channel ?: Channel(
                                        id = current.channelId,
                                        title = current.channelTitle
                                    )
                                    store.toggleSubscription(target)
                                }
                            )
                        }
                    }
                }

                // -------------------------------------------------- description
                if (current.description.isNotBlank()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable { descriptionExpanded = !descriptionExpanded }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = current.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (descriptionExpanded) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (descriptionExpanded) "Show less" else "Show more",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (related.isNotEmpty()) {
                item { SectionHeader(title = "Related videos") }
                items(related, key = { "rel-" + it.id }) { item ->
                    VideoRow(
                        video = item,
                        onClick = { onVideoClick(item) },
                        onChannelClick = onChannelClick
                    )
                }
            }
            item { Spacer(Modifier.height(28.dp)) }
        }
    }

    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text("Add to playlist") },
            text = {
                Column {
                    if (store.playlists.isEmpty()) {
                        Text(
                            "You have no playlists yet. Create one below.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        store.playlists.forEach { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        video?.let { store.addToPlaylist(playlist.id, it) }
                                        showPlaylistDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = playlist.videos.size.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = newPlaylistName,
                            onValueChange = { newPlaylistName = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                if (newPlaylistName.isNotBlank()) {
                                    val created = store.createPlaylist(newPlaylistName)
                                    video?.let { store.addToPlaylist(created.id, it) }
                                    newPlaylistName = ""
                                    showPlaylistDialog = false
                                }
                            }
                        ) { Text("Create") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistDialog = false }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun ActionChip(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (label) {
            "Save", "Saved" -> IconBookmark(
                tint = if (active) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                filled = active,
                size = 17.dp
            )
            "Playlist" -> IconPlus(MaterialTheme.colorScheme.onSurfaceVariant, size = 17.dp)
            else -> IconShare(MaterialTheme.colorScheme.onSurfaceVariant, size = 17.dp)
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (active) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SubscribeButton(subscribed: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (subscribed) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.primary
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
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
