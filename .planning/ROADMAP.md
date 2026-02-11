# Roadmap: KMP Full-Stack Template

## Overview

This roadmap delivers a production-ready Kotlin Multiplatform template in 6 phases, progressing from architectural foundation through server features, client infrastructure, UI, and AI capabilities. The structure follows strict dependency ordering: modules and error patterns first (Phase 1), then server endpoints (Phase 2), then the client SDK that calls them (Phase 3), then navigation and UI components (Phase 4), then screens that compose everything (Phase 5), and finally the AI agent differentiator (Phase 6). Every phase delivers a coherent, independently verifiable capability.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: Foundation & Module Structure** - Establish module boundaries, upgrade dependencies, wire cross-cutting patterns (completed 2026-02-10)
- [x] **Phase 2: Server Auth & Users** - Complete server-side authentication and user management (completed 2026-02-11)
- [x] **Phase 3: Client SDK & Storage** - Build the Either-based client networking layer and local persistence (completed 2026-02-11)
- [ ] **Phase 4: Navigation & UI Components** - Set up multiplatform navigation, component library, and theming
- [ ] **Phase 5: Auth Screens, Dashboard & Setup CLI** - Deliver end-to-end user-facing screens and template onboarding
- [ ] **Phase 6: AI Agent Infrastructure** - Integrate Koog agents with tool system and conversation management

## Phase Details

### Phase 1: Foundation & Module Structure
**Goal**: Developers have a correctly structured multiplatform project with focused modules, upgraded dependencies, cross-target DI, structured logging, and shared error/model types -- all verified on every KMP target including WASM.
**Depends on**: Nothing (first phase)
**Requirements**: FOUND-01, FOUND-02, FOUND-03, FOUND-04, FOUND-05, FOUND-06, FOUND-07, CC-01, CC-02
**Research flag**: SKIP -- well-documented module structure patterns, official Gradle docs, Koin multiplatform guides
**Success Criteria** (what must be TRUE):
  1. Project compiles and runs on all 4 KMP targets (Android, iOS, Desktop, WASM) after module restructuring
  2. Separate modules exist for sdk, storage, shared-models, and server:ai with correct dependency graphs (no circular deps, no WASM-incompatible deps in commonMain)
  3. Koin dependency injection resolves correctly on every target (verified with koin.verify() or equivalent test per target)
  4. All domain error handling uses Arrow Raise API with context parameters -- zero try/catch for domain errors anywhere in the codebase
  5. Structured logging (Kermit) produces formatted output on at least one target, replacing all println calls
**Plans**: 4 plans

Plans:
- [x] 01-01-PLAN.md -- Dependency upgrades, version catalog, BOMs, and buildSrc convention plugins
- [x] 01-02-PLAN.md -- Module restructuring (:core:models, :core:sdk, :core:storage, stubs), AppError hierarchy, shared DTOs
- [x] 01-03-PLAN.md -- Koin DI wiring across all targets + Arrow validation helpers with context parameters
- [x] 01-04-PLAN.md -- Logging infrastructure (Kermit + Log4j) and println replacement

### Phase 2: Server Auth & Users
**Goal**: A developer can register, log in, refresh tokens, log out, view/update a profile, and hit role-protected endpoints against a running server -- all through documented API endpoints.
**Depends on**: Phase 1
**Requirements**: AUTH-01, AUTH-02, AUTH-03, AUTH-04, AUTH-05, AUTH-06, AUTH-07
**Research flag**: SKIP -- standard Ktor auth + Exposed CRUD, well-documented
**Success Criteria** (what must be TRUE):
  1. A new user can POST /api/auth/register with email/password and receive a success response (password stored bcrypt-hashed, never in plaintext)
  2. A registered user can POST /api/auth/login and receive both a JWT access token and a refresh token
  3. An expired access token can be renewed via POST /api/auth/refresh using a valid refresh token, without requiring re-login
  4. A logged-in user can GET and PUT their own profile, and access to other users' data is denied based on role permissions
  5. All auth endpoint error responses use structured Arrow Raise error types (not exception stack traces), with validation errors accumulated (e.g., multiple signup field errors returned at once)
**Plans**: 3 plans

Plans:
- [x] 02-01-PLAN.md -- Tables, repositories, security utilities, registration endpoint, Koin wiring, migration visibility fix
- [x] 02-02-PLAN.md -- Login, token refresh with rotation, and logout endpoints
- [x] 02-03-PLAN.md -- Profile CRUD (GET/PUT /me, admin GET /{id}) and RBAC plugin

### Phase 3: Client SDK & Storage
**Goal**: Client code can call every server endpoint through typed Kotlin functions that return Either<ClientError, T>, with tokens persisted locally and refreshed automatically -- no direct HTTP or manual token management anywhere in UI code.
**Depends on**: Phase 2
**Requirements**: SDK-01, SDK-02, SDK-03, SDK-04, STOR-01, STOR-02
**Research flag**: NEEDS research-phase -- token refresh mutex patterns in KMP, WASM DataStore limitations, Arrow Either serialization with Ktor content negotiation
**Success Criteria** (what must be TRUE):
  1. Every server endpoint has a corresponding SDK function returning Either<ClientError, T> -- UI code never imports Ktor Client directly
  2. When an access token expires mid-session, the SDK automatically refreshes it and retries the failed request without user-visible interruption
  3. Auth tokens survive app restart on all platforms (persisted via DataStore or platform-appropriate secure storage)
  4. User preferences (theme, language) persist across app restarts via local storage
  5. ClientError is a sealed class hierarchy that maps HTTP status codes to typed errors (Unauthorized, NotFound, ValidationFailed, NetworkError) -- no raw HTTP codes leak to callers
