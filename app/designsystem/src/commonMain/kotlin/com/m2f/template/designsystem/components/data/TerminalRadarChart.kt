package com.m2f.template.designsystem.components.data

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m2f.template.designsystem.theme.TerminalPreview
import com.m2f.template.designsystem.theme.TerminalTheme
import kotlin.math.cos
import kotlin.math.sin

private const val TWO_PI = 2 * kotlin.math.PI
private const val HALF_PI = kotlin.math.PI / 2

/**
 * A data point on a radar chart axis.
 *
 * @param label The axis name (e.g. "cpu", "mem"). Must match across series for alignment.
 * @param value Normalized value from 0 to 1 representing the magnitude on this axis.
 */
data class RadarDataPoint(
    val label: String,
    val value: Float,
)

/**
 * A named series of radar data points.
 *
 * @param label Display label used in the legend.
 * @param points The data points for each axis. Order determines polygon shape.
 * @param color Optional override color. If null, the chart assigns a default from theme tokens.
 */
data class RadarSeries(
    val label: String,
    val points: List<RadarDataPoint>,
    val color: Color? = null,
)

/**
 * A terminal-styled radar chart that renders one or more data series as overlapping polygons
 * on a hexagonal grid with concentric rings and labeled axes.
 *
 * The chart draws 4 concentric hexagonal rings at 25%, 50%, 75%, and 100% scales, with
 * 6 axes radiating from center to vertices. Data series are rendered as filled polygons
 * with opacity fills and colored strokes.
 *
 * All colors are sourced from [TerminalTheme.colors] chart tokens, ensuring correct appearance
 * in both light and dark modes.
 *
 * @param title The chart title displayed in the header.
 * @param series The list of data series to plot.
 * @param modifier Modifier for the outer container.
 * @param description Optional subtitle displayed below the title.
 * @param chartSize The width and height of the hexagonal plot area.
 */
