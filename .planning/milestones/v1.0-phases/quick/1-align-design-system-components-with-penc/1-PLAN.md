---
phase: quick-01
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
autonomous: true
must_haves:
  truths:
    - "Default card renders header (title + description + icon) / content slot / footer with two buttons"
    - "Accent card renders dark-background header / content slot (NO left edge, NO footer)"
    - "Info card renders colored header on infoBg / content slot (NO footer)"
    - "Highlighted card renders accent-colored header with SemiBold title / content slot (NO footer)"
    - "Compact card renders as a single horizontal row with leading icon, info column, trailing icon"
    - "All spacing, colors, typography, and radii match Pencil design tokens exactly"
    - "Preview shows all 5 variants with realistic content"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt"
      provides: "TerminalCard (4 variants) + TerminalCompactCard + CardVariant enum + Preview"
      contains: "fun TerminalCard"
  key_links:
    - from: "TerminalCard.kt"
      to: "TerminalTheme"
      via: "theme token access"
      pattern: "TerminalTheme\\.(colors|spacing|gap|radius|borders|typography)"
---

<objective>
Redesign TerminalCard to match the Pencil design system exactly. The current implementation is a flat Column with variant-based colors -- it lacks header/content/footer structure, has an incorrect Accent variant (draws left edge instead of dark header), and a broken Compact variant (just smaller padding instead of horizontal layout).

Purpose: Align the card component with the Pencil design file so the design system is a faithful 1:1 implementation.
Output: Single self-contained TerminalCard.kt with all 5 variants and comprehensive @Preview.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTheme.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTypography.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalSpacing.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalGap.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalRadius.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalBorders.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalOpacity.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/TerminalText.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Rewrite TerminalCard with proper header/content/footer structure for Default, Accent, Info, Highlighted variants</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt</files>
  <action>
Completely rewrite TerminalCard.kt. Keep the same package and file location. Remove all existing code and replace with the following structure.

**Keep the enum** `CardVariant { Default, Accent, Info, Highlighted, Compact }`.

**New TerminalCard API** (for Default, Accent, Info, Highlighted -- NOT Compact):
```kotlin
@Composable
fun TerminalCard(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    variant: CardVariant = CardVariant.Default,
    icon: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
)
```

**Internal structure -- build as private composables:**

1. `CardHeader` -- a Row with `Arrangement.SpaceBetween`, `Alignment.CenterVertically`:
   - Left side: Column with `gap.xs` (4dp) spacing between title and optional description
   - Right side: icon lambda (if provided)
   - Padding: vertical=`spacing.lg` (16dp), horizontal=`spacing.xl` (20dp)

2. `CardContent` -- a Box with padding=`spacing.xl` (20dp) on all sides, `fillMaxWidth()`

3. `CardFooter` (only for Default variant, rendered only when `footer` param is non-null):
   - Row with `Arrangement.End`, gap=`gap.md` (12dp)
   - Padding: vertical=`spacing.md` (12dp), horizontal=`spacing.xl` (20dp)
   - Top border: 1dp line in `colors.border` -- use `drawBehind` to draw a line at y=0

**Per-variant styling -- derive all colors from TerminalTheme tokens:**

**Default:**
- Container: `colors.surface` bg, `borders.thin` + `colors.border` border, `radius.sm` (4dp) corners, clip=true
- Header: no background fill (inherits container surface), bottom border 1dp `colors.border` (use `drawBehind` line at bottom)
- Title: `colors.text`, `typography.md.copy(fontWeight = FontWeight.Medium)` (14sp, weight 500)
- Description: `colors.textMuted`, `typography.xs` (11sp)
- Icon tint: `colors.textMuted`
- Footer: rendered if `footer` param non-null; top border 1dp `colors.border`

