@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.m2f.server.auth.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.UnexpectedError
import com.m2f.server.auth.repository.UserRepository
import com.m2f.server.auth.security.JwtTokenProvider
import com.m2f.server.auth.security.PasswordHasher
import com.m2f.template.models.dto.AuthResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.server.auth.OAuthAccessTokenResponse
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo"

/**
 * Google userinfo response (subset of fields we need).
 */
@Serializable
private data class GoogleUserInfo(
    val id: String,
    val email: String,
    val name: String = "",
    @SerialName("verified_email")
    val verifiedEmail: Boolean = false,
)

/**
 * Handles OAuth callbacks for Google and Apple providers.
 * Creates users if they don't exist, or logs them in if they do.
 */
class OAuthService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: JwtTokenProvider,
    private val httpClient: HttpClient,
    private val refreshTokenRepository: com.m2f.server.auth.repository.RefreshTokenRepository,
) {

    /**
     * Handle Google OAuth callback.
     * Extracts email from Google userinfo endpoint, creates or finds user, returns token pair.
     */
    context(raise: Raise<DomainError>)
    suspend fun handleGoogleCallback(principal: OAuthAccessTokenResponse.OAuth2?): AuthResponse {
        val accessToken = principal?.accessToken
            ?: raise.raise(UnexpectedError("Missing OAuth access token from Google"))

        // Fetch user info from Google
        val userInfo: GoogleUserInfo = try {
            httpClient.get(GOOGLE_USERINFO_URL) {
                bearerAuth(accessToken)
            }.body()
        } catch (e: Exception) {
            raise.raise(UnexpectedError("Failed to fetch Google user info: ${e.message}"))
        }

        return findOrCreateUser(
            email = userInfo.email,
            name = userInfo.name.ifBlank { userInfo.email.substringBefore("@") },
        )
    }

    /**
     * Handle Apple OAuth callback.
     * Decodes the id_token JWT to extract email, creates or finds user, returns token pair.
     */
    context(raise: Raise<DomainError>)
    suspend fun handleAppleCallback(principal: OAuthAccessTokenResponse.OAuth2?): AuthResponse {
        val idToken = principal?.extraParameters?.get("id_token")
            ?: raise.raise(UnexpectedError("Missing id_token from Apple OAuth response"))

        // Decode JWT payload (Apple id_token is a standard JWT)
        val email = try {
            decodeAppleIdToken(idToken)
        } catch (e: Exception) {
            raise.raise(UnexpectedError("Failed to decode Apple id_token: ${e.message}"))
        }

        return findOrCreateUser(
            email = email,
            name = email.substringBefore("@"),
        )
    }

    /**
     * Find an existing user by email or create a new one, then generate a token pair.
     */
    private suspend fun findOrCreateUser(
        email: String,
        name: String,
    ): AuthResponse {
        val existingUser = userRepository.findByEmail(email)
        val userId: Uuid
        val role: String

        if (existingUser != null) {
            // User exists -- log them in
            userId = existingUser.id
            role = existingUser.role
        } else {
            // Create new user with a random password hash (OAuth users don't use passwords)
            val randomHash = passwordHasher.hash(Uuid.random().toString())
            userId = userRepository.insert(email, randomHash, name, "USER")
            role = "USER"
        }

        // Generate token pair
        val (authResponse, rawRefreshToken) = tokenProvider.generateTokenPair(
            userId.toString(),
            role,
        )

        // Hash and store refresh token
        val hashedToken = tokenProvider.hashRefreshToken(rawRefreshToken)
        val expiresAt = Clock.System.now()
            .plus(tokenProvider.getRefreshTokenExpiry().milliseconds)
            .toLocalDateTime(TimeZone.UTC)
        refreshTokenRepository.store(userId, hashedToken, expiresAt)

        return authResponse
    }

    /**
     * Decode an Apple id_token JWT to extract the email claim.
     * Apple id_tokens are standard JWTs with email in the payload.
     */
    private fun decodeAppleIdToken(idToken: String): String {
        val parts = idToken.split(".")
        require(parts.size >= 2) { "Invalid JWT format" }
        // Base64 decode the payload (second part)
        val payload = java.util.Base64.getUrlDecoder().decode(parts[1]).toString(Charsets.UTF_8)
        // Simple JSON extraction for email field
        val emailRegex = """"email"\s*:\s*"([^"]+)"""".toRegex()
        return emailRegex.find(payload)?.groupValues?.get(1)
            ?: error("Email not found in Apple id_token")
    }
}
