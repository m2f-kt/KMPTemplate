package com.m2f.template.app.auth

sealed interface RegisterIntent {
    data class FirstNameChanged(val firstName: String) : RegisterIntent
    data class LastNameChanged(val lastName: String) : RegisterIntent
    data class EmailChanged(val email: String) : RegisterIntent
    data class PasswordChanged(val password: String) : RegisterIntent
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterIntent
    data class TermsAcceptedChanged(val accepted: Boolean) : RegisterIntent
    data object SubmitRegisterClicked : RegisterIntent
}
