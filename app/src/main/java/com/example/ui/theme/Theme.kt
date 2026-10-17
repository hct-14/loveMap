package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = LovePrimary,
    onPrimary = Color.White,
    primaryContainer = LoveSecondary,
    onPrimaryContainer = LoveTextPrimary,
    secondary = LoveSecondary,
    onSecondary = LoveTextPrimary,
    secondaryContainer = LoveSecondaryContainer,
    onSecondaryContainer = LoveTextPrimary,
    background = LoveBackground,
    onBackground = LoveTextPrimary,
    surface = LoveSurface,
    onSurface = LoveTextPrimary,
    surfaceVariant = LoveSecondaryContainer,
    onSurfaceVariant = LoveTextSecondary,
    outline = LoveCardBorder
)

@Composable
fun LoveMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
