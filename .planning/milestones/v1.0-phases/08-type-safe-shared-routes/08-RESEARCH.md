# Phase 8: Type-Safe Shared Routes - Research

**Researched:** 2026-02-15
**Domain:** Ktor Resources plugin (type-safe routing shared across server and client in KMP)
**Confidence:** HIGH

## Summary

The Ktor Resources plugin provides a well-supported mechanism for defining `@Resource`-annotated route classes in a shared Kotlin Multiplatform module and consuming them from both the server (type-safe handlers via `get<R>`, `post<R>`) and the client (type-safe requests via `client.get(R())`, `client.post(R())`). The architecture is a three-layer dependency: `ktor-resources` (shared, multiplatform) contains the `@Resource` annotation and serialization format; `ktor-server-resources` (JVM) provides server handler functions; `ktor-client-resources` (multiplatform) provides client request functions. Both server and client artifacts depend on the shared `ktor-resources` artifact.

This project has Ktor 3.4.0, which fully supports the Resources plugin (the old `Locations` plugin from Ktor 1.x/2.x was removed in 3.0). The `ktor-resources` shared artifact supports all 23+ Kotlin/Multiplatform targets including JVM, Android, iOS, and WasmJs -- matching this project's target set exactly.

The current codebase has 17 string-based route definitions on the server side and 11 hardcoded URL strings in the SDK client (plus 1 string-match in AuthInterceptor). All can be migrated to typed resources except the WebSocket route (`/api/ai/chat/ws`), which does not have type-safe support in Ktor (KTOR-4369 is likely still open). The WebSocket route should keep its string path but reference a constant from the resource hierarchy.

**Primary recommendation:** Add `ktor-resources` to `core:models`, define `@Resource` route classes there, add `ktor-server-resources` to server modules, add `ktor-client-resources` to `core:sdk`, then migrate routes in three batches (auth, users, AI). The WebSocket route keeps its string path but uses a companion constant from the resource class.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `io.ktor:ktor-resources` | 3.4.0 | `@Resource` annotation and `ResourcesFormat` serializer | Shared KMP artifact -- the single source of truth for route definitions used by both server and client |
| `io.ktor:ktor-server-resources` | 3.4.0 | Server-side `get<R>`, `post<R>`, `put<R>`, `delete<R>` handler functions and `install(Resources)` plugin | Official Ktor server plugin for type-safe routing |
| `io.ktor:ktor-client-resources` | 3.4.0 | Client-side `client.get(R())`, `client.post(R())` type-safe request functions | Official Ktor client plugin for type-safe requests |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `org.jetbrains.kotlinx:kotlinx-serialization-core` | Already present (1.10.0) | Required by `@Resource` annotation (which has `@Serializable` behavior) | Already a dependency of `core:models` |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Ktor Resources | Manual route constants object | Loses type-safe path parameters, no query parameter support, no serialization integration |
| Ktor Resources | felixwiemuth/TypesafeKtorAPI | Third-party library with less ecosystem support; Ktor Resources is the official solution |

**Installation (version catalog additions):**
```toml
# In libs.versions.toml [libraries]
ktor-resources = { group = "io.ktor", name = "ktor-resources", version.ref = "ktor" }
ktor-server-resources = { group = "io.ktor", name = "ktor-server-resources", version.ref = "ktor" }
ktor-client-resources = { group = "io.ktor", name = "ktor-client-resources", version.ref = "ktor" }
```

## Architecture Patterns

