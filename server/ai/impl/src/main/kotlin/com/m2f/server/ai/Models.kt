package com.m2f.server.ai

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

/**
 * Custom LLM model constants for this project.
 *
 * text-embedding-004 outputs 768 dimensions natively, matching the pgvector column
 * defined in DocumentEmbeddingsTable. We use this instead of gemini-embedding-001
 * (which outputs 3072 dims by default) because Koog 0.6.2's GoogleEmbeddingRequest
 * does not support the outputDimensionality parameter.
 *
 * TODO: When Koog adds outputDimensionality support, migrate to gemini-embedding-001
 *       with 768 dims configured, or re-create pgvector column at 3072 dims.
 */
val TextEmbedding004 = LLModel(
    provider = LLMProvider.Google,
    id = "text-embedding-004",
    capabilities = listOf(LLMCapability.Embed),
    contextLength = 2048,
)
