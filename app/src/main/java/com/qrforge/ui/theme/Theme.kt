package com.qrforge.ui.theme

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class QRForgeColors(
    val background0: Color,
    val background1: Color,
    val background2: Color,
    val background3: Color,
    val surfaceBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentMuted: Color,
    val accentSurface: Color,
    val error: Color,
    val success: Color,
    val warning: Color
)

val QRForgeDarkColors = QRForgeColors(
    background0 = Background0,
    background1 = Background1,
    background2 = Background2,
    background3 = Background3,
    surfaceBorder = SurfaceBorder,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textTertiary = TextTertiary,
    accent = Accent,
    accentMuted = AccentMuted,
    accentSurface = AccentSurface,
    error = Error,
    success = Success,
    warning = Warning
)

val LocalQRForgeColors = staticCompositionLocalOf { QRForgeDarkColors }

object QRForgeTheme {
    val colors: QRForgeColors
        @Composable get() = LocalQRForgeColors.current

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(18.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(32.dp)
    )
}

private val DarkColorScheme = darkColorScheme(
    background = Background0,
    surface = Background1,
    surfaceVariant = Background2,
    primary = Accent,
    onPrimary = Background0,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = Error
)

@Composable
fun QRForgeTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalQRForgeColors provides QRForgeDarkColors
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = QRForgeTypography,
            shapes = QRForgeTheme.shapes,
            content = content
        )
    }
}
