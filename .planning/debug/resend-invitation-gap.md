---
status: diagnosed
trigger: "UAT gap: no re-send invitation option for admin, no re-request option for user on expired/revoked invitations"
created: 2026-03-01T00:00:00Z
updated: 2026-03-01T00:00:00Z
---

## Current Focus

hypothesis: CONFIRMED — Two separate feature gaps exist: (1) Admin panel has no resend button for expired/revoked invitations, (2) Revoked invitation state is not surfaced to InviteAcceptScreen so the "request new" button only shows for expired, not revoked
test: Searched all layers — server routes, service, SDK, admin UI, invite accept screen
expecting: No resend/re-request functionality for revoked state; partial for expired
next_action: Return diagnosis

## Symptoms

expected: Admin should be able to re-send expired/revoked invitations; users should be able to request a new invitation when viewing an expired/revoked one
actual: Admin can only create new invitations and revoke existing ones; no re-send button; no user-facing re-request option for revoked invitations (expired DOES have one)
errors: N/A (missing feature, not a bug)
reproduction: Go to admin panel invitations section — no resend action visible; visit revoked invitation link — shows generic error instead of revoked-specific UI with re-request option
started: Never existed — feature gap from Phase 21-02 and 18.2 implementations

## Eliminated

- hypothesis: No "request new invitation" button exists anywhere on InviteAcceptScreen
  evidence: InviteAcceptScreen.kt lines 142-160 DO show a "Request New" button when `state.isExpired == true`. InviteAcceptIntent.RequestNewInvitation and InviteAcceptEvent.RequestedNewInvitation exist and are wired through AppNavHost. This partial implementation was done in Phase 18.2.
  timestamp: 2026-03-01

## Evidence

- timestamp: 2026-03-01
  checked: Server InvitationRoutes.kt — all route definitions
  found: 5 routes exist: create, list, revoke, get-by-token, accept. NO resend route.
  implication: Server has no resend endpoint — admin cannot resend invitations via API

- timestamp: 2026-03-01
  checked: InvitationService.kt — all public methods
  found: 5 methods: createInvitation, getInvitation, acceptInvitation, listInvitations, revokeInvitation. NO resend method.
  implication: Server business logic has no resend capability. Would need a new method.

- timestamp: 2026-03-01
  checked: InvitationApi.kt (SDK interface)
  found: 5 methods matching server: createInvitation, getInvitation, acceptInvitation, listInvitations, revokeInvitation. NO resend method.
  implication: SDK has no resend method either — full stack gap

- timestamp: 2026-03-01
  checked: ApiRoutes.kt — typed route definitions
  found: Groups.CreateInvitation, Groups.ListInvitations, Groups.RevokeInvitation, Invitations.ByToken, Invitations.Accept. NO ResendInvitation route class.
  implication: No route definition exists for resend

- timestamp: 2026-03-01
  checked: AdminPanelScreen.kt InvitationsSection (lines 530-613)
  found: Actions column (line 599-606) only shows a "Revoke" button, and ONLY for active invitations (!isAccepted && !isRevoked && !isExpired). Expired/revoked/accepted invitations have NO action buttons at all.
  implication: Admin has no way to resend expired or revoked invitations from the panel

- timestamp: 2026-03-01
  checked: AdminPanelIntent.kt
  found: Invitation-related intents are: LoadInvitations, ConfirmRevokeInvitation, CancelRevoke, ExecuteRevoke. NO resend intent.
  implication: Admin panel ViewModel has no resend capability

- timestamp: 2026-03-01
  checked: InviteAcceptScreen.kt expired block (lines 142-160)
  found: Expired state DOES show a "Request New Invitation" button via `onRequestNewInvitation` callback. This navigates to LoginRoute via InviteAcceptEvent.RequestedNewInvitation in AppNavHost.
  implication: Partial implementation for expired — but just navigates to login, doesn't actually request a new invitation

- timestamp: 2026-03-01
  checked: InviteAcceptModel.kt and InviteAcceptMutation.SetInvitationDetails
  found: Model has `isExpired: Boolean` and `isAlreadyAccepted: Boolean` but NO `isRevoked: Boolean`. Mutation SetInvitationDetails also lacks isRevoked. The server returns isRevoked in InvitationResponse but the ViewModel drops it.
  implication: Revoked invitations are not properly handled on the accept screen. Server returns isRevoked=true via getInvitation, but client never reads it. Revoked invitations either show as a generic error (if trying to accept) or may display normally (if just viewing).

- timestamp: 2026-03-01
  checked: InviteAcceptScreen.kt for revoked state handling
  found: No revoked state block exists in the screen. Only expired (line 142), already accepted (line 163), and success (line 171) states are rendered. No `state.isRevoked` check anywhere.
  implication: When a user views a revoked invitation, they see invitation details but get a generic error if they try to accept. No clear "this invitation was revoked" message, and no "request new" button.

- timestamp: 2026-03-01
  checked: InviteAcceptEvent.RequestedNewInvitation handler in AppNavHost (lines 380-384)
  found: Handler just navigates to LoginRoute() with no invitation context. Does NOT actually create or request a new invitation.
  implication: The "Request New" button for expired invitations is misleading — it doesn't request anything, just redirects to login

## Resolution

root_cause: |
  Two feature gaps plus one data-propagation bug:

  **GAP 1 — Admin Panel: No resend action for expired/revoked invitations**
  The InvitationsSection only shows a "Revoke" button for active invitations. When an invitation is expired or revoked, the Actions column is empty. The admin's only option is to create a brand-new invitation (via the "Invite Member" button), which creates a new token+email — there's no "resend" that reuses or refreshes an existing invitation.

  **GAP 2 — InviteAcceptScreen: Revoked state not surfaced**
  The InviteAcceptModel has `isExpired` and `isAlreadyAccepted` but NOT `isRevoked`. The server's `getInvitation` endpoint returns `isRevoked: true` in the InvitationResponse DTO, but the ViewModel's `handleLoadInvitation()` maps the response to `SetInvitationDetails` which drops `isRevoked`. As a result, the InviteAcceptScreen cannot show a revoked-specific message or a "request new invitation" button for revoked invitations.

  **GAP 3 — "Request New" button is a no-op redirect**
  The existing "Request New Invitation" button on the expired state (InviteAcceptScreen lines 154-159) fires `InviteAcceptEvent.RequestedNewInvitation`, which just navigates to LoginRoute(). It does NOT actually trigger any API call to request a new invitation. The user is just redirected to login with no context about what happened.

  **Server-side**: No resend endpoint exists. InvitationService has no resend method. InvitationRoutes has no resend route. The SDK InvitationApi has no resend method.

fix:
verification:
files_changed: []
