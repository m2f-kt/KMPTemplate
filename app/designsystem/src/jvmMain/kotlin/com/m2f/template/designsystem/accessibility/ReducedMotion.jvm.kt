package com.m2f.template.designsystem.accessibility

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

/**
 * JVM `actual` for [prefersReducedMotion]. On macOS reads
 * `[[NSWorkspace sharedWorkspace] accessibilityDisplayShouldReduceMotion]` through a self-contained
 * AppKit Objective-C JNA bridge. Off macOS (Windows/Linux) — and on any failure — returns `false`
 * so animations stay on rather than crashing; the lookup is gated on [isMacOsHost] so the AppKit
 * library is never loaded on non-macOS hosts.
 *
 * Wiring note: this is kept self-contained (its own minimal JNA binding rather than depending on
 * `core:platform`) because `core:platform`'s rich JNA objects are intentionally `internal` to its
 * `jvmMain` and exposing them would leak JNA across the module boundary. A single BOOL message is
 * cheaper to inline than to make public, so the design system stays the only owner of this
 * accessibility concern.
 */
@Suppress("ReturnCount")
internal actual fun prefersReducedMotion(): Boolean {
    if (!isMacOsHost) return false
    return try {
        val appKit = AppKitLib.INSTANCE
        val workspaceClass = appKit.objc_getClass("NSWorkspace")
        if (workspaceClass == 0L) return false
        val shared = appKit.objc_msgSend(workspaceClass, appKit.sel_registerName("sharedWorkspace"), 0L)
        if (shared == 0L) return false
        val reduce = appKit.objc_msgSend(
            shared,
            appKit.sel_registerName("accessibilityDisplayShouldReduceMotion"),
            0L,
        )
        (reduce and OBJC_BOOL_MASK) != 0L
    } catch (_: Throwable) {
        false
    }
}

/** Runtime macOS-host detection; gates the AppKit load so non-macOS desktops never touch JNA. */
private val isMacOsHost: Boolean = System.getProperty("os.name") == "Mac OS X"

/** Objective-C `BOOL` is returned in the low byte of the result register; mask before testing. */
private const val OBJC_BOOL_MASK = 0xFFL

/**
 * Minimal JNA binding to the Objective-C runtime via AppKit (re-exports `objc_msgSend` /
 * `sel_registerName`). Loaded lazily on first use behind the [isMacOsHost] guard.
 */
private interface AppKitLib : Library {
    fun sel_registerName(name: String): Pointer
    fun objc_getClass(name: String): Long
    fun objc_msgSend(receiver: Long, selector: Pointer, arg: Long): Long

    companion object {
        @Suppress("ObjectPropertyNaming") // JNA convention
        val INSTANCE: AppKitLib by lazy {
            Native.load("AppKit", AppKitLib::class.java) as AppKitLib
        }
    }
}
