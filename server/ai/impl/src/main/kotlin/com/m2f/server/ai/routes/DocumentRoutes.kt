@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.ai.routes

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import com.m2f.core.config.configuration.Configuration
import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.InvalidContent
import com.m2f.core.config.server.conduitAuth
import com.m2f.server.ai.contract.errors.DocumentAccessDenied
import com.m2f.server.ai.contract.errors.DocumentNotFound
import com.m2f.server.ai.rag.DocumentIngestionService
import com.m2f.server.ai.rag.DocumentRepository
import com.m2f.template.models.dto.DocumentListResponse
import com.m2f.template.models.dto.DocumentResponse
import com.m2f.template.models.routes.Documents
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveMultipart
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.utils.io.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Supported document types for RAG ingestion.
 * Only text-based formats that can be directly decoded as UTF-8.
 */
private val ALLOWED_DOCUMENT_TYPES = setOf("text/plain", "text/markdown")
private val ALLOWED_EXTENSIONS = setOf("txt", "md")

/**
 * Document management routes for the RAG pipeline.
 * Upload, list, get, and delete documents with proper scope enforcement.
 *
 * @param documentIngestionService Handles chunking + embedding pipeline
 * @param documentRepository CRUD for DocumentsTable
 * @param fileUploader Lambda to upload file bytes to S3 (avoids direct FileService dependency)
 * @param fileDeleter Lambda to delete file from S3 by key
 * @param roleChecker Lambda to check if user is admin in a group (avoids groups module dependency)
 * @param aiDispatcher Dispatcher for background ingestion
 */
