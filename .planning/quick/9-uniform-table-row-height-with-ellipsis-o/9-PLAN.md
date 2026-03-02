---
phase: quick-9
plan: 1
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
autonomous: true
requirements: [QUICK-9]
must_haves:
  truths:
    - "All table rows have uniform single-line height regardless of text length"
    - "Long text in table cells is truncated with ellipsis (...) instead of wrapping"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt"
      provides: "TerminalTableCell and TerminalTableRow with ellipsis overflow"
      contains: "TextOverflow.Ellipsis"
  key_links: []
---

<objective>
Add single-line ellipsis overflow to TerminalTable text cells so all rows have uniform height.

Purpose: Long text (e.g. email addresses) currently wraps to multiple lines, causing inconsistent row heights. Setting maxLines=1 with TextOverflow.Ellipsis ensures uniform rows.
Output: Updated TerminalTable.kt with ellipsis overflow on all cell text.
</objective>

<execution_context>
@/Users/marc/.config/opencode/get-shit-done/workflows/execute-plan.md
@/Users/marc/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add maxLines and ellipsis overflow to TerminalTable cell text</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt</files>
  <action>
In TerminalTable.kt, make these changes:

1. Add import: `import androidx.compose.ui.text.style.TextOverflow`

2. In `TerminalTableCell` (RowScope extension, ~line 194), add `maxLines = 1` and `overflow = TextOverflow.Ellipsis` to the BasicText call:
```kotlin
BasicText(
    text = text,
    modifier = modifier.weight(1f).padding(end = 8.dp),
    style = typography.sm.copy(
        color = if (secondary) colors.tableRowTextSecondary else colors.tableRowTextPrimary,
    ),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
)
```

3. In the `TerminalTableRow(cells: List<String>)` convenience overload (~line 165), add the same parameters to its BasicText:
```kotlin
BasicText(
    text = cell,
    modifier = Modifier.weight(1f),
    style = typography.sm.copy(color = colors.tableRowTextPrimary),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
)
```

Do NOT modify the header BasicText calls (they use short labels that won't overflow).
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinWasmJs 2>&1 | tail -5</automated>
  </verify>
  <done>Both TerminalTableCell and the TerminalTableRow(cells) overload render text with maxLines=1 and TextOverflow.Ellipsis. Long text is truncated with "..." instead of wrapping, ensuring uniform row height across all table instances.</done>
</task>

</tasks>

<verification>
- `./gradlew :app:designsystem:compileKotlinWasmJs` compiles without errors
- TerminalTable.kt contains `TextOverflow.Ellipsis` in both cell rendering locations
- No changes to header row text rendering
</verification>

<success_criteria>
- All table cell text is single-line with ellipsis overflow
- Design system compiles successfully
- Existing table usages (invitation table, member table) automatically benefit from the fix
</success_criteria>

<output>
After completion, create `.planning/quick/9-uniform-table-row-height-with-ellipsis-o/9-SUMMARY.md`
</output>
