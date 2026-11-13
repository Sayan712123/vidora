package com.vidora.app.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vidora.app.data.AppGraph
import com.vidora.app.data.Video
import com.vidora.app.ui.components.ChannelAvatar
import com.vidora.app.ui.components.SectionHeader
import com.vidora.app.ui.components.VidoraWordmark
import com.vidora.app.ui.icons.IconBookmark
import com.vidora.app.ui.icons.IconHistory
import com.vidora.app.ui.icons.IconLibrary
import com.vidora.app.ui.icons.IconPlus
import com.vidora.app.ui.icons.IconSettings

@Composable
fun LibraryScreen(
    onVideoClick: (Video) -> Unit,
    onHistoryClick: () -> Unit,
    onWatchLaterClick: () -> Unit,
    onMyPlaylistClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val store = AppGraph.store
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VidoraWordmark(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onSettingsClick),
                contentAlignment = Alignment.Center
            ) {
                IconSettings(MaterialTheme.colorScheme.onBackground)
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LibraryTile(
                        label = "History",
                        count = store.history.size,
                        modifier = Modifier.weight(1f),
                        onClick = onHistoryClick
                    ) { IconHistory(MaterialTheme.colorScheme.primary, size = 20.dp) }
                    LibraryTile(
                        label = "Watch later",
                        count = store.watchLater.size,
                        modifier = Modifier.weight(1f),
                        onClick = onWatchLaterClick
                    ) { IconBookmark(MaterialTheme.colorScheme.primary, size = 20.dp) }
                }
            }

            item {
                SectionHeader(
                    title = "Your playlists",
                    action = "New",
                    onAction = { showCreate = true }
                )
            }

            if (store.playlists.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { showCreate = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconPlus(MaterialTheme.colorScheme.primary, size = 20.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Create a playlist",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Group videos you want to watch together.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(store.playlists, key = { "pl-" + it.id }) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMyPlaylistClick(playlist.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            IconLibrary(MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = playlist.videos.size.toString() + " videos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { store.deletePlaylist(playlist.id) }
                                .padding(8.dp)
                        )
                    }
                }
            }

            if (store.subscriptions.isNotEmpty()) {
                item { SectionHeader(title = "Subscribed channels") }
                items(store.subscriptions, key = { "lib-sub-" + it.id }) { channel ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChannelAvatar(url = channel.avatar, name = channel.title, size = 40)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = channel.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Unsubscribe",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { store.toggleSubscription(channel) }
                                .padding(8.dp)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(28.dp)) }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("New playlist") },
            text = {
                BasicTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            store.createPlaylist(newName)
                            newName = ""
                            showCreate = false
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LibraryTile(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (count == 0) "Empty" else count.toString() + " videos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
