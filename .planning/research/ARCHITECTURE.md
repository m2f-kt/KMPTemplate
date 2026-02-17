# Architecture Research

**Domain:** KMP Full-Stack Template -- MVI ViewModel, Group Management, Testing, Localization
**Researched:** 2026-02-17
**Confidence:** HIGH

## System Overview

### Current Module Graph

```
                           settings.gradle.kts
                                  |
        +-----------+-------------+-------------+-----------+
        |           |             |             |           |
   core:models  core:sdk    core:storage     shared    composeApp
        |           |             |             |           |
        |     (depends on         |        (api: models,   |
        |      models,storage)    |         sdk, kermit,   |
        |           |             |          koin-core)     |
        +-----+-----+------+-----+             |           |
              |             |                   |           |
         app:auth     app:dashboard        app:profile  app:designsystem
              |             |                   |           |
              +------+------+------+------+-----+-----------+
                     |
                  composeApp  (navigation host, DI root)

        server
          |
    +-----+-----+----------+
    |           |          |
server:auth  server:ai  server:core
    |           |      +---+---+-------+
    |           |      |       |       |
    |           |   config  database  security
    +-----+-----+------+------+-------+
          |
       Application.kt (routes, DI, startup)
```

### Proposed New Modules

```
                      NEW MODULES
                          |
    +----------+----------+----------+----------+
    |          |          |          |          |
core:viewmodel core:l10n core:testing server:groups
    |          |          |          |
    |    (string keys,   (test    (GroupsTable,
    | MVI base class,   DSL,      GroupMembersTable,
    | Koin integration) fakes)    routes, service)
    |          |          |          |
    |          |          |     app:admin
    |          |          |     (admin panel UI)
    +-----+----+----------+----------+
```

### Updated settings.gradle.kts Additions

```kotlin
// New core modules
include("core:viewmodel")
include("core:l10n")
include("core:testing")

// New server feature module
include("server:groups")

// New app feature module
include("app:admin")
```

## Recommended Architecture

### 1. MVI ViewModel Base Class -- `core:viewmodel`

**Module:** New `core:viewmodel` KMP library module
**Package:** `com.m2f.template.viewmodel`

The existing ViewModels (LoginViewModel, ProfileViewModel, DashboardViewModel) follow an informal MVI-like pattern: they hold a `MutableStateFlow<State>`, expose `val state: StateFlow<State>`, and handle user actions via method calls. The key missing piece is a formalized base class that standardizes this pattern and provides testability hooks.

**Why a new module, not inline in `shared`:**
- The `shared` module currently only re-exports `core:models`, `core:sdk`, and `kermit`. Adding ViewModel infrastructure there would break the separation of concerns.
- `core:viewmodel` can depend on `lifecycle-viewmodel-compose` and `kotlinx-coroutines` without polluting `core:models` or `core:sdk`.
- App feature modules (`app:auth`, `app:dashboard`, etc.) already depend on `lifecycle-viewmodel-compose`; having a shared base avoids duplication.

**Base class design:**

```kotlin
package com.m2f.template.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base MVI ViewModel providing unidirectional data flow.
 *
 * @param S The immutable state type (data class).
 * @param I The intent/action sealed interface.
 * @param E The one-shot effect type (navigation, toast, etc.).
 */
abstract class MviViewModel<S, I, E>(initialState: S) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<E>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    protected val currentState: S get() = _state.value

    fun dispatch(intent: I) {
        viewModelScope.launch { handleIntent(intent) }
    }

    protected abstract suspend fun handleIntent(intent: I)

    protected fun setState(reducer: S.() -> S) {
        _state.update(reducer)
    }

    protected suspend fun sendEffect(effect: E) {
        _effects.send(effect)
    }
}
```

**Key design decisions:**
- **`Channel<E>` for effects, not `SharedFlow`:** Effects (navigation, snackbar) are one-shot events that must be consumed exactly once. Channel guarantees delivery even if no collector is active at emit time (buffered). This avoids the well-known "event consumed twice" bug with `SharedFlow(replay=1)` and the "event lost" bug with `SharedFlow(replay=0)`.
- **`dispatch(intent)` instead of individual methods:** Standardizes the entry point, makes the intent contract explicit via a sealed interface, and enables intent logging/debugging.
- **`setState(reducer)` wrapping `update`:** Keeps the `MutableStateFlow` private to the base class, preventing subclasses from accidentally replacing the flow.
- **Extends `androidx.lifecycle.ViewModel`:** This is what the project already uses (via `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` version 2.9.6). No need for a custom lifecycle-aware base.
- **No external MVI library (Orbit, MVIKotlin):** The base class is ~30 lines. Adding Orbit or MVIKotlin would add transitive dependencies and API surface for minimal gain. The project already uses coroutines and StateFlow; the base class just standardizes the pattern.

**Integration with Koin -- zero changes needed:**

Koin's `viewModelOf(::MyViewModel)` and `koinViewModel<MyViewModel>()` work with any `ViewModel` subclass. The existing pattern in `AppModule.kt` continues to work:

```kotlin
// Existing pattern -- still works
val appModule = module {
    viewModelOf(::LoginViewModel)  // LoginViewModel extends MviViewModel extends ViewModel
}

// In composable -- still works
val viewModel = koinViewModel<LoginViewModel>()
val state by viewModel.state.collectAsStateWithLifecycle()
```

**Integration with Navigation Compose 2.9.2 -- zero changes needed:**

The existing `AppNavHost.kt` pattern of `koinViewModel<T>()` inside `composable<Route>` blocks continues to work. The MVI base class does not impose any navigation opinion. Effects are consumed via `LaunchedEffect`:

```kotlin
composable<LoginRoute> {
    val viewModel = koinViewModel<LoginViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Consume one-shot effects for navigation
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToDashboard -> {
                    navController.navigate(DashboardRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                }
            }
        }
    }

    LoginScreen(
        state = state,
        onEmailChange = { viewModel.dispatch(LoginIntent.EmailChanged(it)) },
        onLoginClick = { viewModel.dispatch(LoginIntent.Submit) },
    )
}
```

