package com.example.projetorevisao.data.remote

import com.example.projetorevisao.data.session.SessionManager
import org.json.JSONObject

/** Login, cadastro, saída e exclusão de conta. Em caso de sucesso a sessão fica salva no [SessionManager]. */
object AuthApi {

    suspend fun login(email: String, password: String): ApiResult =
        saveSession(
            BackendHttp.post(
                "/auth/login",
                JSONObject().put("email", email.trim()).put("password", password)
            )
        )

    suspend fun register(name: String, email: String, password: String, neighborhood: String, acceptedTerms: Boolean): ApiResult =
        saveSession(
            BackendHttp.post(
                "/auth/register",
                JSONObject()
                    .put("name", name.trim())
                    .put("email", email.trim())
                    .put("password", password)
                    .put("neighborhood", neighborhood.trim())
                    .put("acceptedTerms", acceptedTerms)
            )
        )

    /** Avisa o servidor (melhor esforço) e sempre limpa a sessão local. */
    suspend fun logout() {
        val refresh = SessionManager.current?.refreshToken
        if (refresh != null) BackendHttp.post("/auth/logout", JSONObject().put("refreshToken", refresh))
        SessionManager.clear()
    }

    suspend fun deleteAccount(password: String): ApiResult {
        val result = BackendHttp.post(
            "/auth/delete-account",
            JSONObject().put("password", password),
            authenticated = true
        )
        if (result is ApiResult.Success) SessionManager.clear()
        return result
    }

    private fun saveSession(result: ApiResult): ApiResult {
        if (result is ApiResult.Success) {
            val data = result.data ?: return ApiResult.Failure("Resposta inválida do servidor.")
            SessionManager.saveFromAuthJson(data)
        }
        return result
    }
}
