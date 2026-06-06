package com.m2f.template.app.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.AuraButton
import com.m2f.template.designsystem.components.display.AuraDivider
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.AuraAlert
import com.m2f.template.designsystem.components.feedback.AuraBadge
import com.m2f.template.designsystem.components.input.AuraInput
import com.m2f.template.designsystem.components.input.AuraPasswordInput
import com.m2f.template.designsystem.components.selection.AuraCheckbox
import com.m2f.template.designsystem.theme.AuraTheme
import org.jetbrains.compose.resources.stringResource
import template.app.auth.generated.resources.Res
import template.app.auth.generated.resources.common_brand_name
import template.app.auth.generated.resources.common_brand_prompt
import template.app.auth.generated.resources.common_or
import template.app.auth.generated.resources.login_brand_prompt
import template.app.auth.generated.resources.login_brand_quote_1
import template.app.auth.generated.resources.login_brand_quote_2
import template.app.auth.generated.resources.login_brand_quote_author
import template.app.auth.generated.resources.login_brand_status
import template.app.auth.generated.resources.login_brand_status_operational
import template.app.auth.generated.resources.login_brand_version
import template.app.auth.generated.resources.login_button
import template.app.auth.generated.resources.login_button_loading
import template.app.auth.generated.resources.login_command
import template.app.auth.generated.resources.login_email_label
import template.app.auth.generated.resources.login_email_placeholder
import template.app.auth.generated.resources.login_forgot_password
import template.app.auth.generated.resources.login_form_subtitle
import template.app.auth.generated.resources.login_form_title
import template.app.auth.generated.resources.login_google
import template.app.auth.generated.resources.login_apple
import template.app.auth.generated.resources.login_password_label
import template.app.auth.generated.resources.login_remember_me
import template.app.auth.generated.resources.login_signup_link
import template.app.auth.generated.resources.login_signup_prompt
import template.app.auth.generated.resources.login_title
import template.app.auth.generated.resources.login_subtitle

/**
 * Login screen composable with responsive desktop/mobile layouts.
 *
 * Desktop (>840dp): Split layout with brand panel left and login form right.
 * Mobile (<=840dp): Centered card layout with compact form.
 *
 * Uses only Foundation-level AuraTheme design system components.
 * Matches Pencil design xNUU3 (desktop) and 9UXn1 (mobile).
 */
@Composable
fun LoginScreen(
    state: LoginModel,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    val colors = AuraTheme.colors

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        if (maxWidth > 840.dp) {
            LoginDesktopLayout(
                state = state,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onRememberMeChange = onRememberMeChange,
                onLoginClick = onLoginClick,
                onGoogleClick = onGoogleClick,
                onAppleClick = onAppleClick,
                onForgotPassword = onForgotPassword,
                onRegister = onRegister,
            )
        } else {
            LoginMobileLayout(
                state = state,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onRememberMeChange = onRememberMeChange,
                onLoginClick = onLoginClick,
                onGoogleClick = onGoogleClick,
                onAppleClick = onAppleClick,
                onForgotPassword = onForgotPassword,
                onRegister = onRegister,
            )
        }
    }
}

// region Desktop Layout

@Composable
private fun LoginDesktopLayout(
    state: LoginModel,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    val colors = AuraTheme.colors

    Row(modifier = Modifier.fillMaxSize()) {
        // Left brand panel
        LoginBrandPanel(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )

        // Right form panel (520dp fixed width, border-left)
        val borderColor = colors.border
        Column(
            modifier = Modifier
                .width(520.dp)
                .fillMaxHeight()
                .drawBehind {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 64.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            LoginFormContent(
                state = state,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onRememberMeChange = onRememberMeChange,
                onLoginClick = onLoginClick,
                onGoogleClick = onGoogleClick,
                onAppleClick = onAppleClick,
                onForgotPassword = onForgotPassword,
                onRegister = onRegister,
            )
        }
    }
}

@Composable
private fun LoginBrandPanel(modifier: Modifier = Modifier) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Column(
        modifier = modifier
            .background(colors.surface)
            .padding(48.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top: Logo section
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuraText(
                text = stringResource(Res.string.login_brand_prompt),
                style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                color = colors.accent,
            )
            AuraText(
                text = stringResource(Res.string.common_brand_name),
                style = typography.md.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                color = colors.text,
            )
        }

        // Middle: ASCII art + quote
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            // Status line
            AuraText(
                text = stringResource(Res.string.login_brand_status),
                style = typography.xs,
                color = colors.textDim,
            )

            // ASCII art block
            Column {
                val dimColor = colors.textDim
                val accentColor = colors.accent
                val successColor = colors.success
                val textColor = colors.text

                AuraText(
                    text = "\u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510",
                    style = typography.base,
                    color = dimColor,
                )
                AuraText(
                    text = "\u2502  $ ssh user@system              \u2502",
                    style = typography.base,
                    color = accentColor,
                )
                AuraText(
                    text = "\u2502  connecting...                  \u2502",
                    style = typography.base,
                    color = dimColor,
                )
                AuraText(
                    text = "\u2502  connection established \u2713       \u2502",
                    style = typography.base,
                    color = successColor,
                )
                AuraText(
                    text = "\u2502  welcome to the system.         \u2502",
                    style = typography.base,
                    color = textColor,
                )
                AuraText(
                    text = "\u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518",
                    style = typography.base,
                    color = dimColor,
                )
            }

            // Quote block
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuraText(
                    text = stringResource(Res.string.login_brand_quote_1),
                    style = typography.md,
                    color = colors.textDim,
                )
                AuraText(
                    text = stringResource(Res.string.login_brand_quote_2),
                    style = typography.md,
                    color = colors.textDim,
                )
                AuraText(
                    text = stringResource(Res.string.login_brand_quote_author),
                    style = typography.sm.copy(fontWeight = FontWeight.Medium),
                    color = colors.accent,
                )
            }
        }

        // Bottom: Footer with version and status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuraText(
                text = stringResource(Res.string.login_brand_version),
                style = typography.xs,
                color = colors.textDim,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Canvas(modifier = Modifier.size(6.dp)) {
                    drawCircle(color = colors.success)
                }
                AuraText(
                    text = stringResource(Res.string.login_brand_status_operational),
                    style = typography.xs,
                    color = colors.textDim,
                )
            }
        }
    }
}

