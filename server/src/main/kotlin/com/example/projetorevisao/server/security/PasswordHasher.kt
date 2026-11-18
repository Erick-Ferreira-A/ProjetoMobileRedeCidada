package com.example.projetorevisao.server.security

import at.favre.lib.crypto.bcrypt.BCrypt
import java.security.MessageDigest

/** Hash de senhas com BCrypt (custo 12). */
object PasswordHasher {
    private const val COST = 12

    /** Hash usado para gastar o mesmo tempo quando o e-mail não existe (evita descobrir contas pelo tempo de resposta). */
    private val dummyHash: String = hash("rede-cidada-dummy-password")

    fun hash(password: String): String =
        BCrypt.withDefaults().hashToString(COST, password.toCharArray())

    fun verify(password: String, storedHash: String): Boolean =
        if (isBcrypt(storedHash)) {
            BCrypt.verifyer().verify(password.toCharArray(), storedHash).verified
        } else {
            verifyLegacySha256(password, storedHash)
        }

    /** Executa uma verificação descartável para igualar o tempo de resposta. */
    fun burnTime(password: String) {
        BCrypt.verifyer().verify(password.toCharArray(), dummyHash)
    }

    fun isBcrypt(storedHash: String): Boolean = storedHash.startsWith("$2")

    /**
     * Hashes antigos (SHA-256 sem salt ou com o salt fixo da versão anterior).
     * Só servem para migrar: no primeiro login correto a senha é regravada em BCrypt.
     */
    private fun verifyLegacySha256(password: String, storedHash: String): Boolean {
        val candidates = listOf(password, "$password:RedeCidadaSP_SecureKey_2026")
        return candidates.any { input ->
            val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
            val hex = digest.joinToString("") { "%02x".format(it) }
            MessageDigest.isEqual(hex.toByteArray(), storedHash.lowercase().toByteArray())
        }
    }
}
