package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    secondary = NeonMagenta,
    onSecondary = Color.White,
    tertiary = NeonGreen,
    onTertiary = Color.Black,
    background = CyberBlack,
    onBackground = Color(0xFFE2E8F0),
    surface = CyberDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = CyberCardSurface,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = CyberBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Cyberpunk dark IDE aesthetic by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
