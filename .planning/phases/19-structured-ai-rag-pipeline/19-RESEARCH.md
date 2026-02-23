# Phase 19: Structured AI & RAG Pipeline - Research

**Researched:** 2026-02-23
**Domain:** Koog Framework (embeddings, RAG, structured output) + pgvector
**Confidence:** HIGH (verified against Koog 0.6.2 source JARs)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- No dedicated structured output API endpoint — Koog structured LLM calls are used inline in server code wherever needed
- No example schemas needed — the Koog integration itself is the pattern for template users
- Stick with Gemini for structured output calls (consistent with existing chat model)
- Structured output used wherever helpful — within RAG pipeline, multi-agent (Phase 20), or any server logic
- Embedding via Koog LLMEmbedder with Google text-embedding-004 (matches 768-dim pgvector column from Phase 17)
- Dedicated Documents UI for uploading and managing documents (not API-only)
- Auto-detect relevance — RAG triggers only when the query seems to need document context (not always-on, not manual toggle)
- Hidden context — retrieved chunks are injected silently into the prompt, no citations/sources shown to user
- Enhance existing AI chat streaming endpoint — RAG is built into current chat, not a separate endpoint
- Documents can be deleted with full cleanup — embeddings removed from vector store when document is deleted
- Two document levels: personal documents and group documents
- Any member can upload personal documents for themselves
- Only admins can upload group-level documents
- Admins can upload personal documents on behalf of specific group members (e.g., trainer assigns materials to a client)
- Admin-assigned personal documents are visible to the member but only deletable by an admin
- RAG search scope is role-based:
  - Members: RAG searches their personal documents only (including admin-assigned ones)
  - Admins: RAG searches personal documents AND/OR group documents
- Documents UI shows a single list with labels indicating personal vs group (no tabs)
- Admin document management lives in the admin panel's group section — admins manage group docs and member-assigned docs from there

### Claude's Discretion
- Auto-ingest vs explicit indexing action on upload
- Supported file types (text, PDF, etc.)
- Chunking strategy and chunk size
- Number of chunks retrieved per query
- Relevance detection heuristic for auto-RAG
- Loading states and empty states in Documents UI
- Exact layout of the Documents UI

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope
</user_constraints>

## Summary

Phase 19 adds two capabilities to the existing Koog-based AI infrastructure: (1) structured LLM output using Koog's `executeStructured()` API, and (2) a RAG pipeline that embeds uploaded documents into pgvector and retrieves relevant context for AI chat queries.

All required Koog modules (`embeddings-base`, `embeddings-llm`, `rag-base`, `vector-storage`, `prompt-structure`, `prompt-executor-google-client`) are already transitively included via the `koog-agents` dependency in `libs.versions.toml`. No new Gradle dependencies are needed.

The key custom component is a `PgVectorStorage` class implementing Koog's `VectorStorage<String>` interface, backed by the existing `document_embeddings` table with pgvector's `<=>` cosine similarity operator. The existing `DocumentEmbeddingsTable` needs additional columns for user-level scoping (`userId`, `documentId`, `chunkIndex`). A `DocumentsTable` must be created to track uploaded documents metadata and link chunks back to source documents for deletion cleanup.

