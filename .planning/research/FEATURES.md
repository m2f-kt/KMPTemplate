# Feature Research

**Domain:** KMP Full-Stack Template -- Milestone 2 (MVI ViewModel, Groups/Admin, Testing, Localization)
**Researched:** 2026-02-17
**Confidence:** HIGH (codebase-verified patterns, official docs corroborated)

## Context: What Already Exists

Before defining new features, here is what Milestone 1 delivered that we build upon:

- **ViewModels:** 5 existing ViewModels (`LoginViewModel`, `RegisterViewModel`, `ForgotPasswordViewModel`, `ProfileViewModel`, `DashboardViewModel`) all using `androidx.lifecycle.ViewModel` + `MutableStateFlow` + `viewModelScope.launch`. No formal MVI contract -- each ViewModel has ad-hoc methods (`onXChange`, `login`, `register`, etc.).
- **SDK:** `AuthApi` and `UserApi` returning `Either<AppError, T>` via `apiCall` wrapper. Arrow `Raise` DSL on server.
- **RBAC:** `UserRole` sealed class (User/Admin/PowerAdmin) with `RoleAuthorizationPlugin` for server-side route protection. `withRole()` extension on `Route`. Roles stored in separate `RolesTable` with FK from `UsersTable`.
- **State classes:** Data classes (`LoginState`, `RegisterState`, etc.) with flat fields. No sealed Intent/Mutation/Event hierarchy.
- **Tests:** Only placeholder tests (`assertEquals(3, 1+2)`) in `commonTest`. Zero real test coverage. `testing-server` bundle declared in version catalog but unused.
- **Localization:** Zero. Only fonts in `composeResources`. No `values/strings.xml`.
- **Groups:** Zero. No group concept anywhere. Users are flat -- no organizational grouping.

---

## Feature Landscape

### Table Stakes (Users Expect These)

Features that make this milestone feel complete. Without them, the new capability areas feel half-baked.

#### 1. MVI ViewModel Foundation

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **Base MVI ViewModel abstract class** | The 5 existing ViewModels all duplicate flow/state boilerplate. A template must provide the reusable foundation, not leave users to reinvent it per screen. | MEDIUM | Based on the AiraloViewModel reference: `MutableSharedFlow<Intent>` for intake, `StateFlow<Model>` for state, `SharedFlow<Event>` for one-shots. `take(intent)` public entry point, `reduce(model, mutation): Model` pure function, `sendMutation()`/`sendEvent()` internal routing. Arrow `Either<Event, Mutation>` flow for unifying mutation vs event dispatching through one `MutableSharedFlow`. |
| **Intent sealed interface per ViewModel** | Users expect a single entry point for all actions. Scattered `onXChange()` / `doY()` methods are the anti-pattern MVI eliminates. | LOW | Each feature ViewModel defines `sealed interface FooIntent`. The `take(intent: FooIntent)` method replaces all individual public methods. Composable sends `viewModel.take(FooIntent.EmailChanged("..."))`. |
| **Model (state) data class** | Already exists as `LoginState`, etc. Needs to be renamed/retyped to `Model` within the MVI contract for consistency. | LOW | Keep existing data class shape. Rename convention from `XState` to `XModel` (or alias). Must remain immutable `data class`. |
| **Mutation sealed interface** | Mutations represent state transitions the reducer applies. Without explicit mutations, `_state.update { ... }` calls are scattered everywhere with no testable contract. | LOW | `sealed interface FooMutation`. The `reduce(model, mutation): Model` function is a `when` over mutations. Pure function -- zero side effects, trivially testable. |
| **Event sealed interface (one-shots)** | Navigation triggers, toast messages, analytics events. Currently handled via boolean flags in state (`loginSuccess`, `logoutTriggered`) which is a well-known anti-pattern (must be manually reset, races on recomposition). | LOW | `sealed interface FooEvent`. Collected via `SharedFlow` in the UI layer. Consumed exactly once. Replaces `loginSuccess: Boolean` pattern. |
| **Migrate existing ViewModels to MVI** | 5 ViewModels exist. If the base class ships but existing VMs are not migrated, the template contradicts its own pattern. | HIGH | `LoginViewModel`, `RegisterViewModel`, `ForgotPasswordViewModel`, `ProfileViewModel`, `DashboardViewModel` all need conversion. Each gets Intent/Model/Mutation/Event sealed types and a `reduce` function. Dependencies on `AuthApi`/`UserApi` remain the same. |
| **MVI test DSL (Turbine-based)** | The entire point of MVI is testability. If the pattern ships without a test utility, it is incomplete. Turbine is the standard for Flow assertion in Kotlin. | MEDIUM | Test DSL: `viewModel.test { take(SomeIntent); awaitModel { shouldBe expectedModel }; awaitEvent { shouldBe expectedEvent } }`. Wraps Turbine's `Flow.test {}` with `awaitItem()`. Uses kotest assertions (`shouldBe`, `assertSoftly`). Needs `kotlinx-coroutines-test` `runTest` for virtual time. Must add `app.cash.turbine:turbine` to version catalog. |

