package com.example.projetorevisao.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.example.projetorevisao.R
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Tipografia "Calçada" (guia visual v1.0 §3).
 *
 * Títulos: Bricolage Grotesque · Texto: Atkinson Hyperlegible Next.
 * Arquivos variáveis em res/font (google/fonts, OFL 1.1; licenças em assets/licenses).
 * No Android 8+ cada peso usa o eixo `wght` da fonte variável; no 7.x (minSdk 24) o sistema
 * usa a instância padrão e sintetiza o negrito.
 */
@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: FontWeight, vararg extra: FontVariation.Setting) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight), *extra)
)

val RcDisplayFamily: FontFamily = FontFamily(
    // opsz 24–36: o desenho de "títulos médios" da Bricolage, menos apertado que o de 96.
    variable(R.font.bricolage_grotesque, FontWeight.SemiBold, FontVariation.Setting("opsz", 24f)),
    variable(R.font.bricolage_grotesque, FontWeight.Bold, FontVariation.Setting("opsz", 24f)),
    variable(R.font.bricolage_grotesque, FontWeight.ExtraBold, FontVariation.Setting("opsz", 36f))
)

val RcTextFamily: FontFamily = FontFamily(
    variable(R.font.atkinson_hyperlegible_next, FontWeight.Normal),
    variable(R.font.atkinson_hyperlegible_next, FontWeight.Medium),
    variable(R.font.atkinson_hyperlegible_next, FontWeight.Bold)
)

private fun display(weight: FontWeight, size: Int, line: Int, tracking: Double = -0.01) = TextStyle(
    fontFamily = RcDisplayFamily, fontWeight = weight,
    fontSize = size.sp, lineHeight = line.sp, letterSpacing = tracking.em
)

private fun text(weight: FontWeight, size: Int, line: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = RcTextFamily, fontWeight = weight,
    fontSize = size.sp, lineHeight = line.sp, letterSpacing = tracking.em
)

val Typography = Typography(
    displayLarge = display(FontWeight.ExtraBold, 48, 52, -0.02),
    displayMedium = display(FontWeight.ExtraBold, 42, 46, -0.02),
    displaySmall = display(FontWeight.ExtraBold, 36, 40, -0.02),
    headlineLarge = display(FontWeight.ExtraBold, 32, 36),
    headlineMedium = display(FontWeight.ExtraBold, 28, 32),   // frase-resumo da tela
    headlineSmall = display(FontWeight.SemiBold, 24, 30),
    titleLarge = display(FontWeight.SemiBold, 22, 28, 0.0),   // título de seção e de sheet
    titleMedium = text(FontWeight.Bold, 17, 23),              // título de card
    titleSmall = text(FontWeight.Bold, 15, 20),
    bodyLarge = text(FontWeight.Normal, 17, 26),
    bodyMedium = text(FontWeight.Normal, 15, 22),
    bodySmall = text(FontWeight.Normal, 13, 18),
    labelLarge = text(FontWeight.Bold, 16, 20),               // botões
    labelMedium = text(FontWeight.Bold, 14, 18),              // chips, abas
    labelSmall = text(FontWeight.Bold, 12, 16, 0.06)          // categoria e selos, caixa alta
)
