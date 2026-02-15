---
status: testing
phase: 06-ai-agent-infrastructure
source: [06-01-SUMMARY.md, 06-02-SUMMARY.md, 06-03-SUMMARY.md]
started: 2026-02-13T23:30:00Z
updated: 2026-02-14T01:00:00Z
---

## Current Test

number: 5
name: Chat agent creates new conversation
expected: |
  With AI enabled, POST to `/api/ai/chat` with a valid auth token and body `{"message": "Hello, remember my name is Marc"}`.
  Should return a JSON response with a `message` field (the agent's reply) and a `conversationId` field (a UUID string).
  Save this conversationId for the next test.
awaiting: user response

## Tests

### 1. Server starts without AI env vars
expected: Run the server without AI_ENABLED or GOOGLE_API_KEY set. Server starts normally, no crash. Existing endpoints still work.
result: pass

### 2. AI endpoints return ProviderUnavailable when disabled
expected: With the server running (AI_ENABLED not set or false), POST to `/api/ai/assistant` with a valid auth token and body `{"message": "hello"}`. Should return an error response with code `AI_PROVIDER_UNAVAILABLE` (not a 500 stack trace).
result: pass

### 3. AI endpoints require authentication
expected: POST to `/api/ai/assistant` with body `{"message": "hello"}` but WITHOUT an Authorization header. Should return 401 Unauthorized (not a 500 or AI-specific error).
result: pass

### 4. Assistant agent responds with tool use
expected: Set `AI_ENABLED=true` and `GOOGLE_API_KEY=<your key>`, restart the server. POST to `/api/ai/assistant` with a valid auth token and body `{"message": "Look up the user with email admin@test.com"}`. Should return a JSON response with `agentType: "assistant"` and a `message` field containing information retrieved via the getUserByEmail tool (or a "No user found" message if that email doesn't exist).
result: pass

### 5. Chat agent creates new conversation
expected: With AI enabled, POST to `/api/ai/chat` with a valid auth token and body `{"message": "Hello, remember my name is Marc"}`. Should return a JSON response with a `message` field (the agent's reply) and a `conversationId` field (a UUID string). Save this conversationId for the next test.
result: issue
reported: "Request never completes. chatAgentStrategy() enters infinite loop in giveFeedbackToCallTools node demanding tool calls, but ToolRegistry.EMPTY means no tools exist. LLM keeps responding with text, strategy keeps demanding tools."
severity: blocker

### 6. Chat agent continues existing conversation
expected: POST to `/api/ai/chat` with the same auth token and body `{"message": "What is my name?", "conversationId": "<id from test 5>"}`. The agent should respond with something referencing "Marc" -- proving it has context from the previous turn via persistence.
result: [pending]

## Summary

total: 6
passed: 4
issues: 1
pending: 1
skipped: 0

## Gaps

- truth: "Chat agent returns response for new conversation"
  status: failed
  reason: "User reported: Request never completes. chatAgentStrategy() enters infinite loop in giveFeedbackToCallTools node demanding tool calls, but ToolRegistry.EMPTY means no tools exist."
  severity: blocker
  test: 5
  root_cause: "chatAgentStrategy() has hard-coded giveFeedbackToCallTools node that forces tool calls. With ToolRegistry.EMPTY, this creates an infinite loop. Fix: switch to reActStrategy() which handles empty tool registries gracefully."
  artifacts:
    - path: "server/ai/src/main/kotlin/com/m2f/server/ai/agents/ChatAgent.kt"
      issue: "chatAgentStrategy() incompatible with ToolRegistry.EMPTY"
  missing:
    - "Replace chatAgentStrategy() with reActStrategy() in ChatAgentService"
  debug_session: ""
