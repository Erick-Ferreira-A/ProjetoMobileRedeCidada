package com.example.projetorevisao.data.repository

import android.content.Context
import android.net.Uri
import com.example.projetorevisao.data.local.IncidentDao
import com.example.projetorevisao.data.local.IncidentEntity
import com.example.projetorevisao.data.remote.ApiResult
import com.example.projetorevisao.data.media.MediaAttachment
import com.example.projetorevisao.data.media.MediaPreparationException
import com.example.projetorevisao.data.remote.CitizenBackendApiService
import kotlinx.coroutines.flow.Flow

/** Resultado do envio de um relato novo. */
enum class SubmitOutcome { SENT, SAVED_LOCALLY_LOGIN_REQUIRED, SAVED_LOCALLY_OFFLINE, SAVED_LOCALLY_REJECTED }

/** Resultado do envio + quantos anexos falharam (e o motivo do primeiro). */
data class SubmitResult(val outcome: SubmitOutcome, val failedAttachments: Int = 0, val attachmentError: String? = null)

/** Coordena o banco local (Room) e o backend. */
class IncidentRepository(private val dao: IncidentDao, private val context: Context? = null) {

    fun observeIncidents(): Flow<List<IncidentEntity>> = dao.observeAll()

    /** Apaga os relatos fictícios que versões de teste antigas gravaram no aparelho (ids inc-1 a inc-8). */
    suspend fun removeLegacyExamples() {
        (1..8).forEach { dao.deleteById("inc-$it") }
    }

    /** Atualiza só a cópia local (ex.: confirmação). Não envia nada ao servidor. */
    suspend fun saveLocal(incident: IncidentEntity) {
        dao.upsert(incident)
    }

    /**
     * Salva o relato no aparelho e tenta enviá-lo. Se o servidor aceitar, a cópia local é trocada
     * pelo registro do servidor (que tem outro id), para não aparecer duplicado.
     */
    suspend fun submitNew(incident: IncidentEntity, attachments: List<Uri> = emptyList()): SubmitResult {
        dao.upsert(incident)
        val (result, created) = CitizenBackendApiService.postIncidentToServer(
            category = incident.category,
            title = incident.title,
            description = incident.description,
            address = incident.address,
            neighborhood = incident.neighborhood,
            isAnonymous = incident.isAnonymous,
            latitude = incident.latitude,
            longitude = incident.longitude
        )
        if (created == null) {
            val outcome = when {
                result is ApiResult.Failure && result.isUnauthorized -> SubmitOutcome.SAVED_LOCALLY_LOGIN_REQUIRED
                result is ApiResult.Failure && result.status == null -> SubmitOutcome.SAVED_LOCALLY_OFFLINE
                else -> SubmitOutcome.SAVED_LOCALLY_REJECTED
            }
            // Os anexos dependem do relato no servidor; sem ele, não há onde guardá-los.
            return SubmitResult(outcome, failedAttachments = attachments.size)
        }

        dao.deleteById(incident.id)
        dao.upsert(created)

        val ctx = context ?: return SubmitResult(SubmitOutcome.SENT, attachments.size)
        val mediaLines = mutableListOf<String>()
        var failed = 0
        var firstError: String? = null
        attachments.take(MediaAttachment.MAX_ATTACHMENTS).forEachIndexed { index, uri ->
            try {
                val prepared = MediaAttachment.prepare(ctx, uri, index + 1)
                val (upload, line) = CitizenBackendApiService.uploadMedia(created.id, prepared.bytes, prepared.mimeType, prepared.fileName)
                if (line != null) {
                    mediaLines += line
                } else {
                    failed++
                    if (firstError == null) firstError = (upload as? ApiResult.Failure)?.message
                }
            } catch (e: MediaPreparationException) {
                failed++
                if (firstError == null) firstError = e.message
            } catch (_: SecurityException) {
                failed++ // o acesso temporário ao arquivo escolhido expirou
            }
        }
        if (mediaLines.isNotEmpty()) dao.upsert(created.copy(media = mediaLines.joinToString("\n")))
        failed += (attachments.size - MediaAttachment.MAX_ATTACHMENTS).coerceAtLeast(0)
        return SubmitResult(SubmitOutcome.SENT, failed, firstError)
    }

    suspend fun syncWithServer(neighborhood: String? = null): Boolean {
        val serverIncidents = CitizenBackendApiService.fetchIncidentsFromServer(neighborhood = neighborhood) ?: return false
        // Substitui as cópias locais (confirmações e anexos novos aparecem) e tira o que o servidor não mostra mais.
        dao.upsertAll(serverIncidents)
        if (serverIncidents.isEmpty()) dao.deleteAllServerCopies()
        else dao.deleteServerCopiesExcept(serverIncidents.map { it.id })
        return true
    }

    /** Liga/desliga a confirmação no servidor e atualiza a cópia local. */
    suspend fun toggleConfirmation(incident: IncidentEntity): ApiResult {
        val (result, total, confirmed) = CitizenBackendApiService.toggleConfirmation(incident.id)
        if (result is ApiResult.Success && total != null && confirmed != null) {
            dao.upsert(incident.copy(confirmations = total, userConfirmed = confirmed))
        }
        return result
    }

    /** Denuncia e já tira o relato da tela desta pessoa. */
    suspend fun report(incidentId: String, reason: String, details: String): ApiResult {
        val result = CitizenBackendApiService.reportIncident(incidentId, reason, details)
        if (result is ApiResult.Success) dao.deleteById(incidentId)
        return result
    }

    /** Bloqueia o autor e recarrega a lista (some tudo o que ele publicou). */
    suspend fun blockAuthor(incidentId: String): ApiResult {
        val result = CitizenBackendApiService.blockAuthor(incidentId)
        if (result is ApiResult.Success) {
            dao.deleteById(incidentId)
            syncWithServer()
        }
        return result
    }
}
