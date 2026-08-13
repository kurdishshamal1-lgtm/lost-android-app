package com.lost.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurpleAccent,
    secondary = PurpleGlow,
    background = Slate900,
    surface = Slate800,
    onPrimary = Color.White,
    onBackground = TextLight,
    onSurface = TextLight,
    surfaceVariant = Slate700
)

object LostAITheme {
    val colors: androidx.compose.material3.ColorScheme
        @Composable
        get() = MaterialTheme.colorScheme
}

@Composable
fun LostAITheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}