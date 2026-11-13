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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vidora.app.BuildConfig
import com.vidora.app.data.AppGraph
import com.vidora.app.ui.components.Pill
import com.vidora.app.ui.components.ScreenTopBar
import com.vidora.app.ui.icons.IconCheck
import com.vidora.app.ui.icons.IconMoon
import com.vidora.app.ui.icons.IconSun
import com.vidora.app.ui.icons.IconVideo

private val regionOptions = listOf(
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
fun SettingsScreen(onBack: () -> Unit) {
    val store = AppGraph.store
    var showKeyHelp by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTopBar(title = "Settings", onBack = onBack)

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // ------------------------------------------------------------- theme
            item { GroupLabel("Appearance") }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOption(
                        label = "Light",
                        selected = store.themeMode == "light",
                        modifier = Modifier.weight(1f)
                    ) { store.setTheme("light") }
                    ThemeOption(
                        label = "System",
                        selected = store.themeMode == "system",
                        modifier = Modifier.weight(1f)
                    ) { store.setTheme("system") }
                    ThemeOption(
                        label = "Dark",
                        selected = store.themeMode == "dark",
                        modifier = Modifier.weight(1f)
                    ) { store.setTheme("dark") }
                }
            }

            // ------------------------------------------------------------ region
            item { GroupLabel("Content region") }
            item {
                Text(
                    text = "The home feed shows the most-popular chart for this country.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(regionOptions, key = { it.first }) { (code, label) ->
                        Pill(
                            text = label,
                            selected = store.region == code,
                            onClick = { store.setRegion(code) }
                        )
                    }
                }
            }

            // --------------------------------------------------------- api status
            item { GroupLabel("YouTube Data API") }
            item {
                val configured = AppGraph.repo.hasApiKey()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { showKeyHelp = !showKeyHelp }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (configured) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (configured) {
                            IconCheck(MaterialTheme.colorScheme.primary, size = 18.dp)
                        } else {
                            Text(
                                text = "!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (configured) "API key configured" else "API key missing",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (configured) {
                                "Search, channels and playlists are live."
                            } else {
                                "Add YOUTUBE_API_KEY to local.properties, then rebuild."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (showKeyHelp) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "1. Create a key in Google Cloud Console and enable " +
                                "\"YouTube Data API v3\".",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "2. Put YOUTUBE_API_KEY=AIza... in local.properties at the " +
                                "project root (it is git-ignored).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "3. Rebuild the app. The key is read at build time into " +
                                "BuildConfig.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ------------------------------------------------------------ storage
            item { GroupLabel("Storage") }
            item {
                InfoRow(
                    title = "Watch history",
                    value = store.history.size.toString() + " videos",
                    actionLabel = "Clear",
                    onAction = { store.clearHistory() }
                )
            }
            item {
                InfoRow(
                    title = "Watch later",
                    value = store.watchLater.size.toString() + " videos",
                    actionLabel = "Clear",
                    onAction = { store.clearWatchLater() }
                )
            }
            item {
                InfoRow(
                    title = "Recent searches",
                    value = store.recentSearches.size.toString() + " terms",
                    actionLabel = "Clear",
                    onAction = { store.clearSearches() }
                )
            }

            // -------------------------------------------------------------- about
            item { GroupLabel("About") }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconVideo(MaterialTheme.colorScheme.primary, size = 20.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Vidora",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Version " + BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Vidora is an independent client. It uses the official YouTube " +
                            "Data API v3 for search and metadata and the official embedded " +
                            "player for playback. It is not affiliated with, endorsed by, or " +
                            "sponsored by YouTube or Google.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (label) {
            "Light" -> IconSun(
                tint = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 20.dp
            )
            "Dark" -> IconMoon(
                tint = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 20.dp
            )
            else -> IconVideo(
                tint = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 20.dp
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(
    title: String,
    value: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAction)
                .padding(8.dp)
        )
    }
}
