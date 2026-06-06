package com.m2f.template.platform

/**
 * Common, JNA-free entry point for asking whether the current host is macOS.
 *
 * The rich JNA bridge (AppKit / ApplicationServices / CoreFoundation bindings, ObjC
 * runtime helpers) is deliberately kept `internal` to `jvmMain` so non-JVM targets never
 * link against JNA. Multiplatform code that only needs the boolean answer calls this
 * `expect` instead.
 *
 * Actuals:
 *  - jvm: real detection via `System.getProperty("os.name")` (see [isMacOsHost]).
 *  - android / ios / wasmJs: always `false` — these are not macOS desktop hosts, and the
 *    macOS native scaffolding is unavailable there.
 */
expect fun isMacOs(): Boolean
