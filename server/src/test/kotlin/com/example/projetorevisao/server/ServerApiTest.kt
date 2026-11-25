package com.example.projetorevisao.server

import com.example.projetorevisao.server.database.DatabaseFactory
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.client.statement.readRawBytes
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.util.UUID

class ServerApiTest {

    companion object {
        private const val DEMO_EMAIL = DatabaseFactory.DEMO_EMAIL
        private const val DEMO_PASSWORD = "senha123"
        private const val ADMIN_TOKEN = "token-de-moderacao-para-testes-123"

        @BeforeClass
        @JvmStatic
        fun setupDb() {
            // Postgres embutido e temporário: os testes não tocam em nenhum banco real.
            System.setProperty("DATABASE_URL", TestDatabase.jdbcUrl("api_test"))
            System.setProperty("JWT_SECRET", "test-secret-com-pelo-menos-32-caracteres-ok")
            System.setProperty("SEED_DEMO_DATA", "true")
            System.setProperty("MEDIA_DIR", java.nio.file.Files.createTempDirectory("rc_media").toString())
            System.setProperty("ADMIN_TOKEN", ADMIN_TOKEN)
            System.setProperty("TRUST_PROXY", "true")
            DatabaseFactory.init()
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    private fun field(body: String, name: String): String =
        json.parseToJsonElement(body).jsonObject["data"]!!.jsonObject[name]!!.jsonPrimitive.content

    private suspend fun HttpClient.postJson(path: String, body: String, token: String? = null): HttpResponse =
        post(path) {
            contentType(ContentType.Application.Json)
            setBody(body)
            token?.let { bearerAuth(it) }
        }

    private suspend fun HttpClient.login(email: String = DEMO_EMAIL, password: String = DEMO_PASSWORD) =
        postJson("/api/v1/auth/login", """{"email":"$email","password":"$password"}""")

    private fun uniqueEmail() = "teste-${UUID.randomUUID().toString().take(8)}@exemplo.com"

    @Test
    fun healthCheckIsPublic() = testApplication {
        application { module() }
        val response = client.get("/api/v1/integrations/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"status\": \"UP\""))
    }

    @Test
    fun loginWithCorrectPasswordReturnsTokens() = testApplication {
        application { module() }
        val response = client.login()
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(field(body, "accessToken").count { it == '.' } == 2) // formato JWT
        assertTrue(field(body, "refreshToken").isNotBlank())
    }

    @Test
    fun loginWithWrongPasswordIsRejected() = testApplication {
        application { module() }
        assertEquals(HttpStatusCode.Unauthorized, client.login(password = "qualquer-coisa").status)
        assertEquals(HttpStatusCode.Unauthorized, client.login(email = "naoexiste@exemplo.com").status)
    }

    @Test
    fun protectedRoutesRequireToken() = testApplication {
        application { module() }
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/auth/me").status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/sos/guardians").status)
        val forged = client.get("/api/v1/auth/me") { bearerAuth("rc_sp_usr-default_123_abc") }
        assertEquals(HttpStatusCode.Unauthorized, forged.status)
    }

    @Test
    fun meReturnsAuthenticatedUser() = testApplication {
        application { module() }
        val token = field(client.login().bodyAsText(), "accessToken")
        val response = client.get("/api/v1/auth/me") { bearerAuth(token) }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(DEMO_EMAIL, field(response.bodyAsText(), "email"))
    }

    @Test
    fun registerValidatesInputAndRejectsDuplicates() = testApplication {
        application { module() }
        val email = uniqueEmail()
        fun body(password: String = "senhaForte1", terms: Boolean = true) =
            """{"name":"Maria Teste","email":"$email","password":"$password","neighborhood":"Capão Redondo","acceptedTerms":$terms}"""

        assertEquals(HttpStatusCode.BadRequest, client.postJson("/api/v1/auth/register", body(terms = false)).status)
        assertEquals(HttpStatusCode.BadRequest, client.postJson("/api/v1/auth/register", body(password = "curta")).status)

        val created = client.postJson("/api/v1/auth/register", body())
        assertEquals(HttpStatusCode.Created, created.status)
        // Cadastro antigo com CPF continua aceito (campo é ignorado) e nada de CPF é guardado.
        val legacy = client.postJson(
            "/api/v1/auth/register",
            """{"name":"Legado","email":"${uniqueEmail()}","cpf":"529.982.247-25","password":"senhaForte1","neighborhood":"Centro","acceptedTerms":true}"""
        )
        assertEquals(HttpStatusCode.Created, legacy.status)
        assertFalse(legacy.bodyAsText().contains("529"))

        assertEquals(HttpStatusCode.Conflict, client.postJson("/api/v1/auth/register", body()).status)
        assertEquals(HttpStatusCode.OK, client.login(email = email.uppercase(), password = "senhaForte1").status)
    }

    @Test
    fun refreshRotatesTokenAndDetectsReuse() = testApplication {
        application { module() }
        val first = field(client.login().bodyAsText(), "refreshToken")

        val rotated = client.postJson("/api/v1/auth/refresh", """{"refreshToken":"$first"}""")
        assertEquals(HttpStatusCode.OK, rotated.status)
        val second = field(rotated.bodyAsText(), "refreshToken")
        assertNotEquals(first, second)

        // Reapresentar o token antigo revoga todas as sessões, inclusive a nova.
        assertEquals(HttpStatusCode.Unauthorized, client.postJson("/api/v1/auth/refresh", """{"refreshToken":"$first"}""").status)
        assertEquals(HttpStatusCode.Unauthorized, client.postJson("/api/v1/auth/refresh", """{"refreshToken":"$second"}""").status)
    }

    @Test
    fun logoutRevokesRefreshToken() = testApplication {
        application { module() }
        val refresh = field(client.login().bodyAsText(), "refreshToken")
        assertEquals(HttpStatusCode.OK, client.postJson("/api/v1/auth/logout", """{"refreshToken":"$refresh"}""").status)
        assertEquals(HttpStatusCode.Unauthorized, client.postJson("/api/v1/auth/refresh", """{"refreshToken":"$refresh"}""").status)
    }

    @Test
    fun loginIsRateLimited() = testApplication {
        application { module() }
        val statuses = (1..12).map { client.login(password = "errada").status }
        assertTrue(statuses.contains(HttpStatusCode.TooManyRequests))
    }

    @Test
    fun legacySha256PasswordIsMigratedOnLogin() = testApplication {
        application { module() }
        val email = uniqueEmail()
        val sha256OfSenha123 = "55a5e9e78207b4df8699d60886fa070079463547b095d1a05bc719bb4e6cd251"
        DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement(
                "INSERT INTO users (id, name, email, password_hash, neighborhood) VALUES (?, 'Legado', ?, ?, 'Centro')"
            ).use {
                it.setString(1, "usr-${UUID.randomUUID()}")
                it.setString(2, email)
                it.setString(3, sha256OfSenha123)
                it.executeUpdate()
            }
        }
        assertEquals(HttpStatusCode.OK, client.login(email = email, password = "senha123").status)
        val stored = DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement("SELECT password_hash FROM users WHERE email = ?").use {
                it.setString(1, email)
                it.executeQuery().run { next(); getString(1) }
            }
        }
        assertTrue(stored.startsWith("$2"))
    }

    @Test
    fun incidentsAreReadablePubliclyButWritingRequiresLogin() = testApplication {
        application { module() }
        assertEquals(HttpStatusCode.OK, client.get("/api/v1/incidents").status)

        val createJson = """
            {
                "category": "ROUBO_FURTO",
                "title": "Tentativa de furto na ciclovia",
                "description": "Suspeito de bicicleta tentando puxar celulares perto do semáforo.",
                "address": "Av. Paulista, 1000",
                "neighborhood": "Av. Paulista, Bela Vista - Centro",
                "isAnonymous": true
            }
        """.trimIndent()

        assertEquals(HttpStatusCode.Unauthorized, client.postJson("/api/v1/incidents", createJson).status)

        val token = field(client.login().bodyAsText(), "accessToken")
        val response = client.postJson("/api/v1/incidents", createJson, token)
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("Tentativa de furto na ciclovia"))
    }

    @Test
    fun malformedJsonReturnsGenericBadRequest() = testApplication {
        application { module() }
        val response = client.postJson("/api/v1/auth/login", """{"email": """)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertFalse(response.bodyAsText().contains("Exception"))
    }

    @Test
    fun telemetryWeatherEndpoint() = testApplication {
        application { module() }
        val response = client.get("/api/v1/telemetry/weather?neighborhood=Cap%C3%A3o+Redondo")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("temperatureCelsius"))
    }

    @Test
    fun mobilityMetroEndpoint() = testApplication {
        application { module() }
        val response = client.get("/api/v1/mobility/metro")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Linha 1 - Azul"))
    }

    private suspend fun HttpClient.registerNewUser(email: String = uniqueEmail(), password: String = "senhaForte1"): String {
        val response = postJson(
            "/api/v1/auth/register",
            """{"name":"Teste SOS","email":"$email","password":"$password","neighborhood":"Centro","acceptedTerms":true}"""
        )
        assertEquals(HttpStatusCode.Created, response.status)
        return field(response.bodyAsText(), "accessToken")
    }

    @Test
    fun guardiansAreScopedToTheirOwnerAndSosReturnsThem() = testApplication {
        application { module() }
        val alice = client.registerNewUser()
        val bob = client.registerNewUser()

        val added = client.postJson("/api/v1/sos/guardians", """{"name":"Mãe","relation":"Família","phone":"+55 11 91234-5678"}""", alice)
        assertEquals(HttpStatusCode.Created, added.status)
        val guardianId = field(added.bodyAsText(), "id")

        assertFalse(client.get("/api/v1/sos/guardians") { bearerAuth(bob) }.bodyAsText().contains("Mãe"))
        assertEquals(HttpStatusCode.NotFound, client.delete("/api/v1/sos/guardians/$guardianId") { bearerAuth(bob) }.status)

        val alert = client.postJson("/api/v1/sos/alert", """{"neighborhood":"Centro","latitude":-23.5,"longitude":-46.6}""", alice)
        assertEquals(HttpStatusCode.Created, alert.status)
        val body = alert.bodyAsText()
        assertTrue(body.contains("+55 11 91234-5678"))
        assertFalse(body.contains("COPOM"))

        assertEquals(HttpStatusCode.OK, client.delete("/api/v1/sos/guardians/$guardianId") { bearerAuth(alice) }.status)
    }

    @Test
    fun deleteAccountRequiresPasswordAndRemovesUser() = testApplication {
        application { module() }
        val email = uniqueEmail()
        val token = client.registerNewUser(email)

        assertEquals(HttpStatusCode.Forbidden, client.postJson("/api/v1/auth/delete-account", """{"password":"errada123"}""", token).status)
        assertEquals(HttpStatusCode.OK, client.postJson("/api/v1/auth/delete-account", """{"password":"senhaForte1"}""", token).status)

        assertEquals(HttpStatusCode.Unauthorized, client.login(email = email, password = "senhaForte1").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/auth/me") { bearerAuth(token) }.status)
    }

    @Test
    fun legalPagesArePublicHtml() = testApplication {
        application { module() }
        for ((path, text) in listOf(
            "/privacidade" to "Política de privacidade",
            "/termos" to "não substitui os serviços de emergência",
            "/excluir-conta" to "Excluir conta do Rede Cidadã"
        )) {
            val response = client.get(path)
            assertEquals(path, HttpStatusCode.OK, response.status)
            assertTrue(path, response.headers[HttpHeaders.ContentType]!!.startsWith("text/html"))
            val body = response.bodyAsText()
            assertTrue(path, body.contains(text))
            assertFalse(path, body.contains("{{FORM}}"))
        }
    }

    @Test
    fun deletionPageDeletesAccountWithEmailAndPassword() = testApplication {
        application { module() }
        val email = uniqueEmail()
        client.registerNewUser(email)
        suspend fun submit(password: String) = client.post("/excluir-conta") {
            header("X-Forwarded-For", "203.0.113.77")
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("email=${email.uppercase()}&password=$password")
        }.bodyAsText()

        assertTrue(submit("errada123").contains("E-mail ou senha incorretos"))
        assertEquals(HttpStatusCode.OK, client.login(email = email, password = "senhaForte1").status)

        assertTrue(submit("senhaForte1").contains("Conta excluída"))
        assertEquals(HttpStatusCode.Unauthorized, client.login(email = email, password = "senhaForte1").status)
    }

    private suspend fun HttpClient.uploadMedia(incidentId: String, token: String, bytes: ByteArray, mime: String) =
        submitFormWithBinaryData(
            url = "/api/v1/incidents/$incidentId/media",
            formData = formData {
                append("file", bytes, Headers.build {
                    append(HttpHeaders.ContentType, mime)
                    append(HttpHeaders.ContentDisposition, "filename=\"anexo\"")
                })
            }
        ) { bearerAuth(token) }

    @Test
    fun reporterCanAttachPhotoAndOthersCannot() = testApplication {
        application { module() }
        val owner = client.registerNewUser()
        val other = client.registerNewUser()
        val created = client.postJson(
            "/api/v1/incidents",
            """{"category":"ALAGAMENTO","title":"Rua alagada","description":"Água na calçada","address":"Rua A, 10","neighborhood":"Centro","latitude":-23.55,"longitude":-46.63}""",
            owner
        )
        assertEquals(HttpStatusCode.Created, created.status)
        val incidentId = field(created.bodyAsText(), "id")
        assertEquals("-23.55", field(created.bodyAsText(), "latitude"))

        val jpeg = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()) + ByteArray(200) { 7 }
        val upload = client.uploadMedia(incidentId, owner, jpeg, "image/jpeg")
        assertEquals(HttpStatusCode.Created, upload.status)
        val mediaUrl = field(upload.bodyAsText(), "url")

        val served = client.get(mediaUrl)
        assertEquals(HttpStatusCode.OK, served.status)
        assertArrayEquals(jpeg, served.readRawBytes())
        // Download em partes (Range): o player do celular precisa disso para vídeos com o índice no fim.
        val partial = client.get(mediaUrl) { header(HttpHeaders.Range, "bytes=0-3") }
        assertEquals(HttpStatusCode.PartialContent, partial.status)
        assertArrayEquals(jpeg.copyOfRange(0, 4), partial.readRawBytes())
        assertTrue(client.get("/api/v1/incidents").bodyAsText().contains(mediaUrl))

        assertEquals(HttpStatusCode.Forbidden, client.uploadMedia(incidentId, other, jpeg, "image/jpeg").status)
        val fake = "isto nao e uma imagem de verdade".toByteArray()
        assertEquals(HttpStatusCode.BadRequest, client.uploadMedia(incidentId, owner, fake, "image/jpeg").status)
        assertEquals(HttpStatusCode.BadRequest, client.uploadMedia(incidentId, owner, jpeg, "application/pdf").status)
    }

    @Test
    fun deletedAccountLeavesIncidentAndCommentsAnonymous() = testApplication {
        application { module() }
        val email = uniqueEmail()
        val author = client.registerNewUser(email)
        val incidentId = client.createIncident(author, "Relato que fica")
        assertEquals(HttpStatusCode.Created, client.postJson("/api/v1/incidents/$incidentId/comments", """{"content":"Com nome 🚨 cuidado","isAnonymous":false}""", author).status)
        assertEquals(HttpStatusCode.Created, client.postJson("/api/v1/incidents/$incidentId/comments", """{"content":"Sem nome","isAnonymous":true}""", author).status)
        assertTrue(client.get("/api/v1/incidents/$incidentId/comments").bodyAsText().contains("Teste SOS"))
        assertTrue(Regex(""""commentsCount"\s*:\s*2""").containsMatchIn(client.feed()))

        assertEquals(HttpStatusCode.OK, client.postJson("/api/v1/auth/delete-account", """{"password":"senhaForte1"}""", author).status)

        // O relato e os comentários continuam, sem ligação com a pessoa.
        assertTrue(client.feed().contains("Relato que fica"))
        val comments = client.get("/api/v1/incidents/$incidentId/comments").bodyAsText()
        assertFalse(comments.contains("Teste SOS"))
        assertTrue(comments.contains("Cidadão Colaborador"))
        assertTrue(comments.contains("Cidadão Anônimo"))
        assertTrue(comments.contains("🚨")) // UTF-8 de ponta a ponta
    }

    private suspend fun HttpClient.createIncident(token: String, title: String = "Relato ${UUID.randomUUID()}"): String {
        val created = postJson(
            "/api/v1/incidents",
            """{"category":"TRANSITO","title":"$title","description":"desc","address":"Rua B, 20","neighborhood":"Centro"}""",
            token
        )
        assertEquals(HttpStatusCode.Created, created.status)
        return field(created.bodyAsText(), "id")
    }

    private suspend fun HttpClient.feed(token: String? = null): String =
        get("/api/v1/incidents") { token?.let { bearerAuth(it) } }.bodyAsText()

    @Test
    fun confirmationIsOnePerPersonAndToggles() = testApplication {
        application { module() }
        val author = client.registerNewUser()
        val a = client.registerNewUser()
        val b = client.registerNewUser()
        val id = client.createIncident(author)

        assertEquals(HttpStatusCode.Unauthorized, client.postJson("/api/v1/incidents/$id/confirm", "").status)
        val first = client.postJson("/api/v1/incidents/$id/confirm", "", a).bodyAsText()
        assertEquals("1", field(first, "confirmations"))
        assertEquals("true", field(first, "userConfirmed"))
        assertEquals("2", field(client.postJson("/api/v1/incidents/$id/confirm", "", b).bodyAsText(), "confirmations"))
        // A mesma pessoa de novo: retira a confirmação dela, não soma.
        val again = client.postJson("/api/v1/incidents/$id/confirm", "", a).bodyAsText()
        assertEquals("1", field(again, "confirmations"))
        assertEquals("false", field(again, "userConfirmed"))
    }

    @Test
    fun reportHidesForReporterAndThreeReportsHideForEveryone() = testApplication {
        application { module() }
        val author = client.registerNewUser()
        val title = "Denunciavel ${UUID.randomUUID()}"
        val id = client.createIncident(author, title)
        val r1 = client.registerNewUser()
        val r2 = client.registerNewUser()
        val r3 = client.registerNewUser()
        fun report(reason: String = "FALSO") = """{"reason":"$reason","details":"não aconteceu"}"""

        assertEquals(HttpStatusCode.BadRequest, client.postJson("/api/v1/incidents/$id/report", report("QUALQUER"), r1).status)
        assertEquals(HttpStatusCode.BadRequest, client.postJson("/api/v1/incidents/$id/report", report(), author).status)

        assertEquals(HttpStatusCode.OK, client.postJson("/api/v1/incidents/$id/report", report(), r1).status)
        assertFalse(client.feed(r1).contains(title)) // some para quem denunciou
        assertTrue(client.feed(r2).contains(title))  // os outros ainda veem
        assertTrue(client.feed().contains(title))

        client.postJson("/api/v1/incidents/$id/report", report("OFENSIVO"), r2)
        client.postJson("/api/v1/incidents/$id/report", report(), r3)
        assertFalse(client.feed().contains(title)) // 3 denúncias: oculto para todos

        // Moderação: sem token não existe; com token lista e decide.
        assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/admin/reports").status)
        val pending = client.get("/api/v1/admin/reports") { header("X-Admin-Token", ADMIN_TOKEN) }.bodyAsText()
        assertTrue(pending.contains(title))
        assertTrue(pending.contains("OFENSIVO"))
        assertEquals(HttpStatusCode.OK, client.post("/api/v1/admin/incidents/$id/keep") { header("X-Admin-Token", ADMIN_TOKEN) }.status)
        assertTrue(client.feed().contains(title))
        assertFalse(client.get("/api/v1/admin/reports") { header("X-Admin-Token", ADMIN_TOKEN) }.bodyAsText().contains(title))
    }

    @Test
    fun moderatorCanRemoveIncidentAndItsFiles() = testApplication {
        application { module() }
        val author = client.registerNewUser()
        val title = "Remover ${UUID.randomUUID()}"
        val id = client.createIncident(author, title)
        val jpeg = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()) + ByteArray(100) { 1 }
        val mediaUrl = field(client.uploadMedia(id, author, jpeg, "image/jpeg").bodyAsText(), "url")

        assertEquals(HttpStatusCode.NotFound, client.post("/api/v1/admin/incidents/$id/remove") { header("X-Admin-Token", "errado") }.status)
        assertEquals(HttpStatusCode.OK, client.post("/api/v1/admin/incidents/$id/remove") { header("X-Admin-Token", ADMIN_TOKEN) }.status)
        assertFalse(client.feed().contains(title))
        assertEquals(HttpStatusCode.NotFound, client.get(mediaUrl).status)
    }

    @Test
    fun blockingAuthorHidesAllTheirIncidents() = testApplication {
        application { module() }
        val author = client.registerNewUser()
        val viewer = client.registerNewUser()
        val t1 = "Bloq1 ${UUID.randomUUID()}"
        val t2 = "Bloq2 ${UUID.randomUUID()}"
        val id = client.createIncident(author, t1)
        client.createIncident(author, t2)

        assertEquals(HttpStatusCode.BadRequest, client.postJson("/api/v1/incidents/$id/block-author", "", author).status)
        assertEquals(HttpStatusCode.OK, client.postJson("/api/v1/incidents/$id/block-author", "", viewer).status)
        val feed = client.feed(viewer)
        assertFalse(feed.contains(t1))
        assertFalse(feed.contains(t2))
        assertTrue(client.feed().contains(t2))
    }

    @Test
    fun behindProxyEachClientHasItsOwnLoginLimit() = testApplication {
        application { module() }
        suspend fun attempt(ip: String) = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            // O proxy acrescenta o IP real no fim; o começo da lista é inventado pelo cliente e não vale.
            header("X-Forwarded-For", "1.2.3.4, $ip")
            setBody("""{"email":"ninguem@exemplo.com","password":"errada123"}""")
        }
        repeat(10) { attempt("10.0.0.1") }
        assertEquals(HttpStatusCode.TooManyRequests, attempt("10.0.0.1").status)
        assertEquals(HttpStatusCode.Unauthorized, attempt("10.0.0.2").status) // outra pessoa não é bloqueada
    }

    @Test
    fun transitRoutesExplainWhenSourcesAreNotConfigured() = testApplication {
        val noKeys = com.example.projetorevisao.server.transit.TransitService(
            io.ktor.client.engine.mock.MockEngine { error("sem rede") }, sptransToken = null, artespKey = null
        )
        application { module(noKeys) }
        val rail = client.get("/api/v1/transit/rail/status")
        assertEquals(HttpStatusCode.ServiceUnavailable, rail.status)
        assertTrue(rail.bodyAsText().contains("não configurado"))
        assertEquals(HttpStatusCode.BadRequest, client.get("/api/v1/transit/bus/lines?q=8").status)
        assertEquals(HttpStatusCode.ServiceUnavailable, client.get("/api/v1/transit/bus/lines?q=8000").status)
    }
}
