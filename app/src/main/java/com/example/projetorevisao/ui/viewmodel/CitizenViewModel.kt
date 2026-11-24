package com.example.projetorevisao.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.example.projetorevisao.data.local.IncidentEntity
import com.example.projetorevisao.data.remote.ApiResult
import com.example.projetorevisao.data.remote.Guardian
import com.example.projetorevisao.data.remote.SosApi
import com.example.projetorevisao.data.repository.IncidentRepository
import com.example.projetorevisao.data.repository.SubmitOutcome
import com.example.projetorevisao.data.session.SessionManager
import com.example.projetorevisao.data.repository.PublicSafetyRepository
import com.example.projetorevisao.data.remote.ExternalApiConnectionStatus
import com.example.projetorevisao.data.remote.WeatherSafetyTelemetry
import com.example.projetorevisao.ui.theme.RcTone
import com.example.projetorevisao.ui.theme.AccentAmberWarning
import com.example.projetorevisao.ui.theme.AccentEmeraldSafe
import com.example.projetorevisao.ui.theme.AccentIndigoTraffic
import com.example.projetorevisao.ui.theme.AccentPoliceNavy
import com.example.projetorevisao.ui.theme.AccentRoseCritical
import com.example.projetorevisao.ui.theme.BrandBlueHero
import com.example.projetorevisao.ui.theme.BrandOrange
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.CancellationException

enum class IncidentCategory(
    val label: String,
    val badgeTitle: String,
    /** Função da categoria (guia visual §2.1). A cor real vem do tema: use `tone.color` em composables. */
    val tone: RcTone
) {
    ROUBO_FURTO(
        label = "Assalto / Roubo",
        badgeTitle = "ASSALTO / ROUBO",
        tone = RcTone.CRITICAL
    ),
    INCENDIO(
        label = "Incêndio",
        badgeTitle = "INCÊNDIO",
        tone = RcTone.CRITICAL
    ),
    ALAGAMENTO(
        label = "Alagamento",
        badgeTitle = "ALAGAMENTO",
        tone = RcTone.WARNING
    ),
    TEMPESTADE(
        label = "Tempestade",
        badgeTitle = "TEMPESTADE",
        tone = RcTone.WARNING
    ),
    TRANSITO(
        label = "Trânsito",
        badgeTitle = "TRÂNSITO",
        tone = RcTone.WARNING
    ),
    OPERACAO_POLICIAL(
        label = "Polícia",
        badgeTitle = "AÇÃO POLICIAL",
        tone = RcTone.INFO
    ),
    UTILIDADE_PUBLICA(
        label = "Iluminação / Via",
        badgeTitle = "ILUMINAÇÃO / VIA",
        tone = RcTone.INFO
    );

    /** Cor fixa do tema claro, só para o mapa web (fora do Compose). Em composables use `tone.color`. */
    val badgeColor: Color get() = tone.lightColor

}

enum class IncidentStatus(val label: String, val stepIndex: Int) {
    REPORTADO("Reportado pela Comunidade", 1),
    EM_ATENDIMENTO("Equipes em Deslocamento / Local", 2),
    CONTROLADO("Situação Controlada", 3)
}

enum class NoticeTone { INFO, SUCCESS, ERROR }

/** Motivos de denúncia (iguais aos aceitos pelo servidor). */
enum class ReportReason(val label: String) {
    FALSO("É falso ou enganoso"),
    OFENSIVO("Ofensivo, preconceituoso ou discurso de ódio"),
    DADOS_PESSOAIS("Expõe rosto, placa ou dados de alguém"),
    VIOLENCIA("Violência explícita ou imagem chocante"),
    SPAM("Propaganda ou spam"),
    OUTRO("Outro motivo")
}

