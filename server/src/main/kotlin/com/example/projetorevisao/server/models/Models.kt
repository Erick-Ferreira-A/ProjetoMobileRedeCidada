package com.example.projetorevisao.server.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

// --- Auth DTOs ---
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val neighborhood: String,
    /** O cadastro só é aceito com os termos de uso e a política de privacidade aceitos. */
    val acceptedTerms: Boolean = false
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
    val userId: String,
    val name: String,
    val email: String,
    val neighborhood: String,
    val userTrustScore: Int
)

@Serializable
data class UserProfileDto(
    val userId: String,
    val name: String,
    val email: String,
    val neighborhood: String,
    val userTrustScore: Int,
    val anonymousReportingDefault: Boolean = true,
    val geofenceRadiusKm: Float = 2.0f,
    val totalReportsCreated: Int = 0,
    val totalConfirmationsGiven: Int = 0
)

@Serializable
data class UpdateProfileRequest(
    val neighborhood: String? = null,
    val anonymousReportingDefault: Boolean? = null,
    val geofenceRadiusKm: Float? = null
)

// --- Incident DTOs ---
@Serializable
data class IncidentDto(
    val id: String,
    val category: String,
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
    val status: String,
    val authorityAssigned: String,
    val isAnonymous: Boolean,
    val mapXFraction: Float,
    val mapYFraction: Float,
    val latitude: Double,
    val longitude: Double,
    val aiSafetyRecommendation: String,
    val userConfirmed: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val media: List<MediaDto> = emptyList()
)

/** Foto ou vídeo de um relato. `url` é relativa ao servidor (ex.: /api/v1/media/{id}). */
@Serializable
data class MediaDto(
    val id: String,
    val url: String,
    val mimeType: String
)

@Serializable
data class CreateIncidentRequest(
    val category: String,
    val title: String,
    val description: String,
    val address: String,
    val neighborhood: String,
    val isAnonymous: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class ConfirmIncidentResponse(
    val incidentId: String,
    val confirmations: Int,
    val userConfirmed: Boolean
)

@Serializable
data class CommentDto(
    val id: String,
    val incidentId: String,
    val authorName: String,
    val content: String,
    val timeAgo: String,
    val isOfficialAuthority: Boolean = false
)

@Serializable
data class CreateCommentRequest(
    val content: String,
    val isAnonymous: Boolean = false
)

// --- Telemetry DTOs ---
@Serializable
data class WeatherTelemetryDto(
    val regionName: String,
    val temperatureCelsius: Double,
    val precipitationMm: Double,
    val windSpeedKmh: Double,
    val windGustsKmh: Double,
    val humidityPercent: Int,
    val conditionLabel: String,
    val inmetAlertLevel: String,
    val cgeFloodRiskLabel: String,
    val cgeKeywordMatchCount: Int,
    val sourceApi: String,
    val lastUpdatedTime: String,
    val isLiveNetworkData: Boolean
)

@Serializable
data class DistrictTelemetryDto(
    val dpCode: String,
    val dpName: String,
    val militaryBattalion: String,
    val smartSampaCamerasActive: Int,
    val securityTotemsCount: Int,
    val riskIndexScore: Int,
    val peakAlertHours: String,
    val recentTrendSummary: String,
    val sp156OpenProtocolsNearby: Int,
    val isLiveSynced: Boolean = true
)

// --- Mobility DTOs ---
@Serializable
data class MetroLineDto(
    val code: String,
    val name: String,
    val operator: String,
    val colorHex: String,
    val operationalStatus: String,
    val isNormal: Boolean,
    val crowdLevel: String,
    val headWayMinutes: Int,
    val safetyNote: String,
    val activeSecurityAgents: Int
)

@Serializable
data class SafeRouteDto(
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

// --- Safety Reels DTOs ---
@Serializable
data class SafetyReelDto(
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
    val category: String,
    val isLiked: Boolean = false,
    val durationSeconds: Int = 18
)

@Serializable
data class ToggleLikeResponse(
    val reelId: String,
    val isLiked: Boolean,
    val newLikesCount: Int
)

// --- Emergency SOS DTOs ---
@Serializable
data class SosAlertRequest(
    val neighborhood: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val emergencyType: String = "POLICIA_E_GUARDIOS",
    val triggerSource: String = "BOTAO_SOS_APP"
)

/**
 * O servidor só registra o alerta e devolve os guardiões do usuário. Quem avisa os guardiões é o app
 * (WhatsApp, pelo aparelho). Não existe integração com 190/COPOM: o app orienta a ligar.
 */
@Serializable
data class SosAlertResponse(
    val protocolId: String,
    val timestamp: String,
    val status: String,
    val guardians: List<GuardianContactDto>
)

@Serializable
data class GuardianContactDto(
    val id: String,
    val name: String,
    val relation: String,
    val phone: String,
    val isSharingLiveLocation: Boolean,
    val distanceLabel: String
)

@Serializable
data class DeleteAccountRequest(
    val password: String
)

@Serializable
data class AddGuardianRequest(
    val name: String,
    val relation: String,
    val phone: String
)

// --- Integrations & Health DTOs ---
@Serializable
data class HealthStatusDto(
    val status: String,
    val service: String,
    val version: String,
    val environment: String,
    val uptimeSeconds: Long,
    val timestamp: String
)

@Serializable
data class ExternalApiStatusDto(
    val id: String,
    val name: String,
    val provider: String,
    val endpointSummary: String,
    val category: String,
    val isConnected: Boolean,
    val latencyMs: Int,
    val featuresProvided: String
)

@Serializable
data class ReportIncidentRequest(
    /** FALSO, OFENSIVO, DADOS_PESSOAIS, VIOLENCIA, SPAM ou OUTRO. */
    val reason: String,
    val details: String = ""
)

@Serializable
data class ModerationItemDto(
    val incidentId: String,
    val title: String,
    val description: String,
    val reportCount: Int,
    val reasons: List<String>,
    val details: List<String>,
    val hiddenFromEveryone: Boolean,
    val media: List<MediaDto> = emptyList()
)
