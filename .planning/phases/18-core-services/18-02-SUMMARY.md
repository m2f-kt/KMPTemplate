---
phase: 18-core-services
plan: 02
subsystem: api
tags: [email, smtp, jakarta-mail, mailhog, file-upload, sdk, ktor, koin, password-reset]

# Dependency graph
requires:
  - phase: 18-core-services/01
    provides: FileResponse DTO, Files @Resource routes, FileService, FileModule
provides:
  - EmailService interface for sending emails
  - SmtpEmailService SMTP implementation using Jakarta Mail
  - emailModule Koin module for email DI
  - Password reset flow with real email delivery
  - FileApi SDK interface with uploadFile and getFileUrl
  - FileApiImpl HTTP client implementation
  - FakeFileApiBuilder for test doubles
  - Updated FakeSdkBuilder with file() DSL
affects: [19-rag-pipeline, 21-profile-features]

# Tech tracking
tech-stack:
  added: [com.sun.mail:jakarta.mail 2.0.2]
  patterns: [EmailService interface abstraction, SMTP via Jakarta Mail with Dispatchers.IO, email failure silencing for security]

key-files:
  created:
    - server/auth/src/main/kotlin/com/m2f/server/auth/service/EmailService.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/service/SmtpEmailService.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/di/EmailModule.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/FileApi.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/FileApiImpl.kt
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeFileApiBuilder.kt
  modified:
    - gradle/libs.versions.toml
    - server/auth/build.gradle.kts
    - server/auth/src/main/kotlin/com/m2f/server/auth/service/PasswordResetService.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/di/AuthModule.kt
    - server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt
    - server/src/main/kotlin/com/m2f/template/di/ServerModule.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeSdkBuilder.kt

key-decisions:
  - "Used Jakarta Mail 2.0.2 (latest available) instead of plan's 2.0.3 which doesn't exist"
  - "Added Configuration as constructor dependency to PasswordResetService for accessing baseUrl"
  - "Separate emailModule Koin module (not merged into authModule) per CONTEXT decision"

patterns-established:
  - "EmailService interface pattern: suspend fun sendEmail(to, subject, body) with fire-and-forget semantics"
  - "Email failure silencing: catch exceptions silently in security-sensitive flows (password reset)"
  - "SDK FileApi pattern: multipart upload via submitFormWithBinaryData, type-safe resource routing for GET"

# Metrics
duration: 6min
completed: 2026-02-22
---

# Phase 18 Plan 02: Email Service + Password Reset Migration + SDK FileApi Summary

**EmailService with SMTP/MailHog delivery, password reset migrated from println to real email, SDK FileApi with multipart upload and fake builder for testing**

## Performance

- **Duration:** 6 min
- **Started:** 2026-02-22T11:58:35Z
- **Completed:** 2026-02-22T12:04:42Z
- **Tasks:** 3
- **Files modified:** 15

## Accomplishments
- EmailService interface + SmtpEmailService using Jakarta Mail SMTP transport with MailHog-compatible defaults
- Password reset flow sends real emails with reset links instead of println — failures silenced for security
- SDK FileApi with multipart file upload and presigned URL retrieval, plus FakeFileApiBuilder for test support

## Task Commits

Each task was committed atomically:

1. **Task 1: EmailService Interface + SMTP Implementation + Koin Wiring** - `bba251b` (feat)
2. **Task 2: Password Reset Email Migration** - `9973a6d` (feat)
3. **Task 3: SDK FileApi + Fake Builder** - `81da6cd` (feat)

## Files Created/Modified
- `server/auth/.../service/EmailService.kt` - Email sending abstraction interface
- `server/auth/.../service/SmtpEmailService.kt` - SMTP implementation using Jakarta Mail with Dispatchers.IO
- `server/auth/.../di/EmailModule.kt` - Koin module binding EmailService to SmtpEmailService
- `server/auth/.../service/PasswordResetService.kt` - Migrated from println to emailService.sendEmail()
- `server/auth/.../di/AuthModule.kt` - Updated PasswordResetService binding with new dependencies
- `server/core/config/.../Env.kt` - Added baseUrl to Env.Http for user-facing URLs
- `server/auth/build.gradle.kts` - Added jakarta-mail dependency
- `gradle/libs.versions.toml` - Added jakarta-mail 2.0.2 version and library entry
- `server/src/.../di/ServerModule.kt` - Added includes(emailModule)
- `core/sdk/.../api/FileApi.kt` - SDK interface with uploadFile and getFileUrl
- `core/sdk/.../api/FileApiImpl.kt` - HTTP implementation using multipart upload
- `core/sdk/.../Sdk.kt` - Facade now delegates to FileApi
- `core/sdk/.../di/SdkModule.kt` - FileApiImpl wired with HttpClient
- `core/testing/.../fakes/FakeFileApiBuilder.kt` - Test double builder with fail-fast defaults
- `core/testing/.../fakes/FakeSdkBuilder.kt` - Added file() DSL block

## Decisions Made
- **Jakarta Mail version:** Used 2.0.2 instead of plan-specified 2.0.3 which doesn't exist in Maven Central
- **Configuration injection:** Added Configuration as constructor dependency to PasswordResetService for accessing `config.env.http.baseUrl` to construct reset links
- **Separate emailModule:** Created dedicated Koin module per CONTEXT decision (not merged into authModule)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Jakarta Mail version 2.0.3 doesn't exist**
- **Found during:** Task 1 (Dependency resolution)
- **Issue:** Plan specified `jakarta-mail = "2.0.3"` but Maven Central only has up to 2.0.2
- **Fix:** Changed version to 2.0.2 (latest available)
- **Files modified:** gradle/libs.versions.toml
- **Verification:** `./gradlew :server:auth:compileKotlin` succeeds
- **Committed in:** bba251b

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Minor version adjustment. No functional difference.

## Issues Encountered
None

## User Setup Required
None - MailHog SMTP configuration was already established in Phase 17 docker-compose.yml.

## Next Phase Readiness
- Email infrastructure ready for any future email-sending features (welcome emails, notifications, etc.)
- Password reset flow fully functional with real email delivery
- SDK FileApi ready for client-side file upload UI integration
- Ready for 18-03-PLAN.md (if exists) or next phase

---
*Phase: 18-core-services*
*Completed: 2026-02-22*

## Self-Check: PASSED
