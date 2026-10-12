package com.example.projetorevisao.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetorevisao.data.remote.ExternalApiConnectionStatus
import com.example.projetorevisao.data.remote.SpPublicSafetyApiService
import com.example.projetorevisao.data.remote.SspDistrictTelemetry
import com.example.projetorevisao.data.remote.WeatherSafetyTelemetry
import com.example.projetorevisao.ui.theme.AccentAmberWarning
import com.example.projetorevisao.ui.theme.AccentEmeraldSafe
import com.example.projetorevisao.ui.theme.AccentIndigoTraffic
import com.example.projetorevisao.ui.theme.AccentPoliceNavy
import com.example.projetorevisao.ui.theme.AccentRoseCritical
import com.example.projetorevisao.ui.theme.BrandBlueHero
import com.example.projetorevisao.ui.theme.BrandOrange
import kotlinx.coroutines.launch

enum class IncidentCategory(
    val label: String,
    val badgeTitle: String,
    val badgeColor: Color
) {
    ROUBO_FURTO(
        label = "Assalto / Roubo",
        badgeTitle = "ROUBO À MÃO ARMADA",
        badgeColor = AccentRoseCritical
    ),
    INCENDIO(
        label = "Incêndio",
        badgeTitle = "INCÊNDIO EM RESIDÊNCIA",
        badgeColor = BrandOrange
    ),
    ALAGAMENTO(
        label = "Alagamento",
        badgeTitle = "ALAGAMENTO NA VIA",
        badgeColor = BrandBlueHero
    ),
    TEMPESTADE(
        label = "Tempestade",
        badgeTitle = "ALERTA DE TEMPESTADE",
        badgeColor = AccentAmberWarning
    ),
    TRANSITO(
        label = "Trânsito",
        badgeTitle = "ACIDENTE DE TRÂNSITO",
        badgeColor = AccentIndigoTraffic
    ),
    OPERACAO_POLICIAL(
        label = "Polícia",
        badgeTitle = "OPERAÇÃO POLICIAL",
        badgeColor = AccentPoliceNavy
    ),
    UTILIDADE_PUBLICA(
        label = "Iluminação / Via",
        badgeTitle = "ZELADORIA URBANA",
        badgeColor = AccentEmeraldSafe
    )
}

enum class IncidentStatus(val label: String, val stepIndex: Int) {
    REPORTADO("Reportado pela Comunidade", 1),
    EM_ATENDIMENTO("Equipes em Deslocamento / Local", 2),
    CONTROLADO("Situação Controlada", 3)
}

data class IncidentAlert(
    val id: String,
    val category: IncidentCategory,
    val customBadgeTitle: String? = null,
    val title: String,
    val description: String,
    val address: String,
    val neighborhood: String,
    val distanceMeters: Int,
    val timeAgo: String,
    val verified: Boolean,
    val confirmations: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val status: IncidentStatus,
    val authorityAssigned: String,
    val isAnonymous: Boolean,
    val mapXFraction: Float,
    val mapYFraction: Float,
    val aiSafetyRecommendation: String,
    val userConfirmed: Boolean = false
) {
    val distanceLabel: String
        get() = if (distanceMeters < 1000) "${distanceMeters}m de você" else String.format("%.1fkm de você", distanceMeters / 1000f)

    val effectiveBadgeTitle: String
        get() = customBadgeTitle ?: category.badgeTitle
}

data class SafetyReel(
    val id: String,
    val title: String,
    val description: String,
    val authorName: String,
    val authorRole: String,
    val isOfficialVerified: Boolean,
    val location: String,
    val timeAgo: String,
    val viewsLabel: String,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val category: IncidentCategory,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val durationSeconds: Int = 18
)

data class MetroLineStatus(
    val code: String,
    val name: String,
    val operator: String,
    val lineColor: Color,
    val operationalStatus: String,
    val isNormal: Boolean,
    val crowdLevel: String,
    val headWayMinutes: Int,
    val safetyNote: String,
    val activeSecurityAgents: Int
)

data class SafePedestrianRoute(
    val id: String,
    val origin: String,
    val destination: String,
    val title: String,
    val durationMinutes: Int,
    val distanceKm: String,
    val safetyScore: Int,
    val lightingCoveragePercent: Int,
    val activeCamerasCount: Int,
    val avoidedIncidentsCount: Int,
    val isRecommendedSafe: Boolean,
    val routeSummary: String
)

