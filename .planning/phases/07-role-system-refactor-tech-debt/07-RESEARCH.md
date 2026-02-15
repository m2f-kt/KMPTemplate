# Phase 7: Role System Refactor & Tech Debt - Research

**Researched:** 2026-02-15
**Domain:** Exposed R2DBC FK migrations, sealed class role typing, tech debt resolution
**Confidence:** HIGH

## Summary

Phase 7 replaces all string-based role handling with a typed `UserRole` sealed class backed by a database `roles` table with referential integrity, and resolves accumulated tech debt (stale comments, stub tool method). The codebase currently has a `UserTier` sealed class in `core:models` that demonstrates the pattern -- `UserRole` will follow the same approach but replace the raw strings flowing through JWT claims, DTOs, and the RBAC plugin.

The migration path is well-understood: the project already uses Exposed R2DBC migrations with `SchemaUtils.create()` (Auth.kt, Ai.kt pattern). Adding a `roles` table and migrating `users.role` from a varchar to a foreign key requires a multi-step migration: create the roles table, seed rows, add `role_id` column with a reference, backfill from existing `role` varchar, then drop the old column. The existing `RefreshTokensTable` already demonstrates the `.references()` pattern for FK columns in Exposed 1.0.

**Primary recommendation:** Create `UserRole` as a sealed class in `core:models` (mirroring `UserTier`), backed by a `RolesTable` with seeded rows. Use a multi-step Exposed R2DBC migration to add FK with data backfill. Replace all `String` role parameters throughout the server with `UserRole` typed values.

## Standard Stack

### Core (already in project -- no new dependencies)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Exposed R2DBC | 1.0.0 | Database schema, migrations, queries | Already used; FK via `.references()` |
| kotlinx.serialization | 1.10.0 | Sealed class wire serialization | Already used for UserTier, DTOs |
| Arrow Core | 2.2.1.1 | Raise error handling | Already used throughout |
| Ktor Server Auth JWT | 3.4.0 | JWT claim encoding/decoding | Already used for role claims |

### Supporting (no new dependencies needed)

This phase requires zero new library dependencies. All work uses existing Exposed R2DBC, kotlinx.serialization, and Ktor auth infrastructure.

## Architecture Patterns

### Recommended Changes by File

```
core/models/src/commonMain/kotlin/com/m2f/template/models/
  UserRole.kt               # NEW: sealed class (mirrors UserTier pattern)

server/auth/src/main/kotlin/com/m2f/server/auth/
  tables/
    RolesTable.kt            # NEW: Exposed Table for roles
    UsersTable.kt            # MODIFY: role_id FK replaces role varchar
  Auth.kt                    # MODIFY: add roles migration
  repository/
    UserRepository.kt        # MODIFY: UserRecord.role -> UserRole typed
  service/
    AuthService.kt           # MODIFY: "USER" string -> UserRole.User
    OAuthService.kt          # MODIFY: "USER" string -> UserRole.User
    UserService.kt           # MODIFY: toUserResponse() uses UserRole
  authorization/
    RoleAuthorization.kt     # MODIFY: withRole accepts UserRole, not String
  security/
    JwtTokenProvider.kt      # MODIFY: role param typed as UserRole

core/models/src/commonMain/kotlin/com/m2f/template/models/dto/
  UserDtos.kt                # MODIFY: UserResponse.role -> UserRole typed

server/ai/src/main/kotlin/com/m2f/server/ai/
  tools/UserTools.kt         # MODIFY: getUserCount() returns actual DB count
  agents/ChatStreamingStrategy.kt  # MODIFY: fix stale SSE comment

app/profile/                 # MODIFY: replace UserTier references with UserRole
                             # (or keep UserTier as display concern, map from UserRole)
```

### Pattern 1: Sealed Class as Typed Role (mirrors existing UserTier)

