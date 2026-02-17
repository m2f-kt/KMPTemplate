# Pitfalls Research

**Domain:** Adding MVI ViewModel, Groups/Admin, Testing Infrastructure, and Localization to existing KMP Full-Stack Template
**Researched:** 2026-02-17
**Confidence:** MEDIUM-HIGH (verified against codebase, official docs, community reports, GitHub issues)

## Critical Pitfalls

### Pitfall 1: MVI State Mutation Ordering -- StateFlow.update Is Not Atomic Across Multiple Fields

**What goes wrong:**
The existing ViewModels (LoginViewModel, ProfileViewModel, DashboardViewModel) already use `MutableStateFlow` with `_state.update { it.copy(...) }`. When migrating to a formal MVI pattern with an `Intent -> Reducer -> State` pipeline, developers assume that sequential `update` calls within a single coroutine produce atomic state transitions. They do not. If two intents arrive in rapid succession (e.g., user types fast), the second `update` call can observe a stale `it` from before the first `update` completed its `copy()`. This causes field values to regress -- the classic "typed character disappears" bug.

This is already latent in the current codebase. Look at `LoginViewModel.login()`: it reads `_state.value` into `current`, validates, then later calls `_state.update { it.copy(isLoading = true) }`. If `onEmailChange` fires between the read and the update, the email change is lost.

**Why it happens:**
`StateFlow.update` uses CAS (compare-and-set) internally, which retries on conflict. But the retry re-invokes the lambda with the new state, discarding the closure's captured `current`. Developers who read `_state.value` into a local variable before calling `update` bypass the CAS protection entirely.

**How to avoid:**
- Never read `_state.value` into a local variable and then call `_state.update` later. Always read state inside the `update` lambda.
- Implement a single `reduce` function that processes all intents through one code path: `_state.update { currentState -> reduce(currentState, intent) }`. This guarantees the reducer always sees the latest state.
- Use a `Channel<Intent>` with `UNLIMITED` buffer, consumed by a single `viewModelScope.launch { intents.consumeAsFlow().collect { processIntent(it) } }`. This serializes intent processing.

**Warning signs:**
- `val current = _state.value` followed by `_state.update {}` anywhere in a ViewModel
- Text input values "flickering" or reverting during fast typing
- Race condition test failures that only appear under `UnconfinedTestDispatcher`

**Phase to address:**
MVI ViewModel phase -- must establish the intent channel + reducer pattern before building any new ViewModels. Existing ViewModels (Login, Register, Profile, Dashboard, ForgotPassword) should be migrated to the new pattern.

---

### Pitfall 2: ViewModel viewModelScope Dispatcher Differences Across KMP Targets

**What goes wrong:**
On Android, `viewModelScope` uses `Dispatchers.Main.immediate` with automatic lifecycle cancellation. On non-Android targets (Desktop, WASM, iOS via Compose), `viewModelScope` also uses `Dispatchers.Main` but the "Main" dispatcher behaves differently: on WASM, there is no separate main thread (everything is single-threaded), so `Dispatchers.Main` and `Dispatchers.Default` are identical. On Desktop, the main dispatcher is the AWT/Swing event loop. On iOS via Compose Multiplatform, it maps to the main queue.

The practical consequence: ViewModels that assume `viewModelScope.launch {}` runs on a background thread (a common Android developer mistake) will block the UI thread on all targets. Conversely, ViewModels that assume `Dispatchers.Main.immediate` skips dispatch (an Android optimization) will find that this optimization does not exist on WASM and Desktop.

Additionally, on WASM, the CREATED lifecycle state is skipped entirely -- the app is always attached to the page. This means ViewModels that rely on lifecycle transitions (like pausing updates when the app is backgrounded) cannot work on WASM.

**Why it happens:**
Developers test primarily on Android or JVM Desktop during development, where the dispatcher model is well-understood. WASM's single-threaded model and iOS's main queue are tested late.

**How to avoid:**
- Always use `withContext(Dispatchers.IO)` or `withContext(Dispatchers.Default)` for SDK calls inside ViewModels, never rely on the viewModelScope dispatcher being non-main.
- For the existing SDK pattern (`Either<AppError, T>` from `apiCall`), ensure the Ktor client is configured with its own dispatcher (it already is, via engine config) so ViewModel coroutines just suspend, not block.
- On WASM, accept that there is no background thread. Long-running operations must use chunked/yielding patterns or run on the server.
- Do not use lifecycle-aware collection patterns (`repeatOnLifecycle`) in Compose Multiplatform -- use `collectAsStateWithLifecycle()` from `lifecycle-runtime-compose` which handles this correctly across targets. The existing codebase already does this correctly in `AppNavHost.kt`.

**Warning signs:**
- UI freezes on WASM during API calls that work fine on Android
- `Dispatchers.IO` crashes on WASM (`IO` is not available on all targets -- use `Dispatchers.Default` instead for KMP-safe code)
- Tests pass on JVM but hang on wasmJs

