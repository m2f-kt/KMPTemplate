---
phase: 18-core-services
plan: 03
subsystem: testing
tags: [integration-tests, testcontainers, minio, greenmail, s3, smtp, file-upload, email]

# Dependency graph
requires:
  - phase: 18-core-services/01
    provides: FileService, FileRoutes, FileModule, FileResponse DTO
  - phase: 18-core-services/02
    provides: SmtpEmailService, EmailService interface, emailModule
provides:
  - FileRoutesTest with 5 integration tests verifying file upload end-to-end
  - TestMinIO singleton for S3-compatible container lifecycle
  - EmailServiceTest with 2 integration tests verifying SMTP delivery
  - TestHelpers (fileTestApp, createTestBucket, createTestToken, createTestUser)
affects: []

# Tech tracking
tech-stack:
  added: [com.icegreen:greenmail 2.1.3]
  patterns: [GenericContainer for MinIO S3, GreenMail in-JVM SMTP, fileTestApp Koin+Ktor test harness]

key-files:
  created:
    - server/files/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
    - server/files/src/test/kotlin/com/m2f/server/files/FileRoutesTest.kt
    - server/auth/src/test/kotlin/com/m2f/server/auth/EmailServiceTest.kt
  modified:
    - server/files/build.gradle.kts
    - server/auth/build.gradle.kts
    - gradle/libs.versions.toml

key-decisions:
  - "GreenMail 2.1.3 (latest) instead of plan's 2.1.2"
  - "URL-encode fileKey in round-trip test since key contains '/' from userId prefix"
  - "testImplementation for server:core:database and server:core:security (not in plan but required for compilation)"

patterns-established:
  - "TestMinIO singleton: GenericContainer(minio/minio:latest) with S3 API on port 9000"
  - "fileTestApp pattern: Koin + auth migrations + security + file routes (mirrors groupTestApp)"
  - "GreenMail random port pattern: ServerSetup(0, null, PROTOCOL_SMTP) for conflict-free tests"

# Metrics
duration: 10min
completed: 2026-02-22
---

# Phase 18 Plan 03: Integration Tests (File Upload + Email Delivery) Summary

**File upload integration tests with MinIO via Testcontainers (5 tests) and SMTP email delivery tests with GreenMail (2 tests), verifying end-to-end correctness of both core services**

## Performance

- **Duration:** 10 min
- **Started:** 2026-02-22T12:06:43Z
- **Completed:** 2026-02-22T12:17:22Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- FileRoutesTest: 5 integration tests covering upload success, 413 size rejection, 415 type rejection, S3 round-trip content match, and 401 auth required
- TestMinIO singleton managing MinIO container lifecycle following TestDatabase pattern from groups module
- EmailServiceTest: 2 tests verifying SMTP email delivery with correct recipient/subject/body and from-address
- fileTestApp helper providing full Koin+Ktor test harness with database, auth, security, and file routes

## Task Commits

Each task was committed atomically:

1. **Task 1: File Upload Integration Tests (TestMinIO + FileRoutesTest)** - `7ae08ba` (test)
2. **Task 2: Email Delivery Integration Test** - `ad17ce2` (test)

## Files Created/Modified
- `server/files/src/test/.../TestHelpers.kt` - TestDatabase, TestMinIO, createTestToken, createTestUser, createTestBucket, testConfiguration, fileTestApp
- `server/files/src/test/.../FileRoutesTest.kt` - 5 integration tests for file upload routes
- `server/auth/src/test/.../EmailServiceTest.kt` - 2 integration tests for SmtpEmailService via GreenMail
- `server/files/build.gradle.kts` - Added testImplementation deps for database, security, testing bundle, client content-negotiation
- `server/auth/build.gradle.kts` - Added testImplementation for GreenMail
- `gradle/libs.versions.toml` - Added greenmail 2.1.3 version and library entry

## Decisions Made
- **GreenMail version:** Used 2.1.3 (latest) instead of plan-specified 2.1.2
- **URL encoding:** fileKey contains '/' (userId/fileId.ext format) so round-trip GET test URL-encodes the key
- **Test dependencies:** Added server:core:database and server:core:security as testImplementation to files module (needed for R2dbcDatabase, Migrations, configureSecurity used in test harness)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Missing test dependencies for database/security modules**
- **Found during:** Task 1 (compilation)
- **Issue:** TestHelpers.kt imports R2dbcDatabase, Migrations, configureSecurity which live in server:core:database and server:core:security — not transitive from server:core:config
- **Fix:** Added testImplementation(projects.server.core.database) and testImplementation(projects.server.core.security) to server/files/build.gradle.kts
- **Files modified:** server/files/build.gradle.kts
- **Committed in:** 7ae08ba

**2. [Rule 1 - Bug] fileKey URL encoding in round-trip test**
- **Found during:** Task 1 (test execution — 404 on GET)
- **Issue:** FileService generates keys as `userId/fileId.ext` — the `/` in the key breaks Ktor's `{fileKey}` path parameter matching, returning 404
- **Fix:** URL-encode the fileKey with `URLEncoder.encode(key, "UTF-8")` before using in GET path
- **Files modified:** server/files/src/test/.../FileRoutesTest.kt
- **Committed in:** 7ae08ba

---

**Total deviations:** 2 auto-fixed (1 blocking, 1 bug)
**Impact on plan:** Minor additions. All planned functionality delivered.

## Issues Encountered
None

## User Setup Required
None - Docker must be running for Testcontainers (MinIO). GreenMail runs in-JVM.

## Phase 18 Completion

This was the final plan (3 of 3) in Phase 18 — Core Services. All phase success criteria satisfied:
- Plan 01: FileService + FileRoutes with S3/MinIO integration
- Plan 02: EmailService + SMTP delivery + password reset migration + SDK FileApi
- Plan 03: Integration tests verifying file upload round-trip and email delivery

---
*Phase: 18-core-services*
*Completed: 2026-02-22*

## Self-Check: PASSED
