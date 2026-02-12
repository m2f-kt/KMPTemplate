---
phase: quick-17
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalLineChart.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalBarChart.kt
autonomous: true
must_haves:
  truths:
    - "TerminalLineChart renders a titled area chart with gradient fill, line strokes, data point circles, Y-axis labels, X-axis labels, horizontal grid lines, and a legend"
    - "TerminalBarChart renders a titled histogram with colored bars per tier, Y-axis labels, X-axis labels with highlighting, horizontal grid lines, and a total readout"
    - "All chart colors come from theme tokens (TerminalTheme.colors) and switch correctly between light/dark mode"
    - "Preview functions show both chart types in light/dark/desktop modes"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt"
      provides: "Chart color tokens (chartBg, chartGrid, chartAxis, chartAxisText, chartSeries1/2, chartBar1/2/3, chartBarHighlight, chartSeries1Muted)"
      contains: "chartBg"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalLineChart.kt"
      provides: "TerminalLineChart composable with ChartDataPoint, ChartSeries data models"
      exports: ["TerminalLineChart", "ChartDataPoint", "ChartSeries"]
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalBarChart.kt"
      provides: "TerminalBarChart composable with BarData model"
      exports: ["TerminalBarChart", "BarData"]
  key_links:
    - from: "TerminalLineChart.kt"
      to: "TerminalColors.kt"
      via: "TerminalTheme.colors.chartSeries1, chartGrid, chartBg, etc."
      pattern: "colors\\.chart"
    - from: "TerminalBarChart.kt"
      to: "TerminalColors.kt"
      via: "TerminalTheme.colors.chartBar1, chartBarHighlight, etc."
      pattern: "colors\\.chart"
---

<objective>
Add TerminalLineChart and TerminalBarChart composables to the design system, backed by new chart color tokens in TerminalColors.

Purpose: Complete the Charts section of the Pencil design system in code, giving the app reusable, theme-aware chart components.
Output: Three modified/created files -- updated TerminalColors.kt + two new chart component files with previews.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTypography.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTheme.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalBorders.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalRadius.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalPreview.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalProgress.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add chart color tokens to TerminalColors</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt</files>
  <action>
Add 11 new chart color properties to the TerminalColors data class, after the existing table tokens comment block. Add a `// Chart tokens` comment before them.

New properties in the data class:
```
val chartBg: Color,
val chartGrid: Color,
val chartAxis: Color,
val chartAxisText: Color,
val chartSeries1: Color,
val chartSeries1Muted: Color,
val chartSeries2: Color,
val chartBar1: Color,
val chartBar2: Color,
val chartBar3: Color,
val chartBarHighlight: Color,
```

Add corresponding `Color.Unspecified` entries in the `LocalTerminalColors` default.

Add light values in `TerminalLightColors`:
```
chartBg = Color(0xFFFFFFFF),
chartGrid = Color(0xFFE5E5E5),
chartAxis = Color(0xFFD4D4D4),
chartAxisText = Color(0xFF737373),
chartSeries1 = Color(0xFF4A9B6E),
chartSeries1Muted = Color(0x304A9B6E),     // #4A9B6E30 -> alpha 0x30
chartSeries2 = Color(0xFF4A7EB0),
chartBar1 = Color(0xFF525252),
chartBar2 = Color(0xFF737373),
chartBar3 = Color(0xFFA3A3A3),
chartBarHighlight = Color(0xFF4A9B6E),
```

Add dark values in `TerminalDarkColors`:
```
chartBg = Color(0xFF171717),
chartGrid = Color(0xFF2A2A2A),
chartAxis = Color(0xFF333333),
chartAxisText = Color(0xFF6B6B6B),
chartSeries1 = Color(0xFF6BAF8A),
chartSeries1Muted = Color(0x206BAF8A),     // #6BAF8A20 -> alpha 0x20
chartSeries2 = Color(0xFF7AA4CA),
chartBar1 = Color(0xFFA3A3A3),
chartBar2 = Color(0xFF8A8A8A),
chartBar3 = Color(0xFF6B6B6B),
chartBarHighlight = Color(0xFF6BAF8A),
```

