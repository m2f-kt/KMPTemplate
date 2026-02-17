---
phase: 05-auth-screens-dashboard-setup-cli
plan: 01
subsystem: auth
tags: [oauth, password-reset, sealed-class, ktor-oauth, user-tier, dto]

# Dependency graph
requires:
  - phase: 02-server-auth-user-api
    provides: AuthService, UserRepository, RefreshTokenRepository, JwtTokenProvider, PasswordHasher
  - phase: 03-client-sdk-networking
    provides: AuthApi, apiCall, TokenStorage
provides:
  - UserTier sealed class with 5 tiers and fromString() conversion in core:models
  - RegisterRequest with firstName/lastName fields (replacing single name)
  - ForgotPasswordRequest and ResetPasswordRequest DTOs
  - UserResponse.tier extension property mapping role string to sealed type
  - OAuth endpoints for Google and Apple (server-side authorization code flow)
  - Password reset flow (forgot-password + reset-password) with token table
  - PasswordResetService with Arrow Raise error handling
  - OAuthService with Google userinfo and Apple id_token decoding
  - SDK forgotPassword() and resetPassword() functions
  - OAuth env vars (GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, APPLE_CLIENT_ID, etc.)
affects: [05-02, 05-03, 05-04, 05-05, 05-06, 05-07]

# Tech tracking
tech-stack:
  added: [ktor-client-cio (server-side for OAuth), ktor-client-content-negotiation (server-side)]
  patterns: [server-side OAuth with Ktor oauth provider, password reset token-based flow, sealed class for user tiers]

key-files:
  created:
    - core/models/src/commonMain/kotlin/com/m2f/template/models/UserTier.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/tables/PasswordResetTokensTable.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/repository/PasswordResetTokenRepository.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/service/OAuthService.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/service/PasswordResetService.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/routes/OAuthRoutes.kt
  modified:
    - core/models/src/commonMain/kotlin/com/m2f/template/models/dto/AuthDtos.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/dto/UserDtos.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/service/AuthService.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/routes/AuthRoutes.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/di/AuthModule.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/Auth.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/repository/UserRepository.kt
    - server/src/main/kotlin/com/m2f/template/Application.kt
    - server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt

key-decisions:
  - "UserTier is a sealed class (not enum) with fromString() -- exhaustive when expressions enforce tier handling"
  - "RegisterRequest uses firstName/lastName; server concatenates to name for storage"
  - "UserResponse.tier extension property bridges wire format (String role) to sealed type"
  - "Password reset tokens hashed with SHA-256 (same as refresh tokens) and expire after 1 hour"
  - "OAuth uses server-side authorization code flow via Ktor oauth provider (no client-side OAuth SDKs)"
  - "OAuth endpoints non-functional until env vars configured (empty defaults)"
  - "forgotPassword always returns success to prevent user enumeration"

patterns-established:
  - "Sealed class for domain types: UserTier models 5 tiers with level/displayName, fromString() for DB/wire bridging"
  - "Extension property on DTO: UserResponse.tier maps role String to sealed type without breaking wire format"
  - "Password reset token flow: generate random token, hash for storage, validate/mark-used on reset"
  - "Server-side OAuth: authenticate(provider-name) wrapper + callback route extracting principal"

# Metrics
duration: 7min
completed: 2026-02-13
---

# Phase 5 Plan 1: Server OAuth, Password Reset, and UserTier Summary

**Server-side Google/Apple OAuth endpoints, token-based password reset flow, UserTier sealed class with 5 tiers, and updated RegisterRequest with firstName/lastName**

## Performance

- **Duration:** 7 min
- **Started:** 2026-02-13T15:36:05Z
- **Completed:** 2026-02-13T15:43:11Z
- **Tasks:** 2
- **Files modified:** 19

## Accomplishments
- UserTier sealed class with 5 data object subtypes (Free, Paid, Premium, Admin, PowerAdmin) and fromString() conversion
- Server OAuth endpoints for Google and Apple using Ktor's built-in oauth provider with authorization code flow
- Complete password reset flow: PasswordResetTokensTable, repository, service, routes, and SDK functions
- RegisterRequest updated to firstName/lastName with server-side concatenation for storage
- SDK AuthApi extended with forgotPassword() and resetPassword() returning Either<AppError, Unit>

## Task Commits

Each task was committed atomically:

1. **Task 1: Add UserTier sealed type and update DTOs** - `95a8dc9` (feat)
2. **Task 2: Add server OAuth, password reset endpoints, and SDK functions** - `d092d14` (feat)

