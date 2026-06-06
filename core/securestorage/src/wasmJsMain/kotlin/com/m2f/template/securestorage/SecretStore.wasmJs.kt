package com.m2f.template.securestorage

/**
 * Browser/wasmJs [SecretStore]. There is no OS-backed secure store available to a WASM
 * module running in a browser sandbox (no Keychain, no Keystore, no DPAPI), and persisting
 * secrets in `localStorage`/`IndexedDB` would be plaintext-equivalent and readable by any
 * script on the origin. Rather than offer a false sense of security, every operation throws
 * so misuse fails loudly at the call site.
 */
internal class UnavailableSecretStore : SecretStore {

    override suspend fun get(service: String, account: String): ByteArray? = unsupported()

    override suspend fun put(service: String, account: String, bytes: ByteArray): Unit = unsupported()

    override suspend fun delete(service: String, account: String): Unit = unsupported()

    private fun unsupported(): Nothing =
        throw UnsupportedOperationException(
            "Secure storage is not available on wasmJs (browser). " +
                "There is no OS-backed secret store in a browser sandbox.",
        )
}

public actual fun defaultSecretStore(): SecretStore = UnavailableSecretStore()
