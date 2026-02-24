package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.DocumentListResponse
import com.m2f.template.models.dto.DocumentResponse
import com.m2f.template.models.routes.Documents
import com.m2f.template.sdk.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class DocumentApiImpl(private val client: HttpClient) : DocumentApi {

    override suspend fun uploadDocument(
        groupId: String,
        scope: String,
        fileName: String,
        fileBytes: ByteArray,
        contentType: String,
        assignedToUserId: String?,
    ): Either<AppError, DocumentResponse> = apiCall {
        client.submitFormWithBinaryData(
            url = "/api/documents/upload",
            formData = formData {
                append("file", fileBytes, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    append(HttpHeaders.ContentType, contentType)
                })
                append("groupId", groupId)
                append("scope", scope)
                if (assignedToUserId != null) {
                    append("assignedToUserId", assignedToUserId)
                }
            },
        )
    }

    override suspend fun listDocuments(
        groupId: String,
        scope: String?,
    ): Either<AppError, DocumentListResponse> = apiCall {
        client.get(Documents.ListByGroup(groupId = groupId, scope = scope))
    }

    override suspend fun getDocument(documentId: String): Either<AppError, DocumentResponse> =
        apiCall { client.get(Documents.ById(documentId = documentId)) }

    override suspend fun deleteDocument(documentId: String): Either<AppError, Unit> =
        apiCall { client.post(Documents.Delete(documentId = documentId)) }
}
