package com.m2f.template.designsystem.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * A terminal-styled text input composable with optional label, placeholder, error state,
 * and leading/trailing icon slots.
 *
 * Uses Foundation [BasicTextField] exclusively -- no Material3 dependencies.
 * All styling tokens are read from [TerminalTheme].
 *
 * @param value The current text value of the input.
 * @param onValueChange Callback invoked when the text changes.
 * @param modifier Modifier applied to the root Column container.
 * @param label Optional label text displayed above the input field.
 * @param placeholder Placeholder text shown when [value] is empty.
 * @param enabled Whether the input is editable.
 * @param isError Whether the input is in an error state (changes border to error color).
 * @param errorMessage Optional error message displayed below the input when [isError] is true.
 * @param leadingIcon Optional composable rendered at the start of the input field.
 * @param trailingIcon Optional composable rendered at the end of the input field.
 */
@Composable
fun TerminalInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val radius = TerminalTheme.radius
    val spacing = TerminalTheme.spacing
    val gap = TerminalTheme.gap
    val borders = TerminalTheme.borders
    val opacity = TerminalTheme.opacity

    val borderColor = if (isError) colors.error else colors.border
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

        // Input field
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            textStyle = typography.sm.copy(color = colors.text),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .border(borders.thin, borderColor, shape)
                        .background(colors.surface)
                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (leadingIcon != null) {
                            leadingIcon()
                            Spacer(modifier = Modifier.padding(end = gap.sm))
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                BasicText(
                                    text = placeholder,
                                    style = typography.sm.copy(color = colors.textDim),
                                )
                            }
                            innerTextField()
                        }

                        if (trailingIcon != null) {
                            Spacer(modifier = Modifier.padding(start = gap.sm))
                            trailingIcon()
                        }
                    }
                }
            },
        )

        // Error message
        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(gap.xs))
            BasicText(
                text = errorMessage,
                style = typography.xs.copy(color = colors.error),
            )
        }
    }
}
