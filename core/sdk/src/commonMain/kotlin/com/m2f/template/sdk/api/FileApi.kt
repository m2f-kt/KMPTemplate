package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.FileResponse

/**
 * SDK functions for file management endpoints.
 *
 * All functions return [Either<AppError, T>] for consistent error handling.
 * Auth token is automatically attached by the [AuthInterceptor].
 */
interface FileApi {
    suspend fun uploadFile(fileName: String, bytes: ByteArray, contentType: String): Either<AppError, FileResponse>
    suspend fun getFileUrl(fileKey: String): Either<AppError, Map<String, String>>
}
