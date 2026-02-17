# Stack Research

**Domain:** KMP Full-Stack Template -- Milestone 2: MVI ViewModel, Groups/Admin, Testing, Localization
**Researched:** 2026-02-17
**Confidence:** HIGH (most choices verified against official docs, existing project dependencies, and Maven Central)

---

## Existing Stack (Already in Place -- Do NOT Add)

These are already integrated and confirmed in `gradle/libs.versions.toml`. Listed for context to prevent duplication.

| Technology | Version | Purpose |
|---|---|---|
| Kotlin | 2.3.10 | Language (context parameters enabled via `-Xcontext-parameters`) |
| Compose Multiplatform | 1.10.1 | Shared UI framework (Android, iOS, Desktop, WASM) |
| Ktor Server | 3.4.0 | Backend HTTP framework |
| Ktor Client | 3.4.0 | Multiplatform HTTP client |
| Exposed | 1.0.0 | Database ORM (R2DBC + JDBC) |
| PostgreSQL + R2DBC | 42.7.9 / 1.0.7.RELEASE | Database drivers |
| Koin | 4.1.1 | DI across server + all KMP targets |
| Arrow | 2.2.1.1 | FP (core/fx/resilience), context parameter Raise |
| Kotest assertions | 6.1.3 | `kotest-assertions-core`, `kotest-assertions-arrow`, `kotest-assertions-arrow-fx-coroutines` |
| Testcontainers | 2.0.3 | Server integration testing (PostgreSQL containers) |
| kotlinx-coroutines | 1.10.2 | Async runtime |
| kotlinx-serialization | 1.10.0 | JSON serialization |
| AndroidX Lifecycle ViewModel | 2.9.6 | `lifecycle-viewmodel-compose` + `lifecycle-runtime-compose` (KMP) |
| Koin Compose ViewModel | 4.1.1 | `koin-compose-viewmodel` for ViewModel injection |
| Navigation Compose | 2.9.2 | Multiplatform navigation |
| Kermit | 2.0.8 | Multiplatform logging |
| compose.components.resources | (bundled with CMP 1.10.1) | Font loading, drawable resources |
| Ktor Server Test Host | 3.4.0 | `ktor-server-test-host` for testApplication |
| kotlin-test | 2.3.10 | Base test assertions |
| Kover | 0.9.7 | Code coverage |
| Detekt | 1.23.8 | Static analysis |

**Key observation:** The project has already upgraded significantly since the v1.0 research (Kotlin 2.3.10, CMP 1.10.1, Ktor 3.4.0, Exposed 1.0.0, Arrow 2.2.1.1). The `testing-server` bundle already includes kotest-assertions-core, kotest-assertions-arrow, kotest-assertions-arrow-fx, ktor-server-test-host, testcontainers, and koin-test.

---

## Recommended Stack (New Additions for Milestone 2)

### 1. MVI ViewModel Infrastructure

**No new library dependencies needed.** The MVI ViewModel base class uses only existing dependencies.

| What Exists | Version | How It Serves MVI |
|---|---|---|
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.9.6 | KMP `ViewModel` base class with `viewModelScope` -- already used by LoginViewModel, ProfileViewModel, DashboardViewModel |
| `arrow-core` | 2.2.1.1 | `Either<L, R>` for typed error handling in mutations; `Raise` context for error composition |
| `kotlinx-coroutines` | 1.10.2 | `StateFlow`, `MutableStateFlow`, `Channel` for events |
| `koin-compose-viewmodel` | 4.1.1 | `koinViewModel<T>()` injection in composables |

**Pattern to implement (no new deps):**
```kotlin
abstract class MviViewModel<Intent, Model, Mutation, Event>(
    initialModel: Model,
) : ViewModel() {
    private val _model = MutableStateFlow(initialModel)
    val model: StateFlow<Model> = _model.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    fun dispatch(intent: Intent) {
        viewModelScope.launch {
            process(intent).collect { mutation ->
                _model.update { reduce(it, mutation) }
            }
        }
    }

    protected abstract fun process(intent: Intent): Flow<Mutation>
    protected abstract fun reduce(model: Model, mutation: Mutation): Model
    protected fun emit(event: Event) { viewModelScope.launch { _events.send(event) } }
}
```

**Why no new library:** The existing `ViewModel` from AndroidX Lifecycle 2.9.6 already supports all KMP targets. The MVI pattern is a structural concern, not a library concern. Adding third-party MVI libraries (Orbit MVI, MVIKotlin, etc.) would add unnecessary dependencies to a template project. The base class is ~30 lines of code using `StateFlow`, `Channel`, and `ViewModel` -- all already in the project.

