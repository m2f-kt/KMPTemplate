package com.m2f.template.sdk.di

import com.m2f.template.sdk.AuthInterceptor
import com.m2f.template.sdk.api.AuthApi
import com.m2f.template.sdk.api.UserApi
import com.m2f.template.sdk.createApiClient
import org.koin.dsl.module

/**
 * Koin module wiring all SDK dependencies:
 * - [AuthInterceptor] for bearer token attachment and 401 refresh+retry
 * - [HttpClient] via [createApiClient] with interceptor installed
 * - [AuthApi] for register, login, refresh, logout
 * - [UserApi] for getProfile, updateProfile, getUserById
 *
 * Depends on [TokenStorage] from storageModule being available in the DI graph.
 */
val sdkModule = module {
    single {
        AuthInterceptor(tokenStorage = get())
    }
    single {
        createApiClient(
            authInterceptor = get(),
            baseUrl = getProperty("BASE_URL", "http://localhost:8080"),
        )
    }
    single { AuthApi(client = get(), tokenStorage = get()) }
    single { UserApi(client = get()) }
}
