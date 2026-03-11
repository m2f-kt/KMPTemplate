package com.m2f.server.ai.wire

import com.m2f.server.ai.di.aiModule
import com.m2f.server.ai.registerAiMigrations
import org.koin.dsl.module

/**
 * Wire module that aggregates AI's internal DI modules.
 * Exposes only contract types to consumers.
 */
val aiWireModule = module {
    includes(aiModule)
}

/**
 * Delegates to impl's migration registration.
 */
fun registerAiWireMigrations() {
    registerAiMigrations()
}
