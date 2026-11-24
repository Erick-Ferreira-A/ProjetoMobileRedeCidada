package com.example.projetorevisao.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Tipografia "Calçada" (guia visual v1.0 §3).
 *
 * Títulos: Bricolage Grotesque · Texto: Atkinson Hyperlegible Next (ambas OFL, Google Fonts).
 * Enquanto os arquivos não estão em res/font, as duas famílias caem na fonte do sistema.
 * Para ativar: adicionar os .ttf em app/src/main/res/font e trocar as duas linhas abaixo por
 * FontFamily(Font(R.font.bricolage_grotesque, ...)) / FontFamily(Font(R.font.atkinson_hyperlegible_next, ...)).
 */
val RcDisplayFamily: FontFamily = FontFamily.Default
val RcTextFamily: FontFamily = FontFamily.Default

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
