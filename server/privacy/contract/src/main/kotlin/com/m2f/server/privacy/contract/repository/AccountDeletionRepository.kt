@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.repository

import kotlinx.datetime.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AccountDeletionRecord(
    val id: Uuid,
    val userId: Uuid,
    val status: String,
    val reason: String?,
    val scheduledAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
)

interface AccountDeletionRepository {
    suspend fun insert(userId: Uuid, reason: String?, scheduledAt: LocalDateTime): Uuid
    suspend fun findPendingByUser(userId: Uuid): AccountDeletionRecord?
    suspend fun findById(id: Uuid): AccountDeletionRecord?
    suspend fun updateStatus(id: Uuid, status: String, completedAt: LocalDateTime? = null): Boolean
    suspend fun findDueForExecution(): List<AccountDeletionRecord>
    suspend fun cancelByUser(userId: Uuid): Boolean
}
