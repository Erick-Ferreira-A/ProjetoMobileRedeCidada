package com.example.projetorevisao.server

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres

/**
 * Um Postgres de verdade, embutido (sem Docker), ligado uma vez para todos os testes.
 * Cada suíte usa o seu próprio banco dentro dele, para os dados de uma não interferirem na outra.
 */
object TestDatabase {
    private val server: EmbeddedPostgres by lazy {
        EmbeddedPostgres.builder()
            .setLocaleConfig("encoding", "UTF8") // como no Railway; no Windows o padrão seria Windows-1252
            .setLocaleConfig("locale", "C")
            .start().also { pg -> Runtime.getRuntime().addShutdownHook(Thread { pg.close() }) }
    }
    private val created = mutableSetOf<String>()

    /** URL JDBC de um banco novo e vazio chamado [name] (criado na primeira chamada). */
    @Synchronized
    fun jdbcUrl(name: String): String {
        require(name.matches(Regex("[a-z_]+"))) { "Nome de banco inválido: $name" }
        if (created.add(name)) {
            server.postgresDatabase.connection.use { conn ->
                conn.createStatement().use { it.execute("CREATE DATABASE $name") }
            }
        }
        return server.getJdbcUrl("postgres", name)
    }
}
