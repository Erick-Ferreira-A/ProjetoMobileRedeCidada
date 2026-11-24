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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
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
import com.example.projetorevisao.data.location.hasLocationPermission
import com.example.projetorevisao.ui.viewmodel.IncidentAlert
import androidx.compose.ui.platform.LocalContext
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
    onToggleHeatmap: () -> Unit,
    onPinClick: (IncidentAlert) -> Unit,
    onCenterOnUser: () -> Unit,
    modifier: Modifier = Modifier,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    userAreaLabel: String? = null,
    userLocationVersion: Int = 0,
    /** Mapa de tela inteira (Início): sem card, sem camadas/mancha de calor e sem rótulo de bairro. */
    fullBleed: Boolean = false,
    /** Espaço livre embaixo (ex.: painel recolhido do Início) para o botão de localização não ficar coberto. */
    controlsBottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val isPreview = LocalInspectionMode.current
    val hasGoogleMapsKey = BuildConfig.MAPS_API_KEY.isNotBlank() &&
        BuildConfig.MAPS_API_KEY != "YOUR_MAPS_API_KEY"

    var preferCartographicCanvas by remember { mutableStateOf(false) }

    // Filter pins for the active neighborhood so pins are clean and well-spaced
    val mapIncidents = remember(incidents) { incidents.take(100) }

    val mapFrame: @Composable (@Composable () -> Unit) -> Unit = { content ->
        if (fullBleed) {
            Box(modifier) { content() }
        } else {
            Card(
                modifier = modifier,
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) { content() }
        }
    }
    mapFrame {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                !isPreview && hasGoogleMapsKey && !preferCartographicCanvas -> {
                    NativeGoogleMapsSdkView(
                        neighborhood = neighborhood,
                        incidents = mapIncidents,
                        showHeatmap = showHeatmap,
                        onPinClick = onPinClick,
                        userLatLng = if (userLatitude != null && userLongitude != null) LatLng(userLatitude, userLongitude) else null,
                        userLocationVersion = userLocationVersion
                    )
                }
                !isPreview && !preferCartographicCanvas -> {
                    LiveCartographicWebMapView(
                        neighborhood = neighborhood,
                        incidents = mapIncidents,
                        showHeatmap = showHeatmap,
                        userLatitude = userLatitude,
                        userLongitude = userLongitude,
                        onPinClick = onPinClick
                    )
                }
                else -> {
                    DetailedCartographicCanvasMap(
                        neighborhood = neighborhood,
                        incidents = mapIncidents,
                        showHeatmap = showHeatmap,
                        onPinClick = onPinClick
                    )
                }
            }

            // Controles do mapa. Em tela cheia: só "minha localização", acima do painel de baixo.
            Column(
                modifier = if (fullBleed) {
                    Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = controlsBottomPadding + 16.dp)
                } else {
                    Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)
                },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!fullBleed) Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onToggleHeatmap)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = CitizenIcons.Layers,
                            contentDescription = "Alternar Mancha de Calor",
                            tint = if (showHeatmap) BrandOrange else BrandNavyDark,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(if (fullBleed) 48.dp else 36.dp)
                        .clickable(onClickLabel = "Centralizar na minha localização", onClick = onCenterOnUser)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = CitizenIcons.MyLocation,
                            contentDescription = "Minha localização",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (fullBleed) 22.dp else 19.dp)
                        )
                    }
                }
            }

            // Bottom Location Pill (Figma 32:838) — no Início em tela cheia o bairro já está no cabeçalho.
            if (!fullBleed) Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = CitizenIcons.LocationPin,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = userAreaLabel ?: neighborhood,
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandNavyDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NativeGoogleMapsSdkView(
    neighborhood: String,
    incidents: List<IncidentAlert>,
    showHeatmap: Boolean,
    onPinClick: (IncidentAlert) -> Unit,
    userLatLng: LatLng?,
    userLocationVersion: Int
) {
    // Com GPS, o mapa centraliza no usuário; sem GPS, no bairro escolhido.
    val targetLatLng = userLatLng ?: centerLatLngForNeighborhood(neighborhood)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(targetLatLng, 15f)
    }
    val context = LocalContext.current
    val showMyLocationDot = context.hasLocationPermission()

    LaunchedEffect(neighborhood, userLocationVersion) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(targetLatLng, 15f),
            durationMs = 700
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isTrafficEnabled = true,
            isBuildingEnabled = true,
            isMyLocationEnabled = showMyLocationDot
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true
        )
    ) {
        incidents.forEach { alert ->
          // key(id): ao filtrar a lista, cada pino mantém o próprio estado em vez de herdar o de outro alerta.
          key(alert.id) {
            val pos = LatLng(alert.latitude, alert.longitude)
            // Lembrado entre recomposições (antes era recriado a cada recomposição — lint UnrememberedMutableState).
            val markerState = remember { MarkerState(position = pos) }
            SideEffect { markerState.position = pos }

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
                state = markerState,
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
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LiveCartographicWebMapView(
    neighborhood: String,
    incidents: List<IncidentAlert>,
    showHeatmap: Boolean,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    onPinClick: (IncidentAlert) -> Unit = {}
) {
    // Centro: a pessoa (quando o GPS já respondeu); senão, o bairro.
    val center = if (userLatitude != null && userLongitude != null) LatLng(userLatitude, userLongitude)
        else centerLatLngForNeighborhood(neighborhood)
    val currentIncidents by rememberUpdatedState(incidents)
    val currentOnPinClick by rememberUpdatedState(onPinClick)
    // Fundo enquanto os tiles carregam: segue o tema (neutro quente), não um azul fixo.
    val loadingBackgroundHex = String.format("#%06X", MaterialTheme.colorScheme.surfaceContainerHigh.toArgb() and 0xFFFFFF)

    val markersJs = incidents.joinToString(separator = "\n") { alert ->
        val hexColor = String.format("#%06X", 0xFFFFFF and alert.category.badgeColor.value.toLong().shr(32).toInt())
        val safeId = alert.id.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
        """
        L.circleMarker([${alert.latitude}, ${alert.longitude}], {
            radius: 11, color: "#FFFFFF", weight: 3, fillColor: "$hexColor", fillOpacity: 1
        }).addTo(map).on('click', function () { RcBridge.onPin("$safeId"); });
        if ($showHeatmap) {
            L.circle([${alert.latitude}, ${alert.longitude}], {
                radius: 240,
                fillColor: "$hexColor",
                color: "$hexColor",
                weight: 1,
                fillOpacity: 0.20
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
                html, body { height: 100%; width: 100%; margin: 0; padding: 0; background: $loadingBackgroundHex; }
                /* Fixado nas bordas (recomendação do Leaflet): altura em % ficava 0 dentro do app. */
                #map { position: absolute; top: 0; right: 0; bottom: 0; left: 0; background: $loadingBackgroundHex; }
                .leaflet-control-attribution { font-size: 10px; background: rgba(255,255,255,0.75) !important; }
                .leaflet-control-attribution a { color: #1D3B8B; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                // Dentro do app, alturas relativas (%, top/bottom) chegam a 0; fixa em pixels pela janela.
                function fitMap() {
                    var el = document.getElementById('map');
                    el.style.height = window.innerHeight + 'px';
                    el.style.width = window.innerWidth + 'px';
                }
                fitMap();
                var map = L.map('map', { zoomControl: false, attributionControl: false }).setView([${center.latitude}, ${center.longitude}], 14);
                // OpenStreetMap: aberto, sem chave. A licença exige o crédito visível (ODbL).
                // INTEGRAR: antes da loja, usar a chave do Google Maps (ou um provedor com cota própria);
                // os servidores do OSM não são para uso pesado de apps.
                var tiles = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: ''
                }).addTo(map);
                // Crédito no topo direito: embaixo ele fica atrás do painel de relatos.
                L.control.attribution({ position: 'topright', prefix: false }).addAttribution('© OpenStreetMap').addTo(map);
                var ok = 0, fail = 0;
                tiles.on('tileload', function () { ok++; });
                tiles.on('tileerror', function (e) { fail++; console.log('rc-map: bloco falhou ' + (e.tile && e.tile.src)); });
                setTimeout(function () {
                    var c = map.getContainer();
                    console.log('rc-map: blocos ok=' + ok + ' falhas=' + fail + ' container=' + c.clientWidth + 'x' + c.clientHeight + ' janela=' + window.innerWidth + 'x' + window.innerHeight + ' body=' + document.body.clientHeight + ' html=' + document.documentElement.clientHeight + ' pos=' + getComputedStyle(c).position);
                }, 4000);
                $markersJs
                ${if (userLatitude != null && userLongitude != null) """
                L.circleMarker([$userLatitude, $userLongitude], {
                    radius: 8, color: "#FFFFFF", weight: 3, fillColor: "#1D3B8B", fillOpacity: 1
                }).addTo(map);
                """ else ""}
                // O WebView pode abrir com altura zero (a tela ainda está montando): recalcula o
                // tamanho quando ele muda, senão o Leaflet não pede nenhum bloco do mapa.
                window.addEventListener('resize', function () { fitMap(); map.invalidateSize(); });
                [100, 400, 1200].forEach(function (ms) { setTimeout(function () { fitMap(); map.invalidateSize(); }, ms); });
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
                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(view: WebView, request: android.webkit.WebResourceRequest, error: android.webkit.WebResourceError) {
                        if (com.example.projetorevisao.BuildConfig.DEBUG) android.util.Log.d("RcWebMap", "erro ${error.errorCode} ${error.description} em ${request.url}")
                    }
                    override fun onPageFinished(view: WebView, url: String?) {
                        if (com.example.projetorevisao.BuildConfig.DEBUG) android.util.Log.d("RcWebMap", "pagina carregada, altura=${view.height}")
                    }
                }
                if (com.example.projetorevisao.BuildConfig.DEBUG) {
                    // Debug: erros da página do mapa vão para o logcat (tag "RcWebMap").
                    webChromeClient = object : android.webkit.WebChromeClient() {
                        override fun onConsoleMessage(message: android.webkit.ConsoleMessage): Boolean {
                            android.util.Log.d("RcWebMap", "${message.messageLevel()} ${message.message()} @${message.sourceId()}:${message.lineNumber()}")
                            return true
                        }
                    }
                }
                // Toque num pino (desenhado no Leaflet) abre o detalhe do relato no app.
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun onPin(id: String) {
                        post { currentIncidents.firstOrNull { it.id.filter { c -> c.isLetterOrDigit() || c == '-' || c == '_' } == id }?.let(currentOnPinClick) }
                    }
                }, "RcBridge")
                loadDataWithBaseURL("https://unpkg.com", htmlContent, "text/html", "UTF-8", null)
                tag = htmlContent
            }
        },
        update = { webView ->
            // Só recarrega quando o conteúdo do mapa muda. Antes recarregava a cada recomposição
            // (clima, localização, relatos), e os blocos do mapa nunca terminavam de baixar.
            if (webView.tag != htmlContent) {
                webView.tag = htmlContent
                webView.loadDataWithBaseURL("https://unpkg.com", htmlContent, "text/html", "UTF-8", null)
            }
        }
    )
}

/**
 * Renderização cartográfica estilo Google Maps Urbano para o @Preview e modo offline,
 * com quarteirões, avenidas arteriais, parque, Linha 5-Lilás e pins sem sobreposição.
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
            // Base map land color (Google Maps Dark/Night Urban Palette)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF17263C),
                        Color(0xFF1D314F),
                        Color(0xFF152338)
                    )
                )
            )

            // Urban city blocks (subtle lighter polygons like Google Maps building footprints)
            val blockColor = Color(0xFF243B5E).copy(alpha = 0.55f)
            for (row in 0..3) {
                for (col in 0..4) {
                    val left = size.width * (0.04f + col * 0.19f)
                    val top = size.height * (0.08f + row * 0.23f)
                    drawRoundRect(
                        color = blockColor,
                        topLeft = Offset(left, top),
                        size = Size(size.width * 0.14f, size.height * 0.16f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                }
            }

            // Green Park Area (Parque Santo Dias / Parque Trianon)
            drawRoundRect(
                color = Color(0xFF059669).copy(alpha = 0.32f),
                topLeft = Offset(size.width * 0.60f, size.height * 0.10f),
                size = Size(size.width * 0.26f, size.height * 0.30f),
                cornerRadius = CornerRadius(20f, 20f)
            )

            // Secondary local streets
            val localStreetColor = Color(0xFF334E75)
            for (i in 1..4) {
                val y = size.height * (i / 5f)
                drawLine(localStreetColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 5f)
            }
            for (i in 1..5) {
                val x = size.width * (i / 6f)
                drawLine(localStreetColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 5f)
            }

            // Arterial Highway 1: Estrada de Itapecerica (Google Maps yellow/amber arterial road)
            val highwayPath = Path().apply {
                moveTo(0f, size.height * 0.78f)
                quadraticTo(
                    size.width * 0.45f,
                    size.height * 0.52f,
                    size.width,
                    size.height * 0.20f
                )
            }
            drawPath(
                path = highwayPath,
                color = Color(0xFF0F172A),
                style = Stroke(width = 18f, cap = StrokeCap.Round)
            )
            drawPath(
                path = highwayPath,
                color = Color(0xFFF59E0B).copy(alpha = 0.72f),
                style = Stroke(width = 11f, cap = StrokeCap.Round)
            )

            // Arterial Avenue 2: Av. Carlos Caldeira Filho
            val avenuePath = Path().apply {
                moveTo(size.width * 0.12f, 0f)
                lineTo(size.width * 0.58f, size.height)
            }
            drawPath(
                path = avenuePath,
                color = Color(0xFF0F172A),
                style = Stroke(width = 14f, cap = StrokeCap.Round)
            )
            drawPath(
                path = avenuePath,
                color = Color(0xFF38BDF8).copy(alpha = 0.60f),
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )

            // Metro Line 5-Lilás Track
            drawLine(
                color = Color(0xFFC084FC),
                start = Offset(size.width * 0.05f, size.height * 0.34f),
                end = Offset(size.width * 0.88f, size.height * 0.56f),
                strokeWidth = 6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f)
            )

            // Heatmap circles when active
            if (showHeatmap) {
                incidents.forEach { alert ->
                    val safeX = alert.mapXFraction.coerceIn(0.16f, 0.76f)
                    val safeY = alert.mapYFraction.coerceIn(0.24f, 0.70f)
                    drawCircle(
                        color = alert.category.badgeColor.copy(alpha = 0.26f),
                        radius = 58f,
                        center = Offset(size.width * safeX, size.height * safeY)
                    )
                }
            }

            // User current GPS blue dot
            val userCenter = Offset(size.width * 0.45f, size.height * 0.48f)
            drawCircle(
                color = BrandBlueHero.copy(alpha = 0.32f),
                radius = 26f,
                center = userCenter
            )
            drawCircle(
                color = Color.White,
                radius = 8.5f,
                center = userCenter
            )
            drawCircle(
                color = BrandBlueHero,
                radius = 5.5f,
                center = userCenter
            )
        }

        // Clean cartographic street labels (positioned away from controls and pins)
        val isCapao = neighborhood.contains("Capão", ignoreCase = true)
        MapStreetLabel(
            text = if (isCapao) "Estação Capão Redondo (L5)" else "Estação Trianon-Masp (L2)",
            color = Color(0xFFE9D5FF),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 40.dp)
        )
        MapStreetLabel(
            text = if (isCapao) "Pq. Santo Dias" else "Pq. Trianon",
            color = Color(0xFF6EE7B7),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 56.dp, top = 14.dp)
        )
        MapStreetLabel(
            text = if (isCapao) "Est. de Itapecerica" else "Av. Paulista",
            color = Color(0xFFFDE68A),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 56.dp, bottom = 12.dp)
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
        color = Color(0xFF0B1528).copy(alpha = 0.72f),
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
    val density = LocalDensity.current
    val halfPinPx = with(density) { 16.dp.toPx() }.roundToInt()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val mapWidthPx = constraints.maxWidth
        val mapHeightPx = constraints.maxHeight

        incidents.forEach { alert ->
            // Coerce fractions into safe interior bounds so pins never touch edge buttons or pills
            val safeX = alert.mapXFraction.coerceIn(0.16f, 0.76f)
            val safeY = alert.mapYFraction.coerceIn(0.24f, 0.70f)

            val pinX = (mapWidthPx * safeX).roundToInt() - halfPinPx
            val pinY = (mapHeightPx * safeY).roundToInt() - halfPinPx

            Surface(
                shape = CircleShape,
                color = alert.category.badgeColor,
                border = BorderStroke(2.dp, Color.White),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .offset { IntOffset(pinX, pinY) }
                    .size(32.dp)
                    .clickable { onPinClick(alert) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = categoryIcon(alert.category),
                        contentDescription = alert.title,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
