package com.m2f.template.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m2f.template.models.dto.LoginRequest
import com.m2f.template.sdk.api.AuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authApi: AuthApi,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }

    fun onRememberMeChange(checked: Boolean) {
        _state.update { it.copy(rememberMe = checked) }
    }

    fun login() {
        val current = _state.value

        // Local validation
        val emailError = when {
            current.email.isBlank() -> "Email must not be blank"
            !current.email.contains("@") || !current.email.contains(".") -> "Email format is invalid"
            else -> null
        }
        val passwordError = when {
            current.password.isBlank() -> "Password must not be blank"
            else -> null
        }

        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _state.update { it.copy(isLoading = true, serverError = null) }

        viewModelScope.launch {
            authApi.login(LoginRequest(current.email.trim(), current.password))
                .fold(
                    ifLeft = { error ->
                        _state.update { it.copy(serverError = error.message, isLoading = false) }
                    },
                    ifRight = {
                        _state.update { it.copy(loginSuccess = true, isLoading = false) }
                    },
                )
        }
    }
}
