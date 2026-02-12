---
phase: quick-12
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalDropdownMenu.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
autonomous: true

must_haves:
  truths:
    - "TerminalDropdownMenu renders a floating popup with surface bg, border, shadow, and rounded corners"
    - "TerminalDropdownMenuItem shows text with hover highlight using inset background"
    - "TerminalListItem shows an ellipsis trigger when menuItems is provided"
    - "Clicking ellipsis opens the dropdown, clicking outside or on an item dismisses it"
    - "menuItems takes precedence over trailingContent when both are provided"
    - "Disabled state shows dots but does not open the menu"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalDropdownMenu.kt"
      provides: "TerminalDropdownMenu and TerminalDropdownMenuItem composables"
      exports: ["TerminalDropdownMenu", "TerminalDropdownMenuItem"]
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt"
      provides: "Updated TerminalListItem with menuItems parameter and preview"
      contains: "menuItems"
  key_links:
    - from: "TerminalList.kt (TerminalListItem)"
      to: "TerminalDropdownMenu.kt (TerminalDropdownMenu)"
      via: "menuItems lambda rendered inside Popup-anchored Box"
      pattern: "TerminalDropdownMenu"
    - from: "TerminalDropdownMenu.kt"
      to: "Popup from androidx.compose.ui.window"
      via: "Popup composable for floating overlay"
      pattern: "import androidx\\.compose\\.ui\\.window\\.Popup"
---

<objective>
Add a reusable TerminalDropdownMenu composable and integrate it into TerminalListItem as an optional contextual menu triggered by an ellipsis icon.

Purpose: List items in the terminal design system need contextual actions (view details, terminate, copy PID) accessible through a floating dropdown menu anchored to an ellipsis dots icon.
Output: New TerminalDropdownMenu.kt file + updated TerminalList.kt with menuItems support and preview.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalTooltip.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTheme.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Create TerminalDropdownMenu and TerminalDropdownMenuItem composables</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalDropdownMenu.kt</files>
  <action>
Create a new file `TerminalDropdownMenu.kt` in the `components/data` package (same package as TerminalList) with two public composables:

