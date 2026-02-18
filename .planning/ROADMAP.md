# Roadmap: KMP Full-Stack Template

## Milestones

- ✅ **v1.0 MVP** -- Phases 1-9 (shipped 2026-02-17)
- 🚧 **v1.1 Architecture** -- Phases 10-15 (in progress)

## Phases

<details>
<summary>✅ v1.0 MVP (Phases 1-9) -- SHIPPED 2026-02-17</summary>

- [x] Phase 1: Foundation & Module Structure (4/4 plans) -- completed 2026-02-10
- [x] Phase 2: Server Auth & Users (3/3 plans) -- completed 2026-02-11
- [x] Phase 3: Client SDK & Storage (3/3 plans) -- completed 2026-02-11
- [x] Phase 4: Navigation & UI Components (7/7 plans) -- completed 2026-02-12
- [x] Phase 5: Auth Screens, Dashboard & Setup CLI (11/11 plans) -- completed 2026-02-13
- [x] Phase 6: AI Agent Infrastructure (3/3 plans) -- completed 2026-02-13
- [x] Phase 6.1: Chat Agent Streaming Refactor (2/2 plans) -- completed 2026-02-14
- [x] Phase 7: Role System Refactor & Tech Debt (2/2 plans) -- completed 2026-02-15
- [x] Phase 8: Type-Safe Shared Routes (3/3 plans) -- completed 2026-02-15
- [x] Phase 9: WASM HTTP Engine Fix (1/1 plan) -- completed 2026-02-16

Full details: milestones/v1.0-ROADMAP.md

</details>

### 🚧 v1.1 Architecture (In Progress)

**Milestone Goal:** Add architectural patterns and domain capabilities: MVI ViewModel layer, group-based user management with admin panel, full testing infrastructure, and localization system.

- [x] **Phase 10: MVI ViewModel Foundation** - Base class with Intent/Model/Mutation/Event, StateFlow state, SharedFlow effects, Koin injection (completed 2026-02-17)
- [ ] **Phase 11: Testing Infrastructure** - core:testing module with Turbine DSL, SDK interface extraction, fake implementations, shared fixtures
- [ ] **Phase 12: ViewModel Migration** - Migrate all 5 existing ViewModels to MVI pattern with tests
- [ ] **Phase 13: Group Server & SDK** - server:groups module with tables, RBAC, SDK class, shared routes, integration tests
- [ ] **Phase 14: Group Admin UI** - Admin panel with group management screens and role-gated navigation
- [ ] **Phase 15: Localization** - StringKey enum, resource files, server i18n, client string loading, runtime locale switching

## Phase Details

### Phase 10: MVI ViewModel Foundation
**Goal**: Developers have a formal MVI base class they can extend with typed Intent/Model/Event parameters to build any ViewModel
**Depends on**: Nothing (first phase of v1.1)
**Requirements**: MVI-01, MVI-02, MVI-03, MVI-04
**Success Criteria** (what must be TRUE):
  1. Developer can create a new ViewModel by extending MviViewModel with custom Intent, Model, and Event sealed types
  2. ViewModel state is exposed as StateFlow and UI recomposes reactively when state changes
  3. One-shot events (navigation, toasts) arrive via SharedFlow<Event> with replay=0 -- never double-fire on recomposition
  4. ViewModels are injectable via Koin using koinViewModel() on all KMP targets (Android, iOS, Desktop, WASM)
**Plans**: 1 plan
- [ ] 10-01-PLAN.md -- Create core:mvi module with MviViewModel base class

### Phase 11: Testing Infrastructure
**Goal**: Developers have a reusable testing toolkit -- SDK interfaces with fake implementations and a Turbine-based ViewModel test DSL -- so all subsequent feature work ships with tests
**Depends on**: Phase 10
**Requirements**: TEST-01, TEST-03, TEST-04, TEST-05, TEST-06, MVI-06
**Success Criteria** (what must be TRUE):
  1. Developer can write ViewModel tests using a Turbine-based DSL that dispatches intents and asserts state/event sequences
  2. SDK API classes (AuthApi, UserApi) have extracted interfaces with hand-written fake implementations for test substitution
  3. Shared test fixtures and utilities are available as a core:testing module importable by any project module
  4. Kotest assertions work with Arrow Either/Raise types in multiplatform tests (JVM, iOS, WASM)
  5. At least one working ViewModel test (LoginViewModel) demonstrates the full pattern as a reference
**Plans**: 3 plans
Plans:
- [ ] 11-01-PLAN.md -- Extract SDK interfaces, create Impl classes, Sdk facade, update Koin bindings
- [ ] 11-02-PLAN.md -- Create core:testing module with Turbine DSL, annotations, ViewModelTest base class
- [ ] 11-03-PLAN.md -- Fake SDK builder DSL and reference LoginViewModel test

