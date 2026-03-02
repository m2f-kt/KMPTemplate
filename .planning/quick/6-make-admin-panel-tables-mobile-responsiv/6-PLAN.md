---
phase: quick-6
plan: 1
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
  - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
  - app/admin/src/commonMain/composeResources/values/strings.xml
  - app/admin/src/commonMain/composeResources/values-es/strings.xml
autonomous: false
requirements: [QUICK-6]

must_haves:
  truths:
    - "On mobile (<600dp), Email and Joined columns are hidden in the Members table"
    - "On mobile (<600dp), Email column is hidden in the Invitations table"
    - "On mobile, Remove/Revoke/Resend text buttons become icon-only buttons with tooltips"
    - "On desktop (>=600dp), all columns and text buttons remain unchanged"
    - "Tooltips on icon buttons show the translated action text (e.g., 'Eliminar' in Spanish)"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt"
      provides: "TerminalTableCell with visibleOnMobile parameter"
    - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt"
      provides: "Responsive tables with BoxWithConstraints and icon buttons on mobile"
  key_links:
    - from: "AdminPanelScreen.kt"
      to: "TerminalTable.kt"
      via: "TerminalTableCell(visibleOnMobile=false)"
      pattern: "visibleOnMobile"
    - from: "AdminPanelScreen.kt"
      to: "TerminalTooltip.kt"
      via: "TerminalTooltip wrapping TerminalIconButton on mobile"
      pattern: "TerminalTooltip"
---

<objective>
Make admin panel tables (Members and Invitations) mobile-responsive by hiding non-essential columns on small screens, replacing text CTA buttons with icon buttons on mobile, and adding translated tooltips to icon buttons.

Purpose: Tables are cramped on mobile — hide secondary columns, use compact icon buttons to save space, maintain accessibility via tooltips.
Output: Responsive admin tables that work well on both desktop and mobile.
</objective>

<execution_context>
@/Users/marc/.config/opencode/get-shit-done/workflows/execute-plan.md
@/Users/marc/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalTooltip.kt
@app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
@app/admin/src/commonMain/composeResources/values/strings.xml
@app/admin/src/commonMain/composeResources/values-es/strings.xml

<interfaces>
<!-- TerminalTable.kt key API -->
```kotlin
// TerminalTable renders headers + content rows
@Composable fun TerminalTable(headers: List<String>, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit)

// TerminalTableRow — composable content version
@Composable fun TerminalTableRow(modifier: Modifier = Modifier, showBottomBorder: Boolean = true, content: @Composable RowScope.() -> Unit)

// TerminalTableCell — text cell with equal weight
@Composable fun RowScope.TerminalTableCell(text: String, modifier: Modifier = Modifier, secondary: Boolean = false)
```

<!-- TerminalButton.kt key API -->
```kotlin
@Composable fun TerminalButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, variant: ButtonVariant = ButtonVariant.Default, enabled: Boolean = true, icon: (@Composable () -> Unit)? = null)

// TerminalIconButton — square icon-only button with Secondary styling
@Composable fun TerminalIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable () -> Unit)
```

<!-- TerminalTooltip.kt key API -->
```kotlin
@Composable fun TerminalTooltip(text: String, modifier: Modifier = Modifier, content: @Composable () -> Unit)
```

<!-- Project uses BoxWithConstraints with 840.dp breakpoint for desktop/mobile. For tables, use a tighter breakpoint like 600.dp -->
<!-- Project uses Unicode characters for icons (terminal aesthetic), no Material icons library -->
<!-- TerminalIconButton only has Secondary styling — need to add variant support for Destructive -->
</interfaces>
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add visibleOnMobile to TerminalTableCell and variant to TerminalIconButton</name>
  <files>
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
  </files>
  <action>
**TerminalTable.kt — Add `visibleOnMobile` parameter to `TerminalTableCell`:**

