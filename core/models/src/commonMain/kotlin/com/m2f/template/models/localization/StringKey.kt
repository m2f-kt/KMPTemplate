package com.m2f.template.models.localization

import kotlinx.serialization.Serializable

/**
 * Shared string key enum bridging error codes and localization keys.
 *
 * Each entry maps to a string resource in `strings.xml` (prefixed with `error_`)
 * and to an [com.m2f.template.models.AppError.code] value.
 *
 * Used by ViewModels to emit localizable error/validation messages without
 * depending on Compose resources directly.
 */
@Serializable
enum class StringKey(val code: String) {

    // Auth errors
    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS"),
    AUTH_TOKEN_EXPIRED("AUTH_TOKEN_EXPIRED"),
    AUTH_TOKEN_INVALID("AUTH_TOKEN_INVALID"),
    AUTH_UNAUTHORIZED("AUTH_UNAUTHORIZED"),
    AUTH_USER_ALREADY_EXISTS("AUTH_USER_ALREADY_EXISTS"),

    // Validation errors (from AppError)
    VALIDATION_INVALID_FIELD("VALIDATION_INVALID_FIELD"),
    VALIDATION_INVALID_INPUT("VALIDATION_INVALID_INPUT"),
    VALIDATION_MISSING_FIELD("VALIDATION_MISSING_FIELD"),

    // User errors
    USER_NOT_FOUND("USER_NOT_FOUND"),
    USER_FORBIDDEN("USER_FORBIDDEN"),

    // Server errors
    SERVER_INTERNAL_ERROR("SERVER_INTERNAL_ERROR"),
    SERVER_SERVICE_UNAVAILABLE("SERVER_SERVICE_UNAVAILABLE"),

    // Client errors
    CLIENT_NETWORK_ERROR("CLIENT_NETWORK_ERROR"),
    CLIENT_TIMEOUT("CLIENT_TIMEOUT"),
    CLIENT_UNKNOWN_ERROR("CLIENT_UNKNOWN_ERROR"),

    // Group errors
    GROUP_NOT_FOUND("GROUP_NOT_FOUND"),
    GROUP_FORBIDDEN("GROUP_FORBIDDEN"),
    GROUP_ALREADY_EXISTS("GROUP_ALREADY_EXISTS"),
    GROUP_MEMBER_ALREADY_EXISTS("GROUP_MEMBER_ALREADY_EXISTS"),

    // AI errors
    AI_AGENT_FAILED("AI_AGENT_FAILED"),
    AI_AGENT_NOT_FOUND("AI_AGENT_NOT_FOUND"),
    AI_CONVERSATION_NOT_FOUND("AI_CONVERSATION_NOT_FOUND"),
    AI_PROVIDER_UNAVAILABLE("AI_PROVIDER_UNAVAILABLE"),

    // Validation messages (from ValidationSupport.kt and inline ViewModel validation)
    VALIDATION_EMAIL_BLANK("VALIDATION_EMAIL_BLANK"),
    VALIDATION_EMAIL_INVALID("VALIDATION_EMAIL_INVALID"),
    VALIDATION_PASSWORD_TOO_SHORT("VALIDATION_PASSWORD_TOO_SHORT"),
    VALIDATION_PASSWORD_BLANK("VALIDATION_PASSWORD_BLANK"),
    VALIDATION_NAME_BLANK("VALIDATION_NAME_BLANK"),
    VALIDATION_NAME_LENGTH("VALIDATION_NAME_LENGTH"),
    VALIDATION_FIELD_REQUIRED("VALIDATION_FIELD_REQUIRED"),
    VALIDATION_PASSWORDS_MISMATCH("VALIDATION_PASSWORDS_MISMATCH"),
    VALIDATION_TERMS_NOT_ACCEPTED("VALIDATION_TERMS_NOT_ACCEPTED"),

    // Generic fallback
    GENERIC_ERROR("GENERIC_ERROR"),
    ;

    companion object {
        private val byCode: Map<String, StringKey> = entries.associateBy { it.code }

        /**
         * Looks up a [StringKey] by its [code] value.
         * Returns `null` if no matching key exists (e.g. for dynamic server-mapped codes).
         */
        fun fromCode(code: String): StringKey? = byCode[code]
    }
}