**Expected MVI ViewModel skeleton (based on AiraloViewModel reference):**

```kotlin
abstract class MVIViewModel<Intent, Model, Mutation, Event>(
    initialModel: Model
) : ViewModel() {

    private val intents = MutableSharedFlow<Intent>(extraBufferCapacity = 64)
    private val _model = MutableStateFlow(initialModel)
    val model: StateFlow<Model> = _model.asStateFlow()

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 64)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            intents.collect { intent ->
                handleIntent(intent)
                    .collect { result ->
                        result.fold(
                            ifLeft = { event -> _events.emit(event) },
                            ifRight = { mutation -> _model.update { reduce(it, mutation) } }
                        )
                    }
            }
        }
    }

    fun take(intent: Intent) {
        intents.tryEmit(intent)
    }

    abstract fun handleIntent(intent: Intent): Flow<Either<Event, Mutation>>
    abstract fun reduce(model: Model, mutation: Mutation): Model
}
```

#### 2. Testing Infrastructure

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **ViewModel unit tests with Turbine** | MVI exists for testability. Shipping MVI without tests defeats the purpose. | MEDIUM | Each migrated ViewModel gets tests: `runTest { viewModel.model.test { take(intent); awaitItem() shouldBe ... } }`. Need `Dispatchers.setMain(testDispatcher)` in `@BeforeTest`. |
| **Server integration tests (Ktor testApplication)** | The `testing-server` bundle exists in version catalog but is unused. Auth routes, user routes, and RBAC must have tests. | HIGH | Use `testApplication { application { module() } }`. Test: register -> login -> refresh -> logout flow. Test: RBAC enforcement (user cannot access admin routes). Test: validation errors return proper error codes. Uses H2 in-memory DB for speed (already in catalog), or Testcontainers PostgreSQL for fidelity. |
| **SDK tests with mock HttpClient** | The SDK (`AuthApi`, `UserApi`) is the contract between client and server. Must be tested independently. | MEDIUM | Ktor `MockEngine` to simulate server responses. Test: `apiCall` maps 401 to `AppError.Auth.Unauthorized`. Test: `AuthInterceptor` refreshes on 401. Test: successful deserialization of `UserResponse`. |
| **Test fixtures and fakes module** | Repeating fake data across test files is maintenance hell. | LOW | Shared `test-fixtures` module (or `commonTest` source set in `:core:models`): `FakeAuthApi`, `FakeUserApi`, `FakeTokenStorage`, factory functions like `aUserResponse()`, `aLoginRequest()`. |
| **Kotest assertion conventions** | Codebase already declares `kotest-assertionsCore`, `kotest-arrow`, `kotest-arrow-fx`. Using them consistently is table stakes. | LOW | All tests use `shouldBe`, `shouldBeRight()`, `shouldBeLeft()`, `assertSoftly {}`. No mixing with JUnit `assertEquals`. Kotest Arrow assertions for `Either` results. |

