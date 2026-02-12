---
phase: quick-8
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalProgress.kt
autonomous: true

must_haves:
  truths:
    - "Determinate progress bar shows label text on the left and percentage on the right above the track"
    - "Indeterminate progress bar shows label text above the track (no percentage)"
    - "Track height is 8dp with cornerRadius 2dp"
    - "Track uses accentMuted color, fill uses accent color"
    - "Indeterminate indicator uses a linear gradient from accent to accent at 50% alpha"
    - "Label and percentage use typography.xs with textMuted color"
    - "Progress bar works without label (label=null shows track only, backward compatible)"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalProgress.kt"
      provides: "TerminalProgress composable with label support, Pencil-aligned styling"
      contains: "label: String?"
  key_links:
    - from: "TerminalProgress"
      to: "TerminalTheme.colors"
      via: "accentMuted for track, accent for fill, textMuted for labels"
      pattern: "colors\\.accentMuted|colors\\.accent|colors\\.textMuted"
---

<objective>
Align TerminalProgress with Pencil design specifications.

Purpose: The current TerminalProgress lacks label text, uses wrong track height/radius/color tokens, and the indeterminate indicator uses a solid color instead of a gradient. This plan brings it into full alignment with the Pencil determinate (OvoQ4) and indeterminate (YP7h8) designs.

Output: Updated TerminalProgress.kt with label parameter, correct track dimensions/colors, gradient indeterminate indicator, and Pencil-aligned previews.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalProgress.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTypography.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalPreview.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add label parameter, fix track styling, add gradient indicator</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalProgress.kt</files>
  <action>
Modify TerminalProgress.kt with the following changes:

**1. Fix duplicate import (line 22 and 28 both import dp):** Remove the duplicate `import androidx.compose.ui.unit.dp` on line 28.

**2. Add required imports:** Add imports for:
- `androidx.compose.foundation.layout.Row`
- `androidx.compose.foundation.layout.Spacer`
- `androidx.compose.foundation.layout.width`
- `androidx.compose.foundation.text.BasicText`
- `androidx.compose.ui.graphics.Brush`
- `androidx.compose.ui.text.font.FontWeight`

**3. Add `label` parameter to TerminalProgress:**
```kotlin
fun TerminalProgress(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    label: String? = null,
)
```

**4. Update KDoc:** Add `@param label Optional label text displayed above the track. For determinate mode, a percentage is shown to the right of the label.`

**5. Inside the composable, update local values:**
- `trackHeight` from `6.dp` to `8.dp`
- `shape` from `RoundedCornerShape(radius.pill)` to `RoundedCornerShape(2.dp)` (Pencil cornerRadius=2, no matching token)
- `trackColor` from `colors.inset` to `colors.accentMuted`
- Keep `fillColor = colors.accent` unchanged

**6. Wrap the entire body in a Column:**
- The root composable layout becomes a `Column(modifier = modifier)` with no vertical arrangement (gap only between label and track).
- If `label != null`, render the label row FIRST, then an `8.dp` spacer (matching Pencil gap=8).
- Then render the track Box (remove `modifier` from the Box since Column owns it; Box gets `Modifier.fillMaxWidth().height(trackHeight).clip(shape).background(trackColor)`).

**7. Label row logic:**
- **Determinate (progress != null):** Render a `Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween)` containing:
  - Left: `BasicText(text = label, style = typography.xs.copy(color = colors.textMuted, fontWeight = FontWeight.Normal))`
  - Right: `BasicText(text = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%", style = typography.xs.copy(color = colors.textMuted, fontWeight = FontWeight.SemiBold))`
- **Indeterminate (progress == null):** Render just `BasicText(text = label, style = typography.xs.copy(color = colors.textMuted, fontWeight = FontWeight.Normal))`

**8. Also read `val typography = TerminalTheme.typography`** alongside the existing colors/radius reads.

**9. Indeterminate gradient:** In the Canvas drawRoundRect call for indeterminate mode, change `color = fillColor` to `brush = Brush.linearGradient(colors = listOf(fillColor, fillColor.copy(alpha = 0.5f)), start = Offset(startX, 0f), end = Offset(startX + indicatorWidthPx, 0f))`. This gives the accent-to-faded gradient per Pencil spec while remaining theme-adaptive.

**10. Also update the corner radius in the Canvas drawRoundRect:** Change `val cornerRadiusPx = size.height / 2f` to `val cornerRadiusPx = 2f * density` (using `with(drawScope)` -- actually since we're already in a Canvas DrawScope, just use `2.dp.toPx()` which is available in DrawScope).  Wait -- simpler: since shape is already `RoundedCornerShape(2.dp)` and the Box is clipped, the Canvas fill is clipped by the parent. The cornerRadius in `drawRoundRect` on the Canvas should also use `2.dp.toPx()` for the indicator itself. Replace `val cornerRadiusPx = size.height / 2f` with `val cornerRadiusPx = 2.dp.toPx()`.

**11. Update preview function:**
```kotlin
@TerminalPreview
@Composable
private fun TerminalProgressPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Determinate with label (Pencil OvoQ4)
            TerminalProgress(
                progress = 0.67f,
                label = "downloading...",
            )
            // Indeterminate with label (Pencil YP7h8)
            TerminalProgress(
                progress = null,
                label = "compiling assets...",
            )
            // Without label (API completeness)
            TerminalProgress(progress = 0.4f)
        }
    }
}
```

**Remove** the `radius` read (`val radius = TerminalTheme.radius`) since we no longer use `radius.pill`.
  </action>
  <verify>
Run: `cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinDesktop 2>&1 | tail -20`
Must compile without errors. Verify file has no duplicate imports.
  </verify>
  <done>
TerminalProgress shows label+percentage row for determinate, label-only for indeterminate, no label when null. Track is 8dp tall with 2dp radius, accentMuted background, accent fill. Indeterminate uses gradient indicator. Preview shows all three variants matching Pencil specs.
  </done>
</task>

</tasks>

<verification>
- `./gradlew :app:designsystem:compileKotlinDesktop` compiles clean
- TerminalProgress accepts `label: String? = null` parameter
- Determinate with label shows "downloading..." left and "67%" right above track
- Indeterminate with label shows "compiling assets..." above track
- Track height is 8dp, cornerRadius is 2dp, track color is accentMuted
- Indeterminate indicator uses linearGradient brush
- No duplicate imports
</verification>

<success_criteria>
TerminalProgress fully matches Pencil determinate (OvoQ4) and indeterminate (YP7h8) designs with label text, correct track dimensions, proper color tokens, and gradient indicator. Clean compilation. Preview renders all three variants.
</success_criteria>

<output>
After completion, create `.planning/quick/8-align-terminalprogress-with-pencil-desig/8-SUMMARY.md`
</output>
