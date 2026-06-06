package com.m2f.template.desktop

/**
 * Pure window-close decision for the JVM Desktop shell (minimize-to-tray).
 *
 * Returns `true` when closing the main window should HIDE it (e.g. to a system tray, so the
 * process stays resident), and `false` when the close must keep the window OPEN.
 *
 * The decision is driven solely by whether a system-tray icon successfully mounted:
 *  - tray mounted     → close hides (a tray item can bring the window back).
 *  - tray NOT mounted → close keeps the window open. Hiding with no tray icon would strand the
 *    user with no visible surface AND no way back, so `trayMounted` defaults to `false` until a
 *    successful mount is confirmed.
 *
 * Kept Compose-free and AWT-free so it is unit-testable.
 */
fun shouldHideOnClose(trayMounted: Boolean): Boolean = trayMounted

/**
 * Pure registration gate for the macOS `java.awt.Desktop` preferences handler — the affordance
 * that binds **Cmd+,** and adds a native **"Settings…"** item to the application menu.
 *
 * `supported` is the caller-computed
 * `Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(APP_PREFERENCES)`.
 *
 * On Linux, Windows, and headless CI the platform reports no `APP_PREFERENCES` support, so this
 * gate keeps the native `Desktop.setPreferencesHandler` call a guarded no-op (it would otherwise
 * throw onto the AWT EDT). The real registration is native + EDT-bound and thus not unit-testable;
 * this pure, AWT-free, Compose-free decision is the testable seam.
 */
fun shouldRegisterPreferencesHandler(supported: Boolean): Boolean = supported
