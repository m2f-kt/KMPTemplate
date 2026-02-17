---
status: diagnosed
phase: 04-navigation-ui-components
source: [04-01-SUMMARY.md, 04-02-SUMMARY.md, 04-03-SUMMARY.md, 04-04-SUMMARY.md, 04-05-SUMMARY.md]
started: 2026-02-12T12:00:00Z
updated: 2026-02-12T12:15:00Z
---

## Current Test

[testing complete]

## Tests

### 1. App launches with TerminalTheme on Desktop
expected: Run the desktop app. The app window opens showing a login placeholder screen with a dark terminal-styled background and light text. No Material3 default white/purple theming visible.
result: pass

### 2. JetBrains Mono font renders
expected: Text in the app renders in JetBrains Mono (monospace). Characters like "W" and "i" should appear fixed-width. If you compare with a proportional font, the difference should be obvious.
result: pass

### 3. Navigate from Login to Dashboard
expected: On the login placeholder screen, there is a clickable element to navigate to the dashboard. Clicking it takes you to the dashboard placeholder screen.
result: pass

### 4. Auth back stack cleared after login
expected: After navigating from login to dashboard, pressing the system back button (or back gesture) does NOT return to the login screen. The app either exits or stays on dashboard.
result: pass

### 5. Navigate to Register from Login
expected: On the login placeholder screen, there is a clickable element to go to the register screen. Clicking it shows the register placeholder screen.
result: pass

### 6. Navigate to Profile from Dashboard
expected: On the dashboard placeholder screen, there is a clickable element to go to the profile screen. Clicking it shows the profile placeholder screen.
result: pass

### 7. All KMP targets compile
expected: Running `./gradlew assemble` (or at minimum jvmMain + wasmJsMain compilations) completes with BUILD SUCCESSFUL. No compilation errors in designsystem, composeApp, or feature modules.
result: pass

### 8. Design system components compile without Material3
expected: The app:designsystem module compiles with zero Material3 imports. All 17+ component files (Button, Input, Card, Alert, Badge, Progress, Tooltip, Checkbox, Switch, Radio, Table, List, Kbd, Avatar, Divider, etc.) use only Foundation primitives.
result: issue
reported: "they compile but there are no previews and this is critical to have"
severity: major

## Summary

total: 8
passed: 7
issues: 1
pending: 0
skipped: 0

## Gaps

- truth: "Design system components have @Preview composables so developers can visually inspect each component variant without running the full app"
  status: failed
  reason: "User reported: they compile but there are no previews and this is critical to have"
  severity: major
  test: 8
  root_cause: "Missing preview dependencies (compose.ui.tooling.preview, compose.preview, compose.uiTooling) in app:designsystem/build.gradle.kts and zero @Preview annotated functions across all 17 component files"
  artifacts:
    - path: "app/designsystem/build.gradle.kts"
      issue: "Missing compose.ui.tooling.preview, compose.preview, compose.uiTooling dependencies"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/"
      issue: "17 component files have zero @Preview composables"
  missing:
    - "Add preview dependencies to app:designsystem build.gradle.kts"
    - "Add @Preview functions to each component file showing all variants wrapped in TerminalTheme"
  debug_session: ".planning/debug/missing-component-previews.md"