**TerminalDropdownMenu:**
```
@Composable
fun TerminalDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
)
```
- When `expanded` is true, render a `Popup` (from `androidx.compose.ui.window`) wrapping the menu content.
- Popup content structure: `Box` with shadow, clip, surface background, border, rounded corners (radius.md), containing a `Column` for menu items.
- Styling follows TerminalTooltip pattern exactly: `shadow(shadows.sm.blur, shape)`, `clip(shape)`, `background(colors.surface)`, `border(borders.thin, colors.border, shape)`.
- Add vertical padding of 4.dp on the Column (so items don't touch the top/bottom border).
- For dismiss-on-click-outside: wrap the Popup content in a Box that uses `pointerInput` with `detectTapGestures` to NOT propagate, and add a separate fullscreen transparent overlay Box before the menu content that calls `onDismissRequest` on tap. Alternative simpler approach: use `Popup(onDismissRequest = onDismissRequest)` if the Popup overload supports it -- check the API. If the Foundation Popup does not have `onDismissRequest`, handle dismissal in the TerminalListItem integration (clicking an item calls onDismissRequest, and handle outside clicks via `pointerInput` on the Popup's background).
- Minimum width: 160.dp on the Column.

**TerminalDropdownMenuItem:**
```
@Composable
fun TerminalDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
)
```
- Row layout with horizontal padding 12.dp, vertical padding 8.dp.
- If `leadingIcon` provided, render it followed by 8.dp spacer.
- Text styled with `typography.sm.copy(color = colors.text)`.
- Hover state: use `MutableInteractionSource` + `collectIsHoveredAsState` + `hoverable` modifier (same pattern as TerminalButton). When hovered, background changes to `colors.inset`.
- Clickable via `.clickable(onClick = onClick)`.
- Full width: `fillMaxWidth()`.

**Preview:**
Add a `@TerminalPreview` private preview function `TerminalDropdownMenuPreview` showing the menu expanded with 3 items: "View Details", "Terminate" (with a text "X" as leading icon), "Copy PID". Wrap in TerminalTheme + Column with bg padding.

Imports needed:
- `androidx.compose.ui.window.Popup`
- `androidx.compose.foundation.hoverable`, `MutableInteractionSource`, `collectIsHoveredAsState`
- `androidx.compose.foundation.clickable`
- Standard foundation layout imports (Box, Column, ColumnScope, Row, Spacer, fillMaxWidth, padding, width, height)
- `androidx.compose.foundation.background`, `border`
- `androidx.compose.foundation.shape.RoundedCornerShape`
- `androidx.compose.foundation.text.BasicText`
- `androidx.compose.ui.draw.clip`, `shadow`
- `TerminalTheme`, `TerminalPreview`
  </action>
  <verify>
Build the designsystem module: `./gradlew :app:designsystem:compileCommonMainKotlinMetadata` compiles without errors.
  </verify>
  <done>
TerminalDropdownMenu.kt exists with both composables and preview. TerminalDropdownMenu renders a Popup with surface/border/shadow styling. TerminalDropdownMenuItem renders a hoverable, clickable row with text and optional leading icon.
  </done>
</task>

<task type="auto">
  <name>Task 2: Integrate menuItems into TerminalListItem and update preview</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt</files>
  <action>
Modify `TerminalListItem` in TerminalList.kt:

**1. Add `menuItems` parameter:**
```kotlin
fun TerminalListItem(
    text: String,
    modifier: Modifier = Modifier,
    state: ListItemState = ListItemState.Default,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable (iconColor: Color) -> Unit)? = null,
    trailingContent: (@Composable (actionColor: Color) -> Unit)? = null,
    menuItems: (@Composable ColumnScope.() -> Unit)? = null,
)
```

**2. Add expanded state** inside the composable body, right after the color resolution block:
```kotlin
var menuExpanded by remember { mutableStateOf(false) }
```

**3. Replace the trailing content rendering block.** Currently the code has:
```kotlin
if (trailingContent != null) {
    Spacer(modifier = Modifier.width(12.dp))
    trailingContent(actionColor)
}
```
Replace with logic that prioritizes `menuItems` over `trailingContent`:
```kotlin
if (menuItems != null) {
    Spacer(modifier = Modifier.width(12.dp))
    Box {
        // Ellipsis dots trigger -- three horizontal dots as text
        BasicText(
            text = "\u2022\u2022\u2022",  // three bullet dots (or use "\u22EF" midline ellipsis)
            modifier = Modifier
                .clickable(enabled = state != ListItemState.Disabled) {
                    menuExpanded = true
                }
                .padding(horizontal = 4.dp, vertical = 2.dp),
            style = typography.sm.copy(
                color = actionColor,
                letterSpacing = 2.sp,
            ),
        )
        TerminalDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            // Wrap each menu item click to also dismiss the menu
            menuItems()
        }
    }
} else if (trailingContent != null) {
    Spacer(modifier = Modifier.width(12.dp))
    trailingContent(actionColor)
}
```

Note: Use `\u22EF` (midline horizontal ellipsis) for the three dots character -- it renders as three horizontally centered dots which matches the "lucide ellipsis" concept. If it does not render well with the monospace font, fall back to `"\u2026"` (standard horizontal ellipsis) or `"..."`. The executor should pick whichever renders best.

**4. Add needed imports** at the top of the file:
- `import androidx.compose.runtime.getValue`
- `import androidx.compose.runtime.mutableStateOf`
- `import androidx.compose.runtime.remember`
- `import androidx.compose.runtime.setValue`
- `import androidx.compose.ui.unit.sp`
- `import com.m2f.template.designsystem.components.data.TerminalDropdownMenu`
- `import com.m2f.template.designsystem.components.data.TerminalDropdownMenuItem`

Note: TerminalDropdownMenu and TerminalDropdownMenuItem are in the same package, so they may not need explicit imports. Include them only if needed.

**5. Update the preview** `TerminalListPreview` to demonstrate the dropdown. Change the first or second item to use `menuItems`:
```kotlin
TerminalListItem(
    text = "node_process",
    subtitle = "PID: 1234",
    state = ListItemState.Default,
    menuItems = {
        TerminalDropdownMenuItem(text = "View Details", onClick = {})
        TerminalDropdownMenuItem(text = "Terminate", onClick = {})
        TerminalDropdownMenuItem(text = "Copy PID", onClick = {})
    },
)
```
Keep at least one item with no menuItems and no trailingContent, and keep one with Disabled state to demonstrate that dots show but are not clickable.

Also add the Disabled state item with menuItems to show the non-clickable dots:
```kotlin
TerminalListItem(
    text = "legacy_service",
    subtitle = "PID: 3456",
    state = ListItemState.Disabled,
    menuItems = {
        TerminalDropdownMenuItem(text = "View Details", onClick = {})
    },
)
```
  </action>
  <verify>
Build the full designsystem module: `./gradlew :app:designsystem:compileCommonMainKotlinMetadata` compiles without errors. Verify the preview function is present and compiles.
  </verify>
  <done>
TerminalListItem accepts optional `menuItems` lambda. When provided, an ellipsis trigger appears instead of trailingContent. Clicking the dots toggles a TerminalDropdownMenu popup. Disabled state renders dots but prevents opening. Preview shows items with contextual menus containing "View Details", "Terminate", "Copy PID".
  </done>
</task>

</tasks>

<verification>
- `./gradlew :app:designsystem:compileCommonMainKotlinMetadata` passes
- TerminalDropdownMenu.kt exists in components/data/ with TerminalDropdownMenu and TerminalDropdownMenuItem
- TerminalList.kt has menuItems parameter on TerminalListItem
- Preview functions compile and show dropdown menu items
</verification>

<success_criteria>
- New TerminalDropdownMenu composable renders floating popup with surface/border/shadow/radius styling
- New TerminalDropdownMenuItem renders hoverable clickable row with text, optional icon, and inset hover bg
- TerminalListItem.menuItems parameter triggers ellipsis icon as trailing action
- menuItems takes precedence over trailingContent
- Disabled list items show dots but do not open menu
- All code uses Foundation-level composables only (no Material3)
- Popup imported from androidx.compose.ui.window
</success_criteria>

<output>
After completion, create `.planning/quick/12-add-contextual-dropdown-menu-to-terminal/12-SUMMARY.md`
</output>
