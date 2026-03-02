---
phase: 21-group-invitations-profiles
verified: 2026-03-02T00:41:35Z
status: passed
score: 5/5 success criteria verified
re_verification:
  previous_status: passed
  previous_score: 5/5
  gaps_closed: []
  gaps_remaining: []
  regressions: []
requirements_coverage:
  satisfied: [INVITE-01, INVITE-02, INVITE-03, INVITE-04, PROF-01, PROF-02, PROF-03, PROF-04, DEBT-01]
  blocked: []
  orphaned: []
human_verification:
  - test: "Admin creates invitation → check MailHog for email with acceptance link"
    expected: "Email arrives with correct subject and token URL"
    why_human: "Requires running MailHog + full SMTP stack"
  - test: "Profile → click avatar → select image → confirm crop → avatar renders"
    expected: "Avatar image replaces initials in Profile, Dashboard, and Sidebar"
    why_human: "Involves file picker, image cropping, visual rendering"
  - test: "Admin Panel → pending invitations table → revoke → resend"
    expected: "Table shows email, localized role, status, action buttons"
    why_human: "Visual layout and interaction verification"
  - test: "Open invite link → Login/Register → verify email pre-filled and locked"
    expected: "Email field shows invitation email, cannot be edited"
    why_human: "Multi-screen navigation flow"
  - test: "Invite email → user accepts → admin views list → stale invite shows accepted"
    expected: "No action buttons for existing members; resend returns 409"
    why_human: "Multi-user workflow with database state transitions"
---

# Phase 21: Group Invitations & Profiles — Verification Report

**Phase Goal:** Admins can invite users by email, users can accept invites and upload profile avatars — the complete user lifecycle
**Verified:** 2026-03-02T00:41:35Z
**Status:** passed
**Re-verification:** Yes — full codebase re-verification after plans 21-09 and 21-10 completed

## Goal Achievement

### Observable Truths (from ROADMAP Success Criteria)

| # | Truth (Success Criterion) | Status | Evidence |
|---|---------------------------|--------|----------|
| 1 | Admin invites user@example.com → invite email arrives in MailHog with acceptance link containing token | ✓ VERIFIED | `InvitationService.kt:107-112` calls `sendInvitationEmail()` → `emailService.sendEmail()` at line 387. Link format uses `{appUrl}/invite/accept?token={token}`. INVITATION_EXPIRY = 7.days (line 56). |
| 2 | Recipient clicks invite link → account is created/activated and user joins the group with specified role | ✓ VERIFIED | `InvitationService.kt:140-183` validates token, checks revoked (line 151), expired (line 158), email match (line 170), inserts membership (line 173), marks accepted (line 176), cleans up other invitations (line 179 `markAcceptedByGroupAndEmail`). `RegisterViewModel.kt:99-107` handles post-registration invite navigation via `getInvitation` → `NavigateToGroup`. AuthService registered ONCE in serverModule with `onRegistered` callback (authModule confirmed clean — no duplicate). |
| 3 | Admin sees list of pending invitations and can revoke any of them; expired tokens are rejected | ✓ VERIFIED | **List:** `InvitationRoutes.kt:44` GET route → `InvitationService.listInvitations()` with membership cross-reference (memberEmails at line 217, stale override at line 228). **Revoke:** `InvitationRoutes.kt:52` POST route → `InvitationService.revokeInvitation()`. **Resend:** `InvitationRoutes.kt:60` POST route. **UI:** `AdminPanelScreen.kt:537` InvitationsSection with localized role badges (lines 583-585). **Expired rejection:** `InvitationService.kt:158` raises `InvitationExpired()`. **Resend guard:** `InvitationService.kt:310` rejects with `MemberAlreadyInGroup` if email is existing member. |
| 4 | User uploads a profile image → avatar URL appears in their profile; TerminalAvatar component shows image instead of initials | ✓ VERIFIED | **Upload:** `AvatarRoutes.kt` PUT (81 lines) — multipart → FileService → `updateAvatarUrl()`. **DB:** `UsersTable.kt:24` avatarUrl column. **DTO:** `UserDtos.kt:13` avatarUrl field. **ProfileVM:** `ProfileViewModel.kt:110` calls `sdk.uploadAvatar()`, line 120 `SetAvatarUrl(user.avatarUrl)`. **TerminalAvatar:** `TerminalAvatar.kt:47-50` renders `AsyncImage` when `imageUrl != null`, falls back to initials on error (line 48 `showFallback`). **Wired:** ProfileScreen:359 `imageUrl = state.avatarUrl`, DashboardScreen:257,309, DashboardSidebar:163 all pass `imageUrl = avatarUrl`. |
| 5 | Integration tests cover the full auth → groups → invite → accept flow end-to-end against Testcontainers PostgreSQL | ✓ VERIFIED | `InvitationRoutesTest.kt` — 378 lines, 8 test methods, 6 `invitationTestApp(database)` calls. Uses `PostgreSQLContainer("postgres:16-alpine")` via `TestHelpers.kt:48`. Tests cover: lifecycle, list, revoke, expired, authorization (2 tests), and consistency. |