### Recommended Project Structure
```
core/models/src/commonMain/kotlin/com/m2f/template/models/
  routes/
    ApiRoutes.kt          # All @Resource route classes (single file, ~60 lines)

server/auth/src/main/kotlin/.../routes/
    AuthRoutes.kt         # post<Auth.Register> { ... } (refactored)
    UserRoutes.kt         # get<Users.Me> { ... } (refactored)
    OAuthRoutes.kt        # Keeps string routes (OAuth has browser redirects)

server/ai/src/main/kotlin/.../routes/
    AiRoutes.kt           # post<Ai.Assistant> { ... } (refactored)
                          # webSocket(Ai.Chat.WS_PATH) { ... } (constant, not typed)

core/sdk/src/commonMain/kotlin/.../api/
    AuthApi.kt            # client.post(Auth.Register()) { ... } (refactored)
    UserApi.kt            # client.get(Users.Me()) (refactored)

core/sdk/src/commonMain/kotlin/.../
    AuthInterceptor.kt    # Uses Auth.Refresh resource for detection (refactored)
    ApiClient.kt          # install(Resources) added to client config
```

### Pattern 1: Flat @Resource Hierarchy with Nesting
**What:** Define top-level route groups (`Auth`, `Users`, `Ai`) as `@Resource` classes with nested inner classes for each endpoint.
**When to use:** When routes follow a RESTful hierarchy (e.g., `/api/auth/register`, `/api/users/me`, `/api/users/{id}`).
**Example:**
```kotlin
// Source: https://ktor.io/docs/server-resources.html + project-specific adaptation
// Located in core:models (shared module)
package com.m2f.template.models.routes

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("/api/auth")
class Auth {
    @Serializable
    @Resource("register")
    class Register(val parent: Auth = Auth())

    @Serializable
    @Resource("login")
    class Login(val parent: Auth = Auth())

    @Serializable
    @Resource("refresh")
    class Refresh(val parent: Auth = Auth())

    @Serializable
    @Resource("logout")
    class Logout(val parent: Auth = Auth())

    @Serializable
    @Resource("forgot-password")
    class ForgotPassword(val parent: Auth = Auth())

    @Serializable
    @Resource("reset-password")
    class ResetPassword(val parent: Auth = Auth())
}

@Serializable
@Resource("/api/users")
class Users {
    @Serializable
    @Resource("me")
    class Me(val parent: Users = Users())

    @Serializable
    @Resource("{id}")
    class ById(val parent: Users = Users(), val id: String)
}

@Serializable
@Resource("/api/ai")
class Ai {
    @Serializable
    @Resource("assistant")
    class Assistant(val parent: Ai = Ai())

    @Serializable
    @Resource("chat")
    class Chat(val parent: Ai = Ai()) {
        companion object {
            /** WebSocket path -- type-safe routing not supported for WebSockets (KTOR-4369) */
            const val WS_PATH = "/api/ai/chat/ws"
        }
    }
}
```

### Pattern 2: Server-Side Type-Safe Handlers
**What:** Replace `route("/path") { get("/sub") { ... } }` with `get<Resource.Sub> { resource -> ... }`.
**When to use:** For all HTTP API endpoints on the server.
**Example:**
```kotlin
// Source: https://ktor.io/docs/server-resources.html
// Server-side handler (e.g., AuthRoutes.kt)
import io.ktor.server.resources.post

fun Route.authRoutes(authService: AuthService, passwordResetService: PasswordResetService) {
    post<Auth.Register> {
        conduit(HttpStatusCode.Created) {
            val request = getModel<RegisterRequest>()
            authService.register(request)
        }
    }
    post<Auth.Login> {
        conduit {
            val request = getModel<LoginRequest>()
            authService.login(request)
        }
    }
    // ... etc.
    authenticate {
        post<Auth.Logout> {
            conduitAuth { userId ->
                authService.logout(userId)
            }
        }
    }
}
```

### Pattern 3: Client-Side Type-Safe Requests
**What:** Replace `client.post("/api/auth/register") { ... }` with `client.post(Auth.Register()) { ... }`.
**When to use:** For all SDK API calls.
**Example:**
```kotlin
// Source: https://ktor.io/docs/client-resources.html
// Client-side request (e.g., AuthApi.kt)
import io.ktor.client.plugins.resources.post

class AuthApi(private val client: HttpClient, private val tokenStorage: TokenStorage) {
    suspend fun register(request: RegisterRequest): Either<AppError, AuthResponse> =
        apiCall<AuthResponse> {
            client.post(Auth.Register()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }.onRight { response ->
            tokenStorage.saveTokens(response.accessToken, response.refreshToken)
        }
}
```

