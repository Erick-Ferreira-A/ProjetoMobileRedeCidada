package com.example.projetorevisao.data.remote

import com.example.projetorevisao.BuildConfig
import com.example.projetorevisao.data.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Resultado de uma chamada ao backend. [Success.root] é o envelope inteiro (`success`, `message`, `data`). */
sealed interface ApiResult {
    data class Success(val root: JSONObject) : ApiResult {
        val data: JSONObject? get() = root.optJSONObject("data")
        val message: String? get() = root.optString("message").takeIf { it.isNotBlank() && it != "null" }
    }

    data class Failure(val message: String, val status: Int? = null) : ApiResult {
        val isUnauthorized: Boolean get() = status == 401
    }
}

/**
 * Cliente HTTP do backend Rede Cidadã. Coloca o access token nas rotas protegidas e, se ele vencer,
 * renova uma vez com o refresh token. Se a renovação for recusada, encerra a sessão local.
 */
object BackendHttp {
    const val MSG_OFFLINE = "Sem conexão com o servidor. Verifique a internet e tente de novo."
    const val MSG_LOGIN_REQUIRED = "Entre na sua conta para usar este recurso."
    const val MSG_SESSION_EXPIRED = "Sua sessão expirou. Entre novamente."

    val baseUrl: String = BuildConfig.BACKEND_BASE_URL.trimEnd('/')

    /** Raiz do servidor (sem /api/v1), onde ficam as páginas de privacidade, termos e exclusão de conta. */
    val siteUrl: String = baseUrl.removeSuffix("/api/v1")

    private val client = HttpClient(Android) {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000 // uploads de vídeo podem demorar
            connectTimeoutMillis = 6_000
            socketTimeoutMillis = 10_000
        }
    }

    private val refreshMutex = Mutex()

    private enum class RefreshOutcome { REFRESHED, REJECTED, OFFLINE }

    suspend fun get(path: String, authenticated: Boolean = false) = request(HttpMethod.Get, path, null, authenticated)
    suspend fun post(path: String, body: JSONObject? = null, authenticated: Boolean = false) = request(HttpMethod.Post, path, body, authenticated)
    suspend fun delete(path: String, authenticated: Boolean = true) = request(HttpMethod.Delete, path, null, authenticated)

    suspend fun request(
        method: HttpMethod,
        path: String,
        body: JSONObject? = null,
        authenticated: Boolean = false
    ): ApiResult = execute(authenticated) { token -> send(method, path, body, token) }

    /** Envia um arquivo (multipart, campo "file") para uma rota protegida. */
    suspend fun upload(path: String, bytes: ByteArray, mimeType: String, fileName: String): ApiResult =
        execute(authenticated = true) { token ->
            client.submitFormWithBinaryData(
                url = "$baseUrl$path",
                formData = formData {
                    append("file", bytes, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            ) {
                token?.let { bearerAuth(it) }
            }
        }

    /** Baixa um arquivo público do servidor (ex.: foto de um relato). Null se falhar. */
    suspend fun downloadBytes(absoluteUrl: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val response = client.request(absoluteUrl) { method = HttpMethod.Get }
            if (response.status.value in 200..299) response.readRawBytes() else null
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
    }

    /** Converte "/api/v1/media/x" em endereço completo do servidor. */
    fun absoluteUrl(serverPath: String): String =
        if (serverPath.startsWith("http")) serverPath else baseUrl.substringBefore("/api/") + serverPath

    private suspend fun execute(
        authenticated: Boolean,
        sender: suspend (token: String?) -> HttpResponse
    ): ApiResult = withContext(Dispatchers.IO) {
        try {
            var token: String? = null
            if (authenticated) {
                token = SessionManager.current?.accessToken
                    ?: return@withContext ApiResult.Failure(MSG_LOGIN_REQUIRED, 401)
            }
            var response = sender(token)
            if (authenticated && response.status.value == 401) {
                when (refreshTokens(usedAccessToken = token!!)) {
                    RefreshOutcome.REFRESHED -> response = sender(SessionManager.current?.accessToken)
                    RefreshOutcome.REJECTED -> {
                        SessionManager.clear()
                        return@withContext ApiResult.Failure(MSG_SESSION_EXPIRED, 401)
                    }
                    RefreshOutcome.OFFLINE -> return@withContext ApiResult.Failure(MSG_OFFLINE)
                }
            }
            parse(response)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            ApiResult.Failure(MSG_OFFLINE)
        }
    }

    private suspend fun send(method: HttpMethod, path: String, body: JSONObject?, token: String?): HttpResponse =
        client.request("$baseUrl$path") {
            this.method = method
            token?.let { bearerAuth(it) }
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body.toString())
            }
        }

    /** Só uma renovação por vez; quem chegar depois reaproveita o token novo. */
    private suspend fun refreshTokens(usedAccessToken: String): RefreshOutcome = refreshMutex.withLock {
        val session = SessionManager.current ?: return RefreshOutcome.REJECTED
        if (session.accessToken != usedAccessToken) return RefreshOutcome.REFRESHED
        return try {
            val response = send(
                HttpMethod.Post,
                "/auth/refresh",
                JSONObject().put("refreshToken", session.refreshToken),
                token = null
            )
            when (val result = parse(response)) {
                is ApiResult.Success -> {
                    result.data?.let { SessionManager.saveFromAuthJson(it) } ?: return RefreshOutcome.REJECTED
                    RefreshOutcome.REFRESHED
                }
                is ApiResult.Failure -> if (result.status == 401) RefreshOutcome.REJECTED else RefreshOutcome.OFFLINE
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            RefreshOutcome.OFFLINE
        }
    }

    private suspend fun parse(response: HttpResponse): ApiResult {
        val status = response.status.value
        val root = runCatching { JSONObject(response.bodyAsText()) }.getOrNull()
            ?: return ApiResult.Failure(if (status in 200..299) "Resposta inválida do servidor." else MSG_OFFLINE, status)
        return if (status in 200..299 && root.optBoolean("success", false)) {
            ApiResult.Success(root)
        } else {
            val message = root.optString("message").takeIf { it.isNotBlank() && it != "null" }
                ?: "Não foi possível concluir a operação."
            ApiResult.Failure(message, status)
        }
    }
}
