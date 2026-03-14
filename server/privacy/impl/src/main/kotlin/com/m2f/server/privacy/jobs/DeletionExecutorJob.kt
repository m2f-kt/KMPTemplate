@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.m2f.server.privacy.jobs

import com.m2f.server.privacy.contract.repository.AccountDeletionRepository
import com.m2f.server.privacy.contract.repository.ConsentRepository
import com.m2f.server.privacy.contract.repository.DataExportRepository
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Scheduled job that executes pending account deletion requests that are due.
 * Cascades deletion: removes export data, anonymizes consents, then marks the request complete.
 * Real production workflows would include additional cascading steps (e.g. deleting the user row).
 */
class DeletionExecutorJob(
    private val accountDeletionRepository: AccountDeletionRepository,
    private val dataExportRepository: DataExportRepository,
    private val consentRepository: ConsentRepository,
) : PrivacyJob {

    override val name: String = "deletion-executor"

    override suspend fun execute() {
        val dueRequests = accountDeletionRepository.findDueForExecution()

        for (request in dueRequests) {
            accountDeletionRepository.updateStatus(request.id, "PROCESSING")

            try {
                val anonymizedId = Uuid.random()
                dataExportRepository.deleteByUser(request.userId)
                consentRepository.anonymizeByUser(request.userId, anonymizedId)

                val completedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                accountDeletionRepository.updateStatus(request.id, "COMPLETED", completedAt)
            } catch (_: Exception) {
                // Leave in PROCESSING so it can be retried or investigated
            }
        }
    }
}
