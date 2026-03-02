---
phase: quick-4
plan: 1
type: execute
wave: 1
depends_on: []
files_modified:
  - app/admin/src/commonMain/composeResources/values-es/strings.xml
  - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
autonomous: true
requirements: [QUICK-4]

must_haves:
  truths:
    - "Remove button in member table shows 'Eliminar' in Spanish locale"
    - "Remove Member confirmation dialog is fully translated to Spanish"
    - "Register Member CTA button is no longer visible in the admin panel"
    - "Member joined dates display as DD/MM/YYYY instead of ISO-8601"
  artifacts:
    - path: "app/admin/src/commonMain/composeResources/values-es/strings.xml"
      provides: "Spanish translations for Remove Member dialog and button"
      contains: "admin_remove_member_title"
    - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt"
      provides: "Updated admin panel without Register Member CTA, with formatted dates"
  key_links:
    - from: "AdminPanelScreen.kt"
      to: "values-es/strings.xml"
      via: "stringResource(Res.string.admin_remove_*)"
      pattern: "admin_remove"
---

<objective>
Fix three issues in the admin group member view: (1) translate the Remove button and Remove Member dialog to Spanish, (2) remove the "Registrar Miembro" / "Register Member" CTA button from the admin panel, (3) format member joined dates as DD/MM/YYYY instead of raw ISO-8601.

Purpose: Polish the admin panel for Spanish-locale users and improve date readability.
Output: Updated string resources and AdminPanelScreen.kt.
</objective>

<execution_context>
@/Users/marc/.config/opencode/get-shit-done/workflows/execute-plan.md
@/Users/marc/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/admin/src/commonMain/composeResources/values/strings.xml
@app/admin/src/commonMain/composeResources/values-es/strings.xml
@app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add Spanish translations for Remove Member, remove Register Member CTA, and format dates as DD/MM/YYYY</name>
  <files>
    app/admin/src/commonMain/composeResources/values-es/strings.xml
    app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
  </files>
  <action>
**1. Add missing Spanish translations in `values-es/strings.xml`:**

Add the following entries after the existing `admin_resend_button` line (after line 53), before the Admin Role Labels section:

```xml
    <!-- Remove Member Dialog -->
    <string name="admin_remove_member_title">Eliminar Miembro</string>
    <string name="admin_remove_member_confirm">¿Eliminar a %1$s de este grupo?</string>
    <string name="admin_remove_member_cancel">Cancelar</string>
    <string name="admin_remove_member_submit">Eliminar</string>
    <string name="admin_remove_button">Eliminar</string>
    <string name="admin_table_actions">ACCIONES</string>
```

**2. Remove the Register Member CTA button in `AdminPanelScreen.kt`:**

In the "Action buttons row" section (around lines 251-266), remove the `TerminalButton` that uses `Res.string.admin_register_member_button` and its `onClick = onRegisterMember` callback. Keep only the Invite Member button. The Row wrapper can also be simplified or kept — either is fine as long as the Register Member button is gone.

Also remove the unused import: `import template.app.admin.generated.resources.admin_register_member_button`

**3. Format `joinedAt` dates as DD/MM/YYYY in `AdminPanelScreen.kt`:**

The member table cell at line ~300 currently shows `member.joinedAt.ifBlank { "-" }` which displays raw ISO-8601 datetime strings (e.g. `2026-03-02T12:34:56`).

Add a private helper function to format the date:

```kotlin
/**
 * Formats an ISO-8601 date string (e.g. "2026-03-02T12:34:56") as "DD/MM/YYYY".
 * Returns "-" if the string is blank or parsing fails.
 */
private fun formatJoinedDate(isoDate: String): String {
    if (isoDate.isBlank()) return "-"
    return try {
        val dateTime = LocalDateTime.parse(isoDate)
        val day = dateTime.dayOfMonth.toString().padStart(2, '0')
        val month = dateTime.monthNumber.toString().padStart(2, '0')
        val year = dateTime.year.toString()
        "$day/$month/$year"
    } catch (_: Exception) {
        "-"
    }
}
```

Then update the joined date cell from:
```kotlin
TerminalTableCell(
    text = member.joinedAt.ifBlank { "-" },
    secondary = true,
)
```
to:
```kotlin
TerminalTableCell(
    text = formatJoinedDate(member.joinedAt),
    secondary = true,
)
```

Note: `LocalDateTime` is already imported in this file (`import kotlinx.datetime.LocalDateTime`).
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:admin:compileCommonMainKotlinMetadata 2>&1 | tail -5</automated>
  </verify>
  <done>
    - Spanish strings.xml contains all 6 Remove Member entries (admin_remove_member_title, admin_remove_member_confirm, admin_remove_member_cancel, admin_remove_member_submit, admin_remove_button, admin_table_actions)
    - Register Member button no longer appears in AdminPanelScreen
    - Member joined dates display as DD/MM/YYYY (e.g. "02/03/2026") instead of ISO-8601
    - Project compiles without errors
  </done>
</task>

</tasks>

<verification>
- `./gradlew :app:admin:compileCommonMainKotlinMetadata` compiles successfully
- Spanish strings.xml contains `admin_remove_button`, `admin_remove_member_title`, `admin_remove_member_confirm`, `admin_remove_member_cancel`, `admin_remove_member_submit`, `admin_table_actions`
- AdminPanelScreen.kt no longer references `admin_register_member_button`
- AdminPanelScreen.kt uses `formatJoinedDate()` helper for the joined column
</verification>

<success_criteria>
- Remove button and dialog fully translated to Spanish (Eliminar)
- Register Member CTA removed from admin panel view
- Dates formatted as DD/MM/YYYY in the member table
- Project compiles cleanly
</success_criteria>

<output>
After completion, create `.planning/quick/4-fix-admin-group-member-view-translate-re/4-SUMMARY.md`
</output>