**Phase to address:**
MVI ViewModel phase -- the base ViewModel pattern must account for cross-platform dispatch. Use `Dispatchers.Default` as the KMP-safe background dispatcher, not `Dispatchers.IO`.

---

### Pitfall 3: Group-Scoped Authorization Bypass via Missing tenant_id Predicates

**What goes wrong:**
The existing RBAC system (`withRole(UserRole.PowerAdmin)`) checks "is this user an admin?" globally. When groups are added, the question becomes "is this user an admin OF THIS GROUP?" If group-scoped authorization is implemented by only checking user role without also verifying group membership, any admin of Group A can access Group B's data.

The existing `conduitAuth` pattern extracts `userId` from the JWT but has no concept of `groupId`. Adding group context requires either: (a) adding `groupId` to the JWT claims (stale when user switches groups), or (b) looking up the user's group membership on every request (adds a DB query). Both approaches have trade-offs and both are easy to implement incorrectly.

Critically, the existing `UserRepository.findById()` has no group filter. If a group admin endpoint calls `userRepository.findById(targetUserId)`, it returns the user regardless of which group they belong to. This is an authorization bypass that returns a 200 OK with another group's user data.

**Why it happens:**
The existing auth system was designed for a flat user model. Adding groups is a fundamental change to the authorization model, but developers treat it as "just another field on the user." The existing `withRole()` plugin inspects JWT claims but has no mechanism to check group membership against the requested resource.

**How to avoid:**
- Add `groupId` to every data access query, not just authorization checks. Every repository method that returns user data must accept a `groupId` parameter and include `WHERE group_id = ?` in the query.
- Implement a `withGroupRole` authorization plugin that extracts BOTH the user's role and their group membership, then verifies the user has the required role within the specific group being accessed.
- Store the user's `activeGroupId` in the JWT (for performance) but always verify group membership in the database for write operations (defense in depth).
- Add integration tests that specifically attempt cross-group access: "User A is admin of Group 1, attempts to access Group 2's resources -- must get 403."

**Warning signs:**
- Repository methods that take a `userId` but no `groupId`
- Admin endpoints that return user lists without filtering by group
- Tests that only test "admin can access" without testing "admin of OTHER group cannot access"
- No `GROUP_UNAUTHORIZED` or `GROUP_FORBIDDEN` error type in `AppError`

**Phase to address:**
Groups/Admin phase -- this is the most security-critical aspect and must be designed before any group-related code is written. The database schema (groups table, group_members table with role column) must enforce these constraints at the schema level with foreign keys.

---

### Pitfall 4: Cascade Delete Destroys Data When Removing Users From Groups

**What goes wrong:**
When a user is removed from a group (or a group is deleted), `ON DELETE CASCADE` on the `group_members` foreign key to `users` would delete the user entirely -- not just their membership. Conversely, if the cascade goes the other direction (deleting a group cascades to group_members), this is correct. But if there are additional tables referencing group_members (group permissions, group-scoped settings, audit logs), cascades can propagate unexpectedly far.

The existing schema uses `ON DELETE` defaults (which in Postgres is `NO ACTION`/`RESTRICT`). When adding group tables with foreign keys to the existing `users` table, getting the cascade direction wrong destroys user data.

**Why it happens:**
Developers set up foreign keys with `CASCADE` as a convenience without mapping out the full cascade graph. With Exposed R2DBC (the current ORM), cascade behavior is specified in the table definition but the effects are not immediately visible in code review. The existing `UsersTable` has no cascade dependencies, so there is no precedent in the codebase.

**How to avoid:**
- Map the cascade graph on paper before writing table definitions: `groups -> group_members <- users`. Deleting a group should cascade to group_members (correct). Deleting a user should cascade to group_members (correct). But neither cascade should propagate further.
- Use `ON DELETE RESTRICT` for references from group_members to users and groups, with explicit application-level deletion logic that checks dependencies before deletion.
- Better: use soft deletes for groups (add an `is_active` boolean) instead of hard deletes. This preserves audit trails and prevents cascade accidents.
- Write a migration test that inserts test data, performs the delete, and verifies only the intended rows were removed.

**Warning signs:**
- `CASCADE` in any table definition without a corresponding deletion test
- No soft-delete pattern for groups or users
- Admin "delete group" endpoint that does not preview affected data

**Phase to address:**
Groups/Admin phase -- database schema design. The migration that creates group tables must be accompanied by deletion tests.

---

### Pitfall 5: Admin Panel Privilege Escalation Through Role Self-Assignment

**What goes wrong:**
An Admin-level user can promote themselves to PowerAdmin if the "update user role" endpoint only checks that the caller is an admin, without verifying that the caller's role is strictly higher than the target role. Similarly, an admin could create a new user with PowerAdmin role, or change another admin's group to gain access to additional groups.

