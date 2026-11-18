package com.example.projetorevisao.server.repository

import com.example.projetorevisao.server.database.DatabaseFactory
import com.example.projetorevisao.server.models.AuthResponse
import com.example.projetorevisao.server.models.RegisterRequest
import com.example.projetorevisao.server.security.JwtConfig
import com.example.projetorevisao.server.security.PasswordHasher
import com.example.projetorevisao.server.security.Validators
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import java.util.UUID

sealed interface RegisterResult {
    data class Success(val auth: AuthResponse) : RegisterResult
    data object EmailTaken : RegisterResult
}

/** Cadastro, login e ciclo de vida dos tokens. */
class AuthRepository {
    private val log = LoggerFactory.getLogger(AuthRepository::class.java)

    private data class UserRow(
        val id: String,
        val name: String,
        val email: String,
        val neighborhood: String,
        val trustScore: Int,
        val passwordHash: String
    )

    fun register(req: RegisterRequest): RegisterResult {
        val email = Validators.normalizeEmail(req.email)
        val userId = "usr-${UUID.randomUUID()}"
        val passwordHash = PasswordHasher.hash(req.password)
        val now = System.currentTimeMillis()

        DatabaseFactory.getConnection().use { conn ->
            if (findUserByEmail(conn, email) != null) return RegisterResult.EmailTaken
            try {
                conn.prepareStatement(
                    """
                    INSERT INTO users (id, name, email, cpf_masked, password_hash, neighborhood, trust_score, anonymous_default, geofence_radius, created_at, terms_accepted_at)
                    VALUES (?, ?, ?, '', ?, ?, 0, 1, 2.0, ?, ?)
                    """.trimIndent()
                ).use { stmt ->
                    stmt.setString(1, userId)
                    stmt.setString(2, req.name.trim())
                    stmt.setString(3, email)
                    stmt.setString(4, passwordHash)
                    stmt.setString(5, req.neighborhood.trim())
                    stmt.setLong(6, now)
                    stmt.setLong(7, now)
                    stmt.executeUpdate()
                }
            } catch (ex: SQLException) {
                // Corrida entre dois cadastros com o mesmo e-mail: a restrição UNIQUE decide.
                if (ex.message?.contains("UNIQUE", ignoreCase = true) == true) return RegisterResult.EmailTaken
                throw ex
            }
            val user = UserRow(userId, req.name.trim(), email, req.neighborhood.trim(), 0, passwordHash)
            return RegisterResult.Success(issueTokens(conn, user))
        }
    }

