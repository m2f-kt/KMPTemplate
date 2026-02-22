---
phase: 18-core-services
plan: 01
subsystem: api
tags: [s3, minio, file-upload, multipart, aws-sdk-kotlin, ktor, koin]

# Dependency graph
requires:
  - phase: 17-infrastructure-foundation
    provides: MinIO S3 config in Env.S3, Docker Compose setup
provides:
  - FileResponse DTO in core:models for shared use
  - Files @Resource route definitions for client SDK
  - AppError.File error hierarchy (TooLarge, UnsupportedType, UploadFailed, NotFound)
  - FileService with S3 upload and presigned URL generation
  - FileRoutes with authenticated multipart upload endpoint
  - FileModule Koin wiring for S3Client and FileService
  - HTTP status helpers payloadTooLarge() and unsupportedMediaType() in Error.kt
affects: [19-rag-pipeline, 21-profile-features]

# Tech tracking
tech-stack:
  added: [aws.sdk.kotlin:s3 1.4.45, aws.smithy.kotlin:runtime-core 1.4.12]
  patterns: [multipart file upload via Ktor 3.x ByteReadChannel.toByteArray(), S3Client with forcePathStyle for MinIO]

key-files:
  created:
    - core/models/src/commonMain/kotlin/com/m2f/template/models/dto/FileDtos.kt
    - server/files/build.gradle.kts
    - server/files/src/main/kotlin/com/m2f/server/files/errors/FileErrors.kt
    - server/files/src/main/kotlin/com/m2f/server/files/service/FileService.kt
    - server/files/src/main/kotlin/com/m2f/server/files/routes/FileRoutes.kt
    - server/files/src/main/kotlin/com/m2f/server/files/di/FileModule.kt
  modified:
    - gradle/libs.versions.toml
    - core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt
    - server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt
    - settings.gradle.kts
    - server/build.gradle.kts
    - server/src/main/kotlin/com/m2f/template/Application.kt
    - server/src/main/kotlin/com/m2f/template/di/ServerModule.kt

key-decisions:
  - "Used ByteReadChannel.toByteArray() for Ktor 3.x multipart file reading instead of deprecated readRemaining().readBytes()"
  - "Used arrow.core.raise.context.ensureNotNull for context-parameter-based Raise (not extension function form)"
  - "Moved multipart parsing outside conduitAuth block since RoutingContext.call is not available inside the context(Raise) block"
  - "Used kotlin.time.Clock instead of kotlinx.datetime.Clock (no kotlinx-datetime dependency in files module)"

patterns-established:
  - "S3 file upload pattern: validate size/type → generate UUID key with userId prefix → putObject → return FileResponse"
  - "Multipart route pattern: parse multipart outside conduitAuth, validate and process inside conduitAuth"

# Metrics
duration: 16min
completed: 2026-02-22
---

# Phase 18 Plan 01: S3 File Upload Service Summary

**S3-backed file upload service with multipart handling, size/type validation, and MinIO-compatible S3Client using AWS SDK for Kotlin**

## Performance

- **Duration:** 16 min
- **Started:** 2026-02-22T11:39:39Z
- **Completed:** 2026-02-22T11:56:10Z
- **Tasks:** 2
- **Files modified:** 14

## Accomplishments
- Complete server/files module with FileService (S3 upload with size/type validation), FileRoutes (multipart upload endpoint), FileErrors, and FileModule (Koin DI)
- Shared models: FileResponse DTO, Files @Resource routes, AppError.File error hierarchy — available to all KMP targets
- Full application wiring: ServerModule includes fileModule, Application.kt routes fileRoutes, server depends on server:files

## Task Commits

Each task was committed atomically:

1. **Task 1: Shared Models + Server Files Module Skeleton** - `bb2d7a0` (feat)
2. **Task 2: File Routes + DI Wiring + Application Integration** - `ddeae03` (feat)

## Files Created/Modified
- `core/models/.../dto/FileDtos.kt` - FileResponse DTO (id, key, originalName, contentType, size, url, createdAt)
- `core/models/.../routes/ApiRoutes.kt` - Added Files @Resource class with Upload and Get sub-routes
- `core/models/.../AppError.kt` - Added sealed class File with TooLarge, UnsupportedType, UploadFailed, NotFound
- `gradle/libs.versions.toml` - Added aws-s3 1.4.45 and aws-smithy 1.4.12 dependencies
- `settings.gradle.kts` - Added include("server:files")
- `server/files/build.gradle.kts` - New module build config with S3 + Ktor + Arrow dependencies
- `server/files/.../errors/FileErrors.kt` - 4 DomainError implementations with i18n respond()
- `server/files/.../service/FileService.kt` - S3 upload (10MB limit, content type whitelist) + presigned URL
- `server/files/.../routes/FileRoutes.kt` - Authenticated multipart upload (POST) and file retrieval (GET)
- `server/files/.../di/FileModule.kt` - Koin wiring for S3Client (MinIO forcePathStyle) and FileService
- `server/core/config/.../Error.kt` - Added payloadTooLarge() and unsupportedMediaType() HTTP helpers
- `server/src/.../di/ServerModule.kt` - Added includes(fileModule)
- `server/src/.../Application.kt` - Added fileRoutes(fileService) wiring
- `server/build.gradle.kts` - Added projects.server.files dependency

