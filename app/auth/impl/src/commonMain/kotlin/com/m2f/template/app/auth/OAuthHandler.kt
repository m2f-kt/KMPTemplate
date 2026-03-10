package com.m2f.template.app.auth

/**
 * Platform-specific handler for initiating OAuth flows.
 *
 * Opens the server's OAuth URL in the platform browser and provides
 * the platform-specific redirect URI for receiving the callback.
 *
 * @param serverBaseUrl Base URL of the server (e.g., "http://localhost:8080").
 */
expect class OAuthHandler(serverBaseUrl: String) {
    /**
     * Opens the OAuth authorization URL for the given provider in the platform browser.
     * The URL includes a `state` parameter containing the platform redirect URI
     * so the server knows where to redirect after authentication.
     *
     * @param provider OAuth provider name ("google" or "apple").
     */
    fun startOAuth(provider: String)

    /**
     * Returns the platform-specific URI where the OAuth callback should redirect.
     * - WASM: same-origin `/auth/callback` URL
     * - Android/iOS: custom scheme `template://auth/callback`
     * - JVM Desktop: `http://localhost:9876/auth/callback`
     */
    fun getRedirectUri(): String
}
