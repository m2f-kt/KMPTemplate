@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.files

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CreateBucketRequest
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.m2f.core.config.configuration.Configuration
import com.m2f.core.config.configuration.Env
import com.m2f.core.database.migrations.Migrations
import com.m2f.core.security.configureSecurity
import com.m2f.server.auth.wire.authWireModule
import com.m2f.server.auth.wire.registerAuthWireMigrations
import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.server.auth.security.BcryptPasswordHasher
import com.m2f.server.files.di.fileModule
import com.m2f.server.files.routes.fileRoutes
import com.m2f.server.files.contract.service.FileService
import com.m2f.template.models.UserRole
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.r2dbc.spi.ConnectionFactoryOptions
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.testcontainers.containers.GenericContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import java.util.Date
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// ---- Constants ----

const val TEST_SECRET = "ThisIsAReallyReallyReallyStrongSecretKeyForJWT123!@#\$%^&*()"
const val TEST_AUDIENCE = "jwt-audience"
const val TEST_ISSUER = "IssuerName"

// ---- Testcontainers ----

object TestDatabase {
    val container: PostgreSQLContainer = PostgreSQLContainer("postgres:16-alpine").apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
    }

    fun start() {
        container.start()
    }

    fun stop() {
        container.stop()
    }

    fun createDatabase(): R2dbcDatabase = R2dbcDatabase.connect {
        connectionFactoryOptions {
            option(ConnectionFactoryOptions.DRIVER, "postgresql")
            option(ConnectionFactoryOptions.HOST, container.host)
            option(ConnectionFactoryOptions.PORT, container.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT))
            option(ConnectionFactoryOptions.DATABASE, container.databaseName)
            option(ConnectionFactoryOptions.USER, container.username)
            option(ConnectionFactoryOptions.PASSWORD, container.password)
        }
    }
}

object TestMinIO {
    private val container = GenericContainer("minio/minio:latest").apply {
        withExposedPorts(9000)
        withCommand("server", "/data")
        withEnv("MINIO_ROOT_USER", "minioadmin")
        withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
    }

    fun start() {
        container.start()
    }

    fun stop() {
        container.stop()
    }

    val endpoint: String get() = "http://${container.host}:${container.getMappedPort(9000)}"
}

// ---- JWT Token Generation ----

fun createTestToken(
    userId: String,
    role: UserRole,
    secret: String = TEST_SECRET,
    audience: String = TEST_AUDIENCE,
    issuer: String = TEST_ISSUER,
): String = JWT.create()
    .withSubject(userId)
    .withClaim("role", role.value)
    .withAudience(audience)
    .withIssuer(issuer)
    .withExpiresAt(Date(System.currentTimeMillis() + 3600000)) // 1 hour
    .sign(Algorithm.HMAC256(secret))

// ---- Test User Creation ----

suspend fun createTestUser(
    userRepository: UserRepository,
    email: String,
    name: String,
    role: UserRole,
): Uuid {
    val passwordHasher = BcryptPasswordHasher(kotlinx.coroutines.Dispatchers.Default)
    val hash = passwordHasher.hash("TestPassword1!")
    return userRepository.insert(email, hash, name, role)
}

// ---- S3 Bucket Creation ----

suspend fun createTestBucket(s3Client: S3Client, bucketName: String) {
    try {
        s3Client.createBucket(CreateBucketRequest { bucket = bucketName })
    } catch (_: Exception) {
        // bucket may already exist
    }
}

// ---- Test Configuration ----

fun testConfiguration(): Configuration = Configuration(
    env = Env(
        auth = Env.Auth(
            secret = TEST_SECRET,
            audience = TEST_AUDIENCE,
            issuer = TEST_ISSUER,
        ),
        s3 = Env.S3(
            endpoint = TestMinIO.endpoint,
            bucket = "test-uploads",
            region = "us-east-1",
            accessKey = "minioadmin",
            secretKey = "minioadmin",
        ),
        email = Env.Email(
            host = "localhost",
            port = 1025,
            username = "",
            password = "",
            fromAddress = "test@example.com",
        ),
    ),
)

// ---- Test Application ----

suspend fun fileTestApp(
    database: R2dbcDatabase,
    block: suspend ApplicationTestBuilder.() -> Unit,
) {
    val config = testConfiguration()

    // Register and run migrations
    registerAuthWireMigrations()
    context(config) {
        Migrations.migrate(database)
    }

    // Start Koin before testApplication so test blocks can access it
    if (GlobalContext.getOrNull() != null) {
        stopKoin()
    }
    startKoin {
        modules(
            module {
                single { config }
                single { database }
            },
            authWireModule,
            fileModule,
        )
    }

    try {
        testApplication {
            install(Resources)
            install(ContentNegotiation) { json() }

            application {
                context(config) {
                    configureSecurity()
                }

                routing {
                    val fileService: FileService = GlobalContext.get().get()
                    fileRoutes(fileService)
                }
            }
            block()
        }
    } finally {
        stopKoin()
    }
}
