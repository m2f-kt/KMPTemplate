package com.m2f.template.app.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.TerminalButton
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.input.TerminalInput
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * Forgot password screen with centered card layout.
 *
 * Accepts email input and shows success state after submission.
 * Uses only Foundation-level TerminalTheme design system components.
 */
@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordModel,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Brand header
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TerminalText(
                    text = ">_",
                    style = typography.md.copy(fontWeight = FontWeight.Bold),
                    color = colors.accent,
                )
                TerminalText(
                    text = "terminal",
                    style = typography.md.copy(fontWeight = FontWeight.Medium),
                    color = colors.text,
                )
            }

            // Title
            TerminalText(
                text = "$ reset_password",
                style = typography.xxl.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )

            // Description
            TerminalText(
                text = "// enter your email to receive a reset link",
                style = typography.sm,
                color = colors.textDim,
            )

            // Card with form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface),
            ) {
                // Accent bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(colors.accent),
                )

                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Success alert
                    if (state.emailSent) {
                        TerminalAlert(
                            message = "Reset link sent. Check your inbox.",
                            variant = AlertVariant.Success,
                        )
                    }

                    // Server error alert
                    if (state.serverError != null) {
                        TerminalAlert(
                            message = state.serverError,
                            variant = AlertVariant.Error,
                        )
                    }

                    // Email input
                    if (!state.emailSent) {
                        TerminalInput(
                            value = state.email,
                            onValueChange = onEmailChange,
                            label = "email",
                            placeholder = "user@example.com",
                            isError = state.emailError != null,
                            errorMessage = state.emailError,
                        )

                        // Submit button
                        TerminalButton(
                            text = if (state.isLoading) "$ sending..." else "$ send_reset_link()",
                            onClick = onSubmit,
                            modifier = Modifier.fillMaxWidth(),
                            variant = ButtonVariant.Default,
                            enabled = !state.isLoading,
                        )
                    }
                }
            }

            // Back to login link
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                TerminalText(
                    text = "<-",
                    style = typography.sm.copy(fontWeight = FontWeight.Bold),
                    color = colors.accent,
                )
                BasicText(
                    text = "back to login",
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onBackToLogin,
                    ),
                    style = typography.sm.copy(
                        color = colors.accent,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }
}