### 2. Testing Infrastructure -- New Additions

#### 2a. Turbine (Flow Testing)

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| Turbine | 1.2.1 | Flow testing in `commonTest` and JVM test | The standard library for testing `StateFlow`/`Flow` emissions. Provides `test {}` extension, `awaitItem()`, `awaitComplete()`, `expectNoEvents()`. Essential for testing MVI ViewModel model emissions. KMP multiplatform: supports JVM, JS, wasmJs, iOS, macOS, Linux, Windows targets. | HIGH |

**Gradle dependency:**
```kotlin
// In libs.versions.toml
turbine = "1.2.1"
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
```

**Where to add:**
- `commonTest.dependencies` in KMP app modules (app:auth, app:dashboard, app:profile, app:groups) -- for ViewModel flow testing
- `testImplementation` in server modules -- for testing Flow-based services

**Usage with MVI ViewModel:**
```kotlin
@Test
fun `login intent emits loading then success`() = runTest {
    val vm = LoginMviViewModel(fakeAuthApi)
    vm.model.test {
        assertThat(awaitItem()).isEqualTo(LoginModel()) // initial
        vm.dispatch(LoginIntent.Submit("a@b.com", "pass"))
        assertThat(awaitItem().isLoading).isTrue()
        assertThat(awaitItem().loginSuccess).isTrue()
    }
}
```

#### 2b. kotlinx-coroutines-test (Test Dispatcher)

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| kotlinx-coroutines-test | 1.10.2 | `runTest`, `TestDispatcher`, `advanceUntilIdle` | Required for deterministic coroutine testing. Provides `runTest {}` which auto-advances virtual time for `delay()` calls. Already using coroutines 1.10.2 at runtime; this is the test counterpart. KMP multiplatform. | HIGH |

**Gradle dependency:**
```kotlin
// In libs.versions.toml (version matches existing kotlinx-coroutines)
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }
```

**Where to add:**
- `commonTest.dependencies` in KMP modules -- for ViewModel/repository tests
- `testImplementation` in server modules -- for coroutine-based service tests

#### 2c. Kotest Assertions Multiplatform (Already Partially Present)

The `kotest-assertionsCore` (6.1.3) is already in the `testing-server` bundle but only available in server `testImplementation`. For KMP module testing, we need it in `commonTest`.

**Where to add (no version change):**
```kotlin
// In KMP app module build.gradle.kts
commonTest.dependencies {
    implementation(libs.kotest.assertionsCore)  // already in version catalog at 6.1.3
    implementation(libs.kotest.arrow)            // already in version catalog at 6.1.3
}
```

**Note:** `kotest-assertions-core` at version 6.1.3 is KMP multiplatform (supports JVM, JS, wasmJs, Native). It works in `commonTest` out of the box. The `kotest-assertions-arrow` module (also at 6.1.3 under `io.kotest` group, not the old `io.kotest.extensions` group) provides `shouldBeRight()`, `shouldBeLeft()`, `shouldBeSome()` matchers.

#### 2d. Testing Bundle Updates

**Existing `testing-server` bundle (no changes needed):**
```toml
testing-server = [
    "ktor-server-test-host",
    "kotlin-testJunit",
    "testcontainers",
    "testcontainers-postgresql",
    "koin-test",
    "koin-test-junit4",
    "kotest-assertionsCore",
    "kotest-arrow",
    "kotest-arrow-fx",
]
```

**New bundle to add -- `testing-kmp` for multiplatform commonTest:**
```toml
[libraries]
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }

[bundles]
testing-kmp = [
    "kotlin-test",
    "kotest-assertionsCore",
    "kotest-arrow",
    "turbine",
    "kotlinx-coroutines-test",
]
```

### 3. Group Entity Management (Server-Side)

**No new library dependencies needed.** Server-side group CRUD uses the same stack as existing user/auth management.

| What Exists | Version | How It Serves Groups |
|---|---|---|
| Exposed (core, r2dbc, dao, datetime) | 1.0.0 | Table definitions for `GroupsTable`, `GroupMembershipsTable` |
| Ktor Server (core, resources, auth, content-negotiation) | 3.4.0 | REST routes for group CRUD, admin endpoints |
| Arrow core + fx | 2.2.1.1 | `Either`-based service layer, `Raise` context for error handling |
| Koin (ktor, core) | 4.1.1 | DI for GroupService, GroupRepository |
| Ktor Resources | 3.4.0 | Type-safe `@Resource` routes (already used for Users routes) |
| RBAC (withRole plugin) | existing | `withRole(UserRole.Admin)` for admin-only group management routes |

