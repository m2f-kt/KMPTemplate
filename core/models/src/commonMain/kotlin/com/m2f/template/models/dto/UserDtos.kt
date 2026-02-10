package com.m2f.template.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: String
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null
)
