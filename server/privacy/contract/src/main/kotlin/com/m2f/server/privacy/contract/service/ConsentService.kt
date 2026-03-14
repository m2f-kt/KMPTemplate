package com.m2f.server.privacy.contract.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.template.models.dto.privacy.ConsentStatus
import com.m2f.template.models.dto.privacy.GrantConsentRequest
import com.m2f.template.models.dto.privacy.RequiredConsentsResponse

interface ConsentService {
    context(raise: Raise<DomainError>)
    suspend fun getActiveConsents(userId: String): List<ConsentStatus>

    context(raise: Raise<DomainError>)
    suspend fun grantConsent(userId: String, request: GrantConsentRequest, ipAddress: String?, userAgent: String?)

    context(raise: Raise<DomainError>)
    suspend fun withdrawConsent(userId: String, consentType: String, ipAddress: String?, userAgent: String?)

    context(raise: Raise<DomainError>)
    suspend fun getRequiredConsents(userId: String): RequiredConsentsResponse
}
