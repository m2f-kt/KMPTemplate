@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups.repository

import com.m2f.server.groups.tables.InvitationsTable
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents an invitation record from the database.
 */
data class InvitationRecord(
    val id: Uuid,
    val token: String,
    val groupId: Uuid,
    val email: String,
    val invitedBy: Uuid,
    val role: String,
    val expiresAt: LocalDateTime,
    val acceptedAt: LocalDateTime?,
    val acceptedBy: Uuid?,
    val revokedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
)

/**
 * Database access for invitation CRUD operations.
 */
class InvitationRepository(private val db: R2dbcDatabase) {

    /**
     * Create a new invitation record.
     * Token is generated as a 32-char random alphanumeric string.
     */
    suspend fun create(
        groupId: Uuid,
        email: String,
        invitedBy: Uuid,
        role: String = "MEMBER",
        expiresAt: LocalDateTime,
    ): InvitationRecord = suspendTransaction(db = db) {
        val token = generateToken()

        val insertedId = InvitationsTable.insert {
            it[InvitationsTable.token] = token
            it[InvitationsTable.groupId] = groupId
            it[InvitationsTable.email] = email
            it[InvitationsTable.invitedBy] = invitedBy
            it[InvitationsTable.role] = role
            it[InvitationsTable.expiresAt] = expiresAt
        }[InvitationsTable.id]

        // Fetch the inserted record to get all defaulted values
        InvitationsTable
            .select(InvitationsTable.columns)
            .where { InvitationsTable.id eq insertedId }
            .singleOrNull()
            ?.toInvitationRecord()
            ?: error("Failed to retrieve inserted invitation")
    }

    /**
     * Find an invitation by its unique token.
     */
    suspend fun findByToken(token: String): InvitationRecord? = suspendTransaction(db = db) {
        InvitationsTable
            .select(InvitationsTable.columns)
            .where { InvitationsTable.token eq token }
            .singleOrNull()
            ?.toInvitationRecord()
    }

    /**
     * Mark an invitation as accepted by a user.
     * Returns true if a row was updated.
     */
    suspend fun markAccepted(token: String, userId: Uuid): Boolean = suspendTransaction(db = db) {
        val now = java.time.LocalDateTime.now().toKotlinLocalDateTime()
        val rowsUpdated = InvitationsTable.update({ InvitationsTable.token eq token }) {
            it[acceptedAt] = now
            it[acceptedBy] = userId
        }
        rowsUpdated > 0
    }

    /**
     * Find all invitations for a group.
     */
    suspend fun findByGroupId(groupId: Uuid): List<InvitationRecord> = suspendTransaction(db = db) {
        InvitationsTable
            .select(InvitationsTable.columns)
            .where { InvitationsTable.groupId eq groupId }
            .toList()
            .map { it.toInvitationRecord() }
    }

    /**
     * Find an invitation by its ID.
     */
    suspend fun findById(id: Uuid): InvitationRecord? = suspendTransaction(db = db) {
        InvitationsTable
            .select(InvitationsTable.columns)
            .where { InvitationsTable.id eq id }
            .singleOrNull()
            ?.toInvitationRecord()
    }

    /**
     * Mark an invitation as revoked.
     * Returns true if a row was updated.
     */
    suspend fun revokeById(id: Uuid): Boolean = suspendTransaction(db = db) {
        val now = java.time.LocalDateTime.now().toKotlinLocalDateTime()
        val rowsUpdated = InvitationsTable.update({ InvitationsTable.id eq id }) {
            it[revokedAt] = now
        }
        rowsUpdated > 0
    }

    /**
     * Mark all non-accepted invitations for a given email+group as accepted.
     * Used for cleanup when a user joins a group via any invitation.
     * Returns the number of rows updated.
     */
    suspend fun markAcceptedByGroupAndEmail(
        groupId: Uuid,
        email: String,
        acceptedBy: Uuid,
    ): Int = suspendTransaction(db = db) {
        val now = java.time.LocalDateTime.now().toKotlinLocalDateTime()
        InvitationsTable.update({
            (InvitationsTable.groupId eq groupId) and
                (InvitationsTable.email.lowerCase() eq email.lowercase()) and
                (InvitationsTable.acceptedAt.isNull())
        }) {
            it[acceptedAt] = now
            it[InvitationsTable.acceptedBy] = acceptedBy
        }
    }

    /**
     * Delete an invitation by its ID.
     * Returns true if a row was deleted.
     */
    suspend fun deleteById(id: Uuid): Boolean = suspendTransaction(db = db) {
        val rowsDeleted = InvitationsTable.deleteWhere { InvitationsTable.id eq id }
        rowsDeleted > 0
    }

    /**
     * Generate a 32-character random alphanumeric token.
     */
    private fun generateToken(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32).map { chars.random() }.joinToString("")
    }
}

private fun ResultRow.toInvitationRecord(): InvitationRecord = InvitationRecord(
    id = this[InvitationsTable.id],
    token = this[InvitationsTable.token],
    groupId = this[InvitationsTable.groupId],
    email = this[InvitationsTable.email],
    invitedBy = this[InvitationsTable.invitedBy],
    role = this[InvitationsTable.role],
    expiresAt = this[InvitationsTable.expiresAt],
    acceptedAt = this[InvitationsTable.acceptedAt],
    acceptedBy = this[InvitationsTable.acceptedBy],
    revokedAt = this[InvitationsTable.revokedAt],
    createdAt = this[InvitationsTable.createdAt],
)
