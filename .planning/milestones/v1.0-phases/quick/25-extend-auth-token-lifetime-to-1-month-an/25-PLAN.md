---
phase: quick-25
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt
  - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt
  - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
autonomous: true
requirements: [AUTH-TOKEN-LIFETIME, AUTH-SESSION-EXPIRY]

must_haves:
  truths:
    - "Access token lasts 1 day, refresh token lasts 30 days by default"
    - "When refresh token expires and refresh fails, app navigates to login screen"
    - "Navigation stack is fully cleared on session expiry (no back to dashboard)"
    - "Token lifetimes remain overridable via JWT_ACCESS_EXPIRY and JWT_REFRESH_EXPIRY env vars"
  artifacts:
    - path: "server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt"
      provides: "Updated default token lifetimes"
      contains: "86400000L"
    - path: "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt"
      provides: "Session expired event emission via SharedFlow"
      contains: "sessionExpired"
    - path: "composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt"
      provides: "Session expiry observer that navigates to LoginRoute"
      contains: "sessionExpired"
  key_links:
    - from: "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt"
      to: "composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt"
      via: "sessionExpired SharedFlow collected in LaunchedEffect"
      pattern: "sessionExpired"
---

<objective>
Extend auth token lifetimes (access: 15min -> 1 day, refresh: 7 days -> 30 days) and add forced logout when the refresh token expires, navigating the user back to the login screen with a clean navigation stack.

Purpose: Users are getting logged out too frequently with 15-minute access tokens. When both tokens expire, the app stays in an error state instead of gracefully redirecting to login.
Output: Updated token defaults in Env.kt, session expiry signaling in AuthInterceptor, and auto-navigation to login in AppNavHost.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt
@core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt
@composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
@core/storage/src/commonMain/kotlin/com/m2f/template/storage/TokenStorage.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Extend token lifetimes and add session expiry signaling</name>
  <files>
    server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt
    core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt
  </files>
  <action>
  1. In Env.kt, update the default token expiry constants:
     - Change ACCESS_TOKEN_EXPIRY from 900000L (15 min) to 86400000L (1 day = 24 * 60 * 60 * 1000)
     - Change REFRESH_TOKEN_EXPIRY from 604800000L (7 days) to 2592000000L (30 days = 30 * 24 * 60 * 60 * 1000)
     - Update the comments to reflect the new durations

  2. In AuthInterceptor.kt, add a session expiry event mechanism:
     - Add import for kotlinx.coroutines.flow.MutableSharedFlow, SharedFlow, asSharedFlow
     - Add a private `_sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)` field
     - Expose a public `val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()` property
     - In both places where `tokenStorage.clearTokens()` is called on refresh failure (the `else` branch on line 82 and the `catch` block on line 87), also call `_sessionExpired.tryEmit(Unit)` AFTER clearTokens()
     - This signals the UI that the session is dead and the user must re-authenticate

  extraBufferCapacity = 1 ensures tryEmit never drops the event even if no collector is active yet. SharedFlow (not StateFlow) because this is a one-shot event, not state.
  </action>
  <verify>
  Project compiles: run `./gradlew :server:core:config:compileKotlin` and `./gradlew :core:sdk:compileKotlin` (or equivalent module tasks). Verify no compilation errors.
  </verify>
  <done>
  - Env.kt ACCESS_TOKEN_EXPIRY = 86400000L (1 day), REFRESH_TOKEN_EXPIRY = 2592000000L (30 days)
  - AuthInterceptor exposes sessionExpired: SharedFlow<Unit> that emits when refresh token fails
  </done>
</task>

<task type="auto">
  <name>Task 2: Observe session expiry in AppNavHost and force navigate to login</name>
  <files>
    composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
  </files>
  <action>
  1. In AppNavHost.kt, inject AuthInterceptor from Koin:
     - Add `val authInterceptor = koinInject<AuthInterceptor>()`
     - AuthInterceptor is already registered in Koin's sdkModule (it's injected into HttpClient setup)

  2. Add a LaunchedEffect that collects the sessionExpired flow:
     ```
     LaunchedEffect(Unit) {
         authInterceptor.sessionExpired.collect {
             navController.navigate(LoginRoute) {
                 popUpTo(0) { inclusive = true }
             }
         }
     }
     ```
     Place this AFTER the existing startup LaunchedEffects (token check and OAuth callback) so it does not interfere with startup navigation logic. The `popUpTo(0) { inclusive = true }` clears the entire back stack so the user cannot press back to reach the dashboard.

  3. Add necessary imports: the AuthInterceptor import from com.m2f.template.sdk.AuthInterceptor

  NOTE: Do NOT add a snackbar/toast for "Session expired" -- the plan description says optional and there is no snackbar infrastructure currently in AppNavHost. The navigation to login is sufficient UX signal.
  </action>
  <verify>
  Run `./gradlew :composeApp:compileKotlinDesktop` (or whichever compileKotlin task covers commonMain). Verify no compilation errors. Check that AuthInterceptor is resolvable from Koin by searching the DI module for its registration.
  </verify>
  <done>
  - AppNavHost collects AuthInterceptor.sessionExpired and navigates to LoginRoute with full stack clear
  - Compilation passes on all targets
  </done>
</task>

</tasks>

<verification>
1. Token lifetime defaults: grep Env.kt for 86400000L and 2592000000L
2. Session expiry flow: grep AuthInterceptor.kt for "sessionExpired" and "tryEmit"
3. Navigation observer: grep AppNavHost.kt for "sessionExpired" and "popUpTo(0)"
4. Full build: `./gradlew build` passes (or at minimum compileKotlin for affected modules)
</verification>

<success_criteria>
- Access token default is 1 day (86400000ms), refresh token default is 30 days (2592000000ms)
- Both remain configurable via JWT_ACCESS_EXPIRY and JWT_REFRESH_EXPIRY environment variables
- When AuthInterceptor's refresh attempt fails (401 or exception), it emits on sessionExpired flow
- AppNavHost observes sessionExpired and navigates to LoginRoute with popUpTo(0) { inclusive = true }
- Project compiles without errors
</success_criteria>

<output>
After completion, create `.planning/quick/25-extend-auth-token-lifetime-to-1-month-an/25-SUMMARY.md`
</output>
