@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.m2f.server.auth.service

import arrow.core.nonEmptyListOf
import arrow.core.raise.Raise
import arrow.core.raise.withError
import arrow.core.raise.zipOrAccumulate
import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.IncorrectInput
import com.m2f.core.config.server.InvalidField
import com.m2f.server.auth.errors.InvalidCredentials
import com.m2f.server.auth.errors.TokenInvalid
import com.m2f.server.auth.errors.UserAlreadyExists
import com.m2f.server.auth.repository.RefreshTokenRepository
import com.m2f.server.auth.repository.UserRepository
import com.m2f.server.auth.security.JwtTokenProvider
import com.m2f.server.auth.security.PasswordHasher
import com.m2f.template.models.FieldError
import com.m2f.template.models.dto.AuthResponse
import com.m2f.template.models.dto.LoginRequest
import com.m2f.template.models.dto.RefreshTokenRequest
import com.m2f.template.models.dto.RegisterRequest
import com.m2f.template.models.validation.validateEmail
import com.m2f.template.models.validation.validateName
import com.m2f.template.models.validation.validatePassword
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Authentication service handling registration, login, token refresh, and logout.
 * Uses Arrow Raise for error handling -- zero try/catch for domain errors.
 */
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: JwtTokenProvider,
) {

    /**
     * Register a new user with accumulated validation.
     * Returns an [AuthResponse] with access and refresh tokens on success.
     *
     * @throws DomainError via Raise if validation fails or user already exists.
     */
    context(raise: Raise<DomainError>)
    suspend fun register(request: RegisterRequest): AuthResponse {
        // Step 1: Validate all fields with accumulated errors
        val (validEmail, validPassword, validName) = raise.withError(
            { errors: arrow.core.NonEmptyList<FieldError> ->
                IncorrectInput(
                    errors.map { fieldError ->
                        object : InvalidField {
                            override val field: String = fieldError.field
                            override val errors = nonEmptyListOf(fieldError.message)
                        }
                    }
                )
            }
        ) {
            zipOrAccumulate(
                { validateEmail(request.email) },
                { validatePassword(request.password) },
                { validateName(request.name) },
            ) { email, password, name -> Triple(email, password, name) }
        }

        // Step 2: Check for duplicate email
        val existingUser = userRepository.findByEmail(validEmail)
        if (existingUser != null) {
            raise.raise(UserAlreadyExists())
        }

        // Step 3: Hash password
        val hash = passwordHasher.hash(validPassword)

        // Step 4: Insert user
        val userId = userRepository.insert(validEmail, hash, validName, "USER")

        // Step 5: Generate token pair
        val (authResponse, rawRefreshToken) = tokenProvider.generateTokenPair(
            userId.toString(),
            "USER",
        )

        // Step 6: Hash and store refresh token
        val hashedToken = tokenProvider.hashRefreshToken(rawRefreshToken)
        val expiresAt = Clock.System.now()
            .plus(tokenProvider.getRefreshTokenExpiry().milliseconds)
            .toLocalDateTime(TimeZone.UTC)
        refreshTokenRepository.store(userId, hashedToken, expiresAt)

        // Step 7: Return the response
        return authResponse
    }

    /**
     * Authenticate a user with email and password.
     * Returns an [AuthResponse] with access and refresh tokens on success.
     * Uses the same [InvalidCredentials] error for both wrong password and non-existent email
     * to prevent user enumeration.
     */
    context(raise: Raise<DomainError>)
    suspend fun login(request: LoginRequest): AuthResponse {
        // Step 1: Find user by email -- same error for missing user (prevents enumeration)
        val user = userRepository.findByEmail(request.email)
            ?: raise.raise(InvalidCredentials())

        // Step 2: Verify password -- same generic error
        val passwordValid = passwordHasher.verify(request.password, user.passwordHash)
        if (!passwordValid) {
            raise.raise(InvalidCredentials())
        }

        // Step 3: Generate token pair
        val (authResponse, rawRefreshToken) = tokenProvider.generateTokenPair(
            user.id.toString(),
            user.role,
        )

        // Step 4: Hash and store refresh token
        val hashedToken = tokenProvider.hashRefreshToken(rawRefreshToken)
        val expiresAt = Clock.System.now()
            .plus(tokenProvider.getRefreshTokenExpiry().milliseconds)
            .toLocalDateTime(TimeZone.UTC)
        refreshTokenRepository.store(user.id, hashedToken, expiresAt)

        return authResponse
    }

    /**
     * Refresh an access token using a valid refresh token.
     * Implements refresh token rotation: the old token is revoked and a new pair is issued.
     * Reusing a revoked token will fail with [TokenInvalid].
     */
    context(raise: Raise<DomainError>)
    suspend fun refresh(request: RefreshTokenRequest): AuthResponse {
        // Step 1: Hash the incoming refresh token for lookup
        val hashedToken = tokenProvider.hashRefreshToken(request.refreshToken)

        // Step 2: Find valid (non-revoked, non-expired) token
        val tokenRecord = refreshTokenRepository.findValidToken(hashedToken)
            ?: raise.raise(TokenInvalid())

        // Step 3: Revoke old token (rotation)
        refreshTokenRepository.revokeById(tokenRecord.id)

        // Step 4: Look up user for role (deleted user = invalid token)
        val user = userRepository.findById(tokenRecord.userId)
            ?: raise.raise(TokenInvalid())

        // Step 5: Generate new token pair
        val (authResponse, rawRefreshToken) = tokenProvider.generateTokenPair(
            user.id.toString(),
            user.role,
        )

        // Step 6: Hash and store new refresh token
        val newHashedToken = tokenProvider.hashRefreshToken(rawRefreshToken)
        val expiresAt = Clock.System.now()
            .plus(tokenProvider.getRefreshTokenExpiry().milliseconds)
            .toLocalDateTime(TimeZone.UTC)
        refreshTokenRepository.store(user.id, newHashedToken, expiresAt)

        return authResponse
    }

    /**
     * Log out a user by revoking all their refresh tokens.
     * The userId comes from the JWT principal (already authenticated).
     */
    context(raise: Raise<DomainError>)
    suspend fun logout(userId: String): Map<String, String> {
        val uuid = Uuid.parse(userId)
        refreshTokenRepository.revokeByUserId(uuid)
        return mapOf("message" to "Logged out successfully")
    }
}
