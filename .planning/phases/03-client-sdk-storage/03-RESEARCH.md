# Phase 3: Client SDK & Storage - Research

**Researched:** 2026-02-11
**Domain:** Ktor client HTTP layer, Arrow Either integration, token refresh interceptor, multiplatform key-value persistence, platform-specific Ktor engines
**Confidence:** HIGH

## Summary

Phase 3 builds the client-side networking and persistence layers that sit between the server endpoints (delivered in Phase 2) and the UI screens (Phase 5). The project already has empty `core:sdk` and `core:storage` KMP modules with the correct target matrix (Android, iOS, JVM, WasmJs), Arrow Core as a dependency in `core:sdk`, and shared DTOs (`LoginRequest`, `RegisterRequest`, `AuthResponse`, `RefreshTokenRequest`, `UserResponse`, `UpdateProfileRequest`, `ErrorResponse`) in `core:models`. The `composeApp` already depends on `projects.core.sdk`. The version catalog already declares `ktor-client-core`, `ktor-client-cio`, `ktor-client-content-negotiation`, `ktor-client-logging`, and `ktor-serialization-kotlinx-json` in a `ktor-client` bundle.

The three key technical challenges flagged for research are resolved:

1. **Token refresh mutex patterns in KMP:** Ktor 3.4.0 ships a built-in `Auth` plugin with `bearer {}` block that handles `loadTokens` / `refreshTokens` automatically. When a 401 is received, the plugin calls `refreshTokens`, retries the request, and caches the result. Ktor 3.4.0 adds `clearToken()` for logout. **However, the Auth plugin has a known limitation on WasmJs/browser**: the `Authorization` header cannot be set programmatically by the plugin in the browser fetch API. The workaround is a custom `HttpSend` interceptor that manually adds the bearer token and handles refresh with a coroutines `Mutex`. This is the recommended approach since it works uniformly across all platforms.

2. **WASM DataStore limitations:** AndroidX DataStore KMP (1.2.0) supports Android, iOS, and JVM Desktop only -- **no WasmJs support**. The solution is `multiplatform-settings` 1.3.0 by Russell Wolf, which supports all project targets including WasmJs (via `StorageSettings` backed by `localStorage`). For secure token storage on mobile, the library provides `KeychainSettings` on Apple platforms and can be paired with Android `EncryptedSharedPreferences`.

