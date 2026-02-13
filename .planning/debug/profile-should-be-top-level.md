---
status: diagnosed
trigger: "Profile screen embedded inside dashboard content area instead of being a top-level route. Double-sidebar effect."
created: 2026-02-13T21:00:00Z
updated: 2026-02-13T21:00:00Z
---

## Current Focus

hypothesis: 05-08 gap closure intentionally embedded ProfileScreen as a composable slot inside DashboardScreen, creating a double-sidebar (DashboardSidebar + ProfileSidebar both visible)
test: Read all wiring from AppNavHost through DashboardScreen to ProfileScreen
expecting: Confirmation that profile is rendered inside the dashboard shell instead of as a top-level route
next_action: Document findings and required changes (research only, no code changes)

## Symptoms

expected: Profile navigates to a full-screen top-level route (replacing the dashboard entirely). ProfileSidebar is the only sidebar visible.
actual: Profile renders inside the dashboard content area. On desktop: DashboardSidebar (260dp, left) + ProfileSidebar (260dp, nested inside content area) = double sidebar. On mobile: dashboard back-button header wraps profile's own back-button header.
errors: No runtime errors -- this is a UX/architecture bug
reproduction: Click user avatar or profile link in dashboard sidebar. Profile appears nested inside dashboard instead of replacing it.
started: Introduced by commit ddabc11 and 542d3fe (05-08 gap closure)

## Eliminated

(none -- root cause is clear from code reading)

## Evidence

- timestamp: 2026-02-13T21:00:00Z
  checked: AppNavHost.kt (lines 122-160) -- DashboardRoute composable
  found: |
    ProfileViewModel is created INSIDE the DashboardRoute composable block (line 125-126).
    ProfileScreen is passed as a `profileContent` lambda to DashboardScreen (lines 138-149).
    This means ProfileScreen renders as a child of DashboardScreen, not as a peer route.
    The `onBack` callback calls `dashboardViewModel.hideProfile()` (line 147) -- state-based toggle, not navigation.
  implication: Profile is wired as a composable slot inside dashboard, not as a navigation destination.

- timestamp: 2026-02-13T21:00:00Z
  checked: AppNavHost.kt (lines 162-183) -- ProfileRoute composable
  found: |
    A standalone `composable<ProfileRoute>` block STILL EXISTS (lines 162-183).
    It creates its own ProfileViewModel, renders ProfileScreen, and uses `navController.popBackStack()` for onBack.
    This route is a proper top-level destination but is NEVER navigated to from the dashboard.
  implication: The correct top-level ProfileRoute infrastructure already exists but is orphaned. Nothing navigates to it.

- timestamp: 2026-02-13T21:00:00Z
  checked: DashboardScreen.kt -- DesktopDashboard (lines 99-216)
  found: |
    `profileContent` lambda parameter accepted by DashboardScreen (line 66).
    When `state.showProfile == true` (line 117), the desktop layout renders:
      - DashboardSidebar (260dp) -- always visible (line 109-115)
      - A Column containing a "< back" button + profileContent() (lines 119-143)
    ProfileScreen's DesktopProfile layout (ProfileScreen.kt line 137) renders its OWN ProfileSidebar (260dp).
    Result: DashboardSidebar (260dp) | ProfileSidebar (260dp) | Profile content -- DOUBLE SIDEBAR.
  implication: This is the exact cause of the double-sidebar. The dashboard shell stays visible and profile adds its own sidebar inside the content area.

- timestamp: 2026-02-13T21:00:00Z
  checked: DashboardScreen.kt -- MobileDashboard (lines 269-405)
  found: |
    When `state.showProfile == true` (line 281), mobile layout renders:
      - A "< back" header row from DashboardScreen (lines 283-296)
      - profileContent() filling remaining space (lines 303-305)
    ProfileScreen's MobileProfile layout (ProfileScreen.kt lines 197-253) renders its OWN "< back" header.
    Result: Two back-button headers stacked. Bottom nav is hidden but the wrapper headers double up.
  implication: Mobile also has duplicated chrome (two back headers) due to the embedding approach.

