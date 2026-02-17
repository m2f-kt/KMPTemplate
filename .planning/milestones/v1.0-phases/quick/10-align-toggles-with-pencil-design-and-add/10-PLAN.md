---
phase: quick-10
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
autonomous: true
must_haves:
  truths:
    - "Unchecked checkbox uses badge-default-bg fill (#F5F5F5 light, #262626 dark) not surface"
    - "Checked checkbox uses btnPrimaryBg fill and btnPrimaryText checkmark color"
    - "Checkbox supports tri-state: Off (empty box), On (checkmark), Indeterminate (dash)"
    - "Checkbox and switch label gap is 10dp (not 8dp)"
    - "Switch track cornerRadius is 10dp (not 24dp pill)"
    - "Switch checked knob uses btnPrimaryText color, track on uses btnPrimaryBg"
    - "Table has optional checkbox column at position 0 with 16dp inline checkboxes"
    - "Header checkbox shows indeterminate when some rows checked, checked when all, unchecked when none"
    - "Selected table rows have tableRowSelectedBg background (#EDF2EE light, #1A231C dark)"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt"
      provides: "checkboxBg and tableRowSelectedBg color tokens"
      contains: "checkboxBg"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt"
      provides: "Tri-state checkbox with corrected Pencil colors"
      contains: "ToggleableState"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt"
      provides: "Switch with corrected gap, radius, and color tokens"
      contains: "10.dp"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt"
      provides: "Table with optional checkbox selection column"
      contains: "TerminalSelectableTable"
  key_links:
    - from: "TerminalCheckbox.kt"
      to: "TerminalColors.kt"
      via: "colors.checkboxBg for unchecked fill"
      pattern: "colors\\.checkboxBg"
    - from: "TerminalTable.kt"
      to: "TerminalCheckbox.kt"
      via: "inline 16dp checkbox variant for table rows"
      pattern: "TerminalTableCheckbox"
    - from: "TerminalTable.kt"
      to: "TerminalColors.kt"
      via: "tableRowSelectedBg for selected row background"
      pattern: "colors\\.tableRowSelectedBg"
---

<objective>
Align TerminalCheckbox and TerminalSwitch with Pencil design spec, add tri-state checkbox support (On/Off/Indeterminate), and add a checkbox selection column to TerminalTable.

