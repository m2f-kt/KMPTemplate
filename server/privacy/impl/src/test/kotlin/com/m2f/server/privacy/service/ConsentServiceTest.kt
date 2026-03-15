@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.service

import arrow.core.raise.either
import com.m2f.server.privacy.contract.repository.ConsentRecord
import com.m2f.server.privacy.contract.repository.ConsentRepository
import com.m2f.server.privacy.contract.repository.LegalDocumentRecord
import com.m2f.server.privacy.contract.repository.LegalDocumentRepository
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.dto.privacy.GrantConsentRequest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ConsentServiceTest {

    private val userId = Uuid.random()
    private val userIdStr = userId.toString()

    private val now = LocalDateTime(2026, 3, 15, 10, 0, 0)

    private val privacyPolicyDocument = LegalDocumentRecord(
        id = Uuid.random(),
        type = ConsentType.PRIVACY_POLICY.name,
        version = "1.0",
        locale = "en",
        content = "Privacy policy content",
        publishedAt = now,
    )

    private val termsDocument = LegalDocumentRecord(
        id = Uuid.random(),
        type = ConsentType.TERMS_OF_SERVICE.name,
        version = "1.0",
        locale = "en",
        content = "Terms of service content",
        publishedAt = now,
    )

    @Test
    fun `getActiveConsents returns all consent types with defaults for missing records`() = runTest {
        val consentRepo = FakeConsentRepository()
        val legalDocRepo = FakeLegalDocumentRepository()
        val service = ConsentServiceImpl(consentRepo, legalDocRepo)

        val result = either {
            service.getActiveConsents(userIdStr)
        }

        val consents = result.getOrNull().shouldNotBeNull()
        consents shouldHaveSize ConsentType.entries.size
        consents.forEach { status ->
            status.granted shouldBe false
            status.grantedAt.shouldBeNull()
            status.documentVersion.shouldBeNull()
        }
        consents.map { it.type } shouldBe ConsentType.entries
    }

    @Test
    fun `grantConsent creates a record and can be retrieved`() = runTest {
        val consentRepo = FakeConsentRepository()
        val legalDocRepo = FakeLegalDocumentRepository(
            documents = mutableListOf(privacyPolicyDocument),
        )
        val service = ConsentServiceImpl(consentRepo, legalDocRepo)

        val grantResult = either {
            service.grantConsent(
                userId = userIdStr,
                request = GrantConsentRequest(
                    type = ConsentType.PRIVACY_POLICY,
                    documentVersion = "1.0",
                ),
                ipAddress = "127.0.0.1",
                userAgent = "TestAgent",
            )
        }
        grantResult.isRight().shouldBeTrue()

        val getResult = either {
            service.getActiveConsents(userIdStr)
        }
        val consents = getResult.getOrNull().shouldNotBeNull()
        val privacyConsent = consents.first { it.type == ConsentType.PRIVACY_POLICY }
        privacyConsent.granted shouldBe true
        privacyConsent.documentVersion shouldBe "1.0"
        privacyConsent.grantedAt.shouldNotBeNull()
    }

    @Test
    fun `withdrawConsent creates a withdrawal record`() = runTest {
        val consentRepo = FakeConsentRepository()
        val legalDocRepo = FakeLegalDocumentRepository(
            documents = mutableListOf(privacyPolicyDocument),
        )
        val service = ConsentServiceImpl(consentRepo, legalDocRepo)

        // First grant consent
        either {
            service.grantConsent(
                userId = userIdStr,
                request = GrantConsentRequest(
                    type = ConsentType.PRIVACY_POLICY,
                    documentVersion = "1.0",
                ),
                ipAddress = null,
                userAgent = null,
            )
        }

        // Then withdraw
        val withdrawResult = either {
            service.withdrawConsent(
                userId = userIdStr,
                consentType = ConsentType.PRIVACY_POLICY.name,
                ipAddress = null,
                userAgent = null,
            )
        }
        withdrawResult.isRight().shouldBeTrue()

        // The latest record should be a withdrawal (granted=false)
        val latest = consentRepo.findLatestByUserAndType(userId, ConsentType.PRIVACY_POLICY.name)
        latest.shouldNotBeNull()
        latest.granted shouldBe false
    }

    @Test
    fun `getRequiredConsents returns outdated when no consents exist`() = runTest {
        val consentRepo = FakeConsentRepository()
        val legalDocRepo = FakeLegalDocumentRepository(
            documents = mutableListOf(privacyPolicyDocument, termsDocument),
        )
        val service = ConsentServiceImpl(consentRepo, legalDocRepo)

        val result = either {
            service.getRequiredConsents(userIdStr)
        }

        val response = result.getOrNull().shouldNotBeNull()
        response.hasOutdated.shouldBeTrue()
        response.consents shouldHaveSize 2
        response.consents.forEach { consent ->
            consent.needsUpdate.shouldBeTrue()
            consent.acceptedVersion.shouldBeNull()
            consent.currentVersion shouldBe "1.0"
        }
    }

    @Test
    fun `getRequiredConsents returns up-to-date when all consents match current version`() = runTest {
        val consentRepo = FakeConsentRepository()
        val legalDocRepo = FakeLegalDocumentRepository(
            documents = mutableListOf(privacyPolicyDocument, termsDocument),
        )
        val service = ConsentServiceImpl(consentRepo, legalDocRepo)

        // Grant both required consents with current version
        either {
            service.grantConsent(
                userId = userIdStr,
                request = GrantConsentRequest(type = ConsentType.PRIVACY_POLICY, documentVersion = "1.0"),
                ipAddress = null,
                userAgent = null,
            )
        }
        either {
            service.grantConsent(
                userId = userIdStr,
                request = GrantConsentRequest(type = ConsentType.TERMS_OF_SERVICE, documentVersion = "1.0"),
                ipAddress = null,
                userAgent = null,
            )
        }

        val result = either {
            service.getRequiredConsents(userIdStr)
        }

        val response = result.getOrNull().shouldNotBeNull()
        response.hasOutdated.shouldBeFalse()
        response.consents.forEach { consent ->
            consent.needsUpdate.shouldBeFalse()
            consent.acceptedVersion shouldBe "1.0"
        }
    }

    @Test
    fun `getActiveConsents deduplicates by type returning latest`() = runTest {
        val consentRepo = FakeConsentRepository()
        val legalDocRepo = FakeLegalDocumentRepository(
            documents = mutableListOf(privacyPolicyDocument),
        )
        val service = ConsentServiceImpl(consentRepo, legalDocRepo)

        // Grant consent twice with different versions
        either {
            service.grantConsent(
                userId = userIdStr,
                request = GrantConsentRequest(type = ConsentType.PRIVACY_POLICY, documentVersion = "1.0"),
                ipAddress = null,
                userAgent = null,
            )
        }
        either {
            service.grantConsent(
                userId = userIdStr,
                request = GrantConsentRequest(type = ConsentType.PRIVACY_POLICY, documentVersion = "2.0"),
                ipAddress = null,
                userAgent = null,
            )
        }

        val result = either {
            service.getActiveConsents(userIdStr)
        }

        val consents = result.getOrNull().shouldNotBeNull()
        consents shouldHaveSize ConsentType.entries.size
        val privacyConsent = consents.first { it.type == ConsentType.PRIVACY_POLICY }
        privacyConsent.granted shouldBe true
        // The latest granted record should be the one returned by the repository
        privacyConsent.documentVersion shouldBe "2.0"
    }
}

