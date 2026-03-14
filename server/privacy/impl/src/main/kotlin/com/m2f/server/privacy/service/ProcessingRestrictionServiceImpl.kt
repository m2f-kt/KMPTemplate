@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.server.privacy.contract.service.ProcessingRestrictionService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ProcessingRestrictionServiceImpl(
    private val userRepository: UserRepository,
) : ProcessingRestrictionService {

    context(raise: Raise<DomainError>)
    override suspend fun restrictProcessing(userId: String) {
        val uuid = Uuid.parse(userId)
        userRepository.updateProcessingRestricted(uuid, true)
    }

    context(raise: Raise<DomainError>)
    override suspend fun liftRestriction(userId: String) {
        val uuid = Uuid.parse(userId)
        userRepository.updateProcessingRestricted(uuid, false)
    }
}
