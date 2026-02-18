package com.m2f.template.app.auth

sealed interface RegisterMutation {
    data class SetFirstName(val firstName: String) : RegisterMutation
    data class SetLastName(val lastName: String) : RegisterMutation
    data class SetEmail(val email: String) : RegisterMutation
    data class SetPassword(val password: String) : RegisterMutation
    data class SetConfirmPassword(val confirmPassword: String) : RegisterMutation
    data class SetTermsAccepted(val accepted: Boolean) : RegisterMutation
    data class SetLoading(val loading: Boolean) : RegisterMutation
    data class SetFieldErrors(val errors: Map<String, String>) : RegisterMutation
    data class SetServerError(val error: String?) : RegisterMutation
}
