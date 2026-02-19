# Plan 13-04 Summary: Integration Tests for Group Routes

**Status:** Complete
**Duration:** ~15 min (across context restores)
**Commits:** 1

## What was built
- 16 integration tests in GroupRoutesTest covering:
  - Group CRUD: create, read, update, delete
  - Duplicate slug rejection
  - System-level RBAC (Admin+ for create, PowerAdmin for listAll)
  - Group-level RBAC (Owner can delete, Admin can update, Member cannot)
  - Member management: add, remove, list members, cannot remove owner
  - Cross-group isolation (user in group A cannot access group B)
  - PowerAdmin bypass (can access any group)
- TestHelpers.kt with Testcontainers PostgreSQL, JWT token generation, test user creation, Koin lifecycle management
- MembershipRepository.deleteByGroup() added to support group deletion without CASCADE FK

## Key files
- `server/groups/src/test/kotlin/com/m2f/server/groups/GroupRoutesTest.kt` -- 16 integration tests
- `server/groups/src/test/kotlin/com/m2f/server/groups/TestHelpers.kt` -- Test infrastructure
- `server/groups/src/main/kotlin/com/m2f/server/groups/repository/MembershipRepository.kt` -- Added deleteByGroup
- `server/groups/src/main/kotlin/com/m2f/server/groups/service/GroupService.kt` -- Fixed deleteGroup to remove memberships first
- `server/groups/build.gradle.kts` -- Added ktor-client-content-negotiation test dependency

## Issues resolved during implementation
- **Koin lifecycle**: Starting Koin inside `application {}` block prevented test blocks from accessing DI. Fixed by moving `startKoin` before `testApplication {}` with `finally { stopKoin() }`.
- **Missing AuthService in test DI**: GroupService depends on AuthService via groupModule. Fixed by including `authModule` in test Koin setup instead of manually providing only UserRepository and PasswordHasher.
- **FK constraint on group deletion**: Deleting a group failed because memberships FK had no ON DELETE CASCADE. Fixed by adding `MembershipRepository.deleteByGroup()` and calling it before `groupRepository.delete()`.

## Self-Check: PASSED
- All 16 tests pass
- server:groups:test completes successfully
- Full server compiles
