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
import io.ktor.http.isSuccess

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
 * Attempts to deserialize the response body as [ErrorResponse] to preserve
 * the server's error code and message. Falls back to generic messages
 * when the body cannot be deserialized.
 *
 * Status code mapping:
 * - 401 -> [AppError.Auth.Unauthorized]
 * - 403 -> [AppError.User.Forbidden]
 * - 404 -> [AppError.User.NotFound]
 * - 409 -> [AppError.Auth.UserAlreadyExists]
 * - 422 -> [AppError.Client.ServerMapped] (preserves server's error code/message)
 * - 500..599 -> [AppError.Server.Internal]
 * - Other -> [AppError.Client.Unknown]
 */
suspend fun mapHttpError(response: HttpResponse): AppError {
    val errorResponse: ErrorResponse? = try {
        response.body<ErrorResponse>()
    } catch (_: Exception) {
        null
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

        422 -> AppError.Client.ServerMapped(
            code = errorResponse?.code ?: "VALIDATION_ERROR",
            message = errorResponse?.message ?: "Validation failed",
        )

        in 500..599 -> AppError.Server.Internal(
            message = errorResponse?.message ?: "Server error",
        )

        else -> AppError.Client.Unknown(
            detail = errorResponse?.message ?: "HTTP ${response.status.value}",
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