The existing `withRole(UserRole.PowerAdmin)` check in `UserRoutes.kt` only gates the "get user by ID" endpoint. There are no endpoints yet for role assignment or user management. When these are added for the admin panel, the authorization model must be carefully layered.

**Why it happens:**
Role hierarchies look simple (User < Admin < PowerAdmin) but the authorization rules for role management are non-obvious. Developers implement "admin can edit users" without the additional constraint "admin can only assign roles equal to or lower than their own role."

**How to avoid:**
- Implement a `canAssignRole` check: `callerRole.level > targetRole.level` for role changes. An Admin (level 1) cannot assign Admin or PowerAdmin. Only PowerAdmin (level 2) can assign Admin.
- No user should be able to modify their own role, period. Self-role-modification must be a hard-coded check, not just a policy.
- Group admin should only be able to manage users within their group. Cross-group user management is PowerAdmin-only.
- Add the role hierarchy check to the authorization plugin level, not just the service layer -- defense in depth.

**Warning signs:**
- Admin endpoint that accepts a `role` field in the request body without validating against the caller's role
- Tests that only test "admin can update user" without testing "admin cannot promote to PowerAdmin"
- No test for "user cannot modify their own role"

**Phase to address:**
Groups/Admin phase -- role management endpoints. The `RoleAuthorization` plugin should be extended with `canManageRole` logic.

---

### Pitfall 6: Turbine Timeout on StateFlow Testing Due to Conflation

**What goes wrong:**
StateFlow conflates emissions: if two values are emitted before the collector processes the first, the intermediate value is dropped. When testing with Turbine (`flow.test {}`), calling `awaitItem()` after triggering a state change may return a stale value (the initial state) or skip directly to the final state, depending on timing. Tests become flaky -- they pass with `StandardTestDispatcher` but fail with `UnconfinedTestDispatcher`, or vice versa.

The existing ViewModels emit states like `isLoading = true` followed by `isLoading = false` in quick succession. A Turbine test that does `awaitItem()` twice expecting to see both states will miss the `isLoading = true` state due to conflation.

**Why it happens:**
StateFlow is designed for UI observation where only the latest state matters. But tests often want to verify intermediate states (loading indicators, error flashes). Developers write tests assuming `awaitItem()` captures every emission, which is only true for `SharedFlow(replay=0)` or `Flow`.

**How to avoid:**
- For StateFlow testing, use `expectMostRecentItem()` instead of `awaitItem()` when you only care about the final state after an action.
- When intermediate states matter (e.g., verifying a loading indicator appears), structure the ViewModel to use a `Channel<Effect>` for one-shot events (navigation, snackbar) and `StateFlow` only for persistent UI state.
- Use `UnconfinedTestDispatcher` for ViewModel tests to eagerly execute coroutines, making state transitions more predictable.
- Wrap test assertions in `turbineScope {}` for multiple flow testing to avoid leaked coroutine warnings.

**Warning signs:**
- Tests that call `awaitItem()` multiple times on a StateFlow and expect to see every intermediate state
- Flaky tests that pass individually but fail when run as a suite
- Turbine timeout errors (`Timed out waiting for next item`) on StateFlow but not on SharedFlow
- Tests using `cancelAndConsumeRemainingEvents()` as a workaround without understanding why events are missing

**Phase to address:**
Testing Infrastructure phase -- establish Turbine patterns and test utilities before writing ViewModel tests. Create a `BaseViewModelTest` class that configures dispatchers correctly.

---

### Pitfall 7: Ktor testApplication Does Not Use Test Coroutine Dispatcher

**What goes wrong:**
Ktor's `testApplication` block runs with real coroutine dispatchers, not `TestCoroutineScheduler`. This means that `delay()` calls in server code (rate limiting, debouncing, scheduled tasks) run with real wall-clock time in tests. A server endpoint that delays 30 seconds will make the test wait 30 real seconds. Worse, Ktor's internal timeout mechanisms can cause `unexpected request timeout` errors (KTOR-6925) when server startup takes longer than expected under test.

For the existing codebase, the `DashboardViewModel` uses `delay(300)` which would be a real delay in integration tests. Server-side code using Arrow's `Schedule` for retry logic will also run at real speed.

**Why it happens:**
Ktor's test infrastructure was designed before `kotlinx-coroutines-test` was stable. The `testApplication` function uses `runBlocking` internally, not `runTest`. JetBrains has been migrating to `runTest` but as of Ktor 3.4.0, the test HTTP client still does not use the specified coroutine dispatcher (KTOR-7121).

