package com.m2f.template.desktop

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

/**
 * Unit coverage for the pure desktop-shell decision helpers [shouldHideOnClose] and
 * [shouldRegisterPreferencesHandler]. These are the only seams a refactor can break without a
 * human running the app, so they are guarded directly.
 */
class WindowLifecycleTest {

    @Test
    fun `close hides when the tray mounted`() {
        // Tray is live → a tray item can bring the window back, so hiding is safe.
        shouldHideOnClose(trayMounted = true).shouldBeTrue()
    }

    @Test
    fun `close keeps the window open when the tray failed to mount`() {
        // No tray icon → hiding would strand the user with no way back. This is the cold-start
        // safe path (trayMounted defaults to false).
        shouldHideOnClose(trayMounted = false).shouldBeFalse()
    }

    @Test
    fun `registers preferences handler when the platform reports support`() {
        shouldRegisterPreferencesHandler(supported = true).shouldBeTrue()
    }

    @Test
    fun `does not register preferences handler when the platform lacks support`() {
        // Cross-platform / CI guard: the native AWT call would otherwise throw onto the EDT.
        shouldRegisterPreferencesHandler(supported = false).shouldBeFalse()
    }
}
