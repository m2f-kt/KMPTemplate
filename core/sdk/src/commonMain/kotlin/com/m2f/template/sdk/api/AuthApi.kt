package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AuthResponse
import com.m2f.template.models.dto.ForgotPasswordRequest
import com.m2f.template.models.dto.LoginRequest
import com.m2f.template.models.dto.RefreshTokenRequest
import com.m2f.template.models.dto.RegisterRequest
import com.m2f.template.models.dto.ResetPasswordRequest

/**
 * SDK functions for all authentication endpoints.
 *
 * - [register] and [login] save tokens on success.
 * - [logout] always clears local tokens, even if the server call fails.
 * - [refresh] is exposed for manual use; the [AuthInterceptor] handles automatic refresh.
 */
interface AuthApi {
    suspend fun register(request: RegisterRequest): Either<AppError, AuthResponse>
    suspend fun login(request: LoginRequest, rememberMe: Boolean = true): Either<AppError, AuthResponse>
    suspend fun refresh(request: RefreshTokenRequest): Either<AppError, AuthResponse>
    suspend fun forgotPassword(request: ForgotPasswordRequest): Either<AppError, Unit>
    suspend fun resetPassword(request: ResetPasswordRequest): Either<AppError, Unit>
    suspend fun logout(): Either<AppError, Unit>
}
