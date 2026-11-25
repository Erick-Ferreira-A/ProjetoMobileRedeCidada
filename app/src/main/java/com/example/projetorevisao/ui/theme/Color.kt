package com.example.projetorevisao.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

// Guia visual v1.0 (docs/design/GUIA_VISUAL.md). Contraste de cada par medido pela WCAG 2.x.

// ── Marca ────────────────────────────────────────────────────────────────
val BrandNavyDark = Color(0xFF0A2540)
val BrandBlue = Color(0xFF1D3B8B)
val BrandBlueHero = Color(0xFF1E3A8A)
val BrandOrange = Color(0xFFFF6B00)
val BrandOrangeLight = Color(0xFFFF8533)

// ── Neutros quentes ──────────────────────────────────────────────────────
// Claro: off-white de papel no fundo, branco só nos cards.
internal val PaperBackground = Color(0xFFF6F4EF)
internal val PaperSurface = Color(0xFFFFFFFF)
internal val PaperContainer = Color(0xFFEEEBE5)
internal val PaperContainerHigh = Color(0xFFE6E2DA)
internal val PaperContainerHighest = Color(0xFFDDD8CF)
internal val InkPrimary = Color(0xFF1D1C1A)
internal val InkSecondary = Color(0xFF5C574F)
internal val PaperOutline = Color(0xFF7D776D)
internal val PaperLine = Color(0xFFDDD8CF)

// Escuro: cinzas com traço de amarelo-terra (sem azul, sem preto puro).
internal val NightBackground = Color(0xFF121110)
internal val NightSurface = Color(0xFF1B1A18)
internal val NightContainer = Color(0xFF252321)
internal val NightContainerHigh = Color(0xFF2F2D2A)
internal val NightContainerHighest = Color(0xFF393632)
internal val NightInkPrimary = Color(0xFFEDEAE4)
internal val NightInkSecondary = Color(0xFFB3ADA3)
internal val NightOutline = Color(0xFF8A847A)
internal val NightLine = Color(0xFF36332F)

/** Cores além do esquema do Material: acento da marca e cores de função. */
@Immutable
data class RcExtendedColors(
    val accent: Color,
    val onAccent: Color,
    /** Laranja usado como texto. No claro, só sobre background/surface. */
    val accentText: Color,
    val critical: Color,
    val criticalContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val safe: Color,
    val safeContainer: Color,
    val info: Color,
    val infoContainer: Color
)

internal val LightExtendedColors = RcExtendedColors(
    accent = BrandOrange,
    onAccent = BrandNavyDark,          // 5,44:1 — branco sobre o laranja daria 2,86:1
    accentText = Color(0xFFB44900),
    critical = Color(0xFFB91D20),
    criticalContainer = Color(0xFFFFDAD6),
    warning = Color(0xFF8A5100),
    warningContainer = Color(0xFFFFDCBD),
    safe = Color(0xFF1B6D24),
    safeContainer = Color(0xFFC9F2C2),
    info = Color(0xFF005DB7),
    infoContainer = Color(0xFFD6E3FF)
)

private fun nightContainer(color: Color) = color.copy(alpha = 0.16f).compositeOver(NightSurface)

internal val DarkExtendedColors = RcExtendedColors(
    accent = Color(0xFFFF7A1A),
    onAccent = BrandNavyDark,          // 5,96:1
    accentText = Color(0xFFFF9A55),
    critical = Color(0xFFFFB4AC),
    criticalContainer = nightContainer(Color(0xFFFFB4AC)),
    warning = Color(0xFFFFB86E),
    warningContainer = nightContainer(Color(0xFFFFB86E)),
    safe = Color(0xFF88D982),
    safeContainer = nightContainer(Color(0xFF88D982)),
    info = Color(0xFFA9C7FF),
    infoContainer = nightContainer(Color(0xFFA9C7FF))
)

val LocalRcColors = staticCompositionLocalOf { LightExtendedColors }

/** Acesso às cores estendidas: `RcTheme.colors.accent`. */
object RcTheme {
    val colors: RcExtendedColors
        @Composable @ReadOnlyComposable
        get() = LocalRcColors.current
}

/** Função de uma informação. A cor vem do tema; nunca escolha cor direto para um alerta. */
enum class RcTone(internal val lightColor: Color) {
    CRITICAL(LightExtendedColors.critical),
    WARNING(LightExtendedColors.warning),
    SAFE(LightExtendedColors.safe),
    INFO(LightExtendedColors.info);

    val color: Color
        @Composable @ReadOnlyComposable
        get() = when (this) {
            CRITICAL -> RcTheme.colors.critical
            WARNING -> RcTheme.colors.warning
            SAFE -> RcTheme.colors.safe
            INFO -> RcTheme.colors.info
        }

    val container: Color
        @Composable @ReadOnlyComposable
        get() = when (this) {
            CRITICAL -> RcTheme.colors.criticalContainer
            WARNING -> RcTheme.colors.warningContainer
            SAFE -> RcTheme.colors.safeContainer
            INFO -> RcTheme.colors.infoContainer
        }
}

// ── Compatibilidade com as telas que ainda não migraram ──────────────────
// As constantes abaixo apontam para as cores de função novas. Não use em código novo:
// prefira RcTheme.colors e MaterialTheme.colorScheme.
val AccentRoseCritical = Color(0xFFB91D20)
val AccentAmberWarning = Color(0xFF8A5100)
val AccentEmeraldSafe = Color(0xFF1B6D24)
val AccentIndigoTraffic = Color(0xFF005DB7)
val AccentPoliceNavy = BrandNavyDark

object CitizenStatusColors {
    val critical: Color
        @Composable @ReadOnlyComposable
        get() = RcTheme.colors.critical

    val warning: Color
        @Composable @ReadOnlyComposable
        get() = RcTheme.colors.warning

    val safe: Color
        @Composable @ReadOnlyComposable
        get() = RcTheme.colors.safe

    val verifiedBadgeBg: Color
        @Composable @ReadOnlyComposable
        get() = RcTheme.colors.safe

    val insight: Color
        @Composable @ReadOnlyComposable
        get() = RcTheme.colors.info

    val flood: Color
        @Composable @ReadOnlyComposable
        get() = RcTheme.colors.info

    val traffic: Color
        @Composable @ReadOnlyComposable
        get() = RcTheme.colors.info

    /** Mantido para chamadas antigas que comparam com o fundo. */
    val isLight: Boolean
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.background == PaperBackground
}

/** Cor de função para usar sobre mídia/fundo preto (feed de Vídeos), igual nos dois temas. */
fun DarkToneOnBlack(tone: RcTone): Color = when (tone) {
    RcTone.CRITICAL -> DarkExtendedColors.critical
    RcTone.WARNING -> DarkExtendedColors.warning
    RcTone.SAFE -> DarkExtendedColors.safe
    RcTone.INFO -> DarkExtendedColors.info
}
