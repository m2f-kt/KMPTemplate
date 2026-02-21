---
phase: quick-1
plan: 1
type: execute
wave: 1
depends_on: []
files_modified:
  - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardModel.kt
  - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardMutation.kt
  - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardViewModel.kt
  - app/dashboard/src/commonTest/kotlin/com/m2f/template/app/dashboard/DashboardViewModelTest.kt
autonomous: true
must_haves:
  truths:
    - "Dashboard shows user's name (from profile) instead of hardcoded email"
    - "Dashboard falls back to user's email if name is blank"
    - "Dashboard shows empty string as default before profile loads"
  artifacts:
    - path: "app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardMutation.kt"
      provides: "SetUserName mutation"
      contains: "SetUserName"
    - path: "app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardViewModel.kt"
      provides: "Profile name extraction in LoadDashboard"
      contains: "SetUserName"
    - path: "app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardModel.kt"
      provides: "userName defaults to empty string"
    - path: "app/dashboard/src/commonTest/kotlin/com/m2f/template/app/dashboard/DashboardViewModelTest.kt"
      provides: "Tests verifying userName is set from profile"
  key_links:
    - from: "DashboardViewModel.kt"
      to: "sdk.getProfile()"
      via: "user.name extraction in onRight block"
      pattern: "SetUserName.*name.*ifBlank.*email"
---

<objective>
Fix the dashboard display name to show the user's actual name from their profile instead of the hardcoded `"user@terminal.dev"` placeholder. Falls back to email when name is blank, matching the profile screen's behavior.

Purpose: Dashboard currently shows a hardcoded email for all users. The profile data is already fetched during LoadDashboard but name is not extracted.
Output: Dashboard shows user's real name with email fallback.
</objective>

<execution_context>
@/Users/marc/.config/opencode/get-shit-done/workflows/execute-plan.md
@/Users/marc/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardModel.kt
@app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardMutation.kt
@app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardViewModel.kt
@app/dashboard/src/commonTest/kotlin/com/m2f/template/app/dashboard/DashboardViewModelTest.kt
@app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileScreen.kt (line 165 — reference for name fallback pattern)
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add SetUserName mutation and update ViewModel to extract name from profile</name>
  <files>
    app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardModel.kt
    app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardMutation.kt
    app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardViewModel.kt
  </files>
  <action>
1. **DashboardModel.kt** — Change `userName` default from `"user@terminal.dev"` to `""` (empty string). The real value will come from the profile API.

2. **DashboardMutation.kt** — Add a new mutation:
   ```kotlin
   data class SetUserName(val userName: String) : DashboardMutation
   ```

3. **DashboardViewModel.kt** — In the `LoadDashboard` handler's existing `sdk.getProfile().onRight { user -> ... }` block, add a `SetUserName` mutation AFTER the `SetSystemAdmin` mutation. Use the same fallback pattern as ProfileScreen:
   ```kotlin
   sendMutation(DashboardMutation.SetUserName(user.name.ifBlank { user.email }))
   ```
   This extracts the user's name from the profile response, falling back to email if name is blank.

4. **DashboardViewModel.kt** — In the `reduce` function, add the new case:
   ```kotlin
   is DashboardMutation.SetUserName -> model.copy(userName = mutation.userName)
   ```
  </action>
  <verify>Build compiles: `./gradlew :app:dashboard:compileCommonMainKotlinMetadata`</verify>
  <done>DashboardModel.userName defaults to "", SetUserName mutation exists, ViewModel extracts name from profile with email fallback, reduce handles SetUserName.</done>
</task>

<task type="auto">
  <name>Task 2: Update DashboardViewModel tests to verify userName from profile</name>
  <files>
    app/dashboard/src/commonTest/kotlin/com/m2f/template/app/dashboard/DashboardViewModelTest.kt
  </files>
  <action>
Update existing tests where `getProfile` is configured with a `UserResponse` to include the expected `userName` in model assertions. Key changes:

1. **`LoadDashboard toggles loading state`** and **`NavItemSelected updates selectedNavItem`** — These use default `fakeSdk()` where `getProfile` returns `Left(Unknown)`. The `onRight` block won't execute, so `userName` stays at default `""`. Update all `DashboardModel()` assertions in these tests to NOT specify userName (it defaults to `""`), which means no change needed since the default changed from `"user@terminal.dev"` to `""`.

2. **`LoadDashboard with system admin role sets isAdmin true`** — The fake configures `getProfile` returning `UserResponse(id="1", email="admin@test.com", name="Admin", role=UserRole.Admin)`. The final model assertion must include `userName = "Admin"`.

3. **`AdminPanelClicked emits NavigateToAdmin with null groupId for system admin`** — Same `getProfile` config. The model after LoadDashboard must include `userName = "Admin"`.

4. **`LoadDashboard with system admin AND group admin membership gets groupId`** — Same `getProfile` config. The final model must include `userName = "Admin"`.

5. **`LoadDashboard with admin membership sets isAdmin true`** — This test does NOT configure `getProfile` (default returns error), so `userName` stays `""`. No change needed.

6. **`LoadDashboard with member-only membership does not set isAdmin`** — Same as above, no `getProfile` configured. No change needed.

NOTE on StateFlow conflation: `SetSystemAdmin` and `SetUserName` fire in quick succession in the same `onRight` block (sync fake), so they'll be conflated together in the same observed model state. The existing model assertions already capture the final state — just add `userName` to those assertions.
  </action>
  <verify>Tests pass: `./gradlew :app:dashboard:cleanAllTests :app:dashboard:allTests`</verify>
  <done>All existing DashboardViewModel tests pass. Tests that configure getProfile assert userName matches the profile name. Tests with default fakeSdk (getProfile returns error) correctly expect userName = "" (default).</done>
</task>

</tasks>

<verification>
1. `./gradlew :app:dashboard:allTests` — all tests pass
2. Grep for hardcoded "user@terminal.dev" in dashboard module — should return 0 matches
3. DashboardModel.userName default is "" not the old hardcoded email
</verification>

<success_criteria>
- Dashboard display name shows user's real name from profile API
- Falls back to email when name is blank (handled in ViewModel via `ifBlank`)
- No hardcoded "user@terminal.dev" remains in dashboard module
- All existing tests pass with updated assertions
</success_criteria>

<output>
After completion, create `.planning/quick/1-fix-dashboard-display-name-to-show-user-/1-SUMMARY.md`
</output>
