---
phase: 13-group-server-sdk
verified: 2026-02-19T00:00:00Z
status: gaps_found
score: 9/11 must-haves verified
gaps:
  - truth: "Integration tests verify RBAC -- regular members cannot list all members or manage the group"
    status: partial
    reason: "Test for 'group member cannot list members' exists, but 'group member cannot add members' and 'group admin can manage members but not delete group' tests are absent"
    artifacts:
      - path: "server/groups/src/test/kotlin/com/m2f/server/groups/GroupRoutesTest.kt"
        issue: "Missing test: 'group member cannot add members'; missing test: 'group admin can manage members but not delete group'"
    missing:
      - "Add @Test fun `group member cannot add members`() asserting POST /api/groups/{id}/members/add by a MEMBER role returns 403"
      - "Add @Test fun `group admin can manage members but not delete group`() asserting Admin can add members (200) but DELETE returns 403"
  - truth: "Integration tests verify member management (add, remove, list with pagination)"
    status: partial
    reason: "Tests for add and remove exist; list-members test exists but only checks 2 members -- cursor-based pagination boundary is untested"
    artifacts:
      - path: "server/groups/src/test/kotlin/com/m2f/server/groups/GroupRoutesTest.kt"
        issue: "No test for cursor-based pagination (hasMore=true/false boundary, cursor token used for next page)"
    missing:
      - "Add pagination test: create group with 12+ members, GET /members?limit=10 -> hasMore=true, cursor present; then GET with cursor -> hasMore=false"
  - truth: "Integration tests verify admin can register new user directly into group"
    status: failed
    reason: "No test exercises the POST /api/groups/{id}/members/register endpoint"
    artifacts:
      - path: "server/groups/src/test/kotlin/com/m2f/server/groups/GroupRoutesTest.kt"
        issue: "registerMember endpoint is implemented in GroupRoutes and GroupService but has no integration test"
    missing:
      - "Add @Test fun `admin can register new user into group`() calling POST /api/groups/{id}/members/register and asserting 200 with MemberResponse"
---

# Phase 13: Group Server & SDK Verification Report

**Phase Goal:** The server supports group-based user organization with CRUD operations, membership management, and data isolation -- accessible through the shared SDK
**Verified:** 2026-02-19
**Status:** gaps_found
**Re-verification:** No -- initial verification

## Goal Achievement

### Success Criteria (from ROADMAP.md)

| # | Criterion | Status | Evidence |
|---|-----------|--------|----------|
| 1 | Admin can create a group and it persists in the database with correct schema | VERIFIED | GroupsTable, GroupRepository.insert, GroupService.createGroup, test: `admin can create a group` |
| 2 | Users belong to one group and can only access their own group's data -- cross-group requests return 403 | VERIFIED | GroupService.getGroup raises GroupForbidden when user is not a member, test: `user in group A cannot access group B - returns 403` |
| 3 | GroupApi SDK functions return Either<AppError, T> using shared @Resource route definitions | VERIFIED | GroupApi interface, GroupApiImpl uses Groups.* @Resource classes, Sdk delegates via Kotlin delegation |
| 4 | Server integration tests verify auth flow, group CRUD, RBAC enforcement, and cross-group isolation using Ktor testApplication | PARTIAL | 16 tests exist covering CRUD and isolation; RBAC tests incomplete (missing member-cannot-add-members, admin-can-manage-but-not-delete); registerMember untested |

**Score:** 9/11 must-have truths verified (see detail below)

---

### Observable Truths (from Plan must_haves)

