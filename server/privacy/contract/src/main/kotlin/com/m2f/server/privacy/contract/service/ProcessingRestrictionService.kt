package com.m2f.server.privacy.contract.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError

interface ProcessingRestrictionService {
    context(raise: Raise<DomainError>)
    suspend fun restrictProcessing(userId: String)

    context(raise: Raise<DomainError>)
    suspend fun liftRestriction(userId: String)
}
