# SDK API Group Source Patterns

Complete source code patterns for all files in the SDK API group pipeline. Replace `Xxx` with the actual group name (e.g., `Groups`, `Notifications`).

## 1. @Resource Route Definition

**File:** `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt` (append to existing)

```kotlin
@Serializable
@Resource("/api/xxx")
class Xxx {
    @Serializable @Resource("list")
    class List(val parent: Xxx = Xxx())

    @Serializable @Resource("{id}")
    class ById(val parent: Xxx = Xxx(), val id: String)

    @Serializable @Resource("create")
    class Create(val parent: Xxx = Xxx())
}
```

**Key rules:**
- All route classes are `@Serializable`
- Nested classes reference parent via `val parent: Xxx = Xxx()`
- Path params use `{id}` syntax with corresponding `val id: String`
- Route names match REST conventions (list, create, by-id)

---

## 2. DTO Definitions

**File:** `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/XxxDtos.kt` (new file)

```kotlin
package com.m2f.template.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class XxxResponse(
    val id: String,
    val name: String,
    // add fields as needed
)

@Serializable
data class CreateXxxRequest(
    val name: String,
    // add fields as needed
)

@Serializable
data class UpdateXxxRequest(
    val name: String? = null,
    // nullable fields for partial updates
)
```

**Key rules:**
- All DTOs are `@Serializable`
- Response types end with `Response`
- Request types end with `Request`, prefixed with verb (`Create`, `Update`)
- Update requests use nullable fields for partial updates

---

## 3. API Interface

**File:** `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/XxxApi.kt` (new file)

```kotlin
package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.XxxResponse
import com.m2f.template.models.dto.CreateXxxRequest
import com.m2f.template.models.dto.UpdateXxxRequest

/**
 * SDK functions for xxx endpoints.
 *
 * All functions return [Either<AppError, T>] for consistent error handling.
 * Auth token is automatically attached by the [AuthInterceptor].
 */
interface XxxApi {
    suspend fun getAll(): Either<AppError, List<XxxResponse>>
    suspend fun getById(id: String): Either<AppError, XxxResponse>
    suspend fun create(request: CreateXxxRequest): Either<AppError, XxxResponse>
    suspend fun update(id: String, request: UpdateXxxRequest): Either<AppError, XxxResponse>
    suspend fun delete(id: String): Either<AppError, Unit>
}
```

**Key rules:**
- All methods are `suspend`
- All return `Either<AppError, T>`
- Interface name is clean (no `Impl` suffix)
- KDoc describes the API group purpose

---

## 4. API Implementation

**File:** `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/XxxApiImpl.kt` (new file)

```kotlin
package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.XxxResponse
import com.m2f.template.models.dto.CreateXxxRequest
import com.m2f.template.models.dto.UpdateXxxRequest
import com.m2f.template.models.routes.Xxx
import com.m2f.template.sdk.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class XxxApiImpl(private val client: HttpClient) : XxxApi {

    override suspend fun getAll(): Either<AppError, List<XxxResponse>> =
        apiCall { client.get(Xxx.List()) }

    override suspend fun getById(id: String): Either<AppError, XxxResponse> =
        apiCall { client.get(Xxx.ById(id = id)) }

    override suspend fun create(request: CreateXxxRequest): Either<AppError, XxxResponse> =
        apiCall {
            client.post(Xxx.Create()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun update(id: String, request: UpdateXxxRequest): Either<AppError, XxxResponse> =
        apiCall {
            client.put(Xxx.ById(id = id)) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun delete(id: String): Either<AppError, Unit> =
        apiCall { client.delete(Xxx.ById(id = id)) }
}
```

**Key rules:**
- Constructor takes `HttpClient` (add `TokenStorage` only if side effects like token saving needed)
- All calls wrapped in `apiCall<T>` for error handling
- Uses Ktor Resources type-safe routing (`Xxx.List()`, `Xxx.ById(id = id)`)
- POST/PUT requests set `contentType(ContentType.Application.Json)` and `setBody(request)`
- GET/DELETE requests have no body

---

## 5. Sdk Facade Update

**File:** `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt` (modify)

