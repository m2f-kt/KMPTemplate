package com.m2f.template.sdk

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.context.either
import arrow.core.raise.context.ensure
import arrow.core.raise.context.raise
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.ErrorResponse
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/** Max number of raw-body characters preserved in a fallback [AppError.Client.Unknown] detail. */
private const val BODY_PREVIEW_CHARS: Int = 500

/** Lenient decoder for parsing an [ErrorResponse] out of a raw error body. */
private val errorBodyJson = Json { ignoreUnknownKeys = true }

/**
 * Wraps an HTTP call in [Either], mapping success responses to [Right] and
 * error responses / exceptions to [Left] with the appropriate [AppError] subtype.
 *
 * This is the core entry point for all SDK API functions. Every HTTP call goes through
 * this wrapper to ensure consistent error handling across the SDK.
 *
 * Usage:
 * ```
 * suspend fun login(request: LoginRequest): Either<AppError, AuthResponse> =
 *     apiCall { client.post(Auth.Login()) { setBody(request) } }
 * ```
 *
 * @param T The expected response body type (must be [kotlinx.serialization.Serializable]).
 * @param block A suspend lambda that executes the HTTP request and returns [HttpResponse].
 */
suspend inline fun <reified T> apiCall(
    block: () -> HttpResponse,
): Either<AppError, T> = either {
    catch(block = {
        val response = block()
        ensure(response.status.isSuccess()) { mapHttpError(response) }
        response.body<T>()
    }, catch = { e ->
        raise(mapException(e))
    })
}

/**
 * Maps an HTTP error response to the appropriate [AppError] subtype.
 *
 * Reads the response body text ONCE, then attempts to decode it as [ErrorResponse] with a lenient
 * decoder to preserve the server's error code and message. When the body does not match the
 * `{code, message}` shape, the raw body (truncated to [BODY_PREVIEW_CHARS]) is preserved inside the
 * [AppError.Client.Unknown] fallback so the real cause is not discarded.
 *
 * A pluggable [domainCodeMapper] is consulted (when an [ErrorResponse] decoded) BEFORE the status
 * fallback, letting callers route their own body `code` namespace to typed errors. It defaults to a
 * no-op returning `null`, so by default the status-based mapping applies unchanged.
 *
 * Status code mapping:
 * - 401 -> [AppError.Auth.Unauthorized]
 * - 403 -> [AppError.User.Forbidden]
 * - 404 -> [AppError.User.NotFound]
 * - 409 -> [AppError.Auth.UserAlreadyExists]
 * - 410 -> [AppError.Client.ServerMapped] (preserves server's error code/message, e.g. revoked invitation)
 * - 422 -> [AppError.Client.ServerMapped] (preserves server's error code/message)
 * - 500..599 -> [AppError.Server.Internal]
 * - Other -> [AppError.Client.Unknown] (raw body preserved when un-parseable)
 *
 * @param response The error HTTP response.
 * @param domainCodeMapper Optional hook mapping a decoded [ErrorResponse] to a typed [AppError];
 *   returning `null` falls through to the status-based mapping.
 */
suspend fun mapHttpError(
    response: HttpResponse,
    domainCodeMapper: (ErrorResponse) -> AppError? = { null },
): AppError {
    // Capture the raw body text BEFORE attempting ErrorResponse decoding. Reading it here keeps the
    // actual server/provider message available even when the body does not match our shape.
    val rawBody: String = runCatching { response.bodyAsText() }.getOrDefault("")
    val errorResponse: ErrorResponse? = runCatching {
        errorBodyJson.decodeFromString(ErrorResponse.serializer(), rawBody)
    }.getOrNull()
    errorResponse?.let { body ->
        domainCodeMapper(body)?.let { return it }
    }
    return when (response.status.value) {
        401 -> AppError.Auth.Unauthorized(
            message = errorResponse?.message ?: "Authentication required",
        )

        403 -> AppError.User.Forbidden(
            message = errorResponse?.message ?: "Access denied",
        )

        404 -> AppError.User.NotFound(
            message = errorResponse?.message ?: "Not found",
        )

        409 -> AppError.Auth.UserAlreadyExists(
            message = errorResponse?.message ?: "Resource already exists",
        )

        410 -> AppError.Client.ServerMapped(
            code = errorResponse?.code ?: "INVITATION_ERROR",
            message = errorResponse?.message ?: "Resource is no longer available",
        )

        422 -> AppError.Client.ServerMapped(
            code = errorResponse?.code ?: "VALIDATION_ERROR",
            message = errorResponse?.message ?: "Validation failed",
        )

        in 500..599 -> AppError.Server.Internal(
            message = errorResponse?.message ?: "Server error",
        )

        else -> AppError.Client.Unknown(
            // Preserve the raw body when it can't be parsed as our ErrorResponse shape, so a server
            // or provider error using a different envelope is not collapsed to a bare "HTTP 400".
            detail = errorResponse?.message
                ?: "HTTP ${response.status.value}: ${rawBody.take(BODY_PREVIEW_CHARS)}",
        )
    }
}

/**
 * Maps an exception to the appropriate [AppError] subtype.
 *
 * - [kotlin.io.IOException] (available on all KMP targets) -> [AppError.Client.Network]
 * - [kotlinx.coroutines.TimeoutCancellationException] -> [AppError.Client.Timeout]
 * - Any other exception -> [AppError.Client.Unknown]
 */
fun mapException(e: Throwable): AppError = when {
    e::class.simpleName == "TimeoutCancellationException" -> AppError.Client.Timeout()
    e::class.simpleName?.contains("IOException") == true -> AppError.Client.Network()
    else -> AppError.Client.Unknown(detail = e.message)
}