#### Plan 01 -- Shared Models

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | GroupRole sealed class exists with OWNER, ADMIN, MEMBER variants and serializes to flat strings | VERIFIED | `core/models/src/commonMain/kotlin/com/m2f/template/models/GroupRole.kt` -- sealed class with GroupRoleSerializer using PrimitiveSerialDescriptor |
| 2 | GroupResponse, CreateGroupRequest, UpdateGroupRequest, AddMemberRequest, RegisterMemberRequest, MemberResponse, PaginatedMemberResponse DTOs exist and are @Serializable | VERIFIED | All 7 types in `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/GroupDtos.kt` |
| 3 | PaginatedResponse generic wrapper exists with items, cursor, hasMore fields | VERIFIED (as PaginatedMemberResponse) | Plan called for generic PaginatedResponse.kt; implementation delivered `PaginatedMemberResponse` in GroupDtos.kt. Plan itself acknowledged this approach as the simplest practical option. Content is correct. |
| 4 | Groups @Resource route class exists with nested endpoints for CRUD, members, and registration | VERIFIED | `ApiRoutes.kt` -- Groups class with 9 nested @Resource classes: Create, ById, Update, Delete, ListAll, Members, AddMember, RemoveMember, RegisterMember |
| 5 | AppError.Group sealed class exists with NotFound, Forbidden, AlreadyExists, MemberAlreadyExists variants | VERIFIED | `AppError.kt` -- `sealed class Group : AppError()` with all 4 variants, correct codes (GROUP_NOT_FOUND, GROUP_FORBIDDEN, GROUP_ALREADY_EXISTS, GROUP_MEMBER_ALREADY_EXISTS) |

#### Plan 02 -- Server Module

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Admin can create a group via POST /api/groups/create and it persists in the database | VERIFIED | GroupService.createGroup inserts into GroupsTable, creator gets OWNER membership; GroupRoutes wires POST to Groups.Create |
| 2 | Admin can read, update, and delete groups they manage | VERIFIED | GroupService.getGroup, updateGroup, deleteGroup with RBAC checks; routes GET Groups.ById, POST Groups.Update, POST Groups.Delete |
| 3 | Power admin can list all groups across the system | VERIFIED | GroupService.listAllGroups; withRole(UserRole.PowerAdmin) guard on GET Groups.ListAll |
| 4 | Admin can add existing users to their group and remove members | VERIFIED | GroupService.addMember, removeMember; routes POST Groups.AddMember, POST Groups.RemoveMember |
| 5 | Admin can register new users directly into their group | VERIFIED | GroupService.registerMember calls AuthService.register then inserts membership; route POST Groups.RegisterMember is wired |
| 6 | Members can only read their own membership, not the full member list | VERIFIED | GroupService.getMembers requires GroupRole.Admin minimum via requireGroupRole; member-level access raises GroupForbidden |
| 7 | Cross-group access returns 403 Forbidden | VERIFIED | GroupService.getGroup: non-PowerAdmin without membership raises GroupForbidden -> HTTP 403 |
| 8 | Migration creates default group and assigns all existing users | VERIFIED | SeedDefaultGroupMigration (version 20260219000003) inserts default group and memberships for all existing users |

#### Plan 03 -- SDK

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | GroupApi interface exists with all group CRUD and member management functions returning Either<AppError, T> | VERIFIED | `GroupApi.kt` -- 9 methods, all return `Either<AppError, T>` |
| 2 | GroupApiImpl uses Ktor HttpClient with @Resource route classes for type-safe HTTP calls | VERIFIED | `GroupApiImpl.kt` -- every method uses `client.post(Groups.*)` or `client.get(Groups.*)` via resources plugin |
| 3 | Sdk facade delegates to GroupApi via Kotlin delegation | VERIFIED | `Sdk.kt`: `class Sdk(..., private val groupApi: GroupApi) : AuthApi by authApi, UserApi by userApi, GroupApi by groupApi` |
| 4 | SdkModule registers GroupApi and updates Sdk constructor | VERIFIED | `SdkModule.kt`: `single<GroupApi> { GroupApiImpl(client = get()) }` and `single { Sdk(authApi = get(), userApi = get(), groupApi = get()) }` |
| 5 | FakeGroupApiBuilder exists for test substitution | VERIFIED | `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeGroupApiBuilder.kt` -- all 9 methods configurable |
| 6 | FakeSdkBuilder updated to include groupApi configuration | VERIFIED | `FakeSdkBuilder.kt`: `fun group(init: FakeGroupApiBuilder.() -> Unit)` and `groupApi = groupApiBuilder.build()` |

