---
phase: 02-server-auth-users
plan: 01
subsystem: auth
tags: [jwt, bcrypt, exposed-r2dbc, arrow-raise, koin, ktor, registration]

# Dependency graph
requires:
  - phase: 01-foundation-module-structure
    provides: "Module structure, Koin DI, Arrow validation helpers, DomainError hierarchy, conduit pattern"
provides:
  - "UsersTable and RefreshTokensTable Exposed table definitions"
  - "UserRepository and RefreshTokenRepository with R2DBC suspendTransaction"
  - "PasswordHasher wrapping BCrypt with coroutine dispatcher"
  - "JwtTokenProvider generating HMAC256 tokens via com.auth0.jwt"
  - "AuthService with register() using Arrow zipOrAccumulate for accumulated validation"
  - "POST /api/auth/register endpoint via conduit pattern"
  - "Auth-specific DomainError subtypes (InvalidCredentials, UserAlreadyExists, etc.)"
  - "AuthModule Koin module wiring all auth dependencies"
  - "Public Migration interface for feature module access"
  - "R2dbcDatabase registered in Koin for repository injection"
affects: [02-server-auth-users, 03-client-sdk-networking]

# Tech tracking
tech-stack:
  added: [jbcrypt, com.auth0.jwt (via ktor-server-auth-jwt)]
  patterns: [Arrow zipOrAccumulate for accumulated validation, context(Raise) service methods, MigrationRegistry.register() for feature module migrations, getKoin().declare() for runtime DI registration]

key-files:
  created:
    - server/auth/src/main/kotlin/com/m2f/server/auth/tables/UsersTable.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/tables/RefreshTokensTable.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/repository/UserRepository.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/repository/RefreshTokenRepository.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/security/PasswordHasher.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/security/JwtTokenProvider.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/errors/AuthErrors.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/service/AuthService.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/routes/AuthRoutes.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/di/AuthModule.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/Auth.kt
  modified:
    - server/core/database/src/main/kotlin/com/m2f/core/database/migrations/Migration.kt
    - server/core/database/src/main/kotlin/com/m2f/core/database/migrations/MigrationRegistry.kt
    - server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt
    - server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt
    - server/auth/build.gradle.kts
    - server/build.gradle.kts
    - server/src/main/kotlin/com/m2f/template/Application.kt
    - server/src/main/kotlin/com/m2f/template/di/ServerModule.kt

key-decisions:
  - "Used kotlin.uuid.Uuid (Exposed 1.0) instead of java.util.UUID for table columns and repository types"
  - "Used top-level Exposed operator imports (eq, and, greater) instead of deprecated SqlExpressionBuilder"
  - "Used kotlinx.coroutines.flow.singleOrNull() for R2DBC query result collection"
  - "Used anonymous InvalidField implementations in AuthService for mapping FieldError to IncorrectInput"
  - "Used getKoin().declare(database) for runtime R2dbcDatabase registration in DI container"
  - "Called registerAuthMigrations() in main() before startDatabase() for migration registration"

patterns-established:
  - "Repository pattern: class XRepository(private val db: R2dbcDatabase) with suspendTransaction(db = db)"
  - "Service validation: context(raise: Raise<DomainError>) with raise.withError + zipOrAccumulate for accumulated errors"
  - "Feature module migration: implement Migration interface, register via MigrationRegistry.register() before startDatabase()"
  - "Koin feature module: create val xxxModule = module { ... } and include in serverModule"

# Metrics
duration: 53min
completed: 2026-02-11
---

# Phase 02 Plan 01: Auth Foundation Summary

**Registration endpoint with BCrypt password hashing, JWT HMAC256 tokens, Arrow accumulated validation, Exposed R2DBC persistence, and Koin-wired auth module**

## Performance

- **Duration:** 53 min
- **Started:** 2026-02-11T15:57:01Z
- **Completed:** 2026-02-11T16:50:16Z
- **Tasks:** 2
- **Files modified:** 19