/** "Agora mesmo", "há 5 min", "há 3 h", "ontem", "há 4 dias" (igual ao servidor). */
fun timeAgoLabel(createdAtMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    val minutes = (nowMs - createdAtMs).coerceAtLeast(0) / 60_000
    return when {
        minutes < 1 -> "Agora mesmo"
        minutes < 60 -> "há $minutes min"
        minutes < 24 * 60 -> "há ${minutes / 60} h"
        minutes < 2 * 24 * 60 -> "ontem"
        else -> "há ${minutes / (24 * 60)} dias"
    }
}

/** Distância em metros entre dois pontos (fórmula de haversine). */
fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
    val r = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2).let { it * it } +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2).let { it * it }
    return (2 * r * Math.asin(Math.sqrt(a))).toInt()
}

/** Anexo de um relato. */
data class IncidentMedia(val url: String, val isVideo: Boolean)

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
    val latitude: Double = -23.6596,
    val longitude: Double = -46.7681,
    val aiSafetyRecommendation: String,
    val userConfirmed: Boolean = false,
    /** Fotos/vídeos anexados (endereço no servidor). */
    val media: List<IncidentMedia> = emptyList(),
    val createdAtEpochMs: Long = 0,
    /** false = criado neste aparelho e ainda não enviado ao servidor. */
    val fromServer: Boolean = false
) {
    val distanceLabel: String
        get() = when {
            distanceMeters <= 0 -> neighborhood.substringBefore(",").ifBlank { address }
            distanceMeters < 100 -> "a menos de 100 m"
            distanceMeters < 1000 -> "a ${distanceMeters} m"
            else -> String.format(java.util.Locale.forLanguageTag("pt-BR"), "a %.1f km", distanceMeters / 1000f)
        }

    val effectiveBadgeTitle: String
        get() = customBadgeTitle ?: category.badgeTitle
}

data class GuardianContact(
    val id: String,
    val name: String,
    val relation: String,
    val phone: String,
    val isSharingLiveLocation: Boolean,
    val distanceLabel: String
)

