---
status: diagnosed
trigger: "UAT gap: unauthenticated user invite flow - auth succeeds but invitation not applied until page refresh"
created: 2026-03-01T00:00:00Z
updated: 2026-03-01T00:02:00Z
symptoms_prefilled: true
goal: find_root_cause_only
---

## Current Focus

hypothesis: CONFIRMED - RegisterViewModel doesn't handle post-registration invitation navigation
test: traced full code path for both login and register flows
expecting: confirmed
next_action: return diagnosis

## Symptoms

expected: Open invite link → see details → login/register → auto-accept invitation → navigate to dashboard
actual: Auth succeeds but invitation NOT applied until refresh. Only manual page refresh triggers correct flow.
errors: No errors - silent failure
reproduction: As unauthenticated user, open invite link, register, observe no group navigation
started: Phase 18.2 implemented this flow

## Eliminated

- hypothesis: Server doesn't accept invitation during registration
  evidence: AuthService.register() lines 96-99 and ServerModule.kt lines 48-58 confirm server calls invitationService.acceptInvitation() during registration when invitationToken is present
  timestamp: 2026-03-01T00:00:45Z

- hypothesis: Invitation token not passed from InviteAcceptScreen to RegisterScreen
  evidence: InviteAcceptEvent.NavigateToRegister carries token, AppNavHost navigates to RegisterRoute(invitationToken=token), RegisterRoute LaunchedEffect sets token on ViewModel
  timestamp: 2026-03-01T00:00:50Z

- hypothesis: Login flow has the same problem
  evidence: LoginViewModel.handlePostLogin() (lines 62-81) correctly calls sdk.acceptInvitation() and emits NavigateToGroup. Login flow works correctly.
  timestamp: 2026-03-01T00:00:55Z

## Evidence

- timestamp: 2026-03-01T00:00:30Z
  checked: LoginViewModel.handlePostLogin() (lines 62-81)
  found: Login flow correctly handles invitation token - checks model.value.invitationToken, calls sdk.acceptInvitation(), emits LoginEvent.NavigateToGroup(response.groupId) on success
  implication: Login invitation flow is correctly implemented as a TWO-STEP process (login first, then accept invitation client-side)

- timestamp: 2026-03-01T00:00:35Z
  checked: RegisterViewModel.handleRegister() (lines 84-96)
  found: Register flow includes invitationToken in RegisterRequest (line 72) so server accepts it, but on success ONLY emits RegisterEvent.NavigateToDashboard (line 92). No post-registration invitation handling.
  implication: Client doesn't know which group was joined and navigates to plain dashboard

- timestamp: 2026-03-01T00:00:40Z
  checked: Server AuthService.register() (lines 96-99) + ServerModule.kt (lines 48-58)
  found: Server calls invitationService.acceptInvitation() during registration when invitationToken is present. Invitation IS accepted server-side.
  implication: The data is correct on the server — the user IS in the group — but the client doesn't react

- timestamp: 2026-03-01T00:00:45Z
  checked: Server acceptInvitation() (InvitationService.kt lines 140-177)
  found: NOT idempotent - raises InvitationAlreadyAccepted (line 153) and MemberAlreadyInGroup (line 164)
  implication: Cannot simply duplicate LoginViewModel's approach of calling sdk.acceptInvitation() post-registration — it will fail because server already accepted it

- timestamp: 2026-03-01T00:00:50Z  
  checked: AuthResponse DTO (AuthDtos.kt lines 21-25)
  found: AuthResponse only has accessToken, refreshToken, expiresIn — no groupId field
  implication: Server register endpoint doesn't return the accepted group info to the client, so client has no way to know which group was joined

- timestamp: 2026-03-01T00:00:55Z
  checked: RegisterEvent sealed interface
  found: Has NavigateToGroup(groupId) variant defined but NEVER emitted by RegisterViewModel
  implication: The event was anticipated but never wired up

- timestamp: 2026-03-01T00:01:00Z
  checked: Why page refresh fixes it
  found: On refresh, user is authenticated, app re-initializes, DashboardViewModel loads user profile/groups and shows the correctly-joined group
  implication: Server state is correct; only client navigation/reaction is wrong

## Resolution

root_cause: |
  RegisterViewModel.handleRegister() has a missing post-registration step for invitation handling.
  
  The Login flow works correctly because LoginViewModel does TWO separate steps:
  1. sdk.login() — authenticates the user
  2. sdk.acceptInvitation(token) — accepts the invitation client-side, gets back groupId
  3. Emits LoginEvent.NavigateToGroup(groupId) — navigates to the correct group
  
  The Register flow is broken because it only does ONE step:
  1. sdk.register(request with invitationToken) — server accepts invitation during registration
  2. Emits RegisterEvent.NavigateToDashboard — navigates to plain dashboard (WRONG)
  
  The problem is architectural: the server accepts the invitation during registration but the
  AuthResponse doesn't include the groupId. The client has no way to know which group was joined.
  
  There are two possible fixes:
  
  OPTION A (Minimal — client-side): After successful registration with invitationToken, have
  RegisterViewModel call sdk.getInvitation(token) to retrieve the invitation details (which
  include groupId), then emit RegisterEvent.NavigateToGroup(invitation.groupId). This avoids
  touching the server.
  
  OPTION B (Proper — server-side): Extend the server's register endpoint to return the groupId 
  when an invitation was accepted (e.g., add optional groupId to AuthResponse or a new response 
  type), then have RegisterViewModel use that to emit NavigateToGroup.

fix: 
verification: 
files_changed: []
