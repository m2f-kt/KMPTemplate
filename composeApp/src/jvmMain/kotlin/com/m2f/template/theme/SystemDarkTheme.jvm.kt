package com.m2f.template.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val log = Logger.withTag("SystemDarkTheme")
private const val POLL_INTERVAL_MS = 2_000L

/**
 * Resolves the desktop system dark/light appearance.
 *
 * On macOS, Compose's [isSystemInDarkTheme] does not track the system appearance
 * (compose-jb#1986), so we read `AppleInterfaceStyle` from the global defaults and poll it
 * so a live System Settings toggle is reflected without a relaunch. On non-macOS hosts we
 * defer to the Compose default (the Windows/Linux desktop appearance bridge is left to Compose).
 */
@Composable
actual fun rememberSystemDarkTheme(): Boolean {
    if (!isMacOs()) return isSystemInDarkTheme()

    var dark by remember { mutableStateOf(readMacOsDarkAppearance()) }
    LaunchedEffect(Unit) {
        log.i { "initial macOS appearance dark=$dark (os.name='${System.getProperty("os.name")}')" }
        while (true) {
            delay(POLL_INTERVAL_MS)
            val current = withContext(Dispatchers.IO) { readMacOsDarkAppearance() }
            if (current != dark) {
                log.i { "macOS appearance changed -> dark=$current" }
                dark = current
            }
        }
    }
    return dark
}

private fun isMacOs(): Boolean =
    System.getProperty("os.name").orEmpty().contains("Mac", ignoreCase = true)

/**
 * `defaults read -g AppleInterfaceStyle` prints `Dark` in dark mode and exits non-zero (the
 * key is absent) in light mode. Any failure is treated as light — the safe default. Absolute
 * path so it resolves regardless of the launching process's PATH. Not a suspend function: the
 * `runCatching` wraps only blocking process I/O, so no `CancellationException` is swallowed.
 */
private fun readMacOsDarkAppearance(): Boolean {
    val result = runCatching {
        val process = ProcessBuilder("/usr/bin/defaults", "read", "-g", "AppleInterfaceStyle")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { reader -> reader.readText() }
        process.waitFor()
        output.trim().equals("Dark", ignoreCase = true)
    }
    result.exceptionOrNull()?.let { error -> log.w(error) { "defaults read failed -> defaulting to light" } }
    return result.getOrDefault(false)
}