#### Plan 04 -- Integration Tests

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Integration tests verify admin can create a group and it persists | VERIFIED | `admin can create a group` -- asserts 201, GroupResponse fields |
| 2 | Integration tests verify cross-group access returns 403 Forbidden | VERIFIED | `user in group A cannot access group B - returns 403` -- asserts 403 and GROUP_FORBIDDEN code |
| 3 | Integration tests verify group CRUD operations (create, read, update, delete) | VERIFIED | 4 CRUD tests present and complete |
| 4 | Integration tests verify member management (add, remove, list with pagination) | PARTIAL | Add and remove tested; list tested (basic 2-member count); cursor-based pagination boundary NOT tested |
| 5 | Integration tests verify RBAC -- regular members cannot list all members or manage the group | PARTIAL | `regular member cannot list group members` and `group member cannot update group` exist; `group member cannot add members` and `group admin can manage members but not delete group` are MISSING |
| 6 | Integration tests run against real PostgreSQL via Testcontainers | VERIFIED | TestHelpers.kt -- PostgreSQLContainer("postgres:16-alpine"), R2dbcDatabase.connect to container |
| 7 | Tests use Ktor testApplication with full server configuration | VERIFIED | groupTestApp() function uses testApplication with Resources, ContentNegotiation, configureSecurity, groupRoutes |

---

### Required Artifacts

| Artifact | Status | Details |
|----------|--------|---------|
| `core/models/src/commonMain/kotlin/com/m2f/template/models/GroupRole.kt` | VERIFIED | 71 lines, GroupRoleSerializer present, OWNER/ADMIN/MEMBER with level hierarchy |
| `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/GroupDtos.kt` | VERIFIED | 84 lines, 7 @Serializable types including PaginatedMemberResponse |
| `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/PaginatedResponse.kt` | PATH DEVIATION | File does not exist; content delivered as PaginatedMemberResponse in GroupDtos.kt (plan itself acknowledged this approach) |
| `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt` | VERIFIED | Groups class with 9 nested @Resource endpoints |
| `core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt` | VERIFIED | AppError.Group sealed class with 4 variants |
| `server/groups/build.gradle.kts` | VERIFIED | Module configuration with server-module-convention |
| `server/groups/src/main/kotlin/com/m2f/server/groups/tables/GroupsTable.kt` | VERIFIED | object GroupsTable with all required columns |
| `server/groups/src/main/kotlin/com/m2f/server/groups/tables/UserGroupMembershipsTable.kt` | VERIFIED | object UserGroupMembershipsTable with composite PK |
| `server/groups/src/main/kotlin/com/m2f/server/groups/service/GroupService.kt` | VERIFIED | Full RBAC-enforced service with all 8 methods |
| `server/groups/src/main/kotlin/com/m2f/server/groups/routes/GroupRoutes.kt` | VERIFIED | fun Route.groupRoutes with all 9 endpoints |
| `server/src/main/kotlin/com/m2f/template/Application.kt` | VERIFIED | registerGroupMigrations() called; groupService injected; groupRoutes(groupService) wired |
| `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/GroupApi.kt` | VERIFIED | interface GroupApi with 9 methods returning Either<AppError, T> |
| `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/GroupApiImpl.kt` | VERIFIED | class GroupApiImpl using Groups.* @Resource for all HTTP calls |
| `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt` | VERIFIED | GroupApi by groupApi delegation present |
| `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeGroupApiBuilder.kt` | VERIFIED | class FakeGroupApiBuilder with 9 configurable lambda properties and build() |
| `server/groups/src/test/kotlin/com/m2f/server/groups/GroupRoutesTest.kt` | PARTIAL | class GroupRoutesTest exists with 16 tests; 3 planned tests absent (registerMember, member-cannot-add, admin-can-manage-not-delete) |
| `server/groups/src/test/kotlin/com/m2f/server/groups/TestHelpers.kt` | VERIFIED | createTestToken function, Testcontainers setup, groupTestApp builder |

