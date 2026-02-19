# Plan 13-02 Summary: Server Groups Module

**Status:** Complete
**Duration:** ~5 min
**Commits:** 1

## What was built
- `server:groups` Gradle module with server-module-convention plugin
- GroupsTable (uuid PK, name, slug unique, description, createdBy FK, timestamps)
- UserGroupMembershipsTable (composite PK userId+groupId, role default MEMBER, joinedAt)
- GroupRepository (CRUD + findBySlug, listAll, count)
- MembershipRepository (cursor-based pagination with inner join to UsersTable for email/name)
- GroupErrors (6 DomainError implementations: GroupNotFound, GroupForbidden, GroupAlreadyExists, MemberAlreadyInGroup, MemberNotInGroup, CannotRemoveOwner)
- GroupService with full RBAC enforcement (PowerAdmin bypass, Owner > Admin > Member hierarchy)
- GroupRoutes with 9 endpoints wired to type-safe @Resource routes
- GroupModule (Koin DI wiring for repositories and service)
- 4 migrations: CreateGroupsTable, CreateMemberships, SeedDefaultGroup, SeedDevTestGroups
- Application.kt wired with registerGroupMigrations() and groupRoutes()
- ServerModule.kt includes groupModule in Koin

## Key files
- `server/groups/build.gradle.kts` -- Module configuration
- `server/groups/src/main/kotlin/com/m2f/server/groups/tables/GroupsTable.kt` -- Groups table
- `server/groups/src/main/kotlin/com/m2f/server/groups/tables/UserGroupMembershipsTable.kt` -- Join table
- `server/groups/src/main/kotlin/com/m2f/server/groups/repository/GroupRepository.kt` -- Group CRUD
- `server/groups/src/main/kotlin/com/m2f/server/groups/repository/MembershipRepository.kt` -- Membership ops with cursor pagination
- `server/groups/src/main/kotlin/com/m2f/server/groups/errors/GroupErrors.kt` -- 6 domain errors
- `server/groups/src/main/kotlin/com/m2f/server/groups/service/GroupService.kt` -- Business logic with RBAC
- `server/groups/src/main/kotlin/com/m2f/server/groups/routes/GroupRoutes.kt` -- 9 route handlers
- `server/groups/src/main/kotlin/com/m2f/server/groups/di/GroupModule.kt` -- Koin DI
- `server/groups/src/main/kotlin/com/m2f/server/groups/Groups.kt` -- 4 migrations + registerGroupMigrations()
- `server/src/main/kotlin/com/m2f/template/Application.kt` -- Server entry wiring
- `server/src/main/kotlin/com/m2f/template/di/ServerModule.kt` -- DI aggregation

## Decisions
- UUID cursor pagination uses GreaterOp+QueryParameter since kotlin.uuid.Uuid is not Comparable
- getUserRole() helper in GroupRoutes extracts UserRole from JWT "role" claim
- registerMember uses findByEmail after AuthService.register() since AuthResponse lacks userId
- Group-level RBAC checked in service layer; system-level RBAC (withRole plugin) for create and listAll

## Self-Check: PASSED
- `./gradlew :server:compileKotlin` passes
- All 9 group endpoints defined in routes
- GroupService enforces group-level RBAC (OWNER/ADMIN/MEMBER permissions)
- Cross-group access raises GroupForbidden (403)
- PowerAdmin bypasses group-level checks
- Migrations create tables and seed default group
