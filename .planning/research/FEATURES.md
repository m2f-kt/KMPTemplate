# Feature Research

**Domain:** KMP Full-Stack Template — Milestone v1.2 (Advanced AI, File Uploads, Email Invites, Dev Onboarding, Tech Debt)
**Researched:** 2026-02-21
**Confidence:** HIGH (codebase-verified infrastructure, Koog docs corroborated, pgvector docs verified)

## Context: What Already Exists (v1.0 + v1.1)

Before defining new features, here is the infrastructure we build upon:

- **AI Infrastructure:** `ChatAgentService` (streaming + persistence via `ExposedPersistenceStorage`), `AssistantAgentService` (ReAct strategy with `UserTools`), custom `ChatStreamingStrategy`, WebSocket streaming endpoint. Google Gemini via **Koog 0.6.2**.
- **Auth:** JWT access/refresh tokens, `UserRole` sealed class (User/Admin/PowerAdmin), `RoleAuthorizationPlugin`, password reset (dev-mode console logging only — no real email).
- **Groups:** Full RBAC (Owner > Admin > Member), `GroupService` with CRUD, `RegisterMemberRequest` creates user + assigns group. Group-scoped roles via `GroupMembershipsTable`.
- **MVI ViewModels:** `MviViewModel` base class with Intent/Model/Mutation/Event, all ViewModels migrated. Test DSL with Turbine.
- **SDK:** `Sdk` facade delegates to `AuthApi`, `UserApi`, `GroupApi`. Returns `Either<AppError, T>` via Arrow.
- **Design System:** `TerminalAvatar` (initials only — no image support), responsive dashboard, terminal theme.
- **Localization:** `composeResources/values/strings.xml` with `StringKey` enum, English + Spanish.
- **Database:** PostgreSQL with R2DBC via Exposed 1.0.0. `UsersTable` has: id, email, passwordHash, name, roleId, createdAt, updatedAt. **No avatarUrl column. No vector columns.**
- **Config:** `Env.kt` has `Http`, `Auth`, `OAuth`, `Ai`, `ServerConfig`. **No S3 config. No SMTP config.**
- **Version Catalog:** Ktor 3.4.0, Exposed 1.0.0, Koog 0.6.2, Arrow 2.2.1.1, Koin 4.1.1, Kotlin 2.3.10. **No AWS SDK, no pgvector, no email libs.**

---

## Feature Landscape

### Table Stakes (Users Expect These)

Features that make v1.2 feel complete. Without them, the new capability areas are half-baked.

#### 1. Structured AI Output

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **`executeStructured<T>()` endpoint** | The existing AI chat returns free-text. Any production AI app needs typed, parseable responses for UI rendering, form-filling, data extraction. Without structured output, the AI is a toy chat widget. | LOW | Koog has full structured output API: `@Serializable` + `@LLMDescription` annotated data classes. Three layers: PromptExecutor, Agent LLM Context, Node layer. `StructureFixingParser` handles malformed LLM output automatically. We already have Koog 0.6.2 — this is configuration, not new infrastructure. |
| **Structured output data classes** | Users need concrete examples: a `SentimentAnalysis`, `TaskExtraction`, or `DataClassification` response type to see the pattern. | LOW | 2-3 `@Serializable` data classes with `@LLMDescription` annotations. Nested classes, enums, sealed classes all supported. The data classes live in `:core:models` (shared) so the SDK can deserialize them. |
| **Structured output API route** | A REST endpoint that accepts a prompt + schema identifier and returns typed JSON. | LOW | `POST /api/ai/structured` with request body specifying the output type. Server calls `agent.executeStructured<T>()`. Returns typed JSON matching the data class schema. Reuses existing auth middleware. |

