@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.repository

import com.m2f.server.privacy.contract.repository.ConsentRecord
import com.m2f.server.privacy.contract.repository.ConsentRepository
import com.m2f.server.privacy.contract.tables.ConsentRecordsTable
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ExposedConsentRepository(private val db: R2dbcDatabase) : ConsentRepository {

    override suspend fun insert(
        userId: Uuid,
        consentType: String,
        granted: Boolean,
        legalDocumentVersion: String,
        ipAddress: String?,
        userAgent: String?,
    ): Uuid = suspendTransaction(db = db) {
        ConsentRecordsTable.insert {
            it[ConsentRecordsTable.userId] = userId
            it[ConsentRecordsTable.consentType] = consentType
            it[ConsentRecordsTable.granted] = granted
            it[ConsentRecordsTable.legalDocumentVersion] = legalDocumentVersion
            it[ConsentRecordsTable.ipAddress] = ipAddress
            it[ConsentRecordsTable.userAgent] = userAgent
        }[ConsentRecordsTable.id]
    }

    override suspend fun findLatestByUserAndType(userId: Uuid, consentType: String): ConsentRecord? =
        suspendTransaction(db = db) {
            ConsentRecordsTable
                .select(ConsentRecordsTable.columns)
                .where {
                    (ConsentRecordsTable.userId eq userId) and
                        (ConsentRecordsTable.consentType eq consentType)
                }
                .orderBy(ConsentRecordsTable.createdAt, SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.toConsentRecord()
        }

    override suspend fun findAllActiveByUser(userId: Uuid): List<ConsentRecord> =
        suspendTransaction(db = db) {
            ConsentRecordsTable
                .select(ConsentRecordsTable.columns)
                .where {
                    (ConsentRecordsTable.userId eq userId) and
                        (ConsentRecordsTable.granted eq true)
                }
                .orderBy(ConsentRecordsTable.createdAt, SortOrder.DESC)
                .toList()
                .map { it.toConsentRecord() }
                .distinctBy { it.consentType }
        }

    override suspend fun findAllByUser(userId: Uuid): List<ConsentRecord> =
        suspendTransaction(db = db) {
            ConsentRecordsTable
                .select(ConsentRecordsTable.columns)
                .where { ConsentRecordsTable.userId eq userId }
                .orderBy(ConsentRecordsTable.createdAt, SortOrder.DESC)
                .toList()
                .map { it.toConsentRecord() }
        }

    override suspend fun anonymizeByUser(userId: Uuid, anonymizedId: Uuid): Unit =
        suspendTransaction(db = db) {
            ConsentRecordsTable.update({ ConsentRecordsTable.userId eq userId }) { stmt ->
                stmt[ConsentRecordsTable.userId] = anonymizedId
                stmt[ConsentRecordsTable.ipAddress] = null
                stmt[ConsentRecordsTable.userAgent] = null
            }
        }
}

private fun ResultRow.toConsentRecord(): ConsentRecord = ConsentRecord(
    id = this[ConsentRecordsTable.id],
    userId = this[ConsentRecordsTable.userId],
    consentType = this[ConsentRecordsTable.consentType],
    granted = this[ConsentRecordsTable.granted],
    legalDocumentVersion = this[ConsentRecordsTable.legalDocumentVersion],
    ipAddress = this[ConsentRecordsTable.ipAddress],
    userAgent = this[ConsentRecordsTable.userAgent],
    createdAt = this[ConsentRecordsTable.createdAt],
)
