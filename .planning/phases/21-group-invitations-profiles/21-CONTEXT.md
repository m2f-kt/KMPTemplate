# Phase 21: Group Invitations & Profiles - Context

**Gathered:** 2026-02-26
**Status:** Ready for planning

<domain>
## Phase Boundary

Complete the admin invitation management flow (list/revoke pending invitations) and add integration tests for the full user lifecycle. Avatar upload, display, invitation creation, and acceptance are already implemented — this phase fills the remaining gaps: INVITE-03 (admin view/revoke), PROF-03/PROF-04 (verify existing avatar works E2E), and DEBT-01.

**Already implemented (do NOT reimplement):**
- Avatar upload: PUT /api/users/me/avatar endpoint, SDK uploadAvatar(), ProfileViewModel with crop dialog, ProfileScreen with picker + upload UI
- TerminalAvatar: Supports image URLs via Coil3 AsyncImage with initials fallback
- Invitation create: POST /api/groups/{groupId}/invitations/create, SDK createInvitation(), AdminPanel invite dialog
- Invitation accept: POST /api/invitations/accept, SDK acceptInvitation(), InviteAcceptScreen with login/register flow
- Invitation get: GET /api/invitations/{token}, SDK getInvitation()

**Remaining work:**
- INVITE-03: List pending invitations endpoint + SDK + admin UI with revoke
- Integration tests: Full lifecycle E2E against Testcontainers PostgreSQL
- Verify existing avatar + invitation features work together end-to-end

</domain>

<decisions>
## Implementation Decisions

### Invitation management screen
- Pending invitations list lives as a new section/tab inside AdminPanelScreen (not a separate screen)
- Each invitation row shows: invitee email, assigned role badge, and expiry countdown ("Expires in 2 days") or "Expired" label
- Expired invitations remain visible but with visual distinction (dimmed/strikethrough + "Expired" badge)
- Revoke flow uses a confirmation dialog: "Revoke invitation to user@example.com?" with Cancel/Revoke buttons

### Integration test scope
- Priority: Full invitation lifecycle test (auth -> create group -> invite user -> register/accept -> verify membership)
- Include avatar upload round-trip test (upload image -> verify avatarUrl in profile response)
- Reuse existing Testcontainers + Kotest pattern from Phase 19 integration tests

### Claude's Discretion
- Server endpoint design for list/revoke invitations (REST conventions)
- SDK method signatures for new invitation operations
- AdminPanel layout details for the pending invitations section (spacing, card design)
- Whether to add DELETE avatar endpoint (nice-to-have, not required)
- Test utility setup and shared fixtures

</decisions>

<specifics>
## Specific Ideas

- Invitation rows should show expiry as a human-readable countdown ("Expires in 2 days"), not raw timestamps
- Expired invitations stay visible so admins can see what happened (audit trail)
- Revoke confirmation matches existing dialog patterns in the admin panel (similar to invite dialog)

</specifics>

<deferred>
## Deferred Ideas

- Bulk invitation (invite multiple users at once) -- future phase
- Re-send expired invitation -- future phase
- Invitation analytics/history dashboard -- future phase

</deferred>

---

*Phase: 21-group-invitations-profiles*
*Context gathered: 2026-02-26*