#### 3. Groups/Admin Management

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **Group entity (server-side)** | Multi-user applications need organizational grouping. A flat user list does not scale past a toy app. The existing RBAC (User/Admin/PowerAdmin) has no scope -- Admin of what? Groups answer that question. | HIGH | New `GroupsTable`: id (UUID), name, createdBy (FK to users), createdAt, updatedAt. New `GroupMembershipsTable`: id, groupId (FK), userId (FK), role (group-scoped role: owner/admin/member), joinedAt. Migration creates tables. |
| **Admin creates group** | An Admin or PowerAdmin creates a group and becomes its owner. | MEDIUM | `POST /api/groups` (Admin+ only). Returns `GroupResponse` with id, name, memberCount. Server-side: `GroupService.createGroup()`, `GroupRepository`. SDK: `GroupApi.createGroup()`. |
| **Admin manages group members** | Add/remove users, change roles within a group. | HIGH | `POST /api/groups/{id}/members` (add), `DELETE /api/groups/{id}/members/{userId}` (remove), `PUT /api/groups/{id}/members/{userId}` (change role). Validation: cannot remove last owner, cannot change own role below owner if sole owner. |
| **Admin registers users into group** | Admin can create user accounts pre-assigned to their group. The new user gets an email with credentials or activation link. | MEDIUM | `POST /api/groups/{id}/register` -- similar to normal registration but: (a) requires Admin+ caller, (b) auto-assigns group membership, (c) optionally sets initial group role. Reuses `AuthService.register()` logic with group context. |
| **Admin dashboard view** | Admin sees different content: group list, member count, management actions. Standard user sees only their groups (read-only). | MEDIUM | Client: `AdminDashboardScreen` conditionally shown when `user.role >= Admin`. Lists groups with member counts. Links to group detail/management. Leverages existing `UserRole` sealed class for conditional rendering. |
| **Group-scoped content visibility** | Content (dashboard data, AI conversations, etc.) can be scoped to a group. Members see group content. | HIGH | This is the deeper value: groups are not just user containers, they scope data access. `groupId` column on relevant tables. Server middleware: `withGroup(groupId)` that verifies caller membership. |
| **Shared route definitions for groups** | Type-safe `@Resource` routes in `:core:models`, matching existing `Auth`/`Users`/`Ai` pattern. | LOW | `Groups` resource class with nested `ById`, `Members`, `Register` sub-resources. SDK: `GroupApi` class following `AuthApi`/`UserApi` pattern. |

#### 4. Localization System

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **Client-side string resources** | All hardcoded strings in UI composables and ViewModels must come from resource files. The template currently has zero `strings.xml` files. | MEDIUM | Use Compose Multiplatform's built-in `composeResources/values/strings.xml` system. Generated `Res.strings` accessor. `stringResource(Res.strings.login_button)` in composables. Existing hardcoded strings in auth screens ("Email must not be blank", "Password must not be blank", etc.) must migrate. |
| **Locale-qualified resource directories** | Support at least English + one other language to prove the pattern works. | LOW | `composeResources/values/strings.xml` (English default), `composeResources/values-es/strings.xml` (Spanish example). Pattern documented so users add more languages. |
| **Shared string key enum** | ViewModels produce validation error messages using hardcoded strings. These must reference the same keys the UI uses, from shared code. | MEDIUM | `StringKey` enum in `:core:models` (or new `:core:localization` module). ViewModels emit `StringKey.EMAIL_REQUIRED` instead of `"Email must not be blank"`. Composable maps `StringKey` to `stringResource(...)`. Non-composable contexts (ViewModel, SDK) work with keys, not resolved strings. |
| **Server-side localized error messages** | Server currently returns hardcoded English error messages. Should return error codes and let the client localize. | MEDIUM | Server error responses already use `ErrorResponse(code, message)`. Strategy: server sends `code` (e.g., `"EMAIL_REQUIRED"`), client maps code to localized string. Server `message` becomes a fallback only. Existing `AppError` hierarchy already has `code` fields. |
| **Locale detection and switching** | App detects system locale. Power users can override. | LOW | Compose Multiplatform handles locale detection automatically from system settings. Optional: locale override in app settings stored in `multiplatform-settings`. |

