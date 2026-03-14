package com.m2f.server.privacy.contract.errors

import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.conflict
import com.m2f.core.config.server.forbidden
import com.m2f.core.config.server.notFound
import com.m2f.core.config.server.preferredLanguage
import com.m2f.core.config.server.localization.ServerStrings
import com.m2f.template.models.AppError
import io.ktor.server.routing.RoutingContext

data class ProcessingRestricted(
    val detail: String = "Data processing is currently restricted for this user",
) : DomainError {
    override fun toAppError(): AppError = AppError.Privacy.ProcessingRestricted()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.forbidden(error.code, message)
    }
}

data class ConsentRequired(
    val detail: String = "User consent is required before processing",
) : DomainError {
    override fun toAppError(): AppError = AppError.Privacy.ConsentRequired()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.forbidden(error.code, message)
    }
}

data class DeletionAlreadyPending(
    val detail: String = "An account deletion request is already pending",
) : DomainError {
    override fun toAppError(): AppError = AppError.Privacy.DeletionPending()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.conflict(error.code, message)
    }
}

data class ExportNotReady(
    val detail: String = "Data export is not ready for download",
) : DomainError {
    override fun toAppError(): AppError = AppError.Privacy.ExportNotReady()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.notFound(error.code, message)
    }
}

data class ExportAlreadyActive(
    val detail: String = "An export is already in progress",
) : DomainError {
    override fun toAppError(): AppError = AppError.Privacy.ExportNotReady(message = "An export is already in progress")

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.conflict(error.code, message)
    }
}
