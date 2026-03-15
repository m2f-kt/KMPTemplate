---
phase: quick-9
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionModel.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionMutation.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionIntent.kt
  - app/privacy/impl/src/commonMain/composeResources/values/strings.xml
  - app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml
  - core/models/src/commonMain/kotlin/com/m2f/template/models/dto/privacy/PrivacyDtos.kt
  - core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt
  - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt
  - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApiImpl.kt
  - server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/AccountDeletionService.kt
  - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceImpl.kt
  - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/DeletionRoutes.kt
autonomous: true
must_haves:
  truths:
    - "Navigating back from deletion flow and re-entering shows step 1 (WARNING) with empty password/reason/error"
    - "All 5 steps show consistent step counter at top of main content (STEP N OF 5)"
    - "No step titles use red/error color -- all titles use colors.text"
    - "Re-auth step validates password against server before proceeding"
    - "Confirmation step sends confirmation token (not raw password) to request deletion"
  artifacts:
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/DeletionRoutes.kt"
      provides: "POST verify-password endpoint"
    - path: "core/models/src/commonMain/kotlin/com/m2f/template/models/dto/privacy/PrivacyDtos.kt"
      provides: "VerifyPasswordRequest, VerifyPasswordResponse DTOs"
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionModel.kt"
      provides: "confirmationToken field"
  key_links:
    - from: "AccountDeletionViewModel.handleReAuthenticate"
      to: "sdk.verifyPassword"
      via: "API call returning confirmation token"
    - from: "AccountDeletionViewModel.handleConfirmDeletion"
      to: "sdk.requestAccountDeletion"
      via: "DeletionRequest with confirmationToken instead of password"
---

<objective>
Fix 4 issues in the account deletion flow: (1) reset state on navigation back, (2) consistent step counters across all 5 steps, (3) fix red title colors, and (4) add server-side password validation with confirmation token before deletion.

Purpose: Improve UX consistency and security of the deletion flow.
Output: Working deletion flow with proper state reset, consistent UI, and server-validated password step.
</objective>

<execution_context>
@/Users/marc/.claude/get-shit-done/workflows/execute-plan.md
@/Users/marc/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionModel.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionMutation.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionIntent.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionEvent.kt
@app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt
@app/privacy/impl/src/commonMain/composeResources/values/strings.xml
@app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml
@core/models/src/commonMain/kotlin/com/m2f/template/models/dto/privacy/PrivacyDtos.kt
@core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt
@core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt
@core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApiImpl.kt
@server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/AccountDeletionService.kt
@server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceImpl.kt
@server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/DeletionRoutes.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Fix UI issues -- state reset on load, consistent step counters, red title colors</name>
  <files>
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
    app/privacy/impl/src/commonMain/composeResources/values/strings.xml
    app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml
  </files>
  <action>
**A. State reset on navigation back (AccountDeletionViewModel.kt):**

In `handleLoad()`, add reset mutations BEFORE the profile/status fetches. Insert at the top of `handleLoad()`:
```kotlin
sendMutation(AccountDeletionMutation.SetStep(DeletionStep.WARNING))
sendMutation(AccountDeletionMutation.SetPassword(""))
sendMutation(AccountDeletionMutation.SetReason(""))
sendMutation(AccountDeletionMutation.SetError(null))
```
This ensures every time the screen enters composition and fires `LaunchedEffect` -> `Load`, the form resets to defaults. The subsequent `getDeletionStatus()` call will override step to SCHEDULED if a pending deletion exists.

**B. Consistent step counters (AccountDeletionScreen.kt):**

Add a step counter label at the top of each step composable's content (before the title). For steps 1-3, add:
```kotlin
TerminalText(
    text = stringResource(Res.string.privacy_deletion_step_N),
    style = typography.xs.copy(fontSize = 10.sp, letterSpacing = 2.sp),
    color = colors.textDim,
)
```
Where N = 1, 2, 3 respectively.

Steps 4 and 5 already have step labels (`privacy_deletion_confirm_step` and `privacy_deletion_scheduled_step`). Leave those as-is.

In `DesktopRightPanel`, remove the `PanelStepIndicator(...)` calls for steps 1-3 (lines calling `PanelStepIndicator` in WARNING, RE_AUTH, and REASON branches). The step indicator is now in the main content only.

**C. Fix red title colors (AccountDeletionScreen.kt):**

- In `WarningStep()`, change `color = colors.error` to `color = colors.text` on the title TerminalText (line ~212).
- In `ConfirmStep()`, change `color = colors.error` to `color = colors.text` on the title TerminalText (line ~435).

**D. String resources (strings.xml -- both EN and ES):**

Add to EN strings.xml (after the Step 1 warning section comments):
```xml
<string name="privacy_deletion_step_1">STEP 1 OF 5</string>
<string name="privacy_deletion_step_2">STEP 2 OF 5</string>
<string name="privacy_deletion_step_3">STEP 3 OF 5</string>
```

