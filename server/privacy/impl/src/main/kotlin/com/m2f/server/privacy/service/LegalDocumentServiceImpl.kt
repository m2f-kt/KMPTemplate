package com.m2f.server.privacy.service

import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.UnexpectedError
import com.m2f.server.privacy.contract.repository.LegalDocumentRecord
import com.m2f.server.privacy.contract.repository.LegalDocumentRepository
import com.m2f.server.privacy.contract.service.LegalDocumentService
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.dto.privacy.LegalDocumentResponse
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class LegalDocumentServiceImpl(
    private val legalDocumentRepository: LegalDocumentRepository,
) : LegalDocumentService {

    context(raise: Raise<DomainError>)
    override suspend fun getCurrentDocument(type: String, locale: String?): LegalDocumentResponse {
        val requestedLocale = locale ?: "en"
        val document = legalDocumentRepository.findCurrentByTypeAndLocale(type, requestedLocale)
            ?: legalDocumentRepository.findCurrentByTypeAndLocale(type, "en")

        ensureNotNull(document) {
            UnexpectedError("Legal document not found for type $type")
        }

        return document.toResponse()
    }

    context(raise: Raise<DomainError>)
    override suspend fun getAllVersions(type: String): List<LegalDocumentResponse> {
        val documents = legalDocumentRepository.findAllVersionsByType(type)
        return documents.map { it.toResponse() }
    }

    private fun LegalDocumentRecord.toResponse(): LegalDocumentResponse = LegalDocumentResponse(
        type = ConsentType.valueOf(type),
        version = version,
        locale = this.locale,
        content = content,
        publishedAt = publishedAt.toInstant(TimeZone.UTC).toString(),
    )
}