#### 2. S3 File Uploads with Profile Images

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **File upload endpoint** | Any real app needs file uploads. Profile images are the minimum viable use case. Without them, the `TerminalAvatar` forever shows initials. | MEDIUM | New `POST /api/files/upload` multipart endpoint. Accepts file + metadata. Stores in S3-compatible storage. Returns URL. Needs **AWS SDK for Kotlin** (`aws.sdk.kotlin:s3`) dependency. |
| **S3-compatible storage backend** | S3 is the standard for object storage. MinIO provides local dev parity. | MEDIUM | Production: AWS S3. Development: **MinIO** in Docker Compose (already have Docker for PostgreSQL). New `Env.S3` config section: endpoint, bucket, region, accessKey, secretKey. MinIO is wire-compatible with S3 API. |
| **Profile image upload** | `PUT /api/users/me/avatar` uploads an avatar image. Stores in S3, saves URL in user record. | MEDIUM | New `avatarUrl: String?` column in `UsersTable`. Updated `UserResponse` DTO to include `avatarUrl`. SDK `UserApi.uploadAvatar()` method. Server validates file type (JPEG/PNG/WebP), size limit (5MB), resizes to standard dimensions. |
| **TerminalAvatar image support** | The existing `TerminalAvatar` component only renders initials. Must display actual images when `avatarUrl` is present, falling back to initials. | LOW | Extend `TerminalAvatar` composable: if `imageUrl != null`, load with `coil3` (already likely available in KMP) or Compose `AsyncImage`. Fallback to current initials rendering. Circular clip already exists. |
| **ProfileViewModel avatar integration** | `ProfileModel` needs `avatarUrl` field. Profile screen shows avatar, allows upload. | LOW | Add `avatarUrl: String?` to `ProfileModel`. New `ProfileIntent.UploadAvatar(bytes)` intent. `handleIntent` calls `userApi.uploadAvatar()`, emits mutation with new URL. |

#### 3. Email Invitations

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **SMTP email sender service** | The existing `PasswordResetService` prints reset links to console. A real app sends email. Invitations require email delivery. | MEDIUM | New `EmailService` interface with `sendEmail(to, subject, body)`. Implementation: **Jakarta Mail** (formerly JavaMail) or **Simple Java Mail** for SMTP. New `Env.Email` config: host, port, username, password, fromAddress. Dev mode: continue console logging. Production: real SMTP (SendGrid, SES, or any SMTP server). |
| **Token-based invite link generation** | Admin invites user to group via email. Link contains a one-time token. Clicking sets password + activates account. | MEDIUM | New `InviteTokensTable`: id, token (unique), email, groupId, groupRole, createdBy, expiresAt, usedAt. `POST /api/groups/{id}/invite` generates token, sends email with link. `POST /api/auth/accept-invite` validates token, creates/activates user, adds to group. Reuses `PasswordResetService` token pattern. |
| **Invite status tracking** | Admin sees pending invites, can resend or revoke. | LOW | `GET /api/groups/{id}/invites` lists pending invitations. `DELETE /api/groups/{id}/invites/{inviteId}` revokes. Status: pending, accepted, expired, revoked. Admin dashboard shows invite status per group. |
| **Password reset with real email** | Existing `PasswordResetService` already generates tokens. Just needs to use `EmailService` instead of `println()`. | LOW | Swap `println(resetLink)` with `emailService.sendEmail(user.email, "Password Reset", template)`. Template is simple HTML with the reset link. Minimal effort since token logic already exists. |

#### 4. Developer Onboarding

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **Setup CLI polish** | The existing CLI scaffolds the project. Needs to handle new infrastructure (MinIO, SMTP) and verify prerequisites. | LOW | Extend existing CLI: add MinIO container to Docker Compose generation, add SMTP config prompts (or default to console mode), verify `docker compose` available, health-check all services on startup. |
| **First-run walkthrough** | New developer clones repo, runs setup, and needs guided path through the architecture. | LOW | Interactive CLI that: (1) checks prerequisites (Docker, JDK, Node), (2) starts Docker services, (3) runs DB migrations, (4) seeds sample data (admin user, sample group, sample AI conversation), (5) opens browser to dashboard. |
| **Dev documentation** | Architecture decision records, module dependency diagram, "how to add a new feature" guide. | LOW | `docs/` directory with: `ARCHITECTURE.md` (module map), `ADDING-A-FEATURE.md` (step-by-step: model → server route → SDK method → ViewModel → screen), `AI-FEATURES.md` (how to add structured output types, RAG sources, new agents). |
| **Tooling shortcuts** | Common dev tasks should be one command. | LOW | Gradle tasks or shell scripts: `./gradlew devUp` (start Docker + run server), `./gradlew seedData` (populate dev data), `./gradlew testAll` (all test suites). Documented in README. |

