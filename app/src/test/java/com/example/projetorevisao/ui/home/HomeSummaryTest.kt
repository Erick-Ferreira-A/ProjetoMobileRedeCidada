package com.example.projetorevisao.ui.home

import com.example.projetorevisao.data.remote.RailLine
import com.example.projetorevisao.ui.theme.RcTone
import com.example.projetorevisao.ui.viewmodel.IncidentAlert
import com.example.projetorevisao.ui.viewmodel.IncidentCategory
import com.example.projetorevisao.ui.viewmodel.IncidentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSummaryTest {

    private fun alert(
        id: String,
        category: IncidentCategory,
        distance: Int,
        address: String = "Rua Teodoro Sampaio, 2100 - Pinheiros",
        confirmations: Int = 1,
        createdAt: Long = 0
    ) = IncidentAlert(
        id = id, category = category, title = "t", description = "d", address = address,
        neighborhood = "Pinheiros", distanceMeters = distance, timeAgo = "agora", verified = false,
        confirmations = confirmations, commentsCount = 0, sharesCount = 0, status = IncidentStatus.REPORTADO,
        authorityAssigned = "", isAnonymous = true, mapXFraction = 0f, mapYFraction = 0f,
        aiSafetyRecommendation = "", createdAtEpochMs = createdAt
    )

    private fun rail(line: String, normal: Boolean, situation: String = "Velocidade reduzida") =
        RailLine("Metrô", line, "L", if (normal) "Operação normal" else situation, "", "", normal)

    @Test
    fun nothingAround_isCalmAndSafeTone() {
        val s = homeSummary(emptyList(), listOf(rail("Linha 5", true)), loading = false)
        assertEquals("Seu caminho está tranquilo agora.", s.text)
        assertEquals(RcTone.SAFE, s.tone)
    }

    @Test
    fun whenAlertsCouldNotLoad_neverClaimsCalm() {
        val s = homeSummary(emptyList(), emptyList(), loading = false, alertsUnavailable = true)
        assertEquals(null, s.tone)
        assertTrue(s.text.startsWith("Não consegui ver os alertas"))
    }

    @Test
    fun whileLoading_doesNotClaimCalm() {
        val s = homeSummary(emptyList(), emptyList(), loading = true)
        assertEquals(null, s.tone)
        assertTrue(s.text.startsWith("Verificando"))
    }

    @Test
    fun criticalWithin500m_namesTheStreet() {
        val s = homeSummary(
            listOf(alert("a", IncidentCategory.ROUBO_FURTO, 320, "Av. Carlos Caldeira Filho, 1200 - Capão Redondo")),
            emptyList(), loading = false
        )
        assertEquals("Evite Av. Carlos Caldeira Filho agora.", s.text)
        assertEquals(RcTone.CRITICAL, s.tone)
    }

    @Test
    fun criticalWithUnknownDistance_doesNotBecomeAlarm() {
        val s = homeSummary(listOf(alert("a", IncidentCategory.ROUBO_FURTO, 0)), emptyList(), loading = false)
        assertEquals(RcTone.WARNING, s.tone)
        assertEquals("Atenção a 1 ponto perto de você.", s.text)
    }

    @Test
    fun farAlerts_areNotCountedAsNearby() {
        val s = homeSummary(listOf(alert("a", IncidentCategory.TRANSITO, 5_000)), emptyList(), loading = false)
        assertEquals("Seu caminho está tranquilo agora.", s.text)
    }

    @Test
    fun railProblemAlone_isReportedWithLineName() {
        val s = homeSummary(emptyList(), listOf(rail("Linha 5-Lilás", false), rail("Linha 4", true)), loading = false)
        assertEquals("Linha 5-Lilás: velocidade reduzida.", s.text)
    }

    @Test
    fun alertsAndRail_areBothMentioned() {
        val s = homeSummary(
            listOf(alert("a", IncidentCategory.ALAGAMENTO, 800), alert("b", IncidentCategory.TRANSITO, 900)),
            listOf(rail("Linha 9", false)), loading = false
        )
        assertEquals("Atenção a 2 pontos e ao transporte.", s.text)
    }

    @Test
    fun relevance_prefersCloserAndMoreConfirmed() {
        val now = 10_000_000_000L
        val near = alert("near", IncidentCategory.UTILIDADE_PUBLICA, 200, confirmations = 1, createdAt = now)
        val far = alert("far", IncidentCategory.UTILIDADE_PUBLICA, 1_800, confirmations = 1, createdAt = now)
        val confirmed = alert("conf", IncidentCategory.UTILIDADE_PUBLICA, 1_800, confirmations = 40, createdAt = now)
        assertTrue(relevanceScore(near, now) < relevanceScore(far, now))
        assertTrue(relevanceScore(confirmed, now) < relevanceScore(far, now))
    }

    @Test
    fun streetOf_handlesMissingSeparators() {
        assertEquals("Praça da Sé", streetOf("Praça da Sé"))
        assertEquals("a região", streetOf(""))
    }
}
