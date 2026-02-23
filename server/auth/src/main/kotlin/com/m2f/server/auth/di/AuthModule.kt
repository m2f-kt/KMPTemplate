package com.m2f.server.auth.di

import com.m2f.core.config.configuration.Configuration
import com.m2f.server.auth.repository.PasswordResetTokenRepository
import com.m2f.server.auth.repository.RefreshTokenRepository
import com.m2f.server.auth.repository.UserRepository
import com.m2f.server.auth.security.JwtTokenProvider
import com.m2f.server.auth.security.PasswordHasher
import com.m2f.server.auth.service.AuthService
import com.m2f.server.auth.service.OAuthService
import com.m2f.server.auth.service.PasswordResetService
import com.m2f.server.auth.service.UserService
import io.ktor.client.HttpClient
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.dsl.module

/**
 * Koin module wiring all auth dependencies.
 * Note: AuthService is NOT registered here — it's registered in ServerModule
 * where InvitationService is available for the registration callback.
 */
val authModule = module {
    single { PasswordHasher(get<Configuration>().computeDispatcher) }
    single { JwtTokenProvider(get<Configuration>()) }
    single { UserRepository(get<R2dbcDatabase>()) }
    single { RefreshTokenRepository(get<R2dbcDatabase>()) }
    single { PasswordResetTokenRepository(get<R2dbcDatabase>()) }
    single { UserService(get()) }
    single { OAuthService(get(), get(), get(), get<HttpClient>(), get()) }
    single { PasswordResetService(get(), get(), get(), get(), get(), get()) }
}
