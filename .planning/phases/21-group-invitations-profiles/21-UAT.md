---
status: diagnosed
phase: 21-group-invitations-profiles
source: [21-01-SUMMARY.md, 21-02-SUMMARY.md, 21-03-SUMMARY.md]
started: 2026-03-01T14:12:00Z
updated: 2026-03-01T14:30:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Admin Lists Pending Invitations
expected: Open the admin panel for a group. The invitations section shows a table with Email, Role, Status, and Actions columns. Previously sent invitations appear with correct status badges (expiry countdown for active, Accepted/Revoked/Expired badges).
result: issue
reported: "the data looks correct but the items are not translated (the words looking in english should look in spanish)"
severity: minor

### 2. Admin Revokes an Invitation
expected: In the invitations table, click the Revoke button on an active invitation. A confirmation dialog appears asking "Revoke invitation to {email}?". Confirm the revocation. The invitation status changes to "Revoked" (Error badge) and the Revoke button disappears for that row.
result: pass

### 3. Revoked Invitation Cannot Be Accepted
expected: After revoking an invitation, attempt to open the invitation acceptance link. The server rejects it — the user sees an error state indicating the invitation is no longer valid (410 Gone or equivalent error).
result: issue
reported: "several things: 1) when accepting invitation no matter if already accepted, revoked or pending, the user when accepts via login or register the app authenticates correctly but doesn't apply the invitation. logging out and clicking link again has same problem — invitation is not applied from the invitation page. 2) from a logged-in account opening the invitation email works correctly — already accepted/revoked are rejected, pending invitations are accepted. 3) without authentication opening link then login/register does nothing, but refreshing page (cmd+R) triggers correct flow: pending gets accepted, already accepted shows message, but revoked shows generic message instead of saying it's revoked. No option to re-ask for invitation or re-send from admin."
severity: major

### 4. Expired Invitation Shows Proper Status
expected: An invitation that has passed its expiry time shows an "Expired" warning badge in the admin panel invitations table. The Revoke button is not shown for expired invitations. Attempting to accept an expired invitation link returns an error.
result: pass

### 5. Non-Admin Cannot Access Invitation Management
expected: A regular (non-admin) group member cannot see or access invitation management features. API calls to list or revoke invitations return 403 Forbidden.
result: pass

### 6. Integration Tests Pass
expected: Run `./gradlew :server:groups:test --tests "com.m2f.server.groups.InvitationRoutesTest"` — all 6 tests pass (lifecycle, list, revoke, expired, auth x2).
result: pass

## Summary

total: 6
passed: 4
issues: 2
pending: 0
skipped: 0

## Gaps

- truth: "Invitation table labels and status badges display in user's selected locale (Spanish)"
  status: failed
  reason: "User reported: the data looks correct but the items are not translated (the words looking in english should look in spanish)"
  severity: minor
  test: 1
  root_cause: "Spanish translation file values-es/strings.xml is missing all 17 invitation and revoke dialog string keys. They were added to English values/strings.xml (lines 38-55) but never added to Spanish. Compose Multiplatform falls back to English when keys are absent."
  artifacts:
    - path: "app/admin/src/commonMain/composeResources/values-es/strings.xml"
      issue: "Missing 17 invitation string keys (admin_invitations_title, admin_invitations_email, admin_invitations_role, admin_invitations_status, admin_invitations_actions, admin_invitations_accepted, admin_invitations_revoked, admin_invitations_expired, admin_invitations_expires_days, admin_invitations_expires_tomorrow, admin_invitations_expires_today, admin_revoke_title, admin_revoke_confirm, admin_revoke_cancel, admin_revoke_submit, admin_revoke_button, admin_invitations_none)"
  missing:
    - "Add Spanish translations for all 17 invitation string keys to values-es/strings.xml"
  debug_session: ".planning/debug/invitations-spanish-strings.md"