**New module recommended:** `server:groups` following the same convention as `server:auth`.

**Build file pattern (mirrors server:auth):**
```kotlin
plugins {
    id("server-module-convention")
}
group = "com.m2f.server"
dependencies {
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(projects.server.core.security)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.server.resources)
    implementation(libs.bundles.di)
    testImplementation(libs.bundles.testing.server)
}
```

**New table definitions needed (Exposed 1.0.0):**
- `GroupsTable` -- id (uuid), name, description, createdAt, createdBy
- `GroupMembershipsTable` -- groupId, userId, role (owner/admin/member), joinedAt

**New shared models needed (in core:models):**
- `Group` data class
- `GroupMembership` data class
- `GroupRole` enum (Owner, Admin, Member)
- `GroupRoutes` resource definitions (matching existing `Users` pattern)

### 4. Localization System

#### 4a. Client-Side Localization (Compose Multiplatform Resources)

**No new library dependencies needed.** The built-in Compose Multiplatform resources system (`compose.components.resources`, already in `app/designsystem` and `composeApp`) handles client-side i18n.

| What Exists | Version | How It Serves Localization |
|---|---|---|
| `compose.components.resources` | bundled with CMP 1.10.1 | `stringResource(Res.strings.key)`, auto-generated `Res` class, locale-qualified `values-xx/strings.xml` directories |
| Compose resource qualifiers | CMP 1.10.1 | Language qualifier (`values-es`, `values-fr`), theme qualifier, density qualifier |

**Setup needed (configuration, not new deps):**
```
app/designsystem/src/commonMain/composeResources/
  values/strings.xml          -- default (English)
  values-es/strings.xml       -- Spanish
  values-fr/strings.xml       -- French
  ...
```

**Access pattern:**
```kotlin
import template.app.designsystem.generated.resources.Res
import template.app.designsystem.generated.resources.login_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen() {
    Text(stringResource(Res.strings.login_button))
}
```

**Locale switching:** Requires `CompositionLocalProvider` with platform `expect/actual` for locale management. No library needed -- it's built into the Compose resource environment system.

#### 4b. Server-Side i18n

**No third-party library recommended.** Ktor has no mature first-party i18n plugin. The best approach for server-side localization is a lightweight custom solution.

| Approach | Implementation | Why |
|---|---|---|
| Custom `I18n` object with `.properties` files | Load `messages_en.properties`, `messages_es.properties` etc. via `java.util.ResourceBundle` or a simple map | Simple, no dependencies, matches JVM standard practices. The server only needs i18n for error messages and API response strings -- not full UI localization. |
| `Accept-Language` header parsing | `call.request.acceptLanguageItems()` (built into Ktor 3.4.0) | Already available, returns quality-sorted language preferences. No plugin needed. |

**Why NOT use third-party i18n libraries:**
- `aymanizz/ktor-i18n` -- last updated for Ktor 2.0, incompatible with Ktor 3.x. Community project, not maintained.
- `i18n4k` -- adds a code generator and build plugin. Overkill for server-side error messages. Adds build complexity.
- Compose Resources on server -- technically possible but drags in Compose dependencies onto the JVM server module. Inappropriate coupling.

**Shared key approach:** Define a `StringKey` enum in `core:models` that both client and server reference. The client maps keys to `Res.strings.*` via Compose resources. The server maps keys to `.properties` bundles. This keeps the localization contract type-safe across client and server without coupling their resource loading mechanisms.

```kotlin
// core:models (shared)
enum class StringKey {
    ERROR_UNAUTHORIZED,
    ERROR_NOT_FOUND,
    ERROR_VALIDATION_FAILED,
    GROUP_CREATED,
    GROUP_DELETED,
    // ...
}
```

---

## Version Catalog Additions (New Entries Only)

```toml
[versions]
turbine = "1.2.1"

[libraries]
# Testing (NEW)
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }

[bundles]
# NEW bundle for KMP commonTest
testing-kmp = [
    "kotlin-test",
    "kotest-assertionsCore",
    "kotest-arrow",
    "turbine",
    "kotlinx-coroutines-test",
]
```

**That is the ENTIRE list of new version catalog entries.** Everything else needed (Exposed tables, Ktor routes, Arrow Either, Koin DI, Compose resources, ViewModel lifecycle) uses existing dependencies already in the catalog.

