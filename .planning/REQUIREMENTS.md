# Requirements: KMP Full-Stack Template

**Defined:** 2026-02-10
**Core Value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Foundation

- [ ] **FOUND-01**: Upgrade Compose Multiplatform to stable 1.9.3
- [ ] **FOUND-02**: Upgrade Exposed to stable 1.0.0
- [ ] **FOUND-03**: Upgrade Arrow to 2.2.0 with context parameter support
- [ ] **FOUND-04**: Restructure into separate modules (sdk, storage, ai, shared-models)
- [ ] **FOUND-05**: Wire Koin DI across all KMP targets (Android, iOS, Desktop, WASM)
- [ ] **FOUND-06**: Replace println with Kermit structured logging across all modules
- [ ] **FOUND-07**: Create shared models module with serializable types used by both server and clients

### Authentication & Users

- [ ] **AUTH-01**: User can sign up with email and password (bcrypt hashed)
- [ ] **AUTH-02**: User can log in and receive JWT access + refresh tokens
- [ ] **AUTH-03**: User can refresh expired access tokens using refresh token
- [ ] **AUTH-04**: User can log out (token invalidation)
- [ ] **AUTH-05**: User can view and update their own profile
- [ ] **AUTH-06**: User can be assigned roles with permission checks on protected endpoints
- [ ] **AUTH-07**: All auth/user endpoints use Arrow Raise (no try/catch), with error accumulation for validation

### Client SDK

- [ ] **SDK-01**: SDK module provides typed API functions returning `Either<ClientError, T>` for all server endpoints
- [ ] **SDK-02**: SDK automatically refreshes tokens on 401 and retries the original request
- [ ] **SDK-03**: ClientError sealed class hierarchy maps server DomainError to client-side typed errors
- [ ] **SDK-04**: SDK uses platform-specific Ktor Client engines (OkHttp/Darwin/CIO/JS)

### Navigation & UI

- [ ] **NAV-01**: Multiplatform navigation with type-safe routes (Navigation Compose 2.9.1)
- [ ] **NAV-02**: Auth screens (login, signup, forgot password) with form validation
- [ ] **NAV-03**: Shared UI component library (buttons, inputs, cards, dialogs)
- [ ] **NAV-04**: Custom theme system sourced from developer-specified design system

### Local Storage

- [ ] **STOR-01**: Auth tokens persisted securely across app restarts
- [ ] **STOR-02**: User preferences storage (theme, language, settings)

### AI Agents (Koog)

- [ ] **AI-01**: Koog Ktor plugin integrated with agent registry and tool system
- [ ] **AI-02**: Conversation management with persistence and resume capability
- [ ] **AI-03**: 2-3 example agents demonstrating patterns (tools, strategies, conversation)
- [ ] **AI-04**: All agent error handling uses Arrow Raise (no try/catch)

### Developer Experience

- [ ] **DX-01**: Setup CLI bash script configures project name, package, DB credentials on clone
- [ ] **DX-02**: Sample dashboard screen demonstrating auth, navigation, AI, and component library

### Cross-Cutting

- [ ] **CC-01**: Arrow Raise API used for all error handling across server and client (zero try/catch for domain errors)
- [ ] **CC-02**: Error accumulation (Raise.accumulate) used for validation scenarios (signup forms, config validation)

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Authentication

- **AUTH-V2-01**: OAuth login (Google, GitHub)
- **AUTH-V2-02**: Magic link authentication
- **AUTH-V2-03**: Two-factor authentication (2FA)

### UI

- **NAV-V2-01**: Adaptive layouts (phone/tablet/desktop responsive)
- **NAV-V2-02**: Dark mode / light mode toggle

### Infrastructure

- **INFRA-V2-01**: Gradle convention plugins for zero-config new modules
- **INFRA-V2-02**: CI/CD pipeline templates
- **INFRA-V2-03**: WASM build verification in CI

### AI Agents

- **AI-V2-01**: MCP (Model Context Protocol) integration
- **AI-V2-02**: Streaming agent responses

## Out of Scope

| Feature | Reason |
|---------|--------|
| Real-time features (WebSockets, SSE) | Adds complexity beyond template scope |
| Payment/billing integration | Too domain-specific, requires merchant credentials |
| Push notifications | Requires platform-specific accounts (FCM, APNs) |
| Social login (OAuth) | Deferred to v2 -- email/password sufficient for v1 |
| Analytics SDKs | Requires third-party account credentials |
| Server-side rendering | API-first architecture |
| iOS-specific UIKit views | Compose Multiplatform only |
| CI/CD pipeline configuration | Varies too much per team/platform |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| FOUND-01 | Phase 1 | Pending |
| FOUND-02 | Phase 1 | Pending |
| FOUND-03 | Phase 1 | Pending |
| FOUND-04 | Phase 1 | Pending |
| FOUND-05 | Phase 1 | Pending |
| FOUND-06 | Phase 1 | Pending |
| FOUND-07 | Phase 1 | Pending |
| AUTH-01 | Phase 2 | Pending |
| AUTH-02 | Phase 2 | Pending |
| AUTH-03 | Phase 2 | Pending |
| AUTH-04 | Phase 2 | Pending |
| AUTH-05 | Phase 2 | Pending |
| AUTH-06 | Phase 2 | Pending |
| AUTH-07 | Phase 2 | Pending |
| SDK-01 | Phase 3 | Pending |
| SDK-02 | Phase 3 | Pending |
| SDK-03 | Phase 3 | Pending |
| SDK-04 | Phase 3 | Pending |
| STOR-01 | Phase 3 | Pending |
| STOR-02 | Phase 3 | Pending |
| NAV-01 | Phase 4 | Pending |
| NAV-02 | Phase 5 | Pending |
| NAV-03 | Phase 4 | Pending |
| NAV-04 | Phase 4 | Pending |
| AI-01 | Phase 6 | Pending |
| AI-02 | Phase 6 | Pending |
| AI-03 | Phase 6 | Pending |
| AI-04 | Phase 6 | Pending |
| DX-01 | Phase 5 | Pending |
| DX-02 | Phase 5 | Pending |
| CC-01 | Phase 1 | Pending |
| CC-02 | Phase 1 | Pending |

**Coverage:**
- v1 requirements: 32 total
- Mapped to phases: 32
- Unmapped: 0

---
*Requirements defined: 2026-02-10*
*Last updated: 2026-02-10 after roadmap creation*
