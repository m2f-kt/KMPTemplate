---
phase: quick-3
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelIntent.kt
  - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelModel.kt
  - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelMutation.kt
  - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelViewModel.kt
  - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
  - app/admin/src/commonMain/composeResources/values/strings.xml
  - app/admin/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt
  - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
autonomous: true
requirements: [QUICK-3]

must_haves:
  truths:
    - "Admin can see a Remove button next to each non-owner member in the members table"
    - "Admin can confirm member removal via a confirmation dialog before it executes"
    - "After successful removal, the member list refreshes and the removed member is gone"
    - "The group owner cannot be removed (no Remove button shown for Owner role)"
    - "If removal fails, the dialog closes and the error is handled gracefully"
  artifacts:
    - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelIntent.kt"
      provides: "ConfirmRemoveMember, CancelRemoveMember, ExecuteRemoveMember intents"
      contains: "ConfirmRemoveMember"
    - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelModel.kt"
      provides: "Remove member dialog state fields"
      contains: "removeMemberTarget"
    - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelMutation.kt"
      provides: "ShowRemoveMemberDialog, HideRemoveMemberDialog, SetRemovingMember, RemoveMemberFromList mutations"
      contains: "ShowRemoveMemberDialog"
    - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelViewModel.kt"
      provides: "handleRemoveMember logic calling sdk.removeMember + list refresh"
      contains: "handleRemoveMember"
    - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt"
      provides: "ACTIONS column in members table with Remove button + RemoveMemberDialog"
      contains: "RemoveMemberDialog"
    - path: "app/admin/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt"
      provides: "Tests for remove member success and failure flows"
      contains: "ExecuteRemoveMember"
  key_links:
    - from: "AdminPanelViewModel"
      to: "sdk.removeMember"
      via: "handleRemoveMember calls sdk.removeMember(groupId, userId)"
      pattern: "sdk\\.removeMember"
    - from: "AdminPanelScreen"
      to: "AdminPanelViewModel"
      via: "onConfirmRemoveMember/onExecuteRemoveMember callbacks wired in AppNavHost"
      pattern: "ConfirmRemoveMember|ExecuteRemoveMember"
    - from: "AppNavHost"
      to: "AdminPanelIntent"
      via: "Lambda callbacks dispatching remove member intents"
      pattern: "RemoveMember"
---

<objective>
Add "Remove Member" functionality to the Admin Panel so group admins/owners can remove non-owner members from their group via a confirmation dialog.

Purpose: The server-side `removeMember` API and SDK client (`sdk.removeMember`) are already fully implemented. This task wires the UI layer — MVI types, ViewModel handler, confirmation dialog, members table ACTIONS column, and tests.

Output: Working remove-member flow in the admin panel with confirmation dialog, following the exact same pattern as the existing revoke-invitation flow.
</objective>

<execution_context>
@/Users/marc/.config/opencode/get-shit-done/workflows/execute-plan.md
@/Users/marc/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelIntent.kt
@app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelModel.kt
@app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelMutation.kt
@app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelViewModel.kt
@app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelEvent.kt
@app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
@app/admin/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt
@app/admin/src/commonMain/composeResources/values/strings.xml
@composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
@core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/GroupApi.kt
@core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeGroupApiBuilder.kt

<interfaces>
<!-- The SDK removeMember method is already fully wired — server, SDK client, and fake builder all exist. -->

From GroupApi.kt:
```kotlin
suspend fun removeMember(groupId: String, userId: String): Either<AppError, Unit>
```

From FakeGroupApiBuilder.kt:
```kotlin
fun removeMember(behavior: suspend (String, String) -> Either<AppError, Unit>)
```

From Sdk.kt:
```kotlin
class Sdk(..., groupApi: GroupApi, ...) : ... GroupApi by groupApi ...
// So sdk.removeMember(groupId, userId) is available
```

From MemberResponse (used in AdminPanelModel.members):
```kotlin
data class MemberResponse(
    val userId: String,
    val email: String,
    val name: String,
    val role: GroupRole,
    val joinedAt: String,
)
```

From GroupRole:
```kotlin
sealed class GroupRole {
    data object Owner : GroupRole()   // level=2 — cannot be removed
    data object Admin : GroupRole()   // level=1
    data object Member : GroupRole()  // level=0
}
```

Server enforces: Cannot remove OWNER (returns AppError.Group.Forbidden).
Client should ALSO hide the Remove button for Owner role as a UX guard.
</interfaces>
</context>

<tasks>

