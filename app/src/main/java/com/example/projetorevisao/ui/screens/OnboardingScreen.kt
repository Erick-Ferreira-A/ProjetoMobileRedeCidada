package com.example.projetorevisao.ui.screens

import android.content.res.Configuration
import com.example.projetorevisao.ui.theme.BrandNavyDark
import com.example.projetorevisao.ui.theme.BrandBlueHero
import com.example.projetorevisao.ui.theme.BrandBlue
import com.example.projetorevisao.ui.components.CitizenIcons
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.ui.components.RedeCidadaLogo
import com.example.projetorevisao.ui.designsystem.RcAccentButton
import com.example.projetorevisao.ui.designsystem.RcSource
import com.example.projetorevisao.ui.designsystem.RcSourceBadge
import com.example.projetorevisao.ui.designsystem.RcSosHoldButton
import com.example.projetorevisao.ui.designsystem.RcTextButton
import com.example.projetorevisao.ui.theme.CitizenSPTheme
import com.example.projetorevisao.ui.theme.RcSpacing
import com.example.projetorevisao.ui.theme.RcTheme
import com.example.projetorevisao.ui.theme.RcTone
import kotlinx.coroutines.launch

/** Um passo do onboarding: a frase (resposta), o apoio e um pedaço real do app no lugar de ilustração. */
private data class OnboardingPage(
    val headline: String,
    val body: String,
    val preview: @Composable () -> Unit
)

/**
 * Onboarding (docs/design/TELAS.md §2.9) — 3 passos e depois a permissão de localização.
 *
 * Guia visual v1.0: fundo neutro do tema (sem degradê), títulos em Bricolage, laranja só no
 * botão de destaque com texto marinho, "Pular" sempre visível e o atalho de emergência no topo,
 * longe do botão principal (achado de 23/11/2026).
 */
@Composable
fun CitizenOnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    // 0..2 = apresentação; 3 = localização.
    var stage by rememberSaveable { mutableIntStateOf(0) }
    if (stage < 3) {
        OnboardingPager(onDone = { stage = 3 })
    } else {
        // O onboarding desenha por trás da barra de status (topo azul); aqui devolvemos esse espaço.
        Box(Modifier.statusBarsPadding()) {
            LocationPermissionGate(
                onGranted = onFinishOnboarding,
                onBack = { stage = 0 }
            )
        }
    }
}

@Composable
private fun OnboardingPager(onDone: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            headline = "Saiba o que acontece no seu caminho.",
            body = "Alertas, trânsito e transporte perto de você.",
            preview = { PreviewWhatsHappening() }
        ),
        OnboardingPage(
            headline = "Avise sem se expor.",
            body = "Anônimo por padrão. Cada relato ganha um score de confiabilidade.",
            preview = { PreviewAnonymous() }
        ),
        OnboardingPage(
            headline = "Alguém de confiança, a um toque.",
            body = "Segure o SOS para avisar seus guardiões. O app não substitui o 190.",
            preview = { PreviewSos() }
        )
    )
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == pages.lastIndex

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        BrandHeader()

        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { index ->
            val page = pages[index]
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = RcSpacing.lg)
            ) {
                // Título e apoio no topo; o pedaço do app embaixo.
                Text(
                    page.headline,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(RcSpacing.xs))
                Text(page.body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(RcSpacing.lg))
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) { page.preview() }
            }
        }

        // Indicador de passo.
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .padding(horizontal = RcSpacing.lg)
                .semantics { contentDescription = "Passo ${pager.currentPage + 1} de ${pages.size}" }
        ) {
            pages.indices.forEach { i ->
                val width by animateDpAsState(if (i == pager.currentPage) 24.dp else 8.dp, label = "dot")
                Box(
                    Modifier
                        .size(width = width, height = 8.dp)
                        .background(
                            if (i == pager.currentPage) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outlineVariant,
                            CircleShape
                        )
                )
            }
        }

        // Base: ação principal e, à direita, Pular.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(RcSpacing.md)
        ) {
            RcAccentButton(
                text = if (last) "Começar" else "Continuar",
                onClick = {
                    if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                },
                modifier = Modifier.weight(1f)
            )
            if (!last) {
                Spacer(Modifier.width(RcSpacing.xs))
                RcTextButton("Pular", onClick = onDone)
            }
        }
    }
}

/**
 * Topo com a identidade: degradê do azul da marca até o fundo da tela, logo no centro e o nome embaixo.
 * Branco sobre o azul: 10,2:1 (claro) e 15,5:1 (escuro).
 */
