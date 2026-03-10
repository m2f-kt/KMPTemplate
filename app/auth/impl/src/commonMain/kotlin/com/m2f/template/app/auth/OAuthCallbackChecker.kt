package com.m2f.template.app.auth

/**
 * Platform-specific check for OAuth callback parameters on app entry.
 *
 * - WASM: Checks `window.location` for `access_token` and `refresh_token` query params
 * - Android: Returns null (deep link handled via intent in composable)
 * - iOS: Returns null (custom scheme handled via app delegate)
 * - JVM Desktop: Returns null (localhost server publishes via OAuthHandler.oauthResult flow)
 *
 * @return Pair of (accessToken, refreshToken) if OAuth callback detected, null otherwise.
 */
expect fun checkOAuthCallback(): Pair<String, String>?
