package com.example.projetorevisao.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CitizenDarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = ObsidianBackground,
    secondary = AccentRoseCritical,
    onSecondary = TextExpressivePrimary,
    tertiary = AccentAmberWarning,
    onTertiary = ObsidianBackground,
    background = ObsidianBackground,
    onBackground = TextExpressivePrimary,
    surface = SurfaceDarkCard,
    onSurface = TextExpressivePrimary,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = TextExpressiveSecondary,
    outline = SurfaceBorderGlow
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