@Composable
private fun BrandHeader() {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // Azul mais vivo que o da marca, para o brilho; some até ficar transparente (sem faixa nem borda).
    val glow = if (dark) Color(0xFF3D6BFF) else Color(0xFF3D63E0)
    val glowStart = if (dark) 0.55f else 0.42f
    Column(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = RcSpacing.md, bottom = RcSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(176.dp)
                .background(
                    Brush.radialGradient(
                        0f to glow.copy(alpha = glowStart),
                        0.45f to glow.copy(alpha = glowStart * 0.45f),
                        1f to Color.Transparent
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // A parte branca do escudo fica um pouco translúcida para se misturar ao brilho.
            RedeCidadaLogo(size = 68.dp, innerSurfaceColor = Color.White.copy(alpha = 0.92f))
        }
        Text(
            "Rede Cidadã",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 0.dp)
        )
    }
}

/** Passo 1: as funções principais, uma linha cada (sem repetir o que os passos 2 e 3 mostram). */
@Composable
private fun PreviewWhatsHappening() {
    Column(verticalArrangement = Arrangement.spacedBy(RcSpacing.xs), modifier = Modifier.fillMaxWidth()) {
        FeatureRow(CitizenIcons.FloodWater, RcTone.WARNING, "Risco de alagamento na sua região", "Avisos de chuva forte")
        FeatureRow(CitizenIcons.CarIncident, RcTone.CRITICAL, "Acidente trava a Estrada de Itapecerica", "Relato de morador · há 5 min")
        // Pedido do Erick (24/11/2026). Guia §2.1: mostrar relatos perto da pessoa, nunca "bairro perigoso".
        FeatureRow(CitizenIcons.WarningTriangle, RcTone.CRITICAL, "Onde há mais relatos de assalto agora", "Relatos da comunidade em tempo real")
        FeatureRow(CitizenIcons.Map, RcTone.INFO, "Mapa em tempo real", "Tudo isso perto de você")
    }
}

@Composable
private fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, tone: RcTone, title: String, subtitle: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = RcSpacing.md, vertical = RcSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(tone.container, CircleShape),
                contentAlignment = Alignment.Center
            ) { androidx.compose.material3.Icon(icon, contentDescription = null, tint = tone.color, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(RcSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

/** Passo 2: a chave de anônimo, como aparece ao relatar. */
@Composable
private fun PreviewAnonymous() = Column(Modifier.fillMaxWidth()) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(RcSpacing.md), verticalArrangement = Arrangement.spacedBy(RcSpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Publicar como anônimo", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text("Ligado por padrão", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = true, onCheckedChange = null)
            }
            Text("O que os outros veem", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(RcSpacing.xs)) {
                RcSourceBadge(RcSource.Resident)
                RcSourceBadge(RcSource.Confirmations(5))
            }
            Text("O que fica só com você", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Nome · e-mail · guardiões · alertas SOS", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
    Spacer(Modifier.height(RcSpacing.sm))
    TrustScorePreview()
}

/**
 * Score de confiabilidade do relato (pedido do Erick, 24/11/2026).
 * INTEGRAR: hoje não há análise automática (IA) nem cálculo — o score do usuário é fixo (940).
 * Implementar antes de publicar na loja ou tirar a menção à análise automática.
 */
@Composable
private fun TrustScorePreview() {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(RcSpacing.md), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).background(RcTone.SAFE.container, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("92%", style = MaterialTheme.typography.titleSmall, color = RcTone.SAFE.color) }
            Spacer(Modifier.width(RcSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text("Confiabilidade do relato", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Avaliação de outros moradores + análise automática",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Passo 3: o botão de SOS de verdade (aqui só demonstra; não aciona nada). */
@Composable
private fun PreviewSos() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        RcSosHoldButton(onActivate = {}, size = 168.dp)
        Spacer(Modifier.height(RcSpacing.sm))
        Surface(shape = CircleShape, color = RcTheme.colors.safeContainer, contentColor = RcTheme.colors.safe) {
            Text("Mariana e Carlos recebem sua localização", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
        }
    }
}

@Preview(name = "Onboarding · claro", showSystemUi = true)
@Composable
fun PreviewCitizenOnboardingWelcome() {
    CitizenSPTheme(darkTheme = false) { CitizenOnboardingScreen(onFinishOnboarding = {}) }
}

@Preview(name = "Onboarding · escuro", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewCitizenOnboardingLocationDark() {
    CitizenSPTheme(darkTheme = true) { CitizenOnboardingScreen(onFinishOnboarding = {}) }
}
