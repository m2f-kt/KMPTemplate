package com.m2f.template.models.validation

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import com.m2f.template.models.FieldError
import com.m2f.template.models.localization.StringKey

/**
 * Validates an email address format.
 * Used in accumulated validation contexts.
 */
context(raise: Raise<FieldError>)
fun validateEmail(email: String): String {
    raise.ensure(email.isNotBlank()) { FieldError("email", StringKey.VALIDATION_EMAIL_BLANK.code) }
    raise.ensure(email.contains("@") && email.contains(".")) {
        FieldError("email", StringKey.VALIDATION_EMAIL_INVALID.code)
    }
    return email.trim().lowercase()
}

/**
 * Validates a password meets minimum requirements.
 */
context(raise: Raise<FieldError>)
fun validatePassword(password: String): String {
    raise.ensure(password.length >= 8) {
        FieldError("password", StringKey.VALIDATION_PASSWORD_TOO_SHORT.code)
    }
    return password
}

/**
 * Validates a name field.
 */
context(raise: Raise<FieldError>)
fun validateName(name: String): String {
    raise.ensure(name.isNotBlank()) { FieldError("name", StringKey.VALIDATION_NAME_BLANK.code) }
    raise.ensure(name.length in 2..100) {
        FieldError("name", StringKey.VALIDATION_NAME_LENGTH.code)
    }
    return name.trim()
}

/**
 * Validates a non-empty required string field.
 */
context(raise: Raise<FieldError>)
fun validateRequired(value: String, fieldName: String): String {
    raise.ensure(value.isNotBlank()) { FieldError(fieldName, StringKey.VALIDATION_FIELD_REQUIRED.code) }
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