**What:** `UserRole` is a sealed class with `data object` variants, each carrying a `value` string for DB/wire serialization.
**When to use:** When you need compile-time exhaustive role checking + DB storage + wire serialization.
**Example:**
```kotlin
// In core:models (commonMain, KMP-visible)
@Serializable
sealed class UserRole {
    abstract val value: String
    abstract val level: Int

    @Serializable
    @SerialName("USER")
    data object User : UserRole() {
        override val value: String = "USER"
        override val level: Int = 0
    }

    @Serializable
    @SerialName("ADMIN")
    data object Admin : UserRole() {
        override val value: String = "ADMIN"
        override val level: Int = 1
    }

    @Serializable
    @SerialName("POWER_ADMIN")
    data object PowerAdmin : UserRole() {
        override val value: String = "POWER_ADMIN"
        override val level: Int = 2
    }

    companion object {
        fun fromString(role: String): UserRole = when (role.uppercase()) {
            "USER" -> User
            "ADMIN" -> Admin
            "POWER_ADMIN", "POWERADMIN" -> PowerAdmin
            else -> User // safe default
        }

        val entries: List<UserRole> = listOf(User, Admin, PowerAdmin)
    }
}
```

### Pattern 2: Roles Table with FK Migration (multi-step)

**What:** A `roles` table seeded with role rows, referenced by `users.role_id` FK.
**When to use:** When roles need referential integrity in the database.
**Example:**
```kotlin
// RolesTable.kt
object RolesTable : Table("roles") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50).uniqueIndex()
    val level = integer("level")
    override val primaryKey = PrimaryKey(id)
}

// Updated UsersTable.kt
object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val email = varchar("email", EMAIL_LENGTH).uniqueIndex()
    val passwordHash = varchar("password_hash", PASSWORD_HASH_LENGTH)
    val name = varchar("name", NAME_LENGTH)
    val roleId = integer("role_id").references(RolesTable.id)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}
```

### Pattern 3: Multi-Step Migration for Existing Data

**What:** A single Migration class that executes multiple DDL/DML steps to avoid data loss.
**When to use:** When adding FK to a table that already has data.
**Example (follows existing Auth.kt migration pattern):**
```kotlin
internal class CreateRolesTableAndMigrateUsersMigration : Migration {
    override val version: String = "20260215000001"
    override val description: String = "Create roles table, seed roles, migrate users.role to role_id FK"

    override suspend fun migrate() {
        // Step 1: Create roles table
        SchemaUtils.create(RolesTable)

        // Step 2: Seed role rows
        RolesTable.insert { it[name] = "USER"; it[level] = 0 }
        RolesTable.insert { it[name] = "ADMIN"; it[level] = 1 }
        RolesTable.insert { it[name] = "POWER_ADMIN"; it[level] = 2 }

        // Step 3: Add role_id column (initially nullable for backfill)
        // Use SchemaUtils.addMissingColumnsStatements or raw DDL
        // Step 4: Backfill role_id from existing role varchar
        // Step 5: Make role_id NOT NULL, add FK constraint
        // Step 6: Drop old role varchar column
    }
}
```

**CRITICAL DESIGN DECISION:** The migration must handle the case where the users table already has data with the old `role` varchar column. Two approaches:

**Approach A (Recommended -- Simpler, matches codebase style):** Use `SchemaUtils.addMissingColumnsStatements()` for the new column, then use Exposed DSL for data backfill. The existing migrations use `SchemaUtils.create()` exclusively, so adding column-level operations is a natural extension.

**Approach B (Fallback):** If `addMissingColumnsStatements` proves unreliable with R2DBC for ALTER TABLE + FK, keep the `role` varchar in the users table (no FK) but always map it through `UserRole.fromString()` at the repository layer. This trades DB-level referential integrity for implementation simplicity.

### Pattern 4: RBAC Plugin with Typed Roles

**What:** Change `withRole(vararg roles: String)` to `withRole(vararg roles: UserRole)`.
**When to use:** Replace the current string-based role authorization.
**Example:**
```kotlin
// Updated RoleAuthorization.kt
class RoleConfig {
    var roles: Set<UserRole> = emptySet()
}

val RoleAuthorizationPlugin: RouteScopedPlugin<RoleConfig> = createRouteScopedPlugin(
    name = "RoleAuthorizationPlugin",
    createConfiguration = ::RoleConfig,
) {
    val requiredRoles = pluginConfig.roles
    on(AuthenticationChecked) { call ->
        val principal = call.principal<JWTPrincipal>()
        val roleString = principal?.payload?.getClaim("role")?.asString()
        val userRole = roleString?.let { UserRole.fromString(it) }
        if (userRole == null || userRole !in requiredRoles) {
            call.respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(code = "USER_FORBIDDEN", message = "Insufficient permissions"),
            )
        }
    }
}

fun Route.withRole(vararg roles: UserRole, build: Route.() -> Unit) {
    val authorizedRoute = createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int) =
            RouteSelectorEvaluation.Transparent
        override fun toString() = "(roles: ${roles.joinToString { it.value }})"
    })
    authorizedRoute.install(RoleAuthorizationPlugin) { this.roles = roles.toSet() }
    authorizedRoute.build()
}
```