<task type="auto" tdd="true">
  <name>Task 1: Add MVI types + ViewModel handler + tests for remove member</name>
  <files>
    app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelIntent.kt
    app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelModel.kt
    app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelMutation.kt
    app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelViewModel.kt
    app/admin/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt
  </files>
  <behavior>
    - Test 1: `ExecuteRemoveMember success removes member from list` — Load admin panel, then dispatch ConfirmRemoveMember(member) → ExecuteRemoveMember. Assert final model has showRemoveMemberDialog=false, removeMemberTarget=null, and the member is removed from the members list, memberCount decremented.
    - Test 2: `ExecuteRemoveMember failure closes dialog` — Configure sdk.removeMember to return Left(AppError.Group.Forbidden). Assert final model has showRemoveMemberDialog=false, removeMemberTarget=null, error is set.
    - Test 3: `CancelRemoveMember hides dialog` — Dispatch ConfirmRemoveMember then CancelRemoveMember. Assert dialog hidden, target null.
  </behavior>
  <action>
    **1. AdminPanelIntent.kt** — Add three new intents following the revoke-invitation pattern:
    ```kotlin
    // Remove member intents
    data class ConfirmRemoveMember(val member: MemberResponse) : AdminPanelIntent
    data object CancelRemoveMember : AdminPanelIntent
    data object ExecuteRemoveMember : AdminPanelIntent
    ```

    **2. AdminPanelModel.kt** — Add 3 new fields at the bottom (before the closing paren):
    ```kotlin
    // Remove member dialog state
    val showRemoveMemberDialog: Boolean = false,
    val removeMemberTarget: MemberResponse? = null,
    val isRemovingMember: Boolean = false,
    ```

    **3. AdminPanelMutation.kt** — Add 4 new mutations:
    ```kotlin
    // Remove member mutations
    data class ShowRemoveMemberDialog(val member: MemberResponse) : AdminPanelMutation
    data object HideRemoveMemberDialog : AdminPanelMutation
    data class SetRemovingMember(val removing: Boolean) : AdminPanelMutation
    data class RemoveMemberFromList(val userId: String) : AdminPanelMutation
    ```

    **4. AdminPanelViewModel.kt** — Add intent handling in the `when` block:
    ```kotlin
    is AdminPanelIntent.ConfirmRemoveMember -> sendMutation(AdminPanelMutation.ShowRemoveMemberDialog(intent.member))
    is AdminPanelIntent.CancelRemoveMember -> sendMutation(AdminPanelMutation.HideRemoveMemberDialog)
    is AdminPanelIntent.ExecuteRemoveMember -> handleRemoveMember()
    ```

    Add `handleRemoveMember()` private method following the exact revoke-invitation pattern:
    ```kotlin
    private suspend fun handleRemoveMember() {
        val target = model.value.removeMemberTarget ?: return
        sendMutation(AdminPanelMutation.SetRemovingMember(true))
        sdk.removeMember(model.value.groupId, target.userId).fold(
            ifLeft = { error ->
                sendMutation(AdminPanelMutation.SetRemovingMember(false))
                sendMutation(AdminPanelMutation.HideRemoveMemberDialog)
                sendMutation(AdminPanelMutation.SetError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
            },
            ifRight = {
                sendMutation(AdminPanelMutation.SetRemovingMember(false))
                sendMutation(AdminPanelMutation.HideRemoveMemberDialog)
                sendMutation(AdminPanelMutation.RemoveMemberFromList(target.userId))
            },
        )
    }
    ```

    Add `reduce` cases:
    ```kotlin
    is AdminPanelMutation.ShowRemoveMemberDialog -> model.copy(showRemoveMemberDialog = true, removeMemberTarget = mutation.member)
    is AdminPanelMutation.HideRemoveMemberDialog -> model.copy(showRemoveMemberDialog = false, removeMemberTarget = null, isRemovingMember = false)
    is AdminPanelMutation.SetRemovingMember -> model.copy(isRemovingMember = mutation.removing)
    is AdminPanelMutation.RemoveMemberFromList -> model.copy(
        members = model.members.filter { it.userId != mutation.userId },
        memberCount = model.memberCount - 1,
    )
    ```

    **5. Tests** — Write tests FIRST per TDD workflow. Use `fakeSdk { group { removeMember { ... } } }` to configure the fake. Follow existing test patterns in AdminPanelViewModelTest (load admin panel first to set groupId, then test remove flow).
  </action>
  <verify>
    <automated>./gradlew :app:admin:allTests</automated>
  </verify>
  <done>
    - Three new intents, three model fields, four mutations, and handleRemoveMember handler exist
    - ViewModel reduce handles all four new mutations
    - All three TDD tests pass (success removes from list, failure shows error, cancel hides dialog)
    - Existing tests still pass
  </done>
</task>