/**
 * In-memory fake for [ConsentRepository] that stores records in a list,
 * ordered by insertion (most recent last).
 */
private class FakeConsentRepository : ConsentRepository {

    private val records = mutableListOf<ConsentRecord>()
    private var counter = 0

    override suspend fun insert(
        userId: Uuid,
        consentType: String,
        granted: Boolean,
        legalDocumentVersion: String,
        ipAddress: String?,
        userAgent: String?,
    ): Uuid {
        counter++
        val id = Uuid.random()
        val createdAt = LocalDateTime(2026, 3, 15, 10, 0, counter)
        records.add(
            ConsentRecord(
                id = id,
                userId = userId,
                consentType = consentType,
                granted = granted,
                legalDocumentVersion = legalDocumentVersion,
                ipAddress = ipAddress,
                userAgent = userAgent,
                createdAt = createdAt,
            ),
        )
        return id
    }

    override suspend fun findLatestByUserAndType(userId: Uuid, consentType: String): ConsentRecord? =
        records
            .filter { it.userId == userId && it.consentType == consentType }
            .maxByOrNull { it.createdAt }

    override suspend fun findAllActiveByUser(userId: Uuid): List<ConsentRecord> =
        records
            .filter { it.userId == userId && it.granted }
            .sortedByDescending { it.createdAt }
            .distinctBy { it.consentType }

    override suspend fun findAllByUser(userId: Uuid): List<ConsentRecord> =
        records
            .filter { it.userId == userId }
            .sortedByDescending { it.createdAt }

    override suspend fun anonymizeByUser(userId: Uuid, anonymizedId: Uuid) {
        val indices = records.indices.filter { records[it].userId == userId }
        indices.forEach { i ->
            records[i] = records[i].copy(userId = anonymizedId, ipAddress = null, userAgent = null)
        }
    }
}

/**
 * In-memory fake for [LegalDocumentRepository].
 */
private class FakeLegalDocumentRepository(
    private val documents: MutableList<LegalDocumentRecord> = mutableListOf(),
) : LegalDocumentRepository {

    override suspend fun findCurrentByTypeAndLocale(type: String, locale: String): LegalDocumentRecord? =
        documents
            .filter { it.type == type && it.locale == locale }
            .maxByOrNull { it.publishedAt }

    override suspend fun findAllVersionsByType(type: String): List<LegalDocumentRecord> =
        documents.filter { it.type == type }

    override suspend fun insert(type: String, version: String, locale: String, content: String): Uuid {
        val id = Uuid.random()
        documents.add(
            LegalDocumentRecord(
                id = id,
                type = type,
                version = version,
                locale = locale,
                content = content,
                publishedAt = LocalDateTime(2026, 1, 1, 0, 0, 0),
            ),
        )
        return id
    }
}
