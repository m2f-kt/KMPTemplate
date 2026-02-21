@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.m2f.core.config.configuration.Configuration
import com.m2f.core.config.configuration.Env
import com.m2f.core.database.migrations.Migrations
import com.m2f.core.security.configureSecurity
import com.m2f.server.auth.di.authModule
import com.m2f.server.auth.registerAuthMigrations
import com.m2f.server.auth.repository.UserRepository
import com.m2f.server.auth.security.PasswordHasher
import com.m2f.server.groups.di.groupModule
import com.m2f.server.groups.routes.groupRoutes
import com.m2f.server.groups.service.GroupService
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

    /**
     * Create an R2DBC database connection to the Testcontainers PostgreSQL instance.
     */
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

// ---- JWT Token Generation ----

/**
 * Create a test JWT token for a given user ID and role.
 * Uses the same secret/audience/issuer as the test configuration.
 */
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

/**
 * Create a test user directly in the database with a hashed password.
 * Returns the generated UUID.
 */
suspend fun createTestUser(
    userRepository: UserRepository,
    email: String,
    name: String,
    role: UserRole,
): Uuid {
    val passwordHasher = PasswordHasher(kotlinx.coroutines.Dispatchers.Default)
    val hash = passwordHasher.hash("TestPassword1!")
    return userRepository.insert(email, hash, name, role)
}

// ---- Test Configuration ----

/**
 * Create a test Configuration matching the default dev environment secrets.
 */
fun testConfiguration(): Configuration = Configuration(
    env = Env(
        auth = Env.Auth(
            secret = TEST_SECRET,
            audience = TEST_AUDIENCE,
            issuer = TEST_ISSUER,
        ),
    ),
)

// ---- Test Application ----

/**
 * Set up a Ktor testApplication with database, auth, and group routes configured.
 * Koin is started before testApplication so that test blocks can access DI.
 */
suspend fun groupTestApp(
    database: R2dbcDatabase,
    block: suspend ApplicationTestBuilder.() -> Unit,
) {
    val config = testConfiguration()

    // Register and run migrations
    registerAuthMigrations()
    registerGroupMigrations()
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
            authModule,
            groupModule,
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
                    val groupService: GroupService = GlobalContext.get().get()
                    groupRoutes(groupService)
                }
            }
            block()
        }
    } finally {
        stopKoin()
    }
}
