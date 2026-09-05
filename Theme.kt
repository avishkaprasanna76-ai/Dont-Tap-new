package com.donttap.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BackgroundDark = Color(0xFF0D0D12)
val SurfaceDark = Color(0xFF16161E)
val AccentYellow = Color(0xFFFFD23F)
val AccentRed = Color(0xFFFF4757)
val TextPrimary = Color(0xFFF5F5F7)
val TextSecondary = Color(0xFF9A9AA5)

private val DarkColors = darkColorScheme(
    background = BackgroundDark,
    surface = SurfaceDark,
    primary = AccentYellow,
    secondary = AccentRed,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = Color(0xFF1A1A1A)
)

@Composable
fun DontTapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = DontTapTypography,
        content = content
    )
}
