---
status: complete
phase: 21-group-invitations-profiles
source: [21-04-SUMMARY.md, 21-05-SUMMARY.md, 21-06-SUMMARY.md]
started: 2026-03-01T19:10:00Z
updated: 2026-03-02T10:00:00Z
---

## Current Test

[all tests complete]

## Tests

### 1. Spanish Invitation Table Labels
expected: Switch the app to Spanish locale. Open the admin panel for a group with invitations. The invitations table headers (Email, Role, Status, Actions), status badges (Accepted, Revoked, Expired, expiry countdowns), and revoke dialog labels should all display in Spanish.
result: issue
reported: "almost everything is translated except the role badges, the title is fine but the roles are not translated — showing MEMBER/OWNER in English"
severity: minor

### 2. Register with Invitation Navigates to Group
expected: Open an invitation link while logged out. Click Register and create a new account. After registration completes, the app navigates to the Dashboard (the group the invitation belongs to), not staying on the invitation page or showing an error.
result: issue
reported: "registering after clicking the CTA on the invitation didn't work, the admin panel didn't show the invited user moved to the group. Also: 1) login/register flows should pre-fill the email from the invitation and not allow editing — invitation is tied to that email. 2) backend should ensure that if a different email is used the invitation is not applied. 3) once navigating away from invitation screen to login/register/dashboard, refreshing (cmd+R) should not reload the invitation screen."
severity: major

### 3. Revoked Invitation Shows Specific Error
expected: Open a revoked invitation link (or refresh the invite-accept page for a revoked invitation). The page shows a specific error message like "This invitation has been revoked by the administrator" with a hint to contact the admin, NOT a generic error message. The Accept/Login/Register action buttons should be hidden.
result: pass

### 4. Admin Resends Expired Invitation
expected: In the admin panel invitations table, find an expired invitation row. A "Resend" button should appear. Click it. The old invitation is revoked and a new invitation email is sent to the same address. The table refreshes showing the new active invitation.
result: issue
reported: "the invitation was resent but as I had an account already signed in the app navigated to the dashboard, the invitation appears as accepted in the admin panel (which should not as the account which accepted was not the correct account) and the list of members for the group does not contain the invited member (but the account which accepted the invitation, a totally different email)"
severity: major

### 5. Admin Resends Revoked Invitation
expected: In the admin panel invitations table, find a revoked invitation row. A "Resend" button should appear. Click it. A new invitation is created and sent. The table refreshes accordingly.
result: issue
reported: "while the resend a revoked invitation works there are the same repeated email invitation twice as this should not happen — test@test.com is duplicated in the table, once as Revocada and once as Aceptada"
severity: medium

### 6. Login with Invitation Applies Correctly
expected: Open an invitation link while logged out. Click Login and authenticate with an existing account. After login, the invitation is applied and the app navigates to the Dashboard. The user is now a member of the invited group.
result: pass

## Summary

total: 6
passed: 3
issues: 3
pending: 0
skipped: 0

## Gaps

- truth: "Invitation role badges display in user's selected locale (Spanish)"
  status: failed
  reason: "User reported: role badges show raw server strings (MEMBER/OWNER) instead of Spanish translations (Miembro/Propietario)"
  severity: minor
  test: 1
  root_cause: "InvitationsSection renders invitation.role directly as TerminalTableCell text (line 574 of AdminPanelScreen.kt). The role field is a raw String from InvitationResponse DTO, not resolved through string resources. No role name translations exist for the invitation context — only the column header 'ROL' is translated."
  artifacts:
    - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt"
      issue: "Line 574: TerminalTableCell(text = invitation.role) uses raw server string, no localization"
    - path: "app/admin/src/commonMain/composeResources/values-es/strings.xml"
      issue: "Missing role value translations for invitation context (Miembro, Admin, Propietario)"
  missing:
    - "Add string resources for role names (admin_role_member, admin_role_admin, admin_role_owner) in EN and ES"
    - "Map invitation.role string to localized string resource in InvitationsSection"

- truth: "Register from invitation link applies the invitation and navigates to the group"
  status: failed
  reason: "User reported: registering from invitation CTA didn't work — invited user not added to group. Also raised 3 UX issues: (1) email should be pre-filled and locked from invitation, (2) backend must reject mismatched emails, (3) navigating away then refreshing should not reload invitation screen."
  severity: major
  test: 2
  root_cause: "Server-side invitation acceptance during registration IS correctly wired (AuthService.register -> onRegistered -> invitationService.acceptInvitation). The real issues are: (1) No email validation — server acceptInvitation() never compares invitation.email with authenticated user's email, so a different-email registration still 'accepts' the invitation. (2) Login/Register screens don't pre-fill or lock the invitation email. (3) Both NavigateToGroup and NavigateToDashboard navigate to DashboardRoute (AppNavHost.kt:146-158), so the user may not see the group they joined. (4) After navigating away, the invite URL remains in browser history causing cmd+R to reload the invite screen."
  artifacts:
    - path: "server/groups/src/main/kotlin/com/m2f/server/groups/service/InvitationService.kt"
      issue: "Lines 139-177: acceptInvitation() never validates invitation.email against accepting user's email"
    - path: "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterViewModel.kt"
      issue: "Line 72: RegisterRequest includes invitationToken but email field is not pre-filled from invitation"
    - path: "composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt"
      issue: "Lines 146-158: NavigateToGroup and NavigateToDashboard both go to DashboardRoute, no group-specific navigation"
    - path: "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginViewModel.kt"
      issue: "Lines 62-81: handlePostLogin() auto-calls acceptInvitation without email validation"
  missing:
    - "Server: Add email match validation in InvitationService.acceptInvitation() — compare invitation.email with user's email"
    - "Server: Add InvitationEmailMismatch error type to InvitationErrors.kt and AppError.kt"
    - "Client: Pre-fill and lock email field in Login/Register when invitationToken is present"
    - "Client: Replace invite URL after navigating away (browser history management)"
    - "Server: Add test for email mismatch rejection"