- timestamp: 2026-02-13T21:00:00Z
  checked: DashboardViewModel.kt (lines 34-40)
  found: |
    showProfile() sets `showProfile = true` and `selectedNavItem = "profile"` (line 35).
    hideProfile() sets `showProfile = false` and `selectedNavItem = "dashboard"` (line 39).
    selectNavItem() also sets `showProfile = false` (line 31).
    Profile visibility is managed by dashboard state, not by the navigation system.
  implication: Profile show/hide is state-driven inside dashboard, bypassing the navController entirely.

- timestamp: 2026-02-13T21:00:00Z
  checked: DashboardState.kt
  found: |
    `showProfile: Boolean = false` field exists (line 11).
    This field is solely used for the embedded profile approach.
  implication: This field should be removed when profile becomes a top-level route again.

- timestamp: 2026-02-13T21:00:00Z
  checked: Routes.kt
  found: |
    `ProfileRoute` data object still exists (line 15).
    The route definition is already in place -- no new route needed.
  implication: Route infrastructure is ready; only the navigation wiring needs to change.

- timestamp: 2026-02-13T21:00:00Z
  checked: DashboardSidebar.kt (lines 47-54)
  found: |
    DashboardSidebar has `onProfileClick` callback (line 52), used on the user row at bottom (line 136).
    Currently wired to `onShowProfile` which sets `showProfile = true` in DashboardViewModel.
  implication: This callback needs to be rewired to `navController.navigate(ProfileRoute)` instead.

## Resolution

root_cause: |
  The 05-08 gap closure (commits ddabc11, 542d3fe) embedded ProfileScreen as a composable slot
  inside DashboardScreen. On desktop, this creates a double-sidebar layout because DashboardSidebar
  (260dp) remains visible while ProfileScreen renders its own ProfileSidebar (260dp) inside the
  dashboard content area. The existing top-level `composable<ProfileRoute>` in AppNavHost is
  orphaned -- nothing navigates to it.

fix: (not applied -- research only, see "Required Changes" below)
verification: (not applied)
files_changed: []

---

## Investigation: How Profile Is Currently Wired (05-08 Slot Injection)

### Flow diagram

```
User clicks avatar/profile link
  -> DashboardSidebar.onProfileClick
  -> DashboardViewModel.showProfile()
  -> DashboardState.showProfile = true, selectedNavItem = "profile"
  -> DashboardScreen checks state.showProfile
  -> Desktop: DashboardSidebar (260dp) + Column(backBtn + profileContent())
  -> profileContent() = ProfileScreen (injected from AppNavHost)
  -> ProfileScreen.DesktopProfile renders ProfileSidebar (260dp) + profile content
  -> RESULT: DashboardSidebar | ProfileSidebar | profile content (DOUBLE SIDEBAR)
```

### The composable slot injection pattern (AppNavHost.kt lines 122-160)

```kotlin
composable<DashboardRoute> {
    val dashboardViewModel = koinViewModel<DashboardViewModel>()
    val profileViewModel = koinViewModel<ProfileViewModel>()     // <-- created here
    val profileState by profileViewModel.state.collectAsStateWithLifecycle()

    DashboardScreen(
        state = dashboardState,
        onShowProfile = { dashboardViewModel.showProfile() },    // <-- state toggle
        onHideProfile = { dashboardViewModel.hideProfile() },
        profileContent = {                                       // <-- SLOT INJECTION
            ProfileScreen(
                state = profileState,
                onBack = { dashboardViewModel.hideProfile() },   // <-- state toggle, not nav
            )
        },
    )
}
```

### The orphaned top-level route (AppNavHost.kt lines 162-183)

```kotlin
composable<ProfileRoute> {                                       // <-- EXISTS but unused
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onBack = { navController.popBackStack() },               // <-- proper navigation
    )
}
```

---

## Required Changes to Make Profile a Top-Level Route Again

### 1. AppNavHost.kt -- Remove slot injection from DashboardRoute

