package com.pedro.octoos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OctoColorScheme = darkColorScheme(
    primary = OctoLime,
    onPrimary = OctoBackground,
    secondary = OctoGray,
    onSecondary = OctoText,
    background = OctoBackground,
    onBackground = OctoText,
    surface = OctoTile,
    onSurface = OctoText,
    surfaceVariant = OctoTile,
    onSurfaceVariant = OctoGray,
    outline = OctoBorder,
    error = OctoDanger,
    onError = OctoBackground
)

@Composable
fun OctoOSTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OctoColorScheme,
        typography = OctoTypography,
        content = content
    )
}