```kotlin
class Sdk(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val xxxApi: XxxApi,  // ADD: new constructor param
) : AuthApi by authApi, UserApi by userApi, XxxApi by xxxApi  // ADD: delegation
```

**Key rules:**
- Add constructor parameter with the API interface type
- Add Kotlin `by` delegation to the class declaration
- Keep parameters in alphabetical order by convention

---

## 6. SdkModule Koin Update

**File:** `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt` (modify)

```kotlin
val sdkModule = module {
    single { AuthInterceptor(tokenStorage = get()) }
    single { createApiClient(authInterceptor = get(), baseUrl = defaultBaseUrl()) }
    single<AuthApi> { AuthApiImpl(client = get(), tokenStorage = get()) }
    single<UserApi> { UserApiImpl(client = get()) }
    single<XxxApi> { XxxApiImpl(client = get()) }  // ADD: bind interface to impl
    single { Sdk(authApi = get(), userApi = get(), xxxApi = get()) }  // UPDATE: add to Sdk constructor
}
```

**Key rules:**
- Bind using interface type qualifier: `single<XxxApi> { XxxApiImpl(...) }`
- Pass `client = get()` for HttpClient (add `tokenStorage = get()` only if needed)
- Update Sdk constructor to include new `xxxApi = get()`
- Add import for both the interface and implementation

---

## 7. Fake Builder

**File:** `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeXxxApiBuilder.kt` (new file)

```kotlin
package com.m2f.template.core.testing.fakes

import arrow.core.Either
import com.m2f.template.core.testing.FakeSDKDsl
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.XxxResponse
import com.m2f.template.models.dto.CreateXxxRequest
import com.m2f.template.models.dto.UpdateXxxRequest
import com.m2f.template.sdk.api.XxxApi

/**
 * DSL builder for creating fake [XxxApi] instances in tests.
 *
 * Each method defaults to returning `Either.Left(AppError.Client.Unknown())`
 * so that unconfigured code paths fail fast instead of silently succeeding.
 *
 * Usage:
 * ```kotlin
 * val xxxApi = fakeXxxApi {
 *     getAll { Either.Right(listOf(XxxResponse(...))) }
 * }
 * ```
 */
@FakeSDKDsl
class FakeXxxApiBuilder {

    private var _getAll: suspend () -> Either<AppError, List<XxxResponse>> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _getById: suspend (String) -> Either<AppError, XxxResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _create: suspend (CreateXxxRequest) -> Either<AppError, XxxResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _update: suspend (String, UpdateXxxRequest) -> Either<AppError, XxxResponse> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }

    private var _delete: suspend (String) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    fun getAll(behavior: suspend () -> Either<AppError, List<XxxResponse>>) {
        _getAll = behavior
    }

    fun getById(behavior: suspend (String) -> Either<AppError, XxxResponse>) {
        _getById = behavior
    }

    fun create(behavior: suspend (CreateXxxRequest) -> Either<AppError, XxxResponse>) {
        _create = behavior
    }

    fun update(behavior: suspend (String, UpdateXxxRequest) -> Either<AppError, XxxResponse>) {
        _update = behavior
    }

    fun delete(behavior: suspend (String) -> Either<AppError, Unit>) {
        _delete = behavior
    }

    internal fun build(): XxxApi = object : XxxApi {
        override suspend fun getAll(): Either<AppError, List<XxxResponse>> =
            _getAll()

        override suspend fun getById(id: String): Either<AppError, XxxResponse> =
            _getById(id)

        override suspend fun create(request: CreateXxxRequest): Either<AppError, XxxResponse> =
            _create(request)

        override suspend fun update(id: String, request: UpdateXxxRequest): Either<AppError, XxxResponse> =
            _update(id, request)

        override suspend fun delete(id: String): Either<AppError, Unit> =
            _delete(id)
    }
}

/**
 * Top-level DSL entry point for creating a fake [XxxApi].
 *
 * @param block optional configuration block to override default method behaviors
 * @return a configured [XxxApi] instance backed by the builder's lambdas
 */
fun fakeXxxApi(block: FakeXxxApiBuilder.() -> Unit = {}): XxxApi =
    FakeXxxApiBuilder().apply(block).build()
```