data class GuardianContact(
    val id: String,
    val name: String,
    val relation: String,
    val phone: String,
    val isSharingLiveLocation: Boolean,
    val distanceLabel: String
)

class CitizenViewModel : ViewModel() {

    val availableNeighborhoods = listOf(
        "Capão Redondo, São Paulo - Zona Sul",
        "Av. Paulista, Bela Vista - Centro",
        "Pinheiros / Faria Lima - Zona Oeste"
    )

    var selectedNeighborhood by mutableStateOf(availableNeighborhoods.first())
        private set

    var selectedCategoryFilter by mutableStateOf<IncidentCategory?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var activeHomeTab by mutableStateOf("feed") // "feed" or "reels" (Figma 32:634)
        private set

    var selectedIncidentForDetail by mutableStateOf<IncidentAlert?>(null)
        private set

    var selectedReelForPlayer by mutableStateOf<SafetyReel?>(null)
        private set

    var showHeatmapOverlay by mutableStateOf(true)
        private set

    var anonymousReportingDefault by mutableStateOf(true)
        private set

    var geofenceRadiusKm by mutableFloatStateOf(2.0f)
        private set

    var userTrustScore by mutableIntStateOf(940)
        private set

    var isRefreshingApis by mutableStateOf(false)
        private set

    var weatherTelemetry by mutableStateOf(
        WeatherSafetyTelemetry(
            regionName = availableNeighborhoods.first(),
            temperatureCelsius = 20.4,
            precipitationMm = 8.6,
            windSpeedKmh = 24.0,
            windGustsKmh = 52.0,
            humidityPercent = 86,
            conditionLabel = "Pancadas de Chuva e Vento Forte",
            inmetAlertLevel = "LARANJA",
            cgeFloodRiskLabel = "Estado de Alerta CGE-SP • Monitoramento Estrada de Itapecerica",
            activeFloodPointsCount = 1,
            sourceApi = "Open-Meteo + CGE-SP / INMET",
            lastUpdatedTime = "Agora",
            isLiveNetworkData = false
        )
    )
        private set

    val districtTelemetry: SspDistrictTelemetry
        get() = SpPublicSafetyApiService.getDistrictTelemetry(selectedNeighborhood)

    val integratedApis: List<ExternalApiConnectionStatus> =
        SpPublicSafetyApiService.getIntegratedApisCatalog()

    init {
        refreshPublicApis()
    }

    fun refreshPublicApis() {
        viewModelScope.launch {
            isRefreshingApis = true
            weatherTelemetry = SpPublicSafetyApiService.fetchLiveWeatherSafety(selectedNeighborhood)
            isRefreshingApis = false
        }
    }

