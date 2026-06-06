@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.ai.rag

import ai.koog.embeddings.base.Vector
import ai.koog.rag.base.DocumentWithPayload
import com.m2f.core.database.tables.DocumentEmbeddingsTable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.StatementType
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Result of a cosine similarity search against pgvector.
 */
data class SimilarChunk(
    val content: String,
    val similarity: Double,
    val documentId: String?,
)

/**
 * VectorStorage<String> implementation backed by pgvector cosine similarity.
 *
 * Uses the pgvector <=> operator for server-side similarity ranking instead of
 * loading all vectors into memory (see research pitfall #3).
 *
 * Scoped by groupId for multi-tenant isolation. userId and isAdmin control
 * the personal vs group document scope within a group.
 *
 * Double<->Float conversion happens at the storage boundary:
 * - Koog Vector uses List<Double>
 * - pgvector/VectorColumnType stores List<Float>
 */
class PgVectorStorage(
    private val db: R2dbcDatabase,
    private val groupId: Uuid,
    private val userId: Uuid?,
    private val isAdmin: Boolean,
) {

    suspend fun store(document: String, data: Vector): String {
        val id = Uuid.random()
        suspendTransaction(db = db) {
            DocumentEmbeddingsTable.insert {
                it[DocumentEmbeddingsTable.id] = id
                it[DocumentEmbeddingsTable.groupId] = this@PgVectorStorage.groupId
                it[content] = document
                it[embedding] = data.values.map { v -> v.toFloat() }
            }
        }
        return id.toString()
    }

    suspend fun delete(documentId: String): Boolean {
        val uuid = Uuid.parse(documentId)
        val affected = suspendTransaction(db = db) {
            DocumentEmbeddingsTable.deleteWhere {
                DocumentEmbeddingsTable.id eq uuid
            }
        }
        return affected > 0
    }

    suspend fun read(documentId: String): String? {
        val uuid = Uuid.parse(documentId)
        return suspendTransaction(db = db) {
            DocumentEmbeddingsTable
                .select(DocumentEmbeddingsTable.content)
                .where { DocumentEmbeddingsTable.id eq uuid }
                .toList()
                .firstOrNull()
                ?.get(DocumentEmbeddingsTable.content)
        }
    }

    suspend fun getPayload(documentId: String): Vector? {
        val uuid = Uuid.parse(documentId)
        return suspendTransaction(db = db) {
            DocumentEmbeddingsTable
                .select(DocumentEmbeddingsTable.embedding)
                .where { DocumentEmbeddingsTable.id eq uuid }
                .toList()
                .firstOrNull()
                ?.get(DocumentEmbeddingsTable.embedding)
                ?.let { floats -> Vector(floats.map { it.toDouble() }) }
        }
    }

    suspend fun readWithPayload(documentId: String): DocumentWithPayload<String, Vector>? {
        val uuid = Uuid.parse(documentId)
        return suspendTransaction(db = db) {
            DocumentEmbeddingsTable
                .select(DocumentEmbeddingsTable.content, DocumentEmbeddingsTable.embedding)
                .where { DocumentEmbeddingsTable.id eq uuid }
                .toList()
                .firstOrNull()
                ?.let { row ->
                    DocumentWithPayload(
                        document = row[DocumentEmbeddingsTable.content],
                        payload = Vector(row[DocumentEmbeddingsTable.embedding].map { it.toDouble() }),
                    )
                }
        }
    }

    fun allDocuments(): Flow<String> = flow {
        val results = suspendTransaction(db = db) {
            DocumentEmbeddingsTable
                .select(DocumentEmbeddingsTable.content)
                .where { DocumentEmbeddingsTable.groupId eq groupId }
                .toList()
        }
        for (row in results) {
            emit(row[DocumentEmbeddingsTable.content])
        }
    }

    fun allDocumentsWithPayload(): Flow<DocumentWithPayload<String, Vector>> = flow {
        val results = suspendTransaction(db = db) {
            DocumentEmbeddingsTable
                .select(DocumentEmbeddingsTable.content, DocumentEmbeddingsTable.embedding)
                .where { DocumentEmbeddingsTable.groupId eq groupId }
                .toList()
        }
        for (row in results) {
            emit(
                DocumentWithPayload(
                    document = row[DocumentEmbeddingsTable.content],
                    payload = Vector(row[DocumentEmbeddingsTable.embedding].map { it.toDouble() }),
                ),
            )
        }
    }

    /**
     * Search for the most similar document chunks using pgvector cosine similarity.
     * Uses the <=> operator for server-side ranking (O(1) index lookup vs O(n) memory).
     *
     * IMPORTANT: Do NOT use EmbeddingBasedDocumentStorage.rankDocuments() for retrieval
     * as it loads ALL vectors into memory (research pitfall #3).
     *
     * Uses R2DBC exec with Row transform to execute raw pgvector SQL.
     */
    suspend fun searchSimilar(
        queryVector: Vector,
        topK: Int = 5,
        minSimilarity: Double = 0.0,
    ): List<SimilarChunk> {
        val vectorStr = "[${queryVector.values.joinToString(",")}]"
        val scopeClause = scopeClause()
        val sql = """
            SELECT id, content, document_id, 1 - (embedding <=> '$vectorStr'::vector) as similarity
            FROM document_embeddings
            WHERE group_id = '$groupId'
            $scopeClause
            AND 1 - (embedding <=> '$vectorStr'::vector) >= $minSimilarity
            ORDER BY embedding <=> '$vectorStr'::vector
            LIMIT $topK
        """.trimIndent()

        return suspendTransaction(db = db) {
            val flow = exec(sql, explicitStatementType = StatementType.SELECT) { row ->
                SimilarChunk(
                    content = row.get("content", String::class.java) ?: "",
                    similarity = row.get("similarity", Double::class.javaObjectType) ?: 0.0,
                    documentId = row.get("document_id", String::class.java),
                )
            }
            flow?.toList()?.filterNotNull() ?: emptyList()
        }
    }

    /**
     * Delete all embeddings for a specific document.
     * Used for cleanup when a document is deleted.
     */
    suspend fun deleteByDocumentId(docId: Uuid) {
        suspendTransaction(db = db) {
            DocumentEmbeddingsTable.deleteWhere {
                DocumentEmbeddingsTable.documentId eq docId
            }
        }
    }

    /**
     * Returns SQL scope clause based on user role:
     * - Members: only see their own personal documents
     * - Admins: see all documents within the group (no additional filter)
     */
    private fun scopeClause(): String = if (!isAdmin && userId != null) {
        "AND user_id = '$userId'"
    } else {
        ""
    }
}
