package com.m2f.template.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import com.m2f.template.desktop.PlatformDirs
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

private val installLog = Logger.withTag("FileLogWriter")

/**
 * Kermit [LogWriter] that appends client-side log lines to a file in the host OS's standard
 * per-user log directory (resolved by [PlatformDirs.logDir] for the given app name).
 *
 * A JVM app launched via the OS launcher (a packaged bundle) often writes no observable stdout,
 * so routing Kermit to a file makes the client side observable after the fact.
 *
 * ## Safety
 *
 * - **Never crashes boot:** the constructor's file-open is `try/catch`-wrapped; on failure
 *   [writer] stays `null` and [log] early-returns — a degraded no-op writer, never an exception
 *   out of app startup.
 * - **Bounded growth:** a [MAX_LOG_BYTES] size guard runs at construction *and* every
 *   [SIZE_CHECK_INTERVAL] writes inside [log], truncating-and-restarting the file so even a
 *   single long-running session cannot grow it without bound.
 * - **Thread-safe:** Kermit calls [log] from arbitrary threads; the timestamp + append is
 *   wrapped in `synchronized(this)`.
 */
class FileLogWriter(appName: String) : LogWriter() {

    @Volatile
    private var writer: PrintWriter? = null

    /** The backing log file — retained so [log] can re-check its size mid-session. */
    private val logFile: File? =
        try {
            File(PlatformDirs.logDir(appName), "$appName-client.log")
        } catch (_: Throwable) {
            null
        }

    /** Write counter; every [SIZE_CHECK_INTERVAL]th write re-runs the size guard. */
    private var writesSinceSizeCheck: Int = 0

    init {
        writer = try {
            val file = logFile ?: throw IllegalStateException("log file path unavailable")
            file.parentFile?.mkdirs()
            // Size guard: truncate-and-restart so the file cannot grow without bound.
            truncateIfOversized(file)
            PrintWriter(FileWriter(file, /* append = */ true), /* autoFlush = */ true)
        } catch (_: Throwable) {
            // Log dir unwritable (sandboxing, disk full, …) — degrade to a no-op writer rather
            // than crashing app boot. There is no logger yet to report this.
            null
        }
    }

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val out = writer ?: return
        synchronized(this) {
            out.println("${Instant.now()} $severity [$tag] $message")
            throwable?.printStackTrace(out)
            // A single long-running session would otherwise append forever, so re-check the size
            // every SIZE_CHECK_INTERVAL writes and re-truncate if needed.
            writesSinceSizeCheck++
            if (writesSinceSizeCheck >= SIZE_CHECK_INTERVAL) {
                writesSinceSizeCheck = 0
                rotateIfOversized()
            }
        }
    }

    /**
     * Re-runs the [MAX_LOG_BYTES] guard mid-session: if the file has grown past the bound,
     * closes the current writer, truncates the file, and reopens. Best-effort — any failure
     * leaves the existing writer untouched rather than crashing the logging path.
     */
    private fun rotateIfOversized() {
        val file = logFile ?: return
        try {
            if (file.exists() && file.length() > MAX_LOG_BYTES) {
                writer?.flush()
                writer?.close()
                file.writeText("")
                writer = PrintWriter(FileWriter(file, /* append = */ true), /* autoFlush = */ true)
            }
        } catch (_: Throwable) {
            // Rotation is best-effort — never let it crash the logging path.
        }
    }

    private companion object {
        // Truncate-and-restart threshold — keeps the log file bounded.
        const val MAX_LOG_BYTES: Long = 5_000_000L

        // How many writes between mid-session size re-checks. Cheap File.length() call
        // amortised across writes so the hot logging path stays fast.
        const val SIZE_CHECK_INTERVAL: Int = 500

        /**
         * Truncate-and-restart the file if it already exceeds [MAX_LOG_BYTES]. Used at
         * construction (before the writer is opened in append mode).
         */
        fun truncateIfOversized(file: File) {
            if (file.exists() && file.length() > MAX_LOG_BYTES) {
                file.writeText("")
            }
        }
    }
}

private val fileLoggingInstalled = AtomicBoolean(false)

/**
 * Installs [FileLogWriter] alongside the platform (stdout) log writer so a packaged bundle emits
 * an observable client-side log while dev-run stdout logging stays intact. Call as the FIRST line
 * of `main()`.
 *
 * `Logger.setLogWriters` *replaces* Kermit's writer list wholesale, so a second call would
 * silently clobber the file writer. [fileLoggingInstalled] guards against that: the first call
 * wins, any later call is a logged no-op.
 *
 * The body is `try/catch`-wrapped: logging setup must never crash app boot.
 */
fun installFileLogging(appName: String) {
    if (!fileLoggingInstalled.compareAndSet(false, true)) {
        installLog.w {
            "installFileLogging() called more than once — ignoring (the first call owns " +
                "the writer list; a later setLogWriters would clobber the file writer)"
        }
        return
    }
    try {
        Logger.setLogWriters(platformLogWriter(), FileLogWriter(appName))
    } catch (_: Throwable) {
        // Logging is best-effort — a failure here must never prevent the app from starting.
    }
}
