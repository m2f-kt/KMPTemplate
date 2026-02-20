# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-17)

**Core value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.
**Current focus:** Phase 15 -- Localization

## Current Position

Phase: 15 (Localization) -- COMPLETE (verified)
Plan: 11 of 11 in current phase (all done)
Status: Phase 15 complete -- all 11 plans executed, verification PASSED (5/5 must-haves)
Last activity: 2026-02-20 -- Gap closure complete, verification passed

Progress: [████████████████████] 100% (11/11 plans)

## v1.0 Performance (archived)

Progress: [████████████████████] 100% (39/39 plans)

## Performance Metrics

**Velocity (v1.0):**
- Total plans completed: 39
- Average duration: ~8 min
- Total execution time: ~315 min

**By Phase (v1.1):**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 10. MVI ViewModel Foundation | 1 | 2min | 2min |
| 11. Testing Infrastructure | 3 | 24min | 8min |
| 11.1 Fake SDK Facade + Fixes | 2 | 6min | 3min |
| 12. ViewModel Migration | 7/7 | 34min | ~5min |
| Phase 12 P04 | 22min | 2 tasks | 14 files |
| Phase 12 P07 | 9min | 1 task | 1 file |
| 13. Group Server & SDK | 4/4 | ~25min | ~6min |
| 14. Group Admin UI | 4/4 | ~34min | ~9min |
| 15. Localization P01 | 3 tasks | 4min | 3 files |
| 15. Localization P04 | 2 tasks | 2min | 5 files |
| 15. Localization P03 | 2 tasks | 11min | 15 files |

| 15. Localization P05 | 2 tasks | 4min | 11 files |
| 15. Localization P08 | 2 tasks | 1min | 6 files |
| Phase 15-localization P11 | 2min | 2 tasks | 6 files |
| Phase 15-localization P09 | ~15min | 2 tasks | 13 files |
| Phase 15-localization P10 | 2min | 2 tasks | 4 files |

## Accumulated Context

### Decisions

