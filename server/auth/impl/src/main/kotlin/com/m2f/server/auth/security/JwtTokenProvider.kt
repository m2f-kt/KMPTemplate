package com.m2f.server.auth.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.m2f.core.config.configuration.Configuration
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.AuthResponse
import java.security.MessageDigest
import java.util.Date
import java.util.UUID

/**
 * JWT token generation and refresh token management.
 * Uses the same HMAC256 algorithm and configuration as the SecurityPlugin verifier.
 */
class JwtTokenProvider(config: Configuration) {

    private val secret: String = config.env.auth.secret
    private val audience: String = config.env.auth.audience
    private val issuer: String = config.env.auth.issuer
    private val accessTokenExpiry: Long = config.env.auth.accessTokenExpiry
    private val refreshTokenExpiry: Long = config.env.auth.refreshTokenExpiry

    /**
     * Generate a JWT access token for the given user.
     * The role claim is encoded as the string value (e.g. "ADMIN") for backward compatibility.
     */
    fun generateAccessToken(userId: String, role: UserRole): String =
        JWT.create()
            .withSubject(userId)
            .withClaim("role", role.value)
            .withAudience(audience)
            .withIssuer(issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiry))
            .sign(Algorithm.HMAC256(secret))

    /**
     * Generate a random refresh token string.
     */
    fun generateRefreshToken(): String = UUID.randomUUID().toString()

    /**
     * Generate both an access token and a refresh token.
     * Returns a pair of (AuthResponse for the client, raw refresh token for DB hashing).
     */
    fun generateTokenPair(userId: String, role: UserRole): Pair<AuthResponse, String> {
        val accessToken = generateAccessToken(userId, role)
        val rawRefreshToken = generateRefreshToken()
        val response = AuthResponse(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            expiresIn = accessTokenExpiry / MILLIS_PER_SECOND,
        )
        return response to rawRefreshToken
    }

    /**
     * Get the refresh token expiry duration in milliseconds.
     */
    fun getRefreshTokenExpiry(): Long = refreshTokenExpiry

    /**
     * Hash a refresh token using SHA-256 for secure database storage.
     */
    fun hashRefreshToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1000L
    }
}
