package com.m2f.template.app.auth

import com.m2f.template.models.localization.StringKey

sealed interface ForgotPasswordMutation {
    data class SetEmail(val email: String) : ForgotPasswordMutation
    data class SetLoading(val loading: Boolean) : ForgotPasswordMutation
    data object SetEmailSent : ForgotPasswordMutation
    data class SetEmailError(val error: StringKey?) : ForgotPasswordMutation
    data class SetServerError(val error: StringKey?) : ForgotPasswordMutation
}
