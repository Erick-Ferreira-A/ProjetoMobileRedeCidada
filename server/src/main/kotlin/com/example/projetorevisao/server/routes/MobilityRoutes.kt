package com.example.projetorevisao.server.routes

import com.example.projetorevisao.server.models.*
import com.example.projetorevisao.server.repository.ServerRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.mobilityRoutes(repository: ServerRepository) {
    route("/api/v1/mobility") {

        get("/metro") {
            val lines = repository.getMetroLines()
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = lines))
        }

        get("/routes") {
            val routes = repository.getSafeRoutes()
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = routes))
        }
    }
}
