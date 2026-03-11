package com.m2f.server.auth.contract.errors

import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.forbidden
import com.m2f.core.config.server.notFound
import com.m2f.core.config.server.preferredLanguage
import com.m2f.core.config.server.unauthorized
import com.m2f.core.config.server.unprocessable
import com.m2f.core.config.server.localization.ServerStrings
import com.m2f.template.models.AppError
import io.ktor.server.routing.RoutingContext

data class InvalidCredentials(
    val detail: String = "Email or password is incorrect",
) : DomainError {
    override fun toAppError(): AppError = AppError.Auth.InvalidCredentials()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unauthorized(error.code, message)
    }
}

data class UserAlreadyExists(
    val detail: String = "A user with this email already exists",
) : DomainError {
    override fun toAppError(): AppError = AppError.Auth.UserAlreadyExists()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unprocessable(error.code, message)
    }
}

data class TokenExpired(
    val detail: String = "Authentication token has expired",
) : DomainError {
    override fun toAppError(): AppError = AppError.Auth.TokenExpired()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unauthorized(error.code, message)
    }
}

data class TokenInvalid(
    val detail: String = "Authentication token is invalid",
) : DomainError {
    override fun toAppError(): AppError = AppError.Auth.TokenInvalid()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unauthorized(error.code, message)
    }
}

data class UserNotFound(
    val detail: String = "User not found",
) : DomainError {
    override fun toAppError(): AppError = AppError.User.NotFound()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.notFound(error.code, message)
    }
}

data class Forbidden(
    val detail: String = "You do not have permission to access this resource",
) : DomainError {
    override fun toAppError(): AppError = AppError.User.Forbidden()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.forbidden(error.code, message)
    }
}