3. **Arrow Either serialization with Ktor content negotiation:** The `arrow-core-serialization` artifact (version 2.2.1.1, matching the project's Arrow version) provides `EitherSerializer`, `OptionSerializer`, and `ArrowModule` for `kotlinx.serialization`. The SDK layer does NOT need to serialize `Either` over the wire -- the server returns plain JSON DTOs or `ErrorResponse`. The SDK deserializes the response, inspects the HTTP status code, and wraps the result in `Either<AppError, T>` client-side. No special serialization module is needed for the wire format.

**Primary recommendation:** Use a custom `HttpSend`-based auth interceptor (not the `Auth` plugin) for cross-platform consistency including WasmJs, `multiplatform-settings` for all key-value persistence (both tokens and preferences), and the existing `AppError` sealed hierarchy as the client error type (no separate `ClientError` needed -- `AppError.Client.*` and `AppError.Auth.*` subtypes already exist).

## Standard Stack

### Core (Already in Project)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Ktor Client Core | 3.4.0 | HTTP client foundation | Already in version catalog (`ktor-client-core`); multiplatform |
| Ktor Client Content Negotiation | 3.4.0 | JSON serialization for requests/responses | Already in version catalog; pairs with `ktor-serialization-kotlinx-json` |
| Ktor Client Logging | 3.4.0 | HTTP request/response logging | Already in version catalog |
| Ktor Serialization kotlinx-json | 3.4.0 | JSON format for content negotiation | Already in version catalog |
| Arrow Core | 2.2.1.1 | `Either`, `Raise`, typed error handling | Already a dependency of `core:sdk` |
| kotlinx-serialization-json | 1.10.0 | JSON parsing for DTOs | Already a dependency of `core:models` |
| Koin Core | 4.1.1 | Dependency injection | Already used project-wide |
| kotlinx-coroutines-core | 1.10.2 | `Mutex`, coroutine primitives | Already a transitive dependency |

### New Dependencies Required

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Ktor Client OkHttp | 3.4.0 | Android HTTP engine | Standard Android engine; uses OkHttp under the hood |
| Ktor Client Darwin | 3.4.0 | iOS HTTP engine | Uses NSURLSession; the only viable iOS engine |
| multiplatform-settings | 1.3.0 | Key-value persistence | Only actively-maintained KMP settings library with full WasmJs support |
| multiplatform-settings-coroutines | 1.3.0 | Flow-based settings observation | Enables reactive preference updates for UI |
| multiplatform-settings-no-arg | 1.3.0 | Platform-default Settings() factory | Simplifies DI -- `Settings()` returns platform-appropriate impl |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| multiplatform-settings | AndroidX DataStore KMP 1.2.0 | DataStore has no WasmJs support. Would need `expect/actual` with localStorage fallback for WASM. More complex, less uniform. **Recommendation: multiplatform-settings.** |
| Custom HttpSend interceptor | Ktor Auth plugin (`ktor-client-auth`) | Auth plugin does not work on WasmJs browser due to Authorization header restriction. Using it would require different code paths per platform. **Recommendation: Custom interceptor for uniform behavior.** |
| multiplatform-settings | Direct `expect/actual` with SharedPreferences / NSUserDefaults / localStorage | Reimplementing what multiplatform-settings already provides perfectly. **Recommendation: Don't hand-roll.** |
| CIO engine for all platforms | Platform-specific engines | CIO technically works on WasmJs and JVM but OkHttp/Darwin are more mature for Android/iOS (connection pooling, HTTP/2, certificate pinning). CIO is fine for JVM desktop and WasmJs. **Recommendation: OkHttp for Android, Darwin for iOS, CIO for JVM desktop and WasmJs.** |

### Dependencies to Add

In `gradle/libs.versions.toml`:
```toml
[versions]
multiplatform-settings = "1.3.0"

[libraries]
ktor-client-okhttp = { group = "io.ktor", name = "ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { group = "io.ktor", name = "ktor-client-darwin", version.ref = "ktor" }
multiplatform-settings = { module = "com.russhwolf:multiplatform-settings", version.ref = "multiplatform-settings" }
multiplatform-settings-noarg = { module = "com.russhwolf:multiplatform-settings-no-arg", version.ref = "multiplatform-settings" }
multiplatform-settings-coroutines = { module = "com.russhwolf:multiplatform-settings-coroutines", version.ref = "multiplatform-settings" }
```

In `core/sdk/build.gradle.kts` sourceSets:
```kotlin
commonMain.dependencies {
    api(projects.core.models)
    implementation(libs.arrow.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.koin.core)
}
androidMain.dependencies {
    implementation(libs.ktor.client.okhttp)
}
iosMain.dependencies {
    implementation(libs.ktor.client.darwin)
}
jvmMain.dependencies {
    implementation(libs.ktor.client.cio)
}
wasmJsMain.dependencies {
    implementation(libs.ktor.client.cio)
}
```

In `core/storage/build.gradle.kts` sourceSets:
```kotlin
commonMain.dependencies {
    api(projects.core.models)
    implementation(libs.multiplatform.settings)
    implementation(libs.multiplatform.settings.noarg)
    implementation(libs.multiplatform.settings.coroutines)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.koin.core)
}
```

## Architecture Patterns

### Recommended Project Structure

```
core/sdk/src/
├── commonMain/kotlin/com/m2f/template/sdk/
│   ├── ApiClient.kt            # HttpClient factory with interceptors
│   ├── AuthInterceptor.kt      # Token attach + 401 refresh + retry
│   ├── ErrorMapper.kt          # HTTP status -> AppError mapping
│   ├── TokenProvider.kt        # Interface for token read/write
│   ├── api/
│   │   ├── AuthApi.kt          # register, login, refresh, logout
│   │   └── UserApi.kt          # getProfile, updateProfile, getUserById
│   └── di/
│       └── SdkModule.kt        # Koin module for SDK dependencies
├── androidMain/kotlin/.../
│   └── PlatformEngine.android.kt   # expect/actual -> OkHttp
├── iosMain/kotlin/.../
│   └── PlatformEngine.ios.kt       # expect/actual -> Darwin
├── jvmMain/kotlin/.../
│   └── PlatformEngine.jvm.kt       # expect/actual -> CIO
└── wasmJsMain/kotlin/.../
    └── PlatformEngine.wasmJs.kt     # expect/actual -> CIO (or Js)

core/storage/src/
├── commonMain/kotlin/com/m2f/template/storage/
│   ├── TokenStorage.kt         # Read/write/clear auth tokens
│   ├── PreferencesStorage.kt   # Theme, language, user settings
│   └── di/
│       └── StorageModule.kt    # Koin module for storage dependencies
└── (platform source sets only if needed for Settings.Factory)
```

### Pattern 1: Platform Engine via expect/actual

**What:** Declare an `expect` function in commonMain that returns an `HttpClientEngineFactory`, with `actual` implementations per platform.
**When to use:** HttpClient creation.

```kotlin
// commonMain
import io.ktor.client.engine.HttpClientEngineFactory

expect fun platformEngine(): HttpClientEngineFactory<*>

// androidMain
import io.ktor.client.engine.okhttp.OkHttp
actual fun platformEngine(): HttpClientEngineFactory<*> = OkHttp

// iosMain
import io.ktor.client.engine.darwin.Darwin
actual fun platformEngine(): HttpClientEngineFactory<*> = Darwin

// jvmMain
import io.ktor.client.engine.cio.CIO
actual fun platformEngine(): HttpClientEngineFactory<*> = CIO

// wasmJsMain
import io.ktor.client.engine.cio.CIO
actual fun platformEngine(): HttpClientEngineFactory<*> = CIO
```

### Pattern 2: Either-wrapped API Functions

**What:** Each SDK function makes an HTTP call, deserializes the response, and returns `Either<AppError, T>`. On success, returns `Right(T)`. On HTTP error, deserializes `ErrorResponse` from body and maps to the appropriate `AppError` subtype.
**When to use:** Every SDK function.

```kotlin
// Source: Project patterns -- Arrow Either + Ktor client
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.*

class AuthApi(private val client: HttpClient) {

    suspend fun login(request: LoginRequest): Either<AppError, AuthResponse> =
        apiCall { client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }}

    suspend fun register(request: RegisterRequest): Either<AppError, AuthResponse> =
        apiCall { client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }}
}
```

### Pattern 3: Centralized Error Mapping

**What:** A single `apiCall` helper that wraps any Ktor HTTP call, catches exceptions, and maps HTTP status codes + `ErrorResponse` bodies to `AppError` subtypes.
**When to use:** Every HTTP call in the SDK.

```kotlin
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

suspend inline fun <reified T> apiCall(
    block: () -> HttpResponse
): Either<AppError, T> = try {
    val response = block()
    if (response.status.isSuccess()) {
        response.body<T>().right()
    } else {
        mapHttpError(response).left()
    }
} catch (e: Exception) {
    mapException(e).left()
}

suspend fun mapHttpError(response: HttpResponse): AppError {
    val errorResponse = try {
        response.body<ErrorResponse>()
    } catch (_: Exception) {
        null
    }
    return when (response.status.value) {
        401 -> AppError.Auth.Unauthorized(
            message = errorResponse?.message ?: "Authentication required"
        )
        403 -> AppError.User.Forbidden(
            message = errorResponse?.message ?: "Access denied"
        )
        404 -> AppError.User.NotFound(
            message = errorResponse?.message ?: "Not found"
        )
        422 -> if (errorResponse != null) {
            AppError.Client.ServerMapped(
                code = errorResponse.code,
                message = errorResponse.message
            )
        } else {
            AppError.Validation.InvalidInput(fieldErrors = emptyList())
        }
        in 500..599 -> AppError.Server.Internal(
            message = errorResponse?.message ?: "Server error"
        )
        else -> AppError.Client.Unknown(
            detail = errorResponse?.message ?: "HTTP ${response.status.value}"
        )
    }
}

fun mapException(e: Exception): AppError = when (e) {
    is kotlinx.io.IOException -> AppError.Client.Network()
    else -> AppError.Client.Unknown(detail = e.message)
}
```

### Pattern 4: Custom Auth Interceptor with HttpSend

**What:** A custom interceptor using Ktor's `HttpSend` plugin that attaches the access token to every request and handles 401 responses by refreshing the token and retrying. Uses `Mutex` to prevent concurrent refresh calls.
**When to use:** Configured once on the HttpClient; applies to all requests.

```kotlin
// Source: Ktor HttpSend docs + community patterns
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthInterceptor(
    private val tokenStorage: TokenStorage,
    private val refreshCall: suspend (String) -> AuthResponse?,
) {
    private val refreshMutex = Mutex()

    fun install(client: HttpClient) {
        client.plugin(HttpSend).intercept { request ->
            // Attach current access token
            val accessToken = tokenStorage.getAccessToken()
            if (accessToken != null) {
                request.bearerAuth(accessToken)
            }

            val originalCall = execute(request)

            // If 401, attempt refresh and retry
            if (originalCall.response.status == HttpStatusCode.Unauthorized) {
                val refreshToken = tokenStorage.getRefreshToken() ?: return@intercept originalCall

                val newTokens = refreshMutex.withLock {
                    // Double-check: another coroutine may have already refreshed
                    val currentToken = tokenStorage.getAccessToken()
                    if (currentToken != accessToken) {
                        // Already refreshed by another coroutine
                        null
                    } else {
                        refreshCall(refreshToken)
                    }
                }

                if (newTokens != null) {
                    tokenStorage.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                    request.bearerAuth(newTokens.accessToken)
                    execute(request)
                } else {
                    // Retry with potentially updated token from another coroutine
                    val updatedToken = tokenStorage.getAccessToken()
                    if (updatedToken != null && updatedToken != accessToken) {
                        request.bearerAuth(updatedToken)
                        execute(request)
                    } else {
                        originalCall // Give up, return 401
                    }
                }
            } else {
                originalCall
            }
        }
    }
}
```

**Critical note on `expectSuccess`:** When `expectSuccess = true` (Ktor default), 4xx/5xx responses throw exceptions BEFORE `HttpSend` intercepts. The HttpClient must be configured with `expectSuccess = false` so that 401 responses reach the interceptor instead of being thrown as exceptions.

### Pattern 5: Token Storage with multiplatform-settings

**What:** A `TokenStorage` class that persists access/refresh tokens using `multiplatform-settings`. Uses the `Settings` interface for synchronous read/write.
**When to use:** Token persistence and retrieval across app restarts.

```kotlin
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class TokenStorage(private val settings: Settings) {

    fun getAccessToken(): String? = settings[KEY_ACCESS_TOKEN]
    fun getRefreshToken(): String? = settings[KEY_REFRESH_TOKEN]

    fun saveTokens(accessToken: String, refreshToken: String) {
        settings[KEY_ACCESS_TOKEN] = accessToken
        settings[KEY_REFRESH_TOKEN] = refreshToken
    }

    fun clearTokens() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
    }
}
```

### Pattern 6: Preferences Storage

**What:** A `PreferencesStorage` class for user preferences (theme, language) using `multiplatform-settings-coroutines` for Flow-based observation.
**When to use:** User preference persistence and reactive UI updates.

```kotlin
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalSettingsApi::class)
class PreferencesStorage(private val settings: ObservableSettings) {

    var theme: String
        get() = settings[KEY_THEME] ?: "system"
        set(value) { settings[KEY_THEME] = value }

    var language: String
        get() = settings[KEY_LANGUAGE] ?: "en"
        set(value) { settings[KEY_LANGUAGE] = value }

    fun observeTheme(): Flow<String> =
        settings.getStringFlow(KEY_THEME, "system")

    fun observeLanguage(): Flow<String> =
        settings.getStringFlow(KEY_LANGUAGE, "en")

    companion object {
        private const val KEY_THEME = "pref_theme"
        private const val KEY_LANGUAGE = "pref_language"
    }
}
```

### Pattern 7: HttpClient Factory with Koin

**What:** A factory function that creates and configures the `HttpClient` with content negotiation, logging, auth interceptor, and base URL.
**When to use:** Single HttpClient instance per app, provided via Koin.

```kotlin
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createApiClient(
    authInterceptor: AuthInterceptor,
    baseUrl: String,
): HttpClient {
    val client = HttpClient(platformEngine()) {
        expectSuccess = false // Required for auth interceptor to see 401s

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        install(Logging) {
            level = LogLevel.HEADERS
        }

        defaultRequest {
            url(baseUrl)
        }
    }

    authInterceptor.install(client)
    return client
}
```

### Anti-Patterns to Avoid

- **Using Ktor Auth plugin for bearer tokens on WasmJs:** The Auth plugin cannot programmatically set the `Authorization` header in browser environments (WasmJs). Requests will fail with 401 even though `loadTokens` returns valid tokens. Use a custom `HttpSend` interceptor instead.
- **Serializing Either over the wire:** The server sends plain JSON DTOs and `ErrorResponse` -- not Arrow `Either`. The SDK wraps responses in `Either` client-side after HTTP deserialization. Don't configure `ArrowModule` in the client's `ContentNegotiation`.
- **Separate `ClientError` sealed hierarchy:** The project already has `AppError.Client.*` subtypes (`Network`, `Timeout`, `Unknown`, `ServerMapped`) and `AppError.Auth.*` subtypes. Creating a parallel `ClientError` hierarchy would duplicate types. Use `AppError` directly as the Left type in `Either`.
- **`expectSuccess = true` with auth interceptor:** When `expectSuccess` is enabled (Ktor default), HTTP 4xx/5xx throw exceptions before `HttpSend` intercepts them. Must set `expectSuccess = false` for the interceptor to work.
- **Storing tokens in DataStore for WasmJs:** DataStore has no WasmJs target. Using it would require a fragmented `expect/actual` approach with different storage backends per platform.
- **Calling `client.close()` prematurely in KMP:** The HttpClient lifecycle should be tied to the application lifecycle via Koin (singleton scope). Closing the client and recreating it on each call wastes resources and connections.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Platform HTTP engines | Custom fetch/URLSession wrappers | Ktor platform engines (OkHttp, Darwin, CIO) | SSL pinning, connection pooling, HTTP/2, redirect handling |
| Key-value persistence | `expect/actual` with SharedPreferences/NSUserDefaults/localStorage | multiplatform-settings 1.3.0 | Handles all platforms including WasmJs; tested; type-safe API |
| JSON deserialization | Manual JSON parsing | Ktor ContentNegotiation + kotlinx-serialization | Type safety, null handling, unknown key tolerance |
| Token refresh synchronization | Custom locking / atomic flags | kotlinx.coroutines `Mutex` | Coroutine-safe; works on all KMP targets including WasmJs |
| HTTP error code mapping | Switch on status codes in every call site | Centralized `apiCall<T>` wrapper with `mapHttpError` | Single source of truth for error mapping; consistent behavior |
| Observable preferences | Custom callback system | multiplatform-settings-coroutines `getStringFlow` | Integrates with Compose state collection; lifecycle-aware |

**Key insight:** The SDK module's job is to be a thin typed wrapper around Ktor HTTP calls. It should NOT contain business logic -- just HTTP call + error mapping + token management. Keep it thin.

## Common Pitfalls

### Pitfall 1: Ktor Auth Plugin Fails on WasmJs Browser
**What goes wrong:** The `Auth` plugin's `bearer {}` block loads tokens and attaches them to the `Authorization` header, but in browser environments (JS/WasmJs), the `fetch` API does not allow programmatic control of the `Authorization` header through Ktor's plugin pipeline. Requests go out without the token.
**Why it happens:** Browser security restrictions on the fetch API prevent certain headers from being set by JavaScript/Wasm code through the Ktor abstraction layer.
**How to avoid:** Use a custom `HttpSend` interceptor that calls `request.bearerAuth(token)` directly. This approach works uniformly across all platforms.
**Warning signs:** Login works on Android/iOS/Desktop but fails on WasmJs with 401 errors despite having valid tokens.

### Pitfall 2: Concurrent Token Refresh Race Condition
**What goes wrong:** Multiple simultaneous API calls all receive 401, and each independently tries to refresh the token. The second refresh call fails because the first already rotated the refresh token (server implements rotation as established in Phase 2).
**Why it happens:** Without synchronization, each failing request independently triggers a refresh.
**How to avoid:** Use `Mutex.withLock {}` around the refresh logic. Inside the lock, double-check whether the token has already been refreshed by another coroutine before making the refresh call.
**Warning signs:** Intermittent "token invalid" errors under concurrent requests; works fine with sequential calls.

### Pitfall 3: expectSuccess Prevents Auth Interceptor from Seeing 401s
**What goes wrong:** The `HttpSend` interceptor never receives 401 responses because Ktor's default `expectSuccess = true` throws a `ClientRequestException` before the interceptor runs.
**Why it happens:** The `HttpCallValidator` plugin (installed by default with `expectSuccess = true`) throws on non-2xx responses, and it runs in the pipeline before `HttpSend` intercepts.
**How to avoid:** Configure `expectSuccess = false` on the HttpClient. Handle HTTP errors in the `apiCall` wrapper instead.
**Warning signs:** `ClientRequestException` stack traces instead of `Either.Left(AppError)` values.

### Pitfall 4: Forgetting to Add Platform Source Sets in KMP Module
**What goes wrong:** `core:sdk` has only `commonMain` currently. Adding platform-specific Ktor engine dependencies without creating the corresponding source set directories causes Gradle configuration errors.
**Why it happens:** The `build.gradle.kts` declares `androidTarget()`, `iosX64()`, etc., but the `src/` directory only has `commonMain/`.
**How to avoid:** Create the platform source set directories (`androidMain`, `iosMain`, `jvmMain`, `wasmJsMain`) with the `expect/actual` engine factory implementations. Even if the platform-specific code is just one file, the directory must exist.
**Warning signs:** Gradle sync errors about unresolved `actual` declarations.

### Pitfall 5: multiplatform-settings Kotlin Version Mismatch
**What goes wrong:** multiplatform-settings 1.3.0 was compiled with Kotlin 2.0.0/2.1.0. The project uses Kotlin 2.3.10. While Kotlin generally maintains backward compatibility for consuming libraries, WasmJs target metadata format may have changed between major Kotlin versions.
**Why it happens:** KMP libraries must be compiled with a compatible Kotlin version for certain targets, especially WasmJs which has evolving ABI.
**How to avoid:** Test the `core:storage` module compilation on all targets (especially WasmJs) immediately after adding the multiplatform-settings dependency. If it fails, check for a newer multiplatform-settings release or pin the Kotlin version for storage.
**Warning signs:** Gradle resolution errors mentioning "incompatible" or "variant" on the WasmJs target only.

### Pitfall 6: Token Storage Not Cleared on Logout
**What goes wrong:** After calling the logout endpoint, tokens remain in `multiplatform-settings`. The user appears logged out but the next API call uses the old (now-revoked) access token, gets a 401, tries to refresh with the old (now-revoked) refresh token, and gets another 401.
**Why it happens:** The logout SDK function calls the server endpoint but forgets to call `tokenStorage.clearTokens()`.
**How to avoid:** The `logout()` SDK function must clear local token storage as its LAST step, after the server call succeeds (or even if it fails -- tokens should still be cleared locally).
**Warning signs:** After logout, the next login attempt fails or shows stale session data.

## Code Examples

### HttpClient Configuration for KMP

```kotlin
// Source: Ktor docs - client creation + content negotiation
fun createApiClient(
    authInterceptor: AuthInterceptor,
    baseUrl: String = "http://localhost:8080",
): HttpClient {
    val client = HttpClient(platformEngine()) {
        // CRITICAL: Must be false for auth interceptor to see 401 responses
        expectSuccess = false

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true  // Future-proof against new server fields
                isLenient = true
            })
        }

        install(Logging) {
            level = LogLevel.HEADERS
        }

        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }
    }

    authInterceptor.install(client)
    return client
}
```

### Koin Module for SDK + Storage

```kotlin
// Source: Follows established project Koin patterns
import org.koin.dsl.module

val sdkModule = module {
    single { TokenStorage(settings = get()) }
    single { PreferencesStorage(settings = get()) }
    single {
        AuthInterceptor(
            tokenStorage = get(),
            refreshCall = { refreshToken ->
                // Direct HTTP call without interceptor (to avoid recursion)
                // Use a separate "bare" client for refresh
                try {
                    val response = get<HttpClient>().post("/api/auth/refresh") {
                        setBody(RefreshTokenRequest(refreshToken))
                    }
                    if (response.status.isSuccess()) response.body<AuthResponse>() else null
                } catch (_: Exception) { null }
            }
        )
    }
    single { createApiClient(authInterceptor = get()) }
    single { AuthApi(client = get()) }
    single { UserApi(client = get()) }
}

val storageModule = module {
    single<Settings> { Settings() }  // multiplatform-settings-no-arg
    // For ObservableSettings (coroutines module):
    // single<ObservableSettings> { Settings() as ObservableSettings }
}
```

**Note on refresh recursion:** The `refreshCall` lambda inside `AuthInterceptor` must NOT go through the same interceptor, or it will loop infinitely on 401. Options: (a) use a separate "bare" `HttpClient` without the interceptor for refresh calls, (b) mark refresh requests with an attribute and skip interception for them, or (c) make the refresh call directly inline.

### Full SDK API Function Example

```kotlin
class UserApi(private val client: HttpClient) {

    suspend fun getProfile(): Either<AppError, UserResponse> =
        apiCall { client.get("/api/users/me") }

    suspend fun updateProfile(request: UpdateProfileRequest): Either<AppError, UserResponse> =
        apiCall { client.put("/api/users/me") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }}
}
```

### Settings Factory with Koin (Platform-Specific)

```kotlin
// commonMain - no-arg approach
import com.russhwolf.settings.Settings

// With multiplatform-settings-no-arg, Settings() returns:
// Android: SharedPreferencesSettings
// iOS: NSUserDefaultsSettings
// JVM: PreferencesSettings
// WasmJs: StorageSettings (localStorage)

val storageModule = module {
    single<Settings> { Settings() }
}
```

If `no-arg` doesn't work reliably with Kotlin 2.3.10, fall back to explicit `expect/actual`:

```kotlin
// commonMain
expect fun createSettings(): Settings

// androidMain
actual fun createSettings(): Settings =
    SharedPreferencesSettings(context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE))

// iosMain
actual fun createSettings(): Settings =
    NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)

// jvmMain
actual fun createSettings(): Settings =
    PreferencesSettings(Preferences.userRoot().node("com.m2f.template"))

// wasmJsMain
actual fun createSettings(): Settings =
    StorageSettings()  // uses localStorage
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Ktor Auth plugin for bearer tokens | Custom HttpSend interceptor (for WasmJs compat) | Ktor 3.4.0 (Jan 2026) - Auth plugin still has WasmJs limitations | Required for uniform cross-platform behavior |
| AndroidX DataStore for KMP settings | multiplatform-settings for full-platform KMP | DataStore KMP 1.2.0 (2026) - still no WasmJs | multiplatform-settings covers all targets |
| Manual token cache management | Ktor 3.4.0 `clearToken()` / `clearAuthTokens()` | Jan 2026 | Better logout support, but moot if using custom interceptor |
| Separate ClientError type | Use existing AppError sealed hierarchy | Project decision (Phase 1) | AppError.Client.* already covers all client error cases |

**Deprecated/outdated:**
- `ktor-client-js` engine: Use `ktor-client-cio` for WasmJs (CIO has WasmJs support as of Ktor 3.x). The `Js` engine is for legacy JS target only.
- `cacheTokens` in old Ktor Auth plugin versions: Replaced by `cacheTokens` config + explicit `clearToken()` in Ktor 3.4.0.

## Open Questions

1. **multiplatform-settings 1.3.0 + Kotlin 2.3.10 on WasmJs target**
   - What we know: multiplatform-settings 1.3.0 was compiled with Kotlin 2.0.0/2.1.0. The project uses Kotlin 2.3.10. JVM/Android/iOS targets should be backward-compatible. WasmJs ABI compatibility between Kotlin versions is less certain.
   - What's unclear: Whether the WasmJs target binaries are compatible across this Kotlin version gap.
   - Recommendation: Add the dependency and compile immediately. If WasmJs fails, check for a newer release or use `expect/actual` with direct `localStorage` access on WasmJs as a fallback. **Confidence: MEDIUM** -- Kotlin 2.3 should maintain backward compat but WasmJs is still maturing.

2. **Secure token storage on mobile**
   - What we know: `multiplatform-settings` stores tokens in SharedPreferences (Android) and NSUserDefaults (iOS), which are NOT encrypted by default. For a template project this is acceptable, but production apps need encrypted storage.
   - What's unclear: Whether to add `KeychainSettings` (iOS) / `EncryptedSharedPreferences` (Android) in this phase or defer.
   - Recommendation: Defer encrypted storage to a future enhancement. Use basic Settings for now and document the production hardening step. The template should demonstrate the pattern, not be production-security-complete.

3. **Refresh token recursion avoidance**
   - What we know: The auth interceptor must call `/api/auth/refresh` to get new tokens. This call must NOT go through the same interceptor (infinite loop on 401).
   - What's unclear: Best approach -- separate HttpClient instance, request attribute flag, or inline call.
   - Recommendation: Use a request attribute/flag (`markAsRefreshTokenRequest()` pattern from Ktor docs). Check the attribute in the interceptor and skip token attachment + retry for flagged requests. Cleaner than maintaining two HttpClient instances.

4. **Base URL configuration**
   - What we know: The server runs on `localhost:8080` in development. `Constants.kt` has `SERVER_PORT = 8080`. Production URL will differ.
   - What's unclear: How to configure the base URL per environment (debug vs release, per platform).
   - Recommendation: Accept `baseUrl` as a Koin parameter (or from a config in `shared` module). Default to `http://10.0.2.2:8080` for Android emulator, `http://localhost:8080` for others. Make it overridable.

## Sources

### Primary (HIGH confidence)
- [Ktor Client Bearer Auth Docs](https://ktor.io/docs/client-bearer-auth.html) - loadTokens, refreshTokens, cacheTokens API
- [Ktor Client Engines Docs](https://ktor.io/docs/client-engines.html) - Platform engine matrix (OkHttp, Darwin, CIO, WasmJs support)
- [Ktor HttpSend Interceptor Docs](https://ktor.io/docs/client-http-send.html) - Request interception and retry pattern
- [Ktor 3.4.0 What's New](https://ktor.io/docs/whats-new-340.html) - clearToken(), clearAuthTokens(), cacheTokens improvements
- [multiplatform-settings README](https://github.com/russhwolf/multiplatform-settings/blob/main/README.md) - Platform support matrix including WasmJs, Settings implementations per platform
- [AndroidX DataStore KMP Setup](https://developer.android.com/kotlin/multiplatform/datastore) - Confirmed no WasmJs support as of 1.2.0
- [Maven Central: arrow-core-serialization 2.2.1.1](https://central.sonatype.com/artifact/io.arrow-kt/arrow-core-serialization-jvm) - Confirmed availability matching project Arrow version

### Secondary (MEDIUM confidence)
- [Ktor Client Auth WasmJs Slack Discussion](https://slack-chats.kotlinlang.org/t/23190930/) - Confirmed Auth plugin Authorization header limitation on WasmJs browser
- [multiplatform-settings Releases](https://github.com/russhwolf/multiplatform-settings/releases) - Version 1.3.0 with WasmJs StorageSettings
- [Ktor Client Supported Platforms](https://ktor.io/docs/client-supported-platforms.html) - WasmJs listed as supported target

### Tertiary (LOW confidence)
- multiplatform-settings 1.3.0 compatibility with Kotlin 2.3.10 on WasmJs: Not verified. Library was compiled with Kotlin 2.0-2.1. Needs validation at build time.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - All core libraries already in version catalog. New dependencies (multiplatform-settings, ktor-client-okhttp, ktor-client-darwin) are well-established and verified on Maven Central.
- Architecture: HIGH - Patterns verified against Ktor official docs, existing project conventions, and Arrow usage patterns. The custom HttpSend interceptor approach is documented by Ktor and used in community projects.
- Pitfalls: HIGH - WasmJs Auth plugin limitation confirmed via Kotlinlang Slack and community reports. Token refresh race condition is a well-documented distributed systems problem. expectSuccess behavior verified in Ktor docs.
- Storage: MEDIUM - multiplatform-settings API and WasmJs support verified via README. Kotlin 2.3.10 compatibility is the only uncertainty (LOW confidence on that specific point).

**Research date:** 2026-02-11
**Valid until:** 2026-03-11 (stable libraries, Ktor 3.4.0 is current stable)
