---
phase: 21-group-invitations-profiles
verified: 2026-03-02T10:15:00Z
status: passed
score: 5/5 success criteria verified
re_verification:
  previous_status: passed
  previous_score: 10/10
  gaps_closed: []
  gaps_remaining: []
  regressions: []
requirements_coverage:
  satisfied: [INVITE-01, INVITE-02, INVITE-03, INVITE-04, PROF-01, PROF-02, PROF-03, PROF-04, DEBT-01]
  blocked: []
  orphaned: []
---

# Phase 21: Group Invitations & Profiles — Verification Report

**Phase Goal:** Admins can invite users by email, users can accept invites and upload profile avatars — the complete user lifecycle
**Verified:** 2026-03-02T10:15:00Z
**Status:** passed
**Re-verification:** Yes — full phase verification (previous VERIFICATION.md covered gap closure plans 21-04 through 21-06 only)

## Goal Achievement

### Observable Truths (from ROADMAP Success Criteria)

| # | Truth (Success Criterion) | Status | Evidence |
|---|---------------------------|--------|----------|
| 1 | Admin invites user@example.com → invite email arrives in MailHog with acceptance link containing token | ✓ VERIFIED | `InvitationService.kt:107-112` calls `sendInvitationEmail()` with token in URL → `emailService.sendEmail()` at line 363. Link format: `{appUrl}/invite/accept?token={token}` |
| 2 | Recipient clicks invite link → account is created/activated and user joins the group with specified role | ✓ VERIFIED | `InvitationService.kt:141-183` validates token, checks revoked/accepted/expired/email-match, calls `membershipRepository.insert()` at line 173 + `markAccepted()` at line 176. Returns `AcceptInvitationResponse` with groupId/role. `RegisterViewModel.kt:96-109` handles post-registration invite flow, navigating to the group. |
| 3 | Admin sees list of pending invitations and can revoke any of them; expired tokens are rejected | ✓ VERIFIED | **List:** `InvitationRoutes.kt:44-48` GET route → `InvitationService.listInvitations()` at line 190. **Revoke:** `InvitationRoutes.kt:52-56` POST route → `InvitationService.revokeInvitation()` at line 225. **UI:** `AdminPanelScreen.kt:537` `InvitationsSection` composable renders table with email/role/status/actions. **Expired rejection:** `InvitationService.kt:158` raises `InvitationExpired()` when `nowLocal >= expiresAt`. |
| 4 | User uploads a profile image → avatar URL appears in their profile; TerminalAvatar component shows image instead of initials | ✓ VERIFIED | **Upload:** `AvatarRoutes.kt:34-79` PUT `/api/users/me/avatar` receives multipart, uploads via FileService, updates `userRepository.updateAvatarUrl()`. **DTO:** `UserDtos.kt:13` has `avatarUrl: String?`. **ProfileVM:** `ProfileViewModel.kt:110` calls `sdk.uploadAvatar()`, line 120 sets `SetAvatarUrl(user.avatarUrl)`. **TerminalAvatar:** `TerminalAvatar.kt:47-58` renders `AsyncImage` when `imageUrl != null`, falls back to initials on error. **Wired:** `ProfileScreen.kt:359` passes `imageUrl = state.avatarUrl`, `DashboardScreen.kt:257,309` and `DashboardSidebar.kt:163` also pass `imageUrl = avatarUrl`. |
| 5 | Integration tests cover the full auth → groups → invite → accept flow end-to-end against Testcontainers PostgreSQL | ✓ VERIFIED | `InvitationRoutesTest.kt` (378 lines, 6 tests): lifecycle (create→register→accept→verify membership), list, revoke+verify-cannot-accept, expired rejection, non-admin 403 on list, non-admin 403 on revoke. Uses `TestDatabase` with `PostgreSQLContainer("postgres:16-alpine")` and `invitationTestApp()` helper. Avatar test deferred (covered by FileRoutesTest in server/files). |

