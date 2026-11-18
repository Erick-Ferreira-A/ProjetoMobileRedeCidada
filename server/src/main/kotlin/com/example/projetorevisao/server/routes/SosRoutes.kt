package com.example.projetorevisao.server.routes

import com.example.projetorevisao.server.models.*
import com.example.projetorevisao.server.repository.ServerRepository
import com.example.projetorevisao.server.security.JwtConfig
import com.example.projetorevisao.server.security.respondError
import com.example.projetorevisao.server.security.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

private val phoneRegex = Regex("^\\+?[0-9 ()-]{8,20}$")

fun Route.sosRoutes(repository: ServerRepository) {
    authenticate(JwtConfig.AUTH_NAME) {
        route("/api/v1/sos") {

            post("/alert") {
                val req = call.receive<SosAlertRequest>()
                if (req.neighborhood.isBlank() || req.neighborhood.length > 120) {
                    call.respondError(HttpStatusCode.BadRequest, "Informe o bairro do alerta.")
                    return@post
                }
                val response = repository.triggerSosAlert(call.userId(), req)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, message = response.status, data = response))
            }

            get("/guardians") {
                val list = repository.getGuardians(call.userId())
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = list))
            }

            post("/guardians") {
                val req = call.receive<AddGuardianRequest>()
                val error = when {
                    req.name.isBlank() || req.name.length > 100 -> "Informe o nome do guardião."
                    req.relation.length > 50 -> "A relação pode ter no máximo 50 caracteres."
                    !phoneRegex.matches(req.phone.trim()) -> "Telefone inválido."
                    else -> null
                }
                if (error != null) {
                    call.respondError(HttpStatusCode.BadRequest, error)
                    return@post
                }
                val created = repository.addGuardian(call.userId(), req)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, message = "Guardião cadastrado com sucesso", data = created))
            }

            delete("/guardians/{id}") {
                if (repository.deleteGuardian(call.userId(), call.parameters["id"]!!)) {
                    call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Guardião removido."))
                } else {
                    call.respondError(HttpStatusCode.NotFound, "Guardião não encontrado.")
                }
            }
        }
    }
}
