package com.m2f.template.app.auth

sealed interface ForgotPasswordMutation {
    data class SetEmail(val email: String) : ForgotPasswordMutation
    data class SetLoading(val loading: Boolean) : ForgotPasswordMutation
    data object SetEmailSent : ForgotPasswordMutation
    data class SetEmailError(val error: String?) : ForgotPasswordMutation
    data class SetServerError(val error: String?) : ForgotPasswordMutation
}
