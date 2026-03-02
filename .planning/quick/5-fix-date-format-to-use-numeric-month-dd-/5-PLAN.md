---
phase: quick-5
plan: 1
type: execute
wave: 1
depends_on: []
files_modified:
  - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
autonomous: true
requirements: [Q5-01]
must_haves:
  truths:
    - "Joined date displays as DD/MM/YYYY with numeric zero-padded month (e.g., 02/03/2026)"
  artifacts:
    - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt"
      provides: "formatJoinedDate with numeric month"
      contains: "monthNumber"
  key_links: []
---

<objective>
Fix formatJoinedDate() to use numeric month instead of month name.

Purpose: `dateTime.month` returns a `Month` enum (e.g., `MARCH`), which `.toString().padStart(2, '0')` renders as "MARCH" not "03". Must use `dateTime.monthNumber` to get the integer value.
Output: Correctly formatted dates showing DD/MM/YYYY (e.g., "02/03/2026" not "02/MARCH/2026")
</objective>

<execution_context>
@/Users/marc/.config/opencode/get-shit-done/workflows/execute-plan.md
@/Users/marc/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt (line 793-804)
</context>

<tasks>

<task type="auto">
  <name>Task 1: Fix month formatting to use monthNumber</name>
  <files>app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt</files>
  <action>
In `formatJoinedDate()` at line 798, change:
```kotlin
val month = dateTime.month.toString().padStart(2, '0')
```
to:
```kotlin
val month = dateTime.monthNumber.toString().padStart(2, '0')
```

`dateTime.month` is a `kotlinx.datetime.Month` enum (renders as "MARCH"). `dateTime.monthNumber` is an `Int` (renders as "3"), which padStart correctly formats to "03".
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && grep -n "monthNumber" app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt</automated>
  </verify>
  <done>formatJoinedDate() uses dateTime.monthNumber so dates display as DD/MM/YYYY with zero-padded numeric month</done>
</task>

</tasks>

<verification>
- `grep "monthNumber" app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt` shows the fix
- No `dateTime.month.toString()` pattern remains in formatJoinedDate
</verification>

<success_criteria>
- formatJoinedDate() returns "DD/MM/YYYY" with numeric month (e.g., "02/03/2026")
- Build compiles successfully
</success_criteria>

<output>
After completion, create `.planning/quick/5-fix-date-format-to-use-numeric-month-dd-/5-SUMMARY.md`
</output>
