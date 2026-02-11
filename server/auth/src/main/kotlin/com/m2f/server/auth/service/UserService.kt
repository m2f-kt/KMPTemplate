@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.auth.service

import arrow.core.nonEmptyListOf
import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.raise.withError
import arrow.core.raise.zipOrAccumulate
import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.IncorrectInput
import com.m2f.core.config.server.InvalidField
import com.m2f.server.auth.errors.UserAlreadyExists
import com.m2f.server.auth.errors.UserNotFound
import com.m2f.server.auth.repository.UserRecord
import com.m2f.server.auth.repository.UserRepository
import com.m2f.template.models.FieldError
import com.m2f.template.models.dto.UpdateProfileRequest
import com.m2f.template.models.dto.UserResponse
import com.m2f.template.models.validation.validateEmail
import com.m2f.template.models.validation.validateName
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * User profile management service.
 * Handles profile retrieval, updates, and admin user lookup.
 * Uses Arrow Raise for error handling -- zero try/catch.
 */
class UserService(
    private val userRepository: UserRepository,
) {

    /**
     * Get the authenticated user's own profile.
     */
    context(raise: Raise<DomainError>)
    suspend fun getProfile(userId: String): UserResponse {
        val uuid = Uuid.parse(userId)
        val user = userRepository.findById(uuid)
        raise.ensure(user != null) { UserNotFound() }
        return user.toUserResponse()
    }

    /**
     * Update the authenticated user's profile with accumulated validation.
     * At least one field (name or email) must be provided.
     */
    context(raise: Raise<DomainError>)
    suspend fun updateProfile(userId: String, request: UpdateProfileRequest): UserResponse {
        val uuid = Uuid.parse(userId)

        // Ensure at least one field is provided
        raise.ensure(request.name != null || request.email != null) {
            IncorrectInput(
                nonEmptyListOf(
                    object : InvalidField {
                        override val field: String = "body"
                        override val errors = nonEmptyListOf("At least one field (name or email) must be provided")
                    },
                ),
            )
        }

        // Validate provided fields with accumulated errors
        val (validName, validEmail) = raise.withError(
            { errors: arrow.core.NonEmptyList<FieldError> ->
                IncorrectInput(
                    errors.map { fieldError ->
                        object : InvalidField {
                            override val field: String = fieldError.field
                            override val errors = nonEmptyListOf(fieldError.message)
                        }
                    },
                )
            },
        ) {
            zipOrAccumulate(
                { request.name?.let { validateName(it) } },
                { request.email?.let { validateEmail(it) } },
            ) { name, email -> Pair(name, email) }
        }

        // If email is being changed, check uniqueness
        if (validEmail != null) {
            val existingUser = userRepository.findByEmail(validEmail)
            if (existingUser != null && existingUser.id != uuid) {
                raise.raise(UserAlreadyExists())
            }
        }

        // Update profile
        userRepository.updateProfile(uuid, validName, validEmail)

        // Fetch and return updated profile
        val updatedUser = userRepository.findById(uuid)
        raise.ensure(updatedUser != null) { UserNotFound() }
        return updatedUser.toUserResponse()
    }

    /**
     * Admin: get any user's profile by ID.
     */
    context(raise: Raise<DomainError>)
    suspend fun getUserById(targetUserId: String): UserResponse {
        val uuid = Uuid.parse(targetUserId)
        val user = userRepository.findById(uuid)
        raise.ensure(user != null) { UserNotFound() }
        return user.toUserResponse()
    }
}

private fun UserRecord.toUserResponse(): UserResponse = UserResponse(
    id = id.toString(),
    email = email,
    name = name,
    role = role,
)
