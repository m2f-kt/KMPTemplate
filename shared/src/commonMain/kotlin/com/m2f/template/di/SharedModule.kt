package com.m2f.template.di

import org.koin.dsl.module

/**
 * Shared DI module available to all KMP targets.
 * Contains platform-agnostic service definitions.
 */
val sharedModule = module {
    // Platform-agnostic dependencies go here
    // Features will add their own modules in subsequent phases
}