---

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| GroupDtos.kt | GroupRole.kt | MemberResponse uses GroupRole for role field | WIRED | MemberResponse.role: GroupRole confirmed in GroupDtos.kt |
| ApiRoutes.kt | GroupDtos.kt | Routes define the HTTP contract that DTOs travel through | WIRED | Groups @Resource class present; GroupDtos types used in routes |
| GroupRoutes.kt | GroupService.kt | Routes delegate to service for business logic | WIRED | Every route handler calls groupService.* |
| GroupService.kt | GroupRepository.kt | Service uses repository for database access | WIRED | groupRepository.findById, insert, update, delete, listAll, findBySlug all called |
| Application.kt | GroupRoutes.kt | Application.module() installs group routes | WIRED | groupRoutes(groupService) in routing {} block of Application.kt |
| GroupApiImpl.kt | ApiRoutes.kt | Uses Groups @Resource classes for type-safe HTTP requests | WIRED | All 9 GroupApiImpl methods reference Groups.Create, Groups.ById, Groups.Update, etc. |
| Sdk.kt | GroupApi.kt | Kotlin delegation | WIRED | `GroupApi by groupApi` in Sdk class declaration |
| GroupRoutesTest.kt | GroupRoutes.kt | Tests exercise all group route handlers through HTTP | PARTIAL | 9 of 9 routes exercised in at least one test; registerMember route has no test |
| GroupRoutesTest.kt | Groups.kt | Tests rely on migrations to set up database schema | WIRED | groupTestApp calls registerGroupMigrations() and Migrations.migrate(database) |

---

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|------------|-------------|--------|----------|
| GRP-01 | 13-01, 13-02, 13-04 | Admin can create a group with name and description | SATISFIED | GroupService.createGroup, route POST Groups.Create, test `admin can create a group` |
| GRP-02 | 13-01, 13-02 | User belongs to one group (schema designed for future multi-group) | SATISFIED | UserGroupMembershipsTable uses composite PK (userId, groupId) supporting future multi-group; SeedDefaultGroupMigration assigns existing users |
| GRP-07 | 13-01, 13-03 | Group SDK functions return Either<AppError, T> with shared @Resource routes | SATISFIED | GroupApi interface and GroupApiImpl confirmed; GroupApiImpl uses Groups.* @Resource classes |
| GRP-08 | 13-02, 13-04 | Group data is isolated -- users cannot see other groups' data | SATISFIED | GroupService enforces membership check (GroupForbidden for non-members); cross-group isolation test passes |
| TEST-02 | 13-04 | Server integration tests run via Ktor testApplication with test database | SATISFIED (partial coverage) | 16 tests via groupTestApp using Testcontainers + testApplication; 3 planned tests missing but core requirement met |

**Note on orphaned requirements:** GRP-03, GRP-04 are mapped to Phase 14 in REQUIREMENTS.md -- correctly NOT claimed by Phase 13 plans.

---

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| GroupService.kt | 246, 326 | `joinedAt = ""` with comment "Will be set by DB default; client can re-fetch" | Warning | `addMember` and `registerMember` return empty joinedAt in the immediate response. The value is populated in DB by CurrentDateTime default but not re-queried. Client receives empty string instead of actual timestamp. Not a blocker -- data is correct in DB. |

---

### Human Verification Required

None -- all key behaviors are verifiable programmatically from the codebase structure.

---

### Gaps Summary

Three integration test gaps block full satisfaction of Plan 04 must-haves. The core goal (group CRUD, cross-group isolation, SDK, migrations) is fully achieved. The gaps are:

1. **registerMember endpoint has no integration test.** The `POST /api/groups/{id}/members/register` route and service method are fully implemented, but no test exercises this path end-to-end.

2. **RBAC coverage incomplete.** The test `group member cannot add members` is absent (tests only that a member cannot update the group). The test `group admin can manage members but not delete group` is absent. These are required to fully verify the OWNER > ADMIN > MEMBER permission hierarchy.

3. **Pagination boundary untested.** `admin can list group members` verifies a list is returned with 2 members but does not test cursor-based pagination (hasMore=true/false, cursor token used for a second page request).

All three gaps are in the test layer only. The production code (service, routes, repository) implements these behaviors correctly.

---

_Verified: 2026-02-19_
_Verifier: Claude (gsd-verifier)_