#### 5. Tech Debt Cleanup

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **Server integration tests** | v1.1 deferred comprehensive integration tests. Core auth + groups + AI flows need end-to-end coverage with real PostgreSQL. | HIGH | Testcontainers PostgreSQL (already in catalog). Test: register → login → create group → invite member → accept invite → verify RBAC. Test: AI structured output returns valid typed response. Test: file upload stores and retrieves correctly. |
| **WASM locale persistence** | Known gap: locale selection may not persist on WASM target. | LOW | Investigate `multiplatform-settings` behavior on WASM. If broken, implement `localStorage`-based fallback for web target. |
| **Ktor dispatcher configuration** | Server coroutine dispatchers may not be optimally configured for R2DBC + AI streaming workloads. | LOW | Review `Dispatchers.IO` usage in AI agent calls vs R2DBC suspend functions. Ensure AI streaming doesn't block the event loop. Add `CoroutineDispatcher` injection via Koin for testability. |

---

### Differentiators (Competitive Advantage)

Features that elevate this template above competitors. Not required, but significantly increase value.

#### AI Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **RAG with pgvector** | The existing AI chat answers from its training data only. RAG lets it answer from your documents — the single most requested enterprise AI feature. No KMP template ships with a working RAG pipeline. | HIGH | **New infrastructure:** pgvector extension in PostgreSQL (Docker: `pgvector/pgvector:pg18`), new `DocumentEmbeddingsTable` with `vector(768)` column, custom `PgVectorStorage` implementing Koog's `VectorStorage` interface. **Pipeline:** document upload → chunk → embed via Koog `LLMEmbedder` (Gemini embeddings or Ollama local) → store vectors in pgvector → query with cosine distance (`<=>`) → inject top-K results into prompt context. Koog provides `RankedDocumentStorage` and `EmbeddingBasedDocumentStorage` but **no built-in pgvector adapter** — we write a custom one against Exposed. |
| **Multi-agent AI orchestration** | The existing single-agent chat is limited. A router agent that delegates to specialist sub-agents (code helper, document analyst, task planner) demonstrates production AI architecture. | HIGH | Koog supports strategy graphs with **subgraphs** — self-contained processing units with unique names, node graphs, tool subsets, and input/output contracts. Pattern: router agent analyzes user intent → delegates to specialist subgraph → aggregates response. Each subgraph can have different tools, system prompts, and even different LLM configurations. The existing `ChatStreamingStrategy` and `AssistantAgentService` provide the foundation. |
| **Document ingestion pipeline** | Upload PDFs/text → chunk → embed → store. The full RAG pipeline from document to queryable knowledge. | HIGH | Server endpoint: `POST /api/ai/documents`. Accepts file upload (reuses S3 infrastructure). Extracts text (Apache Tika or simple text extraction). Chunks with overlap (512 tokens, 50 token overlap). Embeds via Koog embeddings module. Stores in pgvector. Associates documents with groups for scoped RAG. |