**Score:** 5/5 success criteria verified

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `server/groups/.../tables/InvitationsTable.kt` | revokedAt column | ✓ VERIFIED | Line 30: `val revokedAt = datetime("revoked_at").nullable()` |
| `server/groups/.../repository/InvitationRepository.kt` | findByGroupId, findById, revokeById, deleteById, markAcceptedByGroupAndEmail | ✓ VERIFIED | Lines 104, 115, 127, 160, 140. All with proper Exposed imports (and, isNull, lowerCase). |
| `server/groups/.../service/InvitationService.kt` | create, accept, list, revoke, resend + consistency logic | ✓ VERIFIED | Full service with membership cross-reference (line 217), resend guard (line 310), accept cleanup (line 179). |
| `server/groups/.../routes/InvitationRoutes.kt` | GET list, POST create/revoke/resend | ✓ VERIFIED | Lines 44, 52, 60 — all type-safe routes. |
| `server/groups/.../errors/InvitationErrors.kt` | InvitationExpired, InvitationRevoked, InvitationEmailMismatch | ✓ VERIFIED | Lines 26, 54, 68. |
| `core/models/.../dto/InvitationDtos.kt` | isRevoked field | ✓ VERIFIED | Line 31: `val isRevoked: Boolean = false` |
| `core/models/.../routes/ApiRoutes.kt` | ListInvitations, RevokeInvitation, ResendInvitation | ✓ VERIFIED | Lines 93, 96, 99. |
| `core/models/.../AppError.kt` | Invitation.Revoked, Invitation.EmailMismatch | ✓ VERIFIED | Lines 184, 190. |
| `core/models/.../i18n/StringKey.kt` | 5 INVITATION_* entries | ✓ VERIFIED | Lines 66-70: REVOKED, EXPIRED, ALREADY_ACCEPTED, NOT_FOUND, EMAIL_MISMATCH. |
| `core/sdk/.../InvitationApi.kt` | 6 interface methods | ✓ VERIFIED | 53 lines, 6 suspend functions. |
| `core/sdk/.../InvitationApiImpl.kt` | 6 implementations | ✓ VERIFIED | 51 lines, 6 override methods. |
| `core/sdk/.../ErrorMapper.kt` | HTTP 410 → ServerMapped | ✓ VERIFIED | Line 82: `410 -> AppError.Client.ServerMapped(...)` |
| `core/testing/.../FakeInvitationApiBuilder.kt` | All 6 fakes | ✓ VERIFIED | 94 lines. |
| `app/admin/.../AdminPanelScreen.kt` | InvitationsSection + localized role badges | ✓ VERIFIED | Line 537 InvitationsSection, lines 278-280 and 583-585 `stringResource(Res.string.admin_role_*)`. No `member.role.value` found (raw string removed). |
| `app/auth/.../InviteAcceptViewModel.kt` | Load + Accept + GoToLogin/Register with email | ✓ VERIFIED | 126 lines, full MVI, passes email in events (lines 28-36). |
| `app/auth/.../InviteAcceptScreen.kt` | Revoked-state UI | ✓ VERIFIED | `isRevoked` guard at line 165, conditional UI at line 218. |
| `app/auth/.../RegisterViewModel.kt` | Post-registration invite navigation | ✓ VERIFIED | Lines 99-107: `getInvitation` → `NavigateToGroup(invitation.groupId)`. |
| `app/auth/.../LoginModel.kt` + `RegisterModel.kt` | invitationEmail field | ✓ VERIFIED | LoginModel:14, RegisterModel:16 — both have `invitationEmail: String? = null`. Email disables input: LoginScreen:462, RegisterScreen:567. |
| `app/designsystem/.../TerminalAvatar.kt` | imageUrl param with AsyncImage | ✓ VERIFIED | Lines 39-50: `imageUrl` param, `AsyncImage` with coil3, error fallback to initials. |
| `server/.../AvatarRoutes.kt` | PUT avatar upload | ✓ VERIFIED | 81 lines, multipart → FileService → updateAvatarUrl. |
| `server/auth/.../tables/UsersTable.kt` | avatarUrl column | ✓ VERIFIED | Line 24: `val avatarUrl = varchar("avatar_url"...)`. |
| `server/auth/.../UserRepository.kt` | updateAvatarUrl method | ✓ VERIFIED | Line 107. |
| `server/groups/test/.../InvitationRoutesTest.kt` | Integration tests | ✓ VERIFIED | 378 lines, 8 test methods, Testcontainers PostgreSQL 16-alpine. |
| `server/auth/.../di/AuthModule.kt` | NO duplicate AuthService | ✓ VERIFIED | AuthService NOT registered here (line 18 comment confirms intent, line 28 only has OAuthService). |
| `server/.../di/ServerModule.kt` | AuthService with onRegistered | ✓ VERIFIED | Lines 39-43: single registration with invitation acceptance callback. |
| `values/strings.xml` (admin) | EN role translations | ✓ VERIFIED | Lines 59-61: admin_role_member/admin/owner. |
| `values-es/strings.xml` (admin) | Spanish translations | ✓ VERIFIED | Lines 56-58: Miembro/Admin/Propietario. |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| InvitationRoutes | InvitationService.listInvitations() | Type-safe GET route | ✓ WIRED | Line 44 |
| InvitationRoutes | InvitationService.revokeInvitation() | Type-safe POST route | ✓ WIRED | Line 52 |
| InvitationRoutes | InvitationService.resendInvitation() | Type-safe POST route | ✓ WIRED | Line 60 |
| InvitationService.listInvitations | membershipRepository + memberEmails | Cross-reference member emails | ✓ WIRED | Lines 217, 228 — stale invitations for existing members overridden to isAccepted=true |
| InvitationService.resendInvitation | MemberAlreadyInGroup guard | Reject resend for existing members | ✓ WIRED | Line 310 |
| InvitationService.acceptInvitation | markAcceptedByGroupAndEmail() | Cleanup all same-email+group invitations | ✓ WIRED | Line 179 |
| InvitationService.acceptInvitation | email mismatch check | Security validation | ✓ WIRED | Line 170 |
| InvitationService.acceptInvitation | revokedAt null check | Revoke guard | ✓ WIRED | Line 151 |
| InvitationApiImpl | Groups.ListInvitations/RevokeInvitation/ResendInvitation | HTTP calls | ✓ WIRED | All 6 methods |
| AdminPanelScreen Members | stringResource(admin_role_*) | Localized role badges | ✓ WIRED | Lines 278-280 (members table), 583-585 (invitations table) |
| InviteAcceptVM.GoToLogin | NavigateToLogin(token, email) | Event emission | ✓ WIRED | Lines 28-30 |
| InviteAcceptVM.GoToRegister | NavigateToRegister(token, email) | Event emission | ✓ WIRED | Lines 34-36 |
| RegisterVM.handleRegister | sdk.getInvitation(token) → NavigateToGroup | Post-registration | ✓ WIRED | Lines 99-107 |
| ProfileVM.handleCropConfirmed | sdk.uploadAvatar() → SetAvatarUrl | SDK upload chain | ✓ WIRED | Lines 110, 120 |
| ProfileScreen | TerminalAvatar(imageUrl = state.avatarUrl) | Compose render | ✓ WIRED | Line 359 |
| DashboardScreen | TerminalAvatar(imageUrl = avatarUrl) | Compose render | ✓ WIRED | Lines 257, 309 |
| DashboardSidebar | TerminalAvatar(imageUrl = avatarUrl) | Compose render | ✓ WIRED | Line 163 |
| ServerModule | AuthService with onRegistered callback | Single DI registration | ✓ WIRED | Lines 39-43 (authModule confirmed clean — no duplicate) |
| LoginScreen | invitationEmail → disabled email field | Pre-fill and lock | ✓ WIRED | Line 462: `enabled = state.invitationEmail == null` |
| RegisterScreen | invitationEmail → disabled email field | Pre-fill and lock | ✓ WIRED | Line 567: `enabled = state.invitationEmail == null` |

