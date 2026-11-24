package com.example.projetorevisao.ui.home

import com.example.projetorevisao.data.remote.RailLine
import com.example.projetorevisao.ui.theme.RcTone
import com.example.projetorevisao.ui.viewmodel.IncidentAlert

/**
 * Frase-resumo do Início (TELAS §2.1): responde "está tudo bem no meu caminho agora?".
 * Função pura para poder ser testada sem tela.
 */
data class HomeSummary(val text: String, val tone: RcTone?)

/** Distância até a qual um alerta conta como "perto de você". */
const val NEARBY_METERS = 2_000

/** Distância até a qual um alerta crítico vira a frase principal. */
const val CRITICAL_METERS = 500

/** Distância conhecida (o app usa 0 quando não sabe a posição da pessoa). */
private val IncidentAlert.hasKnownDistance get() = distanceMeters > 0

private fun IncidentAlert.isNearby() = !hasKnownDistance || distanceMeters <= NEARBY_METERS

/** "Av. Carlos Caldeira Filho, 1200 - Capão Redondo" → "Av. Carlos Caldeira Filho". */
internal fun streetOf(address: String): String =
    address.substringBefore(",").substringBefore(" - ").trim().ifBlank { "a região" }

fun homeSummary(
    alerts: List<IncidentAlert>,
    railLines: List<RailLine>,
    loading: Boolean,
    /** A busca de relatos falhou: sem alertas na tela não significa "tudo bem". */
    alertsUnavailable: Boolean = false
): HomeSummary {
    val critical = alerts
        .filter { it.category.tone == RcTone.CRITICAL && it.hasKnownDistance && it.distanceMeters <= CRITICAL_METERS }
        .minByOrNull { it.distanceMeters }
    if (critical != null) {
        return HomeSummary("Evite ${streetOf(critical.address)} agora.", RcTone.CRITICAL)
    }

    val nearby = alerts.count { it.isNearby() }
    val railProblems = railLines.filterNot { it.normal }
    return when {
        nearby == 1 && railProblems.isEmpty() -> HomeSummary("Atenção a 1 ponto perto de você.", RcTone.WARNING)
        nearby > 1 && railProblems.isEmpty() -> HomeSummary("Atenção a $nearby pontos perto de você.", RcTone.WARNING)
        nearby == 0 && railProblems.size == 1 -> HomeSummary("${railProblems[0].line}: ${railProblems[0].situation.lowercase()}.", RcTone.WARNING)
        nearby == 0 && railProblems.size > 1 -> HomeSummary("${railProblems.size} linhas de trem e metrô com problema.", RcTone.WARNING)
        nearby > 0 -> HomeSummary("Atenção a $nearby ${if (nearby == 1) "ponto" else "pontos"} e ao transporte.", RcTone.WARNING)
        loading -> HomeSummary("Verificando o que acontece perto de você…", null)
        alertsUnavailable -> HomeSummary("Não consegui ver os alertas agora. Puxe para tentar de novo.", null)
        else -> HomeSummary("Seu caminho está tranquilo agora.", RcTone.SAFE)
    }
}

/** Ordem do feed: perto, recente e confirmado primeiro (TELAS §2.1). Menor = mais relevante. */
fun relevanceScore(alert: IncidentAlert, nowEpochMs: Long): Double {
    val distanceKm = if (alert.hasKnownDistance) alert.distanceMeters / 1000.0 else 1.0
    val ageHours = if (alert.createdAtEpochMs > 0) ((nowEpochMs - alert.createdAtEpochMs).coerceAtLeast(0) / 3_600_000.0) else 1.0
    val confirmationBoost = kotlin.math.ln(1.0 + alert.confirmations.coerceAtLeast(0))
    val criticalBoost = if (alert.category.tone == RcTone.CRITICAL) 0.5 else 0.0
    return distanceKm + ageHours * 0.5 - confirmationBoost * 0.3 - criticalBoost
}