**Migration path for existing ViewModels:**

Existing ViewModels do NOT need to migrate immediately. They can be migrated incrementally. New ViewModels should use `MviViewModel`. Example migration of LoginViewModel:

```kotlin
// BEFORE (existing pattern in app:auth):
class LoginViewModel(private val authApi: AuthApi) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()
    fun onEmailChange(email: String) { _state.update { it.copy(email = email) } }
    fun login() { viewModelScope.launch { ... } }
}

// AFTER (MVI pattern):
class LoginViewModel(private val authApi: AuthApi) :
    MviViewModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {

    override suspend fun handleIntent(intent: LoginIntent) = when (intent) {
        is LoginIntent.EmailChanged -> setState { copy(email = intent.email, emailError = null) }
        is LoginIntent.PasswordChanged -> setState { copy(password = intent.password) }
        is LoginIntent.RememberMeChanged -> setState { copy(rememberMe = intent.checked) }
        is LoginIntent.Submit -> performLogin()
    }

    private suspend fun performLogin() {
        val current = currentState
        // ... validation logic ...
        setState { copy(isLoading = true, serverError = null) }
        authApi.login(LoginRequest(current.email.trim(), current.password), rememberMe = current.rememberMe)
            .fold(
                ifLeft = { error -> setState { copy(serverError = error.message, isLoading = false) } },
                ifRight = { sendEffect(LoginEffect.NavigateToDashboard) },
            )
    }
}

sealed interface LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent
    data class PasswordChanged(val password: String) : LoginIntent
    data class RememberMeChanged(val checked: Boolean) : LoginIntent
    data object Submit : LoginIntent
}

sealed interface LoginEffect {
    data object NavigateToDashboard : LoginEffect
}
```

**Build file (`core/viewmodel/build.gradle.kts`):**

```kotlin
plugins {
    id("kmp-library-convention")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.kotlinx.coroutines)
        }
    }
}

android {
    namespace = "com.m2f.template.viewmodel"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
```

### 2. Group Management -- `server:groups` + `core:models` + `core:sdk` + `app:admin`

**Database schema (new tables in `server:groups`):**

```sql
-- groups table
CREATE TABLE groups (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    owner_id    UUID NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- group_members table (join table with role)
CREATE TABLE group_members (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id    UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role        VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(group_id, user_id)
);
```

**Exposed table definitions:**

```kotlin
// server:groups/tables/GroupsTable.kt
@OptIn(ExperimentalUuidApi::class)
object GroupsTable : Table("groups") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 100)
    val description = varchar("description", 500).nullable()
    val ownerId = uuid("owner_id").references(UsersTable.id)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}

// server:groups/tables/GroupMembersTable.kt
@OptIn(ExperimentalUuidApi::class)
object GroupMembersTable : Table("group_members") {
    val id = uuid("id").autoGenerate()
    val groupId = uuid("group_id").references(GroupsTable.id)
    val userId = uuid("user_id").references(UsersTable.id)
    val role = varchar("role", 20).default("MEMBER")
    val joinedAt = datetime("joined_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
    init { uniqueIndex(groupId, userId) }
}
```

**Why `server:groups` as a new feature module (not in `server:auth`):**
- Groups are a distinct domain concept. Auth handles identity; groups handle organization.
- Follows the existing pattern: `server:auth` owns users/roles/tokens, `server:ai` owns agents/conversations. `server:groups` owns groups/membership.
- Has its own Koin module (`groupsModule`), migration registration (`registerGroupsMigrations()`), and route installation.
- The `server:groups` module CAN reference `UsersTable` from `server:auth` for foreign key definitions (both are implementation dependencies of the `server` root module).

**Note on cross-module table references:** `server:groups` needs to reference `UsersTable.id` for foreign keys. Two approaches:
1. **Direct dependency:** `server:groups` depends on `server:auth` (simplest, but creates coupling).
2. **Shared table interface:** Extract `UsersTable` to `server:core:database` (cleaner, but more refactoring).

**Recommendation:** Option 1 (direct dependency) for now. The coupling is only at the table definition level, and `server:groups` genuinely needs to know about users. This mirrors how `server:ai` depends on `server:auth` tables for conversation ownership.

**Shared models (additions to `core:models`):**

```kotlin
// core:models - new file: dto/GroupDtos.kt
@Serializable
data class GroupResponse(
    val id: String,
    val name: String,
    val description: String?,
    val ownerId: String,
    val memberCount: Int,
)

@Serializable
data class CreateGroupRequest(val name: String, val description: String? = null)

@Serializable
data class GroupMemberResponse(
    val userId: String,
    val userName: String,
    val role: String, // OWNER, ADMIN, MEMBER
    val joinedAt: String,
)

@Serializable
data class AddMemberRequest(val userId: String, val role: String = "MEMBER")

// core:models - new file: routes/GroupRoutes.kt
@Serializable
@Resource("/api/groups")
class Groups {
    @Serializable @Resource("create")
    class Create(val parent: Groups = Groups())

    @Serializable @Resource("{id}")
    class ById(val parent: Groups = Groups(), val id: String)

    @Serializable @Resource("{id}/members")
    class Members(val parent: Groups = Groups(), val id: String)

    @Serializable @Resource("{id}/members/{userId}")
    class Member(val parent: Groups = Groups(), val id: String, val userId: String)
}
```

**SDK functions (additions to `core:sdk`):**

```kotlin
// core:sdk - new file: api/GroupApi.kt
class GroupApi(private val client: HttpClient) {
    suspend fun createGroup(request: CreateGroupRequest): Either<AppError, GroupResponse> =
        apiCall { client.post(Groups.Create()) {
            contentType(ContentType.Application.Json)
            setBody(request)
        } }

    suspend fun getGroup(id: String): Either<AppError, GroupResponse> =
        apiCall { client.get(Groups.ById(id = id)) }

    suspend fun getMembers(groupId: String): Either<AppError, List<GroupMemberResponse>> =
        apiCall { client.get(Groups.Members(id = groupId)) }

    suspend fun addMember(groupId: String, request: AddMemberRequest): Either<AppError, Unit> =
        apiCall { client.post(Groups.Members(id = groupId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        } }

    suspend fun removeMember(groupId: String, userId: String): Either<AppError, Unit> =
        apiCall { client.delete(Groups.Member(id = groupId, userId = userId)) }
}
```

