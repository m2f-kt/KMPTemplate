# Phase 14: Group Admin UI — Research

**Phase Goal:** Deliver admin panel screens for group management: view group info + member list, register new members into a group, role-differentiated dashboard content, and role-gated navigation items.

**Depends on:** Phase 12 (ViewModel MVI migration), Phase 13 (Group server + SDK)

**Requirements:** GRP-03, GRP-04, GRP-05, GRP-06

---

## 1. Existing Patterns

### 1.1 MVI ViewModel Pattern (4 files + ViewModel)

Every feature follows the same structure. The planner MUST create these files for each ViewModel:

| File | Purpose | Example |
|------|---------|---------|
| `*Intent.kt` | Sealed interface of user actions | `DashboardIntent.kt` — `LoadDashboard`, `NavItemSelected`, `LogoutClicked` |
| `*Model.kt` | Data class representing UI state | `DashboardModel.kt` — `isLoading`, `selectedNavItem`, metric lists |
| `*Mutation.kt` | Sealed interface of state transitions | `DashboardMutation.kt` — `SetLoading`, `SetNavItem` |
| `*Event.kt` | Sealed interface of one-shot side effects | `DashboardEvent.kt` — `NavigateToLogin` |
| `*ViewModel.kt` | Extends `MviViewModel<I,M,Mu,E>`, takes `Sdk` | `DashboardViewModel.kt` — launches coroutines in `take()` |

**Key file:** `core/mvi/src/commonMain/kotlin/com/m2f/template/core/mvi/MviViewModel.kt`

```kotlin
abstract class MviViewModel<Intent, Model, Mutation, Event>(
    initialState: Model,
    modelSharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5_000),
) : ViewModel()
```

**Constructor dependency:** Every ViewModel takes `private val sdk: Sdk` as its sole constructor parameter. Koin resolves it automatically via `viewModelOf(::MyViewModel)`.

**Confidence:** HIGH — 5 existing ViewModels all follow this exact pattern.

### 1.2 Init-Dispatching ViewModels

ViewModels that load data in `init {}` (like ProfileViewModel) MUST use `SharingStarted.Eagerly` and force-initialize `model` and `event` before calling `take()`:

```kotlin
class ProfileViewModel(private val sdk: Sdk) : MviViewModel<...>(
    initialState = ProfileModel(),
    modelSharingStarted = SharingStarted.Eagerly,  // <-- REQUIRED
) {
    init {
        model   // Force lazy initialization
        event   // Force lazy initialization
        take(ProfileIntent.LoadProfile)
    }
}
```

**Why:** The `model` and `event` are `by lazy`. If `take()` fires before they're initialized, mutations emitted during init are lost because no subscriber exists yet.

**Applies to Phase 14:** The admin panel ViewModel will load group info + member list on init, so it MUST use this pattern.

**Key file:** `app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileViewModel.kt:11-24`

**Confidence:** HIGH — ProfileViewModel demonstrates this exact issue and fix.

### 1.3 Screen Composable Pattern

Screens are stateless `@Composable` functions that receive:
- `state: Model` — the current state from `viewModel.model.collectAsStateWithLifecycle()`
- Callback lambdas for each user action (mapped to `viewModel.take(Intent)` at the call site)

The ViewModel is NOT injected into the Screen composable. It stays in `AppNavHost.kt`:

```kotlin
composable<ProfileRoute> {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.model.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onStartEditing = { viewModel.take(ProfileIntent.StartEditing) },
        // ...
    )
    LaunchedEffect(Unit) {
        viewModel.event.collect { event -> /* navigation side effects */ }
    }
}
```

**Key file:** `composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt:170-217`

**Confidence:** HIGH — all 5 existing screens follow this pattern.

### 1.4 Navigation Pattern

Routes are flat `@Serializable data object` or `data class` definitions:

```kotlin
// Routes.kt
@Serializable data object DashboardRoute
@Serializable data object ProfileRoute
@Serializable data class OAuthCallbackRoute(val accessToken: String, val refreshToken: String)
```

Navigation is wired in `AppNavHost.kt` using `NavHost` + `composable<Route>`. Events trigger `navController.navigate()`.

