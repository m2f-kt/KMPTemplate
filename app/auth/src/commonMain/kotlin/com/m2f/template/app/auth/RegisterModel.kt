package com.m2f.template.app.auth

import com.m2f.template.models.localization.StringKey

data class RegisterModel(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val fieldErrors: Map<String, StringKey> = emptyMap(),
    val serverError: StringKey? = null,
    val invitationToken: String? = null,
    val invitationEmail: String? = null,
)
