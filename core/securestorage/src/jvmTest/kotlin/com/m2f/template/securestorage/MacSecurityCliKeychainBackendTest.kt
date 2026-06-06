package com.m2f.template.securestorage

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.test.Test

/**
 * Unit tests for the macOS Keychain CLI backend's pure logic and its injectable process seams
 * — no real `/usr/bin/security` invocation. Verifies hex round-tripping, exit-code handling
 * (errSecItemNotFound exit 44 vs transient failure), and the stdin-not-argv store path.
 */
class MacSecurityCliKeychainBackendTest {

    @Test
    fun parseFindOutput_decodes_trimmed_hex() {
        MacSecurityCliKeychainBackend.parseFindOutput("00017fff\n")?.toList() shouldBe
            listOf<Byte>(0x00, 0x01, 0x7F, -1)
    }

    @Test
    fun parseFindOutput_rejects_odd_length() {
        MacSecurityCliKeychainBackend.parseFindOutput("abc").shouldBeNull()
    }

    @Test
    fun parseFindOutput_rejects_non_hex() {
        MacSecurityCliKeychainBackend.parseFindOutput("zz").shouldBeNull()
    }

    @Test
    fun parseFindOutput_rejects_empty() {
        MacSecurityCliKeychainBackend.parseFindOutput("\n").shouldBeNull()
    }

    @Test
    fun load_returns_null_on_exit_44() {
        val backend = MacSecurityCliKeychainBackend(
            processFactory = { MacSecurityCliKeychainBackend.ProcessOutcome(44, "", "not found") },
            storeProcessFactory = { _, _ -> MacSecurityCliKeychainBackend.ProcessOutcome(0, "", "") },
        )
        backend.load("svc", "acc").shouldBeNull()
    }

    @Test
    fun load_throws_on_transient_failure() {
        val backend = MacSecurityCliKeychainBackend(
            processFactory = { MacSecurityCliKeychainBackend.ProcessOutcome(1, "", "locked") },
            storeProcessFactory = { _, _ -> MacSecurityCliKeychainBackend.ProcessOutcome(0, "", "") },
        )
        runCatching { backend.load("svc", "acc") }.isFailure shouldBe true
    }

    @Test
    fun store_delivers_hex_via_stdin_file_not_argv() {
        var capturedArgv: List<String> = emptyList()
        var capturedFileBytes = ByteArray(0)
        val backend = MacSecurityCliKeychainBackend(
            processFactory = { MacSecurityCliKeychainBackend.ProcessOutcome(0, "", "") },
            storeProcessFactory = { argv, file: File ->
                capturedArgv = argv
                capturedFileBytes = file.readBytes()
                MacSecurityCliKeychainBackend.ProcessOutcome(0, "", "")
            },
        )

        backend.store("svc", "acc", byteArrayOf(0x0a, 0x0b))

        // Hardening invariant: the secret is NEVER on argv; `-w` is a bare flag.
        capturedArgv.none { it == "0a0b" } shouldBe true
        capturedArgv.contains("-w") shouldBe true
        // The hex payload is delivered via the stdin temp file.
        capturedFileBytes.decodeToString().trim() shouldBe "0a0b"
    }
}
