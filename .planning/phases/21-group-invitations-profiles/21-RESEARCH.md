# Phase 21: Group Invitations & Profiles - Research

**Researched:** 2026-02-26
**Domain:** Invitation management (list/revoke), avatar verification, integration testing
**Confidence:** HIGH

## Summary

Phase 21 closes the remaining gaps in the invitation and profile feature set. The bulk of the work is already implemented: invitation create/accept/get endpoints, avatar upload with S3 storage, TerminalAvatar with Coil3 AsyncImage, and the full client-side acceptance flow. What remains is: (1) a list/revoke invitations API and admin UI section (INVITE-03), (2) verification that existing avatar and invitation features work end-to-end (PROF-03, PROF-04), and (3) comprehensive integration tests covering the full auth-to-invite-to-accept lifecycle against Testcontainers PostgreSQL (DEBT-01).

The codebase has well-established patterns for all three tasks. Server routes use the `conduitAuth` / Arrow Raise pattern, the SDK uses `Either<AppError, T>`, and tests use JUnit 4 + Testcontainers + Kotest assertions + Ktor testApplication. The admin panel follows the MVI pattern (Intent/Model/Mutation/Event) with modal dialogs for actions. No new libraries are needed -- this phase composes existing primitives.

**Primary recommendation:** Extend the existing InvitationRepository, InvitationService, InvitationRoutes, InvitationApi, and AdminPanel using the patterns already established in the codebase. Write integration tests in `server/groups/src/test/` reusing the TestHelpers infrastructure.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Pending invitations list lives as a new section/tab inside AdminPanelScreen (not a separate screen)
- Each invitation row shows: invitee email, assigned role badge, and expiry countdown ("Expires in 2 days") or "Expired" label
- Expired invitations remain visible but with visual distinction (dimmed/strikethrough + "Expired" badge)
- Revoke flow uses a confirmation dialog: "Revoke invitation to user@example.com?" with Cancel/Revoke buttons
- Integration test priority: Full invitation lifecycle test (auth -> create group -> invite user -> register/accept -> verify membership)
- Include avatar upload round-trip test (upload image -> verify avatarUrl in profile response)
- Reuse existing Testcontainers + Kotest pattern from Phase 19 integration tests

### Claude's Discretion
- Server endpoint design for list/revoke invitations (REST conventions)
- SDK method signatures for new invitation operations
- AdminPanel layout details for the pending invitations section (spacing, card design)
- Whether to add DELETE avatar endpoint (nice-to-have, not required)
- Test utility setup and shared fixtures

