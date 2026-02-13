@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.auth.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

private const val TOKEN_HASH_LENGTH = 512

/**
 * Exposed Table definition for the password_reset_tokens table.
 * Stores hashed reset tokens with expiry and usage tracking.
 */
object PasswordResetTokensTable : Table("password_reset_tokens") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val tokenHash = varchar("token_hash", TOKEN_HASH_LENGTH)
    val expiresAt = datetime("expires_at")
    val used = bool("used").default(false)

    override val primaryKey = PrimaryKey(id)
}
