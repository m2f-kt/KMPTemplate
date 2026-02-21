# Architecture Research

**Domain:** KMP Full-Stack Template v1.2 — pgvector RAG, S3 File Uploads, Multi-Agent AI, Email Invitations, Developer Onboarding
**Researched:** 2026-02-21
**Confidence:** HIGH

## System Overview

### Current Module Graph (Post v1.1)

```
                           settings.gradle.kts
                                  |
        +-----------+-------------+-------------+-----------+
        |           |             |             |           |
   core:models  core:sdk    core:storage   core:mvi    core:testing
        |           |             |             |           |
        |     (depends on         |        (MviViewModel   (fakes,
        |      models,storage)    |         base class)    test DSL)
        |           |             |             |           |
        +-----+-----+------+-----+------+------+-----------+
              |             |            |
         app:auth     app:dashboard  app:profile  app:admin  app:designsystem
              |             |            |            |           |
              +------+------+------+-----+------+----+-----------+
                     |
                  composeApp  (navigation host, DI root)

        server
          |
    +-----+-----+----------+----------+
    |           |          |          |
server:auth  server:ai  server:groups  server:core
    |           |          |         +---+---+-------+
    |           |          |         |       |       |
    |           |          |      config  database  security
    +-----+-----+----+-----+--------+------+-------+
          |
       Application.kt (routes, DI, startup)
```

### Proposed New & Modified Modules for v1.2

```
                    NEW MODULES                           MODIFIED MODULES
                        |                                       |
    +----------+--------+--------+               +------+------+------+------+
    |          |                 |               |      |      |      |      |
server:files  server:email  (server:ai    server:ai  server:auth  server:groups
    |          |             extensions)      |      |      |      |
  (S3 upload, (SMTP send,   [integrated   (RAG,    (avatar (invite
  presigned   templating,    into          multi-   URL     members
  URLs,       queue)         server:ai)    agent,   col)    via email)
  avatar                                   struct
  mgmt)                                    output)
    |          |                 |               |      |      |
    +----------+---------+------+         core:models  core:sdk  core:testing
                         |                (new DTOs)   (FileApi, (new fakes)
                   docker-compose                      InviteApi)
                   (pgvector, MinIO,
                    MailHog)
```

### Updated settings.gradle.kts Additions

```kotlin
// New server modules
include("server:files")
include("server:email")

// Existing modules — no settings change needed, just code changes:
// server:ai, server:auth, server:groups, core:models, core:sdk, core:testing
// app:profile, app:designsystem, app:admin
```

## Component Inventory

### New Modules

| Module | Type | Purpose | Key Dependencies |
|--------|------|---------|------------------|
| `server:files` | Server (JVM) | S3 file operations: upload, presigned URLs, avatar management | `aws.sdk.kotlin:s3`, `server:core:config`, `server:core:database` |
| `server:email` | Server (JVM) | SMTP email sending with HTML templates, send queue | Jakarta Mail, `server:core:config` |

### Modified Modules

| Module | Changes | Scope of Change |
|--------|---------|-----------------|
| `server:ai` | Add RAG pipeline (pgvector embeddings, document ingestion, retrieval), structured output support, multi-agent orchestration via Koog subgraphs | Major — new services, new tables, new tools |
| `server:auth` | Add `avatarUrl` column to UsersTable, expose in UserResponse | Minor — 1 migration, 1 column, DTO update |
| `server:groups` | Add email invitation flow (invite tokens table, invite endpoint, accept endpoint) | Medium — new table, new service methods, new routes |
| `core:models` | Add DTOs: `FileUploadResponse`, `PresignedUrlResponse`, `InviteRequest`, `InviteResponse`, `DocumentChunk`, `RagQueryRequest/Response`, new `ApiRoutes` for files/invites | Medium — new files, additive only |
| `core:sdk` | Add `FileApi`, `InviteApi` interfaces + implementations | Medium — new API classes, SDK facade additions |
| `core:testing` | Add fakes: `FakeFileApi`, `FakeInviteApi`, `FakeEmailService` | Minor — new fake classes |
| `app:profile` | Avatar upload UI (image picker → presigned URL → S3 upload → update profile) | Medium — new UI flow |
| `app:designsystem` | `TerminalAvatar` component: support loading actual profile images (not just initials) | Minor — conditional image rendering |
| `app:admin` | Group invite management UI | Minor — new screen/tab |
| `docker-compose.yml` | Add MinIO (S3-compatible), MailHog (SMTP mock), change Postgres image to pgvector | Medium — infrastructure |
| `server:core:config` | Add `Env.S3`, `Env.Email`, `Env.Embedding` config sections | Minor — new data classes |
| `server:core:database` | Add custom `VectorColumnType` for Exposed R2DBC pgvector support | Minor — 1 utility class |

## Recommended Architecture

### 1. S3 File Uploads — `server:files`

**Module:** New `server:files` server module
**Package:** `com.m2f.server.files`

**Architecture: Presigned URL Pattern (server-mediated, client-direct-upload)**

The server never receives file bytes. Instead:
1. Client requests a presigned upload URL from the server
2. Server generates a presigned PUT URL with S3 SDK, stores pending upload metadata
3. Client uploads directly to S3 (MinIO in dev) using the presigned URL
4. Client confirms upload completion to server
5. Server validates the object exists in S3, updates database references

**Why presigned URLs instead of multipart upload through server:**
- Eliminates server as a bottleneck for large files
- Reduces server memory pressure (no buffering file bytes)
- Works identically with MinIO (dev) and real S3 (prod)
- AWS SDK for Kotlin is JVM-only — fine for server, but clients need plain HTTP PUT

