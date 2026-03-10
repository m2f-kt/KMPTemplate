package com.m2f.template.app.auth

import kotlinx.browser.window

/**
 * WASM (browser) OAuth handler.
 * Navigates the full browser window to the OAuth URL.
 * After OAuth consent, the browser redirects back to the same origin `/auth/callback`.
 */
actual class OAuthHandler actual constructor(private val serverBaseUrl: String) {
    actual fun startOAuth(provider: String) {
        val redirectUri = getRedirectUri()
        window.location.href =
            "$serverBaseUrl/api/auth/oauth/$provider?state=$redirectUri"
    }

    actual fun getRedirectUri(): String =
        "${window.location.origin}/auth/callback"
}
