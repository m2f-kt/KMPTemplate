package com.m2f.template.models.validation

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import com.m2f.template.models.FieldError

/**
 * Validates an email address format.
 * Used in accumulated validation contexts.
 */
context(raise: Raise<FieldError>)
fun validateEmail(email: String): String {
    raise.ensure(email.isNotBlank()) { FieldError("email", "Email must not be blank") }
    raise.ensure(email.contains("@") && email.contains(".")) {
        FieldError("email", "Email format is invalid")
    }
    return email.trim().lowercase()
}

/**
 * Validates a password meets minimum requirements.
 */
context(raise: Raise<FieldError>)
fun validatePassword(password: String): String {
    raise.ensure(password.length >= 8) {
        FieldError("password", "Password must be at least 8 characters")
    }
    return password
}

/**
 * Validates a name field.
 */
context(raise: Raise<FieldError>)
fun validateName(name: String): String {
    raise.ensure(name.isNotBlank()) { FieldError("name", "Name must not be blank") }
    raise.ensure(name.length in 2..100) {
        FieldError("name", "Name must be between 2 and 100 characters")
    }
    return name.trim()
}

/**
 * Validates a non-empty required string field.
 */
context(raise: Raise<FieldError>)
fun validateRequired(value: String, fieldName: String): String {
    raise.ensure(value.isNotBlank()) { FieldError(fieldName, "$fieldName must not be blank") }
    return value.trim()
}

/**
 * Example: Validate a registration request using accumulated errors.
 * Demonstrates the pattern -- real validation will be in the auth feature module.
 *
 * Usage with Raise<NonEmptyList<FieldError>>:
 * ```
 * zipOrAccumulate(
 *     { validateEmail(email) },
 *     { validatePassword(password) },
 *     { validateName(name) }
 * ) { validEmail, validPassword, validName ->
 *     // All valid -- proceed
 *     RegisterRequest(validEmail, validPassword, validName)
 * }
 * ```
 */
