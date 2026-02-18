# Phase 12: ViewModel Migration - Research

**Researched:** 2026-02-18
**Domain:** MVI ViewModel pattern migration (Kotlin Multiplatform, Compose)
**Confidence:** HIGH

## Summary

Phase 12 is a pure mechanical migration of 5 existing ViewModels from ad-hoc `ViewModel` + `MutableStateFlow` patterns to the `MviViewModel<Intent, Model, Mutation, Event>` base class established in Phase 10. The migration involves three coordinated changes per ViewModel: (1) rewrite the ViewModel class to extend MviViewModel with typed Intent/Model/Mutation/Event sealed hierarchies, (2) update the Composable layer in `AppNavHost.kt` to consume `model`/`event` instead of `state` with boolean flag observation, and (3) write unit tests using the `MviViewModel.test {}` DSL from core:testing. A sixth cleanup plan removes dead code after all 5 migrations complete.

The codebase is well-prepared for this migration. The MviViewModel base class, test DSL (Turbine-based), ViewModelTest base class, and fakeSdk builder infrastructure are all in place from Phases 10-11.1. The mvi-viewmodel skill documents the exact TDD workflow and patterns to follow. The primary complexity lies in correctly mapping each ViewModel's existing behavior (validation, API calls, navigation flags) into the Intent/Mutation/Event boundaries dictated by user decisions.

**Primary recommendation:** Follow the mvi-viewmodel skill's TDD workflow for each ViewModel migration. LoginViewModel is the reference implementation; complete it first, then apply the identical pattern to the remaining 4 VMs.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### Effect vs State boundary
- Navigation is always a one-shot Effect (NavigateToDashboard, NavigateToLogin, etc.) -- never state
- Login/auth success fires NavigateToDashboard immediately -- no success state shown first
- Logout fires NavigateToLogin as a one-shot Effect -- dashboard has no "logged out" state
- Validation and server errors are persistent Model state (shown inline, survive recomposition) -- not toasts/effects
- Loading indicators use `isLoading` boolean in Model state -- not sealed state variants

#### Migration purity
- Pure mechanical migration -- same behavior, new MVI pattern. No UX improvements during migration
- Both ViewModel and Composable sides change: ViewModel emits Effects, Composable collects via LaunchedEffect
- Old ViewModel base classes and legacy patterns removed after all 5 VMs are migrated (separate cleanup plan)
- Intent types use descriptive action-based names: SubmitLoginClicked, EmailChanged, ForgotPasswordRequested (reads like UI event log)

#### Test depth
- Each VM gets happy path + key error scenario tests (not just happy path, not full comprehensive)
- Tests verify both Model state transitions AND Effect emissions (navigation, etc.)
- All 5 VM tests use the same fakeSdk {} + Turbine-based test DSL from core:testing -- consistent pattern
- Existing LoginViewModel test from Phase 11.1 is rewritten to match the new depth standard

#### Execution order
- LoginViewModel is the reference migration (already partially migrated in Phase 11.1) -- complete it first
- Order after Login: Register -> ForgotPassword -> Profile -> Dashboard (auth screens first, then others)
- One plan per ViewModel (5 plans) -- each migrates + tests one ViewModel
- Plan 6: Final cleanup -- remove old ViewModel patterns, unused state classes, dead code

### Claude's Discretion
- Exact Intent/Model/Event sealed class structure per ViewModel
- How to wire Koin injection for migrated ViewModels
- Composable-side implementation details for LaunchedEffect collection
- Which error scenarios to test per ViewModel

### Deferred Ideas (OUT OF SCOPE)
None -- discussion stayed within phase scope
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| MVI-05 | All 5 existing ViewModels are migrated to the MVI pattern | Full codebase audit of all 5 VMs completed. MviViewModel base class, test DSL, fakeSdk builder, and mvi-viewmodel skill all in place. Each VM's current behavior mapped to Intent/Model/Mutation/Event boundaries. Build dependency gaps identified (core:mvi, core:testing). |
</phase_requirements>

## Standard Stack

