# Phase 13: Group Server & SDK - Research

**Researched:** 2026-02-19
**Domain:** Server-side group management (Ktor + Exposed R2DBC + Arrow), SDK client (KMP), integration testing
**Confidence:** HIGH

## Summary

Phase 13 adds group-based user organization to the existing Ktor server. The codebase already has well-established patterns for server modules (`server:auth`), Exposed R2DBC tables, Arrow Raise error handling, type-safe `@Resource` routes, and SDK client APIs returning `Either<AppError, T>`. This phase follows the exact same architecture -- a new `server:groups` module with tables, repositories, services, routes, migrations, and a corresponding `GroupApi` interface + impl in `core:sdk`.

The project uses Exposed 1.0.0 with R2DBC (async/non-blocking), PostgreSQL via R2DBC driver, Testcontainers 2.0.3 for integration tests, and Arrow for typed error handling. All patterns are already proven in the auth module.

**Primary recommendation:** Mirror the `server:auth` module structure exactly for `server:groups`. Reuse the established `conduit`/`conduitAuth` response helpers, `DomainError` hierarchy, `Migration` interface, and `withRole()` authorization. Add `GroupApi` to the `Sdk` facade via Kotlin delegation.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Join table (`user_group_memberships`) from day one -- multi-group ready without future migration
- Group fields: id, name, slug (URL-friendly), description, created_by (user_id), created_at, updated_at
- Membership table: user_id, group_id, role (group-specific role column) -- a user could be admin in one group and member in another
- Full CRUD for groups: create, read, update, delete + list all groups (power admin)
- Member management: add/remove existing users AND register new users directly into a group
- Member list is paginated with cursor-based pagination
- All SDK functions return `Either<ClientError, T>` using shared `@Resource` route definitions
- Admins manage group (CRUD + member management); members can only read their own membership, not the full member list
- Cross-group access returns 403 Forbidden (explicit, not 404)
- Existing power admin role gets cross-group visibility -- can see and manage all groups
- Group isolation applies only to group endpoints -- existing features stay as-is for now
- No auto-create on server startup -- seed scripts handle group creation
- Migration creates a default group and assigns all existing users to it
- Setup CLI stays unchanged -- group creation is a separate concern
- Dev/test seed creates 2 groups with users in each for out-of-the-box isolation testing

### Claude's Discretion
- Exact table naming and index strategy
- Cursor pagination implementation details
- Error response message wording
- Test fixture organization

### Deferred Ideas (OUT OF SCOPE)
None -- discussion stayed within phase scope
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| GRP-01 | Admin can create a group with name and description | Group CRUD service + routes following auth module patterns; `conduitAuth` + `withRole` for authorization |
| GRP-02 | User belongs to one group (schema designed for future multi-group) | Join table `user_group_memberships` with composite PK; migration seeds default group and assigns existing users |
| GRP-07 | Group SDK functions return Either<ClientError, T> with shared @Resource routes | `GroupApi` interface + `GroupApiImpl` in `core:sdk`, `Groups` resource class in `core:models`, `Sdk` facade delegation |
| GRP-08 | Group data is isolated -- users cannot see other groups' data | Middleware/service-level check: extract group from JWT or membership lookup, compare to requested group, return 403 |
| TEST-02 | Server integration tests run via Ktor testApplication with test database | Testcontainers PostgreSQL + `ktor-server-test-host`; test helper for auth token generation; verify CRUD, RBAC, isolation |
</phase_requirements>

## Standard Stack

### Core (Already in Project)
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Exposed | 1.0.0 | ORM / SQL DSL for table definitions, queries, migrations | Already used for all DB access (R2DBC variant) |
| Exposed R2DBC | 1.0.0 | Async database operations via R2DBC driver | Non-blocking coroutine-based DB access |
| PostgreSQL R2DBC | 1.0.7.RELEASE | R2DBC driver for PostgreSQL | Already configured in project |
| Ktor Server | (project version) | HTTP framework with type-safe routing | Routes use `@Resource` + `post<>`, `get<>`, etc. |
| Ktor Resources | (project version) | Type-safe route definitions shared between server and client | `@Resource` classes in `core:models` |
| Arrow Core | (project version) | `Either`, `Raise`, `zipOrAccumulate` for error handling | Server uses Arrow Raise context receivers throughout |
| Koin | (project version) | Dependency injection | Module-per-feature pattern (`authModule`, `serverModule`) |
| Testcontainers | 2.0.3 | Real PostgreSQL in tests | Already in `testing-server` bundle |
| Kotest Assertions | (project version) | Test assertions including Arrow matchers | Already in `testing-server` bundle |

### No New Dependencies Needed
The existing stack covers everything Phase 13 requires. No new libraries are needed.

## Architecture Patterns

### Recommended Module Structure

