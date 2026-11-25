package com.example.projetorevisao.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Guia visual v1.0 §2.3 — neutros quentes. Sem azul no fundo, sem preto puro.

private val RcLightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE1FF),
    onPrimaryContainer = Color(0xFF00164D),
    inversePrimary = Color(0xFFB5C4FF),
    secondary = BrandNavyDark,
    onSecondary = Color.White,
    secondaryContainer = PaperContainerHigh,
    onSecondaryContainer = InkPrimary,
    tertiary = BrandOrange,
    onTertiary = BrandNavyDark,
    tertiaryContainer = Color(0xFFFFDBCC),
    onTertiaryContainer = Color(0xFF351000),
    background = PaperBackground,
    onBackground = InkPrimary,
    surface = PaperSurface,
    onSurface = InkPrimary,
    surfaceVariant = PaperContainer,
    onSurfaceVariant = InkSecondary,
    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFF32302D),
    inverseOnSurface = Color(0xFFF6F4EF),
    outline = PaperOutline,
    outlineVariant = PaperLine,
    error = LightExtendedColors.critical,
    onError = Color.White,
    errorContainer = LightExtendedColors.criticalContainer,
    onErrorContainer = Color(0xFF410003),
    scrim = Color.Black,
    surfaceBright = PaperSurface,
    surfaceDim = PaperContainerHigh,
    surfaceContainerLowest = PaperSurface,
    surfaceContainerLow = Color(0xFFFAF8F4),
    surfaceContainer = PaperContainer,
    surfaceContainerHigh = PaperContainerHigh,
    surfaceContainerHighest = PaperContainerHighest
)

private val RcDarkColorScheme = darkColorScheme(
    primary = Color(0xFFAFC1FF),
    onPrimary = BrandNavyDark,
    primaryContainer = Color(0xFF244191),
    onPrimaryContainer = Color(0xFFDBE1FF),
    inversePrimary = BrandBlue,
    secondary = Color(0xFFD2CCC2),
    onSecondary = Color(0xFF2A2824),
    secondaryContainer = NightContainerHigh,
    onSecondaryContainer = NightInkPrimary,
    tertiary = Color(0xFFFF7A1A),
    onTertiary = BrandNavyDark,
    tertiaryContainer = Color(0xFF7A3000),
    onTertiaryContainer = Color(0xFFFFDBCC),
    background = NightBackground,
    onBackground = NightInkPrimary,
    surface = NightSurface,
    onSurface = NightInkPrimary,
    surfaceVariant = NightContainer,
    onSurfaceVariant = NightInkSecondary,
    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFFEDEAE4),
    inverseOnSurface = Color(0xFF1D1C1A),
    outline = NightOutline,
    outlineVariant = NightLine,
    error = DarkExtendedColors.critical,
    onError = Color(0xFF690007),
    errorContainer = DarkExtendedColors.criticalContainer,
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color.Black,
    surfaceBright = NightContainerHighest,
    surfaceDim = NightBackground,
    surfaceContainerLowest = Color(0xFF0E0D0C),
    surfaceContainerLow = Color(0xFF171614),
    surfaceContainer = NightContainer,
    surfaceContainerHigh = NightContainerHigh,
    surfaceContainerHighest = NightContainerHighest
)

/** Guia v1.0 §4: 8 chip/selo · 14 card/botão/campo · 24 topo de sheet. */
val RcShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Grade de 4 dp (guia v1.0 §4). */
object RcSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val minTouch = 48.dp
}

@Composable
fun RcAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalRcColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) RcDarkColorScheme else RcLightColorScheme,
            typography = Typography,
            shapes = RcShapes,
            content = content
        )
    }
}

/** Nome antigo, mantido para as telas e previews existentes. */
@Composable
fun CitizenSPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = RcAppTheme(darkTheme = darkTheme, content = content)
