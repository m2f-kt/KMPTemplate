# Milestones

## v1.0 MVP (Shipped: 2026-02-17)

**Phases completed:** 10 phases (1-9 + 6.1), 39 plans, 27 quick tasks
**Timeline:** 7 days (2026-02-10 → 2026-02-17)
**Lines of code:** 15,687 LOC Kotlin across 170 files
**Feature commits:** 97
**Requirements:** 32/32 satisfied

**Delivered:** A production-ready KMP full-stack template with auth, AI agents, a terminal-themed component library, and setup CLI -- clone, configure, and start building.

**Key accomplishments:**
- Module structure with convention plugins, Arrow Raise error handling, Koin DI across 4 KMP targets
- Full server auth (register, login, refresh, logout, RBAC) with typed UserRole sealed class and DB-backed roles
- Client SDK returning Either<ClientError, T> with auto token refresh and persistent token storage
- Terminal-themed UI component library (41+ components, charts, gestures) designed from Pencil reference
- End-to-end auth screens, responsive dashboard, profile system, and setup CLI for project cloning
- AI agent infrastructure with Koog (tool system, conversation persistence, WebSocket streaming)
- Type-safe shared routes (@Resource) across server and SDK, WASM browser support with Js engine

**Known tech debt (non-blocking):**
- Runtime Koin DI verification on all 4 KMP targets (human-needed)
- WASM production build stability unconfirmed (dev works)
- STOR-02 PreferencesStorage wired but no runtime consumer

**Git range:** feat(01-01) → feat(quick-27)
**Archives:** milestones/v1.0-ROADMAP.md, milestones/v1.0-REQUIREMENTS.md

---


## v1.1 Architecture (Shipped: 2026-02-21)

**Phases completed:** 7 phases (10-15 + 11.1), 34 plans
**Timeline:** 4 days (2026-02-18 → 2026-02-21)
**Lines of code:** 21,583 LOC Kotlin (+5,896 from v1.0)
**Files changed:** 248 files (+14,541 / -4,306)
**Requirements:** 26/26 satisfied

**Delivered:** Architectural patterns and domain capabilities: MVI ViewModel layer with testing infrastructure, group-based user management with admin panel, and full localization system with runtime locale switching.

**Key accomplishments:**
- MVI ViewModel base class with Intent/Model/Mutation/Event pipeline, StateFlow state, SharedFlow effects, Koin injection across all KMP targets
- Testing infrastructure: SDK interface extraction with fake implementations, Turbine-based ViewModel test DSL, fakeSdk {} builder, core:testing module
- All 5 existing ViewModels migrated to MVI pattern with comprehensive unit tests using core:testing DSL
- Group server module with CRUD, RBAC enforcement, membership management, and Testcontainers integration tests
- Group Admin UI with admin panel, member registration, role-gated navigation, and Arrow zipOrAccumulate form validation
- Localization system: StringKey enum, EN/ES Compose resource files, server i18n via Accept-Language, runtime locale switching on all platforms

**Known tech debt (non-blocking):**
- Missing integration tests: registerMember endpoint, RBAC edge cases, pagination boundary
- WASM locale stored in memory only (page reload required for full switch)
- Ktor testApplication dispatcher issue (KTOR-7121) unresolved

**Git range:** feat(10-01) → feat(quick-1)
**Archives:** milestones/v1.1-ROADMAP.md, milestones/v1.1-REQUIREMENTS.md

---

