---
phase: quick-22
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalRadio.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
autonomous: true
must_haves:
  truths:
    - "Checkbox ripple effect stays within visible component bounds"
    - "Switch ripple effect stays within visible component bounds"
    - "Radio ripple effect stays within visible component bounds"
    - "Table inline checkbox ripple stays within visible component bounds"
    - "Ripple still visually triggers on press for all selection components"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt"
      provides: "Bounded ripple for standalone checkbox"
      contains: "bounded = true"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt"
      provides: "Bounded ripple for switch"
      contains: "bounded = true"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalRadio.kt"
      provides: "Bounded ripple for radio"
      contains: "bounded = true"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt"
      provides: "Bounded ripple for inline table checkbox"
      contains: "bounded = true"
  key_links: []
---

<objective>
Fix checkbox, switch, and radio ripple effects drawing beyond component bounds.

Purpose: The ripple effect on selection components (checkbox, switch, radio) currently uses `bounded = false`, causing the ripple circle to draw outside the component's visible area. This looks ugly -- the ripple bleeds into surrounding UI. Changing to `bounded = true` clips the ripple to the component's Row/Box bounds via `clipRect` in `TerminalRippleNode.draw()`.

Output: All selection components use bounded ripples that stay visually contained.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalRipple.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalRadio.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Change all selection component ripples from unbounded to bounded</name>
  <files>
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalRadio.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
  </files>
  <action>
    In each of the four files, change `rememberTerminalRipple(bounded = false)` to `rememberTerminalRipple(bounded = true)`.

    Specific locations:
    1. **TerminalCheckbox.kt** line 71: in the `triStateToggleable` call, change `bounded = false` to `bounded = true`
    2. **TerminalSwitch.kt** line 81: in the `toggleable` call, change `bounded = false` to `bounded = true`
    3. **TerminalRadio.kt** line 65: in the `selectable` call, change `bounded = false` to `bounded = true`
    4. **TerminalTable.kt** line 240: in the `TerminalTableCheckbox` composable's `triStateToggleable` call, change `bounded = false` to `bounded = true`

    Since `bounded = true` is the default parameter of `rememberTerminalRipple()`, you can also simply remove the `bounded = false` argument entirely and call `rememberTerminalRipple()` with no arguments. Either approach is correct; prefer the explicit `bounded = true` for clarity since the original code was explicitly setting `bounded = false`.

    Do NOT modify TerminalRipple.kt -- the ripple implementation itself is correct. The `clipRect` path (bounded=true) already handles clipping properly. The fix is purely in the call sites.
  </action>
  <verify>
    Run: `cd /Users/marc/IdeaProjects/Template && grep -rn "bounded = false" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/`
    Expected: No matches found (all instances replaced).

    Run: `cd /Users/marc/IdeaProjects/Template && grep -rn "bounded = true" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/`
    Expected: 4 matches -- one each in TerminalCheckbox.kt, TerminalSwitch.kt, TerminalRadio.kt, TerminalTable.kt.

    Build check: `cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinJvm` should succeed with no errors.
  </verify>
  <done>
    All four selection component files use `bounded = true` for their ripple indication. No instances of `bounded = false` remain in the selection components or table checkbox. Project compiles successfully.
  </done>
</task>

</tasks>

<verification>
- `grep -rn "bounded = false" app/designsystem/src/` returns zero matches in selection/ and data/ directories
- `grep -rn "bounded = true" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/` returns 4 matches
- `:app:designsystem:compileKotlinJvm` compiles without errors
</verification>

<success_criteria>
Ripple effects on checkbox, switch, radio, and table checkbox are all bounded (clipped to component bounds). The ripple no longer visually overflows beyond the component area.
</success_criteria>

<output>
After completion, create `.planning/quick/22-fix-checkbox-ripple-effect-drawing-beyon/22-SUMMARY.md`
</output>
