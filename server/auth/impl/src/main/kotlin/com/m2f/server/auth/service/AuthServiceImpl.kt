@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.m2f.server.auth.service

import arrow.core.nonEmptyListOf
import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.withError
import arrow.core.raise.zipOrAccumulate
import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.IncorrectInput
import com.m2f.core.config.server.InvalidField
import com.m2f.server.auth.contract.errors.InvalidCredentials
import com.m2f.server.auth.contract.errors.TokenInvalid
import com.m2f.server.auth.contract.errors.UserAlreadyExists
import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.server.auth.contract.security.PasswordHasher
import com.m2f.server.auth.contract.service.AuthService
import com.m2f.server.auth.repository.RefreshTokenRepository
import com.m2f.server.auth.security.JwtTokenProvider
import com.m2f.template.models.FieldError
import com.m2f.template.models.UserRole
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
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: JwtTokenProvider,
    private val onRegistered: (suspend (raise: Raise<DomainError>, userId: String, request: RegisterRequest) -> Unit)? = null,
) : AuthService {

    context(raise: Raise<DomainError>)
    override suspend fun register(request: RegisterRequest): AuthResponse {
        val (validEmail, validPassword, validFirstName, validLastName) = raise.withError(
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
                { validateName(request.firstName) },
                { validateName(request.lastName) },
            ) { email, password, firstName, lastName ->
                RegisterFields(email, password, firstName, lastName)
            }
        }

        val fullName = "$validFirstName $validLastName"
        ensure(userRepository.findByEmail(validEmail) == null) { UserAlreadyExists() }
        val hash = passwordHasher.hash(validPassword)
        val userId = userRepository.insert(validEmail, hash, fullName, UserRole.User)

        if (request.invitationToken != null && onRegistered != null) {
            onRegistered.invoke(raise, userId.toString(), request)
        }

        val (authResponse, rawRefreshToken) = tokenProvider.generateTokenPair(
            userId.toString(),
            UserRole.User,
        )

        val hashedToken = tokenProvider.hashRefreshToken(rawRefreshToken)
        val expiresAt = Clock.System.now()
            .plus(tokenProvider.getRefreshTokenExpiry().milliseconds)
            .toLocalDateTime(TimeZone.UTC)
        refreshTokenRepository.store(userId, hashedToken, expiresAt)

        return authResponse
    }

    context(raise: Raise<DomainError>)
    override suspend fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: raise.raise(InvalidCredentials())

        ensure(passwordHasher.verify(request.password, user.passwordHash)) { InvalidCredentials() }

        val (authResponse, rawRefreshToken) = tokenProvider.generateTokenPair(
            user.id.toString(),
            user.role,
        )

        val hashedToken = tokenProvider.hashRefreshToken(rawRefreshToken)
        val expiresAt = Clock.System.now()
            .plus(tokenProvider.getRefreshTokenExpiry().milliseconds)
            .toLocalDateTime(TimeZone.UTC)
        refreshTokenRepository.store(user.id, hashedToken, expiresAt)

        return authResponse
    }

    context(raise: Raise<DomainError>)
    override suspend fun refresh(request: RefreshTokenRequest): AuthResponse {
        val hashedToken = tokenProvider.hashRefreshToken(request.refreshToken)

        val tokenRecord = refreshTokenRepository.findValidToken(hashedToken)
            ?: raise.raise(TokenInvalid())

        refreshTokenRepository.revokeById(tokenRecord.id)

        val user = userRepository.findById(tokenRecord.userId)
            ?: raise.raise(TokenInvalid())

        val (authResponse, rawRefreshToken) = tokenProvider.generateTokenPair(
            user.id.toString(),
            user.role,
        )

        val newHashedToken = tokenProvider.hashRefreshToken(rawRefreshToken)
        val expiresAt = Clock.System.now()
            .plus(tokenProvider.getRefreshTokenExpiry().milliseconds)
            .toLocalDateTime(TimeZone.UTC)
        refreshTokenRepository.store(user.id, newHashedToken, expiresAt)

        return authResponse
    }

    context(raise: Raise<DomainError>)
    override suspend fun logout(userId: String): Map<String, String> {
        val uuid = Uuid.parse(userId)
        refreshTokenRepository.revokeByUserId(uuid)
        return mapOf("message" to "Logged out successfully")
    }
}

private data class RegisterFields(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
)
