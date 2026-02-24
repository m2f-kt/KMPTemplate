package com.m2f.template.core.testing.fakes

import arrow.core.Either
import com.m2f.template.core.testing.FakeSDKDsl
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.DocumentListResponse
import com.m2f.template.models.dto.DocumentResponse
import com.m2f.template.sdk.api.DocumentApi

/**
 * DSL builder for creating fake [DocumentApi] instances in tests.
 *
 * Each method defaults to returning `Either.Left(AppError.Client.Unknown())`
 * so that unconfigured code paths fail fast instead of silently succeeding.
 *
 * Usage:
 * ```kotlin
 * val documentApi = FakeDocumentApiBuilder().apply {
 *     listDocuments { _, _ -> Either.Right(DocumentListResponse(emptyList())) }
 * }.build()
 * ```
 */
@FakeSDKDsl
class FakeDocumentApiBuilder {

    private var _uploadDocument: suspend (String, String, String, ByteArray, String, String?) -> Either<AppError, DocumentResponse> =
        { _, _, _, _, _, _ -> Either.Left(AppError.Client.Unknown(detail = "uploadDocument not configured in fake")) }

    private var _listDocuments: suspend (String, String?) -> Either<AppError, DocumentListResponse> =
        { _, _ -> Either.Left(AppError.Client.Unknown(detail = "listDocuments not configured in fake")) }

    private var _getDocument: suspend (String) -> Either<AppError, DocumentResponse> =
        { Either.Left(AppError.Client.Unknown(detail = "getDocument not configured in fake")) }

    private var _deleteDocument: suspend (String) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown(detail = "deleteDocument not configured in fake")) }

    fun uploadDocument(behavior: suspend (String, String, String, ByteArray, String, String?) -> Either<AppError, DocumentResponse>) {
        _uploadDocument = behavior
    }

    fun listDocuments(behavior: suspend (String, String?) -> Either<AppError, DocumentListResponse>) {
        _listDocuments = behavior
    }

    fun getDocument(behavior: suspend (String) -> Either<AppError, DocumentResponse>) {
        _getDocument = behavior
    }

    fun deleteDocument(behavior: suspend (String) -> Either<AppError, Unit>) {
        _deleteDocument = behavior
    }

    internal fun build(): DocumentApi = object : DocumentApi {
        override suspend fun uploadDocument(
            groupId: String,
            scope: String,
            fileName: String,
            fileBytes: ByteArray,
            contentType: String,
            assignedToUserId: String?,
        ) = this@FakeDocumentApiBuilder._uploadDocument(groupId, scope, fileName, fileBytes, contentType, assignedToUserId)

        override suspend fun listDocuments(groupId: String, scope: String?) =
            this@FakeDocumentApiBuilder._listDocuments(groupId, scope)

        override suspend fun getDocument(documentId: String) =
            this@FakeDocumentApiBuilder._getDocument(documentId)

        override suspend fun deleteDocument(documentId: String) =
            this@FakeDocumentApiBuilder._deleteDocument(documentId)
    }
}
