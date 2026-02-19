package com.m2f.template.app.auth

import com.m2f.template.models.localization.StringKey

data class LoginModel(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: StringKey? = null,
    val passwordError: StringKey? = null,
    val serverError: StringKey? = null,
)
