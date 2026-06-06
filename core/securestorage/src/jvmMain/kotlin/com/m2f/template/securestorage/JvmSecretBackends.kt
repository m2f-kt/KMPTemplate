package com.m2f.template.securestorage

import java.io.File

/**
 * Pluggable macOS Keychain backend. Production binding wraps the Apple-shipped
 * `/usr/bin/security` CLI ([MacSecurityCliKeychainBackend]); tests inject
 * [InMemoryKeychainBackend].
 *
 * Service/account names are passed by the caller so the same backend can host any
 * number of independent secrets without duplicating the shell-out plumbing.
 */
internal interface KeychainBackend {
    fun load(service: String, account: String): ByteArray?
    fun store(service: String, account: String, bytes: ByteArray)
    fun remove(service: String, account: String)
}

/**
 * Pluggable Windows DPAPI seal-and-store backend. Production binding wraps
 * JNA-Platform's `Crypt32Util.cryptProtectData` / `cryptUnprotectData`
 * ([WindowsDpapiBackend]); tests inject a fake.
 *
 * Kept on a small abstraction so the JVM [SecretStore] can dispatch by OS without
 * if-chains and so tests can verify the Windows branch is selected when
 * `os.name.contains("windows")` — without needing real Windows hardware.
 */
internal interface DpapiBackend {
    fun load(dir: File, service: String, account: String): ByteArray?
    fun store(dir: File, service: String, account: String, bytes: ByteArray)
    fun remove(dir: File, service: String, account: String)
}
