package com.m2f.server.auth.wire

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.server.auth.contract.service.AuthService
import com.m2f.server.auth.di.authModule
import com.m2f.server.auth.di.emailModule
import com.m2f.server.auth.registerAuthMigrations
import com.m2f.server.auth.service.AuthServiceImpl
import com.m2f.template.models.dto.RegisterRequest
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Wire module that aggregates auth's internal DI modules.
 * Exposes only contract types to consumers.
 */
val authWireModule = module {
    includes(authModule)
    includes(emailModule)
}

/**
 * Registers AuthService with an optional cross-module callback.
 * This factory pattern lets the server aggregator wire the groups callback
 * without accessing impl types.
 */
fun Module.registerAuthService(
    onRegistered: (suspend (raise: Raise<DomainError>, userId: String, request: RegisterRequest) -> Unit)? = null,
) {
    single<AuthService> {
        AuthServiceImpl(
            userRepository = get(),
            refreshTokenRepository = get(),
            passwordHasher = get(),
            tokenProvider = get(),
            onRegistered = onRegistered,
        )
    }
}

/**
 * Delegates to impl's migration registration.
 */
fun registerAuthWireMigrations() {
    registerAuthMigrations()
}
