---
phase: quick-5
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
autonomous: true
must_haves:
  truths:
    - "TerminalCardPreview shows 'process_info' default card with PID/CPU/MEM/STATUS content and kill/restart footer buttons"
    - "TerminalCardPreview shows 'active_session' accent card with HOST/PORT/LATENCY content"
    - "TerminalCardPreview shows 'system_info' info card with update notice content"
    - "TerminalCardPreview shows 'featured_process' highlighted card with Priority/CPU/Threads content"
    - "TerminalCardPreview shows 'process.log' compact card with '2.4MB - Modified 2h ago' details"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt"
      provides: "TerminalCard previews aligned with Pencil design"
      contains: "process_info"
  key_links: []
---

<objective>
Update the TerminalCardPreview() function string literals and footer buttons to match the Pencil design specification exactly. Only preview content changes -- no structural or composable modifications.

Purpose: Align card previews with the Pencil design system reference so previews accurately represent the intended design.
Output: Updated TerminalCard.kt with Pencil-accurate preview data.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Update TerminalCardPreview content to match Pencil design</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt</files>
  <action>
Edit ONLY the `TerminalCardPreview()` function (lines 373-483). Change the string literals for each card variant to match Pencil values. Do NOT change any composable structure, parameters, icon text, icon colors, variants, or modifiers -- only the title, description, content text, footer button labels, and compact card details.

**1. Default Card (lines 382-408):**
- title: "Terminal Session" -> "process_info"
- description: "Active connection" -> "// system process details"
- content TerminalText: "Session content goes here" -> "PID: 1337\nCPU: 12.4%\nMEM: 256MB\nSTATUS: running"
- First footer button text: "Cancel" -> "kill" (keep Ghost variant)
- Second footer button text: "Connect" -> "restart" (keep Default variant)

**2. Accent Card (lines 411-425):**
- title: "Network Status" -> "active_session"
- description: "All systems operational" -> "// current connection"
- content TerminalText: "Monitoring dashboard content" -> "HOST: 192.168.1.42\nPORT: 8080\nLATENCY: 12ms"

**3. Info Card (lines 428-442):**
- title: "System Notice" -> "system_info"
- description: "Scheduled maintenance" -> "// important notice"
- content TerminalText: "The server will be restarted at 02:00 UTC." -> "System update available.\nVersion: 2.4.1\nSize: 145MB"

**4. Highlighted Card (lines 445-459):**
- title: "Featured Project" -> "featured_process"
- description: "Pinned repository" -> "// high priority"
- content TerminalText: "Project details and stats" -> "Priority: HIGH\nCPU: 45.2%\nThreads: 12"

**5. Compact Card (lines 462-480):**
- title: "config.yaml" -> "process.log"
- details: "Modified 2 hours ago" -> "2.4MB \u2022 Modified 2h ago"

Leave ALL other code untouched: icons, colors, variants, modifiers, import statements, the Column wrapper, everything outside the preview function.
  </action>
  <verify>
1. Grep for old values that should no longer exist: `grep -n "Terminal Session\|Network Status\|System Notice\|Featured Project\|config.yaml" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt` -- should return ZERO matches.
2. Grep for new values: `grep -n "process_info\|active_session\|system_info\|featured_process\|process.log" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt` -- should return 5 matches (one per card).
3. Compile: `./gradlew :app:designsystem:compileKotlinDesktop` passes without errors.
  </verify>
  <done>All five card previews in TerminalCardPreview() display Pencil-accurate titles, descriptions, content, footer button labels, and compact card details. No structural changes to the composables.</done>
</task>

</tasks>

<verification>
1. Old placeholder strings ("Terminal Session", "Network Status", "System Notice", "Featured Project", "config.yaml") are gone from the preview function
2. Pencil-specified strings ("process_info", "active_session", "system_info", "featured_process", "process.log") are present
3. Footer buttons read "kill" (Ghost) and "restart" (Default) on the default card
4. Compact card details read "2.4MB . Modified 2h ago"
5. Designsystem module compiles successfully
</verification>

<success_criteria>
- All 5 card preview blocks use exact Pencil design text values
- No old placeholder text remains in the preview function
- Card composable structure, variants, icons, and modifiers are unchanged
- Project compiles without errors
</success_criteria>

<output>
After completion, create `.planning/quick/5-align-terminalcard-previews-network-stat/5-SUMMARY.md`
</output>
