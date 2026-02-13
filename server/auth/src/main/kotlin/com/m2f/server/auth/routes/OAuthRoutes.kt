package com.m2f.server.auth.routes

import arrow.core.raise.either
import com.m2f.core.config.configuration.Env
import com.m2f.server.auth.service.OAuthService
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * Allowed redirect URI patterns for OAuth callbacks.
 * Prevents open-redirect attacks by validating the redirect target.
 */
private fun isAllowedRedirectUri(uri: String, env: Env.OAuth): Boolean {
    val allowedPatterns = listOf(
        env.wasmRedirectUrl,
        "${env.mobileScheme}://auth/callback",
        "http://localhost:${env.desktopLocalhostPort}/auth/callback",
    )
    return allowedPatterns.any { pattern -> uri.startsWith(pattern) }
}

/**
 * Resolves and validates the redirect URI from the OAuth state parameter.
 * Falls back to WASM redirect URL if state is missing or not in the allowlist.
 */
private fun resolveRedirectUri(state: String?, oauthEnv: Env.OAuth): String {
    val redirectUri = state ?: return oauthEnv.wasmRedirectUrl
    return if (isAllowedRedirectUri(redirectUri, oauthEnv)) redirectUri else oauthEnv.wasmRedirectUrl
}

/**
 * OAuth routes for Google and Apple social login.
 * The initial GET endpoints trigger Ktor's OAuth redirect flow.
 * The callback endpoints handle the provider response and redirect
 * the browser to the client with JWT tokens in URL parameters.
 */
fun Route.oauthRoutes(oauthService: OAuthService, oauthEnv: Env.OAuth) {
    route("/api/auth/oauth") {
        // Google OAuth
        authenticate("google-oauth") {
            get("/google") {
                // Ktor automatically redirects to Google's authorization page
            }
            get("/google/callback") {
                val redirectUri = resolveRedirectUri(
                    call.request.queryParameters["state"],
                    oauthEnv,
                )
                val result = either { oauthService.handleGoogleCallback(call.principal<OAuthAccessTokenResponse.OAuth2>()) }
                result.fold(
                    ifLeft = {
                        val separator = if ("?" in redirectUri) "&" else "?"
                        call.respondRedirect("$redirectUri${separator}error=oauth_failed")
                    },
                    ifRight = { authResponse ->
                        val separator = if ("?" in redirectUri) "&" else "?"
                        call.respondRedirect(
                            "$redirectUri${separator}access_token=${authResponse.accessToken}" +
                                "&refresh_token=${authResponse.refreshToken}",
                        )
                    },
                )
            }
        }

        // Apple OAuth
        authenticate("apple-oauth") {
            get("/apple") {
                // Ktor automatically redirects to Apple's authorization page
            }
            get("/apple/callback") {
                val redirectUri = resolveRedirectUri(
                    call.request.queryParameters["state"],
                    oauthEnv,
                )
                val result = either { oauthService.handleAppleCallback(call.principal<OAuthAccessTokenResponse.OAuth2>()) }
                result.fold(
                    ifLeft = {
                        val separator = if ("?" in redirectUri) "&" else "?"
                        call.respondRedirect("$redirectUri${separator}error=oauth_failed")
                    },
                    ifRight = { authResponse ->
                        val separator = if ("?" in redirectUri) "&" else "?"
                        call.respondRedirect(
                            "$redirectUri${separator}access_token=${authResponse.accessToken}" +
                                "&refresh_token=${authResponse.refreshToken}",
                        )
                    },
                )
            }
        }
    }
}
