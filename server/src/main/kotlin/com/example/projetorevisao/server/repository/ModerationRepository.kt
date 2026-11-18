package com.example.projetorevisao.server.repository

import com.example.projetorevisao.server.database.DatabaseFactory
import com.example.projetorevisao.server.models.ModerationItemDto
import java.io.File
import java.util.UUID

/** Resultado de uma denúncia ou bloqueio feito pelo usuário. */
enum class CommunityActionResult { DONE, NOT_FOUND, OWN_CONTENT }

/**
 * Denúncias, bloqueios e moderação (exigidos pela política de conteúdo gerado por usuário do Google Play).
 *
 * - Quem denuncia deixa de ver o relato na hora.
 * - Com [AUTO_HIDE_REPORTS] denúncias de pessoas diferentes, o relato some para todos até a moderação decidir.
 * - Quem bloqueia o autor deixa de ver todos os relatos dele.
 */
class ModerationRepository(private val mediaRepository: MediaRepository = MediaRepository()) {

    companion object {
        const val AUTO_HIDE_REPORTS = 3
        val REASONS = setOf("FALSO", "OFENSIVO", "DADOS_PESSOAIS", "VIOLENCIA", "SPAM", "OUTRO")
    }

    fun reportIncident(userId: String, incidentId: String, reason: String, details: String): CommunityActionResult {
        DatabaseFactory.getConnection().use { conn ->
            val reporter = reporterOf(conn, incidentId) ?: return CommunityActionResult.NOT_FOUND
            if (reporter == userId) return CommunityActionResult.OWN_CONTENT
            conn.prepareStatement(
                "INSERT OR IGNORE INTO incident_reports (id, incident_id, reporter_user_id, reason, details, created_at) VALUES (?, ?, ?, ?, ?, ?)"
            ).use { st ->
                st.setString(1, "rep-${UUID.randomUUID()}")
                st.setString(2, incidentId)
                st.setString(3, userId)
                st.setString(4, reason)
                st.setString(5, details.take(500))
                st.setLong(6, System.currentTimeMillis())
                st.executeUpdate()
            }
            val count = conn.prepareStatement("SELECT COUNT(*) FROM incident_reports WHERE incident_id = ?").use { st ->
                st.setString(1, incidentId)
                st.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
            if (count >= AUTO_HIDE_REPORTS) {
                conn.prepareStatement("UPDATE incidents SET hidden = 1 WHERE id = ? AND reviewed = 0").use { st ->
                    st.setString(1, incidentId)
                    st.executeUpdate()
                }
            }
            return CommunityActionResult.DONE
        }
    }

    fun blockAuthorOf(userId: String, incidentId: String): CommunityActionResult {
        DatabaseFactory.getConnection().use { conn ->
            val author = reporterOf(conn, incidentId) ?: return CommunityActionResult.NOT_FOUND
            if (author == userId) return CommunityActionResult.OWN_CONTENT
            if (author.isEmpty()) return CommunityActionResult.DONE // autor já apagou a conta
            conn.prepareStatement("INSERT OR IGNORE INTO user_blocks (blocker_id, blocked_id, created_at) VALUES (?, ?, ?)").use { st ->
                st.setString(1, userId)
                st.setString(2, author)
                st.setLong(3, System.currentTimeMillis())
                st.executeUpdate()
            }
            return CommunityActionResult.DONE
        }
    }

    /** Relatos com denúncia que ainda não foram analisados, do mais denunciado para o menos. */
    fun pendingReports(): List<ModerationItemDto> {
        val items = mutableListOf<ModerationItemDto>()
        DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement(
                """
                SELECT i.id, i.title, i.description, i.hidden, COUNT(r.id) AS total,
                       GROUP_CONCAT(r.reason) AS reasons, GROUP_CONCAT(r.details, ' | ') AS details
                FROM incidents i JOIN incident_reports r ON r.incident_id = i.id
                WHERE i.reviewed = 0
                GROUP BY i.id ORDER BY total DESC, MAX(r.created_at) DESC
                """.trimIndent()
            ).use { st ->
                st.executeQuery().use { rs ->
                    while (rs.next()) {
                        items += ModerationItemDto(
                            incidentId = rs.getString("id"),
                            title = rs.getString("title"),
                            description = rs.getString("description"),
                            reportCount = rs.getInt("total"),
                            reasons = rs.getString("reasons").orEmpty().split(',').filter { it.isNotBlank() }.distinct(),
                            details = rs.getString("details").orEmpty().split(" | ").filter { it.isNotBlank() },
                            hiddenFromEveryone = rs.getInt("hidden") == 1
                        )
                    }
                }
            }
        }
        val media = mediaRepository.mediaForAll(items.map { it.incidentId })
        return items.map { it.copy(media = media[it.incidentId].orEmpty()) }
    }

    /** Apaga o relato, os anexos (arquivos inclusive), comentários, confirmações e denúncias. */
    fun removeIncident(incidentId: String): Boolean {
        DatabaseFactory.getConnection().use { conn ->
            if (reporterOf(conn, incidentId) == null) return false
            val files = conn.prepareStatement("SELECT file_name FROM incident_media WHERE incident_id = ?").use { st ->
                st.setString(1, incidentId)
                st.executeQuery().use { rs -> generateSequence { if (rs.next()) rs.getString(1) else null }.toList() }
            }
            conn.autoCommit = false
            try {
                for (sql in listOf(
                    "DELETE FROM incident_media WHERE incident_id = ?",
                    "DELETE FROM comments WHERE incident_id = ?",
                    "DELETE FROM incident_confirmations WHERE incident_id = ?",
                    "DELETE FROM incident_reports WHERE incident_id = ?",
                    "DELETE FROM incidents WHERE id = ?"
                )) {
                    conn.prepareStatement(sql).use { st -> st.setString(1, incidentId); st.executeUpdate() }
                }
                conn.commit()
            } catch (ex: Exception) {
                conn.rollback()
                throw ex
            } finally {
                conn.autoCommit = true
            }
            files.forEach { File(mediaRepository.directory, it).delete() }
            return true
        }
    }

    /** A moderação decidiu manter: o relato volta a aparecer e novas denúncias não o escondem mais sozinhas. */
    fun keepIncident(incidentId: String): Boolean {
        DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement("UPDATE incidents SET hidden = 0, reviewed = 1 WHERE id = ?").use { st ->
                st.setString(1, incidentId)
                return st.executeUpdate() > 0
            }
        }
    }

    /** Id de quem criou o relato; "" se o autor apagou a conta; null se o relato não existe. */
    private fun reporterOf(conn: java.sql.Connection, incidentId: String): String? =
        conn.prepareStatement("SELECT reporter_id FROM incidents WHERE id = ?").use { st ->
            st.setString(1, incidentId)
            st.executeQuery().use { rs -> if (rs.next()) rs.getString(1).orEmpty() else null }
        }
}
