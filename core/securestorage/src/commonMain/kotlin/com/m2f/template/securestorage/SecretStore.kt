package com.m2f.template.securestorage

/**
 * Service/account-parameterized secret-storage backend.
 *
 * A [SecretStore] persists arbitrary opaque byte payloads keyed by a
 * `(service, account)` pair, mirroring the shape of the platform secure stores it
 * wraps (Apple Keychain `kSecAttrService`/`kSecAttrAccount`, the macOS
 * `/usr/bin/security` CLI `-s`/`-a` flags, Android Keystore aliases, Windows DPAPI
 * envelopes). The same backend can therefore host many independent secrets without
 * duplicating the platform plumbing — e.g. an auth token and a refresh token.
 *
 * The payload is treated as opaque bytes: callers are responsible for any encoding.
 * Implementations MUST round-trip the exact bytes written.
 *
 * This is a generic *backend layer* only — it intentionally knows nothing about
 * AES keys, AEAD, key rotation, or any product-specific crypto. Build those on top.
 *
 * All operations are `suspend` because some backends (notably the macOS CLI backend)
 * perform blocking process / I/O work that should be dispatched off the main thread
 * by the caller.
 */
public interface SecretStore {

    /**
     * Returns the bytes previously stored under [service]/[account], or `null` if no
     * such item exists. Implementations distinguish a genuinely-absent item (returns
     * `null`) from a transient backend failure (throws), so callers never mistake a
     * locked keychain for a first-launch and silently overwrite an existing secret.
     */
    public suspend fun get(service: String, account: String): ByteArray?

    /**
     * Stores [bytes] under [service]/[account], overwriting any existing value
     * idempotently.
     */
    public suspend fun put(service: String, account: String, bytes: ByteArray)

    /**
     * Removes the item stored under [service]/[account]. A no-op if it does not exist.
     */
    public suspend fun delete(service: String, account: String)
}

/**
 * Returns the platform-default [SecretStore] for the current target:
 *
 *  - **JVM desktop** — dispatches by OS: macOS → Keychain (`/usr/bin/security` CLI),
 *    Windows → DPAPI, anything else → a guarded raw-file dev fallback that throws in
 *    release builds.
 *  - **Apple (iOS)** — `Security.framework` generic-password Keychain items.
 *  - **Android** — AndroidKeyStore AES/GCM-wrapped values in private SharedPreferences.
 *  - **wasmJs (browser)** — not available; returns a store that throws on every call.
 */
public expect fun defaultSecretStore(): SecretStore