**Key files:**
- `composeApp/src/commonMain/kotlin/com/m2f/template/navigation/Routes.kt`
- `composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt`

**Confidence:** HIGH

### 1.5 Dashboard Content Switching (State-Based)

The Dashboard uses `selectedNavItem: String` in the model to switch content inline — NOT separate routes:

```kotlin
// DashboardScreen.kt — Desktop layout
when (state.selectedNavItem) {
    "dashboard" -> { /* main dashboard content */ }
    "processes" -> { PlaceholderContent(title = "> processes", ...) }
    "logs" -> { PlaceholderContent(title = "> logs", ...) }
    // etc.
}
```

The sidebar nav items are a hardcoded list in `DashboardSidebar.kt:100-106`:
```kotlin
val navItems = listOf(
    "dashboard" to "dashboard",
    "processes" to "processes",
    "logs" to "logs",
    "deployments" to "deployments",
    "settings" to "settings",
)
```

**Implication for Phase 14:** Adding an "admin" or "groups" nav item means modifying this list. Role-gated nav items means the list must become dynamic based on the user's role.

**Key files:**
- `app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardScreen.kt:108-174`
- `app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardSidebar.kt:100-106`

**Confidence:** HIGH

### 1.6 Form Validation Pattern (Arrow zipOrAccumulate)

The RegisterViewModel demonstrates the accumulated validation pattern:

```kotlin
val validationResult = either {
    zipOrAccumulate(
        { validateEmail(current.email) },
        { validatePassword(current.password) },
        { validateName(current.firstName) },
    ) { email, password, name -> RegisterRequest(email, password, name) }
}
validationResult.fold(
    ifLeft = { errors -> sendMutation(SetFieldErrors(errors.associate { it.field to it.message })) },
    ifRight = { request -> /* call SDK */ },
)
```

**Existing validators available for reuse:** `validateEmail`, `validatePassword`, `validateName`, `validateRequired` — all in `core/models/src/commonMain/kotlin/com/m2f/template/models/validation/ValidationSupport.kt`.

**Key file:** `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterViewModel.kt:37-91`

**Confidence:** HIGH

### 1.7 Module Structure

Each feature is a Gradle module under `app/`:

```
app/dashboard/
  build.gradle.kts          # kmp-library-convention + compose plugins
  src/commonMain/kotlin/...  # ViewModel, Screen, Intent, Model, Mutation, Event
  src/commonTest/kotlin/...  # Tests (depends on core:testing)
```

`build.gradle.kts` template (from `app/dashboard/build.gradle.kts`):
- Plugin: `kmp-library-convention`, `com.android.library`, compose plugins
- commonMain deps: `projects.core.models`, `projects.core.sdk`, `projects.core.mvi`, `projects.app.designsystem`, compose, arrow, coroutines, lifecycle, koin, navigation
- commonTest deps: `projects.core.testing`

Registration: Module must be added to `settings.gradle.kts` as `include("app:admin")`.

**Key file:** `app/dashboard/build.gradle.kts`

**Confidence:** HIGH

### 1.8 DI Wiring

All ViewModels are registered in `composeApp/src/commonMain/kotlin/com/m2f/template/di/AppModule.kt`:

```kotlin
val appModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::ProfileViewModel)
}
```

**Confidence:** HIGH

---

## 2. Available Infrastructure from Phase 13

### 2.1 GroupApi SDK Interface

All needed SDK methods already exist in `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/GroupApi.kt`:

| Method | Returns | Use in Phase 14 |
|--------|---------|-----------------|
| `getGroup(groupId)` | `Either<AppError, GroupResponse>` | Admin views group info |
| `getMembers(groupId, cursor?, limit)` | `Either<AppError, PaginatedMemberResponse>` | Admin views member list |
| `registerMember(groupId, request)` | `Either<AppError, MemberResponse>` | Admin registers new user into group |
| `addMember(groupId, request)` | `Either<AppError, MemberResponse>` | Admin adds existing user to group |
| `removeMember(groupId, userId)` | `Either<AppError, Unit>` | Admin removes member |
| `updateGroup(groupId, request)` | `Either<AppError, GroupResponse>` | Admin edits group details |

