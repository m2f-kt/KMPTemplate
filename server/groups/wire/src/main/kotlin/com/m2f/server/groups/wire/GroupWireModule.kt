package com.m2f.server.groups.wire

import com.m2f.server.groups.di.groupModule
import com.m2f.server.groups.registerGroupMigrations
import org.koin.dsl.module

/**
 * Wire module that aggregates groups' internal DI modules.
 * Exposes only contract types to consumers.
 */
val groupWireModule = module {
    includes(groupModule)
}

/**
 * Delegates to impl's migration registration.
 */
fun registerGroupWireMigrations() {
    registerGroupMigrations()
}