### Deferred Ideas (OUT OF SCOPE)
- Bulk invitation (invite multiple users at once) -- future phase
- Re-send expired invitation -- future phase
- Invitation analytics/history dashboard -- future phase
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| INVITE-01 | Admin can invite a user to a group by email -- server generates token, sends invite email with link | **Already complete.** POST /api/groups/{groupId}/invitations/create exists with full token generation + email send via InvitationService. No work needed. |
| INVITE-02 | Recipient can accept invite via token link -- creates/activates account and joins group with specified role | **Already complete (Phase 18.2).** POST /api/invitations/accept + InviteAcceptScreen + LoginViewModel integration all exist. No work needed. |
| INVITE-03 | Admin can view pending invitations for their group and revoke them | **NEW WORK REQUIRED.** Need: (1) `findByGroupId()` in InvitationRepository, (2) `listInvitations()` + `revokeInvitation()` in InvitationService, (3) new routes GET /api/groups/{groupId}/invitations + POST /api/groups/{groupId}/invitations/{invitationId}/revoke, (4) SDK methods `listInvitations()` + `revokeInvitation()`, (5) AdminPanel UI section with invitation table and revoke dialog. See Architecture Patterns section. |
| INVITE-04 | Invite tokens expire after configurable duration and cannot be reused | **Already complete (Phase 18.2).** 7-day expiry in InvitationService.INVITATION_EXPIRY, expiry check in acceptInvitation(), isExpired computed in toResponse(). No work needed. |
| PROF-01 | UsersTable has avatarUrl column and UserResponse DTO includes avatarUrl field | **Already complete.** UserResponse has `val avatarUrl: String? = null`. No work needed. |
| PROF-02 | User can upload a profile image via PUT /api/users/me/avatar endpoint | **Already complete.** AvatarRoutes.kt with multipart upload to S3 via FileService + avatarUrl update in UserRepository. No work needed. |
| PROF-03 | TerminalAvatar component displays image when avatarUrl is present, falls back to initials | **Verify only.** TerminalAvatar.kt already supports `imageUrl: String?` parameter with Coil3 AsyncImage + initials fallback. Need integration test to verify end-to-end (upload -> profile response includes URL). |
| PROF-04 | ProfileViewModel supports avatar upload intent and reflects avatar URL in state | **Verify only.** ProfileViewModel handles ImageSelected/CropConfirmed intents, calls sdk.uploadAvatar(), and updates state via SetAvatarUrl mutation. ProfileModel has avatarUrl field. Need integration test. |
| DEBT-01 | Integration tests cover auth + groups + invite flow end-to-end with Testcontainers PostgreSQL | **NEW WORK REQUIRED.** Full lifecycle integration test: register admin -> create group -> invite user by email -> register invitee -> accept invitation -> verify membership. Plus avatar upload round-trip test. See Architecture Patterns section. |
</phase_requirements>

## Standard Stack

### Core (Already in project -- no new dependencies)
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Ktor Server | 3.x | HTTP routes, type-safe resources, testApplication | Already used throughout server |
| Exposed R2DBC | v1 | Database access via R2dbcDatabase + suspendTransaction | All repositories use this pattern |
| Arrow Core | latest | Either<AppError, T>, Raise context for domain errors | All services use this pattern |
| Koin | latest | Dependency injection for repositories, services | groupModule already wires InvitationRepository/Service |
| Testcontainers | 2.0.3 | PostgreSQL container for integration tests | Already used in GroupRoutesTest |
| Kotest | 6.1.3 | Assertions (shouldBe, shouldNotBe) | Already used in all server tests |
| JUnit 4 | - | Test runner with @Test, @BeforeClass/@AfterClass | Already used in GroupRoutesTest |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| kotlinx-datetime | - | Clock.System.now(), LocalDateTime for expiry calculations | Already used in InvitationService |
| Coil3 | - | AsyncImage for avatar display | Already used in TerminalAvatar |

### Alternatives Considered
None -- this phase uses exclusively existing stack. No new dependencies needed.

## Architecture Patterns

### Recommended Structure for New Code
```
server/groups/src/main/kotlin/com/m2f/server/groups/
├── repository/InvitationRepository.kt   # ADD: findByGroupId(), revokeById()
├── service/InvitationService.kt          # ADD: listInvitations(), revokeInvitation()
├── routes/InvitationRoutes.kt            # ADD: GET list, POST revoke routes
├── errors/InvitationErrors.kt            # ADD: InvitationAlreadyRevoked error

core/models/src/commonMain/kotlin/.../
├── dto/InvitationDtos.kt                 # ADD: RevokeInvitationResponse (or reuse map)
├── routes/ApiRoutes.kt                   # ADD: Groups.ListInvitations, Groups.RevokeInvitation

core/sdk/src/commonMain/kotlin/.../
├── api/InvitationApi.kt                  # ADD: listInvitations(), revokeInvitation()
├── api/InvitationApiImpl.kt              # ADD: implementations

app/admin/src/commonMain/kotlin/.../
├── AdminPanelModel.kt                    # ADD: invitations list, loading state
├── AdminPanelIntent.kt                   # ADD: LoadInvitations, RevokeInvitation, ConfirmRevoke
├── AdminPanelMutation.kt                 # ADD: SetInvitations, SetRevoking, etc.
├── AdminPanelViewModel.kt                # ADD: handleLoadInvitations(), handleRevokeInvitation()
├── AdminPanelScreen.kt                   # ADD: Pending Invitations section + RevokeDialog

server/groups/src/test/kotlin/.../
├── InvitationRoutesTest.kt               # NEW: Full lifecycle integration test
```

