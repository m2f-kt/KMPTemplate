package com.m2f.template.app.admin

import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import arrow.core.raise.withError
import arrow.core.raise.zipOrAccumulate
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.FieldError
import com.m2f.template.models.dto.RegisterMemberRequest
import com.m2f.template.models.validation.validateEmail
import com.m2f.template.models.validation.validateName
import com.m2f.template.models.validation.validatePassword
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class RegisterMemberViewModel(
    private val sdk: Sdk,
) : MviViewModel<RegisterMemberIntent, RegisterMemberModel, RegisterMemberMutation, RegisterMemberEvent>(
    initialState = RegisterMemberModel(),
) {

    override fun take(intent: RegisterMemberIntent) {
        viewModelScope.launch {
            when (intent) {
                is RegisterMemberIntent.EmailChanged -> sendMutation(RegisterMemberMutation.SetEmail(intent.email))
                is RegisterMemberIntent.PasswordChanged -> sendMutation(RegisterMemberMutation.SetPassword(intent.password))
                is RegisterMemberIntent.FirstNameChanged -> sendMutation(RegisterMemberMutation.SetFirstName(intent.firstName))
                is RegisterMemberIntent.LastNameChanged -> sendMutation(RegisterMemberMutation.SetLastName(intent.lastName))
                is RegisterMemberIntent.RoleChanged -> sendMutation(RegisterMemberMutation.SetRole(intent.role))
                is RegisterMemberIntent.SubmitRegisterMember -> handleSubmit(intent.groupId)
            }
        }
    }

    private suspend fun handleSubmit(groupId: String) {
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
            ) { firstName, lastName, email, password ->
                RegisterMemberRequest(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    role = current.role,
                )
            }
        }

        validationResult.fold(
            ifLeft = { errors ->
                val fieldErrorMap = errors.associate { it.field to it.message }
                sendMutation(RegisterMemberMutation.SetFieldErrors(fieldErrorMap))
            },
            ifRight = { request ->
                sendMutation(RegisterMemberMutation.SetLoading(true))
                sdk.registerMember(groupId, request).fold(
                    ifLeft = { error ->
                        sendMutation(RegisterMemberMutation.SetServerError(error.message))
                    },
                    ifRight = {
                        sendEvent(RegisterMemberEvent.RegistrationSuccess)
                    },
                )
            },
        )
    }

    override suspend fun reduce(
        model: RegisterMemberModel,
        mutation: RegisterMemberMutation,
    ): RegisterMemberModel = when (mutation) {
        is RegisterMemberMutation.SetEmail -> model.copy(email = mutation.email, fieldErrors = model.fieldErrors - "email")
        is RegisterMemberMutation.SetPassword -> model.copy(password = mutation.password, fieldErrors = model.fieldErrors - "password")
        is RegisterMemberMutation.SetFirstName -> model.copy(firstName = mutation.firstName, fieldErrors = model.fieldErrors - "firstName")
        is RegisterMemberMutation.SetLastName -> model.copy(lastName = mutation.lastName, fieldErrors = model.fieldErrors - "lastName")
        is RegisterMemberMutation.SetRole -> model.copy(role = mutation.role)
        is RegisterMemberMutation.SetLoading -> model.copy(isLoading = mutation.loading, serverError = null)
        is RegisterMemberMutation.SetFieldErrors -> model.copy(fieldErrors = mutation.errors)
        is RegisterMemberMutation.SetServerError -> model.copy(serverError = mutation.error, isLoading = false)
    }
}