---

### Differentiators (Competitive Advantage)

Features that elevate this template above competitors. These are not required but significantly increase perceived value.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **MVI test DSL as a first-class library** | No KMP template ships a ViewModel test DSL. Most show raw `runTest` + manual Flow collection. A `viewModel.test { take(...); awaitModel { ... } }` DSL is a genuine DX win that developers will adopt. | MEDIUM | Package as a reusable utility in `:core:testing` module. Works with any `MVIViewModel` subclass. Wraps Turbine + kotest. Could be extracted as a standalone library later. |
| **Group-scoped RBAC** | Existing templates have flat RBAC (global roles). Group-scoped roles (owner/admin/member per group) is a multi-tenant pattern that real SaaS apps need. The existing `UserRole` hierarchy becomes the global tier, while group roles handle per-group permissions. | HIGH | Two-tier auth: global `UserRole` (User/Admin/PowerAdmin) for platform-level access + per-group `GroupRole` (Owner/Admin/Member) for group-level access. The `withRole()` middleware handles global; new `withGroupRole()` handles group-scoped. This is a genuine architectural differentiator. |
| **Admin invitation flow with pre-registration** | Admin registers users into their group before the user signs up. User receives email with activation link. No other KMP template handles delegated user creation. | MEDIUM | `POST /api/groups/{id}/invite` creates a pending user record. Email contains activation token. User visits activation link, sets password, becomes active group member. Reuses `PasswordResetService` token pattern for activation. |
| **Server integration test harness with Testcontainers** | The `testcontainers` and `testcontainers-postgresql` dependencies are already declared. Providing a working test harness that spins up real PostgreSQL in CI is a differentiator -- most templates skip this entirely or use H2 only. | MEDIUM | Abstract `IntegrationTestBase` class: spins up Testcontainers PostgreSQL, runs migrations, provides `testApplication` with real DB. Example tests prove: register -> login -> create group -> add member -> verify RBAC. |
| **Full-stack test coverage strategy** | Demonstrating ViewModel tests + SDK tests + server integration tests + shared model tests in one template is unheard of. It proves the architecture is testable end-to-end. | HIGH | Four test layers documented and implemented: (1) `:core:models` -- serialization round-trip tests, (2) `:core:sdk` -- MockEngine tests, (3) `:app:*` -- ViewModel Turbine tests, (4) `:server` -- testApplication integration tests. Each layer has its own test utilities. |
| **Pluralization and parameterized strings** | Beyond basic key-value localization, supporting `pluralStringResource` and `%s`/`%d` parameters shows production readiness. | LOW | `<plurals>` in strings.xml. `pluralStringResource(Res.plurals.member_count, count, count)`. Parameterized: `stringResource(Res.strings.welcome, userName)`. |

### Anti-Features (Commonly Requested, Often Problematic)

