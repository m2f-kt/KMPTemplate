# Phase 5: Auth Screens, Dashboard & Setup CLI - Research

**Researched:** 2026-02-13
**Domain:** Compose Multiplatform UI (auth flows, dashboard, profile), server-side OAuth, setup CLI scripting
**Confidence:** HIGH

## Summary

Phase 5 builds the end-to-end user-facing screens: login/signup with form validation and OAuth, a sample dashboard with sidebar navigation, profile screens for all 5 user tiers, and a setup CLI script. The codebase is well-positioned -- Phase 3 delivered the SDK layer (AuthApi, UserApi, AuthInterceptor, TokenStorage, ErrorMapper) and Phase 4 delivered the TerminalTheme design system with 41 reusable components and the navigation skeleton (Routes.kt, AppNavHost with placeholder screens). The existing Pencil design file (`terminal_design_system.pen`) contains fully specified designs for all screens: 4 auth screens (Login Desktop/Mobile, Register Desktop/Mobile), Dashboard Desktop/Mobile, and 10 profile screens (5 tiers x 2 form factors).

The primary work is (1) building ViewModels with Koin injection and Arrow validation, (2) composing screens from existing design system components matching the Pencil layouts, (3) adding server-side OAuth endpoints for Google and Apple, (4) modeling user tiers as a sealed type, (5) implementing responsive layout switching, and (6) creating the setup CLI bash script. All needed dependencies (Koin Compose ViewModel, navigation-compose, Arrow, Ktor client/server) are already in the project.

**Primary recommendation:** Build from the inside out -- ViewModel layer first (with validation and auth state management), then screen composables matching Pencil designs, then server OAuth endpoints, then user tier modeling, and finally the setup CLI script. Use `koinViewModel()` from the already-included `koin-compose-viewmodel` library for ViewModel injection at navigation destinations.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Follow Pencil designs exactly -- 4 screens in the Auth group: Login Desktop (split: brand panel left, form right), Login Mobile (centered card), Register Desktop (split: brand tagline left, form right), Register Mobile (centered card)
- Responsive: desktop uses split layout, mobile uses centered card
- Google OAuth: functional on all targets (Android, iOS, Desktop, WASM)
- Apple Sign-In: functional on web (WASM) and iOS only -- not shown on Android/Desktop
- Replaces GitHub from Pencil design -- update button to show Apple instead of GitHub where applicable
- Both Remember Me and Reset Password functional
- Remember me: persists session (longer token expiry or persistent refresh token)
- Reset password: sends email flow (requires new server endpoint)
- Combined error approach: field-level inline errors for validation, alert banner for server errors
- Accumulated validation errors -- Arrow zipOrAccumulate
- Direct navigation to dashboard -- no intermediate loading state or animation
- Mock/static data for all dashboard metrics (uptime 99.98%, requests 1.2M, response time 42ms, error rate 0.03%)
- All 5 tiers implemented: Free, Paid, Premium, Admin, PowerAdmin
- User type modeled as sealed type in backend (not a String enum)
- Dashboard + Profile are real functional screens; other sidebar nav items show placeholder screens
- Mobile uses bottom tab navigation per Pencil design
- Edit profile: functional -- opens editable fields, uses PUT /me endpoint
- Logout: functional -- clears tokens, navigates back to login screen

### Claude's Discretion
- Form validation timing (on blur vs on submit vs real-time)
- Loading states/skeletons during data fetch
- Password strength requirements display
- Mobile bottom nav exact behavior and transitions
- Setup CLI implementation details (interactive prompts, what gets renamed)
- Placeholder screen content for non-functional nav items

### Deferred Ideas (OUT OF SCOPE)
- Real server metrics (actual uptime, request counts) -- could replace mock data later
- Functional processes/logs/deployments/settings screens -- each could be its own phase
- GitHub OAuth -- user chose Google + Apple instead
</user_constraints>

## Standard Stack