- Phase 10: extraBufferCapacity=64 on MviViewModel pipeline to prevent emit suspension before collectors ready
- Phase 10: reduce is suspend per user decision (diverges from Airalo reference)
- Phase 10: koin-core kept as implementation dependency (not exposed to consumers)
- Phase 11: Interface gets clean name (AuthApi), Impl suffix for concrete class (AuthApiImpl)
- Phase 11: SdkModule Koin bindings use interface type qualifiers (single<AuthApi>) for correct DI resolution
- Phase 11: Added kotlin-test-junit JVM dependency for @BeforeTest/@AfterTest resolution in commonMain test library
- Phase 11: Statement queuing pattern for ViewModel test DSL (intent/model/event calls build list, runner processes sequentially)
- Phase 11: Initial StateFlow emission auto-consumed in DSL so test authors only see state changes
- Phase 11: MviViewModel.test{} DSL sets Dispatchers.Main to UnconfinedTestDispatcher(testScheduler) for scheduler alignment
- Phase 11: advanceUntilIdle() after each intent dispatch in DSL ensures viewModelScope coroutines complete
- Phase 11.1: FakeSdkBuilder uses direct Sdk constructor instead of subclassing (Sdk is final class)
- Phase 11.1: Android kotlin-test-junit declared separately from jvmMain (KMP androidTarget is independent compilation unit)
- Phase 11.1: No AppModule.kt change needed -- Koin viewModelOf auto-resolves Sdk from sdkModule
- Phase 11.1: mvi-viewmodel skill mandates Sdk as ViewModel dependency and fakeSdk {} as test entry point
- Phase 12: StateFlow conflation -- sync fake SDK causes isLoading=true to be conflated with SetServerError in error path; test expects final state only
- Phase 12: LoginScreen parameter stays state: LoginModel (not model) to minimize churn in composable body references
- Phase 12: RegisterScreen parameter stays state: RegisterModel (matching Login pattern) to minimize churn
- Phase 12: StateFlow conflation pattern confirmed reusable across all ViewModel error-path tests with sync fakes
- Phase 12: emailSent stays as Model field (not Event) because user remains on screen to see success message
- Phase 12: ForgotPasswordEvent is empty sealed interface -- no ViewModel-driven navigation for this screen
- Phase 12: StateFlow conflation also affects success path when both SetLoading and SetEmailSent are Mutations (unlike Login where success emits Event)
- Phase 12: DashboardEvent is empty sealed interface -- no ViewModel-driven navigation for dashboard screen
- Phase 12: Init-block auto-dispatched intents require explicit intent() call in test DSL (init coroutine orphaned on setUp's StandardTestDispatcher)

Decisions are logged in PROJECT.md Key Decisions table.
- [Phase 12]: SharingStarted.Eagerly for init-dispatching ViewModels: WhileSubscribed loses mutations emitted before first subscriber; use Eagerly + eager model/event access in init block
- [Phase 12]: turbineScope testing for init-dispatch VMs: MviViewModel.test{} DSL incompatible with init { take() }; use UnconfinedTestDispatcher + turbineScope { testIn(backgroundScope) } directly
- [Phase 12-07]: SharingStarted.Eagerly VMs (ProfileViewModel) auto-dispatch init intents in DSL tests; do NOT use explicit intent(LoadProfile) -- it causes duplicate coroutine interference. WhileSubscribed VMs (DashboardViewModel) still need explicit intent().
- Phase 14: MembershipSummary on UserApi (not GroupApi) -- endpoint is /api/users/me/memberships, user-scoped data
- Phase 14: getMyMemberships route in GroupRoutes.kt despite being user-scoped -- needs GroupService access
- Phase 14: getMyMemberships has no Raise<DomainError> context -- empty list is valid result
- Phase 14: AdminPanelClicked as separate intent rather than overloading NavItemSelected("admin") -- cleaner separation
- Phase 14: Silent failure on membership load (ifLeft no-op) -- user may not be in any group
- Phase 14: AdminPanelRoute/RegisterMemberRoute defined early in Routes.kt for compilation -- Plans 03/04 add handlers
- Phase 14: StateFlow conflation in admin membership test -- sync fakes conflate SetLoading and SetMembership
- Phase 14: AdminPanelViewModel uses Approach B (no init dispatch) -- groupId from route dispatched via LaunchedEffect in composable
- Phase 14: BadgeVariant.Error for error badges, BadgeVariant.Accent for member count (no Danger/Info variants exist in design system)
- Phase 14: StateFlow conflation pattern in admin panel tests -- assert only final settled state per intent dispatch with sync fakes
- Phase 14: Arrow zipOrAccumulate with withError field remapping for multi-field form validation (reusing validateName for firstName/lastName)
- Phase 14: TerminalBadge Success/Default variants for role selector toggle UI
- Phase 14: Field error clearing in reduce by map key removal on field change mutation
- Phase 15: resolveStringKey function name chosen over stringResource to avoid collision with org.jetbrains.compose.resources.stringResource
- Phase 15: StringKey enum entries use identical code strings for direct AppError.code mapping via fromCode()
- Phase 15: Error string resources use error_ prefix + lowercased code as naming convention
- Phase 15: StringKey is @Serializable for potential wire usage (server-sent error keys)
- Phase 15: ServerStrings keys match AppError.code values exactly for direct lookup
- Phase 15: preferredLanguage() takes first 2 chars of Accept-Language for ISO 639-1 extraction
- Phase 15: Validation errors use ServerStrings for base message but keep field-level detail in formattedErrors
- Phase 15: Feature modules use StringKey.code for error display (resolveStringKey inaccessible from feature modules)
- Phase 15: Dashboard module unchanged in Plan 03 — no error fields, no hardcoded error strings
- Phase 15: UI text left hardcoded in feature screens — Res.string inaccessible without architectural change, deferred to Plan 05
- Phase 15: java.util.Locale for Android/JVM locale switching (avoids AppCompat dependency, Compose Resources respects JVM default locale)
- Phase 15: LocaleSelector lives in composeApp, injected into ProfileScreen via composable slot — profile module stays dependency-free
- Phase 15: Locale change handled directly in composable (no ViewModel intent) — simple synchronous preference write + platform call
- Phase 15: WASM locale stored in memory only — page reload required for full Compose Resources locale switch
- [Phase 15-localization]: PreferencesStorage.language as locale source for Accept-Language header (core:sdk depends on core:storage)

- [Phase 15-localization]: DashboardMockData uses StringResource fields (labelRes/titleRes) for non-composable static data, resolved at render sites
- [Phase 15-localization]: key(currentLocale) forces full UI tree recomposition for reactive locale switching
- [Phase 15-localization]: ProfileRoute reads locale from CompositionLocal, writes only to PreferencesStorage

### Roadmap Evolution

- Phase 11.1 inserted after Phase 11: Fake SDK facade, fix Android compile, update mvi-viewmodel skill (URGENT)

### Blockers/Concerns

- RESOLVED: Turbine 1.2.1 + Kotlin 2.3.10 compatibility verified (compiles on all KMP targets including wasmJs)
- Research flags: Ktor testApplication dispatcher issue (KTOR-7121) needs spike in Phase 11
- Research flags: WASM async resource loading timing needs smoke test in Phase 15

## Session Continuity

Last session: 2026-02-20
Stopped at: Phase 15 gap closure complete -- verification passed (5/5 must-haves)
Resume file: .planning/phases/15-localization/15-VERIFICATION.md