    private val _incidents = mutableStateListOf(
        // Figma 32:608 - Card 1
        IncidentAlert(
            id = "inc-1",
            category = IncidentCategory.ROUBO_FURTO,
            title = "Assalto reportado próximo ao metrô",
            description = "Moradores relataram dois indivíduos em motocicleta abordando pedestres na saída do acesso norte da Estação Capão Redondo. Viatura da PM acionada via 190.",
            address = "Av. Carlos Caldeira Filho, 1200 - Capão Redondo",
            neighborhood = "Capão Redondo, São Paulo - Zona Sul",
            distanceMeters = 500,
            timeAgo = "2 min atrás",
            verified = true,
            confirmations = 38,
            commentsCount = 14,
            sharesCount = 29,
            status = IncidentStatus.EM_ATENDIMENTO,
            authorityAssigned = "PMESP • 37º BPM/M (2 viaturas a 400m)",
            isAnonymous = true,
            mapXFraction = 0.25f,
            mapYFraction = 0.28f,
            aiSafetyRecommendation = "Evite caminhar sozinho pelo acesso norte nos próximos 25 min. Utilize a saída principal iluminada junto ao terminal de ônibus."
        ),
        // Figma 32:608 - Card 2
        IncidentAlert(
            id = "inc-2",
            category = IncidentCategory.INCENDIO,
            title = "Fogo em imóvel residencial",
            description = "Incêndio de médio porte em sobrado residencial. Equipes do Corpo de Bombeiros já estão no local realizando o rescaldo e isolamento da rua.",
            address = "Rua Cassiano dos Santos, 450 - Capão Redondo",
            neighborhood = "Capão Redondo, São Paulo - Zona Sul",
            distanceMeters = 1200,
            timeAgo = "15 min atrás",
            verified = true,
            confirmations = 64,
            commentsCount = 22,
            sharesCount = 41,
            status = IncidentStatus.CONTROLADO,
            authorityAssigned = "Corpo de Bombeiros SP • 3 viaturas no local",
            isAnonymous = false,
            mapXFraction = 0.66f,
            mapYFraction = 0.36f,
            aiSafetyRecommendation = "Fumaça densa na Rua Cassiano dos Santos. Mantenha janelas fechadas num raio de 300m e dê passagem aos veículos de emergência."
        ),
        // Figma 32:608 - Card 3
        IncidentAlert(
            id = "inc-3",
            category = IncidentCategory.ALAGAMENTO,
            title = "Água acumulada bloqueia trânsito",
            description = "Lâmina d'água elevada impede a passagem de veículos de passeio sentido centro. Apenas ônibus e caminhões conseguem atravessar o trecho.",
            address = "Estrada de Itapecerica, 3500 - Capão Redondo",
            neighborhood = "Capão Redondo, São Paulo - Zona Sul",
            distanceMeters = 800,
            timeAgo = "28 min atrás",
            verified = true,
            confirmations = 51,
            commentsCount = 19,
            sharesCount = 37,
            status = IncidentStatus.EM_ATENDIMENTO,
            authorityAssigned = "Defesa Civil SP & CET • Monitoramento ativo",
            isAnonymous = true,
            mapXFraction = 0.32f,
            mapYFraction = 0.64f,
            aiSafetyRecommendation = "Rota alternativa recomendada: desvie pela Av. Ellis Maas ou utilize a Linha 5-Lilás do Metrô."
        ),
        // Figma 32:608 - Card 4
        IncidentAlert(
            id = "inc-4",
            category = IncidentCategory.TEMPESTADE,
            title = "Chuva forte e ventos previstos",
            description = "Defesa Civil emite alerta severo para pancadas de chuva com rajadas de vento de até 65 km/h e risco de queda de galhos na Zona Sul.",
            address = "Região do Capão Redondo e Jardim Ângela",
            neighborhood = "Capão Redondo, São Paulo - Zona Sul",
            distanceMeters = 2100,
            timeAgo = "45 min atrás",
            verified = true,
            confirmations = 89,
            commentsCount = 31,
            sharesCount = 74,
            status = IncidentStatus.EM_ATENDIMENTO,
            authorityAssigned = "CGE & Defesa Civil Estadual • Alerta Laranja",
            isAnonymous = false,
            mapXFraction = 0.76f,
            mapYFraction = 0.72f,
            aiSafetyRecommendation = "Evite estacionar sob árvores ou próximo a fiações elétricas. Abrigue-se em local seguro."
        ),
        // Figma 32:608 - Card 5
        IncidentAlert(
            id = "inc-5",
            category = IncidentCategory.TRANSITO,
            title = "Colisão entre dois veículos",
            description = "Colisão lateral entre carro de passeio e utilitário bloqueia a faixa da direita. Sem vítimas graves, aguardando remoção da CET.",
            address = "Av. Comendador Sant'Anna, 890 - Capão Redondo",
            neighborhood = "Capão Redondo, São Paulo - Zona Sul",
            distanceMeters = 1500,
            timeAgo = "1h atrás",
            verified = true,
            confirmations = 27,
            commentsCount = 8,
            sharesCount = 12,
            status = IncidentStatus.CONTROLADO,
            authorityAssigned = "CET São Paulo • Agentes sinalizando a via",
            isAnonymous = true,
            mapXFraction = 0.50f,
            mapYFraction = 0.48f,
            aiSafetyRecommendation = "Lentidão de 900m no sentido Bairro. Prefira a faixa da esquerda ou vias paralelas."
        ),
        // Figma 32:608 - Card 6
        IncidentAlert(
            id = "inc-6",
            category = IncidentCategory.OPERACAO_POLICIAL,
            customBadgeTitle = "OPERACAO POLICIAL",
            title = "Atividade Policial Intensa",
            description = "Operação preventiva de saturação e bloqueio policial com apoio de bases comunitárias móveis para coibir roubos de celulares e motocicletas.",
            address = "Parque Santo Dias - Capão Redondo",
            neighborhood = "Capão Redondo, São Paulo - Zona Sul",
            distanceMeters = 1800,
            timeAgo = "1h atrás",
            verified = true,
            confirmations = 45,
            commentsCount = 15,
            sharesCount = 21,
            status = IncidentStatus.CONTROLADO,
            authorityAssigned = "PMESP & GCM • Base Comunitária Móvel",
            isAnonymous = false,
            mapXFraction = 0.82f,
            mapYFraction = 0.25f,
            aiSafetyRecommendation = "Corredor seguro ativo no entorno do Parque Santo Dias com policiamento ostensivo reforçado."
        ),
        // Av. Paulista / Pinheiros extra incidents so switching neighborhood is realistic
        IncidentAlert(
            id = "inc-7",
            category = IncidentCategory.ROUBO_FURTO,
            customBadgeTitle = "FURTO COM BICICLETA",
            title = "Gangue da bicicleta na ciclovia",
            description = "Relatos de furtos de celulares destravados na altura do MASP. GCM reforçou patrulhamento nos cruzamentos.",
            address = "Av. Paulista, 1578 - Bela Vista",
            neighborhood = "Av. Paulista, Bela Vista - Centro",
            distanceMeters = 350,
            timeAgo = "4 min atrás",
            verified = true,
            confirmations = 52,
            commentsCount = 18,
            sharesCount = 40,
            status = IncidentStatus.EM_ATENDIMENTO,
            authorityAssigned = "GCM Inspetoria Paulista • 2 equipes",
            isAnonymous = true,
            mapXFraction = 0.42f,
            mapYFraction = 0.35f,
            aiSafetyRecommendation = "Não utilize o celular próximo ao meio-fio aguardando o semáforo abrir."
        ),
        IncidentAlert(
            id = "inc-8",
            category = IncidentCategory.UTILIDADE_PUBLICA,
            customBadgeTitle = "ILUMINAÇÃO PÚBLICA",
            title = "Quatro postes apagados na saída da estação",
            description = "Trecho escuro relatado por estudantes no retorno noturno. Protocolo SP156 enviado automaticamente com prioridade.",
            address = "Rua Teodoro Sampaio, 2100 - Pinheiros",
            neighborhood = "Pinheiros / Faria Lima - Zona Oeste",
            distanceMeters = 620,
            timeAgo = "19 min atrás",
            verified = true,
            confirmations = 33,
            commentsCount = 9,
            sharesCount = 16,
            status = IncidentStatus.REPORTADO,
            authorityAssigned = "Ilumina SP / SP156 • Protocolo #98412",
            isAnonymous = true,
            mapXFraction = 0.58f,
            mapYFraction = 0.52f,
            aiSafetyRecommendation = "Prefira caminhar pela calçada oposta em frente aos comércios abertos."
        )
    )

