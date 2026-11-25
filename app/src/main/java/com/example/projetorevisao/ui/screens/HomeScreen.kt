package com.example.projetorevisao.ui.screens

import android.content.res.Configuration
import com.example.projetorevisao.ui.designsystem.weatherKindOf
import com.example.projetorevisao.ui.designsystem.RcWeatherGlyph
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.semantics.contentDescription
import com.example.projetorevisao.ui.designsystem.RcPrimaryButton
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.example.projetorevisao.ui.designsystem.RcAlertCard
import com.example.projetorevisao.ui.designsystem.RcFilterChip
import com.example.projetorevisao.ui.designsystem.RcSource
import com.example.projetorevisao.ui.designsystem.RcTextButton
import com.example.projetorevisao.ui.home.homeSummary
import com.example.projetorevisao.ui.home.relevanceScore
import com.example.projetorevisao.ui.home.streetOf
import com.example.projetorevisao.ui.theme.RcSpacing
import com.example.projetorevisao.ui.theme.RcTone
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.data.remote.WeatherSafetyTelemetry
import com.example.projetorevisao.ui.components.CitizenAppTopBar
import com.example.projetorevisao.ui.components.CitizenBottomNavBar
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.components.IncidentMediaViewer
import com.example.projetorevisao.ui.components.RemoteMediaTile
import com.example.projetorevisao.ui.components.CitizenInteractiveMapContainer
import com.example.projetorevisao.ui.theme.BrandBlue
import com.example.projetorevisao.ui.theme.BrandBlueHero
import com.example.projetorevisao.ui.theme.BrandOrange
import com.example.projetorevisao.ui.theme.CitizenSPTheme
import com.example.projetorevisao.ui.theme.CitizenStatusColors
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import com.example.projetorevisao.ui.viewmodel.IncidentAlert
import com.example.projetorevisao.ui.viewmodel.NoticeTone
import com.example.projetorevisao.ui.viewmodel.ReportReason
import com.example.projetorevisao.ui.viewmodel.IncidentCategory
import com.example.projetorevisao.ui.viewmodel.IncidentStatus
import com.example.projetorevisao.ui.viewmodel.rememberCitizenViewModel

/** Raios de alerta oferecidos (km). */
private val RadiusOptionsKm = listOf(1, 2, 5)
private const val DefaultRadiusKm = 2

/** Altura do painel recolhido: alça + resumo + clima. O resto da tela é mapa. */
private val SheetPeekHeight = 124.dp

