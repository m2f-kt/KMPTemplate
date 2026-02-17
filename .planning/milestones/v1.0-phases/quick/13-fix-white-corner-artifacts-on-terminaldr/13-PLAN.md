---
phase: quick-13
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalDropdownMenu.kt
autonomous: true
---

<objective>
Fix white corner artifacts on TerminalDropdownMenu caused by the .shadow() modifier drawing a white-filled rectangle base that peeks through rounded corners.
</objective>

<tasks>
<task type="auto">
  <name>Remove .shadow() from TerminalDropdownMenu and its preview</name>
  <files>TerminalDropdownMenu.kt</files>
  <action>Remove .shadow(shadows.sm.blur, shape) from both the composable and preview Box modifier chains. Remove unused shadow import and shadows variable.</action>
</task>
</tasks>
