@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.m2f.server.privacy.jobs

import com.m2f.server.privacy.contract.repository.DataExportRepository
import com.m2f.server.privacy.contract.service.ExportContributor
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

/**
 * Scheduled job that processes PENDING data export requests.
 * Collects data from all ExportContributors, serializes to JSON, and marks the export COMPLETED.
 * Runs frequently (every minute) so exports are processed quickly after being requested.
 */
class ExportProcessorJob(
    private val dataExportRepository: DataExportRepository,
    private val exportContributors: List<ExportContributor>,
) : PrivacyJob {

    override val name: String = "export-processor"
    override val interval = 1.minutes

    override suspend fun execute() {
        val pendingExports = dataExportRepository.findPending()

        for (export in pendingExports) {
            dataExportRepository.updateStatus(export.id, "PROCESSING")

            try {
                val sections = exportContributors.map { contributor ->
                    contributor.export(export.userId)
                }

                val fileKey = sections.joinToString(
                    separator = ",",
                    prefix = "{",
                    postfix = "}",
                ) { section -> "\"${section.name}\":${section.jsonData}" }

                val now = Clock.System.now()
                val completedAt = now.toLocalDateTime(TimeZone.UTC)
                val expiresAt = (now + 7.days).toLocalDateTime(TimeZone.UTC)

                dataExportRepository.updateStatus(
                    id = export.id,
                    status = "COMPLETED",
                    fileKey = fileKey,
                    completedAt = completedAt,
                    expiresAt = expiresAt,
                )
            } catch (_: Exception) {
                dataExportRepository.updateStatus(export.id, "FAILED")
            }
        }
    }
}
