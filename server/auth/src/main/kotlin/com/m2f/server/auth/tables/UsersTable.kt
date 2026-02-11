@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.auth.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

private const val EMAIL_LENGTH = 255
private const val PASSWORD_HASH_LENGTH = 255
private const val NAME_LENGTH = 100
private const val ROLE_LENGTH = 50

/**
 * Exposed Table definition for the users table.
 */
object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val email = varchar("email", EMAIL_LENGTH).uniqueIndex()
    val passwordHash = varchar("password_hash", PASSWORD_HASH_LENGTH)
    val name = varchar("name", NAME_LENGTH)
    val role = varchar("role", ROLE_LENGTH).default("USER")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