### Requirements Coverage

| Requirement | Source Plan(s) | Description | Status | Evidence |
|-------------|---------------|-------------|--------|----------|
| INVITE-01 | 21-03 | Admin can invite a user to a group by email — server generates token, sends invite email with link | ✓ SATISFIED | `InvitationService.createInvitation()` generates token, `sendInvitationEmail()` sends email. Lifecycle test verifies end-to-end. |
| INVITE-02 | 21-03, 21-09 | Recipient can accept invite via token link — creates/activates account and joins group with specified role | ✓ SATISFIED | `InvitationService.acceptInvitation()` validates + adds member + cleans up duplicates. RegisterVM handles post-reg flow. AuthService DI fix (plan 09) ensures `onRegistered` callback fires. |
| INVITE-03 | 21-01, 21-02, 21-10 | Admin can view pending invitations for their group and revoke them | ✓ SATISFIED | Server: list+revoke+resend endpoints with membership consistency cross-reference. SDK: 6 methods. UI: AdminPanelScreen InvitationsSection with revoke/resend. Stale invitations for existing members shown as accepted. |
| INVITE-04 | 21-03, 21-10 | Invite tokens expire after configurable duration and cannot be reused | ✓ SATISFIED | `INVITATION_EXPIRY = 7.days` (line 56). Expiry check at line 158. Expired test in InvitationRoutesTest. Resend guard prevents resending to existing members. |
| PROF-01 | 21-03 | UsersTable has avatarUrl column and UserResponse DTO includes avatarUrl field | ✓ SATISFIED | `UsersTable.kt:24` avatarUrl column. `UserDtos.kt:13` avatarUrl field. `UserRepository.kt:107` updateAvatarUrl. |
| PROF-02 | 21-03 | User can upload a profile image via PUT /api/users/me/avatar endpoint | ✓ SATISFIED | `AvatarRoutes.kt` 81-line PUT handler: multipart → FileService → updateAvatarUrl → UserResponse. |
| PROF-03 | 21-02 | TerminalAvatar component displays image when avatarUrl is present, falls back to initials | ✓ SATISFIED | `TerminalAvatar.kt:47-50` AsyncImage when imageUrl != null, falls back on error. Wired in ProfileScreen:359, DashboardScreen:257,309, DashboardSidebar:163. **Note:** REQUIREMENTS.md tracking shows `[ ]` Pending — this is a tracking gap, not an implementation gap. |
| PROF-04 | 21-02, 21-09 | ProfileViewModel supports avatar upload intent and reflects avatar URL in state | ✓ SATISFIED | ProfileVM:110 `sdk.uploadAvatar()`, line 120 `SetAvatarUrl(user.avatarUrl)`, line 173 mutation reducer. |
| DEBT-01 | 21-03 | Integration tests cover auth + groups + invite flow end-to-end with Testcontainers PostgreSQL | ✓ SATISFIED | `InvitationRoutesTest.kt` — 378 lines, 8 test methods, `PostgreSQLContainer("postgres:16-alpine")`. **Note:** REQUIREMENTS.md tracking shows `[ ]` Pending — this is a tracking gap, not an implementation gap. |

