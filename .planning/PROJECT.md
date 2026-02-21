# KMP Full-Stack Template

## What This Is

A production-ready Kotlin Multiplatform project template for building full-stack applications. It provides a Ktor backend and Compose Multiplatform clients (Android, iOS, Desktop, Web/WASM) with pre-wired infrastructure: JWT authentication with typed roles and group-based RBAC, database with migrations, Koin DI across all targets, MVI ViewModel architecture with testing DSL, AI agent support (Koog), a terminal-themed component library (41+ components), localization with runtime locale switching, and a shared SDK layer. Developers clone it, run a setup script, and start building features on top of a working app with auth, groups, admin panel, dashboard, and AI chat.

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
- ✓ MVI ViewModel base class with Intent/Model/Mutation/Event pipeline -- v1.1
- ✓ ViewModel state as StateFlow with pure reduce function -- v1.1
- ✓ One-shot events via SharedFlow (no double-firing on recomposition) -- v1.1
- ✓ ViewModels injectable via Koin across all KMP targets -- v1.1
- ✓ All 5 ViewModels migrated to MVI pattern -- v1.1
- ✓ Turbine-based ViewModel test DSL for intent/model/event assertions -- v1.1
- ✓ Group CRUD with admin management and data isolation -- v1.1
- ✓ Group membership with role-gated navigation and admin panel -- v1.1
- ✓ Admin can register new users into their group -- v1.1
- ✓ GroupApi SDK functions with Either<ClientError, T> and shared routes -- v1.1
- ✓ core:testing module with fake SDK builder, test fixtures, Kotest assertions -- v1.1
- ✓ Server integration tests via Ktor testApplication with Testcontainers -- v1.1
- ✓ StringKey enum bridging server and client localization -- v1.1
- ✓ Compose resource files with locale qualifiers (EN/ES) -- v1.1
- ✓ Server localized errors via Accept-Language header -- v1.1
- ✓ Runtime locale switching on all platforms -- v1.1
- ✓ StringKey-to-Compose Res.strings bridge function -- v1.1

### Active

**Current Milestone: v1.2 Polish & Patterns**

**Goal:** Add advanced AI patterns, file upload with profile images, group invitations, developer onboarding, and resolve tech debt — making the template production-complete and easy to adopt.

**Target features:**
- ~~Tech debt cleanup (integration tests, WASM locale persistence, Ktor dispatcher)~~ -- Phase 16 shipped
- ~~Infrastructure foundation (Docker services, config sections, pgvector table)~~ -- Phase 17 shipped
- AI patterns: structured output (JSON), RAG (pgvector), multi-agent orchestration, tool-use examples
- Group email invite links (admin sends invite, recipient joins via token)
- S3-compatible file uploads with user avatar/profile image feature
- Developer onboarding: CLI polish, dev docs, tooling shortcuts, first-run walkthrough

### Out of Scope

- Real-time features beyond AI chat WebSocket -- adds complexity beyond template scope
- Payment/billing integration -- too domain-specific for a template
- CI/CD pipeline configuration -- varies too much per team/platform
- iOS-specific UIKit views -- Compose Multiplatform only
- Server-side rendering -- API-first architecture
- Push notifications -- requires platform-specific accounts (FCM, APNs)
- Offline/caching -- adds significant complexity, not core to template value
- Granular permissions model -- existing 3-role system (SuperAdmin/Admin/Member) sufficient for template

## Context

Shipped v1.0 with 15,687 LOC Kotlin across 170 files in 7 days.
Shipped v1.1 with 21,583 LOC Kotlin across 248+ files in 4 days (+5,896 LOC).
Tech stack: Ktor + Netty, Exposed R2DBC, Arrow 2.2.0, Koin, Compose Multiplatform 1.9.3, Koog 0.6.2, Navigation Compose 2.9.1, Turbine 1.2.1.
Targets: Android (minSdk 24), iOS, JVM Desktop, WASM/JS Web.

The template uses Kotlin context parameters (`-Xcontext-parameters`) for dependency propagation and Arrow's Raise API for all domain error handling. The client SDK abstracts all networking behind clean Kotlin functions returning `Either<DomainError, T>`. All ViewModels follow MVI pattern with typed Intent/Model/Mutation/Event and are testable via Turbine DSL.

Known tech debt: Koin DI runtime verification on all targets (human-needed), WASM production stability unconfirmed, missing integration tests for some group endpoints.

v1.2 infrastructure shipped: pgvector extension enabled in PostgreSQL, MinIO for S3-compatible storage, MailHog for SMTP testing, Env.S3 and Env.Email config sections, custom VectorColumnType for Exposed R2DBC, document_embeddings table with vector(768) column.

## Constraints

- **Tech stack**: Kotlin Multiplatform -- no non-Kotlin dependencies for shared code
- **KMP targets**: Android (minSdk 24), iOS, JVM Desktop, WASM/JS Web
- **Server**: Ktor + Netty, compatible with OpenAPI EAP branch
- **Database**: PostgreSQL via R2DBC (async) -- no blocking JDBC in production paths
- **DI**: Koin across all targets
- **Error handling**: Arrow Either/Raise -- no exception-based error handling for domain errors
- **UI**: Compose Multiplatform only -- no platform-specific UI frameworks
- **Kotlin version**: 2.3.10 with context parameters enabled

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
| MVI ViewModel base class | ~30 LOC, template owns its patterns, no third-party dep | ✓ Good -- unified Either pipeline, typed generics |
| SDK interface extraction | Interfaces for fakes, Impl suffix for concrete classes | ✓ Good -- clean test substitution via fakeSdk {} |
| Turbine test DSL | Statement queuing pattern, auto-consume initial state | ✓ Good -- concise ViewModel test authoring |
| SharingStarted.Lazily default | WhileSubscribed resets state on back-navigation timeout | ✓ Good -- no state loss on recomposition |
| Group RBAC on server | Data isolation per group, membership-scoped access | ✓ Good -- cross-group requests return 403 |
| StringKey enum (not code-gen) | Manual mapping sufficient for template scope | ✓ Good -- shared between server and client |
| java.util.Locale for locale switching | Avoids AppCompat dependency, Compose Resources respects JVM default | ✓ Good -- works on Android/JVM/WASM |
| Pre-WASM localStorage for locale | Read persisted locale before Compose Resources initializes | ✓ Good -- closes timing gap, locale survives refresh |
| Named bounded dispatchers | Separate IO views for DB (16), AI (8), compute (Default) | ✓ Good -- prevents AI stream starvation of DB queries |
| Fire-and-forget agent cleanup | CoroutineScope.launch instead of runBlocking in awaitClose | ✓ Good -- no Netty event loop blocking |
| pgvector/pgvector:pg15 image | Drop-in replacement for postgres:15-alpine, includes vector extension | ✓ Good -- no separate vector DB needed |
| MinIO sidecar bucket creation | Ephemeral minio/mc container creates bucket then exits | ✓ Good -- zero-config local dev |
| Custom VectorColumnType | Exposed R2DBC has no native vector support, custom ColumnType needed | ✓ Good -- maps List<Float> to/from pgvector |
| TransactionManager.current().exec() | Raw SQL in migrations since exec is on R2dbcTransaction not top-level | ✓ Good -- enables pgvector extension in migration |
| Vector dimension 768 | Matches Google text-embedding-004 output (Koog/Gemini stack) | TBD -- verify against actual Koog embedding API |

---
*Last updated: 2026-02-21 after Phase 17 Infrastructure Foundation*