Features that seem like they belong in this milestone but should be deferred or avoided.

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| **Full Redux/Orbit MVI library** | Developers familiar with Redux or Orbit-MVI want their preferred framework. | Adding a third-party MVI library creates a heavy dependency and opinionated architecture that may not match the template's Arrow-based patterns. Orbit adds its own test utilities, DSL, and conventions that conflict with our Turbine + kotest approach. The reference implementation (AiraloViewModel) is lightweight and custom. | Ship the lightweight `MVIViewModel` base class. It is 40 lines of code, not a library dependency. Users who want Orbit can swap it in. |
| **Compose UI snapshot tests** | Visual regression testing sounds valuable for a component library. | Compose Multiplatform snapshot testing tooling is immature across KMP targets. Paparazzi is Android-only. Screenshot testing for iOS/Desktop/Web has no stable solution. Adding platform-specific snapshot tests to a template creates maintenance burden. | Focus on ViewModel behavior tests (deterministic, fast, cross-platform). Document how to add Paparazzi for Android-specific snapshot testing if desired. |
| **Server-side i18n with Accept-Language** | HTTP `Accept-Language` header for server-side localization. | Server should return error codes, not localized strings. Localizing server responses requires maintaining translation files on the server, which duplicates the client's `strings.xml` and creates sync problems. Server i18n is a different problem than client i18n. | Server returns `ErrorResponse(code = "EMAIL_REQUIRED", message = "fallback English")`. Client maps `code` to locale-appropriate string. Clean separation of concerns. |
| **Runtime language switching without restart** | Users want to change language without restarting the app. | Compose Multiplatform's resource system uses system locale. Runtime switching requires either platform-specific locale override APIs (different on Android vs iOS vs Desktop) or wrapping every string call in a custom provider, which defeats the built-in `stringResource()` system. | Support system locale detection (automatic). Document per-platform locale override for advanced users. Most apps follow the system setting -- forcing an in-app language picker is over-engineering for a template. |
| **Multi-tenancy with data isolation** | Groups could use schema-per-tenant or DB-per-tenant isolation. | Extreme complexity for a template. Schema-per-tenant requires dynamic data source routing, complicates migrations, and does not work well with connection pooling. For a template, shared tables with `group_id` columns is the right level of isolation. | Use `group_id` column filtering (simplest multi-tenant pattern). Server middleware validates group membership before data access. Document how to upgrade to schema isolation for high-security use cases. |
| **Role inheritance / permission matrix** | Fine-grained permissions beyond Admin/User (e.g., `can_edit_members`, `can_view_analytics`). | Permission matrices add enormous complexity (N permissions x M roles x G groups). For a template, three fixed group roles (Owner/Admin/Member) are sufficient to demonstrate the pattern. Adding a dynamic permission system makes the template harder to understand. | Fixed `GroupRole` enum: Owner > Admin > Member. Check `withGroupRole(GroupRole.Admin)`. Document how to extend to a permission matrix if needed. |

---

## Feature Dependencies

```
[MVI Base Class]
    |
    +--enables--> [MVI Test DSL]
    |                 |
    |                 +--requires--> [Turbine] (new dependency)
    |                 +--requires--> [kotlinx-coroutines-test] (new dependency)
    |                 +--requires--> [kotest-assertions] (already in catalog)
    |
    +--enables--> [Migrate Existing ViewModels to MVI]
    |                 |
    |                 +--requires--> [Existing AuthApi, UserApi] (already built)
    |                 +--requires--> [Existing State classes] (already built)
    |                 +--enables--> [ViewModel Unit Tests]
    |
    +--enables--> [Group Management ViewModels]

[Groups Entity (server)]
    |
    +--requires--> [UsersTable, RolesTable] (already built)
    +--requires--> [UserRole sealed class] (already built)
    +--requires--> [withRole() RBAC middleware] (already built)
    |
    +--enables--> [Group API Routes]
    |                 +--enables--> [GroupApi SDK class]
    |                                   +--enables--> [Group Management ViewModels]
    |                                                     +--enables--> [Admin Dashboard Screen]
    |
    +--enables--> [Group-scoped content]
    +--enables--> [Admin invitation flow]
    +--enables--> [Server integration tests for groups]

[String Resources (composeResources)]
    |
    +--independent (can start anytime)
    +--enables--> [Shared StringKey enum]
    |                 +--enables--> [ViewModel localized errors]
    |                 +--enables--> [Server error code mapping]
    |
    +--enables--> [Locale-qualified resources]

[Testing Infrastructure]
    |
    +--requires--> [MVI Base Class + Test DSL] (for ViewModel tests)
    +--requires--> [Groups entity] (for integration test coverage)
    +--requires--> [SDK classes] (for MockEngine tests)
    |
    +--enables--> [Test fixtures module]
    +--enables--> [Server integration test harness]
    +--enables--> [SDK MockEngine tests]
    +--enables--> [ViewModel Turbine tests]
```

