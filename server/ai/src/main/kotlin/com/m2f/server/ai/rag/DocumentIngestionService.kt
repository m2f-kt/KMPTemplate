@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.ai.rag

import ai.koog.embeddings.local.LLMEmbedder
import com.m2f.core.database.tables.DocumentEmbeddingsTable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.slf4j.LoggerFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Document ingestion pipeline: chunk text, embed via Koog LLMEmbedder, store in pgvector.
 *
 * Uses character-based chunking (~500 chars per chunk, ~100 char overlap) to stay
 * well under text-embedding-004's 2048 token limit (~125 tokens per chunk).
 *
 * Auto-ingests on upload: the upload route triggers ingestion in a background coroutine.
 */
class DocumentIngestionService(
    private val embedder: LLMEmbedder,
    private val db: R2dbcDatabase,
    private val documentRepository: DocumentRepository,
    private val aiDispatcher: CoroutineDispatcher,
) {
    private val logger = LoggerFactory.getLogger(DocumentIngestionService::class.java)

    /**
     * Ingest a document: chunk, embed, and store vectors.
     * Updates document status throughout the process.
     * On failure, sets status to "failed" and logs the error (does NOT re-raise).
     */
    suspend fun ingest(
        documentId: Uuid,
        groupId: Uuid,
        userId: Uuid,
        text: String,
    ) {
        try {
            documentRepository.updateStatus(documentId, "processing")

            val chunks = chunkText(text)
            if (chunks.isEmpty()) {
                documentRepository.updateStatus(documentId, "indexed", chunkCount = 0)
                return
            }

            for ((index, chunk) in chunks.withIndex()) {
                // Embed via Koog LLMEmbedder (returns Vector with 768-dim List<Double>)
                val vector = withContext(aiDispatcher) {
                    embedder.embed(chunk)
                }

                // Store embedding in pgvector
                suspendTransaction(db = db) {
                    DocumentEmbeddingsTable.insert {
                        it[DocumentEmbeddingsTable.id] = Uuid.random()
                        it[DocumentEmbeddingsTable.groupId] = groupId
                        it[DocumentEmbeddingsTable.userId] = userId
                        it[DocumentEmbeddingsTable.documentId] = documentId
                        it[DocumentEmbeddingsTable.chunkIndex] = index
                        it[content] = chunk
                        it[embedding] = vector.values.map { v -> v.toFloat() }
                    }
                }
            }

            documentRepository.updateStatus(documentId, "indexed", chunkCount = chunks.size)
            logger.info("Ingested document $documentId: ${chunks.size} chunks embedded")
        } catch (e: Exception) {
            logger.error("Failed to ingest document $documentId", e)
            try {
                documentRepository.updateStatus(documentId, "failed")
            } catch (updateError: Exception) {
                logger.error("Failed to update document status to failed", updateError)
            }
        }
    }

    /**
     * Delete all embeddings for a document.
     * Used for cleanup when a document is deleted.
     */
    suspend fun deleteEmbeddings(documentId: Uuid) {
        suspendTransaction(db = db) {
            DocumentEmbeddingsTable.deleteWhere {
                DocumentEmbeddingsTable.documentId eq documentId
            }
        }
    }

    companion object {
        private const val DEFAULT_CHUNK_SIZE = 500
        private const val DEFAULT_OVERLAP = 100

        /**
         * Simple character-based text chunking.
         * ~500 chars per chunk with ~100 char overlap yields ~125 tokens,
         * well under text-embedding-004's 2048 token limit.
         */
        fun chunkText(
            text: String,
            chunkSize: Int = DEFAULT_CHUNK_SIZE,
            overlap: Int = DEFAULT_OVERLAP,
        ): List<String> {
            if (text.isBlank()) return emptyList()
            if (text.length <= chunkSize) return listOf(text.trim())

            val chunks = mutableListOf<String>()
            var start = 0
            while (start < text.length) {
                val end = minOf(start + chunkSize, text.length)
                chunks.add(text.substring(start, end).trim())
                start += chunkSize - overlap
            }
            return chunks.filter { it.isNotBlank() }
        }
    }
}
