package com.m2f.server.groups.errors

import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.gone
import com.m2f.core.config.server.notFound
import com.m2f.core.config.server.preferredLanguage
import com.m2f.core.config.server.unprocessable
import com.m2f.core.config.server.localization.ServerStrings
import com.m2f.template.models.AppError
import io.ktor.server.routing.RoutingContext

data class InvitationNotFound(
    val detail: String = "Invitation not found",
) : DomainError {
    override fun toAppError(): AppError = AppError.Invitation.NotFound()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.notFound(error.code, message)
    }
}

data class InvitationExpired(
    val detail: String = "Invitation has expired",
) : DomainError {
    override fun toAppError(): AppError = AppError.Invitation.Expired()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.gone(error.code, message)
    }
}

data class InvitationAlreadyAccepted(
    val detail: String = "Invitation has already been accepted",
) : DomainError {
    override fun toAppError(): AppError = AppError.Invitation.AlreadyAccepted()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unprocessable(error.code, message)
    }
}

data class InvitationRevoked(
    val detail: String = "Invitation has been revoked",
) : DomainError {
    override fun toAppError(): AppError = AppError.Invitation.Revoked()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.gone(error.code, message)
    }
}
