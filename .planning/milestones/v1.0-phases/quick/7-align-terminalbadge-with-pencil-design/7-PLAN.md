---
phase: quick-7
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalBadge.kt
autonomous: true

must_haves:
  truths:
    - "Default badge has sm radius (4dp), surface bg, thin border, Medium font weight"
    - "Accent badge uses btnPrimaryBg/btnPrimaryText colors with no stroke"
    - "Success/Warning/Error badges display leading icon text before label"
    - "All non-Default badges use SemiBold font weight"
    - "Preview shows each variant with Pencil-accurate content"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalBadge.kt"
      provides: "TerminalBadge composable with icon support, correct tokens, Pencil-aligned preview"
      contains: "icon: String? = null"
  key_links:
    - from: "TerminalBadge"
      to: "TerminalTheme.colors.btnPrimaryBg"
      via: "Accent variant color mapping"
      pattern: "colors\\.btnPrimaryBg"
---

<objective>
Align TerminalBadge with Pencil design spec: fix shape from pill to sm radius, fix padding to 10dp/4dp, fix Accent colors to btnPrimary tokens, add icon parameter for Success/Warning/Error leading symbols, set correct font weights per variant, and update preview content to match Pencil.

Purpose: Badge component currently uses wrong shape, padding, colors, and lacks icon support -- all diverging from the Pencil design system source of truth.
Output: Updated TerminalBadge.kt matching Pencil spec across all 5 variants.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalBadge.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTypography.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalRadius.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalGap.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalSpacing.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Restructure TerminalBadge composable and fix all tokens</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalBadge.kt</files>
  <action>
Rewrite the TerminalBadge composable in TerminalBadge.kt with these specific changes:

**1. Add icon parameter:**
Add `icon: String? = null` parameter after `modifier` and before `variant`.

**2. Fix shape (line 52):**
Change `RoundedCornerShape(radius.pill)` to `RoundedCornerShape(radius.sm)` (4dp matches Pencil cornerRadius: 4).

**3. Fix Accent colors (line 56):**
Change `Triple(colors.accentMuted, colors.accent, colors.accentMuted)` to `Triple(colors.btnPrimaryBg, colors.btnPrimaryText, colors.btnPrimaryBg)`.

**4. Fix padding (line 72):**
Change `.padding(horizontal = spacing.sm, vertical = spacing.xs)` to `.padding(horizontal = 10.dp, vertical = 4.dp)`. Use literal dp values -- Pencil specifies [4, 10] which doesn't map to any existing spacing token (10dp falls between spacing.sm=8 and spacing.md=12). This follows the established pattern from quick-02 and quick-03 which use literal dp when Pencil values don't match tokens.

**5. Derive font weight per variant:**
Create a local val `fontWeight` in the when block:
- `BadgeVariant.Default` -> `FontWeight.Medium`
- All others (`Accent`, `Success`, `Warning`, `Error`) -> `FontWeight.SemiBold`

Note: No Medium font file exists but FontWeight.Medium works fine -- TerminalButton already uses it (line 168 of TerminalButton.kt) and the font engine interpolates from Regular. SemiBold has a dedicated font file.

**6. Restructure body from single BasicText to Row with optional icon + label:**
Replace the single `BasicText(text = text, modifier = badgeModifier, style = typography.xs.copy(color = fgColor))` with:

```kotlin
Row(
    modifier = badgeModifier,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    if (icon != null) {
        BasicText(
            text = icon,
            style = typography.xs.copy(
                color = fgColor,
                fontWeight = fontWeight,
                fontSize = 10.sp,
            ),
        )
    }
    BasicText(
        text = text,
        style = typography.xs.copy(
            color = fgColor,
            fontWeight = fontWeight,
            fontSize = 10.sp,
        ),
    )
}
```

Use literal `6.dp` for icon-to-label gap (Pencil gap: 6 falls between gap.xs=4 and gap.sm=8, same literal dp pattern as quick-02). Use literal `10.sp` for fontSize (Pencil specifies fontSize 10, but typography.xs is 11.sp).

**7. Add required imports:**
- `import androidx.compose.ui.Alignment` (for CenterVertically)
- `import androidx.compose.ui.text.font.FontWeight` (for Medium/SemiBold)
- `import androidx.compose.ui.unit.sp` (for 10.sp literal)
- Keep existing imports for `Arrangement`, `Row` (already imported)

**8. Update KDoc:**
- Update the class doc to mention "rounded (not pill)" shape
- Add `@param icon` doc: "Optional leading icon text displayed before the label (e.g., checkmark, warning symbol)."

**9. Update preview function:**
Replace the `BadgeVariant.entries.forEach` loop with individual badge calls matching Pencil content:

```kotlin
@TerminalPreview
@Composable
private fun TerminalBadgePreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TerminalBadge(text = "v1.0.0", variant = BadgeVariant.Default)
                TerminalBadge(text = "RUNNING", variant = BadgeVariant.Accent)
                TerminalBadge(text = "PASSED", icon = "\u2713", variant = BadgeVariant.Success)
                TerminalBadge(text = "PENDING", icon = "\u25D0", variant = BadgeVariant.Warning)
                TerminalBadge(text = "FAILED", icon = "\u2715", variant = BadgeVariant.Error)
            }
        }
    }
}
```

Use unicode escapes for the icon characters: \u2713 (checkmark), \u25D0 (circle with left half black), \u2715 (multiplication x).
  </action>
  <verify>
Run the Kotlin compiler check for the designsystem module:
```bash
./gradlew :app:designsystem:compileKotlinDesktop --quiet
```
Verify no compilation errors.
  </verify>
  <done>
TerminalBadge uses radius.sm (not pill), 10dp/4dp padding, btnPrimaryBg/btnPrimaryText for Accent, FontWeight.Medium for Default / SemiBold for others, fontSize 10.sp, Row layout with optional leading icon text, and preview shows Pencil-accurate content for all 5 variants.
  </done>
</task>

</tasks>

<verification>
- Shape uses `radius.sm` (4dp rounded corners, not pill)
- Padding is `10.dp` horizontal, `4.dp` vertical
- Accent variant uses `btnPrimaryBg` / `btnPrimaryText` (not `accentMuted` / `accent`)
- Default variant: FontWeight.Medium; Accent/Success/Warning/Error: FontWeight.SemiBold
- fontSize is 10.sp (not 11.sp from typography.xs)
- Success badge renders checkmark icon before "PASSED"
- Warning badge renders half-circle icon before "PENDING"
- Error badge renders x icon before "FAILED"
- Row layout with 6.dp gap between icon and label
- Module compiles without errors
</verification>

<success_criteria>
- TerminalBadge.kt compiles successfully
- All 5 Pencil badge variants accurately represented in code
- Icon parameter enables leading text symbols for status variants
- Preview function shows Pencil-accurate content per variant
</success_criteria>

<output>
After completion, create `.planning/quick/7-align-terminalbadge-with-pencil-design/7-SUMMARY.md`
</output>
