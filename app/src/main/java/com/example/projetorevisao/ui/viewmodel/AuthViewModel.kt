package com.example.projetorevisao.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetorevisao.data.remote.ApiResult
import com.example.projetorevisao.data.remote.AuthApi
import com.example.projetorevisao.data.session.SessionManager
import com.example.projetorevisao.data.session.UserSession
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Estado de login, cadastro, saída e exclusão de conta. */
class AuthViewModel : ViewModel() {

    val session: StateFlow<UserSession?> = SessionManager.session

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        errorMessage = null
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        val error = when {
            !email.isValidEmail() -> "Informe um e-mail válido."
            password.isEmpty() -> "Informe a senha."
            else -> null
        }
        if (error != null) {
            errorMessage = error
            return
        }
        run({ AuthApi.login(email, password) }, onSuccess)
    }

    fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        neighborhood: String,
        acceptedTerms: Boolean,
        onSuccess: () -> Unit
    ) {
        val error = when {
            name.trim().length < 2 -> "Informe seu nome completo."
            !email.isValidEmail() -> "Informe um e-mail válido."
            neighborhood.isBlank() -> "Informe seu bairro."
            password.length < 8 -> "A senha precisa ter pelo menos 8 caracteres."
            password.none { it.isLetter() } || password.none { it.isDigit() } -> "A senha precisa ter letras e números."
            password != passwordConfirmation -> "As senhas não são iguais."
            !acceptedTerms -> "Aceite os termos de uso e a política de privacidade para criar a conta."
            else -> null
        }
        if (error != null) {
            errorMessage = error
            return
        }
        run({ AuthApi.register(name, email, password, neighborhood, acceptedTerms) }, onSuccess)
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            AuthApi.logout()
            isLoading = false
            onDone()
        }
    }

    fun deleteAccount(password: String, onDeleted: () -> Unit) {
        if (password.isEmpty()) {
            errorMessage = "Digite sua senha para confirmar."
            return
        }
        run({ AuthApi.deleteAccount(password) }, onDeleted)
    }

    private fun run(call: suspend () -> ApiResult, onSuccess: () -> Unit) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            when (val result = call()) {
                is ApiResult.Success -> onSuccess()
                is ApiResult.Failure -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    private fun String.isValidEmail(): Boolean =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(trim())
}
