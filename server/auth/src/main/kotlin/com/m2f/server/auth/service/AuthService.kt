@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.m2f.server.auth.service

import arrow.core.nonEmptyListOf
import arrow.core.raise.Raise
import arrow.core.raise.withError
import arrow.core.raise.zipOrAccumulate
import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.IncorrectInput
import com.m2f.core.config.server.InvalidField
import com.m2f.server.auth.errors.UserAlreadyExists
import com.m2f.server.auth.repository.RefreshTokenRepository
import com.m2f.server.auth.repository.UserRepository
import com.m2f.server.auth.security.JwtTokenProvider
import com.m2f.server.auth.security.PasswordHasher
import com.m2f.template.models.FieldError
import com.m2f.template.models.dto.AuthResponse
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
}