---

## Version Compatibility Matrix

| New Addition | Compatible With | Notes |
|---|---|---|
| Turbine 1.2.1 | Kotlin 2.3.10, kotlinx-coroutines 1.10.2 | Turbine depends on kotlinx-coroutines. Version 1.2.1 works with coroutines 1.7+. No Kotlin version constraint beyond standard stdlib compatibility. Supports wasmJs target (added in 1.1.0). |
| kotlinx-coroutines-test 1.10.2 | Kotlin 2.3.10, kotlinx-coroutines 1.10.2 | Same artifact group/version as the runtime coroutines. Must match exactly. Already at 1.10.2. |
| Kotest 6.1.3 in commonTest | Kotlin 2.3.10, all KMP targets | Kotest 6.1.x supports Kotlin 2.0-2.3. Multiplatform artifacts exist for assertions-core. Already confirmed working in project (used in server tests). |

---

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|---|---|---|---|
| MVI Framework | Custom base class (~30 LOC) | Orbit MVI | Adds unnecessary dependency. Orbit is great for complex apps but overkill for a template. The MVI pattern is trivial to implement with StateFlow + Channel. |
| MVI Framework | Custom base class | MVIKotlin (Arkadii Ivanov) | Brings its own store/lifecycle model that conflicts with AndroidX ViewModel. Over-engineered for this use case. |
| MVI Framework | Custom base class | Decompose | Full lifecycle framework. Already decided against in v1.0 research. Template uses AndroidX Lifecycle + Navigation. |
| Flow Testing | Turbine 1.2.1 | Manual `toList()` / `first()` | Fragile, timing-dependent, no timeout handling. Turbine is the industry standard for Flow testing in Kotlin. |
| Flow Testing | Turbine 1.2.1 | kotlinx-coroutines-test only | `runTest` + `TestScope` works for simple cases but lacks Turbine's `awaitItem()` / `expectNoEvents()` semantics for StateFlow testing. |
| Client i18n | Compose Multiplatform Resources | Moko Resources | Third-party library that predates official CMP resource support. CMP 1.10.1 has stable, built-in string resources with locale qualifiers. No reason to add a third-party alternative. |
| Client i18n | Compose Multiplatform Resources | i18n4k | Adds a code generator build plugin. Compose Resources already generates `Res` class with type-safe string accessors. Redundant. |
| Server i18n | java.util.ResourceBundle / custom | ktor-i18n (aymanizz) | Unmaintained, Ktor 2.x only, not compatible with Ktor 3.4.0. |
| Server i18n | java.util.ResourceBundle / custom | Spring MessageSource | Wrong ecosystem. This is Ktor. |
| Mocking (KMP) | Manual fakes / interfaces | MockK | MockK does NOT support Kotlin/Native, wasmJs, or iOS targets. Cannot be used in `commonTest`. Manual fakes are the recommended KMP testing pattern. |
| Mocking (KMP) | Manual fakes | Mockative / MocKMP | KSP-based mocking frameworks add build complexity and code generation overhead. For a template, manual fakes are clearer and demonstrate good testing patterns. |

---

## What NOT to Add

| Avoid | Why | Use Instead |
|---|---|---|
| Orbit MVI / MVIKotlin | Over-engineered for a template. MVI is ~30 lines of code with StateFlow + Channel. Adding a framework obscures the pattern. | Custom `MviViewModel` base class |
| MockK for KMP tests | No Kotlin/Native, wasmJs, or iOS support. Only works in JVM tests. | Manual fakes implementing interfaces. MockK is fine in server `test/` (JVM-only) if needed, but don't add it to `commonTest`. |
| Moko Resources | Third-party; Compose Multiplatform 1.10.1 has built-in localization. Moko predates the official solution. | `compose.components.resources` (already in project) |
| i18n4k | Adds a Gradle plugin for code generation. Compose Resources already code-generates `Res.strings.*`. | Compose Resources for client, ResourceBundle for server |
| ktor-i18n | Unmaintained, Ktor 2.x only. | Custom `Accept-Language` parsing with Ktor's built-in `acceptLanguageItems()` |
| Room KMP for groups | Overkill for server-side. The server uses Exposed. Client doesn't need local group storage. | Exposed 1.0.0 (server), SDK API calls (client) |
| SQLDelight for client | No client-side database needed. Groups are fetched from server via SDK. | Ktor Client + Arrow Either (already in project) |
| New Ktor plugins for admin | Existing RBAC (`withRole`) already handles admin routes. No new auth plugin needed. | Existing `withRole(UserRole.Admin)` / `withRole(UserRole.PowerAdmin)` |

