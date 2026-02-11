# Phase 2: Server Auth & Users - Research

**Researched:** 2026-02-11
**Domain:** Ktor server authentication, Exposed R2DBC persistence, JWT tokens, bcrypt hashing, Arrow Raise error handling
**Confidence:** HIGH

## Summary

Phase 2 implements server-side authentication and user management on top of the foundation established in Phase 1. The project already has significant scaffolding in place: JWT verification is configured in `SecurityPlugin.kt` (using `com.auth0.jwt` via Ktor's auth-jwt plugin), the `Env.Auth` configuration holds JWT secret/audience/issuer, the `DomainError` hierarchy includes `Unauthorized`, shared DTOs (`LoginRequest`, `RegisterRequest`, `AuthResponse`, `RefreshTokenRequest`, `UserResponse`, `UpdateProfileRequest`) are defined in `core:models`, and validation helpers (`validateEmail`, `validatePassword`, `validateName`) using Arrow Raise context parameters already exist in `ValidationSupport.kt`. The `conduit`/`conduitAuth` pattern in `Error.kt` provides the established route handler wrapper that converts `DomainError` to HTTP responses via Arrow `either`.

The primary work is: (1) defining Exposed R2DBC tables for users and refresh tokens, (2) implementing repository and service layers with Arrow Raise, (3) creating JWT token generation (the verification side already exists), (4) building route handlers using the established `conduit`/`conduitAuth` pattern, (5) adding a custom `createRouteScopedPlugin` for role-based authorization, and (6) wiring everything through Koin DI.

**Primary recommendation:** Build on existing patterns -- use `com.auth0.jwt.JWT.create()` for token generation (matching the existing verifier), jBCrypt 0.4 for password hashing, Exposed R2DBC DSL-only operations (no DAO), and the established `conduit`/`conduitAuth` + `DomainError` pattern for all route handlers. Use `zipOrAccumulate` with the existing `context(raise: Raise<FieldError>)` validators for error accumulation.

## Standard Stack

### Core (Already in Project)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Ktor Server Auth JWT | 3.4.0 | JWT authentication plugin (verify + principal extraction) | Official Ktor plugin; already installed in SecurityPlugin.kt |
| com.auth0:java-jwt | (transitive via ktor-server-auth-jwt) | JWT token creation and verification | Comes bundled with Ktor auth-jwt; already used for verification |
| jBCrypt | 0.4 | bcrypt password hashing | Already in version catalog as `jbcrypt`; standard Java bcrypt implementation |
| Exposed R2DBC | 1.0.0 | Non-blocking database operations | Already configured with R2dbcDatabase, suspendTransaction, SchemaUtils |
| Exposed Core | 1.0.0 | Table definitions, DSL query API | Already used for MigrationsTable definition |
| Exposed Kotlin DateTime | 1.0.0 | datetime column type support | Already used in MigrationsTable |
| Arrow Core | 2.2.1.1 | Raise DSL, Either, zipOrAccumulate | Already used for error handling patterns |
| Koin | 4.1.1 | Dependency injection | Already wired with `install(Koin)` in Application.kt |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Ktor Server Test Host | 3.4.0 | testApplication {} for route testing | Already in testing-server bundle |
| Kotest Assertions | 6.1.3 | shouldBe, shouldBeRight, shouldBeLeft matchers | Already in testing-server bundle |
| Kotest Arrow | 6.1.3 | Arrow-specific assertions (shouldBeRight/Left) | Already in testing-server bundle |
| Testcontainers PostgreSQL | 2.0.3 | Disposable PostgreSQL for integration tests | Already in testing-server bundle |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| com.auth0:java-jwt (for token creation) | io.jsonwebtoken:jjwt (in version catalog as `jwt` bundle) | JJWT is in the version catalog but NOT used anywhere yet. The existing SecurityPlugin.kt already imports `com.auth0.jwt`. Using JJWT would require managing two JWT libraries. **Recommendation: Use com.auth0:java-jwt for both creation and verification -- it is already a transitive dependency.** The `jwt` bundle (JJWT) in the version catalog should be removed or deferred. |
| Plain Exposed DSL Table with uuid column | UUIDTable (from exposed-core dao.id package) | UUIDTable is technically in exposed-core (not exposed-dao) so it works with R2DBC. However, the project's existing table (MigrationsTable) uses plain `Table`. **Recommendation: Use plain `Table` with `uuid()` column and `autoGenerate()` for consistency with existing patterns.** |
| Custom RBAC plugin | ktor-role-based-auth (third-party) | Third-party plugin adds a dependency for something achievable in ~30 lines with `createRouteScopedPlugin`. **Recommendation: Custom plugin using the established Ktor pattern.** |

### Dependencies to Add

The `server:auth` module `build.gradle.kts` needs:
```kotlin
dependencies {
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(projects.server.core.security)
    implementation(libs.bundles.fp)           // Arrow
    implementation(libs.bundles.ktor.core)    // Ktor server + auth + content negotiation
    implementation(libs.jbcrypt)              // Password hashing
    implementation(libs.bundles.di)           // Koin

    testImplementation(libs.bundles.testing.server)
}
```

**Note on JJWT:** The `libs.bundles.jwt` (JJWT) bundle is NOT needed. The `com.auth0:java-jwt` library comes transitively via `ktor-server-auth-jwt` (which is in `libs.bundles.ktor.core`). Token creation and verification should both use the Auth0 library for consistency.

## Architecture Patterns

### Recommended Project Structure

```
server/auth/src/main/kotlin/com/m2f/server/auth/
├── tables/              # Exposed table definitions
│   ├── UsersTable.kt
│   └── RefreshTokensTable.kt
├── repository/          # Data access (suspendTransaction + DSL)
│   └── UserRepository.kt
│   └── RefreshTokenRepository.kt
├── service/             # Business logic with Raise<DomainError>
│   ├── AuthService.kt
│   └── UserService.kt
├── security/            # Token generation, password hashing
│   ├── JwtTokenProvider.kt
│   └── PasswordHasher.kt
├── routes/              # Ktor route definitions
│   ├── AuthRoutes.kt
│   └── UserRoutes.kt
├── authorization/       # RBAC plugin
│   └── RoleAuthorization.kt
├── errors/              # Auth-specific DomainError subtypes
│   └── AuthErrors.kt
└── di/                  # Koin module for auth
    └── AuthModule.kt

server/auth/src/test/kotlin/com/m2f/server/auth/
├── routes/
│   ├── AuthRoutesTest.kt
│   └── UserRoutesTest.kt
└── service/
    ├── AuthServiceTest.kt
    └── UserServiceTest.kt
```

### Pattern 1: Repository Layer with R2DBC suspendTransaction

**What:** All database access goes through repository classes that wrap Exposed DSL calls in `suspendTransaction`. Repositories return domain types (not Exposed ResultRow).
**When to use:** Every database operation.

```kotlin
// Source: Matches existing project pattern in Migration.kt
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select

class UserRepository(private val db: R2dbcDatabase) {

    suspend fun findByEmail(email: String): UserRecord? =
        suspendTransaction(db = db) {
            UsersTable
                .select(UsersTable.columns)
                .where { UsersTable.email eq email }
                .collect { row -> row.toUserRecord() }
                .singleOrNull()
        }

    suspend fun insert(email: String, passwordHash: String, name: String, role: String): UUID =
        suspendTransaction(db = db) {
            val id = UUID.randomUUID()
            UsersTable.insert {
                it[UsersTable.id] = id
                it[UsersTable.email] = email
                it[UsersTable.passwordHash] = passwordHash
                it[UsersTable.name] = name
                it[UsersTable.role] = role
            }
            id
        }
}
```

**Critical R2DBC note:** The R2DBC DSL `select` returns a `Flow`. In `suspendTransaction`, results from `select` must be collected using `.collect {}` or converted via `.toList()`. The pattern used in `MigrationsTable` (`.collect { resultRow -> ... }`) is the established project pattern.

### Pattern 2: Service Layer with Arrow Raise context

**What:** Business logic uses `context(raise: Raise<DomainError>)` to short-circuit on errors. Services compose repository calls with validation and business rules.
**When to use:** All business logic that can fail.

```kotlin
// Source: Follows established pattern from Error.kt conduit/conduitAuth
import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.m2f.core.config.server.DomainError

context(raise: Raise<DomainError>)
suspend fun register(request: RegisterRequest): AuthResponse {
    // Validate with accumulation (produces all errors at once)
    val (validEmail, validPassword, validName) = validateRegistration(request)

    // Check uniqueness
    val existing = userRepository.findByEmail(validEmail)
    ensure(existing == null) { UserAlreadyExists() }

    // Hash password and persist
    val hash = passwordHasher.hash(validPassword)
    val userId = userRepository.insert(validEmail, hash, validName, Role.USER.name)

    // Generate tokens
    return tokenProvider.generateTokenPair(userId.toString(), Role.USER.name)
}
```

### Pattern 3: Accumulated Validation with zipOrAccumulate

**What:** Registration/update validation uses Arrow's `zipOrAccumulate` to collect all field errors before failing. Follows the existing `context(raise: Raise<FieldError>)` pattern from `ValidationSupport.kt`.
**When to use:** Any multi-field validation (register, update profile).

```kotlin
// Source: Arrow zipOrAccumulate docs + existing ValidationSupport.kt pattern
import arrow.core.raise.Raise
import arrow.core.raise.zipOrAccumulate
import com.m2f.template.models.FieldError
import com.m2f.template.models.validation.validateEmail
import com.m2f.template.models.validation.validatePassword
import com.m2f.template.models.validation.validateName
import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.IncorrectInput

context(raise: Raise<DomainError>)
fun validateRegistration(request: RegisterRequest): Triple<String, String, String> {
    return raise.withError({ errors: NonEmptyList<FieldError> ->
        IncorrectInput(errors.map { InvalidFieldImpl(it.field, nonEmptyListOf(it.message)) })
    }) {
        zipOrAccumulate(
            { validateEmail(request.email) },
            { validatePassword(request.password) },
            { validateName(request.name) }
        ) { email, password, name -> Triple(email, password, name) }
    }
}
```

**Key integration point:** The existing `IncorrectInput` error type takes `NonEmptyList<InvalidField>` and has a `respond()` method that formats field-level errors into the HTTP response. The existing validators use `context(raise: Raise<FieldError>)`. Arrow's `zipOrAccumulate` internally uses `Raise<NonEmptyList<E>>`. The `withError` combinator maps from `NonEmptyList<FieldError>` to `DomainError`.

### Pattern 4: Route Handlers with conduit/conduitAuth

**What:** All route handlers use the existing `conduit` (public) or `conduitAuth` (authenticated) wrappers from `Error.kt`. These handle the Arrow Either folding and HTTP response generation.
**When to use:** Every route handler.

```kotlin
// Source: Matches existing Error.kt pattern
fun Route.authRoutes(authService: AuthService) {
    route("/api/auth") {
        post("/register") {
            conduit(HttpStatusCode.Created) {
                val request: RegisterRequest = getModel()
                authService.register(request)
            }
        }
        post("/login") {
            conduit {
                val request: LoginRequest = getModel()
                authService.login(request)
            }
        }
        post("/refresh") {
            conduit {
                val request: RefreshTokenRequest = getModel()
                authService.refresh(request)
            }
        }
        authenticate {
            post("/logout") {
                conduitAuth { userId ->
                    authService.logout(userId)
                }
            }
        }
    }
}
```

### Pattern 5: Custom RBAC Plugin with createRouteScopedPlugin

**What:** A route-scoped plugin that checks JWT role claims against required roles after authentication.
**When to use:** Any endpoint requiring role-based access control (admin-only, etc.).

```kotlin
// Source: Ktor createRouteScopedPlugin docs + AuthenticationChecked hook
// Verified pattern from: https://blog.codersee.com/secure-rest-api-ktor-role-based-authorization-rbac/
class RoleConfig {
    var roles: Set<String> = emptySet()
}

val RoleAuthorizationPlugin = createRouteScopedPlugin(
    name = "RoleAuthorizationPlugin",
    createConfiguration = ::RoleConfig
) {
    val requiredRoles = pluginConfig.roles
    on(AuthenticationChecked) { call ->
        val principal = call.principal<JWTPrincipal>()
        val userRole = principal?.payload?.getClaim("role")?.asString()
        if (userRole == null || userRole !in requiredRoles) {
            call.respond(HttpStatusCode.Forbidden,
                ErrorResponse(code = "USER_FORBIDDEN", message = "Insufficient permissions"))
        }
    }
}

fun Route.withRole(vararg roles: String, build: Route.() -> Unit) {
    install(RoleAuthorizationPlugin) { this.roles = roles.toSet() }
    build()
}
```

### Pattern 6: Koin Module Wiring

**What:** Each feature module provides a Koin module. The server's main `serverModule` aggregates them.
**When to use:** All injectable dependencies.

```kotlin
// Auth module DI
val authModule = module {
    single { PasswordHasher() }
    single { JwtTokenProvider(get<Configuration>()) }
    single { UserRepository(get<R2dbcDatabase>()) }
    single { RefreshTokenRepository(get<R2dbcDatabase>()) }
    single { AuthService(get(), get(), get()) }
    single { UserService(get()) }
}

// In ServerModule.kt - include the auth module
val serverModule = module {
    includes(authModule)
}
```

**Issue:** The current `ServerModule.kt` is a stub. The `R2dbcDatabase` instance is created in `Application.kt` via `startDatabase()` but is passed as a context parameter, NOT registered in Koin. For Koin to inject it into repositories, we need to register it:

```kotlin
// In Application.kt module(), after install(Koin)
install(Koin) {
    modules(configurationModule, serverModule)
    // Register database instance
    koin.declare(database) // where database is the R2dbcDatabase from context
}
```

Or alternatively, provide a Koin module that creates the database. The current architecture passes `R2dbcDatabase` via Kotlin context parameters. **Decision needed: context parameters vs Koin for database injection.** Recommendation: Register the database in Koin for repository injection, since repositories are Koin-managed singletons.

### Anti-Patterns to Avoid

- **DAO API with R2DBC:** The project uses `exposed-r2dbc` which ONLY supports DSL operations. Never use `Entity`, `EntityClass`, or DAO-style CRUD -- they require JDBC and blocking execution. All operations must use DSL (`Table.insert {}`, `Table.select {}`, etc.) within `suspendTransaction`.
- **Two JWT libraries:** The version catalog has both `com.auth0:java-jwt` (transitive via ktor-server-auth-jwt) and `io.jsonwebtoken:jjwt` (in `jwt` bundle). Using both creates confusion. Stick with `com.auth0:java-jwt` since it's already used in SecurityPlugin.kt.
- **try/catch for domain errors:** The project mandate (CC-01, AUTH-07) requires Arrow Raise exclusively. All error paths must use `raise()`, `ensure()`, `ensureNotNull()` -- never try/catch for domain logic.
- **Blocking bcrypt in coroutine context:** `BCrypt.hashpw()` is CPU-bound and blocking. Wrap it with `withContext(Dispatchers.Default)` to avoid blocking the event loop.
- **Storing plaintext refresh tokens:** Refresh tokens stored in the database should be hashed (or at minimum, the lookup should be done by a token ID/family, not the raw token value).

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Password hashing | Custom hash function | jBCrypt `BCrypt.hashpw()` / `BCrypt.checkpw()` | Timing attacks, salt management, work factor tuning -- bcrypt handles all of this |
| JWT creation/verification | Manual token parsing | `com.auth0.jwt.JWT.create()` + Ktor's jwt auth plugin | Token format, signature verification, claim parsing, expiry checking -- all handled |
| Accumulated validation | Manual error list building | Arrow `zipOrAccumulate` with `Raise<NonEmptyList<FieldError>>` | Accumulation semantics, short-circuit vs. collect-all -- Arrow handles correctly |
| SQL injection prevention | String interpolation in queries | Exposed DSL parameterized queries | Exposed DSL auto-parameterizes all values |
| UUID generation | Custom ID scheme | `java.util.UUID.randomUUID()` | Collision probability, format standardization |
| CORS/CSRF protection | Custom headers | Ktor `cors` and `csrf` plugins (already in ktor-security bundle) | Complex browser security rules |

**Key insight:** Every building block for Phase 2 already exists in the project's dependency tree. The work is wiring them together following established patterns, not introducing new libraries.

## Common Pitfalls

### Pitfall 1: Exposed R2DBC Select Returns Flow, Not List
**What goes wrong:** Calling `.toList()` or `.singleOrNull()` directly on a select query result outside `suspendTransaction` -- or forgetting that R2DBC select returns `Flow<ResultRow>`.
**Why it happens:** JDBC Exposed returns `Query` (iterable). R2DBC Exposed returns `Flow` which must be collected within the transaction scope.
**How to avoid:** Always use `.collect {}` or `.toList()` within `suspendTransaction`. The existing MigrationsTable code demonstrates the pattern: `MigrationsTable.select(...).collect { resultRow -> ... }`.
**Warning signs:** Compilation errors about Flow, or empty results when data exists.

### Pitfall 2: Blocking BCrypt in Coroutine Context
**What goes wrong:** `BCrypt.hashpw()` blocks the coroutine dispatcher thread (it takes ~100ms with default work factor), potentially starving the event loop.
**Why it happens:** jBCrypt is a pure Java library with no coroutine awareness.
**How to avoid:** Wrap hashing operations in `withContext(Dispatchers.Default)` or use the `Configuration.default` dispatcher already available in the project.
**Warning signs:** Slow response times under concurrent load, thread starvation warnings.

### Pitfall 3: Mixing Two JWT Libraries
**What goes wrong:** Using `io.jsonwebtoken:jjwt` for token creation but `com.auth0:java-jwt` (via Ktor) for verification. Token format incompatibilities or unnecessary dependency bloat.
**Why it happens:** Both libraries are in the version catalog. Developer picks JJWT for creation without realizing Ktor already brings Auth0's JWT.
**How to avoid:** Use `com.auth0.jwt.JWT.create()` for all token operations. The `jwt` bundle in the version catalog (JJWT) is not needed for Phase 2.
**Warning signs:** Two different JWT import prefixes in the codebase.

### Pitfall 4: Missing Error Mapping for Auth-Specific Domain Errors
**What goes wrong:** New auth-specific errors (InvalidCredentials, UserAlreadyExists, TokenExpired) are not integrated with the `DomainError.respond()` pattern, causing generic 500 errors.
**Why it happens:** `DomainError` interface requires both `toAppError()` and `respond()` implementations. Forgetting either breaks the error pipeline.
**How to avoid:** Every new auth error class must implement `DomainError`, `toAppError()` (mapping to existing `AppError.Auth.*` subtypes), and `respond()` (calling the appropriate response helper like `unauthorized()` or `unprocessable()`).
**Warning signs:** Stack traces in API responses instead of structured JSON errors.

### Pitfall 5: Refresh Token Reuse Attack
**What goes wrong:** A stolen refresh token can be used indefinitely if not properly invalidated.
**Why it happens:** Storing refresh tokens as simple strings without rotation or family tracking.
**How to avoid:** Implement refresh token rotation: each refresh request invalidates the old token and issues a new one. Store a token family ID to detect reuse (if an already-rotated token is presented, invalidate the entire family).
**Warning signs:** Security audit flags, tokens that work forever.

### Pitfall 6: R2dbcDatabase Not in Koin
**What goes wrong:** Repositories injected via Koin cannot access the database because `R2dbcDatabase` is only available as a context parameter in `Application.module()`, not registered in Koin.
**Why it happens:** Phase 1 used context parameters for database passing. Koin-managed repositories need it injected.
**How to avoid:** Register the `R2dbcDatabase` instance in Koin during application startup. Use `koin.declare(database)` or create a module factory.
**Warning signs:** Koin resolution errors at startup.

## Code Examples

### Table Definitions

```kotlin
// Source: Follows existing MigrationsTable pattern + Exposed docs
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.datetime.CurrentDateTime

private const val MAX_EMAIL_LENGTH = 255
private const val MAX_NAME_LENGTH = 100
private const val MAX_HASH_LENGTH = 255
private const val MAX_ROLE_LENGTH = 50
private const val MAX_TOKEN_LENGTH = 512

object UsersTable : Table("users") {
    val id = uuid("id")
    val email = varchar("email", MAX_EMAIL_LENGTH).uniqueIndex()
    val passwordHash = varchar("password_hash", MAX_HASH_LENGTH)
    val name = varchar("name", MAX_NAME_LENGTH)
    val role = varchar("role", MAX_ROLE_LENGTH).default("USER")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}

object RefreshTokensTable : Table("refresh_tokens") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id)
    val tokenHash = varchar("token_hash", MAX_TOKEN_LENGTH)
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val revoked = bool("revoked").default(false)

    override val primaryKey = PrimaryKey(id)
}
```

### JWT Token Generation

```kotlin
// Source: Matches existing SecurityPlugin.kt pattern (com.auth0.jwt)
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.m2f.core.config.configuration.Configuration
import java.util.Date
import java.util.UUID

class JwtTokenProvider(private val config: Configuration) {

    private val env = config.env.auth
    private val algorithm = Algorithm.HMAC256(env.secret)
    private val accessTokenExpiry = 15 * 60 * 1000L   // 15 minutes
    private val refreshTokenExpiry = 7 * 24 * 60 * 60 * 1000L // 7 days

    fun generateAccessToken(userId: String, role: String): String =
        JWT.create()
            .withAudience(env.audience)
            .withIssuer(env.issuer)
            .withSubject(userId)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiry))
            .sign(algorithm)

    fun generateRefreshToken(): String = UUID.randomUUID().toString()

    fun generateTokenPair(userId: String, role: String): AuthResponse =
        AuthResponse(
            accessToken = generateAccessToken(userId, role),
            refreshToken = generateRefreshToken(),
            expiresIn = accessTokenExpiry / 1000
        )
}
```

### Password Hashing Wrapper

```kotlin
// Source: jBCrypt docs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

private const val BCRYPT_ROUNDS = 12

class PasswordHasher {
    suspend fun hash(password: String): String =
        withContext(Dispatchers.Default) {
            BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS))
        }

    suspend fun verify(password: String, hash: String): Boolean =
        withContext(Dispatchers.Default) {
            BCrypt.checkpw(password, hash)
        }
}
```

### Auth Error Types

```kotlin
// Source: Follows existing DomainError pattern in DomainError.kt
import com.m2f.core.config.server.DomainError
import com.m2f.template.models.AppError
import io.ktor.server.routing.RoutingContext

data class InvalidCredentials(
    val msg: String = "Email or password is incorrect"
) : DomainError {
    override fun toAppError(): AppError = AppError.Auth.InvalidCredentials()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        routingContext.unauthorized(error.code, error.message)
    }
}

data class UserAlreadyExists(
    val email: String = ""
) : DomainError {
    override fun toAppError(): AppError = AppError.Auth.UserAlreadyExists()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        routingContext.unprocessable(error.code, error.message)
    }
}

data class TokenExpired(
    val msg: String = "Token has expired"
) : DomainError {
    override fun toAppError(): AppError = AppError.Auth.TokenExpired()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        routingContext.unauthorized(error.code, error.message)
    }
}

data class Forbidden(
    val msg: String = "Insufficient permissions"
) : DomainError {
    override fun toAppError(): AppError = AppError.User.Forbidden()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        // Need to add a forbidden() helper to Error.kt, similar to unauthorized()
        routingContext.call.respond(HttpStatusCode.Forbidden,
            ErrorResponse(code = error.code, message = error.message))
    }
}
```

### Database Migration for Users Table

```kotlin
// Source: Follows existing Migration interface + MigrationRegistry pattern
import com.m2f.core.database.migrations.Migration
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils

internal class CreateUsersTableMigration : Migration {
    override val version = "20260211000001"
    override val description = "Create users table"
    override suspend fun migrate() {
        SchemaUtils.create(UsersTable)
    }
}

internal class CreateRefreshTokensTableMigration : Migration {
    override val version = "20260211000002"
    override val description = "Create refresh tokens table"
    override suspend fun migrate() {
        SchemaUtils.create(RefreshTokensTable)
    }
}
```

**Migration registration issue:** The current `Migration` interface and `MigrationRegistry` are `internal` to the `server:core:database` module. The auth module cannot register its own migrations directly. Solutions:
1. Make `Migration` and `Migrations.register()` public (recommended -- migrations are a cross-module concern)
2. Use a service-loader pattern
3. Pass table references to `SchemaUtils.create()` from the main application

### Ktor Test Example

```kotlin
// Source: Ktor testApplication docs + existing testing bundle
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.kotest.matchers.shouldBe

fun testApp(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
    application {
        // Install plugins, configure test database, wire routes
    }
    block()
}

@Test
fun `register returns 201 with valid input`() = testApp {
    val client = createClient {
        install(ContentNegotiation) { json() }
    }
    val response = client.post("/api/auth/register") {
        contentType(ContentType.Application.Json)
        setBody(RegisterRequest(email = "test@example.com", password = "password123", name = "Test User"))
    }
    response.status shouldBe HttpStatusCode.Created
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Exposed JDBC `transaction {}` | Exposed R2DBC `suspendTransaction(db) {}` | Exposed 1.0.0 (Jan 2026) | Non-blocking DB access; MUST use DSL only (no DAO) |
| Exposed `org.jetbrains.exposed.sql.*` | `org.jetbrains.exposed.v1.core.*` / `.v1.r2dbc.*` | Exposed 1.0.0 (Jan 2026) | Complete package rename for v1 API |
| Arrow extension functions for Raise | Arrow context parameters `context(raise: Raise<E>)` | Arrow 2.2.0 (Nov 2025) | Cleaner syntax; project already uses this |
| Ktor 2.x `install(Authentication)` | Ktor 3.x `install(Authentication)` (same API, minor config differences) | Ktor 3.0 (2024) | Mostly compatible; auth plugins work the same |

**Deprecated/outdated:**
- `suspendTransaction()` overloads with `CoroutineContext?` parameter are deprecated in Exposed 1.0.0. Use `withContext()` externally if needed.
- `ISqlExpressionBuilder` is deprecated in Exposed 1.0.0. Use top-level functions instead.

## Open Questions

1. **R2dbcDatabase Koin registration**
   - What we know: `R2dbcDatabase` is created in `Application.kt` via `startDatabase()` and passed as a context parameter. Koin doesn't know about it.
   - What's unclear: Whether to register it via `koin.declare()` after Koin installation, or restructure to create it inside a Koin module factory.
   - Recommendation: Use `koin.declare(database)` inside `Application.module()` after `install(Koin)` -- minimal change, maximum compatibility.

2. **Migration visibility**
   - What we know: `Migration` interface and `Migrations.register()` are `internal` to `server:core:database`.
   - What's unclear: Was this intentional? Auth tables need migrations.
   - Recommendation: Make `Migration` and `Migrations.register()` public. Feature modules should own their own migrations.

3. **Refresh token storage strategy**
   - What we know: Need to store refresh tokens for invalidation (AUTH-04 logout).
   - What's unclear: Hash tokens before storage (security) vs. store plaintext (simpler query)?
   - Recommendation: Store hashed refresh tokens. Use a separate token ID (UUID) for lookup. On refresh, rotate the token and invalidate the old one.

4. **SecurityPlugin.kt enhancement**
   - What we know: The existing `configureSecurity()` validates JWT but doesn't extract the `role` claim. The `validate` block only checks audience.
   - What's unclear: Should we modify it to also validate role claim presence?
   - Recommendation: Enhance the validate block to ensure `subject` (userId) is present. Role validation happens at the route level via the RBAC plugin, not at the JWT level.

5. **Configuration for token expiry**
   - What we know: JWT secret/audience/issuer are configurable via env vars in `Env.Auth`.
   - What's unclear: Should token expiry durations also be configurable?
   - Recommendation: Add `accessTokenExpiry` and `refreshTokenExpiry` to `Env.Auth` with sensible defaults (15 min / 7 days).

## Sources

### Primary (HIGH confidence)
- [Ktor JWT Authentication Docs](https://ktor.io/docs/server-jwt.html) - JWT setup, token generation, protected routes
- [Ktor 3.4.0 What's New](https://ktor.io/docs/whats-new-340.html) - Confirmed no built-in RBAC; API key auth added
- [Exposed Transactions Docs](https://www.jetbrains.com/help/exposed/transactions.html) - suspendTransaction R2DBC patterns
- [Exposed Breaking Changes](https://www.jetbrains.com/help/exposed/breaking-changes.html) - Package renames, R2DBC API changes
- [Exposed CRUD Operations](https://www.jetbrains.com/help/exposed/dsl-crud-operations.html) - Insert, select, update, delete DSL patterns
- [Arrow Typed Errors](https://arrow-kt.io/learn/typed-errors/working-with-typed-errors/) - zipOrAccumulate, Raise, withError

### Secondary (MEDIUM confidence)
- [Codersee RBAC Tutorial](https://blog.codersee.com/secure-rest-api-ktor-role-based-authorization-rbac/) - createRouteScopedPlugin RBAC pattern (verified against Ktor docs for plugin API)
- [jBCrypt GitHub](https://github.com/jeremyh/jBCrypt) - BCrypt.hashpw/checkpw API
- [Ktor Server Testing Docs](https://ktor.io/docs/server-testing.html) - testApplication pattern

### Tertiary (LOW confidence)
- None. All findings verified against official documentation.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - All libraries already in version catalog and partially configured. Versions verified.
- Architecture: HIGH - Patterns verified against existing codebase code (SecurityPlugin.kt, Error.kt, MigrationsTable, ValidationSupport.kt). Architecture follows established conventions.
- Pitfalls: HIGH - R2DBC DSL-only constraint verified in Exposed docs. JWT library conflict observed directly in version catalog vs. imports. BCrypt blocking nature is well-documented.

**Research date:** 2026-02-11
**Valid until:** 2026-03-11 (stable libraries, no fast-moving concerns)