## Accomplishments
- Built complete auth data layer: UsersTable, RefreshTokensTable, UserRepository, RefreshTokenRepository
- Created security utilities: BCrypt PasswordHasher (async), JWT token generation with HMAC256
- Implemented AuthService.register() with Arrow zipOrAccumulate for accumulated field validation
- Wired POST /api/auth/register endpoint through Koin DI, returning 201 with access/refresh tokens
- Made Migration interface public and added MigrationRegistry.register() for feature module migration support
- Registered R2dbcDatabase in Koin for repository injection pattern

## Task Commits

Each task was committed atomically:

1. **Task 1: Infrastructure fixes, tables, repositories, and security utilities** - `3a9d897` (feat)
2. **Task 2: AuthService, registration route, Koin wiring, and Application integration** - `46b1b10` (feat)

## Files Created/Modified
- `server/auth/src/main/kotlin/com/m2f/server/auth/tables/UsersTable.kt` - Exposed Table for users (uuid PK, email unique index, passwordHash, name, role, timestamps)
- `server/auth/src/main/kotlin/com/m2f/server/auth/tables/RefreshTokensTable.kt` - Exposed Table for refresh_tokens (uuid PK, userId FK, tokenHash, expiresAt, revoked)
- `server/auth/src/main/kotlin/com/m2f/server/auth/repository/UserRepository.kt` - CRUD operations: findByEmail, findById, insert, updateProfile
- `server/auth/src/main/kotlin/com/m2f/server/auth/repository/RefreshTokenRepository.kt` - Token operations: store, findValidToken, revokeByUserId, revokeById
- `server/auth/src/main/kotlin/com/m2f/server/auth/security/PasswordHasher.kt` - BCrypt hash/verify with Dispatchers.Default
- `server/auth/src/main/kotlin/com/m2f/server/auth/security/JwtTokenProvider.kt` - HMAC256 JWT generation, SHA-256 refresh token hashing
- `server/auth/src/main/kotlin/com/m2f/server/auth/errors/AuthErrors.kt` - Auth DomainError subtypes: InvalidCredentials, UserAlreadyExists, TokenExpired, TokenInvalid, UserNotFound, Forbidden
- `server/auth/src/main/kotlin/com/m2f/server/auth/service/AuthService.kt` - register() with accumulated validation via Arrow Raise
- `server/auth/src/main/kotlin/com/m2f/server/auth/routes/AuthRoutes.kt` - POST /api/auth/register route using conduit pattern
- `server/auth/src/main/kotlin/com/m2f/server/auth/di/AuthModule.kt` - Koin module: PasswordHasher, JwtTokenProvider, UserRepository, RefreshTokenRepository, AuthService
- `server/auth/src/main/kotlin/com/m2f/server/auth/Auth.kt` - CreateUsersTableMigration, CreateRefreshTokensTableMigration, registerAuthMigrations()
- `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/Migration.kt` - Removed `internal` from Migration interface and Migrations object
- `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/MigrationRegistry.kt` - Removed `internal`, added register(migration) method
- `server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt` - Added forbidden() and notFound() response helpers
- `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt` - Added accessTokenExpiry and refreshTokenExpiry to Env.Auth
- `server/auth/build.gradle.kts` - Full dependencies: core.models, core.config, core.database, core.security, fp, ktor.core, jbcrypt, di
- `server/build.gradle.kts` - Added implementation(projects.server.auth)
- `server/src/main/kotlin/com/m2f/template/Application.kt` - registerAuthMigrations(), getKoin().declare(database), authRoutes, fixed OpenApiDocSource
- `server/src/main/kotlin/com/m2f/template/di/ServerModule.kt` - Added includes(authModule)

