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
import java.sql.Timestamp
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
                    INSERT INTO users (id, name, email, password_hash, neighborhood, trust_score, anonymous_default, geofence_radius, created_at, terms_accepted_at)
                    VALUES (?, ?, ?, ?, ?, 0, TRUE, 2.0, ?, ?)
                    """.trimIndent()
                ).use { stmt ->
                    stmt.setString(1, userId)
                    stmt.setString(2, req.name.trim())
                    stmt.setString(3, email)
                    stmt.setString(4, passwordHash)
                    stmt.setString(5, req.neighborhood.trim())
                    stmt.setTimestamp(6, Timestamp(now))
                    stmt.setTimestamp(7, Timestamp(now))
                    stmt.executeUpdate()
                }
            } catch (ex: SQLException) {
                // Corrida entre dois cadastros com o mesmo e-mail: o índice único decide (23505 = unique_violation).
                if (ex.sqlState == "23505") return RegisterResult.EmailTaken
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
                    revoked = rs.getBoolean("revoked")
                    expiresAt = rs.getTimestamp("expires_at").time
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
            val claimed = conn.prepareStatement("UPDATE refresh_tokens SET revoked = TRUE WHERE id = ? AND NOT revoked").use { stmt ->
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
            conn.prepareStatement("UPDATE refresh_tokens SET revoked = TRUE WHERE token_hash = ?").use { stmt ->
                stmt.setString(1, JwtConfig.hashRefreshToken(refreshToken))
                stmt.executeUpdate()
            }
        }
    }

    /**
     * Exclui a conta. Exige a senha atual; devolve false se a senha estiver errada.
     * As FKs do banco fazem o resto: CASCADE apaga sessões, guardiões, alertas SOS, confirmações,
     * denúncias e bloqueios; SET NULL deixa relatos, anexos e comentários públicos sem ligação com a pessoa.
     */
    fun deleteAccount(userId: String, password: String): Boolean {
        DatabaseFactory.getConnection().use { conn ->
            val user = findUserById(conn, userId) ?: return false
            if (!PasswordHasher.verify(password, user.passwordHash)) return false
            conn.prepareStatement("DELETE FROM users WHERE id = ?").use { stmt ->
                stmt.setString(1, userId)
                stmt.executeUpdate()
            }
            log.info("Conta {} excluída a pedido do usuário.", userId)
            return true
        }
    }

    /** Exclusão pela página web: identifica a conta pelo e-mail e exige a senha. */
    fun deleteAccountByEmail(rawEmail: String, password: String): Boolean {
        val email = Validators.normalizeEmail(rawEmail)
        val userId = DatabaseFactory.getConnection().use { conn -> findUserByEmail(conn, email)?.id } ?: return false
        return deleteAccount(userId, password)
    }

    /** Encerra todas as sessões do usuário (ex.: "sair de todos os aparelhos"). */
    fun logoutAll(userId: String) {
        DatabaseFactory.getConnection().use { conn -> revokeAll(conn, userId) }
    }

    private fun revokeAll(conn: Connection, userId: String) {
        conn.prepareStatement("UPDATE refresh_tokens SET revoked = TRUE WHERE user_id = ?").use { stmt ->
            stmt.setString(1, userId)
            stmt.executeUpdate()
        }
    }

    private fun issueTokens(conn: Connection, user: UserRow): AuthResponse {
        val refreshToken = JwtConfig.generateRefreshToken()
        val now = System.currentTimeMillis()
        conn.prepareStatement(
            "INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, revoked, created_at) VALUES (?, ?, ?, ?, FALSE, ?)"
        ).use { stmt ->
            stmt.setString(1, UUID.randomUUID().toString())
            stmt.setString(2, user.id)
            stmt.setString(3, JwtConfig.hashRefreshToken(refreshToken))
            stmt.setTimestamp(4, Timestamp(now + JwtConfig.refreshTokenTtlMillis))
            stmt.setTimestamp(5, Timestamp(now))
            stmt.executeUpdate()
        }
        // Limpeza oportunista de tokens vencidos deste usuário.
        conn.prepareStatement("DELETE FROM refresh_tokens WHERE user_id = ? AND expires_at < ?").use { stmt ->
            stmt.setString(1, user.id)
            stmt.setTimestamp(2, Timestamp(now))
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
