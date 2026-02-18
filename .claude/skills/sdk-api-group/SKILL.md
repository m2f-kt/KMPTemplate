---
name: sdk-api-group
description: This skill should be used when adding new API endpoints to the SDK or creating entirely new API groups. It activates proactively when Claude detects intent to add SDK methods, create API groups, or implement client-side SDK endpoints (e.g., "add a getGroups endpoint", "create a Groups API", "I need an endpoint for notifications", "add a method to UserApi"). It handles the full client SDK pipeline including interface, implementation, fake builder, Koin wiring, Sdk facade delegation, and FakeSdkBuilder composition.
---

# SDK API Group

Add new API endpoints to existing SDK groups or create entirely new API groups with the full pipeline: interface, implementation, @Resource routes, DTOs, fake builder, Koin wiring, Sdk facade delegation, and FakeSdkBuilder composition.

## When to Use

- Adding a new endpoint to an existing API group (e.g., new method on AuthApi or UserApi)
- Creating an entirely new API group (e.g., GroupsApi, NotificationsApi)
- Implementing client SDK support for a new server feature

## When NOT to Use

- Server-only route changes with no SDK involvement
- ViewModel or UI work (use the mvi-viewmodel skill instead)
- Refactoring existing SDK code without adding new endpoints
- Pure testing work without new endpoints

## Workflow

### Step 1: Scan Existing API Groups

Read all API interfaces to understand what groups exist:

```
Glob: core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/*Api.kt
```

Read each interface file. Present a summary:

```
Existing API groups:
- AuthApi: register, login, refresh, forgotPassword, resetPassword, logout
- UserApi: getProfile, updateProfile, getUserById
```

Determine whether the requested endpoint fits an existing group based on domain.

### Step 2: Classify — Existing Group or New Group

Present the classification to the user:

- **If fits existing group** → "This looks like it belongs in {XxxApi}. Confirm?"
- **If ambiguous** → "This could go in {XxxApi} or be a new group. Which do you prefer?"
- **If clearly new** → "This needs a new API group. What name? (e.g., Groups, Notifications)"

Use `AskUserQuestion` to confirm.

### Step 3: Interview for Endpoint Details

Ask interactively for each endpoint using `AskUserQuestion`:

1. **Method name** — e.g., `getGroups`, `createGroup`
2. **HTTP verb** — GET, POST, PUT, DELETE
3. **Parameters** — request DTO fields (name, type)
4. **Return type** — response DTO fields (name, type)
5. **Route path** — e.g., `/api/groups`, `/api/groups/{id}`

Ask about multiple endpoints if the user needs more than one.

### Step 4a: Add to Existing Group

When adding to an existing API group, modify these files in order:

1. **Add @Resource route** (if new route needed) — append to `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt`
2. **Add DTOs** (if new types needed) — create or modify files in `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/`
3. **Add method to interface** — edit `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/XxxApi.kt`
4. **Add implementation** — edit `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/XxxApiImpl.kt`
5. **Add fake lambda** — edit `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeXxxApiBuilder.kt`

Load `references/source-patterns.md` for exact code patterns before writing any code.

### Step 4b: Create New API Group

When creating an entirely new API group, create/modify these files in order:

1. **@Resource routes** — append to `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt`
2. **DTOs** — create `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/XxxDtos.kt`
3. **Interface** — create `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/XxxApi.kt`
4. **Implementation** — create `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/XxxApiImpl.kt`
5. **Sdk.kt** — add constructor param + `by` delegation
6. **SdkModule.kt** — add `single<XxxApi> { XxxApiImpl(client = get()) }` and update `Sdk(...)` constructor
7. **Fake builder** — create `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeXxxApiBuilder.kt`
8. **FakeSdkBuilder.kt** — add builder field + DSL function + `build()` wiring
9. **AppError subclass** — ask user if domain-specific errors are needed

Load `references/source-patterns.md` for exact code patterns before writing any code. Every pattern (interface, impl, fake builder, routes, DTOs, Sdk wiring, FakeSdkBuilder composition) is documented there with complete examples.

### Step 5: Verify Compilation

After all changes, run:

```bash
./gradlew :core:models:compileCommonMainKotlinMetadata  # routes + DTOs
./gradlew :core:sdk:compileCommonMainKotlinMetadata      # interface + impl + Sdk + SdkModule
./gradlew :core:testing:compileCommonMainKotlinMetadata   # fake builder + FakeSdkBuilder
```

If any compilation fails, read the error output, fix the issue, and re-run.

## Critical Rules

- All API methods are `suspend` and return `Either<AppError, T>`
- All implementations use `apiCall<T> { ... }` wrapper — never raw Ktor calls
- Fake builders default unconfigured methods to `Either.Left(AppError.Client.Unknown())`
- Fake builder `build()` methods are `internal` visibility
- All fake builders are annotated with `@FakeSDKDsl`
- Sdk facade uses Kotlin `by` delegation — never manual forwarding
- Koin binds using interface type qualifiers: `single<XxxApi> { XxxApiImpl(...) }`
- DTO files use `@Serializable` on all data classes
- @Resource routes are `@Serializable` with nested classes referencing parent

## File Locations

| File Type | Location |
|-----------|----------|
| Routes | `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt` |
| DTOs | `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/` |
| API interfaces | `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/` |
| API implementations | `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/` |
| Sdk facade | `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt` |
| SdkModule | `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt` |
| Fake builders | `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/` |
| FakeSdkBuilder | `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeSdkBuilder.kt` |
| AppError | `core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt` |
| @FakeSDKDsl | `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/Annotations.kt` |

## References

- `references/source-patterns.md` — Complete source code patterns for all 9 file types in the pipeline. Load before writing any code.
