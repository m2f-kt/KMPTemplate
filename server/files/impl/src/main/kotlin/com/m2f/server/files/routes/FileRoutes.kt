package com.m2f.server.files.routes

import com.m2f.core.config.server.InvalidContent
import com.m2f.core.config.server.conduitAuth
import com.m2f.server.files.contract.service.FileService
import com.m2f.template.models.routes.Files
import arrow.core.raise.context.ensureNotNull
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveMultipart
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.utils.io.toByteArray

/**
 * File upload and retrieval routes.
 * All routes require JWT authentication.
 */
fun Route.fileRoutes(fileService: FileService) {
    authenticate {
        post<Files.Upload> {
            val multipart = call.receiveMultipart()
            var fileName = "unknown"
            var contentType = "application/octet-stream"
            var fileBytes: ByteArray? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        fileName = part.originalFileName ?: "unknown"
                        contentType = part.contentType?.toString() ?: "application/octet-stream"
                        fileBytes = part.provider().toByteArray()
                    }
                    else -> {}
                }
                part.dispose()
            }

            conduitAuth(HttpStatusCode.Created) { userId ->
                val bytes = ensureNotNull(fileBytes) { InvalidContent }
                fileService.upload(userId, fileName, contentType, bytes)
            }
        }

        get<Files.Get> { route ->
            conduitAuth { _ ->
                val url = fileService.getPresignedUrl(route.fileKey)
                mapOf("url" to url)
            }
        }
    }
}