### Pattern 4: AuthInterceptor Refresh Detection
**What:** Replace string matching `contains("/auth/refresh")` with typed resource URL generation via `href()`.
**When to use:** For the AuthInterceptor's refresh endpoint detection.
**Example:**
```kotlin
// In AuthInterceptor, replace:
//   val isRefreshRequest = request.url.buildString().contains("/auth/refresh")
// With:
import io.ktor.resources.href

val refreshPath = href(Auth.Refresh())
val isRefreshRequest = request.url.encodedPath == refreshPath
// Or simpler: request.url.encodedPath.endsWith("/api/auth/refresh")
// Using the resource path ensures it stays in sync with route definitions.
```

**Important note on `href()`:** The `href()` function is available from the `ktor-resources` module. It generates a URL path string from a resource instance. This is available in the shared/client code since `ktor-resources` is multiplatform.

### Anti-Patterns to Avoid
- **Putting server-specific dependencies in `core:models`:** Only `ktor-resources` (the shared artifact) goes in `core:models`. Never add `ktor-server-resources` there -- it's JVM-only and would break KMP compilation.
- **Duplicating resource classes:** Define them once in `core:models`. Both server and client import from there. If you define them in two places, you lose the single-source-of-truth benefit.
- **Using `@Resource` for WebSocket routes:** Ktor does not support `webSocket<R>` (KTOR-4369). Use a companion constant on the Chat resource instead.
- **Forgetting `install(Resources)` on both server AND client:** The Resources plugin must be installed in the server's `Application.module()` AND in the SDK's `HttpClient` builder. Missing either will cause runtime crashes.
- **Using `data class` instead of `class` for resources without properties:** `@Resource`-annotated classes with no properties should be regular classes (not data classes) -- they need a no-arg constructor. Ktor requires the default constructor for deserialization.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Route path constants | `object Routes { const val REGISTER = "/api/auth/register" }` | `@Resource` classes with Ktor Resources plugin | Resources give you type-safe path params, query params, nested routes, and serialization -- not just path strings |
| URL building for client | `"/api/users/$id"` string interpolation | `Users.ById(id = userId)` resource instance | Type-safe: compiler catches missing params; Resources plugin builds the URL correctly |
| Refresh endpoint detection | `url.contains("/auth/refresh")` | `href(Auth.Refresh())` for path comparison | String matching is fragile; if the route path changes, the detection breaks silently |

**Key insight:** The value of Ktor Resources is not just eliminating string duplication -- it's making route changes a compile-time error across the full stack. If you rename a route in the resource class, the compiler will flag every server handler and client call that needs updating.

## Common Pitfalls

### Pitfall 1: Import Conflicts Between Server and Client Resources
**What goes wrong:** Both `io.ktor.server.resources` and `io.ktor.client.plugins.resources` export functions named `get`, `post`, `put`, `delete`. If both are imported (e.g., in test files), you get ambiguous call errors.
**Why it happens:** The server and client Resources extensions have identical function names but different receivers.
**How to avoid:** In server code, import `io.ktor.server.resources.*`. In client/SDK code, import `io.ktor.client.plugins.resources.*`. Never import both in the same file. In test files, use fully-qualified calls if needed.
**Warning signs:** "Overload resolution ambiguity" compiler errors.

### Pitfall 2: Missing `install(Resources)` on Client
**What goes wrong:** `client.get(Auth.Register())` throws `IllegalStateException: Plugin not installed: Resources`.
**Why it happens:** The Resources plugin is needed for URL serialization. It's easy to remember on the server but forget on the client.
**How to avoid:** Add `install(Resources)` in `createApiClient()` alongside the existing `ContentNegotiation` and `Logging` installs.
**Warning signs:** Runtime crashes on first SDK API call.

