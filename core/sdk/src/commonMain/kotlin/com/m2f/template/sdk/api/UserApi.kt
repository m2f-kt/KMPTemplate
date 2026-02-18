package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.UpdateProfileRequest
import com.m2f.template.models.dto.UserResponse

/**
 * SDK functions for user profile endpoints.
 *
 * All functions return [Either<AppError, T>] for consistent error handling.
 * Auth token is automatically attached by the [AuthInterceptor].
 */
interface UserApi {
    suspend fun getProfile(): Either<AppError, UserResponse>
    suspend fun updateProfile(request: UpdateProfileRequest): Either<AppError, UserResponse>
    suspend fun getUserById(id: String): Either<AppError, UserResponse>
}
