package com.m2f.template.app.auth

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.dto.ForgotPasswordRequest
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val sdk: Sdk,
) : MviViewModel<ForgotPasswordIntent, ForgotPasswordModel, ForgotPasswordMutation, ForgotPasswordEvent>(
    initialState = ForgotPasswordModel()
) {

    override fun take(intent: ForgotPasswordIntent) {
        viewModelScope.launch {
            when (intent) {
                is ForgotPasswordIntent.EmailChanged -> sendMutation(ForgotPasswordMutation.SetEmail(intent.email))
                is ForgotPasswordIntent.SubmitForgotPasswordClicked -> handleSubmit()
            }
        }
    }

    private suspend fun handleSubmit() {
        val current = model.value

        // Local email validation
        val emailError = when {
            current.email.isBlank() -> "Email must not be blank"
            !current.email.contains("@") || !current.email.contains(".") -> "Email format is invalid"
            else -> null
        }

        if (emailError != null) {
            sendMutation(ForgotPasswordMutation.SetEmailError(emailError))
            return
        }

        sendMutation(ForgotPasswordMutation.SetLoading(true))

        sdk.forgotPassword(ForgotPasswordRequest(current.email.trim()))
            .fold(
                ifLeft = { error ->
                    sendMutation(ForgotPasswordMutation.SetServerError(error.message))
                },
                ifRight = {
                    sendMutation(ForgotPasswordMutation.SetEmailSent)
                },
            )
    }

    override suspend fun reduce(model: ForgotPasswordModel, mutation: ForgotPasswordMutation): ForgotPasswordModel =
        when (mutation) {
            is ForgotPasswordMutation.SetEmail -> model.copy(email = mutation.email, emailError = null)
            is ForgotPasswordMutation.SetLoading -> model.copy(isLoading = mutation.loading, serverError = null)
            is ForgotPasswordMutation.SetEmailSent -> model.copy(isLoading = false, emailSent = true)
            is ForgotPasswordMutation.SetEmailError -> model.copy(emailError = mutation.error)
            is ForgotPasswordMutation.SetServerError -> model.copy(serverError = mutation.error, isLoading = false)
        }
}