### Core
| Library | Purpose | Why Standard |
|---------|---------|--------------|
| MviViewModel (core:mvi) | Base ViewModel class with Intent/Model/Mutation/Event pipeline | Phase 10 deliverable; the entire point of this migration |
| core:testing DSL | ViewModelTest base, MviViewModel.test{}, fakeSdk{} | Phase 11 deliverable; standardized test infrastructure |
| Arrow Core (Either) | API result handling in take() and fold() | Already used by all ViewModels and SDK APIs |
| Turbine | Flow testing inside MviViewModel.test{} DSL | Already integrated into core:testing |
| Kotest Assertions | shouldBe matchers in test DSL | Already integrated into core:testing |

### Supporting
| Library | Purpose | When to Use |
|---------|---------|-------------|
| Koin (viewModelOf) | DI registration for ViewModels | AppModule.kt -- already wired for all 5 VMs |
| kotlinx.coroutines.test | runTest, advanceUntilIdle, UnconfinedTestDispatcher | Handled internally by the test DSL |

### Alternatives Considered
None. All tooling decisions are locked by Phases 10-11.

## Architecture Patterns

### Current ViewModel Structure (BEFORE migration)
All 5 existing ViewModels follow the same ad-hoc pattern:
```
app/<feature>/
  <Feature>ViewModel.kt    -- extends ViewModel(), uses MutableStateFlow
  <Feature>State.kt         -- data class with boolean flags (loginSuccess, logoutTriggered, etc.)
  <Feature>Screen.kt        -- Composable, receives State + callbacks
```

### Target MVI Structure (AFTER migration)
Per mvi-viewmodel skill:
```
app/<feature>/
  <Feature>ViewModel.kt    -- extends MviViewModel<Intent, Model, Mutation, Event>
  <Feature>Intent.kt        -- sealed interface for user actions
  <Feature>Model.kt         -- data class for UI state (replaces <Feature>State)
  <Feature>Mutation.kt      -- sealed interface for state changes
  <Feature>Event.kt         -- sealed interface for one-shot effects (navigation)
  <Feature>Screen.kt        -- Composable, receives Model + (Intent) -> Unit
  <Feature>ViewModelTest.kt -- in commonTest, extends ViewModelTest()
```

### Pattern 1: ViewModel Migration Template
**What:** Convert ad-hoc ViewModel to MviViewModel with 4 type parameters
**When to use:** Every ViewModel in this phase

Before:
```kotlin
class LoginViewModel(private val sdk: Sdk) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun login() {
        // validation...
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            sdk.login(...).fold(
                ifLeft = { _state.update { it.copy(serverError = error.message, isLoading = false) } },
                ifRight = { _state.update { it.copy(loginSuccess = true, isLoading = false) } }
            )
        }
    }
}
```

After:
```kotlin
class LoginViewModel(private val sdk: Sdk) : MviViewModel<LoginIntent, LoginModel, LoginMutation, LoginEvent>(
    initialState = LoginModel()
) {
    override fun take(intent: LoginIntent) {
        viewModelScope.launch {
            when (intent) {
                is LoginIntent.EmailChanged -> sendMutation(LoginMutation.SetEmail(intent.email))
                is LoginIntent.SubmitLoginClicked -> handleLogin()
                // ...
            }
        }
    }

    private suspend fun handleLogin() {
        val current = model.value
        // validation -> sendMutation for errors
        sendMutation(LoginMutation.SetLoading(true))
        sdk.login(...).fold(
            ifLeft = { sendMutation(LoginMutation.SetServerError(it.message)) },
            ifRight = { sendEvent(LoginEvent.NavigateToDashboard) }  // Effect, not state!
        )
    }

    override suspend fun reduce(model: LoginModel, mutation: LoginMutation): LoginModel =
        when (mutation) {
            is LoginMutation.SetEmail -> model.copy(email = mutation.email, emailError = null)
            is LoginMutation.SetLoading -> model.copy(isLoading = mutation.loading)
            // ...
        }
}
```

### Pattern 2: Composable-side Effect Collection
**What:** Replace boolean flag observation with Channel-based event collection
**When to use:** Every Composable that navigates on success/logout