### Pattern 1: List Invitations Endpoint
**What:** GET endpoint to retrieve pending invitations for a group, with auth + role check.
**When to use:** Admin wants to see who has been invited but hasn't yet accepted.
**Example:**
```kotlin
// In ApiRoutes.kt -- follows existing Groups.CreateInvitation pattern
@Serializable @Resource("{groupId}/invitations")
class ListInvitations(val parent: Groups = Groups(), val groupId: String)

@Serializable @Resource("{groupId}/invitations/{invitationId}/revoke")
class RevokeInvitation(val parent: Groups = Groups(), val groupId: String, val invitationId: String)

// In InvitationRoutes.kt -- inside existing authenticate + withRole block
get<Groups.ListInvitations> { route ->
    conduitAuth { userId ->
        val role = getUserRole()
        invitationService.listInvitations(route.groupId, userId, role)
    }
}

post<Groups.RevokeInvitation> { route ->
    conduitAuth { userId ->
        val role = getUserRole()
        invitationService.revokeInvitation(route.groupId, route.invitationId, userId, role)
    }
}
```

### Pattern 2: Revoke via revokedAt Column
**What:** Soft-revoke by adding a `revokedAt` column to InvitationsTable instead of DELETE.
**When to use:** Keeps audit trail (expired invitations already remain visible per user decision).
**Example:**
```kotlin
// In InvitationsTable.kt -- add column
val revokedAt = datetime("revoked_at").nullable()

// Migration (new version string after existing ones)
class AddRevokedAtColumnMigration : Migration {
    override val version: String = "20260226000001"
    override val description: String = "Add revoked_at column to invitations table"
    override suspend fun migrate() {
        // ALTER TABLE invitations ADD COLUMN revoked_at TIMESTAMP NULL
        TransactionManager.current().exec("ALTER TABLE invitations ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP NULL")
    }
}

// In InvitationRepository.kt
suspend fun findByGroupId(groupId: Uuid): List<InvitationRecord> = suspendTransaction(db = db) {
    InvitationsTable
        .select(InvitationsTable.columns)
        .where { InvitationsTable.groupId eq groupId }
        .toList()
        .map { it.toInvitationRecord() }
}

suspend fun revokeById(id: Uuid): Boolean = suspendTransaction(db = db) {
    val now = java.time.LocalDateTime.now().toKotlinLocalDateTime()
    InvitationsTable.update({ InvitationsTable.id eq id }) {
        it[revokedAt] = now
    } > 0
}
```

### Pattern 3: Integration Test Structure (GroupRoutesTest pattern)
**What:** Full lifecycle test using Testcontainers + Ktor testApplication + Koin DI.
**When to use:** DEBT-01 integration tests.
**Example:**
```kotlin
// Follows existing GroupRoutesTest.kt pattern exactly
class InvitationRoutesTest {
    companion object {
        private lateinit var database: R2dbcDatabase
        @BeforeClass @JvmStatic fun setup() { TestDatabase.start(); database = TestDatabase.createDatabase() }
        @AfterClass @JvmStatic fun teardown() { TestDatabase.stop() }
    }

    @Test
    fun `full invitation lifecycle - create invite, register, accept, verify membership`() = runTest {
        invitationTestApp(database) {
            // 1. Create admin user + JWT token
            // 2. Create group (admin becomes owner)
            // 3. Invite user@example.com (POST /api/groups/{groupId}/invitations/create)
            // 4. Verify invitation appears in list (GET /api/groups/{groupId}/invitations)
            // 5. Register invitee (POST /api/auth/register)
            // 6. Create invitee JWT token
            // 7. Get invitation details (GET /api/invitations/{token})
            // 8. Accept invitation (POST /api/invitations/accept)
            // 9. Verify membership (GET /api/groups/{groupId}/members)
        }
    }
}
```