#### Infrastructure Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **MinIO local dev environment** | Developers get S3-compatible storage locally without AWS credentials. Docker Compose brings it up alongside PostgreSQL. No other KMP template ships with integrated object storage. | LOW | Add MinIO service to `docker-compose.yml`. Default bucket created on startup. `Env.S3` config points to `localhost:9000` in dev, real S3 in production. MinIO console at `localhost:9001` for debugging. |
| **Email templating system** | Beyond raw SMTP, HTML email templates for invitations and password resets. Branded, professional emails from a template project. | MEDIUM | Simple Kotlin string templates (no heavy template engine needed). `InviteEmailTemplate`, `PasswordResetEmailTemplate` data classes with `render(): String` method. Inline CSS for email client compatibility. |
| **Group-scoped RAG** | Documents uploaded to a group are only searchable by group members. Multi-tenant RAG out of the box. | MEDIUM | `group_id` column on `DocumentEmbeddingsTable`. Vector search queries filter by `group_id` before cosine similarity. Group admins manage documents; members query them. |

### Anti-Features (Commonly Requested, Often Problematic)

Features that seem like they belong in v1.2 but should be deferred or avoided.

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| **Full vector database (Pinecone/Weaviate)** | Dedicated vector DBs offer more features (metadata filtering, hybrid search). | Adds an entirely new infrastructure dependency. pgvector runs inside the PostgreSQL we already have — zero new services. For a template with <100K documents, pgvector with HNSW indexing is more than sufficient. Switching to a dedicated vector DB is an optimization, not a starting point. | Use pgvector in PostgreSQL. Add HNSW index for production performance. Document how to swap to Pinecone/Weaviate if needed. |
| **LangChain-style agent framework** | LangChain (Python) patterns are well-known. Developers ask for equivalent in Kotlin. | We already have Koog, which is JetBrains' native Kotlin agent framework. Adding LangChain4j or building a LangChain clone creates parallel abstractions. Koog's subgraph system is the Kotlin-native equivalent of LangChain's agent chains. | Use Koog's strategy graphs and subgraphs. They are the Kotlin-idiomatic equivalent. |
| **Real-time collaborative editing** | "Google Docs-style" editing of shared documents. | Enormous complexity (CRDTs or OT), completely orthogonal to the template's purpose. This is a product feature, not a template feature. | Shared documents are view-only. Editing is single-user. Upload/replace workflow is sufficient. |
| **Image generation / DALL-E integration** | AI image generation is trendy. | Adds another AI service dependency, significant cost, and a completely different UI paradigm (image gallery, generation queue). Not related to the core structured-output/RAG/multi-agent story. | Defer entirely. The AI pipeline is text-in/text-out for v1.2. Image features are v2+. |
| **Complex file management (folders, versioning)** | Once file upload exists, requests for folder hierarchy and version history follow. | File management is a product, not a template feature. Folders need tree data structures, breadcrumb navigation, move/copy operations. Versioning needs diffing, storage multiplication, rollback. | Flat file storage per user/group. Upload, list, delete. No folders, no versioning. The pattern is demonstrated; users extend it. |
| **OAuth-based email (Gmail API)** | Send emails through Gmail API instead of SMTP. | OAuth token management for sending email is fragile (token refresh, scope management, Google's aggressive deprecation). SMTP is universally supported and simpler. | Use SMTP. Works with Gmail (app passwords), SendGrid, SES, or any provider. |
| **Client-side vector search (on-device embeddings)** | Run embeddings locally on mobile/desktop for offline RAG. | Embedding models are 50-500MB. KMP doesn't have a mature on-device inference story. ONNX Runtime for Kotlin is experimental. This is a research project, not a template feature. | All embedding and vector search happens server-side. Client sends text, server returns results. |

---

## Feature Dependencies

```
[S3 File Upload Infrastructure]
    |
    +--requires--> [AWS SDK Kotlin dependency] (NEW)
    +--requires--> [MinIO in Docker Compose] (NEW)
    +--requires--> [Env.S3 config section] (NEW)
    |
    +--enables--> [Profile Image Upload]
    |                 +--requires--> [avatarUrl column in UsersTable]
    |                 +--requires--> [UserResponse DTO update]
    |                 +--enables--> [TerminalAvatar image support]
    |                 +--enables--> [ProfileViewModel avatar integration]
    |
    +--enables--> [Document Upload for RAG]
                      +--requires--> [RAG Pipeline]

[Email Infrastructure]
    |
    +--requires--> [SMTP library dependency] (NEW)
    +--requires--> [Env.Email config section] (NEW)
    |
    +--enables--> [Email Invite Flow]
    |                 +--requires--> [InviteTokensTable] (NEW)
    |                 +--requires--> [Groups infrastructure] (ALREADY EXISTS)
    |                 +--enables--> [Invite Status Tracking]
    |
    +--enables--> [Real Password Reset Emails]
                      +--requires--> [PasswordResetService] (ALREADY EXISTS)

[Structured AI Output]
    |
    +--requires--> [Koog 0.6.2] (ALREADY EXISTS)
    +--requires--> [ChatAgentService] (ALREADY EXISTS)
    |
    +--enables--> [Structured Output Endpoint]
    +--enables--> [Typed AI response data classes]
    +--independent of file uploads and email

[RAG Pipeline]
    |
    +--requires--> [pgvector extension in PostgreSQL] (NEW)
    +--requires--> [Koog embeddings module] (NEW dependency: koog-embeddings-llm)
    +--requires--> [Custom PgVectorStorage adapter] (NEW)
    +--requires--> [S3 File Upload] (for document ingestion)
    |
    +--enables--> [Document ingestion pipeline]
    +--enables--> [RAG-augmented chat]
    +--enables--> [Group-scoped RAG]

[Multi-Agent Orchestration]
    |
    +--requires--> [Koog subgraphs] (ALREADY AVAILABLE in Koog 0.6.2)
    +--requires--> [Existing ChatStreamingStrategy] (ALREADY EXISTS)
    +--requires--> [Structured AI Output] (for typed agent responses)
    |
    +--optionally-uses--> [RAG Pipeline] (document-aware agent)
    |
    +--enables--> [Router Agent + Specialist Subgraphs]
    +--enables--> [Agent-specific tool sets]

[Developer Onboarding]
    |
    +--requires--> [All infrastructure in place] (S3, Email, pgvector)
    +--runs-last--> because it documents and automates everything above
    |
    +--enables--> [CLI polish]
    +--enables--> [Dev documentation]
    +--enables--> [First-run walkthrough]

[Tech Debt]
    |
    +--integration tests require all features to exist
    +--can be addressed incrementally alongside features
```

### Dependency Notes

- **S3 infrastructure is a prerequisite for both profile images AND RAG document ingestion.** Build file upload first because two major feature areas depend on it.
- **Email infrastructure is independent of S3/AI.** Can be built in parallel. Only depends on existing Groups infrastructure.
- **Structured AI output is the simplest AI feature** — it uses existing Koog APIs with minimal new infrastructure. Build it first among AI features to validate the pattern before tackling RAG.
- **RAG depends on both S3 (document upload) and pgvector (vector storage).** This is the most infrastructure-heavy feature. Building S3 first and structured output first de-risks the RAG phase.
- **Multi-agent orchestration builds on structured output** because specialist agents return typed responses. RAG is optional but valuable (one specialist agent can be document-aware).
- **Developer onboarding must come last** because it documents and automates all the infrastructure above. Writing docs for features that don't exist yet is waste.
- **Tech debt (integration tests) can be woven in alongside feature work** — each feature phase should include its own integration tests.

---

## MVP Definition

### Build in v1.2 (This Milestone)

Core features that complete the template's story as a production-ready KMP starter.

- [ ] **Structured AI output endpoint** — lowest complexity AI feature, validates Koog structured output pattern
- [ ] **S3 file upload infrastructure** — MinIO in Docker, `Env.S3` config, upload endpoint
- [ ] **Profile image upload + TerminalAvatar image support** — visible, tangible improvement users see immediately
- [ ] **SMTP email service** — replaces console-logged password resets, enables invitations
- [ ] **Email invitation flow** — admin invites user to group via email with token link
- [ ] **RAG pipeline with pgvector** — document upload → embed → store → query. The headline AI feature.
- [ ] **Multi-agent orchestration** — router agent + 2-3 specialist subgraphs demonstrating the pattern
- [ ] **Integration tests for new features** — each feature ships with tests (not deferred)
- [ ] **CLI updates for new infrastructure** — Docker Compose includes MinIO + pgvector PostgreSQL
- [ ] **Dev documentation** — architecture guide, "add a feature" guide, AI features guide

### Add After Validation (v1.2.x)

- [ ] **Group-scoped RAG** — documents scoped to groups, queries filtered by membership
- [ ] **Document ingestion pipeline** — PDF/text upload with chunking and automatic embedding
- [ ] **Email HTML templates** — branded invitation and password reset emails
- [ ] **Invite status dashboard** — admin UI showing pending/accepted/expired invites
- [ ] **WASM locale persistence fix** — investigate and fix if broken
- [ ] **Ktor dispatcher optimization** — review and optimize for concurrent AI + DB workloads

### Future Consideration (v2+)

- [ ] **On-device embeddings** — local vector search for offline scenarios
- [ ] **Advanced RAG** — hybrid search (vector + keyword), re-ranking, query expansion
- [ ] **Agent memory** — long-term memory across conversations, user preference learning
- [ ] **File versioning** — version history for uploaded documents
- [ ] **Real-time agent collaboration** — multiple agents working on a task simultaneously with user visibility

---

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Dependencies | Priority |
|---------|------------|---------------------|--------------|----------|
| Structured AI output endpoint | HIGH | LOW | Koog (exists) | P1 |
| S3 file upload infrastructure | HIGH | MEDIUM | AWS SDK (new), MinIO (new) | P1 |
| Profile image upload + avatar | HIGH | MEDIUM | S3 infrastructure, UsersTable migration | P1 |
| SMTP email service | HIGH | MEDIUM | SMTP lib (new), Env.Email (new) | P1 |
| Email invitation flow | HIGH | MEDIUM | Email service, Groups (exists) | P1 |
| RAG pipeline with pgvector | HIGH | HIGH | pgvector (new), Koog embeddings (new), S3 | P1 |
| Multi-agent orchestration | MEDIUM | HIGH | Structured output, Koog subgraphs | P1 |
| Integration tests (per feature) | HIGH | MEDIUM | Each respective feature | P1 |
| CLI polish for new infra | MEDIUM | LOW | All infrastructure decisions finalized | P1 |
| Dev documentation | MEDIUM | LOW | All features complete | P1 |
| Group-scoped RAG | MEDIUM | MEDIUM | RAG pipeline, Groups (exists) | P2 |
| Document ingestion pipeline | MEDIUM | HIGH | RAG pipeline, S3, text extraction | P2 |
| Email HTML templates | LOW | LOW | Email service | P2 |
| Invite status dashboard UI | LOW | MEDIUM | Invite flow | P2 |
| WASM locale persistence | LOW | LOW | Investigation needed | P2 |
| Ktor dispatcher optimization | LOW | LOW | Profiling needed | P3 |
| Agent memory / long-term context | MEDIUM | HIGH | Multi-agent system | P3 |

**Priority key:**
- P1: Must have for v1.2 launch
- P2: Should have, add when possible within milestone
- P3: Nice to have, future milestone

---

## Detailed Feature Specifications

### A. Structured AI Output — What "Done" Looks Like

**Data class with LLM annotations:**

```kotlin
@Serializable
data class SentimentAnalysis(
    @LLMDescription("The overall sentiment: POSITIVE, NEGATIVE, or NEUTRAL")
    val sentiment: Sentiment,
    @LLMDescription("Confidence score between 0.0 and 1.0")
    val confidence: Double,
    @LLMDescription("Key phrases that influenced the sentiment determination")
    val keyPhrases: List<String>,
    @LLMDescription("Brief explanation of the sentiment analysis")
    val explanation: String
)

@Serializable
enum class Sentiment {
    @LLMDescription("The text expresses positive feelings or opinions")
    POSITIVE,
    @LLMDescription("The text expresses negative feelings or opinions")
    NEGATIVE,
    @LLMDescription("The text is factual or does not express clear sentiment")
    NEUTRAL
}
```

**Agent execution:**

```kotlin
// Using Koog's executeStructured API
val result: SentimentAnalysis = agent.executeStructured<SentimentAnalysis>(
    prompt = "Analyze the sentiment of: '$userText'"
)
```

**API endpoint:**

```kotlin
// POST /api/ai/structured
@Serializable
data class StructuredOutputRequest(
    val prompt: String,
    val outputType: String // "sentiment", "task_extraction", etc.
)

// Response is the typed JSON matching the data class
```

### B. RAG Pipeline — What "Done" Looks Like

**pgvector table:**

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE document_embeddings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL,
    group_id UUID REFERENCES groups(id),
    chunk_index INTEGER NOT NULL,
    chunk_text TEXT NOT NULL,
    embedding vector(768) NOT NULL,  -- Gemini embedding dimension
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ON document_embeddings
    USING hnsw (embedding vector_cosine_ops);
```

**Custom PgVectorStorage adapter:**

```kotlin
class PgVectorStorage(
    private val database: Database
) : VectorStorage {
    
    suspend fun store(documentId: UUID, chunks: List<EmbeddedChunk>) {
        // INSERT chunks with embeddings into document_embeddings
    }
    
    suspend fun query(
        queryEmbedding: FloatArray,
        topK: Int = 5,
        groupId: UUID? = null
    ): List<RankedDocument> {
        // SELECT with cosine distance, optional group_id filter
        // ORDER BY embedding <=> query_embedding LIMIT topK
    }
}
```

**RAG-augmented chat flow:**

```
User query → Embed query → pgvector cosine search → Top-K chunks
    → Inject into system prompt as context → LLM generates answer
    → Response includes source references
```

### C. Multi-Agent Architecture — What "Done" Looks Like

**Router agent with specialist subgraphs:**

```kotlin
// Router strategy graph
val orchestratorStrategy = strategy("orchestrator") {
    val router = subgraph("router") {
        // Analyzes user intent, returns routing decision
        nodeExecuteStructured<RoutingDecision>()
    }
    
    val codeHelper = subgraph("code-helper") {
        // Tools: code analysis, syntax checking
        nodeLLMCall()
    }
    
    val documentAnalyst = subgraph("document-analyst") {
        // Tools: RAG search, document retrieval
        nodeLLMCall()
    }
    
    val taskPlanner = subgraph("task-planner") {
        // Tools: task extraction, structured output
        nodeExecuteStructured<TaskPlan>()
    }
    
    // Route: router → specialist → response
    edge(router to codeHelper, condition = { it.agent == "code" })
    edge(router to documentAnalyst, condition = { it.agent == "document" })
    edge(router to taskPlanner, condition = { it.agent == "task" })
}
```

### D. S3 File Upload — What "Done" Looks Like

**Config:**

```kotlin
// In Env.kt
data class S3(
    val endpoint: String,      // "http://localhost:9000" (MinIO) or S3 URL
    val bucket: String,        // "template-uploads"
    val region: String,        // "us-east-1"
    val accessKey: String,
    val secretKey: String,
    val publicUrl: String      // URL prefix for public access
)
```

**Docker Compose addition:**

```yaml
minio:
  image: minio/minio
  ports:
    - "9000:9000"
    - "9001:9001"  # Console
  environment:
    MINIO_ROOT_USER: minioadmin
    MINIO_ROOT_PASSWORD: minioadmin
  command: server /data --console-address ":9001"
  volumes:
    - minio_data:/data
```

**Upload endpoint:**

```kotlin
// POST /api/files/upload (multipart)
// PUT /api/users/me/avatar (multipart, profile-specific)

// FileService stores to S3, returns public URL
// Server validates: file type, size limit, generates unique key
```

### E. Email Invitation Flow — What "Done" Looks Like

**Database:**

```sql
CREATE TABLE invite_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    group_id UUID NOT NULL REFERENCES groups(id),
    group_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    created_by UUID NOT NULL REFERENCES users(id),
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'  -- PENDING, ACCEPTED, EXPIRED, REVOKED
);
```

**Flow:**

```
Admin: POST /api/groups/{id}/invite { email, role }
    → Server creates invite_tokens record
    → Server sends email via EmailService with link:
      https://app.example.com/accept-invite?token=abc123