Before (in AppNavHost.kt):
```kotlin
composable<LoginRoute> {
    val viewModel = koinViewModel<LoginViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginScreen(state = state, onLoginClick = viewModel::login, ...)
    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) {
            navController.navigate(DashboardRoute) { popUpTo<LoginRoute> { inclusive = true } }
        }
    }
}
```

After (in AppNavHost.kt):
```kotlin
composable<LoginRoute> {
    val viewModel = koinViewModel<LoginViewModel>()
    val model by viewModel.model.collectAsStateWithLifecycle()
    LoginScreen(model = model, onIntent = viewModel::take, ...)
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is LoginEvent.NavigateToDashboard -> {
                    navController.navigate(DashboardRoute) { popUpTo<LoginRoute> { inclusive = true } }
                }
            }
        }
    }
}
```

### Pattern 3: Test DSL Usage
**What:** Using MviViewModel.test{} for state + event assertions
**When to use:** Every ViewModel test in this phase

```kotlin
class LoginViewModelTest : ViewModelTest() {
    @Test
    fun `successful login emits NavigateToDashboard event`() {
        val sdk = fakeSdk {
            auth { login { _, _ -> Either.Right(AuthResponse(...)) } }
        }
        val viewModel = LoginViewModel(sdk)
        viewModel.test {
            intent(LoginIntent.EmailChanged("user@test.com"))
            model(LoginModel(email = "user@test.com"))
            intent(LoginIntent.PasswordChanged("password123"))
            model(LoginModel(email = "user@test.com", password = "password123"))
            intent(LoginIntent.SubmitLoginClicked)
            model(LoginModel(email = "user@test.com", password = "password123", isLoading = true))
            event(LoginEvent.NavigateToDashboard)
            // Model after event -- loading cleared
            model(LoginModel(email = "user@test.com", password = "password123", isLoading = false))
        }
    }
}
```

### Anti-Patterns to Avoid
- **Boolean navigation flags in Model:** `loginSuccess`, `registerSuccess`, `logoutTriggered`, `emailSent` -- these MUST become Events, not Model fields. User decision is explicit: navigation is always a one-shot Effect.
- **Using sub-APIs directly:** ViewModels take `Sdk` (facade), not `AuthApi` or `UserApi`. RegisterViewModel and ForgotPasswordViewModel currently take `AuthApi` directly -- they must be changed to take `Sdk`.
- **Using fakeAuthApi/fakeUserApi directly in tests:** Always use `fakeSdk {}` as the entry point.
- **Stateful reduce:** `reduce()` must be pure -- no API calls, no side effects.
- **Multiple state emissions for one logical operation:** e.g., clearing `loginSuccess` flag after navigation -- not needed with Events.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| ViewModel base class | Custom MutableStateFlow patterns | MviViewModel from core:mvi | Already built, tested, and documented |
| Test infrastructure | Manual runTest + Turbine boilerplate | MviViewModel.test{} DSL from core:testing | Handles dispatcher setup, initial emission skip, advanceUntilIdle |
| Fake API creation | Manual mock objects per test | fakeSdk {} builder from core:testing | Consistent DSL, fail-fast defaults, composable |
| Dispatcher management in tests | Manual Dispatchers.setMain/resetMain | ViewModelTest base class | BeforeTest/AfterTest handled automatically |

**Key insight:** All infrastructure is built. This phase is purely about applying established patterns to existing code.

## Common Pitfalls

### Pitfall 1: Missing core:mvi and core:testing Dependencies
**What goes wrong:** Build fails because app modules don't depend on the MVI base class or testing infrastructure.
**Why it happens:** Currently, `app/auth` has `core.testing` in commonTest but NOT `core.mvi` in commonMain. `app/profile` and `app/dashboard` have NEITHER dependency.
**How to avoid:**
- `app/auth/build.gradle.kts`: Add `implementation(projects.core.mvi)` to commonMain
- `app/profile/build.gradle.kts`: Add `implementation(projects.core.mvi)` to commonMain, add `commonTest.dependencies { implementation(projects.core.testing) }`
- `app/dashboard/build.gradle.kts`: Add `implementation(projects.core.mvi)` to commonMain, add `commonTest.dependencies { implementation(projects.core.testing) }`, add `implementation(libs.kotlinx.coroutines)` to commonMain if missing
**Warning signs:** Unresolved reference errors for MviViewModel, ViewModelTest, fakeSdk, test{}

