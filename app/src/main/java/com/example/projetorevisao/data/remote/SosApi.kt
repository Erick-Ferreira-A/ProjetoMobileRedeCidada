package com.example.projetorevisao.data.remote

import org.json.JSONArray
import org.json.JSONObject

/** Contato que recebe o SMS de emergência. */
data class Guardian(
    val id: String,
    val name: String,
    val relation: String,
    val phone: String
)

/** Guardiões e registro de alertas SOS. Todas as rotas exigem login. */
object SosApi {

    suspend fun guardians(): Pair<ApiResult, List<Guardian>> {
        val result = BackendHttp.get("/sos/guardians", authenticated = true)
        val list = (result as? ApiResult.Success)?.root?.optJSONArray("data")?.toGuardians().orEmpty()
        return result to list
    }

    suspend fun addGuardian(name: String, relation: String, phone: String): Pair<ApiResult, Guardian?> {
        val result = BackendHttp.post(
            "/sos/guardians",
            JSONObject().put("name", name.trim()).put("relation", relation.trim()).put("phone", phone.trim()),
            authenticated = true
        )
        return result to (result as? ApiResult.Success)?.data?.toGuardian()
    }

    suspend fun deleteGuardian(id: String): ApiResult =
        BackendHttp.delete("/sos/guardians/$id")

    /** Registra o alerta no servidor. Não aciona polícia: quem avisa os guardiões é o próprio aparelho, por SMS. */
    suspend fun registerAlert(neighborhood: String, latitude: Double?, longitude: Double?): ApiResult {
        val body = JSONObject()
            .put("neighborhood", neighborhood)
            .put("emergencyType", "GUARDIOES")
            .put("triggerSource", "BOTAO_SOS_APP")
        latitude?.let { body.put("latitude", it) }
        longitude?.let { body.put("longitude", it) }
        return BackendHttp.post("/sos/alert", body, authenticated = true)
    }

    private fun JSONArray.toGuardians(): List<Guardian> =
        (0 until length()).mapNotNull { optJSONObject(it)?.toGuardian() }

    private fun JSONObject.toGuardian() = Guardian(
        id = optString("id"),
        name = optString("name"),
        relation = optString("relation"),
        phone = optString("phone")
    )
}