### Pattern 4: AdminPanel MVI Extension (Pending Invitations Section)
**What:** Add invitations list as a section below the members table in AdminPanelScreen.
**When to use:** INVITE-03 admin UI.
**Example:**
```kotlin
// AdminPanelModel.kt additions
data class AdminPanelModel(
    // ... existing fields ...
    val invitations: List<InvitationResponse> = emptyList(),
    val isLoadingInvitations: Boolean = false,
    val showRevokeDialog: Boolean = false,
    val revokeTarget: InvitationResponse? = null,
    val isRevoking: Boolean = false,
)

// AdminPanelIntent.kt additions
sealed interface AdminPanelIntent {
    // ... existing intents ...
    data object LoadInvitations : AdminPanelIntent
    data class ConfirmRevokeInvitation(val invitation: InvitationResponse) : AdminPanelIntent
    data object CancelRevoke : AdminPanelIntent
    data object ExecuteRevoke : AdminPanelIntent
}
```

### Pattern 5: Expiry Countdown Display
**What:** Format expiration as human-readable countdown ("Expires in 2 days") or "Expired".
**When to use:** Invitation row in admin panel.
**Example:**
```kotlin
// Common utility -- parse expiresAt string and calculate relative time
fun formatExpiry(expiresAt: String, isExpired: Boolean): String {
    if (isExpired) return "Expired"
    // Parse ISO datetime, compute days until expiry from Clock.System.now()
    val expiry = Instant.parse(expiresAt) // or LocalDateTime parsing
    val now = Clock.System.now()
    val remaining = expiry - now
    val days = remaining.inWholeDays
    return when {
        days > 1 -> "Expires in $days days"
        days == 1L -> "Expires tomorrow"
        else -> "Expires today"
    }
}
```

### Anti-Patterns to Avoid
- **Hard DELETE for revocation:** Don't delete invitation rows. Use revokedAt column to preserve audit trail (user decision: expired invitations stay visible).
- **Separate screen for invitations:** Decision is locked -- invitations section goes inside AdminPanelScreen, not a separate navigation destination.
- **Reimplementing existing features:** Avatar upload, invitation create/accept/get, TerminalAvatar image display are all already implemented. Only extend, don't rebuild.
- **Skipping migration for new column:** Adding `revokedAt` to InvitationsTable requires a database migration, not just schema change in Exposed.
- **Using Kotest spec style for server tests:** Existing server tests use JUnit 4 `@Test` with Kotest assertions (shouldBe). Don't switch to Kotest spec runner -- keep consistency.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| JWT token generation for tests | Custom token builder | `createTestToken()` from TestHelpers.kt | Already handles secret/audience/issuer/claims correctly |
| Test database setup | Manual PostgreSQL config | `TestDatabase` object from TestHelpers.kt | Testcontainers lifecycle already managed |
| Test application wiring | Manual Koin + Ktor setup | `groupTestApp()` pattern from TestHelpers.kt | Handles migration, Koin start/stop, security config |
| Invitation response mapping | Manual field mapping | Existing `InvitationRecord.toResponse()` | Already handles isExpired/isAccepted computation |
| Error handling in routes | try/catch blocks | `conduitAuth` + Arrow Raise | Established pattern throughout all routes |

**Key insight:** Every pattern needed for this phase already exists in the codebase. The risk is introducing inconsistency, not missing functionality.

## Common Pitfalls

### Pitfall 1: Missing Migration for revokedAt Column
**What goes wrong:** Adding `revokedAt` to InvitationsTable Exposed definition without a migration causes schema mismatch -- table doesn't have the column in existing databases.
**Why it happens:** Exposed's SchemaUtils.create only runs on table creation, not ALTER TABLE.
**How to avoid:** Create a new Migration class (e.g., `AddRevokedAtColumnMigration`) with raw SQL `ALTER TABLE invitations ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP NULL`. Register it in `registerGroupMigrations()`.
**Warning signs:** Test passes (fresh DB) but dev/staging fails (existing DB).

