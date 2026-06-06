package com.m2f.template.securestorage

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test

/**
 * JVM OS-dispatch tests for [JvmSecretStore], using the in-memory keychain backend and a fake
 * DPAPI backend so each branch is exercised without real macOS/Windows hardware.
 */
class JvmSecretStoreTest {

    @Test
    fun mac_branch_round_trips_through_keychain_backend() = runTest {
        val store = JvmSecretStore(
            keychainBackend = InMemoryKeychainBackend(),
            dpapiBackend = FakeDpapiBackend(),
            osName = "Mac OS X",
            allowRawSecretStore = false,
        )
        val payload = byteArrayOf(9, 8, 7, 0, -1)

        store.put(SERVICE, ACCOUNT, payload)
        store.get(SERVICE, ACCOUNT)?.toList() shouldBe payload.toList()

        store.delete(SERVICE, ACCOUNT)
        store.get(SERVICE, ACCOUNT).shouldBeNull()
    }

    @Test
    fun windows_branch_dispatches_to_dpapi_backend() = runTest {
        val dpapi = FakeDpapiBackend()
        val store = JvmSecretStore(
            keychainBackend = InMemoryKeychainBackend(),
            dpapiBackend = dpapi,
            osName = "Windows 11",
            allowRawSecretStore = false,
        )

        store.put(SERVICE, ACCOUNT, byteArrayOf(1, 2, 3))
        store.get(SERVICE, ACCOUNT)?.toList() shouldBe listOf<Byte>(1, 2, 3)

        dpapi.stores shouldBeGreaterThan 0
        dpapi.loads shouldBeGreaterThan 0
    }

    @Test
    fun other_os_raw_fallback_throws_when_not_allowed() = runTest {
        val store = JvmSecretStore(
            keychainBackend = InMemoryKeychainBackend(),
            dpapiBackend = FakeDpapiBackend(),
            osName = "Linux",
            allowRawSecretStore = false,
        )

        val error = runCatching { store.put(SERVICE, ACCOUNT, byteArrayOf(1)) }.exceptionOrNull()
        error shouldNotBe null
    }

    @Test
    fun other_os_raw_fallback_round_trips_when_allowed() = runTest {
        val tmpHome = Files.createTempDirectory("template-securestorage-test").toFile()
        try {
            val store = JvmSecretStore(
                keychainBackend = InMemoryKeychainBackend(),
                dpapiBackend = FakeDpapiBackend(),
                osName = "Linux",
                allowRawSecretStore = true,
                homeDir = tmpHome.absolutePath,
            )
            val payload = byteArrayOf(4, 5, 6)

            store.put(SERVICE, ACCOUNT, payload)
            store.get(SERVICE, ACCOUNT)?.toList() shouldBe payload.toList()

            store.delete(SERVICE, ACCOUNT)
            store.get(SERVICE, ACCOUNT).shouldBeNull()
        } finally {
            tmpHome.deleteRecursively()
        }
    }

    private companion object {
        const val SERVICE = "com.m2f.template.test"
        const val ACCOUNT = "default"
    }
}
