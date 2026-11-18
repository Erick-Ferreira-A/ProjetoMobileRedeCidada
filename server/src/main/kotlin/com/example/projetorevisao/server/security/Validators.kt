package com.example.projetorevisao.server.security

/** Validação de entrada. Cada função devolve a mensagem de erro, ou null quando o valor é válido. */
object Validators {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    const val PASSWORD_MIN = 8
    const val PASSWORD_MAX = 128

    fun normalizeEmail(email: String): String = email.trim().lowercase()

    fun email(email: String): String? = when {
        email.isBlank() -> "Informe o e-mail."
        email.length > 254 || !emailRegex.matches(email) -> "E-mail inválido."
        else -> null
    }

    fun password(password: String): String? = when {
        password.length < PASSWORD_MIN -> "A senha precisa ter pelo menos $PASSWORD_MIN caracteres."
        password.length > PASSWORD_MAX -> "A senha pode ter no máximo $PASSWORD_MAX caracteres."
        password.none { it.isLetter() } || password.none { it.isDigit() } -> "A senha precisa ter letras e números."
        else -> null
    }

    fun name(name: String): String? = when {
        name.trim().length < 2 -> "Informe o nome completo."
        name.length > 100 -> "O nome pode ter no máximo 100 caracteres."
        else -> null
    }

    fun neighborhood(neighborhood: String): String? = when {
        neighborhood.isBlank() -> "Informe o bairro."
        neighborhood.length > 120 -> "O bairro pode ter no máximo 120 caracteres."
        else -> null
    }

    fun geofenceRadius(radiusKm: Float): String? =
        if (radiusKm.isNaN() || radiusKm < 0.5f || radiusKm > 50f) "O raio de alerta deve ficar entre 0,5 e 50 km." else null
}
