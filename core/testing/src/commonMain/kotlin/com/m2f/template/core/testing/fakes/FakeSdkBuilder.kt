package com.m2f.template.core.testing.fakes

import com.m2f.template.core.testing.FakeSDKDsl
import com.m2f.template.sdk.Sdk

/**
 * DSL builder for creating a fake [Sdk] instance in tests.
 *
 * Composes [FakeAuthApiBuilder], [FakeUserApiBuilder], [FakeGroupApiBuilder],
 * [FakeFileApiBuilder], [FakeInvitationApiBuilder], [FakeDocumentApiBuilder],
 * and [FakePrivacyApiBuilder] so tests configure only the API methods they exercise,
 * while unconfigured paths fail fast.
 *
 * Usage:
 * ```kotlin
 * val sdk = fakeSdk {
 *     auth { login { _, _ -> Either.Right(AuthResponse(...)) } }
 *     user { getProfile { Either.Right(UserResponse(...)) } }
 *     group { createGroup { Either.Right(GroupResponse(...)) } }
 *     file { uploadFile { _, _, _ -> Either.Right(FileResponse(...)) } }
 *     invitation { createInvitation { _, _ -> Either.Right(InvitationResponse(...)) } }
 *     privacy { getActiveConsents { Either.Right(emptyList()) } }
 * }
 * ```
 */
@FakeSDKDsl
class FakeSdkBuilder {

    private var authApiBuilder: FakeAuthApiBuilder = FakeAuthApiBuilder()
    private var userApiBuilder: FakeUserApiBuilder = FakeUserApiBuilder()
    private var groupApiBuilder: FakeGroupApiBuilder = FakeGroupApiBuilder()
    private var fileApiBuilder: FakeFileApiBuilder = FakeFileApiBuilder()
    private var invitationApiBuilder: FakeInvitationApiBuilder = FakeInvitationApiBuilder()
    private var documentApiBuilder: FakeDocumentApiBuilder = FakeDocumentApiBuilder()
    private var privacyApiBuilder: FakePrivacyApiBuilder = FakePrivacyApiBuilder()

    fun auth(init: FakeAuthApiBuilder.() -> Unit) {
        authApiBuilder.init()
    }

    fun user(init: FakeUserApiBuilder.() -> Unit) {
        userApiBuilder.init()
    }

    fun group(init: FakeGroupApiBuilder.() -> Unit) {
        groupApiBuilder.init()
    }

    fun file(init: FakeFileApiBuilder.() -> Unit) {
        fileApiBuilder.init()
    }

    fun invitation(init: FakeInvitationApiBuilder.() -> Unit) {
        invitationApiBuilder.init()
    }

    fun document(init: FakeDocumentApiBuilder.() -> Unit) {
        documentApiBuilder.init()
    }

    fun privacy(init: FakePrivacyApiBuilder.() -> Unit) {
        privacyApiBuilder.init()
    }

    internal fun build(): Sdk {
        return Sdk(
            authApi = authApiBuilder.build(),
            userApi = userApiBuilder.build(),
            groupApi = groupApiBuilder.build(),
            fileApi = fileApiBuilder.build(),
            invitationApi = invitationApiBuilder.build(),
            documentApi = documentApiBuilder.build(),
            privacyApi = privacyApiBuilder.build(),
        )
    }
}

/**
 * Top-level DSL entry point for creating a fake [Sdk].
 *
 * @param block optional configuration block to override default API behaviors
 * @return a configured [Sdk] instance backed by fake builders
 */
fun fakeSdk(block: FakeSdkBuilder.() -> Unit = {}): Sdk =
    FakeSdkBuilder().apply(block).build()
