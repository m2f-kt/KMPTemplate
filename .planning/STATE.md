---
gsd_state_version: 1.0
milestone: v1.2
milestone_name: Polish & Patterns
status: unknown
last_updated: "2026-03-02T00:45:25.371Z"
progress:
  total_phases: 14
  completed_phases: 14
  total_plans: 67
  completed_plans: 67
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-21)

**Core value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.
**Current focus:** Phase 21 complete — all 10 plans shipped including gap closure round 2

## Current Position

Milestone: v1.2 Polish & Patterns
Phase: 21 (Group Invitations & Profiles) — COMPLETE
Plan: 10 of 10 completed in Phase — all plans shipped
Status: Phase 21 fully complete, ready for Phase 22
Last activity: 2026-03-02 - Completed quick task 8: fix invitation table row vertical alignment and column spacing

Progress:
- v1.0 MVP: [████████████████████] 100% (39 plans) -- shipped 2026-02-17
- v1.1 Architecture: [████████████████████] 100% (34 plans) -- shipped 2026-02-21
- v1.2 Polish & Patterns: [████████████████░░░░] 24/TBD plans

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

  - [Phase 21]: Resend = revoke old + create new invitation (not just extend expiry) for clean audit trail
  - [Phase 21]: Resend only allowed for expired/revoked invitations (active ones should be revoked first)
  - [Phase 21]: HTTP 410 mapped to ServerMapped (same as 422) to preserve server error code for StringKey resolution
  - [Phase 21]: All 5 StringKeyResolver files must be updated simultaneously when adding StringKey entries
  - [Phase 21]: Invitation email pre-fill uses separate invitationEmail field to distinguish locked vs user-typed state
  - [Phase 21]: TerminalInput enabled param controls email lock — no design system changes needed
  - [Phase 21]: HTTP 422 (unprocessable) for email mismatch — validation error, not auth/gone
  - [Phase 21]: Resend = delete old + create new (not revoke old) to eliminate duplicate invitation rows
  - [Phase 21]: Remove duplicate AuthService from authModule — serverModule is sole registrar with invitation callback (single-registration pattern)
  - [Phase 21]: Cross-reference member emails at query time in listInvitations — override stale invitation status for existing members
  - [Phase 21]: markAcceptedByGroupAndEmail uses case-insensitive email matching via Exposed lowerCase()

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

| # | Description | Date | Commit | Status | Directory |
|---|-------------|------|--------|--------|-----------|
| 1 | Fix dashboard display name to show user name instead of hardcoded email | 2026-02-21 | f0e3dcc | | [1-fix-dashboard-display-name-to-show-user-](./quick/1-fix-dashboard-display-name-to-show-user-/) |
| 3 | as an admin I would like to remove users from my group | 2026-03-02 | 4ff65aa | Verified | [3-as-an-admin-i-would-like-to-remove-users](./quick/3-as-an-admin-i-would-like-to-remove-users/) |
| 4 | Fix admin group member view: translate remove member, remove register CTA, format dates | 2026-03-02 | aa6623a | Verified | [4-fix-admin-group-member-view-translate-re](./quick/4-fix-admin-group-member-view-translate-re/) |
| 5 | Fix date format to use numeric month (DD/MM/YYYY) | 2026-03-02 | 105dc4e | Verified | [5-fix-date-format-to-use-numeric-month-dd-](./quick/5-fix-date-format-to-use-numeric-month-dd-/) |
| 6 | Make admin panel tables mobile-responsive | 2026-03-02 | 6dcf1e8 | Complete | [6-make-admin-panel-tables-mobile-responsiv](./quick/6-make-admin-panel-tables-mobile-responsiv/) |
| 7 | Hide resend action for accepted invitations | 2026-03-02 | b13c283 | Complete | [7-hide-resend-action-for-accepted-invitati](./quick/7-hide-resend-action-for-accepted-invitati/) |
| 8 | Fix invitation table row vertical alignment and column spacing | 2026-03-02 | 536aea2 | Complete | [8-fix-invitation-table-row-vertical-alignm](./quick/8-fix-invitation-table-row-vertical-alignm/) |

## Session Continuity

Last session: 2026-03-02
Stopped at: Completed quick task 8: fix invitation table row vertical alignment and column spacing
Resume file: None
Next action: Begin Phase 22 (Developer Onboarding) or any other priority
