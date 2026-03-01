---
gsd_state_version: 1.0
milestone: v1.0
milestone_name: Polish & Patterns
status: unknown
last_updated: "2026-03-01T17:42:45Z"
progress:
  total_phases: 13
  completed_phases: 13
  total_plans: 57
  completed_plans: 57
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-21)

**Core value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.
**Current focus:** Phase 21 gap closure plan 04 complete — Spanish translations + register-with-invite navigation fix

## Current Position

Milestone: v1.2 Polish & Patterns
Phase: 21 (Group Invitations & Profiles)
Plan: 4 of N in Phase — COMPLETE
Status: Plan 21-04 complete — UAT gaps 1 & 2 closed
Last activity: 2026-03-01 — Phase 21 plan 04 executed

Progress:
- v1.0 MVP: [████████████████████] 100% (39 plans) -- shipped 2026-02-17
- v1.1 Architecture: [████████████████████] 100% (34 plans) -- shipped 2026-02-21
- v1.2 Polish & Patterns: [████████████████░░░░] 22/TBD plans

## Performance Metrics

**Velocity (v1.0):**
- Total plans completed: 39
- Average duration: ~8 min
- Total execution time: ~315 min

**Velocity (v1.1):**
- Total plans completed: 34
- Average duration: ~6 min
- Total execution time: ~170 min

**Combined:** 92 plans shipped, ~577 min total

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
- Phase 18.1: InviteAcceptRoute navigates to AdminPanelRoute on success (SUPERSEDED by 18.2-04: DashboardRoute)
- Phase 18.1: MviViewModel uses eager StateFlow initialization (not lazy) to prevent mutation loss
- RAG pipeline exclusively Koog-based (no LangChain4j, Spring AI)
- pgvector in existing PostgreSQL (no separate vector DB service)
- MinIO for local S3-compatible storage
- MailHog for local SMTP testing
- Integration tests ship WITH feature phases, not deferred
- [Phase 18.2]: Callback pattern instead of direct InvitationService dependency — avoids circular module dependency between server/auth and server/groups
- [Phase 18.2]: Koin dual registration: AuthService in authModule (default) + override in serverModule (with invitation callback) for test compatibility
- [Phase 18.2]: Post-login acceptInvitation call in LoginViewModel — on failure, falls back to dashboard (user is still logged in)
- [Phase 18.2]: Register with invitationToken navigates to dashboard for MVP (server auto-links via RegisterRequest)
- [Phase 18.2]: NavigateToGroup handlers use DashboardRoute (not AdminPanelRoute) -- invited users are regular members without admin permissions
- [Phase 18.2]: LoginRoute as expired invite escape destination -- user can log in/register after seeing expired message
- [Phase 19]: arrow.core.raise.context.ensure for Kotlin 2.x context parameters (not arrow.core.raise.ensure)
- [Phase 19]: AIAgentConfig is final class (not data class), construct new instance instead of copy()
- [Phase 19]: Lambda bridges for cross-module dependencies in documentRoutes (fileUploader, fileDeleter, roleChecker)
- [Phase 19]: Shared GoogleLLMClient singleton across agents, embedder, and structured output
- [Phase 19]: Auto-RAG detection via RelevanceDetector gates retrieval (avoids unnecessary embedding calls)
- [Phase 19]: RagService fails open on errors (returns null, never breaks chat)
- [Phase 19]: FakeEmbeddingProvider returning zero vectors for test LLMEmbedder construction
- [Phase 19]: TextEmbedding004 is a top-level val, not Models.TextEmbedding004

- [Phase 21]: Use getInvitation (read-only) instead of acceptInvitation for post-registration groupId lookup — avoids non-idempotent call
- [Phase 21]: Fall back to NavigateToDashboard on getInvitation failure — user IS in group, navigation is best-effort

### Blockers/Concerns

- Open: Ktor testApplication dispatcher issue (KTOR-7121) -- workaround exists
- Open: WASM production build stability unconfirmed (dev works)
- Resolved: Koog RAG/embedding APIs verified against 0.6.2 source JARs (RAG-07, Phase 19)
- Resolved: Exposed R2DBC has no native vector column type -- custom VectorColumnType created in Phase 17

### Roadmap Evolution

- Phase 18.1 inserted after Phase 18: Profile Uploads, Group Creation & Email Invitations (URGENT)
- **Phase 18.2 completed**: Invitation Acceptance Flow for Unauthenticated Users (Gap from 18.1 UAT) — verified 2026-02-23, all 5 plans complete 2026-02-25

### UAT Gaps Requiring New Phases

| Phase | Gap | Description | Priority |
|-------|-----|-------------|----------|
| 18.1 | #1 | ~~Invitation acceptance flow for unauthenticated users~~ | ✓ Closed (Phase 18.2) |

### Quick Tasks Completed

| # | Description | Date | Commit | Directory |
|---|-------------|------|--------|-----------|
| 1 | Fix dashboard display name to show user name instead of hardcoded email | 2026-02-21 | f0e3dcc | [1-fix-dashboard-display-name-to-show-user-](./quick/1-fix-dashboard-display-name-to-show-user-/) |

## Session Continuity

Last session: 2026-03-01
Stopped at: Completed 21-04-PLAN.md (gap closure: Spanish translations + register-with-invite navigation)
Resume file: None
Next action: Continue Phase 21 remaining plans or next phase