## Files Created/Modified
- `core/models/.../UserTier.kt` - Sealed class with 5 tiers, fromString(), displayName/level
- `core/models/.../dto/AuthDtos.kt` - RegisterRequest firstName/lastName, ForgotPasswordRequest, ResetPasswordRequest
- `core/models/.../dto/UserDtos.kt` - UserResponse.tier extension property
- `server/auth/.../tables/PasswordResetTokensTable.kt` - Exposed table: id, userId, tokenHash, expiresAt, used
- `server/auth/.../repository/PasswordResetTokenRepository.kt` - store(), findValidToken(), markUsed()
- `server/auth/.../service/OAuthService.kt` - Google userinfo fetch, Apple id_token decode, find-or-create user
- `server/auth/.../service/PasswordResetService.kt` - forgotPassword (dev log), resetPassword (token validate + password update)
- `server/auth/.../routes/OAuthRoutes.kt` - authenticate("google-oauth"/"apple-oauth") wrapped routes
- `server/auth/.../routes/AuthRoutes.kt` - Added forgot-password and reset-password POST endpoints
- `server/auth/.../service/AuthService.kt` - register() updated for firstName/lastName with accumulated validation
- `server/auth/.../repository/UserRepository.kt` - Added updatePasswordHash() for reset flow
- `server/auth/.../Auth.kt` - CreatePasswordResetTokensTableMigration registered
- `server/auth/.../di/AuthModule.kt` - Wired OAuthService, PasswordResetService, PasswordResetTokenRepository
- `server/auth/build.gradle.kts` - Added ktor-client-core, ktor-client-cio, ktor-client-content-negotiation
- `server/build.gradle.kts` - Added ktor-client deps for HttpClient in ServerModule
- `server/.../Application.kt` - OAuth provider installation (Google + Apple), oauthRoutes + passwordResetService wiring
- `server/.../di/ServerModule.kt` - HttpClient(CIO) singleton for OAuth userinfo calls
- `server/core/config/.../Env.kt` - OAuth data class with Google/Apple env vars
- `core/sdk/.../api/AuthApi.kt` - forgotPassword() and resetPassword() SDK functions

## Decisions Made
- UserTier as sealed class (not enum) per user decision -- compiler enforces exhaustive tier handling in when expressions
- RegisterRequest uses firstName/lastName fields; server concatenates to "$firstName $lastName" for backward-compatible name storage
- UserResponse.tier extension property keeps wire format unchanged (role: String) while providing type safety on client
- Password reset tokens expire after 1 hour and are hashed with SHA-256 (same pattern as refresh tokens)
- forgotPassword endpoint always returns success regardless of email existence (prevents user enumeration)
- OAuth providers installed with empty string defaults -- non-functional until GOOGLE_CLIENT_ID etc. are configured
- OAuthService creates users with random password hash (OAuth users don't use passwords)
- HttpClient(CIO) with JSON content negotiation provided via Koin for server-side OAuth userinfo calls

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added UserRepository.updatePasswordHash() method**
- **Found during:** Task 2 (PasswordResetService implementation)
- **Issue:** PasswordResetService needs to update a user's password hash, but UserRepository only had updateProfile(name, email)
- **Fix:** Added updatePasswordHash(id, passwordHash) method to UserRepository
- **Files modified:** server/auth/src/main/kotlin/com/m2f/server/auth/repository/UserRepository.kt
- **Verification:** server:auth:compileKotlin passes
- **Committed in:** d092d14 (Task 2 commit)

**2. [Rule 3 - Blocking] Added ktor-client dependencies to server and server:auth modules**
- **Found during:** Task 2 (OAuthService and ServerModule implementation)
- **Issue:** OAuthService uses HttpClient for Google userinfo API calls; neither server nor server:auth had ktor-client dependencies
- **Fix:** Added ktor-client-core, ktor-client-cio, ktor-client-content-negotiation to both build.gradle.kts files
- **Files modified:** server/auth/build.gradle.kts, server/build.gradle.kts
- **Verification:** server:compileKotlin passes
- **Committed in:** d092d14 (Task 2 commit)

---

**Total deviations:** 2 auto-fixed (2 blocking)
**Impact on plan:** Both auto-fixes necessary for functionality. No scope creep.

## Issues Encountered
None

## User Setup Required
None - OAuth endpoints are non-functional until environment variables are configured. This is by design (empty defaults).

## Next Phase Readiness
- UserTier sealed class ready for profile screen tier differentiation (plans 05-04, 05-05)
- RegisterRequest firstName/lastName ready for registration screen form (plan 05-03)
- OAuth endpoints ready for social login buttons (plan 05-02, 05-03)
- Password reset endpoints ready for forgot-password screen (plan 05-03)
- SDK forgotPassword/resetPassword ready for ViewModel integration

## Self-Check: PASSED

All 18 key files verified present. Both task commits (95a8dc9, d092d14) verified in git log.

---
*Phase: 05-auth-screens-dashboard-setup-cli*
*Completed: 2026-02-13*