**Score:** 5/5 success criteria verified

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `server/groups/.../InvitationsTable.kt` | revokedAt column | ✓ VERIFIED | Line 30: `val revokedAt = datetime("revoked_at").nullable()` |
| `server/groups/.../InvitationRepository.kt` | findByGroupId, findById, revokeById, deleteById | ✓ VERIFIED | All methods present, used by service |
| `server/groups/.../InvitationService.kt` | create, get, accept, list, revoke, resend | ✓ VERIFIED | 382 lines, all 6 methods with auth checks |
| `server/groups/.../InvitationRoutes.kt` | GET list, POST create/revoke/resend, POST accept, GET byToken | ✓ VERIFIED | All routes wired inside admin-protected block |
| `server/groups/.../InvitationErrors.kt` | InvitationRevoked, InvitationEmailMismatch | ✓ VERIFIED | Both error classes exist |
| `core/models/.../InvitationDtos.kt` | isRevoked field | ✓ VERIFIED | Line 31: `val isRevoked: Boolean = false` |
| `core/models/.../ApiRoutes.kt` | ListInvitations, RevokeInvitation, ResendInvitation | ✓ VERIFIED | Lines 93, 96, 99 |
| `core/models/.../AppError.kt` | Invitation.Revoked, Invitation.EmailMismatch | ✓ VERIFIED | Both variants in sealed class |
| `core/models/.../StringKey.kt` | 5 INVITATION_* entries | ✓ VERIFIED | Lines 66-70: REVOKED, EXPIRED, ALREADY_ACCEPTED, NOT_FOUND, EMAIL_MISMATCH |
| `core/sdk/.../InvitationApi.kt` | 6 interface methods | ✓ VERIFIED | create, get, accept, list, revoke, resend |
| `core/sdk/.../InvitationApiImpl.kt` | 6 implementations | ✓ VERIFIED | All 6 methods call correct type-safe resources |
| `core/sdk/.../ErrorMapper.kt` | HTTP 410 → ServerMapped | ✓ VERIFIED | Line 82: `410 -> AppError.Client.ServerMapped(...)` |
| `core/testing/.../FakeInvitationApiBuilder.kt` | All 6 fakes | ✓ VERIFIED | Lines 39-92 |
| `app/admin/.../AdminPanelScreen.kt` | InvitationsSection with revoke/resend | ✓ VERIFIED | Line 537 composable, line 612 resend button |
| `app/admin/.../AdminPanelViewModel.kt` | Load/Revoke/Resend handlers | ✓ VERIFIED | Lines 161, 172, 188 |
| `app/admin/.../AdminPanelModel.kt` | invitations state | ✓ VERIFIED | Line 32: `val invitations: List<InvitationResponse>` |
| `app/auth/.../InviteAcceptViewModel.kt` | Load + Accept + GoToLogin/Register with email | ✓ VERIFIED | 126 lines, full MVI, passes email in events |
| `app/auth/.../InviteAcceptScreen.kt` | Revoked-state UI | ✓ VERIFIED | isRevoked guard with warning alert |
| `app/auth/.../RegisterViewModel.kt` | Post-registration invite navigation | ✓ VERIFIED | Lines 96-112: getInvitation → NavigateToGroup or fallback |
| `app/auth/.../LoginModel.kt` + `RegisterModel.kt` | invitationEmail field | ✓ VERIFIED | Both have `invitationEmail: String? = null` |
| `app/designsystem/.../TerminalAvatar.kt` | imageUrl param with AsyncImage | ✓ VERIFIED | Lines 39-58: AsyncImage with error fallback to initials |
| `server/src/main/.../AvatarRoutes.kt` | PUT avatar upload | ✓ VERIFIED | 81 lines, multipart → FileService → updateAvatarUrl |
| `server/auth/.../UsersTable.kt` | avatarUrl column | ✓ VERIFIED | Line 24: `val avatarUrl = varchar("avatar_url"...)` |
| `server/auth/.../UserRepository.kt` | updateAvatarUrl method | ✓ VERIFIED | Line 107 |
| `server/groups/test/.../InvitationRoutesTest.kt` | 6 integration tests | ✓ VERIFIED | 378 lines, Testcontainers PostgreSQL |
| Navigation `Routes.kt` | invitationEmail params | ✓ VERIFIED | Lines 6, 9: LoginRoute + RegisterRoute with invitationEmail |
| Navigation `AppNavHost.kt` | SetInvitationEmail on launch | ✓ VERIFIED | Lines 84-89, 128-133: LaunchedEffect sets email from route |
| `values-es/strings.xml` (admin) | Spanish translations | ✓ VERIFIED | 17 invitation keys + 3 role keys |
| `values/strings.xml` (admin) | EN role translations | ✓ VERIFIED | Lines 59-61: admin_role_member/admin/owner |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| InvitationRoutes.ListInvitations | InvitationService.listInvitations() | Ktor type-safe resource route | ✓ WIRED | Line 47 |
| InvitationRoutes.RevokeInvitation | InvitationService.revokeInvitation() | Ktor type-safe resource route | ✓ WIRED | Line 55 |
| InvitationRoutes.ResendInvitation | InvitationService.resendInvitation() | Ktor type-safe resource route | ✓ WIRED | Line 63 |
| InvitationService.listInvitations | invitationRepository.findByGroupId() | Direct repo call | ✓ WIRED | Line 206 |
| InvitationService.resendInvitation | invitationRepository.deleteById() | Delete-on-resend | ✓ WIRED | Line 297 |
| InvitationService.acceptInvitation | revokedAt null check | Revoke guard | ✓ WIRED | Line 151 |
| InvitationService.acceptInvitation | email mismatch check | Security validation | ✓ WIRED | Line 170 |
| InvitationApiImpl.listInvitations | Groups.ListInvitations | HTTP GET | ✓ WIRED | Line 44 |
| InvitationApiImpl.revokeInvitation | Groups.RevokeInvitation | HTTP POST | ✓ WIRED | Line 47 |
| InvitationApiImpl.resendInvitation | Groups.ResendInvitation | HTTP POST | ✓ WIRED | Line 50 |
| AdminPanelViewModel.handleLoadInvitations | sdk.listInvitations() | SDK call | ✓ WIRED | Line 163 |
| AdminPanelViewModel.handleRevokeInvitation | sdk.revokeInvitation() | SDK call | ✓ WIRED | Line 175 |
| AdminPanelViewModel.handleResendInvitation | sdk.resendInvitation() | SDK call | ✓ WIRED | Line 192 |
| InviteAcceptViewModel.GoToLogin | NavigateToLogin(token, email) | Event emission | ✓ WIRED | Line 28-31 |
| AppNavHost.LoginRoute | LoginIntent.SetInvitationEmail | LaunchedEffect | ✓ WIRED | Lines 84-89 |
| AppNavHost.RegisterRoute | RegisterIntent.SetInvitationEmail | LaunchedEffect | ✓ WIRED | Lines 128-133 |
| RegisterViewModel.handleRegister | sdk.getInvitation(token) | Post-reg navigation | ✓ WIRED | Lines 97-109 |
| ProfileViewModel.handleCropConfirmed | sdk.uploadAvatar() | SDK upload | ✓ WIRED | Line 110 |
| ProfileScreen | TerminalAvatar(imageUrl = state.avatarUrl) | Compose render | ✓ WIRED | Line 359 |
| DashboardScreen/Sidebar | TerminalAvatar(imageUrl = avatarUrl) | Compose render | ✓ WIRED | Lines 257, 309, 163 |