### Anti-Patterns to Avoid

- **Keeping string roles anywhere in the call chain:** The goal is compile-time safety. If `withRole("ADMIN")` compiles but `withRole("AMDIN")` also compiles silently, you have no safety. Use `withRole(UserRole.Admin)`.
- **Dual role systems (UserTier + UserRole):** The current `UserTier` has Free/Paid/Premium/Admin/PowerAdmin -- some of these are subscription tiers, some are access roles. This phase should clarify: `UserRole` is for access control (User/Admin/PowerAdmin), `UserTier` remains for subscription differentiation (Free/Paid/Premium). They are orthogonal concerns.
- **Polymorphic JSON serialization for roles:** Default sealed class serialization emits `{"type": "com.m2f.template.models.UserRole.Admin"}` -- too verbose for a JWT claim or wire field. Use `@SerialName("ADMIN")` on each variant and a custom serializer or just use the `value` string property.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Role string validation | Manual if/when checks scattered across code | `UserRole.fromString()` in one place | Single source of truth, exhaustive when |
| FK migration | Manual SQL string concatenation | `SchemaUtils.addMissingColumnsStatements()` + `.references()` | Exposed generates correct DDL per dialect |
| JWT role encoding | Custom claim serializer | `UserRole.value` string into JWT claim + `UserRole.fromString()` on read | Simple, stateless, no custom serializer needed |
| Row counting | Manual SQL `SELECT COUNT(*)` string | `UsersTable.selectAll().count()` | Exposed DSL, type-safe, R2DBC compatible |

**Key insight:** The JWT wire format should remain a simple string (the `value` property of `UserRole`). Attempting to serialize the full sealed class hierarchy into JWT claims adds complexity with no benefit -- the server can always reconstruct the typed role from the string via `fromString()`.

## Common Pitfalls

### Pitfall 1: Migration Order -- FK Before Data Exists
**What goes wrong:** Adding a NOT NULL FK column to users before the roles table has data causes constraint violations.
**Why it happens:** The FK constraint validates on insert/update, and existing rows have no valid `role_id`.
**How to avoid:** Multi-step migration: (1) create roles table, (2) seed roles, (3) add role_id as nullable, (4) backfill, (5) alter to NOT NULL + FK.
**Warning signs:** Migration fails with "violates foreign key constraint" or "violates not-null constraint."

### Pitfall 2: Exposed R2DBC SchemaUtils.addMissingColumnsStatements Limitations
**What goes wrong:** `addMissingColumnsStatements()` may not generate correct ALTER TABLE for all column modifications with R2DBC.
**Why it happens:** R2DBC support in Exposed 1.0 is newer than JDBC; some SchemaUtils edge cases may not be fully covered.
**How to avoid:** Test the migration against a real PostgreSQL database (not just H2). If `addMissingColumnsStatements()` fails, fall back to keeping the varchar column and doing application-level mapping.
**Warning signs:** Migration produces empty statement list or incorrect DDL.

### Pitfall 3: UserTier vs UserRole Confusion
**What goes wrong:** Removing or renaming UserTier breaks profile UI (exhaustive `when` expressions in ProfileScreen, ProfileSidebar).
**Why it happens:** UserTier serves double duty -- both access control AND subscription tier display.
**How to avoid:** Keep `UserTier` for subscription/display purposes. Create `UserRole` separately for access control. Map between them where needed (e.g., Admin UserTier implies Admin UserRole).
**Warning signs:** Compilation errors in `app/profile` composables.

### Pitfall 4: JWT Claim Backward Compatibility
**What goes wrong:** Existing JWTs in the wild contain `"role": "USER"` as a string. Changing the claim format breaks active sessions.
**Why it happens:** The JWT `role` claim is currently a plain string. If the new system expects a different format, existing tokens won't decode.
**How to avoid:** Keep the JWT claim as a plain string (`UserRole.value`). The `fromString()` function handles backward compatibility. No JWT format change needed.
**Warning signs:** 401/403 errors after deployment for users with existing tokens.

