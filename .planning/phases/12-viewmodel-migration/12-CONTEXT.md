# Phase 12: ViewModel Migration - Context

**Gathered:** 2026-02-18
**Status:** Ready for planning

<domain>
## Phase Boundary

Migrate all 5 existing ViewModels (Login, Register, ForgotPassword, Profile, Dashboard) from ad-hoc patterns to MVI using the MviViewModel base class from Phase 10. Replace boolean navigation flags with Channel-based effects. Each migrated ViewModel gets unit tests using the core:testing DSL. Old ViewModel patterns are removed after migration. No new features or behavior changes.

</domain>

<decisions>
## Implementation Decisions

### Effect vs State boundary
- Navigation is always a one-shot Effect (NavigateToDashboard, NavigateToLogin, etc.) — never state
- Login/auth success fires NavigateToDashboard immediately — no success state shown first
- Logout fires NavigateToLogin as a one-shot Effect — dashboard has no "logged out" state
- Validation and server errors are persistent Model state (shown inline, survive recomposition) — not toasts/effects
- Loading indicators use `isLoading` boolean in Model state — not sealed state variants

### Migration purity
- Pure mechanical migration — same behavior, new MVI pattern. No UX improvements during migration
- Both ViewModel and Composable sides change: ViewModel emits Effects, Composable collects via LaunchedEffect
- Old ViewModel base classes and legacy patterns removed after all 5 VMs are migrated (separate cleanup plan)
- Intent types use descriptive action-based names: SubmitLoginClicked, EmailChanged, ForgotPasswordRequested (reads like UI event log)

### Test depth
- Each VM gets happy path + key error scenario tests (not just happy path, not full comprehensive)
- Tests verify both Model state transitions AND Effect emissions (navigation, etc.)
- All 5 VM tests use the same fakeSdk {} + Turbine-based test DSL from core:testing — consistent pattern
- Existing LoginViewModel test from Phase 11.1 is rewritten to match the new depth standard

### Execution order
- LoginViewModel is the reference migration (already partially migrated in Phase 11.1) — complete it first
- Order after Login: Register → ForgotPassword → Profile → Dashboard (auth screens first, then others)
- One plan per ViewModel (5 plans) — each migrates + tests one ViewModel
- Plan 6: Final cleanup — remove old ViewModel patterns, unused state classes, dead code

### Claude's Discretion
- Exact Intent/Model/Event sealed class structure per ViewModel
- How to wire Koin injection for migrated ViewModels
- Composable-side implementation details for LaunchedEffect collection
- Which error scenarios to test per ViewModel

</decisions>

<specifics>
## Specific Ideas

- Login serves as the canonical reference — other 4 VMs should follow its exact structural pattern
- Auth screens (Login, Register, ForgotPassword) likely share similar patterns and can mirror each other closely
- Profile and Dashboard are different concerns but should still follow the same MVI structure

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 12-viewmodel-migration*
*Context gathered: 2026-02-18*