### Core (Already in Project)
| Library | Version | Purpose | Status |
|---------|---------|---------|--------|
| Koin Compose ViewModel | 4.1.1 | ViewModel injection in Compose navigation | In `libs.versions.toml`, in `composeApp/build.gradle.kts` |
| Koin Compose ViewModel Navigation | 4.1.1 | `koinViewModel()` at navigation destinations | In `libs.versions.toml`, in `composeApp/build.gradle.kts` |
| Navigation Compose | 2.9.2 | Type-safe navigation with `@Serializable` routes | In `composeApp/build.gradle.kts` |
| Arrow Core | 2.2.1.1 | `zipOrAccumulate`, `Either`, `Raise` for validation | In `core:sdk/build.gradle.kts` |
| Ktor Client | 3.4.0 | HTTP client with AuthInterceptor | In `core:sdk/build.gradle.kts` |
| Ktor Server Auth | 3.4.0 | Server-side OAuth plugin | In `server/build.gradle.kts` via `ktor-core` bundle |
| Multiplatform Settings | 1.3.0 | TokenStorage, PreferencesStorage | In `core:storage/build.gradle.kts` |
| Compose Multiplatform | 1.10.1 | Foundation UI primitives | In `composeApp/build.gradle.kts` |
| Lifecycle ViewModel Compose | 2.9.6 | ViewModel lifecycle management | In `composeApp/build.gradle.kts` |
| Lifecycle Runtime Compose | 2.9.6 | `collectAsStateWithLifecycle` | In `composeApp/build.gradle.kts` |

### Needs Adding
| Library | Version | Purpose | Where |
|---------|---------|---------|-------|
| None | - | All dependencies already present | - |

**Key finding:** Every needed dependency is already in the project. No new library additions required. The `koin-compose-viewmodel` and `koin-compose-viewmodel-navigation` are already declared in `libs.versions.toml` and wired in `composeApp/build.gradle.kts`. Arrow is in `core:sdk`. Ktor server auth (including OAuth provider) is in the `ktor-core` bundle.

### Not Needed
| Instead of | Why Not |
|------------|---------|
| KMPAuth / KotlinMultiplatformAuth | OAuth flow is server-initiated -- client just opens a URL and receives callback token. No need for KMP-specific OAuth library. |
| material3-windowsizeclass-multiplatform | Can use `BoxWithConstraints` or simple width measurement. Avoids Material3 dependency in the terminal design system. |
| Firebase Auth | Server-side OAuth with Ktor gives more control and avoids Firebase dependency |

## Architecture Patterns

### Recommended Module Structure (Extends Existing)
```
app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/
    LoginScreen.kt              # Login composable (desktop + mobile)
    LoginViewModel.kt           # Login state + auth logic
    RegisterScreen.kt           # Register composable (desktop + mobile)
    RegisterViewModel.kt        # Register state + validation
    ForgotPasswordScreen.kt     # Reset password flow
    ForgotPasswordViewModel.kt  # Reset password logic

app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/
    DashboardScreen.kt          # Dashboard composable (desktop + mobile)
    DashboardViewModel.kt       # Mock data provider
    DashboardSidebar.kt         # Sidebar nav (desktop)
    DashboardBottomNav.kt       # Bottom tabs (mobile)

app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/   # NEW MODULE
    ProfileScreen.kt            # Profile composable (desktop + mobile)
    ProfileViewModel.kt         # Profile state + edit logic
    ProfileSidebar.kt           # Tier-aware sidebar nav

core/models/src/commonMain/.../models/
    UserTier.kt                 # Sealed type: Free, Paid, Premium, Admin, PowerAdmin
    dto/UserDtos.kt             # Update UserResponse to include tier

server/auth/.../
    routes/OAuthRoutes.kt       # Google + Apple OAuth endpoints
    service/OAuthService.kt     # Token exchange logic
    routes/PasswordResetRoutes.kt  # Password reset endpoint

composeApp/src/commonMain/.../
    navigation/Routes.kt        # Add ForgotPasswordRoute + placeholder routes
    navigation/AppNavHost.kt    # Wire real screens + responsive layout
```

### Pattern 1: ViewModel with Koin Injection
**What:** ViewModels injected via `koinViewModel()` at navigation destinations, using Arrow Either for error handling.
**When to use:** Every screen that needs state management or API calls.
**Example:**
```kotlin
// LoginViewModel.kt
class LoginViewModel(
    private val authApi: AuthApi,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, serverError = null) }
            authApi.login(LoginRequest(
                email = _state.value.email,
                password = _state.value.password,
            )).fold(
                ifLeft = { error ->
                    _state.update { it.copy(isLoading = false, serverError = error.message) }
                },
                ifRight = {
                    _state.update { it.copy(isLoading = false, loginSuccess = true) }
                },
            )
        }
    }
}

// In AppNavHost.kt
composable<LoginRoute> {
    val viewModel = koinViewModel<LoginViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginScreen(state = state, onAction = viewModel::onAction, ...)
}
```
**Source:** Existing pattern in codebase (koin-compose-viewmodel already in deps)