**Koin registration in `SdkModule.kt`:**

```kotlin
// Addition to existing sdkModule
single { GroupApi(client = get()) }
```

**RBAC integration -- how groups hook into existing auth:**

The existing `withRole(UserRole.PowerAdmin)` pattern guards admin-only routes via JWT claims. Group management introduces a second authorization dimension: group-level roles. The approach:

1. **Route-level:** `authenticate { }` for JWT auth (existing). `withRole(UserRole.Admin)` for platform-wide admin routes (listing all groups, etc.).
2. **Service-level:** Group-specific permission checks (is this user an admin OF THIS GROUP?) happen in `GroupService` using `Raise<DomainError>`. This requires a DB lookup and cannot be done at the route level.
3. **No new Ktor plugin.** The existing `RoleAuthorizationPlugin` handles platform-level roles from JWT claims. Group-level roles are a different concern handled in the service layer.

```kotlin
// server:groups/routes/GroupRoutes.kt
fun Route.groupRoutes(groupService: GroupService) {
    authenticate {
        // Any authenticated user can create a group
        post<Groups.Create> {
            conduitAuth { userId ->
                val request = getModel<CreateGroupRequest>()
                groupService.createGroup(userId, request)
            }
        }

        // Group members can view their group
        get<Groups.ById> { resource ->
            conduitAuth { userId ->
                groupService.getGroup(userId, resource.id)
            }
        }

        // Group admins/owners can add members
        post<Groups.Members> { resource ->
            conduitAuth { userId ->
                val request = getModel<AddMemberRequest>()
                groupService.addMember(userId, resource.id, request)
            }
        }

        // Platform Admin: list ALL groups
        withRole(UserRole.Admin) {
            get<Groups> {
                conduitAuth { _ ->
                    groupService.listAllGroups()
                }
            }
        }
    }
}
```

**Service-layer authorization pattern:**

```kotlin
// server:groups/service/GroupService.kt
class GroupService(
    private val groupRepository: GroupRepository,
    private val memberRepository: GroupMemberRepository,
) {
    context(raise: Raise<DomainError>)
    suspend fun addMember(callerId: String, groupId: String, request: AddMemberRequest) {
        val group = groupRepository.findById(Uuid.parse(groupId))
        raise.ensure(group != null) { GroupNotFound() }

        val callerMembership = memberRepository.findMembership(groupId, callerId)
        raise.ensure(callerMembership != null) { NotGroupMember() }
        raise.ensure(callerMembership.role in setOf("OWNER", "ADMIN")) {
            InsufficientGroupRole()
        }

        val existing = memberRepository.findMembership(groupId, request.userId)
        raise.ensure(existing == null) { AlreadyGroupMember() }

        memberRepository.addMember(groupId, request.userId, request.role)
    }
}
```

**New `AppError` subtypes (additions to `core:models/AppError.kt`):**

```kotlin
@Serializable
sealed class Group : AppError() {
    @Serializable
    data class NotFound(
        override val code: String = "GROUP_NOT_FOUND",
        override val message: String = "Group not found"
    ) : Group()

    @Serializable
    data class NotMember(
        override val code: String = "GROUP_NOT_MEMBER",
        override val message: String = "You are not a member of this group"
    ) : Group()

    @Serializable
    data class InsufficientRole(
        override val code: String = "GROUP_INSUFFICIENT_ROLE",
        override val message: String = "You do not have the required role in this group"
    ) : Group()

    @Serializable
    data class AlreadyMember(
        override val code: String = "GROUP_ALREADY_MEMBER",
        override val message: String = "User is already a member of this group"
    ) : Group()
}
```

**New server-side DomainError types (following `server:auth/errors/AuthErrors.kt` pattern):**

```kotlin
// server:groups/errors/GroupErrors.kt
data class GroupNotFound(val detail: String = "Group not found") : DomainError {
    override fun toAppError(): AppError = AppError.Group.NotFound()
    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        routingContext.notFound(error.code, error.message)
    }
}

data class NotGroupMember(val detail: String = "Not a group member") : DomainError {
    override fun toAppError(): AppError = AppError.Group.NotMember()
    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        routingContext.forbidden(error.code, error.message)
    }
}

data class InsufficientGroupRole(val detail: String = "Insufficient group role") : DomainError {
    override fun toAppError(): AppError = AppError.Group.InsufficientRole()
    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        routingContext.forbidden(error.code, error.message)
    }
}

data class AlreadyGroupMember(val detail: String = "Already a member") : DomainError {
    override fun toAppError(): AppError = AppError.Group.AlreadyMember()
    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        routingContext.unprocessable(error.code, error.message)
    }
}
```

**Admin panel -- `app:admin` feature module:**

New `app:admin` module provides admin UI (user listing, group management). Depends on `core:sdk`, `core:viewmodel`, `app:designsystem`. Only accessible to users with `UserRole.Admin` or higher, gated in navigation:

```kotlin
// In AppNavHost.kt
composable<AdminRoute> {
    val viewModel = koinViewModel<AdminViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    AdminScreen(state = state, onDispatch = viewModel::dispatch)
}
```

Navigation guard in the dashboard or profile screen checks the user's role before showing the admin link.

### 3. Testing Infrastructure -- `core:testing`

**Module:** New `core:testing` KMP library module (commonMain + jvmMain)
**Purpose:** Shared test utilities, fakes, DSLs. Published as a regular library so that test source sets in other modules can use `testImplementation(projects.core.testing)`.

