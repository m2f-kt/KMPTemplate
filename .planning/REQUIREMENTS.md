# Requirements: KMP Full-Stack Template

**Defined:** 2026-02-21
**Core Value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.

## v1.2 Requirements

Requirements for v1.2 Polish & Patterns milestone. Each maps to roadmap phases.

### Structured AI Output

- [ ] **AISTR-01**: Server exposes a structured output endpoint that returns typed JSON matching a Kotlin data class schema
- [ ] **AISTR-02**: At least 2 example structured output data classes exist with @Serializable and @LLMDescription annotations (e.g., SentimentAnalysis, TaskExtraction)
- [ ] **AISTR-03**: SDK provides a function to request structured AI output and receive a typed response as Either<AppError, T>

### RAG Pipeline (Koog-exclusive)

- [x] **RAG-01**: PostgreSQL runs with pgvector extension enabled, and a document_embeddings table stores vector embeddings -- Phase 17
- [ ] **RAG-02**: RAG pipeline uses exclusively Koog framework APIs — `koog-agents-ext-embeddings` and `koog-agents-ext-rag` modules. No third-party RAG/vector libraries (LangChain4j, Spring AI, etc.)
- [ ] **RAG-03**: Custom PgVectorStorage adapter implements Koog's `VectorStorage` interface backed by pgvector with cosine similarity search
- [ ] **RAG-04**: Documents are chunked and embedded via Koog's `LLMEmbedder` and stored as vectors in pgvector
- [ ] **RAG-05**: AI chat can be augmented with RAG context — query embeds via Koog, retrieves top-K relevant chunks, injects into prompt using Koog's RAG tools
- [ ] **RAG-06**: RAG queries are scoped to the user's group — documents uploaded to Group A are not searchable by Group B members
- [ ] **RAG-07**: Implementation must reference latest Koog documentation (via Context7 or equivalent doc-fetching tool) to ensure API correctness — no assumptions about Koog APIs without doc verification

### Multi-Agent Orchestration

- [ ] **AGENT-01**: A router agent analyzes user intent and delegates to the appropriate specialist agent (at least 2 specialists)
- [ ] **AGENT-02**: Each specialist agent has its own tool set, system prompt, and optionally different LLM configuration
- [ ] **AGENT-03**: Multi-agent orchestration is exposed via API endpoint with streaming response support

### File Uploads

- [ ] **FILE-01**: Server provides S3-compatible file upload endpoint with MinIO as local dev backend
- [x] **FILE-02**: Docker Compose includes MinIO service with default bucket created on startup -- Phase 17
- [x] **FILE-03**: Env.S3 configuration section exists with endpoint, bucket, region, accessKey, secretKey -- Phase 17
- [ ] **FILE-04**: Server validates uploaded files (type whitelist, size limit) before storing in S3

### Profile Images

- [x] **PROF-01**: UsersTable has avatarUrl column and UserResponse DTO includes avatarUrl field
- [x] **PROF-02**: User can upload a profile image via PUT /api/users/me/avatar endpoint
- [ ] **PROF-03**: TerminalAvatar component displays image when avatarUrl is present, falls back to initials
- [x] **PROF-04**: ProfileViewModel supports avatar upload intent and reflects avatar URL in state

### Email Infrastructure

- [ ] **EMAIL-01**: EmailService interface exists with sendEmail(to, subject, htmlBody) method and SMTP implementation
- [x] **EMAIL-02**: Docker Compose includes MailHog service for local dev email testing -- Phase 17
- [x] **EMAIL-03**: Env.Email configuration section exists with SMTP host, port, credentials, fromAddress -- Phase 17
- [ ] **EMAIL-04**: Password reset flow sends real email via EmailService instead of console println

### Group Invitations

- [x] **INVITE-01**: Admin can invite a user to a group by email — server generates token, sends invite email with link
- [x] **INVITE-02**: Recipient can accept invite via token link — creates/activates account and joins group with specified role -- Phase 18.2
- [x] **INVITE-03**: Admin can view pending invitations for their group and revoke them
- [x] **INVITE-04**: Invite tokens expire after configurable duration and cannot be reused -- Phase 18.2

### Tech Debt

- [ ] **DEBT-01**: Integration tests cover auth + groups + invite flow end-to-end with Testcontainers PostgreSQL
- [ ] **DEBT-02**: Integration tests cover file upload + retrieval with S3/MinIO
- [ ] **DEBT-03**: Integration tests cover AI structured output endpoint
- [x] **DEBT-04**: WASM locale selection persists across page reloads via localStorage -- Phase 16
- [x] **DEBT-05**: Server coroutine dispatcher configuration reviewed and optimized for concurrent AI + R2DBC workloads -- Phase 16

