# Roadmap: KMP Full-Stack Template

## Milestones

- ✅ **v1.0 MVP** -- Phases 1-9 (shipped 2026-02-17)
- ✅ **v1.1 Architecture** -- Phases 10-15 (shipped 2026-02-21)
- 🚧 **v1.2 Polish & Patterns** -- Phases 16-22 (in progress)

## Phases

<details>
<summary>✅ v1.0 MVP (Phases 1-9) -- SHIPPED 2026-02-17</summary>

- [x] Phase 1: Foundation & Module Structure (4/4 plans) -- completed 2026-02-10
- [x] Phase 2: Server Auth & Users (3/3 plans) -- completed 2026-02-11
- [x] Phase 3: Client SDK & Storage (3/3 plans) -- completed 2026-02-11
- [x] Phase 4: Navigation & UI Components (7/7 plans) -- completed 2026-02-12
- [x] Phase 5: Auth Screens, Dashboard & Setup CLI (11/11 plans) -- completed 2026-02-13
- [x] Phase 6: AI Agent Infrastructure (3/3 plans) -- completed 2026-02-13
- [x] Phase 6.1: Chat Agent Streaming Refactor (2/2 plans) -- completed 2026-02-14
- [x] Phase 7: Role System Refactor & Tech Debt (2/2 plans) -- completed 2026-02-15
- [x] Phase 8: Type-Safe Shared Routes (3/3 plans) -- completed 2026-02-15
- [x] Phase 9: WASM HTTP Engine Fix (1/1 plan) -- completed 2026-02-16

Full details: milestones/v1.0-ROADMAP.md

</details>

<details>
<summary>✅ v1.1 Architecture (Phases 10-15) -- SHIPPED 2026-02-21</summary>

- [x] Phase 10: MVI ViewModel Foundation (1/1 plan) -- completed 2026-02-17
- [x] Phase 11: Testing Infrastructure (3/3 plans) -- completed 2026-02-18
- [x] Phase 11.1: Fake SDK facade & fixes (2/2 plans) -- completed 2026-02-18
- [x] Phase 12: ViewModel Migration (7/7 plans) -- completed 2026-02-18
- [x] Phase 13: Group Server & SDK (4/4 plans) -- completed 2026-02-19
- [x] Phase 14: Group Admin UI (4/4 plans) -- completed 2026-02-19
- [x] Phase 15: Localization (13/13 plans) -- completed 2026-02-21

Full details: milestones/v1.1-ROADMAP.md

</details>

### 🚧 v1.2 Polish & Patterns (In Progress)

**Milestone Goal:** Add AI patterns (structured output, RAG, multi-agent), file uploads, group invitations, email infrastructure, and developer onboarding — completing the template's production-ready feature set.

- [x] **Phase 16: Tech Debt Cleanup** - Fix WASM locale persistence and optimize server dispatchers (completed 2026-02-21)
- [x] **Phase 17: Infrastructure Foundation** - Docker services (MinIO, MailHog, pgvector), config sections, vector storage table (completed 2026-02-21)
- [x] **Phase 18: Core Services** - S3 file upload service and SMTP email service with integration tests (completed 2026-02-22)
- [x] **Phase 18.1: Profile Uploads, Group Creation & Email Invitations** - Profile picture uploads from UI, group creation from admin panel, email invitations (completed 2026-02-22)
- [ ] **Phase 19: Structured AI & RAG Pipeline** - Structured output endpoint, Koog-exclusive RAG with pgvector, group-scoped retrieval
- [ ] **Phase 20: Multi-Agent Orchestration** - Router agent delegating to specialist agents with streaming API
- [ ] **Phase 21: Group Invitations & Profiles** - Email-based invite flow, profile avatars, SDK + client UI, end-to-end integration tests
- [ ] **Phase 22: Developer Onboarding** - Setup CLI enhancements, documentation, Gradle tooling, first-run walkthrough

## Phase Details

### Phase 16: Tech Debt Cleanup
**Goal**: Existing rough edges are smoothed before new features land
**Depends on**: Nothing (first phase of v1.2)
**Requirements**: DEBT-04, DEBT-05
**Success Criteria** (what must be TRUE):
  1. WASM user selects a locale, refreshes the page, and the selected locale persists (not reset to default)
  2. Server handles concurrent AI streaming + R2DBC database queries without thread starvation or timeouts
  3. Dispatcher configuration is explicit in code with documented rationale for AI vs DB workloads
