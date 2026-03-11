package com.m2f.server.files.contract.errors

import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.notFound
import com.m2f.core.config.server.payloadTooLarge
import com.m2f.core.config.server.preferredLanguage
import com.m2f.core.config.server.unexpected
import com.m2f.core.config.server.unsupportedMediaType
import com.m2f.core.config.server.localization.ServerStrings
import com.m2f.template.models.AppError
import io.ktor.server.routing.RoutingContext

data class FileTooLarge(
    val detail: String = "File exceeds maximum allowed size",
) : DomainError {
    override fun toAppError(): AppError = AppError.File.TooLarge()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.payloadTooLarge(error.code, message)
    }
}

data class FileUnsupportedType(
    val detail: String = "File type is not allowed",
) : DomainError {
    override fun toAppError(): AppError = AppError.File.UnsupportedType()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unsupportedMediaType(error.code, message)
    }
}

data class FileUploadFailed(
    val detail: String = "Failed to upload file",
) : DomainError {
    override fun toAppError(): AppError = AppError.File.UploadFailed()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.unexpected(error.code, message)
    }
}

data class FileNotFound(
    val detail: String = "File not found",
) : DomainError {
    override fun toAppError(): AppError = AppError.File.NotFound()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.notFound(error.code, message)
    }
}
