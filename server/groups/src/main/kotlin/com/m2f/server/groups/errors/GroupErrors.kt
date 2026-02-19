package com.m2f.server.groups.errors

import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.forbidden
import com.m2f.core.config.server.notFound
import com.m2f.core.config.server.preferredLanguage
import com.m2f.core.config.server.unprocessable
import com.m2f.core.config.server.localization.ServerStrings
import com.m2f.template.models.AppError
import io.ktor.server.routing.RoutingContext

data class GroupNotFound(
    val detail: String = "Group not found",
) : DomainError {
    override fun toAppError(): AppError = AppError.Group.NotFound()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.notFound(error.code, message)
    }
}

data class GroupForbidden(
    val detail: String = "You do not have permission to access this group",
) : DomainError {
    override fun toAppError(): AppError = AppError.Group.Forbidden()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.forbidden(error.code, message)
    }
}

data class GroupAlreadyExists(
    val detail: String = "A group with this slug already exists",
) : DomainError {
    override fun toAppError(): AppError = AppError.Group.AlreadyExists()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unprocessable(error.code, message)
    }
}

data class MemberAlreadyInGroup(
    val detail: String = "User is already a member of this group",
) : DomainError {
    override fun toAppError(): AppError = AppError.Group.MemberAlreadyExists()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unprocessable(error.code, message)
    }
}

data class MemberNotInGroup(
    val detail: String = "User is not a member of this group",
) : DomainError {
    override fun toAppError(): AppError = AppError.Group.NotFound(
        message = "Member not found in this group",
    )

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.notFound(error.code, message)
    }
}

data class CannotRemoveOwner(
    val detail: String = "Cannot remove the group owner",
) : DomainError {
    override fun toAppError(): AppError = AppError.Group.Forbidden(
        message = "Cannot remove the group owner",
    )

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.forbidden(error.code, message)
    }
}
