package com.m2f.server.privacy.contract.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.template.models.dto.privacy.LegalDocumentResponse

interface LegalDocumentService {
    context(raise: Raise<DomainError>)
    suspend fun getCurrentDocument(type: String, locale: String?): LegalDocumentResponse

    context(raise: Raise<DomainError>)
    suspend fun getAllVersions(type: String): List<LegalDocumentResponse>
}