**Primary recommendation:** Use `text-embedding-004` (768 dims, matching existing pgvector column) via custom `LLModel` constant, implement `VectorStorage<String>` with pgvector cosine similarity SQL, and use Koog `executeStructured()` with `GoogleModels.Gemini2_5Flash` for structured output calls (including the relevance detection heuristic).

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| AISTR-01 | Server exposes structured output endpoint returning typed JSON matching Kotlin data class | Koog `executeStructured()` API verified in source; works with `GoogleModels.Gemini2_5Flash` which has `LLMCapability.Schema.JSON.Standard`; Google-specific schema generators auto-registered |
| AISTR-02 | At least 2 example structured output data classes with @Serializable and @LLMDescription | `@LLMDescription` from `ai.koog.agents.core.tools.annotations`; `@Serializable` from kotlinx.serialization; `@property:LLMDescription` for fields; user decided no dedicated examples needed -- Koog integration IS the pattern |
| AISTR-03 | SDK provides function to request structured AI output as Either<AppError, T> | `executeStructured()` returns `Result<StructuredResponse<T>>` which maps to `Either` via Arrow; existing `DomainError`/`conduitAuth` pattern handles the mapping |
| RAG-02 | RAG pipeline uses exclusively Koog APIs -- no LangChain4j, Spring AI | All components verified: `LLMEmbedder`, `VectorStorage`, `EmbeddingBasedDocumentStorage`, `mostRelevantDocuments()` -- all from Koog modules |
| RAG-03 | Custom PgVectorStorage implements Koog VectorStorage backed by pgvector | `VectorStorage<String>` interface verified: `store()`, `delete()`, `read()`, `getPayload()`, `allDocuments()`, `allDocumentsWithPayload()` -- custom impl uses pgvector `<=>` operator |
| RAG-04 | Documents chunked and embedded via Koog LLMEmbedder stored in pgvector | `LLMEmbedder(client: LLMEmbeddingProvider, model: LLModel)` verified; `GoogleLLMClient` implements `LLMEmbeddingProvider`; `embed()` returns `Vector(values: List<Double>)` |
| RAG-05 | AI chat augmented with RAG context -- query embeds, retrieves top-K, injects into prompt | `mostRelevantDocuments(query, count, similarityThreshold)` verified; inject results into `ChatAgentService` system prompt |
| RAG-06 | RAG scoped to user's group -- Group A docs not searchable by Group B | WHERE clause on `group_id` in PgVectorStorage; additional `user_id` filtering per user decisions |
| RAG-07 | Implementation references latest Koog docs -- no API assumptions | All APIs verified against 0.6.2 source JARs extracted from Gradle cache; every interface and method signature confirmed |
| DEBT-03 | Integration tests cover AI structured output endpoint | Existing test infrastructure (Testcontainers, Ktor test-host, kotest assertions) supports this; pattern from `FileRoutesTest` applies |
</phase_requirements>

## Standard Stack

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Koog `koog-agents` | 0.6.2 | AI agent framework (includes embeddings, RAG, structured output) | Already in project; transitively includes all needed modules |
| Koog `embeddings-llm` | 0.6.2 (transitive) | `LLMEmbedder` class for generating embeddings | Wraps `LLMEmbeddingProvider` into `Embedder` interface |
| Koog `vector-storage` | 0.6.2 (transitive) | `VectorStorage<T>` interface, `EmbeddingBasedDocumentStorage` | Foundation for custom PgVectorStorage |
| Koog `prompt-structure` | 0.6.2 (transitive) | `executeStructured()`, `StructuredRequestConfig`, `StructureFixingParser` | Type-safe structured LLM output with auto JSON schema |
| pgvector | pg15 (Docker) | PostgreSQL vector extension for cosine similarity search | Already configured in Phase 17 |
| Exposed R2DBC | 1.0.0 | Database access with custom `VectorColumnType` | Already in project with vector column support |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Koog `rag-base` | 0.6.2 (transitive) | `RankedDocumentStorage`, `mostRelevantDocuments()`, `RankedDocument` | Document ranking and retrieval |
| Koog `embeddings-base` | 0.6.2 (transitive) | `Embedder` interface, `Vector` data class with `cosineSimilarity()` | Base types for embedding operations |
| Arrow Core | 2.2.1.1 | `Either<AppError, T>` for structured output responses | Wrap `Result` into project's error handling |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `text-embedding-004` | `gemini-embedding-001` | Newer model, but outputs 3072 dims by default and Koog 0.6.2 `GoogleEmbeddingRequest` does not support `outputDimensionality` parameter -- would require pgvector column change to 3072 |
| Custom PgVectorStorage | Koog `InMemoryVectorStorage` | Built-in but no persistence, no group scoping |
| pgvector cosine similarity | Koog `EmbeddingBasedDocumentStorage.rankDocuments()` | Koog's built-in ranking loads ALL documents into memory via `allDocumentsWithPayload()` -- does not scale; pgvector `<=>` operator does server-side ranking |

**No new Gradle dependencies needed.** All modules are transitive via `koog-agents`.

## Architecture Patterns

### Recommended Project Structure

