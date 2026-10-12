package com.example.projetorevisao.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetorevisao.data.remote.SspDistrictTelemetry
import com.example.projetorevisao.data.remote.WeatherSafetyTelemetry
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.components.RedeCidadaLogo
import com.example.projetorevisao.ui.theme.BrandBlue
import com.example.projetorevisao.ui.theme.BrandBlueHero
import com.example.projetorevisao.ui.theme.BrandNavyDark
import com.example.projetorevisao.ui.theme.BrandOrange
import com.example.projetorevisao.ui.theme.CitizenSPTheme
import com.example.projetorevisao.ui.theme.CitizenStatusColors
import com.example.projetorevisao.ui.viewmodel.CitizenViewModel
import com.example.projetorevisao.ui.viewmodel.IncidentAlert
import com.example.projetorevisao.ui.viewmodel.IncidentCategory
import com.example.projetorevisao.ui.viewmodel.IncidentStatus
import com.example.projetorevisao.ui.viewmodel.rememberCitizenViewModel
import kotlin.math.roundToInt

@Composable
fun CitizenHomeScreen(
    viewModel: CitizenViewModel = rememberCitizenViewModel(),
    onSosClick: () -> Unit = {},
    onOpenReportSheet: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val selectedIncident = viewModel.selectedIncidentForDetail

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar + Segmented Tabs (Figma 32:608 & 32:634)
        HomeTopHeaderAndTabs(
            activeTab = viewModel.activeHomeTab,
            onSelectTab = { viewModel.setHomeTab(it) },
            onSosClick = onSosClick,
            onOpenProfile = onOpenProfile
        )

        if (viewModel.activeHomeTab == "reels") {
            CitizenReelsScreen(
                viewModel = viewModel,
                onOpenReportSheet = onOpenReportSheet
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Neighborhood Switcher Pills
                item {
                    NeighborhoodSelectorRow(
                        neighborhoods = viewModel.availableNeighborhoods,
                        selected = viewModel.selectedNeighborhood,
                        onSelect = { viewModel.selectNeighborhood(it) }
                    )
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

                // Live Weather (Open-Meteo/INMET/CGE-SP) & SSP-SP Smart Sampa API Card
                item {
                    PublicSafetyApisLiveBanner(
                        weather = viewModel.weatherTelemetry,
                        district = viewModel.districtTelemetry,
                        isRefreshing = viewModel.isRefreshingApis,
                        onRefresh = { viewModel.refreshPublicApis() }
                    )
                }

                // Search & Category Filters
                item {
                    SearchAndCategoryFilterSection(
                        searchQuery = viewModel.searchQuery,
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        selectedCategory = viewModel.selectedCategoryFilter,
                        onSelectCategory = { viewModel.selectCategoryFilter(it) }
                    )
                }

                // Section Title "Alertas Recentes" (Figma 32:656)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Alertas Recentes",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${viewModel.incidents.size} ocorrências monitoradas • Toque para detalhes e rota segura",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CitizenStatusColors.verifiedBadgeBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = CitizenIcons.Shield,
                                    contentDescription = null,
                                    tint = CitizenStatusColors.safe,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Score ${viewModel.userTrustScore}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CitizenStatusColors.safe,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Incident Alert Cards (Figma 32:658)
                items(viewModel.incidents, key = { it.id }) { incident ->
                    FigmaIncidentAlertCard(
                        incident = incident,
                        onCardClick = { viewModel.openIncidentDetail(incident) },
                        onConfirmClick = { viewModel.confirmIncident(incident.id) }
                    )
                }
            }
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
private fun HomeTopHeaderAndTabs(
    activeTab: String,
    onSelectTab: (String) -> Unit,
    onSosClick: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RedeCidadaLogo(size = 38.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Rede Cidadã",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "São Paulo • SSP-SP, Metrô & CGE Live",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CitizenStatusColors.critical.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, CitizenStatusColors.critical.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable(onClick = onSosClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = CitizenIcons.SosEmergency,
                                contentDescription = "SOS 190",
                                tint = CitizenStatusColors.critical,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "190",
                                style = MaterialTheme.typography.labelMedium,
                                color = CitizenStatusColors.critical,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenProfile,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = CitizenIcons.Person,
                            contentDescription = "Perfil e APIs Conectadas",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Segmented Switcher "Feed & Mapa | Reels" (Figma 32:634)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SegmentedTopTabButton(
                    selected = activeTab == "feed",
                    icon = CitizenIcons.Map,
                    title = "Feed & Mapa",
                    onClick = { onSelectTab("feed") },
                    modifier = Modifier.weight(1f)
                )
                SegmentedTopTabButton(
                    selected = activeTab == "reels",
                    icon = CitizenIcons.Reels,
                    title = "Reels de Segurança",
                    onClick = { onSelectTab("reels") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SegmentedTopTabButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = if (selected) BrandBlue else Color.Transparent,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NeighborhoodSelectorRow(
    neighborhoods: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = CitizenIcons.LocationPin,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(255.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Vector Urban Map Canvas (Figma 32:781)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF101C33),
                            Color(0xFF162747),
                            Color(0xFF0E182C)
                        )
                    )
                )

                // Green Park Zone (Parque Santo Dias / Urban Park)
                drawRoundRect(
                    color = Color(0xFF10B981).copy(alpha = 0.14f),
                    topLeft = Offset(size.width * 0.68f, size.height * 0.08f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.28f, size.height * 0.34f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
                )

                // Street grid lines
                val gridColor = Color.White.copy(alpha = 0.08f)
                for (i in 1..5) {
                    val y = size.height * (i / 6f)
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
                }
                for (i in 1..6) {
                    val x = size.width * (i / 7f)
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2f)
                }

                // Main Arterial Avenue (Estrada de Itapecerica / Av. Carlos Caldeira Filho)
                drawLine(
                    color = Color(0xFF38BDF8).copy(alpha = 0.30f),
                    start = Offset(0f, size.height * 0.72f),
                    end = Offset(size.width, size.height * 0.22f),
                    strokeWidth = 9f
                )

                // Metro Line 5-Lilás Dashed Track
                drawLine(
                    color = Color(0xFFC084FC).copy(alpha = 0.55f),
                    start = Offset(size.width * 0.08f, size.height * 0.30f),
                    end = Offset(size.width * 0.92f, size.height * 0.58f),
                    strokeWidth = 5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                )

                // Heatmap zones when enabled
                if (showHeatmap) {
                    incidents.forEach { alert ->
                        drawCircle(
                            color = alert.category.badgeColor.copy(alpha = 0.24f),
                            radius = 68f,
                            center = Offset(
                                size.width * alert.mapXFraction,
                                size.height * alert.mapYFraction
                            )
                        )
                    }
                }

                // User current GPS pulse dot in the center
                val userCenter = Offset(size.width * 0.46f, size.height * 0.50f)
                drawCircle(
                    color = BrandBlueHero.copy(alpha = 0.30f),
                    radius = 28f,
                    center = userCenter
                )
                drawCircle(
                    color = Color.White,
                    radius = 9f,
                    center = userCenter
                )
                drawCircle(
                    color = BrandBlueHero,
                    radius = 6f,
                    center = userCenter
                )
            }

            // Top-left Live Radar & Smart Sampa Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.62f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background( Color(0xFF34D399))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Google Maps + Smart Sampa ($smartSampaCameras câmeras)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Clickable Incident Pins (Figma 32:796)
            androidx.compose.foundation.layout.BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val mapWidthPx = constraints.maxWidth
                val mapHeightPx = constraints.maxHeight

                incidents.forEach { alert ->
                    val pinX = (mapWidthPx * alert.mapXFraction).roundToInt() - 36
                    val pinY = (mapHeightPx * alert.mapYFraction).roundToInt() - 36

                    Surface(
                        shape = CircleShape,
                        color = alert.category.badgeColor,
                        border = BorderStroke(2.dp, Color.White),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .offset { IntOffset(pinX, pinY) }
                            .size(34.dp)
                            .clickable { onPinClick(alert) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = categoryIcon(alert.category),
                                contentDescription = alert.title,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Right-side Map Controls (Figma 32:824 - MyLocation & Heatmap Layers)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable(onClick = onToggleHeatmap)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = CitizenIcons.Layers,
                            contentDescription = "Alternar Mancha de Calor",
                            tint = if (showHeatmap) BrandOrange else BrandNavyDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable(onClick = onReportClick)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = CitizenIcons.Add,
                            contentDescription = "Adicionar Alerta no Mapa",
                            tint = BrandBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Bottom Location Pill (Figma 32:838 "Capão Redondo, São Paulo - Zona Sul")
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.94f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = CitizenIcons.LocationPin,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = neighborhood,
                        style = MaterialTheme.typography.labelLarge,
                        color = BrandNavyDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, alertColor.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = CitizenIcons.Storm,
                        contentDescription = null,
                        tint = alertColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CLIMA & DEFESA CIVIL SP (${weather.inmetAlertLevel})",
                        style = MaterialTheme.typography.labelMedium,
                        color = alertColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                TextButton(
                    onClick = onRefresh,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = CitizenIcons.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRefreshing) "Sincronizando..." else "Atualizar (${weather.lastUpdatedTime})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = weather.cgeFloodRiskLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Chuva: ${weather.precipitationMm}mm",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rajadas: ${weather.windGustsKmh.toInt()} km/h",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // SSP-SP Jurisdição Policial Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = CitizenIcons.PoliceShield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${district.dpName} • ${district.militaryBattalion}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${district.securityTotemsCount} Totens SP",
                    style = MaterialTheme.typography.labelSmall,
                    color = CitizenStatusColors.safe,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SearchAndCategoryFilterSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: IncidentCategory?,
    onSelectCategory: (IncidentCategory?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
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
                            modifier = Modifier.size(16.dp)
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Badges Row (Figma 32:661 - Category Pill + Green "✓ Verificado" Pill)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
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
                                modifier = Modifier.size(13.dp)
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
                            shape = RoundedCornerShape(8.dp),
                            color = CitizenStatusColors.verifiedBadgeBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = CitizenIcons.VerifiedCheck,
                                    contentDescription = null,
                                    tint = CitizenStatusColors.safe,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
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
                            modifier = Modifier.size(14.dp)
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
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = incident.description,
                style = MaterialTheme.typography.bodyMedium,
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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = incident.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
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
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
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
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = CitizenIcons.ThumbUp,
                            contentDescription = "Confirmar ocorrência",
                            tint = if (incident.userConfirmed) BrandOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${incident.confirmations} confirmam",
                            style = MaterialTheme.typography.labelMedium,
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
        shape = RoundedCornerShape(10.dp),
        color = statusColor.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.30f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STATUS: ${status.label.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Etapa ${status.stepIndex}/3",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = authority,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

            // Google Cloud Vertex AI / Gemini Preventive Recommendation
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

@Preview(showBackground = true, name = "Home Screen - Light")
@Composable
fun PreviewCitizenHomeLight() {
    CitizenSPTheme(darkTheme = false) {
        CitizenHomeScreen()
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Home Screen - Dark"
)
@Composable
fun PreviewCitizenHomeDark() {
    CitizenSPTheme(darkTheme = true) {
        CitizenHomeScreen()
    }
}
