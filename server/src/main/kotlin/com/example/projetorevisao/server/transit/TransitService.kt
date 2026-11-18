package com.example.projetorevisao.server.transit

import com.example.projetorevisao.server.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

// --- Respostas para o app (nomes claros, não os nomes curtos da SPTrans) ---

@Serializable
data class BusLineDto(
    val code: Int,            // cl: código da linha (um por sentido)
    val sign: String,         // letreiro completo, ex. "8000-10"
    val direction: Int,       // 1 = terminal principal → secundário, 2 = volta
    val mainTerminal: String,
    val secondaryTerminal: String,
    val circular: Boolean
) {
    /** Destino do sentido escolhido. */
    val destination: String get() = if (direction == 1) secondaryTerminal else mainTerminal
}

@Serializable
data class BusStopDto(val code: Int, val name: String, val address: String, val latitude: Double, val longitude: Double)

@Serializable
data class BusVehicleDto(val prefix: String, val arrivalTime: String?, val accessible: Boolean, val latitude: Double?, val longitude: Double?)

@Serializable
data class BusArrivalLineDto(val sign: String, val lineCode: Int, val direction: Int, val destination: String, val origin: String, val vehicles: List<BusVehicleDto>)

@Serializable
data class BusArrivalsDto(val stopCode: Int, val stopName: String, val referenceTime: String, val lines: List<BusArrivalLineDto>)

@Serializable
data class RailLineStatusDto(
    val operator: String,
    val line: String,
    val code: String,
    val situation: String,
    val description: String,
    /** operacional | problema | ignorar (fora do horário comercial) */
    val classification: String,
    val normal: Boolean,
    val updatedAt: String?,
    val verifiedAt: String?
)

@Serializable
data class RailStatusDto(
    val lines: List<RailLineStatusDto>,
    val fetchedAtEpochMs: Long,
    val source: String,
    val coverageNote: String
)

/** Resultado de uma consulta a fonte externa. */
sealed interface TransitResult<out T> {
    data class Ok<T>(val data: T) : TransitResult<T>
    data class NotConfigured(val message: String) : TransitResult<Nothing>
    data class Unavailable(val message: String) : TransitResult<Nothing>
}

/**
 * Integra as fontes públicas de transporte de SP. As chaves ficam só no servidor e as respostas
 * são guardadas em cache para respeitar os limites das fontes:
 * - Olho Vivo (SPTrans): ônibus municipais. Login por token, a sessão é mantida por cookie.
 * - API Trilhos (ARTESP): status das linhas concedidas. Limite de 12 consultas/hora por chave.
 */
