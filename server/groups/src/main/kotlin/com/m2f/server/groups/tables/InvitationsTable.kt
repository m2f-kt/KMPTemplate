@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups.tables

import com.m2f.server.auth.tables.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

private const val TOKEN_LENGTH = 64
private const val EMAIL_LENGTH = 255
private const val ROLE_LENGTH = 50

/**
 * Exposed Table definition for the invitations table.
 * Each invitation has a unique token for URL-based acceptance.
 * Invitations expire after 7 days from creation.
 */
object InvitationsTable : Table("invitations") {
    val id = uuid("id").autoGenerate()
    val token = varchar("token", TOKEN_LENGTH).uniqueIndex()
    val groupId = uuid("group_id").references(GroupsTable.id)
    val email = varchar("email", EMAIL_LENGTH)
    val invitedBy = uuid("invited_by").references(UsersTable.id)
    val role = varchar("role", ROLE_LENGTH).default("MEMBER")
    val expiresAt = datetime("expires_at")
    val acceptedAt = datetime("accepted_at").nullable()
    val acceptedBy = uuid("accepted_by").references(UsersTable.id).nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
