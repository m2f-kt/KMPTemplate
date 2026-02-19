package com.m2f.template.app.admin

import com.m2f.template.models.GroupRole

data class RegisterMemberModel(
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: GroupRole = GroupRole.Member,
    val isLoading: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val serverError: String? = null,
)
