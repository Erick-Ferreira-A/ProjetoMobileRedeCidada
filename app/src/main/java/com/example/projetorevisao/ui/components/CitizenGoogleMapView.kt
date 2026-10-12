package com.example.projetorevisao.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.projetorevisao.BuildConfig
import com.example.projetorevisao.ui.screens.categoryIcon
import com.example.projetorevisao.ui.theme.BrandBlue
import com.example.projetorevisao.ui.theme.BrandBlueHero
import com.example.projetorevisao.ui.theme.BrandNavyDark
import com.example.projetorevisao.ui.theme.BrandOrange
import com.example.projetorevisao.ui.viewmodel.IncidentAlert
import com.example.projetorevisao.ui.viewmodel.IncidentCategory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.roundToInt

private fun centerLatLngForNeighborhood(neighborhood: String): LatLng {
    return when {
        neighborhood.contains("Capão Redondo", ignoreCase = true) -> LatLng(-23.6628, -46.7682)
        neighborhood.contains("Paulista", ignoreCase = true) -> LatLng(-23.5614, -46.6559)
        neighborhood.contains("Pinheiros", ignoreCase = true) -> LatLng(-23.5642, -46.6894)
        else -> LatLng(-23.5505, -46.6333)
    }
}

private fun markerHueForCategory(category: IncidentCategory): Float {
    return when (category) {
        IncidentCategory.ROUBO_FURTO -> BitmapDescriptorFactory.HUE_RED
        IncidentCategory.INCENDIO -> BitmapDescriptorFactory.HUE_ORANGE
        IncidentCategory.ALAGAMENTO -> BitmapDescriptorFactory.HUE_AZURE
        IncidentCategory.TEMPESTADE -> BitmapDescriptorFactory.HUE_YELLOW
        IncidentCategory.TRANSITO -> BitmapDescriptorFactory.HUE_VIOLET
        IncidentCategory.OPERACAO_POLICIAL -> BitmapDescriptorFactory.HUE_BLUE
        IncidentCategory.UTILIDADE_PUBLICA -> BitmapDescriptorFactory.HUE_GREEN
    }
}

@Composable
fun CitizenInteractiveMapContainer(
    neighborhood: String,
    incidents: List<IncidentAlert>,
    showHeatmap: Boolean,
    smartSampaCameras: Int,
    onToggleHeatmap: () -> Unit,
    onPinClick: (IncidentAlert) -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    val hasGoogleMapsKey = BuildConfig.MAPS_API_KEY.isNotBlank() &&
        BuildConfig.MAPS_API_KEY != "YOUR_MAPS_API_KEY"

    // Allows switching between Google Maps SDK / Live Street Tiles / Vector Radar
    var useLiveWebTilesWhenNoKey by remember { mutableStateOf(true) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                !isPreview && hasGoogleMapsKey -> {
                    NativeGoogleMapsSdkView(
                        neighborhood = neighborhood,
                        incidents = incidents,
                        showHeatmap = showHeatmap,
                        onPinClick = onPinClick
                    )
                }
                !isPreview && useLiveWebTilesWhenNoKey -> {
                    LiveCartographicWebMapView(
                        neighborhood = neighborhood,
                        incidents = incidents,
                        showHeatmap = showHeatmap
                    )
                    // Overlay clickable pins so tapping opens the native Compose bottom sheet
                    ClickablePinsOverlay(
                        incidents = incidents,
                        onPinClick = onPinClick
                    )
                }
                else -> {
                    DetailedCartographicCanvasMap(
                        neighborhood = neighborhood,
                        incidents = incidents,
                        showHeatmap = showHeatmap,
                        onPinClick = onPinClick
                    )
                }
            }

            // Top-left Live Radar & Map Engine Status Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.68f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clickable {
                        if (!hasGoogleMapsKey) {
                            useLiveWebTilesWhenNoKey = !useLiveWebTilesWhenNoKey
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34D399))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasGoogleMapsKey) {
                            "Google Maps SDK Ativo • $smartSampaCameras câmeras"
                        } else {
                            "Mapa SP Ao Vivo • $smartSampaCameras câmeras Smart Sampa"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Right-side Map Controls (Figma 32:824)
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
                color = Color.White.copy(alpha = 0.95f),
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

/**
 * Renderização oficial usando Google Maps SDK for Android (maps-compose) quando MAPS_API_KEY está configurada.
 */
@Composable
private fun NativeGoogleMapsSdkView(
    neighborhood: String,
    incidents: List<IncidentAlert>,
    showHeatmap: Boolean,
    onPinClick: (IncidentAlert) -> Unit
) {
    val targetLatLng = centerLatLngForNeighborhood(neighborhood)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(targetLatLng, 14.5f)
    }

    LaunchedEffect(neighborhood) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(targetLatLng, 14.5f),
            durationMs = 700
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isTrafficEnabled = true,
            isBuildingEnabled = true
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true
        )
    ) {
        // Linha 5-Lilás / Corredor Seguro Polyline
        Polyline(
            points = listOf(
                LatLng(-23.6593, -46.7678), // Estação Capão Redondo
                LatLng(-23.6490, -46.7580), // Campo Limpo
                LatLng(-23.6405, -46.7485)  // Vila das Belezas
            ),
            color = Color(0xFF9B3894),
            width = 10f
        )

        incidents.forEach { alert ->
            val pos = LatLng(alert.latitude, alert.longitude)

            if (showHeatmap) {
                Circle(
                    center = pos,
                    radius = 280.0,
                    fillColor = alert.category.badgeColor.copy(alpha = 0.24f),
                    strokeColor = alert.category.badgeColor.copy(alpha = 0.7f),
                    strokeWidth = 3f
                )
            }

            Marker(
                state = MarkerState(position = pos),
                title = alert.title,
                snippet = "${alert.effectiveBadgeTitle} • ${alert.distanceLabel}",
                icon = BitmapDescriptorFactory.defaultMarker(markerHueForCategory(alert.category)),
                onClick = {
                    onPinClick(alert)
                    false
                }
            )
        }
    }
}

