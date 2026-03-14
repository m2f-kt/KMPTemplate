package com.m2f.template.sdk

import com.m2f.template.sdk.api.AuthApi
import com.m2f.template.sdk.api.DocumentApi
import com.m2f.template.sdk.api.FileApi
import com.m2f.template.sdk.api.GroupApi
import com.m2f.template.sdk.api.InvitationApi
import com.m2f.template.sdk.api.PrivacyApi
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
    private val groupApi: GroupApi,
    private val fileApi: FileApi,
    private val invitationApi: InvitationApi,
    private val documentApi: DocumentApi,
    private val privacyApi: PrivacyApi,
) : AuthApi by authApi, UserApi by userApi, GroupApi by groupApi, FileApi by fileApi, InvitationApi by invitationApi, DocumentApi by documentApi, PrivacyApi by privacyApi