User: Clicks link → shown registration form (email pre-filled)
    → POST /api/auth/accept-invite { token, name, password }
    → Server validates token (not expired, not used)
    → Creates user account (or activates existing)
    → Adds user to group with specified role
    → Marks token as used
    → Returns auth tokens (user is logged in)
```

### F. Developer Onboarding — What "Done" Looks Like

**CLI first-run experience:**

```bash
$ ./gradlew setup
> Checking prerequisites...
  ✓ JDK 21 found
  ✓ Docker found
  ✓ docker compose found

> Starting services...
  ✓ PostgreSQL (with pgvector) started on :5432
  ✓ MinIO started on :9000 (console: :9001)

> Running migrations...
  ✓ Users, Roles, Groups tables created
  ✓ pgvector extension enabled
  ✓ Document embeddings table created
  ✓ Invite tokens table created

> Seeding dev data...
  ✓ Admin user created (admin@example.com / admin123)
  ✓ Sample group "Engineering" created
  ✓ MinIO bucket "template-uploads" created

> Configuration...
  ✓ .env.local generated with dev defaults
  ✓ AI provider: Gemini (set GEMINI_API_KEY in .env.local)

> Ready! Run: ./gradlew :server:run
```

---

## Sources

- **Koog Structured Output** — Koog docs: executeStructured API, @LLMDescription annotations, StructureFixingParser. Three-layer API (PromptExecutor, Agent LLM Context, Node). Supports nested classes, enums, sealed classes, collections. (HIGH confidence, official JetBrains docs)
- **Koog Embeddings** — Koog docs: `embeddings-base` and `embeddings-llm` modules. `LLMEmbedder` class with `embed()` and `diff()`. Providers: Ollama, OpenAI, AWS Bedrock. (HIGH confidence, official docs)
- **Koog RAG/VectorStorage** — Koog docs: `RankedDocumentStorage`, `InMemoryVectorStorage`, `FileVectorStorage`, `EmbeddingBasedDocumentStorage`. No built-in pgvector adapter. Custom implementation needed. (HIGH confidence, official docs)
- **Koog Subgraphs** — Koog docs: self-contained processing units within strategies. Unique name, node graph, tool subset, input/output contracts. (HIGH confidence, official docs)
- **pgvector** — [GitHub: pgvector/pgvector](https://github.com/pgvector/pgvector). HNSW and IVFFlat indexes, cosine/L2/inner product distances, up to 2000 dimensions. Docker: `pgvector/pgvector:pg18`. (HIGH confidence, official repo)
- **AWS SDK for Kotlin** — [AWS docs](https://docs.aws.amazon.com/sdk-for-kotlin/). S3 client with suspend functions, KMP-compatible. (HIGH confidence, official docs)
- **MinIO** — [min.io](https://min.io). S3-compatible object storage. Docker image, wire-compatible with AWS S3 API. (HIGH confidence, official docs)
- **Ktor Multipart** — [Ktor docs](https://ktor.io/docs/server-requests.html#multipart). `receiveMultipart()` API for file uploads. (HIGH confidence, official docs)
- **Simple Java Mail** — [simplejavamail.org](https://www.simplejavamail.org). Clean SMTP API for JVM. Alternative: Jakarta Mail. (MEDIUM confidence, popular library)

---
*Feature research for: KMP Full-Stack Template — Milestone v1.2*
*Researched: 2026-02-21*