**Plans**: 3 plans

Plans:
- [x] 03-01-PLAN.md -- SDK module foundation: Ktor Client deps, platform engines (OkHttp/Darwin/CIO), HttpClient factory, apiCall error mapper
- [x] 03-02-PLAN.md -- Storage module: multiplatform-settings for TokenStorage and PreferencesStorage with Flow observation
- [x] 03-03-PLAN.md -- Auth interceptor, AuthApi, UserApi, SdkModule Koin wiring, DI integration into composeApp

### Phase 4: Navigation & UI Components
**Goal**: The app has type-safe multiplatform navigation between screens, a reusable component library (buttons, inputs, cards, dialogs), and a custom theme system -- all working identically on every KMP target.
**Depends on**: Phase 1 (module structure), Phase 3 (SDK/storage for DI wiring)
**Requirements**: NAV-01, NAV-03, NAV-04
**Research flag**: SKIP -- Navigation Compose 2.9.1 and Material3 have official JetBrains guides
**Success Criteria** (what must be TRUE):
  1. Navigation between screens uses type-safe route objects (serializable data classes) -- no string-based routing anywhere
  2. A shared component library provides at minimum: buttons (primary, secondary, outlined), text inputs (with validation states), cards, and dialogs -- all themed consistently
  3. The app renders with a custom theme (colors, typography) that a developer can change by editing a single theme configuration, and it applies uniformly across all targets
**Plans**: 2 plans

Plans:
- [ ] 04-01-PLAN.md -- Navigation deps, serialization plugin, @Serializable routes, NavHost, App.kt wiring
- [ ] 04-02-PLAN.md -- Custom theme system (Color, Type, Shape, AppTheme) and shared component library (buttons, inputs, cards, dialogs)

### Phase 5: Auth Screens, Dashboard & Setup CLI
**Goal**: A developer who clones the template can run a setup script to customize it, then see a working app with login/signup screens, a sample dashboard behind auth, and form validation -- proving the entire architecture works end-to-end.
**Depends on**: Phase 3 (SDK), Phase 4 (navigation, components)
**Requirements**: NAV-02, DX-01, DX-02
**Research flag**: SKIP -- standard Compose UI patterns and bash scripting
**Success Criteria** (what must be TRUE):
  1. A user can sign up, log in, and land on a dashboard screen -- the full auth flow works visually with loading states, error messages, and form validation (including Arrow-based validation with accumulated errors)
  2. The sample dashboard screen demonstrates real data from the server fetched via SDK, proving auth + SDK + navigation + UI all work together
  3. A developer can run the setup CLI script after cloning and have the project renamed (package, applicationId, DB schema) and building within 2 minutes, with no manual find-and-replace needed
**Plans**: TBD

Plans:
- [ ] 05-01: Auth screens (login, signup) with form validation
- [ ] 05-02: Dashboard and profile screens
- [ ] 05-03: Setup CLI script

### Phase 6: AI Agent Infrastructure
**Goal**: The template ships with working Koog AI agent infrastructure -- an agent registry, tool system, conversation persistence, and example agents that a developer can extend to build their own AI features.
**Depends on**: Phase 2 (server auth/user services for agent tools to access)
**Requirements**: AI-01, AI-02, AI-03, AI-04
**Research flag**: NEEDS research-phase -- Koog 0.6.1 patterns, MCP integration, tool sandboxing, conversation persistence strategies
**Success Criteria** (what must be TRUE):
  1. A developer can POST to an agent endpoint and receive a response generated by a Koog agent that used at least one tool (e.g., querying user data from the database)
  2. Agent conversations persist across requests -- a follow-up message to the same conversation ID continues context from previous turns
  3. At least 2 example agents exist with different patterns (e.g., one using ReAct strategy with tools, one demonstrating conversation management) that a developer can study and extend
  4. All agent error handling uses Arrow Raise -- agent failures produce typed errors, not exception stack traces
**Plans**: TBD

Plans:
- [ ] 06-01: Koog plugin setup and agent registry
- [ ] 06-02: Example agents with tools and conversation persistence

## Progress

**Execution Order:**
Phases execute in numeric order: 1 -> 2 -> 3 -> 4 -> 5 -> 6

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Foundation & Module Structure | 4/4 | Complete | 2026-02-10 |
| 2. Server Auth & Users | 3/3 | Complete | 2026-02-11 |
| 3. Client SDK & Storage | 3/3 | Complete | 2026-02-11 |
| 4. Navigation & UI Components | 0/2 | Not started | - |
| 5. Auth Screens, Dashboard & Setup CLI | 0/3 | Not started | - |
| 6. AI Agent Infrastructure | 0/2 | Not started | - |