**Why a dedicated module:**
- Currently there are only 2 test files in the entire project, both trivial placeholders (`ComposeAppCommonTest`, `SharedCommonTest`).
- Test utilities (fake implementations, DSLs) would be duplicated across `app:auth/commonTest`, `server:auth/test`, etc. without a shared module.
- `core:testing` provides `testImplementation` dependencies for any module that needs them.

**Module structure:**

```
core/testing/
  src/
    commonMain/kotlin/com/m2f/template/testing/
      fakes/
        FakeTokenStorage.kt       -- in-memory TokenStorage
        FakeAuthApi.kt            -- programmable AuthApi returning Either values
        FakeUserApi.kt            -- programmable UserApi
        FakeGroupApi.kt           -- programmable GroupApi
      viewmodel/
        ViewModelTestDsl.kt       -- DSL for testing MviViewModel
      assertions/
        EitherAssertions.kt       -- kotest matchers for Either<AppError, T>
        StateFlowAssertions.kt    -- kotest matchers for StateFlow values
    jvmMain/kotlin/com/m2f/template/testing/
      server/
        TestServerHelpers.kt      -- Ktor testApplication wrappers
        TestDatabaseHelpers.kt    -- Testcontainers PostgreSQL setup
        TestAuthHelpers.kt        -- JWT token generation for test requests
```

**Interface extraction for testability (modification to `core:sdk`):**

The existing `AuthApi`, `UserApi`, and new `GroupApi` are concrete classes. To create test fakes without mocking libraries, extract interfaces:

```kotlin
// core:sdk - modified AuthApi.kt
interface AuthApiContract {
    suspend fun register(request: RegisterRequest): Either<AppError, AuthResponse>
    suspend fun login(request: LoginRequest, rememberMe: Boolean = true): Either<AppError, AuthResponse>
    suspend fun refresh(request: RefreshTokenRequest): Either<AppError, AuthResponse>
    suspend fun forgotPassword(request: ForgotPasswordRequest): Either<AppError, Unit>
    suspend fun resetPassword(request: ResetPasswordRequest): Either<AppError, Unit>
    suspend fun logout(): Either<AppError, Unit>
}

class AuthApi(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage,
) : AuthApiContract {
    // ... existing implementation unchanged
}
```

ViewModels change their constructor parameter type from `AuthApi` to `AuthApiContract`. Koin binds the concrete class to the interface:

```kotlin
// sdkModule
single<AuthApiContract> { AuthApi(client = get(), tokenStorage = get()) }
```

**Why interfaces, not mocking libraries:**
- MockK has incomplete KMP support (no WASM, limited iOS).
- Mockito is JVM-only.
- Hand-written fakes are portable across all KMP targets, explicit about behavior, and test contract compliance rather than interaction details.

**ViewModel test DSL design:**

```kotlin
package com.m2f.template.testing.viewmodel

import app.cash.turbine.test
import com.m2f.template.viewmodel.MviViewModel
import kotlinx.coroutines.test.runTest

/**
 * DSL for testing MviViewModel subclasses.
 *
 * Usage:
 * ```
 * @Test
 * fun `login dispatches loading then success`() = testViewModel(
 *     viewModel = LoginViewModel(fakeAuthApi)
 * ) {
 *     dispatch(LoginIntent.EmailChanged("user@test.com"))
 *     expectState { email shouldBe "user@test.com" }
 *
 *     dispatch(LoginIntent.Submit)
 *     expectState { isLoading shouldBe true }
 *     expectState { isLoading shouldBe false }
 *
 *     expectEffect<LoginEffect.NavigateToDashboard>()
 * }
 * ```
 */
fun <S, I, E> testViewModel(
    viewModel: MviViewModel<S, I, E>,
    block: suspend ViewModelTestScope<S, I, E>.() -> Unit,
) = runTest {
    ViewModelTestScope(viewModel).block()
}

class ViewModelTestScope<S, I, E>(private val viewModel: MviViewModel<S, I, E>) {
    fun dispatch(intent: I) = viewModel.dispatch(intent)

    suspend fun expectState(assertion: S.() -> Unit) {
        viewModel.state.test {
            awaitItem().assertion()
            cancelAndConsumeRemainingEvents()
        }
    }

    suspend inline fun <reified T : E> expectEffect() {
        viewModel.effects.test {
            val effect = awaitItem()
            check(effect is T) { "Expected ${T::class.simpleName}, got $effect" }
            cancelAndConsumeRemainingEvents()
        }
    }

    fun currentState(): S = viewModel.state.value
}
```

**Fake implementation pattern:**

```kotlin
// core:testing - FakeAuthApi.kt
class FakeAuthApi(
    var loginResult: Either<AppError, AuthResponse> = AuthResponse(
        accessToken = "test-access-token",
        refreshToken = "test-refresh-token",
    ).right(),
    var logoutResult: Either<AppError, Unit> = Unit.right(),
    // ... other defaults
) : AuthApiContract {
    val loginCalls = mutableListOf<LoginRequest>()

    override suspend fun login(request: LoginRequest, rememberMe: Boolean): Either<AppError, AuthResponse> {
        loginCalls.add(request)
        return loginResult
    }

    override suspend fun logout(): Either<AppError, Unit> = logoutResult
    // ... other methods
}
```

**Ktor server test helpers (JVM-only, in jvmMain):**

```kotlin
// Wraps ktor testApplication with standard server config
fun testApp(
    additionalModules: List<Module> = emptyList(),
    block: suspend ApplicationTestBuilder.() -> Unit,
) = testApplication {
    application {
        install(ContentNegotiation) { json() }
        install(Resources)
        install(Koin) {
            modules(testKoinModules + additionalModules)
        }
    }
    block()
}

// JWT token generation for authenticated test requests
fun testJwtToken(
    userId: String = "00000000-0000-0000-0000-000000000001",
    role: UserRole = UserRole.User,
    secret: String = "test-secret",
): String = JWT.create()
    .withSubject(userId)
    .withClaim("role", role.value)
    .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
    .sign(Algorithm.HMAC256(secret))
```

**Build file (`core/testing/build.gradle.kts`):**

