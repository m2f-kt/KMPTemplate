package com.m2f.template.designsystem.components.input

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.m2f.template.designsystem.theme.AuraPreview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * An Aura-styled text input composable with optional label, placeholder, error state,
 * and trailing icon slot.
 *
 * Renders a ">" prefix inside the input that is muted when the field is empty and
 * [AuraTheme.colors.success] (green) when the field has content. The border disappears
 * when the field has content (unless in error state).
 *
 * Uses Foundation [BasicTextField] exclusively -- no Material3 dependencies.
 * All styling tokens are read from [AuraTheme].
 *
 * @param value The current text value of the input.
 * @param onValueChange Callback invoked when the text changes.
 * @param modifier Modifier applied to the root Column container.
 * @param label Optional label text displayed above the input field.
 * @param placeholder Placeholder text shown when [value] is empty.
 * @param enabled Whether the input is editable.
 * @param isError Whether the input is in an error state (changes border to error color).
 * @param errorMessage Optional error message displayed below the input when [isError] is true.
 * @param visualTransformation Transforms the visual representation of the input (e.g. password masking).
 * @param trailingIcon Optional composable rendered at the end of the input field.
 */
@Composable
fun AuraInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val radius = AuraTheme.radius
    val spacing = AuraTheme.spacing
    val gap = AuraTheme.gap
    val borders = AuraTheme.borders
    val opacity = AuraTheme.opacity

    val showBorder = isError || value.isEmpty()
    val borderColor = if (isError) colors.error else colors.border
    val shape = RoundedCornerShape(radius.sm)
    val alphaValue = if (enabled) opacity.full else opacity.medium

    // Prefix ">" color: success (green) when filled, textMuted when empty
    val prefixColor = if (value.isNotEmpty()) colors.success else colors.textMuted

    val selectionColors = TextSelectionColors(
        handleColor = colors.accent,
        backgroundColor = colors.accent.copy(alpha = 0.3f),
    )

    Column(modifier = modifier.alpha(alphaValue)) {
        // Label
        if (label != null) {
            BasicText(
                text = label,
                style = typography.xs.copy(
                    color = colors.textMuted,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Input field
        CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            textStyle = typography.base.copy(color = colors.text),
            cursorBrush = SolidColor(colors.text),
            singleLine = true,
            visualTransformation = visualTransformation,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .then(
                            if (showBorder) {
                                Modifier.border(borders.thin, borderColor, shape)
                            } else {
                                Modifier
                            },
                        )
                        .background(colors.surface)
                        .padding(horizontal = spacing.lg, vertical = spacing.md),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(gap.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Aura ">" prefix
                        BasicText(
                            text = ">",
                            style = typography.base.copy(
                                fontWeight = FontWeight.Bold,
                                color = prefixColor,
                            ),
                        )

                        // Content area (text field + placeholder)
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                BasicText(
                                    text = placeholder,
                                    style = typography.base.copy(color = colors.textDim),
                                )
                            }
                            innerTextField()
                        }

                        // Trailing icon
                        if (trailingIcon != null) {
                            trailingIcon()
                        }
                    }
                }
            },
        )
        }

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

/**
 * An Aura-styled password input that wraps [AuraInput] with password
 * masking toggle functionality.
 *
 * Includes a Canvas-based eye icon that toggles between masked (eye with slash)
 * and visible (eye with pupil) states.
 *
 * @param value The current password text value.
 * @param onValueChange Callback invoked when the password text changes.
 * @param modifier Modifier applied to the root container.
 * @param label Optional label text displayed above the input field.
 * @param placeholder Placeholder text shown when [value] is empty.
 * @param enabled Whether the input is editable.
 * @param isError Whether the input is in an error state.
 * @param errorMessage Optional error message displayed below the input when [isError] is true.
 */
@Composable
fun AuraPasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val colors = AuraTheme.colors

    AuraInput(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        isError = isError,
        errorMessage = errorMessage,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { passwordVisible = !passwordVisible },
                    ),
            ) {
                EyeIcon(
                    open = passwordVisible,
                    color = colors.textDim,
                    modifier = Modifier.size(16.dp),
                )
            }
        },
    )
}

/**
 * Canvas-based eye icon for password visibility toggle.
 *
 * When [open], renders an eye outline with a filled pupil circle.
 * When closed, renders the eye outline with a diagonal slash line.
 *
 * Consistent with how [com.m2f.template.designsystem.components.selection.AuraCheckbox]
 * and [com.m2f.template.designsystem.components.selection.AuraRadio] draw their icons
 * using Canvas.
 */
@Composable
private fun EyeIcon(
    open: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        val centerY = h * 0.5f

        // Eye outline: almond/lens shape using cubic bezier curves
        val eyePath = androidx.compose.ui.graphics.Path().apply {
            // Left point
            moveTo(w * 0.1f, centerY)
            // Top arc (curves upward)
            cubicTo(
                w * 0.25f, h * 0.2f,
                w * 0.75f, h * 0.2f,
                w * 0.9f, centerY,
            )
            // Bottom arc (curves downward, back to left)
            cubicTo(
                w * 0.75f, h * 0.8f,
                w * 0.25f, h * 0.8f,
                w * 0.1f, centerY,
            )
            close()
        }

        drawPath(
            path = eyePath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        if (open) {
            // Pupil: filled circle in center
            drawCircle(
                color = color,
                radius = w * 0.1f,
                center = Offset(w * 0.5f, centerY),
            )
        } else {
            // Slash line: diagonal line through the eye
            drawLine(
                color = color,
                start = Offset(w * 0.25f, h * 0.25f),
                end = Offset(w * 0.75f, h * 0.75f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@AuraPreview
@Composable
private fun AuraInputPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Empty input (shows border, muted prefix)
            AuraInput(
                value = "",
                onValueChange = {},
                label = "username",
                placeholder = "Enter text...",
            )
            // Filled input (no border, green prefix)
            AuraInput(
                value = "Hello",
                onValueChange = {},
                label = "username",
            )
            // Error state
            AuraInput(
                value = "bad@",
                onValueChange = {},
                label = "Email",
                isError = true,
                errorMessage = "Invalid email",
            )
            // Password masked (eye-off icon)
            AuraPasswordInput(
                value = "mySecretP4ss",
                onValueChange = {},
                label = "password",
                placeholder = "Enter password...",
            )
            // Password empty
            AuraPasswordInput(
                value = "",
                onValueChange = {},
                label = "password",
                placeholder = "Enter password...",
            )
        }
    }
}
