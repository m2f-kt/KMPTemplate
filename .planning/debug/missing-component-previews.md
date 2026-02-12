---
status: diagnosed
trigger: "Debug Task: Diagnose missing component previews in design system"
created: 2026-02-12T00:00:00Z
updated: 2026-02-12T00:00:00Z
symptoms_prefilled: true
goal: find_root_cause_only
---

## Current Focus

hypothesis: Design system components lack @Preview annotations and/or missing preview dependencies
test: Survey all component files and check build configuration
expecting: Find missing preview composables and potentially missing dependencies
next_action: List all component files and check for @Preview annotations

## Symptoms

expected: Component files should have @Preview composables for visual inspection without running full app
actual: Components in app:designsystem have no @Preview composables
errors: None reported
reproduction: Navigate to app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/
started: Always been missing (new design system)

## Eliminated

## Evidence

- timestamp: 2026-02-12T00:10:00Z
  checked: All component files in app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/
  found: 17 component files identified (TerminalButton, TerminalAlert, TerminalInput, etc.)
  implication: Complete list of files needing preview functions

- timestamp: 2026-02-12T00:15:00Z
  checked: Searched for @Preview annotations in designsystem module
  found: Zero preview annotations found in any component file
  implication: Confirms complete absence of preview composables

- timestamp: 2026-02-12T00:20:00Z
  checked: app/designsystem/build.gradle.kts dependencies
  found: Missing compose.uiTooling (debugImplementation) and compose.preview dependencies
  implication: Module lacks necessary preview infrastructure

- timestamp: 2026-02-12T00:25:00Z
  checked: composeApp module for preview pattern reference
  found: Uses `compose.preview` in androidMain, `compose.uiTooling` in debugImplementation, and `compose.ui.tooling.preview` library in commonMain
  implication: Established pattern exists in project for multiplatform previews

- timestamp: 2026-02-12T00:30:00Z
  checked: MainActivity.kt preview example
  found: Android-specific preview using @Preview from androidx.compose.ui.tooling.preview
  implication: Android previews use androidx annotation, not JetBrains CMP annotation

- timestamp: 2026-02-12T00:35:00Z
  checked: libs.versions.toml for preview dependencies
  found: compose-ui-tooling-preview library defined (org.jetbrains.compose.ui:ui-tooling-preview)
  implication: Project already has CMP preview library configured for cross-platform use

- timestamp: 2026-02-12T00:40:00Z
  checked: Component structure (TerminalButton, TerminalInput, TerminalAlert)
  found: All components use TerminalTheme CompositionLocals for styling, support multiple variants
  implication: Preview functions must wrap components in TerminalTheme and show all variants

## Resolution

root_cause: Design system module lacks preview infrastructure and preview composables. The app/designsystem module has no preview dependencies configured (missing compose.preview, compose.uiTooling, and compose.ui.tooling.preview) and none of the 17 component files contain @Preview annotated functions. This prevents developers from visually inspecting component variants in Android Studio/IntelliJ preview pane without running the full app.
fix:
verification:
files_changed: []