**Accent:**
- Container: `colors.bg` background (maps to #E8E8E8 light / #101012 dark), NO explicit border, `radius.sm` corners, clip=true
- Header: `colors.textMuted` background fill (maps to #5A5A5A -- closest to Pencil #525252 dark header)
- Title: `colors.surface` color (light text on dark bg; maps to #F5F5F5 light -- closest to Pencil #E8EBE4)
- Description: `colors.surface.copy(alpha = 0.7f)` (Pencil specifies opacity=0.7 on #E8EBE4)
- Icon tint: `colors.surface` (light on dark)
- Footer: NONE -- Accent has no footer. Ignore footer param.
- CRITICAL: Do NOT draw a left accent edge. The old code was wrong.

**Info:**
- Container: `colors.surface` bg, `borders.thin` + `colors.info` border, `radius.sm` corners
- Header: `colors.infoBg` background fill
- Title: `colors.info`, `typography.md.copy(fontWeight = FontWeight.Medium)`
- Description: `colors.textMuted`, `typography.xs`
- Icon tint: `colors.info`
- Footer: NONE

**Highlighted:**
- Container: `colors.accentMuted` bg, `borders.default` (2dp) + `colors.accent` border, `radius.sm` corners
- Header: no background fill (inherits container accentMuted bg), NO bottom border
- Title: `colors.accent`, `typography.md.copy(fontWeight = FontWeight.SemiBold)` (weight 600, NOT 500)
- Description: `colors.textMuted`, `typography.xs`
- Icon tint: `colors.accent`
- Footer: NONE

**Implementation approach for the main TerminalCard composable:**
```kotlin
Column(
    modifier = modifier
        .clip(shape)
        .then(borderModifier)
        .background(containerBg)
) {
    // Header section
    CardHeader(...)

    // Content slot
    CardContent(content = content)

    // Footer (Default only, when non-null)
    if (variant == CardVariant.Default && footer != null) {
        CardFooter(footer = footer)
    }
}
```

For header/footer borders, use `Modifier.drawBehind`:
- Header bottom border (Default only): `drawLine(color=borderColor, start=Offset(0f, size.height), end=Offset(size.width, size.height), strokeWidth=1.dp.toPx())`
- Footer top border: `drawLine(color=borderColor, start=Offset.Zero, end=Offset(size.width, 0f), strokeWidth=1.dp.toPx())`

**Required imports:** Add `FontWeight`, `Arrangement`, `Row`, `Box`, `fillMaxWidth`, `Offset` (already present), `BasicText` or use `TerminalText`. Use `TerminalText` for consistency with codebase conventions.

Do NOT use Material3 Divider or HorizontalDivider. Use drawBehind for section borders.
  </action>
  <verify>
File compiles: run `./gradlew :app:designsystem:compileKotlinDesktop 2>&1 | tail -20` (or equivalent desktop target). Verify no compilation errors.
Grep the file for: `drawBehind` (should appear for header/footer borders), NO `drawRect.*accentColor` (old left-edge code removed), `fontWeight = FontWeight.Medium` and `fontWeight = FontWeight.SemiBold`.
  </verify>
  <done>
TerminalCard composable accepts title, description, icon, content slot, optional footer. Default variant renders header with bottom border + content + optional footer with top border. Accent variant renders dark-bg header + content (no left edge, no footer). Info variant renders infoBg header + content. Highlighted variant renders accent-colored header with SemiBold title + content. All colors/spacing/typography from TerminalTheme tokens. No Material3 dependencies.
  </done>
</task>

<task type="auto">
  <name>Task 2: Add TerminalCompactCard composable and comprehensive 5-variant @Preview</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt</files>
  <action>
In the same TerminalCard.kt file, add a separate `TerminalCompactCard` composable and rewrite the @Preview.

**TerminalCompactCard API** (separate from TerminalCard because it has a fundamentally different layout):
```kotlin
@Composable
fun TerminalCompactCard(
    title: String,
    modifier: Modifier = Modifier,
    details: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
)
```

**Compact layout** -- a single horizontal Row (NOT vertical Column):
- Container: `colors.surface` bg, `borders.thin` + `colors.border` border, `radius.sm` corners, clip=true
- If `onClick` is non-null, add `.clickable(onClick = onClick)` to the container
- Row with `gap.lg` (16dp) gap, padding=`spacing.md` (12dp), `Alignment.CenterVertically`
  - Leading icon slot (if provided): renders the lambda
  - Info column (Column, `gap.xs` (4dp) gap, `Modifier.weight(1f)` to fill):
    - Title: `colors.text`, `typography.base.copy(fontWeight = FontWeight.Medium)` (13sp, weight 500)
    - Details (if non-null): `colors.textDim`, `typography.xs` (11sp)
  - Trailing icon slot (if provided): renders the lambda

**Rewrite the @Preview** to show all 5 variants with realistic content:

```kotlin
@Preview
@Composable
private fun TerminalCardPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(TerminalTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(TerminalTheme.gap.lg),
        ) {
            // 1. Default Card -- with footer
            TerminalCard(
                title = "Terminal Session",
                description = "Active connection",
                variant = CardVariant.Default,
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    TerminalText(
                        text = ">_",
                        style = TerminalTheme.typography.sm,
                        color = TerminalTheme.colors.textMuted,
                    )
                },
                footer = {
                    TerminalButton(
                        text = "Cancel",
                        onClick = {},
                        variant = ButtonVariant.Ghost,
                    )
                    TerminalButton(
                        text = "Connect",
                        onClick = {},
                        variant = ButtonVariant.Default,
                    )
                },
            ) {
                TerminalText("Session content goes here")
            }

            // 2. Accent Card -- dark header, no footer
            TerminalCard(
                title = "Network Status",
                description = "All systems operational",
                variant = CardVariant.Accent,
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    TerminalText(
                        text = "~",
                        style = TerminalTheme.typography.sm,
                        color = TerminalTheme.colors.surface,
                    )
                },
            ) {
                TerminalText("Monitoring dashboard content")
            }

            // 3. Info Card
            TerminalCard(
                title = "System Notice",
                description = "Scheduled maintenance",
                variant = CardVariant.Info,
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    TerminalText(
                        text = "i",
                        style = TerminalTheme.typography.sm,
                        color = TerminalTheme.colors.info,
                    )
                },
            ) {
                TerminalText("The server will be restarted at 02:00 UTC.")
            }

            // 4. Highlighted Card
            TerminalCard(
                title = "Featured Project",
                description = "Pinned repository",
                variant = CardVariant.Highlighted,
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    TerminalText(
                        text = "*",
                        style = TerminalTheme.typography.sm,
                        color = TerminalTheme.colors.accent,
                    )
                },
            ) {
                TerminalText("Project details and stats")
            }

            // 5. Compact Card
            TerminalCompactCard(
                title = "config.yaml",
                details = "Modified 2 hours ago",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    TerminalText(
                        text = "#",
                        style = TerminalTheme.typography.xs,
                        color = TerminalTheme.colors.textMuted,
                    )
                },
                trailingIcon = {
                    TerminalText(
                        text = ">",
                        style = TerminalTheme.typography.xs,
                        color = TerminalTheme.colors.textMuted,
                    )
                },
            )
        }
    }
}
```

Note: The preview uses TerminalText with characters (">_", "~", "i", "*", "#", ">") as icon stand-ins because Lucide icons are not available in the design system module. This is consistent with how TerminalIconButtonPreview uses TerminalText("X"). Real consumers will pass actual icon composables.

**Required imports to add:** `ButtonVariant`, `TerminalButton` from the button package, `clickable` from foundation.
  </action>
  <verify>
File compiles: `./gradlew :app:designsystem:compileKotlinDesktop 2>&1 | tail -20`.
Grep the file for: `fun TerminalCompactCard` (exists), `fun TerminalCardPreview` (exists), `CardVariant.Default` + `CardVariant.Accent` + `CardVariant.Info` + `CardVariant.Highlighted` in preview (all 4 used), `TerminalCompactCard(` in preview (compact shown).
Verify no `CardVariant.Compact` is passed to `TerminalCard` (compact has its own composable).
  </verify>
  <done>
TerminalCompactCard composable renders as a horizontal Row with leading icon, title+details column, trailing icon. Preview shows all 5 card variants (Default with footer, Accent with dark header, Info with colored header, Highlighted with accent border, Compact as horizontal row) with realistic content matching the Pencil design intent. File compiles with no errors.
  </done>
</task>

</tasks>

<verification>
1. `./gradlew :app:designsystem:compileKotlinDesktop` -- no compilation errors
2. File contains exactly 2 public composables: `TerminalCard` and `TerminalCompactCard`
3. `TerminalCard` signature has: title, description, variant, icon, footer, content parameters
4. `TerminalCompactCard` signature has: title, details, leadingIcon, trailingIcon, onClick parameters
5. No `drawRect.*accent` (old left-edge code removed)
6. No Material3 imports
7. All 5 variants visible in preview
8. Default card has header bottom border + optional footer with top border
9. Accent card has `colors.textMuted` header background (dark header)
10. Highlighted card uses `FontWeight.SemiBold` for title
11. Compact card uses Row layout (not Column)
</verification>

<success_criteria>
- TerminalCard.kt is a complete, self-contained file with all 5 card variants
- Default: header (title+desc+icon, bottom border) / content / footer (top border, end-aligned buttons)
- Accent: dark header (textMuted bg, surface text) / content -- NO left edge, NO footer
- Info: infoBg header / content -- NO footer
- Highlighted: accentMuted bg, accent border (2dp), accent SemiBold title / content -- NO footer
- Compact: single horizontal Row with leading icon, title+details column, trailing icon
- All styling from TerminalTheme tokens (colors, spacing, gap, radius, borders, typography)
- No Material3 dependencies; Foundation primitives only
- Preview renders all 5 variants with realistic content
- File compiles successfully
</success_criteria>

<output>
After completion, create `.planning/quick/1-align-design-system-components-with-penc/1-SUMMARY.md`
</output>
