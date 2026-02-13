---
phase: quick-19
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalLineChart.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalBarChart.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalRadarChart.kt
autonomous: true

must_haves:
  truths:
    - "Line chart animates line drawing from left to right on first render"
    - "Bar chart animates bars growing from bottom to full height on first render"
    - "Radar chart animates polygon expanding from center to data values on first render"
    - "All animations can be disabled via animated parameter"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalLineChart.kt"
      provides: "Line chart with draw-on animation"
      contains: "Animatable<Float"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalBarChart.kt"
      provides: "Bar chart with grow-up animation"
      contains: "Animatable<Float"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalRadarChart.kt"
      provides: "Radar chart with expand-out animation"
      contains: "Animatable<Float"
  key_links:
    - from: "TerminalLineChart composable"
      to: "Animatable progress state"
      via: "LaunchedEffect triggers animateTo(1f)"
      pattern: "LaunchedEffect.*animateTo"
    - from: "Canvas drawing code"
      to: "progress.value multiplier"
      via: "Rendering multiplied by animation progress"
      pattern: "progress\\.value \\*|clipRect.*progress"
---

<objective>
Add smooth entry animations to all three chart components (line, bar, radar) using Compose Animatable API.

Purpose: Charts reveal their data progressively on first render, creating visual polish and drawing user attention to data patterns.
Output: Three updated chart files with `animated: Boolean = true` parameter and 800ms entry animations.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/PROJECT.md
@.planning/ROADMAP.md
@.planning/STATE.md

Chart files to modify:
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalLineChart.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalBarChart.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalRadarChart.kt

Established animation pattern from quick-15 (swipe gestures):
- Use `Animatable<Float, AnimationVector1D>` from compose.animation.core
- Trigger with `LaunchedEffect(Unit) { animatable.animateTo(targetValue, animationSpec) }`
- Apply progress via multiplier or clipping
</context>

<tasks>

<task type="auto">
  <name>Add left-to-right draw animation to TerminalLineChart</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalLineChart.kt</files>
  <action>
Add `animated: Boolean = true` parameter to TerminalLineChart composable.

Add imports at top of file (alphabetically sorted with existing imports):
- androidx.compose.animation.core.Animatable
- androidx.compose.animation.core.AnimationVector1D
- androidx.compose.animation.core.EaseOutCubic
- androidx.compose.animation.core.tween
- androidx.compose.runtime.LaunchedEffect
- androidx.compose.runtime.remember

Inside TerminalLineChart composable body (before Canvas), add animation state:
```kotlin
val progress = remember { Animatable(if (animated) 0f else 1f) }
LaunchedEffect(Unit) {
    if (animated) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = EaseOutCubic)
        )
    }
}
```

In the Canvas drawScope (around line 220-325 where series are rendered), wrap the entire series rendering loop content with:
```kotlin
clipRect(right = size.width * progress.value) {
    // existing grid line drawing
    // existing series rendering (area fill, line stroke, data points)
}
```

This clips the rendered content from left to right, progressively revealing the line chart as progress animates from 0 to 1.

Ensure the clipRect wraps ALL Canvas drawing operations (grid lines, area fills, line paths, data point dots) so the entire chart reveals together.
  </action>
  <verify>
1. File compiles without errors
2. Grep for "animated: Boolean = true" in function signature
3. Grep for "Animatable" and "LaunchedEffect" in file
4. Grep for "clipRect.*progress.value" in Canvas block
  </verify>
  <done>
TerminalLineChart has animated parameter defaulting to true, uses Animatable for progress state, animates from 0 to 1 over 800ms with EaseOutCubic, and clips Canvas content to progressively reveal chart from left to right.
  </done>
</task>

<task type="auto">
  <name>Add bottom-to-top grow animation to TerminalBarChart</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalBarChart.kt</files>
  <action>
Add `animated: Boolean = true` parameter to TerminalBarChart composable.

Add imports at top of file (alphabetically sorted with existing imports):
- androidx.compose.animation.core.Animatable
- androidx.compose.animation.core.AnimationVector1D
- androidx.compose.animation.core.EaseOutCubic
- androidx.compose.animation.core.tween
- androidx.compose.runtime.LaunchedEffect
- androidx.compose.runtime.remember

