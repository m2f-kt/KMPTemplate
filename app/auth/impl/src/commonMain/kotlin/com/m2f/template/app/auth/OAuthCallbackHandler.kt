package com.m2f.template.app.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.m2f.template.storage.TokenStorage

/**
 * Composable that handles the OAuth callback by storing tokens and signaling navigation.
 *
 * Extracts JWT access and refresh tokens received from the server OAuth redirect,
 * persists them via [TokenStorage], and invokes [onSuccess] to trigger dashboard navigation.
 *
 * @param accessToken JWT access token from the OAuth callback redirect.
 * @param refreshToken JWT refresh token from the OAuth callback redirect.
 * @param tokenStorage Storage for persisting authentication tokens.
 * @param onSuccess Called after tokens are successfully saved.
 * @param onError Called if token storage fails.
 */
@Composable
fun OAuthCallbackHandler(
    accessToken: String,
    refreshToken: String,
    tokenStorage: TokenStorage,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    LaunchedEffect(accessToken, refreshToken) {
        try {
            tokenStorage.saveTokens(accessToken, refreshToken)
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Failed to save OAuth tokens")
        }
    }
}
