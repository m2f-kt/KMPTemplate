package com.m2f.server.ai.rag

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.dsl.prompt
import com.m2f.server.ai.structured.StructuredOutputService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Structured output data class for relevance detection.
 * Demonstrates the @Serializable + @LLMDescription pattern for Koog structured output.
 */
@Serializable
@SerialName("RelevanceCheck")
@LLMDescription("Determines if a user query needs document context to answer")
data class RelevanceCheck(
    @property:LLMDescription("Whether the query needs document context to answer well")
    val needsContext: Boolean,
    @property:LLMDescription("Optimized search query for vector similarity if context needed, null if not needed")
    val searchQuery: String?,
)

/**
 * Uses structured output to determine if a user query needs document context.
 * This enables auto-RAG detection: only queries that would benefit from document
 * context trigger the retrieval pipeline.
 *
 * Fails open: on any error, defaults to needsContext=false so chat is never broken.
 */
class RelevanceDetector(
    private val structuredOutputService: StructuredOutputService,
) {

    private val systemPrompt = """
        |You are a relevance detection system. Your job is to determine whether a user's query
        |would benefit from having uploaded document context to provide a better answer.
        |
        |Queries that need document context:
        |- Questions about specific documents, files, or uploaded content
        |- Questions that reference "my documents", "my files", "the document I uploaded"
        |- Questions about topics that could be in uploaded reference materials
        |- Technical questions where uploaded documentation would help
        |
        |Queries that do NOT need document context:
        |- General knowledge questions (math, science, history)
        |- Casual conversation ("hello", "how are you")
        |- Questions about the application itself or its features
        |- Creative writing requests
        |- Code generation without specific project context
        |
        |If context is needed, provide an optimized search query that captures the key
        |concepts for vector similarity search. Remove filler words and focus on core terms.
    """.trimMargin()

    /**
     * Check if a user query needs document context for a better answer.
     * Returns RelevanceCheck with needsContext and optional optimized searchQuery.
     * On failure, defaults to needsContext=false (fail open -- don't break chat).
     */
    suspend fun check(userQuery: String): RelevanceCheck {
        val checkPrompt = prompt("relevance-check") {
            system(systemPrompt)
            user(userQuery)
        }
        return structuredOutputService.execute<RelevanceCheck>(checkPrompt)
            .getOrNull() ?: RelevanceCheck(needsContext = false, searchQuery = null)
    }
}
