package com.m2f.template.core.testing.fakes

import arrow.core.Either
import com.m2f.template.core.testing.FakeSDKDsl
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AuthResponse
import com.m2f.template.models.dto.ForgotPasswordRequest
import com.m2f.template.models.dto.LoginRequest
import com.m2f.template.models.dto.RefreshTokenRequest
import com.m2f.template.models.dto.RegisterRequest
import com.m2f.template.models.dto.ResetPasswordRequest
import com.m2f.template.sdk.api.AuthApi

/**
 * DSL builder for creating fake [AuthApi] instances in tests.
 *
 * Each method defaults to returning `Either.Left(AppError.Client.Unknown())`
 * so that unconfigured code paths fail fast instead of silently succeeding.
 *
 * Usage:
 * ```kotlin
 * val authApi = fakeAuthApi {
 *     login { _, _ -> Either.Right(AuthResponse(...)) }
 * }
 * ```
 */
@FakeSDKDsl
class FakeAuthApiBuilder {

    private var _register: suspend (RegisterRequest) -> Either<AppError, AuthResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _login: suspend (LoginRequest, Boolean) -> Either<AppError, AuthResponse> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }

    private var _refresh: suspend (RefreshTokenRequest) -> Either<AppError, AuthResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _forgotPassword: suspend (ForgotPasswordRequest) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _resetPassword: suspend (ResetPasswordRequest) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _logout: suspend () -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    fun register(behavior: suspend (RegisterRequest) -> Either<AppError, AuthResponse>) {
        _register = behavior
    }

    fun login(behavior: suspend (LoginRequest, Boolean) -> Either<AppError, AuthResponse>) {
        _login = behavior
    }

    fun refresh(behavior: suspend (RefreshTokenRequest) -> Either<AppError, AuthResponse>) {
        _refresh = behavior
    }

    fun forgotPassword(behavior: suspend (ForgotPasswordRequest) -> Either<AppError, Unit>) {
        _forgotPassword = behavior
    }

    fun resetPassword(behavior: suspend (ResetPasswordRequest) -> Either<AppError, Unit>) {
        _resetPassword = behavior
    }

    fun logout(behavior: suspend () -> Either<AppError, Unit>) {
        _logout = behavior
    }

    internal fun build(): AuthApi = object : AuthApi {
        override suspend fun register(request: RegisterRequest): Either<AppError, AuthResponse> =
            _register(request)

        override suspend fun login(request: LoginRequest, rememberMe: Boolean): Either<AppError, AuthResponse> =
            _login(request, rememberMe)

        override suspend fun refresh(request: RefreshTokenRequest): Either<AppError, AuthResponse> =
            _refresh(request)

        override suspend fun forgotPassword(request: ForgotPasswordRequest): Either<AppError, Unit> =
            _forgotPassword(request)

        override suspend fun resetPassword(request: ResetPasswordRequest): Either<AppError, Unit> =
            _resetPassword(request)

        override suspend fun logout(): Either<AppError, Unit> =
            _logout()
    }
}

/**
 * Top-level DSL entry point for creating a fake [AuthApi].
 *
 * @param block optional configuration block to override default method behaviors
 * @return a configured [AuthApi] instance backed by the builder's lambdas
 */
fun fakeAuthApi(block: FakeAuthApiBuilder.() -> Unit = {}): AuthApi =
    FakeAuthApiBuilder().apply(block).build()
