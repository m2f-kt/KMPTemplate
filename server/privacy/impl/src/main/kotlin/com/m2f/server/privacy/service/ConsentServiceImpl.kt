@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.service

import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import com.m2f.core.config.server.DomainError
import com.m2f.server.privacy.contract.repository.ConsentRepository
import com.m2f.server.privacy.contract.repository.LegalDocumentRepository
import com.m2f.server.privacy.contract.service.ConsentService
import com.m2f.template.models.dto.privacy.ConsentStatus
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.dto.privacy.GrantConsentRequest
import com.m2f.template.models.dto.privacy.RequiredConsent
import com.m2f.template.models.dto.privacy.RequiredConsentsResponse
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ConsentServiceImpl(
    private val consentRepository: ConsentRepository,
    private val legalDocumentRepository: LegalDocumentRepository,
) : ConsentService {

    context(raise: Raise<DomainError>)
    override suspend fun getActiveConsents(userId: String): List<ConsentStatus> {
        val uuid = Uuid.parse(userId)
        val records = consentRepository.findAllActiveByUser(uuid)
        return records.map { record ->
            ConsentStatus(
                type = ConsentType.valueOf(record.consentType),
                granted = record.granted,
                grantedAt = record.createdAt.toInstant(TimeZone.UTC).toString(),
                documentVersion = record.legalDocumentVersion,
            )
        }
    }

    context(raise: Raise<DomainError>)
    override suspend fun grantConsent(userId: String, request: GrantConsentRequest, ipAddress: String?, userAgent: String?) {
        val uuid = Uuid.parse(userId)
        val document = legalDocumentRepository.findCurrentByTypeAndLocale(request.type.name, "en")
        ensureNotNull(document) {
            com.m2f.server.privacy.contract.errors.ConsentRequired(detail = "Legal document not found for type ${request.type}")
        }
        consentRepository.insert(
            userId = uuid,
            consentType = request.type.name,
            granted = true,
            legalDocumentVersion = request.documentVersion,
            ipAddress = ipAddress,
            userAgent = userAgent,
        )
    }

    context(raise: Raise<DomainError>)
    override suspend fun withdrawConsent(userId: String, consentType: String) {
        val uuid = Uuid.parse(userId)
        val latestConsent = consentRepository.findLatestByUserAndType(uuid, consentType)
        consentRepository.insert(
            userId = uuid,
            consentType = consentType,
            granted = false,
            legalDocumentVersion = latestConsent?.legalDocumentVersion ?: "",
            ipAddress = null,
            userAgent = null,
        )
    }

    context(raise: Raise<DomainError>)
    override suspend fun getRequiredConsents(userId: String): RequiredConsentsResponse {
        val uuid = Uuid.parse(userId)
        val requiredTypes = listOf(ConsentType.PRIVACY_POLICY, ConsentType.TERMS_OF_SERVICE)

        val consents = requiredTypes.map { type ->
            val currentDocument = legalDocumentRepository.findCurrentByTypeAndLocale(type.name, "en")
            val userConsent = consentRepository.findLatestByUserAndType(uuid, type.name)

            val currentVersion = currentDocument?.version
            val acceptedVersion = if (userConsent?.granted == true) userConsent.legalDocumentVersion else null

            RequiredConsent(
                type = type,
                currentVersion = currentVersion ?: "1.0",
                acceptedVersion = acceptedVersion,
                needsUpdate = acceptedVersion == null || acceptedVersion != currentVersion,
            )
        }

        return RequiredConsentsResponse(
            consents = consents,
            hasOutdated = consents.any { it.needsUpdate },
        )
    }
}
