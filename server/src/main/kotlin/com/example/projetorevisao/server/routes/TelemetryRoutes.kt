package com.example.projetorevisao.server.routes

import com.example.projetorevisao.server.models.*
import com.example.projetorevisao.server.repository.ServerRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.telemetryRoutes(repository: ServerRepository) {
    route("/api/v1/telemetry") {

        get("/weather") {
            val neighborhood = call.request.queryParameters["neighborhood"] ?: "Capão Redondo, São Paulo - Zona Sul"
            val weatherData = repository.getWeatherTelemetry(neighborhood)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = weatherData))
        }

        get("/district") {
            val neighborhood = call.request.queryParameters["neighborhood"] ?: "Capão Redondo, São Paulo - Zona Sul"
            val districtData = repository.getDistrictTelemetry(neighborhood)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = districtData))
        }
    }
}
