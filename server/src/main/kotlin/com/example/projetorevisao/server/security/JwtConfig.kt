package com.example.projetorevisao.server.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.projetorevisao.server.config.AppConfig
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Date
import java.util.UUID

/** Emissão e validação de tokens. Access token = JWT curto; refresh token = valor aleatório opaco guardado em hash no banco. */
object JwtConfig {
    const val AUTH_NAME = "auth-jwt"
    private const val CLAIM_TYPE = "typ"
    private const val TYPE_ACCESS = "access"

    private val algorithm: Algorithm by lazy { Algorithm.HMAC256(AppConfig.jwtSecret) }
    private val random = SecureRandom()

    val accessTokenTtlSeconds: Long get() = AppConfig.accessTokenTtlMinutes * 60
    val refreshTokenTtlMillis: Long get() = AppConfig.refreshTokenTtlDays * 24 * 60 * 60 * 1000

    val verifier: JWTVerifier by lazy {
        JWT.require(algorithm)
            .withIssuer(AppConfig.jwtIssuer)
            .withAudience(AppConfig.jwtAudience)
            .withClaim(CLAIM_TYPE, TYPE_ACCESS)
            .acceptLeeway(5)
            .build()
    }

    fun generateAccessToken(userId: String, email: String): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(AppConfig.jwtIssuer)
            .withAudience(AppConfig.jwtAudience)
            .withSubject(userId)
            .withClaim("email", email)
            .withClaim(CLAIM_TYPE, TYPE_ACCESS)
            .withJWTId(UUID.randomUUID().toString())
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + accessTokenTtlSeconds * 1000))
            .sign(algorithm)
    }

    /** Gera um refresh token aleatório de 256 bits (enviado ao app uma única vez). */
    fun generateRefreshToken(): String {
        val bytes = ByteArray(32).also { random.nextBytes(it) }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /** Só o hash SHA-256 do refresh token vai para o banco. */
    fun hashRefreshToken(token: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