### Pitfall 2: RegisterViewModel and ForgotPasswordViewModel Take AuthApi, Not Sdk
**What goes wrong:** Current VMs take `AuthApi` directly. The mvi-viewmodel skill mandates `Sdk` as the ViewModel dependency.
**Why it happens:** These VMs were built before the Sdk facade convention was established.
**How to avoid:** Change constructor from `authApi: AuthApi` to `sdk: Sdk`. Update all internal API calls from `authApi.register(...)` to `sdk.register(...)` (Sdk delegates to AuthApi via Kotlin delegation). No AppModule.kt change needed -- Koin auto-resolves `Sdk`.
**Warning signs:** Koin resolution failure at runtime if parameter type doesn't match DI graph.

### Pitfall 3: ProfileViewModel Takes Two Dependencies (UserApi + AuthApi)
**What goes wrong:** ProfileViewModel currently takes `userApi: UserApi` and `authApi: AuthApi` separately.
**Why it happens:** It needs both user profile operations and logout.
**How to avoid:** Change to single `sdk: Sdk` dependency. Since `Sdk` delegates both `AuthApi` and `UserApi`, all calls work: `sdk.getProfile()`, `sdk.updateProfile(...)`, `sdk.logout()`.
**Warning signs:** Compilation error if APIs are called on the wrong type.

### Pitfall 4: DashboardViewModel Has No API Dependency
**What goes wrong:** DashboardViewModel currently takes no constructor parameters and uses a static `delay(300)` mock.
**Why it happens:** Dashboard was built as a static mock screen.
**How to avoid:** Per the mvi-viewmodel skill, ViewModel takes `Sdk`. Even if Dashboard doesn't currently call any SDK methods, it should take `Sdk` for consistency. The `delay(300)` loading simulation moves into `take()` as a `LoadDashboard` intent handled in `init { take(DashboardIntent.LoadDashboard) }`. This is a pure mechanical migration -- no new API calls, just restructuring the mock loading into the MVI flow.
**Warning signs:** DashboardViewModel constructor signature mismatch with `viewModelOf(::DashboardViewModel)` if it gains a parameter but Koin registration doesn't provide it.

### Pitfall 5: Event Ordering After Mutation
**What goes wrong:** When login succeeds, the ViewModel should emit both a mutation (clear loading) and an event (NavigateToDashboard). The order in the pipeline matters for test assertions.
**Why it happens:** `sendMutation()` and `sendEvent()` both emit to the same `MutableSharedFlow<Either<Event, Mutation>>`. The test DSL processes model and event assertions in declared order.
**How to avoid:** Decide and document a consistent pattern. Recommendation: for login success, emit `sendEvent(NavigateToDashboard)` THEN `sendMutation(SetLoading(false))`. The Composable navigates away immediately on the event, so the loading=false mutation is consumed but the user never sees it. In tests, assert `event(...)` before the final `model(...)`. However, note that per user decision "Login/auth success fires NavigateToDashboard immediately -- no success state shown first", so the simplest approach is: on success, ONLY emit the event. The Composable navigates away; there's no need to update Model state after a navigation event.
**Warning signs:** Turbine timeout waiting for a model emission that was never sent.

### Pitfall 6: ForgotPassword emailSent Is NOT Navigation
**What goes wrong:** Treating `emailSent` as a navigation event when it's actually a UI state change.
**Why it happens:** It seems like a "success" pattern similar to loginSuccess, but the ForgotPassword screen stays visible and shows a success message.
**How to avoid:** `emailSent` stays as a Model field (not an Event). The ForgotPasswordScreen already conditionally renders a success alert based on this boolean. The only ForgotPassword navigation is "back to login" which is handled by the Composable's `onBackToLogin` callback (no ViewModel involvement).
**Warning signs:** ForgotPasswordScreen breaking if emailSent is removed from Model.

