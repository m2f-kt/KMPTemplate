package com.m2f.template.di

import com.m2f.server.auth.di.authModule
import org.koin.dsl.module

/**
 * Server DI module aggregating all server feature modules.
 */
val serverModule = module {
    includes(authModule)
}
