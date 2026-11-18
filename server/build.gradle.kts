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
    implementation(libs.bcrypt)
    implementation(libs.ktorClientCio)
    implementation(libs.sqliteJdbc)
    implementation(libs.logbackClassic)
    implementation(libs.kotlinxSerializationJson)

    testImplementation(libs.ktorServerTestHost)
    testImplementation(libs.ktorClientMock)
    testImplementation(libs.junit)
}