    val incidents: List<IncidentAlert>
        get() = _incidents.filter { alert ->
            val matchesCategory = selectedCategoryFilter == null || alert.category == selectedCategoryFilter
            val matchesQuery = searchQuery.isBlank() ||
                alert.title.contains(searchQuery, ignoreCase = true) ||
                alert.address.contains(searchQuery, ignoreCase = true) ||
                alert.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }

    // Figma 192:288 (Grid) & 32:1100 (ReelsView)
    private val _safetyReels = mutableStateListOf(
        SafetyReel(
            id = "reel-1",
            title = "Incêndio controlado",
            description = "Incêndio em residência controlado pela equipe de bombeiros. Sem vítimas. Área isolada para perícia técnica na Rua Cassiano dos Santos.",
            authorName = "Bombeiros SP Oficial",
            authorRole = "Corpo de Bombeiros PMESP",
            isOfficialVerified = true,
            location = "Capão Redondo, São Paulo - SP",
            timeAgo = "Há 15 minutos",
            viewsLabel = "12.5k",
            likesCount = 1240,
            commentsCount = 89,
            sharesCount = 312,
            category = IncidentCategory.INCENDIO,
            durationSeconds = 22
        ),
        SafetyReel(
            id = "reel-2",
            title = "Alagamento na avenida",
            description = "Nível da água subiu rapidamente na Estrada de Itapecerica após pancada de chuva. Equipes da Defesa Civil e CET orientam desvio imediato.",
            authorName = "Defesa Civil SP",
            authorRole = "Canal Oficial de Alertas",
            isOfficialVerified = true,
            location = "Estrada de Itapecerica, 3500 - Zona Sul",
            timeAgo = "Há 28 minutos",
            viewsLabel = "8.3k",
            likesCount = 845,
            commentsCount = 64,
            sharesCount = 219,
            category = IncidentCategory.ALAGAMENTO,
            durationSeconds = 19
        ),
        SafetyReel(
            id = "reel-3",
            title = "Operação policial",
            description = "Operação Paz e Proteção com bases móveis posicionadas nas saídas do Metrô Capão Redondo e Parque Santo Dias garantindo retorno seguro.",
            authorName = "PMESP 37º BPM/M",
            authorRole = "Polícia Militar do Estado de SP",
            isOfficialVerified = true,
            location = "Parque Santo Dias - Capão Redondo",
            timeAgo = "Há 42 minutos",
            viewsLabel = "15.7k",
            likesCount = 1890,
            commentsCount = 142,
            sharesCount = 410,
            category = IncidentCategory.OPERACAO_POLICIAL,
            durationSeconds = 25
        ),
        SafetyReel(
            id = "reel-4",
            title = "Alerta de tempestade",
            description = "Radar meteorológico indica deslocamento de célula de tempestade severa para a Zona Sul nos próximos 30 minutos. Veja medidas de proteção.",
            authorName = "CGE São Paulo",
            authorRole = "Centro de Gerenciamento de Emergências",
            isOfficialVerified = true,
            location = "Zona Sul - Grande São Paulo",
            timeAgo = "Há 45 minutos",
            viewsLabel = "22.1k",
            likesCount = 2410,
            commentsCount = 195,
            sharesCount = 890,
            category = IncidentCategory.TEMPESTADE,
            durationSeconds = 30
        ),
        SafetyReel(
            id = "reel-5",
            title = "Patrulha comunitária",
            description = "Vizinhança Solidária e Guarda Civil Metropolitana ativam corredor iluminado para estudantes voltando das universidades e escolas técnicas.",
            authorName = "Rede Cidadã SP",
            authorRole = "Comunidade Verificada",
            isOfficialVerified = true,
            location = "Av. Carlos Caldeira Filho - Capão Redondo",
            timeAgo = "Há 1 hora",
            viewsLabel = "5.6k",
            likesCount = 620,
            commentsCount = 47,
            sharesCount = 135,
            category = IncidentCategory.UTILIDADE_PUBLICA,
            durationSeconds = 16
        ),
        SafetyReel(
            id = "reel-6",
            title = "Prevenção incêndios",
            description = "Orientações rápidas do Corpo de Bombeiros sobre instalações elétricas residenciais e como acionar o 193 com geolocalização precisa.",
            authorName = "Bombeiros SP Oficial",
            authorRole = "Educação Pública • 193",
            isOfficialVerified = true,
            location = "São Paulo - SP",
            timeAgo = "Há 2 horas",
            viewsLabel = "9.8k",
            likesCount = 970,
            commentsCount = 53,
            sharesCount = 280,
            category = IncidentCategory.INCENDIO,
            durationSeconds = 28
        )
    )

