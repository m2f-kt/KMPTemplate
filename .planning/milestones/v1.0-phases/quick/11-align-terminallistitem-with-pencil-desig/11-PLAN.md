---
phase: quick-11
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
autonomous: true
must_haves:
  truths:
    - "TerminalList header uses 12dp vertical / 16dp horizontal padding, FontWeight.Medium title, '[N items]' count format with Arrangement.SpaceBetween"
    - "TerminalListItem uses 12dp vertical / 16dp horizontal padding and 12dp gaps between icon/content/action slots"
    - "TerminalListItem has 2dp content gap between title and subtitle"
    - "showBottomBorder parameter is removed; items have no individual bottom borders"
    - "Selected state renders a 2dp left accent border via drawBehind"
    - "Selected title uses FontWeight.Medium"
    - "Subtitle color differentiates by state: Default=textDim, Hover=textMuted, Selected=textMuted, Disabled=textDim"
    - "Leading icon color per state: Default=textMuted, Hover=text, Selected=accent, Disabled=textMuted"
    - "Trailing action icon color per state: Default=textMuted, Hover=text, Selected=accent, Disabled=textDim"
    - "Preview shows process_list content with terminal-like item names"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt"
      provides: "Pencil-aligned TerminalList and TerminalListItem"
  key_links:
    - from: "TerminalListItem"
      to: "drawBehind"
      via: "Selected state left border"
      pattern: "drawBehind.*drawRect.*accent"
---

<objective>
Align TerminalList and TerminalListItem with the Pencil design specification.

Purpose: Match the Figma/Pencil design tokens for padding, gaps, colors, font weights, selected state border, and remove the non-Pencil showBottomBorder parameter.
Output: Updated TerminalList.kt with all Pencil alignments applied.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalAlert.kt (drawBehind left border pattern)
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTheme.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Align TerminalList header and TerminalListItem with Pencil design</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt</files>
  <action>
Apply ALL the following changes to TerminalList.kt in a single pass:

**TerminalList header changes:**
1. Header Row padding: change from `horizontal = spacing.sm, vertical = spacing.xs` to `horizontal = 16.dp, vertical = 12.dp` (literal values, Pencil [12, 16])
2. Title fontWeight: change from `FontWeight.SemiBold` to `FontWeight.Medium` (Pencil fontWeight 500)
3. Count format: change `count.toString()` to `"[$count items]"` (Pencil shows `[4 items]`)
4. Header layout: change Row to use `Arrangement.SpaceBetween` horizontally. Remove the `Spacer(modifier = Modifier.width(spacing.xs))` between title and count. The title goes left, count goes far right.

**TerminalListItem structural changes:**
5. Remove `showBottomBorder: Boolean = true` parameter entirely from the composable signature.
6. Remove the entire `if (showBottomBorder)` block at the bottom of the composable (lines 193-200 with the Box border).
7. Item Row padding: change from `horizontal = spacing.sm, vertical = spacing.xs` to `horizontal = 16.dp, vertical = 12.dp` (literal values, Pencil [12, 16])
8. Gap between leading icon and content: change `Spacer(modifier = Modifier.width(spacing.sm))` to `Spacer(modifier = Modifier.width(12.dp))` (Pencil gap: 12)
9. Gap between content and trailing: change `Spacer(modifier = Modifier.width(spacing.sm))` to `Spacer(modifier = Modifier.width(12.dp))`
10. Content gap: add `Spacer(modifier = Modifier.height(2.dp))` between title BasicText and subtitle BasicText inside the Column (Pencil gap: 2 between title and subtitle)

**TerminalListItem color/state changes:**
11. Add subtitle color per state (currently hardcoded to `colors.textMuted`):
    ```kotlin
    val subtitleColor = when (state) {
        ListItemState.Default -> colors.textDim
        ListItemState.Hover -> colors.textMuted
        ListItemState.Selected -> colors.textMuted
        ListItemState.Disabled -> colors.textDim
    }
    ```
    Use `subtitleColor` in the subtitle BasicText style.

12. Selected state title: add `fontWeight = FontWeight.Medium` to the title BasicText style ONLY when state is Selected. Use a conditional: `fontWeight = if (state == ListItemState.Selected) FontWeight.Medium else FontWeight.Normal`