/**
 * Mapa cartográfico real das ruas de São Paulo (Leaflet + CartoDB/OpenStreetMap tiles)
 * que funciona imediatamente no emulador/celular mesmo sem chave de faturamento do Google Cloud.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LiveCartographicWebMapView(
    neighborhood: String,
    incidents: List<IncidentAlert>,
    showHeatmap: Boolean
) {
    val center = centerLatLngForNeighborhood(neighborhood)

    val markersJs = incidents.joinToString(separator = "\n") { alert ->
        val hexColor = String.format("#%06X", 0xFFFFFF and alert.category.badgeColor.value.toLong().shr(32).toInt())
        """
        L.circleMarker([${alert.latitude}, ${alert.longitude}], {
            radius: 10,
            fillColor: "$hexColor",
            color: "#FFFFFF",
            weight: 2,
            opacity: 1,
            fillOpacity: 0.95
        }).addTo(map).bindPopup("<b>${alert.title.replace("\"", "'")}</b><br>${alert.address.replace("\"", "'")}");
        if ($showHeatmap) {
            L.circle([${alert.latitude}, ${alert.longitude}], {
                radius: 260,
                fillColor: "$hexColor",
                color: "$hexColor",
                weight: 1,
                fillOpacity: 0.22
            }).addTo(map);
        }
        """.trimIndent()
    }

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #101C33; }
                .leaflet-control-attribution { display: none !important; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { zoomControl: false }).setView([${center.latitude}, ${center.longitude}], 14);
                L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                    maxZoom: 19
                }).addTo(map);
                $markersJs
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://unpkg.com", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://unpkg.com", htmlContent, "text/html", "UTF-8", null)
        }
    )
}

/**
 * Mapa cartográfico vetorial detalhado para o @Preview do Android Studio e modo offline,
 * exibindo os rótulos reais das avenidas de São Paulo e os pins interativos do Figma (32:781).
 */
@Composable
private fun DetailedCartographicCanvasMap(
    neighborhood: String,
    incidents: List<IncidentAlert>,
    showHeatmap: Boolean,
    onPinClick: (IncidentAlert) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
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

            // Green Park Zone (Parque Santo Dias / Parque Trianon)
            drawRoundRect(
                color = Color(0xFF10B981).copy(alpha = 0.16f),
                topLeft = Offset(size.width * 0.66f, size.height * 0.08f),
                size = Size(size.width * 0.29f, size.height * 0.34f),
                cornerRadius = CornerRadius(24f, 24f)
            )

            // Urban blocks & secondary streets
            val gridColor = Color.White.copy(alpha = 0.09f)
            for (i in 1..5) {
                val y = size.height * (i / 6f)
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 2.2f)
            }
            for (i in 1..6) {
                val x = size.width * (i / 7f)
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2.2f)
            }

            // Main Arterial Avenue (Estrada de Itapecerica / Av. Paulista)
            drawLine(
                color = Color(0xFF38BDF8).copy(alpha = 0.34f),
                start = Offset(0f, size.height * 0.72f),
                end = Offset(size.width, size.height * 0.22f),
                strokeWidth = 10f
            )

            // Av. Carlos Caldeira Filho / Rua da Consolação
            drawLine(
                color = Color(0xFFF59E0B).copy(alpha = 0.28f),
                start = Offset(size.width * 0.15f, 0f),
                end = Offset(size.width * 0.65f, size.height),
                strokeWidth = 7f
            )

            // Metro Line 5-Lilás Dashed Track
            drawLine(
                color = Color(0xFFC084FC).copy(alpha = 0.65f),
                start = Offset(size.width * 0.06f, size.height * 0.30f),
                end = Offset(size.width * 0.94f, size.height * 0.58f),
                strokeWidth = 5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
            )

            // Heatmap overlays
            if (showHeatmap) {
                incidents.forEach { alert ->
                    drawCircle(
                        color = alert.category.badgeColor.copy(alpha = 0.25f),
                        radius = 70f,
                        center = Offset(
                            size.width * alert.mapXFraction,
                            size.height * alert.mapYFraction
                        )
                    )
                }
            }

            // User GPS position pulse dot
            val userCenter = Offset(size.width * 0.46f, size.height * 0.50f)
            drawCircle(
                color = BrandBlueHero.copy(alpha = 0.32f),
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

        // Street & Landmark Labels so the map looks unmistakably like São Paulo in @Preview & screenshots
        val isCapao = neighborhood.contains("Capão", ignoreCase = true)
        MapStreetLabel(
            text = if (isCapao) "Metrô Capão Redondo (L5-Lilás)" else "Metrô Trianon-Masp (L2-Verde)",
            color = Color(0xFFE9D5FF),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 48.dp)
        )
        MapStreetLabel(
            text = if (isCapao) "Pq. Santo Dias" else "Pq. Trianon",
            color = Color(0xFF6EE7B7),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 62.dp, top = 26.dp)
        )
        MapStreetLabel(
            text = if (isCapao) "Est. de Itapecerica" else "Av. Paulista",
            color = Color(0xFFBAE6FD),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 46.dp)
        )

        ClickablePinsOverlay(
            incidents = incidents,
            onPinClick = onPinClick
        )
    }
}

@Composable
private fun MapStreetLabel(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.Black.copy(alpha = 0.48f),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ClickablePinsOverlay(
    incidents: List<IncidentAlert>,
    onPinClick: (IncidentAlert) -> Unit
) {
    BoxWithConstraints(
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
}