### Pattern 2: Arrow Accumulated Validation (Client-Side)
**What:** `zipOrAccumulate` validates all fields at once, collecting all errors before submitting.
**When to use:** Login and registration form validation before API call.
**Example:**
```kotlin
// In RegisterViewModel
fun validateAndRegister() {
    val result: Either<NonEmptyList<FieldError>, RegisterRequest> = either {
        zipOrAccumulate(
            { validateEmail(state.value.email) },
            { validatePassword(state.value.password) },
            { validateName(state.value.firstName) },
            { validateName(state.value.lastName) },
        ) { email, password, firstName, lastName ->
            RegisterRequest(email, password, "$firstName $lastName")
        }
    }
    result.fold(
        ifLeft = { errors ->
            _state.update { it.copy(fieldErrors = errors.toMap()) }
        },
        ifRight = { request ->
            // proceed with API call
        },
    )
}
```
**Source:** Existing `ValidationSupport.kt` + `AuthService.kt` pattern from Phase 2/3

### Pattern 3: Responsive Layout with BoxWithConstraints
**What:** Use `BoxWithConstraints` to switch between desktop (split/sidebar) and mobile (card/bottom nav) layouts without Material3 WindowSizeClass dependency.
**When to use:** Every screen that has desktop vs mobile variants in Pencil designs.
**Example:**
```kotlin
@Composable
fun LoginScreen(state: LoginState, ...) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth > 840.dp) {
            LoginDesktopLayout(state, ...)  // Split: brand panel left, form right
        } else {
            LoginMobileLayout(state, ...)   // Centered card
        }
    }
}
```
**Rationale:** Avoids Material3 dependency entirely. The Pencil design shows exactly 2 breakpoints (desktop split at ~840dp, mobile card). BoxWithConstraints is Foundation-level.