### Phase 12: ViewModel Migration
**Goal**: All existing ViewModels use the MVI pattern consistently -- the template demonstrates one approach, not two
**Depends on**: Phase 11
**Requirements**: MVI-05
**Success Criteria** (what must be TRUE):
  1. All 5 existing ViewModels (Login, Register, ForgotPassword, Profile, Dashboard) extend MviViewModel
  2. Boolean navigation flags (loginSuccess, logoutTriggered) are replaced with Channel-based effects
  3. Composables consume effects via LaunchedEffect collection instead of state observation for one-shot actions
  4. Each migrated ViewModel has at least one unit test using the core:testing DSL
**Plans**: TBD

### Phase 13: Group Server & SDK
**Goal**: The server supports group-based user organization with CRUD operations, membership management, and data isolation -- accessible through the shared SDK
**Depends on**: Phase 10
**Requirements**: GRP-01, GRP-02, GRP-07, GRP-08, TEST-02
**Success Criteria** (what must be TRUE):
  1. Admin can create a group and the group persists in the database with correct schema (designed for future multi-group)
  2. Users belong to one group and can only access their own group's data -- cross-group requests return 403
  3. GroupApi SDK functions return Either<ClientError, T> using shared @Resource route definitions
  4. Server integration tests verify auth flow, group CRUD, RBAC enforcement, and cross-group isolation using Ktor testApplication
**Plans**: TBD

### Phase 14: Group Admin UI
**Goal**: Admins have a dedicated panel to manage their group -- view members, register users, and access admin-specific dashboard content
**Depends on**: Phase 12, Phase 13
**Requirements**: GRP-03, GRP-04, GRP-05, GRP-06
**Success Criteria** (what must be TRUE):
  1. Admin can view group information and a list of group members from the admin panel
  2. Admin can register new users directly into their group from the admin panel
  3. Admin sees different dashboard content than regular users (admin panel sections visible only to admin role)
  4. Navigation is role-gated -- admin routes appear only for admin users, regular users never see admin navigation items
**Plans**: TBD

### Phase 15: Localization
**Goal**: The template demonstrates a complete localization system -- shared string keys between server and client, platform resource files, and runtime locale switching
**Depends on**: Phase 14
**Requirements**: L10N-01, L10N-02, L10N-03, L10N-04, L10N-05, L10N-06
**Success Criteria** (what must be TRUE):
  1. A shared StringKey enum exists that both server and client reference for string lookup -- no hardcoded strings in ViewModels
  2. Compose resource files (strings.xml) support locale qualifiers and load correctly on all KMP targets
  3. Server returns localized error messages based on the Accept-Language header
  4. User can switch locale at runtime and all UI strings update without app restart
  5. A bridge function maps StringKey values to Compose Res.strings accessors for type-safe string resolution
**Plans**: TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 10 -> 11 -> 12 -> 13 -> 14 -> 15

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 1. Foundation & Module Structure | v1.0 | 4/4 | Complete | 2026-02-10 |
| 2. Server Auth & Users | v1.0 | 3/3 | Complete | 2026-02-11 |
| 3. Client SDK & Storage | v1.0 | 3/3 | Complete | 2026-02-11 |
| 4. Navigation & UI Components | v1.0 | 7/7 | Complete | 2026-02-12 |
| 5. Auth Screens, Dashboard & Setup CLI | v1.0 | 11/11 | Complete | 2026-02-13 |
| 6. AI Agent Infrastructure | v1.0 | 3/3 | Complete | 2026-02-13 |
| 6.1. Chat Agent Streaming Refactor | v1.0 | 2/2 | Complete | 2026-02-14 |
| 7. Role System Refactor & Tech Debt | v1.0 | 2/2 | Complete | 2026-02-15 |
| 8. Type-Safe Shared Routes | v1.0 | 3/3 | Complete | 2026-02-15 |
| 9. WASM HTTP Engine Fix | v1.0 | 1/1 | Complete | 2026-02-16 |
| 10. MVI ViewModel Foundation | v1.1 | Complete    | 2026-02-17 | - |
| 11. Testing Infrastructure | v1.1 | 0/3 | Planned | - |
| 12. ViewModel Migration | v1.1 | 0/0 | Not started | - |
| 13. Group Server & SDK | v1.1 | 0/0 | Not started | - |
| 14. Group Admin UI | v1.1 | 0/0 | Not started | - |
| 15. Localization | v1.1 | 0/0 | Not started | - |