- truth: "Invitation acceptance validates the accepting user's email matches the invitation email"
  status: failed
  reason: "User reported: a signed-in account with a different email than the invitation's target accepted the invitation — backend did not reject the mismatch. The invitation shows as accepted in admin panel but the invited member is not in the group; instead the wrong account was added."
  severity: major
  test: 4
  root_cause: "InvitationService.acceptInvitation() (lines 139-177) only validates: invitation exists, not revoked, not accepted, not expired, group exists, user not already member. It NEVER compares invitation.email with the authenticated user's email. Any authenticated user with the token can accept. The acceptedBy field stores the wrong user's ID. Additionally, LoginViewModel.handlePostLogin() (line 66) auto-calls acceptInvitation if invitationToken is set — so simply logging in with the wrong account auto-accepts the invitation for the wrong user."
  artifacts:
    - path: "server/groups/src/main/kotlin/com/m2f/server/groups/service/InvitationService.kt"
      issue: "Lines 139-177: No email comparison between invitation.email and authenticated user's email"
    - path: "server/groups/src/main/kotlin/com/m2f/server/groups/errors/InvitationErrors.kt"
      issue: "Missing InvitationEmailMismatch error class"
    - path: "core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt"
      issue: "Missing EmailMismatch variant in sealed class Invitation (around line 187)"
    - path: "server/groups/src/main/kotlin/com/m2f/server/groups/routes/InvitationRoutes.kt"
      issue: "Lines 68-74: Accept route extracts userId from JWT but no email is passed to service"
    - path: "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginViewModel.kt"
      issue: "Line 66: handlePostLogin() auto-calls acceptInvitation without email check"
  missing:
    - "Server: Add email validation in InvitationService.acceptInvitation() — look up user email via repository and compare with invitation.email"
    - "Server: Create InvitationEmailMismatch error in InvitationErrors.kt"
    - "Client: Add AppError.Invitation.EmailMismatch to sealed class"
    - "Client: Add ErrorMapper mapping for email mismatch HTTP status"
    - "Client: InviteAcceptViewModel — compare logged-in user email with invitation email before allowing accept"
    - "Server: Add test in InvitationRoutesTest for wrong-email rejection"

- truth: "Resending an invitation replaces the old invitation row rather than creating a duplicate"
  status: failed
  reason: "User reported: after resending a revoked invitation for test@test.com, the table shows two rows for the same email — one Revocada and one Aceptada. Expected the old row to be removed or updated."
  severity: medium
  test: 5
  root_cause: "InvitationService.resendInvitation() (lines 260-318) revokes the old invitation and creates a brand new DB row with a new ID/token. The listInvitations() query (InvitationRepository.findByGroupId, line 100-106) returns ALL invitations for the group with no status filtering. InvitationsTable has no unique constraint on (email, groupId) — only token is unique. The UI (AdminPanelScreen.kt:569) renders every item without deduplication. Result: both old revoked + new active invitation rows appear for the same email."
  artifacts:
    - path: "server/groups/src/main/kotlin/com/m2f/server/groups/service/InvitationService.kt"
      issue: "Lines 260-318: resendInvitation() creates new row + revokes old; Lines 184-211: listInvitations() returns all rows unfiltered"
    - path: "server/groups/src/main/kotlin/com/m2f/server/groups/repository/InvitationRepository.kt"
      issue: "Lines 100-106: findByGroupId returns ALL invitations, no status filtering"
    - path: "server/groups/src/main/kotlin/com/m2f/server/groups/tables/InvitationsTable.kt"
      issue: "Lines 20-33: No unique constraint on (email, groupId)"
    - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt"
      issue: "Line 569: InvitationsSection renders all invitations without dedup"
  missing:
    - "Server: In resendInvitation(), delete the old invitation record instead of just revoking it (add deleteById to InvitationRepository)"
    - "OR Server: In listInvitations(), filter to only return the most recent invitation per email (group by email, take latest)"
    - "OR Client: Deduplicate invitations by email in AdminPanelViewModel before displaying"