---

## Stack Patterns by Feature

### If implementing MVI ViewModel:
- Use `ViewModel` from `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.6` (already present)
- Use `StateFlow<Model>` for UI state, `Channel<Event>` for one-shot events
- Test with Turbine's `model.test { awaitItem() }` in `commonTest`
- Inject via `koinViewModel<MyMviViewModel>()` (already present)
- No new dependencies needed

### If implementing Groups server module:
- Mirror `server:auth` module structure (convention plugin, same deps)
- Tables: `GroupsTable`, `GroupMembershipsTable` in Exposed 1.0.0
- Routes: Type-safe `@Resource` classes in `core:models`
- Service layer: `context(Raise<DomainError>)` following existing Arrow pattern
- Admin routes: `withRole(UserRole.Admin) { ... }` (existing RBAC)
- No new dependencies needed

### If implementing Testing infrastructure:
- Add Turbine 1.2.1 and kotlinx-coroutines-test 1.10.2 to version catalog
- Create `testing-kmp` bundle for commonTest
- Server tests: use existing `testing-server` bundle (unchanged)
- KMP tests: use new `testing-kmp` bundle in commonTest
- Write manual fakes for interfaces (no mocking library in commonTest)

### If implementing Localization:
- Client: Add `values/strings.xml` files under `composeResources/` in `app/designsystem`
- Client: Use `stringResource(Res.strings.key)` in composables
- Server: Create `I18n` utility with `.properties` files per locale
- Server: Parse `Accept-Language` via Ktor's built-in `call.request.acceptLanguageItems()`
- Shared: `StringKey` enum in `core:models` for type-safe key references
- No new library dependencies needed for either client or server

---

## Sources

### HIGH Confidence (Official docs, releases, verified against project)
- [Turbine GitHub](https://github.com/cashapp/turbine) -- Version 1.2.1, KMP multiplatform support (JVM, JS, wasmJs, Native)
- [Turbine Releases](https://github.com/cashapp/turbine/releases) -- 1.2.1 released Jun 2024, wasmJs added in 1.1.0
- [Kotest Official Docs](https://kotest.io/docs/assertions/arrow.html) -- Arrow assertions module (io.kotest group, matches kotest version)
- [Kotest Releases](https://github.com/kotest/kotest/releases) -- v6.1.3 (Feb 2025), supports Kotlin 2.0-2.3
- [Compose Multiplatform Resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources.html) -- String localization, locale qualifiers
- [Compose Localize Strings](https://kotlinlang.org/docs/multiplatform/compose-localize-strings.html) -- stringResource(), Res.strings, values-xx directories
- [Compose Resource Environment](https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html) -- Programmatic locale switching via CompositionLocal
- [Ktor Testing Docs](https://ktor.io/docs/server-testing.html) -- testApplication {}, test client configuration
- [Ktor acceptLanguageItems](https://api.ktor.io/ktor-server/ktor-server-core/io.ktor.server.request/accept-language-items.html) -- Built-in Accept-Language parsing
- [AndroidX ViewModel KMP](https://developer.android.com/kotlin/multiplatform/viewmodel) -- ViewModel 2.8.0+ supports KMP, confirmed at 2.9.6
- [JetBrains Common ViewModel](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-viewmodel.html) -- lifecycle-viewmodel-compose multiplatform
- [kotlinx-coroutines-test Docs](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/) -- runTest, TestDispatcher, advanceUntilIdle

### MEDIUM Confidence (Multiple sources agree)
- [KMP Testing Guide 2025](https://www.kmpship.app/blog/kotlin-multiplatform-testing-guide-2025) -- Turbine + Kotest + coroutines-test as standard KMP test stack
- [MockK KMP Limitations](https://github.com/mockk/mockk) -- Confirmed no Kotlin/Native support, manual fakes recommended for KMP

### LOW Confidence (Needs validation during implementation)
- Turbine 1.2.1 precise compatibility with Kotlin 2.3.10 -- likely fine (no Kotlin version constraint documented), but latest Turbine release predates Kotlin 2.3. If issues arise, 1.3.0-SNAPSHOT is available.
- Kotest 6.1.3 `kotest-assertions-arrow` in wasmJs `commonTest` -- multiplatform artifacts exist but wasmJs target specifically may have edge cases.

---

*Stack research for: KMP Full-Stack Template -- Milestone 2 (MVI, Groups, Testing, Localization)*
*Researched: 2026-02-17*