### Dependency Notes

- **MVI Base Class is the foundation:** Everything else in this milestone builds on it. Must be implemented first because ViewModel tests require it and the Group Management ViewModels will use it.
- **Groups depends on existing RBAC:** The `UserRole` sealed class, `RolesTable`, `withRole()` middleware, and `AuthService` are all prerequisites. All exist from Milestone 1.
- **Localization is independent:** String resources can be added at any time. However, the `StringKey` enum should exist before ViewModel migration so that migrated ViewModels emit keys instead of hardcoded strings.
- **Testing requires everything else to exist first:** You cannot write tests for features that do not exist. The test infrastructure phase should come last (or in parallel with the features it tests).
- **Server integration tests require Groups:** The interesting integration test scenarios involve multi-step flows (register -> create group -> invite member -> verify RBAC). Without Groups, server tests are limited to auth flow replay.

---

## MVP Definition

### Launch With (v2 -- this milestone)

- [ ] **MVI Base ViewModel class** -- the architectural foundation everything builds on
- [ ] **MVI Test DSL** -- inseparable from MVI (pattern without tests is incomplete)
- [ ] **Migrate all 5 existing ViewModels to MVI** -- template must not contradict itself
- [ ] **Group entity + CRUD API** -- create, list, get, update, delete groups
- [ ] **Group membership management** -- add/remove/change role of members
- [ ] **Admin registration into group** -- admin creates user accounts for their group
- [ ] **GroupApi SDK class** -- client-side access to group endpoints
- [ ] **Admin dashboard screen** -- admin sees group management, user sees their groups
- [ ] **composeResources/values/strings.xml** -- all hardcoded strings extracted
- [ ] **StringKey shared enum** -- ViewModel error messages use keys, not strings
- [ ] **Server error code mapping** -- server returns codes, client localizes
- [ ] **ViewModel unit tests** -- at least LoginViewModel and GroupManagementViewModel tested
- [ ] **Server integration tests** -- auth flow + group CRUD + RBAC enforcement tested
- [ ] **SDK MockEngine tests** -- apiCall mapping + AuthInterceptor tested

### Add After Validation (v2.x)

- [ ] **Second locale (Spanish)** -- proves the localization pattern works across languages
- [ ] **Pluralization support** -- `<plurals>` in strings.xml for member counts etc.
- [ ] **Group invitation flow with email** -- activation token via email for invited users
- [ ] **Group-scoped content filtering** -- dashboard data filtered by group membership
- [ ] **Test coverage reporting** -- Kover integrated with CI, coverage thresholds set

### Future Consideration (v3+)

- [ ] **Group-scoped RBAC middleware** -- `withGroupRole()` route extension
- [ ] **Permission matrix** -- dynamic permissions beyond fixed Owner/Admin/Member roles
- [ ] **Runtime locale switching** -- in-app language picker with persistence
- [ ] **Compose UI snapshot tests** -- visual regression once tooling matures
- [ ] **Test data seeding CLI** -- generate realistic test data for development