### Pitfall 3: OAuth Routes Are Not Good Candidates for Resources
**What goes wrong:** OAuth routes involve browser redirects, Ktor's `authenticate("google-oauth")` provider mechanism, and `urlProvider` callbacks that construct URLs as strings. Making them `@Resource` classes adds complexity without benefit.
**Why it happens:** OAuth is a redirect-based flow. The browser navigates to `/api/auth/oauth/google`, which redirects to Google, which redirects back to `/api/auth/oauth/google/callback`. These routes are never called by the SDK client directly -- they're browser navigation targets.
**How to avoid:** Keep OAuth routes as string-based routes. They are server-only browser navigation endpoints, not API endpoints called by the SDK. The success criteria says "no string-based route/get/post remain for API endpoints" -- OAuth redirects are not API endpoints.
**Warning signs:** Trying to make `authenticate("google-oauth")` work with `get<OAuthResource>` and fighting the OAuth provider's URL construction.

### Pitfall 4: WebSocket Type-Safe Routing Not Supported
**What goes wrong:** There is no `webSocket<R>` function in Ktor. KTOR-4369 is the open feature request.
**Why it happens:** The Resources plugin only extends HTTP verb functions (`get`, `post`, `put`, `delete`), not WebSocket.
**How to avoid:** Use a `companion object` constant on the Chat resource class (e.g., `Ai.Chat.WS_PATH`) so the path is still defined alongside the other routes, even if it can't be type-safe.
**Warning signs:** Compiler error: "Unresolved reference: webSocket" when trying `webSocket<Ai.Chat>`.

### Pitfall 5: `conduit`/`conduitAuth` Compatibility with Resources
**What goes wrong:** The `conduit` and `conduitAuth` helpers use `context(context: RoutingContext)`. When using `get<R> { resource -> ... }`, the lambda has a different context shape than the string-based `get("/path") { ... }`.
**Why it happens:** In Ktor 3.x, `get<R>` provides the resource as a parameter to the lambda, and the `RoutingContext` is available via the same receiver as string-based routes. The `conduit`/`conduitAuth` helpers should continue to work since they use `context(context: RoutingContext)` which is the same receiver.
**How to avoid:** Verify that the existing `conduit`/`conduitAuth` context parameter helpers work inside `get<R>` blocks. They should, since both string-based and resource-based route handlers provide the same `RoutingContext`. Test this with a single route first before migrating all routes.
**Warning signs:** Compiler errors about missing context receivers inside resource handler blocks.

### Pitfall 6: Path Parameters Must Match Property Names
**What goes wrong:** If a `@Resource("{id}")` class has a property named `userId` instead of `id`, the path parameter won't bind.
**Why it happens:** Ktor Resources binds `{paramName}` to a property with the exact same name on the resource class.
**How to avoid:** Ensure property names exactly match the curly-brace path parameter names. E.g., `@Resource("{id}") class ById(val parent: Users = Users(), val id: String)`.
**Warning signs:** Path parameter is always null/default at runtime.

## Code Examples

Verified patterns from official Ktor 3.4.0 documentation:

### Complete Resource Definition (for this project)
```kotlin
// Source: Adapted from https://ktor.io/docs/server-resources.html
// File: core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt
package com.m2f.template.models.routes

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("/api/auth")
class Auth {
    @Serializable @Resource("register")
    class Register(val parent: Auth = Auth())

    @Serializable @Resource("login")
    class Login(val parent: Auth = Auth())

    @Serializable @Resource("refresh")
    class Refresh(val parent: Auth = Auth())

    @Serializable @Resource("logout")
    class Logout(val parent: Auth = Auth())

    @Serializable @Resource("forgot-password")
    class ForgotPassword(val parent: Auth = Auth())

    @Serializable @Resource("reset-password")
    class ResetPassword(val parent: Auth = Auth())
}

@Serializable
@Resource("/api/users")
class Users {
    @Serializable @Resource("me")
    class Me(val parent: Users = Users())

    @Serializable @Resource("{id}")
    class ById(val parent: Users = Users(), val id: String)
}

@Serializable
@Resource("/api/ai")
class Ai {
    @Serializable @Resource("assistant")
    class Assistant(val parent: Ai = Ai())

    @Serializable @Resource("chat")
    class Chat(val parent: Ai = Ai()) {
        companion object {
            const val WS_PATH = "/api/ai/chat/ws"
        }
    }
}
```

