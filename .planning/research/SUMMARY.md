# Project Research Summary

**Project:** KMP Full-Stack Template — Milestone v1.2 (Polish & Patterns)
**Domain:** Kotlin Multiplatform Full-Stack (Android/iOS/Desktop/WASM + Ktor Server) — AI patterns, file uploads, email invitations, developer onboarding
**Researched:** 2026-02-21
**Confidence:** HIGH

## Executive Summary

Milestone v1.2 extends the KMP full-stack template (Kotlin 2.3.10, Compose Multiplatform 1.10.1, Ktor 3.4.0, Exposed 1.0.0 R2DBC, Koog 0.6.2, Arrow 2.2.1.1) with five capability areas: **advanced AI patterns** (structured output, RAG with pgvector, multi-agent orchestration), **S3-compatible file storage** (MinIO dev / AWS S3 prod) with profile image uploads, **email invitations** for groups via SMTP, **developer onboarding** tooling, and **tech debt cleanup** (integration tests, WASM locale persistence, Ktor dispatcher). The existing codebase provides a strong foundation with working AI chat/assistant agents, full RBAC, groups CRUD, MVI ViewModels, and an SDK with Arrow error handling. What's missing is: vector search, file upload infrastructure, email delivery, and polished developer experience.

The recommended approach is **infrastructure-first, then features, then polish**: start with Docker infrastructure changes (pgvector image, MinIO, MailHog) and config additions (`Env.S3`, `Env.Email`, `Env.Embedding`), then build the two new server modules (`server:files`, `server:email`) in parallel, then layer integration features on top (RAG pipeline, email invitations, structured output), then compose the multi-agent orchestrator that unifies all AI capabilities, then wire the client SDK and UI, and finally polish developer onboarding. Six new library additions are needed (AWS SDK Kotlin S3, Simple Java Mail, Coil 3, Koog embeddings/RAG modules) plus three Docker services. The approach keeps pgvector inside PostgreSQL (no separate vector DB), uses presigned URLs for scalable file uploads, and exposes RAG as an agent tool rather than baking it into system prompts.