**How to avoid:**
- For server integration tests, accept real timing. Keep server-side delays short or make them configurable via `Configuration` (the existing `Configuration` class already holds dispatchers -- extend it with test-overridable delay values).
- Do NOT mix `testApplication` with `runTest`. Use `testApplication` for HTTP-level integration tests and `runTest` for unit tests of services/repositories.
- For service-layer tests, inject test dispatchers through the existing `Configuration` class: `Configuration(io = testDispatcher, default = testDispatcher)`.
- Set explicit timeouts on test HTTP client requests to avoid hanging tests.

**Warning signs:**
- Server tests that take minutes to run (real delays in test paths)
- `testApplication` wrapped inside `runTest` (they conflict)
- `UnresolvedAddressException` in tests (testApplication networking quirks)
- Tests that pass locally but timeout in CI

**Phase to address:**
Testing Infrastructure phase -- establish server test patterns and base classes early. Create a `TestConfiguration` that overrides delays and dispatchers.

---

### Pitfall 8: Compose Multiplatform Plurals Formatting Breaks on WASM and Desktop

**What goes wrong:**
Compose Multiplatform's `pluralStringResource()` has known bugs with argument substitution. Using `%d` format specifiers instead of indexed format (`%1$d`) causes arguments to not be substituted on Desktop and WASM targets (GitHub issues #4675, #5040). The plural string appears as literal `%d` in the UI. This works fine on Android because Android's resource system handles both formats, masking the bug during Android-first development.

**Why it happens:**
Compose Multiplatform's resource library implements its own format string parser for non-Android targets. This parser requires indexed format specifiers (`%1$d`, `%1$s`) because it maps arguments by position index, unlike Android's `String.format` which handles both. Developers coming from Android use `%d` habitually and only discover the issue when testing on WASM or Desktop.

**How to avoid:**
- Always use indexed format specifiers in string resources: `%1$d` instead of `%d`, `%1$s` instead of `%s`. This works on all targets including Android.
- Lint the `strings.xml` files for unindexed format specifiers as part of the build.
- Test localized strings on WASM early, not just Android. A single missing index causes silent data display bugs (not crashes).
- For complex plurals (e.g., "1 member" vs "5 members"), define all CLDR plural categories (zero, one, two, few, many, other) even if the language does not use them all -- the library may select unexpected categories on some platforms.

**Warning signs:**
- Literal `%d` or `%s` appearing in UI text on Desktop/WASM
- Plural strings that work on Android but show wrong quantities on other targets
- Missing `other` plural category (required as fallback on all platforms)

**Phase to address:**
Localization phase -- establish string resource conventions and lint rules before any translations are added.

---

### Pitfall 9: Localization Resource Loading on WASM Is Async and Composable-Only

**What goes wrong:**
On Android and JVM, `stringResource()` resolves synchronously from bundled resources. On WASM, resources are fetched over the network (they are served as separate files by the web server). This means: (a) there is a brief moment where strings are not yet available on first load, (b) non-composable code cannot access string resources because `stringResource()` is a `@Composable` function, and (c) the resource loading adds to initial page load time, especially with many locale files.

If error messages from the SDK layer (which is not composable) need to be localized, there is no straightforward way to access string resources outside of Compose.

**Why it happens:**
The existing `AppError` hierarchy has hardcoded English messages (`"Email or password is incorrect"`). Developers naturally want to localize these by replacing the hardcoded strings with resource lookups. But `AppError` is a sealed class in `core:models` which has no Compose dependency and cannot call `stringResource()`.

**How to avoid:**
- Use error codes (already present: `AUTH_INVALID_CREDENTIALS`, etc.) as localization keys. Map codes to localized strings at the UI layer, not in the model layer.
- Create a composable `ErrorMessageMapper` that takes an `AppError` and returns a localized string: `@Composable fun AppError.localizedMessage(): String = stringResource(errorCodeToResource(this.code))`.
- For WASM initial load, use hardcoded English strings as fallback while resources load, then recompose when resources are available.
- Keep the total size of string resource files small. On WASM, each locale is a separate HTTP request. Bundle only the active locale, not all locales.

**Warning signs:**
- `stringResource()` called outside of `@Composable` context
- Import of `compose.resources` in non-UI modules (`:core:models`, `:core:sdk`)
- WASM app showing blank text for a frame on first load
- Error messages not matching the selected locale

**Phase to address:**
Localization phase -- must design the error code to localized string mapping pattern before localizing any error messages.

---

### Pitfall 10: Testing Koin ViewModel Injection Across KMP Targets

**What goes wrong:**
The existing `appModule` uses `viewModelOf(::LoginViewModel)` which relies on Koin's reflection-based constructor injection. In tests, replacing dependencies (like `AuthApi`) with mocks requires either: (a) using Koin's `declareMock()` which depends on MockK/Mockito (JVM-only), or (b) creating manual fakes for each dependency. On non-JVM test targets (wasmJsTest, iosTest), neither MockK nor Mockito is available.

