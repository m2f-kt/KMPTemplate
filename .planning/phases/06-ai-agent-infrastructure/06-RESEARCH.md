# Phase 6: AI Agent Infrastructure - Research

**Researched:** 2026-02-13
**Domain:** AI Agent Framework (Koog by JetBrains), Ktor Integration, Conversation Persistence
**Confidence:** MEDIUM -- Koog is a rapidly evolving (alpha) framework; core APIs are verified from official docs but some integration patterns (Arrow Raise wrapping, Exposed-based custom persistence) require implementation-time validation.

## Summary

Koog (v0.6.2, released 2026-02-10) is JetBrains' official Kotlin framework for building AI agents. It provides a comprehensive DSL for agent creation, a tool system (annotation-based and class-based), predefined strategies (ReAct, chat), and a first-class Ktor plugin (`koog-ktor`) that enables agents within Ktor routes via `install(Koog)`. The framework includes built-in persistence with InMemory, File, and PostgreSQL storage providers, plus a `MessageHistoryOnly` rollback strategy specifically designed for conversational agents that need to maintain context across sessions.

The key integration challenge for this project is wrapping Koog's exception-based error handling in Arrow Raise. Koog does not natively use Arrow -- its tools throw exceptions on failure, and agent runs can throw various exceptions. The project's convention of `context(raise: Raise<DomainError>)` with zero try/catch must be maintained by creating a thin adapter layer that catches Koog exceptions and raises typed domain errors.

The Ktor plugin provides route-level `aiAgent()` and `llm()` functions for running agents and direct LLM calls. Configuration is done via YAML or programmatic DSL, supporting OpenAI, Anthropic, Google, Ollama, and others. For conversation persistence, the `MessageHistoryOnly` rollback strategy combined with a custom `PersistenceStorageProvider` backed by Exposed/R2DBC aligns with the existing database stack.

**Primary recommendation:** Use `koog-ktor` (v0.6.2) as a Ktor plugin, annotation-based tools for simplicity, `reActStrategy` for the tool-using agent, `chatAgentStrategy` for the conversational agent, and implement a custom `PersistenceStorageProvider` using Exposed for database-backed conversation persistence. Wrap all Koog calls in Arrow `either { }` blocks that catch and convert exceptions to typed `DomainError` subtypes.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| koog-ktor | 0.6.2 | Ktor plugin for AI agents | Official JetBrains integration; provides `install(Koog)`, route-level `aiAgent()`, YAML config |
| koog-agents | 0.6.2 | Core agent framework | Provides AIAgent, strategies, ToolRegistry, persistence features |
| agents-mcp-jvm | 0.6.2 | MCP tool integration | Enables connecting to MCP servers for external tools (optional, for extensibility) |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Arrow (already in project) | 2.2.1.1 | Error handling wrapper | Wrapping Koog calls in Raise<DomainError> |
| Exposed (already in project) | 1.0.0 | Custom persistence storage | Implementing PersistenceStorageProvider with R2DBC |
| Koin (already in project) | 4.1.1 | DI for agent services | Registering agent services and tool dependencies |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| koog-ktor plugin | Manual AIAgent instantiation | Plugin gives YAML config, route helpers, provider management; manual gives more control but more boilerplate |
| Annotation-based tools | Class-based tools (Tool<Args, Result>) | Annotation-based is simpler, JVM-only (fine for server); class-based is cross-platform, more verbose |
| Custom Exposed persistence | Built-in PostgresPersistenceStorageProvider | Built-in uses JDBC/HikariCP; project uses R2DBC Exposed -- custom impl aligns with existing stack |
| Custom Exposed persistence | FilePersistenceStorageProvider | File provider is simpler but loses database benefits (querying, ACID, multi-instance support) |

**Dependencies to add to `libs.versions.toml`:**
```toml
[versions]
koog = "0.6.2"

[libraries]
koog-ktor = { module = "ai.koog:koog-ktor", version.ref = "koog" }
koog-agents = { module = "ai.koog:koog-agents", version.ref = "koog" }
koog-mcp = { module = "ai.koog:agents-mcp-jvm", version.ref = "koog" }

[bundles]
koog = ["koog-ktor", "koog-agents"]
```

