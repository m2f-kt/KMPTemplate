# GDPR/LOPD Compliance UAT Fixes

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix all 8+1 issues found during UAT of the GDPR/LOPD compliance implementation.

**Architecture:** Targeted fixes across server routes, SDK, ViewModels, and UI. One feature removal (processing restriction). No new modules — only modifications to existing privacy, auth, and profile modules.

**Tech Stack:** Ktor + Exposed R2DBC (server), Compose Multiplatform + MVI (client), Arrow Either, Koin DI, Kotest assertions.

**Spec:** `docs/superpowers/specs/2026-03-12-gdpr-lopd-compliance-design.md`
**UAT:** `.planning/phases/gdpr-compliance/gdpr-UAT.md`

---

## File Structure

### Modified Files

**Server:**
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/DeletionRoutes.kt` — Fix "none" status response
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/ExportRoutes.kt` — Add active export endpoint
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedConsentRepository.kt` — Deduplicate consent records
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ConsentServiceImpl.kt` — Synthesize all consent types
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceImpl.kt` — Guard null passwordHash
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/di/PrivacyModule.kt` — Remove ProcessingRestrictionService
- `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/DataExportService.kt` — Add getActiveExport method
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt` — Implement getActiveExport
- `server/src/main/kotlin/com/m2f/template/Application.kt` — Remove restriction routes/imports
- `server/auth/contract/src/main/kotlin/com/m2f/server/auth/contract/tables/UsersTable.kt` — Remove processingRestricted
- `server/auth/contract/src/main/kotlin/com/m2f/server/auth/contract/repository/UserRepository.kt` — Remove processingRestricted
- `server/auth/impl/src/main/kotlin/com/m2f/server/auth/repository/ExposedUserRepository.kt` — Remove processingRestricted
- `server/auth/impl/src/main/kotlin/com/m2f/server/auth/Auth.kt` — Remove migration + add drop-column migration

**Core:**
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt` — Fix duplicate Bearer header
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt` — Remove restriction methods, add getActiveExport
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApiImpl.kt` — Same
- `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt` — Add ActiveExport route, remove restriction routes
- `core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt` — Remove ProcessingRestricted
- `core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt` — Remove PRIVACY_PROCESSING_RESTRICTED
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakePrivacyApiBuilder.kt` — Remove restriction fakes, add getActiveExport fake

**Client:**
- `app/auth/impl/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterViewModel.kt` — Grant consent after registration
- `app/auth/impl/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterScreen.kt` — Fix checkbox padding
- `app/auth/impl/src/commonTest/kotlin/com/m2f/template/app/auth/RegisterViewModelTest.kt` — Update test expectations
- `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModel.kt` — Remove restriction, add export load
- `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt` — Remove restriction section, remove onToggleRestriction
- `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsModel.kt` — Remove isRestricted
- `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsIntent.kt` — Remove ToggleRestriction
- `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsMutation.kt` — Remove SetRestricted
- `app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModelTest.kt` — Remove restriction tests, add export load test
- `app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt` — Remove onToggleRestriction
- `app/profile/impl/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileScreen.kt` — Inline privacy content in profile dashboard
- `app/profile/wire/src/commonMain/kotlin/com/m2f/template/app/profile/wire/ProfileNavigation.kt` — Pass privacy content slot

**Deleted Files:**
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/RestrictionRoutes.kt`
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ProcessingRestrictionServiceImpl.kt`
- `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/ProcessingRestrictionService.kt`

**String resources to clean (remove `error_privacy_processing_restricted` entry):**
- `composeApp/src/commonMain/composeResources/values/strings.xml`
- `composeApp/src/commonMain/composeResources/values-es/strings.xml`
- `app/auth/impl/src/commonMain/composeResources/values/strings.xml`
- `app/auth/impl/src/commonMain/composeResources/values-es/strings.xml`
- `app/admin/impl/src/commonMain/composeResources/values/strings.xml`
- `app/admin/impl/src/commonMain/composeResources/values-es/strings.xml`
- `app/profile/impl/src/commonMain/composeResources/values/strings.xml`
- `app/profile/impl/src/commonMain/composeResources/values-es/strings.xml`
- `app/documents/impl/src/commonMain/composeResources/values/strings.xml`

**StringKeyResolver files (remove PRIVACY_PROCESSING_RESTRICTED branch + import):**
- `composeApp/src/commonMain/kotlin/com/m2f/template/localization/StringKeyResolver.kt`
- `app/auth/impl/src/commonMain/kotlin/com/m2f/template/app/auth/StringKeyResolver.kt`
- `app/admin/impl/src/commonMain/kotlin/com/m2f/template/app/admin/StringKeyResolver.kt`
- `app/profile/impl/src/commonMain/kotlin/com/m2f/template/app/profile/StringKeyResolver.kt`
- `app/documents/impl/src/commonMain/kotlin/com/m2f/template/app/documents/StringKeyResolver.kt`

---

## Chunk 1: Critical Bug Fixes (Deletion Blocker + Auth Interceptor + Consent Dedup)

These are the highest-priority fixes: a blocker (account deletion crashes) and two bugs affecting core functionality.

### Task 1: Fix deletion status "none" response (UAT #10, Bug 1)

**Files:**
- Modify: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/DeletionRoutes.kt`