### Pattern 4: Server-Side OAuth (Authorization Code Flow)
**What:** Ktor's built-in `oauth` authentication provider handles the authorization code flow. Client opens a URL, server handles token exchange, returns JWT.
**When to use:** Google and Apple social login.
**Example:**
```kotlin
// Server: OAuthRoutes.kt
fun Route.oauthRoutes(oauthService: OAuthService) {
    authenticate("google-oauth") {
        get("/api/auth/oauth/google") {
            // Ktor automatically redirects to Google
        }
        get("/api/auth/oauth/google/callback") {
            val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()
            val authResponse = oauthService.handleGoogleCallback(principal)
            call.respond(authResponse)
        }
    }
}
```
**Source:** Ktor OAuth documentation (https://ktor.io/docs/server-oauth.html)

### Pattern 5: User Tier Sealed Type
**What:** Model user tiers as a sealed class/interface in `core:models` (shared), with string serialization for wire format and database storage.
**When to use:** Determining profile screen layout, sidebar nav items, feature gating.
**Example:**
```kotlin
// core/models: UserTier.kt
@Serializable
sealed class UserTier {
    abstract val displayName: String
    abstract val level: Int

    @Serializable data object Free : UserTier() {
        override val displayName = "free_tier"
        override val level = 0
    }
    @Serializable data object Paid : UserTier() {
        override val displayName = "paid_user"
        override val level = 1
    }
    @Serializable data object Premium : UserTier() {
        override val displayName = "premium_user"
        override val level = 2
    }
    @Serializable data object Admin : UserTier() {
        override val displayName = "admin_user"
        override val level = 3
    }
    @Serializable data object PowerAdmin : UserTier() {
        override val displayName = "power_admin"
        override val level = 4
    }

    companion object {
        fun fromString(role: String): UserTier = when (role.uppercase()) {
            "FREE" -> Free
            "PAID" -> Paid
            "PREMIUM" -> Premium
            "ADMIN" -> Admin
            "POWERADMIN", "POWER_ADMIN" -> PowerAdmin
            else -> Free
        }
    }
}
```
**Rationale:** Sealed type is exhaustive in `when` expressions -- compiler catches missing tier handling. `fromString()` bridges the current `String` role in `UsersTable` and `UserResponse`.

### Anti-Patterns to Avoid
- **Material3 usage in screens:** The entire design system is Foundation-only. Do NOT import Material3 components. Use TerminalButton, TerminalInput, TerminalCard, TerminalAlert, etc.
- **Stateful composables without ViewModel:** Every screen with user interaction must have a ViewModel. No `remember` blocks holding form state at the composable level.
- **Platform-specific OAuth UI:** Do NOT use platform-specific Google Sign-In buttons or native OAuth flows. The client opens a URL; the server handles everything. The exception is platform-conditional visibility of the Apple button.
- **String comparison for user tiers:** Always use the sealed type, never `if (role == "ADMIN")`.
- **Blocking navigation on login success:** Decision says "direct navigation to dashboard -- no intermediate loading state."

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Form validation accumulation | Custom error collection | Arrow `zipOrAccumulate` | Already used in server-side AuthService; pattern proven. Handles NonEmptyList<FieldError> correctly |
| Token storage/refresh | Custom token management | Existing `TokenStorage` + `AuthInterceptor` | Already built in Phase 3. AuthInterceptor handles 401 -> refresh -> retry automatically |
| OAuth token exchange | Custom HTTP calls to Google/Apple | Ktor `oauth` provider plugin | Handles CSRF state, redirect URLs, code-to-token exchange automatically |
| Responsive breakpoints | Custom window size detection | `BoxWithConstraints` | Foundation-level, no extra deps, works on all KMP targets |
| ViewModel injection | Manual DI in composables | `koinViewModel()` from koin-compose-viewmodel | Already wired. Scopes to NavBackStackEntry lifecycle automatically |
| Password masking toggle | Custom implementation | Existing `TerminalPasswordInput` | Already built in Phase 4 with eye icon toggle |
| Error banners | Custom alert composable | Existing `TerminalAlert` with `AlertVariant.Error` | Already built in Phase 4, matches Pencil design |

**Key insight:** The Phase 3 SDK + Phase 4 design system provide almost everything needed. The main work is composing existing pieces into screens and adding ViewModels.

## Common Pitfalls

### Pitfall 1: Navigation Back Stack on Auth Flow
**What goes wrong:** After login, pressing back returns to the login screen instead of exiting.
**Why it happens:** Default navigation keeps auth screens on the back stack.
**How to avoid:** Already solved in Routes/AppNavHost -- `popUpTo<LoginRoute> { inclusive = true }` clears auth back stack on successful login. Verify this is preserved when replacing placeholder screens with real ones.
**Warning signs:** Back button on dashboard shows login screen.

### Pitfall 2: ViewModel Re-creation on Configuration Change
**What goes wrong:** Form state lost on rotation or window resize.
**Why it happens:** ViewModel not properly scoped to NavBackStackEntry.
**How to avoid:** Use `koinViewModel()` at the `composable<Route>` level, not inside child composables. The ViewModel is automatically scoped to the navigation destination.
**Warning signs:** Fields clear when rotating device.

### Pitfall 3: OAuth Redirect URL Mismatch
**What goes wrong:** OAuth fails with "redirect_uri_mismatch" error.
**Why it happens:** The redirect URL registered in Google/Apple console doesn't match the server's callback URL.
**How to avoid:** Use a consistent callback URL pattern: `{BASE_URL}/api/auth/oauth/{provider}/callback`. Document the required console configuration in setup CLI output.
**Warning signs:** 400 error from Google/Apple during OAuth flow.

### Pitfall 4: Remember Me Token Persistence
**What goes wrong:** "Remember me" doesn't actually persist across app restarts.
**Why it happens:** Access token has short expiry and refresh token is only in memory.
**How to avoid:** When "remember me" is checked, save the refresh token to `TokenStorage` (multiplatform-settings persists to disk). When unchecked, use in-memory only (session-length). The existing `TokenStorage` already uses `Settings` which persists.
**Warning signs:** User must re-login after app kill despite checking "remember me."
**Resolution:** TokenStorage ALREADY persists via `multiplatform-settings`. For "remember me = false", clear tokens on app background/close. For "remember me = true" (default since tokens already persist), do nothing extra.

### Pitfall 5: Sealed Type Serialization in UserResponse
**What goes wrong:** Server sends `"role": "ADMIN"` but client expects a sealed class discriminator.
**Why it happens:** UserResponse currently has `role: String`. Changing to `UserTier` breaks wire format.
**How to avoid:** Keep `role: String` on the wire (UserResponse DTO). Add a computed `tier: UserTier` property that maps from the string. Or use a custom serializer. The sealed type is for client-side logic, not wire protocol.
**Warning signs:** Deserialization failures on profile fetch.

### Pitfall 6: Apple Sign-In Platform Conditional
**What goes wrong:** Apple button shows on Android/Desktop where it doesn't work.
**Why it happens:** Compose Multiplatform commonMain has no `expect/actual` for platform detection by default.
**How to avoid:** Use an `expect fun currentPlatform(): Platform` in commonMain with `actual` implementations. Or use `@OptIn(ExperimentalComposeUiApi::class)` with `LocalWindowInfo` checks. Simplest: use compile-time source sets (show Apple button in `iosMain` and `wasmJsMain`, hide in `androidMain` and `jvmMain`).
**Warning signs:** Apple sign-in button visible on Android.

### Pitfall 7: Setup CLI Partial Execution
**What goes wrong:** CLI script fails midway, leaving project in half-renamed state.
**Why it happens:** `sed` or `find` command fails on one file, but script continues.
**How to avoid:** Use `set -euo pipefail` at script start. Validate inputs before any modification. Consider a dry-run mode that shows what would change.
**Warning signs:** Build fails after running setup script.

## Code Examples

### Login ViewModel State
```kotlin
data class LoginState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    // Field-level validation errors (inline)
    val emailError: String? = null,
    val passwordError: String? = null,
    // Server-level error (alert banner)
    val serverError: String? = null,
)
```

### Register ViewModel State
```kotlin
data class RegisterState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val registerSuccess: Boolean = false,
    // Field-level errors
    val fieldErrors: Map<String, String> = emptyMap(),
    // Server error
    val serverError: String? = null,
)
```

### Dashboard Mock Data
```kotlin
// From Pencil design -- LOCKED mock data values
object DashboardMockData {
    val metrics = listOf(
        MetricItem("UPTIME", "99.98%", "+0.02% ↑", MetricTrend.Up),
        MetricItem("REQUESTS", "1.2M", "+18.3% ↑", MetricTrend.Up),
        MetricItem("AVG LATENCY", "42ms", "-8ms ↓", MetricTrend.Down, isHighlighted = true),
        MetricItem("ERROR RATE", "0.03%", "-0.01% ↓", MetricTrend.Down),
    )

    val processes = listOf(
        ProcessItem("node server.js", 1337, "12.4%", "running"),
        ProcessItem("postgres", 2048, "3.2%", "running"),
        ...
    )

    val recentActivity = listOf(
        ActivityItem("deploy_v2.4.1", "prod", "2 min ago", "git-commit-horizontal"),
        ActivityItem("high_memory_alert", "worker-3", "12 min ago", "triangle-alert"),
        ...
    )

    val deployment = DeploymentStatus(
        build = 1.0f,   // 100%
        tests = 1.0f,   // 100%
        deploy = 0.78f, // 78%
    )
}
```

### Responsive Login Screen
```kotlin
@Composable
fun LoginScreen(
    state: LoginState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(TerminalTheme.colors.bg)) {
        if (maxWidth > 840.dp) {
            // Desktop split: brand panel left, form right (Pencil: xNUU3)
            Row(Modifier.fillMaxSize()) {
                LoginBrandPanel(modifier = Modifier.weight(1f))
                LoginFormPanel(state, ..., modifier = Modifier.width(520.dp))
            }
        } else {
            // Mobile centered card (Pencil: 9UXn1)
            LoginMobileLayout(state, ...)
        }
    }
}
```

### OAuth Server Configuration (Google)
```kotlin
// Server: install OAuth provider
install(Authentication) {
    oauth("google-oauth") {
        urlProvider = { "${config.baseUrl}/api/auth/oauth/google/callback" }
        providerLookup = {
            OAuthServerSettings.OAuth2ServerSettings(
                name = "google",
                authorizeUrl = "https://accounts.google.com/o/oauth2/v2/auth",
                accessTokenUrl = "https://oauth2.googleapis.com/token",
                clientId = config.google.clientId,
                clientSecret = config.google.clientSecret,
                requestMethod = HttpMethod.Post,
                defaultScopes = listOf("openid", "profile", "email"),
            )
        }
        client = httpClient
    }
}
```

### Setup CLI Script Structure
```bash
#!/usr/bin/env bash
set -euo pipefail

# Colors for terminal output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}> terminal setup_project${NC}"
echo ""

# Interactive prompts
read -p "Project name (e.g., MyApp): " PROJECT_NAME
read -p "Package name (e.g., com.company.app): " PACKAGE_NAME
read -p "Database name (e.g., myapp_db): " DB_NAME

# Validation
[[ -z "$PROJECT_NAME" ]] && echo "Error: Project name required" && exit 1
[[ ! "$PACKAGE_NAME" =~ ^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+$ ]] && echo "Error: Invalid package" && exit 1

# Derived values
PACKAGE_PATH="${PACKAGE_NAME//./\/}"
OLD_PACKAGE="com.m2f.template"
OLD_PACKAGE_PATH="com/m2f/template"

echo ""
echo "Renaming: $OLD_PACKAGE -> $PACKAGE_NAME"
echo "Database: template -> $DB_NAME"
echo ""

# 1. Rename package in all Kotlin files
# 2. Move source directories
# 3. Update build.gradle.kts files (namespace, applicationId)
# 4. Update settings.gradle.kts (rootProject.name)
# 5. Update database configuration
# 6. Verify build
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `mutableStateOf()` in composables | ViewModel + StateFlow + `collectAsStateWithLifecycle` | Lifecycle 2.6+ | State survives config changes, testable |
| Firebase Auth SDK | Server-side OAuth + custom JWT | Always valid for full-stack | Full control over token lifecycle, no Firebase dep |
| Material3 WindowSizeClass | `BoxWithConstraints` width check | N/A (design choice) | Zero Material3 dependency, terminal design system stays pure Foundation |
| String-based roles | Sealed type with exhaustive when | Kotlin best practice | Compile-time safety, no forgotten tier handling |
| Platform expect/actual for OAuth | Server-side OAuth (platform-agnostic) | Ktor OAuth plugin | One codebase handles all platforms, client just opens URL |

## Discretion Recommendations

### Form Validation Timing
**Recommendation: Validate on submit, clear errors on field change.**
- On submit: run `zipOrAccumulate` to validate all fields, display all errors at once
- On field change: if field previously had an error, clear that specific error as user types
- Rationale: Avoids premature error display (annoying "invalid email" while still typing), but gives immediate feedback once they've seen an error

### Loading States/Skeletons
**Recommendation: Simple indeterminate TerminalProgress bar at top of content area.**
- Dashboard: show `TerminalProgress(progress = null, label = "loading...")` while mock data "loads" (simulate with 300ms delay for realism)
- Profile: same pattern for profile fetch
- Login/Register: disable button + show loading text ("$ authenticating...") during API call
- Rationale: Terminal aesthetic -- no skeleton shimmer. A single progress indicator matches the CLI feel.

### Password Strength Requirements Display
**Recommendation: Show requirements below password field only after user starts typing.**
- Display as muted text: "min 8 characters"
- Requirements met turn green (success color), unmet stay dim
- Rationale: Minimal but helpful. Matches terminal aesthetic (status indicators).

### Mobile Bottom Nav Behavior
**Recommendation: 4 tabs matching Pencil design (home, processes, logs, settings).**
- Home = Dashboard, other 3 = placeholder screens
- Active tab uses accent color, inactive uses textDim
- No animation between tabs (direct switch, matching "terminal" feel)
- Profile accessed via avatar/user row in header, not bottom nav

### Setup CLI Details
**Recommendation: Single `setup.sh` script with interactive prompts.**
- Prompts: project name, package name, database name
- Renames: Kotlin package, applicationId, namespace, rootProject.name, DB schema name
- Validates inputs before modifying
- Shows dry-run summary, asks for confirmation
- Runs `./gradlew build` at end to verify
- Target: complete in under 2 minutes (requirement DX-01)

### Placeholder Screen Content
**Recommendation: Terminal-styled "coming soon" with route name.**
```
> {route_name}
// under construction
status: pending
```
- Use existing PlaceholderScreen pattern from AppNavHost but with TerminalCard wrapper
- Show the route name as a terminal command prefix

## Pencil Design Inventory

### Auth Group (4 screens)
| Screen | Pencil ID | Layout | Key Elements |
|--------|-----------|--------|--------------|
| Login Desktop | `xNUU3` | Split: leftPanel(surface) + rightPanel(520px) | Brand logo, ASCII art, quote, status line / Form: email, password, remember me, forgot password, login btn, "or" divider, social buttons, signup link |
| Login Mobile | `9UXn1` | Centered card (402px) | Brand+icon header, title block, card with accent bar, email, password, remember me, forgot password, login btn, divider, social row, register link |
| Register Desktop | `B1nWB` | Split: brandPanel + formPanel(520px) | Brand logo, hero text, 3 feature bullets / Card: first+last name, email, password, confirm, terms checkbox, register btn, divider, social, signin link |
| Register Mobile | `KXp69` | Centered card (480px) | Brand+icon header, title, card with accent bar, name row, email, password, confirm, terms, register btn, divider, social, signin link |

### Dashboard (2 screens)
| Screen | Pencil ID | Layout | Key Elements |
|--------|-----------|--------|--------------|
| Dashboard Desktop | `9JbaX` | Sidebar(260px) + mainContent | Sidebar: brand, nav (5 items), divider, user row / Main: header, 4 metric cards, left column (process table), right column (activity list, deploy progress) |
| Dashboard Mobile | `dezbz` | Vertical + bottomNav(64px) | Title block, 2x2 metric grid, process list, deploy progress, activity list / Bottom: 4 tabs (home, processes, logs, settings) |

### Profile (10 screens: 5 tiers x 2 form factors)
| Tier | Desktop ID | Mobile ID | Sidebar Differences | Main Content Differences |
|------|-----------|-----------|---------------------|-------------------------|
| Free | `EnD7X` | `rCzUG` | 3 nav items + upgrade card | Account info, usage limits, warning alert, preferences, locked features list, upgrade CTA |
| Paid | `ENKv0` | `k6yQw` | 6 nav items + premium upgrade info | Account info (more fields), team access, analytics, exports |
| Premium | `d4oDS` | `qgU7x` | 7 nav items + premium success alert | Full features, webhooks, priority support |
| Admin | `uGWqD` | `jiyew` | 7 mgmt + 2 tools nav items | User management, groups, permissions, analytics, audit log, org settings |
| PowerAdmin | `QFwfO` | `OSLp4` | CRM (6) + System (5) nav items | Platform stats, user directory, admin identity, access matrix, system status, danger zone |

### Social Login Buttons (from Pencil)
- Pencil shows GitHub + Google buttons (`JFaAM` secondary button variant)
- **Decision:** Replace GitHub with Apple (Apple icon on WASM + iOS, hidden on Android + Desktop)
- Google button stays on all platforms

## Server-Side Changes Needed

### 1. OAuth Endpoints (New)
- `GET /api/auth/oauth/google` -- initiates Google OAuth flow
- `GET /api/auth/oauth/google/callback` -- handles Google callback, creates/finds user, returns AuthResponse
- `GET /api/auth/oauth/apple` -- initiates Apple OAuth flow (WASM + iOS)
- `GET /api/auth/oauth/apple/callback` -- handles Apple callback
- Implementation: Ktor `oauth` provider plugin (already in deps via ktor-server-auth)

### 2. Password Reset Endpoints (New)
- `POST /api/auth/forgot-password` -- accepts email, generates reset token, "sends" email (log in dev)
- `POST /api/auth/reset-password` -- accepts token + new password, updates password
- Needs: `password_reset_tokens` table, token generation, expiry check

### 3. User Tier Model Changes
- `UsersTable.role` stays as `varchar` (database layer)
- `UserResponse.role` stays as `String` (wire format)
- Add `UserTier` sealed class in `core:models` with `fromString()` conversion
- Client-side: `UserResponse.tier: UserTier get() = UserTier.fromString(role)`

### 4. Register DTO Update
- Current `RegisterRequest` has: email, password, name
- Needs: firstName + lastName (Pencil design has separate fields)
- Option A: Change `name` field to accept "firstName lastName" concatenated
- Option B: Add firstName/lastName fields to RegisterRequest
- **Recommendation:** Option B -- add `firstName` and `lastName` to `RegisterRequest`, concatenate to `name` on server, keep backward compat

## Client-Side Architecture

### Feature Module Dependencies
```
app:auth -> core:models, core:sdk, app:designsystem
app:dashboard -> core:models, core:sdk, app:designsystem
app:profile (NEW) -> core:models, core:sdk, app:designsystem
composeApp -> app:auth, app:dashboard, app:profile, shared
```

### ViewModel Registration (Koin)
```kotlin
// In app:auth module or composeApp
val authViewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
}

