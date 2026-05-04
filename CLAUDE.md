# CLAUDE.md

## Workflow Orchestration

### 1. Plan Node Default

- Enter plan mode for ANY non-trivial task (3+ steps or architectural decisions)
- If something goes sideways, STOP and re-plan immediately - don't keep pushing
- Use plan mode for verification steps, not just building
- Write detailed specs upfront to reduce ambiguity

### 2. Subagent Strategy

- Use subagents liberally to keep main context window clean
- Offload research, exploration, and parallel analysis to subagents
- For complex problems, throw more compute at it via subagents
- One tack per subagent for focused execution

### 3. Self-Improvement Loop

- After ANY correction from the user: update `tasks/lessons.md` with the pattern
- Write rules for yourself that prevent the same mistake
- Ruthlessly iterate on these lessons until mistake rate drops
- Review lessons at session start for relevant project

### 4. Verification Before Done

- Never mark a task complete without proving it works
- Diff behavior between main and your changes when relevant
- Ask yourself: "Would a staff engineer approve this?"
- Run tests, check logs, demonstrate correctness

### 5. Demand Elegance (Balanced)

- For non-trivial changes: pause and ask "is there a more elegant way?"
- If a fix feels hacky: "Knowing everything I know now, implement the elegant solution"
- Skip this for simple, obvious fixes - don't over-engineer
- Challenge your own work before presenting it

### 6. Autonomous Bug Fixing

- When given a bug report: just fix it. Don't ask for hand-holding
- Point at logs, errors, failing tests - then resolve them
- Zero context switching required from the user
- Go fix failing CI tests without being told how

## Task Management

1. **Plan First**: Write plan to `tasks/todo.md` with checkable items
2. **Verify Plan**: Check in before starting implementation
3. **Track Progress**: Mark items complete as you go
4. **Explain Changes**: High-level summary at each step
5. **Document Results**: Add review section to `tasks/todo.md`
6. **Capture Lessons**: Update `tasks/lessons.md` after corrections

## Core Principles

- **Simplicity First**: Make every change as simple as possible. Impact minimal code.
- **No Laziness**: Find root causes. No temporary fixes. Senior developer standards.
- **Minimal Impact**: Changes should only touch what's necessary. Avoid introducing bugs.

## Project Overview

Kotlin Multiplatform (KMP) + Compose Multiplatform application with a Ktor backend.
Terminal-themed design system. Targets: Android, iOS, JVM Desktop, WASM.
Architecture: MVI (Model-View-Intent) on the client side, feature modules on the server side.

## Design ↔ Code Mapping (Pencil)

Two paired skills enforce a deterministic bridge between `terminal_design_system.pen`
and the `app:designsystem` Kotlin module:

- **`terminal-design-generator`** (mandatory when *designing* in Pencil) — every
  component is a `type:"ref"` to one of the 41 reusable IDs, every color/font/spacing
  is a `$--` token. Read `.claude/skills/terminal-design-generator/REFERENCES.md`
  (or `manifest.json`) before producing any node.

- **`terminal-design-implementer`** (mandatory when *implementing* a design in
  Compose) — every Pencil ref → exact Composable, every `$--` token →
  `TerminalTheme.<group>.<property>`. Read
  `.claude/skills/terminal-design-implementer/CODE-MAP.md` before writing any
  Composable that mirrors a design.

- **`terminal-design-sync`** (audit + self-update) — diffs Pencil components,
  Pencil variables, Kotlin Composables, Kotlin theme tokens, and the manifest.
  Defaults to read-only audit (writes a report at
  `.claude/skills/terminal-design-sync/last-sync-report.md`). Pass `--apply`
  to patch `manifest.json`/`REFERENCES.md`/`CODE-MAP.md`. Invoke via
  `/sync-design` or by saying "sync the design system mapping". Run after
  any change to `terminal_design_system.pen` or `app:designsystem/`.

Together they guarantee a deterministic 1:1 mapping in both directions:
design ref `SpHta` ↔ `TerminalButton(variant = ButtonVariant.Default)`,
design token `$--terminal-bg` ↔ `TerminalTheme.colors.bg`. The chain holds in
both reading and writing — no hex leaks, no hallucinated components — and
`terminal-design-sync` keeps the chain honest as both sides evolve.

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

## graphify

This project has a graphify knowledge graph at graphify-out/.

Rules:
- Before answering architecture or codebase questions, read graphify-out/GRAPH_REPORT.md for god nodes and community structure
- If graphify-out/wiki/index.md exists, navigate it instead of reading raw files
- After modifying code files in this session, run `python3 -c "from graphify.watch import _rebuild_code; from pathlib import Path; _rebuild_code(Path('.'))"` to keep the graph current