New Gradle module: `server:groups`

```
server/groups/
├── build.gradle.kts
└── src/main/kotlin/com/m2f/server/groups/
    ├── Groups.kt                    # registerGroupMigrations() + Migration classes
    ├── tables/
    │   ├── GroupsTable.kt           # Exposed table definition
    │   └── UserGroupMembershipsTable.kt  # Join table
    ├── repository/
    │   ├── GroupRepository.kt       # CRUD operations
    │   └── MembershipRepository.kt  # Membership operations
    ├── service/
    │   └── GroupService.kt          # Business logic with Arrow Raise
    ├── routes/
    │   └── GroupRoutes.kt           # Ktor route handlers
    ├── errors/
    │   └── GroupErrors.kt           # DomainError implementations
    └── di/
        └── GroupModule.kt           # Koin module
```

New shared DTOs in `core:models`:
```
core/models/src/commonMain/kotlin/com/m2f/template/models/
├── dto/
│   └── GroupDtos.kt                 # GroupResponse, CreateGroupRequest, etc.
└── routes/
    └── ApiRoutes.kt                 # Add Groups resource class (existing file)
```

New SDK API in `core:sdk`:
```
core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/
├── api/
│   ├── GroupApi.kt                  # Interface
│   └── GroupApiImpl.kt              # Implementation
├── Sdk.kt                           # Add GroupApi delegation (existing file)
└── di/
    └── SdkModule.kt                 # Add GroupApi binding (existing file)
```

### Pattern 1: Arrow Raise Error Handling (from AuthService)
**What:** Service methods use `context(raise: Raise<DomainError>)` to raise typed errors
**When to use:** Every service method that can fail
**Example (existing pattern):**
```kotlin
context(raise: Raise<DomainError>)
suspend fun createGroup(request: CreateGroupRequest, userId: String): GroupResponse {
    // Validation, business logic, raise errors via ensure/ensureNotNull
}
```

### Pattern 2: conduit/conduitAuth Response Helpers (from AuthRoutes)
**What:** Route handlers use `conduit {}` (public) or `conduitAuth {}` (authenticated) to handle Either responses
**When to use:** Every route handler
**Example (existing pattern):**
```kotlin
authenticate {
    withRole(UserRole.Admin, UserRole.PowerAdmin) {
        post<Groups.Create> {
            conduitAuth(HttpStatusCode.Created) { userId ->
                groupService.createGroup(getModel(), userId)
            }
        }
    }
}
```

### Pattern 3: withRole() Authorization (from RoleAuthorization.kt)
**What:** Route-scoped plugin that reads JWT "role" claim and checks against required roles
**When to use:** Routes requiring specific user roles
**Important:** This checks the system-level role (UserRole.Admin/PowerAdmin), NOT the group-specific role. Group-level role checking happens in the service layer by querying the membership table.

### Pattern 4: Migration Registration (from Auth.kt)
**What:** Feature modules register their own migrations via `MigrationRegistry.register()` before `startDatabase()`
**When to use:** Adding new tables
**Example:**
```kotlin
fun registerGroupMigrations() {
    MigrationRegistry.register(CreateGroupsTableMigration())
    MigrationRegistry.register(CreateMembershipsTableMigration())
    MigrationRegistry.register(SeedDefaultGroupMigration())
}
```

### Pattern 5: SDK API with Delegation (from Sdk.kt)
**What:** `Sdk` class uses Kotlin delegation to compose API interfaces
**When to use:** Adding GroupApi to the SDK facade
**Example:**
```kotlin
class Sdk(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val groupApi: GroupApi,
) : AuthApi by authApi, UserApi by userApi, GroupApi by groupApi
```

### Anti-Patterns to Avoid
- **Nesting group_id in JWT claims:** Don't put group_id in the JWT -- users can be in multiple groups (multi-group schema). Look up membership at request time.
- **Using 404 for cross-group access:** CONTEXT.md explicitly requires 403 Forbidden for cross-group access, not 404.
- **Modifying existing auth module:** Group logic belongs in its own `server:groups` module. The auth module stays untouched.
- **Blocking database calls:** All DB access must use `suspendTransaction(db = db)` with R2DBC -- never JDBC.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Authorization gating | Custom auth middleware | `withRole()` from `RoleAuthorization.kt` | Already handles JWT role extraction and 403 response |
| Error response formatting | Custom error serialization | `DomainError.respond()` + `conduit {}` | Proven pattern handles all HTTP status codes |
| Token validation | Manual JWT parsing in routes | `authenticate {}` + `conduitAuth {}` | Ktor auth plugin handles token verification |
| Table migrations | Raw SQL strings | `SchemaUtils.create()` + `Migration` interface | Exposed handles DDL generation from table definitions |
| Cursor pagination encoding | Custom Base64 encoding | Standard approach: encode last-seen ID as opaque cursor | Simple, no library needed, but don't invent a custom scheme |

