package com.m2f.template.securestorage

/**
 * In-memory [SecretStore] test fixture. Substitutes the real platform backends so the
 * service/account contract can be exercised on every target (including wasmJs, where the
 * production store throws) without touching real Keychain / Keystore / DPAPI.
 */
class InMemorySecretStore : SecretStore {
    private val store = mutableMapOf<Pair<String, String>, ByteArray>()

    override suspend fun get(service: String, account: String): ByteArray? =
        store[service to account]?.copyOf()

    override suspend fun put(service: String, account: String, bytes: ByteArray) {
        store[service to account] = bytes.copyOf()
    }

    override suspend fun delete(service: String, account: String) {
        store.remove(service to account)
    }
}
