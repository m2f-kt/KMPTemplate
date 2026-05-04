package com.m2f.template.designsystem.components.data

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m2f.template.designsystem.theme.TerminalPreview
import com.m2f.template.designsystem.theme.TerminalTheme
import kotlin.math.ceil

/**
 * A data point in a chart series.
 *
 * @param x The x-axis value.
 * @param y The y-axis value.
 */
data class ChartDataPoint(val x: Float, val y: Float)

/**
 * A named series of data points for line charts.
 *
 * @param label Display label used in the legend.
 * @param points The data points in this series.
 * @param color Optional override color. If null, the chart assigns a default from theme tokens.
 */
data class ChartSeries(
    val label: String,
    val points: List<ChartDataPoint>,
    val color: Color? = null,
)

/**
 * A terminal-styled area/line chart that renders one or more data series with gradient fill,
 * line strokes, data point circles, grid lines, axis labels, and a legend.
 *
 * When the number of x-axis labels exceeds [scrollThreshold], the plot area and x-axis
 * labels become horizontally scrollable while the y-axis stays pinned.
 *
 * All colors are sourced from [TerminalTheme.colors] chart tokens, ensuring correct appearance
 * in both light and dark modes.
 *
 * @param title The chart title displayed in the header.
 * @param series The list of data series to plot.
 * @param xLabels Labels for the x-axis.
 * @param modifier Modifier for the outer container.
 * @param description Optional subtitle displayed below the title.
 * @param yLabelCount Number of y-axis labels (including 0).
 * @param scrollThreshold When x-label count exceeds this, the chart scrolls horizontally.
 * @param pointSpacing Fixed width per data point slot when scrolling is active.
 */
