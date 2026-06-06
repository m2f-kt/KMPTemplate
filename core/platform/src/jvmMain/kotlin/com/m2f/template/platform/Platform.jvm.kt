package com.m2f.template.platform

/**
 * Runtime detection for the macOS host. Every `MacOs*` JNA helper in this module gates itself
 * on this flag so the same JVM-desktop jar runs on Windows / Linux without attempting to load
 * AppKit / ApplicationServices / CoreFoundation.
 */
internal val isMacOsHost: Boolean = System.getProperty("os.name") == "Mac OS X"

/** JVM `actual` for [isMacOs] — delegates to [isMacOsHost] (reads `os.name`, no JNA required). */
actual fun isMacOs(): Boolean = isMacOsHost
