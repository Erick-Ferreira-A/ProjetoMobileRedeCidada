package com.example.projetorevisao.server.database

import com.example.projetorevisao.server.config.AppConfig
import com.example.projetorevisao.server.security.PasswordHasher
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URLDecoder
import java.sql.Connection
import java.sql.Timestamp

/**
 * Banco Postgres: pool de conexões (HikariCP) e esquema versionado (Flyway, em resources/db/migration).
 * Para mudar o esquema, crie um arquivo novo V2__descricao.sql; nunca edite uma migração já aplicada.
 */
object DatabaseFactory {

    private val log = LoggerFactory.getLogger(DatabaseFactory::class.java)
    private const val LEGACY_BROKEN_DEMO_HASH = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f"

    /** E-mail da conta de demonstração (só existe quando SEED_DEMO_DATA=true). */
    const val DEMO_EMAIL = "demo@redecidada.app"

    /** Senha da conta de demonstração (só existe quando SEED_DEMO_DATA=true). */
    private val demoPassword: String =
        System.getenv("SEED_DEMO_PASSWORD")?.takeIf { it.isNotBlank() } ?: "senha123"

    @Volatile
    private var dataSource: HikariDataSource? = null

    /** Conexão do pool. Sempre use dentro de `use { }`: fechar devolve a conexão ao pool. */
    fun getConnection(): Connection =
        (dataSource ?: error("Banco não iniciado: chame DatabaseFactory.init() antes.")).connection

    @Synchronized
    fun init() {
        if (dataSource != null) return
        val rawUrl = AppConfig.databaseUrl
            ?: error(
                "DATABASE_URL não configurada. Em produção, ligue o Postgres do Railway; " +
                    "no PC, rode .\\gradlew.bat :server:runLocal."
            )
        val jdbc = JdbcSettings.parse(rawUrl)
        val ds = HikariDataSource(HikariConfig().apply {
            jdbcUrl = jdbc.url
            jdbc.user?.let { username = it }
            jdbc.password?.let { password = it }
            maximumPoolSize = AppConfig.databasePoolSize
            minimumIdle = 1
            poolName = "rede-cidada"
            connectionTimeout = 10_000
        })

        val applied = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .load()
            .migrate()
        log.info("Banco Postgres pronto ({} migração(ões) aplicada(s) agora).", applied.migrationsExecuted)
        dataSource = ds

        AppConfig.migrateFromSqlitePath?.let { SqliteImporter.importIfEmpty(it) }
        if (AppConfig.seedDemoData) getConnection().use { seedData(it) }
    }

    /** Fecha o pool (usado nos testes e ao desligar o servidor). */
    @Synchronized
    fun close() {
        dataSource?.close()
        dataSource = null
    }

    private fun seedData(connection: Connection) {
        val hasUsers = connection.prepareStatement("SELECT EXISTS (SELECT 1 FROM users)").use { stmt ->
            stmt.executeQuery().use { rs -> rs.next() && rs.getBoolean(1) }
        }

        if (!hasUsers) {
            connection.prepareStatement(
                """
                INSERT INTO users (id, name, email, password_hash, neighborhood, trust_score, anonymous_default, geofence_radius, created_at)
                VALUES (?, ?, ?, ?, ?, 940, TRUE, 2.0, ?)
                """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, "usr-default")
                stmt.setString(2, "Cidadão Conectado")
                stmt.setString(3, DEMO_EMAIL)
                stmt.setString(4, PasswordHasher.hash(demoPassword))
                stmt.setString(5, "Capão Redondo, São Paulo - Zona Sul")
                stmt.setTimestamp(6, Timestamp(System.currentTimeMillis()))
                stmt.executeUpdate()
            }
            log.warn("Conta de demonstração criada ($DEMO_EMAIL). Desative com SEED_DEMO_DATA=false.")
        } else {
            // Bancos antigos gravaram um hash que não corresponde a nenhuma senha. Regrava a senha da conta demo.
            connection.prepareStatement("UPDATE users SET password_hash = ? WHERE id = 'usr-default' AND password_hash = ?").use { stmt ->
                stmt.setString(1, PasswordHasher.hash(demoPassword))
                stmt.setString(2, LEGACY_BROKEN_DEMO_HASH)
                if (stmt.executeUpdate() > 0) log.warn("Senha da conta de demonstração regravada em BCrypt.")
            }
        }
    }
}

/** URL JDBC e credenciais separadas, a partir do formato do Railway ou de uma URL JDBC. */
internal data class JdbcSettings(val url: String, val user: String?, val password: String?) {
    companion object {
        fun parse(raw: String): JdbcSettings {
            val value = raw.trim()
            if (value.startsWith("jdbc:")) return JdbcSettings(value, null, null)
            val uri = URI(value)
            require(uri.scheme == "postgres" || uri.scheme == "postgresql") {
                "DATABASE_URL precisa começar com postgresql://, postgres:// ou jdbc:postgresql://"
            }
            val (user, password) = uri.rawUserInfo
                ?.split(":", limit = 2)
                ?.map { URLDecoder.decode(it, Charsets.UTF_8) }
                ?.let { it.getOrNull(0) to it.getOrNull(1) }
                ?: (null to null)
            val port = if (uri.port > 0) ":${uri.port}" else ""
            val query = uri.rawQuery?.let { "?$it" } ?: ""
            return JdbcSettings("jdbc:postgresql://${uri.host}$port${uri.rawPath}$query", user, password)
        }
    }
}