**Key rules:**
- Class annotated with `@FakeSDKDsl`
- Private lambda per interface method, prefixed with underscore
- Default: `Either.Left(AppError.Client.Unknown())` (fail fast)
- Multi-param lambdas use `_, _ ->` for unused params in default
- `build()` is `internal`
- Top-level `fakeXxxApi()` function with optional block parameter

---

## 8. FakeSdkBuilder Composition Update

**File:** `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeSdkBuilder.kt` (modify)

Add three things:
1. Private builder field
2. DSL function
3. Build wiring

```kotlin
@FakeSDKDsl
class FakeSdkBuilder {

    private var authApiBuilder: FakeAuthApiBuilder = FakeAuthApiBuilder()
    private var userApiBuilder: FakeUserApiBuilder = FakeUserApiBuilder()
    private var xxxApiBuilder: FakeXxxApiBuilder = FakeXxxApiBuilder()  // ADD

    fun auth(init: FakeAuthApiBuilder.() -> Unit) { authApiBuilder.init() }
    fun user(init: FakeUserApiBuilder.() -> Unit) { userApiBuilder.init() }
    fun xxx(init: FakeXxxApiBuilder.() -> Unit) { xxxApiBuilder.init() }  // ADD

    internal fun build(): Sdk {
        return Sdk(
            authApi = authApiBuilder.build(),
            userApi = userApiBuilder.build(),
            xxxApi = xxxApiBuilder.build(),  // ADD
        )
    }
}
```

**Key rules:**
- DSL function name is the lowercase group name (e.g., `groups`, `notifications`)
- Builder initialized eagerly with defaults
- `build()` passes all sub-builder results to `Sdk(...)` constructor

---

## 9. AppError Subclass (Optional)

**File:** `core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt` (modify, only if needed)

```kotlin
@Serializable
sealed class Xxx : AppError() {
    @Serializable
    data class NotFound(
        override val code: String = "XXX_NOT_FOUND",
        override val message: String = "Xxx not found"
    ) : Xxx()

    @Serializable
    data class Forbidden(
        override val code: String = "XXX_FORBIDDEN",
        override val message: String = "Access to xxx denied"
    ) : Xxx()
}
```

**Key rules:**
- Only create if the API group has domain-specific errors
- Error codes follow `DOMAIN_SPECIFIC_ERROR` format
- Each error has default `code` and `message` values
- Extend from `AppError()` as a sealed class

---

## Existing Reference: AuthApi (complete)

```kotlin
// AuthApi.kt
interface AuthApi {
    suspend fun register(request: RegisterRequest): Either<AppError, AuthResponse>
    suspend fun login(request: LoginRequest, rememberMe: Boolean = true): Either<AppError, AuthResponse>
    suspend fun refresh(request: RefreshTokenRequest): Either<AppError, AuthResponse>
    suspend fun forgotPassword(request: ForgotPasswordRequest): Either<AppError, Unit>
    suspend fun resetPassword(request: ResetPasswordRequest): Either<AppError, Unit>
    suspend fun logout(): Either<AppError, Unit>
}
```

## Existing Reference: UserApi (complete)

```kotlin
// UserApi.kt
interface UserApi {
    suspend fun getProfile(): Either<AppError, UserResponse>
    suspend fun updateProfile(request: UpdateProfileRequest): Either<AppError, UserResponse>
    suspend fun getUserById(id: String): Either<AppError, UserResponse>
}
```

## Existing Reference: FakeSdkBuilder (complete)

```kotlin
// FakeSdkBuilder.kt
@FakeSDKDsl
class FakeSdkBuilder {
    private var authApiBuilder: FakeAuthApiBuilder = FakeAuthApiBuilder()
    private var userApiBuilder: FakeUserApiBuilder = FakeUserApiBuilder()

    fun auth(init: FakeAuthApiBuilder.() -> Unit) { authApiBuilder.init() }
    fun user(init: FakeUserApiBuilder.() -> Unit) { userApiBuilder.init() }

    internal fun build(): Sdk {
        return Sdk(
            authApi = authApiBuilder.build(),
            userApi = userApiBuilder.build(),
        )
    }
}

fun fakeSdk(block: FakeSdkBuilder.() -> Unit = {}): Sdk =
    FakeSdkBuilder().apply(block).build()
```
