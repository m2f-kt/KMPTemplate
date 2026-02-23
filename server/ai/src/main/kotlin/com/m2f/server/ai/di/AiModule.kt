package com.m2f.server.ai.di

import ai.koog.embeddings.local.LLMEmbedder
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import com.m2f.core.config.configuration.Configuration
import com.m2f.server.ai.TextEmbedding004
import com.m2f.server.ai.agents.AssistantAgentService
import com.m2f.server.ai.agents.ChatAgentService
import com.m2f.server.ai.persistence.ExposedPersistenceStorage
import com.m2f.server.ai.rag.DocumentIngestionService
import com.m2f.server.ai.rag.DocumentRepository
import com.m2f.server.ai.rag.RagService
import com.m2f.server.ai.rag.RelevanceDetector
import com.m2f.server.ai.structured.StructuredOutputService
import com.m2f.server.ai.tools.UserTools
import com.m2f.server.auth.repository.UserRepository
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.dsl.module

/**
 * Koin module wiring all AI dependencies.
 *
 * Shared infrastructure:
 * - GoogleLLMClient singleton (shared across agents, embedder, structured output)
 * - SingleLLMPromptExecutor for StructuredOutputService
 *
 * Agent services:
 * - AssistantAgentService (ReAct agent with UserTools)
 * - ChatAgentService (conversational agent with persistence + RAG)
 *
 * RAG pipeline:
 * - LLMEmbedder for text-embedding-004 embeddings
 * - DocumentRepository for document CRUD
 * - DocumentIngestionService for chunking + embedding pipeline
 * - StructuredOutputService for typed LLM output
 * - RelevanceDetector for auto-RAG heuristic
 * - RagService for query embedding + retrieval + context formatting
 */
val aiModule = module {
    // Shared Google LLM client and executor
    single { GoogleLLMClient(get<Configuration>().env.ai.googleApiKey) }
    single { SingleLLMPromptExecutor(get<GoogleLLMClient>()) }

    // Agent infrastructure
    single { UserTools(get<UserRepository>()) }
    single { ExposedPersistenceStorage(get<R2dbcDatabase>()) }

    // Structured output and relevance detection
    single { StructuredOutputService(get<SingleLLMPromptExecutor>()) }
    single { RelevanceDetector(get<StructuredOutputService>()) }

    // RAG pipeline
    single { DocumentRepository(get<R2dbcDatabase>()) }
    single { LLMEmbedder(get<GoogleLLMClient>(), TextEmbedding004) }
    single {
        DocumentIngestionService(
            embedder = get(),
            db = get(),
            documentRepository = get(),
            aiDispatcher = get<Configuration>().aiDispatcher,
        )
    }
    single {
        RagService(
            embedder = get(),
            db = get(),
            relevanceDetector = get(),
            aiDispatcher = get<Configuration>().aiDispatcher,
        )
    }

    // Agent services
    single {
        AssistantAgentService(
            userTools = get(),
            googleApiKey = get<Configuration>().env.ai.googleApiKey,
        )
    }
    single {
        ChatAgentService(
            persistenceStorage = get(),
            googleApiKey = get<Configuration>().env.ai.googleApiKey,
            aiDispatcher = get<Configuration>().aiDispatcher,
            ragService = get(),
        )
    }
}
