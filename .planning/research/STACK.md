# Stack Research

**Domain:** KMP Full-Stack Template — Milestone v1.2: Polish & Patterns (AI, S3, Email, Developer Onboarding)
**Researched:** 2026-02-21
**Confidence:** HIGH (all versions verified against Maven Central, official GitHub releases, and official documentation)

---

## Existing Stack (Already in Place — Do NOT Add)

These are already integrated and confirmed in `gradle/libs.versions.toml`. Listed for context to prevent duplication.

| Technology | Version | Purpose |
|---|---|---|
| Kotlin | 2.3.10 | Language (context parameters via `-Xcontext-parameters`) |
| Compose Multiplatform | 1.10.1 | Shared UI framework (Android, iOS, Desktop, WASM) |
| Ktor Server | 3.4.0 | Backend HTTP framework |
| Ktor Client | 3.4.0 | Multiplatform HTTP client |
| Exposed | 1.0.0 | Database ORM (R2DBC + JDBC) |
| PostgreSQL + R2DBC | 42.7.9 / 1.0.7.RELEASE | Database drivers |
| Koin | 4.1.1 | DI across server + all KMP targets |
| Arrow | 2.2.1.1 | FP (core/fx/resilience), context parameter Raise |
| Koog | 0.6.2 | AI agent framework (`koog-ktor`, `koog-agents`) |
| Kotest assertions | 6.1.3 | Test assertions (core, arrow, arrow-fx) |
| Testcontainers | 2.0.3 | Server integration testing (PostgreSQL containers) |
| Turbine | 1.2.1 | Flow testing |
| kotlinx-coroutines | 1.10.2 | Async runtime (+ test) |
| kotlinx-serialization | 1.10.0 | JSON serialization |
| Navigation Compose | 2.9.2 | Multiplatform navigation |
| Kermit | 2.0.8 | Multiplatform logging |
| AndroidX Lifecycle ViewModel | 2.9.6 | KMP ViewModel |
| Kover | 0.9.7 | Code coverage |
| Detekt | 1.23.8 | Static analysis |

---

## Recommended Stack (New Additions for v1.2)

### 1. AI Patterns: RAG with pgvector

#### 1a. pgvector PostgreSQL Extension (Docker Image)

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| `pgvector/pgvector:pg15` Docker image | pg15 (pgvector 0.8.x) | Vector similarity search in PostgreSQL | Adds the `vector` column type and nearest-neighbor operators (`<->`, `<#>`, `<=>`) directly in Postgres. Eliminates the need for a separate vector database (Qdrant, Pinecone, Weaviate). Keeps the entire data layer in one system. Supports HNSW and IVFFlat indexes for fast approximate search at scale. | HIGH |

**Docker change:** Replace `postgres:15-alpine` with `pgvector/pgvector:pg15` in `docker-compose.yml`. The pgvector image is based on the official PostgreSQL image with the `vector` extension pre-installed. Fully drop-in compatible — existing tables and data are unaffected.

**Migration needed:** `CREATE EXTENSION IF NOT EXISTS vector;` in a database migration.

**No JVM library required for R2DBC.** The R2DBC PostgreSQL driver (1.0.3+, project uses 1.0.7.RELEASE) has native support for the `vector` type. Vectors are read/written as `float[]` arrays. No `com.pgvector:pgvector` Java library needed when using R2DBC — that library is only needed for JDBC `PGvector` type registration.

**Exposed integration:** Define vector columns using raw SQL column type in Exposed:
```kotlin
object EmbeddingsTable : Table("embeddings") {
    val id = uuid("id").autoGenerate()
    val content = text("content")
    val embedding = registerColumn<FloatArray>("embedding", VectorColumnType(1536))
    // Custom ColumnType wrapping float[] <-> vector
}
```

#### 1b. Koog Embeddings Module

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| `ai.koog:koog-agents-ext-embeddings` | 0.6.2 | `LLMEmbedder` for generating text embeddings | Provides the `Embedder` interface and `LLMEmbedder` class that works with OpenAI, Ollama, and AWS Bedrock embedding models. Stays within the Koog ecosystem already used in the project. Supports `OpenAIModels.Embeddings.TextEmbeddingAda002` and Ollama local models. | MEDIUM |

