@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.service

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import com.m2f.core.config.server.DomainError
import com.m2f.server.privacy.contract.errors.ExportAlreadyActive
import com.m2f.server.privacy.contract.errors.ExportNotReady
import com.m2f.server.privacy.contract.repository.DataExportRecord
import com.m2f.server.privacy.contract.repository.DataExportRepository
import com.m2f.server.privacy.contract.service.DataExportService
import com.m2f.server.privacy.contract.service.ExportContributor
import com.m2f.template.models.dto.privacy.DataExportResponse
import com.m2f.template.models.dto.privacy.ExportStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DataExportServiceImpl(
    private val dataExportRepository: DataExportRepository,
    private val exportContributors: List<ExportContributor>,
) : DataExportService {

    context(raise: Raise<DomainError>)
    override suspend fun requestExport(userId: String): DataExportResponse {
        val uuid = Uuid.parse(userId)
        val activeExport = dataExportRepository.findActiveByUser(uuid)
        ensure(activeExport == null) { ExportAlreadyActive() }

        val exportId = dataExportRepository.insert(uuid, ExportStatus.PENDING.name)
        val record = dataExportRepository.findById(exportId)
        ensureNotNull(record) {
            com.m2f.core.config.server.UnexpectedError("Failed to create export record")
        }
        return record.toResponse()
    }

    context(raise: Raise<DomainError>)
    override suspend fun getExportStatus(userId: String, exportId: String): DataExportResponse {
        val uuid = Uuid.parse(userId)
        val exportUuid = Uuid.parse(exportId)
        val record = dataExportRepository.findById(exportUuid)
        ensureNotNull(record) {
            ExportNotReady(detail = "Export not found")
        }
        ensure(record.userId == uuid) {
            ExportNotReady(detail = "Export not found")
        }
        return record.toResponse()
    }

    context(raise: Raise<DomainError>)
    override suspend fun getExportDownloadUrl(userId: String, exportId: String): String {
        val uuid = Uuid.parse(userId)
        val exportUuid = Uuid.parse(exportId)
        val record = dataExportRepository.findById(exportUuid)
        ensureNotNull(record) {
            ExportNotReady(detail = "Export not found")
        }
        ensure(record.userId == uuid) {
            ExportNotReady(detail = "Export not found")
        }
        ensure(record.status == ExportStatus.COMPLETED.name) {
            ExportNotReady()
        }
        // Placeholder: actual presigned URL generation will come with MinIO integration
        return "/api/privacy/exports/${record.id}/download"
    }

    context(raise: Raise<DomainError>)
    override suspend fun getActiveExport(userId: String): DataExportResponse? {
        val uuid = Uuid.parse(userId)
        return dataExportRepository.findActiveByUser(uuid)?.toResponse()
    }

    private fun DataExportRecord.toResponse(): DataExportResponse = DataExportResponse(
        id = id.toString(),
        status = ExportStatus.valueOf(status),
        downloadUrl = if (status == ExportStatus.COMPLETED.name) "/api/privacy/exports/$id/download" else null,
        createdAt = createdAt.toInstant(TimeZone.UTC).toString(),
        expiresAt = expiresAt?.toInstant(TimeZone.UTC)?.toString(),
    )
}
