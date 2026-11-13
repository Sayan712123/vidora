package com.vidora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.vidora.app.data.AppGraph
import com.vidora.app.data.Video
import com.vidora.app.ui.components.Pill
import com.vidora.app.ui.components.SectionHeader
import com.vidora.app.ui.components.VideoFeed
import com.vidora.app.ui.components.VideoTile
import com.vidora.app.ui.components.VidoraWordmark
import com.vidora.app.ui.icons.IconBookmark
import com.vidora.app.ui.icons.IconHistory
import com.vidora.app.ui.icons.IconSearch
import com.vidora.app.ui.icons.IconSettings

/** Region chips: the home feed is the "most popular" chart for the chosen region. */
private val regions = listOf(
    "IN" to "India",
    "US" to "United States",
    "GB" to "United Kingdom",
    "CA" to "Canada",
    "AU" to "Australia",
    "DE" to "Germany",
    "JP" to "Japan",
    "BR" to "Brazil"
)

@Composable
fun HomeScreen(
    onVideoClick: (Video) -> Unit,
    onChannelClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val store = AppGraph.store
    val repo = AppGraph.repo

    // The shelf is a second, small request; fetched once per region.
    var shelf by remember(store.region) { mutableStateOf<List<Video>>(emptyList()) }
    LaunchedEffect(store.region) {
        shelf = runCatching { repo.homeFeed().items.take(12) }.getOrDefault(emptyList())
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TopBar(
            onSearchClick = onSearchClick,
            onSettingsClick = onSettingsClick
        )

        VideoFeed(
            key = "home-" + store.region,
            fetch = { token -> repo.homeFeed(token) },
            onVideoClick = onVideoClick,
            onChannelClick = onChannelClick,
            emptyTitle = "Nothing to play yet",
            emptyMessage = "The most-popular chart for this region is empty right now.",
            header = {
                Column {
                    RegionRow(
                        selected = store.region,
                        onSelect = { store.setRegion(it) }
                    )
                    if (shelf.isNotEmpty()) {
                        SectionHeader(title = "Trending now")
                        LazyRow(
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 8.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(shelf, key = { "shelf-" + it.id }) { video ->
                                VideoTile(video = video, onClick = { onVideoClick(video) })
                            }
                        }
                        SectionHeader(title = "More for you")
                    }
                }
            }
        )
    }
}

@Composable
private fun TopBar(onSearchClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VidoraWordmark(modifier = Modifier.weight(1f))
        RoundIconButton(onClick = onSearchClick) {
            IconSearch(MaterialTheme.colorScheme.onBackground)
        }
        RoundIconButton(onClick = onSettingsClick) {
            IconSettings(MaterialTheme.colorScheme.onBackground)
        }
    }
}

/** A 40dp circular tap target - the minimum comfortable size on a phone. */
@Composable
fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun RegionRow(selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        items(regions, key = { it.first }) { (code, label) ->
            Pill(text = label, selected = code == selected, onClick = { onSelect(code) })
        }
    }
}

/** Quick-access row shown on the library screen. */
@Composable
fun QuickAccessRow(
    onHistoryClick: () -> Unit,
    onWatchLaterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickChip(modifier = Modifier.weight(1f), onClick = onHistoryClick) {
            IconHistory(MaterialTheme.colorScheme.primary, size = 18.dp)
            Spacer(Modifier.width(7.dp))
            Text("History", style = MaterialTheme.typography.labelLarge)
        }
        QuickChip(modifier = Modifier.weight(1f), onClick = onWatchLaterClick) {
            IconBookmark(MaterialTheme.colorScheme.primary, size = 18.dp)
            Spacer(Modifier.width(7.dp))
            Text("Watch later", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun QuickChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
