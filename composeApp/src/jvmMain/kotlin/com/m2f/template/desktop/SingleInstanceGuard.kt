package com.m2f.template.desktop

import co.touchlab.kermit.Logger
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException

/**
 * Process-wide single-instance guard for the JVM desktop app.
 *
 * Holds an exclusive OS file lock for the JVM's lifetime so a second launch can detect that an
 * instance is already running. The OS releases the lock automatically when the process exits —
 * even on a crash — so there is never a stale lock to clear (unlike a pidfile).
 *
 * Fails OPEN: any unexpected error acquiring the lock lets the app start. A guard must never be
 * the reason the app won't launch.
 *
 * The app name and lock directory are parameterised so this is reusable across projects and works
 * on macOS, Windows, and Linux via [PlatformDirs].
 */
object SingleInstanceGuard {

    private val log = Logger.withTag("SingleInstance")

    // Held for the whole process lifetime and never closed, so the lock stays held. Kept as
    // fields so neither is garbage-collected (a collected channel would silently drop the lock).
    private var heldChannel: FileChannel? = null
    private var heldLock: FileLock? = null

    /** True if THIS process is the sole instance (acquired the lock); false if another holds it. */
    fun acquire(appName: String): Boolean = acquire(defaultLockFile(appName))

    internal fun acquire(lockFile: File): Boolean =
        try {
            val channel = RandomAccessFile(lockFile, "rw").channel
            // tryLock returns null when ANOTHER PROCESS holds the lock. It throws
            // OverlappingFileLockException only when THIS JVM already holds it (the in-process
            // test path) — treat that the same as "already locked" so the guard is testable
            // without spawning a real second process. Production calls acquire() exactly once.
            val lock = runCatching { channel.tryLock() }
                .getOrElse { if (it is OverlappingFileLockException) null else throw it }
            if (lock == null) {
                runCatching { channel.close() }
                log.w { "another instance already holds the lock — refusing to start a second" }
                false
            } else {
                heldChannel = channel
                heldLock = lock
                true
            }
        } catch (e: Throwable) {
            log.w { "single-instance check failed (${e::class.simpleName}); proceeding without the guard" }
            true
        }

    private fun defaultLockFile(appName: String): File =
        File(PlatformDirs.appDataDir(appName), "single-instance.lock")
}
