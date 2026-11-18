package com.example.projetorevisao.data.session

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/** Usuário logado e os tokens da sessão atual. */
data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val name: String,
    val email: String,
    val neighborhood: String,
    val trustScore: Int
)

/**
 * Guarda a sessão no armazenamento privado do app (fora do backup: ver res/xml/backup_rules.xml).
 * Chame [init] uma vez no início do app. Sem init (ex.: previews), funciona só em memória.
 */
object SessionManager {
    private const val PREFS = "session"
    private var prefs: SharedPreferences? = null

    private val _session = MutableStateFlow<UserSession?>(null)
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    val current: UserSession? get() = _session.value

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        _session.value = load(p)
    }

    /** Salva a resposta de login, cadastro ou refresh (`data` do servidor). */
    fun saveFromAuthJson(data: JSONObject) {
        val session = UserSession(
            accessToken = data.getString("accessToken"),
            refreshToken = data.getString("refreshToken"),
            userId = data.getString("userId"),
            name = data.optString("name"),
            email = data.optString("email"),
            neighborhood = data.optString("neighborhood"),
            trustScore = data.optInt("userTrustScore", 0)
        )
        prefs?.edit()?.apply {
            putString("accessToken", session.accessToken)
            putString("refreshToken", session.refreshToken)
            putString("userId", session.userId)
            putString("name", session.name)
            putString("email", session.email)
            putString("neighborhood", session.neighborhood)
            putInt("trustScore", session.trustScore)
            apply()
        }
        _session.value = session
    }

    fun clear() {
        prefs?.edit()?.clear()?.apply()
        _session.value = null
    }

    /** Escolheu "Continuar sem conta": na próxima abertura vai direto ao início. Sair da conta desfaz. */
    var guestMode: Boolean
        get() = prefs?.getBoolean(GUEST_KEY, false) ?: false
        set(value) {
            prefs?.edit()?.putBoolean(GUEST_KEY, value)?.apply()
        }

    private const val GUEST_KEY = "guestMode"

    private fun load(p: SharedPreferences): UserSession? {
        val access = p.getString("accessToken", null) ?: return null
        val refresh = p.getString("refreshToken", null) ?: return null
        val userId = p.getString("userId", null) ?: return null
        return UserSession(
            accessToken = access,
            refreshToken = refresh,
            userId = userId,
            name = p.getString("name", "").orEmpty(),
            email = p.getString("email", "").orEmpty(),
            neighborhood = p.getString("neighborhood", "").orEmpty(),
            trustScore = p.getInt("trustScore", 0)
        )
    }
}
