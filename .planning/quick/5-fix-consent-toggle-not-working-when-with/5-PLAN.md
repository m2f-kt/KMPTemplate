---
phase: quick-5
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ConsentServiceImpl.kt
  - server/privacy/impl/src/test/kotlin/com/m2f/server/privacy/service/ConsentServiceTest.kt
autonomous: true
requirements: [CONSENT-WITHDRAW-FIX]

must_haves:
  truths:
    - "After granting then withdrawing a consent, getActiveConsents returns granted=false for that type"
    - "Toggle ON followed by toggle OFF results in the consent showing as not granted in the UI"
    - "Existing grant-only and deduplication behavior is preserved"
  artifacts:
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ConsentServiceImpl.kt"
      provides: "Fixed getActiveConsents using latest record per type (not just granted records)"
      contains: "findLatestByUserAndType"
    - path: "server/privacy/impl/src/test/kotlin/com/m2f/server/privacy/service/ConsentServiceTest.kt"
      provides: "Test proving grant-then-withdraw returns granted=false"
      contains: "withdraw.*getActiveConsents.*granted.*false"
  key_links:
    - from: "ConsentServiceImpl.getActiveConsents"
      to: "ConsentRepository.findLatestByUserAndType"
      via: "per-type latest record lookup"
      pattern: "findLatestByUserAndType"
---

<objective>
Fix consent withdraw not reflecting in UI: after toggling a consent OFF, the getActiveConsents
endpoint still returns granted=true because the repository query `findAllActiveByUser` filters
by `granted=true`, ignoring the newer withdrawal record.

Purpose: The withdraw API call succeeds (returns 200) but the subsequent reload fetches stale
data because the query only looks at granted records, finding the old grant instead of the
newer withdrawal.

Output: Fixed service that queries latest record per consent type regardless of granted status.
</objective>

<execution_context>
@/Users/marc/.claude/get-shit-done/workflows/execute-plan.md
@/Users/marc/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@.planning/quick/4-fix-consent-toggle-grant-withdraw-bugs-d/4-SUMMARY.md

Root cause analysis:
- `ExposedConsentRepository.findAllActiveByUser()` filters `granted eq true` (line 62)
- `ConsentServiceImpl.getActiveConsents()` uses `findAllActiveByUser()` to build the status map
- After withdraw, a `(type, granted=false)` record is inserted, but `findAllActiveByUser` skips it
  and still finds the OLD `(type, granted=true)` record
- Result: reloaded consents show the type as still granted

Fix approach: Change `getActiveConsents` to call `findLatestByUserAndType` per consent type
instead of relying on the `findAllActiveByUser` query. This returns the most recent record
regardless of granted status, correctly reflecting withdrawals.

<interfaces>
From server/privacy/contract/repository/ConsentRepository.kt:
```kotlin
interface ConsentRepository {
    suspend fun findLatestByUserAndType(userId: Uuid, consentType: String): ConsentRecord?
    suspend fun findAllActiveByUser(userId: Uuid): List<ConsentRecord>
    // ... other methods
}
```

From server/privacy/contract/repository/ConsentRecord.kt (approximate):
```kotlin
data class ConsentRecord(
    val id: Uuid,
    val userId: Uuid,
    val consentType: String,
    val granted: Boolean,
    val legalDocumentVersion: String,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: LocalDateTime,
)
```
</interfaces>
</context>

<tasks>

<task type="auto" tdd="true">
  <name>Task 1: Add test for grant-then-withdraw and fix getActiveConsents query</name>
  <files>
    server/privacy/impl/src/test/kotlin/com/m2f/server/privacy/service/ConsentServiceTest.kt,
    server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ConsentServiceImpl.kt
  </files>
  <behavior>
    - Test: after granting then withdrawing MARKETING consent, getActiveConsents returns MARKETING with granted=false
    - Test: existing tests still pass (grant shows granted=true, deduplication works)
  </behavior>
  <action>
RED: Add test `getActiveConsents returns granted=false after consent is withdrawn` to ConsentServiceTest:
1. Create service with FakeConsentRepository + FakeLegalDocumentRepository (with a marketing document)
2. Grant MARKETING consent via service.grantConsent
3. Withdraw MARKETING consent via service.withdrawConsent
4. Call service.getActiveConsents
5. Assert MARKETING consent has `granted shouldBe false` and `grantedAt.shouldBeNull()`
6. Run test -- it MUST fail (currently returns granted=true due to the bug)

GREEN: Fix `ConsentServiceImpl.getActiveConsents()`:
- Replace the `findAllActiveByUser` call with per-type lookups using `findLatestByUserAndType`
- For each `ConsentType` entry, call `consentRepository.findLatestByUserAndType(uuid, type.name)`
- Build ConsentStatus from the latest record: `granted = record?.granted ?: false`
- For `grantedAt`: only populate when `record?.granted == true` (withdrawal records should show null grantedAt)
- For `documentVersion`: only populate when `record?.granted == true`

This is a minimal change to the service method only. The repository is correct -- `findLatestByUserAndType` already returns the most recent record regardless of granted status.

Do NOT modify the repository or its query. Do NOT change findAllActiveByUser (it may be used elsewhere or is semantically correct for "active" records).
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :server:privacy:impl:test --tests "com.m2f.server.privacy.service.ConsentServiceTest" --rerun</automated>
  </verify>
  <done>
    - New test `getActiveConsents returns granted=false after consent is withdrawn` passes
    - All existing ConsentServiceTest tests still pass
    - getActiveConsents uses findLatestByUserAndType per type instead of findAllActiveByUser
  </done>
</task>

</tasks>

<verification>
```bash
# All server privacy tests pass
./gradlew :server:privacy:impl:test --rerun

# Full server test suite still passes
./gradlew :server:test

# Client privacy tests still pass (ViewModel logic unchanged)
./gradlew :app:privacy:impl:allTests
```
</verification>

<success_criteria>
- After grant + withdraw sequence, getActiveConsents returns the consent type with granted=false
- All existing consent service tests pass without modification
- Client-side ViewModel and tests unaffected (the fix is server-side only)
</success_criteria>

<output>
After completion, create `.planning/quick/5-fix-consent-toggle-not-working-when-with/5-SUMMARY.md`
</output>