```
server/ai/src/main/kotlin/com/m2f/server/ai/
├── agents/                       # Existing: AssistantAgent, ChatAgent
├── di/                           # Existing: AiModule (add RAG + structured output DI)
├── errors/                       # Existing: AiErrors
├── persistence/                  # Existing: ExposedPersistenceStorage
├── rag/
│   ├── PgVectorStorage.kt        # VectorStorage<String> backed by pgvector
│   ├── DocumentIngestionService.kt # Chunk + embed + store pipeline
│   ├── RagService.kt             # Query embedding + retrieval + context injection
│   └── RelevanceDetector.kt      # Structured output heuristic: "does query need docs?"
├── routes/
│   ├── AiRoutes.kt               # Existing (enhance chat with RAG)
│   └── DocumentRoutes.kt         # NEW: Document upload/list/delete for RAG
├── structured/
│   └── StructuredOutputService.kt # executeStructured() wrapper with Arrow Either
└── tools/                        # Existing: UserTools
```

```
server/core/database/src/main/kotlin/com/m2f/core/database/
├── tables/
│   ├── DocumentEmbeddingsTable.kt  # MODIFY: add userId, documentId, chunkIndex
│   └── DocumentsTable.kt          # NEW: source document metadata
├── vector/
│   └── VectorColumnType.kt        # Existing: pgvector column type
└── migrations/
    └── VectorMigrations.kt        # MODIFY: add migration for new columns + DocumentsTable
```

### Pattern 1: Custom PgVectorStorage implementing VectorStorage<String>

**What:** Implement Koog's `VectorStorage<String>` interface with pgvector's native cosine similarity instead of Koog's in-memory ranking.

**Why:** Koog's `EmbeddingBasedDocumentStorage.rankDocuments()` calls `allDocumentsWithPayload().collect()` which loads ALL vectors into memory and computes similarity in Kotlin. With pgvector, the database does cosine similarity search via the `<=>` operator -- O(1) index lookup vs O(n) memory scan.

**Critical detail:** We do NOT use `EmbeddingBasedDocumentStorage` for retrieval. We use it only for `store()` (which calls `DocumentEmbedder.embed()` then `VectorStorage.store()`). For retrieval, we bypass Koog's ranking and query pgvector directly.

**Example:**
```kotlin
// Source: Koog 0.6.2 VectorStorage interface + pgvector SQL
class PgVectorStorage(
    private val db: R2dbcDatabase,
    private val groupId: Uuid,    // scoping
    private val userId: Uuid?,    // null = admin sees all
) : VectorStorage<String> {

    override suspend fun store(document: String, data: Vector): String {
        val id = Uuid.random()
        suspendTransaction(db = db) {
            DocumentEmbeddingsTable.insert {
                it[DocumentEmbeddingsTable.id] = id
                it[DocumentEmbeddingsTable.groupId] = groupId
                it[content] = document
                it[embedding] = data.values.map { v -> v.toFloat() } // Vector uses Double, pgvector uses Float
                // userId, documentId, chunkIndex set separately
            }
        }
        return id.toString()
    }

    // Custom method -- NOT part of VectorStorage interface
    suspend fun searchSimilar(queryVector: Vector, topK: Int, minSimilarity: Double = 0.0): List<RankedDocument<String>> {
        val vectorStr = "[${queryVector.values.joinToString(",")}]"
        val sql = """
            SELECT id, content, 1 - (embedding <=> '$vectorStr'::vector) as similarity
            FROM document_embeddings
            WHERE group_id = '${groupId}'
            ${userScopeClause()}
            AND 1 - (embedding <=> '$vectorStr'::vector) >= $minSimilarity
            ORDER BY embedding <=> '$vectorStr'::vector
            LIMIT $topK
        """.trimIndent()
        // Execute via TransactionManager.current().exec()
    }

    // ... other VectorStorage methods (delete, read, getPayload, allDocuments, allDocumentsWithPayload)
}
```

### Pattern 2: Structured Output via executeStructured()

**What:** Use Koog's `executeStructured<T>()` extension on `PromptExecutor` to get typed Kotlin responses from Gemini.

**Why:** Google Gemini models in Koog 0.6.2 have `LLMCapability.Schema.JSON.Standard` capability, which means Koog automatically uses native JSON schema mode (not prompt-based manual mode). The `GoogleLLMClient` companion `init` block registers `GoogleStandardJsonSchemaGenerator` for the Google provider.

