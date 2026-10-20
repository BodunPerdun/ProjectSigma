package com.example.projectsigma.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF42A5F5),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF153359),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color.Black,
    surface = Color(0xFF121212),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFF0F0F0),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    error = Color(0xFFEF5350)
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF1976D2),
    secondary = Color(0xFF1976D2),
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFF8F9FA),
    onSurfaceVariant = Color(0xFF424242),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF121212),
    error = Color(0xFFE53935)
)

@Composable
fun LocaPopTheme(
    content: @Composable () -> Unit
) {
    val isDark by AppThemeManager.isDarkTheme.collectAsState()
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