### Pitfall 7: Profile saveSuccess Is Model State, Not an Event
**What goes wrong:** Treating `saveSuccess` as a navigation event.
**Why it happens:** Similar pattern to loginSuccess flag.
**How to avoid:** `saveSuccess` stays as Model state. The ProfileScreen shows a success alert when `saveSuccess = true`. It does NOT navigate away. Only `logoutTriggered` becomes an Event (NavigateToLogin).
**Warning signs:** Profile save alert disappearing if saveSuccess is wrongly converted to an Event.

## Code Examples

### Per-ViewModel Migration Mapping

#### 1. LoginViewModel

**Current dependencies:** `sdk: Sdk` (already correct)

**Current boolean flags to convert:**
- `loginSuccess: Boolean` -> `LoginEvent.NavigateToDashboard` (Event)

**Recommended Intent sealed interface:**
```kotlin
sealed interface LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent
    data class PasswordChanged(val password: String) : LoginIntent
    data class RememberMeChanged(val checked: Boolean) : LoginIntent
    data object SubmitLoginClicked : LoginIntent
}
```

**Recommended Model (replaces LoginState):**
```kotlin
data class LoginModel(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val serverError: String? = null,
)
// Note: loginSuccess removed -- replaced by Event
```

**Recommended Mutation sealed interface:**
```kotlin
sealed interface LoginMutation {
    data class SetEmail(val email: String) : LoginMutation
    data class SetPassword(val password: String) : LoginMutation
    data class SetRememberMe(val checked: Boolean) : LoginMutation
    data class SetLoading(val loading: Boolean) : LoginMutation
    data class SetValidationErrors(val emailError: String?, val passwordError: String?) : LoginMutation
    data class SetServerError(val error: String?) : LoginMutation
}
```

**Recommended Event sealed interface:**
```kotlin
sealed interface LoginEvent {
    data object NavigateToDashboard : LoginEvent
}
```

**Test scenarios (happy + key error):**
1. Happy: Valid email + password -> loading state -> NavigateToDashboard event
2. Error: Blank email -> validation error in model (no API call)
3. Error: Server returns InvalidCredentials -> server error in model

**Composable changes in AppNavHost.kt:**
- `viewModel.state` -> `viewModel.model`
- `onEmailChange = viewModel::onEmailChange` -> `onEmailChange = { viewModel.take(LoginIntent.EmailChanged(it)) }`
- Remove `LaunchedEffect(state.loginSuccess)` block
- Add `LaunchedEffect(Unit) { viewModel.event.collect { when(it) { ... } } }`

---

#### 2. RegisterViewModel

**Current dependencies:** `authApi: AuthApi` -> MUST change to `sdk: Sdk`

**Current boolean flags to convert:**
- `registerSuccess: Boolean` -> `RegisterEvent.NavigateToDashboard` (Event)

**Recommended Intent sealed interface:**
```kotlin
sealed interface RegisterIntent {
    data class FirstNameChanged(val firstName: String) : RegisterIntent
    data class LastNameChanged(val lastName: String) : RegisterIntent
    data class EmailChanged(val email: String) : RegisterIntent
    data class PasswordChanged(val password: String) : RegisterIntent
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterIntent
    data class TermsAcceptedChanged(val accepted: Boolean) : RegisterIntent
    data object SubmitRegisterClicked : RegisterIntent
}
```

**Recommended Model (replaces RegisterState):**
```kotlin
data class RegisterModel(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val serverError: String? = null,
)
// Note: registerSuccess removed -- replaced by Event
```

**Recommended Event sealed interface:**
```kotlin
sealed interface RegisterEvent {
    data object NavigateToDashboard : RegisterEvent
}
```