The existing test files (`ComposeAppCommonTest.kt`, `SharedCommonTest.kt`) are placeholder tests with no actual DI or ViewModel testing. When real ViewModel tests are added, the mock library choice determines which targets can run tests.

**Why it happens:**
Koin's testing documentation focuses on JVM/Android. The `declareMock()` API delegates to Mockito or MockK, neither of which works on WASM or iOS. Developers set up ViewModel tests on JVM, then discover they cannot run the same tests on other targets.

**How to avoid:**
- Use manual fakes (hand-written test doubles) instead of mocking libraries for all KMP-shared test code. Create interfaces for `AuthApi` and `UserApi`, with production implementations and test fakes.
- The existing `AuthApi` and `UserApi` are classes, not interfaces. Refactoring them to interfaces is the first step toward testability. This is a prerequisite for the testing phase.
- Place JVM-specific mock-based tests in `jvmTest` source set. Place KMP-portable fake-based tests in `commonTest`.
- Use Koin's `koinApplication { modules(...) }` in tests with fake modules, not global `startKoin {}`.

**Warning signs:**
- Test code importing MockK or Mockito in `commonTest`
- `AuthApi` and `UserApi` used as concrete classes in test assertions (cannot substitute)
- Tests that only exist in `jvmTest` or `androidUnitTest` with no `commonTest` equivalents
- `koin.verify()` not called in any test suite

**Phase to address:**
Testing Infrastructure phase -- must refactor SDK APIs to interfaces and create fake implementations before writing ViewModel tests. This is blocking work that should happen early.

---

### Pitfall 11: MVI One-Shot Events Lost on Configuration Change

**What goes wrong:**
The existing ViewModels use boolean flags in the state for navigation triggers: `loginSuccess`, `logoutTriggered`, `registerSuccess`. These are consumed by `LaunchedEffect` in `AppNavHost.kt`. If the composable recomposes before the flag is reset, the navigation fires twice. If the Activity is destroyed and recreated (Android process death), the flag persists in state and triggers navigation again when the new Activity reads it.

MVI purists use `Channel<SideEffect>` for one-shot events. But `Channel` has its own pitfall: if the UI is not collecting when the event is sent, the event is buffered. On Android configuration change, the old collector is cancelled and a new one starts -- buffered events fire in the new lifecycle, which may be correct or not depending on the event type.

**Why it happens:**
There is an ongoing "MVI events debate" in the Android community. The current codebase uses state flags (the simplest approach) which works for the current small navigation graph. Scaling to groups, admin panel, and multiple navigation destinations amplifies the problem.

**How to avoid:**
- For navigation events: use a `Channel<NavigationEvent>(BUFFERED)` in the ViewModel, consumed by `LaunchedEffect(Unit)` in the host. The buffer ensures events survive brief collector gaps (configuration change).
- For UI feedback (toasts, snackbars): use a `Channel<UiEffect>(BUFFERED)` that is consumed-and-cleared. Do NOT put these in StateFlow.
- Remove state flags like `loginSuccess` from the state data class. Replace with channel-based events.
- For the existing codebase, the migration path is: keep `StateFlow` for UI state, add a `Channel<Effect>` for one-shot events, consume both in the composable.

**Warning signs:**
- Boolean flags like `loginSuccess`, `logoutTriggered` in state data classes
- `LaunchedEffect(state.someFlag)` pattern that triggers on flag change
- Double navigation on slow devices
- Toast appearing twice after screen rotation