Keep existing patterns intact: `@Immutable` on data class, same ordering convention (data class, LocalComposition, Light val, Dark val). Place the chart token block after the table tokens block in each location.
  </action>
  <verify>Project compiles: `cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinDesktop --no-daemon -q` (or equivalent desktop target). All 4 locations (data class, Local default, Light, Dark) have exactly 11 new chart properties each.</verify>
  <done>TerminalColors.kt contains chartBg, chartGrid, chartAxis, chartAxisText, chartSeries1, chartSeries1Muted, chartSeries2, chartBar1, chartBar2, chartBar3, chartBarHighlight in data class + Local default + Light + Dark vals, and the module compiles.</done>
</task>

<task type="auto">
  <name>Task 2: Create TerminalLineChart composable</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalLineChart.kt</files>
  <action>
Create a new file in the `components/data` package. Follow the exact same pattern as TerminalTable.kt and TerminalProgress.kt: package declaration, foundation imports only (no Material3), `@Composable` functions using `TerminalTheme.colors/typography/borders/radius`, `@TerminalPreview` private previews wrapped in `TerminalTheme { ... }` with `colors.bg` background.

**Data models** (top of file, public):
```kotlin
data class ChartDataPoint(val x: Float, val y: Float)
data class ChartSeries(
    val label: String,
    val points: List<ChartDataPoint>,
    val color: Color? = null,
)
```

**TerminalLineChart composable** signature:
```kotlin
@Composable
fun TerminalLineChart(
    title: String,
    series: List<ChartSeries>,
    xLabels: List<String>,
    modifier: Modifier = Modifier,
    description: String? = null,
    yLabelCount: Int = 5,
)
```

**Layout structure** (outer Column with `modifier`, `chartBg` background, 1dp border via `colors.chartAxis`, 4dp `RoundedCornerShape`):

1. **Header section** (Column, padding 16dp vertical / 20dp horizontal, bottom border 1dp `chartAxis`):
   - Title: `BasicText`, style `typography.md.copy(fontWeight = FontWeight.Medium, color = colors.text)`  (14sp medium)
   - Description (if non-null): `BasicText`, style `typography.xs.copy(color = colors.textDim)` (11sp dim), 4dp top spacer
   - Legend row (Row, 8dp top spacer, `horizontalArrangement = Arrangement.spacedBy(16.dp)`):
     - For each series: Row with 6dp gap containing a Canvas(8.dp size) drawing a filled circle in the series color (use `series.color ?: if index==0 colors.chartSeries1 else colors.chartSeries2`), and BasicText label in `typography.xs.copy(fontSize = 10.sp, color = colors.textMuted)`

2. **Body section** (Row, padding top=16dp, end=20dp, bottom=12dp, start=0dp):
   - **Y-axis labels** (Column, width=60dp, `verticalArrangement = Arrangement.SpaceBetween`, height matching plot area):
     - Compute yMin (always 0) and yMax from all series points (round up to nice number). Generate `yLabelCount` labels from yMax down to yMin.
     - Each label: `BasicText`, style `typography.xs.copy(fontSize = 9.sp, color = colors.chartAxisText)`, right-aligned in 40dp width padded to 60dp total with end padding.
   - **Plot area** (Box, weight 1f, height 240dp):
     - Canvas fills the entire box. Draw:
       a. **Horizontal grid lines**: `yLabelCount` lines from bottom to top, color `colors.chartGrid`, strokeWidth 1dp.
       b. **For each series** (resolve color as above):
          - Build a `Path` from normalized data points (x scaled to canvas width, y scaled to canvas height inverted).
          - **Area fill**: clone the path, lineTo bottom-right, lineTo bottom-left, close. Fill with `Brush.verticalGradient(listOf(seriesColor.copy(alpha=0.15f), Color.Transparent))`. For series1 specifically use `colors.chartSeries1Muted` as the top gradient stop instead of computing alpha.
          - **Line stroke**: `drawPath(path, color=seriesColor, style=Stroke(width=2.dp.toPx(), cap=StrokeCap.Round, join=StrokeJoin.Round))`
          - **Data points**: for each point, `drawCircle(color=colors.chartBg, radius=3.dp.toPx(), center=pointOffset)` then `drawCircle(color=seriesColor, radius=3.dp.toPx(), center=pointOffset, style=Stroke(width=2.dp.toPx()))` (6dp diameter circle with 2dp border, white/bg center).