Inside TerminalBarChart composable body (before Canvas), add animation state:
```kotlin
val progress = remember { Animatable(if (animated) 0f else 1f) }
LaunchedEffect(Unit) {
    if (animated) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = EaseOutCubic)
        )
    }
}
```

In the Canvas drawScope bar rendering section (around line 196-274), locate the barHeight calculation for each bar. Multiply barHeight by progress.value:
```kotlin
val barHeight = ((value / maxValue) * availableHeight) * progress.value
```

Then barTop becomes:
```kotlin
val barTop = canvasHeight - barHeight
```

This keeps barBottom at the baseline (canvasHeight) while barTop adjusts as barHeight grows from 0 to full height.

All bars grow simultaneously from the bottom upward.
  </action>
  <verify>
1. File compiles without errors
2. Grep for "animated: Boolean = true" in function signature
3. Grep for "Animatable" and "LaunchedEffect" in file
4. Grep for "barHeight.*progress.value" to confirm multiplication
  </verify>
  <done>
TerminalBarChart has animated parameter defaulting to true, uses Animatable for progress state, animates from 0 to 1 over 800ms with EaseOutCubic, and multiplies barHeight by progress so bars grow upward from the baseline.
  </done>
</task>

<task type="auto">
  <name>Add center-to-outward expand animation to TerminalRadarChart</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalRadarChart.kt</files>
  <action>
Add `animated: Boolean = true` parameter to TerminalRadarChart composable.

Add imports at top of file (alphabetically sorted with existing imports):
- androidx.compose.animation.core.Animatable
- androidx.compose.animation.core.AnimationVector1D
- androidx.compose.animation.core.EaseOutCubic
- androidx.compose.animation.core.tween
- androidx.compose.runtime.LaunchedEffect
- androidx.compose.runtime.remember

Inside TerminalRadarChart composable body (before Canvas), add animation state:
```kotlin
val progress = remember { Animatable(if (animated) 0f else 1f) }
LaunchedEffect(Unit) {
    if (animated) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = EaseOutCubic)
        )
    }
}
```

In the Canvas drawScope series rendering section (around line 177-294), locate where clampedValue is computed from each data point value. Multiply clampedValue by progress.value:
```kotlin
val clampedValue = value.coerceIn(0f, 1f) * progress.value
```

This scales the polygon points from the center (0 radius) outward to their full data values as progress animates from 0 to 1.

Data point dots will naturally appear/scale with the polygon expansion since they are positioned using the same clampedValue calculation.
  </action>
  <verify>
1. File compiles without errors
2. Grep for "animated: Boolean = true" in function signature
3. Grep for "Animatable" and "LaunchedEffect" in file
4. Grep for "clampedValue.*progress.value" to confirm multiplication
  </verify>
  <done>
TerminalRadarChart has animated parameter defaulting to true, uses Animatable for progress state, animates from 0 to 1 over 800ms with EaseOutCubic, and multiplies clampedValue by progress so polygons expand from center outward to full data values.
  </done>
</task>

</tasks>

<verification>
All three chart files compile and contain:
1. `animated: Boolean = true` parameter in composable signature
2. `Animatable<Float, AnimationVector1D>` import and usage
3. `LaunchedEffect(Unit)` triggering animateTo(1f, tween(800, EaseOutCubic))
4. Rendering multiplied by progress.value (or clipped for line chart)

Visual verification in preview:
- Line chart: content reveals left to right over 800ms
- Bar chart: bars grow from bottom to top over 800ms
- Radar chart: polygons expand from center to edges over 800ms
- Setting `animated = false` renders charts immediately with no animation
</verification>

<success_criteria>
- TerminalLineChart clips Canvas content to progressively reveal chart from left to right
- TerminalBarChart multiplies barHeight by progress for bottom-to-top bar growth
- TerminalRadarChart multiplies clampedValue by progress for center-to-outward polygon expansion
- All three charts use identical animation pattern: Animatable(0f/1f based on animated flag), LaunchedEffect animateTo with 800ms EaseOutCubic tween
- All three charts accept `animated: Boolean = true` parameter for disabling animation in previews/tests
- Animations trigger on first composition via LaunchedEffect(Unit)
</success_criteria>

<output>
After completion, create `.planning/quick/19-add-entry-animations-to-chart-components/19-SUMMARY.md`
</output>