**Phase to address:**
MVI ViewModel phase -- the base MVI pattern must define the event channel pattern alongside the state reducer.

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Concrete classes for SDK APIs (AuthApi, UserApi) instead of interfaces | Less boilerplate, quicker to write | Cannot substitute in tests on non-JVM targets, blocks dependency injection patterns | Never for a template -- interfaces are required for proper testability |
| State flags for navigation (`loginSuccess: Boolean`) instead of event channels | Simpler initial implementation, no Channel complexity | Double-fire on recomposition, events lost on process death, grows unwieldy with many destinations | MVP only -- must migrate before adding admin panel navigation |
| Hardcoded English strings in AppError | Immediate readability, no localization infrastructure needed | Cannot localize error messages without touching model layer; error codes already exist but are unused for display | Acceptable until Localization phase, but do NOT add more hardcoded strings |
| Global role check without group scope | Works for flat user model | Breaks authorization when groups are added; retrofit is expensive because every repository query needs `groupId` | Never once groups exist -- must be designed from the start |
| Single test dispatcher for all tests | Simpler test configuration | Cannot test timing-dependent behavior, hides real threading bugs | Acceptable for unit tests; integration tests need real dispatchers |
| Skip plural categories in localization | Fewer XML entries per locale | English "zero" category not used, but other languages need it (Arabic has 6 categories); adding later means touching every string file | Never -- define all categories upfront for each string |

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| Groups -> existing UsersTable | Adding `group_id` directly to UsersTable (makes users single-group-only) | Create a separate `group_members` join table with `user_id`, `group_id`, `role_in_group`. Users can belong to multiple groups. |
| Groups -> existing JWT claims | Not including group context in JWT, requiring DB lookup on every request | Add `active_group_id` to JWT claims for read performance. Verify group membership in DB for write/admin operations. Refresh JWT when user switches groups. |
| Groups -> existing withRole plugin | Reusing the existing `RoleAuthorizationPlugin` which checks global role | Create a new `GroupRoleAuthorizationPlugin` that checks role within the group context. Keep `withRole` for system-level endpoints (PowerAdmin-only). |
| MVI ViewModel -> existing SDK (Either<AppError, T>) | Calling `.fold()` directly in the ViewModel coroutine without handling cancellation | Wrap SDK calls in a `processIntent` function that catches `CancellationException` and rethrows it, then maps `Either.Left` to error state. Never catch `CancellationException` in error mapping. |
| Testing -> existing Configuration class | Creating new test helpers instead of using the existing dispatcher injection | Extend `Configuration` with a test factory: `Configuration.forTest(testDispatcher)`. The existing `io` and `default` dispatcher fields are already injectable. |
| Localization -> existing AppError codes | Trying to make `AppError.message` return localized strings | Keep `AppError.message` as English-only debug messages. Use `AppError.code` as the localization key. Map at the UI layer with `@Composable fun AppError.localizedMessage()`. |
| Localization -> WASM resource loading | Assuming `stringResource()` is available immediately on page load | Resources are fetched async on WASM. Use `Res.readBytes()` for pre-loading, or accept a brief English fallback on first frame. |
| Admin Panel -> existing TerminalTheme | Building admin components with Material3 defaults instead of the custom design system | The existing `TerminalTheme` provides colors, typography, spacing, gaps, shadows, opacity, radius, and borders. Admin panel must use `TerminalTheme.colors`, `TerminalTheme.typography` etc., not `MaterialTheme`. |

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| MVI reducer copying large state objects | UI jank during rapid state updates (typing, scrolling), GC pressure on Android | Keep state data classes shallow. Use `@Immutable` annotation. Do not put lists or maps directly in state -- use `ImmutableList` from kotlinx.collections.immutable. | Noticeable with state objects containing 10+ fields or lists > 50 items |
| Group membership lookup on every request | Admin panel requests take 50-100ms extra due to DB roundtrip for group verification | Cache group membership in JWT claims for read-only checks. Use a short-lived in-memory cache (60s TTL) for hot paths. Invalidate on membership changes. | Noticeable at > 20 requests/second to group-scoped endpoints |
| Loading all group members in admin list | Admin panel hangs when a group has 1000+ members | Paginate group member queries. Default page size of 25. Use keyset pagination (not OFFSET) for stable performance with Exposed. | Groups with > 100 members |
| Localization loading all locale files upfront on WASM | Initial page load takes 2-3+ seconds with many locales | Load only the active locale. Lazy-load alternate locales when user changes language in settings. Bundle default locale in the WASM binary if possible. | More than 3 locale files, or any locale file > 50KB |
| Re-rendering entire admin table on any state change | Scrolling stutters, composable recomposition metrics show 100% recomposition | Use `key()` on list items, `LazyColumn` with stable keys, and `derivedStateOf` for filtered/sorted views. Hoist filter state out of the table composable. | Tables with > 30 rows visible simultaneously |

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| Admin endpoint that accepts `role` in request body without hierarchy check | Admin user promotes self to PowerAdmin, gaining system-wide access | Validate `callerRole.level > targetRole.level` on every role change. Block self-role-modification entirely. |
| Group-scoped queries that filter in application code instead of SQL | Memory-load all users then filter by groupId in Kotlin -- data from other groups briefly exists in server memory | Always include `group_id` in SQL WHERE clauses. Never fetch unscoped data and filter post-query. |
| Missing group_id in JWT validation for group-scoped endpoints | User switches groups in another tab, stale JWT grants access to old group | Verify `active_group_id` claim matches the requested resource's group. Require re-auth or JWT refresh on group switch. |
| Admin panel exposing password hashes or internal IDs | Admin UI leaks implementation details in API responses | Create separate `AdminUserResponse` DTO that explicitly includes only the fields needed. Never reuse `UserRecord` (which has `passwordHash`) in API responses. The existing `UserRecord` -> `UserResponse` mapping in `UserService` is correct, but new admin DTOs need the same discipline. |
| Test auth tokens in localization strings | Developer puts `Bearer eyJ...` in a translation file or test fixture that gets committed | Use a test JWT generator (extend `JwtTokenProvider` with a test helper). Never hardcode tokens. Lint for `eyJ` in non-test files. |

