package com.example.projetorevisao.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CitizenDarkColorScheme = darkColorScheme(
    primary = AlertCritical,
    secondary = AlertTransit,
    tertiary = AlertWarning,
    background = CitizenBackground,
    surface = CitizenSurface,
    surfaceVariant = CitizenSurfaceElevated,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun CitizenSPTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CitizenDarkColorScheme,
        typography = Typography,
        content = content
    )
}