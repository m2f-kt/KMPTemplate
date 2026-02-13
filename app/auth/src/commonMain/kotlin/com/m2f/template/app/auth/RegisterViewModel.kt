package com.m2f.template.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.withError
import arrow.core.raise.zipOrAccumulate
import com.m2f.template.models.FieldError
import com.m2f.template.models.dto.RegisterRequest
import com.m2f.template.models.validation.validateEmail
import com.m2f.template.models.validation.validateName
import com.m2f.template.models.validation.validatePassword
import com.m2f.template.sdk.api.AuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authApi: AuthApi,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    fun onFirstNameChange(firstName: String) {
        _state.update {
            it.copy(
                firstName = firstName,
                fieldErrors = it.fieldErrors - "firstName",
            )
        }
    }

    fun onLastNameChange(lastName: String) {
        _state.update {
            it.copy(
                lastName = lastName,
                fieldErrors = it.fieldErrors - "lastName",
            )
        }
    }

    fun onEmailChange(email: String) {
        _state.update {
            it.copy(
                email = email,
                fieldErrors = it.fieldErrors - "email",
            )
        }
    }

    fun onPasswordChange(password: String) {
        _state.update {
            it.copy(
                password = password,
                fieldErrors = it.fieldErrors - "password",
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update {
            it.copy(
                confirmPassword = confirmPassword,
                fieldErrors = it.fieldErrors - "confirmPassword",
            )
        }
    }

    fun onTermsAcceptedChange(accepted: Boolean) {
        _state.update {
            it.copy(
                termsAccepted = accepted,
                fieldErrors = it.fieldErrors - "terms",
            )
        }
    }

    fun register() {
        val current = _state.value

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
                        FieldError("confirmPassword", "Passwords do not match")
                    }
                },
                {
                    ensure(current.termsAccepted) {
                        FieldError("terms", "You must accept the terms")
                    }
                },
            ) { firstName, lastName, email, password, _, _ ->
                RegisterRequest(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                )
            }
        }

        validationResult.fold(
            ifLeft = { errors ->
                val fieldErrorMap = errors.associate { it.field to it.message }
                _state.update { it.copy(fieldErrors = fieldErrorMap) }
            },
            ifRight = { request ->
                _state.update { it.copy(isLoading = true, serverError = null, fieldErrors = emptyMap()) }
                viewModelScope.launch {
                    authApi.register(request).fold(
                        ifLeft = { error ->
                            _state.update { it.copy(serverError = error.message, isLoading = false) }
                        },
                        ifRight = {
                            _state.update { it.copy(registerSuccess = true, isLoading = false) }
                        },
                    )
                }
            },
        )
    }
}
