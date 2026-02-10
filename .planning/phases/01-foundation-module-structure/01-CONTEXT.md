# Phase 1: Foundation & Module Structure - Context

**Gathered:** 2026-02-10
**Status:** Ready for planning

<domain>
## Phase Boundary

Establish module boundaries, upgrade dependencies, and wire cross-cutting patterns (DI, error handling, logging) across all KMP targets (Android, iOS, Desktop, WASM). This phase delivers the architectural skeleton that all subsequent phases build on. No features — just correct structure, patterns, and build infrastructure.

</domain>

<decisions>
## Implementation Decisions

### Module organization
- Grouped by layer using nested Gradle convention: `:core:sdk`, `:core:storage`, `:core:models`, `:server:auth`, `:server:ai`, `:app:auth`, `:app:dashboard`
- Shared DTOs (server-to-client models) live in a shared module visible to both server and client — these ARE the API contract
- Domain-specific intermediate models and mappers live inside the module that owns them (not shared)
- Client is split into feature modules (`:app:auth`, `:app:dashboard`, etc.), not a monolithic composeApp
- Server is split into feature modules (`:server:auth`, `:server:ai`, `:server:core`)

### Error type design
- Single sealed hierarchy: one `AppError` sealed class with nested sealed subtypes (e.g., `AppError.Auth.InvalidCredentials`)
- Server errors carry structured string code + message (e.g., code=`AUTH_INVALID_CREDENTIALS`, message="Email or password is incorrect")
- Client errors are generic (network, timeout, unknown) EXCEPT server-mapped errors which preserve the server's code and message
- Validation uses Arrow accumulated errors (zipOrAccumulate/mapOrAccumulate) — all issues returned at once, not fail-fast
- Structured string codes (e.g., `AUTH_INVALID_CREDENTIALS`, `USER_NOT_FOUND`) serve as identifiers — designed to double as future localization keys

### Logging & observability
- Default log level: Debug (verbose for first-run developer experience)
- Split logging: Log4j with JSON format on server, Kermit on client targets
- Structured logs with key-value metadata tags (e.g., `userId=123`, `action=login`, `status=success`)
- Module-based log tags: `[AUTH]`, `[SDK]`, `[STORAGE]`, `[AI]` — convention established in template examples

### Dependency policy
- All versions pinned to exact releases in Gradle version catalog (libs.versions.toml)
- Use BOMs where available (JetBrains Compose BOM, Ktor BOM, Arrow BOM) to align transitive dependencies
- Core stack is locked and non-negotiable: Koin (DI), Arrow (errors), Kermit (client logging), Exposed (DB), Ktor (server + client)
- Shared build logic lives in buildSrc convention plugins, not repeated per-module

### Claude's Discretion
- Exact module dependency graph wiring
- Kermit log writer configuration per platform
- Convention plugin organization within buildSrc
- Log4j JSON layout configuration details

</decisions>

<specifics>
## Specific Ideas

- Server-to-client DTOs are the shared API contract — they live in a shared module, not duplicated
- Error codes follow `DOMAIN_SPECIFIC_ERROR` format (structured strings, not numeric codes)
- Server logging uses Log4j JSON for production observability; client uses Kermit for multiplatform consistency
- Convention plugins in buildSrc to reduce Gradle duplication across modules

</specifics>

<deferred>
## Deferred Ideas

- Localization/i18n mechanism for error messages — user wants translated messages eventually, structured error codes are designed to support this as localization keys in a future phase

</deferred>

---

*Phase: 01-foundation-module-structure*
*Context gathered: 2026-02-10*