**Key classes:**

```kotlin
// server:files/service/FileService.kt
class FileService(
    private val s3Client: S3Client,
    private val fileRepository: FileRepository,
    private val config: Env.S3,
) {
    context(raise: Raise<DomainError>)
    suspend fun generateUploadUrl(
        userId: Uuid,
        fileName: String,
        contentType: String,
        purpose: FilePurpose, // AVATAR, DOCUMENT, ATTACHMENT
    ): PresignedUrlResponse

    context(raise: Raise<DomainError>)
    suspend fun confirmUpload(userId: Uuid, fileId: Uuid): FileUploadResponse

    context(raise: Raise<DomainError>)
    suspend fun generateDownloadUrl(fileId: Uuid): PresignedUrlResponse

    context(raise: Raise<DomainError>)
    suspend fun deleteFile(userId: Uuid, fileId: Uuid)
}

// server:files/tables/FilesTable.kt
object FilesTable : Table("files") {
    val id = uuid("id").autoGenerate()
    val ownerId = uuid("owner_id")  // references UsersTable.id
    val s3Key = varchar("s3_key", 500)
    val fileName = varchar("file_name", 255)
    val contentType = varchar("content_type", 100)
    val sizeBytes = long("size_bytes").nullable()
    val purpose = varchar("purpose", 50)  // AVATAR, DOCUMENT, ATTACHMENT
    val status = varchar("status", 20).default("PENDING")  // PENDING, CONFIRMED, DELETED
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}
```

**S3 key structure:** `{purpose}/{userId}/{fileId}/{fileName}` — e.g., `avatars/550e8400.../abc123.../profile.jpg`

**Koin wiring:**

```kotlin
val filesModule = module {
    single { S3Client { region = get<Env.S3>().region; /* credentials */ } }
    single { FileRepository(db = get()) }
    single { FileService(s3Client = get(), fileRepository = get(), config = get()) }
}
```

**Avatar integration with `server:auth`:**

When a user uploads an avatar via `server:files`, the `FileService.confirmUpload()` for `AVATAR` purpose also updates the `UsersTable.avatarUrl` column. This cross-module call goes through a defined interface:

```kotlin
// server:core:config/service/UserProfileUpdater.kt (interface in shared core)
interface UserProfileUpdater {
    suspend fun updateAvatarUrl(userId: Uuid, url: String?)
}

// server:auth implements it
class UserRepository(...) : UserProfileUpdater {
    override suspend fun updateAvatarUrl(userId: Uuid, url: String?) { ... }
}

// server:files uses it via Koin injection
class FileService(
    private val userProfileUpdater: UserProfileUpdater,
    // ...
)
```

This avoids `server:files` depending directly on `server:auth`.

### 2. pgvector RAG Pipeline — `server:ai` Extension

**Module:** Modifications to existing `server:ai`
**Package:** `com.m2f.server.ai.rag`

**Architecture: Custom VectorStorage backed by pgvector**

Koog's RAG system uses `VectorStorage` and `RankedDocumentStorage` interfaces. We implement a custom `PgVectorStorage` that stores embeddings in PostgreSQL with pgvector, giving us persistent, queryable vector storage without an external vector database.

**Custom Exposed Column Type:**

```kotlin
// server:core:database/columns/VectorColumnType.kt
class VectorColumnType(private val dimensions: Int) : ColumnType<FloatArray>() {
    override fun sqlType(): String = "vector($dimensions)"

    override fun valueFromDB(value: Any): FloatArray = when (value) {
        is FloatArray -> value
        is io.r2dbc.postgresql.codec.Vector -> value.vector  // R2DBC 1.0.3+ native codec
        is String -> parseVectorString(value)
        else -> error("Unexpected vector value: $value")
    }

    override fun notNullValueToDB(value: FloatArray): Any = value

    private fun parseVectorString(s: String): FloatArray =
        s.removeSurrounding("[", "]").split(",").map { it.trim().toFloat() }.toFloatArray()
}

fun Table.vector(name: String, dimensions: Int): Column<FloatArray> =
    registerColumn(name, VectorColumnType(dimensions))
```

**Key insight:** R2DBC PostgreSQL driver 1.0.3+ (project uses 1.0.7.RELEASE) has native vector type support via `io.r2dbc.postgresql.codec.Vector`. No additional `pgvector-java` library needed. The custom column type bridges Exposed's type system to R2DBC's native codec.

**Embeddings table:**

```kotlin
// server:ai/tables/EmbeddingsTable.kt
object EmbeddingsTable : Table("embeddings") {
    val id = uuid("id").autoGenerate()
    val documentId = varchar("document_id", 255)     // source document reference
    val chunkIndex = integer("chunk_index")           // position within document
    val content = text("content")                     // raw text chunk
    val embedding = vector("embedding", 768)          // embedding vector (dimension depends on model)
    val metadata = text("metadata").nullable()         // JSON metadata
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}

// HNSW index for fast approximate nearest neighbor search
// Migration SQL: CREATE INDEX ON embeddings USING hnsw (embedding vector_cosine_ops)
```

**PgVectorStorage implementation:**