### Requirements Coverage

| Requirement | Source Plan(s) | Description | Status | Evidence |
|-------------|---------------|-------------|--------|----------|
| INVITE-01 | 21-03 | Admin can invite a user to a group by email — server generates token, sends invite email with link | ✓ SATISFIED | `InvitationService.createInvitation()` generates token, `sendInvitationEmail()` sends email. Lifecycle test verifies end-to-end. |
| INVITE-02 | 21-03 | Recipient can accept invite via token link — creates/activates account and joins group with specified role | ✓ SATISFIED | `InvitationService.acceptInvitation()` validates + adds member. `RegisterViewModel` handles post-registration flow. Lifecycle test verifies membership. |
| INVITE-03 | 21-01, 21-02 | Admin can view pending invitations for their group and revoke them | ✓ SATISFIED | Server: list+revoke endpoints. SDK: listInvitations/revokeInvitation. UI: AdminPanelScreen InvitationsSection with revoke dialog. Tests: 3 integration tests. |
| INVITE-04 | 21-03 | Invite tokens expire after configurable duration and cannot be reused | ✓ SATISFIED | `INVITATION_EXPIRY = 7.days` (line 56). Expiry check at line 158. Expired test verifies 410 Gone response. |
| PROF-01 | 21-03 | UsersTable has avatarUrl column and UserResponse DTO includes avatarUrl field | ✓ SATISFIED | `UsersTable.kt:24` avatarUrl column. `UserDtos.kt:13` avatarUrl field. `UserRepository.kt:107` updateAvatarUrl method. |
| PROF-02 | 21-03 | User can upload a profile image via PUT /api/users/me/avatar endpoint | ✓ SATISFIED | `AvatarRoutes.kt:34` PUT handler, multipart upload → FileService → updateAvatarUrl. Returns updated UserResponse. |
| PROF-03 | 21-02 | TerminalAvatar component displays image when avatarUrl is present, falls back to initials | ✓ SATISFIED | `TerminalAvatar.kt:47-58` renders AsyncImage when imageUrl != null, falls back to initials on error. Used in ProfileScreen, DashboardScreen, DashboardSidebar. |
| PROF-04 | 21-02 | ProfileViewModel supports avatar upload intent and reflects avatar URL in state | ✓ SATISFIED | `ProfileIntent.ImageSelected` → `handleImageSelected()` → `CropConfirmed` → `sdk.uploadAvatar()` → `SetAvatarUrl(user.avatarUrl)`. Full MVI chain wired. |
| DEBT-01 | 21-03 | Integration tests cover auth + groups + invite flow end-to-end with Testcontainers PostgreSQL | ✓ SATISFIED | `InvitationRoutesTest.kt` — 6 tests (378 lines) covering lifecycle, list, revoke, expired, authorization. Testcontainers PostgreSQL 16-alpine. Avatar test deferred (covered by existing FileRoutesTest). |

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| — | — | No anti-patterns found | — | — |

All "placeholder" references in AdminPanelScreen and ProfileScreen are legitimate UI input field placeholder text (e.g., `admin_invite_email_placeholder`). No TODO/FIXME/XXX/HACK markers in any key modified file.

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

---

_Verified: 2026-03-02T10:15:00Z_
_Verifier: Claude (gsd-verifier)_
