package com.m2f.template.designsystem.components.feedback

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraPreview
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * An Aura-styled progress bar component supporting determinate and indeterminate states.
 *
 * When [progress] is non-null (0.0 to 1.0), renders a determinate bar with filled portion.
 * When [progress] is null, renders an indeterminate animated sliding indicator.
 * Reads all styling exclusively from [AuraTheme] CompositionLocals.
 *
 * @param modifier Modifier applied to the root container.
 * @param progress Determinate progress value (0.0 to 1.0), or null for indeterminate animation.
 * @param label Optional label text displayed above the track. For determinate mode, a percentage
 *   is shown to the right of the label.
 */
@Composable
fun AuraProgress(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    label: String? = null,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val spacing = AuraTheme.spacing
    val radius = AuraTheme.radius

    val trackHeight = spacing.sm
    val shape = RoundedCornerShape(radius.xs)
    val trackColor = colors.accentMuted
    val fillColor = colors.accent

    Column(modifier = modifier) {
        if (label != null) {
            if (progress != null) {
                // Determinate: label left, percentage right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    BasicText(
                        text = label,
                        style = typography.xs.copy(
                            color = colors.textMuted,
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                    BasicText(
                        text = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
                        style = typography.xs.copy(
                            color = colors.textMuted,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            } else {
                // Indeterminate: label only
                BasicText(
                    text = label,
                    style = typography.xs.copy(
                        color = colors.textMuted,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
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
                    val cornerRadiusPx = radius.xs.toPx()
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(fillColor, fillColor.copy(alpha = 0.5f)),
                            start = Offset(startX, 0f),
                            end = Offset(startX + indicatorWidthPx, 0f),
                        ),
                        topLeft = Offset(startX, 0f),
                        size = Size(indicatorWidthPx, size.height),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    )
                }
            }
        }
    }
}

@AuraPreview
@Composable
private fun AuraProgressPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Determinate with label (Pencil OvoQ4)
            AuraProgress(
                progress = 0.67f,
                label = "downloading...",
            )
            // Indeterminate with label (Pencil YP7h8)
            AuraProgress(
                progress = null,
                label = "compiling assets...",
            )
            // Without label (API completeness)
            AuraProgress(progress = 0.4f)
        }
    }
}
