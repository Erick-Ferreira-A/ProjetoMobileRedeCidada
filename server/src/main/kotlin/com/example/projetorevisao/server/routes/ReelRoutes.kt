package com.example.projetorevisao.server.routes

import com.example.projetorevisao.server.models.*
import com.example.projetorevisao.server.repository.ServerRepository
import io.ktor.http.HttpStatusCode
import com.example.projetorevisao.server.security.JwtConfig
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.reelRoutes(repository: ServerRepository) {
    route("/api/v1/reels") {

        get {
            val reels = repository.getReels()
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = reels))
        }

        authenticate(JwtConfig.AUTH_NAME) {
        post("/{id}/like") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse<ToggleLikeResponse>(success = false, message = "ID ausente."))
                return@post
            }
            val res = repository.toggleLikeReel(id)
            if (res != null) {
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = res))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse<ToggleLikeResponse>(success = false, message = "Reel não encontrado."))
            }
        }
        }
    }
}