```kotlin
plugins {
    id("kmp-library-convention")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            // Published as main so consumers use testImplementation(projects.core.testing)
            api(libs.kotlinx.coroutines)
            api(libs.kotest.assertionsCore)
            api(libs.kotest.arrow)
            api(libs.arrow.core)
            api(libs.turbine)
            implementation(projects.core.models)
            implementation(projects.core.sdk)
            implementation(projects.core.viewmodel)
            implementation(projects.core.storage)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.server.test.host)
            implementation(libs.testcontainers)
            implementation(libs.testcontainers.postgresql)
            implementation(libs.koin.test)
        }
    }
}

android {
    namespace = "com.m2f.template.testing"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
```

**Required addition to `libs.versions.toml`:**

```toml
# Under [versions]
turbine = "1.2.0"

# Under [libraries]
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
```

### 4. Localization -- `core:l10n` + `composeResources`

**Module:** New `core:l10n` KMP library module (pure Kotlin, no Compose dependency)
**Package:** `com.m2f.template.l10n`

**Two-layer localization architecture:**

```
Layer 1: core:l10n (pure Kotlin, no Compose dependency)
  - StringKey enum mapping all localizable strings
  - Default English values (for server-side fallback)
  - Key-to-string resolution function

Layer 2: composeResources in composeApp (Compose-dependent)
  - strings.xml files with translations per locale
  - stringResource(Res.strings.xxx) for UI rendering
  - Bridge function: StringKey -> Res.strings accessor
```

**Why this two-layer approach:**
- `core:l10n` has NO Compose dependency -- it can be used by `server:auth`, `server:groups`, and any JVM-only code for error message resolution.
- Client UI uses Compose Multiplatform's built-in `stringResource()` for automatic locale detection and qualifier-based resolution (verified: Compose Multiplatform 1.10+ supports `composeResources/values-{lang}/strings.xml` with auto-generated `Res.strings` accessors).
- The `StringKey` enum ensures server error codes and client string keys never drift apart.
- Adding a new localizable string is explicit: (1) add to `StringKey` enum, (2) add to `strings.xml` files, (3) update bridge.

**Why centralized strings in `composeApp`, not per feature module:**
- String keys can collide across modules when each module has its own `composeResources`.
- Server error messages must reference the same keys used for client display.
- Translators need a single file to work with, not strings scattered across 5+ modules.
- Feature modules receive strings as parameters from composables in the navigation host.

**Layer 1: `core:l10n` module:**

```kotlin
package com.m2f.template.l10n

/**
 * Exhaustive catalog of all localizable strings in the application.
 * Each entry maps to a <string name="..."> in strings.xml and a server-side English default.
 *
 * Naming convention: DOMAIN_SPECIFIC_KEY
 * - AUTH_* for authentication-related strings
 * - VALIDATION_* for field validation messages
 * - GROUP_* for group management strings
 * - UI_* for general UI labels
 */
enum class StringKey(val code: String, val defaultEn: String) {
    // Auth errors (match existing AppError.code values)
    AUTH_INVALID_CREDENTIALS("auth_invalid_credentials", "Email or password is incorrect"),
    AUTH_TOKEN_EXPIRED("auth_token_expired", "Authentication token has expired"),
    AUTH_UNAUTHORIZED("auth_unauthorized", "Authentication required"),
    AUTH_USER_ALREADY_EXISTS("auth_user_already_exists", "A user with this email already exists"),

    // Validation messages
    VALIDATION_EMAIL_BLANK("validation_email_blank", "Email must not be blank"),
    VALIDATION_EMAIL_FORMAT("validation_email_format", "Email format is invalid"),
    VALIDATION_PASSWORD_LENGTH("validation_password_length", "Password must be at least 8 characters"),
    VALIDATION_NAME_BLANK("validation_name_blank", "Name must not be blank"),
    VALIDATION_NAME_LENGTH("validation_name_length", "Name must be between 2 and 100 characters"),

    // Group errors
    GROUP_NOT_FOUND("group_not_found", "Group not found"),
    GROUP_NOT_MEMBER("group_not_member", "You are not a member of this group"),
    GROUP_INSUFFICIENT_ROLE("group_insufficient_role", "You do not have the required role in this group"),
    GROUP_ALREADY_MEMBER("group_already_member", "User is already a member of this group"),

    // UI labels
    UI_LOGIN_TITLE("ui_login_title", "Sign In"),
    UI_REGISTER_TITLE("ui_register_title", "Create Account"),
    UI_DASHBOARD_TITLE("ui_dashboard_title", "Dashboard"),
    UI_PROFILE_TITLE("ui_profile_title", "Profile"),
    UI_ADMIN_TITLE("ui_admin_title", "Admin Panel"),
    UI_GROUPS_TITLE("ui_groups_title", "Groups"),
    // ... exhaustive list grows with features
}

/**
 * Server-side string resolution (English fallback).
 * Returns the default English string for a given key.
 * Future: accept a locale parameter for server-side i18n.
 */
fun resolveString(key: StringKey): String = key.defaultEn

/**
 * Look up a StringKey by its code string (e.g., from AppError.code).
 * Returns null if no matching key exists.
 */
fun StringKey.Companion.fromCode(code: String): StringKey? =
    entries.find { it.code == code }
```

**Layer 2: Compose resources structure:**

```
composeApp/src/commonMain/composeResources/
  values/
    strings.xml           -- English (default)
  values-es/
    strings.xml           -- Spanish
  values-fr/
    strings.xml           -- French
  drawable/
    compose-multiplatform.xml  -- existing
```

