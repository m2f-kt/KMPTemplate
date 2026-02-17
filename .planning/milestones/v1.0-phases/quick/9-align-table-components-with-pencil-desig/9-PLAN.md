---
phase: quick-9
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
autonomous: true

must_haves:
  truths:
    - "Table container has 4dp corner radius, 1px border, surface fill"
    - "Header row has tableHeaderBg background with 10dp vertical and 16dp horizontal padding"
    - "Header text is 10sp fontSize, FontWeight.Medium, 0.5 letterSpacing, tableHeaderText color"
    - "Row cells have 12dp vertical and 16dp horizontal padding"
    - "Row primary text uses tableRowTextPrimary color, secondary text uses tableRowTextSecondary color"
    - "Each row has a bottom border except the last row"
    - "TerminalTableRow supports composable content (not just List<String>)"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt"
      provides: "4 table color tokens: tableHeaderBg, tableHeaderText, tableRowTextPrimary, tableRowTextSecondary"
      contains: "tableHeaderBg"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt"
      provides: "TerminalTable and TerminalTableRow composables with Pencil-aligned styling"
      contains: "tableHeaderBg"
  key_links:
    - from: "TerminalTable.kt"
      to: "TerminalColors"
      via: "table-specific color tokens"
      pattern: "colors\\.tableHeader|colors\\.tableRowText"
---

<objective>
Align TerminalTable and TerminalTableRow with Pencil design specifications.

Purpose: The current table components use wrong padding values, missing header background, wrong typography for headers, no table-specific color tokens, and the row API is limited to `List<String>` instead of composable content. This plan adds 4 table color tokens and rewrites both composables to match Pencil spec.

Output: Updated TerminalColors.kt with 4 table tokens, and updated TerminalTable.kt with correct padding, header styling, row border handling, composable row content support, and Pencil-aligned previews.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTypography.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalSpacing.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalRadius.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalPreview.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add 4 table color tokens to TerminalColors</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt</files>
  <action>
Add 4 table-specific color tokens to TerminalColors.kt following the exact pattern used for button (btn*) and card-accent (cardAccent*) token groups.

**1. Add properties to TerminalColors data class** (after the cardAccent* block, add a new "Table tokens" comment section):
```kotlin
// Table tokens
val tableHeaderBg: Color,
val tableHeaderText: Color,
val tableRowTextPrimary: Color,
val tableRowTextSecondary: Color,
```

**2. Add defaults to LocalTerminalColors** static composition local (after cardAccentBodyText):
```kotlin
tableHeaderBg = Color.Unspecified,
tableHeaderText = Color.Unspecified,
tableRowTextPrimary = Color.Unspecified,
tableRowTextSecondary = Color.Unspecified,
```

**3. Add light theme values to TerminalLightColors** (after cardAccentBodyText):
```kotlin
tableHeaderBg = Color(0xFFE5E5E5),
tableHeaderText = Color(0xFF525252),
tableRowTextPrimary = Color(0xFF171717),
tableRowTextSecondary = Color(0xFF525252),
```
These come directly from the Pencil spec: $--table-header-bg Light=#E5E5E5, $--table-header-text Light=#525252, $--table-row-text-primary Light=#171717, $--table-row-text-secondary Light=#525252.

**4. Add dark theme values to TerminalDarkColors** (after cardAccentBodyText):
```kotlin
tableHeaderBg = Color(0xFF1F1F1F),
tableHeaderText = Color(0xFFA3A3A3),
tableRowTextPrimary = Color(0xFFE5E5E5),
tableRowTextSecondary = Color(0xFFA3A3A3),
```
These come directly from the Pencil spec: $--table-header-bg Dark=#1F1F1F, $--table-header-text Dark=#A3A3A3, $--table-row-text-primary Dark=#E5E5E5, $--table-row-text-secondary Dark=#A3A3A3.
  </action>
  <verify>
Run: `cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinJvm 2>&1 | tail -20`
Must compile without errors. Grep for `tableHeaderBg` in TerminalColors.kt to confirm token is present in data class, LocalTerminalColors, TerminalLightColors, and TerminalDarkColors (4 occurrences).
  </verify>
  <done>
