---
phase: quick-7
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
autonomous: true
requirements: [QUICK-7]
must_haves:
  truths:
    - "Accepted invitations do NOT show a Resend button"
    - "Expired/revoked invitations still show the Resend button"
    - "Revoke button logic remains unchanged"
  artifacts:
    - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt"
      provides: "InvitationsSection with conditional resend visibility"
      contains: "!invitation.isAccepted"
  key_links: []
---

<objective>
Hide the Resend action for accepted invitations in the admin panel's InvitationsSection.

Purpose: Accepted invitations should not display a Resend button since the user already joined — there's nothing to resend. Currently, if an invitation has `isAccepted=true` AND `isExpired=true` (or `isRevoked=true`), the resend button still appears because the condition only checks `isExpired || isRevoked` without excluding accepted ones.

Output: Updated AdminPanelScreen.kt with `!invitation.isAccepted` guard on the resend condition.
</objective>

<execution_context>
@/Users/marc/.config/opencode/get-shit-done/workflows/execute-plan.md
@/Users/marc/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add isAccepted guard to resend button condition</name>
  <files>app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt</files>
  <action>
In the `InvitationsSection` composable, find the resend button condition on line 691:

```kotlin
if (invitation.isExpired || invitation.isRevoked) {
```

Change it to:

```kotlin
if (!invitation.isAccepted && (invitation.isExpired || invitation.isRevoked)) {
```

This ensures the Resend button only shows for expired/revoked invitations that have NOT been accepted. The parentheses preserve the original OR logic while adding the acceptance guard.

Do NOT change the revoke button condition (line 669) — it already correctly excludes accepted invitations with `!invitation.isAccepted`.
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && grep -n "isAccepted.*isExpired\|isExpired.*isAccepted" app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt</automated>
  </verify>
  <done>Resend button condition includes `!invitation.isAccepted` guard. Accepted invitations never show the Resend action regardless of expired/revoked state.</done>
</task>

</tasks>

<verification>
- grep confirms `!invitation.isAccepted` appears in the resend condition
- The revoke condition (line 669) remains unchanged: `!invitation.isAccepted && !invitation.isRevoked && !invitation.isExpired`
- Build compiles: `./gradlew :app:admin:compileKotlinWasmJs` (if available)
</verification>

<success_criteria>
- Accepted invitations do NOT show the Resend button
- Expired-only and revoked-only (non-accepted) invitations still show Resend
- Active invitations still show Revoke (no regression)
- No compilation errors
</success_criteria>

<output>
After completion, create `.planning/quick/7-hide-resend-action-for-accepted-invitati/7-SUMMARY.md`
</output>
