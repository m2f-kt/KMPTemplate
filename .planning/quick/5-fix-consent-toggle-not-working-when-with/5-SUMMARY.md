---
phase: quick-5
plan: 01
subsystem: server/privacy
tags: [bug-fix, consent, tdd, server]
dependency_graph:
  requires: []
  provides: [correct-consent-withdrawal-reflection]
  affects: [ConsentServiceImpl, ConsentServiceTest, AccountDeletionServiceTest]
tech_stack:
  added: []
  patterns: [per-type-latest-record-lookup]
key_files:
  created: []
  modified:
    - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ConsentServiceImpl.kt
    - server/privacy/impl/src/test/kotlin/com/m2f/server/privacy/service/ConsentServiceTest.kt
    - server/privacy/impl/src/test/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceTest.kt
decisions:
  - "Use findLatestByUserAndType per type instead of findAllActiveByUser to correctly reflect withdrawals in getActiveConsents"
  - "Rename FakeConsentRepository to StubConsentRepository in AccountDeletionServiceTest to resolve pre-existing package-level redeclaration conflict"
metrics:
  duration: ~10 min
  completed: 2026-03-15
  tasks_completed: 1
  files_modified: 3
---

# Phase quick-5 Plan 01: Fix Consent Withdraw Not Reflecting in getActiveConsents Summary

**One-liner:** Fixed getActiveConsents to use per-type findLatestByUserAndType lookups instead of findAllActiveByUser, so withdrawal records correctly reflect granted=false.

## What Was Built

The consent withdraw flow was broken at the read side: `getActiveConsents` called `findAllActiveByUser` which filtered `granted=true`, skipping withdrawal records. After granting then withdrawing, the old grant record was still returned, making the UI show the consent as still active.

**Fix:** Replaced the single `findAllActiveByUser` call in `ConsentServiceImpl.getActiveConsents` with per-`ConsentType` lookups via `findLatestByUserAndType`. This returns the most recent record regardless of `granted` status, so a withdrawal record correctly produces `granted=false` with null `grantedAt`/`documentVersion`.

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Add test for grant-then-withdraw and fix getActiveConsents query | 4a3be60 | ConsentServiceImpl.kt, ConsentServiceTest.kt, AccountDeletionServiceTest.kt |

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Renamed FakeConsentRepository to StubConsentRepository in AccountDeletionServiceTest**
- **Found during:** Task 1 (RED phase compile attempt)
- **Issue:** Both `ConsentServiceTest.kt` and `AccountDeletionServiceTest.kt` declared `private class FakeConsentRepository` in the same package, causing a Kotlin compiler redeclaration error that prevented compilation
- **Fix:** Renamed the minimal stub in `AccountDeletionServiceTest.kt` to `StubConsentRepository` since it only serves as a constructor dependency stub with no behavioral assertions
- **Files modified:** `AccountDeletionServiceTest.kt`
- **Commit:** 4a3be60 (included in same commit)

## Verification

- New test `getActiveConsents returns granted=false after consent is withdrawn` passes
- All existing `ConsentServiceTest` tests pass (grant, deduplication, required consents)
- All existing `AccountDeletionServiceTest` tests pass
- Client `app:privacy:impl:allTests` unaffected (server-side fix only)

## Self-Check: PASSED
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ConsentServiceImpl.kt` - exists and modified
- `server/privacy/impl/src/test/kotlin/com/m2f/server/privacy/service/ConsentServiceTest.kt` - exists and modified
- `server/privacy/impl/src/test/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceTest.kt` - exists and modified
- Commit 4a3be60 - verified in git log
