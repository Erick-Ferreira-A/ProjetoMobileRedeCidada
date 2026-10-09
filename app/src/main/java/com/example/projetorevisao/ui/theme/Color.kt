package com.example.projetorevisao.ui.theme

import androidx.compose.ui.graphics.Color

// Material 3 Expressive - Premium Dark Theme Palette (Android & M3 Standards)
val ObsidianBackground = Color(0xFF0B0F17)     // Deep Dark Background
val SurfaceDarkHeader = Color(0xFF131B2E)      // Translucent TopBar / Header Surface
val SurfaceDarkCard = Color(0xFF192238)        // Primary Card Background
val SurfaceDarkElevated = Color(0xFF232F4D)    // Sub-card & Input Field Container
val SurfaceBorderGlow = Color(0xFF2E3D66)      // 1dp Card Border Stroke

// M3 Expressive Accents & Badges
val AccentCyan = Color(0xFF38BDF8)             // Primary Interactive & Branding (Cyan-400)
val AccentRoseCritical = Color(0xFFF43F5E)     // Emergency SOS & High Risk Alert (Rose-500)
val AccentAmberWarning = Color(0xFFF59E0B)     // Alagamentos & Trânsito Atenção (Amber-500)
val AccentEmeraldSafe = Color(0xFF10B981)      // Operação Normal & Trajeto OK (Emerald-500)
val AccentPurpleAI = Color(0xFFA855F7)         // Resumos Gemini IA (Purple-500)

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