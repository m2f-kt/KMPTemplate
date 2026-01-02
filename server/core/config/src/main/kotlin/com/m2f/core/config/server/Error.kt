@file:Suppress("TooManyFunctions")

package com.m2f.core.config.server

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.raise.recover
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext

context(context: RoutingContext)
suspend inline fun <reified A : Any> conduit(
    status: HttpStatusCode = HttpStatusCode.OK,
    crossinline block: suspend context(Raise<DomainError>) () -> A,
): Unit = either {
    block(this)
}.fold({ with(context) { it.respond() } }, { context.call.respond<A>(status, it) })

context(context: RoutingContext)
suspend inline fun <reified A : Any> conduitAuth(
    status: HttpStatusCode = HttpStatusCode.OK,
    crossinline block: suspend context(Raise<DomainError>) (userId: String) -> A,
): Unit = either {
    val userId = context.call.principal<JWTPrincipal>()?.payload?.subject
    ensureNotNull(userId) { Unauthorized() }
    block(this, userId)
}.fold({ with(context) { it.respond() } }, { context.call.respond<A>(status, it) })

context(context: RoutingContext, raise: Raise<DomainError>)
fun getIntParam(name: String): Int = with(raise) {
    val value = context.call.parameters[name] ?: raise(MissingParameter(name))
    return value.toIntOrNull() ?: raise(InvalidParameter(name, value, "integer"))
}

context(context: RoutingContext, raise: Raise<DomainError>)
suspend inline fun <reified T : Any> getModel(): T = with(raise) {

    catch({
        context.call.receive<T>()
    }) {
        raise(InvalidContent)
    }
}

context(context: RoutingContext, raise: Raise<DomainError>)
fun getStringParam(name: String): String = with(raise) {
    return context.call.parameters[name] ?: raise(MissingParameter(name))
}

context(context: RoutingContext)
fun getOptionalStringParam(name: String): String? {
    return recover({ getStringParam((name)) }) { null }
}

context(context: RoutingContext, raise: Raise<DomainError>)
fun getIntQuery(name: String): Int = with(raise) {
    val value = context.call.request.queryParameters[name] ?: raise(MissingParameter(name))
    return value.toIntOrNull() ?: raise(InvalidParameter(name, value, "integer"))
}

context(context: RoutingContext, raise: Raise<DomainError>)
fun getStringQuery(name: String): String = with(raise) {
    return context.call.request.queryParameters[name] ?: raise(MissingParameter(name))
}

suspend inline fun RoutingContext.unexpected(
    error: String,
): Unit = call.respond(
    HttpStatusCode.InternalServerError,
    GenericErrorModel(GenericErrorModelErrors(listOf(error))),
)


suspend inline fun RoutingContext.unprocessable(
    error: String,
): Unit = call.respond(
    HttpStatusCode.UnprocessableEntity,
    GenericErrorModel(GenericErrorModelErrors(listOf(error))),
)

suspend inline fun RoutingContext.unprocessable(
    errors: List<String>,
): Unit = call.respond(
    HttpStatusCode.UnprocessableEntity,
    GenericErrorModel(GenericErrorModelErrors(errors)),
)

suspend inline fun RoutingContext.unauthorized(
    error: String,
): Unit = call.respond(
    HttpStatusCode.Unauthorized,
    GenericErrorModel(GenericErrorModelErrors(listOf(error))),
)


