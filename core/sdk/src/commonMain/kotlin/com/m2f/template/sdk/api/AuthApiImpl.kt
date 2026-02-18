package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AuthResponse
import com.m2f.template.models.dto.ForgotPasswordRequest
import com.m2f.template.models.dto.LoginRequest
import com.m2f.template.models.dto.RefreshTokenRequest
import com.m2f.template.models.dto.RegisterRequest
import com.m2f.template.models.dto.ResetPasswordRequest
import com.m2f.template.models.routes.Auth
import com.m2f.template.sdk.apiCall
import com.m2f.template.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiImpl(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage,
) : AuthApi {
    override suspend fun register(request: RegisterRequest): Either<AppError, AuthResponse> =
        apiCall<AuthResponse> {
            client.post(Auth.Register()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }.onRight { response ->
            tokenStorage.saveTokens(response.accessToken, response.refreshToken)
        }

    override suspend fun login(request: LoginRequest, rememberMe: Boolean): Either<AppError, AuthResponse> =
        apiCall<AuthResponse> {
            client.post(Auth.Login()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }.onRight { response ->
            tokenStorage.saveTokens(response.accessToken, response.refreshToken, rememberMe)
        }

    override suspend fun refresh(request: RefreshTokenRequest): Either<AppError, AuthResponse> =
        apiCall<AuthResponse> {
            client.post(Auth.Refresh()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }.onRight { response ->
            tokenStorage.saveTokens(response.accessToken, response.refreshToken)
        }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Either<AppError, Unit> =
        apiCall<Unit> {
            client.post(Auth.ForgotPassword()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun resetPassword(request: ResetPasswordRequest): Either<AppError, Unit> =
        apiCall<Unit> {
            client.post(Auth.ResetPassword()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun logout(): Either<AppError, Unit> {
        val result = apiCall<Unit> {
            client.post(Auth.Logout())
        }
        // Always clear local tokens on logout, regardless of server response
        tokenStorage.clearTokens()
        return result
    }
}
