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
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.TerminalButton
import com.m2f.template.designsystem.components.display.TerminalDivider
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.components.input.TerminalInput
import com.m2f.template.designsystem.components.input.TerminalPasswordInput
import com.m2f.template.designsystem.components.selection.TerminalCheckbox
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * Login screen composable with responsive desktop/mobile layouts.
 *
 * Desktop (>840dp): Split layout with brand panel left and login form right.
 * Mobile (<=840dp): Centered card layout with compact form.
 *
 * Uses only Foundation-level TerminalTheme design system components.
 * Matches Pencil design xNUU3 (desktop) and 9UXn1 (mobile).
 */
@Composable
fun LoginScreen(
    state: LoginState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    val colors = TerminalTheme.colors

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
    state: LoginState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    val colors = TerminalTheme.colors

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
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

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
            TerminalText(
                text = ">",
                style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                color = colors.accent,
            )
            TerminalText(
                text = "terminal",
                style = typography.md.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                color = colors.text,
            )
        }

        // Middle: ASCII art + quote
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            // Status line
            TerminalText(
                text = "$ uptime: 99.98%  |  latency: 12ms  |  nodes: 42",
                style = typography.xs,
                color = colors.textDim,
            )

            // ASCII art block
            Column {
                val dimColor = colors.textDim
                val accentColor = colors.accent
                val successColor = colors.success
                val textColor = colors.text

                TerminalText(
                    text = "\u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510",
                    style = typography.base,
                    color = dimColor,
                )
                TerminalText(
                    text = "\u2502  $ ssh user@system              \u2502",
                    style = typography.base,
                    color = accentColor,
                )
                TerminalText(
                    text = "\u2502  connecting...                  \u2502",
                    style = typography.base,
                    color = dimColor,
                )
                TerminalText(
                    text = "\u2502  connection established \u2713       \u2502",
                    style = typography.base,
                    color = successColor,
                )
                TerminalText(
                    text = "\u2502  welcome to the system.         \u2502",
                    style = typography.base,
                    color = textColor,
                )
                TerminalText(
                    text = "\u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518",
                    style = typography.base,
                    color = dimColor,
                )
            }

            // Quote block
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TerminalText(
                    text = "// the best way to predict the future",
                    style = typography.md,
                    color = colors.textDim,
                )
                TerminalText(
                    text = "// is to build it.",
                    style = typography.md,
                    color = colors.textDim,
                )
                TerminalText(
                    text = "\u2014 alan_kay",
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
            TerminalText(
                text = "v2.4.1",
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
                TerminalText(
                    text = "all_systems_operational",
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
    state: LoginState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
    ) {
        // Header: brand + subtitle
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
        Spacer(modifier = Modifier.height(4.dp))
        TerminalText(
            text = "$ authenticate --user",
            style = typography.xs,
            color = colors.textDim,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title block
        TerminalText(
            text = "Welcome back",
            style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
            color = colors.text,
        )
        Spacer(modifier = Modifier.height(4.dp))
        TerminalText(
            text = "Sign in to your workspace",
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
    state: LoginState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Title block (desktop only -- mobile has its own outside the card)
        // Both layouts call this, so we show the form-level header
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TerminalText(
                text = "$ authenticate",
                style = typography.xxl.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )
            TerminalText(
                text = "// enter your credentials to continue",
                style = typography.sm,
                color = colors.textDim,
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
        TerminalInput(
            value = state.email,
            onValueChange = onEmailChange,
            label = "username",
            placeholder = "enter_username",
            isError = state.emailError != null,
            errorMessage = state.emailError,
        )

        // Password input
        TerminalPasswordInput(
            value = state.password,
            onValueChange = onPasswordChange,
            label = "password",
            placeholder = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022",
            isError = state.passwordError != null,
            errorMessage = state.passwordError,
        )

        // Options row: remember me + forgot password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TerminalCheckbox(
                checked = state.rememberMe,
                onCheckedChange = onRememberMeChange,
                label = "--remember-me",
            )

            BasicText(
                text = "$ reset_password",
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onForgotPassword,
                ),
                style = typography.sm.copy(color = colors.accent),
            )
        }

        // Login button
        TerminalButton(
            text = if (state.isLoading) "$ authenticating..." else "$ login()",
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
            TerminalDivider(modifier = Modifier.weight(1f))
            TerminalText(
                text = "or",
                style = typography.xs,
                color = colors.textDim,
            )
            TerminalDivider(modifier = Modifier.weight(1f))
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
            TerminalText(
                text = "// new user?",
                style = typography.sm,
                color = colors.textDim,
            )
            BasicText(
                text = "$ create_account()",
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
        TerminalButton(
            text = "Google",
            onClick = onGoogleClick,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Secondary,
        )
        if (showAppleSignIn()) {
            TerminalButton(
                text = "Apple",
                onClick = onAppleClick,
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.Secondary,
            )
        }
    }
}

// endregion
