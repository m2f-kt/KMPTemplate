package com.m2f.template.app.auth

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.dto.AcceptInvitationRequest
import com.m2f.template.models.dto.LoginRequest
import com.m2f.template.models.localization.StringKey
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
                is LoginIntent.SetInvitationToken -> sendMutation(LoginMutation.SetInvitationToken(intent.token))
            }
        }
    }

    private suspend fun handleLogin() {
        val current = model.value

        // Local validation
        val emailError = when {
            current.email.isBlank() -> StringKey.VALIDATION_EMAIL_BLANK
            !current.email.contains("@") || !current.email.contains(".") -> StringKey.VALIDATION_EMAIL_INVALID
            else -> null
        }
        val passwordError = when {
            current.password.isBlank() -> StringKey.VALIDATION_PASSWORD_BLANK
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
                    val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                    sendMutation(LoginMutation.SetServerError(key))
                },
                ifRight = {
                    handlePostLogin()
                },
            )
    }

    private suspend fun handlePostLogin() {
        val token = model.value.invitationToken
        if (token != null) {
            sendMutation(LoginMutation.SetAcceptingInvitation(true))
            sdk.acceptInvitation(AcceptInvitationRequest(token))
                .fold(
                    ifLeft = {
                        // Log error but still navigate - user is logged in
                        sendMutation(LoginMutation.SetAcceptingInvitation(false))
                        sendEvent(LoginEvent.NavigateToDashboard)
                    },
                    ifRight = { response ->
                        sendMutation(LoginMutation.SetAcceptingInvitation(false))
                        sendEvent(LoginEvent.NavigateToGroup(response.groupId))
                    },
                )
        } else {
            sendEvent(LoginEvent.NavigateToDashboard)
        }
    }

    override suspend fun reduce(model: LoginModel, mutation: LoginMutation): LoginModel =
        when (mutation) {
            is LoginMutation.SetEmail -> model.copy(email = mutation.email, emailError = null)
            is LoginMutation.SetPassword -> model.copy(password = mutation.password, passwordError = null)
            is LoginMutation.SetRememberMe -> model.copy(rememberMe = mutation.checked)
            is LoginMutation.SetLoading -> model.copy(isLoading = mutation.loading, serverError = null)
            is LoginMutation.SetValidationErrors -> model.copy(emailError = mutation.emailError, passwordError = mutation.passwordError)
            is LoginMutation.SetServerError -> model.copy(serverError = mutation.error, isLoading = false)
            is LoginMutation.SetInvitationToken -> model.copy(invitationToken = mutation.token)
            is LoginMutation.SetAcceptingInvitation -> model.copy(isAcceptingInvitation = mutation.accepting)
        }
}
