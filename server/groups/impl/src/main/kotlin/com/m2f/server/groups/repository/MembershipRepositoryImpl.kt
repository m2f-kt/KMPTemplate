@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups.repository

import com.m2f.server.auth.contract.tables.UsersTable
import com.m2f.server.groups.contract.repository.MemberWithUserRecord
import com.m2f.server.groups.contract.repository.MembershipRecord
import com.m2f.server.groups.contract.repository.MembershipRepository
import com.m2f.server.groups.contract.tables.UserGroupMembershipsTable
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
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
 * Database access for user-group membership operations.
 */
class MembershipRepositoryImpl(private val db: R2dbcDatabase) : MembershipRepository {

    override suspend fun findByUserAndGroup(userId: Uuid, groupId: Uuid): MembershipRecord? =
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

    override suspend fun findByUserId(userId: Uuid): List<MembershipRecord> =
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
    override suspend fun listByGroupWithUsers(
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

    override suspend fun countByGroup(groupId: Uuid): Long = suspendTransaction(db = db) {
        UserGroupMembershipsTable
            .select(UserGroupMembershipsTable.columns)
            .where { UserGroupMembershipsTable.groupId eq groupId }
            .count()
    }

    override suspend fun insert(userId: Uuid, groupId: Uuid, role: String): Unit =
        suspendTransaction(db = db) {
            UserGroupMembershipsTable.insert {
                it[UserGroupMembershipsTable.userId] = userId
                it[UserGroupMembershipsTable.groupId] = groupId
                it[UserGroupMembershipsTable.role] = role
            }
        }

    override suspend fun deleteByGroup(groupId: Uuid): Int = suspendTransaction(db = db) {
        UserGroupMembershipsTable.deleteWhere {
            UserGroupMembershipsTable.groupId eq groupId
        }
    }

    override suspend fun delete(userId: Uuid, groupId: Uuid): Boolean = suspendTransaction(db = db) {
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
