---
phase: 01-foundation-module-structure
plan: 02
subsystem: infra
tags: [kmp, gradle, sealed-class, serialization, error-handling, arrow, multiplatform]

requires:
  - phase: 01-01
    provides: "kmp-library-convention and server-module-convention plugins, version catalog with Arrow 2.2.0"
provides:
  - ":core:models KMP module with AppError sealed hierarchy and shared DTOs"
  - ":core:sdk and :core:storage KMP stub modules with correct target configuration"
  - ":server:auth and :server:ai server feature module stubs"
  - ":app:auth and :app:dashboard Compose multiplatform module stubs"
  - "DomainError.toAppError() mapping from server errors to shared types"
  - "ErrorResponse DTO replacing GenericErrorModel as API error format"
affects: [01-03, 01-04, 02-auth-endpoints, 03-client-sdk, 04-compose-ui, 05-setup-cli, 06-ai-agents]

tech-stack:
  added: [kotlinx-serialization-json-1.8.1]
  patterns: [sealed-hierarchy-with-structured-codes, shared-dto-api-contract, toAppError-mapping]

key-files:
  created:
    - core/models/build.gradle.kts
    - core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/dto/AuthDtos.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/dto/UserDtos.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/dto/ErrorResponse.kt
    - core/sdk/build.gradle.kts
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt
    - core/storage/build.gradle.kts
    - core/storage/src/commonMain/kotlin/com/m2f/template/storage/Storage.kt
    - server/auth/build.gradle.kts
    - server/auth/src/main/kotlin/com/m2f/server/auth/Auth.kt
    - server/ai/build.gradle.kts
    - server/ai/src/main/kotlin/com/m2f/server/ai/Ai.kt
    - app/auth/build.gradle.kts
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/AuthScreen.kt
    - app/dashboard/build.gradle.kts
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardScreen.kt
  modified:
    - settings.gradle.kts
    - gradle/libs.versions.toml
    - server/build.gradle.kts
    - server/core/config/build.gradle.kts
    - server/core/config/src/main/kotlin/com/m2f/core/config/server/DomainError.kt
    - server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt
  deleted:
    - server/core/config/src/main/kotlin/com/m2f/core/config/server/GenericErrorModel.kt
    - server/core/config/src/main/kotlin/com/m2f/core/config/server/GenericErrorModelErrors.kt

key-decisions:
  - "Used id(com.android.library) instead of alias(libs.plugins.androidLibrary) because AGP is on buildSrc classpath"
  - "Error response functions (unexpected, unprocessable, unauthorized) now take code+message parameters instead of just message, for structured error codes"
  - "DomainError.toAppError() added as interface method so every server error maps to the shared hierarchy"

patterns-established:
  - "AppError sealed hierarchy: nested sealed subtypes (Auth, Validation, User, Server, Client) with structured DOMAIN_SPECIFIC_ERROR codes"
  - "Shared DTOs in :core:models are the API contract between server and client"
  - "Server DomainError.toAppError() converts server-side errors to shared types for API responses"
  - "ErrorResponse(code, message, errors) is the standard API error body format"

duration: ~16min
completed: 2026-02-10
---

# Plan 01-02: Module Structure & Shared Models Summary

**AppError sealed hierarchy with 5 error domains and shared DTOs in :core:models, plus 6 stub modules establishing the full project module map**

## Performance

- **Duration:** ~16 min
- **Started:** 2026-02-10T20:20:40Z
- **Completed:** 2026-02-10T20:37:27Z
- **Tasks:** 2
- **Files modified:** 25 (17 created, 6 modified, 2 deleted)

## Accomplishments
- Created :core:models KMP module compiling on Android, iOS, JVM, and WASM with AppError sealed hierarchy (Auth, Validation, User, Server, Client subtypes)
- Added shared DTOs (LoginRequest, RegisterRequest, AuthResponse, RefreshTokenRequest, UserResponse, UpdateProfileRequest, ErrorResponse) as the server-client API contract
- Created 6 module stubs (:core:sdk, :core:storage, :server:auth, :server:ai, :app:auth, :app:dashboard) establishing full project module boundaries
- Replaced GenericErrorModel with shared ErrorResponse DTO and added DomainError.toAppError() for typed server-to-shared error mapping
- Added kotlinx-serialization-json 1.8.1 to the version catalog for multiplatform serialization

