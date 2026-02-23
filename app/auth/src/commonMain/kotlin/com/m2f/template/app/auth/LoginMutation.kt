package com.m2f.template.app.auth

import com.m2f.template.models.localization.StringKey

sealed interface LoginMutation {
    data class SetEmail(val email: String) : LoginMutation
    data class SetPassword(val password: String) : LoginMutation
    data class SetRememberMe(val checked: Boolean) : LoginMutation
    data class SetLoading(val loading: Boolean) : LoginMutation
    data class SetValidationErrors(val emailError: StringKey?, val passwordError: StringKey?) : LoginMutation
    data class SetServerError(val error: StringKey?) : LoginMutation
    data class SetInvitationToken(val token: String?) : LoginMutation
    data class SetAcceptingInvitation(val accepting: Boolean) : LoginMutation
}