val dashboardViewModelModule = module {
    viewModelOf(::DashboardViewModel)
}

val profileViewModelModule = module {
    viewModelOf(::ProfileViewModel)
}
```

### Navigation Route Additions
```kotlin
// Current routes (from Phase 4):
@Serializable data object LoginRoute
@Serializable data object RegisterRoute
@Serializable data object DashboardRoute
@Serializable data object ProfileRoute

// Need to add:
@Serializable data object ForgotPasswordRoute
// Placeholder routes for sidebar nav items:
@Serializable data object ProcessesRoute
@Serializable data object LogsRoute
@Serializable data object DeploymentsRoute
@Serializable data object SettingsRoute
```

## Open Questions

1. **OAuth Client Flow on Desktop/WASM**
   - What we know: Server-side OAuth handles the code exchange. Client needs to open a browser URL and receive the token back.
   - What's unclear: How does the desktop app receive the callback? Options: (a) embedded webview, (b) localhost redirect, (c) deep link.
   - Recommendation: Use localhost redirect pattern -- server redirects to `http://localhost:{PORT}/auth/callback?token=...` for desktop. For WASM, normal browser redirect works. For mobile, use deep links/universal links. This can be refined during implementation.

2. **Apple Sign-In on WASM**
   - What we know: Apple Sign-In works via JavaScript SDK on web. WASM can interop with JS.
   - What's unclear: Exact JS interop pattern for triggering Apple Sign-In from Kotlin/WASM.
   - Recommendation: Server-side flow avoids this -- the WASM app just opens a URL to the server's Apple OAuth endpoint, which redirects to Apple. Callback comes back to server. No JS interop needed.

