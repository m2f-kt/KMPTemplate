@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.repository

import com.m2f.server.privacy.contract.repository.LegalDocumentRecord
import com.m2f.server.privacy.contract.repository.LegalDocumentRepository
import com.m2f.server.privacy.contract.tables.LegalDocumentsTable
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ExposedLegalDocumentRepository(private val db: R2dbcDatabase) : LegalDocumentRepository {

    override suspend fun findCurrentByTypeAndLocale(type: String, locale: String): LegalDocumentRecord? =
        suspendTransaction(db = db) {
            LegalDocumentsTable
                .select(LegalDocumentsTable.columns)
                .where {
                    (LegalDocumentsTable.type eq type) and
                        (LegalDocumentsTable.locale eq locale)
                }
                .orderBy(LegalDocumentsTable.publishedAt, SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.toLegalDocumentRecord()
        }

    override suspend fun findAllVersionsByType(type: String): List<LegalDocumentRecord> =
        suspendTransaction(db = db) {
            LegalDocumentsTable
                .select(LegalDocumentsTable.columns)
                .where { LegalDocumentsTable.type eq type }
                .orderBy(LegalDocumentsTable.publishedAt, SortOrder.DESC)
                .toList()
                .map { it.toLegalDocumentRecord() }
        }

    override suspend fun insert(type: String, version: String, locale: String, content: String): Uuid =
        suspendTransaction(db = db) {
            LegalDocumentsTable.insert {
                it[LegalDocumentsTable.type] = type
                it[LegalDocumentsTable.version] = version
                it[LegalDocumentsTable.locale] = locale
                it[LegalDocumentsTable.content] = content
            }[LegalDocumentsTable.id]
        }
}

private fun ResultRow.toLegalDocumentRecord(): LegalDocumentRecord = LegalDocumentRecord(
    id = this[LegalDocumentsTable.id],
    type = this[LegalDocumentsTable.type],
    version = this[LegalDocumentsTable.version],
    locale = this[LegalDocumentsTable.locale],
    content = this[LegalDocumentsTable.content],
    publishedAt = this[LegalDocumentsTable.publishedAt],
)
