package com.m2f.server.privacy.contract.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.template.models.dto.privacy.DeletionRequest
import com.m2f.template.models.dto.privacy.DeletionResponse

interface AccountDeletionService {
    context(raise: Raise<DomainError>)
    suspend fun requestDeletion(userId: String, request: DeletionRequest): DeletionResponse

    context(raise: Raise<DomainError>)
    suspend fun getDeletionStatus(userId: String): DeletionResponse?

    context(raise: Raise<DomainError>)
    suspend fun cancelDeletion(userId: String)
}
