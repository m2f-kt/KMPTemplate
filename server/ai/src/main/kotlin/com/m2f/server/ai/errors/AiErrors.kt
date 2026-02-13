package com.m2f.server.ai.errors

import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.notFound
import com.m2f.core.config.server.unexpected
import com.m2f.template.models.AppError
import io.ktor.server.routing.RoutingContext

data class AgentExecutionFailed(
    val detail: String = "Agent execution failed",
) : DomainError {
    override fun toAppError(): AppError = AppError.AI.AgentFailed(detail = detail)

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        routingContext.unexpected(error.code, error.message)
    }
}

data class AgentNotFound(
    val detail: String = "The requested agent was not found",
) : DomainError {
    override fun toAppError(): AppError = AppError.AI.AgentNotFound()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        routingContext.notFound(error.code, error.message)
    }
}

data class ConversationNotFound(
    val detail: String = "Conversation not found",
) : DomainError {
    override fun toAppError(): AppError = AppError.AI.ConversationNotFound()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        routingContext.notFound(error.code, error.message)
    }
}

data class ProviderUnavailable(
    val detail: String = "AI provider is not available or not configured",
) : DomainError {
    override fun toAppError(): AppError = AppError.AI.ProviderUnavailable()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        routingContext.unexpected(error.code, error.message)
    }
}