**server/ai/build.gradle.kts:**
```kotlin
plugins {
    id("server-module-convention")
}

group = "com.m2f.server"

dependencies {
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(projects.server.auth)
    implementation(libs.bundles.koog)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.di)
    implementation(libs.bundles.ktor.core)
    testImplementation(libs.bundles.testing.server)
}
```

## Architecture Patterns

### Recommended Project Structure
```
server/ai/src/main/kotlin/com/m2f/server/ai/
├── errors/              # AI-specific DomainError subtypes
│   └── AiErrors.kt      # AgentError, AgentNotFound, ConversationNotFound, etc.
├── tools/               # Agent tools (annotation-based ToolSets)
│   └── UserTools.kt     # Tools that query user data from database
├── agents/              # Agent definitions and configurations
│   ├── AssistantAgent.kt # ReAct agent with tools
│   └── ChatAgent.kt     # Chat agent with conversation mgmt
├── persistence/         # Custom storage provider
│   ├── ConversationsTable.kt  # Exposed table for checkpoints
│   └── ExposedPersistenceStorage.kt  # PersistenceStorageProvider impl
├── routes/              # Ktor route definitions
│   └── AiRoutes.kt      # POST /api/ai/chat, /api/ai/agent
├── di/                  # Koin module
│   └── AiModule.kt      # Wire all AI dependencies
└── Ai.kt               # Migration registration
```

### Pattern 1: Koog Ktor Plugin Installation
**What:** Install Koog as a Ktor plugin with provider configuration
**When to use:** Application startup in the main server module
**Example:**
```kotlin
// Source: https://docs.koog.ai/ktor-plugin/
// In Application.kt module()
install(Koog) {
    llm {
        openAI(apiKey = config.env.ai.openaiApiKey) {
            timeouts {
                requestTimeout = 2.minutes
                connectTimeout = 30.seconds
                socketTimeout = 2.minutes
            }
        }
        // Ollama for local development
        ollama { baseUrl = "http://localhost:11434" }

        fallback {
            provider = LLMProvider.OpenAI
            model = OpenAIModels.Chat.GPT4_1
        }
    }
}
```

### Pattern 2: Annotation-Based Tools with Database Access
**What:** Tools that query the database, injected via Koin
**When to use:** When agents need access to application data
**Example:**
```kotlin
// Source: https://docs.koog.ai/annotation-based-tools/
@LLMDescription("Tools for querying user information")
class UserTools(
    private val userRepository: UserRepository,
) : ToolSet {
    @Tool
    @LLMDescription("Look up a user's profile by their email address")
    suspend fun getUserByEmail(
        @LLMDescription("The email address of the user to look up")
        email: String
    ): String {
        val user = userRepository.findByEmail(email)
            ?: return "No user found with email: $email"
        return "User: ${user.name} (${user.email}), role: ${user.role}"
    }
}
```

### Pattern 3: Agent Route with Arrow Raise Wrapping
**What:** Expose agent via Ktor route, wrapping Koog in conduit/Raise
**When to use:** All agent HTTP endpoints
**Example:**
```kotlin
// Wrapping Koog in the project's conduit pattern
fun Route.aiRoutes(/* dependencies */) {
    route("/api/ai") {
        authenticate {
            post("/chat") {
                conduitAuth { userId ->
                    val request = getModel<ChatRequest>()
                    // Arrow either{} to catch Koog exceptions
                    val result = either<DomainError, ChatResponse> {
                        catch({
                            aiAgent(
                                strategy = chatAgentStrategy(),
                                model = OpenAIModels.Chat.GPT4_1,
                                input = request.message
                            )
                        }) { e ->
                            raise(AgentExecutionFailed(e.message ?: "Agent execution failed"))
                        }
                    }.bind()
                    ChatResponse(conversationId = request.conversationId, message = result)
                }
            }
        }
    }
}
```

### Pattern 4: Conversation Persistence with MessageHistoryOnly
**What:** Configure persistence so conversations resume across HTTP requests
**When to use:** For the chat/conversation agent
**Example:**
```kotlin
// Source: https://docs.koog.ai/agent-persistence/
val agent = AIAgent(
    promptExecutor = executor,
    llmModel = model,
    systemPrompt = "You are a helpful assistant.",
    strategy = chatAgentStrategy(),
    toolRegistry = toolRegistry
) {
    install(Persistence) {
        storage = ExposedPersistenceStorage(database) // Custom provider
        enableAutomaticPersistence = true
        rollbackStrategy = RollbackStrategy.MessageHistoryOnly
    }
}
```

