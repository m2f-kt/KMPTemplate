---
phase: quick-8
plan: 1
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
autonomous: true
requirements: [QUICK-8]
must_haves:
  truths:
    - "Invitation table row cells are vertically centered when status badges have different heights than text cells"
    - "Email and role columns have visible spacing between them — text does not run together"
    - "Members table rows remain correctly aligned after changes"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt"
      provides: "TerminalTableRow with vertical centering and TerminalTableCell with right padding"
  key_links:
    - from: "AdminPanelScreen.kt InvitationsSection"
      to: "TerminalTableRow / TerminalTableCell"
      via: "composable usage"
      pattern: "TerminalTableRow|TerminalTableCell"
---

<objective>
Fix two visual issues in admin panel table rows: (1) vertically center all cells in a row so badges and text align, and (2) add spacing between table columns so email/role text doesn't run together.

Purpose: Improve visual polish of admin panel invitation and member tables.
Output: Updated TerminalTable.kt with vertical centering on TerminalTableRow and right padding on TerminalTableCell.
</objective>

<execution_context>
@/Users/marc/.config/opencode/get-shit-done/workflows/execute-plan.md
@/Users/marc/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
@app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt

<interfaces>
From TerminalTable.kt — TerminalTableRow (composable content overload, line 112-137):
```kotlin
@Composable
fun TerminalTableRow(
    modifier: Modifier = Modifier,
    showBottomBorder: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    // Inner Row at line 121 does NOT set verticalAlignment — THIS IS THE BUG
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        content = content,
    )
}
```

From TerminalTable.kt — TerminalTableCell (line 184-200):
```kotlin
@Composable
fun RowScope.TerminalTableCell(
    text: String,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
) {
    BasicText(
        text = text,
        modifier = modifier.weight(1f),  // No padding — cells touch each other
        style = typography.sm.copy(color = ...),
    )
}
```

Note: TerminalSelectableTableRow already has `verticalAlignment = Alignment.CenterVertically` (line 409) — TerminalTableRow should match this.
</interfaces>
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add vertical centering to TerminalTableRow and column spacing to TerminalTableCell</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt</files>
  <action>
Two changes in TerminalTable.kt:

1. **TerminalTableRow vertical centering** (composable content overload, ~line 121):
   Add `verticalAlignment = Alignment.CenterVertically` to the inner Row. This matches TerminalSelectableTableRow which already has this alignment (line 409).

   Change from:
   ```kotlin
   Row(
       modifier = Modifier
           .fillMaxWidth()
           .padding(horizontal = 16.dp, vertical = 12.dp),
       content = content,
   )
   ```
   To:
   ```kotlin
   Row(
       modifier = Modifier
           .fillMaxWidth()
           .padding(horizontal = 16.dp, vertical = 12.dp),
       verticalAlignment = Alignment.CenterVertically,
       content = content,
   )
   ```

2. **TerminalTableCell column spacing** (~line 193):
   Add `padding(end = 8.dp)` to the BasicText modifier chain BEFORE `.weight(1f)` so cells have breathing room between columns.

   Change from:
   ```kotlin
   BasicText(
       text = text,
       modifier = modifier.weight(1f),
       ...
   )
   ```
   To:
   ```kotlin
   BasicText(
       text = text,
       modifier = modifier.weight(1f).padding(end = 8.dp),
       ...
   )
   ```

   This adds 8dp right padding to every text cell, creating consistent spacing between columns without affecting the equal-weight distribution.

Do NOT change TerminalSelectableTableRow (it already has verticalAlignment).
Do NOT change the List&lt;String&gt; convenience overload of TerminalTableRow (it delegates to the composable overload, so it inherits the fix).
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinWasmJs 2>&1 | tail -5</automated>
  </verify>
  <done>TerminalTableRow inner Row has verticalAlignment = Alignment.CenterVertically. TerminalTableCell BasicText has padding(end = 8.dp). Both members table and invitations table rows display with centered vertical alignment and visible column spacing.</done>
</task>

</tasks>

<verification>
- `./gradlew :app:designsystem:compileKotlinWasmJs` compiles without errors
- Visual inspection: invitation rows with status badges (e.g., "Expira en 6 días") show all cells vertically centered
- Visual inspection: email and role columns have clear spacing between them
</verification>

<success_criteria>
- Table row cells are vertically centered when one cell contains a badge and others contain plain text
- Email and role columns have 8dp gap between them, preventing text from running together
- No regressions in members table or selectable table components
</success_criteria>

<output>
After completion, create `.planning/quick/8-fix-invitation-table-row-vertical-alignm/8-SUMMARY.md`
</output>
