package com.example.projetorevisao.server.database

import org.slf4j.LoggerFactory
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.sql.Types

/**
 * Copia os dados do banco SQLite antigo para o Postgres, uma única vez.
 *
 * - Só roda se o Postgres ainda não tiver nenhum usuário nem relato (é seguro reiniciar o servidor).
 * - Não altera o arquivo SQLite: guarde-o por 1 a 2 semanas como cópia de segurança.
 * - Tudo numa transação: se algo falhar, o Postgres fica vazio e o servidor não sobe.
 * - Converte 0/1 para BOOLEAN e milissegundos para TIMESTAMPTZ, ignora o conteúdo de
 *   demonstração antigo e linhas que apontam para usuários ou relatos que não existem mais.
 */
object SqliteImporter {

    private val log = LoggerFactory.getLogger(SqliteImporter::class.java)
    private val legacyIncidents = setOf("inc-1", "inc-2", "inc-3")
    private val reportReasons = setOf("FALSO", "OFENSIVO", "DADOS_PESSOAIS", "VIOLENCIA", "SPAM", "OUTRO")

    /** Linhas copiadas por tabela (para o log e os testes). */
    data class Report(val counts: Map<String, Int>)

    fun importIfEmpty(sqlitePath: String, connect: () -> Connection = DatabaseFactory::getConnection): Report? {
        val file = File(sqlitePath)
        if (!file.isFile) {
            log.warn("MIGRATE_FROM_SQLITE aponta para {}, que não existe. Nada a importar.", sqlitePath)
            return null
        }
        connect().use { pg ->
            val empty = pg.createStatement().use { st ->
                st.executeQuery("SELECT NOT EXISTS (SELECT 1 FROM users) AND NOT EXISTS (SELECT 1 FROM incidents)")
                    .use { it.next() && it.getBoolean(1) }
            }
            if (!empty) {
                log.info("Postgres já tem dados: a importação do SQLite foi pulada.")
                return null
            }
            Class.forName("org.sqlite.JDBC")
            DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}?open_mode=1").use { sqlite ->
                pg.autoCommit = false
                try {
                    val report = Copier(sqlite, pg).copyAll()
                    pg.commit()
                    log.warn("Dados importados do SQLite ({}): {}", sqlitePath, report.counts)
                    return report
                } catch (ex: Exception) {
                    pg.rollback()
                    throw IllegalStateException("Falha ao importar o SQLite; nada foi gravado no Postgres.", ex)
                } finally {
                    pg.autoCommit = true
                }
            }
        }
    }

    private class Copier(private val src: Connection, private val dst: Connection) {
        private val counts = linkedMapOf<String, Int>()
        private val users = mutableSetOf<String>()
        private val emails = mutableSetOf<String>()
        private val incidents = mutableSetOf<String>()

        fun copyAll(): Report {
            copyUsers()
            copyIncidents()
            copyMedia()
            copyComments()
            copyConfirmations()
            copyReports()
            copyBlocks()
            copyGuardians()
            copySos()
            copyRefreshTokens()
            return Report(counts)
        }

        private fun hasTable(table: String) = src.createStatement().use { st ->
            st.executeQuery("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = '$table'").use { it.next() }
        }

        private fun columns(table: String): Set<String> = src.createStatement().use { st ->
            st.executeQuery("PRAGMA table_info($table)").use { rs ->
                generateSequence { if (rs.next()) rs.getString("name") else null }.toSet()
            }
        }

        /** Lê cada linha da tabela antiga e grava em lote no Postgres. */
        private fun copy(table: String, orderBy: String, insertSql: String, bind: PreparedStatement.(Row) -> Boolean) {
            if (!hasTable(table)) {
                counts[table] = 0
                return
            }
            val cols = columns(table)
            var copied = 0
            dst.prepareStatement(insertSql).use { ins ->
                src.createStatement().use { st ->
                    st.executeQuery("SELECT * FROM $table ORDER BY $orderBy").use { rs ->
                        val row = Row(rs, cols)
                        while (rs.next()) {
                            if (ins.bind(row)) {
                                ins.addBatch()
                                copied++
                            }
                        }
                    }
                }
                ins.executeBatch()
            }
            counts[table] = copied
        }

        private fun copyUsers() = copy(
            "users", "created_at",
            """
            INSERT INTO users (id, name, email, password_hash, neighborhood, trust_score, anonymous_default,
                               geofence_radius, terms_accepted_at, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ) { r ->
            val id = r.str("id") ?: return@copy false
            val email = r.str("email")?.trim()?.lowercase() ?: return@copy false
            // No SQLite o e-mail único diferenciava maiúsculas; no Postgres não. Fica a conta mais antiga.
            if (!emails.add(email)) return@copy false
            users += id
            setString(1, id)
            setString(2, r.str("name") ?: "Cidadão")
            setString(3, email)
            setString(4, r.str("password_hash"))
            setString(5, r.str("neighborhood") ?: "")
            setInt(6, r.int("trust_score") ?: 0)
            setBoolean(7, r.bool("anonymous_default", true))
            setFloat(8, r.double("geofence_radius")?.toFloat() ?: 2.0f)
            setTs(9, r.long("terms_accepted_at"))
            setTs(10, r.long("created_at") ?: System.currentTimeMillis())
            true
        }

        private fun copyIncidents() = copy(
            "incidents", "created_at",
            """
            INSERT INTO incidents (id, reporter_id, category, custom_badge_title, title, description, address,
                                   neighborhood, latitude, longitude, is_anonymous, status, verified, hidden,
                                   reviewed, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ) { r ->
            val id = r.str("id") ?: return@copy false
            if (id in legacyIncidents) return@copy false
            incidents += id
            setString(1, id)
            setUser(2, r.str("reporter_id"))
            setString(3, r.str("category"))
            setString(4, r.str("custom_badge_title"))
            setString(5, r.str("title"))
            setString(6, r.str("description"))
            setString(7, r.str("address") ?: "")
            setString(8, r.str("neighborhood") ?: "")
            setDouble(9, r.double("latitude") ?: -23.5505)
            setDouble(10, r.double("longitude") ?: -46.6333)
            setBoolean(11, r.bool("is_anonymous", true))
            setString(12, r.str("status") ?: "REPORTADO")
            setBoolean(13, r.bool("verified", false))
            setBoolean(14, r.bool("hidden", false))
            setBoolean(15, r.bool("reviewed", false))
            setTs(16, r.long("created_at") ?: System.currentTimeMillis())
            true
        }

        private fun copyMedia() = copy(
            "incident_media", "created_at",
            "INSERT INTO incident_media (id, incident_id, uploader_id, file_name, mime_type, size_bytes, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)"
        ) { r ->
            val incident = r.str("incident_id")
            if (incident !in incidents) return@copy false
            setString(1, r.str("id"))
            setString(2, incident)
            setUser(3, r.str("uploader_id"))
            setString(4, r.str("file_name"))
            setString(5, r.str("mime_type"))
            setLong(6, r.long("size_bytes") ?: 0)
            setTs(7, r.long("created_at") ?: System.currentTimeMillis())
            true
        }

        /** O SQLite guardava só o nome em texto: sem como ligar ao usuário, o comentário fica sem autor. */
        private fun copyComments() = copy(
            "comments", "created_at",
            "INSERT INTO comments (id, incident_id, author_id, is_anonymous, content, created_at) VALUES (?, ?, NULL, ?, ?, ?)"
        ) { r ->
            val incident = r.str("incident_id")
            if (incident !in incidents) return@copy false
            setString(1, r.str("id"))
            setString(2, incident)
            setBoolean(3, r.str("author_name") == "Cidadão Anônimo")
            setString(4, r.str("content"))
            setTs(5, r.long("created_at") ?: System.currentTimeMillis())
            true
        }

        private fun copyConfirmations() = copy(
            "incident_confirmations", "created_at",
            "INSERT INTO incident_confirmations (incident_id, user_id, created_at) VALUES (?, ?, ?) ON CONFLICT DO NOTHING"
        ) { r ->
            val incident = r.str("incident_id")
            val user = r.str("user_id")
            if (incident !in incidents || user !in users) return@copy false
            setString(1, incident)
            setString(2, user)
            setTs(3, r.long("created_at") ?: System.currentTimeMillis())
            true
        }

        private fun copyReports() = copy(
            "incident_reports", "created_at",
            """
            INSERT INTO incident_reports (id, incident_id, reporter_user_id, reason, details, created_at)
            VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING
            """.trimIndent()
        ) { r ->
            val incident = r.str("incident_id")
            val user = r.str("reporter_user_id")
            if (incident !in incidents || user !in users) return@copy false
            setString(1, r.str("id"))
            setString(2, incident)
            setString(3, user)
            setString(4, r.str("reason")?.takeIf { it in reportReasons } ?: "OUTRO")
            setString(5, r.str("details") ?: "")
            setTs(6, r.long("created_at") ?: System.currentTimeMillis())
            true
        }

        private fun copyBlocks() = copy(
            "user_blocks", "created_at",
            "INSERT INTO user_blocks (blocker_id, blocked_id, created_at) VALUES (?, ?, ?) ON CONFLICT DO NOTHING"
        ) { r ->
            val blocker = r.str("blocker_id")
            val blocked = r.str("blocked_id")
            if (blocker !in users || blocked !in users) return@copy false
            setString(1, blocker)
            setString(2, blocked)
            setTs(3, r.long("created_at") ?: System.currentTimeMillis())
            true
        }

        /** O SQLite ordenava pelo rowid; aqui a ordem vira created_at crescente. */
        private fun copyGuardians(): Unit {
            val base = System.currentTimeMillis()
            var seq = 0L
            copy(
                "guardians", "rowid",
                "INSERT INTO guardians (id, user_id, name, relation, phone, created_at) VALUES (?, ?, ?, ?, ?, ?)"
            ) { r ->
                val id = r.str("id")
                val user = r.str("user_id")
                if (user !in users || (user == "usr-default" && id in setOf("g-1", "g-2"))) return@copy false
                setString(1, id)
                setString(2, user)
                setString(3, r.str("name"))
                setString(4, r.str("relation") ?: "")
                setString(5, r.str("phone"))
                setTs(6, base + seq++)
                true
            }
        }

        private fun copySos() = copy(
            "sos_alerts", "created_at",
            """
            INSERT INTO sos_alerts (protocol_id, user_id, neighborhood, latitude, longitude, emergency_type,
                                    trigger_source, status, guardians_alerted_count, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ) { r ->
            setString(1, r.str("protocol_id") ?: return@copy false)
            setUser(2, r.str("user_id"))
            setString(3, r.str("neighborhood") ?: "")
            setNullableDouble(4, r.double("latitude"))
            setNullableDouble(5, r.double("longitude"))
            setString(6, r.str("emergency_type") ?: "")
            setString(7, r.str("trigger_source") ?: "")
            setString(8, r.str("status") ?: "REGISTRADO")
            setInt(9, r.int("guardians_alerted_count") ?: 0)
            setTs(10, r.long("created_at") ?: System.currentTimeMillis())
            true
        }

        /** Copiar as sessões evita que todo mundo precise entrar de novo depois da troca. */
        private fun copyRefreshTokens() = copy(
            "refresh_tokens", "created_at",
            "INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, revoked, created_at) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING"
        ) { r ->
            val user = r.str("user_id")
            val expires = r.long("expires_at") ?: return@copy false
            if (user !in users || expires < System.currentTimeMillis()) return@copy false
            setString(1, r.str("id"))
            setString(2, user)
            setString(3, r.str("token_hash"))
            setTs(4, expires)
            setBoolean(5, r.bool("revoked", false))
            setTs(6, r.long("created_at") ?: System.currentTimeMillis())
            true
        }

        /** Usuário que não existe mais vira NULL (a FK do Postgres não aceita órfãos). */
        private fun PreparedStatement.setUser(index: Int, userId: String?) =
            if (userId != null && userId in users) setString(index, userId) else setNull(index, Types.VARCHAR)
    }

    /** Acesso tolerante às colunas: bancos antigos podem não ter todas. */
    private class Row(private val rs: ResultSet, private val cols: Set<String>) {
        fun str(name: String): String? = if (name in cols) rs.getString(name) else null
        fun long(name: String): Long? = if (name in cols) rs.getLong(name).takeUnless { rs.wasNull() } else null
        fun int(name: String): Int? = if (name in cols) rs.getInt(name).takeUnless { rs.wasNull() } else null
        fun double(name: String): Double? = if (name in cols) rs.getDouble(name).takeUnless { rs.wasNull() } else null
        fun bool(name: String, default: Boolean): Boolean = int(name)?.let { it != 0 } ?: default
    }

    private fun PreparedStatement.setTs(index: Int, epochMs: Long?) =
        if (epochMs != null) setTimestamp(index, Timestamp(epochMs)) else setNull(index, Types.TIMESTAMP_WITH_TIMEZONE)

    private fun PreparedStatement.setNullableDouble(index: Int, value: Double?) =
        if (value != null) setDouble(index, value) else setNull(index, Types.DOUBLE)
}
