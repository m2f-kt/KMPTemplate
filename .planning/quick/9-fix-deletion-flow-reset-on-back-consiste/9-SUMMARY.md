---
phase: quick-9
plan: 01
subsystem: privacy
tags: [deletion-flow, ux, security, server-validation]
dependency-graph:
  requires: []
  provides: [verify-password-endpoint, confirmation-token-flow]
  affects: [privacy-module, sdk, server-privacy]
tech-stack:
  added: []
  patterns: [confirmation-token-before-deletion, in-memory-token-store]
key-files:
  created: []
  modified:
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionModel.kt
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionMutation.kt
    - app/privacy/impl/src/commonMain/composeResources/values/strings.xml
    - app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml
    - core/models/src/commonMain/kotlin/com/m2f/template/models/dto/privacy/PrivacyDtos.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApiImpl.kt
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakePrivacyApiBuilder.kt
    - server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/AccountDeletionService.kt
    - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceImpl.kt
    - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/DeletionRoutes.kt
    - server/privacy/impl/src/test/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceTest.kt
decisions:
  - Used in-memory ConcurrentHashMap for confirmation tokens with 15min TTL (sufficient for single-server deployment)
  - Removed password field from model/mutations since ReAuthStep uses local remember state
metrics:
  duration: 640s
  completed: 2026-03-15
---

# Quick Task 9: Fix Deletion Flow Reset, Step Counters, Title Colors, and Server-Side Password Validation

Server-validated password step with confirmation token flow, consistent STEP N OF 5 counters, state reset on re-entry, and no red title colors.

## Task Summary

| # | Task | Commit | Status |
|---|------|--------|--------|
| 1 | Fix UI issues -- state reset, step counters, title colors | d7eeb60 | Done |
| 2 | Add server-side password verification endpoint | 4b2b712 | Done |
| 3 | Wire client SDK and ViewModel to token flow | e6a699b | Done |

## Changes Made

### Task 1: UI Fixes
- Added reset mutations (step, password, reason, error) at top of `handleLoad()` so re-entering the screen starts clean
- Added "STEP N OF 5" labels to steps 1, 2, 3 main content (steps 4, 5 already had them)
- Removed duplicate `PanelStepIndicator` calls from desktop right panel for steps 1-3
- Changed WarningStep and ConfirmStep title colors from `colors.error` to `colors.text`
- Updated desktop panel indicators from "STEP N / 03" to "STEP N / 05"
- Added EN and ES string resources for step counters

### Task 2: Server-Side Password Verification
- Added `VerifyPasswordRequest` and `VerifyPasswordResponse` DTOs
- Changed `DeletionRequest.password` to `DeletionRequest.confirmationToken`
- Added `Privacy.VerifyPassword` resource route
- Implemented `verifyPasswordForDeletion` in service with in-memory token store (ConcurrentHashMap, 15min TTL)
- `requestDeletion` now validates confirmation token instead of raw password
- Added POST `/api/privacy/deletion/verify-password` route

### Task 3: Client SDK and ViewModel Wiring
- Added `verifyPasswordForDeletion` to `PrivacyApi` interface and `PrivacyApiImpl`
- Replaced `password` field with `confirmationToken` in `AccountDeletionModel`
- Replaced `SetPassword` mutation with `SetConfirmationToken`
- `handleReAuthenticate` now calls server to verify password, stores returned token on success
- `handleConfirmDeletion` sends `confirmationToken` (not raw password) in `DeletionRequest`
- Updated `FakePrivacyApiBuilder` with `verifyPasswordForDeletion` support
- Updated server tests to use verify-then-delete token flow

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Updated server-side tests for new DeletionRequest API**
- **Found during:** Task 3
- **Issue:** `AccountDeletionServiceTest` used `DeletionRequest(password = ...)` which no longer compiles after changing field to `confirmationToken`
- **Fix:** Rewrote tests to first call `verifyPasswordForDeletion` to get token, then pass token to `requestDeletion`. Added 3 new test cases for the verify endpoint.
- **Files modified:** `server/privacy/impl/src/test/kotlin/.../AccountDeletionServiceTest.kt`
- **Commit:** e6a699b

**2. [Rule 3 - Blocking] Updated FakePrivacyApiBuilder for new SDK method**
- **Found during:** Task 3
- **Issue:** `FakePrivacyApiBuilder` missing `verifyPasswordForDeletion` would break compilation of any test using `FakePrivacyApi`
- **Fix:** Added `_verifyPasswordForDeletion` field, DSL setter, and override in build()
- **Files modified:** `core/testing/src/commonMain/kotlin/.../FakePrivacyApiBuilder.kt`
- **Commit:** e6a699b

**3. [Rule 1 - Bug] Fixed kotlinx.datetime vs kotlin.time Instant mismatch**
- **Found during:** Task 2
- **Issue:** `Clock.System.now()` from `kotlin.time` returns `kotlin.time.Instant` but `DeletionToken.expiresAt` needed `kotlinx.datetime.Instant`
- **Fix:** Used `kotlinx.datetime.Clock.System.now()` explicitly
- **Files modified:** `AccountDeletionServiceImpl.kt`
- **Commit:** 4b2b712

## Verification

All modules compile successfully:
- `:app:privacy:impl:compileDebugSources` -- BUILD SUCCESSFUL
- `:core:sdk:compileDebugKotlinAndroid` -- BUILD SUCCESSFUL
- `:server:privacy:impl:compileKotlin` -- BUILD SUCCESSFUL
- `:server:privacy:impl:test` -- BUILD SUCCESSFUL (all 8 tests pass)
