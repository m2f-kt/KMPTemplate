package com.m2f.template.designsystem.components.feedback

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.designsystem.theme.TerminalPreview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

/**
 * A terminal-styled progress bar component supporting determinate and indeterminate states.
 *
 * When [progress] is non-null (0.0 to 1.0), renders a determinate bar with filled portion.
 * When [progress] is null, renders an indeterminate animated sliding indicator.
 * Reads all styling exclusively from [TerminalTheme] CompositionLocals.
 *
 * @param modifier Modifier applied to the track container.
 * @param progress Determinate progress value (0.0 to 1.0), or null for indeterminate animation.
 */
@Composable
fun TerminalProgress(
    modifier: Modifier = Modifier,
    progress: Float? = null,
) {
    val colors = TerminalTheme.colors
    val radius = TerminalTheme.radius

    val trackHeight = 6.dp
    val shape = RoundedCornerShape(radius.pill)
    val trackColor = colors.inset
    val fillColor = colors.accent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(shape)
            .background(trackColor),
    ) {
        if (progress != null) {
            // Determinate: fill proportional to progress
            val clampedProgress = progress.coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(clampedProgress)
                    .height(trackHeight)
                    .clip(shape)
                    .background(fillColor)
                    .align(Alignment.CenterStart),
            )
        } else {
            // Indeterminate: sliding indicator via Canvas for precise positioning
            val infiniteTransition = rememberInfiniteTransition()
            val offsetFraction by infiniteTransition.animateFloat(
                initialValue = -0.3f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                ),
            )

            val indicatorWidthFraction = 0.3f

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight),
            ) {
                val indicatorWidthPx = size.width * indicatorWidthFraction
                val startX = offsetFraction * size.width
                val cornerRadiusPx = size.height / 2f
                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(startX, 0f),
                    size = Size(indicatorWidthPx, size.height),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                )
            }
        }
    }
}

@TerminalPreview
@Composable
private fun TerminalProgressPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TerminalProgress(progress = 0.3f)
            TerminalProgress(progress = 0.7f)
            TerminalProgress(progress = null)
        }
    }
}
