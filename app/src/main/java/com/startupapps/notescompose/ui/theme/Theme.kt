package com.startupapps.notescompose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Professional Slate & Indigo Palette
val Slate900 = Color(0xFF0F172A) // Dark background
val Slate800 = Color(0xFF1E293B) // Dark surface
val Slate700 = Color(0xFF334155) // Surface variant
val Slate100 = Color(0xFFF1F5F9) // Light background
val Indigo600 = Color(0xFF4F46E5) // Primary Action
val Indigo700 = Color(0xFF4338CA) // Primary Dark
val Sky500 = Color(0xFF0EA5E9)   // Secondary
val Rose500 = Color(0xFFF43F5E)  // Error/Accent

private val DarkColorScheme = darkColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    secondary = Sky500,
    onSecondary = Color.White,
    tertiary = Rose500,
    background = Slate900,
    onBackground = Slate100,
    surface = Slate800,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    outline = Slate700
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    secondary = Sky500,
    onSecondary = Color.White,
    tertiary = Rose500,
    background = Color.White,
    onBackground = Slate900,
    surface = Slate100,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Slate800,
    outline = Color(0xFFCBD5E1)
)

@Composable
fun NotesComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Отключаем динамические цвета для сохранения стиля
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
