package com.m2f.template.di

import org.koin.dsl.module

/**
 * Client application DI module.
 * Combines shared module with client-specific dependencies.
 */
val appModule = module {
    // Client-specific dependencies (ViewModels, repositories, etc.) go here
    // Populated in subsequent phases as features are built
}

/**
 * All modules that make up the client application DI graph.
 */
val allAppModules = listOf(
    sharedModule,
    appModule,
)
