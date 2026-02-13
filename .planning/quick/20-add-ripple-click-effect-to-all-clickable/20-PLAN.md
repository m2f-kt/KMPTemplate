---
phase: quick-20
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalRipple.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalDropdownMenu.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalRadio.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
autonomous: true

must_haves:
  truths:
    - "Clicking any TerminalButton shows an animated circular ripple expanding from the press point"
    - "Clicking a TerminalListItem, TerminalDropdownMenuItem, TerminalCompactCard, TerminalCheckbox, TerminalSwitch, or TerminalRadio shows the same ripple effect"
    - "The ripple color adapts to the component context (uses textMuted with low alpha)"
    - "Disabled components do NOT show any ripple"
    - "The ripple animation is smooth: expands from press point and fades out on release"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalRipple.kt"
      provides: "Custom Indication implementation for ripple effect"
      exports: ["TerminalRippleIndication", "rememberTerminalRipple"]
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt"
      provides: "TerminalButton and TerminalIconButton with ripple"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt"
      provides: "TerminalListItem with ripple on click"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalDropdownMenu.kt"
      provides: "TerminalDropdownMenuItem with ripple"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt"
      provides: "TerminalCompactCard with ripple on click"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt"
      provides: "TerminalCheckbox with ripple"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt"
      provides: "TerminalSwitch with ripple"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalRadio.kt"
      provides: "TerminalRadio with ripple"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt"
      provides: "TerminalTableCheckbox with ripple"
  key_links:
    - from: "All clickable components"
      to: "TerminalRipple.kt"
      via: "rememberTerminalRipple() passed as indication parameter"
      pattern: "rememberTerminalRipple"
---

<objective>
Add a ripple click effect to all clickable design system components.

Purpose: The design system currently has no press feedback beyond hover states. Adding a ripple effect gives users immediate, satisfying visual feedback when they tap/click interactive elements, improving perceived responsiveness across all platforms (Android, iOS, Desktop, WASM).

Output: A custom `TerminalRippleIndication` using Foundation's `Indication` API (no Material dependency), wired into every clickable component.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/designsystem/build.gradle.kts
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTheme.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalOpacity.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalDropdownMenu.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalRadio.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Create TerminalRippleIndication using Foundation Indication API</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalRipple.kt</files>
  <action>
Create a new file `TerminalRipple.kt` in the theme package that implements a custom ripple effect using Foundation's `Indication` API. This is a pure Foundation implementation -- do NOT add any Material dependency.

The implementation must use the **new `Indication` API** (not the deprecated `IndicationInstance`). Since Compose Multiplatform Foundation provides `Indication` and `IndicationNodeFactory`, use the appropriate API available in the project's Compose version. Check which API is available: if `IndicationNodeFactory` exists, use it; otherwise use the classic `Indication` interface with `rememberUpdatedState` and `drawBehind`.

**Implementation approach -- classic Indication interface (most compatible with CMP):**

1. Create `TerminalRippleIndication` class implementing `Indication`:
   - Accept `bounded: Boolean = true` (bounded clips to component bounds, unbounded extends beyond) and `color: Color = Color.Unspecified` (unspecified means "use theme default").
   - In `rememberUpdatedInstance()`, return a `TerminalRippleIndicationInstance` that:
     - Collects `InteractionSource.interactions` as a flow
     - On `PressInteraction.Press`: records the press position (`Offset`), starts a radius expansion animation (`Animatable` from 0f to target radius using `tween(300ms)`), and starts an alpha animation (`Animatable` from 0.12f staying at 0.12f)
     - On `PressInteraction.Release` or `PressInteraction.Cancel`: animates alpha from current to 0f over 200ms
     - Target radius = `sqrt(width^2 + height^2)` to cover the entire component from any press point
     - In `ContentDrawScope.drawContent()`: draw content first, then draw a circle at the press offset with the animated radius and animated alpha. If `bounded`, clip to component bounds using `clipRect`.

2. Create `@Composable fun rememberTerminalRipple(bounded: Boolean = true, color: Color = Color.Unspecified): Indication` convenience function:
   - If color is `Color.Unspecified`, resolve to `TerminalTheme.colors.text.copy(alpha = 0.12f)` as default ripple color
   - Return `remember(bounded, color) { TerminalRippleIndication(bounded, color) }`

3. The ripple color for the circle should be the resolved color at the alpha from the animation (so the base color already has some alpha like 0.12f, and the animation controls fade-in/fade-out).