3. **X-axis labels** (Row, padding start=60dp, end=20dp, bottom=16dp, `horizontalArrangement = Arrangement.SpaceBetween`):
   - For each label in `xLabels`: `BasicText`, style `typography.xs.copy(fontSize = 9.sp, color = colors.chartAxisText)`

**Preview function** `TerminalLineChartPreview` (private, `@TerminalPreview`):
- Wrap in `TerminalTheme { Column(modifier = Modifier.background(TerminalTheme.colors.bg).padding(16.dp)) { ... } }`
- Show one TerminalLineChart with title "Revenue", description "Monthly revenue (thousands)", two series: "Product A" with 6 points forming an upward trend, "Product B" with 6 points forming a different pattern, xLabels = listOf("Jan","Feb","Mar","Apr","May","Jun").

Use `remember` + `derivedStateOf` or simple local computations for yMax/yMin. Keep computations inside the composable, no external utilities. Import `androidx.compose.ui.graphics.Path`, `StrokeJoin`, `PathEffect` as needed.
  </action>
  <verify>Project compiles: `cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinDesktop --no-daemon -q`. File exists at expected path. Contains `@TerminalPreview` annotation on preview function. Uses only foundation/compose-ui imports (no Material3).</verify>
  <done>TerminalLineChart.kt exists with ChartDataPoint, ChartSeries data classes and TerminalLineChart composable that renders header (title, description, legend), Canvas plot area (grid, gradient area fill, line paths, data point circles), Y-axis labels, X-axis labels -- all styled from TerminalTheme chart tokens. Preview function included.</done>
</task>

<task type="auto">
  <name>Task 3: Create TerminalBarChart composable</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalBarChart.kt</files>
  <action>
Create a new file in the `components/data` package. Same pattern conventions as Task 2.

**Data model** (top of file, public):
```kotlin
data class BarData(
    val label: String,
    val value: Float,
    val highlight: Boolean = false,
)
```

**TerminalBarChart composable** signature:
```kotlin
@Composable
fun TerminalBarChart(
    title: String,
    bars: List<BarData>,
    modifier: Modifier = Modifier,
    description: String? = null,
    yLabelCount: Int = 5,
)
```

**Layout structure** (outer Column with `modifier`, `chartBg` background, 1dp border `chartAxis`, 4dp corner radius -- identical container to line chart):

1. **Header section** (Column, padding 16dp vertical / 20dp horizontal, bottom border 1dp `chartAxis`):
   - Title: `BasicText`, `typography.md.copy(fontWeight = FontWeight.Medium, color = colors.text)`
   - Description (if non-null): `BasicText`, `typography.xs.copy(color = colors.textDim)`, 4dp spacer
   - Total line: `BasicText`, text = "Total: ${bars.sumOf { it.value.toDouble() }.toInt()}", style `typography.xs.copy(fontWeight = FontWeight.Medium, color = colors.textMuted)`, 4dp spacer

2. **Body section** (Row, padding top=16dp, end=20dp, bottom=0dp, start=0dp):
   - **Y-axis labels** (Column, width=60dp, height matching plot, `Arrangement.SpaceBetween`):
     - Compute yMax from bars, round up. Generate `yLabelCount` labels from yMax down to 0.
     - Each: `BasicText`, `typography.xs.copy(fontSize = 9.sp, color = colors.chartAxisText)`, right-aligned in 40dp padded to 60dp.
   - **Plot area** (Box, weight 1f, height 240dp):
     - Canvas fills box. Draw:
       a. **Horizontal grid lines**: same as line chart.
       b. **Bars**: distribute bars evenly across width. Bar width = 55dp (capped at `(canvasWidth - totalGaps) / barCount`). Each bar:
          - Height proportional to `value / yMax * canvasHeight`.
          - Top corner radius [3dp, 3dp, 0, 0] -- use `drawRoundRect` with `CornerRadius(3.dp.toPx())` for the full rect then clip or use `drawPath` with a custom path that has rounded top-left/top-right and flat bottom corners.
          - Color assignment by tier: assign bars into 3 color tiers based on value ranking. The bar with the highest value (or `highlight=true`) gets `colors.chartBarHighlight`. Remaining bars cycle through `colors.chartBar1`, `colors.chartBar2`, `colors.chartBar3` based on descending value rank. Simpler approach: if `bar.highlight` is true use `chartBarHighlight`, else assign `chartBar1` to first third, `chartBar2` to second third, `chartBar3` to last third by index. Actually, simplest correct approach: use `chartBarHighlight` for bars with `highlight=true`, for non-highlighted bars assign colors by index cycling through `listOf(chartBar1, chartBar2, chartBar3)`.

