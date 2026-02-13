---
status: diagnosed
trigger: "Dashboard sidebar navigation navigates to entirely new screens instead of updating the main content area while keeping the sidebar visible. The main content area is also not scrollable. Profile screen navigates away from the dashboard shell entirely, with no back navigation."
created: 2026-02-13T00:00:00Z
updated: 2026-02-13T00:00:00Z
---

## Current Focus

hypothesis: CONFIRMED - Three distinct architectural issues identified
test: Complete code trace through all navigation files
expecting: N/A - diagnosis complete
next_action: Return diagnosis

## Symptoms

expected: Sidebar nav items should swap out the main content section while the sidebar remains visible. Content should be scrollable. Profile should either be inside the dashboard shell or have back navigation.
actual: Dashboard sidebar navigation navigates to entirely new screens instead of updating the main content area while keeping the sidebar visible. The main content area is also not scrollable. Profile screen navigates away from the dashboard shell entirely, with no back navigation.
errors: None reported
reproduction: Click any sidebar navigation item in the dashboard
started: Unknown

## Eliminated

## Evidence

- timestamp: 2026-02-13T00:01:00Z
  checked: AppNavHost.kt - navigation graph structure
  found: All routes (DashboardRoute, ProcessesRoute, LogsRoute, DeploymentsRoute, SettingsRoute, ProfileRoute) are registered as SIBLING composable destinations in a single flat NavHost. There is no nested navigation graph for dashboard sub-screens.
  implication: Every sidebar navigation action performs a full top-level route change, completely replacing DashboardScreen (including its sidebar) with a new screen.

- timestamp: 2026-02-13T00:02:00Z
  checked: AppNavHost.kt lines 144-161 - DashboardScreen wiring
  found: DashboardScreen receives callbacks like `onNavigateToProcesses = { navController.navigate(ProcessesRoute) }`. These use the ROOT navController to navigate to top-level routes. ProcessesRoute, LogsRoute, DeploymentsRoute, SettingsRoute are all top-level PlaceholderScreen composables that fill the entire screen with no sidebar.
  implication: Clicking "processes" in the sidebar calls `navController.navigate(ProcessesRoute)` which replaces the entire DashboardScreen with a PlaceholderScreen. The sidebar is gone.

- timestamp: 2026-02-13T00:03:00Z
  checked: DashboardScreen.kt lines 76-89 - DesktopDashboard onNavItemSelected callback
  found: The callback both (1) calls `onNavItemSelected(item)` to update ViewModel state AND (2) immediately triggers navigation via `onNavigateToProcesses()` / `onNavigateToLogs()` etc. for any non-"dashboard" item. This means selecting any sidebar item except "dashboard" navigates away.
  implication: The sidebar selection state update is pointless because the user immediately leaves the dashboard screen. The architecture treats sub-screens as separate destinations instead of content within the dashboard shell.

- timestamp: 2026-02-13T00:04:00Z
  checked: DashboardScreen.kt lines 126-131 - Desktop content area scroll
  found: The desktop layout Column at line 126 DOES have `.verticalScroll(rememberScrollState())`. The mobile layout Column at line 224 also has `.verticalScroll(rememberScrollState())`.
  implication: The content area IS scrollable in the DashboardScreen itself. However, the PlaceholderScreen destinations (ProcessesRoute etc.) that replace it do NOT have scroll - they use a centered Box with fillMaxSize. If the "not scrollable" complaint refers to the placeholder screens, those are indeed not scrollable. If it refers to dashboard home content, scrolling IS wired but may be constrained by layout issues (the inner Row at line 140 with a fixed 340.dp column could cause horizontal overflow on narrower desktop widths, and the table content could exceed available space).

- timestamp: 2026-02-13T00:05:00Z
  checked: AppNavHost.kt lines 163-184 - ProfileRoute
  found: ProfileRoute is a top-level composable destination. When `onProfileClick` fires, it calls `navController.navigate(ProfileRoute)` which replaces DashboardScreen entirely. ProfileScreen renders its own sidebar (ProfileSidebar) and its own layout - it is a completely separate screen.
  implication: Profile is not inside the dashboard shell. It navigates away from dashboard entirely.

- timestamp: 2026-02-13T00:06:00Z
  checked: ProfileScreen.kt lines 91-101 and 185-207 - back navigation
  found: Desktop DesktopProfile layout has NO back button and no onBack callback wired. Mobile MobileProfile layout DOES have a "< back" button at line 196 that calls onBack. The onBack is wired to `navController.popBackStack()` in AppNavHost line 174.
  implication: On desktop, there is no way to navigate back from the profile screen to the dashboard. Only mobile has back navigation. Desktop users are stranded on the profile screen with only logout as an option.

- timestamp: 2026-02-13T00:07:00Z
  checked: PlaceholderScreen composable in AppNavHost.kt lines 236-289
  found: Each placeholder screen (Processes, Logs, Deployments, Settings) renders as a full-screen Box with a TerminalCard and a "Back" button. These are standalone screens with no sidebar, no dashboard shell context.
  implication: The placeholder screens are architecturally wrong for a sidebar-based dashboard. They should be content panels rendered WITHIN the dashboard shell, not top-level destinations.

- timestamp: 2026-02-13T00:08:00Z
  checked: DashboardBottomNav.kt - mobile bottom nav
  found: Mobile bottom nav has same issue: tabs for "processes", "logs", "settings" trigger the same navigation callbacks which call navController.navigate() to top-level routes, navigating away from the dashboard shell.
  implication: Both desktop sidebar and mobile bottom nav share the same bug pattern.

## Resolution

root_cause: |
  THREE INTERRELATED ARCHITECTURAL ISSUES:

  1. FLAT NAVIGATION GRAPH (PRIMARY): All screens (Dashboard, Processes, Logs, Deployments, Settings, Profile) are sibling destinations in a single flat NavHost in AppNavHost.kt. Sidebar/bottom-nav items trigger `navController.navigate(ProcessesRoute)` etc. on the ROOT NavController, which replaces the entire DashboardScreen (including its sidebar) with a new full-screen destination. The correct architecture would use either:
     (a) A nested NavHost inside DashboardScreen's content area (the Column at line 126), with its own NavController, so sidebar remains while content swaps. OR
     (b) State-based content switching inside DashboardScreen using `selectedNavItem` to render different content composables inline (no navigation at all for dashboard sub-sections).

  2. PROFILE HAS NO DESKTOP BACK NAVIGATION: ProfileRoute is a top-level destination. DesktopProfile (ProfileScreen.kt line 120-166) does not wire an onBack callback or render any back affordance. Only MobileProfile has "< back". Desktop users cannot return to the dashboard from profile except by logging out.

  3. SCROLL ISSUE IS SECONDARY: DashboardScreen's own content areas DO have verticalScroll wired. The perceived "not scrollable" issue likely stems from: (a) the PlaceholderScreens that replace the dashboard have no scroll, (b) the desktop layout's inner Row (line 140) with a hardcoded 340.dp right column could cause layout overflow on medium-width screens, making the content area appear unscrollable when content is clipped.

fix:
verification:
files_changed: []
