package com.m2f.template.app.profile

import com.m2f.template.models.UserTier

/**
 * UI state for the profile screen.
 *
 * Tracks the loaded user data, edit mode fields, validation errors,
 * and side-effect flags (save success, logout trigger).
 */
data class ProfileState(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val tier: UserTier = UserTier.Free,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editEmail: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val serverError: String? = null,
    val saveSuccess: Boolean = false,
    val logoutTriggered: Boolean = false,
)
