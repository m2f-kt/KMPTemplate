# CLAUDE.md

## Project Overview

Kotlin Multiplatform (KMP) + Compose Multiplatform application with a Ktor backend.
Terminal-themed design system. Targets: Android, iOS, JVM Desktop, WASM.
Architecture: MVI (Model-View-Intent) on the client side, feature modules on the server side.

## Module Structure

- `composeApp` -- Main Compose app entry point (all platform targets). Depends on wire modules only.
- `shared` -- DI aggregator (storageModule + sdkModule)
- `core:models` -- Shared DTOs, AppError hierarchy, @Resource routes, StringKey
- `core:mvi` -- MviViewModel<Intent, Model, Mutation, Event> base class
- `core:navigation` -- `Route` interface (extends NavKey). All feature contracts depend on this.
- `core:testing` -- ViewModelTest base, test{} DSL with Turbine, FakeSdkBuilder
- `core:sdk` -- Sdk facade (by delegation), 6 API interfaces, apiCall() wrapper, AuthInterceptor
- `core:storage` -- TokenStorage, PreferencesStorage (multiplatform-settings)
- `app:auth`, `app:admin`, `app:dashboard`, `app:documents`, `app:profile` -- Client feature modules (each has contract/impl/wire submodules)
- `app:designsystem` -- TerminalTheme, Foundation-only UI components
- `server` -- Ktor server entry point
- `server:core:config`, `server:core:database`, `server:core:security` -- Server infrastructure
- `server:auth`, `server:groups`, `server:files`, `server:ai` -- Server feature modules

### App Feature Submodules (contract/impl/wire)

- **contract** -- Routes (extend `Route` from `core:navigation`), shared types. `api(core:navigation)`.
- **impl** -- ViewModel, MVI types, Screen composable, tests. Hidden: only wire sees it.
- **wire** -- `implementation(impl)` (impl truly hidden), `api(contract)`. Provides Koin module + `EntryProviderScope<Route>` navigation extension.

## Key Conventions

- All API calls return `Either<AppError, T>` (Arrow).
- Server uses `context(raise: Raise<DomainError>)` for error handling.
- Kotest assertions (`shouldBe`, `shouldBeRight`, etc.) -- never JUnit assertions.
- MVI strict order: Intent -> take() -> sendMutation/sendEvent -> reduce() -> Model.
- TDD workflow: Define types -> Write tests -> Implement -> Wire Koin -> Verify.
- Tests use `viewModel.test { intent(...); model(...); event(...) }` DSL.
- Fake SDK: `fakeSdk { auth { login { ... } } }` -- defaults to failure.
- Sdk facade uses Kotlin `by` delegation.
- All DTOs are `@Serializable`.
- Routes use Ktor `@Resource` for type-safe routing.
- Server Koin modules: `authModule`, `groupModule`, `fileModule`, `aiModule` -> `serverModule`.
- App Koin: `includes(featureModule)` in `appModule`. Wire modules provide `val xxxModule`.
- Navigation 3 with manual `mutableStateListOf<Route>` back stack. Wire modules provide `EntryProviderScope<Route>` extensions called in AppNavHost.
- Design system: `TerminalTheme` with Foundation-only components (no Material3).
- Responsive layouts: `BoxWithConstraints` with 840.dp breakpoint.
- Compose screens: callbacks pattern (no direct ViewModel access in composables).

## Common Commands

```
./gradlew devSetup             # First-time setup (check prerequisites + start Docker)
./gradlew devUp                # Start Docker services (Postgres, MinIO, MailHog)
./gradlew :server:run          # Start backend server
./gradlew :server:test         # Run server tests
./gradlew :app:<feature>:allTests  # Run client feature tests
./gradlew testAll              # Run all tests
./gradlew detekt               # Run static analysis
./gradlew koverHtmlReport      # Code coverage report
```

## Server Pattern (per feature module)

- `routes/` -- Ktor route definitions with `conduit`/`conduitAuth` helpers
- `service/` -- Business logic with Arrow Raise context
- `repository/` -- Exposed R2DBC with suspend functions and Record types
- `tables/` -- Exposed table definitions
- `di/` -- Koin module (`single { Repository(get()) }`, `single { Service(get(), get()) }`)
- Migrations registered via `registerXxxMigrations()` in module entry file

## Client Pattern (per app feature module)

Each feature has 3 submodules:

**contract/** -- Routes + shared types
- `XxxRoute.kt` -- `@Serializable data object/class XxxRoute : Route`

**impl/** -- Hidden internals (only wire can see)
- `XxxModel.kt` -- data class with defaults
- `XxxIntent.kt` -- sealed interface for user actions
- `XxxMutation.kt` -- sealed interface for state transitions
- `XxxEvent.kt` -- sealed interface for navigation/side effects
- `XxxViewModel.kt` -- extends MviViewModel, `take()` + `reduce()`
- `XxxScreen.kt` -- Composable with callbacks, responsive layout
- `XxxViewModelTest.kt` -- Uses ViewModelTest base + test{} DSL

**wire/** -- DI + Navigation bridge
- `XxxModule.kt` -- Koin module with `viewModelOf`
- `XxxNavigation.kt` -- `EntryProviderScope<Route>` extension encapsulating ViewModel/Screen wiring

## Infrastructure

- Docker: Postgres (pgvector, port 5436), MinIO (ports 9002/9003), MailHog (ports 1025/8025)
- Server port: 8080
- `.env` for secrets (NEVER edit `.env`, use `.env.example` as reference)
- `buildSrc` conventions: `kmp-library-convention`, `server-module-convention`, `kover-convention`
- Context parameters enabled: `-Xcontext-parameters`
