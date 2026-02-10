package com.m2f.template.di

import org.koin.dsl.module

/**
 * Server DI module aggregating all server feature modules.
 */
val serverModule = module {
    // Server-specific dependencies (repositories, services)
    // Populated in Phase 2 as server features are built
}
