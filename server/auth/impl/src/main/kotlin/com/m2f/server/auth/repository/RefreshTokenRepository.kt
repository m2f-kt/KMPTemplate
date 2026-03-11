@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.auth.repository

import com.m2f.server.auth.contract.tables.RefreshTokensTable
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
 * Represents a refresh token record from the database.
 */
data class RefreshTokenRecord(
    val id: Uuid,
    val userId: Uuid,
    val tokenHash: String,
    val expiresAt: LocalDateTime,
    val revoked: Boolean,
)

/**
 * Database access for refresh token operations.
 */
class RefreshTokenRepository(private val db: R2dbcDatabase) {

    /**
     * Store a new refresh token and return the generated UUID.
     */
    suspend fun store(
        userId: Uuid,
        tokenHash: String,
        expiresAt: LocalDateTime,
    ): Uuid = suspendTransaction(db = db) {
        RefreshTokensTable.insert {
            it[RefreshTokensTable.userId] = userId
            it[RefreshTokensTable.tokenHash] = tokenHash
            it[RefreshTokensTable.expiresAt] = expiresAt
        }[RefreshTokensTable.id]
    }

    /**
     * Find a valid (non-revoked, non-expired) refresh token by its hash.
     */
    suspend fun findValidToken(tokenHash: String): RefreshTokenRecord? =
        suspendTransaction(db = db) {
            RefreshTokensTable
                .select(RefreshTokensTable.columns)
                .where {
                    (RefreshTokensTable.tokenHash eq tokenHash) and
                        (RefreshTokensTable.revoked eq false) and
                        (RefreshTokensTable.expiresAt greater CurrentDateTime)
                }
                .singleOrNull()
                ?.toRefreshTokenRecord()
        }

    /**
     * Revoke all refresh tokens for a given user.
     */
    suspend fun revokeByUserId(userId: Uuid): Unit = suspendTransaction(db = db) {
        RefreshTokensTable.update({ RefreshTokensTable.userId eq userId }) {
            it[revoked] = true
        }
    }

    /**
     * Revoke a single refresh token by its ID.
     */
    suspend fun revokeById(id: Uuid): Unit = suspendTransaction(db = db) {
        RefreshTokensTable.update({ RefreshTokensTable.id eq id }) {
            it[revoked] = true
        }
    }
}

private fun ResultRow.toRefreshTokenRecord(): RefreshTokenRecord = RefreshTokenRecord(
    id = this[RefreshTokensTable.id],
    userId = this[RefreshTokensTable.userId],
    tokenHash = this[RefreshTokensTable.tokenHash],
    expiresAt = this[RefreshTokensTable.expiresAt],
    revoked = this[RefreshTokensTable.revoked],
)