## Task Commits

Each task was committed atomically:

1. **Task 1: Create core:models module with AppError hierarchy and DTOs** - `5e7f1ee` (feat)
2. **Task 2: Create sdk/storage stubs and update server DomainError to use AppError** - `a924c8d` (feat)

## Files Created/Modified
- `settings.gradle.kts` - Added 7 new module includes (core:models/sdk/storage, server:auth/ai, app:auth/dashboard)
- `gradle/libs.versions.toml` - Added kotlinx-serialization version and library entry
- `core/models/build.gradle.kts` - KMP library targeting Android, iOS, JVM, WASM with Arrow Core and kotlinx-serialization
- `core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt` - Sealed error hierarchy with Auth, Validation, User, Server, Client subtypes
- `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/ErrorResponse.kt` - Standard API error response body
- `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/AuthDtos.kt` - Login, Register, AuthResponse, RefreshToken DTOs
- `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/UserDtos.kt` - UserResponse and UpdateProfileRequest DTOs
- `core/sdk/build.gradle.kts` - KMP stub module for client networking
- `core/storage/build.gradle.kts` - KMP stub module for local persistence
- `server/auth/build.gradle.kts` - Server auth feature module stub
- `server/ai/build.gradle.kts` - Server AI feature module stub
- `app/auth/build.gradle.kts` - Compose multiplatform auth UI stub
- `app/dashboard/build.gradle.kts` - Compose multiplatform dashboard UI stub
- `server/core/config/build.gradle.kts` - Added api(projects.core.models) dependency
- `server/build.gradle.kts` - Added implementation(projects.core.models) dependency
- `server/core/config/src/main/kotlin/com/m2f/core/config/server/DomainError.kt` - Added toAppError() to DomainError, mapped all subtypes to AppError
- `server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt` - Replaced GenericErrorModel with ErrorResponse, updated error functions to use code+message

## Decisions Made
- Used `id("com.android.library")` instead of `alias(libs.plugins.androidLibrary)` because AGP is already on the buildSrc classpath (same pattern as composeApp)
- Error response helper functions (unexpected, unprocessable, unauthorized) now accept structured code + message parameters to support AppError codes
- DomainError.toAppError() is an interface method ensuring every server error type maps to the shared hierarchy

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Fixed AGP plugin resolution conflict**
- **Found during:** Task 1 (core:models build file)
- **Issue:** `alias(libs.plugins.androidLibrary)` failed because AGP was already on the buildSrc classpath with unknown version
- **Fix:** Changed to `id("com.android.library")` matching the composeApp pattern
- **Files modified:** `core/models/build.gradle.kts` (and applied same fix to all KMP modules)
- **Verification:** All modules compile successfully
- **Committed in:** 5e7f1ee (Task 1 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Necessary fix for Gradle plugin resolution. No scope creep.

## Issues Encountered
- `./gradlew assemble` failed on `composeApp:linkReleaseFrameworkIosArm64` -- this is a pre-existing iOS linking issue in the composeApp module unrelated to this plan's changes. All new modules compile correctly when targeted individually.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- :core:models provides the shared type foundation for all subsequent phases
- Module stubs are ready for Plan 03 (Arrow Raise patterns) and Plan 04 (DI)
- Server auth module stub ready for Phase 2 (auth endpoints)
- App UI stubs ready for Phase 4 (Compose UI)

## Self-Check: PASSED

All 17 created files verified on disk. Both task commits (5e7f1ee, a924c8d) verified in git log. GenericErrorModel.kt and GenericErrorModelErrors.kt confirmed deleted.

---
*Plan: 01-02-foundation-module-structure*
*Completed: 2026-02-10*
