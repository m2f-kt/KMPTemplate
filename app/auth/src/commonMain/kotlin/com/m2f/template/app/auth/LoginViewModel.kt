package com.m2f.template.app.auth

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.dto.LoginRequest
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class LoginViewModel(
    private val sdk: Sdk,
) : MviViewModel<LoginIntent, LoginModel, LoginMutation, LoginEvent>(
    initialState = LoginModel()
) {

    override fun take(intent: LoginIntent) {
        viewModelScope.launch {
            when (intent) {
                is LoginIntent.EmailChanged -> sendMutation(LoginMutation.SetEmail(intent.email))
                is LoginIntent.PasswordChanged -> sendMutation(LoginMutation.SetPassword(intent.password))
                is LoginIntent.RememberMeChanged -> sendMutation(LoginMutation.SetRememberMe(intent.checked))
                is LoginIntent.SubmitLoginClicked -> handleLogin()
            }
        }
    }

    private suspend fun handleLogin() {
        val current = model.value

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
            sendMutation(LoginMutation.SetValidationErrors(emailError = emailError, passwordError = passwordError))
            return
        }

        sendMutation(LoginMutation.SetLoading(true))

        sdk.login(LoginRequest(current.email.trim(), current.password), rememberMe = current.rememberMe)
            .fold(
                ifLeft = { error ->
                    sendMutation(LoginMutation.SetServerError(error.message))
                },
                ifRight = {
                    sendEvent(LoginEvent.NavigateToDashboard)
                },
            )
    }

    override suspend fun reduce(model: LoginModel, mutation: LoginMutation): LoginModel =
        when (mutation) {
            is LoginMutation.SetEmail -> model.copy(email = mutation.email, emailError = null)
            is LoginMutation.SetPassword -> model.copy(password = mutation.password, passwordError = null)
            is LoginMutation.SetRememberMe -> model.copy(rememberMe = mutation.checked)
            is LoginMutation.SetLoading -> model.copy(isLoading = mutation.loading, serverError = null)
            is LoginMutation.SetValidationErrors -> model.copy(emailError = mutation.emailError, passwordError = mutation.passwordError)
            is LoginMutation.SetServerError -> model.copy(serverError = mutation.error, isLoading = false)
        }
}
