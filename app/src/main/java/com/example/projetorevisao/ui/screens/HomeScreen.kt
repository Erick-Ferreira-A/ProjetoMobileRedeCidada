package com.example.projetorevisao.ui.screens

import android.content.res.Configuration
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

@Composable
fun CitizenHomeScreen(
    viewModel: CitizenViewModel = rememberCitizenViewModel(),
    onSosClick: () -> Unit = {},
    onOpenReportSheet: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onCenterOnUser: () -> Unit = {}
) {
    val selectedIncident = viewModel.selectedIncidentForDetail
    var showSearchField by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Onde você está + busca
        item {
            NeighborhoodSelectorRow(
                areaLabel = viewModel.areaLabel,
                hasLocation = viewModel.userLatitude != null,
                showSearch = showSearchField,
                onToggleSearch = { showSearchField = !showSearchField },
                onCenterOnUser = onCenterOnUser
            )
        }

        if (showSearchField) {
            item {
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Buscar rua, estação de metrô ou tipo de alerta...") },
                    leadingIcon = {
                        Icon(
                            imageVector = CitizenIcons.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Interactive Map View Card (Figma 32:781)
        item {
            InteractiveIncidentMapCard(
                neighborhood = viewModel.areaLabel,
                incidents = viewModel.incidents,
                showHeatmap = viewModel.showHeatmapOverlay,
                onToggleHeatmap = { viewModel.toggleHeatmap() },
                onPinClick = { viewModel.openIncidentDetail(it) },
                onCenterOnUser = onCenterOnUser,
                userLatitude = viewModel.userLatitude,
                userLongitude = viewModel.userLongitude,
                userAreaLabel = viewModel.userAreaLabel,
                userLocationVersion = viewModel.userLocationVersion
            )
        }

        // Compact Live Weather (Open-Meteo/INMET/CGE-SP) & SSP-SP Smart Sampa Banner (No text wrapping!)
        item {
            PublicSafetyApisLiveBanner(
                weather = viewModel.weatherTelemetry,
                isRefreshing = viewModel.isRefreshingApis,
                onRefresh = { viewModel.refreshPublicApis() }
            )
        }

        viewModel.incidentActionMessage?.let { message ->
            item(key = "action-message") {
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(5_000)
                    viewModel.clearIncidentActionMessage()
                }
                Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        viewModel.userNotice?.let { notice ->
            item {
                Text(
                    text = notice,
                    color = when (viewModel.userNoticeTone) {
                        NoticeTone.SUCCESS -> CitizenStatusColors.safe
                        NoticeTone.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
                        NoticeTone.ERROR -> MaterialTheme.colorScheme.error
                    },
                    fontWeight = if (viewModel.userNoticeTone == NoticeTone.SUCCESS) FontWeight.SemiBold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Category Filter Chips Row
        item {
            CategoryFilterChipsRow(
                selectedCategory = viewModel.selectedCategoryFilter,
                onSelectCategory = { viewModel.selectCategoryFilter(it) }
            )
        }

        // Section Title "Alertas Recentes" + Quick "+ Reportar" CTA (Figma 32:656)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Relatos da comunidade",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = when (viewModel.incidents.size) {
                            0 -> "Nenhum relato por enquanto"
                            1 -> "1 relato"
                            else -> "${viewModel.incidents.size} relatos"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Enviados por moradores. Não são confirmados pela polícia.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandOrange,
                    modifier = Modifier.clickable(onClick = onOpenReportSheet)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = CitizenIcons.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reportar",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Incident Alert Cards (Figma 32:658) — now immediately visible above the fold!
        items(viewModel.incidents, key = { it.id }) { incident ->
            FigmaIncidentAlertCard(
                incident = incident,
                onCardClick = { viewModel.openIncidentDetail(incident) },
                onConfirmClick = { viewModel.confirmIncident(incident.id) }
            )
        }

        if (viewModel.incidents.isEmpty()) {
            item {
                Text(
                    text = if (viewModel.searchQuery.isNotBlank() || viewModel.selectedCategoryFilter != null) {
                        "Nenhum relato com esse filtro."
                    } else {
                        "Ainda não há relatos nesta região. Viu algo? Toque em Reportar para avisar a vizinhança."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
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

@Composable
private fun NeighborhoodSelectorRow(
    areaLabel: String,
    hasLocation: Boolean,
    showSearch: Boolean,
    onToggleSearch: () -> Unit,
    onCenterOnUser: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (showSearch) BrandBlue else MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .size(36.dp)
                .clickable(onClick = onToggleSearch)
        ) {
            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = CitizenIcons.Search,
                    contentDescription = "Buscar relatos",
                    tint = if (showSearch) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = BrandBlue.copy(alpha = 0.12f),
            modifier = Modifier
                .weight(1f)
                .clickable(onClickLabel = "Atualizar minha localização", onClick = onCenterOnUser)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = CitizenIcons.LocationPin,
                    contentDescription = null,
                    tint = BrandOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (hasLocation) "Perto de você • $areaLabel" else "Buscando sua localização…",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
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
    userLocationVersion: Int
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
            .height(210.dp)
    )
}

@Composable
private fun PublicSafetyApisLiveBanner(
    weather: WeatherSafetyTelemetry,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    if (!weather.isLiveNetworkData) {
        WeatherUnavailableCard(isRefreshing = isRefreshing, onRefresh = onRefresh)
        return
    }
    val riskLabel = when (weather.inmetAlertLevel) {
        "LARANJA", "VERMELHO" -> "ALTO"
        "AMARELO" -> "MÉDIO"
        else -> "BAIXO"
    }
    val alertColor = when (weather.inmetAlertLevel) {
        "LARANJA", "VERMELHO" -> BrandOrange
        "AMARELO" -> CitizenStatusColors.warning
        else -> CitizenStatusColors.safe
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, alertColor.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Single non-wrapping header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = CitizenIcons.Storm,
                        contentDescription = null,
                        tint = alertColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RISCO DE CHUVA E VENTO: $riskLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = alertColor,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.clickable(onClick = onRefresh)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = CitizenIcons.CloudSync,
                            contentDescription = "Sincronizar APIs",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRefreshing) "Atualizando…" else "Atualizar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                text = "Estimativa do app com o tempo medido agora (Open-Meteo). Não substitui os alertas da Defesa Civil.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${String.format("%.1f", weather.temperatureCelsius)}°C • ${weather.conditionLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Atualizado às ${weather.lastUpdatedTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Text(
                        text = "${weather.precipitationMm}mm • ${weather.windGustsKmh.toInt()}km/h",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterChipsRow(
    selectedCategory: IncidentCategory?,
    onSelectCategory: (IncidentCategory?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onSelectCategory(null) },
            label = { Text("Todos", fontWeight = FontWeight.Bold) }
        )
        IncidentCategory.entries.forEach { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(category) },
                label = {
                    Text(
                        text = category.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = categoryIcon(category),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.badgeColor.copy(alpha = 0.18f),
                    selectedLabelColor = category.badgeColor,
                    selectedLeadingIconColor = category.badgeColor
                )
            )
        }
    }
}

@Composable
private fun FigmaIncidentAlertCard(
    incident: IncidentAlert,
    onCardClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Badges Row (Figma 32:661 - Category Pill + Green "✓ Verificado" Pill)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = incident.category.badgeColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = categoryIcon(incident.category),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = incident.effectiveBadgeTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    if (incident.verified) {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = CitizenStatusColors.verifiedBadgeBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = CitizenIcons.VerifiedCheck,
                                    contentDescription = null,
                                    tint = CitizenStatusColors.safe,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Verificado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CitizenStatusColors.safe,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (incident.isAnonymous) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = CitizenIcons.VisibilityOff,
                            contentDescription = "Relato Anônimo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Anônimo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Title & Description (Figma 32:670)
            Text(
                text = incident.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = incident.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Address Row (Figma 32:672)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = CitizenIcons.LocationPin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = incident.address,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Incident Status Feedback Bar (Addressing Figma UX Pain Point: "Falta de Retorno")
            IncidentStatusFeedbackBar(
                status = incident.status,
                authority = incident.authorityAssigned
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Footer Metadata ("2 min atrás • 500m de você") + Confirm Action (Figma 32:681)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${incident.timeAgo}  •  ${incident.distanceLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = if (incident.userConfirmed) {
                        BrandOrange.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    border = BorderStroke(
                        1.dp,
                        if (incident.userConfirmed) BrandOrange else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.clickable(onClick = onConfirmClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = CitizenIcons.ThumbUp,
                            contentDescription = "Confirmar ocorrência",
                            tint = if (incident.userConfirmed) BrandOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${incident.confirmations} confirmam",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (incident.userConfirmed) BrandOrange else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
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
                    color = incident.category.badgeColor
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

@Composable
private fun WeatherUnavailableCard(isRefreshing: Boolean, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = CitizenIcons.Storm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRefreshing) "Consultando o clima…" else "Clima indisponível agora.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (!isRefreshing) {
                androidx.compose.material3.TextButton(onClick = onRefresh) { Text("Tentar de novo") }
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
