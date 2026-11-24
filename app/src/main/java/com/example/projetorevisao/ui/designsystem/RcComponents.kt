package com.example.projetorevisao.ui.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.ui.theme.RcAppTheme
import com.example.projetorevisao.ui.theme.RcSpacing
import com.example.projetorevisao.ui.theme.RcTheme
import com.example.projetorevisao.ui.theme.RcTone

// Componentes do guia visual v1.0 §6. Tela nova usa estes blocos; componente novo entra no guia antes.

private val ButtonHeight = 52.dp
private val ButtonPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)

/** Frase-resumo da tela: diz a resposta, não o nome da tela. Uma por tela. */
@Composable
fun RcSummaryHeadline(text: String, modifier: Modifier = Modifier, eyebrow: String? = null) {
    Column(modifier.semantics(mergeDescendants = true) { heading() }) {
        if (eyebrow != null) {
            Text(eyebrow, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.size(RcSpacing.xxs))
        }
        Text(text, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun RcSectionHeader(title: String, modifier: Modifier = Modifier, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        if (actionLabel != null && onAction != null) RcTextButton(actionLabel, onClick = onAction)
    }
}

/** Ação de destaque (Relatar). Laranja com texto marinho. No máximo uma por tela. */
@Composable
fun RcAccentButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.defaultMinSize(minHeight = ButtonHeight),
        shape = MaterialTheme.shapes.medium,
        contentPadding = ButtonPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = RcTheme.colors.accent,
            contentColor = RcTheme.colors.onAccent
        )
    ) { ButtonContent(text, loading) }
}

@Composable
fun RcPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.defaultMinSize(minHeight = ButtonHeight),
        shape = MaterialTheme.shapes.medium,
        contentPadding = ButtonPadding
    ) { ButtonContent(text, loading) }
}

@Composable
fun RcSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = ButtonHeight),
        shape = MaterialTheme.shapes.medium,
        contentPadding = ButtonPadding,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun RcTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.heightIn(min = RcSpacing.minTouch)) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ButtonContent(text: String, loading: Boolean) {
    if (loading) {
        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.5.dp, color = LocalContentColorOrDefault())
        Spacer(Modifier.width(RcSpacing.xs))
    }
    Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun LocalContentColorOrDefault(): Color = androidx.compose.material3.LocalContentColor.current

/** De onde vem o dado (princípio 3). Sempre com texto; a cor só reforça. */
sealed interface RcSource {
    data class Live(val time: String) : RcSource
    data class Official(val name: String) : RcSource
    data object Resident : RcSource
    data class Confirmations(val count: Int) : RcSource
    data class Stale(val since: String) : RcSource
    data object Example : RcSource
    /** Fonte consultada, mas sem resposta agora. */
    data class Unavailable(val name: String) : RcSource
    /** Opção que já existe na tela mas ainda não está integrada. Nunca mostrar dado inventado no lugar. */
    data object ComingSoon : RcSource
}

@Composable
fun RcSourceBadge(source: RcSource, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val (label, container, content, border) = when (source) {
        is RcSource.Live -> Quad("● Ao vivo ${source.time}", RcTone.SAFE.container, RcTone.SAFE.color, null)
        is RcSource.Official -> Quad("Fonte: ${source.name}", RcTone.INFO.container, RcTone.INFO.color, null)
        RcSource.Resident -> Quad("Relato de morador", scheme.surfaceContainerHigh, scheme.onSurface, null)
        is RcSource.Confirmations -> Quad(
            if (source.count == 1) "✓ 1 confirmação" else "✓ ${source.count} confirmações",
            Color.Transparent, scheme.onSurfaceVariant, scheme.outlineVariant
        )
        is RcSource.Stale -> Quad("Dados de ${source.since}", Color.Transparent, scheme.onSurfaceVariant, scheme.outline)
        RcSource.Example -> Quad("Exemplo · sem conexão", Color.Transparent, scheme.onSurfaceVariant, scheme.outline)
        is RcSource.Unavailable -> Quad("${source.name} sem resposta", Color.Transparent, scheme.onSurfaceVariant, scheme.outline)
        RcSource.ComingSoon -> Quad("Em breve", scheme.surfaceContainerHigh, scheme.onSurfaceVariant, null)
    }
    Surface(
        modifier = modifier,
        shape = if (source is RcSource.Live) CircleShape else MaterialTheme.shapes.small,
        color = container,
        contentColor = content,
        border = border?.let { BorderStroke(1.dp, it) }
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
    }
}

private data class Quad(val a: String, val b: Color, val c: Color, val d: Color?)

