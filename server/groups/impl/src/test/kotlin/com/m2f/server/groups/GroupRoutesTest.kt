@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups

import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.server.groups.repository.GroupRepository
import com.m2f.server.groups.contract.repository.MembershipRepository
import com.m2f.template.models.GroupRole
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.AddMemberRequest
import com.m2f.template.models.dto.CreateGroupRequest
import com.m2f.template.models.dto.ErrorResponse
import com.m2f.template.models.dto.GroupResponse
import com.m2f.template.models.dto.MemberResponse
import com.m2f.template.models.dto.PaginatedMemberResponse
import com.m2f.template.models.dto.RegisterMemberRequest
import com.m2f.template.models.dto.UpdateGroupRequest
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
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import kotlin.uuid.ExperimentalUuidApi

class GroupRoutesTest {

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

    // ---- Group CRUD Tests ----

    @Test
    fun `admin can create a group`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val adminId = createTestUser(userRepo, "admin-create@test.com", "Admin User", UserRole.Admin)
            val token = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }
            val response = client.post("/api/groups/create") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Test Group", slug = "test-group-create", description = "A test group"))
            }

            response.status shouldBe HttpStatusCode.Created
            val body = response.body<GroupResponse>()
            body.name shouldBe "Test Group"
            body.slug shouldBe "test-group-create"
            body.description shouldBe "A test group"
            body.memberCount shouldBe 1
        }
    }

    @Test
    fun `create group returns 403 for regular user`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val userId = createTestUser(userRepo, "user-noadmin@test.com", "Regular User", UserRole.User)
            val token = createTestToken(userId.toString(), UserRole.User)

            val client = createClient { install(ContentNegotiation) { json() } }
            val response = client.post("/api/groups/create") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Unauthorized Group", slug = "unauth-group"))
            }

            response.status shouldBe HttpStatusCode.Forbidden
        }
    }

    @Test
    fun `create group with duplicate slug returns error`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val adminId = createTestUser(userRepo, "admin-dup@test.com", "Admin Dup", UserRole.Admin)
            val token = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create first group
            client.post("/api/groups/create") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "First Group", slug = "dup-slug"))
            }

            // Try to create second group with same slug
            val response = client.post("/api/groups/create") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Second Group", slug = "dup-slug"))
            }

            response.status shouldBe HttpStatusCode.UnprocessableEntity
            val body = response.body<ErrorResponse>()
            body.code shouldBe "GROUP_ALREADY_EXISTS"
        }
    }

    @Test
    fun `admin can read their own group`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val adminId = createTestUser(userRepo, "admin-read@test.com", "Admin Read", UserRole.Admin)
            val token = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group
            val createResponse = client.post("/api/groups/create") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Read Group", slug = "read-group"))
            }
            val created = createResponse.body<GroupResponse>()

            // Read group
            val response = client.get("/api/groups/${created.id}") {
                bearerAuth(token)
            }

            response.status shouldBe HttpStatusCode.OK
            val body = response.body<GroupResponse>()
            body.name shouldBe "Read Group"
            body.slug shouldBe "read-group"
        }
    }

    @Test
    fun `admin can update their group`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val adminId = createTestUser(userRepo, "admin-update@test.com", "Admin Update", UserRole.Admin)
            val token = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group
            val createResponse = client.post("/api/groups/create") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Update Group", slug = "update-group"))
            }
            val created = createResponse.body<GroupResponse>()

            // Update group
            val response = client.post("/api/groups/${created.id}/update") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(UpdateGroupRequest(name = "Updated Name", description = "Updated description"))
            }

            response.status shouldBe HttpStatusCode.OK
            val body = response.body<GroupResponse>()
            body.name shouldBe "Updated Name"
            body.description shouldBe "Updated description"
        }
    }

    @Test
    fun `owner can delete their group`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val adminId = createTestUser(userRepo, "admin-delete@test.com", "Admin Delete", UserRole.Admin)
            val token = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group
            val createResponse = client.post("/api/groups/create") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Delete Group", slug = "delete-group"))
            }
            val created = createResponse.body<GroupResponse>()

            // Delete group
            val response = client.post("/api/groups/${created.id}/delete") {
                bearerAuth(token)
            }

            response.status shouldBe HttpStatusCode.OK

            // Verify group no longer accessible
            val getResponse = client.get("/api/groups/${created.id}") {
                bearerAuth(token)
            }
            getResponse.status shouldBe HttpStatusCode.NotFound
        }
    }

    @Test
    fun `power admin can list all groups`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val powerAdminId = createTestUser(userRepo, "poweradmin-list@test.com", "Power Admin", UserRole.PowerAdmin)
            val adminId = createTestUser(userRepo, "admin-list@test.com", "Admin List", UserRole.Admin)
            val powerToken = createTestToken(powerAdminId.toString(), UserRole.PowerAdmin)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create a group
            client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Listed Group", slug = "listed-group"))
            }

            // Power admin lists all groups
            val response = client.get("/api/groups/list") {
                bearerAuth(powerToken)
            }

            response.status shouldBe HttpStatusCode.OK
            val body = response.body<List<GroupResponse>>()
            body shouldNotBe emptyList<GroupResponse>()
        }
    }

    @Test
    fun `non-power-admin cannot list all groups`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val adminId = createTestUser(userRepo, "admin-nolist@test.com", "Admin NoList", UserRole.Admin)
            val token = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            val response = client.get("/api/groups/list") {
                bearerAuth(token)
            }

            response.status shouldBe HttpStatusCode.Forbidden
        }
    }

    // ---- Member Management Tests ----

    @Test
    fun `admin can add existing user to group`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val adminId = createTestUser(userRepo, "admin-addmember@test.com", "Admin Add", UserRole.Admin)
            val memberId = createTestUser(userRepo, "member-toadd@test.com", "New Member", UserRole.User)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group
            val createResponse = client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Add Member Group", slug = "add-member-group"))
            }
            val group = createResponse.body<GroupResponse>()

            // Add member
            val response = client.post("/api/groups/${group.id}/members/add") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(AddMemberRequest(userId = memberId.toString(), role = GroupRole.Member))
            }

            response.status shouldBe HttpStatusCode.OK
            val body = response.body<MemberResponse>()
            body.email shouldBe "member-toadd@test.com"
            body.role shouldBe GroupRole.Member
        }
    }

    @Test
    fun `admin can remove member from group`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val adminId = createTestUser(userRepo, "admin-remove@test.com", "Admin Remove", UserRole.Admin)
            val memberId = createTestUser(userRepo, "member-toremove@test.com", "Remove Member", UserRole.User)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group
            val createResponse = client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Remove Member Group", slug = "remove-member-group"))
            }
            val group = createResponse.body<GroupResponse>()

            // Add member
            client.post("/api/groups/${group.id}/members/add") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(AddMemberRequest(userId = memberId.toString()))
            }

            // Remove member
            val response = client.post("/api/groups/${group.id}/members/${memberId}/remove") {
                bearerAuth(adminToken)
            }

            response.status shouldBe HttpStatusCode.OK
        }
    }

    @Test
    fun `cannot remove group owner`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val adminId = createTestUser(userRepo, "admin-removeowner@test.com", "Admin Owner", UserRole.Admin)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group (admin becomes owner)
            val createResponse = client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Owner Group", slug = "owner-group"))
            }
            val group = createResponse.body<GroupResponse>()

            // Try to remove owner
            val response = client.post("/api/groups/${group.id}/members/${adminId}/remove") {
                bearerAuth(adminToken)
            }

            response.status shouldBe HttpStatusCode.Forbidden
            val body = response.body<ErrorResponse>()
            body.code shouldBe "GROUP_FORBIDDEN"
        }
    }

    @Test
    fun `admin can list group members`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val adminId = createTestUser(userRepo, "admin-listmembers@test.com", "Admin List", UserRole.Admin)
            val memberId = createTestUser(userRepo, "member-listed@test.com", "Listed Member", UserRole.User)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group
            val createResponse = client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "List Members Group", slug = "list-members-group"))
            }
            val group = createResponse.body<GroupResponse>()

            // Add member
            client.post("/api/groups/${group.id}/members/add") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(AddMemberRequest(userId = memberId.toString()))
            }

            // List members
            val response = client.get("/api/groups/${group.id}/members") {
                bearerAuth(adminToken)
            }

            response.status shouldBe HttpStatusCode.OK
            val body = response.body<PaginatedMemberResponse>()
            body.items.size shouldBe 2 // owner + added member
        }
    }

    @Test
    fun `regular member cannot list group members`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val membershipRepo = org.koin.java.KoinJavaComponent.getKoin().get<MembershipRepository>()
            val groupRepo = org.koin.java.KoinJavaComponent.getKoin().get<GroupRepository>()

            val adminId = createTestUser(userRepo, "admin-membernolist@test.com", "Admin NoList", UserRole.Admin)
            val memberId = createTestUser(userRepo, "member-nolist@test.com", "No List Member", UserRole.User)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)
            val memberToken = createTestToken(memberId.toString(), UserRole.User)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group
            val createResponse = client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "NoList Group", slug = "nolist-group"))
            }
            val group = createResponse.body<GroupResponse>()

            // Add member
            client.post("/api/groups/${group.id}/members/add") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(AddMemberRequest(userId = memberId.toString()))
            }

            // Member tries to list members -- should fail (requires ADMIN role in group)
            val response = client.get("/api/groups/${group.id}/members") {
                bearerAuth(memberToken)
            }

            response.status shouldBe HttpStatusCode.Forbidden
        }
    }

    // ---- RBAC Tests ----

    @Test
    fun `group member cannot update group`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val adminId = createTestUser(userRepo, "admin-rbac-update@test.com", "Admin RBAC", UserRole.Admin)
            val memberId = createTestUser(userRepo, "member-rbac-update@test.com", "Member RBAC", UserRole.User)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)
            val memberToken = createTestToken(memberId.toString(), UserRole.User)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group and add member
            val createResponse = client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "RBAC Group", slug = "rbac-update-group"))
            }
            val group = createResponse.body<GroupResponse>()

            client.post("/api/groups/${group.id}/members/add") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(AddMemberRequest(userId = memberId.toString()))
            }

            // Member tries to update group -- should fail
            val response = client.post("/api/groups/${group.id}/update") {
                bearerAuth(memberToken)
                contentType(ContentType.Application.Json)
                setBody(UpdateGroupRequest(name = "Hacked Name"))
            }

            response.status shouldBe HttpStatusCode.Forbidden
        }
    }

    // ---- Cross-Group Isolation Tests ----

    @Test
    fun `user in group A cannot access group B - returns 403`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val admin1Id = createTestUser(userRepo, "admin-groupA@test.com", "Admin A", UserRole.Admin)
            val admin2Id = createTestUser(userRepo, "admin-groupB@test.com", "Admin B", UserRole.Admin)
            val token1 = createTestToken(admin1Id.toString(), UserRole.Admin)
            val token2 = createTestToken(admin2Id.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group A (admin1 is owner)
            val groupAResponse = client.post("/api/groups/create") {
                bearerAuth(token1)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Group A", slug = "isolation-group-a"))
            }
            val groupA = groupAResponse.body<GroupResponse>()

            // Create group B (admin2 is owner)
            val groupBResponse = client.post("/api/groups/create") {
                bearerAuth(token2)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "Group B", slug = "isolation-group-b"))
            }
            val groupB = groupBResponse.body<GroupResponse>()

            // Admin1 tries to access group B -- should get 403
            val crossResponse = client.get("/api/groups/${groupB.id}") {
                bearerAuth(token1)
            }

            crossResponse.status shouldBe HttpStatusCode.Forbidden
            val body = crossResponse.body<ErrorResponse>()
            body.code shouldBe "GROUP_FORBIDDEN"

            // Admin1 can still access group A
            val ownResponse = client.get("/api/groups/${groupA.id}") {
                bearerAuth(token1)
            }
            ownResponse.status shouldBe HttpStatusCode.OK
        }
    }

    @Test
    fun `power admin can access any group`() = kotlinx.coroutines.test.runTest {
        groupTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val adminId = createTestUser(userRepo, "admin-pa-access@test.com", "Admin PA", UserRole.Admin)
            val powerAdminId = createTestUser(userRepo, "poweradmin-access@test.com", "Power Admin", UserRole.PowerAdmin)
            val adminToken = createTestToken(adminId.toString(), UserRole.Admin)
            val powerToken = createTestToken(powerAdminId.toString(), UserRole.PowerAdmin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // Create group (admin is owner)
            val createResponse = client.post("/api/groups/create") {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(CreateGroupRequest(name = "PA Access Group", slug = "pa-access-group"))
            }
            val group = createResponse.body<GroupResponse>()

            // Power admin (not a member) can access the group
            val response = client.get("/api/groups/${group.id}") {
                bearerAuth(powerToken)
            }

            response.status shouldBe HttpStatusCode.OK
            val body = response.body<GroupResponse>()
            body.name shouldBe "PA Access Group"
        }
    }
}
