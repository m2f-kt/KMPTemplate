package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.DocumentListResponse
import com.m2f.template.models.dto.DocumentResponse

/**
 * SDK functions for document management endpoints (RAG pipeline).
 *
 * All functions return [Either<AppError, T>] for consistent error handling.
 * Auth token is automatically attached by the [AuthInterceptor].
 */
interface DocumentApi {
    suspend fun uploadDocument(
        groupId: String,
        scope: String,
        fileName: String,
        fileBytes: ByteArray,
        contentType: String,
        assignedToUserId: String? = null,
    ): Either<AppError, DocumentResponse>

    suspend fun listDocuments(
        groupId: String,
        scope: String? = null,
    ): Either<AppError, DocumentListResponse>

    suspend fun getDocument(documentId: String): Either<AppError, DocumentResponse>

    suspend fun deleteDocument(documentId: String): Either<AppError, Unit>
}
