plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    application
}

application {
    mainClass.set("com.example.projetorevisao.server.ApplicationKt")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.ktorServerCore)
    implementation(libs.ktorServerNetty)
    implementation(libs.ktorServerContentNegotiation)
    implementation(libs.ktorSerializationKotlinxJson)
    implementation(libs.ktorServerCors)
    implementation(libs.ktorServerStatusPages)
    implementation(libs.ktorServerCallLogging)
    implementation(libs.ktorServerAuth)
    implementation(libs.ktorServerAuthJwt)
    implementation(libs.ktorServerRateLimit)
    implementation(libs.ktorServerForwardedHeader)
    implementation(libs.ktorServerPartialContent)
    implementation(libs.bcrypt)
    implementation(libs.ktorClientCio)
    implementation(libs.postgresql)
    implementation(libs.hikaricp)
    implementation(libs.flywayCore)
    implementation(libs.flywayPostgresql)
    // Só para copiar o banco antigo (SQLite) para o Postgres na troca de produção.
    implementation(libs.sqliteJdbc)
    implementation(libs.logbackClassic)
    implementation(libs.kotlinxSerializationJson)

    testImplementation(libs.ktorServerTestHost)
    testImplementation(libs.ktorClientMock)
    testImplementation(libs.junit)
    testImplementation(libs.embeddedPostgres)
}

// Servidor local com Postgres embutido (não precisa instalar Postgres nem Docker).
// Os dados ficam em server/data/pg-local. Uso: .\gradlew.bat :server:runLocal
tasks.register<JavaExec>("runLocal") {
    group = "application"
    description = "Roda o servidor com um Postgres embutido em server/data/pg-local."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.example.projetorevisao.server.LocalServerKt")
    workingDir = projectDir
    // Mesmo limite de memória da produção (Dockerfile), para pegar estouro de memória antes do deploy.
    maxHeapSize = "192m"
}