**Note on module name:** The Koog repository shows two sub-modules under `embeddings/`: `embeddings-base` (core interfaces) and `embeddings-llm` (LLM-backed implementation). The published Maven artifact name is `koog-agents-ext-embeddings`. Confirm exact artifact ID during implementation — may need to add both `embeddings-base` and `embeddings-llm` as separate dependencies if the umbrella artifact doesn't exist.

**Gradle dependency:**
```kotlin
// In libs.versions.toml (version same as existing koog)
koog-embeddings = { module = "ai.koog:koog-agents-ext-embeddings", version.ref = "koog" }
```

#### 1c. Koog RAG Module

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| `ai.koog:koog-agents-ext-rag` | 0.6.2 | `RankedDocumentStorage`, `EmbeddingBasedDocumentStorage` | Provides the document storage abstraction for RAG. Defines the `VectorStorage` interface that we implement against pgvector. Includes similarity ranking, document chunking utilities. | MEDIUM |

**Custom implementation needed:** Koog RAG provides the `VectorStorage` interface but does NOT ship a pgvector backend out of the box. We need to implement `VectorStorage` using our Exposed + R2DBC pgvector setup. This is intentional — Koog is storage-agnostic, and our implementation keeps everything in PostgreSQL.

**Gradle dependency:**
```kotlin
koog-rag = { module = "ai.koog:koog-agents-ext-rag", version.ref = "koog" }
```

#### 1d. Structured Output (No New Dependencies)

Koog 0.6.2 already includes structured output support via `executeStructured<T>()`, `requestLLMStructured<T>()`, and `nodeLLMRequestStructured<T>()`. Uses `@Serializable` data classes with `@LLMDescription` annotations and generates JSON schemas. **No new library needed** — this is built into `koog-agents` which is already in the project.

### 2. S3-Compatible File Storage

#### 2a. AWS SDK for Kotlin — S3 Client

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| `aws.sdk.kotlin:s3` | 1.6.22 | Server-side S3/MinIO operations | The official AWS SDK for Kotlin. JVM-only (server module). Provides idiomatic Kotlin coroutine-based S3 API: `putObject`, `getObject`, `deleteObject`, `presignGetObject` for signed URLs. Works with MinIO in local dev (S3-compatible API). Active development — 1.6.22 released Feb 20, 2026. | HIGH |

**JVM-only constraint:** This is only used in the `server:storage` module. The SDK is JVM-only — it does NOT support Kotlin Multiplatform targets. Client apps load images via HTTP URLs (presigned S3 URLs or proxied through Ktor).

**Gradle dependency:**
```kotlin
// In libs.versions.toml
aws-s3 = "1.6.22"
aws-s3 = { module = "aws.sdk.kotlin:s3", version.ref = "aws-s3" }
```

**MinIO compatibility:** AWS SDK Kotlin supports endpoint override, which allows pointing to MinIO (`http://localhost:9000`) in local dev:
```kotlin
S3Client {
    region = "us-east-1"
    endpointUrl = Url(env.s3Endpoint) // "http://localhost:9000" for MinIO
    credentialsProvider = StaticCredentialsProvider {
        accessKeyId = env.s3AccessKey
        secretAccessKey = env.s3SecretKey
    }
    forcePathStyle = true // Required for MinIO
}
```

#### 2b. MinIO Docker Image

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| `minio/minio:latest` | latest | Local dev S3-compatible object storage | Industry-standard S3-compatible storage for local development. Eliminates need for real AWS account during dev. Ships with web console on port 9001 for browsing buckets. Drop-in replacement for AWS S3 API. | HIGH |

**Docker-compose addition:**
```yaml
minio:
  image: minio/minio:latest
  command: server /data --console-address ":9001"
  ports:
    - "9000:9000"   # S3 API
    - "9001:9001"   # Web console
  environment:
    MINIO_ROOT_USER: minioadmin
    MINIO_ROOT_PASSWORD: minioadmin
  volumes:
    - minio_data:/data
```

### 3. Email (Group Invite Links)

#### 3a. Simple Java Mail

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| `org.simplejavamail:simple-java-mail` | 8.12.6 | SMTP email sending for group invite links | Clean builder API, async sending via `mailer.sendMail(email)` with `.async()`, handles MIME, HTML, and plain text. 16+ years of active development, 143 dependents on Maven Central. Much simpler than raw Jakarta Mail. Built-in connection pooling and batch module. Works great with MailHog for local testing. | HIGH |

