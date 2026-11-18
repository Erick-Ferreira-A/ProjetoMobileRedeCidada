package com.example.projetorevisao.server.routes

import com.example.projetorevisao.server.config.AppConfig
import com.example.projetorevisao.server.models.ApiResponse
import com.example.projetorevisao.server.repository.ModerationRepository
import com.example.projetorevisao.server.security.respondError
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.security.MessageDigest

/**
 * Moderação (uso interno). Protegida pelo cabeçalho `X-Admin-Token` igual à variável ADMIN_TOKEN.
 * Sem ADMIN_TOKEN configurado, todas as rotas respondem 404.
 */
fun Route.adminRoutes(moderationRepository: ModerationRepository) {
    route("/api/v1/admin") {
        get("/reports") {
            if (!call.isAdmin()) return@get call.respondError(HttpStatusCode.NotFound, "Não encontrado.")
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = moderationRepository.pendingReports()))
        }
        post("/incidents/{id}/remove") {
            if (!call.isAdmin()) return@post call.respondError(HttpStatusCode.NotFound, "Não encontrado.")
            if (moderationRepository.removeIncident(call.parameters["id"]!!)) {
                call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Relato apagado."))
            } else {
                call.respondError(HttpStatusCode.NotFound, "Relato não encontrado.")
            }
        }
        post("/incidents/{id}/keep") {
            if (!call.isAdmin()) return@post call.respondError(HttpStatusCode.NotFound, "Não encontrado.")
            if (moderationRepository.keepIncident(call.parameters["id"]!!)) {
                call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Relato mantido e visível de novo."))
            } else {
                call.respondError(HttpStatusCode.NotFound, "Relato não encontrado.")
            }
        }
    }
}

private fun ApplicationCall.isAdmin(): Boolean {
    val expected = AppConfig.adminToken ?: return false
    val given = request.headers["X-Admin-Token"] ?: return false
    return MessageDigest.isEqual(expected.toByteArray(), given.toByteArray())
}
