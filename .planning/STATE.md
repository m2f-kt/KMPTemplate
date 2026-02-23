# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-21)

**Core value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.
**Current focus:** Phase 18.2 — Invitation Acceptance Flow (Gap Closure)

## Current Position

Milestone: v1.2 Polish & Patterns
Phase: 18.1 of 22 (Profile Uploads, Group Creation & Email Invitations)
Plan: 5 of 5 in Phase (COMPLETE)
Status: UAT Complete - Gap Identified
Last activity: 2026-02-23 — UAT completed with 1 gap requiring new phase

Progress:
- v1.0 MVP: [████████████████████] 100% (39 plans) -- shipped 2026-02-17
- v1.1 Architecture: [████████████████████] 100% (34 plans) -- shipped 2026-02-21
- v1.2 Polish & Patterns: [██████████████░░░░░░] 12/TBD plans

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
- Phase 18: ByteReadChannel.toByteArray() for Ktor 3.x multipart file reading (not deprecated readRemaining)
- Phase 18: arrow.core.raise.context.ensureNotNull for context-parameter Raise (not extension form)
- Phase 18: Multipart parsing outside conduitAuth — RoutingContext.call not available in Raise context block
- Phase 18: Jakarta Mail 2.0.2 for SMTP email with separate emailModule Koin module
- Phase 18: Email failure silencing in password reset (security: don't reveal email existence)
- Phase 18: GreenMail 2.1.3 for in-JVM SMTP testing (no Docker needed)
- Phase 18: URL-encode S3 fileKey in API calls (key contains '/' from userId prefix)
- Phase 18.1: InviteAccept files in auth root (not invite/ subdirectory) — matches module structure
- Phase 18.1: InviteAcceptRoute navigates to AdminPanelRoute on success
- Phase 18.1: MviViewModel uses eager StateFlow initialization (not lazy) to prevent mutation loss
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

### Roadmap Evolution

- Phase 18.1 inserted after Phase 18: Profile Uploads, Group Creation & Email Invitations (URGENT)
- **Phase 18.2 needed**: Invitation Acceptance Flow for Unauthenticated Users (Gap from 18.1 UAT)

### UAT Gaps Requiring New Phases

| Phase | Gap | Description | Priority |
|-------|-----|-------------|----------|
| 18.1 | #1 | Invitation acceptance flow for unauthenticated users | High |

**Gap #1 Details:**
- Current flow requires user to be authenticated to accept invitation
- Correct flow: Unauthenticated user can accept → redirects to login/register → auto-links to group after auth
- See `.planning/phases/18.1-profile-uploads-group-creation-email-invitations/18.1-UAT.md` for full requirements

### Quick Tasks Completed

| # | Description | Date | Commit | Directory |
|---|-------------|------|--------|-----------|
| 1 | Fix dashboard display name to show user name instead of hardcoded email | 2026-02-21 | f0e3dcc | [1-fix-dashboard-display-name-to-show-user-](./quick/1-fix-dashboard-display-name-to-show-user-/) |

## Session Continuity

Last session: 2026-02-23
Stopped at: UAT complete for Phase 18.1 — 8 passed, 1 failed (gap), 1 skipped
Resume file: .planning/phases/18.1-profile-uploads-group-creation-email-invitations/18.1-UAT.md
Next action: Create Phase 18.2 to close invitation acceptance gap, OR proceed to Phase 19