### Pitfall 5: Circular Module Dependency
**What goes wrong:** `core:models` (KMP) cannot depend on `server:auth` (JVM-only), but the roles table is in `server:auth`.
**Why it happens:** `UserRole` must be in `core:models` for client visibility, but `RolesTable` must be in `server:auth` for DB access.
**How to avoid:** `UserRole` sealed class lives in `core:models`. `RolesTable` lives in `server:auth:tables`. They share the role `value` string as their contract -- no direct code dependency needed.
**Warning signs:** Gradle build fails with circular dependency error.

### Pitfall 6: Exposed R2DBC count() Returns Long via Suspend
**What goes wrong:** Calling `UsersTable.selectAll().count()` outside a `suspendTransaction` block throws "No transaction in context."
**Why it happens:** All Exposed R2DBC operations require a transaction context.
**How to avoid:** Always wrap in `suspendTransaction(db = db) { UsersTable.selectAll().count() }`.
**Warning signs:** Runtime exception: "No transaction in context."

## Code Examples

### UserRole Sealed Class (core:models)
```kotlin
// Source: Mirrors existing UserTier.kt pattern in the codebase
package com.m2f.template.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class UserRole {
    abstract val value: String
    abstract val level: Int

    @Serializable @SerialName("USER")
    data object User : UserRole() {
        override val value: String = "USER"
        override val level: Int = 0
    }

    @Serializable @SerialName("ADMIN")
    data object Admin : UserRole() {
        override val value: String = "ADMIN"
        override val level: Int = 1
    }

    @Serializable @SerialName("POWER_ADMIN")
    data object PowerAdmin : UserRole() {
        override val value: String = "POWER_ADMIN"
        override val level: Int = 2
    }

    companion object {
        fun fromString(role: String): UserRole = when (role.uppercase()) {
            "USER" -> User
            "ADMIN" -> Admin
            "POWER_ADMIN", "POWERADMIN" -> PowerAdmin
            else -> User
        }

        val entries: List<UserRole> = listOf(User, Admin, PowerAdmin)
    }
}
```

### RolesTable Definition
```kotlin
// Source: follows RefreshTokensTable/UsersTable pattern
package com.m2f.server.auth.tables

import org.jetbrains.exposed.v1.core.Table

object RolesTable : Table("roles") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50).uniqueIndex()
    val level = integer("level")
    override val primaryKey = PrimaryKey(id)
}
```

### Updated UsersTable with FK
```kotlin
// Source: follows RefreshTokensTable.references() pattern
object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val email = varchar("email", EMAIL_LENGTH).uniqueIndex()
    val passwordHash = varchar("password_hash", PASSWORD_HASH_LENGTH)
    val name = varchar("name", NAME_LENGTH)
    val roleId = integer("role_id").references(RolesTable.id)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}
```

### UserRepository Count Query
```kotlin
// Source: Exposed R2DBC Query.count() API
suspend fun count(): Long = suspendTransaction(db = db) {
    UsersTable.selectAll().count()
}
```

### Updated UserTools.getUserCount()
```kotlin
@Tool
@LLMDescription("Count the total number of registered users in the system")
suspend fun getUserCount(): String {
    val count = userRepository.count()
    return "Total registered users: $count"
}
```

### JWT Role Encoding (unchanged wire format)
```kotlin
// JwtTokenProvider -- role parameter changes from String to UserRole
fun generateAccessToken(userId: String, role: UserRole): String =
    JWT.create()
        .withSubject(userId)
        .withClaim("role", role.value)  // still a plain string on the wire
        .withAudience(audience)
        .withIssuer(issuer)
        .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiry))
        .sign(Algorithm.HMAC256(secret))
```