    val safetyReels: List<SafetyReel>
        get() = _safetyReels

    val metroLines = listOf(
        MetroLineStatus(
            code = "L5",
            name = "Linha 5 - Lilás (Capão Redondo ↔ Chácara Klabin)",
            operator = "ViaMobilidade",
            lineColor = Color(0xFF9B3894),
            operationalStatus = "Operação Normal",
            isNormal = true,
            crowdLevel = "Moderada",
            headWayMinutes = 3,
            safetyNote = "Acesso Sul da Estação Capão Redondo com patrulhamento reforçado. Evite o acesso Norte devido a relato recente a 500m.",
            activeSecurityAgents = 18
        ),
        MetroLineStatus(
            code = "L9",
            name = "Linha 9 - Esmeralda (Osasco ↔ Mendes-Vila Natal)",
            operator = "ViaMobilidade",
            lineColor = Color(0xFF00A88E),
            operationalStatus = "Operação Normal",
            isNormal = true,
            crowdLevel = "Moderada",
            headWayMinutes = 4,
            safetyNote = "Integração Santo Amaro com iluminação 100% ativa e câmeras Smart Sampa integradas.",
            activeSecurityAgents = 22
        ),
        MetroLineStatus(
            code = "L4",
            name = "Linha 4 - Amarela (Luz ↔ Vila Sônia)",
            operator = "ViaQuatro",
            lineColor = Color(0xFFF59E0B),
            operationalStatus = "Operação Normal",
            isNormal = true,
            crowdLevel = "Baixa",
            headWayMinutes = 2,
            safetyNote = "Corredor seguro ativo nas saídas Pinheiros e Paulista.",
            activeSecurityAgents = 26
        ),
        MetroLineStatus(
            code = "L1",
            name = "Linha 1 - Azul (Jabaquara ↔ Tucuruvi)",
            operator = "Metrô SP",
            lineColor = Color(0xFF0053A0),
            operationalStatus = "Velocidade Reduzida (Chuva)",
            isNormal = false,
            crowdLevel = "Alta",
            headWayMinutes = 5,
            safetyNote = "Velocidade reduzida entre Santa Cruz e Jabaquara devido à chuva forte na Zona Sul.",
            activeSecurityAgents = 30
        )
    )

