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
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.AuraButton
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.AuraAlert
import com.m2f.template.designsystem.components.input.AuraInput
import com.m2f.template.designsystem.theme.AuraTheme
import org.jetbrains.compose.resources.stringResource
import template.app.auth.generated.resources.Res
import template.app.auth.generated.resources.common_brand_name
import template.app.auth.generated.resources.common_brand_prompt
import template.app.auth.generated.resources.forgot_back_arrow
import template.app.auth.generated.resources.forgot_button
import template.app.auth.generated.resources.forgot_button_loading
import template.app.auth.generated.resources.forgot_email_label
import template.app.auth.generated.resources.forgot_email_placeholder
import template.app.auth.generated.resources.forgot_login_link
import template.app.auth.generated.resources.forgot_subtitle
import template.app.auth.generated.resources.forgot_success_message
import template.app.auth.generated.resources.forgot_title

/**
 * Forgot password screen with centered card layout.
 *
 * Accepts email input and shows success state after submission.
 * Uses only Foundation-level AuraTheme design system components.
 */
@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordModel,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

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
                AuraText(
                    text = stringResource(Res.string.common_brand_prompt),
                    style = typography.md.copy(fontWeight = FontWeight.Bold),
                    color = colors.accent,
                )
                AuraText(
                    text = stringResource(Res.string.common_brand_name),
                    style = typography.md.copy(fontWeight = FontWeight.Medium),
                    color = colors.text,
                )
            }

            // Title
            AuraText(
                text = stringResource(Res.string.forgot_title),
                style = typography.xxl.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )

            // Description
            AuraText(
                text = stringResource(Res.string.forgot_subtitle),
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
                        AuraAlert(
                            message = stringResource(Res.string.forgot_success_message),
                            variant = AlertVariant.Success,
                        )
                    }

                    // Server error alert
                    if (state.serverError != null) {
                        AuraAlert(
                            message = resolveStringKey(state.serverError),
                            variant = AlertVariant.Error,
                        )
                    }

                    // Email input
                    if (!state.emailSent) {
                        AuraInput(
                            value = state.email,
                            onValueChange = onEmailChange,
                            label = stringResource(Res.string.forgot_email_label),
                            placeholder = stringResource(Res.string.forgot_email_placeholder),
                            isError = state.emailError != null,
                            errorMessage = state.emailError?.let { resolveStringKey(it) },
                        )

                        // Submit button
                        AuraButton(
                            text = if (state.isLoading) stringResource(Res.string.forgot_button_loading) else stringResource(Res.string.forgot_button),
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
                AuraText(
                    text = stringResource(Res.string.forgot_back_arrow),
                    style = typography.sm.copy(fontWeight = FontWeight.Bold),
                    color = colors.accent,
                )
                BasicText(
                    text = stringResource(Res.string.forgot_login_link),
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