**Key details:**
- Use `kotlinx.coroutines.coroutineScope` and `launch` inside the indication instance to handle concurrent press/release animations
- The ripple should be drawn AFTER `drawContent()` so it overlays the component
- For bounded ripples, use `clipRect(0f, 0f, size.width, size.height)` around the circle draw
- Import from `androidx.compose.foundation.indication`, `androidx.compose.foundation.interaction.*`, `androidx.compose.animation.core.*`
- The TerminalRippleIndicationInstance needs to implement `drawIndication` from the Indication.IndicationInstance interface which is `fun ContentDrawScope.drawIndication()`
- Keep the animation subtle: 0.10f-0.12f peak alpha, 300ms expand, 200ms fade-out -- this matches the terminal aesthetic (understated, not flashy)
  </action>
  <verify>
Run `./gradlew :app:designsystem:compileKotlinJvm` and confirm the new file compiles without errors. Verify that `TerminalRippleIndication` and `rememberTerminalRipple` are accessible from the theme package.
  </verify>
  <done>
A `TerminalRipple.kt` file exists in the theme package, exporting `TerminalRippleIndication` (implements `Indication`) and `rememberTerminalRipple()`. The file compiles cleanly with no Material dependencies. The ripple animates: expand circle from press point over 300ms, hold at 0.12f alpha while pressed, fade out over 200ms on release.
  </done>
</task>

<task type="auto">
  <name>Task 2: Wire ripple indication into all clickable design system components</name>
  <files>
app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalDropdownMenu.kt
app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalRadio.kt
app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
  </files>
  <action>
For each clickable component, add `rememberTerminalRipple()` as the `indication` parameter and ensure `interactionSource` is passed through. The pattern for each modifier type:

**For `.clickable()` calls -- TerminalButton, TerminalIconButton, TerminalListItem, TerminalDropdownMenuItem, TerminalCompactCard:**

Replace bare `.clickable(enabled = enabled, onClick = onClick)` with:
```kotlin
.clickable(
    interactionSource = interactionSource,  // reuse existing or create one
    indication = rememberTerminalRipple(),
    enabled = enabled,
    onClick = onClick,
)
```

Component-by-component specifics:

1. **TerminalButton.kt** (TerminalButton + TerminalIconButton):
   - Both already have `val interactionSource = remember { MutableInteractionSource() }` -- reuse it
   - Replace `.clickable(enabled = enabled, onClick = onClick)` with `.clickable(interactionSource = interactionSource, indication = rememberTerminalRipple(), enabled = enabled, onClick = onClick)`
   - Import `rememberTerminalRipple` from the theme package

2. **TerminalList.kt** (TerminalListItem):
   - The clickable is conditional: `if (onClick != null && state != ListItemState.Disabled) Modifier.clickable(onClick = onClick)`
   - Add an interactionSource: `val interactionSource = remember { MutableInteractionSource() }`
   - Change to: `Modifier.clickable(interactionSource = interactionSource, indication = rememberTerminalRipple(), onClick = onClick)`
   - Also the menu ellipsis text has `.clickable(enabled = state != ListItemState.Disabled) { menuExpanded = true }` -- add ripple to this too with a separate interactionSource

3. **TerminalDropdownMenu.kt** (TerminalDropdownMenuItem):
   - Already has `val interactionSource = remember { MutableInteractionSource() }` -- reuse it
   - Replace `.clickable(onClick = onClick)` with `.clickable(interactionSource = interactionSource, indication = rememberTerminalRipple(), onClick = onClick)`

4. **TerminalCard.kt** (TerminalCompactCard):
   - The clickable is conditional: `if (onClick != null) Modifier.clickable(onClick = onClick)`
   - Add: `val interactionSource = remember { MutableInteractionSource() }`
   - Change to: `Modifier.clickable(interactionSource = interactionSource, indication = rememberTerminalRipple(), onClick = onClick)`

**For `.triStateToggleable()` calls -- TerminalCheckbox, TerminalTableCheckbox:**

5. **TerminalCheckbox.kt**:
   - Add `val interactionSource = remember { MutableInteractionSource() }`
   - Replace `.triStateToggleable(state = state, enabled = enabled, role = Role.Checkbox, onClick = onClick)` with:
   ```kotlin
   .triStateToggleable(
       state = state,
       interactionSource = interactionSource,
       indication = rememberTerminalRipple(bounded = false),
       enabled = enabled,
       role = Role.Checkbox,
       onClick = onClick,
   )
   ```
   - Use `bounded = false` so the ripple extends beyond the small checkbox box (standard UX pattern for small touch targets)