- truth: "Unauthenticated user opens invite link, logs in or registers, and invitation is automatically applied"
  status: failed
  reason: "User reported: login/register from invitation page authenticates but doesn't apply the invitation. Only works after page refresh (cmd+R). From logged-in account it works correctly."
  severity: major
  test: 3
  root_cause: "RegisterViewModel.handleRegister() is missing post-registration invitation handling. After successful registration with invitation token, it always emits NavigateToDashboard instead of NavigateToGroup(groupId). Server accepts invitation during registration (AuthService.register), so page refresh works — user IS in the group, client just doesn't navigate there."
  artifacts:
    - path: "RegisterViewModel.kt"
      issue: "Line 92: always emits NavigateToDashboard on success, never checks invitationToken"
    - path: "AuthDtos.kt"
      issue: "Lines 21-25: AuthResponse doesn't carry groupId for invitation-based registrations"
    - path: "InvitationService.kt"
      issue: "Line 153: acceptInvitation is not idempotent, constraining fix options"
  missing:
    - "Add post-register invitation handling in RegisterViewModel (call sdk.getInvitation(token) to get groupId, emit NavigateToGroup)"
    - "Or extend AuthResponse with optional groupId field populated when invitation accepted during registration"
  debug_session: ".planning/debug/invite-post-auth-flow.md"

- truth: "Revoked invitation shows specific 'revoked' error message, not generic error"
  status: failed
  reason: "User reported: revoked invitation shows generic message instead of saying it's revoked"
  severity: minor
  test: 3
  root_cause: "Revoked invitation error is lost at 4 client layers: (1) ErrorMapper has no 410 mapping — falls to AppError.Client.Unknown; (2) StringKey enum has no INVITATION_REVOKED entry; (3) InviteAcceptModel has no isRevoked field; (4) InviteAcceptScreen has no revoked-state UI block."
  artifacts:
    - path: "core/sdk/.../ErrorMapper.kt"
      issue: "Lines 64-93: HTTP 410 Gone has no mapping, falls to CLIENT_UNKNOWN_ERROR"
    - path: "core/models/.../localization/StringKey.kt"
      issue: "Missing INVITATION_REVOKED, INVITATION_EXPIRED, INVITATION_ALREADY_ACCEPTED entries"
    - path: "app/auth/.../InviteAcceptModel.kt"
      issue: "Missing isRevoked field"
    - path: "app/auth/.../InviteAcceptScreen.kt"
      issue: "No revoked-state UI section"
  missing:
    - "Add 410 mapping in ErrorMapper (map to ServerMapped to preserve error code)"
    - "Add invitation error entries to StringKey enum"
    - "Add isRevoked to InviteAcceptModel, thread through mutation/ViewModel"
    - "Add revoked UI block in InviteAcceptScreen with appropriate message"
  debug_session: ".planning/debug/revoked-invitation-error-message.md"

- truth: "Option exists to re-request invitation or admin can re-send from admin panel"
  status: failed
  reason: "User reported: no option to re-ask for an invitation or to re-send an invitation from the admin"
  severity: minor
  test: 3
  root_cause: "Missing feature across full stack: no server resend endpoint, no SDK resend method, no admin panel resend button, and existing 'Request New Invitation' button on expired invitations just redirects to LoginRoute with no context (no-op)."
  artifacts:
    - path: "server/groups/.../routes/InvitationRoutes.kt"
      issue: "No resend route"
    - path: "server/groups/.../service/InvitationService.kt"
      issue: "No resend method"
    - path: "app/admin/.../AdminPanelScreen.kt"
      issue: "No resend button in InvitationsSection Actions column for expired/revoked"
    - path: "app/auth/.../InviteAcceptScreen.kt"
      issue: "'Request New Invitation' button just navigates to LoginRoute, no API call"
  missing:
    - "Add server resend endpoint (create new invitation for same email/group, revoke old one)"
    - "Add SDK resendInvitation method, route class, fake builder"
    - "Add Resend button in admin panel for expired/revoked invitations"
    - "Fix 'Request New Invitation' to show meaningful guidance or trigger actual re-request"
  debug_session: ".planning/debug/resend-invitation-gap.md"