**Why over Jakarta Mail:** Jakarta Mail (`jakarta.mail-api`) is low-level — requires manual `MimeMessage` construction, `Session` management, and verbose try-catch patterns. SimpleJavaEmail wraps it with a builder API that's one-tenth the code.

**Why over Ktor SMTP:** Ktor has no built-in SMTP client. You'd need to write raw socket communication or pull in a mail library anyway.

**Gradle dependency:**
```kotlin
// In libs.versions.toml
simple-java-mail = "8.12.6"
simple-java-mail = { module = "org.simplejavamail:simple-java-mail", version.ref = "simple-java-mail" }
```

**Usage pattern:**
```kotlin
val mailer = MailerBuilder
    .withSMTPServer(env.smtpHost, env.smtpPort)
    .withTransportStrategy(TransportStrategy.SMTP) // MailHog: no TLS
    .async()
    .buildMailer()

val email = EmailBuilder.startingBlank()
    .from("noreply@template.dev", "Template App")
    .to(recipientEmail)
    .withSubject("You've been invited to join ${group.name}")
    .withHTMLText(inviteHtml)
    .withPlainText(invitePlainText)
    .buildEmail()

mailer.sendMail(email) // async, non-blocking
```

#### 3b. MailHog Docker Image

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| `mailhog/mailhog:latest` | latest | Local dev email testing (SMTP + web UI) | Catches all outbound SMTP email without delivering. Web UI on port 8025 shows sent emails. Zero configuration — accepts any SMTP connection on port 1025. Standard tool for local email development. | HIGH |

**Docker-compose addition:**
```yaml
mailhog:
  image: mailhog/mailhog:latest
  ports:
    - "1025:1025"   # SMTP
    - "8025:8025"   # Web UI
```

### 4. Client-Side Image Loading (Profile Avatars)

#### 4a. Coil 3 (Compose Multiplatform)

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| `io.coil-kt.coil3:coil-compose` | 3.3.0 | Loading profile images from S3 URLs in Compose | Coil 3 is the standard image loading library for Compose Multiplatform. Full KMP support: Android, iOS, Desktop, WASM/JS. Provides `AsyncImage` composable with automatic caching, memory/disk cache, placeholder/error states. 11.7k GitHub stars. Latest 3.3.0 released Jul 22, 2025. | HIGH |

**Artifacts needed:**
```kotlin
// In libs.versions.toml
coil = "3.3.0"
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-ktor = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coil" }
```

**Why `coil-network-ktor3`:** Coil needs an HTTP engine for fetching images. The `coil-network-ktor3` module uses our existing Ktor Client as the HTTP engine, avoiding adding OkHttp or another HTTP client just for images.

**Usage pattern:**
```kotlin
AsyncImage(
    model = user.avatarUrl, // presigned S3 URL
    contentDescription = "Profile avatar",
    modifier = Modifier.size(48.dp).clip(CircleShape),
    placeholder = painterResource(Res.drawable.avatar_placeholder),
    error = painterResource(Res.drawable.avatar_placeholder),
)
```

**KMP targets confirmed:** Coil 3.x supports Android, iOS (via ktor-darwin engine), Desktop (JVM), and WASM/JS — all targets in this project.

---

## Version Catalog Additions (New Entries Only)

```toml
[versions]
aws-s3 = "1.6.22"
coil = "3.3.0"
simple-java-mail = "8.12.6"
# koog = "0.6.2" (already exists)

[libraries]
# AI - Koog Extensions (RAG + Embeddings)
koog-embeddings = { module = "ai.koog:koog-agents-ext-embeddings", version.ref = "koog" }
koog-rag = { module = "ai.koog:koog-agents-ext-rag", version.ref = "koog" }

# S3 Storage
aws-s3 = { module = "aws.sdk.kotlin:s3", version.ref = "aws-s3" }

# Email
simple-java-mail = { module = "org.simplejavamail:simple-java-mail", version.ref = "simple-java-mail" }

# Image Loading (KMP)
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-ktor = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coil" }

[bundles]
# Extended Koog bundle (adds RAG + Embeddings to existing koog bundle)
koog-ai = [
    "koog-ktor",
    "koog-agents",
    "koog-embeddings",
    "koog-rag",
]

# Image loading bundle for KMP app modules
image-loading = [
    "coil-compose",
    "coil-network-ktor",
]
```

