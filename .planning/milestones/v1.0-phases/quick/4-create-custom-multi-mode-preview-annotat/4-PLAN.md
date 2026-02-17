---
phase: quick-4
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalPreview.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/TerminalText.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalTextarea.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalAlert.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalBadge.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalProgress.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalTooltip.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/display/TerminalAvatar.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/display/TerminalDivider.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/display/TerminalKbd.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalRadio.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
autonomous: true
must_haves:
  truths:
    - "A single @TerminalPreview annotation generates Light, Dark, and Desktop-wide previews for any composable"
    - "All 17 component files use @TerminalPreview instead of bare @Preview"
    - "No bare @Preview annotations remain in the designsystem module"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalPreview.kt"
      provides: "Multi-mode preview annotation combining Light, Dark, and Desktop modes"
      contains: "@Preview"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt"
      provides: "TerminalButton previews using @TerminalPreview"
      contains: "@TerminalPreview"
  key_links:
    - from: "all 17 component files"
      to: "theme/TerminalPreview.kt"
      via: "import and annotation usage"
      pattern: "import com\\.m2f\\.template\\.designsystem\\.theme\\.TerminalPreview"
---

<objective>
Create a custom multi-mode @TerminalPreview annotation that wraps Light Mode, Dark Mode, and Desktop-wide previews into a single annotation, then replace all existing @Preview annotations across the 17 designsystem component files with it.

Purpose: Eliminate boilerplate and ensure every component is always previewed in all three modes consistently.
Output: One new annotation file + 17 updated component files.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTheme.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Create @TerminalPreview multi-mode annotation</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalPreview.kt</files>
  <action>
Create a new file `TerminalPreview.kt` in the `theme/` package alongside TerminalTheme.kt.

Define a custom annotation `@TerminalPreview` that stacks three `@Preview` annotations:

```kotlin
package com.m2f.template.designsystem.theme

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Light Mode",
    uiMode = AndroidUiModes.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark Mode",
    uiMode = AndroidUiModes.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "Desktop",
    widthDp = 1024,
)
annotation class TerminalPreview
```

This uses `widthDp = 1024` for the desktop/wide-screen preview to simulate a landscape/big-screen layout.
The annotation lives in the `theme` package because it is a design-system-level concern, next to TerminalTheme.
  </action>
  <verify>File exists at the expected path. Confirm it compiles by running: `./gradlew :app:designsystem:compileKotlinDesktop` (or the appropriate compile task). No errors expected since it only uses standard AndroidX preview annotations.</verify>
  <done>TerminalPreview.kt exists in the theme package with three stacked @Preview annotations (Light, Dark, Desktop widthDp=1024).</done>
</task>

<task type="auto">
  <name>Task 2: Replace all @Preview with @TerminalPreview across 17 files</name>
  <files>
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/TerminalText.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalTextarea.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalAlert.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalBadge.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalProgress.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalTooltip.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/display/TerminalAvatar.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/display/TerminalDivider.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/display/TerminalKbd.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalRadio.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
  </files>
  <action>
For each of the 17 files, perform these changes:

**Import replacement:**
- Remove: `import androidx.compose.ui.tooling.preview.Preview`
- Remove: `import androidx.compose.ui.tooling.preview.AndroidUiModes` (only in TerminalButton.kt)
- Add: `import com.m2f.template.designsystem.theme.TerminalPreview`

**Annotation replacement:**

For the 16 files with simple `@Preview`:
- Replace `@Preview` (the bare annotation before each preview composable) with `@TerminalPreview`

For TerminalButton.kt specifically:
- The main `TerminalButtonPreview()` function currently has two stacked `@Preview(name="Light Mode"...)` and `@Preview(name="Dark Mode"...)` annotations. Remove BOTH and replace with a single `@TerminalPreview`.
- The `TerminalIconButtonPreview()` function has a bare `@Preview`. Replace with `@TerminalPreview`.

**Important:** Do NOT change any preview function bodies, function names, visibility modifiers, or the TerminalTheme wrapper pattern. Only the annotations and imports change.
  </action>
  <verify>
1. Run `grep -r "^@Preview" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/` -- should return ZERO matches (no bare @Preview left).
2. Run `grep -r "@TerminalPreview" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/` -- should return 19 matches (one per preview function: 17 files, plus TerminalButton.kt has 2 preview functions = 18 @TerminalPreview usages, plus the annotation definition itself = 19... actually the definition file does not use @TerminalPreview on itself). Expected: 18 matches across component files.
3. Run `grep -r "import androidx.compose.ui.tooling.preview.Preview" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/` -- should return ZERO matches (old import removed from all component files).
4. Compile: `./gradlew :app:designsystem:compileKotlinDesktop` passes without errors.
  </verify>
  <done>All 17 component files use @TerminalPreview. Zero bare @Preview annotations remain in the designsystem components directory. Zero old Preview/AndroidUiModes imports remain. Project compiles successfully.</done>
</task>

</tasks>

<verification>
1. `grep -rn "^@Preview" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/` returns no results
2. `grep -rn "@TerminalPreview" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/` returns 18 component matches + the annotation definition
3. `./gradlew :app:designsystem:compileKotlinDesktop` compiles without errors
4. The new `TerminalPreview.kt` file exists in the theme package with Light, Dark, and Desktop @Preview stacking
</verification>

<success_criteria>
- @TerminalPreview annotation exists combining Light Mode, Dark Mode, and Desktop (widthDp=1024) previews
- All 17 component files migrated from @Preview to @TerminalPreview
- No bare @Preview imports or annotations remain in the components directory
- Designsystem module compiles successfully
</success_criteria>

<output>
After completion, create `.planning/quick/4-create-custom-multi-mode-preview-annotat/4-SUMMARY.md`
</output>