```kotlin
// server:ai/rag/PgVectorStorage.kt
class PgVectorStorage(
    private val db: R2dbcDatabase,
    private val embedder: LLMEmbedder,
) : VectorStorage {

    suspend fun store(documentId: String, chunks: List<TextChunk>) {
        val embeddings = chunks.map { embedder.embed(it.text) }
        suspendTransaction(db = db) {
            chunks.forEachIndexed { index, chunk ->
                EmbeddingsTable.insert {
                    it[EmbeddingsTable.documentId] = documentId
                    it[chunkIndex] = index
                    it[content] = chunk.text
                    it[embedding] = embeddings[index]
                    it[metadata] = chunk.metadata?.let { Json.encodeToString(it) }
                }
            }
        }
    }

    suspend fun similaritySearch(queryEmbedding: FloatArray, limit: Int): List<ScoredChunk> {
        return suspendTransaction(db = db) {
            // Raw SQL for pgvector cosine distance operator
            exec(
                """SELECT id, document_id, chunk_index, content, metadata,
                   1 - (embedding <=> ?::vector) as similarity
                   FROM embeddings
                   ORDER BY embedding <=> ?::vector
                   LIMIT ?""",
                args = listOf(queryEmbedding, queryEmbedding, limit)
            ) { rs -> /* map ResultSet to ScoredChunk */ }
        }
    }
}
```

**RAG as a Koog Tool:**

The RAG pipeline is exposed as a `@Tool` that AI agents can invoke during conversation:

```kotlin
// server:ai/tools/RagTool.kt
class RagTools(private val ragService: RagService) : ToolSet {
    @Tool
    @LLMDescription("Search the knowledge base for relevant information")
    suspend fun searchKnowledge(
        @LLMDescription("The search query") query: String,
        @LLMDescription("Maximum number of results") maxResults: Int = 5,
    ): String {
        val results = ragService.query(query, maxResults)
        return results.joinToString("\n\n") { "[Source: ${it.documentId}]\n${it.content}" }
    }
}
```

**Document ingestion pipeline:**

```
[File uploaded to S3 with purpose=DOCUMENT]
    |
    v [FileService.confirmUpload() emits event]
    |
    v [DocumentIngestionService listens]
    |
    v [Download file from S3]
    |
    v [Split into chunks (TextSplitter — simple overlap chunking)]
    |
    v [Generate embeddings via LLMEmbedder (Google/Ollama)]
    |
    v [Store in EmbeddingsTable via PgVectorStorage]
    |
    v [Document ready for RAG queries]
```

**Embedding model configuration:**

```kotlin
// Addition to Env.kt
data class Embedding(
    val enabled: Boolean,
    val provider: String,       // "google" or "ollama"
    val model: String,          // "text-embedding-004" or "nomic-embed-text"
    val dimensions: Int,        // 768 for Google, 768 for nomic
    val chunkSize: Int,         // 512 tokens
    val chunkOverlap: Int,      // 50 tokens
) {
    companion object {
        operator fun invoke(env: String): Embedding = Embedding(
            enabled = System.getenv("EMBEDDING_ENABLED")?.toBooleanStrictOrNull() ?: false,
            provider = System.getenv("EMBEDDING_PROVIDER") ?: "google",
            model = System.getenv("EMBEDDING_MODEL") ?: "text-embedding-004",
            dimensions = System.getenv("EMBEDDING_DIMENSIONS")?.toIntOrNull() ?: 768,
            chunkSize = System.getenv("EMBEDDING_CHUNK_SIZE")?.toIntOrNull() ?: 512,
            chunkOverlap = System.getenv("EMBEDDING_CHUNK_OVERLAP")?.toIntOrNull() ?: 50,
        )
    }
}
```

### 3. Multi-Agent AI Orchestration — `server:ai` Extension

**Architecture: Koog Subgraph Composition**

The existing `server:ai` has two agents: `ChatAgentService` (conversational) and `AssistantAgentService` (ReAct tool-using). Multi-agent orchestration composes these via Koog's strategy graph system.

**Pattern: Coordinator Agent with Specialist Subgraphs**

```kotlin
// server:ai/agents/OrchestratorAgent.kt
class OrchestratorAgentService(
    private val chatAgent: ChatAgentService,
    private val assistantAgent: AssistantAgentService,
    private val ragService: RagService,
    private val config: Env.Ai,
) {
    suspend fun orchestrate(userId: Uuid, conversationId: Uuid, message: String): Flow<String> {
        // The orchestrator uses structured output to decide which agent to delegate to
        val routing = routeMessage(message)

        return when (routing.agentType) {
            AgentType.CHAT -> chatAgent.streamResponse(userId, conversationId, message)
            AgentType.ASSISTANT -> assistantAgent.executeWithTools(userId, conversationId, message)
            AgentType.RAG -> ragEnhancedResponse(userId, conversationId, message)
        }
    }

    private suspend fun routeMessage(message: String): RoutingDecision {
        // Uses Koog structured output to classify the message
        return promptExecutor.executeStructured<RoutingDecision>(
            prompt = routingPrompt(message)
        )
    }
}

@Serializable
@LLMDescription("Decision about which agent should handle the user's message")
data class RoutingDecision(
    @LLMDescription("The type of agent best suited for this message")
    val agentType: AgentType,
    @LLMDescription("Brief reasoning for the routing decision")
    val reasoning: String,
)

@Serializable
enum class AgentType {
    @LLMDescription("General conversation, greetings, casual talk")
    CHAT,
    @LLMDescription("Tasks requiring tools: calculations, data lookup, actions")
    ASSISTANT,
    @LLMDescription("Questions about documents, knowledge base queries")
    RAG,
}
```

**Koog Structured Output integration:**

Three levels of structured output available in Koog:

```kotlin
// Level 1: Prompt executor level (standalone, no agent context)
val result = promptExecutor.executeStructured<RoutingDecision>(prompt)

// Level 2: Agent session level (within an agent's execution)
agent.run {
    val decision = requestLLMStructured<RoutingDecision>(prompt)
}

// Level 3: Strategy graph node level (within custom strategy)
val node = nodeLLMRequestStructured<RoutingDecision>(nodeName = "router")
```

