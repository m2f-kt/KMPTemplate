package com.m2f.template.designsystem.components.input

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraPreview
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * A single-value slider in `0f..1f` with a glowing cyan knob (handoff §B3 `.at-range`): a `4dp`
 * pill track on `inset`, a cyan-filled portion up to the thumb, an `18dp` solid-cyan knob ringed by
 * a soft cyan halo (`0 0 0 4px rgba(0,229,255,0.14)`). Solid cyan, **not** a gradient. Foundation
 * only — Canvas for the track/knob, pointer gestures for drag + tap-to-position. Min/max ends are
 * labelled (mono, `textFaint`), e.g. **Casual ↔ Formal** / **Verbatim ↔ Tighten**.
 *
 * @param value current position in `0f..1f`.
 * @param onValueChange invoked with the new clamped position on drag/tap.
 * @param startLabel left-end label (the `0f` extreme).
 * @param endLabel right-end label (the `1f` extreme).
 */
@Composable
fun AuraRangeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    startLabel: String,
    endLabel: String,
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    val thumb = 18.dp
    val trackHeight = 4.dp

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumb)
                .pointerInput(Unit) {
                    val half = thumb.toPx() / 2f
                    detectTapGestures { offset ->
                        val span = (size.width - thumb.toPx()).coerceAtLeast(1f)
                        onValueChange(((offset.x - half) / span).coerceIn(0f, 1f))
                    }
                }
                .pointerInput(Unit) {
                    val half = thumb.toPx() / 2f
                    detectHorizontalDragGestures { change, _ ->
                        val span = (size.width - thumb.toPx()).coerceAtLeast(1f)
                        onValueChange(((change.position.x - half) / span).coerceIn(0f, 1f))
                    }
                },
        ) {
            val cy = size.height / 2f
            val thumbPx = thumb.toPx()
            val left = thumbPx / 2f
            val right = size.width - thumbPx / 2f
            val tx = left + value.coerceIn(0f, 1f) * (right - left)
            val trackPx = trackHeight.toPx()
            // Track (resting) then the cyan fill up to the knob.
            drawLine(colors.inset, Offset(left, cy), Offset(right, cy), trackPx, StrokeCap.Round)
            drawLine(colors.neonCyan, Offset(left, cy), Offset(tx, cy), trackPx, StrokeCap.Round)
            // Knob: a soft cyan ring/halo, then the solid cyan knob.
            drawCircle(colors.neonCyan.copy(alpha = 0.14f), thumbPx * 0.95f, Offset(tx, cy))
            drawCircle(colors.neonCyan.copy(alpha = 0.6f), thumbPx * 0.62f, Offset(tx, cy))
            drawCircle(colors.neonCyan, thumbPx / 2f, Offset(tx, cy))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BasicText(
                text = startLabel,
                style = typography.eyebrow.copy(color = colors.textFaint, fontWeight = FontWeight.Medium),
            )
            BasicText(
                text = endLabel,
                style = typography.eyebrow.copy(color = colors.textFaint, fontWeight = FontWeight.Medium),
            )
        }
    }
}

@AuraPreview
@Composable
private fun AuraRangeSliderPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AuraRangeSlider(value = 0.7f, onValueChange = {}, startLabel = "CASUAL", endLabel = "FORMAL")
            AuraRangeSlider(value = 0.3f, onValueChange = {}, startLabel = "VERBATIM", endLabel = "TIGHTEN")
        }
    }
}
