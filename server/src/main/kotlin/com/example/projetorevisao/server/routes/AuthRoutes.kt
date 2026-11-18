package com.example.projetorevisao.server.routes

import com.example.projetorevisao.server.models.*
import com.example.projetorevisao.server.repository.AuthRepository
import com.example.projetorevisao.server.repository.RegisterResult
import com.example.projetorevisao.server.repository.ServerRepository
import com.example.projetorevisao.server.security.JwtConfig
import com.example.projetorevisao.server.security.Validators
import com.example.projetorevisao.server.security.respondError
import com.example.projetorevisao.server.security.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

val AUTH_RATE_LIMIT = RateLimitName("auth")

fun Route.authRoutes(repository: ServerRepository, authRepository: AuthRepository) {
    route("/api/v1/auth") {

        rateLimit(AUTH_RATE_LIMIT) {
            post("/login") {
                val req = call.receive<LoginRequest>()
                if (req.email.isBlank() || req.password.isEmpty() || req.password.length > Validators.PASSWORD_MAX) {
                    call.respondError(HttpStatusCode.BadRequest, "Informe e-mail e senha.")
                    return@post
                }
                val auth = authRepository.login(req.email, req.password)
                if (auth == null) {
                    call.respondError(HttpStatusCode.Unauthorized, "E-mail ou senha incorretos.")
                } else {
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, message = "Autenticação realizada com sucesso", data = auth))
                }
            }

            post("/register") {
                val req = call.receive<RegisterRequest>()
                val error = Validators.name(req.name)
                    ?: Validators.email(req.email.trim())
                    ?: Validators.password(req.password)
                    ?: Validators.neighborhood(req.neighborhood)
                    ?: if (!req.acceptedTerms) "Aceite os termos de uso e a política de privacidade para criar a conta." else null
                if (error != null) {
                    call.respondError(HttpStatusCode.BadRequest, error)
                    return@post
                }
                when (val result = authRepository.register(req)) {
                    is RegisterResult.Success ->
                        call.respond(HttpStatusCode.Created, ApiResponse(success = true, message = "Cadastro realizado com sucesso", data = result.auth))
                    RegisterResult.EmailTaken ->
                        call.respondError(HttpStatusCode.Conflict, "Já existe uma conta com este e-mail.")
                }
            }

            post("/refresh") {
                val req = call.receive<RefreshTokenRequest>()
                val auth = req.refreshToken.takeIf { it.isNotBlank() }?.let { authRepository.refresh(it) }
                if (auth == null) {
                    call.respondError(HttpStatusCode.Unauthorized, "Sessão expirada. Faça login novamente.")
                } else {
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = auth))
                }
            }
        }

        post("/logout") {
            val req = call.receive<RefreshTokenRequest>()
            if (req.refreshToken.isNotBlank()) authRepository.logout(req.refreshToken)
            call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Sessão encerrada."))
        }

        authenticate(JwtConfig.AUTH_NAME) {
            post("/logout-all") {
                authRepository.logoutAll(call.userId())
                call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Todas as sessões foram encerradas."))
            }

            post("/delete-account") {
                val req = call.receive<DeleteAccountRequest>()
                if (authRepository.deleteAccount(call.userId(), req.password)) {
                    call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Conta excluída. Seus dados foram apagados."))
                } else {
                    call.respondError(HttpStatusCode.Forbidden, "Senha incorreta.")
                }
            }

            get("/me") {
                val profile = repository.getUserProfile(call.userId())
                if (profile != null) {
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = profile))
                } else {
                    call.respondError(HttpStatusCode.NotFound, "Usuário não encontrado.")
                }
            }

            put("/profile") {
                val req = call.receive<UpdateProfileRequest>()
                val error = req.neighborhood?.let { Validators.neighborhood(it) }
                    ?: req.geofenceRadiusKm?.let { Validators.geofenceRadius(it) }
                if (error != null) {
                    call.respondError(HttpStatusCode.BadRequest, error)
                    return@put
                }
                val updated = repository.updateUserProfile(call.userId(), req)
                if (updated != null) {
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, message = "Perfil atualizado com sucesso", data = updated))
                } else {
                    call.respondError(HttpStatusCode.NotFound, "Usuário não encontrado.")
                }
            }
        }
    }
}
