---
phase: quick-fix
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionIntent.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
  - app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt
  - app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt
autonomous: true
requirements: [BUG-DELETE-REAUTH, BUG-DELETE-CANCEL]

must_haves:
  truths:
    - "WarningStep 'Continue' button advances to RE_AUTH step (password entry), not directly to REASON"
    - "User enters password in RE_AUTH step before proceeding to REASON step"
    - "ConfirmStep 'Cancel' button navigates back instead of calling cancelDeletion API"
    - "cancelDeletion API is only invoked from ScheduledStep (where a pending deletion exists)"
  artifacts:
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionIntent.kt"
      provides: "ProceedToReAuth intent"
      contains: "ProceedToReAuth"
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt"
      provides: "Handler for ProceedToReAuth that only advances step"
      contains: "handleProceedToReAuth"
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt"
      provides: "Fixed WarningStep and ConfirmStep callbacks"
    - path: "app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt"
      provides: "Test for ProceedToReAuth intent"
      contains: "ProceedToReAuth"
  key_links:
    - from: "AccountDeletionScreen.kt (WarningStep)"
      to: "AccountDeletionViewModel (ProceedToReAuth)"
      via: "onProceedToReAuth callback"
      pattern: "onProceedToReAuth"
    - from: "AccountDeletionScreen.kt (ConfirmStep)"
      to: "backStack navigation"
      via: "onBack callback (not onCancelDeletion)"
      pattern: "onCancel = onBack"
    - from: "PrivacyNavigation.kt"
      to: "AccountDeletionIntent.ProceedToReAuth"
      via: "new onProceedToReAuth wiring"
      pattern: "ProceedToReAuth"
---

<objective>
Fix two bugs in the account deletion flow: (1) WarningStep skips the RE_AUTH password entry step by calling `onReAuthenticate("")`, and (2) ConfirmStep's cancel button incorrectly calls the `cancelDeletion` API when no deletion has been scheduled yet.

Purpose: Users cannot delete their accounts because the password is never captured, causing server AUTH_INVALID_CREDENTIALS errors. Additionally, pressing cancel on the confirm step triggers a 500 error.
Output: Working account deletion flow where WARNING -> RE_AUTH -> REASON -> CONFIRM proceeds correctly, and cancel on ConfirmStep navigates back safely.
</objective>

<execution_context>
@/Users/marc/.claude/get-shit-done/workflows/execute-plan.md
@/Users/marc/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionIntent.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
@app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt
@app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt

<interfaces>
<!-- Existing MVI contracts the executor needs -->

From AccountDeletionIntent.kt:
```kotlin
sealed interface AccountDeletionIntent {
    data object Load : AccountDeletionIntent
    data class ReAuthenticate(val password: String) : AccountDeletionIntent
    data class SetReason(val reason: String) : AccountDeletionIntent
    data object ConfirmDeletion : AccountDeletionIntent
    data object CancelDeletion : AccountDeletionIntent
}
```

From AccountDeletionViewModel.kt:
```kotlin
// handleReAuthenticate stores password AND advances to REASON
private suspend fun handleReAuthenticate(password: String) {
    sendMutation(AccountDeletionMutation.SetPassword(password))
    sendMutation(AccountDeletionMutation.SetStep(DeletionStep.REASON))
}
```

From AccountDeletionScreen.kt (the bugs):
```kotlin
// Line 137 - BUG 1: calls onReAuthenticate("") skipping RE_AUTH step
DeletionStep.WARNING -> WarningStep(
    onContinue = { onReAuthenticate("") },
    ...
)

// Line 156 - BUG 2: cancel calls cancelDeletion API (no pending deletion = 500)
DeletionStep.CONFIRM -> ConfirmStep(
    onCancel = onCancelDeletion,
    ...
)
```

From PrivacyNavigation.kt (AccountDeletionRoute entry):
```kotlin
AccountDeletionScreen(
    state = state,
    onReAuthenticate = { viewModel.take(AccountDeletionIntent.ReAuthenticate(it)) },
    onSetReason = { viewModel.take(AccountDeletionIntent.SetReason(it)) },
    onConfirmDeletion = { viewModel.take(AccountDeletionIntent.ConfirmDeletion) },
    onCancelDeletion = { viewModel.take(AccountDeletionIntent.CancelDeletion) },
    onBack = { backStack.removeLastOrNull() },
)
```
</interfaces>
</context>

<tasks>

