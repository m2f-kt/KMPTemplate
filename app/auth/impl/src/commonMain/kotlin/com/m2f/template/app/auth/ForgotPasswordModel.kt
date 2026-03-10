package com.m2f.template.app.auth

import com.m2f.template.models.localization.StringKey

data class ForgotPasswordModel(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailSent: Boolean = false,
    val emailError: StringKey? = null,
    val serverError: StringKey? = null,
)