The `Sdk` facade (`core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt`) delegates to `GroupApi` via Kotlin delegation, so ViewModels can call `sdk.getGroup()`, `sdk.getMembers()`, etc. directly.

**Confidence:** HIGH — these are all implemented and wired.

### 2.2 DTOs

All DTOs are in `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/GroupDtos.kt`:

- `GroupResponse(id, name, slug, description, createdBy, memberCount, createdAt, updatedAt)`
- `MemberResponse(userId, email, name, role: GroupRole, joinedAt)`
- `PaginatedMemberResponse(items: List<MemberResponse>, cursor: String?, hasMore: Boolean)`
- `RegisterMemberRequest(email, password, firstName, lastName, role: GroupRole = Member)`

**Confidence:** HIGH

### 2.3 GroupRole

`core/models/src/commonMain/kotlin/com/m2f/template/models/GroupRole.kt`:

```kotlin
sealed class GroupRole {
    data object Owner : GroupRole()   // level = 2
    data object Admin : GroupRole()   // level = 1
    data object Member : GroupRole()  // level = 0
}
```

Serializes to flat string (`"OWNER"`, `"ADMIN"`, `"MEMBER"`).

**Confidence:** HIGH

### 2.4 AppError.Group

Error types for UI error handling (`core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt:137-161`):

- `AppError.Group.NotFound` — group not found
- `AppError.Group.Forbidden` — no permission to access group
- `AppError.Group.AlreadyExists` — slug collision
- `AppError.Group.MemberAlreadyExists` — user already in group

**Confidence:** HIGH

### 2.5 Server RBAC

Group-level authorization is enforced in `GroupService`:
- `getGroup` — members can view their own group; PowerAdmin can view any group
- `getMembers` — ADMIN or OWNER required (regular members cannot list members)
- `registerMember`, `addMember`, `removeMember` — ADMIN or OWNER required
- `deleteGroup` — OWNER only

System-level authorization is enforced at the route level:
- `createGroup` — requires system `UserRole.Admin` or `UserRole.PowerAdmin`
- `listAllGroups` — requires system `UserRole.PowerAdmin`

**Key file:** `server/groups/src/main/kotlin/com/m2f/server/groups/service/GroupService.kt`

**Confidence:** HIGH

### 2.6 FakeGroupApiBuilder (Testing)

`core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeGroupApiBuilder.kt`:

All 9 GroupApi methods are configurable. Default behavior returns `Either.Left(AppError.Client.Unknown())` so unconfigured paths fail fast.

```kotlin
val sdk = fakeSdk {
    group {
        getGroup { Either.Right(GroupResponse(...)) }
        getMembers { _, _, _ -> Either.Right(PaginatedMemberResponse(...)) }
        registerMember { _, _ -> Either.Right(MemberResponse(...)) }
    }
}
```

**Confidence:** HIGH

---

## 3. Architecture Recommendations

### 3.1 Critical Gap: No "Get My Group" Endpoint

**Problem:** There is NO SDK method or server route to fetch the current user's group memberships. The `UserResponse` contains only system-level `UserRole` (User/Admin/PowerAdmin), NOT group-level `GroupRole`.

The client has no way to know:
- Which group(s) the user belongs to
- What their GroupRole is in those groups

**Recommended solutions (choose one):**

**Option A — Hardcode groupId for now (simplest, recommended for template)**
Store the admin's groupId as a known value (e.g., from the seed migration's default group). The admin panel takes `groupId` as a known parameter. This avoids new server work.

**Option B — Add a "my memberships" endpoint (proper, more work)**
Add `GET /api/users/me/memberships` returning `List<MembershipResponse>` with `groupId`, `groupName`, and `groupRole`. This requires:
1. New route in `ApiRoutes.kt`
2. New server route in `UserRoutes.kt` or `GroupRoutes.kt`
3. New `MembershipRepository.findByUserId()` call (already exists on server side)
4. New SDK method in `UserApi` or `GroupApi`
5. New fake builder method

