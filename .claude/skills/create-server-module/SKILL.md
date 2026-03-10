---
name: create-server-module
description: Creates a new server-side feature module with 3 submodules (contract, impl, wire) for the Ktor backend. Use when creating new server features, adding backend modules, or scaffolding API endpoints.
allowed-tools: Write, Read, Edit, Bash
---

# Server Feature Module Creator

## Purpose
This skill creates new server feature modules for the Ktor + Koin backend following the contract/implementation/wire 3-submodule pattern.

## Instructions

### 1. Understand the Feature Requirements
- Ask for the feature name (e.g., "notifications", "billing", "chat")
- Clarify domain entities and relationships
- Identify dependencies on other server modules (contracts)

### 2. Generate Module Structure
Run the `create_server_module.sh` script to scaffold the complete structure:

```bash
.claude/skills/create-server-module/scripts/create_server_module.sh --name "feature name"
```

The script will create:
- **3 submodules**: contract, impl, wire
- Directory structure (`server/<feature>/`)
- Base package structure (`com.m2f.server.<feature>`)
- `build.gradle.kts` for each submodule
- Update `settings.gradle.kts` to include all 3 modules
- Service interface in contract
- Domain errors in contract
- Service implementation, repository, table, routes, migrations in impl
- Koin DI module in wire

### 3. Feature Module Structure
After the script runs, you'll have this structure:

```
server/<feature>/
├── contract/                                          # Interfaces and errors (public API)
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/m2f/server/<feature>/contract/
│       ├── <Feature>Service.kt                        # Service interface
│       └── <Feature>Errors.kt                         # Domain errors
│
├── impl/                                              # All implementation details
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/com/m2f/server/<feature>/impl/
│       │   ├── service/
│       │   │   └── <Feature>ServiceImpl.kt            # Service implementation
│       │   ├── repository/
│       │   │   └── <Feature>Repository.kt             # Exposed R2DBC repository
│       │   ├── tables/
│       │   │   └── <Feature>Table.kt                  # Exposed table definition
│       │   ├── routes/
│       │   │   └── <Feature>Routes.kt                 # Ktor route definitions
│       │   └── migrations/
│       │       └── <Feature>Migrations.kt             # DB migrations
│       └── test/kotlin/com/m2f/server/<feature>/impl/
│           └── <Feature>RoutesTest.kt                 # Route tests
│
└── wire/                                              # DI wiring (Koin module)
    ├── build.gradle.kts
    └── src/main/kotlin/com/m2f/server/<feature>/wire/
        └── <Feature>Module.kt                         # Koin module + migration registration
```

### 4. How the 3-Submodule Pattern Works

- **Contract** exposes service interfaces so other server modules can depend on the contract without pulling in the implementation. Example: `server:groups` can depend on `server:auth:contract` to use `UserRepository` interface without depending on all of auth's implementation details.
- **Implementation** is hidden -- only wire sees it. Contains all concrete classes: services, repositories, tables, routes, migrations.
- **Wire** assembles everything and exports the Koin module. This is what `server/build.gradle.kts` imports.

### 5. Architecture Patterns

- **DI**: Koin with `module { single { ... } }` and `includes()`
- **Server framework**: Ktor with @Resource type-safe routing
- **ORM**: Exposed R2DBC with `suspendTransaction(db = db) { ... }`
- **Error handling**: Arrow `context(raise: Raise<DomainError>)`
- **Build convention**: `server-module-convention` plugin
- **DTOs and @Resource routes** live in `core:models`

### 6. Follow Best Practices
- Do NOT write comments unless explicitly requested
- Use Arrow-kt for error handling (Raise, Either)
- Use Exposed R2DBC for database access (not blocking JDBC)
- Keep contract module free of implementations
- Register migrations in the wire module
- Use fakes (not mocks) in tests

### 7. Next Steps After Creation
1. Include wire module in `server/build.gradle.kts`:
   ```kotlin
   implementation(projects.server.<feature>.wire)
   ```
2. Add to `ServerModule.kt`:
   ```kotlin
   includes(<feature>Module)
   ```
3. Register migrations in `Application.kt` config block:
   ```kotlin
   register<Feature>Migrations()
   ```
4. Add routes in `Application.kt` routing block:
   ```kotlin
   <feature>Routes(get())
   ```
5. Sync Gradle project

## Examples
- "Create a new notifications server module"
- "Add a server module for billing"
- "Scaffold a chat backend feature"

## Notes
- Server modules use JVM only (not KMP), with `src/main/kotlin` paths
- Package root is `com.m2f.server`
- The `server-module-convention` plugin is used for all submodules
- Other server modules should depend on contracts, not implementations
