@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.auth.contract.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

private const val EMAIL_LENGTH = 255
private const val PASSWORD_HASH_LENGTH = 255
private const val NAME_LENGTH = 100
private const val AVATAR_URL_LENGTH = 512

/**
 * Exposed Table definition for the users table.
 * The roleId column is a foreign key referencing [RolesTable.id], defaulting to 1 (User).
 */
object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val email = varchar("email", EMAIL_LENGTH).uniqueIndex()
    val passwordHash = varchar("password_hash", PASSWORD_HASH_LENGTH)
    val name = varchar("name", NAME_LENGTH)
    val avatarUrl = varchar("avatar_url", AVATAR_URL_LENGTH).nullable()
    val roleId = integer("role_id").references(RolesTable.id).default(1)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
