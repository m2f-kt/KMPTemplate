package com.m2f.template.app.profile

import com.m2f.template.models.UserTier

sealed interface ProfileMutation {
    data class SetProfile(val userId: String, val email: String, val name: String, val tier: UserTier) : ProfileMutation
    data class SetLoading(val loading: Boolean) : ProfileMutation
    data object StartEdit : ProfileMutation
    data object CancelEdit : ProfileMutation
    data class SetEditName(val name: String) : ProfileMutation
    data class SetEditEmail(val email: String) : ProfileMutation
    data class SetFieldErrors(val errors: Map<String, String>) : ProfileMutation
    data class SetServerError(val error: String?) : ProfileMutation
    data object SetSaveSuccess : ProfileMutation
}
