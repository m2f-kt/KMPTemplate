package com.m2f.server.privacy.wire

import com.m2f.server.privacy.di.privacyModule
import com.m2f.server.privacy.registerPrivacyMigrations
import org.koin.dsl.module

/**
 * Wire module that aggregates privacy's internal DI modules.
 * Exposes only contract types to consumers.
 */
val privacyWireModule = module {
    includes(privacyModule)
}

/**
 * Delegates to impl's migration registration.
 */
fun registerPrivacyWireMigrations() {
    registerPrivacyMigrations()
}
