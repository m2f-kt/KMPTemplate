package com.m2f.core.config.server

import arrow.core.NonEmptyList
import com.m2f.core.config.server.localization.ServerStrings
import com.m2f.template.models.AppError
import com.m2f.template.models.FieldError
import io.ktor.server.routing.RoutingContext

interface DomainError {
    /** Convert this server-side error to the shared AppError for API responses. */
    fun toAppError(): AppError

    context(routingContext: RoutingContext)
    suspend fun respond()
}

data class UnexpectedError(val message: String = "An Unexpected error has occurred") : DomainError {
    override fun toAppError(): AppError = AppError.Server.Internal(message = message)

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unexpected(error.code, message)
    }
}

data class MappingError(val message: String) : DomainError {
    override fun toAppError(): AppError = AppError.Validation.InvalidField(
        field = "body",
        errors = listOf("Error mapping the request: $message")
    )

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unprocessable(error.code, message)
    }
}

sealed interface ValidationError : DomainError

data class IncorrectInput(val errors: NonEmptyList<InvalidField>) : ValidationError {
    override fun toAppError(): AppError = AppError.Validation.InvalidInput(
        fieldErrors = errors.map { field ->
            FieldError(field = field.field, message = field.errors.joinToString("; "))
        }
    )

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val formattedErrors = errors.flatMap { field ->
            field.errors.map { error -> "${field.field}: $error" }
        }
        val appError = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(appError.code, locale)
        routingContext.unprocessable(appError.code, message, formattedErrors)
    }
}

data class MissingParameter(val name: String) : ValidationError {
    override fun toAppError(): AppError = AppError.Validation.MissingField(field = name)

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unprocessable(error.code, message)
    }
}

data class InvalidParameter(val name: String, val value: String, val expectedType: String) : ValidationError {
    override fun toAppError(): AppError = AppError.Validation.InvalidField(
        field = name,
        errors = listOf("'$value' cannot be converted to $expectedType")
    )

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unprocessable(error.code, message)
    }
}

data object InvalidContent : ValidationError {
    override fun toAppError(): AppError = AppError.Validation.InvalidField(
        field = "content",
        errors = listOf("Invalid content received can not be converted")
    )

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unprocessable(error.code, message)
    }
}

data class Unauthorized(val message: String = "Authentication required") : DomainError {
    override fun toAppError(): AppError = AppError.Auth.Unauthorized(message = message)

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unauthorized(error.code, message)
    }
}