    val safeRoutes = listOf(
        SafePedestrianRoute(
            id = "route-safe",
            origin = "Estação Capão Redondo (Acesso Principal)",
            destination = "Parque Santo Dias / Residencial",
            title = "Rota Segura Rede Cidadã (Recomendada)",
            durationMinutes = 14,
            distanceKm = "1,1 km",
            safetyScore = 96,
            lightingCoveragePercent = 98,
            activeCamerasCount = 14,
            avoidedIncidentsCount = 2,
            isRecommendedSafe = true,
            routeSummary = "Desvia do alerta de roubo na Av. Carlos Caldeira Filho (norte) e passa pela base comunitária móvel da PMESP."
        ),
        SafePedestrianRoute(
            id = "route-fast",
            origin = "Estação Capão Redondo (Acesso Norte)",
            destination = "Parque Santo Dias / Residencial",
            title = "Rota Mais Curta (Alto Risco Detectado)",
            durationMinutes = 11,
            distanceKm = "0,85 km",
            safetyScore = 48,
            lightingCoveragePercent = 62,
            activeCamerasCount = 3,
            avoidedIncidentsCount = 0,
            isRecommendedSafe = false,
            routeSummary = "Cruza área com alerta verificado há 2 min (Roubo à Mão Armada) e trecho com alagamento parcial."
        )
    )

    private val _guardians = mutableStateListOf(
        GuardianContact(
            id = "g-1",
            name = "Mariana Silva (Irmã)",
            relation = "Guardiã Principal • Notificação Instantânea",
            phone = "(11) 99812-4401",
            isSharingLiveLocation = true,
            distanceLabel = "A 1,4 km • Em casa"
        ),
        GuardianContact(
            id = "g-2",
            name = "Carlos Eduardo (Pai)",
            relation = "Contato de Emergência SOS",
            phone = "(11) 98734-1190",
            isSharingLiveLocation = true,
            distanceLabel = "A 3,2 km • Online"
        ),
        GuardianContact(
            id = "g-3",
            name = "Grupo Vizinhança Solidária Capão",
            relation = "Comunidade Verificada • 42 moradores ativos",
            phone = "Canal Comunitário Criptografado",
            isSharingLiveLocation = false,
            distanceLabel = "Raio de 500m"
        )
    )

    val guardians: List<GuardianContact>
        get() = _guardians

    fun selectNeighborhood(neighborhood: String) {
        selectedNeighborhood = neighborhood
        refreshPublicApis()
    }