The orchestrator uses Level 1 for routing decisions (lightweight, no full agent context needed).

**Integration with existing WebSocket streaming:**

The existing `/api/ai/chat/ws` WebSocket endpoint is modified to route through the orchestrator instead of directly to `ChatAgentService`:

```kotlin
// Modified: server:ai/routes/AiRoutes.kt
fun Route.aiRoutes(orchestratorService: OrchestratorAgentService) {
    webSocket("/api/ai/chat/ws") {
        getAuth { auth ->
            for (frame in incoming) {
                val message = (frame as? Frame.Text)?.readText() ?: continue
                val parsed = Json.decodeFromString<WsMessage>(message)

                orchestratorService.orchestrate(auth.userId, parsed.conversationId, parsed.content)
                    .collect { chunk -> send(Frame.Text(chunk)) }
            }
        }
    }
}
```

### 4. Email Service — `server:email`

**Module:** New `server:email` server module
**Package:** `com.m2f.server.email`

**Architecture: Template-based SMTP with async send queue**

```kotlin
// server:email/service/EmailService.kt
class EmailService(
    private val config: Env.Email,
    private val templateEngine: EmailTemplateEngine,
) {
    suspend fun send(email: EmailMessage) {
        val session = Session.getInstance(smtpProperties())
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(config.fromAddress, config.fromName))
            setRecipient(Message.RecipientType.TO, InternetAddress(email.to))
            subject = email.subject
            setContent(email.htmlBody, "text/html; charset=utf-8")
        }
        // Jakarta Mail send is blocking — wrap in IO dispatcher
        withContext(Dispatchers.IO) {
            Transport.send(message)
        }
    }
}

// server:email/templates/EmailTemplateEngine.kt
class EmailTemplateEngine {
    fun renderInvite(inviterName: String, groupName: String, acceptUrl: String): String {
        return """
            <html><body>
            <h2>You've been invited!</h2>
            <p>$inviterName invited you to join <strong>$groupName</strong>.</p>
            <a href="$acceptUrl" style="...">Accept Invitation</a>
            </body></html>
        """.trimIndent()
    }

    fun renderPasswordReset(resetUrl: String): String { /* ... */ }
    fun renderWelcome(userName: String): String { /* ... */ }
}
```

**Why Jakarta Mail (not a Ktor email library):**
- Jakarta Mail is the standard Java email API, battle-tested, well-documented
- No Ktor-specific email library exists with meaningful adoption
- Works perfectly with MailHog in dev (SMTP on port 1025, no auth)
- Production: any SMTP provider (SendGrid, SES, Mailgun) via standard SMTP

**Email config:**

```kotlin
data class Email(
    val enabled: Boolean,
    val smtpHost: String,
    val smtpPort: Int,
    val smtpUser: String?,
    val smtpPassword: String?,
    val fromAddress: String,
    val fromName: String,
    val useTls: Boolean,
) {
    companion object {
        operator fun invoke(env: String): Email = Email(
            enabled = System.getenv("EMAIL_ENABLED")?.toBooleanStrictOrNull() ?: false,
            smtpHost = System.getenv("SMTP_HOST") ?: "localhost",
            smtpPort = System.getenv("SMTP_PORT")?.toIntOrNull() ?: 1025,
            smtpUser = System.getenv("SMTP_USER"),
            smtpPassword = System.getenv("SMTP_PASSWORD"),
            fromAddress = System.getenv("EMAIL_FROM_ADDRESS") ?: "noreply@template.local",
            fromName = System.getenv("EMAIL_FROM_NAME") ?: "Template App",
            useTls = System.getenv("EMAIL_USE_TLS")?.toBooleanStrictOrNull() ?: false,
        )
    }
}
```

### 5. Email Invitations — `server:groups` Extension

**Module:** Modifications to existing `server:groups`
**Package:** `com.m2f.server.groups.invites`

**Architecture: Token-based invite with email delivery**

```kotlin
// server:groups/tables/GroupInvitesTable.kt
object GroupInvitesTable : Table("group_invites") {
    val id = uuid("id").autoGenerate()
    val groupId = uuid("group_id").references(GroupsTable.id)
    val invitedEmail = varchar("invited_email", 255)
    val invitedByUserId = uuid("invited_by_user_id")
    val token = varchar("token", 64).uniqueIndex()  // secure random token
    val status = varchar("status", 20).default("PENDING")  // PENDING, ACCEPTED, EXPIRED, REVOKED
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}
```

**Invite flow:**

```
[Group Admin] POST /api/groups/{id}/invites { email: "user@example.com" }
    |
    v [GroupInviteService.createInvite()]
    |   - Validates caller is group ADMIN/OWNER
    |   - Generates secure random token (64 chars)
    |   - Inserts into group_invites table (status=PENDING, expires in 7 days)
    |   - Calls EmailService.send() with invite template
    |
    v [Email sent to user@example.com with accept link]
    |   Link: https://app.example.com/invite/accept?token={token}
    |
    v [Recipient clicks link]
    |
    v POST /api/invites/accept { token: "abc123..." }
    |
    v [GroupInviteService.acceptInvite()]
        - Validates token exists, not expired, status=PENDING
        - If user is authenticated: add to group_members
        - If user not registered: redirect to registration with invite context
        - Updates invite status to ACCEPTED
```

**Cross-module dependency: `server:groups` → `server:email`**

The invite service needs to send emails. This is wired via Koin injection:

```kotlin
// server:groups/service/GroupInviteService.kt
class GroupInviteService(
    private val inviteRepository: GroupInviteRepository,
    private val memberRepository: GroupMemberRepository,
    private val emailService: EmailService,  // from server:email module
    private val config: Configuration,
)
```

`server:groups` declares a dependency on `server:email` in its `build.gradle.kts`.

### 6. Avatar URL Migration — `server:auth` Extension

**Scope:** Minimal — one migration, one column, DTO update

```kotlin
// server:auth/migrations/V6_AddAvatarUrl.kt
class V6AddAvatarUrl : Migration {
    override val version = 6
    override val description = "Add avatar_url column to users table"

    override suspend fun migrate(db: R2dbcDatabase) {
        suspendTransaction(db = db) {
            exec("ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500)")
        }
    }
}
```

**UsersTable modification:**

```kotlin
// Add to existing UsersTable
val avatarUrl = varchar("avatar_url", 500).nullable()
```

**UserResponse DTO modification in `core:models`:**

```kotlin
// Modified: core:models/dto/UserResponse.kt
@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val avatarUrl: String?,  // NEW — nullable, null if no avatar uploaded
)
```

### 7. Docker Infrastructure Changes

**Modified `docker-compose.yml`:**

```yaml
services:
  postgres:
    image: pgvector/pgvector:pg15          # Changed from postgres:15-alpine
    environment:
      POSTGRES_DB: template_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5436:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/init-scripts:/docker-entrypoint-initdb.d  # For CREATE EXTENSION vector

  minio:                                    # NEW — S3-compatible storage
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"   # S3 API
      - "9001:9001"   # Web console
    volumes:
      - minio_data:/data

  mailhog:                                  # NEW — SMTP mock with web UI
    image: mailhog/mailhog:latest
    ports:
      - "1025:1025"   # SMTP
      - "8025:8025"   # Web UI for viewing sent emails

volumes:
  postgres_data:
  minio_data:
```

**Init script for pgvector:**

```sql
-- docker/init-scripts/01-extensions.sql
CREATE EXTENSION IF NOT EXISTS vector;
```

## Architectural Patterns

### Pattern 1: Presigned URL Upload (File Uploads)

**What:** Server generates time-limited signed URLs for direct client-to-S3 uploads
**When to use:** Any file upload (avatars, documents for RAG, attachments)
**Trade-offs:**
- ✅ Server never handles file bytes — lower memory, higher throughput
- ✅ Works with any S3-compatible store (AWS S3, MinIO, R2, etc.)
- ❌ Two-step process (get URL, then upload) adds client complexity
- ❌ Need upload confirmation step to track state

**Example:**

```kotlin
// Client flow (in SDK)
suspend fun uploadAvatar(imageBytes: ByteArray, fileName: String): Either<AppError, FileUploadResponse> = either {
    // Step 1: Get presigned URL from server
    val presigned = fileApi.requestUploadUrl(
        UploadUrlRequest(fileName = fileName, contentType = "image/jpeg", purpose = "AVATAR")
    ).bind()

    // Step 2: Upload directly to S3/MinIO
    httpClient.put(presigned.uploadUrl) {
        setBody(imageBytes)
        contentType(ContentType.Image.JPEG)
    }

    // Step 3: Confirm upload with server
    fileApi.confirmUpload(presigned.fileId).bind()
}
```

### Pattern 2: Custom Exposed ColumnType for pgvector