3. **Password Reset Email Delivery**
   - What we know: Server needs to "send" a reset email with a token link.
   - What's unclear: No email service is configured in the project.
   - Recommendation: For this phase, log the reset link to console (dev mode). Add a `TODO` for real email integration. The endpoint and flow should be complete -- only the delivery mechanism is stubbed.

4. **Profile Module vs Dashboard Module**
   - What we know: Profile screens are complex (5 tiers, sidebar, main content). Dashboard is separate.
   - What's unclear: Should profile be a new `app:profile` module or live inside `app:dashboard`?
   - Recommendation: New `app:profile` module. Profile has its own ViewModel, tier-specific sidebars, and edit functionality. Keeping it separate follows the existing module pattern.

## Sources

### Primary (HIGH confidence)
- Codebase inspection: All `.kt` files, `build.gradle.kts`, `libs.versions.toml` -- verified current state
- Pencil design file: `terminal_design_system.pen` -- all screen layouts, component IDs, design tokens verified
- Ktor OAuth docs: https://ktor.io/docs/server-oauth.html -- server-side OAuth flow pattern
- Koin Compose docs: https://insert-koin.io/docs/reference/koin-compose/compose/ -- koinViewModel pattern

### Secondary (MEDIUM confidence)
- KMPAuth library: https://github.com/mirzemehdi/KMPAuth -- researched but NOT using (server-side OAuth preferred)
- Apple Sign-In KMP: https://medium.com/@Tweeel/implementing-apple-sign-in-in-kotlin-multiplatform-kmp-6e6b1a1cffca -- confirms server-side approach is simpler
- Compose Multiplatform adaptive: https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-adaptive-layouts.html -- BoxWithConstraints confirmed available

### Tertiary (LOW confidence)
- Desktop OAuth localhost redirect pattern -- common approach but not verified for KMP/Compose Desktop specifically

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH -- all libraries already in project, versions confirmed
- Architecture: HIGH -- patterns proven in prior phases (Arrow Raise, Koin, TerminalTheme)
- Pitfalls: HIGH -- most identified from existing codebase patterns and Phase 2-4 decisions
- OAuth server-side: MEDIUM -- Ktor OAuth plugin documented but not yet used in this project
- Setup CLI: MEDIUM -- bash scripting is straightforward but rename paths need careful testing
- Desktop OAuth callback: LOW -- localhost redirect pattern needs validation during implementation

**Research date:** 2026-02-13
**Valid until:** 2026-03-13 (stable -- all deps are released versions, no pre-release)