**Remove from DashboardRoute composable block (lines 125-126, 131, 138-149):**
- Remove `ProfileViewModel` creation (line 125-126)
- Remove `onShowProfile` / `onHideProfile` callbacks (lines 131-132)
- Remove `profileContent` lambda (lines 138-149)
- Remove `LaunchedEffect(profileState.logoutTriggered)` block (lines 153-159)

**Rewire profile navigation to use navController:**
- Change `onShowProfile` to `{ navController.navigate(ProfileRoute) }`
- Remove `onHideProfile` entirely (not needed for top-level route)
- Remove `profileContent` parameter entirely

**Keep the existing `composable<ProfileRoute>` block (lines 162-183)** -- it is already correct.

**After changes, DashboardRoute should look like:**
```kotlin
composable<DashboardRoute> {
    val dashboardViewModel = koinViewModel<DashboardViewModel>()
    val dashboardState by dashboardViewModel.state.collectAsStateWithLifecycle()

    DashboardScreen(
        state = dashboardState,
        onNavItemSelected = dashboardViewModel::selectNavItem,
        onProfileClick = { navController.navigate(ProfileRoute) },
        onLogout = {
            navController.navigate(LoginRoute) {
                popUpTo(0) { inclusive = true }
            }
        },
    )
}
```

### 2. DashboardScreen.kt -- Remove profile embedding

**Remove from DashboardScreen signature:**
- `onShowProfile: () -> Unit` parameter
- `onHideProfile: () -> Unit` parameter
- `profileContent: @Composable () -> Unit` parameter

**Add to DashboardScreen signature:**
- `onProfileClick: () -> Unit` (single callback that triggers top-level navigation)

**Remove from DesktopDashboard:**
- `onShowProfile` / `onHideProfile` / `profileContent` parameters
- The entire `if (state.showProfile)` branch (lines 117-143) -- profile content block with back button
- Replace `onShowProfile` usage in DesktopHeader and DashboardSidebar with `onProfileClick`

**Remove from MobileDashboard:**
- `onShowProfile` / `onHideProfile` / `profileContent` parameters
- The entire `if (state.showProfile)` branch (lines 281-305) -- mobile profile with back button header
- Replace avatar `clickable(onClick = onShowProfile)` with `clickable(onClick = onProfileClick)`

### 3. DashboardViewModel.kt -- Remove profile state management

**Remove entirely:**
- `showProfile()` function (lines 34-36)
- `hideProfile()` function (lines 37-39)

**Modify:**
- `selectNavItem()` -- remove `showProfile = false` from the state update (line 31)

### 4. DashboardState.kt -- Remove showProfile field

**Remove:**
- `showProfile: Boolean = false` field (line 11)

### 5. No changes needed

- **Routes.kt** -- `ProfileRoute` already exists
- **ProfileScreen.kt** -- Already works as a standalone screen (has its own sidebar, back button, full layout)
- **ProfileSidebar.kt** -- No changes needed
- **DashboardSidebar.kt** -- The `onProfileClick` parameter already exists, just needs different wiring from AppNavHost

---

## Summary of Files to Modify

| File | Action |
|------|--------|
| `composeApp/.../AppNavHost.kt` | Remove profile slot injection from DashboardRoute; rewire to `navController.navigate(ProfileRoute)` |
| `app/dashboard/.../DashboardScreen.kt` | Remove `profileContent` slot, `onShowProfile`, `onHideProfile`; add `onProfileClick`; remove profile embedding branches |
| `app/dashboard/.../DashboardViewModel.kt` | Remove `showProfile()`, `hideProfile()` functions; remove `showProfile = false` from `selectNavItem()` |
| `app/dashboard/.../DashboardState.kt` | Remove `showProfile: Boolean = false` field |

## Files That Need NO Changes

| File | Why |
|------|-----|
| `Routes.kt` | `ProfileRoute` already defined |
| `ProfileScreen.kt` | Already works standalone with its own layout, sidebar, and back button |
| `ProfileSidebar.kt` | Independent component, no dashboard coupling |
| `DashboardSidebar.kt` | `onProfileClick` callback already exists in signature |
| `DashboardBottomNav.kt` | No profile-related code |
