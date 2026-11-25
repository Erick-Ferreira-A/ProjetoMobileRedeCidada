package com.example.projetorevisao.server.config

import org.slf4j.LoggerFactory
import java.io.File
import java.security.SecureRandom
import java.util.Properties
import java.util.Base64

/**
 * Configuração do servidor lida de variáveis de ambiente.
 *
 * Variáveis:
 * - `APP_ENV`: `development` (padrão) ou `production`.
 * - `JWT_SECRET`: segredo HMAC dos tokens (mín. 32 caracteres). Obrigatório em produção.
 * - `JWT_ISSUER` / `JWT_AUDIENCE`: identificam emissor e público dos tokens.
 * - `ACCESS_TOKEN_TTL_MINUTES` (padrão 15) / `REFRESH_TOKEN_TTL_DAYS` (padrão 30).
 * - `AUTH_RATE_LIMIT_PER_MINUTE` (padrão 10): tentativas por IP nas rotas de login/cadastro.
 * - `CORS_ALLOWED_HOSTS`: hosts separados por vírgula (ex.: `localhost:3000,painel.exemplo.com`).
 *   Vazio = CORS desligado (o app Android não precisa de CORS).
 * - `SEED_DEMO_DATA` (padrão `true` fora de produção): cria usuário e dados de demonstração.
 * - `DATABASE_URL`: banco Postgres (obrigatória). `DB_POOL_SIZE` e `MIGRATE_FROM_SQLITE` são opcionais.
 * - `MEDIA_DIR`: pasta das fotos/vídeos dos relatos (padrão `uploads`).
 * - `SPTRANS_TOKEN`: token da API Olho Vivo (ônibus municipais). Sem ele, as rotas de ônibus respondem 503.
 * - `ARTESP_API_KEY`: chave da API Trilhos (status das linhas concedidas). Sem ela, a rota de trilhos responde 503.
 *
 * Para desenvolvimento, as mesmas chaves podem ficar em `secrets.properties` na pasta do servidor
 * (fora do git; veja `secrets.properties.example`). Variáveis de ambiente têm prioridade.
 */
object AppConfig {
    private val log = LoggerFactory.getLogger(AppConfig::class.java)

    private val secrets: Properties = Properties().apply {
        val file = File(System.getenv("SECRETS_FILE") ?: "secrets.properties")
        if (file.isFile) file.inputStream().use { load(it) }
    }

    private fun env(name: String): String? =
        System.getenv(name)?.takeIf { it.isNotBlank() }
            ?: System.getProperty(name)?.takeIf { it.isNotBlank() }
            ?: secrets.getProperty(name)?.takeIf { it.isNotBlank() }

    val environment: String = env("APP_ENV") ?: "development"
    val isProduction: Boolean get() = environment.equals("production", ignoreCase = true)

    val jwtIssuer: String = env("JWT_ISSUER") ?: "rede-cidada-sp"
    val jwtAudience: String = env("JWT_AUDIENCE") ?: "rede-cidada-app"
    const val JWT_REALM = "Rede Cidadã SP"

    val accessTokenTtlMinutes: Long = env("ACCESS_TOKEN_TTL_MINUTES")?.toLongOrNull() ?: 15
    val refreshTokenTtlDays: Long = env("REFRESH_TOKEN_TTL_DAYS")?.toLongOrNull() ?: 30
    val authRateLimitPerMinute: Int = env("AUTH_RATE_LIMIT_PER_MINUTE")?.toIntOrNull() ?: 10

    val corsAllowedHosts: List<String> =
        env("CORS_ALLOWED_HOSTS")?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

    val seedDemoData: Boolean = env("SEED_DEMO_DATA")?.toBooleanStrictOrNull() ?: !isProduction

    /**
     * Banco Postgres. Aceita o formato do Railway (postgresql://usuario:senha@host:porta/banco)
     * ou uma URL JDBC (jdbc:postgresql://...). Sem ela o servidor não sobe; em desenvolvimento,
     * use a tarefa :server:runLocal, que liga um Postgres embutido.
     */
    val databaseUrl: String? = env("DATABASE_URL")

    /** Conexões abertas ao mesmo tempo com o Postgres (o plano Hobby do Railway aguenta poucas). */
    val databasePoolSize: Int = env("DB_POOL_SIZE")?.toIntOrNull()?.coerceIn(1, 20) ?: 5

    /**
     * Caminho do banco SQLite antigo. Se o Postgres estiver vazio na inicialização, os dados são
     * copiados de lá uma única vez (o arquivo não é alterado). Usado só na troca de produção.
     */
    val migrateFromSqlitePath: String? = env("MIGRATE_FROM_SQLITE")

    val mediaDirectory: String = env("MEDIA_DIR") ?: "uploads"

    /** Chave do painel de moderação (cabeçalho X-Admin-Token). Sem ela, as rotas de moderação ficam desligadas. */
    val adminToken: String? = env("ADMIN_TOKEN")?.takeIf { it.length >= 24 }

    /**
     * true quando o servidor fica atrás de um proxy (Railway, Render…): o IP real do cliente vem do
     * último item de X-Forwarded-For, que é o que o proxy acrescenta. Sem isso, todos os usuários
     * dividiriam o mesmo limite de tentativas de login.
     */
    val behindProxy: Boolean = env("TRUST_PROXY")?.toBooleanStrictOrNull() ?: false

    val sptransToken: String? = env("SPTRANS_TOKEN")
    val artespApiKey: String? = env("ARTESP_API_KEY")

    /**
     * Intermediário no Brasil para a API Trilhos (a ARTESP não aceita conexões do servidor nos EUA).
     * Com ARTESP_RELAY_URL, o servidor pede o status ao intermediário (que guarda a chave da ARTESP)
     * e se identifica com o cabeçalho X-Relay-Token = ARTESP_RELAY_TOKEN.
     */
    val artespRelayUrl: String? = env("ARTESP_RELAY_URL")
    val artespRelayToken: String? = env("ARTESP_RELAY_TOKEN")

    val jwtSecret: String = resolveJwtSecret()

    private fun resolveJwtSecret(): String {
        val configured = env("JWT_SECRET")
        if (configured != null) {
            require(configured.length >= 32) { "JWT_SECRET precisa ter pelo menos 32 caracteres." }
            return configured
        }
        check(!isProduction) { "JWT_SECRET é obrigatório quando APP_ENV=production." }
        log.warn(
            "JWT_SECRET não definido: usando um segredo aleatório temporário. " +
                "Tokens deixam de valer quando o servidor reinicia."
        )
        val bytes = ByteArray(48).also { SecureRandom().nextBytes(it) }
        return Base64.getEncoder().encodeToString(bytes)
    }
}
