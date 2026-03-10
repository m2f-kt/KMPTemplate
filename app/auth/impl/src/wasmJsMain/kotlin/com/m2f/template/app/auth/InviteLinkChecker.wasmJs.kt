package com.m2f.template.app.auth

import kotlinx.browser.window

/**
 * WASM implementation: checks current browser URL for invitation link.
 * Invitation links are formatted as `/invite/accept?token=...`.
 */
actual fun checkInviteLink(): String? {
    val pathname = window.location.pathname
    val search = window.location.search

    // Check if we're on the invite accept path
    if (!pathname.contains("/invite/accept")) return null

    // Parse query params to extract token
    val params = search.removePrefix("?").split("&")
        .filter { it.contains("=") }
        .associate { param ->
            val (key, value) = param.split("=", limit = 2)
            key to value
        }

    return params["token"]
}
