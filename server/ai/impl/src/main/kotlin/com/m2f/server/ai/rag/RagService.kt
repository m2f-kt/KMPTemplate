@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.ai.rag

import ai.koog.embeddings.local.LLMEmbedder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.slf4j.LoggerFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Result of a RAG retrieval query.
 * Contains relevant document chunks for prompt injection.
 */
data class RagContext(val chunks: List<String>)

/**
 * RAG retrieval service: embeds queries, searches pgvector, and formats context.
 *
 * Uses auto-relevance detection via RelevanceDetector to avoid unnecessary
 * retrieval calls for queries that don't need document context.
 *
 * Creates a scoped PgVectorStorage per request for multi-tenant isolation.
 */
class RagService(
    private val embedder: LLMEmbedder,
    private val db: R2dbcDatabase,
    private val relevanceDetector: RelevanceDetector,
    private val aiDispatcher: CoroutineDispatcher,
) {
    private val logger = LoggerFactory.getLogger(RagService::class.java)

    /**
     * Check if a query needs document context and retrieve if so.
     * Returns null if the query doesn't need RAG context (general knowledge, casual chat, etc.).
     *
     * @param query The user's query text
     * @param groupId Group to scope document search
     * @param userId User making the query
     * @param isAdmin Whether user is admin in the group (sees all docs vs only personal)
     * @param topK Maximum number of chunks to retrieve
     */
    suspend fun checkAndRetrieve(
        query: String,
        groupId: Uuid,
        userId: Uuid,
        isAdmin: Boolean,
        topK: Int = 5,
    ): RagContext? {
        return try {
            val check = relevanceDetector.check(query)
            if (!check.needsContext) return null

            // Use optimized search query if available, fall back to original
            val searchQuery = check.searchQuery ?: query
            retrieve(searchQuery, groupId, userId, isAdmin, topK)
        } catch (e: Exception) {
            logger.error("RAG check-and-retrieve failed, skipping context", e)
            null // Fail open: don't break chat if RAG fails
        }
    }

    /**
     * Retrieve relevant document chunks from pgvector.
     * Creates a scoped PgVectorStorage per call for multi-tenant isolation.
     *
     * @return RagContext with chunks, or null if no relevant chunks found
     */
    suspend fun retrieve(
        query: String,
        groupId: Uuid,
        userId: Uuid,
        isAdmin: Boolean,
        topK: Int = 5,
    ): RagContext? {
        val storage = PgVectorStorage(
            db = db,
            groupId = groupId,
            userId = userId,
            isAdmin = isAdmin,
        )

        // Embed the query
        val queryVector = withContext(aiDispatcher) {
            embedder.embed(query)
        }

        // Search for similar chunks with minimum similarity threshold
        val results = storage.searchSimilar(queryVector, topK, minSimilarity = 0.3)
        if (results.isEmpty()) return null

        return RagContext(chunks = results.map { it.content })
    }

    /**
     * Format RAG context for injection into the system prompt.
     * Hidden context: no citations or source attribution per user decision.
     */
    fun formatContext(ragContext: RagContext): String = buildString {
        appendLine("[Document Context]")
        appendLine("The following excerpts from uploaded documents may be relevant to the user's question.")
        appendLine("Use them to provide accurate, informed answers. Do not mention these documents unless the user asks about them.")
        appendLine()
        for (chunk in ragContext.chunks) {
            appendLine("---")
            appendLine(chunk)
        }
        appendLine("---")
    }
}
