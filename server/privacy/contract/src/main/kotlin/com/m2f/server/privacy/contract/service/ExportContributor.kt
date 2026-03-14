@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.service

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ExportSection(
    val name: String,
    val jsonData: String,
    val files: List<ExportFile> = emptyList(),
)

data class ExportFile(
    val name: String,
    val data: ByteArray,
)

interface ExportContributor {
    val sectionName: String
    suspend fun export(userId: Uuid): ExportSection
}
