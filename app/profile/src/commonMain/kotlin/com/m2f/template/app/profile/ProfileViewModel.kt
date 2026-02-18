package com.m2f.template.app.profile

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.dto.UpdateProfileRequest
import com.m2f.template.models.dto.tier
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val sdk: Sdk,
) : MviViewModel<ProfileIntent, ProfileModel, ProfileMutation, ProfileEvent>(
    initialState = ProfileModel(),
    modelSharingStarted = SharingStarted.Eagerly,
) {

    init {
        // Eagerly initialize model and event collectors before dispatching LoadProfile,
        // otherwise the lazy stateIn/shareIn would miss mutations emitted during init.
        model
        event
        take(ProfileIntent.LoadProfile)
    }

    override fun take(intent: ProfileIntent) {
        viewModelScope.launch {
            when (intent) {
                is ProfileIntent.LoadProfile -> handleLoadProfile()
                is ProfileIntent.StartEditing -> sendMutation(ProfileMutation.StartEdit)
                is ProfileIntent.CancelEditing -> sendMutation(ProfileMutation.CancelEdit)
                is ProfileIntent.EditNameChanged -> sendMutation(ProfileMutation.SetEditName(intent.name))
                is ProfileIntent.EditEmailChanged -> sendMutation(ProfileMutation.SetEditEmail(intent.email))
                is ProfileIntent.SaveProfileClicked -> handleSaveProfile()
                is ProfileIntent.LogoutClicked -> handleLogout()
            }
        }
    }

    private suspend fun handleLoadProfile() {
        sendMutation(ProfileMutation.SetLoading(true))
        sdk.getProfile().fold(
            ifLeft = { error ->
                sendMutation(ProfileMutation.SetServerError(error.message))
                sendMutation(ProfileMutation.SetLoading(false))
            },
            ifRight = { user ->
                sendMutation(ProfileMutation.SetProfile(user.id, user.email, user.name, user.tier))
            },
        )
    }

    private suspend fun handleSaveProfile() {
        val current = model.value

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
            sendMutation(ProfileMutation.SetFieldErrors(errors))
            return
        }

        sendMutation(ProfileMutation.SetLoading(true))
        sdk.updateProfile(
            UpdateProfileRequest(
                name = current.editName.trim(),
                email = current.editEmail.trim(),
            ),
        ).fold(
            ifLeft = { error ->
                sendMutation(ProfileMutation.SetServerError(error.message))
                sendMutation(ProfileMutation.SetLoading(false))
            },
            ifRight = { user ->
                sendMutation(ProfileMutation.SetProfile(user.id, user.email, user.name, user.tier))
                sendMutation(ProfileMutation.SetSaveSuccess)
            },
        )
    }

    private suspend fun handleLogout() {
        sdk.logout()
        sendEvent(ProfileEvent.NavigateToLogin)
    }

    override suspend fun reduce(model: ProfileModel, mutation: ProfileMutation): ProfileModel =
        when (mutation) {
            is ProfileMutation.SetProfile -> model.copy(
                userId = mutation.userId,
                email = mutation.email,
                name = mutation.name,
                tier = mutation.tier,
                isLoading = false,
            )
            is ProfileMutation.SetLoading -> model.copy(isLoading = mutation.loading)
            is ProfileMutation.StartEdit -> model.copy(
                isEditing = true,
                editName = model.name,
                editEmail = model.email,
                saveSuccess = false,
                fieldErrors = emptyMap(),
            )
            is ProfileMutation.CancelEdit -> model.copy(
                isEditing = false,
                editName = "",
                editEmail = "",
                fieldErrors = emptyMap(),
            )
            is ProfileMutation.SetEditName -> model.copy(
                editName = mutation.name,
                fieldErrors = model.fieldErrors - "name",
            )
            is ProfileMutation.SetEditEmail -> model.copy(
                editEmail = mutation.email,
                fieldErrors = model.fieldErrors - "email",
            )
            is ProfileMutation.SetFieldErrors -> model.copy(fieldErrors = mutation.errors)
            is ProfileMutation.SetServerError -> model.copy(serverError = mutation.error, isLoading = false)
            is ProfileMutation.SetSaveSuccess -> model.copy(
                isEditing = false,
                saveSuccess = true,
                isLoading = false,
            )
        }
}
