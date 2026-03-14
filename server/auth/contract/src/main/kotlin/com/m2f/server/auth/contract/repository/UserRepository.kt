@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.auth.contract.repository

import com.m2f.template.models.UserRole
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a user record from the database.
 */
data class UserRecord(
    val id: Uuid,
    val email: String,
    val passwordHash: String,
    val name: String,
    val role: UserRole,
    val avatarUrl: String? = null,
    val processingRestricted: Boolean = false,
)

/**
 * Database access for user CRUD operations.
 */
interface UserRepository {
    suspend fun findByEmail(email: String): UserRecord?
    suspend fun findById(id: Uuid): UserRecord?
    suspend fun insert(email: String, passwordHash: String, name: String, role: UserRole): Uuid
    suspend fun updateProfile(id: Uuid, name: String?, email: String?): Boolean
    suspend fun updatePasswordHash(id: Uuid, passwordHash: String): Boolean
    suspend fun count(): Long
    suspend fun updateAvatarUrl(id: Uuid, avatarUrl: String): Boolean
    suspend fun updateProcessingRestricted(id: Uuid, restricted: Boolean): Boolean
}
