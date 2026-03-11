@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups.contract.repository

import kotlinx.datetime.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a membership record from the database.
 */
data class MembershipRecord(
    val userId: Uuid,
    val groupId: Uuid,
    val role: String,
    val joinedAt: LocalDateTime,
)

/**
 * Represents a membership joined with user details for API responses.
 */
data class MemberWithUserRecord(
    val userId: Uuid,
    val email: String,
    val name: String,
    val role: String,
    val joinedAt: LocalDateTime,
)

/**
 * Database access for user-group membership operations.
 */
interface MembershipRepository {
    suspend fun findByUserAndGroup(userId: Uuid, groupId: Uuid): MembershipRecord?
    suspend fun findByUserId(userId: Uuid): List<MembershipRecord>
    suspend fun listByGroupWithUsers(groupId: Uuid, cursor: Uuid?, limit: Int): List<MemberWithUserRecord>
    suspend fun countByGroup(groupId: Uuid): Long
    suspend fun insert(userId: Uuid, groupId: Uuid, role: String)
    suspend fun deleteByGroup(groupId: Uuid): Int
    suspend fun delete(userId: Uuid, groupId: Uuid): Boolean
}