The server returns `mapOf("status" to "none")` when no deletion is pending, but the client expects a `DeletionResponse?`. The fix: return HTTP 204 No Content.

- [ ] **Step 1: Fix the route to return 204 when no deletion pending**

Replace the `get<Privacy.GetDeletionStatus>` block in `DeletionRoutes.kt`:

```kotlin
get<Privacy.GetDeletionStatus> {
    conduitAuth { userId ->
        accountDeletionService.getDeletionStatus(userId)
            ?: mapOf("status" to "none")
    }
}
```

With:

```kotlin
get<Privacy.GetDeletionStatus> {
    conduitAuth<DeletionResponse> { userId ->
        val status = accountDeletionService.getDeletionStatus(userId)
        if (status == null) {
            call.respond(HttpStatusCode.NoContent)
            return@conduitAuth null as DeletionResponse
        }
        status
    }
}
```

Wait — `conduitAuth` serializes the return value. A cleaner approach: use Ktor's `call.respond` directly and skip `conduitAuth` for this specific case. Actually, let's keep it simpler. The issue is that `conduitAuth` tries to serialize whatever the block returns. Replace with explicit null handling:

```kotlin
get<Privacy.GetDeletionStatus> {
    authenticate {
        val userId = call.principal<JWTPrincipal>()?.subject ?: return@authenticate call.respond(HttpStatusCode.Unauthorized)
        val status = accountDeletionService.getDeletionStatus(userId)
        if (status == null) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(status)
        }
    }
}
```

Actually, to stay consistent with the project's `conduitAuth` pattern, the simplest fix is to make the server return an empty body that the client handles. But the cleanest is:

Replace the entire block:
```kotlin
get<Privacy.GetDeletionStatus> {
    conduitAuth { userId ->
        accountDeletionService.getDeletionStatus(userId)
            ?: mapOf("status" to "none")
    }
}
```

With:
```kotlin
get<Privacy.GetDeletionStatus> {
    authenticate {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
            return@authenticate
        }
        val result = either<DomainError, DeletionResponse?> {
            accountDeletionService.getDeletionStatus(userId)
        }
        result.fold(
            ifLeft = { error -> call.respond(error.toStatusCode(), error.toResponse()) },
            ifRight = { status ->
                if (status == null) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(status)
                }
            },
        )
    }
}
```

Add necessary imports: `HttpStatusCode`, `JWTPrincipal`, `authenticate`, `either`, `respond`.

- [ ] **Step 2: Update client to handle 204 No Content**

Modify `PrivacyApiImpl.kt` `getDeletionStatus()` to handle empty response:

```kotlin
override suspend fun getDeletionStatus(): Either<AppError, DeletionResponse?> =
    apiCall {
        val response = client.get(Privacy.GetDeletionStatus())
        if (response.status == HttpStatusCode.NoContent) {
            return@apiCall response
        }
        response
    }
```

Actually, the `apiCall` wrapper likely auto-deserializes. The cleaner fix: since `DeletionResponse?` is nullable, and `apiCall` will fail to deserialize an empty body, we need a specialized call. Replace with:

```kotlin
override suspend fun getDeletionStatus(): Either<AppError, DeletionResponse?> =
    Either.catch {
        val response = client.get(Privacy.GetDeletionStatus())
        if (response.status == HttpStatusCode.NoContent) {
            null
        } else {
            response.body<DeletionResponse>()
        }
    }.mapLeft { AppError.Unknown(it.message ?: "Unknown error") }
```

Add imports: `HttpStatusCode`, `body`.

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :server:privacy:impl:compileKotlin :core:sdk:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/DeletionRoutes.kt \
      core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApiImpl.kt
git commit -m "fix(privacy): return 204 No Content when no deletion pending"
```

### Task 2: Fix duplicate Authorization header on token refresh (UAT #10, Bug 2)

**Files:**
- Modify: `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt`

The `bearerAuth()` function appends rather than replaces the Authorization header. Lines 103 and 109 need to clear the existing header first.

- [ ] **Step 1: Add header removal before retry**

At line 102-103, before `request.bearerAuth(newTokens.accessToken)`, add:
```kotlin
request.headers.remove(HttpHeaders.Authorization)
```

At line 108-109, before `request.bearerAuth(updatedToken)`, add:
```kotlin
request.headers.remove(HttpHeaders.Authorization)
```

Add import: `io.ktor.http.HttpHeaders`

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :core:sdk:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt
git commit -m "fix(sdk): clear Authorization header before retry to prevent duplicates"
```

