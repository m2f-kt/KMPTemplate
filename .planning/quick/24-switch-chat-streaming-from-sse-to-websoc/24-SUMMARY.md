---
phase: quick-24
plan: 01
subsystem: api
tags: [websocket, ktor, streaming, chat, ai]

# Dependency graph
requires:
  - phase: 06.1
    provides: SSE chat streaming infrastructure with getAuth helper
provides:
  - WebSocket chat endpoint at /api/ai/chat/ws
  - WebSocket-compatible getAuth helper with Authorization header + query param fallback
  - ChatStreamFrame DTO for structured JSON streaming responses
  - Bidirectional WebSocket connection supporting multiple messages
affects: [ai-routes, error-helpers, client-sdk]

# Tech tracking
tech-stack:
  added: [ktor-server-websockets]
  patterns: [WebSocket JSON frame protocol with completed flag, Authorization header auth with query param fallback]

key-files:
  created: []
  modified:
    - gradle/libs.versions.toml
    - server/ai/build.gradle.kts
    - server/build.gradle.kts
    - server/core/config/build.gradle.kts
    - server/src/main/kotlin/com/m2f/template/Application.kt
    - server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt
    - server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/dto/AiDtos.kt

key-decisions:
  - "JWT read from Authorization header first, query param 'token' as fallback for browser clients"
  - "ChatStreamFrame with completed boolean flag for stream termination signaling"
  - "WebSocket errors sent as JSON ErrorResponse frames before close"

patterns-established:
  - "WebSocket chat protocol: client sends ChatRequest JSON, server responds with ChatStreamFrame JSON (completed=false for chunks, completed=true for final)"
  - "WebSocket auth: getAuth reads Authorization header with query param fallback, sends ErrorResponse JSON + close on failure"

# Metrics
duration: 4min
completed: 2026-02-15
---

# Quick Task 24: Switch Chat Streaming from SSE to WebSocket Summary

**WebSocket chat endpoint replacing SSE with bidirectional JSON framing, header-based JWT auth, and ChatStreamFrame DTO**

## Performance

- **Duration:** 4 min
- **Started:** 2026-02-15T00:57:48Z
- **Completed:** 2026-02-15T01:02:01Z
- **Tasks:** 2
- **Files modified:** 8

## Accomplishments
- Replaced SSE `/chat/stream` endpoint with WebSocket `/chat/ws` for bidirectional messaging
- Moved JWT authentication from query params to Authorization header (with query param fallback)
- Added ChatStreamFrame DTO with completed flag for structured streaming responses
- WebSocket connection stays open for multiple message exchanges per session

## Task Commits

Each task was committed atomically:

1. **Task 1: Add WebSocket dependency and replace SSE in build files** - `5d51019` (feat)
2. **Task 2: Replace SSE getAuth with WebSocket getAuth and rewrite chat route** - `a5e6896` (feat)

## Files Created/Modified
- `gradle/libs.versions.toml` - Added ktor-server-websockets library declaration
- `server/ai/build.gradle.kts` - Replaced ktor-server-sse with ktor-server-websockets
- `server/build.gradle.kts` - Replaced ktor-server-sse with ktor-server-websockets
- `server/core/config/build.gradle.kts` - Replaced ktor-server-sse with ktor-server-websockets
- `server/src/main/kotlin/com/m2f/template/Application.kt` - Install WebSockets plugin instead of SSE
- `server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt` - WebSocket-compatible getAuth with JSON error frames and close
- `server/ai/src/main/kotlin/com/m2f/server/ai/routes/AiRoutes.kt` - WebSocket chat/ws route with incoming frame loop
- `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/AiDtos.kt` - ChatStreamFrame DTO

## Decisions Made
- JWT read from Authorization header first, query param `token` as fallback for browser clients that cannot set headers on WebSocket upgrade
- ChatStreamFrame uses `completed: Boolean` field (false for content chunks, true for final frame) instead of SSE event types
- WebSocket errors sent as JSON ErrorResponse frames before closing the connection with appropriate close codes (VIOLATED_POLICY for auth, INTERNAL_ERROR for server errors)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Steps
- Client SDK needs updating to connect via WebSocket instead of SSE EventSource
- Consider adding pingPeriod/timeout configuration to WebSockets plugin if needed for production

## Self-Check: PASSED

All 8 modified files verified on disk. Both task commits (5d51019, a5e6896) verified in git log. Summary file exists.

---
*Quick Task: 24-switch-chat-streaming-from-sse-to-websoc*
*Completed: 2026-02-15*
