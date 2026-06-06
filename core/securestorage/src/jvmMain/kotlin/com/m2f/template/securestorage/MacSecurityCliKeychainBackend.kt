package com.m2f.template.securestorage

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.EnumSet

/**
 * Production [KeychainBackend] for macOS — wraps the Apple-shipped `/usr/bin/security` CLI:
 *
 *   security find-generic-password -s <service> -a <account> -w
 *   security add-generic-password  -U -s <service> -a <account> -w -A   (hex payload on stdin)
 *   security delete-generic-password -s <service> -a <account>
 *
 * The CLI returns the password value as a UTF-8 hex string on stdout; we round-trip via hex
 * to keep arbitrary-byte payloads safe across the process boundary (raw bytes containing
 * NULs would be truncated by the CLI's C-string handling).
 *
 * **Hardening (carried over verbatim from the source crypto module).** The secret hex is
 * NEVER placed on argv — every local process could otherwise read it via `ps -o args=` /
 * `KERN_PROCARGS2` for the lifetime of the spawned `security` process. Instead it is
 * delivered via the spawned process's stdin from a mode-0600 temp file:
 *  - The temp file is created atomically with POSIX 0600 permissions via
 *    `Files.createTempFile(..., asFileAttribute(...))` (single syscall, no race window).
 *  - Argv contains a bare `-w` flag with no value; `security(1)` reads the password from
 *    stdin when `-w` has no argv value and stdin is non-tty.
 *  - The temp file is deleted in a `finally` block after `security` exits.
 *
 * **stderr capture.** [runProcess] does NOT use `redirectErrorStream(true)`; stderr is
 * captured separately into [ProcessOutcome.stderr] so error messages include the actual
 * macOS diagnostics.
 *
 * **errSecItemNotFound vs transient failure.** [load] distinguishes CLI exit code 44
 * (`errSecItemNotFound` — genuinely absent, first launch → returns `null`) from every other
 * non-zero exit (transient `errSecAuthFailed` / `errSecInteractionNotAllowed` /
 * `errSecNotAvailable` / SIP-blocked → throws). Returning `null` on a transient failure
 * would let a caller silently overwrite an existing secret.
 *
 * Process invocation is injected via [processFactory] / [storeProcessFactory] so tests can
 * substitute fakes; in practice the in-memory test fixture bypasses this class entirely, so
 * the seams exist for future macOS-CI smoke tests rather than the unit suite.
 */
