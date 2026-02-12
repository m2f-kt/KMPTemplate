---
phase: quick-14
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/build.gradle.kts
autonomous: true
---

<objective>
Fix build failure: androidx.compose.ui:ui-tooling in commonMain breaks iOS/WasmJs/JVM targets since it's Android-only. Move to debugImplementation.
</objective>

<tasks>
<task type="auto">
  <name>Move androidx.ui.tooling from commonMain to Android debugImplementation</name>
  <files>app/designsystem/build.gradle.kts</files>
  <action>Remove implementation(libs.androidx.ui.tooling) from commonMain.dependencies. Add top-level dependencies { debugImplementation(libs.androidx.ui.tooling) } block.</action>
</task>
</tasks>