### Developer Onboarding

- [ ] **ONBOARD-01**: Setup CLI checks prerequisites (JDK, Docker), starts Docker services, runs migrations, seeds sample data
- [ ] **ONBOARD-02**: Developer documentation exists: architecture overview, module guide, "add a feature" walkthrough
- [ ] **ONBOARD-03**: Gradle tooling shortcuts exist for common operations (devUp, seedData, testAll)
- [ ] **ONBOARD-04**: First-run walkthrough guides new developer from clone to running app with all services

## Future Requirements

Deferred to future milestone. Tracked but not in v1.2 roadmap.

### Advanced AI

- **AIADV-01**: On-device embeddings for offline RAG scenarios
- **AIADV-02**: Hybrid search combining vector similarity and keyword matching
- **AIADV-03**: Long-term agent memory across conversations with user preference learning
- **AIADV-04**: Query expansion and re-ranking for improved RAG accuracy

### Advanced Files

- **FILEADV-01**: File versioning with history and rollback
- **FILEADV-02**: Folder hierarchy with tree navigation and breadcrumbs

### Email Polish

- **EMAILADV-01**: Branded HTML email templates with inline CSS for invite and password reset
- **EMAILADV-02**: Email delivery status tracking and retry logic

## Out of Scope

| Feature | Reason |
|---------|--------|
| Offline/caching | Significant complexity, not core to template value |
| Granular permissions (beyond 3 roles) | Existing SuperAdmin/Admin/Member model sufficient for template |
| Full vector database (Pinecone/Weaviate) | pgvector in existing PostgreSQL is sufficient; zero new services |
| LangChain-style framework | Koog is the Kotlin-native equivalent; no parallel abstractions |
| Non-Koog RAG/embedding libraries | RAG pipeline is exclusively Koog-based — no LangChain4j, Spring AI, or other RAG frameworks |
| Real-time collaborative editing | Enormous complexity (CRDTs/OT), not a template feature |
| Image generation / DALL-E | Different AI paradigm, additional cost, not related to RAG/multi-agent story |
| Complex file management (folders, versioning) | File management is a product, not a template feature |
| OAuth-based email (Gmail API) | SMTP is universal, simpler, works with any provider |
| Client-side vector search | Embedding models are 50-500MB, no mature KMP on-device inference |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| AISTR-01 | Phase 19 | Pending |
| AISTR-02 | Phase 19 | Pending |
| AISTR-03 | Phase 19 | Pending |
| RAG-01 | Phase 17 | Pending |
| RAG-02 | Phase 19 | Pending |
| RAG-03 | Phase 19 | Pending |
| RAG-04 | Phase 19 | Pending |
| RAG-05 | Phase 19 | Pending |
| RAG-06 | Phase 19 | Pending |
| RAG-07 | Phase 19 | Pending |
| AGENT-01 | Phase 20 | Pending |
| AGENT-02 | Phase 20 | Pending |
| AGENT-03 | Phase 20 | Pending |
| FILE-01 | Phase 18 | Pending |
| FILE-02 | Phase 17 | Pending |
| FILE-03 | Phase 17 | Pending |
| FILE-04 | Phase 18 | Pending |
| PROF-01 | Phase 21 | Complete |
| PROF-02 | Phase 21 | Complete |
| PROF-03 | Phase 21 | Pending |
| PROF-04 | Phase 21 | Complete |
| EMAIL-01 | Phase 18 | Pending |
| EMAIL-02 | Phase 17 | Pending |
| EMAIL-03 | Phase 17 | Pending |
| EMAIL-04 | Phase 18 | Pending |
| INVITE-01 | Phase 21 | Complete |
| INVITE-02 | Phase 18.2 | Complete |
| INVITE-03 | Phase 21 | Complete |
| INVITE-04 | Phase 18.2 | Complete |
| DEBT-01 | Phase 21 | Pending |
| DEBT-02 | Phase 18 | Pending |
| DEBT-03 | Phase 19 | Pending |
| DEBT-04 | Phase 16 | Complete |
| DEBT-05 | Phase 16 | Complete |
| ONBOARD-01 | Phase 22 | Pending |
| ONBOARD-02 | Phase 22 | Pending |
| ONBOARD-03 | Phase 22 | Pending |
| ONBOARD-04 | Phase 22 | Pending |

**Coverage:**
- v1.2 requirements: 38 total
- Mapped to phases: 38/38 ✓
- Unmapped: 0

---
*Requirements defined: 2026-02-21*
*Last updated: 2026-02-25 after Phase 18.2 gap closure*
