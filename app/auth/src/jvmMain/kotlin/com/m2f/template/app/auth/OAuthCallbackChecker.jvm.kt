package com.m2f.template.app.auth

/**
 * JVM Desktop implementation: returns null.
 * OAuth callback is received via OAuthHandler's localhost server
 * and published as a StateFlow (observed in AppNavHost).
 */
actual fun checkOAuthCallback(): Pair<String, String>? = null
