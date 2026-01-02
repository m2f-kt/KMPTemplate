package com.m2f.core.config.server

import arrow.core.NonEmptyList
import io.ktor.server.routing.RoutingContext

interface DomainError {
    context(routingContext: RoutingContext)
    suspend fun respond()
}

data class UnexpectedError(val message: String = "An Unexpected error has occurred") : DomainError {
    context(routingContext: RoutingContext)
    override suspend fun respond() {
        routingContext.unexpected(message)
    }
}

data class MappingError(val message: String) : DomainError {
    context(routingContext: RoutingContext)
    override suspend fun respond() {
        routingContext.unprocessable("Error mapping the request: $message")
    }

}

sealed interface ValidationError : DomainError


data class IncorrectInput(val errors: NonEmptyList<InvalidField>) : ValidationError {

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val formattedErrors = errors.flatMap { field ->
            field.errors.map { error -> "${field.field}: $error" }
        }
        routingContext.unprocessable(formattedErrors)
    }
}

data class MissingParameter(val name: String) : ValidationError {
    context(routingContext: RoutingContext)
    override suspend fun respond() {
        routingContext.unprocessable("Missing $name parameter in request")
    }
}

data class InvalidParameter(val name: String, val value: String, val expectedType: String) : ValidationError {
    context(routingContext: RoutingContext)
    override suspend fun respond() {
        routingContext.unprocessable("Invalid $name parameter: '$value' cannot be converted to $expectedType")
    }
}

data object InvalidContent : ValidationError {
    context(routingContext: RoutingContext)
    override suspend fun respond() {
        routingContext.unprocessable("Invalid content received can not be converted")
    }
}

data class Unauthorized(val message: String = "Authentication required") : DomainError {
    context(routingContext: RoutingContext)
    override suspend fun respond() {
        routingContext.unauthorized(message)
    }
}
