---
phase: quick-16
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalSwipeReveal.kt
autonomous: true
---

<objective>
Fix three bugs in TerminalSwipeReveal: actions visible by default (height), wrong layering (no opaque bg), swipe gesture not working (conflicting pointerInput blocks).
</objective>

<tasks>
<task type="auto">
  <name>Fix height, layering, and gesture handling in TerminalSwipeReveal</name>
  <files>TerminalSwipeReveal.kt</files>
</task>
</tasks>
