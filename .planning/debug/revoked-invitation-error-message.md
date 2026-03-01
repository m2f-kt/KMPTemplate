---
status: diagnosed
trigger: "UAT gap: revoked invitation shows generic error instead of specific revoked message"
created: 2026-03-01T00:00:00Z
updated: 2026-03-01T00:00:00Z
symptoms_prefilled: true
goal: find_root_cause_only
---

## Current Focus

hypothesis: CONFIRMED — Multiple breaks in the error propagation chain prevent revoked-specific messaging
test: Traced full error flow from server → SDK → ViewModel → UI
expecting: Found 4 distinct breaks
next_action: Return diagnosis

## Symptoms

expected: When accepting a revoked invitation, user sees a specific "invitation was revoked" message
actual: User sees a generic error message
errors: N/A - functional gap, not crash
reproduction: Accept an invitation that has been revoked
started: Phase 21-01 implementation

## Eliminated

## Evidence

- timestamp: 2026-03-01T00:05:00Z
  checked: Server InvitationErrors.kt — InvitationRevoked error definition
  found: Server correctly raises InvitationRevoked which responds with 410 Gone and code "INVITATION_REVOKED"
  implication: Server side is correct

- timestamp: 2026-03-01T00:06:00Z
  checked: SDK ErrorMapper.kt — mapHttpError function
  found: HTTP 410 Gone is NOT handled in the status code switch. Falls to else → AppError.Client.Unknown (code="CLIENT_UNKNOWN_ERROR"). The error code "INVITATION_REVOKED" from the response body is LOST.
  implication: ROOT CAUSE #1 — SDK drops the server's error code for 410 responses

- timestamp: 2026-03-01T00:07:00Z
  checked: StringKey enum — invitation error entries
  found: No INVITATION_REVOKED, INVITATION_EXPIRED, INVITATION_ALREADY_ACCEPTED, or INVITATION_NOT_FOUND entries exist in StringKey enum
  implication: ROOT CAUSE #2 — Even if the SDK preserved the code, StringKey.fromCode("INVITATION_REVOKED") returns null, falling back to GENERIC_ERROR

- timestamp: 2026-03-01T00:08:00Z
  checked: StringKeyResolver in auth module — when branches
  found: No invitation-specific branches exist. Even if StringKey had the entries, there are no string resources mapped.
  implication: ROOT CAUSE #3 — No UI string resources or resolver branches for invitation errors

- timestamp: 2026-03-01T00:09:00Z
  checked: InviteAcceptModel + SetInvitationDetails mutation
  found: Model has isExpired and isAlreadyAccepted but NO isRevoked field. Mutation does NOT pass invitation.isRevoked.
  implication: ROOT CAUSE #4 — Even for getInvitation (which returns isRevoked:true on the DTO), the model discards revoked state

- timestamp: 2026-03-01T00:10:00Z
  checked: InviteAcceptScreen — UI display
  found: Has explicit sections for isExpired (warning alert + hint + request new button) and isAlreadyAccepted (info alert) but NO section for isRevoked
  implication: ROOT CAUSE #5 — No UI for revoked state even if model carried it

- timestamp: 2026-03-01T00:11:00Z
  checked: InviteAcceptViewModel handleLoadInvitation
  found: On getInvitation success, it maps invitation.isExpired and invitation.isAccepted but NOT invitation.isRevoked
  implication: Confirms model/mutation gap

- timestamp: 2026-03-01T00:12:00Z
  checked: Server getInvitation vs acceptInvitation
  found: getInvitation does NOT reject revoked invitations — it returns the full DTO with isRevoked=true. acceptInvitation raises InvitationRevoked error (410 Gone). Two different paths, both broken client-side.
  implication: Two scenarios need fixing — loading a revoked invitation (DTO path) AND accepting one (error path)

## Resolution

root_cause: |
  The "revoked invitation" error message is lost at FOUR layers of the client stack:

  1. **SDK ErrorMapper.kt:64-93** — HTTP 410 Gone is not mapped. Falls to `else → AppError.Client.Unknown`, 
     discarding the server's "INVITATION_REVOKED" code from the response body. Should map 410 to 
     `AppError.Client.ServerMapped` (like 422) to preserve code+message.

  2. **StringKey.kt** — Missing invitation-specific entries: INVITATION_REVOKED, INVITATION_EXPIRED, 
     INVITATION_ALREADY_ACCEPTED, INVITATION_NOT_FOUND. `StringKey.fromCode("INVITATION_REVOKED")` 
     returns null → falls back to GENERIC_ERROR.

  3. **InviteAcceptModel/Mutation/ViewModel** — No `isRevoked` field on the model. The `SetInvitationDetails` 
     mutation does not include `isRevoked`. The ViewModel's `handleLoadInvitation` does not read 
     `invitation.isRevoked` from the DTO response.

  4. **InviteAcceptScreen + StringKeyResolver** — No UI section for the revoked state (unlike expired/accepted 
     which have dedicated alert blocks). No string resources for invitation errors. No resolver branches.

  The server side is correct: InvitationRevoked responds 410 Gone with code "INVITATION_REVOKED", and 
  getInvitation returns isRevoked=true on the DTO.

fix: 
verification: 
files_changed: []
