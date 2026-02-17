# Project Research Summary

**Project:** KMP Full-Stack Template — Milestone 2
**Domain:** Kotlin Multiplatform Full-Stack (Android/iOS/Desktop/WASM + Ktor Server)
**Researched:** 2026-02-17
**Confidence:** HIGH

## Executive Summary

Milestone 2 extends an already-substantial KMP full-stack template (Kotlin 2.3.10, Compose Multiplatform 1.10.1, Ktor 3.4.0, Exposed 1.0.0, Arrow 2.2.1.1) with four capability areas: a formal MVI ViewModel pattern, Groups/Admin management, a real testing infrastructure, and client/server localization. The existing codebase provides strong foundations — five working ViewModels, RBAC with three global roles, Arrow-based SDK, Koin DI across all targets — but has no formal MVI contract, zero real tests, no localization, and no group concept. Milestone 2 formalizes and completes what Milestone 1 sketched out.

The recommended approach is to build foundation-first: a lightweight `MviViewModel` base class (~30 lines using existing StateFlow + Channel primitives), a `core:testing` module with Turbine + manual fakes, then the `server:groups` feature module mirroring the `server:auth` pattern, and finally client-side Admin UI and localization wired to the new infrastructure. Only two new version catalog entries are needed (Turbine 1.2.1 and kotlinx-coroutines-test 1.10.2). No third-party MVI frameworks, no mocking libraries for shared code, no third-party localization libraries — everything uses existing stack primitives.