internal class MacSecurityCliKeychainBackend(
    private val processFactory: (List<String>) -> ProcessOutcome = ::runProcess,
    private val storeProcessFactory: (List<String>, File) -> ProcessOutcome = ::runStoreProcess,
) : KeychainBackend {

    override fun load(service: String, account: String): ByteArray? {
        val outcome = processFactory(
            listOf(
                "/usr/bin/security",
                "find-generic-password",
                "-s", service,
                "-a", account,
                "-w",
            ),
        )
        // Distinguish errSecItemNotFound (genuinely absent — first launch) from every other
        // non-zero exit (transient errSecAuthFailed / errSecInteractionNotAllowed /
        // errSecNotAvailable / SIP-blocked). Only exit 44 returns null. Anything else throws
        // — returning null on a transient failure would let a caller overwrite an existing
        // secret.
        return when (outcome.exitCode) {
            0 -> parseFindOutput(outcome.stdout)
            ITEM_NOT_FOUND_EXIT_CODE -> null
            else -> error(
                "/usr/bin/security find-generic-password failed: " +
                    "exit=${outcome.exitCode}, stderr=${outcome.stderr}. " +
                    "Refusing to treat a transient failure as item-not-found.",
            )
        }
    }

    override fun store(service: String, account: String, bytes: ByteArray) {
        val hex = bytes.toHex()
        val tmpPath: Path = Files.createTempFile(
            "template-securestorage-",
            ".hex",
            PosixFilePermissions.asFileAttribute(
                EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                ),
            ),
        )
        try {
            Files.write(
                tmpPath,
                (hex + "\n").toByteArray(Charsets.US_ASCII),
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
            )
            // Pass the hex payload to /usr/bin/security via stdin from a mode-0600 temp file.
            // `-w` is a BARE flag (no argv value) — security(1) reads the password from stdin
            // when stdin is non-tty and `-w` has no value. The hex payload is NOT on argv.
            val outcome = storeProcessFactory(
                listOf(
                    "/usr/bin/security",
                    "add-generic-password",
                    "-U", // update if already present (idempotent overwrite)
                    "-s", service,
                    "-a", account,
                    "-w",
                    "-A", // no application allowlist; tighten with `-T <App>` if desired
                ),
                tmpPath.toFile(),
            )
            check(outcome.exitCode == 0) {
                "/usr/bin/security add-generic-password failed " +
                    "(exit=${outcome.exitCode}, stderr=${outcome.stderr})"
            }
        } finally {
            // Files.deleteIfExists can throw IOException (file-system errors, security manager
            // rejection). If check(...) above already threw IllegalStateException carrying the
            // stderr context, an unhandled IOException here would mask it. Wrap in a nested
            // try/catch so the original IllegalStateException (if any) propagates unmasked.
            // Best-effort cleanup: the OS will GC orphans on reboot if deletion fails.
            try {
                Files.deleteIfExists(tmpPath)
            } catch (_: IOException) {
                // Swallow — primary failure (if any) wins; OS reboot GC handles orphans.
            }
        }
    }

    override fun remove(service: String, account: String) {
        val outcome = processFactory(
            listOf(
                "/usr/bin/security",
                "delete-generic-password",
                "-s", service,
                "-a", account,
            ),
        )
        // 0 = deleted, 44 = errSecItemNotFound (already absent — a no-op delete is success).
        // Any other non-zero exit is a real failure.
        when (outcome.exitCode) {
            0, ITEM_NOT_FOUND_EXIT_CODE -> Unit
            else -> error(
                "/usr/bin/security delete-generic-password failed: " +
                    "exit=${outcome.exitCode}, stderr=${outcome.stderr}",
            )
        }
    }

    /**
     * Outcome of a process invocation — minimal surface so the test seam stays small.
     * [stderr] is captured separately (no `redirectErrorStream`) so error messages can
     * include the actual macOS error text.
     */
    internal data class ProcessOutcome(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
    )

    internal companion object {

        /**
         * macOS 10.13+ /usr/bin/security maps `errSecItemNotFound` (Security.framework
         * constant -25300) to CLI exit code **44**. Verify on a macOS host:
         *
         *   /usr/bin/security find-generic-password -s nonexistent.test -a nope
         *   echo $?    # → 44
         *
         * Any other non-zero exit is treated by [load] as a transient/permanent failure that
         * MUST NOT be confused with item-not-found. Pinned as a const so the magic number
         * cannot be quietly changed.
         */
        private const val ITEM_NOT_FOUND_EXIT_CODE: Int = 44

        /**
         * Parse the stdout of `security find-generic-password -w` into raw payload bytes.
         * The CLI emits the password as a single hex line followed by a trailing newline.
         * Returns `null` if the trimmed input is not valid even-length hex (defensive —
         * surfaces locale/format/truncation surprises rather than silently producing bogus
         * bytes).
         */
        fun parseFindOutput(stdout: String): ByteArray? {
            val trimmed = stdout.trim()
            if (trimmed.isEmpty() || trimmed.length % 2 != 0) return null
            val out = ByteArray(trimmed.length / 2)
            var i = 0
            while (i < trimmed.length) {
                val hi = hexNibble(trimmed[i]) ?: return null
                val lo = hexNibble(trimmed[i + 1]) ?: return null
                out[i / 2] = ((hi shl 4) or lo).toByte()
                i += 2
            }
            return out
        }

        private fun hexNibble(c: Char): Int? = when (c) {
            in '0'..'9' -> c - '0'
            in 'a'..'f' -> c - 'a' + 10
            in 'A'..'F' -> c - 'A' + 10
            else -> null
        }

        private fun ByteArray.toHex(): String {
            val sb = StringBuilder(size * 2)
            for (b in this) {
                val v = b.toInt() and 0xFF
                sb.append(HEX_DIGITS[v ushr 4])
                sb.append(HEX_DIGITS[v and 0x0F])
            }
            return sb.toString()
        }

        private const val HEX_DIGITS = "0123456789abcdef"

        /**
         * Default process runner — shells out to ProcessBuilder and captures stdout AND
         * stderr separately (no `redirectErrorStream(true)`).
         */
        private fun runProcess(argv: List<String>): ProcessOutcome {
            val process = ProcessBuilder(argv).start()
            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }
            val exit = process.waitFor()
            return ProcessOutcome(exitCode = exit, stdout = stdout, stderr = stderr)
        }

        /**
         * Variant of [runProcess] that redirects the spawned process's stdin from
         * [stdinFile]. Used by [store] to deliver the hex payload to `security(1)` via stdin
         * instead of placing it on argv.
         */
        private fun runStoreProcess(argv: List<String>, stdinFile: File): ProcessOutcome {
            val process = ProcessBuilder(argv).redirectInput(stdinFile).start()
            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }
            val exit = process.waitFor()
            return ProcessOutcome(exitCode = exit, stdout = stdout, stderr = stderr)
        }
    }
}