**What:** Bridge between Exposed's type system and R2DBC's native pgvector codec
**When to use:** Storing and querying vector embeddings in PostgreSQL
**Trade-offs:**
- ✅ Leverages R2DBC 1.0.3+ native Vector codec — no extra dependencies
- ✅ Works within Exposed's migration and query framework
- ❌ Similarity search requires raw SQL (Exposed DSL doesn't support `<=>` operator)
- ❌ Custom column type needs maintenance if Exposed changes internals

**Example:**

```kotlin
// Table definition — clean DSL
object EmbeddingsTable : Table("embeddings") {
    val embedding = vector("embedding", 768)  // custom column type
    // ...
}

// Insert — standard Exposed
EmbeddingsTable.insert {
    it[embedding] = floatArrayOf(0.1f, 0.2f, ...)
}

// Similarity search — raw SQL (pgvector operators not in Exposed DSL)
exec("SELECT * FROM embeddings ORDER BY embedding <=> $1::vector LIMIT $2")
```

### Pattern 3: Service Interface for Cross-Module Communication

**What:** Modules communicate via interfaces defined in shared core, not direct dependencies
**When to use:** When Module A needs to call Module B but shouldn't depend on B's internals
**Trade-offs:**
- ✅ Modules remain loosely coupled — can be tested independently
- ✅ Interface in shared core is a stable contract
- ❌ Extra indirection (interface + implementation + Koin binding)

**Example:**

```kotlin
// In server:core:config (shared)
interface UserProfileUpdater {
    suspend fun updateAvatarUrl(userId: Uuid, url: String?)
}

// In server:auth (implements)
class UserRepository(...) : UserProfileUpdater { ... }

// In server:files (consumes via Koin)
class FileService(private val userProfileUpdater: UserProfileUpdater, ...)

// Koin wiring in ServerModule
single<UserProfileUpdater> { get<UserRepository>() }
```

### Pattern 4: Coordinator-Delegator Multi-Agent

**What:** A lightweight orchestrator uses structured output to route messages to specialist agents
**When to use:** When different user intents require different agent capabilities (chat vs tools vs RAG)
**Trade-offs:**
- ✅ Each agent stays focused and simple
- ✅ Routing logic is explicit and testable (structured output)
- ✅ New agents can be added without modifying existing ones
- ❌ Extra LLM call for routing adds latency (~200-500ms)
- ❌ Routing errors cascade (wrong agent = poor response)

## Data Flow

### File Upload Pipeline (Avatar Example)

```
[Mobile/Web Client]
    |
    v dispatch(ProfileIntent.UploadAvatar(imageBytes))
    |
    v [ProfileViewModel.handleIntent()]
    |
    v fileApi.requestUploadUrl(UploadUrlRequest(
    |     fileName="avatar.jpg", contentType="image/jpeg", purpose="AVATAR"))
    |
    v HTTP POST /api/files/upload-url
    |    |
    |    v [FileService.generateUploadUrl()]
    |    |    - Generates S3 key: avatars/{userId}/{fileId}/avatar.jpg
    |    |    - Creates presigned PUT URL (expires 15 min)
    |    |    - Inserts FilesTable row (status=PENDING)
    |    v Returns PresignedUrlResponse { fileId, uploadUrl, expiresAt }
    |
    v HTTP PUT {uploadUrl} (direct to MinIO/S3)
    |    - Client sends raw image bytes with Content-Type header
    |    - No server involvement — direct S3 upload
    |
    v fileApi.confirmUpload(fileId)
    |
    v HTTP POST /api/files/{fileId}/confirm
    |    |
    |    v [FileService.confirmUpload()]
    |    |    - Validates object exists in S3 (HeadObject)
    |    |    - Updates FilesTable status → CONFIRMED, records sizeBytes
    |    |    - If purpose=AVATAR: calls UserProfileUpdater.updateAvatarUrl()
    |    |    - Generates presigned GET URL for the avatar
    |    v Returns FileUploadResponse { fileId, url, fileName }
    |
    v setState { copy(avatarUrl = response.url, isUploading = false) }
```

### RAG Query Pipeline

```
[User asks question in chat]
    |
    v WebSocket message to /api/ai/chat/ws
    |
    v [OrchestratorAgentService.orchestrate()]
    |
    v [routeMessage() — structured output classification]
    |    Returns: RoutingDecision(agentType=RAG, reasoning="...")
    |
    v [ragEnhancedResponse()]
    |    |
    |    v [RagService.query(userMessage, maxResults=5)]
    |    |    |
    |    |    v [LLMEmbedder.embed(userMessage)] → query vector
    |    |    |
    |    |    v [PgVectorStorage.similaritySearch(queryVector, limit=5)]
    |    |    |    |
    |    |    |    v SQL: SELECT content, 1-(embedding <=> query) as score
    |    |    |         FROM embeddings ORDER BY embedding <=> query LIMIT 5
    |    |    |
    |    |    v Returns List<ScoredChunk> (content + similarity score)
    |    |
    |    v [Build augmented prompt]:
    |    |    "Context from knowledge base:\n{chunks}\n\nUser question: {message}"
    |    |
    |    v [ChatAgentService.streamResponse(augmentedPrompt)]
    |    |
    |    v Streaming response chunks via WebSocket
    |
    v [Client receives streamed response]
```

### Email Invitation Flow

```
[Group Admin in app:admin]
    |
    v dispatch(InviteIntent.SendInvite(groupId, email))
    |
    v inviteApi.createInvite(groupId, InviteRequest(email))
    |
    v HTTP POST /api/groups/{groupId}/invites
    |    |
    |    v [GroupInviteService.createInvite()]
    |    |    - context(raise: Raise<DomainError>)
    |    |    - Validates caller is group ADMIN/OWNER
    |    |    - Generates secure token: SecureRandom.getInstanceStrong().nextBytes(32).toHex()
    |    |    - Inserts GroupInvitesTable (status=PENDING, expiresAt=now+7days)
    |    |    - Calls EmailService.send(
    |    |        to = email,
    |    |        subject = "Invitation to join {groupName}",
    |    |        htmlBody = templateEngine.renderInvite(inviterName, groupName, acceptUrl)
    |    |      )
    |    v Returns InviteResponse { inviteId, email, status, expiresAt }
    |
    v setState { copy(pendingInvites = pendingInvites + newInvite) }

--- Later, recipient clicks email link ---

[Recipient browser] → https://app.example.com/invite/accept?token={token}
    |
    v [Deep link / web route resolves to AcceptInviteScreen]
    |
    v inviteApi.acceptInvite(AcceptInviteRequest(token))
    |
    v HTTP POST /api/invites/accept
    |    |
    |    v [GroupInviteService.acceptInvite()]
    |         - Finds invite by token
    |         - Validates: status=PENDING, not expired
    |         - If user authenticated: adds to group_members, sets status=ACCEPTED
    |         - If user not registered: returns NeedRegistration error with invite context
    |
    v Either<AppError, AcceptInviteResponse>
         - Right: Navigate to group page
         - Left(NeedRegistration): Navigate to register with inviteToken param
```

### Multi-Agent Orchestration Flow

```
[User WebSocket message]
    |
    v [OrchestratorAgentService.orchestrate(userId, convId, message)]
    |
    v [ROUTING PHASE — single LLM call with structured output]
    |    Prompt: "Classify this user message into one of: CHAT, ASSISTANT, RAG"
    |    Output: RoutingDecision { agentType, reasoning }
    |
    +-------+-------+
    |       |       |
    v       v       v
   CHAT  ASSISTANT  RAG
    |       |       |
    v       v       v
  [Chat   [ReAct  [RAG-enhanced
   Agent]  Agent   Chat]
    |    with       |
    |    Tools]     v
    |       |    [Embed query]
    |       |       |
    |       v    [pgvector search]
    |    [Tool      |
    |    execution] [Augment prompt]
    |       |       |
    v       v       v
  [Streaming response via WebSocket]
```

## Scaling Considerations

| Scale | Architecture Adjustments |
|-------|--------------------------|
| 0-1k users | Monolith as-is. MinIO for S3. MailHog for email. Single Postgres with pgvector. All agents share one LLM connection. |
| 1k-10k users | Move S3 to real AWS S3 / Cloudflare R2. Move email to SendGrid/SES. Add connection pooling for Postgres. Consider background job queue for document ingestion. |
| 10k-100k users | Separate embedding generation into background worker. Add Redis for presigned URL caching. Consider dedicated vector DB (Qdrant/Weaviate) if pgvector becomes bottleneck. Rate-limit LLM calls per user. |
| 100k+ users | S3 with CloudFront CDN for file delivery. Dedicated embedding service. Async email queue (SQS/RabbitMQ). Read replicas for Postgres. Shard embeddings by tenant/group. |

### Scaling Priorities

1. **First bottleneck: LLM API rate limits** — The multi-agent orchestrator makes 1+ LLM calls per message (routing + response). At scale, implement per-user rate limiting and request queuing.
2. **Second bottleneck: Embedding generation** — Document ingestion is CPU/API-intensive. Move to async background processing early. Don't block the upload confirmation on embedding completion.
3. **Third bottleneck: S3 presigned URL generation** — Each file access generates a presigned URL. Cache these with short TTL (5 min) to reduce S3 SDK calls.

## Anti-Patterns

### Anti-Pattern 1: Server-Side File Proxying

**What people do:** Client uploads file to server via multipart form, server buffers in memory, then forwards to S3.
**Why it's wrong:** Server becomes a bottleneck. For a 10MB image, the server must hold 10MB in memory per concurrent upload. Under load, this causes OOM crashes.
**Do this instead:** Presigned URL pattern. Server generates a signed URL, client uploads directly to S3. Server never touches file bytes.

### Anti-Pattern 2: Synchronous Embedding on Upload

**What people do:** When a document is uploaded, immediately generate embeddings before returning the upload response.
**Why it's wrong:** Embedding generation can take 5-30 seconds for large documents. The upload API times out, and the client gets no feedback.
**Do this instead:** Confirm the upload immediately. Trigger embedding generation asynchronously. Provide a document status endpoint (`PROCESSING` → `READY` → `ERROR`) for the client to poll or subscribe to.

### Anti-Pattern 3: Storing Embeddings in Application Memory

**What people do:** Use Koog's `InMemoryVectorStorage` for RAG in production.
**Why it's wrong:** Embeddings are lost on server restart. Cannot scale horizontally (each instance has different data). Memory usage grows unbounded with document count.
**Do this instead:** Use `PgVectorStorage` backed by PostgreSQL pgvector. Persistent, queryable, shared across server instances, backed up with the database.

### Anti-Pattern 4: Direct Module Dependencies for Cross-Cutting Concerns

**What people do:** `server:files` directly depends on `server:auth` to update the user's avatar URL.
**Why it's wrong:** Creates circular dependency risk. Makes `server:files` untestable without `server:auth`. Tight coupling between unrelated domains.
**Do this instead:** Define a `UserProfileUpdater` interface in `server:core:config`. `server:auth` implements it, `server:files` consumes it. Koin wires the binding.

### Anti-Pattern 5: Single Monolithic Agent

**What people do:** One giant AI agent with all tools (RAG search, user management, file operations) in a single tool set.
**Why it's wrong:** Large tool sets confuse LLMs — they make worse tool selection decisions. Prompt becomes enormous. Different tasks need different system prompts and temperatures.
**Do this instead:** Coordinator-delegator pattern. A lightweight router classifies intent, then delegates to specialist agents that each have focused tool sets and optimized prompts.

### Anti-Pattern 6: Exposing S3 Keys/URLs in Database without Presigned Access

**What people do:** Store permanent public S3 URLs in the database and serve them directly to clients.
**Why it's wrong:** Public URLs bypass access control. Anyone with the URL can access the file. Cannot revoke access. S3 bucket must be public.
**Do this instead:** Store S3 keys (not URLs) in the database. Generate time-limited presigned GET URLs on demand. Bucket stays private. URLs expire automatically.

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| MinIO / AWS S3 | `aws.sdk.kotlin:s3` (JVM-only) via `S3Client` | Use MinIO in dev (port 9000), real S3 in prod. Same API. |
| MailHog / SMTP | Jakarta Mail `Transport.send()` | MailHog in dev (port 1025, no auth), SendGrid/SES in prod via SMTP. Web UI at :8025 for dev. |
| pgvector | R2DBC native codec + raw SQL for similarity ops | Requires `pgvector/pgvector:pg15` Docker image. HNSW index for performance. |
| Google AI (Embeddings) | Koog `LLMEmbedder` wrapping `GoogleLLMClient` | Same API key as existing AI config. Model: `text-embedding-004`. |
| Google AI (Structured Output) | Koog `executeStructured<T>()` | Uses existing Gemini model. Requires `@Serializable` + `@LLMDescription` DTOs. |

### Internal Module Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| `server:files` → `server:auth` | Via `UserProfileUpdater` interface (Koin) | No direct dependency. Interface in `server:core:config`. |
| `server:groups` → `server:email` | Direct dependency (Koin injection) | `GroupInviteService` calls `EmailService.send()`. |
| `server:ai` → `server:files` | Via `DocumentStore` interface (Koin) | RAG ingestion triggers on file upload confirmation for DOCUMENT purpose. |
| `server:ai` → `server:core:database` | Uses `VectorColumnType` from shared database utils | Custom column type is a shared utility, not a module dependency. |
| `core:sdk` → `core:models` | DTOs and route definitions | Existing pattern — unchanged. |
| `app:profile` → `core:sdk` | `FileApi` for presigned URLs, `UserApi` for profile updates | New API class follows existing pattern. |
| `app:admin` → `core:sdk` | `InviteApi` for group invitations | New API class follows existing pattern. |
| `app:designsystem` → (none) | `TerminalAvatar` accepts optional `imageUrl: String?` | No new dependency — just a parameter addition. |

## Build Order (Respecting Dependencies)

```
Phase 1: Infrastructure Foundation (no inter-dependencies)
  1a. docker-compose.yml changes (pgvector, MinIO, MailHog)
  1b. server:core:config — add Env.S3, Env.Email, Env.Embedding
  1c. server:core:database — add VectorColumnType utility
  1d. core:models — add new DTOs (FileUploadResponse, InviteRequest, etc.)
  These can all be built in parallel.

Phase 2: Core Services (depends on Phase 1)
  2a. server:files — S3 service, repository, routes, migrations, Koin module
  2b. server:email — EmailService, EmailTemplateEngine, Koin module
  2c. server:auth migration — V6_AddAvatarUrl, UsersTable.avatarUrl column
  2a, 2b, 2c can be built in parallel.

Phase 3: Integration Features (depends on Phases 1 + 2)
  3a. server:groups invites — GroupInvitesTable, GroupInviteService, invite routes
       (depends on server:email from 2b)
  3b. server:ai RAG — EmbeddingsTable, PgVectorStorage, RagService, RagTools
       (depends on VectorColumnType from 1c, Env.Embedding from 1b)
  3c. server:ai structured output — RoutingDecision DTOs, executeStructured integration
  3a and 3b can be built in parallel. 3c can parallel with 3b.

Phase 4: Multi-Agent Orchestration (depends on Phase 3)
  4.  server:ai orchestrator — OrchestratorAgentService, routing logic, WebSocket update
      (depends on RAG from 3b, structured output from 3c)

Phase 5: SDK & Client (depends on Phases 2-4)
  5a. core:sdk — add FileApi, InviteApi, update Sdk facade
  5b. core:testing — add FakeFileApi, FakeInviteApi fakes
  5c. app:profile — avatar upload flow (image picker → presigned URL → S3)
  5d. app:designsystem — TerminalAvatar image support
  5e. app:admin — invite management UI
  5a before 5b-5e. 5c, 5d, 5e can be parallel after 5a.

Phase 6: Wiring & Developer Onboarding
  6a. Wire all new modules in Application.kt, ServerModule.kt, settings.gradle.kts
  6b. Environment variable documentation (.env.example)
  6c. Developer setup guide (docker-compose up, MinIO bucket creation, etc.)
  6d. Integration tests using core:testing
```

**Phase ordering rationale:**
- Phase 1 first because infrastructure (Docker, config, column types, DTOs) has zero runtime dependencies and unblocks everything else.
- Phase 2 builds the two new modules (files, email) which are consumed by Phase 3 features.
- Phase 3 builds features that compose Phase 2 services (invites need email, RAG needs vector storage).
- Phase 4 is the capstone AI feature that composes all Phase 3 AI work (RAG + structured output → orchestrator).
- Phase 5 is client-side work that consumes the server APIs built in Phases 2-4.
- Phase 6 is wiring and documentation — done last because it touches cross-cutting concerns.

**Critical dependency chains:**
- `VectorColumnType` (1c) → `EmbeddingsTable` (3b) → `PgVectorStorage` (3b) → `RagService` (3b) → `OrchestratorAgent` (4)
- `EmailService` (2b) → `GroupInviteService` (3a) → `InviteApi` (5a) → `app:admin` invite UI (5e)
- `FileService` (2a) → `FileApi` (5a) → `app:profile` avatar upload (5c)
- `UsersTable.avatarUrl` (2c) → `UserResponse.avatarUrl` (1d) → `TerminalAvatar` image support (5d)

## Sources

- R2DBC PostgreSQL 1.0.3+ vector codec — HIGH confidence (verified from pgvector-java compatibility docs, project uses r2dbc-postgresql 1.0.7.RELEASE)
- Koog RAG: `LLMEmbedder`, `RankedDocumentStorage`, `VectorStorage` interfaces — HIGH confidence (verified via Context7 Koog docs)
- Koog structured output: `executeStructured<T>()`, `@LLMDescription`, `StructureFixingParser` — HIGH confidence (verified via Context7 Koog docs)
- Koog multi-agent: subgraphs, custom strategy graphs — MEDIUM confidence (API exists in Koog 0.6.2, but complex composition patterns are less documented)
- AWS SDK for Kotlin S3: `aws.sdk.kotlin:s3:1.6.22` — HIGH confidence (JVM-only, verified in project's libs.versions.toml)
- Jakarta Mail — HIGH confidence (standard Java email API, well-documented, successor to javax.mail)
- pgvector Docker image: `pgvector/pgvector:pg15` — HIGH confidence (official pgvector Docker Hub)
- MinIO S3 compatibility — HIGH confidence (widely used S3-compatible dev replacement)
- MailHog — HIGH confidence (standard dev SMTP mock, ports 1025/8025)
- Exposed custom column types — MEDIUM confidence (pattern works but requires maintenance with Exposed version upgrades)

---
*Architecture research for: KMP Full-Stack Template v1.2 — pgvector RAG, S3 File Uploads, Multi-Agent AI, Email Invitations*
*Researched: 2026-02-21*
