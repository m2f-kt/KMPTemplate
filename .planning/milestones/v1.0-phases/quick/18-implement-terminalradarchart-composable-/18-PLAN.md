---
phase: quick-18
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalRadarChart.kt
autonomous: true

must_haves:
  truths:
    - "Radar chart renders with hexagonal grid structure"
    - "Multiple data series render as overlapping polygons with distinct colors"
    - "Six axis labels position correctly around the hexagon perimeter"
    - "Chart follows established terminal styling (card border, header, legend)"
    - "Preview shows complete example with two data series"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalRadarChart.kt"
      provides: "TerminalRadarChart composable, RadarDataPoint, RadarSeries data classes"
      min_lines: 250
      exports: ["RadarDataPoint", "RadarSeries", "TerminalRadarChart"]
  key_links:
    - from: "TerminalRadarChart"
      to: "TerminalTheme.colors"
      via: "color token extraction"
      pattern: "colors\\.(chartBg|chartGrid|chartAxis|chartSeries1|chartSeries2)"
    - from: "Canvas rendering"
      to: "hexagonal vertices"
      via: "cos/sin geometry at 60° intervals"
      pattern: "cos.*sin.*60"
---

<objective>
Create TerminalRadarChart composable matching existing chart component patterns (TerminalLineChart, TerminalBarChart) and implementing the Pencil design specification for hexagonal radar charts.

Purpose: Provide a terminal-styled radar chart component for displaying multi-dimensional performance metrics in a hexagonal layout, completing the chart component suite.

Output: Single Kotlin file with RadarDataPoint and RadarSeries data classes, TerminalRadarChart composable with Canvas-based hexagon rendering, and preview demonstrating two-series system performance visualization.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
Existing chart components establish patterns:
@./app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalLineChart.kt
@./app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalBarChart.kt

Design specifications from Pencil (node n3Pkh):
- Hexagonal layout with 6 axes at 60° intervals
- 4 concentric grid rings at 25%, 50%, 75%, 100%
- Grid and axes use chartGrid color (#2A2A2A dark)
- Two data series with polygons: chartSeries1 (fill opacity 0.2, stroke 2dp), chartSeries2 (fill opacity 0.15, stroke 1.5dp)
- Data point dots: 6x6dp ellipses with 2dp stroke in chartBg color
- Six axis labels: "cpu", "mem", "i/o", "latency", "net", "throughput"
- Labels positioned outside hexagon with chartAxisText color
- Header with title + description + legend (follows TerminalLineChart pattern)
</context>

<tasks>

<task type="auto">
  <name>Create TerminalRadarChart.kt with data models and composable</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalRadarChart.kt</files>
  <action>
Create new file with:

1. **Data Models** (following ChartDataPoint/ChartSeries naming pattern):
   - RadarDataPoint(label: String, value: Float) — label for axis name, value normalized 0..1
   - RadarSeries(label: String, points: List<RadarDataPoint>, color: Color? = null) — optional color override

2. **TerminalRadarChart Composable**:
   Parameters:
   - title: String
   - series: List<RadarSeries>
   - modifier: Modifier = Modifier
   - description: String? = null
   - chartSize: Dp = 300.dp (plot area dimension from Pencil design)

   Structure (match TerminalLineChart pattern exactly):
   - Column with clip(RoundedCornerShape(4.dp)) + border(1.dp, colors.chartAxis) + background(colors.chartBg)
   - Header section with padding(vertical=16.dp, horizontal=20.dp):
     - title: typography.md, fontWeight Medium, colors.text
     - description (if not null): typography.xs, colors.textDim, 4.dp spacer above
     - legend Row with 16.dp spacing between series: 8.dp circle dots + 6.dp gap + label in typography.xs (10.sp, colors.textMuted)
   - Header bottom border: Box 1.dp height, colors.chartAxis background
   - Body with padding(20.dp):
     - Box with chartSize x chartSize dimensions for plot area
     - Canvas for hexagon rendering (see step 3)
     - Positioned axis labels around Canvas (see step 4)

3. **Canvas Rendering** (extract theme colors before Canvas block):
   Extract: chartGridColor, chartAxisColor, chartBgColor, chartSeries1Color, chartSeries2Color

   Inside Canvas:
   a) Compute hexagonal vertices:
      - 6 vertices at angles: -90°, -30°, 30°, 90°, 150°, 210° (start from top, clockwise)
      - Radius = canvasWidth / 2 - padding for labels (use canvasWidth * 0.4 as radius)
      - Center = Offset(canvasWidth/2, canvasHeight/2)
      - For each angle: Offset(center.x + radius * cos(angle), center.y + radius * sin(angle))

   b) Draw concentric hexagons (4 rings at 25%, 50%, 75%, 100%):
      - For each scale in [0.25f, 0.5f, 0.75f, 1.0f]:
        - Create Path connecting scaled vertices
        - drawPath with chartGridColor, Stroke(1.dp.toPx())

   c) Draw axes from center to each vertex:
      - For each vertex: drawLine(chartGridColor, center, vertex, strokeWidth=1.dp.toPx())

   d) Draw data series polygons:
      - For each series in series.reversed() (draw back-to-front):
        - seriesColor = series.color ?: (index 0 → chartSeries1Color, else chartSeries2Color)
        - Build polygon points: for each dataPoint, find matching vertex by label, compute point = center + (vertex - center) * dataPoint.value
        - Fill polygon: drawPath with seriesColor.copy(alpha = if first series 0.2f else 0.15f)
        - Stroke polygon: drawPath with seriesColor, Stroke(width = if first series 2.dp else 1.5.dp, cap=StrokeCap.Round, join=StrokeJoin.Round)

   e) Draw data point dots (only for first series):
      - For each point in first series polygon:
        - drawCircle(chartBgColor, radius=3.dp.toPx(), center=point) — background fill
        - drawCircle(seriesColor, radius=3.dp.toPx(), center=point, style=Stroke(2.dp.toPx())) — colored stroke

