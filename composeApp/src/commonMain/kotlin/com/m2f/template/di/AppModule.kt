package com.m2f.template.di

import com.m2f.template.app.admin.wire.adminModule
import com.m2f.template.app.auth.wire.authModule
import com.m2f.template.app.dashboard.wire.dashboardModule
import com.m2f.template.app.documents.wire.documentsModule
import com.m2f.template.app.privacy.wire.privacyModule
import com.m2f.template.app.profile.wire.profileModule
import org.koin.dsl.module

/**
 * Client application DI module.
 * Combines shared module with client-specific dependencies.
 */
val appModule = module {
    includes(
        authModule,
        adminModule,
        dashboardModule,
        documentsModule,
        privacyModule,
        profileModule,
    )
}

/**
 * All modules that make up the client application DI graph.
 */
val allAppModules = listOf(
    sharedModule,
    appModule,
)
