package com.example.projetorevisao.server.routes

import com.example.projetorevisao.server.models.*
import com.example.projetorevisao.server.repository.CommunityActionResult
import com.example.projetorevisao.server.repository.MediaRepository
import com.example.projetorevisao.server.repository.ModerationRepository
import com.example.projetorevisao.server.repository.ServerRepository
import com.example.projetorevisao.server.repository.UploadResult
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respondFile
import io.ktor.utils.io.jvm.javaio.toInputStream
import com.example.projetorevisao.server.security.JwtConfig
import com.example.projetorevisao.server.security.respondError
import com.example.projetorevisao.server.security.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

private fun validateIncident(req: CreateIncidentRequest): String? = when {
    req.category.isBlank() || req.category.length > 40 -> "Categoria inválida."
    req.title.isBlank() || req.title.length > 120 -> "Informe um título de até 120 caracteres."
    req.description.length > 2000 -> "A descrição pode ter no máximo 2000 caracteres."
    req.address.isBlank() || req.address.length > 200 -> "Informe um endereço de até 200 caracteres."
    req.neighborhood.isBlank() || req.neighborhood.length > 120 -> "Informe o bairro."
    req.latitude != null && req.latitude !in -90.0..90.0 -> "Latitude inválida."
    req.longitude != null && req.longitude !in -180.0..180.0 -> "Longitude inválida."
    else -> null
}

private suspend fun io.ktor.server.application.ApplicationCall.respondCommunityAction(
    result: CommunityActionResult,
    doneMessage: String,
    ownMessage: String
) = when (result) {
    CommunityActionResult.DONE -> respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = doneMessage))
    CommunityActionResult.NOT_FOUND -> respondError(HttpStatusCode.NotFound, "Relato não encontrado.")
    CommunityActionResult.OWN_CONTENT -> respondError(HttpStatusCode.BadRequest, ownMessage)
}

fun Route.incidentRoutes(
    repository: ServerRepository,
    mediaRepository: MediaRepository,
    moderationRepository: ModerationRepository = ModerationRepository(mediaRepository)
) {
    // Arquivos públicos dos relatos (o id é aleatório e vem do banco).
    get("/api/v1/media/{id}") {
        val media = mediaRepository.find(call.parameters["id"]!!)
        if (media == null) {
            call.respondError(HttpStatusCode.NotFound, "Arquivo não encontrado.")
            return@get
        }
        call.response.header("X-Content-Type-Options", "nosniff")
        call.response.header(HttpHeaders.CacheControl, "public, max-age=86400")
        call.response.header(HttpHeaders.ContentType, ContentType.parse(media.mimeType).toString())
        call.respondFile(media.file)
    }

    route("/api/v1/incidents") {

        // Leitura pública: o feed de alertas é aberto. Com login, tira o que a pessoa denunciou ou bloqueou.
        authenticate(JwtConfig.AUTH_NAME, optional = true) {
            get {
                val neighborhood = call.request.queryParameters["neighborhood"]
                val category = call.request.queryParameters["category"]
                val query = call.request.queryParameters["query"]
                val viewer = call.principal<JWTPrincipal>()?.subject

                val list = repository.getIncidents(neighborhood, category, query, viewer)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = list))
            }

            get("/{id}") {
                val id = call.parameters["id"]!!
                val viewer = call.principal<JWTPrincipal>()?.subject
                val item = repository.getIncidents(null, null, null, viewer).firstOrNull { it.id == id }
                if (item != null) {
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = item))
                } else {
                    call.respondError(HttpStatusCode.NotFound, "Incidente não encontrado.")
                }
            }
        }

        get("/{id}/comments") {
            val comments = repository.getCommentsForIncident(call.parameters["id"]!!)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = comments))
        }

        // Escrita exige login (o relato pode continuar anônimo para os outros usuários).
        authenticate(JwtConfig.AUTH_NAME) {
            post {
                val req = call.receive<CreateIncidentRequest>()
                val error = validateIncident(req)
                if (error != null) {
                    call.respondError(HttpStatusCode.BadRequest, error)
                    return@post
                }
                val created = repository.createIncident(call.userId(), req)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, message = "Relato registrado no servidor Rede Cidadã SP", data = created))
            }

            // Envio de foto ou vídeo: multipart com um campo de arquivo "file".
            post("/{id}/media") {
                val incidentId = call.parameters["id"]!!
                var result: UploadResult? = null
                call.receiveMultipart(formFieldLimit = 60L * 1024 * 1024).forEachPart { part ->
                    if (part is PartData.FileItem && result == null) {
                        result = part.provider().toInputStream().use { input ->
                            mediaRepository.save(call.userId(), incidentId, part.contentType?.toString(), input)
                        }
                    }
                    part.dispose()
                }
                when (val r = result) {
                    is UploadResult.Success -> call.respond(HttpStatusCode.Created, ApiResponse(success = true, message = "Arquivo anexado.", data = r.media))
                    is UploadResult.Rejected -> call.respondError(
                        when {
                            r.notFound -> HttpStatusCode.NotFound
                            r.forbidden -> HttpStatusCode.Forbidden
                            else -> HttpStatusCode.BadRequest
                        },
                        r.message
                    )
                    null -> call.respondError(HttpStatusCode.BadRequest, "Envie o arquivo no campo \"file\".")
                }
            }

            // "Eu também vi": cada chamada liga ou desliga a confirmação desta pessoa.
            post("/{id}/confirm") {
                val result = repository.toggleConfirmation(call.userId(), call.parameters["id"]!!)
                if (result != null) {
                    val message = if (result.userConfirmed) "Confirmação registrada." else "Confirmação retirada."
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, message = message, data = result))
                } else {
                    call.respondError(HttpStatusCode.NotFound, "Relato não encontrado.")
                }
            }

            post("/{id}/report") {
                val req = call.receive<ReportIncidentRequest>()
                if (req.reason !in ModerationRepository.REASONS || req.details.length > 500) {
                    call.respondError(HttpStatusCode.BadRequest, "Escolha um motivo válido para a denúncia.")
                    return@post
                }
                call.respondCommunityAction(
                    moderationRepository.reportIncident(call.userId(), call.parameters["id"]!!, req.reason, req.details),
                    doneMessage = "Denúncia recebida. O relato foi ocultado para você e será analisado.",
                    ownMessage = "Você não pode denunciar o próprio relato."
                )
            }

            post("/{id}/block-author") {
                call.respondCommunityAction(
                    moderationRepository.blockAuthorOf(call.userId(), call.parameters["id"]!!),
                    doneMessage = "Pronto. Você não verá mais relatos desta pessoa.",
                    ownMessage = "Este relato é seu."
                )
            }

            post("/{id}/comments") {
                val req = call.receive<CreateCommentRequest>()
                if (req.content.isBlank() || req.content.length > 1000) {
                    call.respondError(HttpStatusCode.BadRequest, "O comentário precisa ter entre 1 e 1000 caracteres.")
                    return@post
                }
                val comment = repository.addComment(call.userId(), call.parameters["id"]!!, req)
                if (comment != null) {
                    call.respond(HttpStatusCode.Created, ApiResponse(success = true, message = "Comentário adicionado", data = comment))
                } else {
                    call.respondError(HttpStatusCode.NotFound, "Incidente não encontrado.")
                }
            }
        }
    }
}
