package com.m2f.template.desktop

import java.io.File

/**
 * Cross-platform resolution of the per-user directories a JVM desktop app may write to.
 *
 * Each OS has its own convention for application data and log files. These helpers pick the
 * right base directory for the host OS and append the app name, so the same call site works on
 * macOS, Windows, and Linux without branching at the use site.
 *
 *  - macOS   data: `~/Library/Application Support/<app>`  logs: `~/Library/Logs/<app>`
 *  - Windows data: `%APPDATA%\<app>`                       logs: `%APPDATA%\<app>\logs`
 *  - Linux   data: `$XDG_DATA_HOME` or `~/.local/share/<app>`
 *            logs: `$XDG_STATE_HOME` or `~/.local/state/<app>/logs`
 *
 * All accessors are pure path computations plus a best-effort `mkdirs()`; they never throw.
 */
object PlatformDirs {

    private val osName: String = System.getProperty("os.name").orEmpty()
    private val isMac: Boolean = osName.contains("mac", ignoreCase = true)
    private val isWindows: Boolean = osName.contains("win", ignoreCase = true)

    private fun userHome(): File = File(System.getProperty("user.home").orEmpty())

    private fun env(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

    /**
     * Per-user application-data directory for [appName], created if missing.
     */
    fun appDataDir(appName: String): File {
        val dir = when {
            isMac -> File(userHome(), "Library/Application Support/$appName")
            isWindows -> File(env("APPDATA") ?: userHome().path, appName)
            else -> File(env("XDG_DATA_HOME") ?: File(userHome(), ".local/share").path, appName)
        }
        runCatching { dir.mkdirs() }
        return dir
    }

    /**
     * Per-user log directory for [appName], created if missing.
     */
    fun logDir(appName: String): File {
        val dir = when {
            isMac -> File(userHome(), "Library/Logs/$appName")
            isWindows -> File(File(env("APPDATA") ?: userHome().path, appName), "logs")
            else -> File(
                env("XDG_STATE_HOME") ?: File(userHome(), ".local/state").path,
                "$appName/logs",
            )
        }
        runCatching { dir.mkdirs() }
        return dir
    }
}