The dominant risks are architectural rather than technical: group authorization bypass (an admin of Group A accessing Group B's data is trivially easy to introduce), StateFlow conflation producing flaky Turbine tests, and MVI one-shot events firing twice if implemented with state flags instead of a Channel. All three are cheap to prevent and expensive to retrofit. These must be designed correctly from the start, not added later.

## Key Findings

### Recommended Stack

The milestone requires only two new library additions to an already-complete stack. Turbine 1.2.1 is the industry-standard Flow testing library for Kotlin and is required to make the MVI pattern meaningfully testable across KMP targets. The `kotlinx-coroutines-test` 1.10.2 module (same version as the existing coroutines runtime) provides `runTest` and `TestDispatcher` for deterministic coroutine testing. Everything else — MVI base class, group entity management, localization, RBAC extension — uses existing dependencies without change.

**Core technologies (existing, confirmed in `gradle/libs.versions.toml`):**
- `lifecycle-viewmodel-compose` 2.9.6: KMP ViewModel with `viewModelScope`, already used in 5 ViewModels
- `kotlinx-coroutines` 1.10.2: `StateFlow`, `Channel`, `MutableStateFlow` for MVI state machine
- Arrow 2.2.1.1: `Either<L,R>` and `Raise` context for typed error handling in intent handlers
- Exposed 1.0.0: New `GroupsTable` + `GroupMembersTable` following existing R2DBC DSL patterns
- Ktor 3.4.0: Type-safe `@Resource` routes and `withRole()` RBAC for group endpoints
- Koin 4.1.1: `viewModelOf()` injection unchanged; interfaces added to SDK classes for testability
- Compose Multiplatform 1.10.1: Built-in `composeResources/values-xx/strings.xml` for localization
- Kotest 6.1.3: Already in `testing-server` bundle; needs adding to `commonTest` via new bundle

**New additions (version catalog only):**
- Turbine 1.2.1: Flow/StateFlow testing in `commonTest` across all KMP targets
- kotlinx-coroutines-test 1.10.2: `runTest`, `TestDispatcher`, `advanceUntilIdle`
- New `testing-kmp` bundle in `libs.versions.toml` for `commonTest` dependencies

**What NOT to add:** Orbit MVI, MVIKotlin, MockK (no WASM/iOS support in `commonTest`), Moko Resources (superseded by CMP built-in), ktor-i18n (Ktor 2.x only, unmaintained), Room KMP, SQLDelight for client.

### Expected Features

**Must have (table stakes — this milestone):**
- `MviViewModel<S,I,E>` abstract base class with `StateFlow` state, `Channel` effects, `dispatch(intent)` entry point
- Intent/State/Effect sealed interfaces per ViewModel
- Migrate all 5 existing ViewModels (Login, Register, ForgotPassword, Profile, Dashboard) to MVI — template cannot contradict its own pattern
- MVI test DSL using Turbine: `viewModel.state.test { dispatch(intent); awaitItem() shouldBe ... }`
- `GroupsTable` + `GroupMembersTable` in Exposed, with group CRUD API (new `server:groups` module)
- Group membership management endpoints (add/remove/change role) with two-tier RBAC
- `GroupApi` SDK class following existing `AuthApi`/`UserApi` pattern
- Admin dashboard screen conditionally shown for `UserRole.Admin+`
- `composeResources/values/strings.xml` with all hardcoded strings extracted from composables and ViewModels
- `StringKey` enum in `core:l10n` — type-safe localization keys shared between client and server
- Server returns `code` fields in `ErrorResponse`; client maps codes to localized strings at UI layer
- ViewModel unit tests and server integration tests (auth flow + group CRUD + RBAC enforcement)

**Should have (competitive differentiators):**
- MVI test DSL packaged as reusable `core:testing` module with `ViewModelTestScope`
- Two-tier RBAC: global `UserRole` for platform access + per-group `GroupRole` (Owner/Admin/Member) at service layer
- `IntegrationTestBase` with Testcontainers PostgreSQL for real database integration tests
- Admin invitation flow (admin creates user pre-assigned to group with activation email)
- Second locale (Spanish) proving the localization pattern works across languages

**Defer to v2.x/v3+:**
- Runtime locale switching without restart (requires platform-specific locale override APIs)
- Group-scoped RBAC Ktor middleware (`withGroupRole()`)
- Compose UI snapshot tests (tooling immature across KMP targets)
- Permission matrix beyond fixed Owner/Admin/Member roles
- Multi-tenancy with schema-per-tenant isolation

### Architecture Approach

The architecture adds four new modules to the existing 12-module project. New modules follow established conventions: `core:viewmodel` (KMP library, MVI base class), `core:l10n` (pure Kotlin, zero Compose dependency, StringKey enum), `core:testing` (KMP library, fakes + DSL + Turbine wrappers), and `server:groups` (JVM server module mirroring `server:auth`). A new `app:admin` feature module houses admin UI. Critically, `core:l10n` has NO Compose dependency so server modules can use it for error message resolution. String resource XML files are centralized in `composeApp/composeResources/` rather than per-feature-module, preventing key collisions and giving translators a single source of truth.

**Major components:**
1. `core:viewmodel` — Abstract `MviViewModel<S,I,E>`; `StateFlow` for persistent UI state, `Channel(BUFFERED)` for one-shot effects via `sendEffect()`
2. `core:l10n` — `StringKey` enum with English defaults; bridge `@Composable fun localizedString(key)` in `composeApp` maps to `Res.strings.*`
3. `core:testing` — Turbine-based `ViewModelTestScope` DSL, `FakeAuthApi`/`FakeUserApi`/`FakeGroupApi`, Ktor `testApp()` helper (JVM-only in `jvmMain`)
4. `server:groups` — `GroupsTable`, `GroupMembersTable`, `GroupService` with `context(raise: Raise<DomainError>)` two-tier authorization, type-safe `@Resource` routes
5. `app:admin` — Admin panel UI using `TerminalTheme` tokens, role-gated in `AppNavHost.kt`, backed by `GroupApi`
6. SDK interface extraction — `AuthApiContract`, `UserApiContract`, `GroupApiContract` enable portable KMP fakes without MockK

**Data flow (MVI):** User action -> `dispatch(intent)` -> `handleIntent()` suspend function -> `setState { reducer }` for persistent state OR `sendEffect(effect)` for one-shot navigation/toast -> collected via `LaunchedEffect(Unit) { effects.collect { ... } }` in composable.

### Critical Pitfalls

1. **Group authorization bypass** — An admin of Group A can silently access Group B's data if repository queries filter by `userId` but not `groupId`. Prevention: include `group_id` in every SQL WHERE clause; verify group membership in service layer via `Raise<DomainError>` (not at the route level which has no DB access). Add explicit cross-group integration tests ("Admin of Group A gets 403 on Group B resources"). Recovery cost if missed: HIGH — every repository method must be audited, every test updated, schema migration needed.

2. **MVI one-shot events double-fire** — Boolean state flags (`loginSuccess: Boolean`) for navigation events fire twice on recomposition and are lost on process death. Prevention: use `Channel<E>(BUFFERED)` for all one-shot effects; consume via `LaunchedEffect(Unit) { effects.collect { ... } }`. Remove all existing boolean navigation flags from state classes during ViewModel migration.

3. **Turbine StateFlow conflation drops intermediate states** — StateFlow conflates: rapid emissions before the collector processes them collapse to the latest value. `awaitItem()` may skip `isLoading = true` and go directly to `isLoading = false`. Prevention: use `expectMostRecentItem()` for final-state assertions; use `UnconfinedTestDispatcher` to eagerly execute coroutines; validate tests pass with BOTH `Standard` and `Unconfined` dispatchers.

4. **Cascade delete propagates too far** — `ON DELETE CASCADE` on `group_members.user_id -> users.id` would delete users when removing group membership. Prevention: map cascade directions explicitly before writing table definitions. Deleting a group cascades to group_members (correct). Deleting a user cascades to group_members (correct). Neither propagates further. Prefer soft deletes (`is_active`) for groups to preserve audit trails.

5. **MockK unusable in `commonTest`** — MockK has no Kotlin/Native or wasmJs support. All `commonTest` code must use hand-written fakes. Prevention: extract `AuthApiContract`, `UserApiContract`, `GroupApiContract` interfaces from concrete SDK classes; create `FakeAuthApi`, `FakeUserApi`, `FakeGroupApi` in `core:testing`. This is prerequisite work that must happen before any ViewModel tests are written.

## Implications for Roadmap

Based on the dependency graph in FEATURES.md and the build order in ARCHITECTURE.md, a 7-phase structure is recommended. The MVI foundation must come first because everything else depends on it. Groups server work is independent of client work and can proceed in parallel once shared models exist. Testing infrastructure is scaffolded early but test authoring occurs alongside each feature.

### Phase 1: MVI ViewModel Foundation

**Rationale:** Five existing ViewModels already use StateFlow informally. A formal base class is the critical-path blocker — new Group management ViewModels and test infrastructure both depend on it. This must come first.
**Delivers:** `core:viewmodel` module with `MviViewModel<S,I,E>` abstract class; `dispatch()` single entry point; `setState(reducer)` + `sendEffect(effect)` pattern; `Channel(BUFFERED)` for one-shot effects
**Addresses:** MVI Base ViewModel class (P1 table stakes), Intent/State/Effect sealed interface convention
**Avoids:** One-shot event double-fire (Channel vs. state flags design established here), state mutation CAS races (single reducer function through `setState`)

### Phase 2: ViewModel Migration

**Rationale:** The template contradicts itself if it ships a new MVI base class while five existing ViewModels still use the old ad-hoc pattern. Migration before new feature ViewModels are written ensures the codebase demonstrates one consistent approach throughout.
**Delivers:** All 5 existing ViewModels (Login, Register, ForgotPassword, Profile, Dashboard) migrated to `MviViewModel`; boolean navigation flags (`loginSuccess`, `logoutTriggered`) replaced with Channel effects; composables updated to collect effects via `LaunchedEffect`
**Addresses:** "Migrate existing ViewModels to MVI" (P1 table stakes); removes navigation state flag anti-pattern
**Avoids:** One-shot events firing twice on recomposition, state mutation ordering race conditions

### Phase 3: Testing Infrastructure

**Rationale:** Tests require the MVI base class to target. Creating the testing scaffold (module + fakes + DSL) before feature phases ensures subsequent phases ship with tests rather than accumulating test debt.
**Delivers:** `core:testing` module; `AuthApiContract`/`UserApiContract` interface extraction from concrete SDK classes; `FakeAuthApi`, `FakeUserApi` implementations; Turbine-based `ViewModelTestScope` DSL; Ktor `testApp()` helper in `jvmMain`; `testing-kmp` bundle added to version catalog; LoginViewModel unit tests as working examples
**Uses:** Turbine 1.2.1 (new entry in version catalog), kotlinx-coroutines-test 1.10.2 (new entry), kotest 6.1.3 (already in catalog, added to commonTest)
**Avoids:** MockK in `commonTest`, concrete classes blocking testability across KMP targets

### Phase 4: Shared Models + SDK for Groups

**Rationale:** Client and server both depend on `core:models` for DTOs and route types. These must exist before either `server:groups` or `app:admin` can be built. Smallest meaningful unit that unblocks both work streams simultaneously.
**Delivers:** `GroupResponse`, `CreateGroupRequest`, `GroupMemberResponse`, `AddMemberRequest` DTOs in `core:models`; `Groups` `@Resource` route class; `AppError.Group` sealed subtypes (NotFound, NotMember, InsufficientRole, AlreadyMember); `GroupApiContract` interface; `GroupApi` SDK class; Koin binding for `GroupApi`
**Addresses:** Shared route definitions for groups (P1), GroupApi SDK class (P1)
**Implements:** `core:models` + `core:sdk` additions described in ARCHITECTURE.md

### Phase 5: Server Groups Module

**Rationale:** Server-side group management can proceed independently of client work once shared models exist. The `server:groups` module mirrors `server:auth` structure exactly, making implementation predictable.
**Delivers:** `server:groups` module; `GroupsTable`, `GroupMembersTable` in Exposed R2DBC; `GroupRepository`, `GroupMemberRepository`; `GroupService` with `context(raise: Raise<DomainError>)` two-tier authorization (platform role from JWT + group membership from DB); type-safe Ktor routes (CRUD + membership management + platform-Admin list-all); database migration; Koin module registration in `Application.kt`; Testcontainers integration tests covering auth flow + group CRUD + RBAC enforcement + cross-group 403 tests
**Addresses:** Group entity + CRUD API (P1), Group membership management (P1), Admin registers users into group (P1), Server integration tests (P1)
**Avoids:** Authorization bypass (group_id in every query), cascade delete data loss (cascade graph mapped before table creation), admin privilege escalation (role hierarchy check `callerRole.level > targetRole.level`)

### Phase 6: Admin UI + Group Client Features

**Rationale:** Client-side group features can only be built once the GroupApi SDK (Phase 4) and server routes (Phase 5) exist. Admin UI is role-gated and must use the existing `TerminalTheme` design system.
**Delivers:** `app:admin` module; `AdminViewModel` and `GroupManagementViewModel` (extending `MviViewModel`); admin dashboard screen showing group list + member counts; group CRUD composables; `AdminRoute` in navigation; role-gated navigation link in dashboard/profile; `FakeGroupApi` test fake; ViewModel unit tests using `core:testing` DSL
**Addresses:** Admin dashboard screen (P1), AdminRoute in navigation, Admin creates/manages groups
**Avoids:** Admin panel using `MaterialTheme` instead of `TerminalTheme` (explicit convention for all admin components)

### Phase 7: Localization

**Rationale:** Localization is architecturally independent but benefits from coming last so all string keys from all features can be cataloged at once, avoiding repeated edits to strings files as features are added.
**Delivers:** `core:l10n` module with `StringKey` enum and English defaults; `composeResources/values/strings.xml` with all hardcoded strings extracted; `composeResources/values-es/strings.xml` (Spanish example proving multi-locale); bridge `@Composable fun localizedString(key: StringKey)` in `composeApp`; ViewModels emitting `StringKey` instead of hardcoded English strings for validation errors; server `ErrorResponse` using `code` field for client-side localization
**Addresses:** Client-side string resources (P1), StringKey shared enum (P1), Server error code mapping (P1), Second locale (P2)
**Avoids:** Plural format specifiers broken on WASM/Desktop (`%1$d` not `%d`); `stringResource()` called outside `@Composable` context; strings scattered across per-feature-module `composeResources` directories

### Phase Ordering Rationale

- Phases 1-2 (MVI foundation + migration) must come first — the ViewModel pattern is the dependency all subsequent client feature work relies on. No new ViewModels should be written before the base class exists.
- Phase 3 (testing infrastructure) before feature phases — `core:testing` module creation and SDK interface extraction are prerequisite work. Feature phases should ship with tests, not accumulate them.
- Phase 4 (shared models/SDK) unblocks both Phase 5 (server) and Phase 6 (client) — small enough to be a standalone phase, critical enough to be its own phase.
- Phases 5 and 6 are partially parallelizable — server work (Phase 5) and client work (Phase 6) share only Phase 4 outputs as a hard dependency.
- Phase 7 (localization) last — all string keys across all features are known before strings files are created; prevents repeated edits as features are added.

### Research Flags

**Phases needing deeper research during planning:**
- **Phase 5 (Server Groups):** Two-tier authorization (global JWT role + group-scoped service-layer check) is the most complex new pattern. The exact interaction sequence between `withRole()` Ktor plugin and `context(raise: Raise<DomainError>)` group membership validation needs to be designed precisely before coding begins.
- **Phase 3 (Testing Infrastructure):** Ktor testApplication known issues (KTOR-6925, KTOR-7121) around real vs. test dispatchers need validation against Ktor 3.4.0 specifically. The `testApplication` + `runTest` incompatibility should be confirmed or denied with a spike test before designing the test base class.

**Phases with standard patterns (skip research-phase):**
- **Phase 1 (MVI Base Class):** Well-documented pattern; base class is ~30 lines and design is fully specified in ARCHITECTURE.md. No ambiguity.
- **Phase 2 (ViewModel Migration):** Mechanical work following the Phase 1 pattern; each ViewModel is independent.
- **Phase 4 (Shared Models/SDK):** Pure data class and sealed interface additions; follows existing `core:models` patterns exactly.
- **Phase 6 (Admin UI):** Standard Compose + MVI composition; `TerminalTheme` design system already in place.
- **Phase 7 (Localization):** CMP resource system is well-documented; two-layer architecture is fully specified. Only WASM async loading edge case needs a smoke test.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | All existing versions verified in `gradle/libs.versions.toml`. Turbine 1.2.1 confirmed KMP multiplatform across JVM, JS, wasmJs, Native. One LOW-confidence item: Turbine 1.2.1 + Kotlin 2.3.10 exact compatibility not documented by Turbine; likely fine but merits a build verification early in Phase 3. |
| Features | HIGH | Grounded in existing codebase analysis (what exists vs. what is missing is explicitly inventoried). Anti-features clearly identified with rationale. Feature dependencies modeled as a directed graph. |
| Architecture | HIGH | New module structure mirrors existing conventions precisely. Build order validated against actual module dependency graph. All code patterns backed by official docs or existing working code in the project. The `server:groups -> server:auth` dependency for UsersTable FK is the only novel cross-module coupling. |
| Pitfalls | MEDIUM-HIGH | Security pitfalls (auth bypass, privilege escalation) based on well-documented multi-tenant RBAC patterns. Testing pitfalls (Turbine conflation, testApplication dispatcher) verified against GitHub issues and official bug reports. One MEDIUM item: WASM resource loading async timing needs empirical verification in Phase 7. |

**Overall confidence:** HIGH

### Gaps to Address

- **Turbine 1.2.1 + Kotlin 2.3.10 compatibility:** No documented incompatibility found, but Turbine 1.2.1 predates Kotlin 2.3. Validate with a build test in Phase 3 before committing the testing architecture. If issues arise, 1.3.0-SNAPSHOT is available as fallback.
- **`testApplication` + real dispatchers (Ktor 3.4.0):** KTOR-7121 documents that `testApplication` does not honor specified coroutine dispatchers. This may be fixed in 3.4.0 or may require the workaround (separate test Configuration with injectable delays). Validate in Phase 3 with a spike before designing test base classes.
- **WASM string resource loading on first frame:** CMP resources load async on WASM. Whether there is a visible flash of English text before locale strings load depends on render timing. Needs a WASM smoke test in Phase 7 before declaring localization complete.
- **`kotest-assertions-arrow` 6.1.3 in `wasmJs` `commonTest`:** Kotest multiplatform artifacts exist but wasmJs support for the arrow assertions module specifically may have edge cases. Validate early in Phase 3 when setting up the `testing-kmp` bundle.

## Sources

### Primary (HIGH confidence)
- Kotlin Multiplatform ViewModel docs — AndroidX Lifecycle 2.9.6 KMP support confirmed
- Compose Multiplatform Resources — string localization, `values-xx` qualifier directories, `Res.strings` code generation
- Compose Localize Strings (JetBrains) — `stringResource()`, locale fallback behavior, `composeResources` structure
- Turbine GitHub (cashapp/turbine) — v1.2.1 release notes, KMP target support (JVM, JS, wasmJs, Native confirmed in 1.1.0+)
- Kotest releases — v6.1.3 (Feb 2025), Kotlin 2.0-2.3 support confirmed, multiplatform assertions confirmed
- Ktor Testing docs — `testApplication` API, client configuration
- Ktor `acceptLanguageItems` API — built-in `Accept-Language` header parsing
- kotlinx-coroutines-test docs — `runTest`, `TestDispatcher`, `advanceUntilIdle`
- JetBrains Exposed GitHub — R2DBC DSL table definitions, `autoGenerate()`, FK patterns
- Koin official docs — `viewModelOf()`, `koinViewModel<T>()`, KMP compose integration
- AndroidX Lifecycle KMP docs — `viewModelScope`, `collectAsStateWithLifecycle()`

### Secondary (MEDIUM confidence)
- KMP Testing Guide 2025 (kmpship.app) — Turbine + Kotest + coroutines-test as standard KMP test stack
- MockK GitHub — confirmed no Kotlin/Native support, manual fakes recommended for KMP `commonTest`
- MVI Event Management Best Practice (2026) — `StateFlow` + `Channel` pattern for one-shot events
- Multi-tenant RBAC Best Practices (Aserto, Permit.io, WorkOS) — tenant_id everywhere, role hierarchy enforcement
- Ktor testApplication Timeout (KTOR-6925) — real-dispatcher behavior in test context
- Ktor Test HTTP Client Dispatcher (KTOR-7121) — dispatcher not honored in testApplication

### Tertiary (LOW confidence — validate during execution)
- Turbine 1.2.1 precise compatibility with Kotlin 2.3.10 — no documented constraint found, empirically validate in Phase 3
- `kotest-assertions-arrow` 6.1.3 in wasmJs `commonTest` — multiplatform artifacts exist, wasmJs target edge cases unverified
- CMP WASM async resource loading first-frame timing — behavior not quantified, validate in Phase 7
- Ktor 3.4.0 KTOR-7121 fix status — open as of research date; may be addressed or require workaround

### Bug Reports Referenced
- KTOR-6925 — testApplication unexpected request timeout on slow server startup
- KTOR-7121 — testApplication HTTP client does not honor specified coroutine dispatcher
- CMP #4675 — `%d` vs `%1$d` in plural strings silent failure on Desktop/WASM
- CMP #5040 — `pluralStringResource` not working properly on non-Android targets

---
*Research completed: 2026-02-17*
*Ready for roadmap: yes*