Add to ES strings.xml:
```xml
<string name="privacy_deletion_step_1">PASO 1 DE 5</string>
<string name="privacy_deletion_step_2">PASO 2 DE 5</string>
<string name="privacy_deletion_step_3">PASO 3 DE 5</string>
```

Also update the desktop panel step indicators from "STEP 01 / 03" to "STEP N / 05" format in both EN and ES:
- EN: `privacy_deletion_desktop_step1_indicator` -> `STEP 01 / 05 — ACCOUNT DELETION`
- EN: `privacy_deletion_desktop_step2_indicator` -> `STEP 02 / 05 — VERIFY IDENTITY`
- EN: `privacy_deletion_desktop_step3_indicator` -> `STEP 03 / 05 — FEEDBACK`
- ES: Same pattern with PASO.
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:privacy:impl:compileKotlinDesktop 2>&1 | tail -5</automated>
  </verify>
  <done>
    - handleLoad() resets password, reason, error, step to WARNING before fetching status
    - All 5 steps have "STEP N OF 5" at the top of main content
    - Desktop right panel no longer has duplicate step indicators for steps 1-3
    - No red title colors -- all step titles use colors.text
    - EN and ES string resources added/updated for step counters
  </done>
</task>

<task type="auto">
  <name>Task 2: Add server-side password verification endpoint with confirmation token</name>
  <files>
    core/models/src/commonMain/kotlin/com/m2f/template/models/dto/privacy/PrivacyDtos.kt
    core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt
    server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/AccountDeletionService.kt
    server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceImpl.kt
    server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/DeletionRoutes.kt
  </files>
  <action>
**A. Shared DTOs (PrivacyDtos.kt):**

Add two new DTOs:
```kotlin
@Serializable
data class VerifyPasswordRequest(val password: String)

@Serializable
data class VerifyPasswordResponse(val confirmationToken: String)
```

Change `DeletionRequest` to use `confirmationToken` instead of `password`:
```kotlin
@Serializable
data class DeletionRequest(
    val confirmationToken: String,
    val reason: String? = null,
)
```

**B. API route (ApiRoutes.kt):**

Add inside the `Privacy` class:
```kotlin
@Serializable @Resource("deletion/verify-password")
class VerifyPassword(val parent: Privacy = Privacy())
```

**C. Service contract (AccountDeletionService.kt):**

Add a new method:
```kotlin
context(raise: Raise<DomainError>)
suspend fun verifyPasswordForDeletion(userId: String, password: String): String
```

**D. Service implementation (AccountDeletionServiceImpl.kt):**

Add an in-memory token store at the class level:
```kotlin
private data class DeletionToken(val token: String, val expiresAt: kotlinx.datetime.Instant)
private val deletionTokens = java.util.concurrent.ConcurrentHashMap<String, DeletionToken>()
```

Implement `verifyPasswordForDeletion`:
1. Parse userId to UUID
2. Fetch user from userRepository.findById(uuid)
3. ensureNotNull(user) { InvalidCredentials() }
4. ensureNotNull(user.passwordHash) { InvalidCredentials() }
5. ensure(passwordHasher.verify(password, user.passwordHash)) { InvalidCredentials() }
6. Generate token = Uuid.random().toString()
7. Store in deletionTokens: userId -> DeletionToken(token, Clock.System.now().plus(15.minutes))
8. Return token

Modify `requestDeletion` to validate confirmationToken instead of password:
1. Remove password verification logic (lines 43-52 that fetch user and verify password)
2. Instead: look up token from deletionTokens[userId]
3. ensureNotNull(token) { InvalidCredentials() } -- reuse InvalidCredentials or create a new error
4. ensure(token.token == request.confirmationToken) { InvalidCredentials() }
5. ensure(token.expiresAt > Clock.System.now()) { InvalidCredentials() }
6. After successful validation, remove token: deletionTokens.remove(userId)
7. Keep the rest of the method as-is (existingDeletion check, insert, return)

Import `kotlin.time.Duration.Companion.minutes`.

**E. Route (DeletionRoutes.kt):**

Add a new route inside the `authenticate` block:
```kotlin
post<Privacy.VerifyPassword> {
    conduitAuth { userId ->
        val request = getModel<VerifyPasswordRequest>()
        val token = accountDeletionService.verifyPasswordForDeletion(userId, request.password)
        VerifyPasswordResponse(confirmationToken = token)
    }
}
```

Add imports for `VerifyPasswordRequest`, `VerifyPasswordResponse`.
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :server:privacy:impl:compileKotlin :core:models:compileKotlinDesktop 2>&1 | tail -5</automated>
  </verify>
  <done>
    - POST /api/privacy/deletion/verify-password endpoint exists, accepts VerifyPasswordRequest, returns VerifyPasswordResponse with token
    - DeletionRequest now uses confirmationToken field (not password)
    - requestDeletion validates token from in-memory store instead of re-verifying password
    - Token expires after 15 minutes and is single-use (removed after successful deletion request)
  </done>
</task>

<task type="auto">
  <name>Task 3: Wire client SDK and ViewModel to use password verification endpoint</name>
  <files>
    core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt
    core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApiImpl.kt
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionModel.kt
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionMutation.kt
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionIntent.kt
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
  </files>
  <action>
