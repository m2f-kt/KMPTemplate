@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.repository

import kotlinx.datetime.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class LegalDocumentRecord(
    val id: Uuid,
    val type: String,
    val version: String,
    val locale: String,
    val content: String,
    val publishedAt: LocalDateTime,
)

interface LegalDocumentRepository {
    suspend fun findCurrentByTypeAndLocale(type: String, locale: String): LegalDocumentRecord?
    suspend fun findAllVersionsByType(type: String): List<LegalDocumentRecord>
    suspend fun insert(type: String, version: String, locale: String, content: String): Uuid
}
