---
phase: 23-implement-sse-getauth
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - server/core/config/build.gradle.kts
  - server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt
  - server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt
autonomous: true
must_haves:
  truths:
    - "SSE endpoints authenticate via JWT query parameter using getAuth helper"
    - "Authentication failures send SSE error events, not HTTP error responses"
    - "Authenticated SSE handler receives userId string via Arrow Raise context"
    - "chat/stream route is cleaner with auth logic extracted to reusable function"
  artifacts:
    - path: "server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt"
      provides: "getAuth function for SSE JWT authentication"
      contains: "getAuth"
    - path: "server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt"
      provides: "Refactored chat/stream route using getAuth"
  key_links:
    - from: "server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt"
      to: "server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt"
      via: "import and call to getAuth"
      pattern: "getAuth"
---

<objective>
Implement the `getAuth` SSE authentication helper in Error.kt and refactor AiRoutes.kt chat/stream to use it.

Purpose: Extract inline JWT validation from SSE endpoints into a reusable `getAuth` function that mirrors `conduitAuth` but sends SSE error events instead of HTTP responses. This eliminates duplicated JWT verification boilerplate for any future SSE endpoint.

Output: Working `getAuth` function in Error.kt, refactored chat/stream route in AiRoutes.kt.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt
@server/core/config/src/main/kotlin/com/m2f/core/config/server/DomainError.kt
@server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt
@server/core/config/build.gradle.kts
</context>

<tasks>

<task type="auto">
  <name>Task 1: Implement getAuth in Error.kt with SSE dependency</name>
  <files>
    server/core/config/build.gradle.kts
    server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt
  </files>
  <action>
1. Add `implementation(libs.ktor.server.sse)` to `server/core/config/build.gradle.kts` dependencies block. This is needed because `ServerSSESession` is from `io.ktor.server.sse` and `core/config` does not currently have this dependency.

2. Implement the `getAuth` function in Error.kt. The function already has this stub signature:

```kotlin
context(session: ServerSSESession)
suspend inline fun getAuth(crossinline block: suspend context(Raise<DomainError>) (userId: String) -> Unit) {
}
```

The function needs three additional parameters for JWT verification: `jwtSecret: String`, `jwtAudience: String`, `jwtIssuer: String`. Update the signature to accept these.

Implementation logic:
- Read the `token` from `session.call.request.queryParameters["token"]`
- If token is null, send `ServerSentEvent(data = "Error: Missing token", event = "error")` and return
- Verify the token using `com.auth0.jwt.JWT.require(com.auth0.jwt.algorithms.Algorithm.HMAC256(jwtSecret)).withAudience(jwtAudience).withIssuer(jwtIssuer).build().verify(token)`
- Extract `.subject` from the verified token. If null, treat as invalid
- On any verification exception, send `ServerSentEvent(data = "Error: Invalid token", event = "error")` and return
- On success, call `either { block(this, userId) }.onLeft { error -> session.send(ServerSentEvent(data = "Error: ${error.toAppError().message}", event = "error")) }` -- this provides the `Raise<DomainError>` context to the block and handles domain errors by converting them to SSE error events via `toAppError().message`

Required imports to add:
- `import io.ktor.server.sse.ServerSSESession`
- `import io.ktor.sse.ServerSentEvent`

The `either`, `Raise`, `ensureNotNull` imports are already present. `com.auth0.jwt.JWT` and `com.auth0.jwt.algorithms.Algorithm` should be used with fully qualified names (matching the existing pattern in AiRoutes.kt) OR imported -- either is fine, but FQN keeps the import section cleaner since this is the only usage.

IMPORTANT: The function must use `return` after sending error events so the block is NOT executed on auth failure. Since this is an inline function with `crossinline`, use early returns from the function itself (not lambdas).
  </action>
  <verify>
Run `cd /Users/marc/IdeaProjects/Template && ./gradlew :server:core:config:compileKotlin` -- must compile without errors. Verify the function signature includes jwtSecret, jwtAudience, jwtIssuer parameters and the body handles missing token, invalid token, and successful auth cases.
  </verify>
  <done>
