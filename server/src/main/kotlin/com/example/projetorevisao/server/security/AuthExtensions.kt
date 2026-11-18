package com.example.projetorevisao.server.security

import com.example.projetorevisao.server.models.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

/** Id do usuário autenticado, tirado do `sub` do JWT. Só use dentro de `authenticate(JwtConfig.AUTH_NAME)`. */
fun ApplicationCall.userId(): String =
    principal<JWTPrincipal>()?.subject ?: error("Rota protegida chamada sem usuário autenticado.")

suspend fun ApplicationCall.respondError(status: HttpStatusCode, message: String) {
    respond(status, ApiResponse<Unit>(success = false, message = message))
}
