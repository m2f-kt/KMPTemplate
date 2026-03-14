package com.m2f.template.app.auth

import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.withError
import arrow.core.raise.zipOrAccumulate
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.FieldError
import com.m2f.template.models.dto.RegisterRequest
import com.m2f.template.models.localization.StringKey
import com.m2f.template.models.validation.validateEmail
import com.m2f.template.models.validation.validateName
import com.m2f.template.models.validation.validatePassword
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val sdk: Sdk,
) : MviViewModel<RegisterIntent, RegisterModel, RegisterMutation, RegisterEvent>(
    initialState = RegisterModel()
) {

    override fun take(intent: RegisterIntent) {
        viewModelScope.launch {
            when (intent) {
                is RegisterIntent.FirstNameChanged -> sendMutation(RegisterMutation.SetFirstName(intent.firstName))
                is RegisterIntent.LastNameChanged -> sendMutation(RegisterMutation.SetLastName(intent.lastName))
                is RegisterIntent.EmailChanged -> sendMutation(RegisterMutation.SetEmail(intent.email))
                is RegisterIntent.PasswordChanged -> sendMutation(RegisterMutation.SetPassword(intent.password))
                is RegisterIntent.ConfirmPasswordChanged -> sendMutation(RegisterMutation.SetConfirmPassword(intent.confirmPassword))
                is RegisterIntent.TermsAcceptedChanged -> sendMutation(RegisterMutation.SetTermsAccepted(intent.accepted))
                is RegisterIntent.SubmitRegisterClicked -> handleRegister()
                is RegisterIntent.SetInvitationToken -> sendMutation(RegisterMutation.SetInvitationToken(intent.token))
                is RegisterIntent.SetInvitationEmail -> {
                    sendMutation(RegisterMutation.SetInvitationEmail(intent.email))
                    if (intent.email != null) sendMutation(RegisterMutation.SetEmail(intent.email))
                }
            }
        }
    }

    private suspend fun handleRegister() {
        val current = model.value

        val validationResult = either {
            zipOrAccumulate(
                {
                    withError({ error: FieldError -> FieldError("firstName", error.message) }) {
                        validateName(current.firstName)
                    }
                },
                {
                    withError({ error: FieldError -> FieldError("lastName", error.message) }) {
                        validateName(current.lastName)
                    }
                },
                { validateEmail(current.email) },
                { validatePassword(current.password) },
                {
                    ensure(current.confirmPassword == current.password) {
                        FieldError("confirmPassword", StringKey.VALIDATION_PASSWORDS_MISMATCH.code)
                    }
                },
                {
                    ensure(current.termsAccepted) {
                        FieldError("terms", StringKey.VALIDATION_TERMS_NOT_ACCEPTED.code)
                    }
                },
            ) { firstName, lastName, email, password, _, _ ->
                RegisterRequest(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    invitationToken = current.invitationToken,
                )
            }
        }

        validationResult.fold(
            ifLeft = { errors ->
                val fieldErrorMap = errors.associate {
                    it.field to (StringKey.fromCode(it.message) ?: StringKey.GENERIC_ERROR)
                }
                sendMutation(RegisterMutation.SetFieldErrors(fieldErrorMap))
            },
            ifRight = { request ->
                sendMutation(RegisterMutation.SetLoading(true))
                sdk.register(request).fold(
                    ifLeft = { error ->
                        val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                        sendMutation(RegisterMutation.SetServerError(key))
                    },
                    ifRight = {
                        val token = current.invitationToken
                        if (!token.isNullOrBlank()) {
                            // Server already accepted invitation during registration.
                            // Use getInvitation (read-only) to retrieve groupId for navigation.
                            // Do NOT call acceptInvitation — it's not idempotent.
                            sdk.getInvitation(token).fold(
                                ifLeft = {
                                    // User IS in the group (server handled it), just navigate to dashboard
                                    navigateWithConsentCheck()
                                },
                                ifRight = { invitation ->
                                    sendEvent(RegisterEvent.NavigateToGroup(invitation.groupId))
                                },
                            )
                        } else {
                            navigateWithConsentCheck()
                        }
                    },
                )
            },
        )
    }

    private suspend fun navigateWithConsentCheck() {
        sdk.getRequiredConsents().fold(
            ifLeft = {
                // If we can't check consents, proceed to dashboard
                sendEvent(RegisterEvent.NavigateToDashboard)
            },
            ifRight = { response ->
                if (response.hasOutdated) {
                    sendEvent(RegisterEvent.NavigateToConsentGate)
                } else {
                    sendEvent(RegisterEvent.NavigateToDashboard)
                }
            },
        )
    }

    override suspend fun reduce(model: RegisterModel, mutation: RegisterMutation): RegisterModel =
        when (mutation) {
            is RegisterMutation.SetFirstName -> model.copy(firstName = mutation.firstName, fieldErrors = model.fieldErrors - "firstName")
            is RegisterMutation.SetLastName -> model.copy(lastName = mutation.lastName, fieldErrors = model.fieldErrors - "lastName")
            is RegisterMutation.SetEmail -> model.copy(email = mutation.email, fieldErrors = model.fieldErrors - "email")
            is RegisterMutation.SetPassword -> model.copy(password = mutation.password, fieldErrors = model.fieldErrors - "password")
            is RegisterMutation.SetConfirmPassword -> model.copy(confirmPassword = mutation.confirmPassword, fieldErrors = model.fieldErrors - "confirmPassword")
            is RegisterMutation.SetTermsAccepted -> model.copy(termsAccepted = mutation.accepted, fieldErrors = model.fieldErrors - "terms")
            is RegisterMutation.SetLoading -> model.copy(isLoading = mutation.loading, serverError = null)
            is RegisterMutation.SetFieldErrors -> model.copy(fieldErrors = mutation.errors)
            is RegisterMutation.SetServerError -> model.copy(serverError = mutation.error, isLoading = false)
            is RegisterMutation.SetInvitationToken -> model.copy(invitationToken = mutation.token)
            is RegisterMutation.SetInvitationEmail -> model.copy(invitationEmail = mutation.email)
        }
}