Purpose: Complete Pencil design alignment for toggle/selection components and enable row selection in tables.
Output: Updated TerminalColors, TerminalCheckbox (tri-state), TerminalSwitch, and TerminalTable (with selectable variant).
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTheme.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add color tokens and align TerminalCheckbox (tri-state) and TerminalSwitch with Pencil</name>
  <files>
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
  </files>
  <action>
    **TerminalColors.kt -- Add 2 new color tokens:**

    Add `checkboxBg` token (for unchecked checkbox fill, maps to Pencil $--badge-default-bg):
    - Light: Color(0xFFF5F5F5)
    - Dark: Color(0xFF262626)

    Add `tableRowSelectedBg` token (for checked row background, maps to Pencil $--table-row-selected-bg):
    - Light: Color(0xFFEDF2EE)
    - Dark: Color(0xFF1A231C)

    Add both to: data class fields, LocalTerminalColors default (Color.Unspecified), TerminalLightColors, TerminalDarkColors.
    Place `checkboxBg` after `btnDisabledBorder` (before card tokens) with a `// Checkbox tokens` comment.
    Place `tableRowSelectedBg` after `tableRowTextSecondary` at the end of table tokens section.

    **TerminalCheckbox.kt -- Tri-state + Pencil color alignment:**

    1. Import `androidx.compose.ui.state.ToggleableState` and `androidx.compose.foundation.selection.triStateToggleable`.

    2. Add a NEW tri-state overload `TerminalCheckbox(state: ToggleableState, onClick: () -> Unit, ...)`:
       - Uses `triStateToggleable(state = state, role = Role.Checkbox, onClick = onClick)` instead of `toggleable`
       - Box fill logic:
         - `ToggleableState.Off` -> `colors.checkboxBg` background + `border(borders.default, colors.border, shape)` (unchanged border)
         - `ToggleableState.On` -> `colors.btnPrimaryBg` background, NO border
         - `ToggleableState.Indeterminate` -> `colors.btnPrimaryBg` background, NO border
       - Icon logic inside Box:
         - `ToggleableState.On` -> Canvas checkmark using `colors.btnPrimaryText` (existing checkmark drawing code)
         - `ToggleableState.Indeterminate` -> Canvas horizontal dash line: `drawLine(color = colors.btnPrimaryText, start = Offset(size.width * 0.2f, size.height * 0.5f), end = Offset(size.width * 0.8f, size.height * 0.5f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)`
         - `ToggleableState.Off` -> nothing
       - Label gap: literal `10.dp` (not `gap.sm` which is 8dp). Per quick-07/quick-08 pattern for Pencil values without matching tokens.
       - Label style: `typography.sm.copy(color = colors.text)` -- unchanged

    3. Update the EXISTING `TerminalCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, ...)` to delegate to the new tri-state overload:
       ```kotlin
       TerminalCheckbox(
           state = if (checked) ToggleableState.On else ToggleableState.Off,
           onClick = { onCheckedChange(!checked) },
           modifier = modifier,
           label = label,
           enabled = enabled,
       )
       ```
       This preserves backward compatibility for all existing call sites.

    4. Update preview to show all three states: unchecked, checked, indeterminate (with label "--partial").

    **TerminalSwitch.kt -- Pencil alignment fixes:**

    1. Track cornerRadius: change `RoundedCornerShape(radius.pill)` to `RoundedCornerShape(10.dp)` (Pencil specifies 10, which is between `radius.md`=6 and `radius.lg`=12; use literal per established pattern).

    2. Track off fill: change `colors.inset` (#EFEFEF) to `colors.accentMuted` (#E5E5E5 light). Pencil specifies #E5E5E5 which matches accentMuted in light mode. Dark mode accentMuted=#262626 is a reasonable dark track color.

    3. Track on fill: keep `colors.accent` -- it matches Pencil #525252 in light. BUT for better alignment with checkbox, change to `colors.btnPrimaryBg`. In light mode both are #525252; in dark mode btnPrimaryBg=#D4D4D4 vs accent=#A3A3A3, and Pencil dark intent is unclear so btnPrimaryBg is the safer choice (consistent with checkbox checked fill).

    4. Knob on fill: change `colors.surface` (#F5F5F5) to `colors.btnPrimaryText` (#FAFAFA light, #171717 dark). Pencil specifies #FAFAFA which is btnPrimaryText.

    5. Knob off fill: keep `colors.textDim` (#787878 vs Pencil #737373). Very close, acceptable per spec analysis.

    6. Label gap: change `gap.sm` to literal `10.dp` (same as checkbox fix).

    7. Update preview -- no structural changes needed, just verify it still renders.
  </action>
  <verify>
    Build the designsystem module:
    ```
    ./gradlew :app:designsystem:compileKotlinDesktop
    ```
    Verify no compilation errors. Specifically check that ToggleableState import resolves and triStateToggleable is available.
  </verify>
  <done>
    - TerminalColors has `checkboxBg` (Light:#F5F5F5, Dark:#262626) and `tableRowSelectedBg` (Light:#EDF2EE, Dark:#1A231C) tokens
    - TerminalCheckbox has tri-state overload accepting ToggleableState with On/Off/Indeterminate rendering
    - Existing Boolean TerminalCheckbox API preserved (delegates to tri-state)
    - Unchecked checkbox uses checkboxBg, checked/indeterminate use btnPrimaryBg fill + btnPrimaryText icon
    - TerminalSwitch uses 10dp track radius, accentMuted off track, btnPrimaryBg on track, btnPrimaryText on knob, 10dp label gap
    - Module compiles without errors
  </done>
</task>

<task type="auto">
  <name>Task 2: Add checkbox selection column to TerminalTable</name>
  <files>
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
  </files>
  <action>
    **Add a private `TerminalTableCheckbox` composable for 16dp inline table checkboxes:**

    ```kotlin
    @Composable
    private fun TerminalTableCheckbox(
        state: ToggleableState,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    )
    ```
    - Box size: 16.dp (smaller than standalone 18dp)
    - Shape: `RoundedCornerShape(TerminalTheme.radius.sm)` (same 4dp terminal-radius)
    - Fill logic identical to TerminalCheckbox tri-state:
      - Off: `colors.checkboxBg` fill + `border(borders.default, colors.border, shape)`
      - On: `colors.btnPrimaryBg` fill, no border
      - Indeterminate: `colors.btnPrimaryBg` fill, no border
    - Icon Canvas size: 10.dp (proportionally smaller)
    - Icon fontSize equivalent: 9sp proportional (use Canvas drawing, not text)
      - On: checkmark lines using `colors.btnPrimaryText`, strokeWidth 1.5.dp
      - Indeterminate: horizontal dash using `colors.btnPrimaryText`, strokeWidth 1.5.dp
    - Wrapped in `Box(modifier.size(32.dp), contentAlignment = Center)` for the 32dp cell width with centering
    - Uses `toggleable`/`triStateToggleable` with `Role.Checkbox` for accessibility

    **Add `TerminalSelectableTable` composable:**

    ```kotlin
    @Composable
    fun TerminalSelectableTable(
        headers: List<String>,
        selectedRows: Set<Int>,
        onSelectionChange: (Set<Int>) -> Unit,
        rowCount: Int,
        modifier: Modifier = Modifier,
        content: @Composable ColumnScope.(isSelected: (Int) -> Boolean) -> Unit,
    )
    ```

    Implementation details:
    - Same outer container as TerminalTable (clip, border, surface background)
    - **Header row:** Starts with a 32dp-wide checkbox cell containing TerminalTableCheckbox:
      - State logic: when `selectedRows.size == rowCount` -> ToggleableState.On, when `selectedRows.isEmpty()` -> ToggleableState.Off, else -> ToggleableState.Indeterminate
      - onClick: if all selected or some selected -> `onSelectionChange(emptySet())`; if none selected -> `onSelectionChange((0 until rowCount).toSet())`
        Wait, per spec: "Clicking header checked -> unchecks all. Clicking header unchecked -> checks all. Clicking header indeterminate -> checks all."
        So: On -> emptySet(), Off -> fullSet, Indeterminate -> fullSet.
        Simplified: if `selectedRows.size == rowCount` -> `onSelectionChange(emptySet())` else -> `onSelectionChange((0 until rowCount).toSet())`
      - Then the regular header text cells follow (same as TerminalTable)
    - **Content:** Passes `isSelected` lambda `{ index -> index in selectedRows }` to the content block

    **Add `TerminalSelectableTableRow` composable:**

    ```kotlin
    @Composable
    fun TerminalSelectableTableRow(
        index: Int,
        selected: Boolean,
        onSelectedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        showBottomBorder: Boolean = true,
        content: @Composable RowScope.() -> Unit,
    )
    ```

    Implementation:
    - Row background: if `selected` -> `colors.tableRowSelectedBg`, else transparent (Color.Transparent)
    - Starts with a 32dp-wide TerminalTableCheckbox cell:
      - state = if (selected) ToggleableState.On else ToggleableState.Off
      - onClick = { onSelectedChange(!selected) }
    - Then the content cells follow
    - Bottom border logic same as TerminalTableRow

    **Keep the existing TerminalTable, TerminalTableRow, TerminalTableCell composables unchanged** -- full backward compatibility.

    **Update preview** to add a third example showing TerminalSelectableTable:
    - 4 rows: "node/1234/12.5%", "gradle/5678/45.2%", "chrome/9012/8.1%", "docker/3456/3.2%"
    - Use `remember { mutableStateOf(setOf(0, 2)) }` for initial selection (rows 0 and 2 checked)
    - Headers: listOf("Process", "PID", "CPU")
    - Show row 0 and 2 as selected with green-ish background, rows 1 and 3 unselected
    - Header checkbox shows indeterminate state (some rows selected)

    **Imports needed:** ToggleableState, triStateToggleable, toggleable, Canvas, Offset, StrokeCap, Stroke, Role, mutableStateOf, remember, Color, Alignment, size (from layout), Arrangement.
  </action>
  <verify>
    Build the designsystem module:
    ```
    ./gradlew :app:designsystem:compileKotlinDesktop
    ```
    Verify no compilation errors. Check that TerminalSelectableTable, TerminalSelectableTableRow, and TerminalTableCheckbox are all resolvable.
  </verify>
  <done>
    - TerminalTableCheckbox renders 16dp inline checkboxes with On/Off/Indeterminate states
    - TerminalSelectableTable renders header with checkbox column + regular headers
    - Header checkbox reflects aggregate selection state (all/none/some)
    - Clicking header toggles between select-all and deselect-all
    - TerminalSelectableTableRow renders per-row checkbox + selection background (tableRowSelectedBg)
    - Existing TerminalTable/TerminalTableRow/TerminalTableCell APIs unchanged
    - Preview shows selectable table with mixed selection state
    - Module compiles without errors
  </done>
</task>

</tasks>

<verification>
1. `./gradlew :app:designsystem:compileKotlinDesktop` passes
2. TerminalColors has checkboxBg and tableRowSelectedBg tokens with correct light/dark values
3. TerminalCheckbox: tri-state overload works, Boolean overload still works (backward compatible)
4. TerminalSwitch: 10dp track radius, 10dp label gap, btnPrimaryBg/btnPrimaryText tokens
5. TerminalSelectableTable: header checkbox reflects aggregate state, row checkboxes toggle individually
6. All preview functions render without crashes
</verification>

<success_criteria>
- TerminalCheckbox supports ToggleableState.On, Off, and Indeterminate with correct Pencil colors
- TerminalSwitch uses 10dp cornerRadius and 10dp gap with aligned color tokens
- TerminalSelectableTable provides checkbox column with 16dp inline checkboxes and row selection highlighting
- All existing APIs remain backward compatible
- designsystem module compiles successfully on Desktop target
</success_criteria>

<output>
After completion, create `.planning/quick/10-align-toggles-with-pencil-design-and-add/10-SUMMARY.md`
</output>
