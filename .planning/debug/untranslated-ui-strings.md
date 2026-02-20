---
status: diagnosed
trigger: "Three UAT tests failed: Profile (Test 3), Dashboard (Test 5), Admin Panel (Test 6) — widespread untranslated strings in feature screens"
created: 2026-02-20T10:00:00Z
updated: 2026-02-20T10:30:00Z
---

## Current Focus

hypothesis: The localization gap closures (Plans 06/07) wired screen composables to stringResource() but the problem is THREE-FOLD: (A) string resource VALUES themselves are raw keys not human-readable text (e.g. EN value is "$ system_overview" instead of "System Overview"), (B) Profile sidebar nav items + tier content are fully hardcoded with no stringResource() at all, (C) DashboardMockData embeds English labels (UPTIME, REQUESTS) directly in data classes, and DashboardBottomNav has hardcoded labels.
test: Compare EN/ES values in strings.xml with what UAT tests expect; check composables for hardcoded strings
expecting: Exact list of untranslated strings across all 3 screens
next_action: Document complete diagnosis

## Symptoms

expected: All UI text on Profile, Dashboard, and Admin screens should appear in the selected locale (Spanish tested)
actual: Raw keys shown (e.g. "$ system_overview", "platform_stats", "user_directory"), English-only text (UPTIME, Build, etc.), sidebar nav items not translated
errors: UAT Tests 3, 5, 6 failed
reproduction: Switch locale to Spanish and view Profile (PowerAdmin tier), Dashboard, Admin Panel screens
started: After gap closure Plans 06 and 07

## Eliminated

(none — all hypotheses confirmed)

## Evidence

- timestamp: 2026-02-20T10:05:00Z
  checked: Dashboard EN strings.xml values
  found: Values ARE the raw keys themselves! E.g. dashboard_system_overview = "$ system_overview", dashboard_active_processes = "active_processes", dashboard_deployment_status = "deployment_status"
  implication: Even in English locale, users see raw underscore-style keys instead of human-readable text

- timestamp: 2026-02-20T10:07:00Z
  checked: Dashboard ES strings.xml values  
  found: Same pattern - values are Spanish-flavored raw keys: "procesos_activos", "estado_despliegue", "actividad_reciente". deployment_build/tests/deploy are STILL English "Build"/"Tests"/"Deploy"
  implication: Spanish translations never actually translated these strings into natural Spanish

- timestamp: 2026-02-20T10:10:00Z
  checked: DashboardMockData.kt metric labels
  found: Metric labels are hardcoded English in the data class: "UPTIME", "REQUESTS", "AVG LATENCY", "ERROR RATE". MetricCard renders metric.label directly — never goes through stringResource()
  implication: Stat cards can never be translated — labels come from mock data, not resources

- timestamp: 2026-02-20T10:12:00Z
  checked: DashboardMockData.kt activity titles
  found: Activity titles hardcoded: "deploy_v2.4.1", "high_memory_alert", "ssl_cert_renewed", "auto_scaling_up" — raw underscore-style English
  implication: Activity items untranslated

- timestamp: 2026-02-20T10:14:00Z
  checked: DashboardBottomNav.kt labels
  found: Bottom nav labels hardcoded: "home", "procs", "logs", "admin", "config" — no stringResource() calls
  implication: Mobile dashboard bottom nav never translated

- timestamp: 2026-02-20T10:16:00Z
  checked: ProfileSidebar.kt nav items (TierNavItems)
  found: ALL sidebar nav labels are hardcoded strings in Kotlin: "profile", "preferences", "billing", "team access", "user directory", "admin identity", "access matrix", etc. Zero stringResource() calls.
  implication: Profile sidebar nav items in ALL tiers are hardcoded English — can never be translated

- timestamp: 2026-02-20T10:18:00Z
  checked: ProfileSidebar.kt brand row & logout
  found: Brand text hardcoded ">_" and "profile" instead of stringResource(). Logout text hardcoded "$ logout"
  implication: Profile sidebar brand/logout not localized

- timestamp: 2026-02-20T10:20:00Z
  checked: ProfileSidebar.kt footer content (TierFooterContent)
  found: ALL footer strings hardcoded: "> upgrade available", "// unlock team access...", "> premium available", "✓ premium active", etc.
  implication: Sidebar footer content not localized

- timestamp: 2026-02-20T10:22:00Z
  checked: PowerAdminTierContent.kt
  found: ALL strings hardcoded: card titles ("platform_stats", "user_directory", "admin_identity", "access_matrix", "system_status"), descriptions, table headers ("ID","NAME","EMAIL","TIER","STATUS","RESOURCE","READ","WRITE","DELETE","ADMIN"), list items ("API Gateway","Database Primary"), alert title "danger_zone", card "destructive_actions", button texts ("purge","reset","logout all"), all descriptive text
  implication: PowerAdmin tier content has zero stringResource() calls

- timestamp: 2026-02-20T10:24:00Z
  checked: AdminTierContent.kt, FreeTierContent.kt, PaidTierContent.kt, PremiumTierContent.kt
  found: ALL tier content files have 100% hardcoded strings — zero stringResource() calls in any of them
  implication: Every tier content screen is completely unlocalized

- timestamp: 2026-02-20T10:26:00Z
  checked: Admin Panel EN strings.xml
  found: admin_title = "> admin_panel" (raw key), admin_table_* = "NAME"/"EMAIL"/"ROLE"/"JOINED" (English), admin_load_more = "load_more" (raw key), admin_register_member_button = "+ register_member" (raw key)
  implication: Even EN admin strings are raw keys, not natural language; table headers are English-only abbreviations

## Resolution

root_cause: THREE interrelated root causes across the 3 screens:

1. **String resource values are raw keys, not translations**: Plans 06/07 created strings.xml files but populated them with underscore-style identifiers as VALUES (e.g. EN: "$ system_overview", ES: "procesos_activos") instead of human-readable text (EN: "System Overview", ES: "Resumen del sistema"). This is a systematic error — the gap closures treated string keys AS the translations.

2. **ProfileSidebar, all 5 TierContent files, and DashboardBottomNav have zero stringResource() calls**: Plans 06/07 only wired DashboardScreen.kt, DashboardSidebar.kt, AdminPanelScreen.kt, and ProfileScreen.kt. They completely skipped: ProfileSidebar.kt (nav items, brand, footer), all 5 tier content files (FreeTierContent, PaidTierContent, PremiumTierContent, AdminTierContent, PowerAdminTierContent), and DashboardBottomNav.kt.

3. **DashboardMockData embeds English labels in data models**: Metric labels ("UPTIME", "REQUESTS", "AVG LATENCY", "ERROR RATE"), activity titles, and process statuses are hardcoded in DashboardMockData.kt. MetricCard, ActivityList, and ProcessTable render these model values directly without any string resource lookup.

fix: See detailed fix list below
verification: (pending fix)
files_changed: []