### Pattern 5: ReAct Agent with Tools
**What:** Agent that reasons step-by-step and calls tools
**When to use:** For the tool-using example agent
**Example:**
```kotlin
// Source: https://docs.koog.ai/predefined-agent-strategies/
val assistantAgent = AIAgent(
    promptExecutor = executor,
    llmModel = OpenAIModels.Chat.GPT4_1,
    systemPrompt = """You are a helpful assistant that can look up user data.
        |Use available tools to answer questions about users.""".trimMargin(),
    strategy = reActStrategy(reasoningInterval = 1),
    toolRegistry = ToolRegistry {
        tools(UserTools(userRepository).asTools())
    },
    maxIterations = 10
)
```

### Anti-Patterns to Avoid
- **Leaking Koog exceptions through routes:** All Koog calls must be wrapped in `either { catch { ... } { raise(...) } }` -- never let agent exceptions propagate to Ktor's default exception handler
- **Blocking the event loop with agent runs:** Agent runs are suspend functions but may be long-running; consider timeouts via `withTimeout()`
- **Hardcoding API keys:** Always read from Configuration/environment variables, never in source code
- **Using InMemoryPersistenceStorageProvider in production:** State is lost on restart; use database-backed storage
- **Creating agents per-request:** Use `AIAgentService` to manage agent lifecycle; create once, run many

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| LLM communication | Custom HTTP client for OpenAI/Anthropic | Koog prompt executors (`simpleOpenAIExecutor`, etc.) | Handles retries, rate limiting, streaming, content moderation |
| Tool calling loop | Manual parse-execute-respond loop | Koog strategies (`reActStrategy`, `chatAgentStrategy`) | Handles tool call parsing, parallel execution, result routing |
| History management | Custom message history truncation | Koog history compression (`HistoryCompressionStrategy`) | Multiple strategies (WholeHistory, FromLastNMessages, Chunked) built-in |
| MCP integration | Custom MCP client | `McpToolRegistryProvider` from agents-mcp | Handles transport (SSE/stdio), tool discovery, result marshaling |
| Agent-to-agent delegation | Custom inter-agent communication | `AIAgentService.createAgentTool()` | Converts agents to tools with proper input/output serialization |

**Key insight:** Koog handles the entire agent execution loop (prompt -> LLM -> parse tool calls -> execute tools -> send results -> repeat). Building this manually is complex and fragile. The framework also handles edge cases like malformed tool calls, LLM hallucinated tool names, and token limit management.

## Common Pitfalls

### Pitfall 1: Koog's Exception-Based Error Model vs. Arrow Raise
**What goes wrong:** Koog throws exceptions (from LLM clients, tool execution, agent lifecycle). The project convention is zero try/catch with Arrow Raise. If not carefully wrapped, Koog exceptions will leak as 500 Internal Server Errors.
**Why it happens:** Koog was not designed with Arrow Raise in mind; it uses standard Kotlin exceptions.
**How to avoid:** Create a `runAgent` utility that wraps all Koog interactions in `either { catch({ ... }) { raise(AgentError(...)) } }`. Define AI-specific DomainError subtypes (AgentExecutionFailed, ProviderUnavailable, ConversationNotFound).
**Warning signs:** Stack traces in API responses instead of typed error JSON.

### Pitfall 2: Persistence Provider Impedance Mismatch
**What goes wrong:** The built-in PostgresPersistenceStorageProvider uses JDBC + HikariCP. The project uses R2DBC Exposed. Using the built-in provider would add a second database connection pool and a blocking I/O pathway.
**Why it happens:** Koog's Postgres provider predates their R2DBC support consideration.
**How to avoid:** Implement a custom `PersistenceStorageProvider` that uses the project's existing `R2dbcDatabase` instance. The interface is simple (save/load/list/delete checkpoint methods). Store checkpoint JSON in a JSONB column in an Exposed table.
**Warning signs:** Two connection pools to the same database, blocking calls in a coroutine context.