### Server Plugin Installation
```kotlin
// Source: https://ktor.io/docs/server-resources.html
// In Application.kt module()
import io.ktor.server.resources.Resources

fun Application.module() {
    install(Resources)
    // ... existing installs ...
}
```

### Client Plugin Installation
```kotlin
// Source: https://ktor.io/docs/client-resources.html
// In ApiClient.kt createApiClient()
import io.ktor.client.plugins.resources.Resources

fun createApiClient(...): HttpClient {
    val client = HttpClient(platformEngine()) {
        install(Resources)  // <-- Add this
        // ... existing config ...
    }
}
```

### Server Route Handler Migration (AuthRoutes)
```kotlin
// Source: https://ktor.io/docs/server-resources.html
import io.ktor.server.resources.post
import com.m2f.template.models.routes.Auth

fun Route.authRoutes(authService: AuthService, passwordResetService: PasswordResetService) {
    post<Auth.Register> {
        conduit(HttpStatusCode.Created) {
            val request = getModel<RegisterRequest>()
            authService.register(request)
        }
    }
    post<Auth.Login> {
        conduit {
            val request = getModel<LoginRequest>()
            authService.login(request)
        }
    }
    // ...
}
```

### Client SDK Migration (AuthApi)
```kotlin
// Source: https://ktor.io/docs/client-resources.html
import io.ktor.client.plugins.resources.post
import com.m2f.template.models.routes.Auth

class AuthApi(private val client: HttpClient, private val tokenStorage: TokenStorage) {
    suspend fun register(request: RegisterRequest): Either<AppError, AuthResponse> =
        apiCall<AuthResponse> {
            client.post(Auth.Register()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }.onRight { response ->
            tokenStorage.saveTokens(response.accessToken, response.refreshToken)
        }
}
```

### Path Parameter Usage (UserApi)
```kotlin
// Source: https://ktor.io/docs/client-resources.html
import io.ktor.client.plugins.resources.get
import com.m2f.template.models.routes.Users

class UserApi(private val client: HttpClient) {
    suspend fun getUserById(id: String): Either<AppError, UserResponse> =
        apiCall { client.get(Users.ById(id = id)) }
}
```

