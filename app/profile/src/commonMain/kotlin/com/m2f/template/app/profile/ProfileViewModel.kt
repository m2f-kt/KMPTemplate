package com.m2f.template.app.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m2f.template.models.dto.UpdateProfileRequest
import com.m2f.template.models.dto.tier
import com.m2f.template.sdk.api.AuthApi
import com.m2f.template.sdk.api.UserApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the profile screen.
 *
 * Loads user data via [UserApi.getProfile], maps role to [com.m2f.template.models.UserTier],
 * supports inline editing of name and email via [UserApi.updateProfile],
 * and handles logout via [AuthApi.logout].
 */
class ProfileViewModel(
    private val userApi: UserApi,
    private val authApi: AuthApi,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, serverError = null) }
            userApi.getProfile().fold(
                ifLeft = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            serverError = error.message,
                        )
                    }
                },
                ifRight = { user ->
                    _state.update {
                        it.copy(
                            userId = user.id,
                            email = user.email,
                            name = user.name,
                            tier = user.tier,
                            isLoading = false,
                        )
                    }
                },
            )
        }
    }

    fun startEditing() {
        _state.update {
            it.copy(
                isEditing = true,
                editName = it.name,
                editEmail = it.email,
                fieldErrors = emptyMap(),
                serverError = null,
                saveSuccess = false,
            )
        }
    }

    fun cancelEditing() {
        _state.update {
            it.copy(
                isEditing = false,
                editName = "",
                editEmail = "",
                fieldErrors = emptyMap(),
            )
        }
    }

    fun onEditNameChange(name: String) {
        _state.update {
            it.copy(
                editName = name,
                fieldErrors = it.fieldErrors - "name",
            )
        }
    }

    fun onEditEmailChange(email: String) {
        _state.update {
            it.copy(
                editEmail = email,
                fieldErrors = it.fieldErrors - "email",
            )
        }
    }

    fun saveProfile() {
        val current = _state.value
        val errors = mutableMapOf<String, String>()
        if (current.editName.isBlank()) {
            errors["name"] = "Name is required"
        }
        if (current.editEmail.isBlank()) {
            errors["email"] = "Email is required"
        } else if (!current.editEmail.contains("@")) {
            errors["email"] = "Invalid email format"
        }
        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(serverError = null) }
            userApi.updateProfile(
                UpdateProfileRequest(
                    name = current.editName.trim(),
                    email = current.editEmail.trim(),
                ),
            ).fold(
                ifLeft = { error ->
                    _state.update { it.copy(serverError = error.message) }
                },
                ifRight = { user ->
                    _state.update {
                        it.copy(
                            name = user.name,
                            email = user.email,
                            isEditing = false,
                            editName = "",
                            editEmail = "",
                            saveSuccess = true,
                        )
                    }
                },
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authApi.logout()
            _state.update { it.copy(logoutTriggered = true) }
        }
    }
}
