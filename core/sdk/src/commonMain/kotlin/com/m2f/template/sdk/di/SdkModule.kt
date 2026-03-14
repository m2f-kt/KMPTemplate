package com.m2f.template.sdk.di

import com.m2f.template.sdk.AuthInterceptor
import com.m2f.template.sdk.Sdk
import com.m2f.template.sdk.api.AuthApi
import com.m2f.template.sdk.api.AuthApiImpl
import com.m2f.template.sdk.api.DocumentApi
import com.m2f.template.sdk.api.DocumentApiImpl
import com.m2f.template.sdk.api.FileApi
import com.m2f.template.sdk.api.PrivacyApi
import com.m2f.template.sdk.api.PrivacyApiImpl
import com.m2f.template.sdk.api.FileApiImpl
import com.m2f.template.sdk.api.GroupApi
import com.m2f.template.sdk.api.GroupApiImpl
import com.m2f.template.sdk.api.InvitationApi
import com.m2f.template.sdk.api.InvitationApiImpl
import com.m2f.template.sdk.api.UserApi
import com.m2f.template.sdk.api.UserApiImpl
import com.m2f.template.sdk.createApiClient
import com.m2f.template.sdk.defaultBaseUrl
import com.m2f.template.storage.PreferencesStorage
import org.koin.dsl.module

/**
 * Koin module wiring all SDK dependencies:
 * - [AuthInterceptor] for bearer token attachment and 401 refresh+retry
 * - [HttpClient] via [createApiClient] with interceptor installed
 * - [AuthApi] for register, login, refresh, logout
 * - [UserApi] for getProfile, updateProfile, getUserById
 * - [GroupApi] for group CRUD and member management
 * - [InvitationApi] for group invitation operations
 *
 * Depends on [TokenStorage] from storageModule being available in the DI graph.
 */
val sdkModule = module {
    single {
        AuthInterceptor(tokenStorage = get())
    }
    single {
        val storage: PreferencesStorage = get()
        createApiClient(
            authInterceptor = get(),
            baseUrl = defaultBaseUrl(),
            localeProvider = { storage.language },
        )
    }
    single<AuthApi> { AuthApiImpl(client = get(), tokenStorage = get()) }
    single<UserApi> { UserApiImpl(client = get()) }
    single<GroupApi> { GroupApiImpl(client = get()) }
    single<FileApi> { FileApiImpl(client = get()) }
    single<InvitationApi> { InvitationApiImpl(client = get()) }
    single<DocumentApi> { DocumentApiImpl(client = get()) }
    single<PrivacyApi> { PrivacyApiImpl(client = get()) }
    single { Sdk(authApi = get(), userApi = get(), groupApi = get(), fileApi = get(), invitationApi = get(), documentApi = get(), privacyApi = get()) }
}
