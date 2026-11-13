package com.vidora.app.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * A tiny hand-drawn icon set for Vidora.
 *
 * All marks are drawn on a 24x24 grid with rounded strokes, which keeps them crisp at any
 * density, avoids a second icon dependency, and - importantly - means every glyph in the
 * app is original to this project.
 */
private const val GRID = 24f

@Composable
private fun IconCanvas(
    size: Dp = 24.dp,
    tint: Color,
    painter: androidx.compose.ui.graphics.drawscope.DrawScope.(Float) -> Unit
) {
    Canvas(modifier = Modifier.size(size)) {
        val scale = this.size.minDimension / GRID
        painter(scale)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.strokePath(
    scale: Float,
    tint: Color,
    width: Float = 1.9f,
    block: Path.() -> Unit
) {
    val path = Path().apply { block() }
    drawPath(
        path = path,
        color = tint,
        style = Stroke(
            width = width * scale,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.fillPath(
    scale: Float,
    tint: Color,
    block: Path.() -> Unit
) {
    val path = Path().apply { block() }
    drawPath(path = path, color = tint)
}

@Composable
fun IconHome(tint: Color, filled: Boolean = false, size: Dp = 24.dp) =
    IconCanvas(size, tint) { s ->
        if (filled) {
            fillPath(s, tint) {
                moveTo(3f * s, 10.6f * s)
                lineTo(12f * s, 3.2f * s)
                lineTo(21f * s, 10.6f * s)
                lineTo(21f * s, 20f * s)
                lineTo(14.4f * s, 20f * s)
                lineTo(14.4f * s, 14.2f * s)
                lineTo(9.6f * s, 14.2f * s)
                lineTo(9.6f * s, 20f * s)
                lineTo(3f * s, 20f * s)
                close()
            }
        } else {
            strokePath(s, tint) {
                moveTo(3.4f * s, 10.4f * s)
                lineTo(12f * s, 3.4f * s)
                lineTo(20.6f * s, 10.4f * s)
                lineTo(20.6f * s, 20.2f * s)
                lineTo(14.2f * s, 20.2f * s)
                lineTo(14.2f * s, 13.8f * s)
                lineTo(9.8f * s, 13.8f * s)
                lineTo(9.8f * s, 20.2f * s)
                lineTo(3.4f * s, 20.2f * s)
                close()
            }
        }
    }

@Composable
fun IconSearch(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    strokePath(s, tint) {
        addOval(Rect(Offset(3.2f * s, 3.2f * s), Size(13.6f * s, 13.6f * s)))
        moveTo(15.6f * s, 15.6f * s)
        lineTo(20.8f * s, 20.8f * s)
    }
}

@Composable
fun IconSubscriptions(tint: Color, filled: Boolean = false, size: Dp = 24.dp) =
    IconCanvas(size, tint) { s ->
        strokePath(s, tint) {
            moveTo(3.2f * s, 6.4f * s)
            lineTo(14.4f * s, 6.4f * s)
            moveTo(3.2f * s, 11f * s)
            lineTo(14.4f * s, 11f * s)
            moveTo(3.2f * s, 15.6f * s)
            lineTo(9.4f * s, 15.6f * s)
        }
        if (filled) {
            fillPath(s, tint) {
                moveTo(15.4f * s, 13.2f * s)
                lineTo(21.8f * s, 16.8f * s)
                lineTo(15.4f * s, 20.4f * s)
                close()
            }
        } else {
            strokePath(s, tint) {
                moveTo(15.4f * s, 12.6f * s)
                lineTo(22f * s, 16.6f * s)
                lineTo(15.4f * s, 20.6f * s)
                close()
            }
        }
    }

@Composable
fun IconLibrary(tint: Color, filled: Boolean = false, size: Dp = 24.dp) =
    IconCanvas(size, tint) { s ->
        strokePath(s, tint) {
            moveTo(3.2f * s, 5.4f * s)
            lineTo(3.2f * s, 18.6f * s)
            moveTo(7.2f * s, 4.2f * s)
            lineTo(7.2f * s, 19.8f * s)
        }
        if (filled) {
            fillPath(s, tint) {
                moveTo(20.8f * s, 4.6f * s)
                lineTo(20.8f * s, 19.4f * s)
                lineTo(11.4f * s, 19.4f * s)
                lineTo(11.4f * s, 4.6f * s)
                close()
            }
        } else {
            strokePath(s, tint) {
                moveTo(11.4f * s, 4.8f * s)
                lineTo(20.8f * s, 4.8f * s)
                lineTo(20.8f * s, 19.2f * s)
                lineTo(11.4f * s, 19.2f * s)
                close()
            }
            strokePath(s, tint, width = 1.5f) {
                moveTo(14.6f * s, 9.6f * s)
                lineTo(18.2f * s, 12f * s)
                lineTo(14.6f * s, 14.4f * s)
                close()
            }
        }
    }

@Composable
fun IconPlay(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    fillPath(s, tint) {
        moveTo(7.4f * s, 4.8f * s)
        lineTo(19.4f * s, 12f * s)
        lineTo(7.4f * s, 19.2f * s)
        close()
    }
}

@Composable
fun IconBack(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    strokePath(s, tint, width = 2.1f) {
        moveTo(14.6f * s, 4.8f * s)
        lineTo(7.6f * s, 12f * s)
        lineTo(14.6f * s, 19.2f * s)
    }
}

@Composable
fun IconClose(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    strokePath(s, tint, width = 2f) {
        moveTo(5.4f * s, 5.4f * s)
        lineTo(18.6f * s, 18.6f * s)
        moveTo(18.6f * s, 5.4f * s)
        lineTo(5.4f * s, 18.6f * s)
    }
}

@Composable
fun IconMore(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    listOf(5.6f, 12f, 18.4f).forEach { y ->
        drawCircle(color = tint, radius = 1.7f * s, center = Offset(12f * s, y * s))
    }
}

@Composable
fun IconCheck(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    strokePath(s, tint, width = 2.1f) {
        moveTo(4.8f * s, 12.6f * s)
        lineTo(9.8f * s, 17.6f * s)
        lineTo(19.4f * s, 6.6f * s)
    }
}

@Composable
fun IconPlus(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    strokePath(s, tint, width = 2f) {
        moveTo(12f * s, 4.6f * s)
        lineTo(12f * s, 19.4f * s)
        moveTo(4.6f * s, 12f * s)
        lineTo(19.4f * s, 12f * s)
    }
}

@Composable
fun IconBookmark(tint: Color, filled: Boolean = false, size: Dp = 24.dp) =
    IconCanvas(size, tint) { s ->
        if (filled) {
            fillPath(s, tint) {
                moveTo(6.2f * s, 3.6f * s)
                lineTo(17.8f * s, 3.6f * s)
                lineTo(17.8f * s, 20.4f * s)
                lineTo(12f * s, 15.6f * s)
                lineTo(6.2f * s, 20.4f * s)
                close()
            }
        } else {
            strokePath(s, tint) {
                moveTo(6.2f * s, 3.8f * s)
                lineTo(17.8f * s, 3.8f * s)
                lineTo(17.8f * s, 20.2f * s)
                lineTo(12f * s, 15.4f * s)
                lineTo(6.2f * s, 20.2f * s)
                close()
            }
        }
    }

@Composable
fun IconHistory(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    strokePath(s, tint) {
        addOval(Rect(Offset(3.6f * s, 3.6f * s), Size(16.8f * s, 16.8f * s)))
        moveTo(12f * s, 7.4f * s)
        lineTo(12f * s, 12.4f * s)
        lineTo(15.8f * s, 14.6f * s)
    }
}

@Composable
fun IconSettings(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    strokePath(s, tint) {
        addOval(Rect(Offset(8.9f * s, 8.9f * s), Size(6.2f * s, 6.2f * s)))
        addOval(Rect(Offset(4.4f * s, 4.4f * s), Size(15.2f * s, 15.2f * s)))
    }
    strokePath(s, tint, width = 1.7f) {
        (0 until 8).forEach { i ->
            val angle = Math.toRadians(i * 45.0)
            val cx = 12f * s
            val cy = 12f * s
            val r1 = 7.6f * s
            val r2 = 10.2f * s
            val cosA = cos(angle).toFloat()
            val sinA = sin(angle).toFloat()
            moveTo(cx + r1 * cosA, cy + r1 * sinA)
            lineTo(cx + r2 * cosA, cy + r2 * sinA)
        }
    }
}

@Composable
fun IconDelete(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    strokePath(s, tint) {
        moveTo(4.4f * s, 6.6f * s)
        lineTo(19.6f * s, 6.6f * s)
        moveTo(9.4f * s, 6.4f * s)
        lineTo(9.4f * s, 4.2f * s)
        lineTo(14.6f * s, 4.2f * s)
        lineTo(14.6f * s, 6.4f * s)
        moveTo(6.6f * s, 6.8f * s)
        lineTo(7.6f * s, 20f * s)
        lineTo(16.4f * s, 20f * s)
        lineTo(17.4f * s, 6.8f * s)
    }
}

@Composable
fun IconShare(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    strokePath(s, tint) {
        addOval(Rect(Offset(15.4f * s, 3.4f * s), Size(5.2f * s, 5.2f * s)))
        addOval(Rect(Offset(3.4f * s, 9.4f * s), Size(5.2f * s, 5.2f * s)))
        addOval(Rect(Offset(15.4f * s, 15.4f * s), Size(5.2f * s, 5.2f * s)))
        moveTo(8.4f * s, 11.2f * s)
        lineTo(15.6f * s, 7.2f * s)
        moveTo(8.4f * s, 12.8f * s)
        lineTo(15.6f * s, 16.8f * s)
    }
}

@Composable
fun IconSun(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    strokePath(s, tint) {
        addOval(Rect(Offset(8.2f * s, 8.2f * s), Size(7.6f * s, 7.6f * s)))
    }
    strokePath(s, tint, width = 1.7f) {
        (0 until 8).forEach { i ->
            val angle = Math.toRadians(i * 45.0)
            val cx = 12f * s
            val cy = 12f * s
            val cosA = cos(angle).toFloat()
            val sinA = sin(angle).toFloat()
            moveTo(cx + 8.6f * s * cosA, cy + 8.6f * s * sinA)
            lineTo(cx + 10.8f * s * cosA, cy + 10.8f * s * sinA)
        }
    }
}

@Composable
fun IconMoon(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    strokePath(s, tint) {
        moveTo(20f * s, 14.6f * s)
        curveTo(17.6f * s, 16.4f * s, 13.4f * s, 16.2f * s, 10.4f * s, 13.2f * s)
        curveTo(7.4f * s, 10.2f * s, 7.2f * s, 6f * s, 9f * s, 3.6f * s)
        curveTo(4.6f * s, 4.6f * s, 2f * s, 8.6f * s, 3.2f * s, 13.4f * s)
        curveTo(4.6f * s, 18.8f * s, 10.4f * s, 21.6f * s, 15.6f * s, 19.8f * s)
        curveTo(17.4f * s, 19.2f * s, 19f * s, 18.2f * s, 20f * s, 14.6f * s)
        close()
    }
}

@Composable
fun IconVideo(tint: Color, size: Dp = 24.dp) = IconCanvas(size, tint) { s ->
    strokePath(s, tint) {
        moveTo(3.2f * s, 6.2f * s)
        lineTo(14.2f * s, 6.2f * s)
        lineTo(14.2f * s, 17.8f * s)
        lineTo(3.2f * s, 17.8f * s)
        close()
        moveTo(14.2f * s, 10.8f * s)
        lineTo(20.8f * s, 7.4f * s)
        lineTo(20.8f * s, 16.6f * s)
        lineTo(14.2f * s, 13.2f * s)
        close()
    }
}
