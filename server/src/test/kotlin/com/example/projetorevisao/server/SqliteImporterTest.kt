package com.example.projetorevisao.server

import com.example.projetorevisao.server.database.SqliteImporter
import org.flywaydb.core.Flyway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

/** Cópia do banco SQLite de produção (esquema antigo) para o Postgres. */
class SqliteImporterTest {

    private val day = 24 * 60 * 60 * 1000L
    private val now = System.currentTimeMillis()

    private fun postgres(): () -> Connection {
        val url = TestDatabase.jdbcUrl("import_test")
        Flyway.configure().dataSource(url, null, null).cleanDisabled(false).load().apply { clean(); migrate() }
        return { DriverManager.getConnection(url) }
    }

    /** Banco no formato que o servidor antigo deixava em /data/rede_cidada.db. */
    private fun legacySqlite(): File {
        val file = File.createTempFile("legado", ".db").apply { deleteOnExit() }
        DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}").use { c ->
            c.createStatement().use { st ->
                listOf(
                    "CREATE TABLE users (id TEXT PRIMARY KEY, name TEXT, email TEXT UNIQUE, cpf_masked TEXT, password_hash TEXT, neighborhood TEXT, trust_score INTEGER, anonymous_default INTEGER, geofence_radius REAL, created_at INTEGER, terms_accepted_at INTEGER)",
                    "CREATE TABLE incidents (id TEXT PRIMARY KEY, category TEXT, custom_badge_title TEXT, title TEXT, description TEXT, address TEXT, neighborhood TEXT, distance_meters INTEGER, time_ago TEXT, verified INTEGER, confirmations INTEGER, comments_count INTEGER, shares_count INTEGER, status TEXT, authority_assigned TEXT, is_anonymous INTEGER, map_x_fraction REAL, map_y_fraction REAL, latitude REAL, longitude REAL, ai_safety_recommendation TEXT, user_confirmed INTEGER, created_at INTEGER, reporter_id TEXT, hidden INTEGER, reviewed INTEGER)",
                    "CREATE TABLE comments (id TEXT PRIMARY KEY, incident_id TEXT, author_name TEXT, content TEXT, time_ago TEXT, is_official_authority INTEGER, created_at INTEGER)",
                    "CREATE TABLE reels (id TEXT PRIMARY KEY, title TEXT)",
                    "CREATE TABLE sos_alerts (protocol_id TEXT PRIMARY KEY, neighborhood TEXT, latitude REAL, longitude REAL, emergency_type TEXT, trigger_source TEXT, dispatched_unit TEXT, estimated_arrival_minutes INTEGER, status TEXT, guardians_alerted_count INTEGER, created_at INTEGER, user_id TEXT)",
                    "CREATE TABLE guardians (id TEXT PRIMARY KEY, user_id TEXT, name TEXT, relation TEXT, phone TEXT, is_sharing_live_location INTEGER, distance_label TEXT)",
                    "CREATE TABLE refresh_tokens (id TEXT PRIMARY KEY, user_id TEXT, token_hash TEXT UNIQUE, expires_at INTEGER, revoked INTEGER, created_at INTEGER)",
                    "CREATE TABLE incident_media (id TEXT PRIMARY KEY, incident_id TEXT, uploader_id TEXT, file_name TEXT, mime_type TEXT, size_bytes INTEGER, created_at INTEGER)",
                    "CREATE TABLE incident_confirmations (incident_id TEXT, user_id TEXT, created_at INTEGER, PRIMARY KEY (incident_id, user_id))",
                    "CREATE TABLE incident_reports (id TEXT PRIMARY KEY, incident_id TEXT, reporter_user_id TEXT, reason TEXT, details TEXT, created_at INTEGER)",
                    "CREATE TABLE user_blocks (blocker_id TEXT, blocked_id TEXT, created_at INTEGER, PRIMARY KEY (blocker_id, blocked_id))",

                    "INSERT INTO users VALUES ('usr-a', 'Ana', 'ana@exemplo.com', '', 'hash-a', 'Centro', 0, 1, 2.0, ${now - 9 * day}, ${now - 9 * day})",
                    "INSERT INTO users VALUES ('usr-b', 'Bia', 'bia@exemplo.com', '', 'hash-b', 'Sé', 0, 0, 3.5, ${now - 8 * day}, NULL)",
                    // Mesmo e-mail com maiúsculas: no Postgres o e-mail é único sem diferenciar; fica a conta mais antiga.
                    "INSERT INTO users VALUES ('usr-dup', 'Ana 2', 'ANA@exemplo.com', '', 'hash-x', 'Centro', 0, 1, 2.0, ${now - day}, NULL)",

                    "INSERT INTO incidents VALUES ('inc-1', 'ROUBO_FURTO', NULL, 'Demo antiga', 'x', 'x', 'x', 0, '', 0, 0, 0, 0, 'REPORTADO', 'x', 1, 0.5, 0.5, -23.5, -46.6, 'x', 0, ${now - 30 * day}, NULL, 0, 0)",
                    "INSERT INTO incidents VALUES ('inc-real', 'ALAGAMENTO', NULL, 'Rua alagada', 'Água na altura do joelho', 'Rua A, 10', 'Centro', 120, 'há 2 h', 0, 7, 3, 0, 'REPORTADO', 'texto', 0, 0.5, 0.5, -23.55, -46.63, 'dica', 1, ${now - 2 * day}, 'usr-a', 1, 0)",
                    "INSERT INTO incidents VALUES ('inc-orfao', 'TRANSITO', NULL, 'Sem autor', 'x', 'x', 'Sé', 0, '', 0, 0, 0, 0, 'REPORTADO', 'x', 1, 0.5, 0.5, -23.5, -46.6, 'x', 0, ${now - day}, 'usr-apagado', 0, 0)",

                    "INSERT INTO incident_media VALUES ('m-1', 'inc-real', 'usr-a', 'm-1.jpg', 'image/jpeg', 1234, ${now - 2 * day})",
                    "INSERT INTO incident_media VALUES ('m-velho', 'inc-1', 'usr-a', 'x.jpg', 'image/jpeg', 1, ${now - 30 * day})",
                    "INSERT INTO comments VALUES ('c-anon', 'inc-real', 'Cidadão Anônimo', 'Vi também', 'há 1 h', 0, ${now - day})",
                    "INSERT INTO comments VALUES ('c-nome', 'inc-real', 'Bia', 'Cuidado', 'há 1 h', 0, ${now - day + 1})",
                    "INSERT INTO incident_confirmations VALUES ('inc-real', 'usr-b', ${now - day})",
                    "INSERT INTO incident_confirmations VALUES ('inc-real', 'usr-apagado', ${now - day})",
                    "INSERT INTO incident_reports VALUES ('r-1', 'inc-real', 'usr-b', 'MOTIVO_ANTIGO', 'detalhe', ${now - day})",
                    "INSERT INTO user_blocks VALUES ('usr-b', 'usr-a', ${now - day})",
                    "INSERT INTO guardians VALUES ('g-2', 'usr-a', 'Mãe', 'Família', '+55 11 90000-0001', 1, 'x')",
                    "INSERT INTO guardians VALUES ('g-1', 'usr-a', 'Pai', 'Família', '+55 11 90000-0002', 1, 'x')",
                    "INSERT INTO guardians VALUES ('g-orfao', 'usr-apagado', 'X', 'Y', '1', 1, 'x')",
                    "INSERT INTO sos_alerts VALUES ('SOS-AAA', 'Centro', -23.5, -46.6, 'GERAL', 'BOTAO_SOS_APP', 'NENHUMA', 0, 'REGISTRADO', 2, ${now - day}, 'usr-a')",
                    "INSERT INTO sos_alerts VALUES ('SOS-BBB', 'Centro', NULL, NULL, 'GERAL', 'BOTAO_SOS_APP', 'NENHUMA', 0, 'REGISTRADO', 0, ${now - day}, NULL)",
                    "INSERT INTO refresh_tokens VALUES ('t-ok', 'usr-a', 'hash-ok', ${now + 10 * day}, 0, ${now - day})",
                    "INSERT INTO refresh_tokens VALUES ('t-vencido', 'usr-a', 'hash-vencido', ${now - day}, 0, ${now - 31 * day})"
                ).forEach { st.execute(it) }
            }
        }
        return file
    }

    private fun Connection.one(sql: String): String? =
        createStatement().use { st -> st.executeQuery(sql).use { rs -> if (rs.next()) rs.getString(1) else null } }

    @Test
    fun copiesLegacyDataFixingTypesAndOrphans() {
        val connect = postgres()
        val report = SqliteImporter.importIfEmpty(legacySqlite().absolutePath, connect)
        assertNotNull(report)
        val counts = report!!.counts
        assertEquals(2, counts["users"]) // a duplicada por maiúsculas fica de fora
        assertEquals(2, counts["incidents"]) // inc-1 (demonstração antiga) fica de fora
        assertEquals(1, counts["incident_media"])
        assertEquals(1, counts["incident_confirmations"])
        assertEquals(2, counts["guardians"])
        assertEquals(1, counts["refresh_tokens"]) // o vencido fica de fora

        connect().use { pg ->
            // Booleanos e datas convertidos
            assertEquals("f", pg.one("SELECT anonymous_default FROM users WHERE id = 'usr-b'"))
            assertEquals("t", pg.one("SELECT hidden FROM incidents WHERE id = 'inc-real'"))
            assertEquals(
                (now - 2 * day).toString(),
                pg.one("SELECT (extract(epoch FROM created_at) * 1000)::bigint FROM incidents WHERE id = 'inc-real'")
            )
            // Autor que não existe mais vira NULL (a FK não aceita órfão)
            assertNull(pg.one("SELECT reporter_id FROM incidents WHERE id = 'inc-orfao'"))
            assertNull(pg.one("SELECT user_id FROM sos_alerts WHERE protocol_id = 'SOS-BBB'"))
            // Motivo de denúncia desconhecido vira OUTRO
            assertEquals("OUTRO", pg.one("SELECT reason FROM incident_reports WHERE id = 'r-1'"))
            // Comentário: o anônimo continua anônimo; o com nome fica sem autor ligado
            assertEquals("t", pg.one("SELECT is_anonymous FROM comments WHERE id = 'c-anon'"))
            assertEquals("f", pg.one("SELECT is_anonymous FROM comments WHERE id = 'c-nome'"))
            // Guardiões mantêm a ordem de cadastro do SQLite (rowid): g-2 entrou antes de g-1
            assertEquals("g-2", pg.one("SELECT id FROM guardians WHERE user_id = 'usr-a' ORDER BY created_at LIMIT 1"))
            // reels não existe mais
            assertNull(pg.one("SELECT to_regclass('public.reels')::text"))
        }

        // Rodar de novo não duplica nada: o Postgres já tem dados.
        assertNull(SqliteImporter.importIfEmpty(legacySqlite().absolutePath, connect))
    }

    @Test
    fun deletingAUserKeepsPublicContentAnonymous() {
        val connect = postgres()
        SqliteImporter.importIfEmpty(legacySqlite().absolutePath, connect)
        connect().use { pg ->
            pg.createStatement().use { it.executeUpdate("DELETE FROM users WHERE id = 'usr-a'") }
            // Pessoal: apagado (CASCADE)
            assertEquals("0", pg.one("SELECT COUNT(*) FROM guardians WHERE user_id = 'usr-a'"))
            assertEquals("0", pg.one("SELECT COUNT(*) FROM refresh_tokens WHERE user_id = 'usr-a'"))
            assertEquals("0", pg.one("SELECT COUNT(*) FROM user_blocks WHERE blocked_id = 'usr-a'"))
            assertEquals("0", pg.one("SELECT COUNT(*) FROM sos_alerts WHERE protocol_id = 'SOS-AAA'"))
            // Público: continua, sem ligação com a pessoa (SET NULL)
            assertNull(pg.one("SELECT reporter_id FROM incidents WHERE id = 'inc-real'"))
            assertNull(pg.one("SELECT uploader_id FROM incident_media WHERE id = 'm-1'"))
            assertEquals("1", pg.one("SELECT COUNT(*) FROM incidents WHERE id = 'inc-real'"))
        }
    }
}
