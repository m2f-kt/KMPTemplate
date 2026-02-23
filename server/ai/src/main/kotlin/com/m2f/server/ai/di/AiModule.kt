package com.m2f.server.ai.di

import ai.koog.embeddings.local.LLMEmbedder
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import com.m2f.core.config.configuration.Configuration
import com.m2f.server.ai.TextEmbedding004
import com.m2f.server.ai.agents.AssistantAgentService
import com.m2f.server.ai.agents.ChatAgentService
import com.m2f.server.ai.persistence.ExposedPersistenceStorage
import com.m2f.server.ai.rag.DocumentIngestionService
import com.m2f.server.ai.rag.DocumentRepository
import com.m2f.server.ai.tools.UserTools
import com.m2f.server.auth.repository.UserRepository
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.dsl.module

/**
 * Koin module wiring all AI dependencies.
 * Uses standalone executor approach -- agents create their own Google executor
 * with the API key from Configuration, no Koog Ktor plugin needed.
 *
 * Also provides RAG pipeline dependencies:
 * - GoogleLLMClient singleton (shared across agents + embedder)
 * - LLMEmbedder for text-embedding-004 embeddings
 * - DocumentRepository for document CRUD
 * - DocumentIngestionService for chunking + embedding pipeline
 */
val aiModule = module {
    // Shared Google LLM client
    single {
        GoogleLLMClient(get<Configuration>().env.ai.googleApiKey)
    }

    single { UserTools(get<UserRepository>()) }
    single { ExposedPersistenceStorage(get<R2dbcDatabase>()) }
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
        )
    }

    // RAG pipeline dependencies
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
}
