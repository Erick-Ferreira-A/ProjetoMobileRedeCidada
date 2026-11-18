package com.example.projetorevisao.data.remote

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

data class BusLine(val code: Int, val sign: String, val direction: Int, val destination: String, val origin: String)
data class BusStop(val code: Int, val name: String, val address: String)
data class BusVehicle(val prefix: String, val arrivalTime: String?, val accessible: Boolean)
data class BusArrivalLine(val sign: String, val destination: String, val vehicles: List<BusVehicle>)
data class BusArrivals(val stopName: String, val referenceTime: String, val lines: List<BusArrivalLine>)
data class RailLine(
    val operator: String,
    val line: String,
    val code: String,
    val situation: String,
    val description: String,
    val classification: String,
    val normal: Boolean
)
data class RailStatus(val lines: List<RailLine>, val fetchedAtEpochMs: Long, val coverageNote: String)

/** Resultado de transporte: dados, ou a mensagem para mostrar (fonte não configurada, fora do ar etc.). */
sealed interface TransitData<out T> {
    data class Ready<T>(val data: T) : TransitData<T>
    data class Message(val text: String) : TransitData<Nothing>
}

/** Ônibus (SPTrans Olho Vivo) e trilhos (ARTESP), sempre via servidor da Rede Cidadã. */
object TransitApi {

    suspend fun railStatus(): TransitData<RailStatus> = fetch("/transit/rail/status") { root ->
        val data = root.optJSONObject("data") ?: return@fetch null
        RailStatus(
            lines = data.optJSONArray("lines").objects().map {
                RailLine(
                    operator = it.optString("operator"),
                    line = it.optString("line"),
                    code = it.optString("code"),
                    situation = it.optString("situation"),
                    description = it.optString("description"),
                    classification = it.optString("classification"),
                    normal = it.optBoolean("normal")
                )
            },
            fetchedAtEpochMs = data.optLong("fetchedAtEpochMs"),
            coverageNote = data.optString("coverageNote")
        )
    }

    suspend fun searchLines(query: String): TransitData<List<BusLine>> =
        fetch("/transit/bus/lines?q=${URLEncoder.encode(query, "UTF-8")}") { root ->
            root.optJSONArray("data").objects().map {
                val direction = it.optInt("direction", 1)
                val main = it.optString("mainTerminal")
                val secondary = it.optString("secondaryTerminal")
                BusLine(
                    code = it.optInt("code"),
                    sign = it.optString("sign"),
                    direction = direction,
                    destination = if (direction == 1) secondary else main,
                    origin = if (direction == 1) main else secondary
                )
            }
        }

    suspend fun stops(lineCode: Int): TransitData<List<BusStop>> = fetch("/transit/bus/lines/$lineCode/stops") { root ->
        root.optJSONArray("data").objects().map { BusStop(it.optInt("code"), it.optString("name"), it.optString("address")) }
    }

    suspend fun arrivals(stopCode: Int): TransitData<BusArrivals> = fetch("/transit/bus/stops/$stopCode/arrivals") { root ->
        val data = root.optJSONObject("data") ?: return@fetch null
        BusArrivals(
            stopName = data.optString("stopName"),
            referenceTime = data.optString("referenceTime"),
            lines = data.optJSONArray("lines").objects().map { line ->
                BusArrivalLine(
                    sign = line.optString("sign"),
                    destination = line.optString("destination"),
                    vehicles = line.optJSONArray("vehicles").objects().map { v ->
                        BusVehicle(
                            prefix = v.optString("prefix"),
                            arrivalTime = v.optString("arrivalTime").takeIf { it.isNotBlank() && it != "null" },
                            accessible = v.optBoolean("accessible")
                        )
                    }
                )
            }
        )
    }

    private suspend fun <T> fetch(path: String, parse: (JSONObject) -> T?): TransitData<T> =
        when (val result = BackendHttp.get(path)) {
            is ApiResult.Success -> parse(result.root)?.let { TransitData.Ready(it) }
                ?: TransitData.Message("Resposta inesperada do servidor.")
            is ApiResult.Failure -> TransitData.Message(result.message)
        }

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }
}
