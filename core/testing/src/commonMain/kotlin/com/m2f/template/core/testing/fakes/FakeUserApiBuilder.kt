package com.m2f.template.core.testing.fakes

import arrow.core.Either
import com.m2f.template.core.testing.FakeSDKDsl
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.MembershipSummary
import com.m2f.template.models.dto.UpdateProfileRequest
import com.m2f.template.models.dto.UserResponse
import com.m2f.template.sdk.api.UserApi

/**
 * DSL builder for creating fake [UserApi] instances in tests.
 *
 * Each method defaults to returning `Either.Left(AppError.Client.Unknown())`
 * so that unconfigured code paths fail fast instead of silently succeeding.
 *
 * Usage:
 * ```kotlin
 * val userApi = fakeUserApi {
 *     getProfile { Either.Right(UserResponse(...)) }
 * }
 * ```
 */
@FakeSDKDsl
class FakeUserApiBuilder {

    private var _getProfile: suspend () -> Either<AppError, UserResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _updateProfile: suspend (UpdateProfileRequest) -> Either<AppError, UserResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _getUserById: suspend (String) -> Either<AppError, UserResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _getMyMemberships: suspend () -> Either<AppError, List<MembershipSummary>> =
        { Either.Left(AppError.Client.Unknown()) }

    fun getProfile(behavior: suspend () -> Either<AppError, UserResponse>) {
        _getProfile = behavior
    }

    fun updateProfile(behavior: suspend (UpdateProfileRequest) -> Either<AppError, UserResponse>) {
        _updateProfile = behavior
    }

    fun getUserById(behavior: suspend (String) -> Either<AppError, UserResponse>) {
        _getUserById = behavior
    }

    fun getMyMemberships(behavior: suspend () -> Either<AppError, List<MembershipSummary>>) {
        _getMyMemberships = behavior
    }

    internal fun build(): UserApi = object : UserApi {
        override suspend fun getProfile(): Either<AppError, UserResponse> =
            _getProfile()

        override suspend fun updateProfile(request: UpdateProfileRequest): Either<AppError, UserResponse> =
            _updateProfile(request)

        override suspend fun getUserById(id: String): Either<AppError, UserResponse> =
            _getUserById(id)

        override suspend fun getMyMemberships(): Either<AppError, List<MembershipSummary>> =
            _getMyMemberships()
    }
}
