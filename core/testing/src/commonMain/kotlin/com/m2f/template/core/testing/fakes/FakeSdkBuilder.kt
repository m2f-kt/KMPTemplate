package com.m2f.template.core.testing.fakes

import com.m2f.template.core.testing.FakeSDKDsl
import com.m2f.template.sdk.Sdk

/**
 * DSL builder for creating a fake [Sdk] instance in tests.
 *
 * Composes [FakeAuthApiBuilder] and [FakeUserApiBuilder] so tests configure
 * only the API methods they exercise, while unconfigured paths fail fast.
 *
 * Usage:
 * ```kotlin
 * val sdk = fakeSdk {
 *     auth { login { _, _ -> Either.Right(AuthResponse(...)) } }
 *     user { getProfile { Either.Right(UserResponse(...)) } }
 * }
 * ```
 */
@FakeSDKDsl
class FakeSdkBuilder {

    private var authApiBuilder: FakeAuthApiBuilder = FakeAuthApiBuilder()
    private var userApiBuilder: FakeUserApiBuilder = FakeUserApiBuilder()

    fun auth(init: FakeAuthApiBuilder.() -> Unit) {
        authApiBuilder.init()
    }

    fun user(init: FakeUserApiBuilder.() -> Unit) {
        userApiBuilder.init()
    }

    internal fun build(): Sdk {
        return Sdk(
            authApi = authApiBuilder.build(),
            userApi = userApiBuilder.build(),
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