/** Chip de filtro: 40 dp visíveis, 48 dp de toque. Selecionado inverte as cores. */
@Composable
fun RcFilterChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .heightIn(min = RcSpacing.minTouch)
            .semantics { this.selected = selected }
            .clickable(role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (selected) scheme.onSurface else scheme.surface,
            contentColor = if (selected) scheme.surface else scheme.onSurface,
            border = if (selected) null else BorderStroke(1.5.dp, scheme.outline)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
        }
    }
}

/** Card de ocorrência (guia §6): categoria·distância → título → onde·quando → selos → até 2 ações. */
@Composable
fun RcAlertCard(
    category: String,
    tone: RcTone,
    distance: String,
    title: String,
    whereWhen: String,
    sources: List<RcSource>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryAction: Pair<String, () -> Unit>? = null,
    secondaryAction: Pair<String, () -> Unit>? = null,
    /** Conteúdo extra entre "onde·quando" e os selos (ex.: miniaturas de foto/vídeo). */
    media: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(RcSpacing.md), verticalArrangement = Arrangement.spacedBy(RcSpacing.xxs)) {
            Text(
                "${category.uppercase()} · $distance",
                style = MaterialTheme.typography.labelSmall,
                color = tone.color
            )
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(whereWhen, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (media != null) {
                Spacer(Modifier.size(RcSpacing.xxs))
                media()
            }
            if (sources.isNotEmpty()) {
                Spacer(Modifier.size(RcSpacing.xxs))
                Row(horizontalArrangement = Arrangement.spacedBy(RcSpacing.xs)) { sources.forEach { RcSourceBadge(it) } }
            }
            if (primaryAction != null || secondaryAction != null) {
                Spacer(Modifier.size(RcSpacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(RcSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                    primaryAction?.let { (label, action) ->
                        OutlinedButton(
                            onClick = action,
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.heightIn(min = 40.dp)
                        ) { Text(label, style = MaterialTheme.typography.labelMedium) }
                    }
                    secondaryAction?.let { (label, action) -> RcTextButton(label, onClick = action) }
                }
            }
        }
    }
}

/** Cartão compacto da "faixa de agora" (clima, trilhos, alagamentos). */
@Composable
fun RcStatusTile(label: String, value: String, detail: String, source: RcSource, modifier: Modifier = Modifier, tone: RcTone? = null) {
    Surface(
        modifier = modifier.widthIn(min = 156.dp, max = 220.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = tone?.color ?: MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            Spacer(Modifier.size(6.dp))
            RcSourceBadge(source)
        }
    }
}

// ── Prévias ──────────────────────────────────────────────────────────────

@Composable
private fun CatalogPreviewContent() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(Modifier.padding(RcSpacing.md), verticalArrangement = Arrangement.spacedBy(RcSpacing.md)) {
            RcSummaryHeadline("Seu caminho está tranquilo agora.", eyebrow = "Boa noite · Capão Redondo")
            Row(horizontalArrangement = Arrangement.spacedBy(RcSpacing.sm)) {
                RcStatusTile("Clima", "19°", "Garoa fraca", RcSource.Live("18:40"))
                RcStatusTile("Trilhos", "Linha 5", "Velocidade reduzida", RcSource.Official("ARTESP"), tone = RcTone.WARNING)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(RcSpacing.xs)) {
                RcFilterChip("Todos", selected = true, onClick = {})
                RcFilterChip("Alagamento", selected = false, onClick = {})
            }
            RcAlertCard(
                category = "Iluminação", tone = RcTone.INFO, distance = "350 m",
                title = "Poste apagado na saída da estação",
                whereWhen = "Rua Teodoro Sampaio · há 20 min",
                sources = listOf(RcSource.Resident, RcSource.Confirmations(12)),
                onClick = {}, primaryAction = "Também vi" to {}, secondaryAction = "Compartilhar" to {}
            )
            RcAccentButton("Relatar agora", onClick = {}, modifier = Modifier.fillMaxWidth())
            RcPrimaryButton("Ver meu trajeto", onClick = {}, modifier = Modifier.fillMaxWidth())
            RcSecondaryButton("Compartilhar", onClick = {}, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(RcSpacing.xs)) {
                RcSourceBadge(RcSource.Stale("18:12"))
                RcSourceBadge(RcSource.Example)
            }
        }
    }
}

@Preview(name = "Design system · claro", widthDp = 400, heightDp = 1000)
@Composable
private fun CatalogLightPreview() = RcAppTheme(darkTheme = false) { CatalogPreviewContent() }

@Preview(name = "Design system · escuro", widthDp = 400, heightDp = 1000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CatalogDarkPreview() = RcAppTheme(darkTheme = true) { CatalogPreviewContent() }