`getAuth` function in Error.kt compiles, accepts JWT config params plus a `Raise<DomainError>` block with userId, sends SSE error events for auth failures, and delegates domain errors from the block to SSE error events via `toAppError().message`.
  </done>
</task>

<task type="auto">
  <name>Task 2: Refactor AiRoutes.kt chat/stream to use getAuth</name>
  <files>
    server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt
  </files>
  <action>
Replace the inline JWT validation block in the `sse("/chat/stream")` handler (lines 80-131) with a call to `getAuth`.

The refactored handler should look like:

```kotlin
sse("/chat/stream") {
    getAuth(jwtSecret, jwtAudience, jwtIssuer) { userId ->
        ensureAiEnabled(aiEnabled)

        val message = call.request.queryParameters["message"]
        if (message == null) {
            session.send(ServerSentEvent(data = "Error: Missing message", event = "error"))
            return@getAuth
        }

        val conversationId = call.request.queryParameters["conversationId"]
            ?: Uuid.random().toString()

        session.send(ServerSentEvent(data = conversationId, event = "conversation"))

        try {
            chatAgentService.streamChat(
                userId = userId,
                conversationId = conversationId,
                input = message,
            ).collect { chunk ->
                session.send(ServerSentEvent(data = chunk, event = "message"))
            }
            session.send(ServerSentEvent(data = "[DONE]", event = "done"))
        } catch (e: Exception) {
            session.send(ServerSentEvent(data = "Error: ${e.message}", event = "error"))
        }
    }
}
```

Key changes:
- Remove the manual `val token = ...` and `val userId = try { ... }` blocks (lines 82-100)
- Remove the inline `if (!aiEnabled)` check (lines 102-105) -- replace with `ensureAiEnabled(aiEnabled)` inside the getAuth block, which will raise `ProviderUnavailable` and get sent as an SSE error event automatically
- Inside the block, `this` is the `Raise<DomainError>` context and `userId` is the parameter
- Note: inside the `getAuth` block, `session` refers to the `ServerSSESession` context receiver. For `send()` calls, use `session.send(...)` since we are now inside a lambda, not directly in the `sse` block. Alternatively, since `getAuth` has `context(session: ServerSSESession)`, the session is available implicitly -- check what compiles and use the appropriate approach.
- Add import for `getAuth`: `import com.m2f.core.config.server.getAuth`
- The `com.auth0.jwt.JWT` and `com.auth0.jwt.algorithms.Algorithm` imports can be removed from AiRoutes.kt since they are no longer used there.
- `ensureAiEnabled` already raises `ProviderUnavailable()` which is a `DomainError`, so it integrates cleanly with the `Raise<DomainError>` context.
  </action>
  <verify>
Run `cd /Users/marc/IdeaProjects/Template && ./gradlew :server:ai:compileKotlin` -- must compile without errors. Then run `cd /Users/marc/IdeaProjects/Template && ./gradlew build` to verify the full build passes. Verify AiRoutes.kt no longer contains `com.auth0.jwt` imports or inline JWT verification code.
  </verify>
  <done>
chat/stream SSE handler uses `getAuth(jwtSecret, jwtAudience, jwtIssuer) { userId -> ... }` instead of inline JWT validation. The `aiEnabled` check uses `ensureAiEnabled` inside the Raise context. No `com.auth0.jwt` references remain in AiRoutes.kt. Full project compiles.
  </done>
</task>

</tasks>

<verification>
1. `./gradlew build` passes with no compilation errors
2. Error.kt contains a fully implemented `getAuth` function (not an empty stub)
3. AiRoutes.kt `sse("/chat/stream")` block uses `getAuth` and contains no inline JWT code
4. No `com.auth0.jwt` imports in AiRoutes.kt
5. `getAuth` sends SSE error events for: missing token, invalid token, and domain errors raised by the block
</verification>

<success_criteria>
- getAuth function implemented in Error.kt with JWT verification and SSE error handling
- chat/stream route in AiRoutes.kt refactored to use getAuth, removing ~20 lines of inline auth code
- Full project compiles and builds successfully
- SSE authentication pattern is now reusable for any future SSE endpoint
</success_criteria>

<output>
After completion, create `.planning/quick/23-implement-sse-getauth-function-in-error-/23-SUMMARY.md`
</output>