<task type="auto">
  <name>Task 2: Add Remove button to members table + confirmation dialog + wiring</name>
  <files>
    app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
    app/admin/src/commonMain/composeResources/values/strings.xml
    composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
  </files>
  <action>
    **1. strings.xml** — Add new string resources after the existing revoke strings:
    ```xml
    <!-- Remove Member Dialog -->
    <string name="admin_remove_member_title">Remove Member</string>
    <string name="admin_remove_member_confirm">Remove %1$s from this group?</string>
    <string name="admin_remove_member_cancel">Cancel</string>
    <string name="admin_remove_member_submit">Remove</string>
    <string name="admin_remove_button">Remove</string>
    <string name="admin_table_actions">ACTIONS</string>
    ```

    **2. AdminPanelScreen.kt** — Make these changes:

    a) Add new callback parameters to `AdminPanelScreen`:
    ```kotlin
    onConfirmRemoveMember: (MemberResponse) -> Unit,
    onCancelRemoveMember: () -> Unit,
    onExecuteRemoveMember: () -> Unit,
    ```

    b) Add a 5th "ACTIONS" column header to the members table (line ~266, after admin_table_joined):
    ```kotlin
    stringResource(Res.string.admin_table_actions),
    ```

    c) Inside each `TerminalTableRow` for members, after the joinedAt cell (line ~291), add an ACTIONS cell:
    ```kotlin
    Box(modifier = Modifier.weight(1f)) {
        if (member.role != GroupRole.Owner) {
            TerminalButton(
                text = stringResource(Res.string.admin_remove_button),
                onClick = { onConfirmRemoveMember(member) },
                variant = ButtonVariant.Destructive,
            )
        }
    }
    ```
    This hides the Remove button for Owners (server also enforces this, but UX should not show it).

    d) Add `RemoveMemberDialog` overlay at the bottom of the `Box` (after RevokeDialog, before the closing `}`), shown when `state.showRemoveMemberDialog && state.removeMemberTarget != null`. Follow the EXACT same pattern as `RevokeDialog`:
    ```kotlin
    if (state.showRemoveMemberDialog && state.removeMemberTarget != null) {
        RemoveMemberDialog(
            memberName = state.removeMemberTarget.name,
            isRemoving = state.isRemovingMember,
            onRemove = onExecuteRemoveMember,
            onCancel = onCancelRemoveMember,
        )
    }
    ```

    e) Create private `RemoveMemberDialog` composable — copy from `RevokeDialog` and adapt:
    - Title: `admin_remove_member_title`
    - Confirm text: `admin_remove_member_confirm` with memberName arg
    - Cancel: `admin_remove_member_cancel`
    - Submit: `admin_remove_member_submit` (with "..." while removing)
    - Submit button variant: `ButtonVariant.Destructive`

    f) Add resource imports for the new string keys.

    **3. AppNavHost.kt** — Add three new callback wiring lines in the `AdminPanelScreen(...)` call (around line 307-310):
    ```kotlin
    onConfirmRemoveMember = { viewModel.take(AdminPanelIntent.ConfirmRemoveMember(it)) },
    onCancelRemoveMember = { viewModel.take(AdminPanelIntent.CancelRemoveMember) },
    onExecuteRemoveMember = { viewModel.take(AdminPanelIntent.ExecuteRemoveMember) },
    ```

    Also add the necessary import for `MemberResponse` if not already present — actually the lambda parameter types are inferred from the function signature, so no new imports needed in AppNavHost.
  </action>
  <verify>
    <automated>./gradlew :app:admin:compileCommonMainKotlinMetadata :composeApp:compileCommonMainKotlinMetadata</automated>
  </verify>
  <done>
    - Members table has 5 columns: NAME, EMAIL, ROLE, JOINED, ACTIONS
    - Remove button appears for Admin and Member roles, hidden for Owner
    - Confirmation dialog shows member name and has Cancel/Remove buttons
    - Dialog wired through AppNavHost to AdminPanelViewModel intents
    - All compilation succeeds
  </done>
</task>

</tasks>

<verification>
1. Run full admin module tests: `./gradlew :app:admin:allTests`
2. Compile both admin and composeApp modules: `./gradlew :app:admin:compileCommonMainKotlinMetadata :composeApp:compileCommonMainKotlinMetadata`
3. Verify no regressions in existing admin panel tests (LoadAdminPanel, LoadMoreMembers, RegisterMemberClicked)
</verification>

<success_criteria>
- Admin can see Remove button next to each non-owner member in the members table
- Clicking Remove opens a confirmation dialog with the member's name
- Confirming removal calls sdk.removeMember and removes the member from the displayed list
- Owner role members have no Remove button (UX guard matching server-side enforcement)
- Cancel closes the dialog without side effects
- Failed removal shows error state and closes dialog
- All existing admin panel tests pass alongside 3 new remove-member tests
</success_criteria>

<output>
After completion, create `.planning/quick/3-as-an-admin-i-would-like-to-remove-users/3-SUMMARY.md`
</output>