context(config: Configuration)
fun Route.documentRoutes(
    documentIngestionService: DocumentIngestionService,
    documentRepository: DocumentRepository,
    fileUploader: suspend context(Raise<DomainError>) (userId: String, fileName: String, contentType: String, bytes: ByteArray) -> String,
    fileDeleter: (String) -> Unit,
    roleChecker: suspend (userId: String, groupId: String) -> Boolean,
    aiDispatcher: kotlinx.coroutines.CoroutineDispatcher,
) {
    authenticate {
        // POST /api/documents/upload (multipart)
        post<Documents.Upload> {
            // Parse multipart OUTSIDE conduitAuth (RoutingContext.call not available in Raise context)
            val multipart = call.receiveMultipart()
            var fileName = "unknown"
            var contentType = "application/octet-stream"
            var fileBytes: ByteArray? = null
            var groupId: String? = null
            var scope: String? = null
            var assignedToUserId: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        fileName = part.originalFileName ?: "unknown"
                        contentType = part.contentType?.toString() ?: "application/octet-stream"
                        fileBytes = part.provider().toByteArray()
                    }
                    is PartData.FormItem -> {
                        when (part.name) {
                            "groupId" -> groupId = part.value
                            "scope" -> scope = part.value
                            "assignedToUserId" -> assignedToUserId = part.value
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            conduitAuth(HttpStatusCode.Created) { userId ->
                val bytes = ensureNotNull(fileBytes) { InvalidContent }
                val gid = ensureNotNull(groupId) { InvalidContent }
                val docScope = ensureNotNull(scope) { InvalidContent }

                // Validate scope
                ensure(docScope in listOf("personal", "group")) { InvalidContent }

                // Validate file type. An unsupported type is a CLIENT error (well-formed request,
                // unprocessable content) → 422 via the InvalidContent path, not 500. Previously this
                // raised DocumentIngestionFailed, whose respond() is `unexpected` (500) — wrong class.
                // Match the scope/null checks above.
                val ext = fileName.substringAfterLast('.', "").lowercase()
                ensure(contentType in ALLOWED_DOCUMENT_TYPES || ext in ALLOWED_EXTENSIONS) { InvalidContent }

                // Authorization: group scope and assignedToUserId require admin
                val isAdmin = roleChecker(userId, gid)
                if (docScope == "group") {
                    ensure(isAdmin) { DocumentAccessDenied("Only admins can upload group-scoped documents") }
                }
                if (assignedToUserId != null) {
                    ensure(isAdmin) { DocumentAccessDenied("Only admins can assign documents to members") }
                }

                // Upload file to S3
                val fileKey = fileUploader(userId, fileName, contentType, bytes)

                // Create document record
                val groupUuid = Uuid.parse(gid)
                val userUuid = Uuid.parse(userId)
                val assignedUuid = assignedToUserId?.let { Uuid.parse(it) }
                val documentId = documentRepository.create(
                    groupId = groupUuid,
                    userId = userUuid,
                    name = fileName,
                    fileKey = fileKey,
                    contentType = contentType,
                    scope = docScope,
                    assignedToUserId = assignedUuid,
                )

                // Launch ingestion in background (auto-ingest)
                val text = bytes.decodeToString()
                CoroutineScope(aiDispatcher).launch {
                    documentIngestionService.ingest(documentId, groupUuid, userUuid, text)
                }

                DocumentResponse(
                    id = documentId.toString(),
                    groupId = gid,
                    name = fileName,
                    contentType = contentType,
                    scope = docScope,
                    status = "pending",
                    chunkCount = 0,
                    assignedToUserId = assignedToUserId,
                    createdAt = kotlin.time.Clock.System.now().toString(),
                )
            }
        }

        // GET /api/documents/list?groupId=X&scope=Y
        get<Documents.ListByGroup> { route ->
            conduitAuth { userId ->
                val isAdmin = roleChecker(userId, route.groupId)
                val groupUuid = Uuid.parse(route.groupId)
                val userUuid = Uuid.parse(userId)

                val documents = if (isAdmin) {
                    documentRepository.findByGroupId(groupUuid, route.scope)
                } else {
                    documentRepository.findByUserId(userUuid, groupUuid)
                }

                DocumentListResponse(
                    documents = documents.map { doc ->
                        DocumentResponse(
                            id = doc.id.toString(),
                            groupId = doc.groupId.toString(),
                            name = doc.name,
                            contentType = doc.contentType,
                            scope = doc.scope,
                            status = doc.status,
                            chunkCount = doc.chunkCount,
                            assignedToUserId = doc.assignedToUserId?.toString(),
                            createdAt = doc.createdAt.toString(),
                        )
                    },
                )
            }
        }

        // GET /api/documents/{documentId}
        get<Documents.ById> { route ->
            conduitAuth { userId ->
                val docId = Uuid.parse(route.documentId)
                val doc = ensureNotNull(documentRepository.findById(docId)) { DocumentNotFound() }

                // Verify access
                val isAdmin = roleChecker(userId, doc.groupId.toString())
                val userUuid = Uuid.parse(userId)
                if (!isAdmin) {
                    ensure(doc.userId == userUuid || doc.assignedToUserId == userUuid) {
                        DocumentAccessDenied()
                    }
                }

                DocumentResponse(
                    id = doc.id.toString(),
                    groupId = doc.groupId.toString(),
                    name = doc.name,
                    contentType = doc.contentType,
                    scope = doc.scope,
                    status = doc.status,
                    chunkCount = doc.chunkCount,
                    assignedToUserId = doc.assignedToUserId?.toString(),
                    createdAt = doc.createdAt.toString(),
                )
            }
        }

        // POST /api/documents/{documentId}/delete
        post<Documents.Delete> { route ->
            conduitAuth { userId ->
                val docId = Uuid.parse(route.documentId)
                val doc = ensureNotNull(documentRepository.findById(docId)) { DocumentNotFound() }

                // Authorization: admin-assigned docs only deletable by admin; personal docs by owner
                val isAdmin = roleChecker(userId, doc.groupId.toString())
                val userUuid = Uuid.parse(userId)
                if (doc.assignedToUserId != null) {
                    ensure(isAdmin) { DocumentAccessDenied("Only admins can delete assigned documents") }
                } else if (doc.scope == "group") {
                    ensure(isAdmin) { DocumentAccessDenied("Only admins can delete group documents") }
                } else {
                    ensure(doc.userId == userUuid || isAdmin) { DocumentAccessDenied() }
                }

                // Cascade delete: embeddings -> S3 -> database
                documentIngestionService.deleteEmbeddings(docId)
                try { fileDeleter(doc.fileKey) } catch (_: Exception) { /* S3 cleanup is best-effort */ }
                documentRepository.delete(docId)

                mapOf("deleted" to true)
            }
        }
    }
}
