package com.m2f.template.app.auth

sealed interface LoginMutation {
    data class SetEmail(val email: String) : LoginMutation
    data class SetPassword(val password: String) : LoginMutation
    data class SetRememberMe(val checked: Boolean) : LoginMutation
    data class SetLoading(val loading: Boolean) : LoginMutation
    data class SetValidationErrors(val emailError: String?, val passwordError: String?) : LoginMutation
    data class SetServerError(val error: String?) : LoginMutation
}