## Decisions Made
- Used `kotlin.uuid.Uuid` with `@OptIn(ExperimentalUuidApi::class)` for Exposed 1.0 compatibility (Exposed v1 uses Kotlin stdlib Uuid, not java.util.UUID)
- Used top-level operator imports (`org.jetbrains.exposed.v1.core.eq`, `.and`, `.greater`) instead of deprecated `SqlExpressionBuilder` members
- Used `kotlinx.coroutines.flow.singleOrNull()` for collecting R2DBC query results (R2DBC select returns Flow, not List)
- Used anonymous `InvalidField` interface implementations in `withError` mapping to avoid creating unnecessary data classes
- Used `getKoin().declare(database)` after `install(Koin)` to register the R2dbcDatabase instance (created outside Koin scope by SuspendApp lifecycle)
- Used `kotlin.time.Clock` instead of deprecated `kotlinx.datetime.Clock` for token expiry computation

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Exposed 1.0 uses kotlin.uuid.Uuid, not java.util.UUID**
- **Found during:** Task 1 (Table definitions and repository compilation)
- **Issue:** Plan specified `java.util.UUID` for ID types, but Exposed 1.0 `uuid()` column returns `kotlin.uuid.Uuid` requiring `@OptIn(ExperimentalUuidApi::class)`
- **Fix:** Changed all UUID types to `kotlin.uuid.Uuid`, added `@file:OptIn(ExperimentalUuidApi::class)` to table and repository files
- **Files modified:** UsersTable.kt, RefreshTokensTable.kt, UserRepository.kt, RefreshTokenRepository.kt, AuthService.kt
- **Verification:** `:server:auth:compileKotlin` passes
- **Committed in:** 3a9d897 (Task 1 commit)

**2. [Rule 1 - Bug] Exposed 1.0 deprecated SqlExpressionBuilder operators**
- **Found during:** Task 1 (Repository compilation)
- **Issue:** `SqlExpressionBuilder.eq` is deprecated in Exposed 1.0.0; top-level functions replace it
- **Fix:** Changed imports to `org.jetbrains.exposed.v1.core.eq`, `.and`, `.greater`
- **Files modified:** UserRepository.kt, RefreshTokenRepository.kt
- **Verification:** `:server:auth:compileKotlin` passes without deprecation errors
- **Committed in:** 3a9d897 (Task 1 commit)

**3. [Rule 1 - Bug] R2DBC query results are Flow, not List**
- **Found during:** Task 1 (Repository compilation)
- **Issue:** Plan specified `.toList()` for R2DBC query collection, but R2DBC `select()` returns `Flow<ResultRow>` which doesn't have `.toList()` directly
- **Fix:** Used `kotlinx.coroutines.flow.singleOrNull()` extension for single-result queries
- **Files modified:** UserRepository.kt, RefreshTokenRepository.kt
- **Verification:** `:server:auth:compileKotlin` passes
- **Committed in:** 3a9d897 (Task 1 commit)

**4. [Rule 3 - Blocking] Pre-existing OpenAPISource reference broken in Ktor 3.4.0**
- **Found during:** Task 2 (Application.kt compilation)
- **Issue:** `io.ktor.server.plugins.openapi.OpenAPISource` does not exist in Ktor 3.4.0 (pre-existing bug, was never compiling)
- **Fix:** Replaced with `io.ktor.server.routing.openapi.OpenApiDocSource.File("openapi/generated-api.json")`
- **Files modified:** Application.kt
- **Verification:** `:server:compileKotlin` passes
- **Committed in:** 46b1b10 (Task 2 commit)

---

**Total deviations:** 4 auto-fixed (3 bugs, 1 blocking)
**Impact on plan:** All fixes necessary for compilation. Exposed 1.0 API differences from plan were the main source of deviation. No scope creep.

## Issues Encountered
- Exposed 1.0 has significant API changes from earlier versions: Kotlin stdlib UUID instead of java.util.UUID, top-level operator functions instead of SqlExpressionBuilder members, and R2DBC returning Flow instead of List. All three required multiple compilation-fix iterations.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Registration endpoint complete, ready for Plan 02-02 (login, refresh, logout endpoints)
- Auth module DI is wired and extensible via authModule includes
- Migration system is public and feature-module-accessible for future table additions
- R2dbcDatabase in Koin enables any future repository to get<R2dbcDatabase>()

## Self-Check: PASSED

All 12 created files verified present. Both task commits (3a9d897, 46b1b10) verified in git log. `:server:compileKotlin` passes.

---
*Phase: 02-server-auth-users*
*Completed: 2026-02-11*
