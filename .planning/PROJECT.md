# KMP Full-Stack Template

## What This Is

A reusable project template for building full-stack Kotlin Multiplatform applications. It provides a production-ready Ktor backend and Compose Multiplatform clients (Android, iOS, Desktop, Web) with pre-wired infrastructure: authentication, database, DI, AI agent support, and a shared SDK layer. Developers clone it, run a setup script, and start building features on top of a working app with auth and a sample dashboard.

## Core Value

A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library — no infrastructure decisions required.

## Requirements

### Validated

<!-- Inferred from existing codebase -->

- ✓ Ktor server with Netty engine and OpenAPI docs — existing
- ✓ JWT authentication with HMAC256 signing — existing
- ✓ PostgreSQL database with R2DBC async access — existing
- ✓ Database migration system with version tracking — existing
- ✓ Arrow-based functional error handling (Raise/Either) — existing
- ✓ Koin dependency injection on server — existing
- ✓ Compose Multiplatform UI shell (Android, iOS, Desktop, WASM) — existing
- ✓ Metrics/monitoring with Micrometer Prometheus — existing
- ✓ Security headers (CSRF, CORS, caching, HTTPS redirect) — existing
- ✓ Docker Compose for local PostgreSQL — existing
- ✓ Gradle version catalog and convention plugins — existing
- ✓ Context receiver pattern for dependency propagation — existing

### Active

<!-- Current scope. Building toward these. -->

- [ ] Complete auth flow (signup, login, logout, token refresh, session persistence)
- [ ] User management (CRUD, profiles, roles)
- [ ] Koin DI wired across all KMP targets (not just server)
- [ ] AI agent infrastructure with Koog (agent registry, tool system, conversation management, example agents)
- [ ] Client SDK layer returning Either/Result for all API calls
- [ ] Multiplatform navigation (Compose-based)
- [ ] Shared UI component library (buttons, inputs, cards, dialogs)
- [ ] Local storage / preferences (multiplatform)
- [ ] Setup CLI script (configure project name, package, DB credentials on clone)
- [ ] Sample dashboard screen demonstrating all template capabilities
- [ ] Structured logging (replace println with proper logging)

### Out of Scope

- Real-time features (WebSockets, SSE) — adds complexity beyond template scope
- Payment/billing integration — too domain-specific for a template
- CI/CD pipeline configuration — varies too much per team/platform
- iOS-specific UIKit views — Compose Multiplatform only
- Server-side rendering — API-first architecture

## Context

This is a brownfield project. The server infrastructure is partially built: Ktor server runs, JWT auth validates tokens, database connects via R2DBC with a migration system, and Koin is set up on the server side. The Compose Multiplatform client shell exists but has no real functionality beyond a greeting screen.

The existing codebase uses Kotlin context receivers (`-Xcontext-parameters`) for dependency propagation and Arrow's `ResourceScope` for lifecycle management. These patterns should be extended, not replaced.

Koog is JetBrains' Kotlin AI agent framework. The template should include a full agent infrastructure (registry, tools, conversation management) with example agents to demonstrate patterns.

The client SDK layer should abstract all networking behind clean Kotlin functions that return `Either<DomainError, T>`, so client code never deals with HTTP directly.

## Constraints

- **Tech stack**: Kotlin Multiplatform — no non-Kotlin dependencies for shared code
- **KMP targets**: Android (minSdk 24), iOS, JVM Desktop, WASM/JS Web
- **Server**: Ktor + Netty, must remain compatible with OpenAPI EAP branch
- **Database**: PostgreSQL via R2DBC (async) — no blocking JDBC in production paths
- **DI**: Koin across all targets
- **Error handling**: Arrow Either/Raise — no exception-based error handling for domain errors
- **UI**: Compose Multiplatform only — no platform-specific UI frameworks
- **Kotlin version**: 2.2.10 with context parameters enabled

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Koin for DI | KMP-native, lightweight, widely adopted in Kotlin ecosystem | — Pending |
| Either/Result SDK pattern | Explicit error handling, no surprise exceptions, clean caller contracts | — Pending |
| Koog for AI agents | JetBrains-native, Kotlin-first, designed for KMP ecosystem | — Pending |
| CLI setup script | Better onboarding than manual renaming; configurable project identity | — Pending |
| Component library (not full design system) | Right balance — useful components without over-engineering for a template | — Pending |
| Arrow context receivers | Already established in codebase, powerful for propagating typed contexts | ✓ Good |
| R2DBC over JDBC | Non-blocking database access, aligns with coroutine-first architecture | ✓ Good |

---
*Last updated: 2026-02-10 after initialization*