/**
 * Início (docs/design/TELAS.md §2.1, revisado em 23/11/2026 com o usuário).
 *
 * - O mapa ocupa quase a tela toda, entre o cabeçalho e a navegação, e pode ser explorado livremente.
 * - "Filtros" no topo do mapa abre um painel com categorias (várias ao mesmo tempo) e o raio.
 * - Painel de baixo: recolhido mostra só o resumo e o clima pequeno; ao puxar para cima, os relatos próximos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenHomeScreen(
    viewModel: CitizenViewModel = rememberCitizenViewModel(),
    onSosClick: () -> Unit = {},
    onOpenReportSheet: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onCenterOnUser: () -> Unit = {}
) {
    val context = LocalContext.current
    val selectedIncident = viewModel.selectedIncidentForDetail
    val snackbar = remember { SnackbarHostState() }

    var radiusKm by rememberSaveable { mutableIntStateOf(DefaultRadiusKm) }
    var selectedCategoryNames by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val selectedCategories = selectedCategoryNames.mapNotNull { name -> IncidentCategory.entries.find { it.name == name } }.toSet()
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var showSearch by rememberSaveable { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    fun matches(alert: IncidentAlert, categories: Set<IncidentCategory>, radius: Int) =
        (alert.distanceMeters <= 0 || alert.distanceMeters <= radius * 1000) &&
            (categories.isEmpty() || alert.category in categories)

    val visibleAlerts = viewModel.incidents
        .filter { matches(it, selectedCategories, radiusKm) }
        .sortedBy { relevanceScore(it, now) }
    val summary = homeSummary(
        visibleAlerts, emptyList(),
        loading = viewModel.isRefreshingApis && visibleAlerts.isEmpty(),
        alertsUnavailable = viewModel.incidentsSyncFailed
    )
    val activeFilterCount = selectedCategories.size + if (radiusKm != DefaultRadiusKm) 1 else 0

    // Avisos do app (relato enviado, erro...) aparecem como snackbar, sem empurrar o mapa.
    val notice = viewModel.incidentActionMessage ?: viewModel.userNotice
    LaunchedEffect(notice) {
        if (notice != null) {
            snackbar.showSnackbar(notice)
            if (viewModel.incidentActionMessage != null) viewModel.clearIncidentActionMessage()
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = SheetPeekHeight,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 12.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        sheetContent = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = RcSpacing.md, end = RcSpacing.md, bottom = RcSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(RcSpacing.md)
            ) {
                // Visível com o painel recolhido: resumo e, logo abaixo, o clima com ícone.
                item(key = "peek") {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = summary.text,
                            style = MaterialTheme.typography.titleMedium,
                            color = summary.tone?.color ?: MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.semantics { heading(); liveRegion = LiveRegionMode.Polite }
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WeatherLine(viewModel.weatherTelemetry, viewModel.isRefreshingApis)
                            Text(
                                text = when (visibleAlerts.size) {
                                    0 -> "  ·  raio de $radiusKm km"
                                    1 -> "  ·  1 relato em $radiusKm km"
                                    else -> "  ·  ${visibleAlerts.size} relatos em $radiusKm km"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                item(key = "disclaimer") {
                    Text(
                        "Relatos de moradores. Não são confirmados pela polícia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(visibleAlerts, key = { it.id }) { incident ->
                    HomeAlertCard(
                        incident = incident,
                        onOpen = { viewModel.openIncidentDetail(incident) },
                        onConfirm = { viewModel.confirmIncident(incident.id) },
                        onShare = { context.shareIncident(incident) }
                    )
                }

                item(key = "end") {
                    HomeListEnd(
                        isEmpty = visibleAlerts.isEmpty(),
                        filtering = selectedCategories.isNotEmpty() || viewModel.searchQuery.isNotBlank(),
                        radiusKm = radiusKm,
                        canGrow = radiusKm < RadiusOptionsKm.last(),
                        onGrowRadius = { radiusKm = RadiusOptionsKm.first { it > radiusKm } },
                        onClearFilter = {
                            selectedCategoryNames = emptyList()
                            viewModel.updateSearchQuery("")
                        },
                        onReport = onOpenReportSheet
                    )
                }
            }
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            CitizenInteractiveMapContainer(
                neighborhood = viewModel.areaLabel,
                incidents = visibleAlerts,
                showHeatmap = false, // TELAS §2.2: sem mancha de calor (estigmatiza territórios)
                onToggleHeatmap = {},
                onPinClick = { viewModel.openIncidentDetail(it) },
                onCenterOnUser = onCenterOnUser,
                userLatitude = viewModel.userLatitude,
                userLongitude = viewModel.userLongitude,
                userAreaLabel = viewModel.userAreaLabel,
                userLocationVersion = viewModel.userLocationVersion,
                fullBleed = true,
                controlsBottomPadding = SheetPeekHeight,
                modifier = Modifier.fillMaxSize()
            )

            // Topo do mapa: Filtros e busca.
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(RcSpacing.md),
                verticalArrangement = Arrangement.spacedBy(RcSpacing.xs)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(RcSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                    MapOverlayButton(
                        label = if (activeFilterCount > 0) "Filtros · $activeFilterCount" else "Filtros",
                        icon = CitizenIcons.Layers,
                        highlighted = activeFilterCount > 0,
                        onClick = { showFilters = true }
                    )
                    MapOverlayButton(
                        label = null,
                        icon = CitizenIcons.Search,
                        highlighted = showSearch,
                        contentDescription = if (showSearch) "Fechar busca" else "Buscar relatos",
                        onClick = {
                            showSearch = !showSearch
                            if (!showSearch) viewModel.updateSearchQuery("")
                        }
                    )
                }
                if (showSearch) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = viewModel.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Rua, estação ou tipo de relato") },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showFilters) {
        HomeFiltersSheet(
            initialCategories = selectedCategories,
            initialRadiusKm = radiusKm,
            previewCount = { categories, radius -> viewModel.incidents.count { matches(it, categories, radius) } },
            onApply = { categories, radius ->
                selectedCategoryNames = categories.map { it.name }
                radiusKm = radius
                showFilters = false
            },
            onDismiss = { showFilters = false }
        )
    }

    if (selectedIncident != null) {
        IncidentDetailBottomSheet(
            incident = selectedIncident,
            onDismiss = { viewModel.openIncidentDetail(null) },
            onConfirm = { viewModel.confirmIncident(selectedIncident.id) },
            actionMessage = viewModel.incidentActionMessage,
            onReport = { reason, details, done -> viewModel.reportIncident(selectedIncident.id, reason, details, done) },
            onBlockAuthor = { viewModel.blockAuthor(selectedIncident.id) },
            onSosClick = {
                viewModel.openIncidentDetail(null)
                onSosClick()
            }
        )
    }
}

/** Botão flutuante sobre o mapa (Filtros, busca). Fundo de superfície para ler sobre qualquer mapa. */
@Composable
private fun MapOverlayButton(
    label: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    highlighted: Boolean,
    onClick: () -> Unit,
    contentDescription: String? = null
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (highlighted) scheme.onSurface else scheme.surface,
        contentColor = if (highlighted) scheme.surface else scheme.onSurface,
        shadowElevation = 4.dp,
        modifier = Modifier
            .heightIn(min = RcSpacing.minTouch)
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (label == null) 12.dp else 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RcSpacing.xs)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            if (label != null) Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Clima numa linha: ícone + temperatura + condição (ensolarado, nublado, chuva, tempestade). */
@Composable
private fun WeatherLine(weather: WeatherSafetyTelemetry, refreshing: Boolean) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {}
    ) {
        if (weather.conditionLabel.isNotBlank()) {
            val kind = weatherKindOf(weather.conditionLabel, hour)
            RcWeatherGlyph(kind)
            Text(
                "${weather.temperatureCelsius.roundToInt()}° · ${kind.label}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            if (!weather.isLiveNetworkData) {
                Text("(${weather.lastUpdatedTime})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Text(
                if (refreshing) "Consultando o clima…" else "Clima indisponível",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Painel de filtros: o que mostrar (várias categorias) e o raio. Só aplica ao confirmar. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun HomeFiltersSheet(
    initialCategories: Set<IncidentCategory>,
    initialRadiusKm: Int,
    previewCount: (Set<IncidentCategory>, Int) -> Int,
    onApply: (Set<IncidentCategory>, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var categories by remember { mutableStateOf(initialCategories) }
    var radius by remember { mutableIntStateOf(initialRadiusKm) }
    val count = previewCount(categories, radius)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = RcSpacing.md).padding(bottom = RcSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(RcSpacing.md)
        ) {
            Text("Filtros", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })

            Text("O que mostrar", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(RcSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(RcSpacing.xs)
            ) {
                RcFilterChip("Tudo", selected = categories.isEmpty(), onClick = { categories = emptySet() })
                IncidentCategory.entries.forEach { category ->
                    RcFilterChip(
                        label = category.label,
                        selected = category in categories,
                        onClick = { categories = if (category in categories) categories - category else categories + category }
                    )
                }
            }

            Text("Raio a partir de você", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(RcSpacing.xs)) {
                RadiusOptionsKm.forEach { km -> RcFilterChip("$km km", selected = radius == km, onClick = { radius = km }) }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = RcSpacing.xs)) {
                RcTextButton("Limpar", onClick = {
                    categories = emptySet()
                    radius = DefaultRadiusKm
                })
                Spacer(Modifier.width(RcSpacing.xs))
                RcPrimaryButton(
                    text = when (count) {
                        0 -> "Nenhum relato com esses filtros"
                        1 -> "Mostrar 1 relato"
                        else -> "Mostrar $count relatos"
                    },
                    onClick = { onApply(categories, radius) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HomeAlertCard(incident: IncidentAlert, onOpen: () -> Unit, onConfirm: () -> Unit, onShare: () -> Unit) {
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    RcAlertCard(
        category = incident.category.label,
        tone = incident.category.tone,
        distance = incident.distanceLabel,
        title = incident.title,
        whereWhen = "${streetOf(incident.address)} · ${incident.timeAgo}",
        sources = buildList {
            add(RcSource.Resident)
            if (incident.confirmations > 0) add(RcSource.Confirmations(incident.confirmations))
        },
        onClick = onOpen,
        primaryAction = (if (incident.userConfirmed) "✓ Você viu" else "Também vi") to onConfirm,
        secondaryAction = "Compartilhar" to onShare,
        media = if (incident.media.isEmpty()) null else {
            {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(RcSpacing.xs)) {
                    itemsIndexed(incident.media) { index, media ->
                        RemoteMediaTile(url = media.url, isVideo = media.isVideo, size = 88.dp) { viewerIndex = index }
                    }
                }
            }
        }
    )
    viewerIndex?.let { start -> IncidentMediaViewer(media = incident.media, startIndex = start) { viewerIndex = null } }
}

@Composable
private fun HomeListEnd(
    isEmpty: Boolean,
    filtering: Boolean,
    radiusKm: Int,
    canGrow: Boolean,
    onGrowRadius: () -> Unit,
    onClearFilter: () -> Unit,
    onReport: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = RcSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(RcSpacing.xs)
    ) {
        Text(
            text = when {
                isEmpty && filtering -> "Nada com esse filtro num raio de $radiusKm km."
                isEmpty -> "Nenhum alerta num raio de $radiusKm km. Viu algo? Avise a vizinhança."
                else -> "Isso é tudo num raio de $radiusKm km."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(RcSpacing.xs)) {
            if (isEmpty && filtering) RcTextButton("Limpar filtro", onClick = onClearFilter)
            if (canGrow) RcTextButton("Aumentar o raio", onClick = onGrowRadius)
            if (isEmpty && !filtering) RcTextButton("Relatar", onClick = onReport)
        }
    }
}

/** Compartilhar um alerta pelo seletor do Android (a pessoa escolhe o app e confirma o envio). */
private fun android.content.Context.shareIncident(incident: IncidentAlert) {
    val text = "${incident.title} — ${streetOf(incident.address)} (${incident.timeAgo}). " +
        "Relato de morador no app Rede Cidadã, não confirmado pela polícia."
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(send, "Compartilhar alerta"))
}

@Composable
private fun ReportIncidentDialog(onDismiss: () -> Unit, onSubmit: (ReportReason, String) -> Unit) {
    var reason by remember { mutableStateOf<ReportReason?>(null) }
    var details by remember { mutableStateOf("") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Denunciar relato") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "O relato some para você na hora e a equipe analisa. Com várias denúncias, ele sai do ar para todos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ReportReason.entries.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = option }
                    ) {
                        androidx.compose.material3.RadioButton(selected = reason == option, onClick = { reason = option })
                        Text(option.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it.take(500) },
                    label = { Text("Detalhes (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                enabled = reason != null,
                onClick = { reason?.let { onSubmit(it, details.trim()) } }
            ) { Text("Enviar denúncia") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun InteractiveIncidentMapCard(
    neighborhood: String,
    incidents: List<IncidentAlert>,
    showHeatmap: Boolean,
    onToggleHeatmap: () -> Unit,
    onPinClick: (IncidentAlert) -> Unit,
    onCenterOnUser: () -> Unit,
    userLatitude: Double?,
    userLongitude: Double?,
    userAreaLabel: String?,
    userLocationVersion: Int,
    height: androidx.compose.ui.unit.Dp = 210.dp
) {
    CitizenInteractiveMapContainer(
        neighborhood = neighborhood,
        incidents = incidents,
        showHeatmap = showHeatmap,
        onToggleHeatmap = onToggleHeatmap,
        onPinClick = onPinClick,
        onCenterOnUser = onCenterOnUser,
        userLatitude = userLatitude,
        userLongitude = userLongitude,
        userAreaLabel = userAreaLabel,
        userLocationVersion = userLocationVersion,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(MaterialTheme.shapes.medium)
    )
}

@Composable
private fun IncidentStatusFeedbackBar(
    status: IncidentStatus,
    authority: String
) {
    // Ninguém atualiza o andamento dos relatos: mostramos só a origem e o aviso de que não vai à polícia.
    val statusColor = BrandOrange
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = statusColor.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.30f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                text = "RELATADO POR MORADOR",
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = authority.ifBlank { "Não é enviado à polícia. Em perigo, ligue 190." },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncidentDetailBottomSheet(
    incident: IncidentAlert,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    actionMessage: String?,
    onReport: (ReportReason, String, () -> Unit) -> Unit,
    onBlockAuthor: () -> Unit,
    onSosClick: () -> Unit
) {
    var showReportDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    if (showReportDialog) {
        ReportIncidentDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { reason, details -> onReport(reason, details) { showReportDialog = false } }
        )
    }
    if (showBlockDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text("Bloquear quem publicou?") },
            text = { Text("Você deixa de ver todos os relatos desta pessoa. Ela não fica sabendo.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showBlockDialog = false; onBlockAuthor() }) { Text("Bloquear") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showBlockDialog = false }) { Text("Cancelar") }
            }
        )
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = incident.category.tone.color
                ) {
                    Text(
                        text = incident.effectiveBadgeTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Text(
                    text = "${incident.timeAgo} • ${incident.distanceLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = incident.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = incident.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            IncidentStatusFeedbackBar(
                status = incident.status,
                authority = incident.authorityAssigned
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CitizenStatusColors.insight.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, CitizenStatusColors.insight.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = CitizenIcons.SparklesAi,
                        contentDescription = null,
                        tint = CitizenStatusColors.insight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                        text = "Dica de segurança",
                            style = MaterialTheme.typography.labelLarge,
                            color = CitizenStatusColors.insight,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = incident.aiSafetyRecommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (incident.media.isNotEmpty()) {
                Text(
                    text = "Fotos e vídeos (${incident.media.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                var viewerIndex by remember { mutableStateOf<Int?>(null) }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    incident.media.forEachIndexed { index, media ->
                        RemoteMediaTile(url = media.url, isVideo = media.isVideo, size = 110.dp) { viewerIndex = index }
                    }
                }
                viewerIndex?.let { start ->
                    IncidentMediaViewer(media = incident.media, startIndex = start) { viewerIndex = null }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = CitizenIcons.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (incident.userConfirmed) "Você confirmou (${incident.confirmations})" else "Eu também vi (${incident.confirmations})")
                }

                Button(
                    onClick = onSosClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CitizenStatusColors.critical,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = CitizenIcons.PhoneCall,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Acionar SOS", fontWeight = FontWeight.Bold)
                }
            }

            actionMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                androidx.compose.material3.TextButton(onClick = { showReportDialog = true }) {
                    Text("Denunciar relato", color = MaterialTheme.colorScheme.error)
                }
                androidx.compose.material3.TextButton(onClick = { showBlockDialog = true }) {
                    Text("Bloquear quem publicou", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

fun categoryIcon(category: IncidentCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        IncidentCategory.ROUBO_FURTO -> CitizenIcons.SosEmergency
        IncidentCategory.INCENDIO -> CitizenIcons.Fire
        IncidentCategory.ALAGAMENTO -> CitizenIcons.FloodWater
        IncidentCategory.TEMPESTADE -> CitizenIcons.Storm
        IncidentCategory.TRANSITO -> CitizenIcons.CarIncident
        IncidentCategory.OPERACAO_POLICIAL -> CitizenIcons.PoliceShield
        IncidentCategory.UTILIDADE_PUBLICA -> CitizenIcons.LocationPin
    }
}

@Preview(showBackground = true, heightDp = 820, name = "Home Screen - Light")
@Composable
fun PreviewCitizenHomeLight() {
    CitizenSPTheme(darkTheme = false) {
        Scaffold(
            topBar = { CitizenAppTopBar(currentScreen = "home") },
            bottomBar = {
                CitizenBottomNavBar(
                    currentScreen = "home",
                    onSelectScreen = {},
                    onOpenReportSheet = {}
                )
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                CitizenHomeScreen()
            }
        }
    }
}

@Preview(
    showBackground = true,
    heightDp = 820,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Home Screen - Dark"
)
@Composable
fun PreviewCitizenHomeDark() {
    CitizenSPTheme(darkTheme = true) {
        Scaffold(
            topBar = { CitizenAppTopBar(currentScreen = "home") },
            bottomBar = {
                CitizenBottomNavBar(
                    currentScreen = "home",
                    onSelectScreen = {},
                    onOpenReportSheet = {}
                )
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                CitizenHomeScreen()
            }
        }
    }
}