4. **Axis Labels** (positioned around Canvas):
   - Use Box with absolute positioning or overlay approach
   - Six labels at vertices: "cpu", "mem", "i/o", "latency", "net", "throughput"
   - Position outside hexagon vertices with offset (use BasicText positioned via Modifier.offset)
   - Style: typography.xs.copy(fontSize=10.sp, color=colors.chartAxisText, textAlign depending on position)

5. **Preview** (@TerminalPreview, private):
   - TerminalTheme wrapper
   - Column with background(colors.bg) + padding(16.dp)
   - TerminalRadarChart with:
     - title = "sys_profile()"
     - description = "// system performance radar"
     - series = two RadarSeries:
       - "current" with 6 points: [("cpu", 0.75f), ("mem", 0.62f), ("i/o", 0.88f), ("latency", 0.45f), ("net", 0.71f), ("throughput", 0.83f)]
       - "baseline" with 6 points: [("cpu", 0.60f), ("mem", 0.55f), ("i/o", 0.65f), ("latency", 0.50f), ("net", 0.58f), ("throughput", 0.70f)]

**Imports needed**: Match TerminalLineChart imports (Canvas, drawPath, drawCircle, Path, Offset, Stroke, StrokeCap, StrokeJoin, BasicText, geometry utilities, plus kotlin.math.cos, kotlin.math.sin, kotlin.math.PI).

**Follow established conventions**:
- Extract all theme colors before Canvas block (NOT inside Canvas lambda)
- Use RoundedCornerShape(4.dp) for card shape
- Use typography.md for title, typography.xs for description/legend/labels
- Legend dots: 8.dp Canvas circles, 6.dp gap before label text
- DO NOT create new color tokens — use existing chartSeries1, chartSeries2, chartGrid, chartAxis, chartAxisText, chartBg
  </action>
  <verify>
File exists and compiles:
```bash
./gradlew :app:designsystem:compileKotlinJvm
```

Verify structure matches existing chart patterns:
```bash
grep -E "^(data class|@Composable|fun TerminalRadarChart)" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalRadarChart.kt
```

Expected output shows: RadarDataPoint, RadarSeries data classes, TerminalRadarChart composable, preview function.
  </verify>
  <done>
TerminalRadarChart.kt exists with RadarDataPoint and RadarSeries data models, TerminalRadarChart composable following established chart component patterns (card structure, header with legend, Canvas rendering), hexagonal grid with axes rendered correctly, data series polygons with opacity fills and strokes, axis labels positioned around hexagon, preview demonstrating two-series system performance visualization, file compiles without errors.
  </done>
</task>

</tasks>

<verification>
1. Build succeeds: `./gradlew :app:designsystem:compileKotlinJvm`
2. File structure matches TerminalLineChart/TerminalBarChart patterns (header, legend, Canvas body)
3. Preview renders correctly in IDE preview pane (if available)
4. Data models follow established naming (RadarDataPoint, RadarSeries)
5. All colors sourced from theme tokens (NO hardcoded colors)
</verification>

<success_criteria>
- [ ] TerminalRadarChart.kt created in correct package location
- [ ] RadarDataPoint and RadarSeries data classes defined with documented fields
- [ ] TerminalRadarChart composable accepts title, series, modifier, description parameters
- [ ] Card structure matches existing charts: RoundedCornerShape(4.dp), 1.dp border with chartAxis color, chartBg background
- [ ] Header includes title (typography.md), optional description (typography.xs), legend with colored dots and series labels
- [ ] Canvas renders hexagonal grid with 4 concentric rings at 25/50/75/100% scales
- [ ] Canvas renders 6 axes from center to vertices
- [ ] Data series render as filled polygons with correct opacity (0.2 for first, 0.15 for second) and stroke width (2dp vs 1.5dp)
- [ ] Data point dots render on first series only (3dp radius, chartBg fill + 2dp colored stroke)
- [ ] Six axis labels positioned correctly around hexagon perimeter with chartAxisText color
- [ ] Preview demonstrates two-series radar chart matching Pencil design example
- [ ] All colors use existing theme tokens (chartSeries1, chartSeries2, chartGrid, chartAxis, chartAxisText, chartBg)
- [ ] File compiles successfully without errors or warnings
</success_criteria>

<output>
After completion, create `.planning/quick/18-implement-terminalradarchart-composable-/18-SUMMARY.md`
</output>
