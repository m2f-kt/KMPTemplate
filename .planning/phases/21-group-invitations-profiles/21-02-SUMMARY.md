---
phase: 21-group-invitations-profiles
plan: 02
subsystem: sdk-ui
tags: [sdk, compose, mvi, invitation, admin-panel]

requires:
  - phase: 21-01
    provides: ListInvitations, RevokeInvitation routes, InvitationResponse.isRevoked
provides:
  - SDK listInvitations() and revokeInvitation() methods
  - FakeInvitationApiBuilder support for both new methods
  - AdminPanelModel with invitations list, loading, revoke dialog state
  - AdminPanelIntent/Mutation for load, confirm-revoke, cancel-revoke, execute-revoke
  - AdminPanelViewModel auto-loads invitations after admin panel loads
  - InvitationsSection composable with status badges and expiry countdown
  - RevokeDialog composable with destructive confirmation
  - AppNavHost wiring for revoke callbacks
  - kotlinx-datetime added to version catalog and admin module
affects: [21-03]

tech-stack:
  - Kotlin Multiplatform SDK (Ktor client, type-safe resources)
  - Compose Multiplatform UI (TerminalCard, TerminalTable, TerminalBadge, TerminalButton)
  - MVI architecture (Model, Intent, Mutation, ViewModel)
  - kotlinx-datetime for expiry countdown computation
---

## Summary

Completed SDK invitation management methods and admin panel UI for viewing
and revoking pending invitations.

## Tasks Completed

### Task 1: SDK + Fake Builder
- Added `listInvitations(groupId)` and `revokeInvitation(groupId, invitationId)` to InvitationApi interface
- Implemented HTTP calls via `Groups.ListInvitations` and `Groups.RevokeInvitation` type-safe resources
- Added fake implementations in FakeInvitationApiBuilder with configurable results
- Sdk delegates automatically via interface delegation

### Task 2a: MVI State, Intents, Mutations, ViewModel
- Added invitation management state to AdminPanelModel: invitations list, isLoadingInvitations, showRevokeDialog, revokeTarget, isRevoking
- Added LoadInvitations, ConfirmRevokeInvitation, CancelRevoke, ExecuteRevoke intents
- Added corresponding mutations for all state transitions
- ViewModel handles handleLoadInvitations() (auto-loads after admin panel load) and handleRevokeInvitation() (revoke + refresh)
- Reducer covers all new mutation cases

### Task 2b: UI Composables, Strings, AppNavHost Wiring
- Added InvitationsSection composable: TerminalCard with TerminalTable showing Email/Role/Status/Actions
  - Status column: Accepted (Success badge), Revoked (Error badge), Expired (Warning badge), or expiry countdown (Accent badge)
  - Actions column: Revoke button (ButtonVariant.Destructive) for active invitations only
  - Expiry countdown computed via kotlinx-datetime LocalDateTime parsing
- Added RevokeDialog composable: modal overlay with "Revoke invitation to {email}?" confirmation
- Added 17 string resources for invitations section and revoke dialog
- Wired onConfirmRevoke, onCancelRevoke, onExecuteRevoke callbacks in AppNavHost
- Added kotlinx-datetime 0.7.1 to version catalog and admin module build.gradle.kts

## Commits
- 81d74f3: SDK listInvitations/revokeInvitation + fake builder
- dffcf36: MVI state, intents, mutations, ViewModel
- (previous session): strings.xml and AdminPanelScreen imports/signature
- 4e1f5c9: InvitationsSection and RevokeDialog composables + AppNavHost wiring

## Verification
- `./gradlew :core:sdk:compileKotlinJvm` -- PASS
- `./gradlew :core:testing:compileKotlinJvm` -- PASS
- `./gradlew :app:admin:compileKotlinJvm` -- PASS
- `./gradlew :composeApp:compileKotlinJvm` -- PASS
