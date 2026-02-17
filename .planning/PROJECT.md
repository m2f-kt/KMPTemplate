# KMP Full-Stack Template

## What This Is

A production-ready Kotlin Multiplatform project template for building full-stack applications. It provides a Ktor backend and Compose Multiplatform clients (Android, iOS, Desktop, Web/WASM) with pre-wired infrastructure: JWT authentication with typed roles, database with migrations, Koin DI across all targets, AI agent support (Koog), a terminal-themed component library (41+ components), and a shared SDK layer. Developers clone it, run a setup script, and start building features on top of a working app with auth, dashboard, and AI chat.

## Core Value

A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.

## Requirements

### Validated

- ✓ Gradle version catalog with convention plugins and BOMs -- v1.0
- ✓ Module structure (core:models, core:sdk, core:storage, server:ai, app:*) -- v1.0
- ✓ Koin DI wired across all KMP targets (Android, iOS, Desktop, WASM) -- v1.0
- ✓ Arrow Raise error handling with context parameters across server and client -- v1.0
- ✓ Kermit structured logging (client) + Log4j2 ECS (server) -- v1.0
- ✓ JWT auth with register, login, refresh, logout, session persistence -- v1.0
- ✓ UserRole sealed class with DB-backed roles table and typed RBAC -- v1.0
- ✓ Client SDK returning Either<ClientError, T> with auto token refresh -- v1.0
- ✓ Type-safe shared routes (@Resource) across server and SDK -- v1.0
- ✓ Terminal-themed component library (41+ components from Pencil design) -- v1.0
- ✓ Type-safe multiplatform navigation (Navigation Compose 2.9.1) -- v1.0
- ✓ Auth screens (login, signup, forgot password) with Arrow validation -- v1.0
- ✓ Responsive dashboard with sidebar (desktop) / bottom nav (mobile) -- v1.0
- ✓ AI agent infrastructure with Koog (tools, persistence, WebSocket streaming) -- v1.0
- ✓ Setup CLI script for project renaming after clone -- v1.0
- ✓ WASM browser support with Js HTTP engine and CORS -- v1.0
- ✓ PostgreSQL via R2DBC with migration system -- v1.0
- ✓ Metrics/monitoring with Micrometer Prometheus -- v1.0
- ✓ Security headers (CSRF, CORS, caching, HTTPS redirect) -- v1.0

### Active

(None -- planning next milestone)

### Out of Scope

- Real-time features beyond AI chat WebSocket -- adds complexity beyond template scope
- Payment/billing integration -- too domain-specific for a template
- CI/CD pipeline configuration -- varies too much per team/platform
- iOS-specific UIKit views -- Compose Multiplatform only
- Server-side rendering -- API-first architecture
- Push notifications -- requires platform-specific accounts (FCM, APNs)

## Context

Shipped v1.0 with 15,687 LOC Kotlin across 170 files in 7 days.
Tech stack: Ktor + Netty, Exposed R2DBC, Arrow 2.2.0, Koin, Compose Multiplatform 1.9.3, Koog 0.6.2, Navigation Compose 2.9.1.
Targets: Android (minSdk 24), iOS, JVM Desktop, WASM/JS Web.

The template uses Kotlin context parameters (`-Xcontext-parameters`) for dependency propagation and Arrow's Raise API for all domain error handling. The client SDK abstracts all networking behind clean Kotlin functions returning `Either<DomainError, T>`.

Known tech debt: Koin DI runtime verification on all targets (human-needed), WASM production stability unconfirmed, STOR-02 PreferencesStorage wired but unused.

## Constraints

- **Tech stack**: Kotlin Multiplatform -- no non-Kotlin dependencies for shared code
- **KMP targets**: Android (minSdk 24), iOS, JVM Desktop, WASM/JS Web
- **Server**: Ktor + Netty, compatible with OpenAPI EAP branch
- **Database**: PostgreSQL via R2DBC (async) -- no blocking JDBC in production paths
- **DI**: Koin across all targets
- **Error handling**: Arrow Either/Raise -- no exception-based error handling for domain errors
- **UI**: Compose Multiplatform only -- no platform-specific UI frameworks
- **Kotlin version**: 2.2.10 with context parameters enabled

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Koin for DI | KMP-native, lightweight, widely adopted in Kotlin ecosystem | ✓ Good -- wired across all 4 targets |
| Either/Result SDK pattern | Explicit error handling, no surprise exceptions, clean caller contracts | ✓ Good -- all SDK functions return Either |
| Koog for AI agents | JetBrains-native, Kotlin-first, designed for KMP ecosystem | ✓ Good -- 2 agent patterns, persistence, WebSocket streaming |
| CLI setup script | Better onboarding than manual renaming; configurable project identity | ✓ Good -- dynamic module discovery, package/DB renaming |
| Terminal design system (not Material) | Unique aesthetic, Foundation-only (no Material3 dep), full Pencil reference | ✓ Good -- 41+ components, charts, gestures |
| Arrow context parameters | Powerful typed context propagation, replaces try/catch for domain errors | ✓ Good -- zero try/catch for domain errors |
| R2DBC over JDBC | Non-blocking database access, aligns with coroutine-first architecture | ✓ Good -- all DB operations async |
| UserRole sealed class | Compile-time role safety, shared across server and client | ✓ Good -- replaced string roles, DB-backed |
| @Resource shared routes | Single source of truth for API routes, compile-time safety | ✓ Good -- eliminated all hardcoded route strings |
| Js engine for WASM | Browser fetch API, CIO requires Node.js net module | ✓ Good -- WASM browser requests work |
| WebSocket for AI streaming | Bidirectional, header-based JWT auth, better than SSE | ✓ Good -- replaced SSE after Phase 6.1 |
| Per-request AIAgent | Streaming callback injection, no singleton state conflicts | ✓ Good -- clean concurrent request handling |

---
*Last updated: 2026-02-17 after v1.0 milestone*
