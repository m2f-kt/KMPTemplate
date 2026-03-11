@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.ai

import com.m2f.server.ai.rag.DocumentRepository
import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.DocumentListResponse
import com.m2f.template.models.dto.DocumentResponse
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DocumentRoutesTest {

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

    // ---- Upload Tests ----

    @Test
    fun `POST upload creates document record`() = kotlinx.coroutines.test.runTest {
        documentTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val userId = createTestUser(userRepo, "upload-doc@test.com", "Upload User", UserRole.Admin)
            val token = createTestToken(userId.toString(), UserRole.Admin)
            val groupId = Uuid.random().toString()

            val client = createClient { install(ContentNegotiation) { json() } }
            val response = client.post("/api/documents/upload") {
                bearerAuth(token)
                setBody(MultiPartFormDataContent(formData {
                    append("groupId", groupId)
                    append("scope", "personal")
                    append("file", "Hello, this is test content".toByteArray(), Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"test.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                }))
            }

            response.status shouldBe HttpStatusCode.Created
            val body = response.body<DocumentResponse>()
            body.name shouldBe "test.txt"
            body.scope shouldBe "personal"
            body.status shouldBe "pending"
            body.contentType shouldBe "text/plain"
            body.groupId shouldBe groupId
        }
    }

    @Test
    fun `upload without auth returns 401`() = kotlinx.coroutines.test.runTest {
        documentTestApp(database) {
            val client = createClient { install(ContentNegotiation) { json() } }
            val response = client.post("/api/documents/upload") {
                setBody(MultiPartFormDataContent(formData {
                    append("groupId", Uuid.random().toString())
                    append("scope", "personal")
                    append("file", "content".toByteArray(), Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"noauth.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                }))
            }

            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }

    // ---- List Tests ----

    @Test
    fun `GET list returns uploaded documents`() = kotlinx.coroutines.test.runTest {
        documentTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val userId = createTestUser(userRepo, "list-doc@test.com", "List User", UserRole.Admin)
            val token = createTestToken(userId.toString(), UserRole.Admin)
            val groupId = Uuid.random().toString()

            val client = createClient { install(ContentNegotiation) { json() } }

            // Upload 2 documents
            repeat(2) { i ->
                client.post("/api/documents/upload") {
                    bearerAuth(token)
                    setBody(MultiPartFormDataContent(formData {
                        append("groupId", groupId)
                        append("scope", "personal")
                        append("file", "Content $i".toByteArray(), Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"doc$i.txt\"")
                            append(HttpHeaders.ContentType, "text/plain")
                        })
                    }))
                }
            }

            // List documents
            val listResponse = client.get("/api/documents/list?groupId=$groupId") {
                bearerAuth(token)
            }

            listResponse.status shouldBe HttpStatusCode.OK
            val body = listResponse.body<DocumentListResponse>()
            body.documents shouldHaveSize 2
        }
    }

    @Test
    fun `GET list respects group scoping - cross-group isolation`() = kotlinx.coroutines.test.runTest {
        val group1Id = Uuid.random().toString()
        val group2Id = Uuid.random().toString()

        documentTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val user1Id = createTestUser(userRepo, "user1-isolation@test.com", "User 1", UserRole.Admin)
            val user2Id = createTestUser(userRepo, "user2-isolation@test.com", "User 2", UserRole.Admin)
            val token1 = createTestToken(user1Id.toString(), UserRole.Admin)
            val token2 = createTestToken(user2Id.toString(), UserRole.Admin)

            val client = createClient { install(ContentNegotiation) { json() } }

            // User 1 uploads doc to group 1
            client.post("/api/documents/upload") {
                bearerAuth(token1)
                setBody(MultiPartFormDataContent(formData {
                    append("groupId", group1Id)
                    append("scope", "personal")
                    append("file", "Group 1 content".toByteArray(), Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"group1.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                }))
            }

            // User 2 lists docs in group 2 -- should see 0 documents
            val listResponse = client.get("/api/documents/list?groupId=$group2Id") {
                bearerAuth(token2)
            }

            listResponse.status shouldBe HttpStatusCode.OK
            val body = listResponse.body<DocumentListResponse>()
            body.documents shouldHaveSize 0
        }
    }

    // ---- Delete Tests ----

    @Test
    fun `POST delete removes document`() = kotlinx.coroutines.test.runTest {
        documentTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val userId = createTestUser(userRepo, "delete-doc@test.com", "Delete User", UserRole.Admin)
            val token = createTestToken(userId.toString(), UserRole.Admin)
            val groupId = Uuid.random().toString()

            val client = createClient { install(ContentNegotiation) { json() } }

            // Upload a document
            val uploadResponse = client.post("/api/documents/upload") {
                bearerAuth(token)
                setBody(MultiPartFormDataContent(formData {
                    append("groupId", groupId)
                    append("scope", "personal")
                    append("file", "Delete me".toByteArray(), Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"deleteme.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                }))
            }

            uploadResponse.status shouldBe HttpStatusCode.Created
            val doc = uploadResponse.body<DocumentResponse>()

            // Wait briefly for background ingestion (fake embedder is fast)
            delay(200)

            // Delete the document
            val deleteResponse = client.post("/api/documents/${doc.id}/delete") {
                bearerAuth(token)
            }

            deleteResponse.status shouldBe HttpStatusCode.OK

            // Verify document is gone from list
            val listResponse = client.get("/api/documents/list?groupId=$groupId") {
                bearerAuth(token)
            }
            val listBody = listResponse.body<DocumentListResponse>()
            listBody.documents shouldHaveSize 0
        }
    }

    // ---- Authorization Tests ----

    @Test
    fun `member cannot upload group-scoped documents`() = kotlinx.coroutines.test.runTest {
        // roleChecker returns false = user is NOT admin
        documentTestApp(database, roleChecker = { _, _ -> false }) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val userId = createTestUser(userRepo, "member-noupload@test.com", "Member User", UserRole.User)
            val token = createTestToken(userId.toString(), UserRole.User)
            val groupId = Uuid.random().toString()

            val client = createClient { install(ContentNegotiation) { json() } }
            val response = client.post("/api/documents/upload") {
                bearerAuth(token)
                setBody(MultiPartFormDataContent(formData {
                    append("groupId", groupId)
                    append("scope", "group")
                    append("file", "Admin only".toByteArray(), Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"group.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                }))
            }

            response.status shouldBe HttpStatusCode.Forbidden
        }
    }

    @Test
    fun `upload rejects unsupported file type`() = kotlinx.coroutines.test.runTest {
        documentTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val userId = createTestUser(userRepo, "upload-bad-type@test.com", "Bad Type User", UserRole.Admin)
            val token = createTestToken(userId.toString(), UserRole.Admin)
            val groupId = Uuid.random().toString()

            val client = createClient { install(ContentNegotiation) { json() } }
            val response = client.post("/api/documents/upload") {
                bearerAuth(token)
                setBody(MultiPartFormDataContent(formData {
                    append("groupId", groupId)
                    append("scope", "personal")
                    append("file", "fake zip".toByteArray(), Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"bad.zip\"")
                        append(HttpHeaders.ContentType, "application/zip")
                    })
                }))
            }

            // Should be rejected due to unsupported content type
            response.status shouldBe HttpStatusCode.UnprocessableEntity
        }
    }
}
