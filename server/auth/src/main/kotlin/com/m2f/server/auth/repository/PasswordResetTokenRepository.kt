@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.auth.repository

import com.m2f.server.auth.tables.PasswordResetTokensTable
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a password reset token record from the database.
 */
data class PasswordResetTokenRecord(
    val id: Uuid,
    val userId: Uuid,
    val tokenHash: String,
    val expiresAt: LocalDateTime,
    val used: Boolean,
)

/**
 * Database access for password reset token operations.
 */
class PasswordResetTokenRepository(private val db: R2dbcDatabase) {

    /**
     * Store a new password reset token and return the generated UUID.
     */
    suspend fun store(
        userId: Uuid,
        tokenHash: String,
        expiresAt: LocalDateTime,
    ): Uuid = suspendTransaction(db = db) {
        PasswordResetTokensTable.insert {
            it[PasswordResetTokensTable.userId] = userId
            it[PasswordResetTokensTable.tokenHash] = tokenHash
            it[PasswordResetTokensTable.expiresAt] = expiresAt
        }[PasswordResetTokensTable.id]
    }

    /**
     * Find a valid (unused, non-expired) password reset token by its hash.
     */
    suspend fun findValidToken(tokenHash: String): PasswordResetTokenRecord? =
        suspendTransaction(db = db) {
            PasswordResetTokensTable
                .select(PasswordResetTokensTable.columns)
                .where {
                    (PasswordResetTokensTable.tokenHash eq tokenHash) and
                        (PasswordResetTokensTable.used eq false) and
                        (PasswordResetTokensTable.expiresAt greater CurrentDateTime)
                }
                .singleOrNull()
                ?.toPasswordResetTokenRecord()
        }

    /**
     * Mark a password reset token as used.
     */
    suspend fun markUsed(id: Uuid): Unit = suspendTransaction(db = db) {
        PasswordResetTokensTable.update({ PasswordResetTokensTable.id eq id }) {
            it[used] = true
        }
    }
}

private fun ResultRow.toPasswordResetTokenRecord(): PasswordResetTokenRecord =
    PasswordResetTokenRecord(
        id = this[PasswordResetTokensTable.id],
        userId = this[PasswordResetTokensTable.userId],
        tokenHash = this[PasswordResetTokensTable.tokenHash],
        expiresAt = this[PasswordResetTokensTable.expiresAt],
        used = this[PasswordResetTokensTable.used],
    )