TerminalColors.kt contains tableHeaderBg, tableHeaderText, tableRowTextPrimary, tableRowTextSecondary with correct Pencil hex values for both light and dark themes.
  </done>
</task>

<task type="auto">
  <name>Task 2: Rewrite TerminalTable and TerminalTableRow with Pencil-aligned styling and composable row content</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt</files>
  <action>
Rewrite TerminalTable.kt to match Pencil design. Keep the same file, replace the content.

**1. TerminalTable composable changes:**

- **Corner radius**: Change `RoundedCornerShape(radius.md)` to `RoundedCornerShape(radius.sm)` -- Pencil uses `$--terminal-radius` which is 4dp, and `radius.sm = 4.dp`.
- **Container**: Keep `clip(shape)`, `border(borders.thin, colors.border, shape)`, `background(colors.surface)` -- these match Pencil (1px border, surface fill).
- **Header row background**: Add `.background(colors.tableHeaderBg)` to the header Row modifier. This is the missing header fill from Pencil.
- **Header padding**: Change from `padding(horizontal = spacing.sm, vertical = spacing.xs)` (8dp/4dp) to literal `padding(horizontal = 16.dp, vertical = 10.dp)` -- Pencil specifies [10, 16]. Use literal dp values since 10dp falls between spacing.sm=8 and spacing.md=12 (consistent with quick-07 and quick-03 decisions).
- **Header text style**: Change from `typography.xs.copy(color = colors.textMuted, fontWeight = FontWeight.SemiBold)` to:
  ```kotlin
  TextStyle(
      fontFamily = typography.fontFamily,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 0.5.sp,
      color = colors.tableHeaderText,
  )
  ```
  Pencil specifies fontSize=10, fontWeight="500" (Medium), letterSpacing=0.5, color=$--table-header-text. Use literal 10.sp since typography.xs is 11.sp (same pattern as quick-07 badge).
- **Remove the separate header bottom border Box**: In Pencil, there is no separate header border element. Each row handles its own bottom border. Remove the `Box(modifier = Modifier.fillMaxWidth().height(borders.thin).background(colors.border))` after the header row.

**2. TerminalTableRow composable changes:**

Enhance the API to support composable content while keeping backward compatibility via a convenience overload.