Example `values/strings.xml`:
```xml
<resources>
    <!-- Auth errors -->
    <string name="auth_invalid_credentials">Email or password is incorrect</string>
    <string name="auth_token_expired">Authentication token has expired</string>
    <string name="auth_unauthorized">Authentication required</string>
    <string name="auth_user_already_exists">A user with this email already exists</string>

    <!-- Validation -->
    <string name="validation_email_blank">Email must not be blank</string>
    <string name="validation_email_format">Email format is invalid</string>

    <!-- Group errors -->
    <string name="group_not_found">Group not found</string>
    <string name="group_not_member">You are not a member of this group</string>

    <!-- UI labels -->
    <string name="ui_login_title">Sign In</string>
    <string name="ui_register_title">Create Account</string>
    <string name="ui_dashboard_title">Dashboard</string>
    <string name="ui_profile_title">Profile</string>
    <string name="ui_admin_title">Admin Panel</string>
    <string name="ui_groups_title">Groups</string>
</resources>
```

**Bridge function in composeApp:**

```kotlin
// composeApp/src/commonMain/kotlin/com/m2f/template/l10n/StringResources.kt
package com.m2f.template.l10n

import androidx.compose.runtime.Composable
import com.m2f.template.l10n.StringKey
import org.jetbrains.compose.resources.stringResource
import template.composeapp.generated.resources.Res
import template.composeapp.generated.resources.*

/**
 * Resolves a [StringKey] to a localized string using Compose Multiplatform resources.
 * Falls back to the key's English default if the resource is not found.
 */
@Composable
fun localizedString(key: StringKey): String = when (key) {
    StringKey.AUTH_INVALID_CREDENTIALS -> stringResource(Res.strings.auth_invalid_credentials)
    StringKey.AUTH_TOKEN_EXPIRED -> stringResource(Res.strings.auth_token_expired)
    StringKey.AUTH_UNAUTHORIZED -> stringResource(Res.strings.auth_unauthorized)
    StringKey.AUTH_USER_ALREADY_EXISTS -> stringResource(Res.strings.auth_user_already_exists)
    StringKey.VALIDATION_EMAIL_BLANK -> stringResource(Res.strings.validation_email_blank)
    StringKey.VALIDATION_EMAIL_FORMAT -> stringResource(Res.strings.validation_email_format)
    StringKey.VALIDATION_PASSWORD_LENGTH -> stringResource(Res.strings.validation_password_length)
    StringKey.VALIDATION_NAME_BLANK -> stringResource(Res.strings.validation_name_blank)
    StringKey.VALIDATION_NAME_LENGTH -> stringResource(Res.strings.validation_name_length)
    StringKey.GROUP_NOT_FOUND -> stringResource(Res.strings.group_not_found)
    StringKey.GROUP_NOT_MEMBER -> stringResource(Res.strings.group_not_member)
    StringKey.GROUP_INSUFFICIENT_ROLE -> stringResource(Res.strings.group_insufficient_role)
    StringKey.GROUP_ALREADY_MEMBER -> stringResource(Res.strings.group_already_member)
    StringKey.UI_LOGIN_TITLE -> stringResource(Res.strings.ui_login_title)
    StringKey.UI_REGISTER_TITLE -> stringResource(Res.strings.ui_register_title)
    StringKey.UI_DASHBOARD_TITLE -> stringResource(Res.strings.ui_dashboard_title)
    StringKey.UI_PROFILE_TITLE -> stringResource(Res.strings.ui_profile_title)
    StringKey.UI_ADMIN_TITLE -> stringResource(Res.strings.ui_admin_title)
    StringKey.UI_GROUPS_TITLE -> stringResource(Res.strings.ui_groups_title)
}
```

**Build file (`core/l10n/build.gradle.kts`):**

```kotlin
plugins {
    id("kmp-library-convention")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            // Pure Kotlin -- no Compose, no Arrow, no Ktor
        }
    }
}

android {
    namespace = "com.m2f.template.l10n"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
```

## Component Responsibilities

| Component | Responsibility | Communicates With |
|-----------|---------------|-------------------|
| `core:viewmodel` | MVI base class (`MviViewModel<S,I,E>`) with StateFlow state, Channel effects | Used by all `app:*` feature modules as base class |
| `core:l10n` | String key registry (`StringKey` enum), server-side string resolution | Used by `server:*` for error messages; `composeApp` for bridge function |
| `core:testing` | Test fakes (FakeAuthApi etc), ViewModel test DSL, kotest matchers, Ktor test helpers | `testImplementation` dependency for all modules |
| `core:models` (modified) | Add `GroupDtos`, `GroupRoutes`, `AppError.Group` sealed class | Already central; additions follow existing patterns exactly |
| `core:sdk` (modified) | Add `GroupApi`, extract interfaces (`AuthApiContract`, etc.) for testability | Already central; interface extraction is backward-compatible |
| `server:groups` | GroupsTable, GroupMembersTable, GroupRepository, GroupMemberRepository, GroupService, groupRoutes | Depends on `server:core:*`, `server:auth` (for UsersTable FK), `core:models` |
| `app:admin` | Admin panel UI (user list, group CRUD, member management) | Depends on `core:sdk`, `core:viewmodel`, `app:designsystem` |

## Data Flow

### MVI Unidirectional Data Flow (Client)

```
User Interaction (tap, type, etc.)
      |
      v
  [Intent]  -- sealed interface, dispatched to ViewModel via dispatch()
      |
      v
  [handleIntent()]  -- ViewModel processes intent (suspend)
      |
      +--> [setState(reducer)]  -- atomic update to MutableStateFlow
      |           |
      |           v
      |    [state: StateFlow<S>]
      |           |
      |           v
      |    [collectAsStateWithLifecycle()] in Composable
      |           |
      |           v
      |    Compose UI recomposes
      |
      +--> [sendEffect(effect)]  -- one-shot event via Channel
                  |
                  v
           [effects: Flow<E>]
                  |
                  v
           LaunchedEffect { effects.collect { when(it) { ... } } }
                  |
                  v
           Navigation / Toast / Dialog / etc.
```

### Group Management Request Flow (Full Stack)

