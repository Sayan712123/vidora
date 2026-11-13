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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vidora.app.data.AppGraph
import com.vidora.app.data.Channel
import com.vidora.app.data.Video
import com.vidora.app.ui.components.ChannelAvatar
import com.vidora.app.ui.components.EmptyState
import com.vidora.app.ui.components.Pill
import com.vidora.app.ui.components.VideoFeed
import com.vidora.app.ui.icons.IconClose
import com.vidora.app.ui.icons.IconSearch
import com.vidora.app.util.subscriberLabel

private val suggestions = listOf(
    "Music", "Live", "Gaming", "News", "Podcasts", "Cooking",
    "Lo-fi beats", "Highlights", "Documentary", "Tech reviews"
)

@Composable
fun SearchScreen(
    onVideoClick: (Video) -> Unit,
    onChannelClick: (String) -> Unit
) {
    val store = AppGraph.store
    val repo = AppGraph.repo

    var query by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(0) } // 0 = videos, 1 = channels
    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var channelsLoading by remember { mutableStateOf(false) }
    var channelsError by remember { mutableStateOf<String?>(null) }
    var channelReload by remember { mutableStateOf(0) }

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    fun runSearch(term: String) {
        val clean = term.trim()
        if (clean.isBlank()) return
        query = clean
        submitted = clean
        store.addSearch(clean)
        keyboard?.hide()
    }

    LaunchedEffect(submitted, tab, channelReload) {
        if (submitted.isBlank() || tab != 1) return@LaunchedEffect
        channelsLoading = true
        channelsError = null
        try {
            channels = repo.searchChannels(submitted)
        } catch (t: Throwable) {
            channelsError = t.message ?: "Could not search channels."
        } finally {
            channelsLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ------------------------------------------------------------- search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconSearch(MaterialTheme.colorScheme.onSurfaceVariant, size = 19.dp)
                Spacer(Modifier.width(9.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search videos and channels",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { runSearch(query) }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }
                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable {
                                query = ""
                                submitted = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        IconClose(MaterialTheme.colorScheme.onSurfaceVariant, size = 15.dp)
                    }
                }
            }
            if (query.isNotBlank()) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Go",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { runSearch(query) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }

        if (submitted.isBlank()) {
            // ------------------------------------------------------- discovery view
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (store.recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recent",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Clear",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { store.clearSearches() }
                                    .padding(6.dp)
                            )
                        }
                    }
                    items(store.recentSearches, key = { "recent-" + it }) { term ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { runSearch(term) }
                                .padding(horizontal = 16.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconSearch(MaterialTheme.colorScheme.onSurfaceVariant, size = 17.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = term,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .clickable { store.removeSearch(term) },
                                contentAlignment = Alignment.Center
                            ) {
                                IconClose(MaterialTheme.colorScheme.onSurfaceVariant, size = 13.dp)
                            }
                        }
                    }
                }
                item {
                    Text(
                        "Explore",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 10.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(suggestions) { term ->
                            Pill(text = term, selected = false) { runSearch(term) }
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        } else {
            // ------------------------------------------------------------ results
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Pill(text = "Videos", selected = tab == 0) { tab = 0 }
                Pill(text = "Channels", selected = tab == 1) { tab = 1 }
            }

            if (tab == 0) {
                VideoFeed(
                    key = "search-" + submitted,
                    fetch = { token -> repo.search(submitted, token) },
                    onVideoClick = onVideoClick,
                    onChannelClick = onChannelClick,
                    compact = true,
                    emptyTitle = "No videos found",
                    emptyMessage = "Nothing matched \"" + submitted + "\". Try different words."
                )
            } else {
                ChannelResults(
                    channels = channels,
                    loading = channelsLoading,
                    error = channelsError,
                    onRetry = { channelReload++ },
                    onChannelClick = onChannelClick
                )
            }
        }
    }
}

@Composable
private fun ChannelResults(
    channels: List<Channel>,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onChannelClick: (String) -> Unit
) {
    when {
        loading -> Column(modifier = Modifier.fillMaxSize()) {
            repeat(5) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    com.vidora.app.ui.components.SkeletonBlock(Modifier.size(52.dp), radius = 26)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        com.vidora.app.ui.components.SkeletonBlock(
                            Modifier.fillMaxWidth(0.6f).height(13.dp), radius = 4
                        )
                        Spacer(Modifier.height(7.dp))
                        com.vidora.app.ui.components.SkeletonBlock(
                            Modifier.fillMaxWidth(0.35f).height(11.dp), radius = 4
                        )
                    }
                }
            }
        }

        error != null -> com.vidora.app.ui.components.ErrorBlock(
            message = error.orEmpty(),
            onRetry = onRetry
        )

        channels.isEmpty() -> EmptyState(
            title = "No channels found",
            message = "Try a different name or spelling."
        )

        else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(channels, key = { it.id }) { channel ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChannelClick(channel.id) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChannelAvatar(url = channel.avatar, name = channel.title, size = 52)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = channel.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subscriberLabel(channel.subscribers),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (channel.description.isNotBlank()) {
                            Text(
                                text = channel.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
