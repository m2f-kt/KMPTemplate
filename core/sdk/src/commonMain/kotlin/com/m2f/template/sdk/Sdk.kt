package com.m2f.template.sdk

import com.m2f.template.sdk.api.AuthApi
import com.m2f.template.sdk.api.UserApi

/**
 * Facade that combines all SDK API interfaces via Kotlin delegation.
 *
 * Useful for tests and scenarios where a single entry-point to the SDK is preferred.
 * Each method call is delegated to the corresponding API implementation.
 */
class Sdk(
    private val authApi: AuthApi,
    private val userApi: UserApi,
) : AuthApi by authApi, UserApi by userApi
