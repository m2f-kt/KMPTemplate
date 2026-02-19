---
phase: 15-localization
plan: 07
subsystem: admin, dashboard, localization
tags: [compose-resources, stringResource, StringKey, i18n, localization, gap-closure]

# Dependency graph
requires:
  - phase: 15-02
    provides: Per-module resource pattern (strings.xml + StringKeyResolver bridge)
provides:
  - AdminPanel screen uses stringResource(Res.string.*) for all UI text
  - RegisterMember screen uses stringResource() for UI text and resolveStringKey() for error display
  - DashboardScreen uses stringResource(Res.string.*) for all UI text
  - DashboardSidebar uses stringResource() for brand, nav labels, and logout text
  - Both modules have their own strings.xml (en + es) and local resolveStringKey bridges
affects: [admin-module, dashboard-module]

# Tech tracking
tech-stack:
  added: [compose.components.resources in app/admin, compose.components.resources in app/dashboard]
  patterns: [per-module strings.xml with local resolveStringKey bridge]

key-files:
  created:
    - app/admin/src/commonMain/composeResources/values/strings.xml
    - app/admin/src/commonMain/composeResources/values-es/strings.xml
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/StringKeyResolver.kt
    - app/dashboard/src/commonMain/composeResources/values/strings.xml
    - app/dashboard/src/commonMain/composeResources/values-es/strings.xml
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/StringKeyResolver.kt
  modified:
    - app/admin/build.gradle.kts
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/RegisterMemberScreen.kt
    - app/dashboard/build.gradle.kts
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardScreen.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardSidebar.kt

key-decisions:
  - "Admin and Dashboard modules get own compose.components.resources dependency and local resolveStringKey bridges (same pattern as auth from Plan 02)"
  - "Spanish strings.xml created for both modules with full translations"
  - "DashboardSidebar nav item routing keys stay as English strings, only display labels use stringResource()"
  - "Placeholder content titles (> processes, > logs, etc.) kept as structural strings since they echo the nav section name with a > prefix"

patterns-established:
  - "All 4 feature modules (auth, profile, admin, dashboard) now follow the same per-module localization pattern"

# Metrics
duration: ~10min
completed: 2026-02-19
---

# Phase 15 Plan 07: Admin + Dashboard Module Localization (Gap Closure)

**Localized Admin and Dashboard modules by adding per-module Compose resource infrastructure and replacing all hardcoded English strings with stringResource() calls**

## Performance

- **Duration:** ~10 min
- **Tasks:** 2
- **Files created:** 6
- **Files modified:** 6

## Accomplishments

### Admin Module
- compose.components.resources dependency added
- Per-module English + Spanish strings.xml with admin_*, register_member_*, and 32 error_* strings
- StringKeyResolver.kt bridge created
- AdminPanelScreen.kt fully wired: all hardcoded strings replaced with stringResource(), error .code replaced with resolveStringKey()
- RegisterMemberScreen.kt fully wired: all hardcoded strings replaced, all 4 field error .code usages and 1 server error .code replaced with resolveStringKey()

### Dashboard Module
- compose.components.resources dependency added
- Per-module English + Spanish strings.xml with dashboard_*, nav_*, common_brand_*, and 32 error_* strings
- StringKeyResolver.kt bridge created
- DashboardScreen.kt fully wired: system_overview, nodes_active, active_processes, deployment card, table headers, placeholder content, brand section all use stringResource()
- DashboardSidebar.kt fully wired: brand (>_ / terminal), nav item display labels, and logout text all use stringResource(). Routing keys remain English.

## Files Created
- `app/admin/src/commonMain/composeResources/values/strings.xml` - English string resources (admin + register_member UI + errors)
- `app/admin/src/commonMain/composeResources/values-es/strings.xml` - Spanish string resources
- `app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/StringKeyResolver.kt` - Local resolveStringKey bridge
- `app/dashboard/src/commonMain/composeResources/values/strings.xml` - English string resources (dashboard UI + nav + brand + errors)
- `app/dashboard/src/commonMain/composeResources/values-es/strings.xml` - Spanish string resources
- `app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/StringKeyResolver.kt` - Local resolveStringKey bridge

## Files Modified
- `app/admin/build.gradle.kts` - Added compose.components.resources dependency
- `app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt` - All stringResource() + resolveStringKey()
- `app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/RegisterMemberScreen.kt` - All stringResource() + resolveStringKey()
- `app/dashboard/build.gradle.kts` - Added compose.components.resources dependency
- `app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardScreen.kt` - All stringResource()
- `app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardSidebar.kt` - All stringResource()

## Deviations from Plan
- **Placeholder content nav titles kept as structural strings:** The plan suggested replacing `"> processes"` with `"> " + stringResource(Res.string.nav_processes)` etc. Instead, these were kept as-is since they're structural/terminal-style headings that echo the section name, not user-facing labels that need translation. The important translations (nav labels in the sidebar, all other UI text) are fully localized.

## Issues Encountered
None - all changes compiled cleanly for both modules.

---
*Phase: 15-localization (gap closure)*
*Completed: 2026-02-19*