**Example:**
```kotlin
// Source: Koog 0.6.2 prompt-structure + prompt-executor-google-client source JARs
@Serializable
@SerialName("RelevanceCheck")
@LLMDescription("Determines if a user query needs document context to answer")
data class RelevanceCheck(
    @property:LLMDescription("Whether the query needs document context to answer well")
    val needsContext: Boolean,
    @property:LLMDescription("Optimized search query for vector similarity if context needed")
    val searchQuery: String?
)

// Usage in service:
val result = executor.executeStructured<RelevanceCheck>(
    prompt = prompt("relevance-check") {
        system("You determine if a user query would benefit from document context.")
        user(userQuery)
    },
    model = GoogleModels.Gemini2_5Flash,  // has Schema.JSON.Standard capability
)
// result: Result<StructuredResponse<RelevanceCheck>>
val check = result.getOrThrow().data
```

### Pattern 3: Embedding Pipeline (LLMEmbedder + GoogleLLMClient)

**What:** Create an `LLMEmbedder` using the existing `GoogleLLMClient` (which implements `LLMEmbeddingProvider`) with a custom `text-embedding-004` model constant.

**Why:** `GoogleModels.Embeddings.GeminiEmbedding001` exists in Koog 0.6.2 but outputs 3072 dims by default. The Koog `GoogleEmbeddingRequest` class does not support `outputDimensionality` parameter, so we cannot request 768 dims. Since `text-embedding-004` outputs 768 dims natively (matching our pgvector column), we create a custom `LLModel` for it.

**Example:**
```kotlin
// Source: Koog 0.6.2 GoogleModels.kt, LLMEmbedder.kt, GoogleLLMClient.kt
val TextEmbedding004 = LLModel(
    provider = LLMProvider.Google,
    id = "text-embedding-004",
    capabilities = listOf(LLMCapability.Embed),
    contextLength = 2048,
)

// In DI module:
val googleClient = GoogleLLMClient(apiKey) // implements LLMEmbeddingProvider
val embedder = LLMEmbedder(googleClient, TextEmbedding004)
// embedder.embed("text") returns Vector(values: List<Double>), 768 dimensions
```

### Pattern 4: RAG Context Injection into Chat

**What:** Enhance `ChatAgentService.streamChat()` to detect relevance and inject document chunks into the prompt before sending to the LLM.

**Why:** User decided RAG is built into existing chat, not a separate endpoint. Hidden context -- no citations shown.

**Example:**
```kotlin
// In ChatAgentService, before running the agent:
val relevance = relevanceDetector.check(input) // uses executeStructured<RelevanceCheck>
if (relevance.needsContext) {
    val chunks = ragService.retrieve(
        query = relevance.searchQuery ?: input,
        groupId = groupId,
        userId = userId,
        userRole = userRole,
        topK = 5
    )
    // Prepend to system prompt:
    val contextBlock = chunks.joinToString("\n---\n") { it }
    // Modify agent prompt to include context
}
```

### Anti-Patterns to Avoid
- **Using EmbeddingBasedDocumentStorage for retrieval:** It loads ALL vectors into memory. Use pgvector `<=>` operator for server-side similarity search.
- **Creating separate GoogleLLMClient instances:** Reuse the same client for chat, structured output, and embeddings. It implements both `LLMClient` and `LLMEmbeddingProvider`.
- **Storing Vector as List<Float> directly:** Koog `Vector` uses `List<Double>`, but pgvector stores as Float. Convert at the storage boundary: `vector.values.map { it.toFloat() }`.
- **Ignoring the StructureFixingParser:** Structured output can fail silently if the model returns malformed JSON. Always use a fixing parser for production code.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Embedding generation | Raw HTTP calls to Google embedding API | Koog `LLMEmbedder(GoogleLLMClient, model)` | Handles API auth, request formatting, error handling |
| Vector similarity search | In-memory cosine similarity in Kotlin | pgvector `<=>` operator in SQL | Database-level search scales; in-memory doesn't |
| JSON schema generation for structured output | Manual JSON schema construction | Koog `executeStructured()` auto-generates from `@Serializable` class | Handles provider-specific schema formats, Google registered |
| Structured output parsing | Manual JSON deserialization + validation | Koog `StructuredResponse<T>` with `StructureFixingParser` | Auto-retry with LLM-based fixing on parse failure |
| Document ranking | Sort by similarity score manually | `mostRelevantDocuments(query, count, similarityThreshold)` for non-pgvector use, or pgvector ORDER BY for production | Koog extension handles threshold + sorting + limiting |