@Composable
fun TerminalRadarChart(
    title: String,
    series: List<RadarSeries>,
    modifier: Modifier = Modifier,
    description: String? = null,
    chartSize: Dp = 300.dp,
    animated: Boolean = true,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    val progress = remember { Animatable(if (animated) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (animated) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
            )
        }
    }

    val shape = RoundedCornerShape(4.dp)

    // Axis labels from first series (defines the hexagon axes)
    val axisLabels = series.firstOrNull()?.points?.map { it.label } ?: emptyList()
    val axisCount = axisLabels.size

    Column(
        modifier = modifier
            .clip(shape)
            .border(1.dp, colors.chartAxis, shape)
            .background(colors.chartBg),
    ) {
        // Header section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
        ) {
            BasicText(
                text = title,
                style = typography.md.copy(
                    fontWeight = FontWeight.Medium,
                    color = colors.text,
                ),
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                BasicText(
                    text = description,
                    style = typography.xs.copy(color = colors.textDim),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                series.forEachIndexed { index, s ->
                    val seriesColor = s.color
                        ?: if (index == 0) colors.chartSeries1 else colors.chartSeries2
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(color = seriesColor)
                        }
                        BasicText(
                            text = s.label,
                            style = typography.xs.copy(
                                fontSize = 10.sp,
                                color = colors.textMuted,
                            ),
                        )
                    }
                }
            }
        }

        // Bottom border of header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.chartAxis),
        )

        // Body with chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Extract theme colors before Canvas block
            val chartGridColor = colors.chartGrid
            val chartBgColor = colors.chartBg
            val chartAxisTextColor = colors.chartAxisText
            val chartSeries1Color = colors.chartSeries1
            val chartSeries2Color = colors.chartSeries2

            Box(
                modifier = Modifier.size(chartSize),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(chartSize)) {
                    if (axisCount == 0) return@Canvas

                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                    val radius = canvasWidth * 0.35f

                    // Compute vertex angles: start from top (-90deg), going clockwise
                    val angles = (0 until axisCount).map { i ->
                        -HALF_PI + (TWO_PI * i / axisCount)
                    }

                    // Compute full-scale vertex positions
                    val vertices = angles.map { angle ->
                        Offset(
                            x = center.x + radius * cos(angle).toFloat(),
                            y = center.y + radius * sin(angle).toFloat(),
                        )
                    }

                    // Draw 4 concentric hexagonal grid rings
                    val gridScales = listOf(0.25f, 0.5f, 0.75f, 1.0f)
                    gridScales.forEach { scale ->
                        val ringPath = Path().apply {
                            val scaledVertices = angles.map { angle ->
                                Offset(
                                    x = center.x + radius * scale * cos(angle).toFloat(),
                                    y = center.y + radius * scale * sin(angle).toFloat(),
                                )
                            }
                            moveTo(scaledVertices.first().x, scaledVertices.first().y)
                            for (i in 1 until scaledVertices.size) {
                                lineTo(scaledVertices[i].x, scaledVertices[i].y)
                            }
                            close()
                        }
                        drawPath(
                            path = ringPath,
                            color = chartGridColor,
                            style = Stroke(width = 1.dp.toPx()),
                        )
                    }

                    // Draw axes from center to each vertex
                    vertices.forEach { vertex ->
                        drawLine(
                            color = chartGridColor,
                            start = center,
                            end = vertex,
                            strokeWidth = 1.dp.toPx(),
                        )
                    }

                    // Draw data series polygons (back-to-front)
                    series.reversed().forEachIndexed { reversedIndex, s ->
                        val originalIndex = series.size - 1 - reversedIndex
                        val seriesColor = s.color
                            ?: if (originalIndex == 0) chartSeries1Color else chartSeries2Color

                        // Build polygon points by matching labels to axes
                        val polygonPoints = (0 until axisCount).map { axisIndex ->
                            val axisLabel = axisLabels[axisIndex]
                            val dataValue = s.points
                                .firstOrNull { it.label == axisLabel }?.value ?: 0f
                            val clampedValue = dataValue.coerceIn(0f, 1f) * progress.value
                            Offset(
                                x = center.x + radius * clampedValue * cos(angles[axisIndex]).toFloat(),
                                y = center.y + radius * clampedValue * sin(angles[axisIndex]).toFloat(),
                            )
                        }

                        if (polygonPoints.isEmpty()) return@forEachIndexed

                        // Fill polygon
                        val fillPath = Path().apply {
                            moveTo(polygonPoints.first().x, polygonPoints.first().y)
                            for (i in 1 until polygonPoints.size) {
                                lineTo(polygonPoints[i].x, polygonPoints[i].y)
                            }
                            close()
                        }

                        val fillAlpha = if (originalIndex == 0) 0.2f else 0.15f
                        drawPath(
                            path = fillPath,
                            color = seriesColor.copy(alpha = fillAlpha),
                        )

                        // Stroke polygon
                        val strokeWidth = if (originalIndex == 0) 2.dp.toPx() else 1.5f.dp.toPx()
                        drawPath(
                            path = fillPath,
                            color = seriesColor,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )

                        // Data point dots (first series only)
                        if (originalIndex == 0) {
                            polygonPoints.forEach { point ->
                                drawCircle(
                                    color = chartBgColor,
                                    radius = 3.dp.toPx(),
                                    center = point,
                                )
                                drawCircle(
                                    color = seriesColor,
                                    radius = 3.dp.toPx(),
                                    center = point,
                                    style = Stroke(width = 2.dp.toPx()),
                                )
                            }
                        }
                    }
                }

                // Axis labels positioned around the hexagon
                if (axisCount > 0) {
                    val labelRadius = chartSize * 0.44f
                    (0 until axisCount).forEach { i ->
                        val angle = -HALF_PI + (TWO_PI * i / axisCount)
                        val offsetX = labelRadius * cos(angle).toFloat()
                        val offsetY = labelRadius * sin(angle).toFloat()

                        // Determine text alignment based on position
                        val textAlignment = when {
                            // Top (index 0)
                            i == 0 -> TextAlign.Center
                            // Right side
                            cos(angle) > 0.1 -> TextAlign.Start
                            // Left side
                            cos(angle) < -0.1 -> TextAlign.End
                            // Bottom
                            else -> TextAlign.Center
                        }

                        BasicText(
                            text = axisLabels[i],
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(x = offsetX, y = offsetY),
                            style = typography.xs.copy(
                                fontSize = 10.sp,
                                color = chartAxisTextColor,
                                textAlign = textAlignment,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@TerminalPreview
@Composable
private fun TerminalRadarChartPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
        ) {
            TerminalRadarChart(
                title = "sys_profile()",
                description = "// system performance radar",
                series = listOf(
                    RadarSeries(
                        label = "current",
                        points = listOf(
                            RadarDataPoint("cpu", 0.75f),
                            RadarDataPoint("mem", 0.62f),
                            RadarDataPoint("i/o", 0.88f),
                            RadarDataPoint("latency", 0.45f),
                            RadarDataPoint("net", 0.71f),
                            RadarDataPoint("throughput", 0.83f),
                        ),
                    ),
                    RadarSeries(
                        label = "baseline",
                        points = listOf(
                            RadarDataPoint("cpu", 0.60f),
                            RadarDataPoint("mem", 0.55f),
                            RadarDataPoint("i/o", 0.65f),
                            RadarDataPoint("latency", 0.50f),
                            RadarDataPoint("net", 0.58f),
                            RadarDataPoint("throughput", 0.70f),
                        ),
                    ),
                ),
            )
        }
    }
}
