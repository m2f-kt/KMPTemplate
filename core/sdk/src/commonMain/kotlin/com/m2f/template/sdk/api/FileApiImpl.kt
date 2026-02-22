package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.FileResponse
import com.m2f.template.models.routes.Files
import com.m2f.template.sdk.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class FileApiImpl(private val client: HttpClient) : FileApi {

    override suspend fun uploadFile(
        fileName: String,
        bytes: ByteArray,
        contentType: String,
    ): Either<AppError, FileResponse> = apiCall {
        client.submitFormWithBinaryData(
            url = "/api/files/upload",
            formData = formData {
                append("file", bytes, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    append(HttpHeaders.ContentType, contentType)
                })
            }
        )
    }

    override suspend fun getFileUrl(fileKey: String): Either<AppError, Map<String, String>> =
        apiCall {
            client.get(Files.Get(fileKey = fileKey))
        }
}
