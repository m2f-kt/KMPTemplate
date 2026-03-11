@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.template.routes

import arrow.core.raise.context.ensureNotNull
import com.m2f.core.config.server.InvalidContent
import com.m2f.core.config.server.conduitAuth
import com.m2f.server.auth.contract.errors.UserNotFound
import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.server.files.contract.service.FileService
import com.m2f.template.models.dto.UserResponse
import com.m2f.template.models.routes.Users
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveMultipart
import io.ktor.server.resources.put
import io.ktor.server.routing.Route
import io.ktor.utils.io.toByteArray
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Avatar upload route.
 * Separated from UserRoutes due to cross-module dependency on FileService.
 * Requires JWT authentication.
 */
fun Route.avatarRoutes(
    userRepository: UserRepository,
    fileService: FileService,
) {
    authenticate {
        // Upload avatar image
        put<Users.Me.Avatar> {
            // Multipart parsing OUTSIDE conduitAuth (RoutingContext.call not available inside)
            val multipart = call.receiveMultipart()
            var fileName = "avatar"
            var contentType = "application/octet-stream"
            var fileBytes: ByteArray? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        fileName = part.originalFileName ?: "avatar"
                        contentType = part.contentType?.toString() ?: "application/octet-stream"
                        fileBytes = part.provider().toByteArray()
                    }
                    else -> {}
                }
                part.dispose()
            }

            conduitAuth { userId ->
                val bytes = ensureNotNull(fileBytes) { InvalidContent }
                ensureNotNull(contentType.takeIf { it.startsWith("image/") }) { InvalidContent }

                // Upload to S3 using FileService
                val fileResponse = fileService.upload(
                    userId = userId,
                    fileName = fileName,
                    contentType = contentType,
                    bytes = bytes,
                )

                // Update user's avatarUrl
                userRepository.updateAvatarUrl(Uuid.parse(userId), fileResponse.url)

                // Return updated user profile
                val user = userRepository.findById(Uuid.parse(userId))
                ensureNotNull(user) { UserNotFound() }
                UserResponse(
                    id = user.id.toString(),
                    email = user.email,
                    name = user.name,
                    role = user.role,
                    avatarUrl = user.avatarUrl,
                )
            }
        }
    }
}
