package com.example.projetorevisao.ui.screens

import android.content.res.Configuration
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
import com.example.projetorevisao.ui.designsystem.RcAlertCard
import com.example.projetorevisao.ui.designsystem.RcEmergencyCallChip
import com.example.projetorevisao.ui.designsystem.RcSource
import com.example.projetorevisao.ui.designsystem.RcSourceBadge
import com.example.projetorevisao.ui.designsystem.RcSosHoldButton
import com.example.projetorevisao.ui.designsystem.RcTextButton
import com.example.projetorevisao.ui.designsystem.RcWeatherGlyph
import com.example.projetorevisao.ui.designsystem.RcWeatherKind
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
        LocationPermissionGate(
            onGranted = onFinishOnboarding,
            onBack = { stage = 0 }
        )
    }
}

@Composable
private fun OnboardingPager(onDone: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            headline = "Saiba o que acontece no seu caminho.",
            body = "Relatos da vizinhança, clima e status do metrô e dos trens no mesmo lugar, perto de onde você está.",
            preview = { PreviewWhatsHappening() }
        ),
        OnboardingPage(
            headline = "Avise sem se expor.",
            body = "Seus relatos saem como anônimos por padrão. Seu nome, e-mail e guardiões nunca ficam públicos.",
            preview = { PreviewAnonymous() }
        ),
        OnboardingPage(
            headline = "Alguém de confiança, a um toque.",
            body = "Segure o SOS para avisar seus guardiões pelo WhatsApp com sua localização. O app não substitui o 190.",
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
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Topo: marca à esquerda, emergência à direita (longe do botão principal).
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = RcSpacing.md, vertical = RcSpacing.xs)
        ) {
            RedeCidadaLogo(size = 32.dp)
            Spacer(Modifier.width(RcSpacing.xs))
            Text("Rede Cidadã", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
            RcEmergencyCallChip()
        }

        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { index ->
            val page = pages[index]
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = RcSpacing.lg),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) { page.preview() }
                Spacer(Modifier.height(RcSpacing.lg))
                Text(
                    page.headline,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(RcSpacing.sm))
                Text(page.body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(RcSpacing.lg))
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

        // Base: Pular (sempre) + ação principal.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(RcSpacing.md)
        ) {
            if (!last) RcTextButton("Pular", onClick = onDone) else Spacer(Modifier.width(1.dp))
            Spacer(Modifier.width(RcSpacing.sm))
            RcAccentButton(
                text = if (last) "Começar" else "Continuar",
                onClick = {
                    if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Passo 1: o que a pessoa vai ver no Início. */
@Composable
private fun PreviewWhatsHappening() {
    Column(verticalArrangement = Arrangement.spacedBy(RcSpacing.sm), modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(Modifier.padding(RcSpacing.md).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RcWeatherGlyph(RcWeatherKind.PARTLY_CLOUDY, size = 28.dp)
                Spacer(Modifier.width(RcSpacing.sm))
                Text("21° · Parcialmente nublado", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                RcSourceBadge(RcSource.Official("Open-Meteo"))
            }
        }
        RcAlertCard(
            category = "Iluminação", tone = RcTone.INFO, distance = "a 350 m",
            title = "Poste apagado na saída da estação",
            whereWhen = "Exemplo · há 20 min",
            sources = listOf(RcSource.Resident, RcSource.Confirmations(12)),
            onClick = {}
        )
    }
}

/** Passo 2: a chave de anônimo, como aparece ao relatar. */
@Composable
private fun PreviewAnonymous() {
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