## UX Pitfalls

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| Admin panel uses different visual language than main app | Admin feels like a separate product, breaks user trust, confusing navigation | Admin panel must use the same `TerminalTheme` design system. Admin-specific components (data tables, role badges) should be additions to the design system, not replacements. |
| Locale change requires app restart | User changes language in settings, nothing happens until they force-close and reopen | Use Compose's recomposition: store locale in a `StateFlow`, provide it via `CompositionLocalProvider`. Locale changes trigger full recomposition of `App()`. |
| Missing translation shows key instead of English fallback | User sees `admin.group.delete.confirm` instead of "Are you sure you want to delete this group?" | Always define an `other` category and ensure `values/strings.xml` (default/English) has every key. Compose Multiplatform falls back to the default resource qualifier automatically. |
| MVI loading states not shown for admin operations | Admin clicks "Remove user from group", nothing happens for 2 seconds, then the list updates | Every intent must immediately transition state to `isLoading = true` before the async operation. Use optimistic UI for non-destructive operations (add member), confirmation dialogs for destructive ones (remove, delete). |
| Group name validation only on server | User types a 200-character group name, submits, waits for server round-trip, gets validation error | Mirror validation rules in `core:models` (already done for email/name validation). Validate group name length and format in the ViewModel before submitting. |

## "Looks Done But Isn't" Checklist

- [ ] **MVI ViewModel:** Reducer processes all intents -- verify that rapid intent submission (hold down Enter on login) does not produce inconsistent state. Test with `UnconfinedTestDispatcher`.
- [ ] **Group Authorization:** Admin can manage own group -- verify admin of Group A CANNOT see Group B's members, CANNOT modify Group B's settings, CANNOT invite users to Group B. Requires explicit cross-group tests.
- [ ] **Group Deletion:** Group can be deleted -- verify all group_members rows are cleaned up, users are NOT deleted, group-scoped data is archived/deleted, cascade does not propagate to users table.
- [ ] **Admin Role Management:** Admin can change user roles -- verify admin cannot promote to own level or above, cannot modify own role, PowerAdmin can modify admin roles, role change takes effect on next JWT refresh.
- [ ] **Turbine Tests:** ViewModel tests pass -- verify they pass with BOTH `StandardTestDispatcher` and `UnconfinedTestDispatcher`. If they only pass with one, the test has a timing dependency.
- [ ] **Server Integration Tests:** testApplication tests pass -- verify they also pass when run in parallel (`maxParallelForks > 1`). testApplication port conflicts are common.
- [ ] **Localization Plurals:** Plurals work on Android -- verify the same plural strings work on WASM and Desktop. Check with `%1$d` indexed format. Test with RTL languages if supported.
- [ ] **Localization Fallback:** Spanish locale works -- verify that a key missing from Spanish falls back to English (default), not to an empty string or key name.
- [ ] **WASM Localization Loading:** Strings display on WASM -- verify the first frame does not show blank text or English before the locale loads. Measure initial load time with locale resources.
- [ ] **ViewModel Cancellation:** ViewModel handles errors -- verify that navigating away mid-API-call does not crash (viewModelScope cancellation must propagate correctly through Either/fold).

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| MVI state mutation ordering bugs | LOW | Introduce intent Channel and reducer function; refactor one ViewModel at a time; each is independent |
| Cross-platform dispatcher issues | LOW | Replace `Dispatchers.IO` with `Dispatchers.Default` in shared code; add `withContext` wrappers; mechanical refactor |
| Group authorization bypass | HIGH | Requires schema migration to add group_id constraints; every repository method must be audited; every existing test must be updated; cannot be done incrementally |
| Cascade delete destroying data | HIGH | Requires data recovery from backup; schema migration to fix cascade rules; cannot be done without downtime |
| Admin privilege escalation | MEDIUM | Add role hierarchy check to authorization plugin; audit existing role assignments in database; add integration tests |
| Turbine StateFlow conflation | LOW | Switch from `awaitItem()` to `expectMostRecentItem()` or restructure tests; mechanical change |
| testApplication timing issues | LOW | Extract delays to Configuration; create TestConfiguration with zero delays; update test base class |
| Plural format specifiers wrong | LOW | Find-and-replace `%d` with `%1$d` in all strings.xml files; lint rule prevents regression |
| WASM resource loading delay | MEDIUM | Implement resource preloading or embed default locale in binary; requires build config changes |
| Concrete SDK classes blocking testing | MEDIUM | Extract interfaces from AuthApi/UserApi; update Koin modules to bind interfaces; create fake implementations; about 2-3 hours of work |
| Navigation state flags causing double-fire | LOW | Replace boolean flags with Channel events; update LaunchedEffect consumers; one ViewModel at a time |

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| State mutation ordering (CAS races) | MVI ViewModel | Rapid-fire intent test passes; no state regression under UnconfinedTestDispatcher |
| viewModelScope dispatcher differences | MVI ViewModel | ViewModel tests pass on jvmTest, wasmJsTest, and iosSimulatorArm64Test |
| Group authorization bypass | Groups/Admin | Integration test: admin of Group A gets 403 accessing Group B resources |
| Cascade delete data loss | Groups/Admin | Migration test: delete group, verify users table unchanged |
| Admin privilege escalation | Groups/Admin | Test: Admin (level 1) attempts to assign PowerAdmin (level 2) -> 403 |
| Turbine StateFlow conflation | Testing Infrastructure | All ViewModel tests pass with both Standard and Unconfined dispatchers |
| testApplication dispatcher issues | Testing Infrastructure | Server integration tests complete in < 30s; no real `delay()` in test paths |
| Plural formatting on WASM/Desktop | Localization | Plural strings render correctly on all 4 targets with indexed format specifiers |
| WASM resource loading async | Localization | WASM first-frame-to-interactive < 2s with 3 locale files |
| Concrete SDK classes block testing | Testing Infrastructure | AuthApi and UserApi have interface and fake implementation; commonTest uses fakes |
| One-shot event double-fire | MVI ViewModel | Navigation event fires exactly once per intent, verified across configuration change |
| Admin panel theme inconsistency | Groups/Admin | Admin components use only TerminalTheme tokens; no MaterialTheme imports in admin module |

