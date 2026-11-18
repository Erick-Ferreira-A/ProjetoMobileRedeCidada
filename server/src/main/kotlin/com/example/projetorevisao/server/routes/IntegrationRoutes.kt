package com.example.projetorevisao.server.routes

import com.example.projetorevisao.server.models.*
import com.example.projetorevisao.server.repository.ServerRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import java.lang.management.ManagementFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Route.integrationRoutes(repository: ServerRepository) {
    val startTime = System.currentTimeMillis()

    route("/api/v1/integrations") {

        get("/health") {
            val uptimeSecs = (System.currentTimeMillis() - startTime) / 1000
            val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())

            val status = HealthStatusDto(
                status = "UP",
                service = "Rede Cidadã SP Backend Server",
                version = "1.0.0",
                environment = "production",
                uptimeSeconds = uptimeSecs,
                timestamp = timestamp
            )

            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = status))
        }

        get("/catalog") {
            val catalog = repository.getIntegratedApisCatalog()
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = catalog))
        }
    }
}
