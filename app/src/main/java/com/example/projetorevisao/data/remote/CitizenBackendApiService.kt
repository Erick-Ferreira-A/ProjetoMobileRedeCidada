package com.example.projetorevisao.data.remote

import com.example.projetorevisao.data.local.IncidentEntity
import com.example.projetorevisao.data.session.SessionManager
import org.json.JSONObject
import java.net.URLEncoder

/** Rotas de incidentes do backend. A leitura é pública; o envio exige login. */
object CitizenBackendApiService {

    suspend fun isBackendOnline(): Boolean =
        BackendHttp.get("/integrations/health") is ApiResult.Success

    /** null = servidor fora do ar ou resposta inválida. */
    suspend fun fetchIncidentsFromServer(
        neighborhood: String? = null,
        category: String? = null
    ): List<IncidentEntity>? {
        val params = buildList {
            if (!neighborhood.isNullOrEmpty()) add("neighborhood=${URLEncoder.encode(neighborhood, "UTF-8")}")
            if (!category.isNullOrEmpty()) add("category=${URLEncoder.encode(category, "UTF-8")}")
        }
        val path = "/incidents" + if (params.isEmpty()) "" else params.joinToString("&", prefix = "?")
        // Com login, o servidor tira o que a pessoa denunciou/bloqueou e diz o que ela já confirmou.
        val result = BackendHttp.get(path, authenticated = SessionManager.current != null) as? ApiResult.Success ?: return null
        val array = result.root.optJSONArray("data") ?: return emptyList()
        return (0 until array.length()).mapNotNull { array.optJSONObject(it)?.toIncidentEntity(userConfirmed = false) }
    }

    /** Envia um relato novo. Em caso de sucesso, devolve o registro criado pelo servidor. */
    suspend fun postIncidentToServer(
        category: String,
        title: String,
        description: String,
        address: String,
        neighborhood: String,
        isAnonymous: Boolean,
        latitude: Double? = null,
        longitude: Double? = null
    ): Pair<ApiResult, IncidentEntity?> {
        val body = JSONObject()
            .put("category", category)
            .put("title", title)
            .put("description", description)
            .put("address", address)
            .put("neighborhood", neighborhood)
            .put("isAnonymous", isAnonymous)
        latitude?.let { body.put("latitude", it) }
        longitude?.let { body.put("longitude", it) }
        val result = BackendHttp.post("/incidents", body, authenticated = true)
        val created = (result as? ApiResult.Success)?.data?.toIncidentEntity(userConfirmed = false)
        return result to created
    }

    /** Envia um anexo. Devolve a linha "image|url" ou "video|url" em caso de sucesso. */
    suspend fun uploadMedia(incidentId: String, bytes: ByteArray, mimeType: String, fileName: String): Pair<ApiResult, String?> {
        val result = BackendHttp.upload("/incidents/$incidentId/media", bytes, mimeType, fileName)
        val line = (result as? ApiResult.Success)?.data?.toMediaLine()
        return result to line
    }

    private fun JSONObject.toMediaLine(): String? {
        val url = optString("url").takeIf { it.isNotBlank() } ?: return null
        val kind = if (optString("mimeType").startsWith("video/")) "video" else "image"
        return "$kind|${BackendHttp.absoluteUrl(url)}"
    }

    private fun JSONObject.toIncidentEntity(userConfirmed: Boolean) = IncidentEntity(
        id = optString("id"),
        category = optString("category"),
        customBadgeTitle = if (isNull("customBadgeTitle")) null else optString("customBadgeTitle"),
        title = optString("title"),
        description = optString("description"),
        address = optString("address"),
        neighborhood = optString("neighborhood"),
        distanceMeters = 0,
        timeAgo = optString("timeAgo", "Recente"),
        verified = optBoolean("verified", false),
        confirmations = optInt("confirmations", 0),
        commentsCount = optInt("commentsCount", 0),
        sharesCount = optInt("sharesCount", 0),
        status = optString("status", "REPORTADO"),
        authorityAssigned = optString("authorityAssigned", "Não é enviado à polícia. Em perigo, ligue 190."),
        isAnonymous = optBoolean("isAnonymous", true),
        mapXFraction = optDouble("mapXFraction", 0.5).toFloat(),
        mapYFraction = optDouble("mapYFraction", 0.5).toFloat(),
        latitude = optDouble("latitude", -23.6596),
        longitude = optDouble("longitude", -46.7681),
        aiSafetyRecommendation = optString("aiSafetyRecommendation", "Em perigo, ligue 190."),
        userConfirmed = userConfirmed || optBoolean("userConfirmed", false),
        media = optJSONArray("media")?.let { arr ->
            (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.toMediaLine() }.joinToString("\n")
        }.orEmpty(),
        createdAtEpochMs = optLong("createdAtEpochMs", 0L),
        fromServer = true
    )

    /** "Eu também vi". Devolve (resultado, total de confirmações, se a pessoa confirmou). */
    suspend fun toggleConfirmation(incidentId: String): Triple<ApiResult, Int?, Boolean?> {
        val result = BackendHttp.post("/incidents/$incidentId/confirm", authenticated = true)
        val data = (result as? ApiResult.Success)?.data
        return Triple(result, data?.optInt("confirmations"), data?.optBoolean("userConfirmed"))
    }

    suspend fun reportIncident(incidentId: String, reason: String, details: String): ApiResult =
        BackendHttp.post(
            "/incidents/$incidentId/report",
            JSONObject().put("reason", reason).put("details", details.take(500)),
            authenticated = true
        )

    suspend fun blockAuthor(incidentId: String): ApiResult =
        BackendHttp.post("/incidents/$incidentId/block-author", authenticated = true)
}
