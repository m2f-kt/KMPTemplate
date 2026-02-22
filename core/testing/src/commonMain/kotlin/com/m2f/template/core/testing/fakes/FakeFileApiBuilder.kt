package com.m2f.template.core.testing.fakes

import arrow.core.Either
import com.m2f.template.core.testing.FakeSDKDsl
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.FileResponse
import com.m2f.template.sdk.api.FileApi

/**
 * DSL builder for creating fake [FileApi] instances in tests.
 *
 * Each method defaults to returning `Either.Left(AppError.Client.Unknown())`
 * so that unconfigured code paths fail fast instead of silently succeeding.
 *
 * Usage:
 * ```kotlin
 * val fileApi = FakeFileApiBuilder().apply {
 *     uploadFile { _, _, _ -> Either.Right(FileResponse(...)) }
 * }.build()
 * ```
 */
@FakeSDKDsl
class FakeFileApiBuilder {

    private var _uploadFile: suspend (String, ByteArray, String) -> Either<AppError, FileResponse> =
        { _, _, _ -> Either.Left(AppError.Client.Unknown(detail = "uploadFile not configured in fake")) }

    private var _getFileUrl: suspend (String) -> Either<AppError, Map<String, String>> =
        { Either.Left(AppError.Client.Unknown(detail = "getFileUrl not configured in fake")) }

    fun uploadFile(behavior: suspend (String, ByteArray, String) -> Either<AppError, FileResponse>) {
        _uploadFile = behavior
    }

    fun getFileUrl(behavior: suspend (String) -> Either<AppError, Map<String, String>>) {
        _getFileUrl = behavior
    }

    internal fun build(): FileApi = object : FileApi {
        override suspend fun uploadFile(fileName: String, bytes: ByteArray, contentType: String) =
            this@FakeFileApiBuilder._uploadFile(fileName, bytes, contentType)

        override suspend fun getFileUrl(fileKey: String) =
            this@FakeFileApiBuilder._getFileUrl(fileKey)
    }
}
