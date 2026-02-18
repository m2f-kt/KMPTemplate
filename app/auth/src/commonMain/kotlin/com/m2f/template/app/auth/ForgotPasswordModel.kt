package com.m2f.template.app.auth

data class ForgotPasswordModel(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailSent: Boolean = false,
    val emailError: String? = null,
    val serverError: String? = null,
)
