@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups.contract.tables

import com.m2f.server.auth.contract.tables.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

private const val ROLE_LENGTH = 20

/**
 * Join table for user-group memberships.
 * Composite primary key (userId, groupId) enforces one membership per user per group.
 * The role column stores the group-level role (OWNER, ADMIN, MEMBER) --
 * separate from the system-level [UserRole].
 */
object UserGroupMembershipsTable : Table("user_group_memberships") {
    val userId = uuid("user_id").references(UsersTable.id)
    val groupId = uuid("group_id").references(GroupsTable.id)
    val role = varchar("role", ROLE_LENGTH).default("MEMBER")
    val joinedAt = datetime("joined_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(userId, groupId)
}