### UserResponse with Typed Role
```kotlin
@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,  // Changed from String to UserRole
)
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `role: String` in UserResponse | `role: UserRole` sealed type | This phase | Wire format changes from `"role":"USER"` to `"role":"USER"` (same value, typed deserialization) |
| `withRole("ADMIN")` string-based | `withRole(UserRole.Admin)` typed | This phase | Compile-time safety for RBAC |
| `varchar("role")` in UsersTable | `integer("role_id").references(RolesTable.id)` | This phase | DB referential integrity |
| `getUserCount()` static string | `getUserCount()` actual DB count | This phase | AI tool returns real data |

**Deprecated/outdated:**
- `UserResponse.tier` extension property: May need updating if `UserResponse.role` changes from String to UserRole. The `tier` extension maps role string to UserTier -- with typed roles, this mapping still works but the source type changes.

## Open Questions

1. **UserTier vs UserRole relationship**
   - What we know: UserTier has Free/Paid/Premium/Admin/PowerAdmin. UserRole will have User/Admin/PowerAdmin.
   - What's unclear: Should UserTier's Admin/PowerAdmin variants be removed since UserRole handles access control? Or keep them for backward compatibility?
   - Recommendation: Keep both -- UserTier for subscription display, UserRole for access control. Admin/PowerAdmin exist in both (orthogonal concerns). Tier determines UI features shown; Role determines API access. The `UserResponse` could carry both `role: UserRole` and `tier: UserTier` (or the tier extension can remain).

2. **Migration approach for existing users table**
   - What we know: `SchemaUtils.addMissingColumnsStatements()` should handle ALTER TABLE with R2DBC. The project's existing migration pattern uses `SchemaUtils.create()` for new tables.
   - What's unclear: Whether `addMissingColumnsStatements()` correctly generates ALTER TABLE + FK for PostgreSQL via R2DBC. The R2DBC SchemaUtils support is newer.
   - Recommendation: Try `addMissingColumnsStatements()` first. If it fails, use Approach B (keep varchar, map at application layer). Both provide compile-time safety; only DB-level integrity differs.

3. **@SerialName vs custom serializer for UserRole wire format**
   - What we know: Default sealed class serialization adds `"type"` discriminator. `@SerialName` on each variant controls the discriminator value.
   - What's unclear: Whether `UserResponse(role = UserRole.Admin)` serializes as `"role":"ADMIN"` (flat string) or `"role":{"type":"ADMIN"}` (nested object).
   - Recommendation: Use a simple custom serializer that maps UserRole to/from its `value` string. This keeps wire format backward-compatible as a plain string: `"role":"ADMIN"`. Alternatively, change `UserResponse.role` to `UserRole` with `@Serializable(with = UserRoleSerializer::class)` to get flat string output.

## Sources

### Primary (HIGH confidence)
- Codebase inspection: `UsersTable.kt`, `UserTier.kt`, `RoleAuthorization.kt`, `RefreshTokensTable.kt`, `Auth.kt` -- verified current implementation patterns
- Codebase inspection: `UserRepository.kt`, `AuthService.kt`, `OAuthService.kt`, `JwtTokenProvider.kt` -- verified all string role usage points
- Codebase inspection: `UserTools.kt`, `ChatStreamingStrategy.kt` -- verified tech debt items
- [Exposed Working with Tables](https://www.jetbrains.com/help/exposed/working-with-tables.html) -- FK `reference()` API
- [Exposed reference() API](https://jetbrains.github.io/Exposed/api/exposed-core/org.jetbrains.exposed.v1.core/-table/reference.html) -- overload signatures
- [Exposed Migrations](https://www.jetbrains.com/help/exposed/migrations.html) -- `addMissingColumnsStatements()` documentation
- [Exposed R2DBC Query API](https://jetbrains.github.io/Exposed/api/exposed-r2dbc/org.jetbrains.exposed.v1.r2dbc/index.html) -- `selectAll()`, `count()` on Query

### Secondary (MEDIUM confidence)
- [Exposed 1.0 Migration Guide](https://www.jetbrains.com/help/exposed/migration-guide-1-0-0.html) -- v1 package paths
- [Exposed CRUD Operations](https://www.jetbrains.com/help/exposed/dsl-crud-operations.html) -- selectAll/count patterns
- [kotlinx.serialization SealedClassSerializer](https://kotlinlang.org/api/kotlinx.serialization/kotlinx-serialization-core/kotlinx.serialization/-sealed-class-serializer/) -- sealed class wire format

### Tertiary (LOW confidence)
- [Exposed R2DBC SchemaUtils completeness](https://github.com/JetBrains/Exposed/pull/2430) -- R2DBC SchemaUtils testing PR (may not cover all ALTER TABLE scenarios)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH -- no new dependencies, all patterns verified in codebase
- Architecture: HIGH -- follows existing UserTier, RefreshTokensTable, and Auth.kt migration patterns
- Pitfalls: HIGH -- identified from direct codebase inspection of all touch points
- Migration strategy: MEDIUM -- `addMissingColumnsStatements()` with R2DBC is less battle-tested; fallback approach documented
- Serialization wire format: MEDIUM -- @SerialName approach needs validation that it produces flat string, not nested object

**Research date:** 2026-02-15
**Valid until:** 2026-03-15 (stable -- Exposed 1.0 is a release version)
