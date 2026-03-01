---
status: resolved
trigger: "Invitation table labels and status badges in the admin panel display in English instead of the user's selected locale (Spanish)"
created: 2026-03-01T00:00:00Z
updated: 2026-03-01T00:00:00Z
---

## Current Focus

hypothesis: Spanish strings_es.xml is missing the entire "Admin Pending Invitations Section" and "Revoke" dialog block
test: Compare string keys present in values/strings.xml vs values-es/strings.xml
expecting: Missing keys in Spanish file explain English fallback
next_action: Report root cause (diagnose-only mode)

## Symptoms

expected: Invitation table labels, status badges, revoke dialog text display in Spanish when locale is set to Spanish
actual: All invitation-related UI text renders in English regardless of locale setting
errors: N/A (not a crash — just wrong locale fallback)
reproduction: Set locale to Spanish, open Admin Panel, observe Pending Invitations section
started: Phase 21-02 (when invitation strings were added to English but not Spanish)

## Eliminated

(none needed — root cause found on first hypothesis)

## Evidence

- timestamp: 2026-03-01T00:00:00Z
  checked: AdminPanelScreen.kt — InvitationsSection composable (lines 531-613)
  found: ALL user-visible strings use stringResource(Res.string.*). No hardcoded English strings.
  implication: The composable code is correct; problem must be in resource files.

- timestamp: 2026-03-01T00:00:00Z
  checked: values/strings.xml (English) lines 38-55
  found: Contains 17 invitation/revoke string keys (admin_invitations_title, admin_invitations_none, admin_invitations_email, admin_invitations_role, admin_invitations_status, admin_invitations_actions, admin_invitations_accepted, admin_invitations_revoked, admin_invitations_expired, admin_invitations_expires_days, admin_invitations_expires_tomorrow, admin_invitations_expires_today, admin_revoke_title, admin_revoke_confirm, admin_revoke_cancel, admin_revoke_submit, admin_revoke_button)
  implication: English resources are complete.

- timestamp: 2026-03-01T00:00:00Z
  checked: values-es/strings.xml (Spanish) — entire file (114 lines)
  found: File jumps from admin_invite_done (line 33) and admin_no_group_* (lines 35-36) directly to admin_table_* headers (line 38). The entire "Admin Pending Invitations Section" block (17 keys) is ABSENT. Zero invitation/revoke keys exist in the Spanish file.
  implication: Compose resource system falls back to English default when Spanish translations are missing. This is the root cause.

## Resolution

root_cause: The Spanish translation file (values-es/strings.xml) is missing all 17 invitation and revoke dialog string resources that were added to the English file (values/strings.xml) in Phase 21-02. Compose Multiplatform's resource system falls back to the default locale (English) when a key has no translation for the active locale.
fix: (diagnose only — not applied)
verification: (diagnose only)
files_changed: []
