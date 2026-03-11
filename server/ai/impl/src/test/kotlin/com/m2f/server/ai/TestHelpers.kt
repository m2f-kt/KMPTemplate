@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.ai

import ai.koog.embeddings.local.LLMEmbedder
import ai.koog.prompt.executor.clients.LLMEmbeddingProvider
import ai.koog.prompt.llm.LLModel
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.m2f.core.config.configuration.Configuration
import com.m2f.core.config.configuration.Env
import com.m2f.core.database.migrations.Migrations
import com.m2f.core.database.migrations.registerVectorMigrations
import com.m2f.core.security.configureSecurity
import com.m2f.server.ai.rag.DocumentIngestionService
import com.m2f.server.ai.rag.DocumentRepository
import com.m2f.server.ai.routes.documentRoutes
import com.m2f.server.auth.wire.authWireModule
import com.m2f.server.auth.wire.registerAuthWireMigrations
import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.server.auth.contract.security.PasswordHasher
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

/**
 * PostgreSQL Testcontainer using pgvector image (required for vector migrations).
 */
object TestDatabase {
    val container: PostgreSQLContainer = PostgreSQLContainer("pgvector/pgvector:pg15").apply {
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

// ---- Fake LLM Embedding Provider ----

/**
 * Fake LLMEmbeddingProvider that returns zero-vectors for testing.
 * Used to construct a real LLMEmbedder without needing a live Google API key.
 */
private class FakeEmbeddingProvider : LLMEmbeddingProvider {
    override suspend fun embed(text: String, model: LLModel): List<Double> {
        // Return a 768-dimension zero vector (matching text-embedding-004 output size)
        return List(768) { 0.0 }
    }
}

/**
 * Create a test-compatible LLMEmbedder that uses fake embeddings.
 */
fun createTestEmbedder(): LLMEmbedder = LLMEmbedder(FakeEmbeddingProvider(), TextEmbedding004)

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
    val passwordHasher = org.koin.java.KoinJavaComponent.getKoin().get<PasswordHasher>()
    val hash = passwordHasher.hash("TestPassword1!")
    return userRepository.insert(email, hash, name, role)
}

// ---- Test Configuration ----

/**
 * Create a test Configuration with a placeholder AI key.
 * Tests that need real AI calls should check for the actual key and skip if absent.
 */
fun testConfiguration(): Configuration = Configuration(
    env = Env(
        auth = Env.Auth(
            secret = TEST_SECRET,
            audience = TEST_AUDIENCE,
            issuer = TEST_ISSUER,
        ),
        ai = Env.Ai(
            googleApiKey = System.getenv("AI_GOOGLE_API_KEY") ?: "test-placeholder-key",
        ),
    ),
)

// ---- Test Application ----

/**
 * Set up a Ktor testApplication with database, auth, and document routes.
 *
 * Document routes are wired with fake lambdas for file upload/delete.
 * The ingestion service uses a fake embedder that returns zero-vectors.
 *
 * @param database R2DBC database connection
 * @param roleChecker Custom role checker for tests (default: always returns true)
 * @param block Test body
 */
suspend fun documentTestApp(
    database: R2dbcDatabase,
    roleChecker: suspend (userId: String, groupId: String) -> Boolean = { _, _ -> true },
    block: suspend ApplicationTestBuilder.() -> Unit,
) {
    val config = testConfiguration()

    // Register and run migrations
    registerAuthWireMigrations()
    registerAiMigrations()
    registerVectorMigrations()
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
            module {
                single { DocumentRepository(get<R2dbcDatabase>()) }
                single {
                    DocumentIngestionService(
                        embedder = createTestEmbedder(),
                        db = get(),
                        documentRepository = get(),
                        aiDispatcher = kotlinx.coroutines.Dispatchers.Default,
                    )
                }
            },
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
                    val documentRepository: DocumentRepository = GlobalContext.get().get()
                    val documentIngestionService: DocumentIngestionService = GlobalContext.get().get()

                    context(config) {
                        documentRoutes(
                            documentIngestionService = documentIngestionService,
                            documentRepository = documentRepository,
                            fileUploader = { userId, fileName, _, _ ->
                                // Fake file uploader: return a deterministic key
                                "$userId/$fileName"
                            },
                            fileDeleter = { /* no-op */ },
                            roleChecker = roleChecker,
                            aiDispatcher = kotlinx.coroutines.Dispatchers.Default,
                        )
                    }
                }
            }
            block()
        }
    } finally {
        stopKoin()
    }
}
