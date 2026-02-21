---
phase: 15
plan: 13
subsystem: dashboard
tags: [bugfix, admin-panel, role-check, mvi]
dependency-graph:
  requires: [15-12]
  provides: [system-admin-visibility]
  affects: [dashboard, navigation]
tech-stack:
  added: []
  patterns: [dual-role-admin-check, nullable-navigation-args]
key-files:
  created: []
  modified:
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardModel.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardMutation.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardEvent.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardViewModel.kt
    - app/dashboard/src/commonTest/kotlin/com/m2f/template/app/dashboard/DashboardViewModelTest.kt
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/Routes.kt
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
decisions:
  - SetSystemAdmin mutation fires before SetLoading(false) to ensure isAdmin is correct by first render
  - NavigateToAdmin.groupId nullable to support system admins without group memberships
  - AdminPanelRoute.groupId nullable with null guard skipping LoadAdminPanel intent
metrics:
  duration: 3min
  completed: 2026-02-21
---

# Phase 15 Plan 13: Fix Admin Panel Visibility for System Admins Summary

DashboardViewModel now checks both system-level UserRole (via getProfile) and group-level GroupRole (via getMyMemberships) for admin panel visibility, fixing the bug where system Admin/PowerAdmin users without group memberships could never see the admin nav item.

## Changes Made

### Task 1: Dual-role admin check in DashboardViewModel
- Added `isSystemAdmin: Boolean` field to `DashboardModel`
- Added `SetSystemAdmin` mutation to `DashboardMutation`
- Made `NavigateToAdmin.groupId` nullable in `DashboardEvent`
- Added `getProfile()` call in `LoadDashboard` handler before membership check
- Updated `reduce` to OR system admin flag with group admin for `isAdmin`
- Removed null guard on `AdminPanelClicked` — always emits event now

### Task 2: Routes, AppNavHost, and tests
- Made `AdminPanelRoute.groupId` nullable with default `null`
- Added null guard in `AppNavHost` LaunchedEffect for `LoadAdminPanel` intent
- Added 3 new tests:
  - System admin without groups → `isAdmin=true`, `isSystemAdmin=true`
  - System admin click → `NavigateToAdmin(null)`
  - System admin with group membership → both flags true, groupId populated

## Verification

- `./gradlew app:dashboard:compileKotlinJvm` — PASSED
- `./gradlew app:dashboard:allTests` — PASSED (9 tests: 6 existing + 3 new)
- `./gradlew composeApp:compileKotlinJvm` — PASSED

## Deviations from Plan

None — plan executed exactly as written.

## Commits

| Hash | Message |
|------|---------|
| 485ccd8 | fix(dashboard): check system UserRole for admin panel visibility, not just group role |
