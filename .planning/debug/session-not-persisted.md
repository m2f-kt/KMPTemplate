---
status: diagnosed
trigger: "Login session is not persisted across app reinitialization even when the remember me checkbox is checked"
created: 2026-02-13T00:00:00Z
updated: 2026-02-13T00:00:00Z
---

## Current Focus

hypothesis: Three independent gaps in the auth persistence chain prevent remember-me from working
test: Trace the full data flow from UI checkbox through to app startup routing
expecting: Gaps at: (1) rememberMe flag ignored during login, (2) TokenStorage always persists regardless of flag, (3) startup always routes to LoginRoute
next_action: Document findings -- investigation complete

## Symptoms

expected: When "remember me" is checked, tokens should be persisted to disk; on app relaunch, AppNavHost should detect existing tokens and auto-navigate to DashboardRoute
actual: User is always sent back to the login screen on relaunch, regardless of rememberMe state
errors: None (no crash -- purely logic/wiring gap)
reproduction: 1. Open app. 2. Enter credentials. 3. Check "remember me". 4. Login successfully. 5. Kill and relaunch app. 6. Observe: login screen appears instead of dashboard.
started: Always broken -- feature was scaffolded across Phase 3 and Phase 5 but the layers were never wired together

## Eliminated

(No hypotheses eliminated -- each of the three gaps was confirmed on first examination.)

## Evidence

- timestamp: 2026-02-13T00:01:00Z
  checked: LoginViewModel.login() at app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginViewModel.kt
  found: |
    Line 53: `authApi.login(LoginRequest(current.email.trim(), current.password))`
    The `current.rememberMe` value (from LoginState) is never read or forwarded.
    LoginRequest DTO (core/models/.../AuthDtos.kt) only has `email` and `password` fields -- no `rememberMe` field.
  implication: GAP 1 -- The rememberMe flag is captured in UI state but the login() function completely ignores it. It is never passed to AuthApi.

- timestamp: 2026-02-13T00:02:00Z
  checked: AuthApi.login() at core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt
  found: |
    Lines 40-48: `login(request: LoginRequest)` unconditionally calls `tokenStorage.saveTokens(...)` on success via `.onRight`.
    There is no `rememberMe` parameter on AuthApi.login().
    The method signature is: `suspend fun login(request: LoginRequest): Either<AppError, AuthResponse>`
  implication: GAP 2 -- AuthApi.login() always persists tokens to TokenStorage unconditionally. There is no conditional logic to skip persistence when rememberMe is false. This means tokens are ALWAYS saved to disk even if the user did NOT check "remember me". The missing piece is actually the inverse: there is no mechanism to distinguish between session-only (in-memory) and persistent storage. Currently, TokenStorage always writes to `Settings()` (multiplatform-settings), which IS disk-persistent. So tokens ARE saved to disk on every login -- but the startup routing never checks for them.

- timestamp: 2026-02-13T00:03:00Z
  checked: TokenStorage at core/storage/src/commonMain/kotlin/com/m2f/template/storage/TokenStorage.kt
  found: |
    `saveTokens()` writes directly to `Settings` (backed by multiplatform-settings, which IS persistent across app restarts on all platforms).
    No conditional logic for rememberMe. No concept of "session-only" vs "persistent" storage.
    DI module (StorageModule.kt line 10): `single<Settings> { Settings() }` -- uses default Settings(), which on Android = SharedPreferences, on iOS = NSUserDefaults, on JVM = java.util.prefs, on WASM = localStorage. All are persistent.
  implication: TokenStorage successfully persists tokens to disk on every login. The tokens survive app restart. The problem is NOT in storage -- the tokens ARE there on relaunch. The problem is that nothing reads them on startup.