**Option C — Extend UserResponse to include group info**
Add `groupMemberships: List<MembershipSummary>` to `UserResponse`. Simplest for the client but couples user and group concerns.

**Recommendation:** Option A for the template use case. The admin is the group creator — they know their groupId. A future phase can add the "my memberships" endpoint if needed.

**Confidence:** MEDIUM — Option A is pragmatic but assumes single-group usage. The planner should decide.

### 3.2 Module Structure

Create `app/admin/` as a new feature module:

```
app/admin/
  build.gradle.kts
  src/commonMain/kotlin/com/m2f/template/app/admin/
    AdminPanelIntent.kt
    AdminPanelModel.kt
    AdminPanelMutation.kt
    AdminPanelEvent.kt
    AdminPanelViewModel.kt
    AdminPanelScreen.kt
    RegisterMemberIntent.kt     (if separate ViewModel)
    RegisterMemberModel.kt
    RegisterMemberMutation.kt
    RegisterMemberEvent.kt
    RegisterMemberViewModel.kt
    RegisterMemberScreen.kt
  src/commonTest/kotlin/com/m2f/template/app/admin/
    AdminPanelViewModelTest.kt
    RegisterMemberViewModelTest.kt
```

**ViewModel split decision:** The admin panel (view group + members) and register-member form have different concerns. Two ViewModels is cleaner than one bloated ViewModel. The RegisterMember flow has its own form state, validation, loading, and success/error handling.

**Confidence:** HIGH — follows existing module patterns exactly.

### 3.3 Navigation Approach

**Two options for admin screens:**

**Option A — Separate top-level route (like ProfileRoute)**
```kotlin
@Serializable data object AdminPanelRoute
```
Navigated to from Dashboard sidebar. Admin panel is its own screen with back navigation.

**Option B — Inline content in Dashboard (like existing nav items)**
Add `"admin"` to the Dashboard sidebar nav items. When selected, render admin content inline within the Dashboard layout (same as "processes", "logs" placeholders).

**Recommendation:** Option B for the admin panel overview (group info + member list) — it fits naturally as a Dashboard tab. Option A for the register-member form — it's a distinct flow with its own back navigation.

**Confidence:** MEDIUM — the planner should decide based on UX preference.

### 3.4 Role-Gated Navigation

The Dashboard sidebar currently has a hardcoded nav item list. To gate items by role:

1. **DashboardModel** gains a `userRole: GroupRole?` field (loaded from the membership data or passed as a nav argument)
2. **DashboardSidebar** receives the role and conditionally includes the "admin" item
3. **DashboardViewModel** loads the user's role on init (or receives it as a constructor parameter)

The simplest approach: DashboardViewModel calls `sdk.getGroup(groupId)` on init. If the call succeeds, the user is a member. Then call `sdk.getMembers(groupId)` — if that succeeds, the user is ADMIN+ (because the server enforces ADMIN-only access to getMembers). This avoids needing a dedicated "get my role" endpoint.

**Alternative:** Pass GroupRole from the admin panel back to the dashboard via a shared state holder or navigation argument.

**Confidence:** MEDIUM — role detection without a dedicated endpoint requires inference.

### 3.5 Role-Differentiated Dashboard Content (GRP-05)

The requirement states the admin panel should show "different dashboard content" based on role. This means:

- **Admin/Owner:** See group management (member list, stats, register-member action)
- **Member:** See a simpler view (e.g., group name, their own membership info)
- **No group:** See the default dashboard content (existing behavior)

This is naturally handled by the Dashboard's `when (selectedNavItem)` pattern — the "admin" tab content can internally branch on the user's role.

**Confidence:** HIGH

---

## 4. Standard Stack