    fun selectCategoryFilter(category: IncidentCategory?) {
        selectedCategoryFilter = if (selectedCategoryFilter == category) null else category
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun setHomeTab(tab: String) {
        activeHomeTab = tab
    }

    fun openIncidentDetail(incident: IncidentAlert?) {
        selectedIncidentForDetail = incident
    }

    fun openReelPlayer(reel: SafetyReel?) {
        selectedReelForPlayer = reel
    }

    fun toggleHeatmap() {
        showHeatmapOverlay = !showHeatmapOverlay
    }

    fun toggleAnonymousDefault(enabled: Boolean) {
        anonymousReportingDefault = enabled
    }

    fun updateGeofenceRadius(radiusKm: Float) {
        geofenceRadiusKm = radiusKm
    }

    fun confirmIncident(incidentId: String) {
        val index = _incidents.indexOfFirst { it.id == incidentId }
        if (index != -1) {
            val current = _incidents[index]
            val toggled = !current.userConfirmed
            val delta = if (toggled) 1 else -1
            val updated = current.copy(
                userConfirmed = toggled,
                confirmations = (current.confirmations + delta).coerceAtLeast(1)
            )
            _incidents[index] = updated
            if (selectedIncidentForDetail?.id == incidentId) {
                selectedIncidentForDetail = updated
            }
            userTrustScore += if (toggled) 5 else -5
        }
    }

    fun addIncident(
        category: IncidentCategory,
        title: String,
        description: String,
        address: String,
        isAnonymous: Boolean
    ) {
        val newAlert = IncidentAlert(
            id = "inc-${System.currentTimeMillis()}",
            category = category,
            title = title.ifBlank { "Ocorrência reportada: ${category.label}" },
            description = description.ifBlank { "Relato enviado pela comunidade via Rede Cidadã SP com geolocalização criptografada." },
            address = address.ifBlank { "Próximo a $selectedNeighborhood" },
            neighborhood = selectedNeighborhood,
            distanceMeters = 120,
            timeAgo = "Agora mesmo",
            verified = true,
            confirmations = 1,
            commentsCount = 0,
            sharesCount = 1,
            status = IncidentStatus.REPORTADO,
            authorityAssigned = when (category) {
                IncidentCategory.ROUBO_FURTO, IncidentCategory.OPERACAO_POLICIAL -> "Central 190 PMESP • Triagem prioritária"
                IncidentCategory.INCENDIO -> "Central 193 Bombeiros SP • Despacho automático"
                IncidentCategory.ALAGAMENTO, IncidentCategory.TEMPESTADE -> "Defesa Civil SP 199 • Alerta geolocalizado"
                IncidentCategory.TRANSITO -> "CET São Paulo 156 • Monitoramento viário"
                IncidentCategory.UTILIDADE_PUBLICA -> "Prefeitura SP 156 • Zeladoria Urbana"
            },
            isAnonymous = isAnonymous,
            mapXFraction = 0.48f,
            mapYFraction = 0.42f,
            aiSafetyRecommendation = "Relato registrado com sucesso. Moradores em um raio de ${geofenceRadiusKm.toInt()}km foram notificados com proteção total de identidade.",
            userConfirmed = true
        )
        _incidents.add(0, newAlert)
        userTrustScore += 15
    }

    fun toggleReelLike(reelId: String) {
        val index = _safetyReels.indexOfFirst { it.id == reelId }
        if (index != -1) {
            val current = _safetyReels[index]
            val liked = !current.isLiked
            val updated = current.copy(
                isLiked = liked,
                likesCount = current.likesCount + if (liked) 1 else -1
            )
            _safetyReels[index] = updated
            if (selectedReelForPlayer?.id == reelId) {
                selectedReelForPlayer = updated
            }
        }
    }

    fun toggleReelBookmark(reelId: String) {
        val index = _safetyReels.indexOfFirst { it.id == reelId }
        if (index != -1) {
            val current = _safetyReels[index]
            val updated = current.copy(isBookmarked = !current.isBookmarked)
            _safetyReels[index] = updated
            if (selectedReelForPlayer?.id == reelId) {
                selectedReelForPlayer = updated
            }
        }
    }

    fun navigateReel(offset: Int) {
        val current = selectedReelForPlayer ?: return
        val currentIndex = _safetyReels.indexOfFirst { it.id == current.id }
        if (currentIndex != -1) {
            val nextIndex = (currentIndex + offset).mod(_safetyReels.size)
            selectedReelForPlayer = _safetyReels[nextIndex]
        }
    }
}

@Composable
fun rememberCitizenViewModel(): CitizenViewModel {
    return remember { CitizenViewModel() }
}