### Task 3: Fix account deletion 500 — guard null passwordHash (UAT #10, Bug 3)

**Files:**
- Modify: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceImpl.kt`

OAuth users have null `passwordHash`. The `passwordHasher.verify()` at line 47 crashes. Guard it.

- [ ] **Step 1: Add null check before password verification**

Replace lines 47-49 in `AccountDeletionServiceImpl.kt`:
```kotlin
ensure(passwordHasher.verify(request.password, user.passwordHash)) {
    InvalidCredentials()
}
```

With:
```kotlin
ensureNotNull(user.passwordHash) {
    InvalidCredentials()
}
ensure(passwordHasher.verify(request.password, user.passwordHash)) {
    InvalidCredentials()
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :server:privacy:impl:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceImpl.kt
git commit -m "fix(privacy): guard null passwordHash in deletion — prevents 500 for OAuth users"
```

### Task 4: Fix consent list returning duplicates (UAT #8 additional bug)

**Files:**
- Modify: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedConsentRepository.kt`

The `findAllActiveByUser` returns all `granted=true` rows. Since `consent_records` is append-only, multiple rows exist per type. Fix: return only the latest per type.

- [ ] **Step 1: Deduplicate the query**

Replace `findAllActiveByUser` in `ExposedConsentRepository.kt` (lines 56-67):

```kotlin
override suspend fun findAllActiveByUser(userId: Uuid): List<ConsentRecord> =
    suspendTransaction(db = db) {
        ConsentRecordsTable
            .select(ConsentRecordsTable.columns)
            .where {
                (ConsentRecordsTable.userId eq userId) and
                    (ConsentRecordsTable.granted eq true)
            }
            .orderBy(ConsentRecordsTable.createdAt, SortOrder.DESC)
            .toList()
            .map { it.toConsentRecord() }
    }
```

With:

```kotlin
override suspend fun findAllActiveByUser(userId: Uuid): List<ConsentRecord> =
    suspendTransaction(db = db) {
        ConsentRecordsTable
            .select(ConsentRecordsTable.columns)
            .where {
                (ConsentRecordsTable.userId eq userId) and
                    (ConsentRecordsTable.granted eq true)
            }
            .orderBy(ConsentRecordsTable.createdAt, SortOrder.DESC)
            .toList()
            .map { it.toConsentRecord() }
            .distinctBy { it.consentType }
    }
```

The `distinctBy` after `orderBy DESC` keeps the latest record per consent type.

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :server:privacy:impl:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedConsentRepository.kt
git commit -m "fix(privacy): deduplicate consent records — return only latest per type"
```

---

## Chunk 2: Registration Consent Flow (UAT #2)

Fix the duplicated consent acceptance: registration should grant consent server-side, and Consent Gate should only appear when policy versions change.

### Task 5: Grant consent after successful registration

**Files:**
- Modify: `app/auth/impl/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterViewModel.kt`
- Modify: `app/auth/impl/src/commonTest/kotlin/com/m2f/template/app/auth/RegisterViewModelTest.kt`

- [ ] **Step 1: Update RegisterViewModel to grant consent after registration**

In `RegisterViewModel.kt`, after `sdk.register(request)` succeeds (line 95), before `navigateWithConsentCheck()`, grant both required consents. Add a helper method:

```kotlin
private suspend fun grantRequiredConsents() {
    val requiredConsents = sdk.getRequiredConsents().getOrNull() ?: return
    for (consent in requiredConsents.consents) {
        sdk.grantConsent(
            GrantConsentRequest(
                type = consent.type,
                documentVersion = consent.currentVersion,
            )
        )
    }
}
```

Add import: `com.m2f.template.models.dto.privacy.GrantConsentRequest`

Then call `grantRequiredConsents()` after registration succeeds (line 95, inside `ifRight`), before `navigateWithConsentCheck()`:

```kotlin
ifRight = {
    grantRequiredConsents()
    val token = current.invitationToken
    // ... rest unchanged
},
```

This way `navigateWithConsentCheck()` will see `hasOutdated = false` and go straight to dashboard.

- [ ] **Step 2: Update the test that expects consent gate after registration**

In `RegisterViewModelTest.kt`, find the test `successful registration with outdated consents navigates to consent gate`. Update it to:
1. Configure `fakeSdk` so that `getRequiredConsents` initially returns `hasOutdated=true` with consents
2. Configure `grantConsent` to succeed
3. After granting, `getRequiredConsents` should return `hasOutdated=false`
4. Expect `NavigateToDashboard` event (not `NavigateToConsentGate`)

- [ ] **Step 3: Run tests**

Run: `./gradlew :app:auth:impl:jvmTest`
Expected: All tests pass

- [ ] **Step 4: Commit**

```bash
git add app/auth/impl/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterViewModel.kt \
      app/auth/impl/src/commonTest/kotlin/com/m2f/template/app/auth/RegisterViewModelTest.kt
git commit -m "fix(auth): grant consent server-side after registration — skip consent gate"
```

### Task 6: Fix checkbox padding on registration screen (UAT #13)

**Files:**
- Modify: `app/auth/impl/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterScreen.kt`

- [ ] **Step 1: Add spacing to TermsCheckboxWithLinks Row**

At line 680 in `RegisterScreen.kt`, change the `Row`:

```kotlin
Row(
    verticalAlignment = Alignment.CenterVertically,
) {
```

To:

```kotlin
Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
) {
```

Add import if missing: `androidx.compose.foundation.layout.Arrangement`

- [ ] **Step 2: Commit**

```bash
git add app/auth/impl/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterScreen.kt
git commit -m "fix(auth): add padding between checkbox and text on registration screen"
```

---

## Chunk 3: Remove Processing Restriction Feature (UAT #12)

Complete removal of the half-implemented processing restriction feature across ~30 files.

### Task 7: Delete restriction server files

**Files:**
- Delete: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/RestrictionRoutes.kt`
- Delete: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ProcessingRestrictionServiceImpl.kt`
- Delete: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/ProcessingRestrictionService.kt`

- [ ] **Step 1: Delete the three restriction files**

```bash
rm server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/RestrictionRoutes.kt
rm server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ProcessingRestrictionServiceImpl.kt
rm server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/ProcessingRestrictionService.kt
```

- [ ] **Step 2: Remove from PrivacyModule.kt**

In `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/di/PrivacyModule.kt`:
- Remove line 11: `import ...ProcessingRestrictionService`
- Remove line 24: `import ...ProcessingRestrictionServiceImpl`
- Remove line 38: `single<ProcessingRestrictionService> { ProcessingRestrictionServiceImpl(get()) }`

- [ ] **Step 3: Remove from Application.kt**

In `server/src/main/kotlin/com/m2f/template/Application.kt`:
- Remove the `ProcessingRestrictionService` import
- Remove the `val processingRestrictionService: ProcessingRestrictionService by inject()` line
- Remove the `restrictionRoutes(processingRestrictionService)` call
- Remove the `import ...RestrictionRoutes` import

- [ ] **Step 4: Remove processingRestricted from auth module**

In `server/auth/contract/src/main/kotlin/com/m2f/server/auth/contract/tables/UsersTable.kt`:
- Remove line 26: `val processingRestricted = bool("processing_restricted").default(false)`

In `server/auth/contract/src/main/kotlin/com/m2f/server/auth/contract/repository/UserRepository.kt`:
- Remove `processingRestricted` field from `UserRecord`
- Remove `updateProcessingRestricted` method from interface

In `server/auth/impl/src/main/kotlin/com/m2f/server/auth/repository/ExposedUserRepository.kt`:
- Remove `processingRestricted` from `toUserRecord()` mapping
- Remove `updateProcessingRestricted` implementation

In `server/auth/impl/src/main/kotlin/com/m2f/server/auth/Auth.kt`:
- Remove `AddProcessingRestrictedToUsersMigration` class
- Remove its registration in `registerAuthMigrations()`
- Add a new migration to drop the column:

```kotlin
class DropProcessingRestrictedFromUsersMigration : SchemaMigration(
    version = 20260315000001,
    description = "Drop processing_restricted column from users table",
) {
    override suspend fun migrate(database: R2dbcDatabase) {
        suspendTransaction(db = database) {
            exec("ALTER TABLE users DROP COLUMN IF EXISTS processing_restricted")
        }
    }
}
```

Register it in `registerAuthMigrations()`.

- [ ] **Step 5: Verify server compiles**

Run: `./gradlew :server:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit server changes**

```bash
git add -A server/
git commit -m "refactor(privacy): remove processing restriction from server"
```

### Task 8: Remove restriction from core layer

**Files:**
- Modify: `core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt`
- Modify: `core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt`
- Modify: `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt`
- Modify: `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt`
- Modify: `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApiImpl.kt`
- Modify: `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakePrivacyApiBuilder.kt`

- [ ] **Step 1: Remove AppError.Privacy.ProcessingRestricted**

In `AppError.kt`, remove the `ProcessingRestricted` data class from the `Privacy` sealed class.

- [ ] **Step 2: Remove PRIVACY_PROCESSING_RESTRICTED from StringKey**

In `StringKey.kt`, remove the `PRIVACY_PROCESSING_RESTRICTED` enum entry.

- [ ] **Step 3: Remove restriction routes from ApiRoutes.kt**

In `ApiRoutes.kt`, remove `RestrictProcessing` and `LiftRestriction` resource classes from the `Privacy` object.

- [ ] **Step 4: Remove from PrivacyApi and PrivacyApiImpl**

In `PrivacyApi.kt`, remove lines 26-27:
```kotlin
suspend fun restrictProcessing(): Either<AppError, Unit>
suspend fun liftRestriction(): Either<AppError, Unit>
```

In `PrivacyApiImpl.kt`, remove lines 67-71:
```kotlin
override suspend fun restrictProcessing(): Either<AppError, Unit> =
    apiCall { client.post(Privacy.RestrictProcessing()) }

override suspend fun liftRestriction(): Either<AppError, Unit> =
    apiCall { client.post(Privacy.LiftRestriction()) }
```

- [ ] **Step 5: Remove from FakePrivacyApiBuilder**

In `FakePrivacyApiBuilder.kt`, remove `restrictProcessing` and `liftRestriction` fake stubs and their configuration methods.

- [ ] **Step 6: Remove from Sdk facade**

In `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt`, if it delegates `restrictProcessing`/`liftRestriction`, remove those delegations.

- [ ] **Step 7: Verify core compiles**

Run: `./gradlew :core:models:compileKotlinJvm :core:sdk:compileKotlinJvm :core:testing:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit core changes**

```bash
git add -A core/
git commit -m "refactor(privacy): remove processing restriction from core layer"
```

### Task 9: Remove restriction from client app

**Files:**
- Modify: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsIntent.kt`
- Modify: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsMutation.kt`
- Modify: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsModel.kt`
- Modify: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModel.kt`
- Modify: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt`
- Modify: `app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt`
- Modify: `app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModelTest.kt`

- [ ] **Step 1: Remove ToggleRestriction from PrivacySettingsIntent.kt**

Remove line 9: `data object ToggleRestriction : PrivacySettingsIntent`

- [ ] **Step 2: Remove SetRestricted from PrivacySettingsMutation.kt**

Remove line 12: `data class SetRestricted(val restricted: Boolean) : PrivacySettingsMutation`

- [ ] **Step 3: Remove isRestricted from PrivacySettingsModel.kt**

Remove line 12: `val isRestricted: Boolean = false,`

- [ ] **Step 4: Remove from PrivacySettingsViewModel.kt**

- Remove line 22: `is PrivacySettingsIntent.ToggleRestriction -> handleToggleRestriction()`
- Remove lines 87-100: entire `handleToggleRestriction()` method
- Remove lines 130-134: the `SetRestricted` case from `reduce()`

- [ ] **Step 5: Remove Processing Restriction section from PrivacySettingsScreen.kt**

- Remove `onToggleRestriction` parameter from `PrivacySettingsScreen` composable signature (line 50)
- Remove it from the call to `PrivacySettingsContent` (line 82)
- Remove it from `PrivacySettingsContent` signature
- Remove lines 208-225: the entire "Processing Restriction" section (TerminalDivider, SectionHeader, TerminalText, TerminalSwitch)

- [ ] **Step 6: Remove from PrivacyNavigation.kt**

Remove line 81 (or wherever `onToggleRestriction` is wired): the mapping to `PrivacySettingsIntent.ToggleRestriction`.

- [ ] **Step 7: Remove restriction tests from PrivacySettingsViewModelTest.kt**

Remove the two test cases for toggle restriction (around lines 120-165).

- [ ] **Step 8: Remove string resources and StringKeyResolver entries**

Remove `error_privacy_processing_restricted` from all 9 strings.xml files listed in the File Structure section.

Remove the `PRIVACY_PROCESSING_RESTRICTED` case and its import from all 5 StringKeyResolver files listed in the File Structure section.

- [ ] **Step 9: Verify client compiles and tests pass**

Run: `./gradlew :app:privacy:impl:compileKotlinJvm :app:privacy:impl:jvmTest`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 10: Commit**

```bash
git add -A app/ composeApp/
git commit -m "refactor(privacy): remove processing restriction from client"
```

---

## Chunk 4: Export Status Persistence (UAT #9)

Add a server endpoint for active export status and load it on PrivacySettings init.

### Task 10: Add server endpoint for active export

**Files:**
- Modify: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/DataExportService.kt`
- Modify: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt`
- Modify: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/ExportRoutes.kt`
- Modify: `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt`

- [ ] **Step 1: Add getActiveExport to DataExportService interface**

In `DataExportService.kt`, add:
```kotlin
context(raise: Raise<DomainError>)
suspend fun getActiveExport(userId: String): DataExportResponse?
```

- [ ] **Step 2: Implement in DataExportServiceImpl**

In `DataExportServiceImpl.kt`, add:
```kotlin
context(raise: Raise<DomainError>)
override suspend fun getActiveExport(userId: String): DataExportResponse? {
    val uuid = Uuid.parse(userId)
    return dataExportRepository.findActiveByUser(uuid)?.toResponse()
}
```

The repository's `findActiveByUser(userId)` already exists.

- [ ] **Step 3: Add ActiveExport route to ApiRoutes.kt**

In `ApiRoutes.kt`, add inside the `Privacy` object:
```kotlin
@Resource("export/active")
class ActiveExport(val parent: Privacy = Privacy())
```

- [ ] **Step 4: Add route in ExportRoutes.kt**

In `ExportRoutes.kt`, add inside the `authenticate` block:
```kotlin
get<Privacy.ActiveExport> {
    authenticate {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@authenticate
        }
        val result = either<DomainError, DataExportResponse?> {
            dataExportService.getActiveExport(userId)
        }
        result.fold(
            ifLeft = { error -> call.respond(error.toStatusCode(), error.toResponse()) },
            ifRight = { export ->
                if (export == null) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(export)
                }
            },
        )
    }
}
```

Or simpler if `conduitAuth` can be adapted for nullable returns, use a similar pattern as the deletion status fix.

- [ ] **Step 5: Verify server compiles**

Run: `./gradlew :server:privacy:impl:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add server/privacy/ core/models/
git commit -m "feat(privacy): add GET /api/privacy/export/active endpoint"
```

### Task 11: Add SDK method and wire into ViewModel

**Files:**
- Modify: `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt`
- Modify: `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApiImpl.kt`
- Modify: `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakePrivacyApiBuilder.kt`
- Modify: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModel.kt`

- [ ] **Step 1: Add getActiveExport to PrivacyApi**

In `PrivacyApi.kt`, add:
```kotlin
suspend fun getActiveExport(): Either<AppError, DataExportResponse?>
```

- [ ] **Step 2: Implement in PrivacyApiImpl**

In `PrivacyApiImpl.kt`, add:
```kotlin
override suspend fun getActiveExport(): Either<AppError, DataExportResponse?> =
    Either.catch {
        val response = client.get(Privacy.ActiveExport())
        if (response.status == HttpStatusCode.NoContent) {
            null
        } else {
            response.body<DataExportResponse>()
        }
    }.mapLeft { AppError.Unknown(it.message ?: "Unknown error") }
```

- [ ] **Step 3: Add fake in FakePrivacyApiBuilder**

Add a `getActiveExport` stub that defaults to returning `null` (no active export).

- [ ] **Step 4: Load export status in PrivacySettingsViewModel.handleLoad()**

In `PrivacySettingsViewModel.kt`, add to `handleLoad()` (after the existing deferred calls):

```kotlin
val exportDeferred = viewModelScope.async { sdk.getActiveExport() }
```

And after the deletion deferred handling:
```kotlin
exportDeferred.await().fold(
    ifLeft = { /* ignore export status error */ },
    ifRight = { export ->
        if (export != null) {
            sendMutation(PrivacySettingsMutation.SetExportStatus(export))
        }
    },
)
```

- [ ] **Step 5: Run tests**

Run: `./gradlew :app:privacy:impl:jvmTest`
Expected: All tests pass

- [ ] **Step 6: Commit**

```bash
git add core/sdk/ core/testing/ app/privacy/
git commit -m "feat(privacy): load active export status on Privacy Settings init"
```

---

## Chunk 5: Marketing/Analytics Consent Toggles (UAT #8)

Make all consent types visible in Privacy Settings, not just the required ones.

### Task 12: Synthesize all consent types in server response

**Files:**
- Modify: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ConsentServiceImpl.kt`

The `getActiveConsents` method only returns consent types that have DB rows. For Marketing/Analytics (which are never explicitly granted), the server should return them with `granted = false`.

- [ ] **Step 1: Modify getActiveConsents to include all consent types**

Replace the `getActiveConsents` method in `ConsentServiceImpl.kt` (lines 29-43):

```kotlin
context(raise: Raise<DomainError>)
override suspend fun getActiveConsents(userId: String): List<ConsentStatus> {
    val uuid = Uuid.parse(userId)
    val records = consentRepository.findAllActiveByUser(uuid)
    val recordsByType = records.associateBy { it.consentType }

    return ConsentType.entries.map { type ->
        val record = recordsByType[type.name]
        ConsentStatus(
            type = type,
            granted = record?.granted ?: false,
            grantedAt = record?.createdAt?.toInstant(TimeZone.UTC)?.toString(),
            documentVersion = record?.legalDocumentVersion,
        )
    }
}
```

This iterates ALL `ConsentType` enum values and fills in defaults for missing ones.

- [ ] **Step 2: Verify server compiles**

Run: `./gradlew :server:privacy:impl:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ConsentServiceImpl.kt
git commit -m "fix(privacy): synthesize all consent types including Marketing/Analytics"
```

### Task 13: Add seed data for Marketing/Analytics legal documents

**Files:**
- Modify: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/Privacy.kt`

The `grantConsent()` method requires a legal document to exist for the consent type. We need seed documents for Marketing and Analytics.

- [ ] **Step 1: Add seed migration for Marketing and Analytics documents**

In `Privacy.kt`, add a new migration class:

```kotlin
class SeedMarketingAnalyticsDocumentsMigration : SchemaMigration(
    version = 20260315000002,
    description = "Seed legal documents for Marketing and Analytics consents",
) {
    override suspend fun migrate(database: R2dbcDatabase) {
        suspendTransaction(db = database) {
            LegalDocumentsTable.insert {
                it[id] = Uuid.random()
                it[type] = "MARKETING"
                it[version] = "1.0.0"
                it[locale] = "en"
                it[content] = "# Marketing Communications\n\nBy enabling this option, you consent to receive marketing communications including product updates, promotional offers, and newsletters. You can withdraw this consent at any time from your Privacy Settings."
                it[publishedAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }
            LegalDocumentsTable.insert {
                it[id] = Uuid.random()
                it[type] = "MARKETING"
                it[version] = "1.0.0"
                it[locale] = "es"
                it[content] = "# Comunicaciones de Marketing\n\nAl habilitar esta opción, consientes recibir comunicaciones de marketing, incluyendo actualizaciones de producto, ofertas promocionales y boletines informativos. Puedes retirar este consentimiento en cualquier momento desde la Configuración de Privacidad."
                it[publishedAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }
            LegalDocumentsTable.insert {
                it[id] = Uuid.random()
                it[type] = "ANALYTICS"
                it[version] = "1.0.0"
                it[locale] = "en"
                it[content] = "# Analytics Data Collection\n\nBy enabling this option, you consent to the collection and processing of analytics data to help us improve our services. This includes usage patterns and feature engagement. You can withdraw this consent at any time from your Privacy Settings."
                it[publishedAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }
            LegalDocumentsTable.insert {
                it[id] = Uuid.random()
                it[type] = "ANALYTICS"
                it[version] = "1.0.0"
                it[locale] = "es"
                it[content] = "# Recopilación de Datos Analíticos\n\nAl habilitar esta opción, consientes la recopilación y el procesamiento de datos analíticos para ayudarnos a mejorar nuestros servicios. Esto incluye patrones de uso e interacción con funcionalidades. Puedes retirar este consentimiento en cualquier momento desde la Configuración de Privacidad."
                it[publishedAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }
        }
    }
}
```

Register it in `registerPrivacyMigrations()`.

- [ ] **Step 2: Verify server compiles**

Run: `./gradlew :server:privacy:impl:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/Privacy.kt
git commit -m "feat(privacy): seed legal documents for Marketing and Analytics consents"
```

---

## Chunk 6: Privacy Settings Inline in Profile Dashboard (UAT #7)

Make Privacy Settings render inside the profile dashboard's main content area instead of navigating to a separate screen.

### Task 14: Add content slot to ProfileScreen

**Files:**
- Modify: `app/profile/impl/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileScreen.kt`
- Modify: `app/profile/wire/src/commonMain/kotlin/com/m2f/template/app/profile/wire/ProfileNavigation.kt`

- [ ] **Step 1: Add privacyContent slot to DesktopProfile**

In `ProfileScreen.kt`, add a `privacyContent` parameter to `DesktopProfile`:

```kotlin
@Composable
private fun DesktopProfile(
    // ... existing params ...
    privacyContent: (@Composable () -> Unit)? = null,
)
```

Also add it to the public `ProfileScreen` composable and propagate.

- [ ] **Step 2: Remove special-case for privacy in sidebar handler**

Replace the `onNavItemSelected` lambda in `DesktopProfile` (lines 207-212):

```kotlin
onNavItemSelected = { key ->
    if (key == "privacy") {
        onNavigateToPrivacy()
    } else {
        selectedNavItem = key
    }
},
```

With:

```kotlin
onNavItemSelected = { key ->
    selectedNavItem = key
},
```

- [ ] **Step 3: Add content switching based on selectedNavItem**

Replace the main content `Column` (lines 216-254) with a `when` switch:

```kotlin
Column(
    modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
        .verticalScroll(rememberScrollState())
        .padding(32.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp),
) {
    when (selectedNavItem) {
        "privacy" -> {
            privacyContent?.invoke()
        }
        else -> {
            // Existing profile content
            TerminalText(
                text = stringResource(Res.string.profile_back),
                style = typography.sm,
                color = colors.textMuted,
                modifier = Modifier.clickable(onClick = onBack),
            )
            ProfileHeader(state = state, onAvatarClick = onAvatarClick)
            if (state.isEditing) {
                EditProfileSection(/* ... */)
            } else {
                ProfileInfoCard(state = state, onStartEditing = onStartEditing)
            }
            if (localeSelector != null) { localeSelector() }
            TierContent(state = state)
        }
    }
}
```

- [ ] **Step 4: Wire privacy content in ProfileNavigation.kt**

In `ProfileNavigation.kt`, update the `ProfileScreen` call to pass `privacyContent`. This will require getting the PrivacySettings ViewModel and composing it inline:

```kotlin
privacyContent = {
    val privacyVm = koinViewModel<PrivacySettingsViewModel>()
    val privacyState by privacyVm.model.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { privacyVm.take(PrivacySettingsIntent.Load) }
    PrivacySettingsContent(
        state = privacyState,
        onRequestExport = { privacyVm.take(PrivacySettingsIntent.RequestExport) },
        onDownloadExport = { privacyVm.take(PrivacySettingsIntent.DownloadExport) },
        onRequestDeletion = { backStack.add(AccountDeletionRoute) },
        onViewDocument = { type -> backStack.add(LegalDocumentRoute(type.name)) },
        onWithdrawConsent = { type -> privacyVm.take(PrivacySettingsIntent.WithdrawConsent(type)) },
        onBack = { /* no-op, sidebar handles navigation */ },
    )
},
```

Note: This requires `PrivacySettingsContent` (the inner content composable) to be accessible from the profile wire module. If it's internal to the privacy impl module, expose it through the privacy wire module or create a composable factory.

Add `implementation(projects.app.privacy.wire)` to `app/profile/wire/build.gradle.kts` dependencies if not present.

- [ ] **Step 5: Keep mobile navigation as full-screen (fallback)**

For mobile layout (no sidebar), keep the existing `onNavigateToPrivacy` callback so it navigates to the full-screen `PrivacySettingsRoute`. The `MobileProfile` composable should still use `onNavigateToPrivacy`.

- [ ] **Step 6: Remove NavigateToPrivacySettings event handler (desktop only)**

In `ProfileNavigation.kt`, remove or guard the `ProfileEvent.NavigateToPrivacySettings` handler (lines 50-52). This event was used for the desktop sidebar click, which is now handled inline.

- [ ] **Step 7: Verify it compiles**

Run: `./gradlew :app:profile:wire:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add app/profile/ app/privacy/
git commit -m "fix(profile): render Privacy Settings inline in profile dashboard sidebar"
```

---

## Chunk 7: Server Privacy Tests (UAT #14)

The server privacy module has no tests. Add integration tests for the core services.

### Task 15: Write server privacy service tests

**Files:**
- Create: `server/privacy/impl/src/test/kotlin/com/m2f/server/privacy/service/ConsentServiceTest.kt`
- Create: `server/privacy/impl/src/test/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceTest.kt`

Follow existing server test patterns in the codebase (check `server/auth/impl/src/test/` for reference).

- [ ] **Step 1: Check existing server test patterns**

Read `server/auth/impl/src/test/` to understand the test setup (database, DI, assertions). Follow the same pattern.

- [ ] **Step 2: Write ConsentService tests**

Test cases:
- `getActiveConsents returns all consent types with defaults for missing records`
- `grantConsent creates a record and can be retrieved`
- `withdrawConsent creates a withdrawal record`
- `getRequiredConsents returns outdated when no consents exist`
- `getRequiredConsents returns up-to-date when all consents match current version`
- `getActiveConsents deduplicates by type returning latest`

- [ ] **Step 3: Write AccountDeletionService tests**

Test cases:
- `requestDeletion with valid password creates pending deletion`
- `requestDeletion with null passwordHash raises InvalidCredentials`
- `requestDeletion with wrong password raises InvalidCredentials`
- `requestDeletion when deletion already pending raises DeletionAlreadyPending`
- `getDeletionStatus returns null when no deletion pending`
- `cancelDeletion cancels a pending deletion`

- [ ] **Step 4: Run tests**

Run: `./gradlew :server:privacy:impl:test`
Expected: All tests pass

- [ ] **Step 5: Commit**

```bash
git add server/privacy/impl/src/test/
git commit -m "test(privacy): add server privacy service integration tests"
```

---

## Verification

After all chunks are complete:

- [ ] **Run full test suite:** `./gradlew testAll`
- [ ] **Run detekt:** `./gradlew detekt`
- [ ] **Start server and verify manually:** `./gradlew devUp && ./gradlew :server:run`
- [ ] **Re-run UAT for all previously failed tests**