```
[Admin UI] -- dispatch(GroupIntent.AddMember(groupId, userId))
    |
    v GroupAdminViewModel.handleIntent()
    |
    v groupApi.addMember(groupId, AddMemberRequest(userId))
    |    |
    |    v apiCall { client.post(Groups.Members(id = groupId)) { setBody(request) } }
    |    |
    |    v HTTP POST /api/groups/{id}/members  (type-safe @Resource)
    |    |
    |    v [Ktor authenticate { }]  -- JWT validation (existing)
    |    |
    |    v [conduitAuth { userId -> }]  -- extracts userId from JWT (existing)
    |    |
    |    v [GroupService.addMember(callerId, groupId, request)]
    |         |
    |         v context(raise: Raise<DomainError>)
    |         v  -- validates caller is group ADMIN/OWNER (DB lookup)
    |         v  -- validates target user is not already a member
    |         v  -- inserts into group_members table
    |
    v Either<AppError, Unit>
    |
    +--> Right: setState { copy(members = members + newMember) }
    +--> Left:  setState { copy(error = it.message) }
```

### Localization Resolution Flow (Client)

```
[Composable UI]
    |
    v stringResource(Res.strings.ui_login_title)
    |    |
    |    v [Compose Multiplatform Resource Library]
    |    |
    |    v Detects device locale (e.g., "es")
    |    |
    |    v Searches composeResources/values-es/strings.xml
    |    |
    |    +--> Found: returns Spanish string "Iniciar Sesion"
    |    +--> Not found: falls back to values/strings.xml (English "Sign In")
    |
    v Returns resolved string to Composable Text()
```

### Localization Resolution Flow (Server)

```
[Server error handler -- DomainError.respond()]
    |
    v DomainError.toAppError() --> AppError with code field
    |
    v ErrorResponse(code = appError.code, message = appError.message)
    |
    v JSON response: {"code": "GROUP_NOT_FOUND", "message": "Group not found"}
    |
    v [Client receives ErrorResponse]
    |
    v [SDK mapHttpError() creates AppError.Client.ServerMapped(code, message)]
    |
    v [ViewModel can use code to look up localized string if needed]:
    v   StringKey.fromCode(error.code)?.let { localizedString(it) } ?: error.message
```

## Modifications to Existing Modules (Explicit)

| Existing Module | Change | Impact |
|-----------------|--------|--------|
| `core:models/AppError.kt` | Add `sealed class Group : AppError()` with 4 subtypes | Additive only -- existing code unaffected |
| `core:models` | Add `dto/GroupDtos.kt`, `routes/GroupRoutes.kt` | Additive only -- new files |
| `core:sdk/api/AuthApi.kt` | Extract `AuthApiContract` interface, class implements it | Backward-compatible (class still exists, just adds interface) |
| `core:sdk/api/UserApi.kt` | Extract `UserApiContract` interface | Same approach |
| `core:sdk/di/SdkModule.kt` | Add `single { GroupApi(client = get()) }`, bind interfaces | 3-4 lines |
| `server/Application.kt` | Add `registerGroupsMigrations()`, `groupRoutes(groupService)` | ~6 lines |
| `server/di/ServerModule.kt` | Add `includes(groupsModule)` | 1 line |
| `composeApp/di/AppModule.kt` | Register admin ViewModels, import new allAppModules | ~5 lines |
| `composeApp/navigation/Routes.kt` | Add `AdminRoute`, `GroupDetailRoute` | ~4 lines |
| `composeApp/navigation/AppNavHost.kt` | Add composable blocks for admin/group screens | ~40 lines |
| `composeApp/build.gradle.kts` | Add `implementation(projects.app.admin)`, `implementation(projects.core.viewmodel)` | 2 lines |
| `settings.gradle.kts` | Include 5 new modules | 5 lines |
| `libs.versions.toml` | Add turbine version and library entry | 2 lines |

## Build Order (Respecting Dependencies)

```
Phase 1: Foundation (no inter-dependencies)
  1a. core:l10n          -- zero dependencies, pure Kotlin
  1b. core:viewmodel     -- depends on lifecycle-viewmodel-compose (already in catalog)
  These can be built in parallel.

Phase 2: Model & SDK changes (depends on Phase 1 only for viewmodel)
  2a. core:models changes -- add group DTOs, routes, AppError.Group
  2b. core:sdk changes    -- add GroupApi, extract AuthApiContract/UserApiContract/GroupApiContract
  2a and 2b are sequential (2b depends on 2a for GroupDtos).

Phase 3: Testing infrastructure (depends on Phases 1 + 2)
  3.  core:testing        -- depends on core:viewmodel, core:models, core:sdk (for interface fakes)

Phase 4: Server groups (depends on Phase 2 for core:models)
  4.  server:groups       -- tables, repository, service, routes, migrations, Koin module
  4 can run in parallel with Phase 3.

Phase 5: Client features (depends on Phases 1-4)
  5a. app:admin           -- depends on core:viewmodel, core:sdk (with GroupApi)
  5b. Migrate existing VMs -- LoginViewModel, ProfileViewModel to MviViewModel (incremental)
  5c. Localization files  -- strings.xml in composeApp, wire up bridge function

Phase 6: Integration + Wiring
  6.  Wire new modules in Application.kt, AppNavHost.kt, settings.gradle.kts
  6.  Write tests using core:testing DSL
```

**Phase ordering rationale:**
- Phase 1 first because core:viewmodel and core:l10n are leaf dependencies with no external requirements.
- Phase 2 before 3 because core:testing needs the interfaces extracted in 2b.
- Phase 3 before 5 because feature development should have test infrastructure ready.
- Phase 4 is independent of client work and can run in parallel with Phase 3.
- Phase 5 last because it consumes everything: core:viewmodel for base class, core:sdk for GroupApi, core:testing for tests.

## Anti-Patterns

### Anti-Pattern 1: SharedFlow for One-Shot Events

**What people do:** Use `MutableSharedFlow(replay=1)` or `MutableStateFlow<Event?>` for navigation events.
**Why it's wrong:** SharedFlow with replay re-emits events on configuration change / recomposition. StateFlow requires manual null-resetting after consumption, creating race conditions.
**Do this instead:** Use `Channel<E>(Channel.BUFFERED)` converted to Flow via `receiveAsFlow()`. Each event is consumed exactly once. The MviViewModel base class provides this via `sendEffect()` / `effects`.

