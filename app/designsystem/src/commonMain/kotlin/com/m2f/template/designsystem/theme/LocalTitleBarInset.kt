package com.m2f.template.designsystem.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Top inset reserved for an overlay window title bar that the app content draws *underneath*.
 *
 * On macOS the JVM desktop window enables full-window-content + a transparent title bar, so the
 * theme background flows edge-to-edge behind the traffic-light buttons. Foreground content that
 * would otherwise collide with the traffic lights — top bars, headers — must offset its top by this
 * inset to clear them; full-bleed backgrounds deliberately ignore it.
 *
 * Defaults to `0.dp`: every platform/window without an overlay title bar (Windows, Linux, mobile,
 * web) leaves layouts unchanged. The JVM desktop entry point provides the real title-bar height
 * only on macOS.
 */
val LocalTitleBarInset = compositionLocalOf { 0.dp }
