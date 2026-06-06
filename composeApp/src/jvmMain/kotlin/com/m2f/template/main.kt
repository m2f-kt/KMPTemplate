package com.m2f.template

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import com.m2f.template.designsystem.theme.LocalTitleBarInset
import com.m2f.template.desktop.SingleInstanceGuard
import com.m2f.template.logging.installFileLogging

// App identity (matches rootProject.name = "template"). Drives the window title, the single-
// instance lock dir, the log file name, and the macOS dev menu-bar name.
private const val APP_NAME = "template"

// macOS draws the window content under a transparent title bar (full-window-content) so the theme
// background flows edge-to-edge behind the traffic-light buttons. Foreground content offsets by
// this height to clear them. Standard modern macOS title bar = 28pt.
private val IS_MACOS: Boolean = System.getProperty("os.name").orEmpty().contains("mac", ignoreCase = true)
private val MACOS_TITLE_BAR_INSET = 28.dp

// The desktop window opens at this size AND cannot be dragged any narrower/shorter. The default size
// equals the minimum so the window can only ever grow from a known-good layout (below it, responsive
// layouts that sit beside a >840dp breakpoint start to clip). Tunable default.
private val DEFAULT_WINDOW_SIZE = DpSize(960.dp, 600.dp)
private val MIN_WINDOW_DIMENSION = Dimension(960, 600)

/**
 * JVM Desktop entry point.
 *
 * Mounts the generic desktop shell: file logging first (so nothing logs before it), a
 * single-instance guard, then a Window with a transparent macOS title bar that lets the theme
 * background flow behind the traffic lights, with [LocalTitleBarInset] reserving that strip for
 * foreground content. The shared [App] composable handles everything else.
 */
fun main() {
    // Route Kermit logs to the per-user log dir FIRST, before anything else can log. Internally
    // try/catch-wrapped so logging setup can never crash boot.
    installFileLogging(APP_NAME)

    // Single-instance guard: if another instance already holds the lock, exit BEFORE mounting any
    // window. Fails open on error.
    if (!SingleInstanceGuard.acquire(APP_NAME)) {
        return
    }

    if (IS_MACOS) {
        // Dev-build menu-bar name: without this the macOS app menu reads "MainKt". Must be set
        // before AWT init. Harmless no-op when run from a packaged bundle.
        System.setProperty("apple.awt.application.name", APP_NAME)
        // Make AWT follow the OS appearance (dark) instead of defaulting to light Aqua, so the
        // title bar reads as a seamless extension of the app. Must be set before AWT init.
        System.setProperty("apple.awt.application.appearance", "system")
    }

    application {
        // Open at the default size; window.minimumSize below pins the lower bound so the window
        // can't be dragged smaller than the known-good layout floor.
        val windowState = rememberWindowState(size = DEFAULT_WINDOW_SIZE)
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = APP_NAME,
        ) {
            // Edge-to-edge title bar on macOS: fullWindowContent makes the Compose content fill the
            // whole window including under the title bar; transparentTitleBar + hidden title text
            // let the theme background flow up behind just the traffic-light buttons. macOS-only
            // client properties; harmless no-ops on other platforms.
            LaunchedEffect(Unit) {
                runCatching {
                    // Forbid shrinking below the known-good layout floor (all desktop OSes).
                    window.minimumSize = MIN_WINDOW_DIMENSION
                    window.background = java.awt.Color(0x07, 0x08, 0x0C)
                    window.rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
                    window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                    window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                }
            }
            // Reserve the traffic-light strip for foreground content only on macOS (where the
            // transparent full-window title bar overlays the content). Zero elsewhere so Windows/
            // Linux desktop layouts are untouched.
            CompositionLocalProvider(
                LocalTitleBarInset provides if (IS_MACOS) MACOS_TITLE_BAR_INSET else 0.dp,
            ) {
                App()
            }
        }
    }
}
