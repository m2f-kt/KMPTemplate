@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.m2f.server.privacy.service

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import com.m2f.core.config.server.DomainError
import com.m2f.server.auth.contract.errors.InvalidCredentials
import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.server.auth.contract.security.PasswordHasher
import com.m2f.server.privacy.contract.errors.DeletionAlreadyPending
import com.m2f.server.privacy.contract.repository.AccountDeletionRecord
import com.m2f.server.privacy.contract.repository.AccountDeletionRepository
import com.m2f.server.privacy.contract.repository.ConsentRepository
import com.m2f.server.privacy.contract.service.AccountDeletionService
import com.m2f.template.models.dto.privacy.DeletionRequest
import com.m2f.template.models.dto.privacy.DeletionResponse
import com.m2f.template.models.dto.privacy.DeletionStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AccountDeletionServiceImpl(
    private val accountDeletionRepository: AccountDeletionRepository,
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val consentRepository: ConsentRepository,
) : AccountDeletionService {

    context(raise: Raise<DomainError>)
    override suspend fun requestDeletion(userId: String, request: DeletionRequest): DeletionResponse {
        val uuid = Uuid.parse(userId)

        val existingDeletion = accountDeletionRepository.findPendingByUser(uuid)
        ensure(existingDeletion == null) { DeletionAlreadyPending() }

        val user = userRepository.findById(uuid)
        ensureNotNull(user) {
            InvalidCredentials()
        }
        ensure(passwordHasher.verify(request.password, user.passwordHash)) {
            InvalidCredentials()
        }

        val scheduledAt = Clock.System.now().plus(7.days).toLocalDateTime(TimeZone.UTC)
        val deletionId = accountDeletionRepository.insert(uuid, request.reason, scheduledAt)

        val record = accountDeletionRepository.findById(deletionId)
        ensureNotNull(record) {
            com.m2f.core.config.server.UnexpectedError("Failed to create deletion request")
        }
        return record.toResponse()
    }

    context(raise: Raise<DomainError>)
    override suspend fun getDeletionStatus(userId: String): DeletionResponse? {
        val uuid = Uuid.parse(userId)
        val record = accountDeletionRepository.findPendingByUser(uuid)
        return record?.toResponse()
    }

    context(raise: Raise<DomainError>)
    override suspend fun cancelDeletion(userId: String) {
        val uuid = Uuid.parse(userId)
        val cancelled = accountDeletionRepository.cancelByUser(uuid)
        ensure(cancelled) {
            com.m2f.core.config.server.UnexpectedError("No pending deletion request found to cancel")
        }
    }

    private fun AccountDeletionRecord.toResponse(): DeletionResponse = DeletionResponse(
        id = id.toString(),
        status = DeletionStatus.valueOf(status),
        scheduledAt = scheduledAt.toInstant(TimeZone.UTC).toString(),
        completedAt = completedAt?.toInstant(TimeZone.UTC)?.toString(),
    )
}
