---
status: complete
phase: 08-type-safe-shared-routes
source: 08-01-SUMMARY.md, 08-02-SUMMARY.md, 08-03-SUMMARY.md
started: 2026-02-16T10:00:00Z
updated: 2026-02-16T10:05:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Project Compiles on All Targets
expected: Running a full project build succeeds on JVM, Android, iOS, and WasmJs targets with no compilation errors. The @Resource route classes in core:models are resolved by both server and client modules.
result: issue
reported: "on wasm target login doesn't work, in the console it appears a message that says: HttpClient: REQUEST http://localhost:8080/api/auth/login failed with exception: kotlin.IllegalArgumentException: Node.js net module is not available. Please verify that you are using Node.js"
severity: major

### 2. Auth Endpoints Work (Register/Login/Refresh/Logout)
expected: Starting the server and hitting auth endpoints (register, login, token refresh, logout) returns correct responses. The type-safe route migration did not break any auth functionality.
result: issue
reported: "Same WASM CIO engine issue - browser console shows HttpClient REQUEST http://localhost:8080/api/auth/login failed with exception: kotlin.IllegalArgumentException: Node.js net module is not available. All network calls blocked on WASM browser target."
severity: major

### 3. User Endpoints Work (Get/Update Profile, Admin Get By ID)
expected: Authenticated requests to user endpoints (GET /api/users/me, PUT /api/users/me, GET /api/users/{id}) return correct responses. Path parameters resolve correctly through the @Resource type system.
result: pass

### 4. AI Chat Endpoint Works
expected: POST to /api/ai/chat returns a response (or appropriate error if AI not configured). The WebSocket path constant Ai.Chat.WS_PATH resolves correctly.
result: pass

### 5. No Hardcoded URL Strings in Server Routes
expected: Searching AuthRoutes.kt, UserRoutes.kt, and AiRoutes.kt for string-based route(), get(), post(), put() calls finds zero occurrences. All handlers use type-safe get<R>/post<R>/put<R> syntax.
result: pass

### 6. No Hardcoded URL Strings in SDK Client
expected: Searching AuthApi.kt, UserApi.kt, AuthInterceptor.kt for hardcoded path strings like "/api/" finds zero occurrences. All calls use type-safe client.post(Resource()) syntax.
result: pass

### 7. Route Definitions Single Source of Truth
expected: ApiRoutes.kt in core:models contains @Resource-annotated classes for Auth (6 routes), Users (2 routes), and Ai (2 routes). Both server and client import from this single file.
result: pass

## Summary

total: 7
passed: 5
issues: 2
pending: 0
skipped: 0

## Gaps

- truth: "Project compiles and runs on all KMP targets including WasmJs"
  status: failed
  reason: "User reported: on wasm target login doesn't work, in the console it appears a message that says: HttpClient: REQUEST http://localhost:8080/api/auth/login failed with exception: kotlin.IllegalArgumentException: Node.js net module is not available. Please verify that you are using Node.js"
  severity: major
  test: 1
  artifacts: []
  missing: []
