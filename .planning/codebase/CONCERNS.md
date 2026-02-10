# Codebase Concerns

**Analysis Date:** 2026-02-10

## Security Considerations

**Hardcoded JWT Secret:**
- Risk: JWT secret is hardcoded in source code instead of environment-only configuration
- Files: `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt`
- Current mitigation: Default value can be overridden via `JWT_SECRET` environment variable
- Recommendations:
  - Make JWT_SECRET mandatory (no fallback default)
  - Add startup validation to ensure secret is loaded from environment
  - Document that this file should never contain production secrets
  - Consider adding a pre-commit hook to prevent secret commits

**Hardcoded Database Credentials:**
- Risk: PostgreSQL credentials (username "postgres", password "postgres") are hardcoded as defaults
- Files: `server/core/database/src/main/kotlin/com/m2f/core/database/connection/DataSource.kt`
- Current mitigation: Can be overridden via PGUSER and PGPASSWORD environment variables
- Recommendations:
  - Make database credentials mandatory from environment variables
  - Fail fast at startup if credentials are not provided from environment
  - Use different defaults or require explicit configuration in production

**Suppressed ForbiddenComment Warning:**
- Risk: Security plugin uses `@Suppress("ForbiddenComment")` to bypass detekt rules that prevent TODO/FIXME in code
- Files: `server/core/security/src/main/kotlin/com/m2f/core/security/SecurityPlugin.kt` (line 12)
- Current mitigation: None - suppression allows forbidden patterns
- Recommendations:
  - Remove suppression and address the underlying detekt rule violation
  - Configure detekt to allow this specific case if legitimate, or refactor to comply

## Logging & Observability Issues

**Debug Println Statements in Production Code:**
- Problem: Migration execution uses `println()` instead of proper logging framework
- Files: `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/Migration.kt` (lines 66, 75)
- Impact: Cannot control log verbosity in production, output not captured by standard logging infrastructure
- Improvement path: Replace `println()` with configured logger (e.g., slf4j, logback already in dependencies)
- Priority: High

## Architectural Concerns

**Empty Migration Registry:**
- Problem: Migration registry exists but contains no actual migrations
- Files: `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/MigrationRegistry.kt`
- Impact: Database schema changes cannot be tracked or version-controlled
- Safe modification: Add migration registration mechanism that enforces version ordering
- Recommendations:
  - Create concrete migration classes for each schema change
  - Add validation that migration versions are chronologically ordered
  - Document migration naming convention (YYYYMMDDHHMMSS recommended)

**Minimal API Coverage:**
- Problem: Only one endpoint implemented (`GET /amazing`) in application module
- Files: `server/src/main/kotlin/com/m2f/template/Application.kt`
- Impact: Most API functionality is missing; routing infrastructure exists but endpoints not defined
- Safe modification: Gradually add new endpoints following established error handling patterns

**Missing Test Coverage for Server Core Modules:**
- Problem: Only one trivial test exists (shared module), no server-side tests
- Files: `shared/src/commonTest/kotlin/com/m2f/template/SharedCommonTest.kt` (tests "1 + 2 = 3")
- Impact: Core functionality (database, security, config, error handling) is untested
- Risk: Database migrations, authentication, error responses cannot be validated
- Test coverage gaps:
  - `server/core/security/` - No JWT validation tests
  - `server/core/database/` - No migration execution tests
  - `server/core/config/` - No configuration loading tests
  - Error handling patterns (`conduit`, `conduitAuth`) - No integration tests

## Fragile Areas

**Error Handling via Arrow Continuations:**
- Files: `server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt`
- Why fragile: Uses advanced Arrow Kotlin library features (context receivers, Raise) that may be difficult for new developers to modify safely
- Safe modification:
  - Write comprehensive tests before making changes
  - Refer to Arrow documentation on Raise pattern
  - Test all error paths (validation errors, authorization, missing parameters)
- Test coverage: No tests found validating error handling behavior

**Database Initialization Chain:**
- Files: `server/src/main/kotlin/com/m2f/template/Application.kt`, `server/core/database/src/main/kotlin/com/m2f/core/database/Startup.kt`
- Why fragile: Complex dependency chain with context parameters and resource scope
- Issues:
  - Failure in migration execution could leave database in inconsistent state
  - No rollback mechanism for failed migrations
  - Migration logging uses println (no proper error handling)
- Safe modification: Add transaction boundaries and error recovery tests

**Configuration Loading with Defaults:**
- Files: `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt`, `server/core/database/src/main/kotlin/com/m2f/core/database/connection/DataSource.kt`
- Why fragile: Multiple layers of defaults make it unclear what configuration is actually being used
- Issues:
  - Env.kt provides JWT defaults
  - DataSource.kt provides database defaults
  - No centralized configuration validation
  - No startup logging of which configuration was actually loaded
- Safe modification: Add configuration audit logging at startup to verify what values are actually in use

## Test Coverage Gaps

**Security Module Untested:**
- What's not tested: JWT validation, authentication failure scenarios, principal extraction
- Files: `server/core/security/src/main/kotlin/com/m2f/core/security/SecurityPlugin.kt`
- Risk: Authentication bypass bugs could go undetected
- Priority: High

**Database Migrations Untested:**
- What's not tested: Migration execution, migration ordering, idempotency, failure handling
- Files: `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/`
- Risk: Schema corruption or data loss on failed migrations
- Priority: High

**Configuration Loading Untested:**
- What's not tested: Environment variable parsing, default value fallback, validation
- Files: `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/`
- Risk: Misconfigured deployments may fail silently with wrong defaults
- Priority: Medium

**Error Response Format Untested:**
- What's not tested: GenericErrorModel serialization, DomainError implementations, HTTP status codes
- Files: `server/core/config/src/main/kotlin/com/m2f/core/config/server/`
- Risk: Error responses may not serialize correctly or contain unexpected format
- Priority: Medium

## Known Issues

**Detekt Configuration Not Fully Applied:**
- Issue: Detekt config enforces no TODO/FIXME comments but suppression exists in code
- Files: `build-config/detekt-config.yml` (lines 581-586), `server/core/security/src/main/kotlin/com/m2f/core/security/SecurityPlugin.kt` (line 12)
- Current state: Rules exist but are bypassed
- Recommendation: Either remove suppression or document why it's necessary

## Performance Considerations

**No Caching Strategy:**
- Current state: No caching layer implemented (database, HTTP, or in-memory)
- Impact: All requests hit database directly
- Scaling path: Add caching for frequently accessed data once patterns emerge

**Synchronous Database URL Parsing:**
- Problem: Database URL regex parsing happens on every DataSource instantiation
- Files: `server/core/database/src/main/kotlin/com/m2f/core/database/connection/DataSource.kt` (lines 31-37)
- Impact: Minor performance issue during startup, repetitive regex compilation
- Improvement path: Cache or move URL parsing to companion object

---

*Concerns audit: 2026-02-10*
