package com.m2f.template.app.auth

/**
 * Platform-conditional flag for showing the Apple Sign-In button.
 *
 * Apple Sign-In is available on iOS and WASM (web) platforms.
 * Android and JVM desktop do not support Apple Sign-In natively.
 */
expect fun showAppleSignIn(): Boolean