**A. SDK interface (PrivacyApi.kt):**

Add method:
```kotlin
suspend fun verifyPasswordForDeletion(request: VerifyPasswordRequest): Either<AppError, VerifyPasswordResponse>
```
Add imports for `VerifyPasswordRequest`, `VerifyPasswordResponse`.

**B. SDK implementation (PrivacyApiImpl.kt):**

Add:
```kotlin
override suspend fun verifyPasswordForDeletion(request: VerifyPasswordRequest): Either<AppError, VerifyPasswordResponse> =
    apiCall {
        client.post(Privacy.VerifyPassword()) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
```
Add imports for `VerifyPasswordRequest`, `VerifyPasswordResponse`, `Privacy`.

**C. Model (AccountDeletionModel.kt):**

Add `confirmationToken: String = ""` field to `AccountDeletionModel`. Keep `password` field for UI input state.

**D. Mutation (AccountDeletionMutation.kt):**

Add:
```kotlin
data class SetConfirmationToken(val token: String) : AccountDeletionMutation
```

**E. Intent -- no changes needed.** `ReAuthenticate(password)` intent is already correct -- it sends the password to be verified.

**F. ViewModel (AccountDeletionViewModel.kt):**

1. In `handleLoad()` reset mutations (from Task 1), also add:
   ```kotlin
   sendMutation(AccountDeletionMutation.SetConfirmationToken(""))
   ```

2. Rewrite `handleReAuthenticate(password)`:
   ```kotlin
   private suspend fun handleReAuthenticate(password: String) {
       sendMutation(AccountDeletionMutation.SetLoading(true))
       sdk.verifyPasswordForDeletion(VerifyPasswordRequest(password)).fold(
           ifLeft = { error ->
               val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
               sendMutation(AccountDeletionMutation.SetError(key))
           },
           ifRight = { response ->
               sendMutation(AccountDeletionMutation.SetConfirmationToken(response.confirmationToken))
               sendMutation(AccountDeletionMutation.SetLoading(false))
               sendMutation(AccountDeletionMutation.SetStep(DeletionStep.REASON))
           },
       )
   }
   ```
   Remove the old SetPassword mutation call. Import `VerifyPasswordRequest`.

3. Rewrite `handleConfirmDeletion()` to use confirmationToken:
   ```kotlin
   private suspend fun handleConfirmDeletion() {
       sendMutation(AccountDeletionMutation.SetLoading(true))
       val currentModel = model.value
       sdk.requestAccountDeletion(
           DeletionRequest(
               confirmationToken = currentModel.confirmationToken,
               reason = currentModel.reason.ifBlank { null },
           )
       ).fold(
           ifLeft = { error ->
               val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
               sendMutation(AccountDeletionMutation.SetError(key))
           },
           ifRight = {
               sendMutation(AccountDeletionMutation.SetLoading(false))
               sdk.logout()
               sendEvent(AccountDeletionEvent.NavigateToLogin)
           },
       )
   }
   ```

4. Add reduce case for the new mutation:
   ```kotlin
   is AccountDeletionMutation.SetConfirmationToken -> model.copy(confirmationToken = mutation.token)
   ```

5. The `SetPassword` mutation and `password` field in the model can be kept for backward compat, but `handleReAuthenticate` no longer calls `SetPassword`. If you prefer, remove `SetPassword` from mutations and `password` from model since the ReAuthStep composable uses local `remember { mutableStateOf("") }` for password input -- it never reads from model.password. Clean it up: remove `password: String = ""` from model and `SetPassword` from mutation.
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:privacy:impl:compileKotlinDesktop :core:sdk:compileKotlinDesktop 2>&1 | tail -5</automated>
  </verify>
  <done>
    - PrivacyApi and PrivacyApiImpl have verifyPasswordForDeletion method
    - AccountDeletionModel has confirmationToken field (password field removed as unused)
    - handleReAuthenticate calls server to verify password, stores token on success, shows error on failure
    - handleConfirmDeletion sends confirmationToken (not password) in DeletionRequest
    - All compiles successfully
  </done>
</task>

</tasks>

<verification>
Run full compilation to ensure no breakages across modules:
```bash
./gradlew :app:privacy:impl:compileKotlinDesktop :core:sdk:compileKotlinDesktop :core:models:compileKotlinDesktop :server:privacy:impl:compileKotlin
```
</verification>

<success_criteria>
- Entering the deletion screen always starts at step 1 with clean state
- All 5 steps display "STEP N OF 5" at top of main content
- Desktop right panel step indicators updated to "STEP N / 05" (no duplicates for steps 1-3)
- All step titles use consistent text color (no red)
- Password is validated server-side at step 2, returning a confirmation token
- Deletion request at step 4 uses the confirmation token, not the raw password
- All modules compile without errors
</success_criteria>

<output>
After completion, create `.planning/quick/9-fix-deletion-flow-reset-on-back-consiste/9-SUMMARY.md`
</output>
