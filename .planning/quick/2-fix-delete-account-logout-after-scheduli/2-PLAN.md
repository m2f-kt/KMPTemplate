---
phase: quick
plan: 2
type: execute
wave: 1
depends_on: []
files_modified:
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
  - app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt
autonomous: true
must_haves:
  truths:
    - "After confirming account deletion, user is logged out and sent to the login screen"
    - "After cancelling a scheduled deletion, the UI resets to the WARNING step with no pending deletion"
  artifacts:
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt"
      provides: "Fixed handleConfirmDeletion and handleCancelDeletion logic"
    - path: "app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt"
      provides: "Updated tests verifying logout-on-delete and state-reset-on-cancel"
  key_links:
    - from: "AccountDeletionViewModel.handleConfirmDeletion"
      to: "sdk.logout()"
      via: "calls logout before emitting NavigateToLogin"
      pattern: "sdk\\.logout\\(\\)"
    - from: "AccountDeletionViewModel.handleCancelDeletion"
      to: "AccountDeletionMutation.SetPendingDeletion(null)"
      via: "resets model state after successful cancellation"
      pattern: "SetPendingDeletion\\(null\\)"
---

<objective>
Fix two bugs in AccountDeletionViewModel: (1) after confirming deletion, call sdk.logout() and emit NavigateToLogin instead of DeletionScheduled, and (2) after cancelling deletion, reset pendingDeletion to null and step to WARNING before emitting DeletionCancelled.

Purpose: Deletion confirmation must log the user out (clear tokens, redirect to login). Cancellation must visually reset the UI so the user sees the initial warning state, not stale scheduled-deletion data.
Output: Fixed ViewModel + updated tests.
</objective>

<execution_context>
@/Users/marc/.claude/get-shit-done/workflows/execute-plan.md
@/Users/marc/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionEvent.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionMutation.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionModel.kt
@app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt

<interfaces>
<!-- Existing types the executor needs -->

From AccountDeletionEvent.kt:
```kotlin
sealed interface AccountDeletionEvent {
    data object DeletionScheduled : AccountDeletionEvent  // OLD - stop using in confirmDeletion
    data object DeletionCancelled : AccountDeletionEvent
    data object NavigateToLogin : AccountDeletionEvent     // USE THIS for confirm deletion
    data class ShowError(val message: String) : AccountDeletionEvent
}
```

From AccountDeletionMutation.kt:
```kotlin
sealed interface AccountDeletionMutation {
    data class SetStep(val step: DeletionStep) : AccountDeletionMutation
    data class SetPendingDeletion(val deletion: DeletionResponse?) : AccountDeletionMutation  // nullable!
    data class SetLoading(val loading: Boolean) : AccountDeletionMutation
    // ... others unchanged
}
```

From AccountDeletionModel.kt:
```kotlin
data class AccountDeletionModel(
    val step: DeletionStep = DeletionStep.WARNING,  // default is WARNING
    val pendingDeletion: DeletionResponse? = null,   // default is null
    // ...
)
```

SDK logout pattern (from AuthApiImpl):
```kotlin
// sdk.logout() returns Either<AppError, Unit>
// Always clears tokens regardless of server response
```

FakeSdk auth logout pattern:
```kotlin
fakeSdk {
    auth { logout { Either.Right(Unit) } }
    privacy { ... }
}
```
</interfaces>
</context>

<tasks>

<task type="auto" tdd="true">
  <name>Task 1: Update tests to expect logout + NavigateToLogin on confirm, and state reset on cancel</name>
  <files>app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt</files>
  <behavior>
    - confirmDeletion test: after successful deletion request, sdk.logout() is called, event is NavigateToLogin (not DeletionScheduled)
    - full deletion flow test: same -- event is NavigateToLogin
    - cancelDeletion test: after successful cancel, model resets to step=WARNING with pendingDeletion=null, then DeletionCancelled event emits
  </behavior>
  <action>
Update 3 existing tests in AccountDeletionViewModelTest.kt:

1. `confirmDeletion calls SDK and emits DeletionScheduled` -- rename to reflect logout behavior. Add `auth { logout { Either.Right(Unit) } }` to fakeSdk block. Change `event(AccountDeletionEvent.DeletionScheduled)` to `event(AccountDeletionEvent.NavigateToLogin)`.

2. `full deletion flow with proceedToReAuth` -- same changes: add auth logout to fakeSdk, change final event from DeletionScheduled to NavigateToLogin.

3. `cancelDeletion calls SDK and emits DeletionCancelled` -- after the CancelDeletion intent, add `model(AccountDeletionModel())` assertion BEFORE the `event(AccountDeletionEvent.DeletionCancelled)` assertion, to verify the model resets to default state (step=WARNING, pendingDeletion=null, loading=false).

Run tests -- they MUST fail (RED phase) since the ViewModel hasn't been updated yet.
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:privacy:impl:allTests 2>&1 | tail -30</automated>
  </verify>
  <done>Tests updated and failing (RED). confirmDeletion tests expect NavigateToLogin event. cancelDeletion test expects model reset to default state.</done>
</task>

<task type="auto">
  <name>Task 2: Fix AccountDeletionViewModel -- logout on confirm, reset state on cancel</name>
  <files>app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt</files>
  <action>
Modify two methods in AccountDeletionViewModel.kt:

1. **handleConfirmDeletion** -- in the `ifRight` branch (lines 71-74), replace current logic with:
   - `sendMutation(AccountDeletionMutation.SetLoading(false))`
   - `sdk.logout()` -- call logout to clear tokens (ignore result, tokens clear regardless)
   - `sendEvent(AccountDeletionEvent.NavigateToLogin)` -- instead of DeletionScheduled

2. **handleCancelDeletion** -- in the `ifRight` branch (lines 85-87), replace current logic with:
   - `sendMutation(AccountDeletionMutation.SetLoading(false))`
   - `sendMutation(AccountDeletionMutation.SetPendingDeletion(null))` -- clear pending deletion
   - `sendMutation(AccountDeletionMutation.SetStep(DeletionStep.WARNING))` -- reset to initial step
   - `sendEvent(AccountDeletionEvent.DeletionCancelled)`

Run tests -- they MUST pass (GREEN phase).
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:privacy:impl:allTests 2>&1 | tail -30</automated>
  </verify>
  <done>All AccountDeletionViewModelTest tests pass. Confirm deletion logs out and navigates to login. Cancel deletion resets UI state to WARNING with no pending deletion.</done>
</task>

</tasks>

<verification>
- `./gradlew :app:privacy:impl:allTests` passes all tests
- confirmDeletion tests assert NavigateToLogin event (not DeletionScheduled)
- cancelDeletion test asserts model reset to default before DeletionCancelled event
- No other tests broken: `./gradlew :app:privacy:allTests`
</verification>

<success_criteria>
- After confirming account deletion, sdk.logout() is called and NavigateToLogin event is emitted
- After cancelling deletion, model state resets (step=WARNING, pendingDeletion=null) before DeletionCancelled event
- All privacy module tests pass
</success_criteria>

<output>
After completion, create `.planning/quick/2-fix-delete-account-logout-after-scheduli/2-SUMMARY.md`
</output>
