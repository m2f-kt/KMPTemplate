---
phase: quick-24
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - gradle/libs.versions.toml
  - server/ai/build.gradle.kts
  - server/build.gradle.kts
  - server/core/config/build.gradle.kts
  - server/src/main/kotlin/com/m2f/template/Application.kt
  - server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt
  - server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt
  - core/models/src/commonMain/kotlin/com/m2f/template/models/dto/AiDtos.kt
autonomous: true
must_haves:
  truths:
    - "WebSocket connects at /api/ai/chat/ws and authenticates via Authorization header JWT"
    - "Client sends JSON ChatRequest frames and receives JSON ChatStreamFrame responses"
    - "Multiple messages can be sent on a single WebSocket connection"
    - "SSE /chat/stream route and SSE-specific getAuth helper are removed"
  artifacts:
    - path: "gradle/libs.versions.toml"
      provides: "ktor-server-websockets library declaration"
      contains: "ktor-server-websockets"
    - path: "server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt"
      provides: "WebSocket chat route replacing SSE"
      contains: "webSocket"
    - path: "server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt"
      provides: "WebSocket-compatible getAuth helper"
      contains: "WebSocketServerSession"
    - path: "core/models/src/commonMain/kotlin/com/m2f/template/models/dto/AiDtos.kt"
      provides: "ChatStreamFrame DTO"
      contains: "ChatStreamFrame"
  key_links:
    - from: "server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt"
      to: "server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt"
      via: "getAuth call in webSocket block"
      pattern: "getAuth"
    - from: "server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt"
      to: "core/models/src/commonMain/kotlin/com/m2f/template/models/dto/AiDtos.kt"
      via: "ChatRequest deserialization and ChatStreamFrame serialization"
      pattern: "ChatStreamFrame|ChatRequest"
---

<objective>
Switch the chat streaming endpoint from SSE (`GET /api/ai/chat/stream` with query params) to WebSocket (`/api/ai/chat/ws` with JSON frames and header-based auth).

Purpose: WebSocket enables bidirectional communication (multiple messages on one connection), moves JWT out of query params into headers, and allows proper JSON message framing with structured request/response types.

Output: Working WebSocket chat endpoint, SSE chat infrastructure removed, new ChatStreamFrame DTO for streaming responses.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt
@server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt
@server/src/main/kotlin/com/m2f/template/Application.kt
@server/ai/src/main/kotlin/com/m2f/server/ai/agents/ChatAgent.kt
@core/models/src/commonMain/kotlin/com/m2f/template/models/dto/AiDtos.kt
@gradle/libs.versions.toml
@server/ai/build.gradle.kts
@server/build.gradle.kts
@server/core/config/build.gradle.kts
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add WebSocket dependency and replace SSE in build files</name>
  <files>
    gradle/libs.versions.toml
    server/ai/build.gradle.kts
    server/build.gradle.kts
    server/core/config/build.gradle.kts
    server/src/main/kotlin/com/m2f/template/Application.kt
    core/models/src/commonMain/kotlin/com/m2f/template/models/dto/AiDtos.kt
  </files>
  <action>
    1. In `gradle/libs.versions.toml`:
       - Add new library: `ktor-server-websockets = { group = "io.ktor", name = "ktor-server-websockets", version.ref = "ktor" }` (add near line 102 next to ktor-server-sse)
       - Keep `ktor-server-sse` in the catalog (it stays available but unused by chat)

    2. In `server/ai/build.gradle.kts`:
       - Replace `implementation(libs.ktor.server.sse)` with `implementation(libs.ktor.server.websockets)`

    3. In `server/build.gradle.kts`:
       - Replace `implementation(libs.ktor.server.sse)` with `implementation(libs.ktor.server.websockets)`

    4. In `server/core/config/build.gradle.kts`:
       - Replace `implementation(libs.ktor.server.sse)` with `implementation(libs.ktor.server.websockets)`

    5. In `server/src/main/kotlin/com/m2f/template/Application.kt`:
       - Replace `import io.ktor.server.sse.SSE` with `import io.ktor.server.websocket.WebSockets`
       - Replace `install(SSE)` with `install(WebSockets)` (use default config -- no pingPeriod/timeout/maxFrameSize customization needed for now)

    6. In `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/AiDtos.kt`:
       - Add new DTO after ChatResponse:
         ```kotlin
         @Serializable
         data class ChatStreamFrame(
             val message: String,
             val conversationId: String,
             val completed: Boolean,
         )
         ```
       - This is the JSON frame sent from server to client during WebSocket streaming. `completed=false` for content chunks, `completed=true` for the final frame.
  </action>
  <verify>Run `cd /Users/marc/IdeaProjects/Template && ./gradlew :server:ai:dependencies --configuration implementationDependencyMetadata 2>&1 | grep websocket` to confirm the websocket dependency resolves. Then run `./gradlew :core:models:compileCommonMainKotlinMetadata` to verify the new DTO compiles.</verify>
  <done>WebSocket dependency declared in version catalog and wired into all 3 server build files. SSE dependency removed from all 3 build files. Application.kt installs WebSockets instead of SSE. ChatStreamFrame DTO exists in AiDtos.kt and compiles.</done>
