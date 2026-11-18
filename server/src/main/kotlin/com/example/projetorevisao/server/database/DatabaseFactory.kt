package com.example.projetorevisao.server.database

import com.example.projetorevisao.server.config.AppConfig
import com.example.projetorevisao.server.security.PasswordHasher
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager

object DatabaseFactory {

    private val log = LoggerFactory.getLogger(DatabaseFactory::class.java)
    private const val LEGACY_BROKEN_DEMO_HASH = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f"

    /** E-mail da conta de demonstração (só existe quando SEED_DEMO_DATA=true). */
    const val DEMO_EMAIL = "demo@redecidada.app"

    /** Senha da conta de demonstração (só existe quando SEED_DEMO_DATA=true). */
    private val demoPassword: String =
        System.getenv("SEED_DEMO_PASSWORD")?.takeIf { it.isNotBlank() } ?: "senha123"

    private val jdbcUrl: String get() = "jdbc:sqlite:${AppConfig.databasePath}"

    fun getConnection(): Connection {
        Class.forName("org.sqlite.JDBC")
        return DriverManager.getConnection(jdbcUrl).also { conn ->
            conn.createStatement().use { it.execute("PRAGMA foreign_keys = ON;") }
        }
    }

    fun init() {
        getConnection().use { connection ->
            connection.createStatement().use { statement ->
                // Enable foreign keys in SQLite
                statement.execute("PRAGMA foreign_keys = ON;")

                // 1. Users table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        cpf_masked TEXT NOT NULL,
                        password_hash TEXT NOT NULL,
                        neighborhood TEXT NOT NULL,
                        trust_score INTEGER NOT NULL DEFAULT 940,
                        anonymous_default INTEGER NOT NULL DEFAULT 1,
                        geofence_radius REAL NOT NULL DEFAULT 2.0,
                        created_at INTEGER NOT NULL
                    );
                """.trimIndent())

                // 2. Incidents table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS incidents (
                        id TEXT PRIMARY KEY,
                        category TEXT NOT NULL,
                        custom_badge_title TEXT,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        address TEXT NOT NULL,
                        neighborhood TEXT NOT NULL,
                        distance_meters INTEGER NOT NULL,
                        time_ago TEXT NOT NULL,
                        verified INTEGER NOT NULL DEFAULT 0,
                        confirmations INTEGER NOT NULL DEFAULT 0,
                        comments_count INTEGER NOT NULL DEFAULT 0,
                        shares_count INTEGER NOT NULL DEFAULT 0,
                        status TEXT NOT NULL,
                        authority_assigned TEXT NOT NULL,
                        is_anonymous INTEGER NOT NULL DEFAULT 1,
                        map_x_fraction REAL NOT NULL,
                        map_y_fraction REAL NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        ai_safety_recommendation TEXT NOT NULL,
                        user_confirmed INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL
                    );
                """.trimIndent())

                // 3. Comments table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS comments (
                        id TEXT PRIMARY KEY,
                        incident_id TEXT NOT NULL,
                        author_name TEXT NOT NULL,
                        content TEXT NOT NULL,
                        time_ago TEXT NOT NULL,
                        is_official_authority INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE
                    );
                """.trimIndent())

                // 4. Safety Reels table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS reels (
                        id TEXT PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        author_name TEXT NOT NULL,
                        author_role TEXT NOT NULL,
                        is_official_verified INTEGER NOT NULL DEFAULT 1,
                        location TEXT NOT NULL,
                        time_ago TEXT NOT NULL,
                        views_label TEXT NOT NULL,
                        likes_count INTEGER NOT NULL DEFAULT 0,
                        comments_count INTEGER NOT NULL DEFAULT 0,
                        shares_count INTEGER NOT NULL DEFAULT 0,
                        category TEXT NOT NULL,
                        is_liked INTEGER NOT NULL DEFAULT 0,
                        duration_seconds INTEGER NOT NULL DEFAULT 18
                    );
                """.trimIndent())

                // 5. Emergency SOS Alerts table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS sos_alerts (
                        protocol_id TEXT PRIMARY KEY,
                        neighborhood TEXT NOT NULL,
                        latitude REAL,
                        longitude REAL,
                        emergency_type TEXT NOT NULL,
                        trigger_source TEXT NOT NULL,
                        dispatched_unit TEXT NOT NULL,
                        estimated_arrival_minutes INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        guardians_alerted_count INTEGER NOT NULL,
                        created_at INTEGER NOT NULL
                    );
                """.trimIndent())

                // 6. Guardians table
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS guardians (
                        id TEXT PRIMARY KEY,
                        user_id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        relation TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        is_sharing_live_location INTEGER NOT NULL DEFAULT 1,
                        distance_label TEXT NOT NULL
                    );
                """.trimIndent())

                // 7. Refresh tokens (só o hash SHA-256 é guardado)
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS refresh_tokens (
                        id TEXT PRIMARY KEY,
                        user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        token_hash TEXT UNIQUE NOT NULL,
                        expires_at INTEGER NOT NULL,
                        revoked INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL
                    );
                """.trimIndent())
                statement.execute("CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);")

                addColumnIfMissing(connection, "sos_alerts", "user_id", "TEXT")
                addColumnIfMissing(connection, "incidents", "reporter_id", "TEXT")

                // 8. Fotos e vídeos dos relatos (o arquivo fica em disco, aqui só a referência)
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS incident_media (
                        id TEXT PRIMARY KEY,
                        incident_id TEXT NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
                        uploader_id TEXT,
                        file_name TEXT NOT NULL,
                        mime_type TEXT NOT NULL,
                        size_bytes INTEGER NOT NULL,
                        created_at INTEGER NOT NULL
                    );
                """.trimIndent())
                statement.execute("CREATE INDEX IF NOT EXISTS idx_media_incident ON incident_media(incident_id);")
                statement.execute("CREATE INDEX IF NOT EXISTS idx_guardians_user ON guardians(user_id);")

                // Confirmações ("Eu também vi"): uma por pessoa.
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS incident_confirmations (
                        incident_id TEXT NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
                        user_id TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        PRIMARY KEY (incident_id, user_id)
                    );
                """.trimIndent())
                // Denúncias de relatos: uma por pessoa por relato.
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS incident_reports (
                        id TEXT PRIMARY KEY,
                        incident_id TEXT NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
                        reporter_user_id TEXT NOT NULL,
                        reason TEXT NOT NULL,
                        details TEXT NOT NULL DEFAULT '',
                        created_at INTEGER NOT NULL,
                        UNIQUE (incident_id, reporter_user_id)
                    );
                """.trimIndent())
                // Bloqueios: quem bloqueia deixa de ver os relatos do bloqueado.
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS user_blocks (
                        blocker_id TEXT NOT NULL,
                        blocked_id TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        PRIMARY KEY (blocker_id, blocked_id)
                    );
                """.trimIndent())
                addColumnIfMissing(connection, "incidents", "hidden", "INTEGER NOT NULL DEFAULT 0")
                addColumnIfMissing(connection, "incidents", "reviewed", "INTEGER NOT NULL DEFAULT 0")
                addColumnIfMissing(connection, "users", "terms_accepted_at", "INTEGER")
                // O app não coleta mais CPF: apaga o que restou de versões anteriores.
                statement.execute("UPDATE users SET cpf_masked = '' WHERE cpf_masked <> ''")
                // Confirmações antigas eram um contador solto; passa a valer só a tabela por pessoa.
                statement.execute(
                    "UPDATE incidents SET confirmations = " +
                        "(SELECT COUNT(*) FROM incident_confirmations c WHERE c.incident_id = incidents.id)"
                )

                // Seed initial data if empty
                if (AppConfig.seedDemoData) seedData(connection)
                removeLegacyDemoContent(statement)
            }
        }
    }

    private fun addColumnIfMissing(connection: Connection, table: String, column: String, definition: String) {
        val exists = connection.createStatement().use { st ->
            st.executeQuery("PRAGMA table_info($table)").use { rs ->
                generateSequence { if (rs.next()) rs.getString("name") else null }.any { it == column }
            }
        }
        if (!exists) connection.createStatement().use { it.execute("ALTER TABLE $table ADD COLUMN $column $definition") }
    }

    /**
     * Versões antigas criavam relatos, vídeos e guardiões fictícios (ids fixos). Eles não podem
     * aparecer para usuários reais, então são apagados em toda inicialização.
     */
    private fun removeLegacyDemoContent(statement: java.sql.Statement) {
        statement.execute("DELETE FROM comments WHERE incident_id IN ('inc-1','inc-2','inc-3')")
        statement.execute("DELETE FROM incidents WHERE id IN ('inc-1','inc-2','inc-3')")
        statement.execute("DELETE FROM reels WHERE id IN ('reel-1','reel-2')")
        statement.execute("DELETE FROM guardians WHERE id IN ('g-1','g-2') AND user_id = 'usr-default'")
        // O e-mail antigo da conta de teste usava domínio do governo (sp.gov.br).
        statement.execute("UPDATE users SET email = '$DEMO_EMAIL' WHERE email = 'cidadao@redecidada.sp.gov.br'")
        // Relatos gravados antes da correção diziam ter sido encaminhados à PM/Bombeiros/Defesa Civil.
        statement.execute(
            "UPDATE incidents SET authority_assigned = 'Não é enviado à polícia. Em perigo, ligue 190.', " +
                "distance_meters = 0 WHERE authority_assigned LIKE 'Polícia Militar de SP%' " +
                "OR authority_assigned LIKE 'Corpo de Bombeiros%' OR authority_assigned LIKE 'Defesa Civil de SP%'"
        )
    }

    private fun seedData(connection: Connection) {
        val now = System.currentTimeMillis()

        // Seed default citizen user
        val userCheck = connection.prepareStatement("SELECT COUNT(*) FROM users").use { stmt ->
            val rs = stmt.executeQuery()
            rs.next() && rs.getInt(1) > 0
        }

        if (!userCheck) {
            connection.prepareStatement("""
                INSERT INTO users (id, name, email, cpf_masked, password_hash, neighborhood, trust_score, anonymous_default, geofence_radius, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()).use { stmt ->
                stmt.setString(1, "usr-default")
                stmt.setString(2, "Cidadão Conectado")
                stmt.setString(3, DEMO_EMAIL)
                stmt.setString(4, "")
                stmt.setString(5, PasswordHasher.hash(demoPassword))
                stmt.setString(6, "Capão Redondo, São Paulo - Zona Sul")
                stmt.setInt(7, 940)
                stmt.setInt(8, 1)
                stmt.setFloat(9, 2.0f)
                stmt.setLong(10, now)
                stmt.executeUpdate()
            }
            log.warn("Conta de demonstração criada ($DEMO_EMAIL). Desative com SEED_DEMO_DATA=false.")
        } else {
            // Bancos criados pela versão anterior gravaram um hash que não corresponde a nenhuma senha
            // (o login só funcionava por um desvio já removido). Regrava a senha da conta demo.
            connection.prepareStatement("UPDATE users SET password_hash = ? WHERE id = 'usr-default' AND password_hash = ?").use { stmt ->
                stmt.setString(1, PasswordHasher.hash(demoPassword))
                stmt.setString(2, LEGACY_BROKEN_DEMO_HASH)
                if (stmt.executeUpdate() > 0) log.warn("Senha da conta de demonstração regravada em BCrypt.")
            }
        }
    }
}