| Concern | Solution | Reference |
|---------|----------|-----------|
| ViewModel | `MviViewModel<I,M,Mu,E>` from `core:mvi` | `MviViewModel.kt` |
| State management | `model: StateFlow<Model>` collected via `collectAsStateWithLifecycle()` | All existing screens |
| One-shot events | `event: SharedFlow<Event>` collected in `LaunchedEffect` | `AppNavHost.kt` |
| DI | `viewModelOf(::MyViewModel)` in `AppModule.kt`, `koinViewModel<T>()` in composables | `AppModule.kt` |
| Network calls | `sdk.getGroup()`, `sdk.getMembers()`, etc. returning `Either<AppError, T>` | `GroupApi.kt`, `Sdk.kt` |
| Error handling | `Either.fold(ifLeft = { sendMutation(SetError(it.message)) }, ifRight = { ... })` | `ProfileViewModel.kt:42-50` |
| Validation | Arrow `zipOrAccumulate` with `FieldError` | `RegisterViewModel.kt:37-72` |
| Testing | `ViewModelTest` base class + `viewModel.test { intent(); model(); event() }` DSL | `MviViewModelTestDsl.kt` |
| Test fakes | `fakeSdk { group { getMembers { ... } } }` | `FakeSdkBuilder.kt` |
| UI components | `TerminalCard`, `TerminalTable`, `TerminalList`, `TerminalInput`, `TerminalButton`, etc. | `app/designsystem/` |
| Navigation | `@Serializable` routes + `composable<Route>` + `koinViewModel` | `Routes.kt`, `AppNavHost.kt` |

---

## 5. Common Pitfalls

### 5.1 StateFlow Conflation (CRITICAL)

`StateFlow` conflates — if two mutations are emitted in rapid succession with the same value, the second is dropped. This matters for:

- **Loading states:** `SetLoading(true)` → `SetError(...)` → `SetLoading(false)` — if `SetLoading(true)` and `SetLoading(false)` happen before the collector processes them, the intermediate state is lost. Use distinct model states (e.g., different `isLoading` values) to avoid conflation.
- **Error messages:** If the same error occurs twice, the second emission is conflated. Consider adding a unique identifier or timestamp to error states.

**Mitigation:** The existing codebase handles this by having mutations that always produce distinct model values (e.g., loading state changes alongside other field changes). Follow the same pattern.

**Confidence:** HIGH — this is a known StateFlow behavior.

### 5.2 Init-Dispatching Must Use Eagerly

If the admin panel ViewModel loads data in `init {}`, it MUST:
1. Pass `modelSharingStarted = SharingStarted.Eagerly` to the superclass
2. Force-initialize `model` and `event` before calling `take()`
3. Tests must account for the initial emission

See Section 1.2 for the full pattern.

**Confidence:** HIGH — ProfileViewModel documents this exact issue.

### 5.3 Navigation Argument Passing

If the admin panel needs a `groupId`, it can be:
- Hardcoded (for template purposes)
- Passed as a route argument: `@Serializable data class AdminPanelRoute(val groupId: String)`
- Stored in a shared preference / SDK state

Avoid storing groupId in a ViewModel singleton — it won't survive process death on Android.

**Confidence:** HIGH

### 5.4 Cursor-Based Pagination in UI

`PaginatedMemberResponse` uses cursor-based pagination. The UI must:
1. Store the current `cursor` in the model
2. Track `hasMore` to show/hide "load more"
3. Append new items to existing list (not replace)
4. Handle loading state for pagination separately from initial load

Pattern:
```kotlin
data class AdminPanelModel(
    val members: List<MemberResponse> = emptyList(),
    val membersCursor: String? = null,
    val hasMoreMembers: Boolean = false,
    val isLoadingMore: Boolean = false,
)
```

**Confidence:** HIGH — standard pagination pattern.

### 5.5 Server RBAC Error Mapping

When a non-admin user calls `sdk.getMembers()`, the server returns 403 which maps to `AppError.Group.Forbidden`. The UI should handle this gracefully — not crash, but show an appropriate message or hide the admin features.

**Confidence:** HIGH

### 5.6 Test DSL Ordering

The `viewModel.test {}` DSL executes statements sequentially. Each `model(...)` assertion calls `awaitItem()` on the Turbine, consuming exactly one emission. If you miss an intermediate state (e.g., `isLoading = true` before `isLoading = false`), the test will hang or fail.