### Pitfall 2: Revoke Authorization Must Match Create Authorization
**What goes wrong:** Revoke endpoint allows any authenticated user, not just group admin/owner.
**Why it happens:** Forgetting to replicate the `withRole(UserRole.Admin, UserRole.PowerAdmin)` + group-level RBAC check.
**How to avoid:** Revoke route must live inside the same `authenticate { withRole(...) { ... } }` block as create. InvitationService.revokeInvitation() must call `requireGroupRole()`.
**Warning signs:** Non-admin user can revoke invitations in tests.

### Pitfall 3: Test Isolation with Shared Testcontainers Database
**What goes wrong:** Tests interfere with each other when they create users/groups with the same emails/slugs.
**Why it happens:** Single database container shared across all tests via `@BeforeClass/@AfterClass`. No table truncation between tests.
**How to avoid:** Use unique emails/slugs per test (e.g., `"admin-invite-lifecycle@test.com"`, `"invite-lifecycle-group"`). Existing GroupRoutesTest already follows this pattern.
**Warning signs:** Tests pass individually but fail when run together.

### Pitfall 4: InvitationResponse Token Exclusion
**What goes wrong:** The list endpoint might inadvertently return invitation tokens, which should only be sent via email.
**Why it happens:** InvitationResponse already excludes the token field (by design), but a new DTO could accidentally include it.
**How to avoid:** Reuse existing `InvitationResponse` which already omits the `token` field. The `InvitationRecord` has the token but `toResponse()` doesn't map it.
**Warning signs:** Token visible in API response JSON.

### Pitfall 5: Invitation Filtering for Revoked Status
**What goes wrong:** List endpoint returns revoked invitations without any indication they were revoked.
**Why it happens:** Adding `revokedAt` column but not including `isRevoked` in InvitationResponse.
**How to avoid:** Add `isRevoked: Boolean` field to InvitationResponse and compute it from `revokedAt != null`. Update the toResponse() mapper.
**Warning signs:** Revoked invitations look the same as pending ones in the admin UI.

### Pitfall 6: Integration Test Needs Invitation Token Access
**What goes wrong:** Integration test can't complete the accept flow because InvitationResponse doesn't include the token.
**Why it happens:** By design, the API doesn't expose tokens. But integration tests need the token to simulate the accept flow.
**How to avoid:** In the test, query the token directly from the database via InvitationRepository.findByGroupId() or a test-only helper. Alternatively, use the InvitationRepository directly in the test to extract the token from the InvitationRecord.
**Warning signs:** Test can create invitation but can't accept it because token is unknown.

### Pitfall 7: Email Service in Tests
**What goes wrong:** InvitationService.createInvitation() calls emailService.sendEmail(), which fails in test environment without SMTP.
**Why it happens:** TestHelpers don't configure a mock EmailService.
**How to avoid:** Register a no-op EmailService in the test Koin module: `single<EmailService> { object : EmailService { override suspend fun sendEmail(...) {} } }`. Or use GreenMail (already a project dependency pattern from Phase 18).
**Warning signs:** Test crashes with SMTP connection refused.

## Code Examples

### Example 1: Repository findByGroupId
```kotlin
// InvitationRepository.kt -- follows existing findByToken() pattern
suspend fun findByGroupId(groupId: Uuid): List<InvitationRecord> = suspendTransaction(db = db) {
    InvitationsTable
        .select(InvitationsTable.columns)
        .where { InvitationsTable.groupId eq groupId }
        .toList()
        .map { it.toInvitationRecord() }
}
```

