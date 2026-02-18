package com.m2f.template.sdk.di

import com.m2f.template.sdk.AuthInterceptor
import com.m2f.template.sdk.Sdk
import com.m2f.template.sdk.api.AuthApi
import com.m2f.template.sdk.api.AuthApiImpl
import com.m2f.template.sdk.api.UserApi
import com.m2f.template.sdk.api.UserApiImpl
import com.m2f.template.sdk.createApiClient
import com.m2f.template.sdk.defaultBaseUrl
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
            baseUrl = defaultBaseUrl(),
        )
    }
    single<AuthApi> { AuthApiImpl(client = get(), tokenStorage = get()) }
    single<UserApi> { UserApiImpl(client = get()) }
    single { Sdk(authApi = get(), userApi = get()) }
}