3. **X-axis labels** (Row, padding start=60dp, end=20dp, top=8dp, bottom=16dp, `Arrangement.SpaceAround`):
   - For each bar: `BasicText` with bar.label, style `typography.xs.copy(fontSize = 9.sp, color = if (bar.highlight) colors.chartBarHighlight else colors.chartAxisText, fontWeight = if (bar.highlight) FontWeight.SemiBold else FontWeight.Normal)` (highlighted label matches bar highlight color with heavier weight per Pencil spec).

**For the bar top-corner-radius**: Use a `Path` approach:
```kotlin
val barPath = Path().apply {
    val r = 3.dp.toPx()
    moveTo(barLeft, barTop + r)
    arcTo(Rect(barLeft, barTop, barLeft + 2*r, barTop + 2*r), 180f, 90f, false)
    arcTo(Rect(barRight - 2*r, barTop, barRight, barTop + 2*r), 270f, 90f, false)
    lineTo(barRight, barBottom)
    lineTo(barLeft, barBottom)
    close()
}
drawPath(barPath, color = barColor)
```

**Preview function** `TerminalBarChartPreview` (private, `@TerminalPreview`):
- Show one TerminalBarChart with title "Deployments", description "Weekly deployment count", bars = listOf of 6 BarData items with varying values and one having `highlight = true`.
- Wrapped in `TerminalTheme { Column(Modifier.background(colors.bg).padding(16.dp)) { ... } }`.

Import `androidx.compose.ui.graphics.Path`, `androidx.compose.ui.geometry.Rect` for the arc-based bar drawing.
  </action>
  <verify>Project compiles: `cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinDesktop --no-daemon -q`. File exists at expected path. Contains `@TerminalPreview` annotation. Uses only foundation/compose-ui imports. BarData model is public.</verify>
  <done>TerminalBarChart.kt exists with BarData data class and TerminalBarChart composable that renders header (title, description, total), Canvas plot area (grid lines, bars with rounded top corners and tier colors, highlighted bar), Y-axis labels, X-axis labels with highlight styling -- all using TerminalTheme chart tokens. Preview function included.</done>
</task>

</tasks>

<verification>
1. Full module compiles: `./gradlew :app:designsystem:compileKotlinDesktop --no-daemon -q`
2. TerminalColors.kt has 11 new chart tokens in data class, Local default, Light, and Dark (44 new lines across 4 locations).
3. TerminalLineChart.kt renders: header with legend, Canvas with grid/area/line/points, axis labels.
4. TerminalBarChart.kt renders: header with total, Canvas with grid/bars, axis labels with highlight.
5. Both chart files use only `TerminalTheme.colors.chart*` tokens -- no hardcoded colors.
6. Both files have `@TerminalPreview` private preview functions.
7. No Material3 imports in any modified/created file.
</verification>

<success_criteria>
- Design system module compiles without errors
- Chart color tokens available via TerminalTheme.colors in both light and dark themes
- TerminalLineChart renders multi-series area chart with gradient, line strokes, data points, grid, and legend
- TerminalBarChart renders histogram with tier-colored bars (rounded top), highlight support, total readout
- Both components follow existing codebase conventions (foundation-only, JetBrains Mono, BasicText, Canvas drawing, TerminalPreview)
</success_criteria>

<output>
After completion, create `.planning/quick/17-implement-chart-components-from-pencil-d/17-SUMMARY.md`
</output>