6. **TerminalTable.kt** (TerminalTableCheckbox):
   - Same pattern as TerminalCheckbox but for the private table checkbox
   - Add interactionSource, pass `indication = rememberTerminalRipple(bounded = false)` to `.triStateToggleable()`

**For `.toggleable()` calls -- TerminalSwitch:**

7. **TerminalSwitch.kt**:
   - Add `val interactionSource = remember { MutableInteractionSource() }`
   - Replace `.toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)` with:
   ```kotlin
   .toggleable(
       value = checked,
       interactionSource = interactionSource,
       indication = rememberTerminalRipple(bounded = false),
       enabled = enabled,
       role = Role.Switch,
       onValueChange = onCheckedChange,
   )
   ```

**For `.selectable()` calls -- TerminalRadio:**

8. **TerminalRadio.kt**:
   - Add `val interactionSource = remember { MutableInteractionSource() }`
   - Replace `.selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)` with:
   ```kotlin
   .selectable(
       selected = selected,
       interactionSource = interactionSource,
       indication = rememberTerminalRipple(bounded = false),
       enabled = enabled,
       role = Role.RadioButton,
       onClick = onClick,
   )
   ```

**Important notes:**
- For components that already use `interactionSource` for hover state (TerminalButton, TerminalIconButton, TerminalDropdownMenuItem), reuse the SAME interactionSource -- do NOT create a second one. The same source drives both hover and ripple.
- When reusing an existing interactionSource for `.clickable()`, you must also remove the separate `.hoverable(interactionSource)` call because `.clickable()` with an interactionSource already handles hoverable internally. Keeping both would cause duplicate hover event collection. So: remove `.hoverable(interactionSource)` from TerminalButton, TerminalIconButton, and TerminalDropdownMenuItem.
- Add `import com.m2f.template.designsystem.theme.rememberTerminalRipple` to each modified file.
- Do NOT touch TerminalSwipeReveal -- it uses raw pointer input for swipe gestures, not clickable/toggleable. The TerminalDeleteAction uses `detectTapGestures` which doesn't support Indication. Leave these as-is.
  </action>
  <verify>
Run `./gradlew :app:designsystem:compileKotlinJvm` and confirm all modified files compile without errors. Verify no import errors, no duplicate interactionSource issues. Grep for `rememberTerminalRipple` across all modified files to confirm it appears in each one. Grep for orphaned `.hoverable(interactionSource)` calls to ensure they were removed from components where `.clickable()` now receives the interactionSource.
  </verify>
  <done>
All 8 component files import and use `rememberTerminalRipple()`. Every `.clickable()`, `.toggleable()`, `.selectable()`, and `.triStateToggleable()` call in the design system now passes `indication = rememberTerminalRipple()` and a proper `interactionSource`. Small touch-target components (checkbox, switch, radio) use `bounded = false`. The project compiles cleanly with `./gradlew :app:designsystem:compileKotlinJvm`.
  </done>
</task>

</tasks>

<verification>
1. `./gradlew :app:designsystem:compileKotlinJvm` passes with zero errors
2. `grep -r "rememberTerminalRipple" app/designsystem/src/` shows the function used in all 8 component files plus defined in TerminalRipple.kt
3. `grep -rn "\.clickable(" app/designsystem/src/commonMain/ | grep -v "indication"` returns zero results (all clickable calls now have indication) -- except for any intentional bare clickable that was kept
4. No Material imports exist: `grep -r "compose.material" app/designsystem/src/` returns nothing
</verification>

<success_criteria>
- TerminalRipple.kt exists with TerminalRippleIndication implementing Foundation's Indication interface
- rememberTerminalRipple() composable function available for all components
- All clickable components (TerminalButton, TerminalIconButton, TerminalListItem, TerminalDropdownMenuItem, TerminalCompactCard, TerminalCheckbox, TerminalSwitch, TerminalRadio, TerminalTableCheckbox) use the ripple indication
- No Material dependency added -- purely Foundation-based
- Project compiles successfully across all targets
</success_criteria>

<output>
After completion, create `.planning/quick/20-add-ripple-click-effect-to-all-clickable/20-SUMMARY.md`
</output>
