package com.m2f.template.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m2f.template.models.dto.ForgotPasswordRequest
import com.m2f.template.sdk.api.AuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authApi: AuthApi,
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun submit() {
        val current = _state.value

        // Local email validation
        val emailError = when {
            current.email.isBlank() -> "Email must not be blank"
            !current.email.contains("@") || !current.email.contains(".") -> "Email format is invalid"
            else -> null
        }

        if (emailError != null) {
            _state.update { it.copy(emailError = emailError) }
            return
        }

        _state.update { it.copy(isLoading = true, serverError = null) }

        viewModelScope.launch {
            authApi.forgotPassword(ForgotPasswordRequest(current.email.trim()))
                .fold(
                    ifLeft = { error ->
                        _state.update { it.copy(serverError = error.message, isLoading = false) }
                    },
                    ifRight = {
                        _state.update { it.copy(emailSent = true, isLoading = false) }
                    },
                )
        }
    }
}
