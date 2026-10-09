package com.example.projetorevisao.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CitizenLightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6ECFF),
    onPrimaryContainer = Color(0xFF14285F),
    secondary = BrandOrange,
    onSecondary = Color(0xFF271000),
    secondaryContainer = Color(0xFFFFEAD8),
    onSecondaryContainer = Color(0xFF713000),
    tertiary = Color(0xFF006B52),
    onTertiary = Color.White,
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF182238),
    surface = Color.White,
    onSurface = Color(0xFF182238),
    surfaceVariant = Color(0xFFEEF1F7),
    onSurfaceVariant = Color(0xFF526078),
    outline = Color(0xFF737F94),
    outlineVariant = Color(0xFFDCE2EC),
    error = Color(0xFFB42342),
    onError = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F9FC),
    surfaceContainer = Color(0xFFF0F3F8),
    surfaceContainerHigh = Color(0xFFE9EDF5),
    surfaceContainerHighest = Color(0xFFE2E7F0)
)

private val CitizenDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB7C8FF),
    onPrimary = Color(0xFF14285F),
    primaryContainer = BrandBlue,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFFB77D),
    onSecondary = Color(0xFF492000),
    secondaryContainer = Color(0xFF713000),
    onSecondaryContainer = Color(0xFFFFEAD8),
    tertiary = AccentEmeraldSafe,
    onTertiary = ObsidianBackground,
    background = ObsidianBackground,
    onBackground = TextExpressivePrimary,
    surface = SurfaceDarkCard,
    onSurface = TextExpressivePrimary,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = TextExpressiveSecondary,
    outline = Color(0xFF8591AA),
    outlineVariant = SurfaceBorderGlow,
    error = Color(0xFFFFB2C0),
    onError = Color(0xFF650023),
    surfaceContainerLowest = ObsidianBackground,
    surfaceContainerLow = SurfaceDarkHeader,
    surfaceContainer = SurfaceDarkCard,
    surfaceContainerHigh = SurfaceDarkElevated,
    surfaceContainerHighest = Color(0xFF2B3957)
)

@Composable
fun CitizenSPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) CitizenDarkColorScheme else CitizenLightColorScheme,
        typography = Typography,
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(24.dp)
        ),
        content = content
    )
}