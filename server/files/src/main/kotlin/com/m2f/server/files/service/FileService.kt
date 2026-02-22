@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.files.service

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.ensure
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import com.m2f.core.config.configuration.Configuration
import com.m2f.core.config.server.DomainError
import com.m2f.server.files.errors.FileTooLarge
import com.m2f.server.files.errors.FileUploadFailed
import com.m2f.server.files.errors.FileUnsupportedType
import com.m2f.template.models.dto.FileResponse
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Service for uploading files to S3-compatible storage (MinIO).
 * Uses Arrow Raise for error handling — zero try/catch for domain errors.
 */
class FileService(
    private val s3Client: S3Client,
    private val config: Configuration,
) {
    companion object {
        const val MAX_FILE_SIZE: Long = 10 * 1024 * 1024 // 10MB
        val ALLOWED_CONTENT_TYPES = setOf(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "text/plain", "text/markdown",
        )
    }

    /**
     * Upload a file to S3. Validates size and content type.
     * Returns [FileResponse] with file metadata and URL.
     */
    context(raise: Raise<DomainError>)
    suspend fun upload(
        userId: String,
        fileName: String,
        contentType: String,
        bytes: ByteArray,
    ): FileResponse {
        // 1. Validate size
        raise.ensure(bytes.size.toLong() <= MAX_FILE_SIZE) { FileTooLarge() }

        // 2. Validate content type
        raise.ensure(contentType in ALLOWED_CONTENT_TYPES) { FileUnsupportedType() }

        // 3. Generate key with userId prefix and UUID-based filename
        val fileId = Uuid.random().toString()
        val extension = fileName.substringAfterLast('.', "bin")
        val key = "$userId/$fileId.$extension"

        // 4. Upload to S3
        catch({
            s3Client.putObject(PutObjectRequest {
                bucket = config.env.s3.bucket
                this.key = key
                body = ByteStream.fromBytes(bytes)
                this.contentType = contentType
            })
        }) {
            raise.raise(FileUploadFailed())
        }

        // 5. Construct URL (direct URL for MinIO; use presigned for production)
        val url = "${config.env.s3.endpoint}/${config.env.s3.bucket}/$key"

        // 6. Return response
        val now = Clock.System.now().toString()
        return FileResponse(
            id = fileId,
            key = key,
            originalName = fileName,
            contentType = contentType,
            size = bytes.size.toLong(),
            url = url,
            createdAt = now,
        )
    }

    /**
     * Get a URL for an existing file.
     * For MinIO local dev, constructs a direct URL.
     * For production, this should use presigned URLs.
     */
    @Suppress("UnusedParameter")
    fun getPresignedUrl(fileKey: String): String =
        "${config.env.s3.endpoint}/${config.env.s3.bucket}/$fileKey"
}
