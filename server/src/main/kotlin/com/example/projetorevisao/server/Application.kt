package com.example.projetorevisao.server

import com.example.projetorevisao.server.config.AppConfig
import io.ktor.server.plugins.partialcontent.PartialContent
import com.example.projetorevisao.server.database.DatabaseFactory
import com.example.projetorevisao.server.models.ApiResponse
import com.example.projetorevisao.server.repository.AuthRepository
import com.example.projetorevisao.server.repository.MediaRepository
import com.example.projetorevisao.server.repository.ModerationRepository
import com.example.projetorevisao.server.repository.ServerRepository
import com.example.projetorevisao.server.routes.*
import com.example.projetorevisao.server.security.JwtConfig
import com.example.projetorevisao.server.transit.TransitService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

fun main(args: Array<String>) {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    println("=================================================================")
    println("  Iniciando Servidor Backend - Rede Cidadã SP na porta $port")
    println("=================================================================")

    // Falha logo na partida se a configuração de produção estiver incompleta (ex.: sem JWT_SECRET).
    println("  Ambiente: ${AppConfig.environment} • JWT configurado: ${AppConfig.jwtSecret.isNotEmpty()}")

    DatabaseFactory.init()

    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(transitService: TransitService = TransitService()) {
    val mediaRepository = MediaRepository()
    val moderationRepository = ModerationRepository(mediaRepository)
    val repository = ServerRepository(mediaRepository)
    val authRepository = AuthRepository()

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = !AppConfig.isProduction
            ignoreUnknownKeys = true
        })
    }

    // O app Android não precisa de CORS. Só liga quando há hosts web autorizados.
    if (AppConfig.corsAllowedHosts.isNotEmpty()) {
        install(CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
            AppConfig.corsAllowedHosts.forEach { host ->
                allowHost(host, schemes = if (AppConfig.isProduction) listOf("https") else listOf("http", "https"))
            }
        }
    }

    install(CallLogging)
    // Download em partes (Range): o player do celular precisa pular até o índice no fim dos vídeos.
    install(PartialContent)

    install(Authentication) {
        jwt(JwtConfig.AUTH_NAME) {
            realm = AppConfig.JWT_REALM
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (!credential.payload.subject.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<Unit>(success = false, message = "Faça login para continuar.")
                )
            }
        }
    }

    if (AppConfig.behindProxy) {
        install(XForwardedHeaders) {
            useLastProxy() // o último IP da lista é o que o proxy viu; os anteriores o cliente pode inventar
        }
    }

    install(RateLimit) {
        register(AUTH_RATE_LIMIT) {
            rateLimiter(limit = AppConfig.authRateLimitPerMinute, refillPeriod = 1.minutes)
            requestKey { call -> call.request.origin.remoteAddress }
        }
        register(TRANSIT_RATE_LIMIT) {
            rateLimiter(limit = 60, refillPeriod = 1.minutes)
            requestKey { call -> call.request.origin.remoteAddress }
        }
    }

    install(StatusPages) {
        exception<BadRequestException> { call, _ ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(success = false, message = "Requisição inválida. Confira os campos enviados.")
            )
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Erro não tratado em ${call.request.local.uri}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Unit>(success = false, message = "Erro interno do servidor. Tente novamente mais tarde.")
            )
        }
        status(HttpStatusCode.TooManyRequests) { call, status ->
            call.respond(status, ApiResponse<Unit>(success = false, message = "Muitas tentativas. Aguarde um minuto e tente de novo."))
        }
    }

    routing {
        authRoutes(repository, authRepository)
        legalRoutes(authRepository)
        incidentRoutes(repository, mediaRepository, moderationRepository)
        adminRoutes(moderationRepository)
        telemetryRoutes(repository)
        mobilityRoutes(repository)
        reelRoutes(repository)
        sosRoutes(repository)
        integrationRoutes(repository)
        transitRoutes(transitService)
    }
}