13. Add `iconColor` val for leading icon color per state:
    ```kotlin
    val iconColor = when (state) {
        ListItemState.Default -> colors.textMuted
        ListItemState.Hover -> colors.text
        ListItemState.Selected -> colors.accent
        ListItemState.Disabled -> colors.textMuted
    }
    ```

14. Add `actionColor` val for trailing action icon color per state:
    ```kotlin
    val actionColor = when (state) {
        ListItemState.Default -> colors.textMuted
        ListItemState.Hover -> colors.text
        ListItemState.Selected -> colors.accent
        ListItemState.Disabled -> colors.textDim
    }
    ```

15. Change the `leadingContent` and `trailingContent` lambdas from `(@Composable () -> Unit)?` to `(@Composable (iconColor: Color) -> Unit)?` so callers receive the state-appropriate color. Update the invocation sites: `leadingContent(iconColor)` and `trailingContent(actionColor)`.

    Add `import androidx.compose.ui.graphics.Color` if not already present.

16. **Selected state left border**: Add a `drawBehind` modifier to draw a 2dp left accent border when `state == ListItemState.Selected`. Follow the TerminalAlert pattern:
    ```kotlin
    .then(
        if (state == ListItemState.Selected) {
            Modifier.drawBehind {
                drawRect(
                    color = accentColor,
                    topLeft = Offset.Zero,
                    size = size.copy(width = 2.dp.toPx()),
                )
            }
        } else {
            Modifier
        },
    )
    ```
    Where `accentColor` is `colors.accent` captured before the composable body. Place this modifier AFTER `.background(backgroundColor)` on the outer Column. Add imports: `import androidx.compose.ui.draw.drawBehind`, `import androidx.compose.ui.geometry.Offset`.

**Preview update:**
17. Update `TerminalListPreview` to show process_list content matching Pencil screenshots:
    - Change list title from "Team Members" to "process_list"
    - Change count to 4
    - Item 1: `text = "node_process"`, `subtitle = "PID: 1234"`, `state = ListItemState.Default`
    - Item 2: `text = "python_script"`, `subtitle = "PID: 5678"`, `state = ListItemState.Hover`
    - Item 3: `text = "docker_container"`, `subtitle = "PID: 9012"`, `state = ListItemState.Selected`
    - Item 4: `text = "legacy_service"`, `subtitle = "PID: 3456"`, `state = ListItemState.Disabled`
    - Remove `showBottomBorder = false` from the last item.
  </action>
  <verify>
Run the Kotlin compiler check:
```bash
cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinDesktop 2>&1 | tail -20
```
Verify no compilation errors. Also verify `showBottomBorder` no longer appears in TerminalList.kt:
```bash
grep -c "showBottomBorder" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
```
Should return 0.
  </verify>
  <done>
TerminalList header uses literal 12dp/16dp padding, FontWeight.Medium, "[N items]" format, SpaceBetween layout. TerminalListItem uses literal 12dp/16dp padding, 12dp icon/content/action gaps, 2dp title-subtitle gap. showBottomBorder removed entirely. Selected state has 2dp left accent border via drawBehind. Subtitle, icon, and action colors all vary by ListItemState per Pencil spec. Leading/trailing content lambdas receive state-appropriate Color parameter. Preview shows process_list content.
  </done>
</task>

</tasks>

<verification>
1. `./gradlew :app:designsystem:compileKotlinDesktop` passes with no errors
2. No references to `showBottomBorder` remain in TerminalList.kt
3. `drawBehind` is used for selected state left border
4. Subtitle color varies by state (textDim for Default/Disabled, textMuted for Hover/Selected)
5. Icon/action color lambdas pass state-appropriate colors to caller
</verification>

<success_criteria>
TerminalList and TerminalListItem match Pencil design specification for padding, gaps, font weights, colors per state, selected border, and item border removal. Compiles successfully on Desktop target.
</success_criteria>

<output>
After completion, create `.planning/quick/11-align-terminallistitem-with-pencil-desig/11-SUMMARY.md`
</output>