### Anti-Pattern 2: Route-Level Database Lookups for Group Authorization

**What people do:** Create a Ktor route-scoped plugin that queries the database to check group membership (similar to `RoleAuthorizationPlugin`).
**Why it's wrong:** The existing `RoleAuthorizationPlugin` works because it reads from JWT claims (stateless, no DB). Group membership requires a DB lookup. Adding DB access to route plugins creates tight coupling, makes unit testing harder, and can cause unexpected coroutine context issues with `AuthenticationChecked` hooks.
**Do this instead:** Use `withRole()` for platform-level role checks (from JWT claims). Use service-layer checks with `Raise<DomainError>` for resource-specific authorization (group membership, ownership).

### Anti-Pattern 3: Scattering Strings Across Feature Module composeResources

**What people do:** Each `app:auth`, `app:dashboard`, `app:profile` has its own `composeResources/values/strings.xml`.
**Why it's wrong:** String key collisions across modules. Server error messages cannot reference the same keys. No single inventory for translators. Compose resource generation creates separate `Res` classes per module, making cross-module string sharing awkward.
**Do this instead:** Centralize strings in `composeApp/src/commonMain/composeResources/` and use `core:l10n` `StringKey` enum as the canonical registry. Feature modules receive localized strings as parameters from composables in the navigation host.

### Anti-Pattern 4: Testing ViewModels with Mocking Libraries

**What people do:** Use MockK or Mockito to mock AuthApi, UserApi in ViewModel tests.
**Why it's wrong:** MockK has incomplete multiplatform support (no WASM, limited iOS). Mockito is JVM-only. Mocks verify implementation details (call order, argument matchers) rather than behavior.
**Do this instead:** Extract interfaces from SDK API classes, create hand-written fakes in `core:testing`. Fakes are portable across all KMP targets, predictable, and test behavior not interactions.

### Anti-Pattern 5: Making core:l10n Depend on Compose

**What people do:** Put the `localizedString()` composable function in `core:l10n` so it can be used everywhere.
**Why it's wrong:** This would make `core:l10n` depend on Compose runtime, which means server modules cannot use it. The whole point of a separate `core:l10n` is that it's pure Kotlin.
**Do this instead:** `core:l10n` contains only the `StringKey` enum and `resolveString()`. The Compose bridge function lives in `composeApp` where it has access to `Res.strings`.

## Integration Points

### New Module Dependencies on Existing Modules

| New Module | Depends On | What It Uses |
|------------|-----------|--------------|
| `core:viewmodel` | `lifecycle-viewmodel-compose` 2.9.6, `kotlinx-coroutines` 1.10.2 | ViewModel base class, StateFlow, Channel |
| `core:l10n` | (none -- pure Kotlin) | String key enum and resolution function only |
| `core:testing` | `core:models`, `core:sdk`, `core:viewmodel`, `core:storage`, `kotest` 6.1.3, `turbine` 1.2.0, `arrow-core` 2.2.1.1 | Types for fakes, assertion helpers |
| `server:groups` | `server:core:config`, `server:core:database`, `server:auth` (for UsersTable), `core:models` | DomainError, conduit helpers, Exposed R2DBC, DTOs |
| `app:admin` | `core:sdk`, `core:viewmodel`, `app:designsystem`, `core:models` | GroupApi, MviViewModel, UI components, DTOs |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| `core:viewmodel` -> `app:*` | Inheritance (MviViewModel base class) | Feature VMs extend MviViewModel. Base class has zero framework opinion. |
| `core:l10n` -> `server:*` | Function call (`resolveString(key)`) | Server uses English defaults. No Compose dependency. |
| `core:l10n` -> `composeApp` | Bridge function in composeApp maps StringKey -> Res.strings | Bridge lives in composeApp, not in core:l10n. |
| `core:testing` -> `**/test` | `testImplementation` dependency | Fakes, DSL, assertions. No production code depends on core:testing. |
| `server:groups` -> `server:auth` | Table reference (UsersTable.id for FK) | One-directional. server:auth does not know about server:groups. |
| `server:groups` -> `server/Application.kt` | Route installation, migration registration | Same pattern as server:auth and server:ai. |
| `app:admin` -> `composeApp` | Navigation route + composable block | Admin screens wired in AppNavHost.kt like existing screens. |

## Sources

- [Kotlin Multiplatform Common ViewModel](https://kotlinlang.org/docs/multiplatform/compose-viewmodel.html) -- HIGH confidence, verified current
- [Compose Multiplatform Localizing Strings](https://kotlinlang.org/docs/multiplatform/compose-localize-strings.html) -- HIGH confidence, verified structure
- [Compose Multiplatform Resources Overview](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources.html) -- HIGH confidence, verified current
- [Koin for Compose Multiplatform](https://insert-koin.io/docs/reference/koin-compose/compose/) -- HIGH confidence
- [Koin Compose ViewModel + Navigation](https://insert-koin.io/docs/reference/koin-compose/navigation3/) -- HIGH confidence
- [JetBrains Exposed GitHub](https://github.com/JetBrains/Exposed) -- HIGH confidence (confirmed R2DBC uses DSL only, no DAO)
- [Orbit MVI](https://orbit-mvi.org/) -- MEDIUM confidence (evaluated and decided against; external dependency not justified for ~30 lines of base class)
- [MVIKotlin](https://arkivanov.github.io/MVIKotlin/) -- MEDIUM confidence (evaluated; more suited for Decompose-based projects, not Koin+Navigation Compose)
- [Compose Multiplatform Resources Setup](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-setup.html) -- HIGH confidence
- [Koin 4.1 Release](https://blog.kotzilla.io/koin-4.1-is-here) -- MEDIUM confidence (features verified against project's Koin 4.1.1)

---
*Architecture research for: KMP Full-Stack Template v2 -- MVI, Groups, Testing, Localization*
*Researched: 2026-02-17*
