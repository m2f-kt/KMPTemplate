package com.m2f.template.models.dto

import com.m2f.template.models.UserTier
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
)

/**
 * Extension property that maps the wire-format [UserResponse.role] string
 * to a type-safe [UserTier] sealed class instance.
 */
val UserResponse.tier: UserTier get() = UserTier.fromString(role)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
)
