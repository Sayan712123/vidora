package com.vidora.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// Brand palette: deep indigo night with a coral accent.
val Coral = Color(0xFFFF6B4A)
val CoralDeep = Color(0xFFD9442A)
val Amber = Color(0xFFFFB347)
val Violet = Color(0xFF8E7CFF)

private val DarkColors = darkColorScheme(
    primary = Coral,
    onPrimary = Color(0xFF2A0F08),
    primaryContainer = Color(0xFF3A1B14),
    onPrimaryContainer = Color(0xFFFFD9CE),
    secondary = Violet,
    onSecondary = Color(0xFF170F35),
    secondaryContainer = Color(0xFF2A2450),
    onSecondaryContainer = Color(0xFFDED8FF),
    background = Color(0xFF0E0F18),
    onBackground = Color(0xFFECEBF4),
    surface = Color(0xFF14151F),
    onSurface = Color(0xFFECEBF4),
    surfaceVariant = Color(0xFF1E2030),
    onSurfaceVariant = Color(0xFFA5A7BC),
    surfaceContainer = Color(0xFF171926),
    surfaceContainerHigh = Color(0xFF1C1E2C),
    outline = Color(0xFF2C2F44),
    outlineVariant = Color(0xFF242739),
    error = Color(0xFFFF6F6F),
    onError = Color(0xFF2A0505)
)

private val LightColors = lightColorScheme(
    primary = CoralDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4DC),
    onPrimaryContainer = Color(0xFF3D0E04),
    secondary = Color(0xFF5B45D6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6E1FF),
    onSecondaryContainer = Color(0xFF1C1147),
    background = Color(0xFFFBFAFE),
    onBackground = Color(0xFF14151F),
    surface = Color.White,
    onSurface = Color(0xFF14151F),
    surfaceVariant = Color(0xFFF1F0F7),
    onSurfaceVariant = Color(0xFF5B5D73),
    surfaceContainer = Color(0xFFF6F5FB),
    surfaceContainerHigh = Color(0xFFEFEEF7),
    outline = Color(0xFFDDDBE8),
    outlineVariant = Color(0xFFEAE8F2),
    error = Color(0xFFB3261E),
    onError = Color.White
)

private val VidoraType = Typography(
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.5.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun VidoraTheme(mode: String, content: @Composable () -> Unit) {
    val dark = when (mode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !dark
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightNavigationBars = !dark
        }
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = VidoraType,
        content = content
    )
}

@Composable
fun isDark(mode: String): Boolean = when (mode) {
    "dark" -> true
    "light" -> false
    else -> isSystemInDarkTheme()
}
