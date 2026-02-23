package com.m2f.template.app.auth

sealed interface LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent
    data class PasswordChanged(val password: String) : LoginIntent
    data class RememberMeChanged(val checked: Boolean) : LoginIntent
    data object SubmitLoginClicked : LoginIntent
    data class SetInvitationToken(val token: String?) : LoginIntent
}