### Pitfall 3: Agent API Key Configuration
**What goes wrong:** Koog requires LLM provider API keys. If the Koog plugin is installed without valid keys, the server fails to start or crashes on first agent request.
**Why it happens:** API keys are required but not validated at install time for all providers.
**How to avoid:** Make the Koog plugin installation conditional on configuration. Add an `ai.enabled` flag to the app configuration. Provide clear error messages when keys are missing. Default to Ollama for local development.
**Warning signs:** Server crashes on startup with "API key is required" or similar.

### Pitfall 4: Conversation ID Management for HTTP APIs
**What goes wrong:** Koog's persistence uses `agentId` and `checkpointId` internally, but the HTTP API needs a user-facing `conversationId`. Without proper mapping, conversations can't be resumed or get mixed between users.
**Why it happens:** Koog was designed for in-process agents, not multi-tenant HTTP APIs. There's no built-in concept of "conversation ownership" tied to user authentication.
**How to avoid:** Generate a UUID `conversationId` when starting a new conversation. Map it to the Koog `agentId`/`checkpointId`. Store the mapping with the authenticated `userId` in the conversations table. Validate that the requesting user owns the conversation before resuming.
**Warning signs:** Users can access each other's conversations, or "conversation not found" errors when trying to resume.

### Pitfall 5: Tool Execution Timeouts
**What goes wrong:** Tools that query the database or external services can hang, causing the entire agent run to stall.
**Why it happens:** No default timeout on tool execution in Koog.
**How to avoid:** Wrap tool execution in `withTimeout()`. Set `maxIterations` on agents to prevent infinite tool-calling loops. Use Koog's `maxAgentIterations` configuration.
**Warning signs:** Agent requests that never complete, growing memory usage.

### Pitfall 6: Alpha API Instability
**What goes wrong:** Koog is in alpha (0.6.x). APIs may change between minor versions. Migration could be required.
**Why it happens:** Active development, API surface not yet stabilized.
**How to avoid:** Pin to exact version 0.6.2. Isolate Koog usage behind project-owned interfaces (agent service, tool interfaces). Don't spread Koog imports across the codebase.
**Warning signs:** Compilation failures after version bumps, deprecated warnings.

## Code Examples

Verified patterns from official sources:

### Ktor Route with aiAgent() Helper
```kotlin
// Source: https://docs.koog.ai/ktor-plugin/
post("/chat") {
    val userInput = call.receiveText()
    val output = aiAgent(
        strategy = reActStrategy(),
        model = OpenAIModels.Chat.GPT4_1,
        input = userInput
    )
    call.respond(HttpStatusCode.OK, output)
}
```

### Annotation-Based Tool with ToolSet
```kotlin
// Source: https://docs.koog.ai/annotation-based-tools/
@LLMDescription("Tools for controlling a switch")
class SwitchTools(val switch: Switch) : ToolSet {
    @Tool
    @LLMDescription("Switches the state of the switch")
    fun switch(
        @LLMDescription("State to set (true/false)")
        state: Boolean
    ): String {
        switch.switch(state)
        return "Switched to ${if (state) "on" else "off"}"
    }
}
```

### Class-Based Tool (Calculator)
```kotlin
// Source: https://docs.koog.ai/class-based-tools/
object CalculatorTool : Tool<CalculatorTool.Args, Int>(
    argsSerializer = Args.serializer(),
    resultSerializer = Int.serializer(),
    name = "calculator",
    description = "A simple calculator that can add two digits (0-9)."
) {
    @Serializable
    data class Args(
        @property:LLMDescription("The first digit to add (0-9)")
        val digit1: Int,
        @property:LLMDescription("The second digit to add (0-9)")
        val digit2: Int
    )

    override suspend fun execute(args: Args): Int = args.digit1 + args.digit2
}
```

### Tool Registry Composition
```kotlin
// Source: https://docs.koog.ai/tools-overview/
val toolRegistry = ToolRegistry {
    tool(SayToUser)
    tools(UserTools(userRepository).asTools())
    tools(TransactionTools(transactionRepository).asTools())
}
```

### Persistence with Custom Storage
```kotlin
// Source: https://docs.koog.ai/agent-persistence/
val agent = AIAgent(promptExecutor = executor, llmModel = model) {
    install(Persistence) {
        storage = customStorageProvider  // Implement PersistenceStorageProvider
        enableAutomaticPersistence = true
        rollbackStrategy = RollbackStrategy.MessageHistoryOnly
    }
}
```

