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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m2f.template.designsystem.theme.TerminalPreview
import com.m2f.template.designsystem.theme.TerminalTheme
import kotlin.math.ceil
import kotlin.math.min

/**
 * A single bar entry in a bar chart.
 *
 * @param label The x-axis label for this bar.
 * @param value The numeric value determining the bar height.
 * @param highlight If true, this bar uses the highlight color and its label is emphasized.
 */
data class BarData(
    val label: String,
    val value: Float,
    val highlight: Boolean = false,
)

/**
 * A terminal-styled bar chart that renders a histogram with tier-colored bars,
 * rounded top corners, grid lines, axis labels, and a total readout.
 *
 * When the number of bars exceeds [scrollThreshold], the plot area and x-axis
 * labels become horizontally scrollable while the y-axis stays pinned.
 *
 * Highlighted bars use [TerminalTheme.colors.chartBarHighlight]. Non-highlighted bars
 * cycle through chartBar1, chartBar2, and chartBar3. All colors come from theme tokens,
 * ensuring correct light/dark mode appearance.
 *
 * @param title The chart title displayed in the header.
 * @param bars The list of bar data entries.
 * @param modifier Modifier for the outer container.
 * @param description Optional subtitle displayed below the title.
 * @param yLabelCount Number of y-axis labels (including 0).
 * @param scrollThreshold When bar count exceeds this, the chart scrolls horizontally.
 * @param slotWidth Fixed width per bar slot when scrolling is active.
 */
@Composable
fun TerminalBarChart(
    title: String,
    bars: List<BarData>,
    modifier: Modifier = Modifier,
    description: String? = null,
    yLabelCount: Int = 5,
    scrollThreshold: Int = 8,
    slotWidth: Dp = 80.dp,
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
    val isScrollable = bars.size > scrollThreshold

    // Compute y-axis range
    val yMax = run {
        val maxValue = bars.maxOfOrNull { it.value } ?: 1f
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

    // Bar color assignment
    val barColors = listOf(colors.chartBar1, colors.chartBar2, colors.chartBar3)

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
            Spacer(modifier = Modifier.height(4.dp))
            BasicText(
                text = "Total: ${bars.sumOf { it.value.toDouble() }.toInt()}",
                style = typography.xs.copy(
                    fontWeight = FontWeight.Medium,
                    color = colors.textMuted,
                ),
            )
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
                .padding(top = 16.dp, end = 20.dp),
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
            val contentWidth = if (isScrollable) slotWidth * bars.size else 0.dp

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
                    val chartGridColor = colors.chartGrid
                    val chartBarHighlightColor = colors.chartBarHighlight

                    Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

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

                            if (bars.isEmpty()) return@Canvas

                            // Bar dimensions — distribute evenly to match SpaceAround x-axis labels
                            val barCount = bars.size
                            val barSlotWidth = canvasWidth / barCount
                            val maxBarWidth = 55.dp.toPx()
                            val barWidth = min(maxBarWidth, barSlotWidth * 0.6f)
                            val cornerRadius = 3.dp.toPx()

                            // Track non-highlight index for color cycling
                            var nonHighlightIndex = 0

                            bars.forEachIndexed { index, bar ->
                                val barColor = if (bar.highlight) {
                                    chartBarHighlightColor
                                } else {
                                    val color = barColors[nonHighlightIndex % barColors.size]
                                    nonHighlightIndex++
                                    color
                                }

                                val barHeight = ((bar.value / yMax) * canvasHeight) * progress.value
                                val slotCenter = barSlotWidth * index + barSlotWidth / 2f
                                val barLeft = slotCenter - barWidth / 2f
                                val barRight = slotCenter + barWidth / 2f
                                val barTop = canvasHeight - barHeight
                                val barBottom = canvasHeight

                                // Path with rounded top corners
                                val barPath = Path().apply {
                                    val r = cornerRadius
                                    moveTo(barLeft, barTop + r)
                                    arcTo(
                                        rect = Rect(
                                            barLeft,
                                            barTop,
                                            barLeft + 2 * r,
                                            barTop + 2 * r,
                                        ),
                                        startAngleDegrees = 180f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false,
                                    )
                                    arcTo(
                                        rect = Rect(
                                            barRight - 2 * r,
                                            barTop,
                                            barRight,
                                            barTop + 2 * r,
                                        ),
                                        startAngleDegrees = 270f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false,
                                    )
                                    lineTo(barRight, barBottom)
                                    lineTo(barLeft, barBottom)
                                    close()
                                }

                                drawPath(barPath, color = barColor)
                            }
                        }
                    }

                    // X-axis labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        bars.forEach { bar ->
                            BasicText(
                                text = bar.label,
                                style = typography.chartAxis.copy(
                                    color = if (bar.highlight) {
                                        colors.chartBarHighlight
                                    } else {
                                        colors.chartAxisText
                                    },
                                    fontWeight = if (bar.highlight) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Normal
                                    },
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
private fun TerminalBarChartPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Non-scrollable (6 bars, under threshold)
            TerminalBarChart(
                title = "Deployments",
                description = "Weekly deployment count",
                bars = listOf(
                    BarData(label = "Mon", value = 12f),
                    BarData(label = "Tue", value = 8f),
                    BarData(label = "Wed", value = 24f, highlight = true),
                    BarData(label = "Thu", value = 18f),
                    BarData(label = "Fri", value = 15f),
                    BarData(label = "Sat", value = 5f),
                ),
            )

            // Scrollable (12 bars, over threshold)
            TerminalBarChart(
                title = "Monthly Errors",
                description = "Error count by month",
                bars = listOf(
                    BarData(label = "Jan", value = 45f),
                    BarData(label = "Feb", value = 32f),
                    BarData(label = "Mar", value = 58f),
                    BarData(label = "Apr", value = 41f),
                    BarData(label = "May", value = 27f),
                    BarData(label = "Jun", value = 63f, highlight = true),
                    BarData(label = "Jul", value = 55f),
                    BarData(label = "Aug", value = 38f),
                    BarData(label = "Sep", value = 49f),
                    BarData(label = "Oct", value = 33f),
                    BarData(label = "Nov", value = 44f),
                    BarData(label = "Dec", value = 29f),
                ),
            )
        }
    }
}
