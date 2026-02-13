@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.m2f.server.auth.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.server.auth.errors.TokenInvalid
import com.m2f.server.auth.errors.UserNotFound
import com.m2f.server.auth.repository.PasswordResetTokenRepository
import com.m2f.server.auth.repository.UserRepository
import com.m2f.server.auth.security.JwtTokenProvider
import com.m2f.server.auth.security.PasswordHasher
import com.m2f.template.models.dto.ForgotPasswordRequest
import com.m2f.template.models.dto.ResetPasswordRequest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val RESET_TOKEN_EXPIRY = 1.hours

/**
 * Service for password reset flow: generating reset tokens and handling password changes.
 * Uses Arrow Raise for error handling, consistent with [AuthService].
 */
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: JwtTokenProvider,
) {

    /**
     * Handle a forgot-password request.
     * If the email exists, generates a reset token, hashes and stores it,
     * and logs the reset link to console (dev mode -- no real email).
     *
     * Always returns success to prevent user enumeration.
     */
    context(raise: Raise<DomainError>)
    suspend fun forgotPassword(request: ForgotPasswordRequest): Map<String, String> {
        val user = userRepository.findByEmail(request.email)
        if (user != null) {
            // Generate a random token
            val rawToken = Uuid.random().toString()
            val hashedToken = tokenProvider.hashRefreshToken(rawToken)

            // Store with expiry
            val expiresAt = Clock.System.now()
                .plus(RESET_TOKEN_EXPIRY)
                .toLocalDateTime(TimeZone.UTC)
            passwordResetTokenRepository.store(user.id, hashedToken, expiresAt)

            // Dev mode: log reset link to console
            println("[DEV] Password reset link: /reset-password?token=$rawToken")
        }
        // Always return success (prevents user enumeration)
        return mapOf("message" to "If the email exists, a reset link has been sent")
    }

    /**
     * Handle a password reset request.
     * Validates the token, updates the user's password hash, and marks the token as used.
     */
    context(raise: Raise<DomainError>)
    suspend fun resetPassword(request: ResetPasswordRequest): Map<String, String> {
        // Hash the incoming token for lookup
        val hashedToken = tokenProvider.hashRefreshToken(request.token)

        // Find valid (unused, non-expired) token
        val tokenRecord = passwordResetTokenRepository.findValidToken(hashedToken)
            ?: raise.raise(TokenInvalid())

        // Find the user
        val user = userRepository.findById(tokenRecord.userId)
            ?: raise.raise(UserNotFound())

        // Hash the new password
        val newPasswordHash = passwordHasher.hash(request.newPassword)

        // Update password via UserRepository
        userRepository.updatePasswordHash(user.id, newPasswordHash)

        // Mark the token as used
        passwordResetTokenRepository.markUsed(tokenRecord.id)

        return mapOf("message" to "Password has been reset successfully")
    }
}