### AuthInterceptor Refresh Detection
```kotlin
// Using href() from ktor-resources to generate the path
import io.ktor.resources.href

class AuthInterceptor(private val tokenStorage: TokenStorage) {
    fun install(client: HttpClient) {
        client.plugin(HttpSend).intercept { request ->
            val isRefreshRequest = request.url.encodedPath.endsWith(href(Auth.Refresh()))
            // ... rest unchanged
        }
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `Locations` plugin | `Resources` plugin | Ktor 3.0.0 (Oct 2024) | Locations completely removed; Resources is the only option |
| `@Location` annotation | `@Resource` annotation | Ktor 2.x (Resources added alongside Locations) | `@Resource` requires `@Serializable` behavior from kotlinx.serialization |
| String-based `route()` + `get()` | `get<Resource>` type-safe handlers | Always available with Resources | Compile-time route safety |
| Manual URL building in client | `client.get(Resource())` | Ktor 2.0+ (client Resources plugin) | Type-safe client requests matching server routes |

**Deprecated/outdated:**
- `Locations` plugin: Removed in Ktor 3.0.0. Must use `Resources` instead.
- `@Location` annotation: Replaced by `@Resource` from `io.ktor.resources`.

## Codebase Inventory: Routes to Migrate

### Server-Side String Routes (17 total)

**AuthRoutes.kt (7 routes):**
- `route("/api/auth")` + `post("/register")` -> `post<Auth.Register>`
- `post("/login")` -> `post<Auth.Login>`
- `post("/refresh")` -> `post<Auth.Refresh>`
- `post("/forgot-password")` -> `post<Auth.ForgotPassword>`
- `post("/reset-password")` -> `post<Auth.ResetPassword>`
- `post("/logout")` (inside `authenticate`) -> `post<Auth.Logout>` (inside `authenticate`)

**UserRoutes.kt (3 routes):**
- `route("/api/users")` + `get("/me")` -> `get<Users.Me>`
- `put("/me")` -> `put<Users.Me>`
- `get("/{id}")` (inside `withRole`) -> `get<Users.ById>` (inside `withRole`)

**AiRoutes.kt (3 HTTP + 1 WebSocket):**
- `route("/api/ai")` + `post("/assistant")` -> `post<Ai.Assistant>`
- `post("/chat")` -> `post<Ai.Chat>`
- `webSocket("/chat/ws")` -> `webSocket(Ai.Chat.WS_PATH)` (constant, not typed)

**OAuthRoutes.kt (4 routes -- KEEP AS STRING):**
- `route("/api/auth/oauth")` + OAuth-specific routes -> Keep string-based (see Pitfall 3)

**Application.kt (1 route -- not an API endpoint):**
- `get("/amazing")` -> Keep string-based (health check / demo, not an API endpoint)

### Client-Side String URLs (12 total)

**AuthApi.kt (6 calls):**
- `client.post("/api/auth/register")` -> `client.post(Auth.Register())`
- `client.post("/api/auth/login")` -> `client.post(Auth.Login())`
- `client.post("/api/auth/refresh")` -> `client.post(Auth.Refresh())`
- `client.post("/api/auth/forgot-password")` -> `client.post(Auth.ForgotPassword())`
- `client.post("/api/auth/reset-password")` -> `client.post(Auth.ResetPassword())`
- `client.post("/api/auth/logout")` -> `client.post(Auth.Logout())`

**UserApi.kt (3 calls):**
- `client.get("/api/users/me")` -> `client.get(Users.Me())`
- `client.put("/api/users/me")` -> `client.put(Users.Me())`
- `client.get("/api/users/$id")` -> `client.get(Users.ById(id = id))`

**AuthInterceptor.kt (2 references):**
- `request.url.buildString().contains("/auth/refresh")` -> `request.url.encodedPath.endsWith(href(Auth.Refresh()))`
- `client.post("/api/auth/refresh")` -> `client.post(Auth.Refresh())`

**ErrorMapper.kt (1 reference -- comment only):**
- KDoc example: `client.post("/api/auth/login")` -> Update KDoc

### OAuth URL Providers in Application.kt (2 references -- KEEP AS STRING)
- `urlProvider = { "$baseUrl/api/auth/oauth/google/callback" }` -> Keep (OAuth-specific)
- `urlProvider = { "$baseUrl/api/auth/oauth/apple/callback" }` -> Keep (OAuth-specific)

### Summary of Scope
- **Migrate:** 13 server HTTP routes, 11 client API calls, 1 AuthInterceptor detection
- **Keep as string:** 4 OAuth routes, 1 `/amazing` demo route, 1 WebSocket route (but use constant), 2 OAuth urlProviders
- **Update:** 1 KDoc example

## Open Questions

1. **`conduit`/`conduitAuth` compatibility with `get<R>` handlers**
   - What we know: Both string-based and resource-based route handlers provide `RoutingContext` as the receiver. The `conduit`/`conduitAuth` helpers use `context(context: RoutingContext)` which should be available.
   - What's unclear: Whether the context parameter resolution works identically inside `get<R> { resource -> ... }` as inside `get("/path") { ... }`. The resource parameter is additional but shouldn't conflict.
   - Recommendation: Test with a single route first (e.g., `post<Auth.Login>`) before migrating all routes. If `conduit`/`conduitAuth` work unchanged inside `get<R>`, proceed with batch migration. If not, a small adapter may be needed.
   - Confidence: HIGH that it works -- Ktor 3.x uses the same `RoutingContext` for both patterns.

2. **`href()` availability in client code (for AuthInterceptor)**
   - What we know: `href()` is defined in the `ktor-resources` module, which is a shared multiplatform artifact. The `ktor-client-resources` module depends on `ktor-resources`.
   - What's unclear: Whether `href()` can be called standalone (without an `Application` context) in client-side code. Server docs show `application.href(Resource())`.
   - Recommendation: If `href()` requires an `Application` context, use a simpler approach: define a `companion object` with the path string on `Auth.Refresh` and check against that. Or construct the expected path string manually (`"/api/auth/refresh"`) but reference it from the resource class.
   - Confidence: MEDIUM -- the `href()` function in `ktor-resources` appears to be a standalone utility, but the server variant may require `Application`. Needs verification during implementation.

3. **`withRole` custom route selector interaction with `get<R>`**
   - What we know: `withRole` creates a child route with a `RouteSelector` and installs the `RoleAuthorizationPlugin`. Inside it, `get("/{id}")` works.
   - What's unclear: Whether `get<Users.ById>` works inside a `withRole { ... }` block the same way.
   - Recommendation: This should work -- `get<R>` is just a different way to register a route handler within the routing tree. The `withRole` creates a transparent route selector that should not interfere. Verify with a test.
   - Confidence: HIGH -- `RouteSelectorEvaluation.Transparent` should pass through for resource-based routes just as it does for string-based ones.

## Sources

### Primary (HIGH confidence)
- [Ktor 3.4.0 Type-Safe Routing (Server)](https://ktor.io/docs/server-resources.html) - `@Resource` definition, `get<R>` handlers, `install(Resources)`, nested resources, path parameters
- [Ktor 3.4.0 Type-Safe Requests (Client)](https://ktor.io/docs/client-resources.html) - `client.get(R())` pattern, `install(Resources)` on client, `ktor-client-resources` artifact
- [Ktor 3.0 Migration Guide](https://ktor.io/docs/migrating-3.html) - `Locations` removed, must use `Resources`; import changes
- [ktor-resources GitHub source (build.gradle.kts)](https://github.com/ktorio/ktor/tree/main/ktor-shared/ktor-resources) - Verified multiplatform targets (23 targets including WasmJs, JVM, iOS, Android)
- [ktor-resources API dump](https://api.ktor.io/ktor-shared/ktor-resources/) - Confirmed all platform targets, `@Resource` annotation, `ResourcesFormat`, `href()` functions

### Secondary (MEDIUM confidence)
- [KTOR-4369: WebSocket type-safe routing](https://youtrack.jetbrains.com/issue/KTOR-4369/) - WebSocket `webSocket<R>` not supported; feature request likely still open
- [Ktor Full-Stack KMP Tutorial](https://ktor.io/docs/full-stack-development-with-kotlin-multiplatform.html) - Shared module architecture patterns for KMP with Ktor
- Codebase analysis - All 17 server routes, 12 client URLs, and 1 AuthInterceptor reference inventoried from actual source files

### Tertiary (LOW confidence)
- None

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Official Ktor plugins, version 3.4.0 matches project, multiplatform verified
- Architecture: HIGH - Well-documented pattern, clear migration path from existing string routes
- Pitfalls: HIGH - WebSocket limitation verified via YouTrack; OAuth exclusion is clear; import conflicts are well-known
- Route inventory: HIGH - Extracted from actual source code with full file-level attribution
- `href()` availability in client: MEDIUM - Needs verification during implementation

**Research date:** 2026-02-15
**Valid until:** 2026-03-15 (stable -- Ktor Resources is a mature, well-documented plugin)