@Composable
fun TerminalLineChart(
    title: String,
    series: List<ChartSeries>,
    xLabels: List<String>,
    modifier: Modifier = Modifier,
    description: String? = null,
    yLabelCount: Int = 5,
    scrollThreshold: Int = 8,
    pointSpacing: Dp = 80.dp,
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

    val shape = RoundedCornerShape(TerminalTheme.radius.sm)
    val isScrollable = xLabels.size > scrollThreshold

    // Compute y-axis range
    val yMax = run {
        val maxValue = series.flatMap { it.points }.maxOfOrNull { it.y } ?: 1f
        val magnitude = generateSequence(1f) { it * 10f }.first { it >= maxValue }
        val step = magnitude / 10f
        ceil(maxValue / step) * step
    }
    val yLabels = (0 until yLabelCount).map { i ->
        val value = yMax - (yMax / (yLabelCount - 1)) * i
        if (value >= 1000) "${(value / 1000).toInt()}k"
        else if (value == value.toInt().toFloat()) value.toInt().toString()
        else "${(value * 10).toInt() / 10.0}"
    }

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
                            style = typography.xxs.copy(
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

        // Body + X-axis section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 20.dp, bottom = 12.dp),
        ) {
            // Y-axis labels (pinned)
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .height(240.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                yLabels.forEach { label ->
                    BasicText(
                        text = label,
                        modifier = Modifier
                            .width(60.dp)
                            .padding(end = 8.dp),
                        style = typography.chartAxis.copy(
                            color = colors.chartAxisText,
                            textAlign = TextAlign.End,
                        ),
                    )
                }
            }

            // Scrollable viewport for plot + x-axis
            val scrollState = rememberScrollState()
            val contentWidth = if (isScrollable) pointSpacing * xLabels.size else 0.dp

            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isScrollable) Modifier.horizontalScroll(scrollState)
                        else Modifier,
                    ),
            ) {
                Column(
                    modifier = if (isScrollable) {
                        Modifier.width(contentWidth)
                    } else {
                        Modifier.fillMaxWidth()
                    },
                ) {
                    // Plot area
                    val chartSeries1Color = colors.chartSeries1
                    val chartSeries1MutedColor = colors.chartSeries1Muted
                    val chartSeries2Color = colors.chartSeries2
                    val chartBgColor = colors.chartBg
                    val chartGridColor = colors.chartGrid

                    Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            clipRect(right = size.width * progress.value) {
                                // Horizontal grid lines
                                for (i in 0 until yLabelCount) {
                                    val y = canvasHeight * i / (yLabelCount - 1)
                                    drawLine(
                                        color = chartGridColor,
                                        start = Offset(0f, y),
                                        end = Offset(canvasWidth, y),
                                        strokeWidth = 1.dp.toPx(),
                                    )
                                }

                                // Draw each series
                                series.forEachIndexed { index, s ->
                                    val seriesColor = s.color
                                        ?: if (index == 0) {
                                            chartSeries1Color
                                        } else {
                                            chartSeries2Color
                                        }

                                    if (s.points.isEmpty()) return@forEachIndexed

                                    // Build points
                                    val xMin = s.points.minOf { it.x }
                                    val xMax = s.points.maxOf { it.x }
                                    val xRange = if (xMax - xMin == 0f) 1f else xMax - xMin

                                    val offsets = s.points.map { point ->
                                        Offset(
                                            x = (point.x - xMin) / xRange * canvasWidth,
                                            y = canvasHeight - (point.y / yMax) * canvasHeight,
                                        )
                                    }

                                    // Area fill path
                                    val areaPath = Path().apply {
                                        moveTo(offsets.first().x, offsets.first().y)
                                        for (i in 1 until offsets.size) {
                                            lineTo(offsets[i].x, offsets[i].y)
                                        }
                                        lineTo(offsets.last().x, canvasHeight)
                                        lineTo(offsets.first().x, canvasHeight)
                                        close()
                                    }

                                    val gradientTopColor = if (index == 0) {
                                        chartSeries1MutedColor
                                    } else {
                                        seriesColor.copy(alpha = 0.15f)
                                    }

                                    drawPath(
                                        path = areaPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                gradientTopColor,
                                                Color.Transparent,
                                            ),
                                        ),
                                    )

                                    // Line stroke path
                                    val linePath = Path().apply {
                                        moveTo(offsets.first().x, offsets.first().y)
                                        for (i in 1 until offsets.size) {
                                            lineTo(offsets[i].x, offsets[i].y)
                                        }
                                    }

                                    drawPath(
                                        path = linePath,
                                        color = seriesColor,
                                        style = Stroke(
                                            width = 2.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round,
                                        ),
                                    )

                                    // Data points
                                    offsets.forEach { offset ->
                                        drawCircle(
                                            color = chartBgColor,
                                            radius = 3.dp.toPx(),
                                            center = offset,
                                        )
                                        drawCircle(
                                            color = seriesColor,
                                            radius = 3.dp.toPx(),
                                            center = offset,
                                            style = Stroke(width = 2.dp.toPx()),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // X-axis labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        xLabels.forEach { label ->
                            BasicText(
                                text = label,
                                style = typography.chartAxis.copy(
                                    color = colors.chartAxisText,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@TerminalPreview
@Composable
private fun TerminalLineChartPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Non-scrollable (6 points, under threshold)
            TerminalLineChart(
                title = "Revenue",
                description = "Monthly revenue (thousands)",
                series = listOf(
                    ChartSeries(
                        label = "Product A",
                        points = listOf(
                            ChartDataPoint(0f, 12f),
                            ChartDataPoint(1f, 18f),
                            ChartDataPoint(2f, 15f),
                            ChartDataPoint(3f, 25f),
                            ChartDataPoint(4f, 32f),
                            ChartDataPoint(5f, 40f),
                        ),
                    ),
                    ChartSeries(
                        label = "Product B",
                        points = listOf(
                            ChartDataPoint(0f, 8f),
                            ChartDataPoint(1f, 12f),
                            ChartDataPoint(2f, 20f),
                            ChartDataPoint(3f, 16f),
                            ChartDataPoint(4f, 22f),
                            ChartDataPoint(5f, 28f),
                        ),
                    ),
                ),
                xLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
            )

            // Scrollable (12 points, over threshold)
            TerminalLineChart(
                title = "CPU Load",
                description = "Hourly average over 24h",
                series = listOf(
                    ChartSeries(
                        label = "server_1",
                        points = (0..11).map {
                            ChartDataPoint(
                                it.toFloat(),
                                listOf(
                                    45f, 52f, 38f, 61f, 55f, 72f,
                                    68f, 43f, 57f, 64f, 48f, 59f,
                                )[it],
                            )
                        },
                    ),
                ),
                xLabels = (0..11).map { "${it * 2}:00" },
            )
        }
    }
}
