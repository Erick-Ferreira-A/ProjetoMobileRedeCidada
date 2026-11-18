package com.example.projetorevisao.server.routes

import com.example.projetorevisao.server.models.ApiResponse
import com.example.projetorevisao.server.security.respondError
import com.example.projetorevisao.server.transit.TransitResult
import com.example.projetorevisao.server.transit.TransitService
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.response.respond
import io.ktor.server.routing.*

val TRANSIT_RATE_LIMIT = RateLimitName("transit")

private suspend inline fun <reified T : Any> RoutingContext.respondTransit(result: TransitResult<T>) {
    when (result) {
        is TransitResult.Ok -> call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = result.data))
        is TransitResult.NotConfigured -> call.respondError(HttpStatusCode.ServiceUnavailable, result.message)
        is TransitResult.Unavailable -> call.respondError(HttpStatusCode.BadGateway, result.message)
    }
}

/** Ônibus (SPTrans Olho Vivo) e trilhos (ARTESP). Leitura pública, com limite por IP para proteger as cotas das fontes. */
fun Route.transitRoutes(transit: TransitService) {
    rateLimit(TRANSIT_RATE_LIMIT) {
        route("/api/v1/transit") {
            get("/rail/status") {
                respondTransit(transit.railStatus())
            }

            get("/bus/lines") {
                val query = call.request.queryParameters["q"]?.trim().orEmpty()
                if (query.length < 2 || query.length > 60) {
                    call.respondError(HttpStatusCode.BadRequest, "Digite o número ou o nome da linha (2 a 60 caracteres).")
                    return@get
                }
                respondTransit(transit.searchBusLines(query))
            }

            get("/bus/lines/{code}/stops") {
                val code = call.parameters["code"]?.toIntOrNull()
                if (code == null) {
                    call.respondError(HttpStatusCode.BadRequest, "Código de linha inválido.")
                    return@get
                }
                respondTransit(transit.stopsForLine(code))
            }

            get("/bus/stops/{code}/arrivals") {
                val code = call.parameters["code"]?.toIntOrNull()
                if (code == null) {
                    call.respondError(HttpStatusCode.BadRequest, "Código de parada inválido.")
                    return@get
                }
                respondTransit(transit.arrivalsAtStop(code))
            }
        }
    }
}
