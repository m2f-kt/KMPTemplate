package com.m2f.server.auth.routes

import com.m2f.core.config.server.conduit
import com.m2f.server.auth.service.OAuthService
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * OAuth routes for Google and Apple social login.
 * The initial GET endpoints trigger Ktor's OAuth redirect flow.
 * The callback endpoints handle the provider response and return an [AuthResponse].
 */
fun Route.oauthRoutes(oauthService: OAuthService) {
    route("/api/auth/oauth") {
        // Google OAuth
        authenticate("google-oauth") {
            get("/google") {
                // Ktor automatically redirects to Google's authorization page
            }
            get("/google/callback") {
                conduit {
                    val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()
                    oauthService.handleGoogleCallback(principal)
                }
            }
        }

        // Apple OAuth
        authenticate("apple-oauth") {
            get("/apple") {
                // Ktor automatically redirects to Apple's authorization page
            }
            get("/apple/callback") {
                conduit {
                    val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()
                    oauthService.handleAppleCallback(principal)
                }
            }
        }
    }
}