### Example 2: Service listInvitations with Auth
```kotlin
// InvitationService.kt -- follows existing getInvitation() pattern
context(raise: Raise<DomainError>)
suspend fun listInvitations(
    groupId: String,
    userId: String,
    userRole: UserRole,
): List<InvitationResponse> {
    val gid = Uuid.parse(groupId)
    val uid = Uuid.parse(userId)

    val group = groupRepository.findById(gid)
    raise.ensure(group != null) { GroupNotFound() }

    // Authorization: ADMIN/OWNER or PowerAdmin
    if (userRole != UserRole.PowerAdmin) {
        requireGroupRole(uid, gid, GroupRole.Admin)
    }

    val invitations = invitationRepository.findByGroupId(gid)
    val inviterNames = mutableMapOf<Uuid, String>()

    return invitations.map { invitation ->
        val inviterName = inviterNames.getOrPut(invitation.invitedBy) {
            userRepository.findById(invitation.invitedBy)?.name ?: "A team member"
        }
        invitation.toResponse(groupName = group.name, inviterName = inviterName)
    }
}
```

### Example 3: Service revokeInvitation
```kotlin
// InvitationService.kt
context(raise: Raise<DomainError>)
suspend fun revokeInvitation(
    groupId: String,
    invitationId: String,
    userId: String,
    userRole: UserRole,
): Map<String, String> {
    val gid = Uuid.parse(groupId)
    val uid = Uuid.parse(userId)
    val iid = Uuid.parse(invitationId)

    val group = groupRepository.findById(gid)
    raise.ensure(group != null) { GroupNotFound() }

    if (userRole != UserRole.PowerAdmin) {
        requireGroupRole(uid, gid, GroupRole.Admin)
    }

    val invitation = invitationRepository.findById(iid)
    raise.ensure(invitation != null) { InvitationNotFound() }
    raise.ensure(invitation.groupId == gid) { InvitationNotFound() }
    raise.ensure(invitation.acceptedAt == null) { InvitationAlreadyAccepted() }

    invitationRepository.revokeById(iid)
    return mapOf("message" to "Invitation revoked")
}
```

### Example 4: SDK Methods
```kotlin
// InvitationApi.kt additions
suspend fun listInvitations(groupId: String): Either<AppError, List<InvitationResponse>>
suspend fun revokeInvitation(groupId: String, invitationId: String): Either<AppError, Unit>

// InvitationApiImpl.kt
override suspend fun listInvitations(groupId: String): Either<AppError, List<InvitationResponse>> =
    apiCall { client.get(Groups.ListInvitations(groupId = groupId)) }

override suspend fun revokeInvitation(groupId: String, invitationId: String): Either<AppError, Unit> =
    apiCall { client.post(Groups.RevokeInvitation(groupId = groupId, invitationId = invitationId)) }
```

