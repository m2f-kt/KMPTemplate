package com.m2f.template.app.auth

import kotlinx.browser.window

/**
 * WASM implementation: checks current browser URL for OAuth callback parameters.
 * After OAuth consent, the browser redirects to `/auth/callback?access_token=...&refresh_token=...`.
 */
actual fun checkOAuthCallback(): Pair<String, String>? {
    val search = window.location.search
    if (!search.contains("access_token")) return null

    val params = search.removePrefix("?").split("&")
        .filter { it.contains("=") }
        .associate { param ->
            val (key, value) = param.split("=", limit = 2)
            key to value
        }

    val accessToken = params["access_token"] ?: return null
    val refreshToken = params["refresh_token"] ?: return null
    return Pair(accessToken, refreshToken)
}