---

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| MVI Base ViewModel class | HIGH | MEDIUM | P1 |
| Migrate existing ViewModels to MVI | HIGH | HIGH | P1 |
| MVI Test DSL (Turbine) | HIGH | MEDIUM | P1 |
| Group entity + CRUD API (server) | HIGH | HIGH | P1 |
| Group membership management | HIGH | HIGH | P1 |
| Admin registers users into group | HIGH | MEDIUM | P1 |
| GroupApi SDK class | HIGH | LOW | P1 |
| Admin dashboard screen | HIGH | MEDIUM | P1 |
| Client string resources (strings.xml) | MEDIUM | MEDIUM | P1 |
| StringKey shared enum | MEDIUM | LOW | P1 |
| Server error code mapping | MEDIUM | LOW | P1 |
| ViewModel unit tests | HIGH | MEDIUM | P1 |
| Server integration tests | HIGH | HIGH | P1 |
| SDK MockEngine tests | MEDIUM | MEDIUM | P1 |
| Test fixtures module | MEDIUM | LOW | P2 |
| Second locale (Spanish) | LOW | LOW | P2 |
| Group invitation with email | MEDIUM | HIGH | P2 |
| Group-scoped content | MEDIUM | HIGH | P2 |
| Kover coverage reporting | LOW | LOW | P2 |
| Pluralization support | LOW | LOW | P3 |
| Group-scoped RBAC middleware | MEDIUM | MEDIUM | P3 |

**Priority key:**
- P1: Must have for this milestone
- P2: Should have, add if time permits
- P3: Nice to have, future milestone

---

## Detailed Feature Specifications

### A. MVI ViewModel Contract -- What "Done" Looks Like

**The four types per ViewModel:**

```kotlin
// 1. Intent -- what the user wants to do
sealed interface LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent
    data class PasswordChanged(val password: String) : LoginIntent
    data class RememberMeChanged(val checked: Boolean) : LoginIntent
    data object Submit : LoginIntent
}

// 2. Model -- the full screen state (immutable)
data class LoginModel(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: StringKey? = null,
    val passwordError: StringKey? = null,
    val serverError: String? = null,
)

// 3. Mutation -- how state changes
sealed interface LoginMutation {
    data class EmailUpdated(val email: String) : LoginMutation
    data class PasswordUpdated(val password: String) : LoginMutation
    data class RememberMeUpdated(val checked: Boolean) : LoginMutation
    data object Loading : LoginMutation
    data class ValidationFailed(val emailError: StringKey?, val passwordError: StringKey?) : LoginMutation
    data class ServerFailed(val message: String) : LoginMutation
}

// 4. Event -- one-shot side effects
sealed interface LoginEvent {
    data object NavigateToHome : LoginEvent
}
```

**The reduce function is pure:**

```kotlin
override fun reduce(model: LoginModel, mutation: LoginMutation): LoginModel = when (mutation) {
    is LoginMutation.EmailUpdated -> model.copy(email = mutation.email, emailError = null)
    is LoginMutation.PasswordUpdated -> model.copy(password = mutation.password, passwordError = null)
    is LoginMutation.RememberMeUpdated -> model.copy(rememberMe = mutation.checked)
    is LoginMutation.Loading -> model.copy(isLoading = true, serverError = null)
    is LoginMutation.ValidationFailed -> model.copy(emailError = mutation.emailError, passwordError = mutation.passwordError)
    is LoginMutation.ServerFailed -> model.copy(serverError = mutation.message, isLoading = false)
}
```

**handleIntent produces a Flow of Either<Event, Mutation>:**

```kotlin
override fun handleIntent(intent: LoginIntent): Flow<Either<LoginEvent, LoginMutation>> = flow {
    when (intent) {
        is LoginIntent.EmailChanged -> emit(LoginMutation.EmailUpdated(intent.email).right())
        is LoginIntent.PasswordChanged -> emit(LoginMutation.PasswordUpdated(intent.password).right())
        is LoginIntent.RememberMeChanged -> emit(LoginMutation.RememberMeUpdated(intent.checked).right())
        is LoginIntent.Submit -> {
            // validate, then call API
            emit(LoginMutation.Loading.right())
            authApi.login(...)
                .fold(
                    ifLeft = { emit(LoginMutation.ServerFailed(it.message).right()) },
                    ifRight = { emit(LoginEvent.NavigateToHome.left()) }
                )
        }
    }
}
```

