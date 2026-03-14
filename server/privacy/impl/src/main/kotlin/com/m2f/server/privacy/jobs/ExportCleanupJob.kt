@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.m2f.server.privacy.jobs

import com.m2f.server.privacy.contract.repository.DataExportRepository

/**
 * Scheduled job that finds expired data export records and marks them as EXPIRED.
 * Actual MinIO/object storage cleanup would be added in a future step.
 */
class ExportCleanupJob(
    private val dataExportRepository: DataExportRepository,
) : PrivacyJob {

    override val name: String = "export-cleanup"

    override suspend fun execute() {
        val expiredExports = dataExportRepository.findExpired()

        for (export in expiredExports) {
            dataExportRepository.updateStatus(export.id, "EXPIRED")
        }
    }
}
