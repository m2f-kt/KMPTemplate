---
name: feature
description: Full-stack feature scaffolding - creates server module + client module + SDK API group + Compose screen in one orchestrated flow. Use when building a new end-to-end feature.
disable-model-invocation: true
---

# Full-Stack Feature Scaffold

Orchestrates creation of a complete feature across all layers: server endpoint, SDK API, client module, and Compose screen.

## Workflow

### Step 1: Gather Requirements

Ask:
- Feature name (e.g., "notifications", "settings", "billing")
- What entities/resources does it manage?
- What operations are needed? (CRUD, custom actions)
- Does it need authentication? (most features do)
- Any special infrastructure? (file uploads -> MinIO, AI -> LLM integration)

### Step 2: Server Module (invoke /create-server-module)

Use the `create-server-module` skill to scaffold:
- `server/<feature>/` with contract/impl/wire submodules
- Tables, repository, service, routes, DI module
- Migration registration

### Step 3: SDK API Group (invoke /sdk-api-group)

Use the `sdk-api-group` skill to scaffold:
- API interface in `core/sdk/`
- API implementation with `apiCall()` wrapper
- Fake builder for testing
- Koin wiring
- Sdk facade delegation
- FakeSdkBuilder composition

### Step 4: Client Module (invoke /create-app-module)

Use the `create-app-module` skill to scaffold:
- `app/<feature>/` with contract/impl/wire submodules
- Route definition in contract
- Wire module with Koin + navigation

### Step 5: Compose Screen (invoke /compose-screen)

Use the `compose-screen` skill to scaffold:
- MVI types (Model, Intent, Mutation, Event)
- ViewModel with `take()` + `reduce()`
- Screen composable with callbacks
- ViewModel tests using test DSL

### Step 6: Wire Everything Together

1. Register server module in `server/src/main/kotlin/.../Application.kt`:
   - Add routes
   - Add Koin module to `serverModule`
   - Register migrations

2. Register client module in `composeApp`:
   - Add wire module dependency in `build.gradle.kts`
   - Add Koin module to `appModule`
   - Add navigation entry in `AppNavHost`

3. Add route to navigation (if needed):
   - Add menu item or navigation trigger

### Step 7: Verify

```bash
# Run server tests
./gradlew :server:<feature>:test

# Run client tests
./gradlew :app:<feature>:allTests

# Run full build
./gradlew testAll

# Run detekt
./gradlew detekt
```

## Important Notes

- Each sub-skill (create-server-module, sdk-api-group, create-app-module, compose-screen) has its own detailed patterns. Invoke them rather than duplicating their logic.
- If any sub-skill asks questions, gather all answers upfront in Step 1 to avoid redundant prompting.
- The order matters: server first (defines the API contract), then SDK (client-side API), then client module + screen (consumes the SDK).
