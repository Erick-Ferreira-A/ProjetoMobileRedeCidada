package com.example.projetorevisao

import com.example.projetorevisao.data.remote.SpPublicSafetyApiService
import com.example.projetorevisao.ui.viewmodel.IncidentCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun sspDistrictTelemetry_returnsCorrectDpForCapaoRedondo() {
        val telemetry = SpPublicSafetyApiService.getDistrictTelemetry("Capão Redondo, São Paulo - Zona Sul")
        assertEquals("47º DP", telemetry.dpCode)
        assertTrue(telemetry.smartSampaCamerasActive > 100)
    }

    @Test
    fun integratedApisCatalog_containsAllRequiredPublicAndCloudApis() {
        val apis = SpPublicSafetyApiService.getIntegratedApisCatalog()
        assertEquals(5, apis.size)
        assertTrue(apis.any { it.category == "Segurança SP" })
        assertTrue(apis.any { it.category == "Mobilidade SP" })
        assertTrue(apis.any { it.category == "Meteorologia" })
        assertTrue(apis.any { it.category == "Google Cloud" })
    }

    @Test
    fun incidentCategories_haveFigmaAlignedBadges() {
        // Selos genéricos: não inventam detalhes que o morador não informou.
        assertEquals("ASSALTO / ROUBO", IncidentCategory.ROUBO_FURTO.badgeTitle)
        assertEquals("INCÊNDIO", IncidentCategory.INCENDIO.badgeTitle)
        assertEquals("ALAGAMENTO", IncidentCategory.ALAGAMENTO.badgeTitle)
        assertEquals("TEMPESTADE", IncidentCategory.TEMPESTADE.badgeTitle)
    }
}