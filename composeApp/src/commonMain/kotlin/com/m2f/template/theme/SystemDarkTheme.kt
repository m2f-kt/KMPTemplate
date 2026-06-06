package com.m2f.template.theme

import androidx.compose.runtime.Composable

/**
 * Whether the host system is currently in dark mode.
 *
 * The JVM/desktop actual reads the macOS appearance natively because Compose's
 * `isSystemInDarkTheme()` does not track it on the desktop target (compose-jb#1986). Android,
 * iOS, and wasm delegate to the Compose default, which already follows the system there.
 *
 * Called directly where the theme is applied (App.kt) rather than provided via a
 * CompositionLocal — a value provided around `App()` from the JVM `application`/`Window` scope
 * does not propagate into `App()`'s composition, so the app would stay light under a dark system.
 */
@Composable
expect fun rememberSystemDarkTheme(): Boolean
