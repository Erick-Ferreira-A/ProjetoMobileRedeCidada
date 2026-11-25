package com.example.projetorevisao.data.session

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Dados do perfil que o servidor ainda **não** guarda (foto, nascimento, telefone, endereço).
 * Ficam só neste aparelho, em armazenamento privado do app.
 *
 * INTEGRAR: quando `PUT /api/v1/auth/profile` aceitar esses campos, enviar ao salvar e
 * deixar este armazenamento apenas como cache. Documentos (CPF/CNPJ, RG) NÃO são guardados:
 * a política de privacidade publicada diz que o app não coleta documentos.
 */
data class LocalProfile(
    val displayName: String = "",
    val birthDate: String = "",      // dd/mm/aaaa
    val phone: String = "",
    val cep: String = "",
    val street: String = "",
    val number: String = "",
    val complement: String = "",
    val district: String = "",
    val city: String = "",
    val avatarPath: String? = null
)

object LocalProfileStore {
    private const val PREFS = "local_profile"
    private const val AVATAR_FILE = "avatar.jpg"
    private var prefs: SharedPreferences? = null
    private val _profile = MutableStateFlow(LocalProfile())
    val profile: StateFlow<LocalProfile> = _profile.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        _profile.value = LocalProfile(
            displayName = p.getString("displayName", "").orEmpty(),
            birthDate = p.getString("birthDate", "").orEmpty(),
            phone = p.getString("phone", "").orEmpty(),
            cep = p.getString("cep", "").orEmpty(),
            street = p.getString("street", "").orEmpty(),
            number = p.getString("number", "").orEmpty(),
            complement = p.getString("complement", "").orEmpty(),
            district = p.getString("district", "").orEmpty(),
            city = p.getString("city", "").orEmpty(),
            avatarPath = p.getString("avatarPath", null)?.takeIf { File(it).exists() }
        )
    }

    fun save(profile: LocalProfile) {
        prefs?.edit()?.apply {
            putString("displayName", profile.displayName)
            putString("birthDate", profile.birthDate)
            putString("phone", profile.phone)
            putString("cep", profile.cep)
            putString("street", profile.street)
            putString("number", profile.number)
            putString("complement", profile.complement)
            putString("district", profile.district)
            putString("city", profile.city)
            putString("avatarPath", profile.avatarPath)
            apply()
        }
        _profile.value = profile
    }

    /** Copia a foto escolhida para a pasta privada do app (o acesso ao arquivo original é temporário). */
    suspend fun importAvatar(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val target = File(context.filesDir, AVATAR_FILE)
            context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { input.copyTo(it) } }
            target.absolutePath
        }.getOrNull()
    }

    /** Ao sair da conta, os dados pessoais deste aparelho são apagados. */
    fun clear(context: Context) {
        prefs?.edit()?.clear()?.apply()
        File(context.filesDir, AVATAR_FILE).delete()
        _profile.value = LocalProfile()
    }
}

/** Idade a partir de "dd/mm/aaaa"; null se a data for inválida. */
fun ageFromBirthDate(birthDate: String, today: java.util.Calendar = java.util.Calendar.getInstance()): Int? {
    val parts = birthDate.split("/")
    if (parts.size != 3) return null
    val (d, m, y) = parts.map { it.toIntOrNull() ?: return null }
    if (y < 1900 || m !in 1..12 || d !in 1..31) return null
    var age = today.get(java.util.Calendar.YEAR) - y
    val month = today.get(java.util.Calendar.MONTH) + 1
    if (month < m || (month == m && today.get(java.util.Calendar.DAY_OF_MONTH) < d)) age--
    return age.takeIf { it in 0..130 }
}
