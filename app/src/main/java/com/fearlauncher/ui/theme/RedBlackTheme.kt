package com.fearlauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RedBlackColorScheme = darkColorScheme(
    primary = Color(0xFFE53935),
    secondary = Color(0xFFB71C1C),
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2D2D2D),
    error = Color(0xFFFF5252),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
)

@Composable
fun RedBlackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RedBlackColorScheme,
        typography = Typography(),
        content = content
    )
}