## Common Pitfalls

### Pitfall 1: Exposed R2DBC Transaction Scoping
**What goes wrong:** Operations outside `suspendTransaction` block fail silently or throw
**Why it happens:** R2DBC requires explicit transaction boundaries unlike JDBC auto-commit
**How to avoid:** Every repository method wraps operations in `suspendTransaction(db = db) { ... }`
**Warning signs:** "No current transaction" exceptions at runtime

### Pitfall 2: Migration Ordering with Foreign Keys
**What goes wrong:** Migration creating `user_group_memberships` runs before `groups` table exists
**Why it happens:** Migration versions must be ordered correctly; FK targets must exist first
**How to avoid:** Use timestamp-based version strings (YYYYMMDDHHMMSS) in correct chronological order. Groups table migration runs before memberships table migration.
**Warning signs:** "Table not found" or "Referenced table does not exist" during migration

### Pitfall 3: Seed Migration Assumptions
**What goes wrong:** Default group seed migration fails when no users exist or runs twice
**Why it happens:** Seed data is migration-order dependent
**How to avoid:** Seed migration should be idempotent (INSERT IF NOT EXISTS pattern). The seed creates a default group and inserts memberships for ALL existing users at migration time.
**Warning signs:** Unique constraint violations on re-run

### Pitfall 4: Group-Level vs System-Level Roles
**What goes wrong:** Confusing `UserRole` (system-level: USER/ADMIN/POWER_ADMIN) with group membership role (group-level: owner/admin/member)
**Why it happens:** Both are "roles" but operate at different scopes
**How to avoid:** System role gates API access (via `withRole()`). Group membership role determines what a user can do *within* a group (checked in service layer). Use distinct types/column names.
**Warning signs:** Admin of one group can manage another group they're only a member of

### Pitfall 5: Cursor Pagination Boundary Conditions
**What goes wrong:** Empty pages, missing items, or infinite loops
**Why it happens:** Off-by-one errors in cursor comparison, wrong sort order
**How to avoid:** Cursor = encoded ID of last item. Query uses `WHERE id > cursor ORDER BY id ASC LIMIT pageSize`. Return `hasMore = results.size == pageSize`.
**Warning signs:** Last page returns items from the beginning, or items are skipped

### Pitfall 6: Kotlin Context Receivers with Exposed R2DBC
**What goes wrong:** Context receiver functions don't compose correctly with `suspendTransaction`
**Why it happens:** `suspendTransaction` is a suspend lambda, context receivers need careful scoping
**How to avoid:** Keep context receivers on the service layer (`Raise<DomainError>`). Repository layer uses plain suspend functions without context receivers (same as `UserRepository`).

## Code Examples

### Exposed Table Definition (following UsersTable pattern)
```kotlin
@file:OptIn(ExperimentalUuidApi::class)
object GroupsTable : Table("groups") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 100)
    val slug = varchar("slug", 100).uniqueIndex()
    val description = text("description").default("")
    val createdBy = uuid("created_by").references(UsersTable.id)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}

object UserGroupMembershipsTable : Table("user_group_memberships") {
    val userId = uuid("user_id").references(UsersTable.id)
    val groupId = uuid("group_id").references(GroupsTable.id)
    val role = varchar("role", 20).default("MEMBER")  // OWNER, ADMIN, MEMBER
    val joinedAt = datetime("joined_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(userId, groupId)  // Composite PK
}
```

### Type-Safe Routes (following ApiRoutes.kt pattern)
```kotlin
@Serializable
@Resource("/api/groups")
class Groups {
    @Serializable @Resource("create")
    class Create(val parent: Groups = Groups())

    @Serializable @Resource("{groupId}")
    class ById(val parent: Groups = Groups(), val groupId: String)

    @Serializable @Resource("{groupId}/members")
    class Members(val parent: Groups = Groups(), val groupId: String, val cursor: String? = null, val limit: Int = 20)

    @Serializable @Resource("{groupId}/members/{userId}")
    class Member(val parent: Groups = Groups(), val groupId: String, val userId: String)

    @Serializable @Resource("{groupId}/members/register")
    class RegisterMember(val parent: Groups = Groups(), val groupId: String)
}
```