1. Add a `visibleOnMobile: Boolean = true` parameter to `RowScope.TerminalTableCell`.
2. The cell already uses `Modifier.weight(1f)` — when `visibleOnMobile` is false, the cell should still render with weight on desktop. The CALLER is responsible for not rendering the cell on mobile (via `if (isMobile)` check). However, a simpler approach: pass an `isMobile` flag and when `visibleOnMobile = false && isMobile`, skip rendering the cell entirely (return early / don't compose).

**Better approach — keep it simple:** Don't modify TerminalTableCell. Instead, add a helper approach in AdminPanelScreen. The TerminalTable `headers` list already controls column count. The pattern is:
- On mobile, pass fewer headers to `TerminalTable` (omit hidden columns)
- On mobile, don't render the corresponding `TerminalTableCell` calls

This keeps the design system component generic and puts the responsive logic in the screen.

**TerminalButton.kt — Add `variant` parameter to `TerminalIconButton`:**

Add a `variant: ButtonVariant = ButtonVariant.Secondary` parameter to `TerminalIconButton`. Currently it hardcodes Secondary styling. Make it resolve colors based on variant, similar to `TerminalButton`:

```kotlin
@Composable
fun TerminalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Secondary,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
)
```

Color resolution for the icon button variants:
- `Secondary` (default): keep current behavior — `btnSecondaryBg`, `btnSecondaryBorder`, `btnSecondaryHoverBg`
- `Destructive`: `btnDestructiveBg`, no border, `btnDestructiveHoverBg`
- `Ghost`: `Color.Transparent`, no border, `btnGhostHoverBg`
- Other variants follow the same pattern as `TerminalButton`

Update disabled state to remain unchanged (already correct).

Update the existing `TerminalIconButton` callers — there are only 2 calls in the preview, both will work with the default `Secondary`.
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinWasmJs 2>&1 | tail -5</automated>
  </verify>
  <done>TerminalIconButton accepts a variant parameter with Destructive/Secondary/Ghost support. No changes to TerminalTableCell needed (responsive logic goes in AdminPanelScreen).</done>
</task>

<task type="auto">
  <name>Task 2: Make AdminPanelScreen tables mobile-responsive with icon buttons and tooltips</name>
  <files>
    app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
    app/admin/src/commonMain/composeResources/values/strings.xml
    app/admin/src/commonMain/composeResources/values-es/strings.xml
  </files>
  <action>
**AdminPanelScreen.kt — Add responsive table behavior:**

1. Import `BoxWithConstraints` and `TerminalTooltip` and `TerminalIconButton`.

2. In the `AdminPanelScreen` composable, wrap the existing `Column` with `BoxWithConstraints` (like DashboardScreen does). Derive `val isMobile = maxWidth < 600.dp` at the top of the `BoxWithConstraints` scope. Pass `isMobile` down to `InvitationsSection` as a parameter.

3. **Members table (lines ~260-305):** Make responsive:
   - Headers: On desktop keep all 5 (Name, Email, Role, Joined, Actions). On mobile, only show 3: Name, Role, Actions. Use `buildList` or `if (isMobile)` to build the headers list.
   - Row content: On mobile, skip `TerminalTableCell(text = member.email, ...)` and `TerminalTableCell(text = formatJoinedDate(...), ...)`.
   - Actions column: On mobile, replace the `TerminalButton(text = stringResource(Res.string.admin_remove_button), ...)` with:
     ```kotlin
     TerminalTooltip(text = stringResource(Res.string.admin_remove_button)) {
         TerminalIconButton(
             onClick = { onConfirmRemoveMember(member) },
             variant = ButtonVariant.Destructive,
         ) {
             TerminalText("\u2715", style = TerminalTheme.typography.sm, color = TerminalTheme.colors.btnDestructiveText)
         }
     }
     ```
   - On desktop, keep the existing `TerminalButton` text buttons unchanged.

4. **Invitations table (InvitationsSection, lines ~588-658):** Add `isMobile: Boolean` parameter.
   - Headers: On desktop keep all 4 (Email, Role, Status, Actions). On mobile, show 3: Email, Status, Actions (hide Role). Actually, re-evaluate: email is important for invitations. Hide Role column on mobile instead.
   - Row content: On mobile, skip the Role `TerminalTableCell`.
   - Actions column: On mobile, replace text buttons with icon buttons:
     - Revoke button becomes: `TerminalTooltip(text = stringResource(Res.string.admin_revoke_button)) { TerminalIconButton(variant = Destructive) { TerminalText("\u2715", ...) } }`
     - Resend button becomes: `TerminalTooltip(text = stringResource(Res.string.admin_resend_button)) { TerminalIconButton(variant = Secondary) { TerminalText("\u21BB", ...) } }`
       Unicode `\u21BB` is the clockwise arrow (↻) — fits the "resend" concept in a terminal aesthetic.
   - On desktop, keep existing text buttons.

5. **Also reduce the root padding on mobile:** Change `.padding(32.dp)` to `.padding(if (isMobile) 16.dp else 32.dp)` so the overall layout breathes better on mobile.

**String resources — no new strings needed.** The tooltip text reuses existing `admin_remove_button`, `admin_revoke_button`, and `admin_resend_button` strings which are already translated in both `values/strings.xml` and `values-es/strings.xml`.
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:admin:compileKotlinWasmJs 2>&1 | tail -5</automated>
  </verify>
  <done>
    - Members table hides Email and Joined columns on mobile (<600dp)
    - Invitations table hides Role column on mobile
    - Remove/Revoke buttons become destructive icon buttons (\u2715) with translated tooltips on mobile
    - Resend button becomes icon button (\u21BB) with translated tooltip on mobile
    - Desktop layout is completely unchanged
    - Root padding reduces from 32dp to 16dp on mobile
  </done>
</task>

<task type="checkpoint:human-verify" gate="blocking">
  <what-built>Responsive admin panel tables with hidden columns on mobile, icon buttons with tooltips replacing text CTAs, and reduced padding</what-built>
  <how-to-verify>
    1. Run `./gradlew wasmJsBrowserDevelopmentRun` and navigate to the Admin Panel
    2. **Desktop test:** Resize browser window wider than 600dp — verify all columns (Name, Email, Role, Joined, Actions) appear in Members table and all 4 columns appear in Invitations table. Verify text buttons ("Remove", "Revoke", "Resend") appear as before.
    3. **Mobile test:** Resize browser window narrower than 600dp:
       - Members table should show only Name, Role, Actions columns
       - Invitations table should show only Email, Status, Actions columns
       - "Remove" button should be a small icon button with the X symbol
       - "Revoke" button should be a small icon button with the X symbol
       - "Resend" button should be a small icon button with the ↻ symbol
       - Hover over any icon button to see the translated tooltip
    4. **Language test:** Switch to Spanish locale — tooltips should show "Eliminar", "Revocar", "Reenviar"
  </how-to-verify>
  <resume-signal>Type "approved" or describe issues</resume-signal>
</task>

</tasks>

<verification>
- `./gradlew :app:designsystem:compileKotlinWasmJs` compiles without errors
- `./gradlew :app:admin:compileKotlinWasmJs` compiles without errors
- Visual inspection confirms responsive behavior at 600dp breakpoint
</verification>

<success_criteria>
- Admin panel tables are usable on mobile with no horizontal cramping
- Hidden columns don't show on mobile, full table shows on desktop
- Icon buttons replace text CTAs on mobile with translated tooltips
- No regressions on desktop layout
</success_criteria>

<output>
After completion, create `.planning/quick/6-make-admin-panel-tables-mobile-responsiv/6-SUMMARY.md`
</output>
