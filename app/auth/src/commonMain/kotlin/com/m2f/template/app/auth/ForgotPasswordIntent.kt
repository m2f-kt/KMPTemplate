package com.m2f.template.app.auth

sealed interface ForgotPasswordIntent {
    data class EmailChanged(val email: String) : ForgotPasswordIntent
    data object SubmitForgotPasswordClicked : ForgotPasswordIntent
}
