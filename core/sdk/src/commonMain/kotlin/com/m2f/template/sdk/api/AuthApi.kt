package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AuthResponse
import com.m2f.template.models.dto.ForgotPasswordRequest
import com.m2f.template.models.dto.LoginRequest
import com.m2f.template.models.dto.RefreshTokenRequest
import com.m2f.template.models.dto.RegisterRequest
import com.m2f.template.models.dto.ResetPasswordRequest
import com.m2f.template.sdk.apiCall
import com.m2f.template.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * SDK functions for all authentication endpoints.
 *
 * - [register] and [login] save tokens to [TokenStorage] on success.
 * - [logout] always clears local tokens, even if the server call fails.
 * - [refresh] is exposed for manual use; the [AuthInterceptor] handles automatic refresh.
 */
class AuthApi(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage,
) {
    suspend fun register(request: RegisterRequest): Either<AppError, AuthResponse> =
        apiCall<AuthResponse> {
            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }.onRight { response ->
            tokenStorage.saveTokens(response.accessToken, response.refreshToken)
        }

    suspend fun login(request: LoginRequest): Either<AppError, AuthResponse> =
        apiCall<AuthResponse> {
            client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }.onRight { response ->
            tokenStorage.saveTokens(response.accessToken, response.refreshToken)
        }

    suspend fun refresh(request: RefreshTokenRequest): Either<AppError, AuthResponse> =
        apiCall<AuthResponse> {
            client.post("/api/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }.onRight { response ->
            tokenStorage.saveTokens(response.accessToken, response.refreshToken)
        }

    suspend fun forgotPassword(request: ForgotPasswordRequest): Either<AppError, Unit> =
        apiCall<Unit> {
            client.post("/api/auth/forgot-password") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun resetPassword(request: ResetPasswordRequest): Either<AppError, Unit> =
        apiCall<Unit> {
            client.post("/api/auth/reset-password") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun logout(): Either<AppError, Unit> {
        val result = apiCall<Unit> {
            client.post("/api/auth/logout")
        }
        // Always clear local tokens on logout, regardless of server response
        tokenStorage.clearTokens()
        return result
    }
}
