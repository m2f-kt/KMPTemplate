package com.m2f.server.privacy.contract.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.template.models.dto.privacy.DataExportResponse

interface DataExportService {
    context(raise: Raise<DomainError>)
    suspend fun requestExport(userId: String): DataExportResponse

    context(raise: Raise<DomainError>)
    suspend fun getExportStatus(userId: String, exportId: String): DataExportResponse

    context(raise: Raise<DomainError>)
    suspend fun getExportDownloadUrl(userId: String, exportId: String): String

    context(raise: Raise<DomainError>)
    suspend fun getActiveExport(userId: String): DataExportResponse?
}