**Test scenarios:**
1. Happy: All fields valid -> loading -> NavigateToDashboard event
2. Error: Validation failures (blank fields, passwords don't match, terms not accepted) -> fieldErrors in model
3. Error: Server returns UserAlreadyExists -> serverError in model

**Key complexity:** RegisterViewModel uses Arrow `either { zipOrAccumulate(...) }` for accumulated validation. This pattern moves into the `take()` handler -- validation logic stays the same, just wrapped in MVI dispatch.

---

#### 3. ForgotPasswordViewModel

**Current dependencies:** `authApi: AuthApi` -> MUST change to `sdk: Sdk`

**Current boolean flags to convert:**
- `emailSent: Boolean` -> STAYS as Model state (NOT an Event -- user stays on screen seeing success message)

**Recommended Intent sealed interface:**
```kotlin
sealed interface ForgotPasswordIntent {
    data class EmailChanged(val email: String) : ForgotPasswordIntent
    data object SubmitForgotPasswordClicked : ForgotPasswordIntent
}
```

**Recommended Model (replaces ForgotPasswordState):**
```kotlin
data class ForgotPasswordModel(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailSent: Boolean = false,  // stays in Model
    val emailError: String? = null,
    val serverError: String? = null,
)
```

**Recommended Event sealed interface:**
```kotlin
sealed interface ForgotPasswordEvent
// Empty for now -- no navigation events. ForgotPassword has no ViewModel-driven navigation.
// "Back to login" is handled by Composable callback directly.
```

Note: Even though ForgotPasswordEvent is empty, the 4 type parameters are still required by MviViewModel. An empty sealed interface is valid Kotlin.

**Test scenarios:**
1. Happy: Valid email -> loading -> emailSent=true in model
2. Error: Blank email -> emailError in model

---

#### 4. ProfileViewModel

**Current dependencies:** `userApi: UserApi, authApi: AuthApi` -> MUST change to single `sdk: Sdk`

**Current boolean flags to convert:**
- `logoutTriggered: Boolean` -> `ProfileEvent.NavigateToLogin` (Event)
- `saveSuccess: Boolean` -> STAYS as Model state (success alert shown on screen)

**Recommended Intent sealed interface:**
```kotlin
sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data object StartEditing : ProfileIntent
    data object CancelEditing : ProfileIntent
    data class EditNameChanged(val name: String) : ProfileIntent
    data class EditEmailChanged(val email: String) : ProfileIntent
    data object SaveProfileClicked : ProfileIntent
    data object LogoutClicked : ProfileIntent
}
```

**Recommended Model (replaces ProfileState):**
```kotlin
data class ProfileModel(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val tier: UserTier = UserTier.Free,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editEmail: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val serverError: String? = null,
    val saveSuccess: Boolean = false,  // stays in Model
)
// Note: logoutTriggered removed -- replaced by Event
```

**Recommended Event sealed interface:**
```kotlin
sealed interface ProfileEvent {
    data object NavigateToLogin : ProfileEvent
}
```

**Key complexity:** ProfileViewModel has an `init { loadProfile() }` block. In MVI, this becomes `init { take(ProfileIntent.LoadProfile) }` so the load happens via the intent pipeline.

**Test scenarios:**
1. Happy: Load profile -> loading -> user data populated in model
2. Happy: Save profile -> success alert in model
3. Error: Load profile fails -> serverError in model
4. Event: Logout -> NavigateToLogin event

---

#### 5. DashboardViewModel

**Current dependencies:** None (no-arg constructor) -> change to `sdk: Sdk` for consistency

**Current boolean flags to convert:** None -- Dashboard has no navigation flags driven by ViewModel

**Recommended Intent sealed interface:**
```kotlin
sealed interface DashboardIntent {
    data object LoadDashboard : DashboardIntent
    data class NavItemSelected(val item: String) : DashboardIntent
}
```

**Recommended Model (replaces DashboardState):**
```kotlin
data class DashboardModel(
    val metrics: List<DashboardMockData.MetricItem> = DashboardMockData.metrics,
    val processes: List<DashboardMockData.ProcessItem> = DashboardMockData.processes,
    val activities: List<DashboardMockData.ActivityItem> = DashboardMockData.activities,
    val deployment: DashboardMockData.DeploymentStatus = DashboardMockData.deployment,
    val isLoading: Boolean = false,
    val userName: String = "user@terminal.dev",
    val selectedNavItem: String = "dashboard",
)
```

**Recommended Event sealed interface:**
```kotlin
sealed interface DashboardEvent
// Empty -- Dashboard has no ViewModel-driven navigation.
// "Logout" and "Profile" navigation are handled by Composable callbacks.
```

**Key complexity:** Dashboard's `delay(300)` loading mock. In MVI, the `init` block dispatches `DashboardIntent.LoadDashboard`, which sends `SetLoading(true)`, delays, then sends `SetLoading(false)`. The mock data is already in the initial state defaults.

**Test scenarios:**
1. Happy: LoadDashboard -> loading true -> loading false (with delay, needs test scheduler control)
2. NavItemSelected -> selectedNavItem updated in model

**Dashboard logout:** Currently handled in AppNavHost.kt composable with `onLogout = { navController.navigate(LoginRoute) { ... } }`. This stays in the Composable -- DashboardViewModel has no logout logic.

---

### AppNavHost.kt Composable Changes Summary

Each route in `AppNavHost.kt` needs these changes:
1. `viewModel.state` -> `viewModel.model` (StateFlow property name changed)
2. Direct method references (`viewModel::onEmailChange`) -> intent dispatch lambdas (`{ viewModel.take(Intent.EmailChanged(it)) }`)
3. Boolean flag LaunchedEffect blocks (`LaunchedEffect(state.loginSuccess)`) -> Event collection (`LaunchedEffect(Unit) { viewModel.event.collect { ... } }`)

Routes with boolean flag navigation that must change:
- **LoginRoute:** Remove `LaunchedEffect(state.loginSuccess)`, add event collection for `LoginEvent.NavigateToDashboard`
- **RegisterRoute:** Remove `LaunchedEffect(state.registerSuccess)`, add event collection for `RegisterEvent.NavigateToDashboard`
- **ProfileRoute:** Remove `LaunchedEffect(state.logoutTriggered)`, add event collection for `ProfileEvent.NavigateToLogin`

Routes with no boolean flag navigation (simpler changes):
- **ForgotPasswordRoute:** Only state -> model rename and method -> intent changes
- **DashboardRoute:** Only state -> model rename and method -> intent changes. Logout navigation stays in Composable.

### Build Configuration Changes

**app/auth/build.gradle.kts:**
```kotlin
commonMain.dependencies {
    // ADD:
    implementation(projects.core.mvi)
    // existing deps unchanged
}
// commonTest already has core.testing
```

**app/profile/build.gradle.kts:**
```kotlin
commonMain.dependencies {
    // ADD:
    implementation(projects.core.mvi)
    implementation(libs.kotlinx.coroutines)  // may already be transitive, add explicitly
    // existing deps unchanged
}
// ADD:
commonTest.dependencies {
    implementation(projects.core.testing)
}
```

**app/dashboard/build.gradle.kts:**
```kotlin
commonMain.dependencies {
    // ADD:
    implementation(projects.core.mvi)
    implementation(libs.kotlinx.coroutines)  // for delay() and viewModelScope
    // existing deps unchanged
}
// ADD:
commonTest.dependencies {
    implementation(projects.core.testing)
}
```

### Koin DI Wiring

**No AppModule.kt changes needed for Plans 1-5.** All 5 ViewModels are already registered with `viewModelOf(::ViewModel)`. Since all VMs will take `Sdk` (which is already in the DI graph from sdkModule), Koin auto-resolves. The only constructor change that matters is:
- RegisterViewModel: `AuthApi` -> `Sdk`
- ForgotPasswordViewModel: `AuthApi` -> `Sdk`
- ProfileViewModel: `UserApi, AuthApi` -> `Sdk`
- DashboardViewModel: `()` -> `Sdk`
- LoginViewModel: already `Sdk` (no change)

All resolve via `single { Sdk(authApi = get(), userApi = get()) }` in sdkModule.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| MutableStateFlow + boolean flags | MviViewModel with typed Events | Phase 10-11 | All VMs must migrate |
| Direct method calls from Composable | Intent dispatch via take() | Phase 10 | Composable API changes |
| LaunchedEffect(booleanFlag) | LaunchedEffect(Unit) + event.collect | Phase 10 | One-shot effects work correctly |
| Per-VM testing patterns | Standardized test DSL | Phase 11 | Consistent test structure |

**Deprecated/outdated after this phase:**
- `LoginState`, `RegisterState`, `ForgotPasswordState`, `ProfileState`, `DashboardState` data classes -- replaced by Model classes
- Boolean navigation flags (`loginSuccess`, `registerSuccess`, `logoutTriggered`)
- Direct method call pattern on ViewModels (onEmailChange, login, register)

## Open Questions

1. **LoginScreen Composable API: callback-per-action vs single onIntent**
   - What we know: Current LoginScreen takes many callbacks (`onEmailChange`, `onPasswordChange`, `onLoginClick`, etc.). The MVI pattern suggests a single `onIntent: (Intent) -> Unit` parameter.
   - What's unclear: Whether to change the Screen composable signature to take a single `onIntent` or keep individual callbacks (mapped to intents at the AppNavHost level).
   - Recommendation: Keep individual callbacks on Screen composables (they're presentation-level concerns). Map to intents at the AppNavHost level where the ViewModel is accessed. This minimizes Screen composable changes and keeps Screens ViewModel-agnostic. This is consistent with the existing pattern where Screens are pure presentation.

2. **DashboardViewModel Sdk dependency -- artificial?**
   - What we know: DashboardViewModel currently has no API calls. The mvi-viewmodel skill mandates `Sdk` as the dependency.
   - What's unclear: Whether adding an unused `Sdk` parameter is pragmatic or wasteful.
   - Recommendation: Add `sdk: Sdk` to DashboardViewModel constructor per skill convention. This keeps the pattern consistent and prepares for future Dashboard API calls. The parameter is simply unused initially.

3. **State class file naming: Model vs State**
   - What we know: mvi-viewmodel skill calls it `<Feature>Model.kt`. Existing files are `<Feature>State.kt`.
   - What's unclear: Whether to rename files and classes, or keep the `State` naming.
   - Recommendation: Follow the skill convention: rename to `<Feature>Model`. Plan 6 (cleanup) removes the old `<Feature>State` files. During each VM's plan, create `<Feature>Model.kt` as a new file, update all references, then delete `<Feature>State.kt`.

## Sources

### Primary (HIGH confidence)
- **Codebase audit** -- All 5 ViewModels, state classes, Screen composables, AppNavHost, DI modules, build configs read directly
- **MviViewModel base class** -- `core/mvi/src/commonMain/.../MviViewModel.kt`
- **Test DSL** -- `core/testing/src/commonMain/.../MviViewModelTestDsl.kt`, `ViewModelTestContext.kt`, `Statement.kt`, `ViewModelTest.kt`
- **Fake builders** -- `core/testing/src/commonMain/.../fakes/FakeSdkBuilder.kt`, `FakeAuthApiBuilder.kt`, `FakeUserApiBuilder.kt`
- **mvi-viewmodel skill** -- `.claude/skills/mvi-viewmodel/SKILL.md` and `references/source-patterns.md`
- **SdkModule DI** -- `core/sdk/src/commonMain/.../di/SdkModule.kt`

### Secondary (MEDIUM confidence)
- None needed. All patterns are established in the codebase.

### Tertiary (LOW confidence)
- None.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH -- All libraries and patterns are already implemented in Phases 10-11.1
- Architecture: HIGH -- MviViewModel pattern, test DSL, fake builder DSL fully documented with working examples in codebase
- Pitfalls: HIGH -- Identified from direct codebase audit (missing deps, wrong constructor params, boolean flag mapping)
- Per-VM migration mapping: HIGH -- Every ViewModel read, every State class analyzed, every boolean flag mapped

**Research date:** 2026-02-18
**Valid until:** 2026-03-18 (stable -- no external dependency changes expected)