Always assert ALL intermediate model states in order:
```kotlin
viewModel.test {
    intent(AdminPanelIntent.LoadGroup)
    model(AdminPanelModel(isLoading = true))                    // first emission
    model(AdminPanelModel(isLoading = false, group = expected)) // second emission
}
```

**Confidence:** HIGH — existing tests demonstrate this pattern.

---

## 6. Don't Hand-Roll

### Reuse These Existing Utilities

| What | Where | Don't reinvent |
|------|-------|----------------|
| `MviViewModel` base class | `core:mvi` | Do not create a custom ViewModel base |
| `ViewModelTest` base class | `core:testing` | Do not set up Dispatchers.Main manually |
| `viewModel.test {}` DSL | `core:testing` | Do not use raw Turbine or manual StateFlow collection |
| `fakeSdk {}` builder | `core:testing` | Do not create ad-hoc mock implementations |
| `FakeGroupApiBuilder` | `core:testing` | All 9 GroupApi methods are already stubbed |
| `validateEmail`, `validateName`, `validatePassword`, `validateRequired` | `core/models/validation/` | Do not write custom email/name/password regex |
| `TerminalTable`, `TerminalList`, `TerminalCard`, `TerminalInput`, `TerminalButton`, `TerminalBadge`, `TerminalAlert` | `app:designsystem` | Do not create custom styled components |
| Arrow `zipOrAccumulate` | Already in `libs.arrow.core` dep | Do not write manual field-by-field validation |
| `AppError.Group.*` error types | `core:models` | Do not create new error types for group operations |
| `GroupRole` sealed class | `core:models` | Do not use strings for role comparison |
| `kmp-library-convention` plugin | `buildSrc` | Do not manually configure KMP targets |

### Patterns to Follow Exactly

| Pattern | Reference Implementation | Notes |
|---------|-------------------------|-------|
| ViewModel takes `Sdk` as sole dependency | `DashboardViewModel(private val sdk: Sdk)` | Never inject individual API interfaces |
| Screen is stateless composable | `DashboardScreen(state, onX, onY, ...)` | Never inject ViewModel into Screen |
| Event collection in LaunchedEffect | `AppNavHost.kt:180-190` | Always `LaunchedEffect(Unit) { viewModel.event.collect { ... } }` |
| Error handling via fold | `ProfileViewModel.kt:42-50` | Always `sdk.method().fold(ifLeft = ..., ifRight = ...)` |
| Registration in settings.gradle.kts | `include("app:dashboard")` pattern | Add `include("app:admin")` |
| DI registration in AppModule | `viewModelOf(::DashboardViewModel)` | Add `viewModelOf(::AdminPanelViewModel)` |

---

## 7. Known Gaps from Phase 13

From `13-VERIFICATION.md`, these Phase 13 gaps are relevant to Phase 14:

1. **`registerMember` endpoint has no integration test** — the server code works, but there's no test proving it. Phase 14 UI tests will be the first real exercise of this endpoint through the SDK.

2. **`joinedAt` field is empty string in addMember/registerMember responses** — `GroupService.kt:246,326` returns `joinedAt = ""` instead of the actual DB timestamp. The UI should handle empty/missing `joinedAt` gracefully.

3. **Server RBAC test gaps** — missing tests for "member cannot add members" and "admin can manage but not delete group". The RBAC logic is implemented correctly in code, but edge cases are undertested.

**Confidence:** HIGH — directly from verification report.

---

## 8. Open Questions for Planner

1. **GroupId source:** How does the admin know their groupId? Hardcode from seed migration? Pass as nav argument? Add a "my memberships" endpoint? (See Section 3.1)

2. **Admin panel as Dashboard tab vs. separate route?** Inline content (Option B in Section 3.3) is simpler but couples admin UI to Dashboard module. Separate route (Option A) is cleaner but adds navigation complexity.

3. **One ViewModel or two?** Admin panel overview + register-member form. Recommendation: two ViewModels for separation of concerns, but one large ViewModel is also viable.

4. **Role detection mechanism:** Without a "get my role" endpoint, how does the client determine the user's GroupRole for navigation gating? (See Section 3.4)

---

_Researched: 2026-02-19_
_Researcher: Claude (gsd-phase-researcher)_
