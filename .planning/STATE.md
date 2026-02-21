# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-21)

**Core value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.
**Current focus:** Phase 18 — Core Services (v1.2 Polish & Patterns)

## Current Position

Milestone: v1.2 Polish & Patterns
Phase: 18 of 22 (Core Services)
Plan: —
Status: Ready to plan
Last activity: 2026-02-21 — Phase 17 complete, 4 plans shipped (2 Phase 16 + 2 Phase 17)

Progress:
- v1.0 MVP: [████████████████████] 100% (39 plans) -- shipped 2026-02-17
- v1.1 Architecture: [████████████████████] 100% (34 plans) -- shipped 2026-02-21
- v1.2 Polish & Patterns: [████░░░░░░░░░░░░░░░░] 4/TBD plans

## Performance Metrics

**Velocity (v1.0):**
- Total plans completed: 39
- Average duration: ~8 min
- Total execution time: ~315 min

**Velocity (v1.1):**
- Total plans completed: 34
- Average duration: ~6 min
- Total execution time: ~170 min

**Combined:** 75 plans shipped, ~493 min total

## Accumulated Context

### Decisions

All v1.0 and v1.1 decisions archived in PROJECT.md Key Decisions table and milestones/ archives.

v1.2 decisions:
- Phase 16: Pre-WASM localStorage read for locale persistence (closes timing gap)
- Phase 16: Named bounded dispatchers (db=16, ai=8, compute=Default) with documented rationale
- Phase 16: Fire-and-forget agent cleanup via CoroutineScope.launch (no runBlocking)
- Phase 17: pgvector/pgvector:pg15 as drop-in replacement for postgres:15-alpine
- Phase 17: MinIO sidecar pattern for automatic bucket creation on first run
- Phase 17: TransactionManager.current().exec() for raw SQL in Exposed R2DBC migrations
- Phase 17: Vector dimension 768 matches Google text-embedding-004 (Koog/Gemini stack)
- Phase 17: metadata column uses text (not JSONB) to avoid R2DBC driver issues
- RAG pipeline exclusively Koog-based (no LangChain4j, Spring AI)
- pgvector in existing PostgreSQL (no separate vector DB service)
- MinIO for local S3-compatible storage
- MailHog for local SMTP testing
- Integration tests ship WITH feature phases, not deferred

### Blockers/Concerns

- Open: Ktor testApplication dispatcher issue (KTOR-7121) -- workaround exists
- Open: WASM production build stability unconfirmed (dev works)
- Open: Koog RAG/embedding APIs must be verified against latest docs (RAG-07)
- Resolved: Exposed R2DBC has no native vector column type -- custom VectorColumnType created in Phase 17

### Quick Tasks Completed

| # | Description | Date | Commit | Directory |
|---|-------------|------|--------|-----------|
| 1 | Fix dashboard display name to show user name instead of hardcoded email | 2026-02-21 | f0e3dcc | [1-fix-dashboard-display-name-to-show-user-](./quick/1-fix-dashboard-display-name-to-show-user-/) |

## Session Continuity

Last session: 2026-02-21
Stopped at: Phase 17 complete, auto-advancing to Phase 18
Resume file: .planning/phases/17-infrastructure-foundation/17-VERIFICATION.md
Next action: `/gsd:discuss-phase 18` or `/gsd:plan-phase 18`
