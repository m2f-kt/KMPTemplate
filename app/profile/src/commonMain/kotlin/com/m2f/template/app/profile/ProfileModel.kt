package com.m2f.template.app.profile

import com.m2f.template.models.UserTier
import com.m2f.template.models.localization.StringKey

data class ProfileModel(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val tier: UserTier = UserTier.Free,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editEmail: String = "",
    val fieldErrors: Map<String, StringKey> = emptyMap(),
    val serverError: StringKey? = null,
    val saveSuccess: Boolean = false,
)