**Plans**: 2 plans

Plans:
- [ ] 16-01-PLAN.md — Fix WASM locale persistence via pre-WASM localStorage read
- [ ] 16-02-PLAN.md — Named dispatcher configuration and runBlocking removal

### Phase 17: Infrastructure Foundation
**Goal**: All Docker services and configuration scaffolding exist so feature phases can build on them
**Depends on**: Phase 16
**Requirements**: FILE-02, FILE-03, EMAIL-02, EMAIL-03, RAG-01
**Success Criteria** (what must be TRUE):
  1. `docker compose up` starts PostgreSQL (with pgvector), MinIO (with default bucket), and MailHog — all healthy
  2. `Env.S3` config section loads endpoint, bucket, region, accessKey, secretKey from environment variables
  3. `Env.Email` config section loads SMTP host, port, credentials, fromAddress from environment variables
  4. A `document_embeddings` table exists in PostgreSQL with a vector column for storing embeddings
  5. Developer can open MailHog UI (port 8025) and MinIO console (port 9001) in browser after `docker compose up`
**Plans**: 2 plans

Plans:
- [x] 17-01-PLAN.md — Docker Compose services (MinIO, MailHog, pgvector) and Env config sections
- [x] 17-02-PLAN.md — Custom VectorColumnType and document_embeddings migration

### Phase 18: Core Services
**Goal**: Files can be uploaded to S3 and emails can be sent — the two infrastructure services other features depend on
**Depends on**: Phase 17 (Docker services and config must exist)
**Requirements**: FILE-01, FILE-04, EMAIL-01, EMAIL-04, DEBT-02
**Success Criteria** (what must be TRUE):
  1. Authenticated user can upload a file via API and it appears in MinIO bucket (verified via MinIO console)
  2. Server rejects files exceeding size limit or not in type whitelist with appropriate error response
  3. Password reset flow sends a real email visible in MailHog instead of printing to console
  4. Integration tests verify file upload + retrieval round-trip against MinIO (Testcontainers or local)
  5. EmailService interface has sendEmail method with working SMTP implementation that delivers to MailHog
**Plans**: 3 plans

Plans:
- [x] 18-01-PLAN.md — S3 file upload service (shared models, FileService, routes, DI wiring)
- [x] 18-02-PLAN.md — Email service + SDK FileApi + password reset migration
- [x] 18-03-PLAN.md — Integration tests (MinIO file round-trip, SMTP email delivery)

### Phase 18.1: Profile Uploads, Group Creation & Email Invitations (INSERTED)

**Goal:** Users can upload profile pictures, admins can create groups and send email invitations, invited users can accept and join groups
**Depends on:** Phase 18 (FileService for uploads, EmailService for invitations)
**Requirements:** PROF-01, PROF-02, INVITE-01, INVITE-02
**Success Criteria** (what must be TRUE):
  1. User can tap avatar in profile → select image → crop → upload → see new avatar displayed
  2. Admin can click "Create Group" in admin panel → enter name → group is created
  3. Admin can click "Invite Member" → enter email → invitation email sent with link
  4. Invited user clicks link → sees group name/inviter → accepts → joins as member
**Plans:** 5 plans in 3 waves

Plans:
- [x] 18.1-01-PLAN.md — Profile Avatar: Server (UsersTable.avatarUrl, upload endpoint, SDK)
- [x] 18.1-02-PLAN.md — Profile Avatar: UI (ImagePicker expect/actual, TerminalAvatar image, crop dialog)
- [x] 18.1-03-PLAN.md — Admin Group Creation: UI (modal dialog in admin panel)
- [x] 18.1-04-PLAN.md — Invitations: Server + SDK (InvitationsTable, service, routes, API)
- [x] 18.1-05-PLAN.md — Invitations: Admin UI + Accept Flow (invite dialog, InviteAcceptScreen)