<task type="auto" tdd="true">
  <name>Task 1: Add ProceedToReAuth intent and fix ViewModel</name>
  <files>
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionIntent.kt,
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt,
    app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt
  </files>
  <behavior>
    - Test: ProceedToReAuth intent advances step from WARNING to RE_AUTH without setting password (password remains empty string default)
    - Test: Existing ReAuthenticate("password") test still passes (sets password AND advances to REASON)
    - Test: Full flow WARNING->RE_AUTH->REASON->CONFIRM->Scheduled works end-to-end with ProceedToReAuth as first step
  </behavior>
  <action>
    1. In AccountDeletionIntent.kt: Add `data object ProceedToReAuth : AccountDeletionIntent` to the sealed interface.

    2. In AccountDeletionViewModel.kt:
       - Add `is AccountDeletionIntent.ProceedToReAuth -> handleProceedToReAuth()` to the `when` in `take()`.
       - Add handler: `private suspend fun handleProceedToReAuth() { sendMutation(AccountDeletionMutation.SetStep(DeletionStep.RE_AUTH)) }` — this ONLY advances the step, does NOT touch password.

    3. In AccountDeletionViewModelTest.kt:
       - Add test: `proceedToReAuth advances to RE_AUTH step without setting password` — sends ProceedToReAuth, asserts model has step=RE_AUTH and password="" (default).
       - Add test: `full deletion flow with proceedToReAuth` — sends ProceedToReAuth -> ReAuthenticate("pass") -> SetReason("reason") -> ConfirmDeletion, asserts DeletionScheduled event. This validates the complete happy path with the new intent.
       - Verify existing `reAuthenticate sets password and advances to REASON step` test still passes unchanged.
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:privacy:impl:allTests --tests "com.m2f.template.app.privacy.AccountDeletionViewModelTest" --no-daemon</automated>
  </verify>
  <done>ProceedToReAuth intent exists, ViewModel handles it by advancing to RE_AUTH only, all tests pass including new ones and existing ones.</done>
</task>

<task type="auto">
  <name>Task 2: Fix Screen callbacks and Navigation wiring</name>
  <files>
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt,
    app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt
  </files>
  <action>
    1. In AccountDeletionScreen.kt:
       - Add `onProceedToReAuth: () -> Unit` parameter to both `AccountDeletionScreen` and `AccountDeletionContent` composables (add after `onReAuthenticate` parameter for readability).
       - Pass `onProceedToReAuth` through from `AccountDeletionScreen` to `AccountDeletionContent` (same pattern as other callbacks).
       - Fix line 137: Change WarningStep from `onContinue = { onReAuthenticate("") }` to `onContinue = onProceedToReAuth`.
       - Fix line 156: Change ConfirmStep from `onCancel = onCancelDeletion` to `onCancel = onBack`. The cancel button at CONFIRM step should navigate away, not call the cancel API.
       - ScheduledStep (line 160-163) remains unchanged — it correctly uses `onCancelDeletion` because at that step a pending deletion actually exists on the server.

    2. In PrivacyNavigation.kt:
       - In the `entry<AccountDeletionRoute>` block, add the new callback wiring to `AccountDeletionScreen`:
         `onProceedToReAuth = { viewModel.take(AccountDeletionIntent.ProceedToReAuth) },`
       - Place it after the `onReAuthenticate` line for consistency.
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:privacy:impl:allTests :app:privacy:wire:allTests --no-daemon</automated>
  </verify>
  <done>WarningStep "Continue" calls onProceedToReAuth (advancing to RE_AUTH step). ConfirmStep "Cancel" calls onBack (navigating away, not calling API). PrivacyNavigation wires ProceedToReAuth intent. Compilation succeeds for both impl and wire modules.</done>
</task>

</tasks>

<verification>
1. Run full privacy module tests: `./gradlew :app:privacy:impl:allTests :app:privacy:wire:allTests`
2. Run detekt on modified files: `./gradlew detekt`
3. Verify the deletion flow logic: WARNING -> (ProceedToReAuth) -> RE_AUTH -> (ReAuthenticate with password) -> REASON -> (SetReason) -> CONFIRM -> (ConfirmDeletion) -> DeletionScheduled event
4. Verify ConfirmStep cancel does NOT call cancelDeletion API
</verification>

<success_criteria>
- All existing AccountDeletionViewModelTest tests pass
- New ProceedToReAuth tests pass
- WarningStep calls onProceedToReAuth instead of onReAuthenticate("")
- ConfirmStep cancel calls onBack instead of onCancelDeletion
- PrivacyNavigation correctly wires the new ProceedToReAuth intent
- No compilation errors across privacy module (impl + wire)
</success_criteria>

<output>
After completion, create `.planning/quick/1-fix-delete-account-feature-missing-passw/1-SUMMARY.md`
</output>