**Tracking notes:** PROF-03 and DEBT-01 are marked `[ ] Pending` in REQUIREMENTS.md but are fully implemented and verified in the codebase. The REQUIREMENTS.md status tracking should be updated to `[x] Complete`.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| — | — | No anti-patterns found | — | — |

No TODO/FIXME/XXX/HACK/PLACEHOLDER markers in any key file. No empty implementations or stub returns. All Exposed imports (and, isNull, lowerCase) properly present. No `member.role.value` raw string remnants.

### Human Verification Required

#### 1. Email Delivery in MailHog

**Test:** Start the app with Docker Compose, create an invitation from Admin Panel, check MailHog UI
**Expected:** Email arrives with correct subject ("You've been invited to join {group}") and acceptance link containing token
**Why human:** Requires running MailHog + full SMTP stack; cannot verify email rendering programmatically

#### 2. Avatar Upload Visual Flow

**Test:** Go to Profile screen, click avatar, select image, confirm crop, verify avatar appears
**Expected:** Avatar image replaces initials in ProfileScreen, DashboardScreen, and DashboardSidebar
**Why human:** Involves file picker, image cropping dialog, and visual rendering verification

#### 3. Invitation Table Visual Layout

**Test:** As admin, navigate to Admin Panel → see pending invitations table
**Expected:** Table shows email, localized role badge, status (countdown/Expired/Revoked/Accepted), Revoke button for active, Resend button for expired/revoked
**Why human:** Visual layout, spacing, color distinction for dimmed expired/revoked rows

#### 4. Email Pre-fill on Login/Register from Invitation

**Test:** Open invite link → click Login or Register → verify email field is pre-filled and locked
**Expected:** Email field shows invitation email, field is disabled (cannot edit)
**Why human:** Requires navigating through InviteAcceptScreen → Login/Register flow

#### 5. Membership Consistency Behavior

**Test:** Invite email → user accepts → admin views invitations list → verify stale invite shows as accepted
**Expected:** No action buttons (revoke/resend) for users already in group; resend for existing member returns 409
**Why human:** Requires full multi-user workflow with database state transitions

---

_Verified: 2026-03-02T00:41:35Z_
_Verifier: Claude (gsd-verifier)_
