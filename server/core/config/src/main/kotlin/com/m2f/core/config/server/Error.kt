@file:Suppress("TooManyFunctions")

package com.m2f.core.config.server

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.raise.recover
import com.m2f.template.models.dto.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.sse.ServerSSESession
import io.ktor.sse.ServerSentEvent

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

context(session: ServerSSESession)
suspend inline fun getAuth(
    jwtSecret: String,
    jwtAudience: String,
    jwtIssuer: String,
    crossinline block: suspend context(Raise<DomainError>) (userId: String) -> Unit,
) {
    val token = session.call.request.queryParameters["token"]
    if (token == null) {
        session.send(ServerSentEvent(data = "Error: Missing token", event = "error"))
        return
    }

    val userId = try {
        val verifier = com.auth0.jwt.JWT.require(
            com.auth0.jwt.algorithms.Algorithm.HMAC256(jwtSecret)
        )
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .build()
        verifier.verify(token).subject
            ?: throw IllegalArgumentException("Missing subject")
    } catch (_: Exception) {
        session.send(ServerSentEvent(data = "Error: Invalid token", event = "error"))
        return
    }

    either { block(this, userId) }.onLeft { error ->
        session.send(ServerSentEvent(data = "Error: ${error.toAppError().message}", event = "error"))
    }
}

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
    code: String,
    error: String,
): Unit = call.respond(
    HttpStatusCode.InternalServerError,
    ErrorResponse(code = code, message = error),
)


suspend inline fun RoutingContext.unprocessable(
    code: String,
    error: String,
): Unit = call.respond(
    HttpStatusCode.UnprocessableEntity,
    ErrorResponse(code = code, message = error),
)

suspend inline fun RoutingContext.unprocessable(
    code: String,
    message: String,
    errors: List<String>,
): Unit = call.respond(
    HttpStatusCode.UnprocessableEntity,
    ErrorResponse(code = code, message = message, errors = errors),
)

suspend inline fun RoutingContext.unauthorized(
    code: String,
    error: String,
): Unit = call.respond(
    HttpStatusCode.Unauthorized,
    ErrorResponse(code = code, message = error),
)

suspend inline fun RoutingContext.forbidden(
    code: String,
    error: String,
): Unit = call.respond(
    HttpStatusCode.Forbidden,
    ErrorResponse(code = code, message = error),
)

suspend inline fun RoutingContext.notFound(
    code: String,
    error: String,
): Unit = call.respond(
    HttpStatusCode.NotFound,
    ErrorResponse(code = code, message = error),
)