**New primary TerminalTableRow (composable content):**
```kotlin
@Composable
fun TerminalTableRow(
    modifier: Modifier = Modifier,
    showBottomBorder: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = TerminalTheme.colors
    val borders = TerminalTheme.borders

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            content = content,
        )

        if (showBottomBorder) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(borders.thin)
                    .background(colors.border),
            )
        }
    }
}
```
- **Row padding**: Changed from `spacing.sm/spacing.xs` (8dp/4dp) to literal `16.dp/12.dp` -- Pencil specifies [12, 16].
- **Border**: Keep the existing Box-based bottom border approach (it works, just needs to match Pencil's "bottom-only 1px" which it already does).
- Content is `@Composable RowScope.() -> Unit` so callers can use `Modifier.weight(1f)` on cells.

**Convenience overload (List<String> cells, backward compatible):**
```kotlin
@Composable
fun TerminalTableRow(
    cells: List<String>,
    modifier: Modifier = Modifier,
    showBottomBorder: Boolean = true,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    TerminalTableRow(
        modifier = modifier,
        showBottomBorder = showBottomBorder,
    ) {
        cells.forEach { cell ->
            BasicText(
                text = cell,
                modifier = Modifier.weight(1f),
                style = typography.sm.copy(color = colors.tableRowTextPrimary),
            )
        }
    }
}
```
- Uses `colors.tableRowTextPrimary` instead of generic `colors.text`.
- Still uses `typography.sm` (12sp) which matches Pencil fontSize=12.

**3. Add TerminalTableCell helper composables** for primary and secondary text styling within composable rows:
```kotlin
@Composable
fun RowScope.TerminalTableCell(
    text: String,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    BasicText(
        text = text,
        modifier = modifier.weight(1f),
        style = typography.sm.copy(
            color = if (secondary) colors.tableRowTextSecondary else colors.tableRowTextPrimary,
        ),
    )
}
```
This gives callers a simple way to differentiate primary and secondary text per Pencil spec without manually constructing TextStyles. The `secondary` flag selects between tableRowTextPrimary and tableRowTextSecondary colors.

**4. Update imports** -- add:
- `import androidx.compose.foundation.layout.RowScope`
- `import androidx.compose.ui.text.TextStyle`
- `import androidx.compose.ui.unit.sp`

Remove unused imports after rewrite (check for `FontWeight` -- still needed for header Medium).

**5. Update preview** to show Pencil-realistic content including both the List<String> convenience API and the composable content API with mixed primary/secondary cells:
```kotlin
@TerminalPreview
@Composable
private fun TerminalTablePreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // List<String> convenience API
            TerminalTable(headers = listOf("Process", "PID", "CPU")) {
                TerminalTableRow(cells = listOf("node", "1234", "12.5%"))
                TerminalTableRow(cells = listOf("gradle", "5678", "45.2%"))
                TerminalTableRow(
                    cells = listOf("chrome", "9012", "8.1%"),
                    showBottomBorder = false,
                )
            }

            // Composable content API with primary/secondary differentiation
            TerminalTable(headers = listOf("Name", "Role", "Status")) {
                TerminalTableRow {
                    TerminalTableCell(text = "alice")
                    TerminalTableCell(text = "admin", secondary = true)
                    TerminalTableCell(text = "active")
                }
                TerminalTableRow(showBottomBorder = false) {
                    TerminalTableCell(text = "bob")
                    TerminalTableCell(text = "user", secondary = true)
                    TerminalTableCell(text = "pending")
                }
            }
        }
    }
}
```
Add `import androidx.compose.foundation.layout.Arrangement` if not already imported.

**6. Update KDoc** on TerminalTable and TerminalTableRow to reflect the new API. Mention the composable content variant and the convenience List<String> overload. Document TerminalTableCell as a helper for primary/secondary text within composable rows.
  </action>
  <verify>
Run: `cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinJvm 2>&1 | tail -20`
Must compile without errors. Grep for `tableHeaderBg` in TerminalTable.kt to confirm table tokens are used. Grep for `RowScope` to confirm composable content API exists. Grep for `10.sp` to confirm header font size. Grep for `16.dp` to confirm horizontal padding.
  </verify>
  <done>
TerminalTable has header with tableHeaderBg background, 10sp/Medium/0.5 letterSpacing header text using tableHeaderText color, 16dp/10dp padding. TerminalTableRow has 16dp/12dp padding with bottom border. Two TerminalTableRow overloads exist: composable content (primary) and List<String> (convenience). TerminalTableCell helper provides primary/secondary text differentiation. Corner radius is radius.sm (4dp). Preview shows both APIs with process-themed content.
  </done>
</task>

</tasks>

<verification>
- `./gradlew :app:designsystem:compileKotlinJvm` compiles clean
- TerminalColors.kt has 4 table tokens (tableHeaderBg, tableHeaderText, tableRowTextPrimary, tableRowTextSecondary) with correct Pencil hex values for light and dark
- TerminalTable header has `.background(colors.tableHeaderBg)` fill
- Header text uses 10.sp, FontWeight.Medium, 0.5.sp letterSpacing, colors.tableHeaderText
- Header padding is 16.dp horizontal, 10.dp vertical
- Row padding is 16.dp horizontal, 12.dp vertical
- No separate header bottom border Box (removed)
- TerminalTableRow has composable content overload with RowScope
- TerminalTableRow has backward-compatible List<String> overload
- TerminalTableCell provides primary/secondary text color differentiation
- Corner radius uses radius.sm (4dp) matching $--terminal-radius
- Preview shows both API styles
</verification>

<success_criteria>
TerminalTable and TerminalTableRow fully match Pencil design spec (wRO5v, QddmF, yRRLK) with correct color tokens, padding, typography, header background, row borders, and composable content support. Clean compilation. Preview renders both convenience and composable API variants with process-themed data.
</success_criteria>

<output>
After completion, create `.planning/quick/9-align-table-components-with-pencil-desig/9-SUMMARY.md`
</output>
