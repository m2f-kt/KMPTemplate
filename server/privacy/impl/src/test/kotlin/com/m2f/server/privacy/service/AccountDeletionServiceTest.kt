@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.m2f.server.privacy.service

import arrow.core.raise.either
import com.m2f.server.auth.contract.errors.InvalidCredentials
import com.m2f.server.auth.contract.repository.UserRecord
import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.server.auth.contract.security.PasswordHasher
import com.m2f.server.privacy.contract.errors.DeletionAlreadyPending
import com.m2f.server.privacy.contract.repository.AccountDeletionRecord
import com.m2f.server.privacy.contract.repository.AccountDeletionRepository
import com.m2f.server.privacy.contract.repository.ConsentRecord
import com.m2f.server.privacy.contract.repository.ConsentRepository
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.privacy.DeletionRequest
import com.m2f.template.models.dto.privacy.DeletionStatus
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Test
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AccountDeletionServiceTest {

    private val userId = Uuid.random()
    private val userIdStr = userId.toString()
    private val passwordHash = "hashed_password_123"

    private val testUser = UserRecord(
        id = userId,
        email = "user@example.com",
        passwordHash = passwordHash,
        name = "Test User",
        role = UserRole.User,
    )

    private fun createService(
        deletionRepo: FakeAccountDeletionRepository = FakeAccountDeletionRepository(),
        userRepo: FakeUserRepository = FakeUserRepository(users = mutableMapOf(userId to testUser)),
        hasher: FakePasswordHasher = FakePasswordHasher(verifyResult = true),
        consentRepo: StubConsentRepository = StubConsentRepository(),
    ) = AccountDeletionServiceImpl(deletionRepo, userRepo, hasher, consentRepo)

    /** Helper: verify password and get a confirmation token for the test user. */
    private suspend fun AccountDeletionServiceImpl.getToken(uid: String = userIdStr, password: String = "correct_password"): String =
        either { verifyPasswordForDeletion(uid, password) }.shouldBeRight()

    @Test
    fun `verifyPasswordForDeletion with valid password returns token`() = runTest {
        val service = createService()

        val result = either {
            service.verifyPasswordForDeletion(userIdStr, "correct_password")
        }

        result.shouldBeRight().shouldNotBeNull()
    }

    @Test
    fun `verifyPasswordForDeletion with wrong password raises InvalidCredentials`() = runTest {
        val service = createService(hasher = FakePasswordHasher(verifyResult = false))

        val result = either {
            service.verifyPasswordForDeletion(userIdStr, "wrong_password")
        }

        result.shouldBeLeft().shouldBeInstanceOf<InvalidCredentials>()
    }

    @Test
    fun `verifyPasswordForDeletion with unknown user raises InvalidCredentials`() = runTest {
        val service = createService(userRepo = FakeUserRepository())

        val result = either {
            service.verifyPasswordForDeletion(userIdStr, "any_password")
        }

        result.shouldBeLeft().shouldBeInstanceOf<InvalidCredentials>()
    }

    @Test
    fun `requestDeletion with valid token creates pending deletion`() = runTest {
        val service = createService()
        val token = service.getToken()

        val result = either {
            service.requestDeletion(
                userId = userIdStr,
                request = DeletionRequest(confirmationToken = token, reason = "Testing"),
            )
        }

        val response = result.shouldBeRight()
        response.status shouldBe DeletionStatus.PENDING
        response.scheduledAt.shouldNotBeNull()
        response.completedAt.shouldBeNull()
    }

    @Test
    fun `requestDeletion with invalid token raises InvalidCredentials`() = runTest {
        val service = createService()
        service.getToken() // generate a real token but don't use it

        val result = either {
            service.requestDeletion(
                userId = userIdStr,
                request = DeletionRequest(confirmationToken = "invalid-token"),
            )
        }

        result.shouldBeLeft().shouldBeInstanceOf<InvalidCredentials>()
    }

    @Test
    fun `requestDeletion without verifying password raises InvalidCredentials`() = runTest {
        val service = createService()

        val result = either {
            service.requestDeletion(
                userId = userIdStr,
                request = DeletionRequest(confirmationToken = "no-such-token"),
            )
        }

        result.shouldBeLeft().shouldBeInstanceOf<InvalidCredentials>()
    }

    @Test
    fun `requestDeletion when deletion already pending raises DeletionAlreadyPending`() = runTest {
        val service = createService()

        // Create first deletion request
        val token1 = service.getToken()
        either {
            service.requestDeletion(
                userId = userIdStr,
                request = DeletionRequest(confirmationToken = token1),
            )
        }.shouldBeRight()

        // Try again -- should fail (already pending)
        val token2 = service.getToken()
        val result = either {
            service.requestDeletion(
                userId = userIdStr,
                request = DeletionRequest(confirmationToken = token2),
            )
        }

        result.shouldBeLeft().shouldBeInstanceOf<DeletionAlreadyPending>()
    }

    @Test
    fun `getDeletionStatus returns null when no deletion pending`() = runTest {
        val service = createService()

        val result = either {
            service.getDeletionStatus(userIdStr)
        }

        result.shouldBeRight().shouldBeNull()
    }

    @Test
    fun `cancelDeletion cancels a pending deletion`() = runTest {
        val service = createService()

        // Create a deletion request first
        val token = service.getToken()
        either {
            service.requestDeletion(
                userId = userIdStr,
                request = DeletionRequest(confirmationToken = token),
            )
        }.shouldBeRight()

        // Cancel it
        val cancelResult = either {
            service.cancelDeletion(userIdStr)
        }
        cancelResult.shouldBeRight()

        // Status should now be null (no pending deletion)
        val statusResult = either {
            service.getDeletionStatus(userIdStr)
        }
        statusResult.shouldBeRight().shouldBeNull()
    }
}