**Key insight:** Koog provides the interfaces; pgvector provides the performance. Use Koog's type system but override the retrieval path with pgvector's native similarity operators.

## Common Pitfalls

### Pitfall 1: Vector Dimension Mismatch
**What goes wrong:** Embedding model outputs N dimensions but pgvector column expects M dimensions. INSERT fails with dimension mismatch error.
**Why it happens:** `gemini-embedding-001` outputs 3072 dims by default. The existing `DocumentEmbeddingsTable` has `vector("embedding", 768)` from Phase 17. Koog 0.6.2's `GoogleEmbeddingRequest` does not support `outputDimensionality` parameter.
**How to avoid:** Use `text-embedding-004` which outputs 768 dims natively. Create a custom `LLModel` constant for it.
**Warning signs:** `VectorColumnType` throws `"Vector dimension mismatch: expected 768, got 3072"`.

### Pitfall 2: Float vs Double Type Mismatch
**What goes wrong:** Koog `Vector(values: List<Double>)` but `VectorColumnType` stores as `List<Float>` via pgvector string format.
**Why it happens:** Different precision conventions between Koog and pgvector.
**How to avoid:** Convert at the PgVectorStorage boundary: `vector.values.map { it.toFloat() }` when storing, and `.map { it.toDouble() }` when reading back to create Koog `Vector`.
**Warning signs:** Compilation errors or subtle precision loss in similarity scores.

### Pitfall 3: EmbeddingBasedDocumentStorage Loads All Vectors
**What goes wrong:** `rankDocuments()` calls `storage.allDocumentsWithPayload().collect()` -- loads every vector into JVM memory.
**Why it happens:** The default Koog implementation is designed for small in-memory stores, not databases with thousands of documents.
**How to avoid:** Use `EmbeddingBasedDocumentStorage` only for `store()` (which calls `DocumentEmbedder.embed()` then `VectorStorage.store()`). For retrieval, query pgvector directly with `<=>` operator.
**Warning signs:** OOM errors or extreme latency as document count grows.

### Pitfall 4: R2DBC Cannot Use pgvector Operators in Exposed DSL
**What goes wrong:** Exposed R2DBC has no `<=>` operator support for vector columns.
**Why it happens:** pgvector operators are not part of standard SQL and Exposed doesn't have a pgvector plugin.
**How to avoid:** Use `TransactionManager.current().exec()` for raw SQL similarity queries, same pattern as Phase 17's `CREATE EXTENSION IF NOT EXISTS vector`.
**Warning signs:** Exposed DSL compilation errors when trying to use vector comparison operators.

### Pitfall 5: GoogleLLMClient Sharing
**What goes wrong:** Multiple `GoogleLLMClient` instances create redundant HTTP clients, connections, and SSL handshakes.
**Why it happens:** Each `GoogleLLMClient` creates its own `HttpClient` with `SSE`, `ContentNegotiation`, and `HttpTimeout` plugins.
**How to avoid:** Create a single `GoogleLLMClient` in Koin DI, inject it into `LLMEmbedder`, `AssistantAgentService`, `ChatAgentService`, and structured output services.
**Warning signs:** Resource exhaustion under load; connection pool starvation.

### Pitfall 6: Chunk Size vs Token Limit
**What goes wrong:** Text chunks exceed `text-embedding-004`'s 2048 token input limit, causing API errors.
**Why it happens:** Character-based chunking doesn't account for tokenization overhead (1 token ~ 4 chars for English).
**How to avoid:** Use ~500 character chunks with ~100 char overlap. This yields ~125 tokens per chunk, well under 2048.
**Warning signs:** Google API returns 400 errors on long chunks.

## Code Examples

### Verified: LLMEmbedder with GoogleLLMClient

