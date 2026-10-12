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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.projetorevisao.data.remote.SspDistrictTelemetry
import com.example.projetorevisao.data.remote.WeatherSafetyTelemetry
import com.example.projetorevisao.ui.components.CitizenAppTopBar
import com.example.projetorevisao.ui.components.CitizenBottomNavBar
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.components.CitizenInteractiveMapContainer
import com.example.projetorevisao.ui.theme.BrandBlue
import com.example.projetorevisao.ui.theme.BrandBlueHero
import com.example.projetorevisao.ui.theme.BrandOrange
import com.example.projetorevisao.ui.theme.CitizenSPTheme
import com.example.projetorevisao.ui.theme.CitizenStatusColors
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import com.example.projetorevisao.ui.viewmodel.IncidentAlert
import com.example.projetorevisao.ui.viewmodel.IncidentCategory
import com.example.projetorevisao.ui.viewmodel.IncidentStatus
import com.example.projetorevisao.ui.viewmodel.rememberCitizenViewModel

@Composable
fun CitizenHomeScreen(
    viewModel: CitizenViewModel = rememberCitizenViewModel(),
    onSosClick: () -> Unit = {},
    onOpenReportSheet: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
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
        // Neighborhood Selector Chips + Quick Search Toggle
        item {
            NeighborhoodSelectorRow(
                neighborhoods = viewModel.availableNeighborhoods,
                selected = viewModel.selectedNeighborhood,
                showSearch = showSearchField,
                onToggleSearch = { showSearchField = !showSearchField },
                onSelect = { viewModel.selectNeighborhood(it) }
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
                neighborhood = viewModel.selectedNeighborhood,
                incidents = viewModel.incidents,
                showHeatmap = viewModel.showHeatmapOverlay,
                smartSampaCameras = viewModel.districtTelemetry.smartSampaCamerasActive,
                onToggleHeatmap = { viewModel.toggleHeatmap() },
                onPinClick = { viewModel.openIncidentDetail(it) },
                onReportClick = onOpenReportSheet
            )
        }

        // Compact Live Weather (Open-Meteo/INMET/CGE-SP) & SSP-SP Smart Sampa Banner (No text wrapping!)
        item {
            PublicSafetyApisLiveBanner(
                weather = viewModel.weatherTelemetry,
                district = viewModel.districtTelemetry,
                isRefreshing = viewModel.isRefreshingApis,
                onRefresh = { viewModel.refreshPublicApis() }
            )
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
                        text = "Alertas Recentes",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${viewModel.incidents.size} ocorrências verificadas na sua região",
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
    }

    if (selectedIncident != null) {
        IncidentDetailBottomSheet(
            incident = selectedIncident,
            onDismiss = { viewModel.openIncidentDetail(null) },
            onConfirm = { viewModel.confirmIncident(selectedIncident.id) },
            onSosClick = {
                viewModel.openIncidentDetail(null)
                onSosClick()
            }
        )
    }
}

@Composable
private fun NeighborhoodSelectorRow(
    neighborhoods: List<String>,
    selected: String,
    showSearch: Boolean,
    onToggleSearch: () -> Unit,
    onSelect: (String) -> Unit
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
                    contentDescription = "Buscar endereço",
                    tint = if (showSearch) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            neighborhoods.forEach { item ->
                val isSelected = item == selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(item) },
                    label = {
                        Text(
                            text = item,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = CitizenIcons.LocationPin,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlue.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        selectedLeadingIconColor = BrandOrange
                    )
                )
            }
        }
    }
}

@Composable
private fun InteractiveIncidentMapCard(
    neighborhood: String,
    incidents: List<IncidentAlert>,
    showHeatmap: Boolean,
    smartSampaCameras: Int,
    onToggleHeatmap: () -> Unit,
    onPinClick: (IncidentAlert) -> Unit,
    onReportClick: () -> Unit
) {
    CitizenInteractiveMapContainer(
        neighborhood = neighborhood,
        incidents = incidents,
        showHeatmap = showHeatmap,
        smartSampaCameras = smartSampaCameras,
        onToggleHeatmap = onToggleHeatmap,
        onPinClick = onPinClick,
        onReportClick = onReportClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    )
}

@Composable
private fun PublicSafetyApisLiveBanner(
    weather: WeatherSafetyTelemetry,
    district: SspDistrictTelemetry,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
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
                        text = "CLIMA & DEFESA CIVIL (${weather.inmetAlertLevel})",
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
                            text = if (isRefreshing) "Sincronizando" else "API Ao Vivo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

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
                        text = "${district.dpName} • ${district.militaryBattalion}",
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
    val statusColor = when (status) {
        IncidentStatus.REPORTADO -> BrandOrange
        IncidentStatus.EM_ATENDIMENTO -> BrandBlueHero
        IncidentStatus.CONTROLADO -> CitizenStatusColors.safe
    }

    Surface(
        shape = RoundedCornerShape(9.dp),
        color = statusColor.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.30f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "STATUS: ${status.label.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = authority,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.16f)
            ) {
                Text(
                    text = "Etapa ${status.stepIndex}/3",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncidentDetailBottomSheet(
    incident: IncidentAlert,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onSosClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
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
                            text = "Recomendação Preventiva (Google Cloud Gemini IA)",
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
                    Text("Confirmar (${incident.confirmations})")
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