## Sources

- [Kotlin Multiplatform ViewModel Lifecycle](https://developer.android.com/kotlin/multiplatform/viewmodel) -- dispatcher differences across targets
- [Compose Multiplatform Lifecycle Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-lifecycle.html) -- WASM lifecycle state skipping
- [MVI Event Management Best Practice (2026)](https://android.benigumo.com/20260207/mvi-combine-merge/) -- StateFlow + Channel pattern for events
- [Clean MVI Architecture for Compose Multiplatform](https://medium.com/@KotlinCraft/clean-and-scalable-mvi-architecture-for-compose-multiplatform-034da423ffc9) -- MVI pattern pitfalls
- [Data Loading in Kotlin with MVI/Flow](https://nek12.dev/blog/en/how-to-load-data-in-kotlin-with-mvvm-mvi-flow-coroutines-complete-guide/) -- init block flow collection pitfalls
- [Turbine GitHub](https://github.com/cashapp/turbine) -- StateFlow testing patterns
- [StateFlow Testing All Emissions (kotlinx.coroutines #3939)](https://github.com/Kotlin/kotlinx.coroutines/issues/3939) -- conflation during testing
- [Testing Android Flows with Turbine](https://proandroiddev.com/testing-android-flows-in-viewmodel-with-turbine-ea9bae7e811a) -- awaitItem vs expectMostRecentItem
- [Ktor testApplication Timeout (KTOR-6925)](https://youtrack.jetbrains.com/issue/KTOR-6925/testApplication-unexpected-request-timeout-when-server-startup-takes-more-time-than-timeout) -- test dispatcher issue
- [Ktor Test HTTP Client Dispatcher (KTOR-7121)](https://youtrack.jetbrains.com/issue/KTOR-7121/testApplication-Test-HTTP-client-does-not-use-specified-coroutine-dispatcher) -- dispatcher not honored
- [Multi-Tenant RBAC Best Practices](https://www.aserto.com/blog/authorization-101-multi-tenant-rbac) -- tenant_id everywhere pattern
- [Multi-Tenant Authorization (Permit.io)](https://www.permit.io/blog/best-practices-for-multi-tenant-authorization) -- role-within-tenant enforcement
- [RBAC Model for Multi-Tenant SaaS (WorkOS)](https://workos.com/blog/how-to-design-multi-tenant-rbac-saas) -- actor -> action -> resource model
- [Compose Multiplatform Plural Formatting (#4675)](https://github.com/JetBrains/compose-multiplatform/issues/4675) -- %d vs %1$d issue
- [Compose Multiplatform Plural String (#5040)](https://github.com/JetBrains/compose-multiplatform/issues/5040) -- plural not working properly
- [Compose Multiplatform Resources Usage](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html) -- resource qualifiers, fallback behavior
- [Localizing Strings in KMP](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-localize-strings.html) -- official localization guide
- [Arrow Typed Errors with Flow](https://arrow-kt.io/learn/typed-errors/working-with-typed-errors/) -- Raise DSL scope leaking
- [Koin Testing Documentation](https://insert-koin.io/docs/reference/koin-test/testing/) -- declareMock, JVM-only limitation
- [CancellationException Handling in Coroutines](https://www.netguru.com/blog/exceptions-in-kotlin-coroutines) -- do not catch CancellationException

---
*Pitfalls research for: Adding MVI ViewModel, Groups/Admin, Testing, and Localization to KMP Full-Stack Template*
*Researched: 2026-02-17*