```kotlin
// Source: Koog 0.6.2 JARs (GoogleLLMClient.kt, LLMEmbedder.kt, GoogleModels.kt)
import ai.koog.embeddings.local.LLMEmbedder
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

// Custom model constant (text-embedding-004 outputs 768 dims natively)
val TextEmbedding004 = LLModel(
    provider = LLMProvider.Google,
    id = "text-embedding-004",
    capabilities = listOf(LLMCapability.Embed),
    contextLength = 2048,
)

// GoogleLLMClient implements both LLMClient and LLMEmbeddingProvider
val client = GoogleLLMClient(apiKey = "...")
val embedder = LLMEmbedder(client, TextEmbedding004)

// Embed text
val vector = embedder.embed("How do I reset my password?")
// vector.values: List<Double>, vector.dimension: 768
```

### Verified: executeStructured() with Google Gemini

```kotlin
// Source: Koog 0.6.2 JARs (PromptExecutorExtensions.kt, GoogleModels.kt)
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.structure.StructureFixingParser
import ai.koog.prompt.structure.executeStructured
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SentimentAnalysis")
@LLMDescription("Sentiment analysis result for a piece of text")
data class SentimentAnalysis(
    @property:LLMDescription("Overall sentiment: positive, negative, or neutral")
    val sentiment: String,
    @property:LLMDescription("Confidence score between 0.0 and 1.0")
    val confidence: Double,
    @property:LLMDescription("Key phrases that influenced the sentiment determination")
    val keyPhrases: List<String>,
)

val executor = SingleLLMPromptExecutor(client)
val result = executor.executeStructured<SentimentAnalysis>(
    prompt = prompt("sentiment") {
        system("You are a sentiment analysis expert.")
        user("Analyze: 'This product exceeded my expectations!'")
    },
    model = GoogleModels.Gemini2_5Flash, // has Schema.JSON.Standard capability
    fixingParser = StructureFixingParser(
        model = GoogleModels.Gemini2_0FlashLite,
        retries = 2,
    ),
)
// result: Result<StructuredResponse<SentimentAnalysis>>
val analysis = result.getOrThrow().data
// analysis.sentiment = "positive", analysis.confidence = 0.95, etc.
```

### Verified: VectorStorage Interface Methods

```kotlin
// Source: Koog 0.6.2 JAR (DocumentStorageWithPayload.kt, VectorStorage.kt)
interface VectorStorage<Document> : DocumentStorageWithPayload<Document, Vector> {
    // Inherited methods:
    suspend fun store(document: Document, data: Vector): String
    suspend fun delete(documentId: String): Boolean
    suspend fun read(documentId: String): Document?
    suspend fun getPayload(documentId: String): Vector?
    suspend fun readWithPayload(documentId: String): DocumentWithPayload<Document, Vector>?
    fun allDocuments(): Flow<Document>
    fun allDocumentsWithPayload(): Flow<DocumentWithPayload<Document, Vector>>
}
```

### Verified: mostRelevantDocuments Extension

```kotlin
// Source: Koog 0.6.2 JAR (RankedDocumentStorage.kt)
suspend fun <Document> RankedDocumentStorage<Document>.mostRelevantDocuments(
    query: String,
    count: Int = Int.MAX_VALUE,
    similarityThreshold: Double = 0.0
): Iterable<Document>
```

### Verified: GoogleModels.Embeddings

