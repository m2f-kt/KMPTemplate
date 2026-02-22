@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.files

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toByteArray
import com.m2f.server.auth.repository.UserRepository
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.ErrorResponse
import com.m2f.template.models.dto.FileResponse
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.net.URLEncoder
import kotlin.uuid.ExperimentalUuidApi

class FileRoutesTest {

    companion object {
        private lateinit var database: R2dbcDatabase

        @BeforeClass
        @JvmStatic
        fun setup() {
            TestDatabase.start()
            TestMinIO.start()
            database = TestDatabase.createDatabase()
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            TestMinIO.stop()
            TestDatabase.stop()
        }
    }

    // ---- Upload Tests ----

    @Test
    fun `authenticated user can upload a file`() = kotlinx.coroutines.test.runTest {
        fileTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val s3Client = org.koin.java.KoinJavaComponent.getKoin().get<S3Client>()
            val userId = createTestUser(userRepo, "upload-user@test.com", "Upload User", UserRole.User)
            val token = createTestToken(userId.toString(), UserRole.User)

            // Create the test bucket
            createTestBucket(s3Client, "test-uploads")

            val fileContent = "Hello, this is a test file!".toByteArray()

            val client = createClient { install(ContentNegotiation) { json() } }
            val response = client.post("/api/files/upload") {
                bearerAuth(token)
                setBody(MultiPartFormDataContent(formData {
                    append("file", fileContent, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"test.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                }))
            }

            response.status shouldBe HttpStatusCode.Created
            val body = response.body<FileResponse>()
            body.key shouldContain userId.toString()
            body.originalName shouldBe "test.txt"
            body.contentType shouldBe "text/plain"
            body.size shouldBeGreaterThan 0L
        }
    }

    @Test
    fun `upload rejects file exceeding 10MB`() = kotlinx.coroutines.test.runTest {
        fileTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val s3Client = org.koin.java.KoinJavaComponent.getKoin().get<S3Client>()
            val userId = createTestUser(userRepo, "upload-large@test.com", "Large Upload", UserRole.User)
            val token = createTestToken(userId.toString(), UserRole.User)

            createTestBucket(s3Client, "test-uploads")

            val largeContent = ByteArray(11 * 1024 * 1024) // 11 MB

            val client = createClient { install(ContentNegotiation) { json() } }
            val response = client.post("/api/files/upload") {
                bearerAuth(token)
                setBody(MultiPartFormDataContent(formData {
                    append("file", largeContent, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"large.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                }))
            }

            response.status shouldBe HttpStatusCode.PayloadTooLarge
            val body = response.body<ErrorResponse>()
            body.code shouldBe "FILE_TOO_LARGE"
        }
    }

    @Test
    fun `upload rejects disallowed content type`() = kotlinx.coroutines.test.runTest {
        fileTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val s3Client = org.koin.java.KoinJavaComponent.getKoin().get<S3Client>()
            val userId = createTestUser(userRepo, "upload-zip@test.com", "Zip Upload", UserRole.User)
            val token = createTestToken(userId.toString(), UserRole.User)

            createTestBucket(s3Client, "test-uploads")

            val zipContent = ByteArray(100) // small fake zip file

            val client = createClient { install(ContentNegotiation) { json() } }
            val response = client.post("/api/files/upload") {
                bearerAuth(token)
                setBody(MultiPartFormDataContent(formData {
                    append("file", zipContent, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"archive.zip\"")
                        append(HttpHeaders.ContentType, "application/zip")
                    })
                }))
            }

            response.status shouldBe HttpStatusCode.UnsupportedMediaType
            val body = response.body<ErrorResponse>()
            body.code shouldBe "FILE_UNSUPPORTED_TYPE"
        }
    }

    @Test
    fun `uploaded file can be retrieved and content matches`() = kotlinx.coroutines.test.runTest {
        fileTestApp(database) {
            val userRepo = org.koin.java.KoinJavaComponent.getKoin().get<UserRepository>()
            val s3Client = org.koin.java.KoinJavaComponent.getKoin().get<S3Client>()
            val userId = createTestUser(userRepo, "upload-roundtrip@test.com", "Roundtrip User", UserRole.User)
            val token = createTestToken(userId.toString(), UserRole.User)

            createTestBucket(s3Client, "test-uploads")

            val fileContent = "Hello MinIO test content"
            val fileBytes = fileContent.toByteArray()

            val client = createClient { install(ContentNegotiation) { json() } }

            // Upload file
            val uploadResponse = client.post("/api/files/upload") {
                bearerAuth(token)
                setBody(MultiPartFormDataContent(formData {
                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"roundtrip.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                }))
            }

            uploadResponse.status shouldBe HttpStatusCode.Created
            val uploadBody = uploadResponse.body<FileResponse>()

            // Retrieve file URL via API (encode key since it contains '/')
            val encodedKey = URLEncoder.encode(uploadBody.key, "UTF-8")
            val getResponse = client.get("/api/files/$encodedKey") {
                bearerAuth(token)
            }

            getResponse.status shouldBe HttpStatusCode.OK

            // Verify round-trip directly via S3Client
            val s3Bytes = s3Client.getObject(GetObjectRequest {
                bucket = "test-uploads"
                key = uploadBody.key
            }) { response ->
                response.body?.toByteArray() ?: ByteArray(0)
            }

            s3Bytes.contentEquals(fileBytes) shouldBe true
        }
    }

    @Test
    fun `upload without auth returns 401`() = kotlinx.coroutines.test.runTest {
        fileTestApp(database) {
            val fileContent = "No auth file".toByteArray()

            val client = createClient { install(ContentNegotiation) { json() } }
            val response = client.post("/api/files/upload") {
                setBody(MultiPartFormDataContent(formData {
                    append("file", fileContent, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"noauth.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                }))
            }

            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }
}
