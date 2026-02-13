package com.m2f.server.ai.di

import com.m2f.core.config.configuration.Configuration
import com.m2f.server.ai.agents.AssistantAgentService
import com.m2f.server.ai.agents.ChatAgentService
import com.m2f.server.ai.persistence.ExposedPersistenceStorage
import com.m2f.server.ai.tools.UserTools
import com.m2f.server.auth.repository.UserRepository
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.dsl.module

/**
 * Koin module wiring all AI dependencies.
 * Uses standalone executor approach -- agents create their own OpenAI executor
 * with the API key from Configuration, no Koog Ktor plugin needed.
 */
val aiModule = module {
    single { UserTools(get<UserRepository>()) }
    single { ExposedPersistenceStorage(get<R2dbcDatabase>()) }
    single {
        AssistantAgentService(
            userTools = get(),
            openaiApiKey = get<Configuration>().env.ai.openaiApiKey,
        )
    }
    single {
        ChatAgentService(
            persistenceStorage = get(),
            openaiApiKey = get<Configuration>().env.ai.openaiApiKey,
        )
    }
}