### B. MVI Test DSL -- What "Done" Looks Like

```kotlin
@Test
fun `login success navigates to home`() = runTest {
    val fakeAuthApi = FakeAuthApi(loginResult = AuthResponse(...).right())
    val vm = LoginViewModel(fakeAuthApi)

    vm.model.test {
        // Initial state
        awaitItem() shouldBe LoginModel()

        // Type email
        vm.take(LoginIntent.EmailChanged("user@test.com"))
        awaitItem().email shouldBe "user@test.com"

        // Type password
        vm.take(LoginIntent.PasswordChanged("password123"))
        awaitItem().password shouldBe "password123"

        // Submit
        vm.take(LoginIntent.Submit)
        awaitItem().isLoading shouldBe true
    }

    vm.events.test {
        vm.take(LoginIntent.Submit)
        awaitItem() shouldBe LoginEvent.NavigateToHome
    }
}
```

### C. Groups Server Schema

```sql
-- New tables
CREATE TABLE groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE group_memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',  -- OWNER, ADMIN, MEMBER
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(group_id, user_id)
);
```

### D. Localization Resource Structure

```
core/localization/src/commonMain/kotlin/
    com/m2f/template/localization/
        StringKey.kt          -- shared enum of all string keys

composeApp/src/commonMain/composeResources/
    values/
        strings.xml           -- English (default)
    values-es/
        strings.xml           -- Spanish (example)
```

**StringKey enum:**

```kotlin
enum class StringKey {
    // Auth
    EMAIL_REQUIRED,
    EMAIL_INVALID,
    PASSWORD_REQUIRED,
    PASSWORD_TOO_SHORT,
    LOGIN_BUTTON,
    REGISTER_BUTTON,
    // Groups
    GROUP_NAME_REQUIRED,
    GROUP_CREATED,
    MEMBER_ADDED,
    // etc.
}
```

---

## Sources

- [Turbine GitHub](https://github.com/cashapp/turbine) -- Flow testing library, v1.2.1 stable (HIGH confidence, official repo)
- [Testing Android Flows with Turbine](https://proandroiddev.com/testing-android-flows-in-viewmodel-with-turbine-ea9bae7e811a) -- ViewModel + Turbine patterns (MEDIUM confidence, community)
- [Ktor Server Testing Documentation](https://ktor.io/docs/server-testing.html) -- testApplication API (HIGH confidence, official docs)
- [Ktor Client Testing Documentation](https://ktor.io/docs/client-testing.html) -- MockEngine for SDK tests (HIGH confidence, official docs)
- [Compose Multiplatform Localization](https://kotlinlang.org/docs/multiplatform/compose-localize-strings.html) -- composeResources/values/ pattern (HIGH confidence, official docs)
- [Compose Multiplatform Resources Setup](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-setup.html) -- resource directory structure (HIGH confidence, official docs)
- [MVI Architecture in Android](https://www.droidcon.com/2025/04/29/reactive-state-management-in-compose-mvi-architecture/) -- MVI patterns 2025 (MEDIUM confidence, community)
- [Multi-tenant SaaS Application Guide](https://blog.logto.io/build-multi-tenant-saas-application) -- group/tenant management patterns (MEDIUM confidence, community)
- [Kotlin Coroutines Test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/) -- runTest, TestDispatcher API (HIGH confidence, official docs)
- [KMP Testing Guide 2025](https://www.kmpship.app/blog/kotlin-multiplatform-testing-guide-2025) -- KMP testing strategy overview (MEDIUM confidence, commercial site)
- [Kotest Ktor Extension](https://kotest.io/docs/extensions/ktor.html) -- Kotest + Ktor integration (HIGH confidence, official docs)

---
*Feature research for: KMP Full-Stack Template -- Milestone 2*
*Researched: 2026-02-17*