</task>

<task type="auto">
  <name>Task 2: Replace SSE getAuth with WebSocket getAuth and rewrite chat route</name>
  <files>
    server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt
    server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt
  </files>
  <action>
    1. In `server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt`:
       - Replace the SSE-specific `getAuth` function (lines 41-66) with a WebSocket-compatible version:
         ```kotlin
         context(session: WebSocketServerSession, config: Configuration)
         suspend inline fun getAuth(
             crossinline block: suspend context(Raise<DomainError>) (userId: String) -> Unit,
         ) {
             val token = session.call.request.headers["Authorization"]?.removePrefix("Bearer ")
                 ?: session.call.request.queryParameters["token"]
             if (token == null) {
                 session.send(Frame.Text(Json.encodeToString(ErrorResponse(code = "UNAUTHORIZED", message = "Missing token"))))
                 session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing token"))
                 return
             }

             val userId = try {
                 val verifier = JWT.require(Algorithm.HMAC256(config.env.auth.secret))
                     .withAudience(config.env.auth.audience)
                     .withIssuer(config.env.auth.issuer)
                     .build()
                 verifier.verify(token).subject
                     ?: throw IllegalArgumentException("Missing subject")
             } catch (_: Exception) {
                 session.send(Frame.Text(Json.encodeToString(ErrorResponse(code = "UNAUTHORIZED", message = "Invalid token"))))
                 session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
                 return
             }

             either { block(this, userId) }.onLeft { error ->
                 session.send(Frame.Text(Json.encodeToString(ErrorResponse(code = "AI_ERROR", message = error.toAppError().message))))
                 session.close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Server error"))
             }
         }
         ```
       - Update imports: remove `io.ktor.server.sse.ServerSSESession` and `io.ktor.sse.ServerSentEvent`
       - Add imports: `io.ktor.websocket.CloseReason`, `io.ktor.websocket.Frame`, `io.ktor.websocket.close`, `io.ktor.websocket.send`, `io.ktor.server.websocket.WebSocketServerSession`, `kotlinx.serialization.json.Json`, `kotlinx.serialization.encodeToString`
       - NOTE: Auth reads JWT from `Authorization: Bearer <token>` header first (standard), with query param `token` as fallback for browser clients that cannot set headers on WebSocket upgrade.

    2. In `server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt`:
       - Replace the entire `sse("/chat/stream") { ... }` block (lines 78-106) with a WebSocket route:
         ```kotlin
         webSocket("/chat/ws") {
             getAuth { userId ->
                 ensureAiEnabled(config.env.ai.enabled)

                 for (frame in incoming) {
                     if (frame !is Frame.Text) continue
                     val request = Json.decodeFromString<ChatRequest>(frame.readText())

                     val conversationId = request.conversationId
                         ?: Uuid.random().toString()

                     try {
                         chatAgentService.streamChat(
                             userId = userId,
                             conversationId = conversationId,
                             input = request.message,
                         ).collect { chunk ->
                             send(Frame.Text(Json.encodeToString(
                                 ChatStreamFrame(
                                     message = chunk,
                                     conversationId = conversationId,
                                     completed = false,
                                 )
                             )))
                         }
                         send(Frame.Text(Json.encodeToString(
                             ChatStreamFrame(
                                 message = "",
                                 conversationId = conversationId,
                                 completed = true,
                             )
                         )))
                     } catch (e: Exception) {
                         send(Frame.Text(Json.encodeToString(
                             ErrorResponse(
                                 code = "AGENT_ERROR",
                                 message = "Error: ${e.message}",
                             )
                         )))
                     }
                 }
             }
         }
         ```
       - Update imports:
         - Remove: `import io.ktor.server.sse.sse`, `import io.ktor.sse.ServerSentEvent`
         - Add: `import io.ktor.server.websocket.webSocket`, `import io.ktor.websocket.Frame`, `import io.ktor.websocket.readText`, `import io.ktor.websocket.send`, `import com.m2f.template.models.dto.ChatStreamFrame`, `import com.m2f.template.models.dto.ErrorResponse`, `import kotlinx.serialization.json.Json`, `import kotlinx.serialization.encodeToString`
       - The `for (frame in incoming)` loop keeps the connection alive for multiple messages. Each ChatRequest gets its own streaming response sequence (chunks with completed=false, then final frame with completed=true).
       - The `this@sse` pattern is no longer needed since WebSocket session is the default receiver.
  </action>
  <verify>Run `cd /Users/marc/IdeaProjects/Template && ./gradlew :server:compileKotlin` to verify full server compilation. Check there are no remaining references to SSE in the chat route: `grep -r "sse\|ServerSentEvent\|ServerSSESession" server/ai/src/ server/core/config/src/` should return zero matches.</verify>
  <done>SSE getAuth replaced with WebSocket getAuth (reads JWT from Authorization header with query param fallback, sends JSON error frames on failure, closes connection). SSE chat/stream route replaced with WebSocket chat/ws route that loops on incoming frames, deserializes ChatRequest JSON, streams ChatStreamFrame JSON responses per message, and supports multiple messages per connection. No SSE imports remain in Error.kt or AiRoutes.kt. Full server compiles successfully.</done>
</task>

</tasks>

<verification>
1. `./gradlew :server:compileKotlin` compiles without errors
2. `grep -rn "sse\|ServerSentEvent\|ServerSSESession" server/ai/src/ server/core/config/src/` returns no matches (SSE fully removed from chat infrastructure)
3. `grep -n "webSocket\|WebSocket" server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt` confirms WebSocket route exists
4. `grep -n "ChatStreamFrame" core/models/src/commonMain/kotlin/com/m2f/template/models/dto/AiDtos.kt` confirms new DTO exists
5. `grep -n "WebSockets" server/src/main/kotlin/com/m2f/template/Application.kt` confirms plugin installed
</verification>

<success_criteria>
- WebSocket endpoint at `/api/ai/chat/ws` replaces SSE at `/chat/stream`
- JWT read from Authorization header (not query param) with query param fallback
- Messages sent as JSON ChatRequest, responses as JSON ChatStreamFrame with completed flag
- Connection stays open for multiple message exchanges
- SSE completely removed from chat route and getAuth helper
- Server compiles and all existing routes unaffected
</success_criteria>

<output>
After completion, create `.planning/quick/24-switch-chat-streaming-from-sse-to-websoc/24-SUMMARY.md`
</output>