### SDK API Interface (following AuthApi pattern)
```kotlin
interface GroupApi {
    suspend fun createGroup(request: CreateGroupRequest): Either<AppError, GroupResponse>
    suspend fun getGroup(groupId: String): Either<AppError, GroupResponse>
    suspend fun updateGroup(groupId: String, request: UpdateGroupRequest): Either<AppError, GroupResponse>
    suspend fun deleteGroup(groupId: String): Either<AppError, Unit>
    suspend fun listGroups(): Either<AppError, List<GroupResponse>>
    suspend fun getMembers(groupId: String, cursor: String? = null, limit: Int = 20): Either<AppError, PaginatedResponse<MemberResponse>>
    suspend fun addMember(groupId: String, request: AddMemberRequest): Either<AppError, MemberResponse>
    suspend fun removeMember(groupId: String, userId: String): Either<AppError, Unit>
    suspend fun registerMember(groupId: String, request: RegisterMemberRequest): Either<AppError, MemberResponse>
}
```

### Integration Test Setup (Testcontainers + Ktor testApplication)
```kotlin
class GroupRoutesTest {
    companion object {
        private val postgres = PostgreSQLContainer("postgres:16-alpine")

        @BeforeAll @JvmStatic
        fun startContainer() { postgres.start() }

        @AfterAll @JvmStatic
        fun stopContainer() { postgres.stop() }
    }

    @Test
    fun `create group returns 201`() = testApplication {
        // Configure test app with Testcontainers DB
        application {
            // Install plugins, run migrations, configure routes
        }
        val response = client.post("/api/groups/create") {
            header("Authorization", "Bearer $adminToken")
            contentType(ContentType.Application.Json)
            setBody(CreateGroupRequest(name = "Test Group", slug = "test-group"))
        }
        response.status shouldBe HttpStatusCode.Created
    }
}
```

### DomainError for Groups (following AuthErrors pattern)
```kotlin
data class GroupNotFound(val groupId: String) : DomainError {
    override fun toAppError(): AppError = AppError.Group.NotFound(
        message = "Group not found: $groupId"
    )
    context(routingContext: RoutingContext)
    override suspend fun respond() {
        routingContext.notFound(toAppError().code, toAppError().message)
    }
}

data class GroupForbidden(val message: String = "You do not have permission to access this group") : DomainError {
    override fun toAppError(): AppError = AppError.Group.Forbidden(message = message)
    context(routingContext: RoutingContext)
    override suspend fun respond() {
        routingContext.forbidden(toAppError().code, toAppError().message)
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Exposed JDBC (blocking) | Exposed R2DBC 1.0.0 (non-blocking) | 2025 (Exposed 1.0 release) | All DB ops are suspend; use `suspendTransaction` not `transaction` |
| `org.jetbrains.exposed.sql` imports | `org.jetbrains.exposed.v1` imports | Exposed 1.0.0 | Package renamed in v1 -- use `v1` package prefix |
| Arrow `either { }` for all error handling | Arrow `Raise` context receivers | Arrow 2.0+ | Services declare `context(raise: Raise<DomainError>)` |

## Open Questions

1. **Group membership role enum vs string**
   - What we know: CONTEXT.md says `role` column in memberships table; existing `UserRole` uses sealed class with serializer
   - What's unclear: Whether to use a shared sealed class like `UserRole` or just a string column
   - Recommendation: Use a `GroupRole` sealed class in `core:models` (OWNER/ADMIN/MEMBER) for type safety, matching the `UserRole` pattern. Store as varchar in DB.

2. **AppError.Group hierarchy**
   - What we know: `AppError` is a sealed class with domain-specific subhierarchies (Auth, User, Server, etc.)
   - What's unclear: Exact error variants needed for groups
   - Recommendation: Add `AppError.Group` sealed class with NotFound, Forbidden, AlreadyExists, MemberAlreadyExists variants. Follow existing `AppError.Auth`/`AppError.User` pattern.

3. **Cursor pagination response shape**
   - What we know: CONTEXT.md requires cursor-based pagination for member list
   - What's unclear: Whether to use a generic `PaginatedResponse<T>` or group-specific DTO
   - Recommendation: Create a generic `PaginatedResponse<T>` in `core:models` (items, cursor, hasMore) -- reusable for future pagination needs.

## Sources

### Primary (HIGH confidence)
- Codebase analysis of `server:auth` module -- complete pattern reference for server modules
- Codebase analysis of `core:sdk` -- complete pattern reference for SDK API interfaces
- Codebase analysis of `core:models` -- complete pattern reference for shared DTOs and routes
- Exposed 1.0.0 R2DBC API -- verified via existing codebase usage patterns

### Secondary (MEDIUM confidence)
- Testcontainers 2.0.3 -- already in `testing-server` bundle, patterns well-established
- Cursor pagination -- standard pattern, no library-specific concerns

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH -- entire stack already exists in project, no new dependencies
- Architecture: HIGH -- exact same patterns used in `server:auth` module
- Pitfalls: HIGH -- pitfalls derived from analyzing existing codebase patterns and Exposed R2DBC behavior

**Research date:** 2026-02-19
**Valid until:** 2026-03-19 (stable -- no dependency changes expected)
