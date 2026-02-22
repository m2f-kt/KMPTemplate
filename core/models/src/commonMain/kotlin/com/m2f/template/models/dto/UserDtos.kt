package com.m2f.template.models.dto

import com.m2f.template.models.UserRole
import com.m2f.template.models.UserTier
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val avatarUrl: String? = null,
)

/**
 * Extension property that maps the [UserResponse.role] sealed type
 * to a type-safe [UserTier] sealed class instance via the role's string value.
 */
val UserResponse.tier: UserTier get() = UserTier.fromString(role.value)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
)
