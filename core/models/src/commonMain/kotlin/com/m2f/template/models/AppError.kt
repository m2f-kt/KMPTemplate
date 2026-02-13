package com.m2f.template.models

import kotlinx.serialization.Serializable

/**
 * Unified error hierarchy for the application.
 * Used by both server (for API responses) and client (for typed error handling).
 *
 * Error codes follow the DOMAIN_SPECIFIC_ERROR format and are designed
 * to serve as future localization keys.
 */
@Serializable
sealed class AppError {
    /** Structured string code identifying this error type (e.g., AUTH_INVALID_CREDENTIALS). */
    abstract val code: String
    /** Human-readable error message. */
    abstract val message: String

    @Serializable
    sealed class Auth : AppError() {
        @Serializable
        data class InvalidCredentials(
            override val code: String = "AUTH_INVALID_CREDENTIALS",
            override val message: String = "Email or password is incorrect"
        ) : Auth()

        @Serializable
        data class TokenExpired(
            override val code: String = "AUTH_TOKEN_EXPIRED",
            override val message: String = "Authentication token has expired"
        ) : Auth()

        @Serializable
        data class TokenInvalid(
            override val code: String = "AUTH_TOKEN_INVALID",
            override val message: String = "Authentication token is invalid"
        ) : Auth()

        @Serializable
        data class Unauthorized(
            override val code: String = "AUTH_UNAUTHORIZED",
            override val message: String = "Authentication required"
        ) : Auth()

        @Serializable
        data class UserAlreadyExists(
            override val code: String = "AUTH_USER_ALREADY_EXISTS",
            override val message: String = "A user with this email already exists"
        ) : Auth()
    }

    @Serializable
    sealed class Validation : AppError() {
        @Serializable
        data class InvalidField(
            val field: String,
            val errors: List<String>,
            override val code: String = "VALIDATION_INVALID_FIELD",
            override val message: String = "Field validation failed: $field"
        ) : Validation()

        @Serializable
        data class InvalidInput(
            val fieldErrors: List<FieldError>,
            override val code: String = "VALIDATION_INVALID_INPUT",
            override val message: String = "One or more fields have validation errors"
        ) : Validation()

        @Serializable
        data class MissingField(
            val field: String,
            override val code: String = "VALIDATION_MISSING_FIELD",
            override val message: String = "Required field is missing: $field"
        ) : Validation()
    }

    @Serializable
    sealed class User : AppError() {
        @Serializable
        data class NotFound(
            override val code: String = "USER_NOT_FOUND",
            override val message: String = "User not found"
        ) : User()

        @Serializable
        data class Forbidden(
            override val code: String = "USER_FORBIDDEN",
            override val message: String = "You do not have permission to access this resource"
        ) : User()
    }

    @Serializable
    sealed class Server : AppError() {
        @Serializable
        data class Internal(
            override val code: String = "SERVER_INTERNAL_ERROR",
            override val message: String = "An unexpected error occurred"
        ) : Server()

        @Serializable
        data class ServiceUnavailable(
            override val code: String = "SERVER_SERVICE_UNAVAILABLE",
            override val message: String = "Service is temporarily unavailable"
        ) : Server()
    }

    @Serializable
    sealed class Client : AppError() {
        @Serializable
        data class Network(
            override val code: String = "CLIENT_NETWORK_ERROR",
            override val message: String = "Network connection failed"
        ) : Client()

        @Serializable
        data class Timeout(
            override val code: String = "CLIENT_TIMEOUT",
            override val message: String = "Request timed out"
        ) : Client()

        @Serializable
        data class Unknown(
            val detail: String? = null,
            override val code: String = "CLIENT_UNKNOWN_ERROR",
            override val message: String = detail ?: "An unknown error occurred"
        ) : Client()

        /** Server error received via API -- preserves server's code and message. */
        @Serializable
        data class ServerMapped(
            override val code: String,
            override val message: String
        ) : Client()
    }

    @Serializable
    sealed class AI : AppError() {
        @Serializable
        data class AgentFailed(
            val detail: String? = null,
            override val code: String = "AI_AGENT_FAILED",
            override val message: String = detail ?: "Agent execution failed"
        ) : AI()

        @Serializable
        data class AgentNotFound(
            override val code: String = "AI_AGENT_NOT_FOUND",
            override val message: String = "The requested agent was not found"
        ) : AI()

        @Serializable
        data class ConversationNotFound(
            override val code: String = "AI_CONVERSATION_NOT_FOUND",
            override val message: String = "Conversation not found or does not belong to this user"
        ) : AI()

        @Serializable
        data class ProviderUnavailable(
            override val code: String = "AI_PROVIDER_UNAVAILABLE",
            override val message: String = "AI provider is not available or not configured"
        ) : AI()
    }
}

/**
 * A single field-level validation error used in accumulated validation.
 */
@Serializable
data class FieldError(
    val field: String,
    val message: String
)
