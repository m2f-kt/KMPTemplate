---
status: resolved
trigger: "Admin panel issues — empty content, light theme, disappears after back navigation"
created: 2026-02-21T00:00:00Z
updated: 2026-02-21T00:01:00Z
---

## Current Focus

hypothesis: Three independent root causes confirmed
test: Code trace analysis complete
expecting: N/A
next_action: Report findings

## Symptoms

expected: Admin panel loads content, respects theme, persists in nav after back
actual: Empty content, light theme, disappears from nav
errors: None specified
reproduction: Navigate to admin panel (with null groupId), observe empty; check theme; navigate back and admin nav item vanishes
started: After Plan 15-13 update (nullable groupId)

## Eliminated

## Evidence

- timestamp: 2026-02-21T00:00:10Z
  checked: AppNavHost.kt lines 260-266
  found: When route.groupId is null, LoadAdminPanel is never dispatched — the if-block skips it entirely
  implication: System admin (no group) sees empty admin panel — initial state only

- timestamp: 2026-02-21T00:00:20Z
  checked: AdminPanelModel initial state
  found: All fields default to empty/false/0 — no content to render
  implication: Confirms empty screen for null groupId

- timestamp: 2026-02-21T00:00:30Z
  checked: AdminPanelScreen.kt root Column modifier
  found: No .background(colors.bg) on root — only .fillMaxSize().verticalScroll().padding(32.dp)
  implication: Transparent background falls through to platform default (white), making it look like light theme

- timestamp: 2026-02-21T00:00:35Z
  checked: DashboardScreen.kt root modifier
  found: DashboardScreen has .background(colors.bg) — admin screen is missing it
  implication: Inconsistency between screens

- timestamp: 2026-02-21T00:00:40Z
  checked: App.kt key(currentLocale) block
  found: key(currentLocale) wraps TerminalTheme { AppNavHost() } — destroys entire NavHost when locale changes
  implication: All NavBackStackEntries and ViewModels are destroyed on locale change

- timestamp: 2026-02-21T00:00:45Z
  checked: DashboardViewModel.LoadDashboard mutation ordering
  found: SetLoading(false) is sent BEFORE getMyMemberships() — isAdmin stays false until memberships load; errors silently ignored
  implication: Race condition + silent failure = admin nav item can permanently disappear after ViewModel recreation

## Resolution

root_cause: |
  Issue 1 (empty content): AppNavHost.kt null guard on route.groupId skips LoadAdminPanel intent entirely for system admins.
  Issue 2 (light theme): AdminPanelScreen.kt root Column lacks .background(colors.bg) modifier.
  Issue 3 (disappears after back nav): key(currentLocale) in App.kt destroys NavHost on locale change, recreating DashboardViewModel; LoadDashboard's SetLoading(false) fires before memberships load, and getMyMemberships() errors are silently ignored.

fix: Not yet applied (research-only mode)
verification: N/A
files_changed: []
