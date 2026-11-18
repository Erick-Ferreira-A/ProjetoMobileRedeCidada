package com.example.projetorevisao.data.repository

import com.example.projetorevisao.data.remote.ExternalApiConnectionStatus
import com.example.projetorevisao.data.remote.SpPublicSafetyApiService
import com.example.projetorevisao.data.remote.SspDistrictTelemetry
import com.example.projetorevisao.data.remote.WeatherSafetyTelemetry

/** Keeps remote-source access behind the data layer used by the ViewModel. */
class PublicSafetyRepository {
    fun apiCatalog(): List<ExternalApiConnectionStatus> =
        SpPublicSafetyApiService.getIntegratedApisCatalog()

    fun districtFallback(neighborhood: String): SspDistrictTelemetry =
        SpPublicSafetyApiService.getDistrictTelemetry(neighborhood)

    suspend fun weather(areaLabel: String, latitude: Double?, longitude: Double?): WeatherSafetyTelemetry =
        SpPublicSafetyApiService.fetchLiveWeatherSafety(areaLabel, latitude, longitude)

    suspend fun district(neighborhood: String): SspDistrictTelemetry =
        SpPublicSafetyApiService.fetchLiveSspAndSmartSampaTelemetry(neighborhood)

    suspend fun lineOneReducedSpeed(): Boolean? =
        SpPublicSafetyApiService.fetchLineOneReducedSpeed()
}
