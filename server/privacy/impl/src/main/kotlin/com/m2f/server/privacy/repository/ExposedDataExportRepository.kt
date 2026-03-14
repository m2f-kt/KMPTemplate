@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.m2f.server.privacy.repository

import com.m2f.server.privacy.contract.repository.DataExportRecord
import com.m2f.server.privacy.contract.repository.DataExportRepository
import com.m2f.server.privacy.contract.tables.DataExportRequestsTable
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ExposedDataExportRepository(private val db: R2dbcDatabase) : DataExportRepository {

    override suspend fun insert(userId: Uuid, status: String): Uuid =
        suspendTransaction(db = db) {
            DataExportRequestsTable.insert {
                it[DataExportRequestsTable.userId] = userId
                it[DataExportRequestsTable.status] = status
            }[DataExportRequestsTable.id]
        }

    override suspend fun findById(id: Uuid): DataExportRecord? =
        suspendTransaction(db = db) {
            DataExportRequestsTable
                .select(DataExportRequestsTable.columns)
                .where { DataExportRequestsTable.id eq id }
                .singleOrNull()
                ?.toDataExportRecord()
        }

    override suspend fun findActiveByUser(userId: Uuid): DataExportRecord? =
        suspendTransaction(db = db) {
            DataExportRequestsTable
                .select(DataExportRequestsTable.columns)
                .where {
                    (DataExportRequestsTable.userId eq userId) and
                        ((DataExportRequestsTable.status eq "PENDING") or
                            (DataExportRequestsTable.status eq "PROCESSING"))
                }
                .singleOrNull()
                ?.toDataExportRecord()
        }

    override suspend fun updateStatus(
        id: Uuid,
        status: String,
        fileKey: String?,
        completedAt: LocalDateTime?,
        expiresAt: LocalDateTime?,
    ): Boolean = suspendTransaction(db = db) {
        val rowsUpdated = DataExportRequestsTable.update({ DataExportRequestsTable.id eq id }) { stmt ->
            stmt[DataExportRequestsTable.status] = status
            fileKey?.let { stmt[DataExportRequestsTable.fileKey] = it }
            completedAt?.let { stmt[DataExportRequestsTable.completedAt] = it }
            expiresAt?.let { stmt[DataExportRequestsTable.expiresAt] = it }
        }
        rowsUpdated > 0
    }

    override suspend fun findExpired(): List<DataExportRecord> =
        suspendTransaction(db = db) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            DataExportRequestsTable
                .select(DataExportRequestsTable.columns)
                .where {
                    (DataExportRequestsTable.status eq "COMPLETED") and
                        (DataExportRequestsTable.expiresAt lessEq now)
                }
                .toList()
                .map { it.toDataExportRecord() }
        }

    override suspend fun deleteByUser(userId: Uuid): Unit =
        suspendTransaction(db = db) {
            DataExportRequestsTable.deleteWhere { DataExportRequestsTable.userId eq userId }
        }
}

private fun ResultRow.toDataExportRecord(): DataExportRecord = DataExportRecord(
    id = this[DataExportRequestsTable.id],
    userId = this[DataExportRequestsTable.userId],
    status = this[DataExportRequestsTable.status],
    fileKey = this[DataExportRequestsTable.fileKey],
    completedAt = this[DataExportRequestsTable.completedAt],
    expiresAt = this[DataExportRequestsTable.expiresAt],
    createdAt = this[DataExportRequestsTable.createdAt],
)