- timestamp: 2026-02-13T00:04:00Z
  checked: AppNavHost at composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
  found: |
    Line 52: `startDestination = LoginRoute` -- hardcoded.
    There is no `LaunchedEffect`, `init` block, or any logic that checks `TokenStorage.getAccessToken()` on startup.
    The only startup `LaunchedEffect` (lines 36-48) checks for OAuth callback URL parameters -- not for existing stored tokens.
    There is no injected `TokenStorage` at the `AppNavHost` level (it is only injected inside the `OAuthCallbackRoute` composable).
  implication: GAP 3 (CRITICAL) -- AppNavHost always starts at LoginRoute. Even though tokens are persisted to disk on every successful login (regardless of rememberMe), the app never checks for them on startup. This is the primary reason the user always sees the login screen.

- timestamp: 2026-02-13T00:05:00Z
  checked: AuthInterceptor at core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt
  found: |
    AuthInterceptor reads tokens from TokenStorage to attach Bearer headers and handles 401 refresh.
    This is working correctly for authenticated API calls during a session.
    Not relevant to the startup routing gap, but confirms TokenStorage is the single source of truth for auth state.
  implication: AuthInterceptor is correctly wired. If startup routing were fixed to check tokens, the rest of the auth pipeline would work for the auto-logged-in session.

## Resolution

root_cause: |
  Three gaps prevent "remember me" from working, listed in order of severity:

  ### GAP 3 (Critical): No token check at startup -- AppNavHost always starts at LoginRoute
  File: `composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt` line 52
  The `startDestination` is hardcoded to `LoginRoute`. There is no logic anywhere in AppNavHost
  that checks `TokenStorage.getAccessToken()` on composition/launch and conditionally navigates
  to `DashboardRoute`. Even though tokens ARE persisted to disk (see Gap 2 note), the app
  ignores them on relaunch.

  ### GAP 1 (Medium): rememberMe flag is never forwarded from LoginViewModel to AuthApi
  File: `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginViewModel.kt` line 53
  `LoginViewModel.login()` reads `current.email` and `current.password` but ignores `current.rememberMe`.
  The `LoginRequest` DTO has no `rememberMe` field. The flag is dead state -- captured in UI but
  never used in business logic.

  ### GAP 2 (Design): TokenStorage has no concept of session-only vs persistent storage
  File: `core/storage/src/commonMain/kotlin/com/m2f/template/storage/TokenStorage.kt`
  `AuthApi.login()` unconditionally calls `tokenStorage.saveTokens()`, which always writes to
  disk-backed `Settings`. There is no way to store tokens in-memory-only for a single session
  (when rememberMe is false). Currently this means tokens are ALWAYS persisted -- which actually
  makes Gap 3 the only critical blocker for the happy path. But for correctness, when rememberMe
  is unchecked, tokens should NOT survive app restart.

fix: (not applied -- research only)
verification: (not applicable)
files_changed: []

## Suggested Fix Direction

1. **Fix Gap 3 first (unblocks the feature):**
   In `AppNavHost`, inject `TokenStorage` via `koinInject()`, and in a `LaunchedEffect(Unit)`
   check `tokenStorage.getAccessToken()`. If a valid token exists, navigate to `DashboardRoute`
   (with `popUpTo(LoginRoute) { inclusive = true }`). Optionally validate the token by hitting
   a profile or token-verify endpoint.

2. **Fix Gap 1 (wire rememberMe through):**
   Add a `rememberMe: Boolean` parameter to `AuthApi.login()` (or pass it alongside the call).
   Have `LoginViewModel.login()` forward `current.rememberMe` to `AuthApi`.

3. **Fix Gap 2 (conditional persistence):**
   When `rememberMe = false`, store tokens only in an in-memory holder (not in `Settings`).
   When `rememberMe = true`, persist to `Settings` as today. `AuthInterceptor` should read
   from whichever store has tokens. On app restart, only `Settings`-backed tokens survive.

4. **Consider token expiry validation:**
   The startup check should ideally verify the stored token is not expired (e.g., decode the
   JWT `exp` claim or call a lightweight auth endpoint) before auto-navigating. If expired,
   attempt a refresh using the stored refresh token; if that also fails, route to login.