// region Fakes

private class FakeAccountDeletionRepository : AccountDeletionRepository {

    private val records = mutableListOf<AccountDeletionRecord>()

    override suspend fun insert(userId: Uuid, reason: String?, scheduledAt: LocalDateTime): Uuid {
        val id = Uuid.random()
        records.add(
            AccountDeletionRecord(
                id = id,
                userId = userId,
                status = "PENDING",
                reason = reason,
                scheduledAt = scheduledAt,
                completedAt = null,
                createdAt = LocalDateTime(2026, 3, 15, 10, 0, 0),
            ),
        )
        return id
    }

    override suspend fun findPendingByUser(userId: Uuid): AccountDeletionRecord? =
        records.firstOrNull { it.userId == userId && it.status == "PENDING" }

    override suspend fun findById(id: Uuid): AccountDeletionRecord? =
        records.firstOrNull { it.id == id }

    override suspend fun updateStatus(id: Uuid, status: String, completedAt: LocalDateTime?): Boolean {
        val index = records.indexOfFirst { it.id == id }
        if (index == -1) return false
        records[index] = records[index].copy(status = status, completedAt = completedAt)
        return true
    }

    override suspend fun findDueForExecution(): List<AccountDeletionRecord> =
        records.filter { it.status == "PENDING" }

    override suspend fun cancelByUser(userId: Uuid): Boolean {
        val index = records.indexOfFirst { it.userId == userId && it.status == "PENDING" }
        if (index == -1) return false
        records[index] = records[index].copy(status = "CANCELLED")
        return true
    }
}

private class FakeUserRepository(
    private val users: MutableMap<Uuid, UserRecord> = mutableMapOf(),
) : UserRepository {

    override suspend fun findByEmail(email: String): UserRecord? =
        users.values.firstOrNull { it.email == email }

    override suspend fun findById(id: Uuid): UserRecord? = users[id]

    override suspend fun insert(email: String, passwordHash: String, name: String, role: UserRole): Uuid {
        val id = Uuid.random()
        users[id] = UserRecord(id = id, email = email, passwordHash = passwordHash, name = name, role = role)
        return id
    }

    override suspend fun updateProfile(id: Uuid, name: String?, email: String?): Boolean {
        val user = users[id] ?: return false
        users[id] = user.copy(
            name = name ?: user.name,
            email = email ?: user.email,
        )
        return true
    }

    override suspend fun updatePasswordHash(id: Uuid, passwordHash: String): Boolean {
        val user = users[id] ?: return false
        users[id] = user.copy(passwordHash = passwordHash)
        return true
    }

    override suspend fun count(): Long = users.size.toLong()

    override suspend fun updateAvatarUrl(id: Uuid, avatarUrl: String): Boolean {
        val user = users[id] ?: return false
        users[id] = user.copy(avatarUrl = avatarUrl)
        return true
    }
}

private class FakePasswordHasher(
    private val verifyResult: Boolean = false,
) : PasswordHasher {

    override suspend fun hash(password: String): String = "hashed_$password"

    override suspend fun verify(password: String, hash: String): Boolean = verifyResult
}

/**
 * Minimal stub for [ConsentRepository] -- only needed as a constructor dependency
 * for [AccountDeletionServiceImpl], not exercised in these tests.
 */
private class StubConsentRepository : ConsentRepository {

    override suspend fun insert(
        userId: Uuid,
        consentType: String,
        granted: Boolean,
        legalDocumentVersion: String,
        ipAddress: String?,
        userAgent: String?,
    ): Uuid = Uuid.random()

    override suspend fun findLatestByUserAndType(userId: Uuid, consentType: String): ConsentRecord? = null

    override suspend fun findAllActiveByUser(userId: Uuid): List<ConsentRecord> = emptyList()

    override suspend fun findAllByUser(userId: Uuid): List<ConsentRecord> = emptyList()

    override suspend fun anonymizeByUser(userId: Uuid, anonymizedId: Uuid) {}
}

// endregion
