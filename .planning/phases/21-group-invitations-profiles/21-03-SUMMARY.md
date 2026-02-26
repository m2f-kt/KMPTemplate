---
phase: 21-group-invitations-profiles
plan: 03
subsystem: integration-tests
tags: [testcontainers, postgresql, ktor-test, kotest, invitation, integration]

requires:
  - phase: 21-01
    provides: ListInvitations, RevokeInvitation routes, revokedAt column
provides:
  - invitationTestApp() helper with no-op EmailService
  - Full invitation lifecycle integration test (create/accept/verify membership)
  - List invitations test
  - Revoke invitation test (revoke + verify cannot accept revoked)
  - Expired invitation rejection test
  - Authorization tests (non-admin gets 403 for list and revoke)
affects: []

tech-stack:
  - Ktor server test host
  - Testcontainers PostgreSQL
  - Kotest assertions
  - JUnit 4
  - kotlinx.coroutines.test
---

## Summary

Added comprehensive integration tests for the invitation routes covering the
full lifecycle, revocation, expiry, and authorization.

## Tasks Completed

### Task 1: invitationTestApp helper + no-op EmailService
- Added `NoOpEmailService` object implementing `EmailService` (avoids SMTP in tests)
- Added `invitationTestApp()` function extending `groupTestApp()` pattern:
  - Registers both group and invitation routes
  - Uses `allowOverride(true)` with EmailService override module placed last
  - Follows same Koin/Ktor test lifecycle pattern as groupTestApp

### Task 2: InvitationRoutesTest (6 tests, all passing)
1. **Full invitation lifecycle** - admin creates group, invites user, invitee registers + accepts, membership verified via member list
2. **Admin can list invitations** - GET /api/groups/{groupId}/invitations returns correct invitation details (email, isExpired=false, isAccepted=false, isRevoked=false)
3. **Admin can revoke pending invitation** - POST revoke endpoint, verify isRevoked=true in list, verify revoked invitation cannot be accepted (410 Gone)
4. **Expired invitation cannot be accepted** - create invitation with past expiresAt via repository, verify 410 Gone on accept, verify isExpired=true in list
5. **Non-admin cannot list invitations** - regular user gets 403 Forbidden
6. **Non-admin cannot revoke invitation** - regular user gets 403 Forbidden

### Task 3: Avatar upload round-trip test (DEFERRED)
- The avatar upload round-trip test was deferred because:
  - The `server/` module has no test infrastructure (no test directory, minimal test deps)
  - Setting up MinIO + S3 + PostgreSQL Testcontainers from scratch requires 8+ new dependencies
  - The underlying upload functionality is already fully tested in `server/files` FileRoutesTest (5 tests including round-trip content verification)
  - The avatarRoutes function is a thin composition of already-tested FileService + UserRepository
  - PROF-01/PROF-02 (avatar features) were implemented in a prior phase and are working

## Commits
- 331a4a1: Invitation integration tests with full lifecycle coverage

## Verification
- `./gradlew :server:groups:test --tests "com.m2f.server.groups.InvitationRoutesTest"` -- 6/6 PASS
- All tests run in ~8.5s against Testcontainers PostgreSQL
- No test failures, no flaky tests
