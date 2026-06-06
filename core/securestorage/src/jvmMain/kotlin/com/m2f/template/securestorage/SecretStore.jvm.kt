package com.m2f.template.securestorage

import java.io.File

/**
 * JVM-desktop [SecretStore]. Three-way OS dispatch:
 *  - **Windows** → DPAPI seal-and-store via JNA `Crypt32Util` ([WindowsDpapiBackend]).
 *  - **macOS**   → macOS Keychain via `/usr/bin/security` CLI ([MacSecurityCliKeychainBackend]).
 *  - **Other**   → a guarded raw-file fallback under `<home>/.template-securestorage/`, gated
 *                  behind a system property `-Dtemplate.dev.allowRawSecretStore=true`. Default
 *                  behavior throws [IllegalStateException] so a misconfigured release build
 *                  cannot silently weaken secret storage. Linux JVM development environments
 *                  opt in for local testing.
 *
 * Backends and the OS string are injectable so tests can exercise each branch without real
 * platform hardware.
 */
internal class JvmSecretStore internal constructor(
    private val keychainBackend: KeychainBackend = MacSecurityCliKeychainBackend(),
    private val dpapiBackend: DpapiBackend = WindowsDpapiBackend(),
    private val osName: String = System.getProperty("os.name").orEmpty(),
    private val allowRawSecretStore: Boolean =
        System.getProperty("template.dev.allowRawSecretStore")
            ?.equals("true", ignoreCase = true) == true,
    private val homeDir: String =
        System.getProperty("user.home") ?: System.getenv("HOME") ?: ".",
) : SecretStore {

    private val storeDir: File get() = File(homeDir, DIR_NAME)

    private enum class Os { WINDOWS, MAC, OTHER }

    private val os: Os
        get() {
            val lower = osName.lowercase()
            return when {
                lower.contains("windows") -> Os.WINDOWS
                lower.contains("mac") -> Os.MAC
                else -> Os.OTHER
            }
        }

    override suspend fun get(service: String, account: String): ByteArray? = when (os) {
        Os.WINDOWS -> dpapiBackend.load(storeDir.apply { mkdirs() }, service, account)
        Os.MAC -> keychainBackend.load(service, account)
        Os.OTHER -> rawLoad(service, account)
    }

    override suspend fun put(service: String, account: String, bytes: ByteArray) {
        when (os) {
            Os.WINDOWS -> dpapiBackend.store(storeDir.apply { mkdirs() }, service, account, bytes)
            Os.MAC -> keychainBackend.store(service, account, bytes)
            Os.OTHER -> rawStore(service, account, bytes)
        }
    }

    override suspend fun delete(service: String, account: String) {
        when (os) {
            Os.WINDOWS -> dpapiBackend.remove(storeDir.apply { mkdirs() }, service, account)
            Os.MAC -> keychainBackend.remove(service, account)
            Os.OTHER -> rawDelete(service, account)
        }
    }

    private fun rawLoad(service: String, account: String): ByteArray? {
        guardRaw()
        val f = rawFile(service, account)
        return if (f.exists()) f.readBytes() else null
    }

    private fun rawStore(service: String, account: String, bytes: ByteArray) {
        guardRaw()
        val dir = storeDir.apply { mkdirs() }
        val f = File(dir, rawFile(service, account).name)
        f.writeBytes(bytes)
        f.setReadable(false, false)
        f.setReadable(true, true)
        f.setWritable(false, false)
        f.setWritable(true, true)
    }

    private fun rawDelete(service: String, account: String) {
        guardRaw()
        rawFile(service, account).delete()
    }

    private fun guardRaw() {
        check(allowRawSecretStore) {
            "raw secret store disabled in release builds " +
                "(-Dtemplate.dev.allowRawSecretStore=true to opt in on this platform)"
        }
    }

    private fun rawFile(service: String, account: String): File {
        val safe = "${service}__${account}".map { c ->
            if (c.isLetterOrDigit() || c == '_' || c == '-' || c == '.') c else '_'
        }.joinToString("")
        return File(storeDir, "$safe.secret")
    }

    private companion object {
        const val DIR_NAME: String = ".template-securestorage"
    }
}

public actual fun defaultSecretStore(): SecretStore = JvmSecretStore()