The dominant risks are **infrastructure integration gaps** rather than conceptual complexity: Exposed R2DBC has no native vector column type (requires a custom `VectorColumnType` bridging to R2DBC's native `io.r2dbc.postgresql.codec.Vector`), WASM has no file system access (requires `PlatformFile` expect/actual), email SMTP is blocking I/O that can starve the R2DBC event loop (requires background Channel-based sending), and multi-agent orchestration can deadlock if agents share a single `PromptExecutor` (requires per-agent executors or message-passing). All four are preventable with upfront design but expensive to retrofit.

## Key Findings

### Recommended Stack

Six new library dependencies and three Docker services. All additions are verified against Maven Central with version compatibility confirmed against the existing stack. The Koog ecosystem (already at 0.6.2) provides embeddings and RAG modules that extend the existing AI infrastructure without introducing a new framework. See [STACK.md](./STACK.md) for full version matrix and compatibility details.

**New server-side dependencies:**
- `aws.sdk.kotlin:s3` 1.6.22 — S3/MinIO file operations with idiomatic Kotlin coroutine API. JVM-only, used in `server:files`.
- `org.simplejavamail:simple-java-mail` 8.12.6 — Clean SMTP email sending with builder API and async support. Replaces console-logged password resets.
- `ai.koog:koog-agents-ext-embeddings` 0.6.2 — `LLMEmbedder` for generating text embeddings via OpenAI/Ollama/Bedrock. Same Koog version already in project.
- `ai.koog:koog-agents-ext-rag` 0.6.2 — `VectorStorage` and `RankedDocumentStorage` interfaces for RAG pipeline. Custom pgvector backend implementation needed.

**New client-side dependencies:**
- `io.coil-kt.coil3:coil-compose` + `coil-network-ktor3` 3.3.0 — Image loading for Compose Multiplatform (all targets: Android, iOS, Desktop, WASM). Uses existing Ktor Client as HTTP engine.

**New Docker services:**
- `pgvector/pgvector:pg15` — Drop-in replacement for `postgres:15-alpine`, adds vector column type and similarity operators.
- `minio/minio:latest` — S3-compatible local object storage (API port 9000, console 9001).
- `mailhog/mailhog:latest` — SMTP mock that catches all outbound email (SMTP 1025, web UI 8025).

**What NOT to add:** Qdrant/Weaviate/Pinecone (pgvector sufficient), LangChain4j (Koog is the Kotlin-native equivalent), MinIO Java SDK (AWS SDK Kotlin works with MinIO via endpoint override), SendGrid/Mailgun SDK (SMTP is universal), Kamel (Coil 3 is the standard), complex template engines for email (Kotlin string templates sufficient).

### Expected Features

See [FEATURES.md](./FEATURES.md) for full feature landscape, dependency graph, and prioritization matrix.

**Must have (table stakes for v1.2):**
- Structured AI output endpoint — `executeStructured<T>()` with `@Serializable` + `@LLMDescription` data classes. Lowest complexity AI feature; validates the pattern.
- S3 file upload infrastructure — MinIO in Docker, `Env.S3` config, presigned URL upload/download flow.
- Profile image upload + TerminalAvatar image support — `avatarUrl` column, Coil 3 `AsyncImage`, circular avatar with fallback to initials.
- SMTP email service — replaces console-logged password resets, enables invitation delivery.
- Email invitation flow — admin sends invite → recipient clicks token link → joins group. Token lifecycle: pending → accepted/expired/revoked.
- RAG pipeline with pgvector — document embed → store in pgvector → cosine similarity search → inject context into LLM prompt.
- Multi-agent orchestration — coordinator agent uses structured output to route messages to specialist subgraphs (chat, assistant, RAG).
- Integration tests per feature — each feature ships with tests, not deferred.
- Developer onboarding CLI + documentation.

**Should have (differentiators):**
- Group-scoped RAG — documents scoped to groups, queries filtered by group membership.
- Document ingestion pipeline — PDF/text upload with chunking and automatic embedding.
- Email HTML templates — branded invitation and password reset emails.
- Invite status dashboard — admin UI showing pending/accepted/expired invites.

**Defer to v2+:**
- On-device embeddings, advanced RAG (hybrid search, re-ranking), agent long-term memory, file versioning, real-time agent collaboration, image generation.

### Architecture Approach

Two new server modules (`server:files`, `server:email`) plus significant extensions to `server:ai`, `server:auth`, and `server:groups`. Cross-module communication uses interfaces in `server:core:config` wired via Koin (e.g., `UserProfileUpdater` lets `server:files` update avatar URLs without depending on `server:auth` directly). The presigned URL pattern keeps file bytes off the server entirely. RAG is exposed as a Koog `@Tool` that agents invoke on demand, keeping system prompts bounded. The multi-agent orchestrator uses structured output for routing decisions, then delegates to specialist agents. See [ARCHITECTURE.md](./ARCHITECTURE.md) for full module graph and data flow diagrams.

**Major components:**
1. `server:files` — S3 file operations (presigned URL generation, upload confirmation, avatar management). Depends on AWS SDK Kotlin.
2. `server:email` — SMTP email sending with HTML templates and async send queue via `Channel` + `Dispatchers.IO`. Uses Simple Java Mail.
3. `server:ai` (extended) — RAG pipeline (`PgVectorStorage`, `EmbeddingsTable`, `RagService`, `RagTools`), structured output integration, `OrchestratorAgentService` for multi-agent routing.
4. `server:groups` (extended) — Email invitation flow (`GroupInvitesTable`, `GroupInviteService`, invite/accept endpoints).
5. `server:auth` (minor) — `avatarUrl` column migration, `UserResponse` DTO update.
6. Client-side — `FileApi` + `InviteApi` in SDK, `ProfileViewModel` avatar upload, `TerminalAvatar` image support via Coil 3, invite management in `app:admin`.

**Key architectural patterns:**
- Presigned URL upload (server never touches file bytes)
- Custom Exposed `VectorColumnType` bridging R2DBC's native pgvector codec
- Service interfaces for cross-module communication (avoid circular dependencies)
- Coordinator-delegator multi-agent with structured output routing
- Background email sending via Channel (never block request handlers)

### Critical Pitfalls

See [PITFALLS.md](./PITFALLS.md) for 12 detailed pitfalls with code examples, warning signs, and recovery strategies.

1. **Exposed R2DBC has no native vector column type** — Must write a custom `VectorColumnType` that handles R2DBC's `io.r2dbc.postgresql.codec.Vector` in `valueFromDB`. Similarity search requires raw SQL (`<=>` operator not in Exposed DSL). Fallback: store as TEXT with SQL casting if custom type proves unreliable. *Phase: RAG/pgvector — blocking infrastructure.*

2. **Docker image must change to pgvector/pgvector:pg15** — Current `postgres:15-alpine` has no pgvector extension. `CREATE EXTENSION vector` silently fails if extension isn't installed. Must be the first infrastructure change. *Phase: RAG/pgvector — prerequisite.*

3. **WASM has no file system access for uploads** — Browser `File` API provides `Blob`/`ArrayBuffer`, not file paths. Need `PlatformFile` expect/actual abstraction per target. Profile images must be resized client-side before upload. *Phase: S3/File Upload — design before any upload code.*

4. **Email SMTP blocks the R2DBC event loop** — JavaMail's `Transport.send()` is blocking I/O. Must use `Channel<EmailJob>` with `Dispatchers.IO` consumer. Synchronous email in request handlers causes 2-5 second endpoint latency. *Phase: Email Invitations — infrastructure before any send calls.*

5. **Multi-agent orchestration deadlocks on shared executor** — If agents share a single `SingleLLMPromptExecutor` with concurrency limits, nested agent calls deadlock on the semaphore. Need per-agent executors or message-passing communication. *Phase: Multi-Agent — architecture decision before coding.*

6. **S3 internal URLs leak in database** — Storing `http://minio:9000/...` URLs instead of object keys means images don't load outside Docker network. Store keys only, resolve URLs at response time via `Env.S3.publicUrl`. *Phase: S3/File Upload — URL strategy with upload design.*

7. **Invitation token security** — Must hash tokens (SHA-256) before storage, enforce expiry (7 days), prevent reuse (PENDING→ACCEPTED atomically), and rate-limit creation (max 50/group/day). *Phase: Email Invitations — schema design.*

## Implications for Roadmap

Based on the dependency graph from FEATURES.md and build order from ARCHITECTURE.md, a **7-phase structure** is recommended. Infrastructure comes first because three independent feature areas (AI, files, email) all depend on Docker and config changes. Features are then built bottom-up following dependency chains.

### Phase 1: Tech Debt Cleanup
**Rationale:** Clean up known issues before building new features. Integration test infrastructure is needed for every subsequent phase. Dispatcher and locale fixes are small, independent items best addressed early.
**Delivers:** Integration test base with Testcontainers, WASM locale persistence fix, Ktor dispatcher configuration review, `CoroutineDispatcher` injection via Koin.
**Addresses:** Tech debt items (integration tests, WASM locale, Ktor dispatcher)
**Avoids:** Building new features on a shaky foundation; deferring test infrastructure leads to untested features.

### Phase 2: Infrastructure Foundation
**Rationale:** Docker, config, shared types, and utility classes have zero runtime dependencies on each other and unblock all subsequent feature work. Building these in parallel maximizes throughput.
**Delivers:** `docker-compose.yml` changes (pgvector image, MinIO, MailHog), `Env.S3`/`Env.Email`/`Env.Embedding` config sections, custom `VectorColumnType` in `server:core:database`, new DTOs in `core:models` (`FileUploadResponse`, `InviteRequest`, `RagQueryRequest`, etc.), version catalog additions.
**Addresses:** All infrastructure prerequisites from STACK.md.
**Avoids:** Docker image missing pgvector (Pitfall 2), port conflicts (Pitfall 11 — use docker-compose profiles for MailHog).

### Phase 3: Core Services (File Storage + Email)
**Rationale:** `server:files` and `server:email` are the two new server modules that Phase 4 features consume. They are independent of each other and can be built in parallel. Avatar URL migration in `server:auth` is a minimal change that unblocks client avatar display.
**Delivers:** `server:files` module (S3Client, FileRepository, FileService with presigned URL flow), `server:email` module (EmailService with Channel-based async sending, EmailTemplateEngine), `server:auth` V6_AddAvatarUrl migration.
**Uses:** AWS SDK Kotlin S3 (new), Simple Java Mail (new).
**Avoids:** Server-proxied uploads for large files (Pitfall 5 — presigned URLs), email blocking event loop (Pitfall 8 — Channel + Dispatchers.IO), S3 internal URL leaking (Pitfall 9 — store keys not URLs).

### Phase 4: Integration Features (AI Patterns + Invitations)
**Rationale:** RAG, structured output, and email invitations all depend on Phase 2 infrastructure and Phase 3 services. RAG needs VectorColumnType + Env.Embedding; invitations need EmailService + GroupsTable. Structured output is independent but validates the Koog pattern before the more complex RAG and multi-agent work.
**Delivers:** Structured output endpoint (`POST /api/ai/structured` with `executeStructured<T>()`), RAG pipeline (EmbeddingsTable, PgVectorStorage, RagService, RagTools as Koog `@Tool`), email invitation flow (GroupInvitesTable, GroupInviteService, invite/accept endpoints with token security).
**Uses:** Koog embeddings + RAG modules (new), pgvector Docker image (Phase 2).
**Avoids:** Custom column type failures (Pitfall 1 — test vector insert/read first), embedding dimension mismatch (Pitfall 7 — store model_name, use named constant), invitation token reuse (Pitfall 10 — hash tokens, enforce expiry atomically), poor RAG chunking (Pitfall 12 — overlap strategy with semantic boundaries).

### Phase 5: Multi-Agent Orchestration
**Rationale:** The capstone AI feature composes structured output (routing) and RAG (document-aware agent) from Phase 4. Cannot be built until both exist. This is the highest-complexity phase with the most novel patterns.
**Delivers:** `OrchestratorAgentService` with `RoutingDecision` structured output, specialist subgraphs (chat, assistant/tools, RAG), modified WebSocket endpoint routing through orchestrator.
**Implements:** Coordinator-delegator pattern from ARCHITECTURE.md.
**Avoids:** Multi-agent deadlock (Pitfall 6 — per-agent executors, timeouts on delegation), monolithic agent anti-pattern (Architecture anti-pattern 5 — focused specialist agents with scoped tool sets).

### Phase 6: SDK + Client UI
**Rationale:** Client-side work consumes the server APIs built in Phases 3-5. SDK extensions (`FileApi`, `InviteApi`) must exist before UI can be built. Client work is parallelizable once SDK is ready.
**Delivers:** `FileApi` + `InviteApi` in `core:sdk`, fakes in `core:testing`, `ProfileViewModel` avatar upload flow, `TerminalAvatar` image support with Coil 3, invite management UI in `app:admin`, `PlatformFile` expect/actual for all targets.
**Uses:** Coil 3 compose + network-ktor3 (new).
**Avoids:** WASM file upload failure (Pitfall 4 — `PlatformFile` expect/actual designed upfront), image URL resolution issues (Pitfall 9 — `Env.S3.publicUrl` for client-accessible URLs).

### Phase 7: Developer Onboarding
**Rationale:** Must come last because it documents and automates all infrastructure from Phases 1-6. Writing docs for features that don't exist is waste. CLI polish depends on knowing the final Docker service set and configuration shape.
**Delivers:** CLI first-run walkthrough (prerequisites check, Docker startup, migrations, seed data), dev documentation (ARCHITECTURE.md, ADDING-A-FEATURE.md, AI-FEATURES.md), tooling shortcuts (`./gradlew devUp`, `./gradlew seedData`), `.env.example` with all environment variables.
**Addresses:** Developer onboarding features from FEATURES.md.
**Avoids:** MinIO bucket not existing on first run (Pitfall 11 — minio-init container), incomplete documentation (written after all features are stable).

### Phase Ordering Rationale

- **Tech debt first (Phase 1)** because integration test infrastructure is consumed by every subsequent phase. Each feature phase should ship with tests.
- **Infrastructure before features (Phase 2)** because Docker, config, and shared types have zero dependencies on each other and unblock three parallel work streams (files, email, AI).
- **Core services before integration features (Phase 3 before 4)** because invitation flow needs EmailService and RAG needs file upload for document ingestion.
- **Structured output before RAG and multi-agent (Phase 4)** because it's the simplest AI pattern, validates Koog structured output, and the multi-agent router depends on it for routing decisions.
- **Multi-agent after RAG + structured output (Phase 5)** because the orchestrator composes both capabilities.
- **Client after server (Phase 6 after 3-5)** because SDK and UI consume server APIs.
- **Onboarding last (Phase 7)** because it documents everything above.

### Research Flags

**Phases likely needing deeper research during planning:**
- **Phase 4 (RAG/pgvector):** Custom `VectorColumnType` for Exposed R2DBC is the highest-risk integration point. The exact behavior of R2DBC's `io.r2dbc.postgresql.codec.Vector` codec with Exposed's `valueFromDB`/`notNullValueToDB` needs a spike test. Koog RAG module's `VectorStorage` interface contract needs implementation-time verification.
- **Phase 5 (Multi-Agent Orchestration):** Koog subgraph composition patterns are less documented than single-agent usage. Agent-to-agent communication pattern (message-passing vs. tool-based delegation vs. nested graph execution) needs design validation with a prototype.
- **Phase 6 (Client File Upload):** `PlatformFile` expect/actual across 4 KMP targets (Android `Uri`, JVM `File`, WASM `Blob`, iOS `NSData`) requires platform-specific research for each target's file picker API.

**Phases with standard patterns (skip deep research):**
- **Phase 1 (Tech Debt):** Integration tests with Testcontainers follow established patterns already partially in the codebase.
- **Phase 2 (Infrastructure Foundation):** Docker Compose changes, config data classes, and DTO additions are mechanical.
- **Phase 3 (Core Services):** S3 presigned URL flow and SMTP email sending are well-documented patterns with official SDK examples.
- **Phase 7 (Developer Onboarding):** Documentation and CLI scripting follow standard conventions.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | All versions verified against Maven Central. AWS SDK 1.6.22 released Feb 20, 2026. Coil 3.3.0 and Simple Java Mail 8.12.6 confirmed. Koog embeddings/RAG artifacts exist on Maven Central. One MEDIUM item: Koog embeddings exact artifact ID may need `embeddings-base` + `embeddings-llm` separately. |
| Features | HIGH | Grounded in codebase analysis — every existing capability inventoried, every gap identified. Feature dependency graph validated against module structure. Anti-features clearly identified with rationale. |
| Architecture | HIGH | Module structure mirrors existing conventions. Build order validated against actual dependency chains. All patterns backed by official docs or existing working code. Cross-module interfaces (UserProfileUpdater, DocumentStore) prevent circular dependencies. |
| Pitfalls | MEDIUM-HIGH | Critical pitfalls (vector column type, Docker image, email blocking) verified against codebase patterns (e.g., existing TEXT workaround for JSONB in ConversationsTable). Security pitfalls (token hashing, EXIF stripping, S3 URL leaking) based on OWASP guidelines. One MEDIUM item: Exposed R2DBC custom column type reliability needs empirical validation. |

**Overall confidence:** HIGH

### Gaps to Address

- **Exposed R2DBC `VectorColumnType` reliability:** The custom column type bridging Exposed to R2DBC's native pgvector codec is the single highest-risk integration. The existing codebase already uses a TEXT workaround for JSONB (ConversationsTable), suggesting R2DBC type mapping has gaps. **Action:** Spike test in Phase 4 before writing any RAG feature code. If unreliable, fall back to TEXT storage with SQL casting.

- **Koog embeddings/RAG exact Maven artifact IDs:** Confirmed on Maven Central but page rendering issues during research. May need `embeddings-base` + `embeddings-llm` as separate dependencies instead of umbrella `ext-embeddings`. **Action:** Resolve during Phase 2 version catalog additions.

- **Coil 3.3.0 + Ktor 3.4.0 compatibility:** Coil 3.3.0 was built before Ktor 3.4.0 release. The `coil-network-ktor3` module targets Ktor 3.x generally but exact 3.4.0 compatibility is unverified. **Action:** Build verification in Phase 6 before committing to Coil for image loading.

- **WASM file picker integration:** KMP has no standard file picker API. Each platform needs its own implementation. WASM specifically needs `kotlinx-browser` `FileReader.readAsArrayBuffer()` for reading `Blob` data. **Action:** Platform-specific research during Phase 6 planning.

- **Koog multi-agent subgraph composition:** Koog 0.6.2's subgraph system exists but complex composition patterns (nested graphs, shared executors, agent-to-agent communication) are less documented. **Action:** Prototype spike in Phase 5 before designing the full orchestrator.

## Sources

### Primary (HIGH confidence)
- [pgvector/pgvector Docker Hub](https://hub.docker.com/r/pgvector/pgvector) — Official pgvector Docker images
- [pgvector-java GitHub](https://github.com/pgvector/pgvector-java) — R2DBC 1.0.3+ native vector support confirmed
- [AWS SDK Kotlin S3 Maven Central](https://central.sonatype.com/artifact/aws.sdk.kotlin/s3) — v1.6.22, Feb 20, 2026
- [Coil 3 GitHub](https://github.com/coil-kt/coil/releases) — v3.3.0, KMP multiplatform confirmed
- [Simple Java Mail](https://www.simplejavamail.org/) — v8.12.6, builder API, async sending
- [Koog Official Docs](https://docs.koog.ai) — Structured output, embeddings, RAG, subgraphs
- [Koog Embeddings Maven Central](https://central.sonatype.com/artifact/ai.koog/koog-agents-ext-embeddings) — Artifact exists
- [Koog RAG Maven Central](https://central.sonatype.com/artifact/ai.koog/koog-agents-ext-rag) — Artifact exists

### Secondary (MEDIUM confidence)
- Koog embeddings/RAG exact artifact structure — confirmed on Maven Central but may need separate base + llm artifacts
- Coil 3.3.0 ABI compatibility with Kotlin 2.3.10 — built against 2.2.0, should be compatible
- AWS SDK Kotlin 1.6.22 ABI compatibility with Kotlin 2.3.10 — built against 2.3.0, should be compatible
- Koog multi-agent subgraph composition patterns — API exists, complex patterns less documented

### Tertiary (LOW confidence — validate during implementation)
- Exposed R2DBC custom `VectorColumnType` with `float[]` parameter binding — may need raw SQL fallback
- Koog `VectorStorage` interface exact contract — confirmed in docs, needs implementation-time verification
- Coil `coil-network-ktor3` exact compatibility with Ktor 3.4.0 — likely compatible but unverified
- WASM `PlatformFile` implementation details — `kotlinx-browser` FileReader API usage patterns

---
*Research completed: 2026-02-21*
*Ready for roadmap: yes*
