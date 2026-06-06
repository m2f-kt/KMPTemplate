package com.m2f.template.platform

/** wasmJs `actual` for [isMacOs] — the browser/Wasm host has no macOS native bridge. */
actual fun isMacOs(): Boolean = false
