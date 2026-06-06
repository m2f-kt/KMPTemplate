package com.m2f.template.securestorage

import java.io.File

/**
 * In-memory [KeychainBackend] test fixture — substitutes the production
 * `MacSecurityCliKeychainBackend` so the macOS Keychain dispatch branch can be exercised on
 * JVM CI without real macOS hardware.
 */
internal class InMemoryKeychainBackend : KeychainBackend {
    private val store = mutableMapOf<Pair<String, String>, ByteArray>()

    override fun load(service: String, account: String): ByteArray? =
        store[service to account]?.copyOf()

    override fun store(service: String, account: String, bytes: ByteArray) {
        store[service to account] = bytes.copyOf()
    }

    override fun remove(service: String, account: String) {
        store.remove(service to account)
    }
}

/**
 * In-memory [DpapiBackend] test double — proves the JVM store picks the Windows branch when
 * `osName.contains("windows")`. Tracks call counts so a test can assert single-dispatch.
 */
internal class FakeDpapiBackend : DpapiBackend {
    private val store = mutableMapOf<Pair<String, String>, ByteArray>()
    var loads: Int = 0
        private set
    var stores: Int = 0
        private set

    override fun load(dir: File, service: String, account: String): ByteArray? {
        loads += 1
        return store[service to account]?.copyOf()
    }

    override fun store(dir: File, service: String, account: String, bytes: ByteArray) {
        stores += 1
        store[service to account] = bytes.copyOf()
    }

    override fun remove(dir: File, service: String, account: String) {
        store.remove(service to account)
    }
}