---

## Docker-Compose Additions

```yaml
# Replace existing postgres service image
services:
  postgres:
    image: pgvector/pgvector:pg15  # was postgres:15-alpine
    # ... rest unchanged

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    volumes:
      - minio_data:/data

  mailhog:
    image: mailhog/mailhog:latest
    ports:
      - "1025:1025"
      - "8025:8025"

volumes:
  minio_data:
```

---

## Version Compatibility Matrix

| New Addition | Compatible With | Notes |
|---|---|---|
| `pgvector/pgvector:pg15` Docker | PostgreSQL R2DBC 1.0.7.RELEASE | R2DBC PostgreSQL 1.0.3+ supports native `vector` type. Our 1.0.7.RELEASE is well above minimum. |
| `ai.koog:koog-agents-ext-embeddings` 0.6.2 | Koog 0.6.2, Kotlin 2.3.10 | Same version as existing Koog deps. Published to Maven Central under `ai.koog` group. |
| `ai.koog:koog-agents-ext-rag` 0.6.2 | Koog 0.6.2, Kotlin 2.3.10 | Same version as existing Koog deps. Depends on embeddings module. |
| `aws.sdk.kotlin:s3` 1.6.22 | Kotlin 2.3.0+ (built against 2.3.0), JVM only | SDK uses Smithy Kotlin runtime 1.6.4. JVM-only — used in server module only. Kotlin ABI compatible with 2.3.10. |
| `org.simplejavamail:simple-java-mail` 8.12.6 | JVM 17+, any SMTP server | Wraps Jakarta Mail. JVM-only — used in server module only. No Kotlin version constraint. |
| `io.coil-kt.coil3:coil-compose` 3.3.0 | Kotlin 2.2.0+ (built against 2.2.0), CMP 1.10.1 | KMP multiplatform (Android, iOS, Desktop, WASM, JS). Kotlin ABI compatible with 2.3.10. |
| `io.coil-kt.coil3:coil-network-ktor3` 3.3.0 | Ktor Client 3.x | Uses Ktor 3 as HTTP engine for fetching images. Compatible with existing Ktor 3.4.0 client. |
| MinIO Docker `latest` | AWS SDK Kotlin S3 | 100% S3-compatible API. Use `forcePathStyle = true` with endpoint override. |
| MailHog Docker `latest` | Simple Java Mail 8.12.6 | Accepts SMTP on port 1025 with no auth. Set `TransportStrategy.SMTP` (no TLS). |

---

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|---|---|---|---|
| Vector DB | pgvector (PostgreSQL extension) | Qdrant, Weaviate, Pinecone | Separate vector DB adds operational complexity (another service, another data sync). pgvector keeps everything in PostgreSQL — one database, one backup, one migration path. For template-scale RAG (thousands to low millions of vectors), pgvector with HNSW indexes is more than sufficient. |
| Vector DB | pgvector (PostgreSQL extension) | Chroma | Python-native, no Kotlin client. Would require HTTP API adapter. pgvector is native SQL. |
| Embeddings | Koog `LLMEmbedder` | Direct OpenAI API calls | Would bypass the Koog abstraction layer already in the project. LLMEmbedder supports multiple providers (OpenAI, Ollama, Bedrock) through a single interface. |
| S3 Client | AWS SDK for Kotlin | MinIO Java SDK | AWS SDK for Kotlin is idiomatic Kotlin with coroutine support. MinIO SDK is Java-based with blocking APIs. AWS SDK works with both AWS S3 and MinIO via endpoint override. |
| S3 Client | AWS SDK for Kotlin | Ktor Client + raw S3 REST API | S3 REST API requires SigV4 signing, multipart upload handling, etc. AWS SDK handles all of this. Don't reinvent the wheel. |
| Local S3 | MinIO | LocalStack | LocalStack emulates dozens of AWS services — overkill for S3 only. MinIO is purpose-built for S3 compatibility, lighter, and faster to start. |
| Email | Simple Java Mail | Jakarta Mail (raw) | Jakarta Mail requires verbose `MimeMessage` construction, manual `Session` management, no built-in async. SimpleJavaEmail is a thin wrapper that reduces boilerplate by 80%. |
| Email | Simple Java Mail | Spring Mail | Wrong ecosystem. This is Ktor, not Spring. |
| Email | Simple Java Mail | Ktor SMTP | Ktor has no SMTP client. You'd write raw TCP/TLS socket code or pull in Jakarta Mail anyway. |
| Email | Simple Java Mail | SendGrid/Mailgun SDK | SaaS dependency for a template project. SMTP is universal and works with any provider. SimpleJavaEmail + MailHog for dev, any SMTP server for prod. |
| Local Email | MailHog | Mailtrap | Mailtrap is a SaaS service (requires account). MailHog is local Docker, zero config, instant. |
| Image Loading | Coil 3 | Kamel | Kamel is less mature (fewer stars, smaller community) and less actively maintained than Coil. Coil 3 is the de facto standard for Compose Multiplatform image loading. |
| Image Loading | Coil 3 | Raw Ktor Client + BitmapFactory | Manual implementation for every platform (Android, iOS, Desktop, WASM). No caching, no placeholder states, no error handling. Coil provides all of this out of the box. |
| RAG Framework | Koog RAG + custom pgvector | LangChain | LangChain is Python. No Kotlin equivalent. Koog is the Kotlin-native AI agent framework already in the project. |