    /** Devolve null para e-mail inexistente ou senha errada, sem distinguir os dois casos. */
    fun login(rawEmail: String, password: String): AuthResponse? {
        val email = Validators.normalizeEmail(rawEmail)
        DatabaseFactory.getConnection().use { conn ->
            val user = findUserByEmail(conn, email)
            if (user == null) {
                PasswordHasher.burnTime(password)
                return null
            }
            if (!PasswordHasher.verify(password, user.passwordHash)) return null

            if (!PasswordHasher.isBcrypt(user.passwordHash)) {
                conn.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?").use { stmt ->
                    stmt.setString(1, PasswordHasher.hash(password))
                    stmt.setString(2, user.id)
                    stmt.executeUpdate()
                }
                log.info("Senha do usuário {} migrada de SHA-256 para BCrypt.", user.id)
            }
            return issueTokens(conn, user)
        }
    }

    /**
     * Troca um refresh token válido por um par novo (rotação). Se um token já revogado
     * for reapresentado, assume vazamento e revoga todas as sessões do usuário.
     */
    fun refresh(refreshToken: String): AuthResponse? {
        val tokenHash = JwtConfig.hashRefreshToken(refreshToken)
        DatabaseFactory.getConnection().use { conn ->
            var tokenId: String? = null
            var userId: String? = null
            var revoked = false
            var expiresAt = 0L
            conn.prepareStatement("SELECT id, user_id, revoked, expires_at FROM refresh_tokens WHERE token_hash = ?").use { stmt ->
                stmt.setString(1, tokenHash)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    tokenId = rs.getString("id")
                    userId = rs.getString("user_id")
                    revoked = rs.getInt("revoked") == 1
                    expiresAt = rs.getLong("expires_at")
                }
            }
            val id = tokenId ?: return null
            val owner = userId ?: return null

            if (revoked) {
                log.warn("Refresh token revogado reutilizado para o usuário {}. Revogando todas as sessões.", owner)
                revokeAll(conn, owner)
                return null
            }
            if (expiresAt < System.currentTimeMillis()) return null

            // Marca como usado de forma atômica: só uma requisição concorrente vence.
            val claimed = conn.prepareStatement("UPDATE refresh_tokens SET revoked = 1 WHERE id = ? AND revoked = 0").use { stmt ->
                stmt.setString(1, id)
                stmt.executeUpdate()
            }
            if (claimed != 1) return null

            val user = findUserById(conn, owner) ?: return null
            return issueTokens(conn, user)
        }
    }

    /** Encerra a sessão ligada a este refresh token. Idempotente. */
    fun logout(refreshToken: String) {
        DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement("UPDATE refresh_tokens SET revoked = 1 WHERE token_hash = ?").use { stmt ->
                stmt.setString(1, JwtConfig.hashRefreshToken(refreshToken))
                stmt.executeUpdate()
            }
        }
    }

    /**
     * Exclui a conta e os dados ligados a ela (sessões, guardiões, alertas SOS).
     * Exige a senha atual. Devolve false se a senha estiver errada.
     */
    fun deleteAccount(userId: String, password: String): Boolean {
        DatabaseFactory.getConnection().use { conn ->
            val user = findUserById(conn, userId) ?: return false
            if (!PasswordHasher.verify(password, user.passwordHash)) return false
            conn.autoCommit = false
            try {
                for (sql in listOf(
                    "DELETE FROM refresh_tokens WHERE user_id = ?",
                    "DELETE FROM guardians WHERE user_id = ?",
                    "DELETE FROM sos_alerts WHERE user_id = ?",
                    "DELETE FROM incident_confirmations WHERE user_id = ?",
                    "DELETE FROM incident_reports WHERE reporter_user_id = ?",
                    "DELETE FROM user_blocks WHERE blocker_id = ?",
                    "DELETE FROM user_blocks WHERE blocked_id = ?",
                    "UPDATE incidents SET reporter_id = NULL WHERE reporter_id = ?",
                    "UPDATE incident_media SET uploader_id = NULL WHERE uploader_id = ?",
                    "DELETE FROM users WHERE id = ?"
                )) {
                    conn.prepareStatement(sql).use { stmt ->
                        stmt.setString(1, userId)
                        stmt.executeUpdate()
                    }
                }
                // As confirmações dessa pessoa saíram: recalcula os contadores.
                conn.createStatement().use {
                    it.executeUpdate(
                        "UPDATE incidents SET confirmations = " +
                            "(SELECT COUNT(*) FROM incident_confirmations c WHERE c.incident_id = incidents.id)"
                    )
                }
                conn.commit()
            } catch (ex: Exception) {
                conn.rollback()
                throw ex
            } finally {
                conn.autoCommit = true
            }
            log.info("Conta {} excluída a pedido do usuário.", userId)
            return true
        }
    }

    /** Encerra todas as sessões do usuário (ex.: "sair de todos os aparelhos"). */
    fun logoutAll(userId: String) {
        DatabaseFactory.getConnection().use { conn -> revokeAll(conn, userId) }
    }

    private fun revokeAll(conn: Connection, userId: String) {
        conn.prepareStatement("UPDATE refresh_tokens SET revoked = 1 WHERE user_id = ?").use { stmt ->
            stmt.setString(1, userId)
            stmt.executeUpdate()
        }
    }

    private fun issueTokens(conn: Connection, user: UserRow): AuthResponse {
        val refreshToken = JwtConfig.generateRefreshToken()
        val now = System.currentTimeMillis()
        conn.prepareStatement(
            "INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, revoked, created_at) VALUES (?, ?, ?, ?, 0, ?)"
        ).use { stmt ->
            stmt.setString(1, UUID.randomUUID().toString())
            stmt.setString(2, user.id)
            stmt.setString(3, JwtConfig.hashRefreshToken(refreshToken))
            stmt.setLong(4, now + JwtConfig.refreshTokenTtlMillis)
            stmt.setLong(5, now)
            stmt.executeUpdate()
        }
        // Limpeza oportunista de tokens vencidos deste usuário.
        conn.prepareStatement("DELETE FROM refresh_tokens WHERE user_id = ? AND expires_at < ?").use { stmt ->
            stmt.setString(1, user.id)
            stmt.setLong(2, now)
            stmt.executeUpdate()
        }
        return AuthResponse(
            accessToken = JwtConfig.generateAccessToken(user.id, user.email),
            refreshToken = refreshToken,
            expiresInSeconds = JwtConfig.accessTokenTtlSeconds,
            userId = user.id,
            name = user.name,
            email = user.email,
            neighborhood = user.neighborhood,
            userTrustScore = user.trustScore
        )
    }

    private fun findUserByEmail(conn: Connection, email: String): UserRow? =
        findUser(conn, "SELECT id, name, email, neighborhood, trust_score, password_hash FROM users WHERE lower(email) = ?", email)

    private fun findUserById(conn: Connection, id: String): UserRow? =
        findUser(conn, "SELECT id, name, email, neighborhood, trust_score, password_hash FROM users WHERE id = ?", id)

    private fun findUser(conn: Connection, sql: String, param: String): UserRow? =
        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, param)
            val rs = stmt.executeQuery()
            if (!rs.next()) return null
            UserRow(
                id = rs.getString("id"),
                name = rs.getString("name"),
                email = rs.getString("email"),
                neighborhood = rs.getString("neighborhood"),
                trustScore = rs.getInt("trust_score"),
                passwordHash = rs.getString("password_hash")
            )
        }
}
