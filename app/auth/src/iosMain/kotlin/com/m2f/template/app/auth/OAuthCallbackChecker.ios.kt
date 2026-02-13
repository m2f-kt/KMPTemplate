package com.m2f.template.app.auth

/**
 * iOS implementation: returns null.
 * Custom scheme OAuth callbacks are handled via the iOS app delegate / scene delegate.
 */
actual fun checkOAuthCallback(): Pair<String, String>? = null
