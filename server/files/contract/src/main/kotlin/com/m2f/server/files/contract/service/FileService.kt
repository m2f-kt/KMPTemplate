package com.m2f.server.files.contract.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.template.models.dto.FileResponse

interface FileService {
    context(raise: Raise<DomainError>)
    suspend fun upload(userId: String, fileName: String, contentType: String, bytes: ByteArray): FileResponse

    fun getPresignedUrl(fileKey: String): String
}
