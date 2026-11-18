package com.example.projetorevisao.server

import com.example.projetorevisao.server.transit.TransitResult
import com.example.projetorevisao.server.transit.TransitService
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class TransitServiceTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private val railJson = """
        {"meta":{"versao":"1.2.0","total_linhas":2,"total_empresas":2},
         "empresas":[
           {"id":1,"nome":"ViaQuatro","fiscalizacao_artesp":true,"linhas":[
             {"nome":"Linha 4-Amarela","codigo":"4","ativa":true,
              "status":{"situacao":"Operação Normal","descricao":"","classificacao":"operacional","operacao_normal":true,
                        "atualizado_em":"2026-11-18T08:12:00-03:00","verificado_em":"2026-11-18T10:28:00-03:00"},
              "estacoes":{"total":11,"nomes":[]}}]},
           {"id":2,"nome":"ViaMobilidade","fiscalizacao_artesp":true,"linhas":[
             {"nome":"Linha 9-Esmeralda","codigo":"9","ativa":true,
              "status":{"situacao":"Velocidade Reduzida","descricao":"Falha em equipamento","classificacao":"problema","operacao_normal":false}},
             {"nome":"Linha 5-Lilás","codigo":"5","ativa":true,"status":null}]}
         ]}
    """.trimIndent()

    @Test
    fun withoutKeysServicesReportNotConfigured() = runBlocking {
        val service = TransitService(MockEngine { error("não deveria chamar a rede") }, sptransToken = null, artespKey = null)
        assertTrue(service.railStatus() is TransitResult.NotConfigured)
        assertTrue(service.searchBusLines("8000") is TransitResult.NotConfigured)
    }

    @Test
    fun railStatusIsParsedSortedAndCachedForFiveMinutes() = runBlocking {
        var calls = 0
        var clock = 1_000_000L
        val engine = MockEngine { request ->
            calls++
            assertEquals("Api-Key chave-teste", request.headers[HttpHeaders.Authorization])
            respond(railJson, HttpStatusCode.OK, jsonHeaders)
        }
        val service = TransitService(engine, sptransToken = null, artespKey = "chave-teste", now = { clock })

        val first = service.railStatus() as TransitResult.Ok
        assertEquals(listOf("4", "5", "9"), first.data.lines.map { it.code })
        val line9 = first.data.lines.first { it.code == "9" }
        assertEquals("Velocidade Reduzida", line9.situation)
        assertFalse(line9.normal)
        assertEquals("Sem informação", first.data.lines.first { it.code == "5" }.situation)

        service.railStatus()
        assertEquals(1, calls) // dentro dos 5 minutos, usa o cache

        clock += 5 * 60 * 1000L + 1
        service.railStatus()
        assertEquals(2, calls)
    }

    @Test
    fun railStatusKeepsLastGoodResultWhenRateLimited() = runBlocking {
        var clock = 0L
        var limited = false
        val engine = MockEngine {
            if (limited) respond("", HttpStatusCode.TooManyRequests) else respond(railJson, HttpStatusCode.OK, jsonHeaders)
        }
        val service = TransitService(engine, sptransToken = null, artespKey = "k", now = { clock })
        service.railStatus()
        limited = true
        clock += 10 * 60 * 1000L
        val result = service.railStatus()
        assertTrue(result is TransitResult.Ok)
        assertEquals(3, (result as TransitResult.Ok).data.lines.size)
    }

    @Test
    fun busLinesLoginOnceAndParseSigns() = runBlocking {
        var logins = 0
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/v2.1/Login/Autenticar" -> {
                    logins++
                    assertEquals("tok", request.url.parameters["token"])
                    respond("true", HttpStatusCode.OK, jsonHeaders)
                }
                "/v2.1/Linha/Buscar" -> respond(
                    """[{"cl":1273,"lc":false,"lt":"8000","sl":1,"tl":10,"tp":"PCA.RAMOS DE AZEVEDO","ts":"TERMINAL LAPA"}]""",
                    HttpStatusCode.OK, jsonHeaders
                )
                else -> respond("", HttpStatusCode.NotFound)
            }
        }
        val service = TransitService(engine, sptransToken = "tok", artespKey = null)
        val lines = (service.searchBusLines("8000") as TransitResult.Ok).data
        assertEquals("8000-10", lines.single().sign)
        assertEquals("TERMINAL LAPA", lines.single().destination)
        service.searchBusLines("lapa")
        assertEquals(1, logins)
    }

    @Test
    fun arrivalsAreParsedAndSessionIsRenewedOn401() = runBlocking {
        var logins = 0
        var expired = true
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/v2.1/Login/Autenticar" -> { logins++; respond("true", HttpStatusCode.OK, jsonHeaders) }
                "/v2.1/Previsao/Parada" -> if (expired) {
                    expired = false
                    respond("", HttpStatusCode.Unauthorized)
                } else {
                    respond(
                        """{"hr":"20:09","p":{"cp":4200953,"np":"PARADA ROBERTO SELMI DEI B/C","py":-23.67,"px":-46.75,
                            "l":[{"c":"7021-10","cl":1989,"sl":1,"lt0":"TERM. JOÃO DIAS","lt1":"JD. MARACÁ","qv":2,
                                  "vs":[{"p":"74558","t":"23:11","a":true,"ta":"2017-05-07T23:09:05Z","py":-23.68,"px":-46.77},
                                        {"p":"74557","t":"23:05","a":false,"ta":"2017-05-07T23:08:00Z","py":-23.67,"px":-46.76}]}]}}""",
                        HttpStatusCode.OK, jsonHeaders
                    )
                }
                else -> respond("", HttpStatusCode.NotFound)
            }
        }
        val service = TransitService(engine, sptransToken = "tok", artespKey = null)
        val arrivals = (service.arrivalsAtStop(4200953) as TransitResult.Ok).data
        assertEquals(2, logins) // sessão expirada → novo login
        assertEquals("PARADA ROBERTO SELMI DEI B/C", arrivals.stopName)
        val line = arrivals.lines.single()
        assertEquals("TERM. JOÃO DIAS", line.destination)
        assertEquals(listOf("23:05", "23:11"), line.vehicles.map { it.arrivalTime }) // mais próximo primeiro
    }

    @Test
    fun rejectedTokenIsReportedAndNotRetriedOnEveryCall() = runBlocking {
        var logins = 0
        val engine = MockEngine { request ->
            if (request.url.encodedPath == "/v2.1/Login/Autenticar") { logins++; respond("false", HttpStatusCode.OK, jsonHeaders) }
            else respond("", HttpStatusCode.Unauthorized)
        }
        val service = TransitService(engine, sptransToken = "invalido", artespKey = null)
        val first = service.searchBusLines("8000")
        assertTrue((first as TransitResult.Unavailable).message.contains("recusou"))
        service.searchBusLines("lapa")
        assertEquals(1, logins) // espera 1 minuto antes de tentar o login de novo
    }
}
