package com.m2f.template.securestorage

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Backend-agnostic round-trip contract for [SecretStore], exercised via [InMemorySecretStore]
 * so it runs on every target.
 *
 * **CI gating note (carried over from the source crypto module):** do NOT gate CI on
 * iOS-simulator Keychain round-trips. Kotlin/Native iOS test binaries run outside an `.app`
 * bundle and lack the keychain-access entitlement, so real `SecItem*` calls fail at runtime
 * with `errSecNotAvailable (-25291)`. `macosX64`/`macosArm64` would be the safe native
 * targets for a real-Keychain round-trip — but Template does not declare macOS targets, so no
 * native-Keychain integration test exists here. Real-device verification is a manual gate.
 */
class SecretStoreRoundTripTest {

    @Test
    fun put_then_get_returns_same_bytes() = runTest {
        val store = InMemorySecretStore()
        val payload = byteArrayOf(0x00, 0x01, 0x02, 0x7F, -1, -128)

        store.put(SERVICE, ACCOUNT, payload)
        val loaded = store.get(SERVICE, ACCOUNT)

        loaded?.toList() shouldBe payload.toList()
    }

    @Test
    fun get_absent_returns_null() = runTest {
        val store = InMemorySecretStore()
        store.get(SERVICE, "missing").shouldBeNull()
    }

    @Test
    fun delete_removes_the_item() = runTest {
        val store = InMemorySecretStore()
        store.put(SERVICE, ACCOUNT, byteArrayOf(1, 2, 3))
        store.delete(SERVICE, ACCOUNT)
        store.get(SERVICE, ACCOUNT).shouldBeNull()
    }

    @Test
    fun distinct_service_account_pairs_are_isolated() = runTest {
        val store = InMemorySecretStore()
        store.put(SERVICE, "a", byteArrayOf(1))
        store.put(SERVICE, "b", byteArrayOf(2))

        store.get(SERVICE, "a")?.toList() shouldBe listOf<Byte>(1)
        store.get(SERVICE, "b")?.toList() shouldBe listOf<Byte>(2)
    }

    private companion object {
        const val SERVICE = "com.m2f.template.test"
        const val ACCOUNT = "default"
    }
}