class CitizenViewModel(
    private val incidentRepository: IncidentRepository? = null,
    private val publicSafetyRepository: PublicSafetyRepository = PublicSafetyRepository()
) : ViewModel() {

    var selectedCategoryFilter by mutableStateOf<IncidentCategory?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var activeHomeTab by mutableStateOf("feed") // "feed" or "reels" (Figma 32:634)
        private set

    var selectedIncidentForDetail by mutableStateOf<IncidentAlert?>(null)
        private set

    var showHeatmapOverlay by mutableStateOf(true)
        private set

    var anonymousReportingDefault by mutableStateOf(true)
        private set

    var isRefreshingApis by mutableStateOf(false)
        private set

    private var remoteNotice by mutableStateOf<String?>(null)
    private var localDataNotice by mutableStateOf<String?>(null)
    private var localNoticeTone by mutableStateOf(NoticeTone.ERROR)
    val userNotice: String?
        get() = localDataNotice ?: remoteNotice
    /** Cor do aviso: sucesso em verde, andamento neutro, problema em vermelho. */
    val userNoticeTone: NoticeTone
        get() = if (localDataNotice != null) localNoticeTone else NoticeTone.ERROR

    /** Começa sem dados: a tela mostra "consultando" até a primeira resposta real. */
    var weatherTelemetry by mutableStateOf(
        WeatherSafetyTelemetry(
            regionName = "São Paulo",
            temperatureCelsius = 0.0,
            precipitationMm = 0.0,
            windSpeedKmh = 0.0,
            windGustsKmh = 0.0,
            humidityPercent = 0,
            conditionLabel = "",
            inmetAlertLevel = "VERDE",
            cgeFloodRiskLabel = "",
            cgeKeywordMatchCount = 0,
            sourceApi = "",
            lastUpdatedTime = "",
            isLiveNetworkData = false
        )
    )
        private set

    val integratedApis: List<ExternalApiConnectionStatus> =
        publicSafetyRepository.apiCatalog()

    /**
     * true quando a última tentativa de buscar os relatos no servidor falhou. A tela não pode
     * dizer "tudo tranquilo" nesse caso: a ausência de alertas pode ser só falta de conexão.
     */
    var incidentsSyncFailed by mutableStateOf(false)
        private set

    fun refreshPublicApis() {
        viewModelScope.launch {
            isRefreshingApis = true
            remoteNotice = null
            try {
                incidentRepository?.let { repository ->
                    incidentsSyncFailed = !repository.syncWithServer()
                }
                weatherTelemetry = publicSafetyRepository.weather(areaLabel, userLatitude, userLongitude)
                // Se o clima falhar, o próprio cartão mostra "Clima indisponível".
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                remoteNotice = "Não foi possível atualizar as fontes agora. Confira a internet e tente novamente."
            } finally {
                isRefreshingApis = false
            }
        }
    }

    /** Relatos reais: os do servidor e os criados neste aparelho. */
    private val _incidents = mutableStateListOf<IncidentAlert>()

    init {
        incidentRepository?.let { repository ->
            viewModelScope.launch {
                try {
                    repository.removeLegacyExamples()
                    repository.observeIncidents().collect { saved ->
                        _incidents.clear()
                        _incidents.addAll(saved.mapNotNull(IncidentEntity::toIncidentAlert))
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    localNoticeTone = NoticeTone.ERROR
                    localDataNotice = "Não foi possível carregar os relatos salvos neste aparelho."
                }
            }
        }
    }

    val incidents: List<IncidentAlert>
        get() {
            val lat = userLatitude
            val lon = userLongitude
            return _incidents.filter { alert ->
                val matchesCategory = selectedCategoryFilter == null || alert.category == selectedCategoryFilter
                val matchesQuery = searchQuery.isBlank() ||
                    alert.title.contains(searchQuery, ignoreCase = true) ||
                    alert.address.contains(searchQuery, ignoreCase = true) ||
                    alert.description.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesQuery
            }.map { alert ->
                // Distância de verdade entre você e o relato (quando o GPS já respondeu).
                if (lat == null || lon == null) alert
                else alert.copy(distanceMeters = distanceMeters(lat, lon, alert.latitude, alert.longitude).coerceAtLeast(1))
            }
        }

    private val _guardians = mutableStateListOf<GuardianContact>()

    // --- Localização do usuário (GPS) ---
    var userLatitude by mutableStateOf<Double?>(null)
        private set
    var userLongitude by mutableStateOf<Double?>(null)
        private set
    /** Bairro/cidade obtidos da posição, para exibir no mapa. */
    var userAreaLabel by mutableStateOf<String?>(null)
        private set
    /** Muda a cada leitura do GPS, para o mapa recentralizar mesmo se a posição for igual. */
    var userLocationVersion by mutableIntStateOf(0)
        private set

    /** Bairro e cidade de onde você está (ou "São Paulo" até o GPS responder). */
    val areaLabel: String
        get() = userAreaLabel ?: "São Paulo"

    fun updateUserLocation(latitude: Double, longitude: Double, areaLabel: String?) {
        val firstFix = userLatitude == null
        userLatitude = latitude
        userLongitude = longitude
        if (!areaLabel.isNullOrBlank()) userAreaLabel = areaLabel
        userLocationVersion++
        // Primeira posição: refaz o clima para o lugar onde a pessoa está.
        if (firstFix) refreshPublicApis()
    }

    /** Nome e e-mail do usuário logado (null = visitante). Atualiza sozinho ao entrar e sair. */
    var currentUserName by mutableStateOf<String?>(null)
        private set
    var currentUserEmail by mutableStateOf<String?>(null)
        private set

    val isLoggedIn: Boolean
        get() = currentUserEmail != null

    var guardiansMessage by mutableStateOf<String?>(null)
        private set

    var isSavingGuardian by mutableStateOf(false)
        private set

    var sosStatusMessage by mutableStateOf<String?>(null)
        private set

    fun loadGuardians() {
        viewModelScope.launch {
            val (result, list) = SosApi.guardians()
            when (result) {
                is ApiResult.Success -> {
                    _guardians.clear()
                    _guardians.addAll(list.map { it.toContact() })
                    guardiansMessage = null
                }
                // Mantém a última lista carregada: o SOS por SMS continua funcionando sem servidor.
                is ApiResult.Failure -> guardiansMessage = result.message
            }
        }
    }

    fun addGuardian(name: String, relation: String, phone: String, onSaved: () -> Unit) {
        if (isSavingGuardian) return
        viewModelScope.launch {
            isSavingGuardian = true
            val (result, created) = SosApi.addGuardian(name, relation, phone)
            if (created != null) {
                _guardians.add(created.toContact())
                guardiansMessage = null
                onSaved()
            } else {
                guardiansMessage = (result as? ApiResult.Failure)?.message ?: "Não foi possível salvar o guardião."
            }
            isSavingGuardian = false
        }
    }

    fun removeGuardian(id: String) {
        viewModelScope.launch {
            when (val result = SosApi.deleteGuardian(id)) {
                is ApiResult.Success -> _guardians.removeAll { it.id == id }
                is ApiResult.Failure -> guardiansMessage = result.message
            }
        }
    }

    /** Telefones dos guardiões, para o SMS que o próprio aparelho envia. */
    val guardianPhones: List<String>
        get() = _guardians.map { it.phone }.filter { it.isNotBlank() }

    /** Registra o SOS no servidor (melhor esforço). O aviso aos guardiões é feito pelo SMS do aparelho. */
    fun registerSosAlert(latitude: Double?, longitude: Double?) {
        if (!isLoggedIn) {
            sosStatusMessage = "Sem conta, o alerta não fica registrado. Em perigo, ligue 190."
            return
        }
        viewModelScope.launch {
            sosStatusMessage = when (val result = SosApi.registerAlert(areaLabel, latitude, longitude)) {
                is ApiResult.Success -> result.message ?: "Alerta registrado."
                is ApiResult.Failure -> "O alerta não foi registrado no servidor (${result.message}). A mensagem aos guardiões não depende disso."
            }
        }
    }

    fun clearSosStatus() {
        sosStatusMessage = null
    }

    val guardians: List<GuardianContact>
        get() = _guardians

    // Fica depois das propriedades de sessão/guardiões: o Kotlin inicializa na ordem do arquivo,
    // e o primeiro valor do fluxo chega na hora (Dispatchers.Main.immediate).
    init {
        refreshPublicApis()
    }

    init {
        // Guardiões pertencem à conta: carrega ao entrar e limpa ao sair.
        viewModelScope.launch {
            SessionManager.session.collect { session ->
                currentUserName = session?.name
                currentUserEmail = session?.email
                if (session != null) {
                    loadGuardians()
                } else {
                    _guardians.clear()
                    sosStatusMessage = null
                }
            }
        }
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

    fun toggleHeatmap() {
        showHeatmapOverlay = !showHeatmapOverlay
    }

    fun toggleAnonymousDefault(enabled: Boolean) {
        anonymousReportingDefault = enabled
    }

    /** Mensagem rápida sobre a última ação no relato (confirmar, denunciar, bloquear). */
    var incidentActionMessage by mutableStateOf<String?>(null)
        private set

    fun clearIncidentActionMessage() {
        incidentActionMessage = null
    }

    /** "Eu também vi": uma confirmação por pessoa, registrada no servidor. */
    fun confirmIncident(incidentId: String) {
        val current = _incidents.firstOrNull { it.id == incidentId } ?: return
        val repository = incidentRepository ?: return
        when {
            !isLoggedIn -> { incidentActionMessage = "Entre na sua conta para confirmar relatos."; return }
            !current.fromServer -> { incidentActionMessage = "Este relato ainda não foi enviado ao servidor."; return }
        }
        viewModelScope.launch {
            when (val result = repository.toggleConfirmation(current.toEntity())) {
                is ApiResult.Success -> {
                    val updated = _incidents.firstOrNull { it.id == incidentId }
                    if (selectedIncidentForDetail?.id == incidentId && updated != null) selectedIncidentForDetail = updated
                }
                is ApiResult.Failure -> incidentActionMessage = result.message
            }
        }
    }

    /** Denuncia um relato. Ele some na hora para quem denunciou. */
    fun reportIncident(incidentId: String, reason: ReportReason, details: String, onDone: () -> Unit) {
        val repository = incidentRepository ?: return
        if (!isLoggedIn) { incidentActionMessage = "Entre na sua conta para denunciar relatos."; return }
        viewModelScope.launch {
            when (val result = repository.report(incidentId, reason.name, details)) {
                is ApiResult.Success -> {
                    if (selectedIncidentForDetail?.id == incidentId) selectedIncidentForDetail = null
                    showLocalNotice(result.message ?: "Denúncia recebida. O relato foi ocultado para você.", NoticeTone.SUCCESS)
                    onDone()
                }
                is ApiResult.Failure -> incidentActionMessage = result.message
            }
        }
    }

    /** Deixa de ver todos os relatos do autor deste relato. */
    fun blockAuthor(incidentId: String) {
        val repository = incidentRepository ?: return
        if (!isLoggedIn) { incidentActionMessage = "Entre na sua conta para bloquear alguém."; return }
        viewModelScope.launch {
            when (val result = repository.blockAuthor(incidentId)) {
                is ApiResult.Success -> {
                    if (selectedIncidentForDetail?.id == incidentId) selectedIncidentForDetail = null
                    showLocalNotice(result.message ?: "Você não verá mais relatos desta pessoa.", NoticeTone.SUCCESS)
                }
                is ApiResult.Failure -> incidentActionMessage = result.message
            }
        }
    }

    private fun showLocalNotice(message: String, tone: NoticeTone) {
        localNoticeTone = tone
        localDataNotice = message
    }

    fun addIncident(
        category: IncidentCategory,
        title: String,
        description: String,
        address: String,
        isAnonymous: Boolean,
        attachments: List<android.net.Uri> = emptyList()
    ) {
        val newAlert = IncidentAlert(
            id = "inc-${System.currentTimeMillis()}",
            category = category,
            title = title.ifBlank { "Ocorrência reportada: ${category.label}" },
            description = description.ifBlank { "Sem descrição." },
            address = address.ifBlank { "Próximo a $areaLabel" },
            neighborhood = areaLabel,
            distanceMeters = 0,
            timeAgo = "Agora mesmo",
            verified = false,
            confirmations = 0,
            commentsCount = 0,
            sharesCount = 0,
            status = IncidentStatus.REPORTADO,
            authorityAssigned = "Sem encaminhamento a órgãos públicos",
            isAnonymous = isAnonymous,
            mapXFraction = 0.48f,
            mapYFraction = 0.42f,
            latitude = userLatitude ?: -23.6596,
            longitude = userLongitude ?: -46.7681,
            aiSafetyRecommendation = "Este relato não é enviado à polícia. Em perigo, ligue 190.",
            userConfirmed = false,
            createdAtEpochMs = System.currentTimeMillis(),
            fromServer = false
        )
        _incidents.add(0, newAlert)
        submitIncident(newAlert, attachments)
    }

    var isSubmittingReport by mutableStateOf(false)
        private set

    private fun submitIncident(incident: IncidentAlert, attachments: List<android.net.Uri>) {
        val repository = incidentRepository ?: return
        viewModelScope.launch {
            isSubmittingReport = true
            localNoticeTone = NoticeTone.INFO
            localDataNotice = if (attachments.isNotEmpty()) "Enviando relato e ${attachments.size} anexo(s)…" else "Enviando relato…"
            var tone = NoticeTone.ERROR
            localDataNotice = try {
                val result = repository.submitNew(incident.toEntity(), attachments)
                val attachmentNote = when {
                    result.failedAttachments == 0 -> ""
                    result.attachmentError != null -> " ${result.failedAttachments} anexo(s) não foram enviados: ${result.attachmentError}"
                    else -> " ${result.failedAttachments} anexo(s) não foram enviados."
                }
                if (result.outcome == SubmitOutcome.SENT && result.failedAttachments == 0) tone = NoticeTone.SUCCESS
                when (result.outcome) {
                    SubmitOutcome.SENT -> "Relato enviado. Ele já aparece para outras pessoas da região.$attachmentNote"
                    SubmitOutcome.SAVED_LOCALLY_LOGIN_REQUIRED -> "Relato salvo só neste aparelho. Entre na sua conta para enviá-lo."
                    SubmitOutcome.SAVED_LOCALLY_OFFLINE -> "Sem conexão: o relato ficou salvo só neste aparelho."
                    SubmitOutcome.SAVED_LOCALLY_REJECTED -> "O servidor recusou o relato. Ele ficou salvo só neste aparelho."
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                "O relato não pôde ser gravado no aparelho. Tente novamente."
            }
            localNoticeTone = tone
            isSubmittingReport = false
        }
    }

    /** Relatos com foto ou vídeo, do mais recente ao mais antigo (aba Vídeos). */
    val mediaReports: List<IncidentAlert>
        get() = _incidents.filter { it.media.isNotEmpty() }

}

@Composable
fun rememberCitizenViewModel(): CitizenViewModel {
    return remember { CitizenViewModel() }
}

private fun IncidentAlert.toEntity() = IncidentEntity(
    id, category.name, customBadgeTitle, title, description, address, neighborhood,
    distanceMeters, timeAgo, verified, confirmations, commentsCount, sharesCount,
    status.name, authorityAssigned, isAnonymous, mapXFraction, mapYFraction,
    latitude, longitude, aiSafetyRecommendation, userConfirmed,
    media.joinToString("\n") { (if (it.isVideo) "video|" else "image|") + it.url },
    createdAtEpochMs, fromServer
)

private fun parseMedia(raw: String): List<IncidentMedia> = raw.lines().mapNotNull { line ->
    val kind = line.substringBefore('|', "")
    val url = line.substringAfter('|', "")
    if (url.isBlank()) null else IncidentMedia(url, isVideo = kind == "video")
}

private fun IncidentEntity.toIncidentAlert(): IncidentAlert? {
    val parsedCategory = runCatching { IncidentCategory.valueOf(category) }.getOrNull() ?: return null
    val parsedStatus = runCatching { IncidentStatus.valueOf(status) }.getOrNull() ?: return null
    return IncidentAlert(
        id, parsedCategory, customBadgeTitle, title, description, address, neighborhood,
        distanceMeters, timeAgo, verified, confirmations, commentsCount, sharesCount,
        parsedStatus, authorityAssigned, isAnonymous, mapXFraction, mapYFraction,
        latitude, longitude, aiSafetyRecommendation, userConfirmed, parseMedia(media),
        createdAtEpochMs, fromServer
    ).let { alert -> if (createdAtEpochMs > 0) alert.copy(timeAgo = timeAgoLabel(createdAtEpochMs)) else alert }
}

class CitizenViewModelFactory(
    private val incidentRepository: IncidentRepository,
    private val publicSafetyRepository: PublicSafetyRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        require(modelClass.isAssignableFrom(CitizenViewModel::class.java))
        return CitizenViewModel(incidentRepository, publicSafetyRepository) as T
    }
}

private fun Guardian.toContact() = GuardianContact(
    id = id,
    name = name,
    relation = relation.ifBlank { "Guardião" },
    phone = phone,
    isSharingLiveLocation = false,
    distanceLabel = phone
)
