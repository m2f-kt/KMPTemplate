---
status: complete
phase: 21-group-invitations-profiles
source: [21-04-SUMMARY.md, 21-05-SUMMARY.md, 21-06-SUMMARY.md, 21-07-SUMMARY.md, 21-08-SUMMARY.md, 21-09-SUMMARY.md, 21-10-SUMMARY.md]
started: 2026-03-02T10:00:00Z
updated: 2026-03-02T10:08:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Spanish Translations in Invitation Table
expected: Open the admin panel for a group with your app set to Spanish locale. The invitations section should display all labels in Spanish: title, column headers (Email, Rol, Estado, Acciones), status badges (Aceptada, Revocada, Expirada, expiry countdowns), revoke dialog text, and the "Reenviar" button.
result: pass

### 2. Post-Registration Invitation Navigation
expected: Open an invitation link while logged out. Register a new account. After registration completes, the app should automatically navigate to the group you were invited to (not the dashboard). The invitation should be accepted and you should be a member of that group.
result: pass

### 3. Revoked Invitation Error Message
expected: Revoke an invitation from the admin panel, then open the revoked invitation link (while logged in with the invited email). The screen should show a specific "revoked" error message (not a generic error). Action buttons (Login/Register) should be hidden.
result: pass

### 4. Resend Invitation from Admin Panel
expected: In the admin panel invitations table, expired or revoked invitations should show a "Resend" button. Click it — the old invitation should be replaced by a new one with a fresh 7-day expiry. The new invitation appears in the table and a new email is sent.
result: pass

### 5. Email Validation on Invitation Acceptance
expected: Open an invitation link while logged in with a DIFFERENT email than the one invited. The server should reject the acceptance with a specific error message about email mismatch (not a generic error).
result: pass

### 6. Localized Role Badges in Tables
expected: In the admin panel with Spanish locale, both the Invitations table and the Members table should show localized role text: "Miembro" (not MEMBER), "Admin" (not ADMIN), "Propietario" (not OWNER).
result: pass

### 7. Email Pre-fill from Invitation Flow
expected: Open an invitation link while logged out. Click "Login" or "Register". The email field should be pre-filled with the invited email address and the field should be disabled (not editable).
result: pass

### 8. Resend Prevented for Existing Members
expected: If a user has already accepted an invitation and is a member of the group, attempting to resend an invitation to that email from the admin panel should be rejected (the server prevents resending to existing members).
result: pass

### 9. Invitation List Shows Correct Status for Members
expected: In the admin panel invitations table, if a user has already joined the group (accepted invitation), their invitation should show as "Accepted" even if the raw database status is different (e.g., pending from a duplicate invitation).
result: pass

## Summary

total: 9
passed: 9
issues: 0
pending: 0
skipped: 0

## Gaps

[none]
