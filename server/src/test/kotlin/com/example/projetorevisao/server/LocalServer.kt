package com.example.projetorevisao.server

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import java.io.File

/**
 * Servidor de desenvolvimento com Postgres embutido (sem instalar nada).
 * Rode com: .\gradlew.bat :server:runLocal   (dados em server/data/pg-local)
 */
fun main() {
    val dataDir = File("data/pg-local").apply { mkdirs() }
    val pg = EmbeddedPostgres.builder()
        // UTF-8 como no Railway (no Windows o padrão seria Windows-1252, que não aceita emoji).
        .setLocaleConfig("encoding", "UTF8")
        .setLocaleConfig("locale", "C")
        .setDataDirectory(dataDir)
        .setCleanDataDirectory(false)
        .setPort(54329)
        .start()
    Runtime.getRuntime().addShutdownHook(Thread { pg.close() })
    System.setProperty("DATABASE_URL", pg.getJdbcUrl("postgres", "postgres"))
    println("Postgres local em ${dataDir.absolutePath} (porta 54329)")
    main(emptyArray())
}
