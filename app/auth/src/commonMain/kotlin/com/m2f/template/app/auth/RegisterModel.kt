package com.m2f.template.app.auth

data class RegisterModel(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val serverError: String? = null,
)