// endregion

// region Mobile Layout

@Composable
private fun LoginMobileLayout(
    state: LoginModel,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        // Header: brand + subtitle
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
        Spacer(modifier = Modifier.height(4.dp))
        AuraText(
            text = stringResource(Res.string.login_command),
            style = typography.xs,
            color = colors.textDim,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title block
        AuraText(
            text = stringResource(Res.string.login_title),
            style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
            color = colors.text,
        )
        Spacer(modifier = Modifier.height(4.dp))
        AuraText(
            text = stringResource(Res.string.login_subtitle),
            style = typography.sm,
            color = colors.textDim,
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Card with accent bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface),
        ) {
            // Accent bar (green top strip)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(colors.accent),
            )

            // Form content inside card
            Column(modifier = Modifier.padding(24.dp)) {
                LoginFormContent(
                    state = state,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onRememberMeChange = onRememberMeChange,
                    onLoginClick = onLoginClick,
                    onGoogleClick = onGoogleClick,
                    onAppleClick = onAppleClick,
                    onForgotPassword = onForgotPassword,
                    onRegister = onRegister,
                )
            }
        }
    }
}

// endregion

// region Shared Form Content

@Composable
private fun LoginFormContent(
    state: LoginModel,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Title block (desktop only -- mobile has its own outside the card)
        // Both layouts call this, so we show the form-level header
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AuraText(
                text = stringResource(Res.string.login_form_title),
                style = typography.xxl.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )
            AuraText(
                text = stringResource(Res.string.login_form_subtitle),
                style = typography.sm,
                color = colors.textDim,
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
        AuraInput(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(Res.string.login_email_label),
            placeholder = stringResource(Res.string.login_email_placeholder),
            isError = state.emailError != null,
            errorMessage = state.emailError?.let { resolveStringKey(it) },
            enabled = state.invitationEmail == null && !state.isLoading,
        )

        // Password input
        AuraPasswordInput(
            value = state.password,
            onValueChange = onPasswordChange,
            label = stringResource(Res.string.login_password_label),
            placeholder = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022",
            isError = state.passwordError != null,
            errorMessage = state.passwordError?.let { resolveStringKey(it) },
        )

        // Options row: remember me + forgot password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuraCheckbox(
                checked = state.rememberMe,
                onCheckedChange = onRememberMeChange,
                label = stringResource(Res.string.login_remember_me),
            )

            BasicText(
                text = stringResource(Res.string.login_forgot_password),
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onForgotPassword,
                ),
                style = typography.sm.copy(color = colors.accent),
            )
        }

        // Login button
        AuraButton(
            text = if (state.isLoading) stringResource(Res.string.login_button_loading) else stringResource(Res.string.login_button),
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Default,
            enabled = !state.isLoading,
        )

        // Divider row: line + "or" + line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuraDivider(modifier = Modifier.weight(1f))
            AuraText(
                text = stringResource(Res.string.common_or),
                style = typography.xs,
                color = colors.textDim,
            )
            AuraDivider(modifier = Modifier.weight(1f))
        }

        // Social buttons
        SocialButtonsRow(
            onGoogleClick = onGoogleClick,
            onAppleClick = onAppleClick,
        )

        // Register link row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            AuraText(
                text = stringResource(Res.string.login_signup_prompt),
                style = typography.sm,
                color = colors.textDim,
            )
            BasicText(
                text = stringResource(Res.string.login_signup_link),
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onRegister,
                ),
                style = typography.sm.copy(
                    color = colors.accent,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

// endregion

// region Shared Components

@Composable
internal fun SocialButtonsRow(
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AuraButton(
            text = stringResource(Res.string.login_google),
            onClick = onGoogleClick,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Secondary,
        )
        if (showAppleSignIn()) {
            AuraButton(
                text = stringResource(Res.string.login_apple),
                onClick = onAppleClick,
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.Secondary,
            )
        }
    }
}

// endregion
