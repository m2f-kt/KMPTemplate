@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.repository

import kotlinx.datetime.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class DataExportRecord(
    val id: Uuid,
    val userId: Uuid,
    val status: String,
    val fileKey: String?,
    val completedAt: LocalDateTime?,
    val expiresAt: LocalDateTime?,
    val createdAt: LocalDateTime,
)

interface DataExportRepository {
    suspend fun insert(userId: Uuid, status: String): Uuid
    suspend fun findById(id: Uuid): DataExportRecord?
    suspend fun findActiveByUser(userId: Uuid): DataExportRecord?
    suspend fun updateStatus(id: Uuid, status: String, fileKey: String? = null, completedAt: LocalDateTime? = null, expiresAt: LocalDateTime? = null): Boolean
    suspend fun findExpired(): List<DataExportRecord>
    suspend fun deleteByUser(userId: Uuid)
}
