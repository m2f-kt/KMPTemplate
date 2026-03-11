package com.m2f.server.auth.di

import com.m2f.core.config.configuration.Configuration
import com.m2f.server.auth.repository.PasswordResetTokenRepository
import com.m2f.server.auth.repository.RefreshTokenRepository
import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.server.auth.repository.ExposedUserRepository
import com.m2f.server.auth.security.JwtTokenProvider
import com.m2f.server.auth.contract.security.PasswordHasher
import com.m2f.server.auth.security.BcryptPasswordHasher
import com.m2f.server.auth.service.OAuthService
import com.m2f.server.auth.service.PasswordResetService
import com.m2f.server.auth.service.UserService
import io.ktor.client.HttpClient
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.dsl.module

/**
 * Koin module wiring auth dependencies (repositories, security, and supporting services).
 * AuthService is NOT registered here — it's registered in serverModule with the
 * invitation acceptance callback properly wired (see ServerModule.kt).
 */
val authModule = module {
    single<PasswordHasher> { BcryptPasswordHasher(get<Configuration>().computeDispatcher) }
    single { JwtTokenProvider(get<Configuration>()) }
    single<UserRepository> { ExposedUserRepository(get<R2dbcDatabase>()) }
    single { RefreshTokenRepository(get<R2dbcDatabase>()) }
    single { PasswordResetTokenRepository(get<R2dbcDatabase>()) }
    single { UserService(get()) }
    single { OAuthService(get(), get(), get(), get<HttpClient>(), get()) }
    single { PasswordResetService(get(), get(), get(), get(), get(), get()) }
}