### Phase 19: Structured AI & RAG Pipeline
**Goal**: AI can return typed structured responses, and chat can be augmented with group-scoped document context via Koog-exclusive RAG
**Depends on**: Phase 17 (pgvector table), Phase 18 (file upload for documents)
**Requirements**: AISTR-01, AISTR-02, AISTR-03, RAG-02, RAG-03, RAG-04, RAG-05, RAG-06, RAG-07, DEBT-03
**Success Criteria** (what must be TRUE):
  1. Client calls structured output endpoint with a prompt and receives a typed Kotlin data class (e.g., SentimentAnalysis) as Either.Right
  2. At least 2 example structured output schemas exist with @Serializable and @LLMDescription annotations
  3. User uploads a document → it is chunked, embedded via Koog LLMEmbedder, and stored as vectors in pgvector
  4. User asks a question in AI chat → relevant document chunks are retrieved via cosine similarity and injected into the prompt
  5. Group A member's documents are NOT returned when Group B member queries RAG — scope isolation verified
**Plans**: TBD

Plans:
- [ ] 19-01: TBD

### Phase 20: Multi-Agent Orchestration
**Goal**: AI chat supports multi-agent routing — user messages are analyzed and delegated to specialist agents
**Depends on**: Phase 19 (structured output + RAG agents exist as potential specialists)
**Requirements**: AGENT-01, AGENT-02, AGENT-03
**Success Criteria** (what must be TRUE):
  1. User sends a message and the router agent delegates to the correct specialist (at least 2 specialists with distinct behaviors)
  2. Each specialist agent has its own system prompt, tool set, and optionally different LLM configuration
  3. Multi-agent responses stream to the client via the existing streaming API endpoint
**Plans**: TBD

Plans:
- [ ] 20-01: TBD

### Phase 21: Group Invitations & Profiles
**Goal**: Admins can invite users by email, users can accept invites and upload profile avatars — the complete user lifecycle
**Depends on**: Phase 18 (email service for invites, file upload for avatars)
**Requirements**: INVITE-01, INVITE-02, INVITE-03, INVITE-04, PROF-01, PROF-02, PROF-03, PROF-04, DEBT-01
**Success Criteria** (what must be TRUE):
  1. Admin invites user@example.com → invite email arrives in MailHog with acceptance link containing token
  2. Recipient clicks invite link → account is created/activated and user joins the group with specified role
  3. Admin sees list of pending invitations and can revoke any of them; expired tokens are rejected
  4. User uploads a profile image → avatar URL appears in their profile; TerminalAvatar component shows image instead of initials
  5. Integration tests cover the full auth → groups → invite → accept flow end-to-end against Testcontainers PostgreSQL
**Plans**: TBD

Plans:
- [ ] 21-01: TBD

### Phase 22: Developer Onboarding
**Goal**: A new developer can go from `git clone` to running the full app with all services in under 10 minutes
**Depends on**: Phase 21 (all features complete — onboarding must cover everything)
**Requirements**: ONBOARD-01, ONBOARD-02, ONBOARD-03, ONBOARD-04
**Success Criteria** (what must be TRUE):
  1. Setup CLI detects missing prerequisites (JDK, Docker) and provides actionable fix instructions
  2. `./gradlew devUp` starts all Docker services, runs migrations, and seeds sample data in one command
  3. Architecture overview document exists explaining modules, data flow, and how to add a new feature
  4. First-run walkthrough guides developer from clone → configure → run → verify all services (auth, AI, files, email)
**Plans**: TBD

Plans:
- [ ] 22-01: TBD

## Progress

**Execution Order:** 16 → 17 → 18 → **18.1** → 19 → 20 → 21 → 22

| Phase | Milestone | Plans | Status | Completed |
|-------|-----------|-------|--------|-----------|
| 1-9 | v1.0 | 39/39 | Complete | 2026-02-17 |
| 10-15 | v1.1 | 34/34 | Complete | 2026-02-21 |
| 16. Tech Debt Cleanup | 2/2 | Complete    | 2026-02-21 | - |
| 17. Infrastructure Foundation | v1.2 | 2/2 | Complete | 2026-02-21 |
| 18. Core Services | v1.2 | 3/3 | Complete | 2026-02-22 |
| 18.1 Profile Uploads, Group Creation & Invitations | v1.2 | 5/5 | Complete | 2026-02-22 |
| 19. Structured AI & RAG | v1.2 | 0/TBD | Not started | - |
| 20. Multi-Agent Orchestration | v1.2 | 0/TBD | Not started | - |
| 21. Invitations & Profiles | v1.2 | 0/TBD | Not started | - |
| 22. Developer Onboarding | v1.2 | 0/TBD | Not started | - |

**Total: 85 plans shipped across 2 milestones, 8 phases planned for v1.2**
