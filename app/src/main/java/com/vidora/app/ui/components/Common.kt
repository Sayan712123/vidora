package com.vidora.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vidora.app.data.Video
import com.vidora.app.util.initialOf
import com.vidora.app.util.relativeTime
import com.vidora.app.util.viewLabel

/* ---------------------------------------------------------------- thumbnails */

@Composable
fun Thumbnail(
    url: String,
    modifier: Modifier = Modifier,
    duration: String = "",
    radius: Int = 12,
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (url.isNotBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (duration.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.78f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ChannelAvatar(url: String, name: String, size: Int = 36) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (url.isBlank()) {
            Text(
                text = initialOf(name),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/* ------------------------------------------------------------------- cards */

/** The big card used by the home feed: full-width 16:9 still, then the meta rows. */
@Composable
fun VideoCard(
    video: Video,
    onClick: () -> Unit,
    onMoreClick: (Video) -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = tween(120),
        label = "cardScale"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Thumbnail(
            url = video.thumbnail,
            duration = video.duration,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )
        Row(modifier = Modifier.padding(top = 10.dp)) {
            ChannelAvatar(
                url = "",
                name = video.channelTitle,
                size = 34
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = buildString {
                        append(video.channelTitle)
                        val views = viewLabel(video.views)
                        if (views.isNotBlank()) append(" \u00B7 ").append(views)
                        val when_ = relativeTime(video.publishedAt)
                        if (when_.isNotBlank()) append(" \u00B7 ").append(when_)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable { onMoreClick(video) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u22EE",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** The compact row used by search, channel, playlist and library screens. */
@Composable
fun VideoRow(
    video: Video,
    onClick: () -> Unit,
    onChannelClick: (String) -> Unit = {},
    onMoreClick: ((Video) -> Unit)? = null,
    showDescription: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Thumbnail(
            url = video.thumbnail,
            duration = video.duration,
            modifier = Modifier
                .width(148.dp)
                .aspectRatio(16f / 9f),
            radius = 10
        )
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = video.channelTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(enabled = video.channelId.isNotBlank()) {
                    onChannelClick(video.channelId)
                }
            )
            Text(
                text = buildString {
                    val views = viewLabel(video.views)
                    if (views.isNotBlank()) append(views)
                    val when_ = relativeTime(video.publishedAt)
                    if (when_.isNotBlank()) {
                        if (isNotEmpty()) append(" \u00B7 ")
                        append(when_)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            if (showDescription && video.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = video.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (onMoreClick != null) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onMoreClick(video) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u22EE",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Horizontal shelf card, used for "Continue watching" style rows. */
@Composable
fun VideoTile(video: Video, onClick: () -> Unit, width: Int = 190) {
    Column(
        modifier = Modifier
            .width(width.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Thumbnail(
            url = video.thumbnail,
            duration = video.duration,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            radius = 10
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = video.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = video.channelTitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ------------------------------------------------------------------ chrome */

@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (action != null && onAction != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun Pill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(50),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

/** Neutral placeholder that keeps the layout from collapsing while data loads. */
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier, radius: Int = 10) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    )
}

@Composable
fun VideoSkeleton() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )
        Spacer(Modifier.height(10.dp))
        Row {
            SkeletonBlock(Modifier.size(34.dp), radius = 17)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                SkeletonBlock(Modifier.fillMaxWidth(0.85f).height(13.dp), radius = 4)
                Spacer(Modifier.height(6.dp))
                SkeletonBlock(Modifier.fillMaxWidth(0.45f).height(11.dp), radius = 4)
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    icon: String = "\u25B6",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/** Thin brand wordmark: a small play glyph plus "Vidora" - no third-party marks. */
@Composable
fun VidoraWordmark(modifier: Modifier = Modifier, dim: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFF6B4A), Color(0xFFFFB347))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\u25B6",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF2A0F08)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Vidora",
            style = MaterialTheme.typography.headlineSmall,
            color = if (dim) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun Dimmed(enabled: Boolean, content: @Composable () -> Unit) {
    if (enabled) {
        Box(modifier = Modifier.alpha(0.55f)) { content() }
    } else {
        content()
    }
}
