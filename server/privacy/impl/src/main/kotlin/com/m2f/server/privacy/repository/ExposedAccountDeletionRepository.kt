@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.m2f.server.privacy.repository

import com.m2f.server.privacy.contract.repository.AccountDeletionRecord
import com.m2f.server.privacy.contract.repository.AccountDeletionRepository
import com.m2f.server.privacy.contract.tables.AccountDeletionRequestsTable
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ExposedAccountDeletionRepository(private val db: R2dbcDatabase) : AccountDeletionRepository {

    override suspend fun insert(userId: Uuid, reason: String?, scheduledAt: LocalDateTime): Uuid =
        suspendTransaction(db = db) {
            AccountDeletionRequestsTable.insert {
                it[AccountDeletionRequestsTable.userId] = userId
                it[AccountDeletionRequestsTable.reason] = reason
                it[AccountDeletionRequestsTable.scheduledAt] = scheduledAt
                it[AccountDeletionRequestsTable.status] = "PENDING"
            }[AccountDeletionRequestsTable.id]
        }

    override suspend fun findPendingByUser(userId: Uuid): AccountDeletionRecord? =
        suspendTransaction(db = db) {
            AccountDeletionRequestsTable
                .select(AccountDeletionRequestsTable.columns)
                .where {
                    (AccountDeletionRequestsTable.userId eq userId) and
                        (AccountDeletionRequestsTable.status eq "PENDING")
                }
                .singleOrNull()
                ?.toAccountDeletionRecord()
        }

    override suspend fun findById(id: Uuid): AccountDeletionRecord? =
        suspendTransaction(db = db) {
            AccountDeletionRequestsTable
                .select(AccountDeletionRequestsTable.columns)
                .where { AccountDeletionRequestsTable.id eq id }
                .singleOrNull()
                ?.toAccountDeletionRecord()
        }

    override suspend fun updateStatus(id: Uuid, status: String, completedAt: LocalDateTime?): Boolean =
        suspendTransaction(db = db) {
            val rowsUpdated = AccountDeletionRequestsTable.update({ AccountDeletionRequestsTable.id eq id }) { stmt ->
                stmt[AccountDeletionRequestsTable.status] = status
                completedAt?.let { stmt[AccountDeletionRequestsTable.completedAt] = it }
            }
            rowsUpdated > 0
        }

    override suspend fun findDueForExecution(): List<AccountDeletionRecord> =
        suspendTransaction(db = db) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            AccountDeletionRequestsTable
                .select(AccountDeletionRequestsTable.columns)
                .where {
                    (AccountDeletionRequestsTable.status eq "PENDING") and
                        (AccountDeletionRequestsTable.scheduledAt lessEq now)
                }
                .toList()
                .map { it.toAccountDeletionRecord() }
        }

    override suspend fun cancelByUser(userId: Uuid): Boolean =
        suspendTransaction(db = db) {
            val rowsUpdated = AccountDeletionRequestsTable.update({
                (AccountDeletionRequestsTable.userId eq userId) and
                    (AccountDeletionRequestsTable.status eq "PENDING")
            }) { stmt ->
                stmt[AccountDeletionRequestsTable.status] = "CANCELLED"
            }
            rowsUpdated > 0
        }
}

private fun ResultRow.toAccountDeletionRecord(): AccountDeletionRecord = AccountDeletionRecord(
    id = this[AccountDeletionRequestsTable.id],
    userId = this[AccountDeletionRequestsTable.userId],
    status = this[AccountDeletionRequestsTable.status],
    reason = this[AccountDeletionRequestsTable.reason],
    scheduledAt = this[AccountDeletionRequestsTable.scheduledAt],
    completedAt = this[AccountDeletionRequestsTable.completedAt],
    createdAt = this[AccountDeletionRequestsTable.createdAt],
)
