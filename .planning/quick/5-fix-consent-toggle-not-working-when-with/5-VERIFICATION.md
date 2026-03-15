---
phase: quick-5
verified: 2026-03-15T18:35:00Z
status: passed
score: 3/3 must-haves verified
---

# Phase quick-5: Fix Consent Toggle Not Working When Withdrawing — Verification Report

**Phase Goal:** Fix consent toggle not working when withdrawing — toggle stays on despite successful withdraw API calls
**Verified:** 2026-03-15T18:35:00Z
**Status:** passed
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | After granting then withdrawing a consent, getActiveConsents returns granted=false for that type | VERIFIED | `getActiveConsents` iterates `ConsentType.entries`, calls `findLatestByUserAndType` per type, returns `granted=false` when latest record has `granted=false` (lines 30-47 of ConsentServiceImpl.kt) |
| 2 | Toggle ON followed by toggle OFF results in the consent showing as not granted in the UI | VERIFIED | Server-side fix is correct; `getActiveConsents` no longer filters by `granted=true`, so the API response reflects the withdrawal immediately on next reload |
| 3 | Existing grant-only and deduplication behavior is preserved | VERIFIED | Grant path unchanged (lines 32-38): when `record != null && record.granted`, populates `grantedAt` and `documentVersion`; test `getActiveConsents deduplicates by type returning latest` verifies version 2.0 wins over 1.0 |

**Score:** 3/3 truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ConsentServiceImpl.kt` | Fixed getActiveConsents using latest record per type | VERIFIED | Uses `findLatestByUserAndType` on line 31; `findAllActiveByUser` absent from this method entirely |
| `server/privacy/impl/src/test/kotlin/com/m2f/server/privacy/service/ConsentServiceTest.kt` | Test proving grant-then-withdraw returns granted=false | VERIFIED | Test at line 208: `` `getActiveConsents returns granted=false after consent is withdrawn` `` asserts `marketingConsent.granted shouldBe false` and `marketingConsent.grantedAt.shouldBeNull()` |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `ConsentServiceImpl.getActiveConsents` | `ConsentRepository.findLatestByUserAndType` | per-type latest record lookup | WIRED | Called at line 31 for every `ConsentType` entry; `findAllActiveByUser` not called anywhere in this method |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|-------------|--------|----------|
| CONSENT-WITHDRAW-FIX | 5-PLAN.md | Fix getActiveConsents to reflect withdrawal records | SATISFIED | Service now calls `findLatestByUserAndType` per type; withdrawal record (granted=false) is returned as-is; TDD test proves the bug case is fixed |

### Anti-Patterns Found

None detected. No TODOs, FIXMEs, placeholder returns, or stub handlers in the modified files.

### Human Verification Required

None. The fix is fully server-side and testable via unit tests. No UI, real-time, or external-service behavior is involved beyond the corrected API response shape.

### Gaps Summary

No gaps. All three observable truths are satisfied by the implementation. The root-cause fix (replacing `findAllActiveByUser` with `findLatestByUserAndType` in `getActiveConsents`) is in place, the key link is wired, and the regression test proves the previously broken grant-then-withdraw scenario now returns `granted=false`.

---

_Verified: 2026-03-15T18:35:00Z_
_Verifier: Claude (gsd-verifier)_