### Example 5: Integration Test - Full Lifecycle
```kotlin
@Test
fun `full invitation lifecycle`() = runTest {
    invitationTestApp(database) {
        val userRepo = getKoin().get<UserRepository>()
        val invitationRepo = getKoin().get<InvitationRepository>()

        // Setup: admin + group
        val adminId = createTestUser(userRepo, "admin-lifecycle@test.com", "Admin", UserRole.Admin)
        val adminToken = createTestToken(adminId.toString(), UserRole.Admin)
        val client = createClient { install(ContentNegotiation) { json() } }

        // Create group
        val groupResp = client.post("/api/groups/create") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(CreateGroupRequest(name = "Lifecycle Group", slug = "lifecycle-group"))
        }.body<GroupResponse>()

        // Invite user
        val inviteResp = client.post("/api/groups/${groupResp.id}/invitations/create") {
            bearerAuth(adminToken)
            contentType(ContentType.Application.Json)
            setBody(CreateInvitationRequest(email = "invitee-lifecycle@test.com"))
        }
        inviteResp.status shouldBe HttpStatusCode.Created

        // Get token from DB (not exposed in API by design)
        val invitations = invitationRepo.findByGroupId(Uuid.parse(groupResp.id))
        val token = invitations.first().token

        // Register invitee + accept
        val inviteeId = createTestUser(userRepo, "invitee-lifecycle@test.com", "Invitee", UserRole.User)
        val inviteeToken = createTestToken(inviteeId.toString(), UserRole.User)

        val acceptResp = client.post("/api/invitations/accept") {
            bearerAuth(inviteeToken)
            contentType(ContentType.Application.Json)
            setBody(AcceptInvitationRequest(token = token))
        }
        acceptResp.status shouldBe HttpStatusCode.OK

        // Verify membership
        val membersResp = client.get("/api/groups/${groupResp.id}/members") {
            bearerAuth(adminToken)
        }.body<PaginatedMemberResponse>()
        membersResp.items.size shouldBe 2
        membersResp.items.any { it.email == "invitee-lifecycle@test.com" } shouldBe true
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Hard DELETE invitation rows | Soft-delete via revokedAt timestamp | This phase | Preserves audit trail per user decision |
| No invitation management UI | Pending invitations section in AdminPanel | This phase | Admins can see and manage outstanding invitations |

**No deprecated/outdated patterns to worry about** -- all existing code is current.

## Open Questions

1. **InvitationResponse isRevoked field**
   - What we know: Need to add `isRevoked: Boolean` to InvitationResponse and `revokedAt` to InvitationRecord/InvitationsTable
   - What's unclear: Should revoked invitations be filtered out from the list or shown with visual distinction? CONTEXT.md only specifies "expired" visual distinction.
   - Recommendation: Show revoked invitations with same dimmed/strikethrough treatment as expired ones, plus a "Revoked" badge. This matches the audit trail philosophy (expired stay visible, revoked should too).

2. **Revoke already-accepted invitations**
   - What we know: Accepted invitations have `acceptedAt != null`. Revoking an already-accepted invitation doesn't make sense (user is already in the group).
   - Recommendation: Block revoking accepted invitations with InvitationAlreadyAccepted error. Separately, user can be removed via existing removeMember endpoint.

3. **Whether to filter revokedAt in acceptInvitation**
   - What we know: A revoked invitation should not be acceptable.
   - Recommendation: Add a check in `acceptInvitation()`: `raise.ensure(invitation.revokedAt == null) { InvitationNotFound() }` (or a new InvitationRevoked error).

## Sources

### Primary (HIGH confidence)
- Codebase inspection: All files read directly from `/Users/marc/IdeaProjects/Template/`
  - InvitationRoutes.kt, InvitationService.kt, InvitationRepository.kt, InvitationsTable.kt
  - AdminPanelScreen.kt, AdminPanelViewModel.kt, AdminPanelModel.kt, AdminPanelIntent.kt, AdminPanelMutation.kt
  - GroupRoutesTest.kt, TestHelpers.kt (test patterns)
  - InvitationApi.kt, InvitationApiImpl.kt, InvitationDtos.kt
  - ApiRoutes.kt (type-safe routes), Sdk.kt, AppError.kt
  - TerminalAvatar.kt, AvatarRoutes.kt, ProfileViewModel.kt, ProfileModel.kt
  - Error.kt (conduit/conduitAuth pattern)
  - groupModule.kt (Koin DI wiring)
  - libs.versions.toml (dependency versions: Testcontainers 2.0.3, Kotest 6.1.3)

### Secondary (MEDIUM confidence)
- CONTEXT.md user decisions (gathered 2026-02-26)
- REQUIREMENTS.md traceability matrix
- STATE.md project decisions and history

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - no new dependencies, all verified in codebase
- Architecture: HIGH - all patterns directly observed in existing code, extending rather than inventing
- Pitfalls: HIGH - pitfalls derived from direct codebase analysis (migration patterns, test isolation patterns, auth patterns)

**Research date:** 2026-02-26
**Valid until:** 2026-03-26 (stable -- no fast-moving dependencies)
