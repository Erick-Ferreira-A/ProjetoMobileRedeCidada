package com.example.projetorevisao.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Material 3 Expressive - Dark & Light Theme Palette (Figma PROJETO UX - IHC V2)
val ObsidianBackground = Color(0xFF0B0F17)     // Deep Dark Background
val SurfaceDarkHeader = Color(0xFF131B2E)      // Translucent TopBar / Header Surface
val SurfaceDarkCard = Color(0xFF192238)        // Primary Card Background
val SurfaceDarkElevated = Color(0xFF232F4D)    // Sub-card & Input Field Container
val SurfaceBorderGlow = Color(0xFF2E3D66)      // 1dp Card Border Stroke

// Figma Official Brand Colors (Escudo Tutela / Rede Cidadã)
val BrandNavyDark = Color(0xFF0A2540)
val BrandBlue = Color(0xFF1D3B8B)
val BrandBlueHero = Color(0xFF1E3A8A)
val BrandOrange = Color(0xFFFF6B00)
val BrandOrangeLight = Color(0xFFFF8533)

// M3 Expressive Accents & Category Badges (Figma 32:608)
val AccentCyan = Color(0xFF0EA5E9)             // Alagamento na Via / Clima (Sky-500)
val AccentRoseCritical = Color(0xFFEF4444)     // Incêndio / SOS Emergência (Red-500)
val AccentAmberWarning = Color(0xFFF97316)     // Roubo / Alerta Laranja (Orange-500)
val AccentEmeraldSafe = Color(0xFF10B981)      // Badge "✓ Verificado" & Operação Normal (Emerald-500)
val AccentPurpleAI = Color(0xFF9333EA)         // Alerta de Tempestade & Gemini IA (Purple-600)
val AccentIndigoTraffic = Color(0xFF3B82F6)    // Acidente de Trânsito (Blue-500)
val AccentPoliceNavy = Color(0xFF1E3A8A)       // Atividade Policial Intensa (Navy-800)

// Typography & Text Tokens
val TextExpressivePrimary = Color(0xFFF8FAFC)  // High Contrast Text (Slate-50)
val TextExpressiveSecondary = Color(0xFF94A3B8)// Muted Secondary Info (Slate-400)
val TextExpressiveMuted = Color(0xFF64748B)    // Helper / Caption Text (Slate-500)

// Backward Compatibility & Semantic Aliases
val CitizenBackground = ObsidianBackground
val CitizenSurface = SurfaceDarkCard
val CitizenSurfaceElevated = SurfaceDarkElevated
val AlertCritical = AccentRoseCritical
val AlertWarning = AccentAmberWarning
val AlertTransit = AccentCyan
val StatusSafe = AccentEmeraldSafe
val TextPrimary = TextExpressivePrimary
val TextSecondary = TextExpressiveSecondary
val TextMuted = TextExpressiveMuted

object CitizenStatusColors {
    private val isLight: Boolean
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.background.luminance() > 0.5f

    val critical: Color
        @Composable @ReadOnlyComposable
        get() = if (isLight) Color(0xFFC81E3A) else AccentRoseCritical

    val warning: Color
        @Composable @ReadOnlyComposable
        get() = if (isLight) Color(0xFFD95B00) else AccentAmberWarning

    val safe: Color
        @Composable @ReadOnlyComposable
        get() = if (isLight) Color(0xFF057A55) else AccentEmeraldSafe

    val verifiedBadgeBg: Color
        @Composable @ReadOnlyComposable
        get() = if (isLight) Color(0xFF10B981) else Color(0xFF059669)

    val insight: Color
        @Composable @ReadOnlyComposable
        get() = if (isLight) Color(0xFF6D28D9) else Color(0xFFC084FC)

    val flood: Color
        @Composable @ReadOnlyComposable
        get() = if (isLight) Color(0xFF0284C7) else Color(0xFF38BDF8)

    val traffic: Color
        @Composable @ReadOnlyComposable
        get() = if (isLight) Color(0xFF2563EB) else Color(0xFF60A5FA)
}