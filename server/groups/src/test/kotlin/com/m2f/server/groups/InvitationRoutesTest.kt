@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups

import com.m2f.server.auth.repository.UserRepository
import com.m2f.server.groups.repository.InvitationRepository
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.AcceptInvitationRequest
import com.m2f.template.models.dto.AcceptInvitationResponse
import com.m2f.template.models.dto.CreateGroupRequest
import com.m2f.template.models.dto.CreateInvitationRequest
import com.m2f.template.models.dto.ErrorResponse
import com.m2f.template.models.dto.GroupResponse
import com.m2f.template.models.dto.InvitationResponse
import com.m2f.template.models.dto.PaginatedMemberResponse
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class InvitationRoutesTest {

    companion object {
        private lateinit var database: R2dbcDatabase

        @BeforeClass
        @JvmStatic
        fun setup() {
            TestDatabase.start()
            database = TestDatabase.createDatabase()
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            TestDatabase.stop()
        }
    }

    // ---- Full Lifecycle Test ----

    @Test
    fun `full invitation lifecycle - create invite, register, accept, verify membership`() =
        kotlinx.coroutines.test.runTest {
            invitationTestApp(database) {
                val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
                val invitationRepo =
                    org.koin.java.KoinJavaComponent.getKoin().get<InvitationRepository>()

                // Create admin user and group
                val adminId =
                    createTestUser(userRepo, "admin-lifecycle@test.com", "Admin Lifecycle", UserRole.Admin)
                val adminToken = createTestToken(adminId.toString(), UserRole.Admin)

                val client = createClient { install(ContentNegotiation) { json() } }

                val groupResp = client.post("/api/groups/create") {
                    bearerAuth(adminToken)
                    contentType(ContentType.Application.Json)
                    setBody(CreateGroupRequest(name = "Lifecycle Group", slug = "lifecycle-group"))
                }.body<GroupResponse>()

                // Invite a user
                val inviteResp = client.post("/api/groups/${groupResp.id}/invitations/create") {
                    bearerAuth(adminToken)
                    contentType(ContentType.Application.Json)
                    setBody(CreateInvitationRequest(email = "invitee-lifecycle@test.com"))
                }
                inviteResp.status shouldBe HttpStatusCode.Created

                // Get token from DB (not exposed via API for security)
                val invitations = invitationRepo.findByGroupId(Uuid.parse(groupResp.id))
                invitations.size shouldBe 1
                val inviteToken = invitations.first().token

                // Create invitee user (simulating registration)
                val inviteeId = createTestUser(
                    userRepo,
                    "invitee-lifecycle@test.com",
                    "Invitee User",
                    UserRole.User,
                )
                val inviteeToken = createTestToken(inviteeId.toString(), UserRole.User)

                // Accept invitation
                val acceptResp = client.post("/api/invitations/accept") {
                    bearerAuth(inviteeToken)
                    contentType(ContentType.Application.Json)
                    setBody(AcceptInvitationRequest(token = inviteToken))
                }
                acceptResp.status shouldBe HttpStatusCode.OK
                val acceptBody = acceptResp.body<AcceptInvitationResponse>()
                acceptBody.groupId shouldBe groupResp.id
                acceptBody.role shouldBe "MEMBER"

                // Verify membership
                val membersResp = client.get("/api/groups/${groupResp.id}/members") {
                    bearerAuth(adminToken)
                }
                membersResp.status shouldBe HttpStatusCode.OK
                val members = membersResp.body<PaginatedMemberResponse>()
                members.items.map { it.email } shouldContain "invitee-lifecycle@test.com"
            }
        }

    // ---- List Invitations Test ----

    @Test
    fun `admin can list invitations for group`() = kotlinx.coroutines.test.runTest {
        invitationTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()

            val adminId =
                createTestUser(userRepo, "admin-listinv@test.com", "Admin ListInv", UserRole.Admin)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group and invite user
            val groupResp = client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "ListInv Group", slug = "listinv-group"))
            }.body<GroupResponse>()

            client.post("/api/groups/${groupResp.id}/invitations/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateInvitationRequest(email = "listed-invitee@test.com"))
            }

            // List invitations
            val listResp = client.get("/api/groups/${groupResp.id}/invitations") {
                bearerAuth(adminToken)
            }

            listResp.status shouldBe HttpStatusCode.OK
            val invitations = listResp.body<List<InvitationResponse>>()
            invitations.size shouldBe 1
            invitations.first().email shouldBe "listed-invitee@test.com"
            invitations.first().isExpired shouldBe false
            invitations.first().isAccepted shouldBe false
            invitations.first().isRevoked shouldBe false
        }
    }

    // ---- Revoke Test ----

    @Test
    fun `admin can revoke a pending invitation`() = kotlinx.coroutines.test.runTest {
        invitationTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val invitationRepo =
                org.koin.java.KoinJavaComponent.getKoin().get<InvitationRepository>()

            val adminId =
                createTestUser(userRepo, "admin-revoke@test.com", "Admin Revoke", UserRole.Admin)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group and invite user
            val groupResp = client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Revoke Group", slug = "revoke-group"))
            }.body<GroupResponse>()

            client.post("/api/groups/${groupResp.id}/invitations/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateInvitationRequest(email = "revokee@test.com"))
            }

            // Get invitation ID from list
            val invitations = client.get("/api/groups/${groupResp.id}/invitations") {
                bearerAuth(adminToken)
            }.body<List<InvitationResponse>>()
            val invitationId = invitations.first().id

            // Revoke
            val revokeResp =
                client.post("/api/groups/${groupResp.id}/invitations/$invitationId/revoke") {
                    bearerAuth(adminToken)
                }
            revokeResp.status shouldBe HttpStatusCode.OK

            // Verify revoked in list
            val updatedInvitations = client.get("/api/groups/${groupResp.id}/invitations") {
                bearerAuth(adminToken)
            }.body<List<InvitationResponse>>()
            updatedInvitations.first().isRevoked shouldBe true

            // Try to accept revoked invitation -- should fail
            val dbInvitations = invitationRepo.findByGroupId(Uuid.parse(groupResp.id))
            val inviteToken = dbInvitations.first().token

            val inviteeId = createTestUser(
                userRepo,
                "revokee@test.com",
                "Revoked Invitee",
                UserRole.User,
            )
            val inviteeToken = createTestToken(inviteeId.toString(), UserRole.User)

            val acceptResp = client.post("/api/invitations/accept") {
                bearerAuth(inviteeToken)
                contentType(ContentType.Application.Json)
                setBody(AcceptInvitationRequest(token = inviteToken))
            }
            acceptResp.status shouldBe HttpStatusCode.Gone
        }
    }

    // ---- Expired Invitation Test ----

    @Test
    fun `expired invitation cannot be accepted`() = kotlinx.coroutines.test.runTest {
        invitationTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val invitationRepo =
                org.koin.java.KoinJavaComponent.getKoin().get<InvitationRepository>()

            val adminId =
                createTestUser(userRepo, "admin-expired@test.com", "Admin Expired", UserRole.Admin)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group
            val groupResp = client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Expired Group", slug = "expired-group"))
            }.body<GroupResponse>()

            // Create invitation directly via repository with past expiry
            val pastExpiry = (Clock.System.now() - 1.days).let { instant ->
                LocalDateTime.Companion.parse(
                    instant.toString().substringBefore("Z").let {
                        // Convert to LocalDateTime-parseable format
                        if (it.contains(".")) it.substringBefore(".") else it
                    },
                )
            }

            invitationRepo.create(
                groupId = Uuid.parse(groupResp.id),
                email = "expired-invitee@test.com",
                invitedBy = Uuid.parse(adminId.toString()),
                role = "MEMBER",
                expiresAt = pastExpiry,
            )

            // Create invitee
            val inviteeId = createTestUser(
                userRepo,
                "expired-invitee@test.com",
                "Expired Invitee",
                UserRole.User,
            )
            val inviteeToken = createTestToken(inviteeId.toString(), UserRole.User)

            // Get invitation token from DB
            val dbInvitations = invitationRepo.findByGroupId(Uuid.parse(groupResp.id))
            val inviteToken = dbInvitations.first().token

            // Try to accept expired invitation -- should fail
            val acceptResp = client.post("/api/invitations/accept") {
                bearerAuth(inviteeToken)
                contentType(ContentType.Application.Json)
                setBody(AcceptInvitationRequest(token = inviteToken))
            }
            acceptResp.status shouldBe HttpStatusCode.Gone

            // Verify invitation appears as expired in list
            val invitations = client.get("/api/groups/${groupResp.id}/invitations") {
                bearerAuth(adminToken)
            }.body<List<InvitationResponse>>()
            invitations.first().isExpired shouldBe true
        }
    }

    // ---- Authorization Tests ----

    @Test
    fun `non-admin cannot list invitations`() = kotlinx.coroutines.test.runTest {
        invitationTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()

            val adminId =
                createTestUser(userRepo, "admin-authlist@test.com", "Admin AuthList", UserRole.Admin)
            val userId =
                createTestUser(userRepo, "user-authlist@test.com", "User AuthList", UserRole.User)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)
            val userToken = createTestToken(userId.toString(), UserRole.User)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group
            val groupResp = client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "AuthList Group", slug = "authlist-group"))
            }.body<GroupResponse>()

            // Regular user tries to list invitations
            val response = client.get("/api/groups/${groupResp.id}/invitations") {
                bearerAuth(userToken)
            }

            response.status shouldBe HttpStatusCode.Forbidden
        }
    }

    @Test
    fun `non-admin cannot revoke invitation`() = kotlinx.coroutines.test.runTest {
        invitationTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()

            val adminId =
                createTestUser(userRepo, "admin-authrevoke@test.com", "Admin AuthRevoke", UserRole.Admin)
            val userId =
                createTestUser(userRepo, "user-authrevoke@test.com", "User AuthRevoke", UserRole.User)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)
            val userToken = createTestToken(userId.toString(), UserRole.User)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group and invite user
            val groupResp = client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "AuthRevoke Group", slug = "authrevoke-group"))
            }.body<GroupResponse>()

            client.post("/api/groups/${groupResp.id}/invitations/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateInvitationRequest(email = "authrevoke-invitee@test.com"))
            }

            // Get invitation ID
            val invitations = client.get("/api/groups/${groupResp.id}/invitations") {
                bearerAuth(adminToken)
            }.body<List<InvitationResponse>>()
            val invitationId = invitations.first().id

            // Regular user tries to revoke
            val response =
                client.post("/api/groups/${groupResp.id}/invitations/$invitationId/revoke") {
                    bearerAuth(userToken)
                }

            response.status shouldBe HttpStatusCode.Forbidden
        }
    }
}
