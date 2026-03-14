@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.repository

import kotlinx.datetime.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ConsentRecord(
    val id: Uuid,
    val userId: Uuid,
    val consentType: String,
    val granted: Boolean,
    val legalDocumentVersion: String,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: LocalDateTime,
)

interface ConsentRepository {
    suspend fun insert(userId: Uuid, consentType: String, granted: Boolean, legalDocumentVersion: String, ipAddress: String?, userAgent: String?): Uuid
    suspend fun findLatestByUserAndType(userId: Uuid, consentType: String): ConsentRecord?
    suspend fun findAllActiveByUser(userId: Uuid): List<ConsentRecord>
    suspend fun findAllByUser(userId: Uuid): List<ConsentRecord>
    suspend fun anonymizeByUser(userId: Uuid, anonymizedId: Uuid)
}