### Custom PersistenceStorageProvider Interface
```kotlin
// Source: https://docs.koog.ai/agent-persistence/
// Interface to implement for database-backed persistence
interface PersistenceStorageProvider {
    suspend fun saveCheckpoint(agentId: String, checkpoint: AgentCheckpointData): String
    suspend fun loadCheckpoint(agentId: String, checkpointId: String): AgentCheckpointData?
    suspend fun getLatestCheckpoint(agentId: String): AgentCheckpointData?
    suspend fun listCheckpoints(agentId: String): List<CheckpointMetadata>
    suspend fun deleteCheckpoint(agentId: String, checkpointId: String)
}
```

### MCP Tool Integration via Ktor Plugin
```kotlin
// Source: https://docs.koog.ai/ktor-plugin/
install(Koog) {
    agentConfig {
        mcp {
            sse("https://your-mcp-server.com/sse")
        }
    }
}
```

### Agent with History Compression
```kotlin
// Source: https://docs.koog.ai/history-compression/
// For long-running conversations
llm.writeSession {
    replaceHistoryWithTLDR(
        strategy = HistoryCompressionStrategy.WholeHistory,
        preserveMemory = true
    )
}
```

### Arrow Raise Wrapper Pattern (Project-Specific)
```kotlin
// Project-specific pattern: wrapping Koog in Arrow Raise
context(raise: Raise<DomainError>)
suspend fun runAgentSafely(
    agent: AIAgent<String, String>,
    input: String
): String = catch({
    agent.run(input)
}) { e ->
    raise(AgentExecutionFailed(
        detail = "Agent failed: ${e.message}",
        cause = e
    ))
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `simpleChatAgent` | `chatAgentStrategy()` | Koog 0.2.0 | Old function removed; use strategy-based approach |
| `Persistency` feature name | `Persistence` feature name | Koog 0.5.0 | Renamed throughout API; old name no longer exists |
| `execute` extensions on `PromptExecutor` | `aiAgent()` route helper via Ktor plugin | Koog 0.4.0 | Ktor plugin provides integrated approach |
| Manual LLM client setup | `install(Koog) { llm { ... } }` | Koog 0.4.0 | Centralized configuration, no manual client wiring |
| Annotation tools as standalone | `ToolSet` interface with `.asTools()` | Koog 0.4.x | Tools must implement ToolSet, registered via `.asTools()` |
| JDBC-only Postgres persistence | JDBC Postgres persistence (R2DBC not available) | Koog 0.4.2 | Must implement custom provider for R2DBC projects |

**Deprecated/outdated:**
- `simpleChatAgent()`: Removed in 0.2.0, replaced by `chatAgentStrategy()`
- `Persistency`: Renamed to `Persistence` in 0.5.0
- `execute` extensions on `PromptExecutor`: Dropped in 0.4.0

## Open Questions

1. **Custom PersistenceStorageProvider with R2DBC Exposed**
   - What we know: The interface requires `suspend` functions (saveCheckpoint, loadCheckpoint, etc.). The built-in Postgres provider uses JDBC Exposed, not R2DBC. The interface itself is async-compatible.
   - What's unclear: Whether the `AgentCheckpointData` serialization format is stable between versions. Whether R2DBC's JSONB column handling works smoothly for checkpoint storage.
   - Recommendation: Implement with InMemory first, validate the API contract, then swap to Exposed R2DBC. Store checkpoint data as serialized JSON text in a TEXT column if JSONB causes issues.

2. **Koog Ktor Plugin + Existing Koin Integration**
   - What we know: Both Koog and the project use Ktor plugins. The project uses Koin for DI. Koog's Ktor plugin manages its own LLM clients.
   - What's unclear: Whether Koog's internal DI conflicts with Koin. Whether the `aiAgent()` route helper works inside `conduit { }` blocks. Whether tools can access Koin-injected dependencies.
   - Recommendation: Install Koog plugin alongside Koin. Create tools that accept Koin-injected dependencies via constructor (not service locator). Test that `aiAgent()` works within existing route patterns before building all routes.

3. **Conversation Ownership and Multi-Tenancy**
   - What we know: Koog persistence uses `agentId` as the key. The project needs user-scoped conversations. Multiple users should not share agent state.
   - What's unclear: Whether `agentId` can encode user+conversation (e.g., `userId:conversationId`) or if a separate mapping table is needed.
   - Recommendation: Use composite `agentId` format: `"user:{userId}:conv:{conversationId}"`. This keeps ownership implicit in the storage key. Validate user ownership in the route handler before loading persistence.

4. **Koog Alpha Stability for Template Project**
   - What we know: Koog is in alpha. API changes happen between minor versions. Version 0.6.2 is current.
   - What's unclear: How stable the core agent API, tool registration, and persistence feature are in 0.6.x.
   - Recommendation: Pin to 0.6.2 exactly. Isolate all Koog usage in the `server:ai` module. Create project-owned interfaces that abstract Koog details so version upgrades only affect the AI module.

5. **Streaming Responses**
   - What we know: Koog supports streaming via `executeStreaming()` which returns a Flow. The Ktor plugin example collects the flow into a StringBuilder.
   - What's unclear: Whether SSE/WebSocket streaming to the client is needed for this phase.
   - Recommendation: Start with non-streaming (simpler). Streaming can be added as a future enhancement. The architecture should not preclude it.

## Sources

### Primary (HIGH confidence)
- [Koog Official Docs](https://docs.koog.ai/) -- Agent creation, tools, strategies, persistence, Ktor plugin, MCP
- [Koog GitHub](https://github.com/JetBrains/koog) -- README, CHANGELOG, release notes
- [Koog Ktor Plugin Docs](https://docs.koog.ai/ktor-plugin/) -- Installation, configuration, route helpers
- [Koog Agent Persistence Docs](https://docs.koog.ai/agent-persistence/) -- Storage providers, rollback strategies, checkpoint API
- [Koog Class-Based Tools](https://docs.koog.ai/class-based-tools/) -- Tool<Args, Result>, SimpleTool<Args>
- [Koog Annotation-Based Tools](https://docs.koog.ai/annotation-based-tools/) -- @Tool, @LLMDescription, ToolSet
- [Koog Predefined Strategies](https://docs.koog.ai/predefined-agent-strategies/) -- reActStrategy, chatAgentStrategy
- [Koog History Compression](https://docs.koog.ai/history-compression/) -- WholeHistory, FromLastNMessages, Chunked
- [Koog MCP Integration](https://docs.koog.ai/model-context-protocol/) -- McpToolRegistryProvider, SSE/stdio transports

### Secondary (MEDIUM confidence)
- [DeepWiki: Koog Persistence and Snapshots](https://deepwiki.com/JetBrains/koog/6.6-persistence-and-snapshots) -- PostgreSQL schema details, checkpoint format
- [Maven Central: ai.koog](https://central.sonatype.com/namespace/ai.koog) -- Available artifacts and versions
- [JetBrains AI Blog: Koog 0.5.0](https://blog.jetbrains.com/ai/2025/10/koog-0-5-0-is-out-smarter-tools-persistent-agents-and-simplified-strategy-design/) -- AIAgentService, persistence improvements

### Tertiary (LOW confidence)
- [Koog Postgres persistence provider schema details](https://deepwiki.com/JetBrains/koog/6.6-persistence-and-snapshots) -- Schema details may differ from actual implementation; verify at implementation time
- Arrow Raise + Koog integration -- No existing examples found; the wrapping pattern is a project-specific design that needs validation

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH -- Koog artifacts, versions, and Ktor plugin are well-documented on official docs and Maven Central
- Architecture: MEDIUM -- Ktor plugin integration pattern is documented; custom persistence provider and Arrow Raise wrapping are project-specific patterns that need implementation validation
- Pitfalls: MEDIUM -- Alpha framework instability and R2DBC impedance mismatch are identified but resolution depends on implementation details
- Tool system: HIGH -- Both annotation-based and class-based tool patterns are thoroughly documented with examples
- Conversation persistence: MEDIUM -- The `MessageHistoryOnly` strategy and custom `PersistenceStorageProvider` are documented, but database-backed implementation with R2DBC Exposed is novel

**Research date:** 2026-02-13
**Valid until:** 2026-02-27 (14 days -- Koog is in active alpha development with ~monthly releases)
