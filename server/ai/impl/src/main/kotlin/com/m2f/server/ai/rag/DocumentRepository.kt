@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.ai.rag

import com.m2f.core.database.tables.DocumentsTable
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a row from the documents table.
 */
data class DocumentRow(
    val id: Uuid,
    val groupId: Uuid,
    val userId: Uuid,
    val assignedToUserId: Uuid?,
    val name: String,
    val fileKey: String,
    val contentType: String,
    val scope: String,
    val status: String,
    val chunkCount: Int,
    val createdAt: LocalDateTime,
)

/**
 * Database access for document metadata (DocumentsTable).
 * Follows the UserRepository/MembershipRepository pattern.
 */
class DocumentRepository(private val db: R2dbcDatabase) {

    suspend fun create(
        groupId: Uuid,
        userId: Uuid,
        name: String,
        fileKey: String,
        contentType: String,
        scope: String,
        assignedToUserId: Uuid?,
    ): Uuid {
        val id = Uuid.random()
        suspendTransaction(db = db) {
            DocumentsTable.insert {
                it[DocumentsTable.id] = id
                it[DocumentsTable.groupId] = groupId
                it[DocumentsTable.userId] = userId
                // Only bind assignedToUserId when present. Exposed-R2DBC cannot encode a NULL of the
                // custom `kotlin.uuid.Uuid` column type ("Cannot encode null parameter of type
                // kotlin.uuid.Uuid"); omitting the column lets the nullable column default to NULL.
                assignedToUserId?.let { uid -> it[DocumentsTable.assignedToUserId] = uid }
                it[DocumentsTable.name] = name
                it[DocumentsTable.fileKey] = fileKey
                it[DocumentsTable.contentType] = contentType
                it[DocumentsTable.scope] = scope
            }
        }
        return id
    }

    suspend fun findById(id: Uuid): DocumentRow? =
        suspendTransaction(db = db) {
            DocumentsTable
                .select(DocumentsTable.columns)
                .where { DocumentsTable.id eq id }
                .toList()
                .firstOrNull()
                ?.toDocumentRow()
        }

    suspend fun findByGroupId(groupId: Uuid, scope: String? = null): List<DocumentRow> =
        suspendTransaction(db = db) {
            val query = DocumentsTable
                .select(DocumentsTable.columns)
                .where {
                    if (scope != null) {
                        (DocumentsTable.groupId eq groupId) and (DocumentsTable.scope eq scope)
                    } else {
                        DocumentsTable.groupId eq groupId
                    }
                }
            query.toList().map { it.toDocumentRow() }
        }

    /**
     * Find documents for a member within a group.
     * Returns own personal docs + docs assigned to this member by an admin.
     */
    suspend fun findByUserId(userId: Uuid, groupId: Uuid): List<DocumentRow> =
        suspendTransaction(db = db) {
            DocumentsTable
                .select(DocumentsTable.columns)
                .where {
                    (DocumentsTable.groupId eq groupId) and
                        ((DocumentsTable.userId eq userId) or (DocumentsTable.assignedToUserId eq userId))
                }
                .toList()
                .map { it.toDocumentRow() }
        }

    suspend fun updateStatus(id: Uuid, status: String, chunkCount: Int = 0) {
        suspendTransaction(db = db) {
            DocumentsTable.update({ DocumentsTable.id eq id }) {
                it[DocumentsTable.status] = status
                it[DocumentsTable.chunkCount] = chunkCount
            }
        }
    }

    suspend fun delete(id: Uuid): Boolean {
        val affected = suspendTransaction(db = db) {
            DocumentsTable.deleteWhere { DocumentsTable.id eq id }
        }
        return affected > 0
    }
}

private fun org.jetbrains.exposed.v1.core.ResultRow.toDocumentRow() = DocumentRow(
    id = this[DocumentsTable.id],
    groupId = this[DocumentsTable.groupId],
    userId = this[DocumentsTable.userId],
    assignedToUserId = this[DocumentsTable.assignedToUserId],
    name = this[DocumentsTable.name],
    fileKey = this[DocumentsTable.fileKey],
    contentType = this[DocumentsTable.contentType],
    scope = this[DocumentsTable.scope],
    status = this[DocumentsTable.status],
    chunkCount = this[DocumentsTable.chunkCount],
    createdAt = this[DocumentsTable.createdAt],
)