## Decisions Made
- **Ktor 3.x multipart API:** Used `ByteReadChannel.toByteArray()` instead of deprecated `readRemaining().readBytes()` — Ktor 3.x changed `FileItem.provider()` to return `ByteReadChannel` instead of `ByteReadPacket`
- **Arrow context parameters:** Used `arrow.core.raise.context.ensureNotNull` (context-parameter variant) instead of `arrow.core.raise.ensureNotNull` (extension function variant) since `conduitAuth` block uses `context(Raise<DomainError>)` not `Raise<DomainError>` receiver
- **Multipart parsing location:** Moved multipart file reading outside `conduitAuth` block because `RoutingContext.call` is not accessible inside the `context(Raise<DomainError>)` block — validation and business logic remain inside `conduitAuth`
- **Clock:** Used `kotlin.time.Clock` instead of `kotlinx.datetime.Clock` since files module doesn't depend on kotlinx-datetime

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Fixed Ktor 3.x multipart byte reading API**
- **Found during:** Task 2 (FileRoutes implementation)
- **Issue:** Plan specified `part.provider().readRemaining().readByteArray()` which doesn't compile — Ktor 3.x `FileItem.provider()` returns `ByteReadChannel` where `readRemaining` is not available as the plan expected
- **Fix:** Used `part.provider().toByteArray()` — the correct Ktor 3.x suspend function for reading all bytes from `ByteReadChannel`
- **Files modified:** server/files/src/main/kotlin/com/m2f/server/files/routes/FileRoutes.kt
- **Verification:** `./gradlew :server:compileKotlin` succeeds
- **Committed in:** ddeae03

**2. [Rule 3 - Blocking] Fixed ensureNotNull context parameter resolution**
- **Found during:** Task 2 (FileRoutes implementation)
- **Issue:** `arrow.core.raise.ensureNotNull` is an extension function on `Raise<Error>` receiver, but `conduitAuth` block provides `Raise<DomainError>` as a context parameter — compiler could not resolve the extension
- **Fix:** Changed import to `arrow.core.raise.context.ensureNotNull` (the context-parameter-aware variant)
- **Files modified:** server/files/src/main/kotlin/com/m2f/server/files/routes/FileRoutes.kt
- **Verification:** `./gradlew :server:compileKotlin` succeeds
- **Committed in:** ddeae03

**3. [Rule 3 - Blocking] Moved multipart parsing outside conduitAuth block**
- **Found during:** Task 2 (FileRoutes implementation)
- **Issue:** `call.receiveMultipart()` requires `RoutingContext` but the `conduitAuth` block only provides `Raise<DomainError>` context — `call` is inaccessible inside the block
- **Fix:** Moved multipart forEachPart parsing before `conduitAuth`, kept validation and `fileService.upload()` inside the auth block
- **Files modified:** server/files/src/main/kotlin/com/m2f/server/files/routes/FileRoutes.kt
- **Verification:** `./gradlew :server:compileKotlin` succeeds
- **Committed in:** ddeae03

**4. [Rule 3 - Blocking] Replaced kotlinx.datetime.Clock with kotlin.time.Clock**
- **Found during:** Task 1 (FileService implementation)
- **Issue:** server/files module doesn't depend on kotlinx-datetime, but FileService needs timestamps
- **Fix:** Used `kotlin.time.Clock.System.now()` with `@OptIn(ExperimentalTime::class)` annotation
- **Files modified:** server/files/src/main/kotlin/com/m2f/server/files/service/FileService.kt
- **Verification:** `./gradlew :server:files:compileKotlin` succeeds
- **Committed in:** bb2d7a0

---

**Total deviations:** 4 auto-fixed (4 blocking issues)
**Impact on plan:** All auto-fixes necessary for compilation. The plan's Ktor 3.x multipart API calls and Arrow context parameter usage were incorrect for the actual library versions. No scope creep.

## Issues Encountered
None beyond the deviations documented above.

## User Setup Required
None - no external service configuration required. MinIO S3 configuration was already established in Phase 17.

## Next Phase Readiness
- File upload endpoint is ready for integration testing with MinIO
- FileResponse DTO and Files routes are available in core:models for SDK client generation
- FileService provides the foundation for RAG document ingestion (Phase 19)
- The presigned URL mechanism enables secure file access for profile features (Phase 21)

---
*Phase: 18-core-services*
*Completed: 2026-02-22*
