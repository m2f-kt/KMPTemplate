@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups.repository

import com.m2f.server.auth.tables.UsersTable
import com.m2f.server.groups.tables.UserGroupMembershipsTable
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.GreaterOp
import org.jetbrains.exposed.v1.core.QueryParameter
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.UuidColumnType
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
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
class MembershipRepository(private val db: R2dbcDatabase) {

    suspend fun findByUserAndGroup(userId: Uuid, groupId: Uuid): MembershipRecord? =
        suspendTransaction(db = db) {
            UserGroupMembershipsTable
                .select(UserGroupMembershipsTable.columns)
                .where {
                    (UserGroupMembershipsTable.userId eq userId) and
                        (UserGroupMembershipsTable.groupId eq groupId)
                }
                .singleOrNull()
                ?.toMembershipRecord()
        }

    suspend fun findByUserId(userId: Uuid): List<MembershipRecord> =
        suspendTransaction(db = db) {
            UserGroupMembershipsTable
                .select(UserGroupMembershipsTable.columns)
                .where { UserGroupMembershipsTable.userId eq userId }
                .toList()
                .map { it.toMembershipRecord() }
        }

    /**
     * List members of a group with cursor-based pagination.
     * Cursor is the last-seen userId. Results are ordered by userId ascending.
     * Joins with UsersTable to provide email and name.
     */
    suspend fun listByGroupWithUsers(
        groupId: Uuid,
        cursor: Uuid?,
        limit: Int,
    ): List<MemberWithUserRecord> = suspendTransaction(db = db) {
        val columns = listOf(
            UserGroupMembershipsTable.userId,
            UsersTable.email,
            UsersTable.name,
            UserGroupMembershipsTable.role,
            UserGroupMembershipsTable.joinedAt,
        )
        (UserGroupMembershipsTable innerJoin UsersTable)
            .select(columns)
            .where {
                val base = UserGroupMembershipsTable.groupId eq groupId
                if (cursor != null) {
                    base and GreaterOp(
                        UserGroupMembershipsTable.userId,
                        QueryParameter(cursor, UuidColumnType()),
                    )
                } else {
                    base
                }
            }
            .orderBy(UserGroupMembershipsTable.userId, SortOrder.ASC)
            .limit(limit)
            .toList()
            .map { row ->
                MemberWithUserRecord(
                    userId = row[UserGroupMembershipsTable.userId],
                    email = row[UsersTable.email],
                    name = row[UsersTable.name],
                    role = row[UserGroupMembershipsTable.role],
                    joinedAt = row[UserGroupMembershipsTable.joinedAt],
                )
            }
    }

    suspend fun countByGroup(groupId: Uuid): Long = suspendTransaction(db = db) {
        UserGroupMembershipsTable
            .select(UserGroupMembershipsTable.columns)
            .where { UserGroupMembershipsTable.groupId eq groupId }
            .count()
    }

    suspend fun insert(userId: Uuid, groupId: Uuid, role: String): Unit =
        suspendTransaction(db = db) {
            UserGroupMembershipsTable.insert {
                it[UserGroupMembershipsTable.userId] = userId
                it[UserGroupMembershipsTable.groupId] = groupId
                it[UserGroupMembershipsTable.role] = role
            }
        }

    suspend fun deleteByGroup(groupId: Uuid): Int = suspendTransaction(db = db) {
        UserGroupMembershipsTable.deleteWhere {
            UserGroupMembershipsTable.groupId eq groupId
        }
    }

    suspend fun delete(userId: Uuid, groupId: Uuid): Boolean = suspendTransaction(db = db) {
        val rowsDeleted = UserGroupMembershipsTable.deleteWhere {
            (UserGroupMembershipsTable.userId eq userId) and
                (UserGroupMembershipsTable.groupId eq groupId)
        }
        rowsDeleted > 0
    }
}

private fun ResultRow.toMembershipRecord(): MembershipRecord = MembershipRecord(
    userId = this[UserGroupMembershipsTable.userId],
    groupId = this[UserGroupMembershipsTable.groupId],
    role = this[UserGroupMembershipsTable.role],
    joinedAt = this[UserGroupMembershipsTable.joinedAt],
)
