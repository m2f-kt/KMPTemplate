package com.m2f.template.designsystem.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.m2f.template.designsystem.theme.TerminalTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp

/**
 * Semantic alert variants mapping to theme color pairs.
 */
enum class AlertVariant {
    Info,
    Success,
    Warning,
    Error,
}

/**
 * A terminal-styled alert component that displays status messages with semantic color coding.
 *
 * Renders a full-width banner with a variant-colored left accent border and background tint.
 * Reads all styling exclusively from [TerminalTheme] CompositionLocals.
 *
 * @param message The alert body text.
 * @param modifier Modifier applied to the root layout.
 * @param variant Semantic variant controlling color scheme.
 * @param title Optional bold title displayed above the message.
 * @param onDismiss Optional dismiss callback; when provided, an "X" close control is shown.
 */
@Composable
fun TerminalAlert(
    message: String,
    modifier: Modifier = Modifier,
    variant: AlertVariant = AlertVariant.Info,
    title: String? = null,
    onDismiss: (() -> Unit)? = null,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val radius = TerminalTheme.radius
    val spacing = TerminalTheme.spacing
    val borders = TerminalTheme.borders
    val gap = TerminalTheme.gap

    val (bgColor, fgColor) = when (variant) {
        AlertVariant.Info -> colors.infoBg to colors.info
        AlertVariant.Success -> colors.successBg to colors.success
        AlertVariant.Warning -> colors.warningBg to colors.warning
        AlertVariant.Error -> colors.errorBg to colors.error
    }

    val accentWidth = borders.thick
    val shape = RoundedCornerShape(radius.md)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bgColor)
            .drawBehind {
                drawRect(
                    color = fgColor,
                    topLeft = Offset.Zero,
                    size = size.copy(width = accentWidth.toPx()),
                )
            }
            .padding(
                start = spacing.lg,
                top = spacing.md,
                end = spacing.md,
                bottom = spacing.md,
            ),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (title != null) {
                BasicText(
                    text = title,
                    style = typography.sm.copy(
                        color = fgColor,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Box(modifier = Modifier.padding(bottom = gap.xs))
            }
            BasicText(
                text = message,
                style = typography.sm.copy(color = fgColor),
            )
        }

        if (onDismiss != null) {
            Spacer(modifier = Modifier.width(gap.sm))
            BasicText(
                text = "\u00D7",
                modifier = Modifier
                    .clickable(onClick = onDismiss)
                    .padding(spacing.xs),
                style = typography.base.copy(
                    color = fgColor,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun TerminalAlertPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TerminalAlert(
                message = "This is an informational message.",
                variant = AlertVariant.Info,
                title = "Info",
                onDismiss = {},
            )
            TerminalAlert(
                message = "Operation completed successfully.",
                variant = AlertVariant.Success,
                title = "Success",
            )
            TerminalAlert(
                message = "Please review before proceeding.",
                variant = AlertVariant.Warning,
                title = "Warning",
            )
            TerminalAlert(
                message = "Something went wrong.",
                variant = AlertVariant.Error,
                title = "Error",
            )
        }
    }
}
