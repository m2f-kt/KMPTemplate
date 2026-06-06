package com.m2f.template.designsystem.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import com.m2f.template.designsystem.theme.AuraTheme
import com.m2f.template.designsystem.theme.AuraPreview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp

/**
 * An Aura-styled multi-line text area composable.
 *
 * Similar to [AuraInput] but supports multiple lines via [minLines].
 * Uses Foundation [BasicTextField] exclusively -- no Material3 dependencies.
 * All styling tokens are read from [AuraTheme].
 *
 * @param value The current text value of the textarea.
 * @param onValueChange Callback invoked when the text changes.
 * @param modifier Modifier applied to the root Column container.
 * @param label Optional label text displayed above the textarea.
 * @param placeholder Placeholder text shown when [value] is empty.
 * @param enabled Whether the textarea is editable.
 * @param minLines Minimum number of visible text lines (defaults to 3).
 */
@Composable
fun AuraTextarea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
    minLines: Int = 3,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val radius = AuraTheme.radius
    val spacing = AuraTheme.spacing
    val gap = AuraTheme.gap
    val borders = AuraTheme.borders
    val opacity = AuraTheme.opacity

    val shape = RoundedCornerShape(radius.md)
    val alphaValue = if (enabled) opacity.full else opacity.medium

    Column(modifier = modifier.alpha(alphaValue)) {
        // Label
        if (label != null) {
            BasicText(
                text = label,
                style = typography.xs.copy(color = colors.textMuted),
            )
            Spacer(modifier = Modifier.height(gap.xs))
        }

        // Multi-line text field
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            textStyle = typography.sm.copy(color = colors.text),
            minLines = minLines,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .border(borders.thin, colors.border, shape)
                        .background(colors.surface)
                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                ) {
                    if (value.isEmpty()) {
                        BasicText(
                            text = placeholder,
                            style = typography.sm.copy(color = colors.textDim),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@AuraPreview
@Composable
private fun AuraTextareaPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraTextarea(
                value = "",
                onValueChange = {},
                label = "Description",
                placeholder = "Enter description...",
            )
            AuraTextarea(
                value = "Line one\nLine two\nLine three",
                onValueChange = {},
                label = "Notes",
            )
        }
    }
}