---

## What NOT to Add

| Avoid | Why | Use Instead |
|---|---|---|
| Qdrant / Weaviate / Pinecone | Separate vector DB adds operational complexity. Template only needs pgvector-scale RAG. | pgvector in PostgreSQL (already the project's database) |
| Chroma | Python-native, no Kotlin client | pgvector with Koog RAG module |
| LangChain | Python framework, not Kotlin | Koog (already in project) + Koog RAG/Embeddings extensions |
| `com.pgvector:pgvector` Java library | Only needed for JDBC `PGvector` type registration. R2DBC PostgreSQL 1.0.3+ has native vector support. | Raw `float[]` with R2DBC PostgreSQL driver (already at 1.0.7.RELEASE) |
| MinIO Java SDK | Java-based with blocking APIs | AWS SDK for Kotlin (coroutine-based, works with MinIO via endpoint override) |
| LocalStack | Overkill — emulates all AWS services | MinIO (purpose-built S3 compatibility) |
| SendGrid / Mailgun SDK | SaaS dependency, not suitable for template | Simple Java Mail (works with any SMTP) |
| Spring Mail | Wrong ecosystem (Spring, not Ktor) | Simple Java Mail |
| Material3 | Project uses custom terminal design system | Existing terminal-themed `app:designsystem` |
| Kamel | Less mature than Coil 3, smaller community | Coil 3 |
| Complex email template engines (Thymeleaf, FreeMarker) | Overkill for invite links — simple text/HTML sufficient | Kotlin string templates for HTML email body |

---

## Stack Patterns by Feature

### If implementing RAG with pgvector:
- Switch Docker image to `pgvector/pgvector:pg15` (drop-in replacement)
- Add migration: `CREATE EXTENSION IF NOT EXISTS vector`
- Create `EmbeddingsTable` in Exposed with custom `VectorColumnType` wrapping `float[]`
- Add `koog-embeddings` for `LLMEmbedder` (OpenAI `text-embedding-ada-002` or Ollama local)
- Add `koog-rag` and implement custom `VectorStorage` against pgvector
- Use SQL operators: `<->` (L2 distance), `<=>` (cosine distance), `<#>` (inner product)
- Create HNSW index: `CREATE INDEX ON embeddings USING hnsw (embedding vector_cosine_ops)`

### If implementing S3 file storage:
- Add MinIO to `docker-compose.yml` (ports 9000/9001)
- Create `server:storage` module with `StorageService` interface
- Use `aws.sdk.kotlin:s3` with endpoint override for MinIO in dev, real S3 in prod
- Generate presigned URLs for client-side image access (time-limited, no auth needed)
- Proxy uploads through Ktor server (client → server → S3) for access control
- Add Coil 3 (`coil-compose` + `coil-network-ktor3`) to KMP app modules for loading images

### If implementing email invites:
- Add MailHog to `docker-compose.yml` (SMTP 1025, Web UI 8025)
- Create `server:email` module with `EmailService` interface
- Use `simple-java-mail` with async mailer, `TransportStrategy.SMTP` for MailHog dev
- Production: switch to `TransportStrategy.SMTP_TLS` with real SMTP credentials from env
- Invite link format: `https://app.example.com/invite/{token}` with expiry

### If implementing structured output:
- No new dependencies — Koog 0.6.2 has `executeStructured<T>()` built in
- Define `@Serializable` data classes with `@LLMDescription` annotations
- Use `requestLLMStructured<T>()` in agent write sessions
- Optional `StructureFixingParser` for automatic error correction

### If implementing multi-agent orchestration:
- No new dependencies — Koog 0.6.2 supports graph-based strategies, parallel node execution, and subgraphs
- Use `strategy("name") { }` DSL with multiple agent nodes
- Use `ToolRegistry` for shared tool access across agents
- Agent-to-agent communication via A2A protocol (built into Koog)

---

## Sources

### HIGH Confidence (Official docs, Maven Central, verified releases)
- [pgvector-java GitHub](https://github.com/pgvector/pgvector-java) — R2DBC section confirms 1.0.3+ native vector support; no Java library needed for R2DBC
- [pgvector/pgvector Docker Hub](https://hub.docker.com/r/pgvector/pgvector) — Official pgvector Docker images based on PostgreSQL
- [AWS SDK Kotlin S3 on Maven Central](https://central.sonatype.com/artifact/aws.sdk.kotlin/s3) — v1.6.22, released Feb 20, 2026
- [AWS SDK Kotlin GitHub Releases](https://github.com/aws/aws-sdk-kotlin/releases) — v1.6.22 latest, active daily releases
- [Coil 3 GitHub Releases](https://github.com/coil-kt/coil/releases) — v3.3.0 latest (Jul 22, 2025), KMP multiplatform
- [Coil Maven Central](https://central.sonatype.com/artifact/io.coil-kt.coil3/coil-compose) — v3.3.0, 548 dependents
- [Simple Java Mail official site](https://www.simplejavamail.org/) — v8.12.6, clean builder API, async sending
- [Simple Java Mail Maven Central](https://central.sonatype.com/artifact/org.simplejavamail/simple-java-mail) — v8.12.6, 143 dependents
- [Koog Official Docs](https://docs.koog.ai) — Structured output, embeddings, RAG, ranked document storage
- [Koog Structured Output Docs](https://docs.koog.ai/structured-output/) — `executeStructured<T>()`, JSON schema generation, `@LLMDescription`
- [Koog Embeddings Docs](https://docs.koog.ai/embeddings/) — `LLMEmbedder`, OpenAI/Ollama/Bedrock providers
- [Koog GitHub](https://github.com/JetBrains/koog) — v0.6.2, `embeddings/` and `rag/` directories confirmed
- [Koog RAG Maven Central](https://central.sonatype.com/artifact/ai.koog/koog-agents-ext-rag) — Artifact exists on Maven Central
- [Koog Embeddings Maven Central](https://central.sonatype.com/artifact/ai.koog/koog-agents-ext-embeddings) — Artifact exists on Maven Central

### MEDIUM Confidence (Multiple sources agree, needs implementation validation)
- Koog embeddings/RAG module exact artifact IDs — confirmed on Maven Central but page didn't render version; may need `embeddings-base` + `embeddings-llm` separately instead of umbrella `ext-embeddings`
- Coil 3.3.0 compatibility with Kotlin 2.3.10 — built against 2.2.0, but Kotlin ABI compatibility should hold; no known issues
- AWS SDK Kotlin 1.6.22 compatibility with Kotlin 2.3.10 — built against 2.3.0, should be ABI compatible

### LOW Confidence (Needs validation during implementation)
- Exposed custom `VectorColumnType` for R2DBC — need to verify how Exposed 1.0.0 R2DBC handles custom column types with `float[]` parameter binding; may need raw SQL fallback for vector operations
- Koog `VectorStorage` interface — confirmed in docs but exact interface contract needs implementation-time verification
- Coil `coil-network-ktor3` exact compatibility with Ktor 3.4.0 — Coil 3.3.0 was built before Ktor 3.4.0 release; likely compatible but unverified

---

*Stack research for: KMP Full-Stack Template — Milestone v1.2 (Polish & Patterns: AI, S3, Email, Developer Onboarding)*
*Researched: 2026-02-21*
