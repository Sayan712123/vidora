package com.vidora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vidora.app.data.Video
import com.vidora.app.data.VideoPage

/**
 * A paged, error-tolerant list of videos.
 *
 * One instance handles: first load, pulling the next page when the user nears the end,
 * a retry button on failure, and the empty state. Reused by every list screen so the
 * paging logic exists exactly once.
 */
@Composable
fun VideoFeed(
    key: Any?,
    fetch: suspend (String?) -> VideoPage,
    onVideoClick: (Video) -> Unit,
    onChannelClick: (String) -> Unit = {},
    onMoreClick: ((Video) -> Unit)? = null,
    emptyTitle: String = "Nothing here yet",
    emptyMessage: String = "Try a different search or check back in a moment.",
    header: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    compact: Boolean = false
) {
    var videos by remember(key) { mutableStateOf<List<Video>>(emptyList()) }
    var nextPage by remember(key) { mutableStateOf<String?>(null) }
    var loading by remember(key) { mutableStateOf(true) }
    var appending by remember(key) { mutableStateOf(false) }
    var error by remember(key) { mutableStateOf<String?>(null) }
    var exhausted by remember(key) { mutableStateOf(false) }
    var reloadToken by remember(key) { mutableStateOf(0) }

    LaunchedEffect(key, reloadToken) {
        loading = true
        error = null
        exhausted = false
        videos = emptyList()
        try {
            val page = fetch(null)
            videos = page.items
            nextPage = page.nextPageToken
            exhausted = page.nextPageToken == null
        } catch (t: Throwable) {
            error = t.message ?: "Could not load videos."
        } finally {
            loading = false
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(listState, key) {
        snapshotFlow {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last to listState.layoutInfo.totalItemsCount
        }.collect { (last, total) ->
            val shouldLoad = total > 0 && last >= total - 3
            val token = nextPage
            if (shouldLoad && token != null && !appending && !loading && error == null) {
                appending = true
                try {
                    val page = fetch(token)
                    val existing = videos.map { it.id }.toHashSet()
                    videos = videos + page.items.filter { existing.add(it.id) }
                    nextPage = page.nextPageToken
                    exhausted = page.nextPageToken == null
                } catch (t: Throwable) {
                    error = t.message ?: "Could not load more videos."
                } finally {
                    appending = false
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading -> Column(modifier = Modifier.fillMaxSize()) {
                leading?.invoke()
                repeat(4) { VideoSkeleton() }
            }

            error != null && videos.isEmpty() -> Column(modifier = Modifier.fillMaxSize()) {
                leading?.invoke()
                ErrorBlock(
                    message = error.orEmpty(),
                    onRetry = { reloadToken++ }
                )
            }

            videos.isEmpty() -> Column(modifier = Modifier.fillMaxSize()) {
                leading?.invoke()
                header?.invoke()
                EmptyState(title = emptyTitle, message = emptyMessage)
            }

            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                leading?.let { item { it() } }
                header?.let { item { it() } }
                items(videos, key = { it.id }) { video ->
                    if (compact) {
                        VideoRow(
                            video = video,
                            onClick = { onVideoClick(video) },
                            onChannelClick = onChannelClick,
                            onMoreClick = onMoreClick?.let { more -> { v: Video -> more(v) } }
                        )
                    } else {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            VideoCard(
                                video = video,
                                onClick = { onVideoClick(video) },
                                onMoreClick = { more -> onMoreClick?.invoke(more) }
                            )
                        }
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            appending -> CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                            error != null -> TextButton(onClick = { reloadToken++ }) {
                                Text("Couldn't load more \u00B7 Retry")
                            }
                            exhausted && videos.size > 6 -> Text(
                                text = "You're all caught up",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorBlock(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Can't load right now",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onRetry) { Text("Try again") }
    }
}