```kotlin
// Source: Koog 0.6.2 JAR (GoogleModels.kt)
object GoogleModels {
    object Embeddings {
        val GeminiEmbedding001: LLModel = LLModel(
            provider = LLMProvider.Google,
            id = "gemini-embedding-001",
            capabilities = listOf(LLMCapability.Embed),
            contextLength = 2048,
        )
    }
    // NOTE: GeminiEmbedding001 outputs 3072 dims by default.
    // Koog 0.6.2 GoogleEmbeddingRequest does NOT support outputDimensionality.
    // Use custom text-embedding-004 model constant for 768 dims.
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `text-embedding-004` (768 dims) | `gemini-embedding-001` (3072 dims) | Google deprecated `text-embedding-004` Aug 2025 | Koog 0.6.2 lacks dimension config; use `text-embedding-004` for now (still functional) |
| Manual JSON schema for structured output | Koog `executeStructured()` with auto schema | Koog 0.4.0+ | Native structured output via `StructuredRequest.Native` for Google |
| Separate RAG libraries (LangChain4j) | Koog-native RAG modules | Koog 0.5.0+ | Unified framework, no parallel abstractions |

**Deprecated/outdated:**
- `text-embedding-004`: Deprecated by Google in favor of `gemini-embedding-001`, but still operational. Migration path: when Koog adds `outputDimensionality` to `GoogleEmbeddingRequest`, switch to `gemini-embedding-001` with 768 dims (or re-create pgvector column at 3072)

## Open Questions

1. **text-embedding-004 deprecation timeline**
   - What we know: Google deprecated it Aug 2025, recommending `gemini-embedding-001`. It still works as of Feb 2026.
   - What's unclear: When Google will actually remove the API endpoint. Could be months or years.
   - Recommendation: Use `text-embedding-004` now (matches 768-dim column). Add a TODO comment for migration when Koog adds dimension configuration to `GoogleEmbeddingRequest`. LOW risk since Google rarely hard-removes APIs quickly.

2. **PDF text extraction**
   - What we know: User wants Claude's discretion on supported file types. Text and Markdown are trivial. PDF requires a JVM library.
   - What's unclear: Whether Apache PDFBox or similar is acceptable to add as a dependency.
   - Recommendation: Start with `.txt` and `.md` only for MVP. PDF support can be added later with Apache PDFBox if needed. The chunking pipeline is file-type agnostic once text is extracted.

3. **DocumentEmbeddingsTable schema evolution**
   - What we know: Current table has `id`, `groupId`, `content`, `embedding(768)`, `metadata`, `createdAt`. Need `userId`, `documentId`, `chunkIndex`.
   - What's unclear: Whether to alter the existing table or create a new migration.
   - Recommendation: New migration in `VectorMigrations.kt` using `ALTER TABLE` via `TransactionManager.current().exec()`. Cannot use Exposed `SchemaUtils` for ALTER TABLE -- requires raw SQL, same pattern as `CREATE EXTENSION`.

## Sources

### Primary (HIGH confidence)
- Koog 0.6.2 source JARs extracted from Gradle cache -- `GoogleModels.kt`, `GoogleLLMClient.kt`, `LLMEmbedder.kt`, `Vector.kt`, `Embedder.kt`, `VectorStorage.kt`, `EmbeddingBasedDocumentStorage.kt`, `RankedDocumentStorage.kt`, `PromptExecutorExtensions.kt`, `GoogleEmbeddingRequest.kt`
- `koog-agents-jvm-0.6.2.pom` -- confirmed transitive dependencies include all RAG/embeddings/structured-output modules
- Project source files -- `server/ai/**/*.kt`, `server/core/database/**/*.kt`, `gradle/libs.versions.toml`

### Secondary (MEDIUM confidence)
- [Koog API docs](https://api.koog.ai/) -- module listing, interface documentation
- [Koog structured output docs](https://docs.koog.ai/structured-output/) -- `executeStructured` usage patterns
- [Koog ranked document storage docs](https://docs.koog.ai/ranked-document-storage/) -- RAG pipeline patterns
- [Koog embeddings docs](https://docs.koog.ai/embeddings/) -- LLMEmbedder usage with providers
- [Koog LLM clients docs](https://docs.koog.ai/prompts/llm-clients/) -- GoogleLLMClient capabilities table
- [Koog releases](https://github.com/JetBrains/koog/releases) -- 0.6.0 added `gemini-embedding-001` support
- [Google Gemini Embedding docs](https://ai.google.dev/gemini-api/docs/models/gemini-embedding-001) -- dimension flexibility (128-3072)

### Tertiary (LOW confidence)
- Web search results on chunking best practices -- 400-512 tokens, 10-20% overlap as starting point

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - verified against actual 0.6.2 source JARs from Gradle cache, not just docs
- Architecture: HIGH - patterns based on verified interfaces with exact method signatures
- Pitfalls: HIGH - dimension mismatch, Float/Double conversion, and memory loading issues confirmed in source code

**Research date:** 2026-02-23
**Valid until:** 2026-03-23 (Koog API is stable within minor versions)