class TransitService(
    engine: HttpClientEngine = CIO.create(),
    private val sptransToken: String? = AppConfig.sptransToken,
    private val artespKey: String? = AppConfig.artespApiKey,
    private val now: () -> Long = System::currentTimeMillis
) {
    private val log = LoggerFactory.getLogger(TransitService::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient(engine) {
        expectSuccess = false
        install(HttpCookies)
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 5_000
        }
    }

    companion object {
        const val SPTRANS_BASE = "https://api.olhovivo.sptrans.com.br/v2.1"
        const val ARTESP_STATUS_URL = "https://ccm.artesp.sp.gov.br/metroferroviario/api/status/"

        private const val RAIL_TTL_MS = 5 * 60 * 1000L        // 12/h = 1 a cada 5 min
        private const val ARRIVALS_TTL_MS = 20 * 1000L
        private const val CATALOG_TTL_MS = 60 * 60 * 1000L     // linhas e paradas mudam pouco

        const val RAIL_COVERAGE =
            "Fonte: ARTESP (API Trilhos). Cobre as linhas concedidas fiscalizadas pela ARTESP. " +
                "As linhas operadas pelo Metrô (1, 2, 3 e 15) e pela CPTM ainda não aparecem aqui."
    }

    // ---------------- Cache simples com validade ----------------

    private class Entry<T>(val value: T, val expiresAt: Long)
    private val cache = ConcurrentHashMap<String, Entry<Any>>()

    @Suppress("UNCHECKED_CAST")
    private fun <T> cached(key: String): T? = cache[key]?.takeIf { it.expiresAt > now() }?.value as T?

    private fun put(key: String, value: Any, ttl: Long) {
        cache[key] = Entry(value, now() + ttl)
    }

    // ---------------- Olho Vivo (SPTrans) ----------------

    private val loginMutex = Mutex()
    @Volatile private var sptransLoggedIn = false
    /** Momento do último login recusado (token inválido/inativo), para não insistir a cada consulta. */
    @Volatile private var sptransRejectedAt = 0L

    private suspend fun sptransLogin(): Boolean = loginMutex.withLock {
        if (sptransLoggedIn) return true
        if (now() - sptransRejectedAt < 60_000) return false
        val response = client.post("$SPTRANS_BASE/Login/Autenticar") { parameter("token", sptransToken) }
        sptransLoggedIn = response.status.value in 200..299 && response.bodyAsText().trim() == "true"
        if (!sptransLoggedIn) sptransRejectedAt = now()
        if (!sptransLoggedIn) log.warn("Login na API Olho Vivo recusado (status {}). Confira o SPTRANS_TOKEN.", response.status.value)
        sptransLoggedIn
    }

    /** GET autenticado na SPTrans; refaz o login uma vez se a sessão tiver expirado. */
    private suspend fun sptransGet(path: String, params: Map<String, Any>): JsonElement? {
        if (!sptransLogin()) return null
        suspend fun call(): HttpResponse = client.get("$SPTRANS_BASE$path") { params.forEach { (k, v) -> parameter(k, v) } }
        var response = call()
        if (response.status.value == 401) {
            sptransLoggedIn = false
            if (!sptransLogin()) return null
            response = call()
        }
        if (response.status.value !in 200..299) return null
        return json.parseToJsonElement(response.bodyAsText())
    }

    private suspend fun <T> sptrans(cacheKey: String, ttl: Long, block: suspend () -> T?): TransitResult<T> {
        if (sptransToken.isNullOrBlank()) {
            return TransitResult.NotConfigured("Integração com ônibus (SPTrans Olho Vivo) ainda não configurada no servidor.")
        }
        cached<T>(cacheKey)?.let { return TransitResult.Ok(it) }
        return try {
            val value = block() ?: return if (!sptransLoggedIn && sptransRejectedAt > 0) {
                TransitResult.Unavailable("A SPTrans recusou a chave de acesso do servidor. Consulta de ônibus indisponível por enquanto.")
            } else {
                TransitResult.Unavailable("A SPTrans não respondeu. Tente de novo em instantes.")
            }
            put(cacheKey, value as Any, ttl)
            TransitResult.Ok(value)
        } catch (e: Exception) {
            log.warn("Falha ao consultar a SPTrans: {}", e.message)
            TransitResult.Unavailable("A SPTrans não respondeu. Tente de novo em instantes.")
        }
    }

    suspend fun searchBusLines(query: String): TransitResult<List<BusLineDto>> =
        sptrans("lines:${query.lowercase()}", CATALOG_TTL_MS) {
            sptransGet("/Linha/Buscar", mapOf("termosBusca" to query))?.jsonArray?.mapNotNull { it.toBusLine() }
        }

    suspend fun stopsForLine(lineCode: Int): TransitResult<List<BusStopDto>> =
        sptrans("stops:$lineCode", CATALOG_TTL_MS) {
            sptransGet("/Parada/BuscarParadasPorLinha", mapOf("codigoLinha" to lineCode))?.jsonArray?.mapNotNull { it.toBusStop() }
        }

    suspend fun arrivalsAtStop(stopCode: Int): TransitResult<BusArrivalsDto> =
        sptrans("arrivals:$stopCode", ARRIVALS_TTL_MS) {
            val root = sptransGet("/Previsao/Parada", mapOf("codigoParada" to stopCode))?.jsonObject ?: return@sptrans null
            val stop = root["p"]?.asObject() ?: return@sptrans BusArrivalsDto(stopCode, "", root.str("hr"), emptyList())
            BusArrivalsDto(
                stopCode = stop.int("cp") ?: stopCode,
                stopName = stop.str("np"),
                referenceTime = root.str("hr"),
                lines = (stop["l"] as? JsonArray).orEmpty().mapNotNull { lineEl ->
                    val line = lineEl.asObject() ?: return@mapNotNull null
                    val direction = line.int("sl") ?: 1
                    BusArrivalLineDto(
                        sign = line.str("c"),
                        lineCode = line.int("cl") ?: 0,
                        direction = direction,
                        destination = line.str("lt0"),
                        origin = line.str("lt1"),
                        vehicles = (line["vs"] as? JsonArray).orEmpty().mapNotNull { it.toVehicle() }
                            .sortedBy { it.arrivalTime ?: "99:99" }
                    )
                }
            )
        }

    private fun JsonElement.asObject(): JsonObject? = this as? JsonObject
    private fun JsonObject.str(key: String): String = this[key]?.jsonPrimitive?.content?.takeIf { it != "null" }.orEmpty()
    private fun JsonObject.int(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull
    private fun JsonObject.dbl(key: String): Double? = this[key]?.jsonPrimitive?.doubleOrNull
    private fun JsonObject.bool(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull

    private fun JsonElement.toBusLine(): BusLineDto? {
        val o = asObject() ?: return null
        val code = o.int("cl") ?: return null
        val number = o.str("lt")
        val suffix = o.int("tl")
        return BusLineDto(
            code = code,
            sign = if (suffix != null) "$number-$suffix" else number,
            direction = o.int("sl") ?: 1,
            mainTerminal = o.str("tp"),
            secondaryTerminal = o.str("ts"),
            circular = o.bool("lc") ?: false
        )
    }

    private fun JsonElement.toBusStop(): BusStopDto? {
        val o = asObject() ?: return null
        return BusStopDto(
            code = o.int("cp") ?: return null,
            name = o.str("np"),
            address = o.str("ed"),
            latitude = o.dbl("py") ?: 0.0,
            longitude = o.dbl("px") ?: 0.0
        )
    }

    private fun JsonElement.toVehicle(): BusVehicleDto? {
        val o = asObject() ?: return null
        return BusVehicleDto(
            prefix = o.str("p"),
            arrivalTime = o.str("t").ifBlank { null },
            accessible = o.bool("a") ?: false,
            latitude = o.dbl("py"),
            longitude = o.dbl("px")
        )
    }

    // ---------------- API Trilhos (ARTESP) ----------------

    private val railMutex = Mutex()

    /**
     * Status das linhas. Consulta a ARTESP no máximo a cada 5 minutos; se a consulta falhar ou o
     * limite estourar (429), devolve o último resultado bom que houver.
     */
    suspend fun railStatus(): TransitResult<RailStatusDto> = railMutex.withLock {
        if (artespKey.isNullOrBlank()) {
            return TransitResult.NotConfigured("Status dos trilhos (ARTESP API Trilhos) ainda não configurado no servidor.")
        }
        cached<RailStatusDto>("rail")?.let { return TransitResult.Ok(it) }
        val stale = cache["rail"]?.value as RailStatusDto?
        try {
            val response = client.get(ARTESP_STATUS_URL) { header(HttpHeaders.Authorization, "Api-Key $artespKey") }
            when (response.status.value) {
                in 200..299 -> {
                    val dto = parseRail(json.parseToJsonElement(response.bodyAsText()))
                    put("rail", dto, RAIL_TTL_MS)
                    TransitResult.Ok(dto)
                }
                401, 403 -> {
                    log.warn("API Trilhos recusou a chave (status {}). Confira ARTESP_API_KEY e os termos aceitos.", response.status.value)
                    stale?.let { TransitResult.Ok(it) } ?: TransitResult.Unavailable("A ARTESP recusou o acesso. Fale com o suporte do app.")
                }
                else -> {
                    log.warn("API Trilhos respondeu {}.", response.status.value)
                    stale?.let { TransitResult.Ok(it) } ?: TransitResult.Unavailable("Status dos trilhos indisponível no momento.")
                }
            }
        } catch (e: Exception) {
            log.warn("Falha ao consultar a API Trilhos: {}", e.message)
            stale?.let { TransitResult.Ok(it) } ?: TransitResult.Unavailable("Status dos trilhos indisponível no momento.")
        }
    }

    private fun parseRail(root: JsonElement): RailStatusDto {
        val lines = mutableListOf<RailLineStatusDto>()
        (root.jsonObject["empresas"] as? JsonArray).orEmpty().forEach { companyEl ->
            val company = companyEl.asObject() ?: return@forEach
            (company["linhas"] as? JsonArray).orEmpty().forEach { lineEl ->
                val line = lineEl.asObject() ?: return@forEach
                if (line.bool("ativa") == false) return@forEach
                val status = line["status"] as? JsonObject
                lines += RailLineStatusDto(
                    operator = company.str("nome"),
                    line = line.str("nome"),
                    code = line.str("codigo"),
                    situation = status?.str("situacao")?.ifBlank { null } ?: "Sem informação",
                    description = status?.str("descricao").orEmpty(),
                    classification = status?.str("classificacao")?.ifBlank { null } ?: "desconhecida",
                    normal = status?.bool("operacao_normal") ?: false,
                    updatedAt = status?.str("atualizado_em")?.ifBlank { null },
                    verifiedAt = status?.str("verificado_em")?.ifBlank { null }
                )
            }
        }
        return RailStatusDto(
            lines = lines.sortedBy { it.code.toIntOrNull() ?: Int.MAX_VALUE },
            fetchedAtEpochMs = now(),
            source = "ARTESP • API Trilhos",
            coverageNote = RAIL_COVERAGE
        )
    }
}
