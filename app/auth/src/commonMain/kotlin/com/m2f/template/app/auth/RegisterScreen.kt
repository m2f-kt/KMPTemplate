package com.m2f.template.app.auth

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
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.input.TerminalInput
import com.m2f.template.designsystem.components.input.TerminalPasswordInput
import com.m2f.template.designsystem.components.selection.TerminalCheckbox
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * Register screen composable with responsive desktop/mobile layouts.
 *
 * Desktop (>840dp): Split layout with brand panel left and register form right.
 * Mobile (<=840dp): Centered card layout with compact form.
 *
 * Uses accumulated field validation via zipOrAccumulate (multiple errors shown at once).
 * Uses only Foundation-level TerminalTheme design system components.
 * Matches Pencil design B1nWB (desktop) and KXp69 (mobile).
 */
@Composable
fun RegisterScreen(
    state: RegisterModel,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onLogin: () -> Unit,
) {
    val colors = TerminalTheme.colors

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        if (maxWidth > 840.dp) {
            RegisterDesktopLayout(
                state = state,
                onFirstNameChange = onFirstNameChange,
                onLastNameChange = onLastNameChange,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onTermsAcceptedChange = onTermsAcceptedChange,
                onRegisterClick = onRegisterClick,
                onGoogleClick = onGoogleClick,
                onAppleClick = onAppleClick,
                onLogin = onLogin,
            )
        } else {
            RegisterMobileLayout(
                state = state,
                onFirstNameChange = onFirstNameChange,
                onLastNameChange = onLastNameChange,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onTermsAcceptedChange = onTermsAcceptedChange,
                onRegisterClick = onRegisterClick,
                onGoogleClick = onGoogleClick,
                onAppleClick = onAppleClick,
                onLogin = onLogin,
            )
        }
    }
}

// region Desktop Layout

@Composable
private fun RegisterDesktopLayout(
    state: RegisterModel,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onLogin: () -> Unit,
) {
    val colors = TerminalTheme.colors

    Row(modifier = Modifier.fillMaxSize()) {
        // Left brand panel
        RegisterBrandPanel(
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
            RegisterFormContent(
                state = state,
                onFirstNameChange = onFirstNameChange,
                onLastNameChange = onLastNameChange,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onTermsAcceptedChange = onTermsAcceptedChange,
                onRegisterClick = onRegisterClick,
                onGoogleClick = onGoogleClick,
                onAppleClick = onAppleClick,
                onLogin = onLogin,
            )
        }
    }
}

@Composable
private fun RegisterBrandPanel(modifier: Modifier = Modifier) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = modifier
            .background(colors.accentMuted)
            .padding(60.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top: Logo
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

        // Middle: Tagline + feature bullets
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            // Tagline
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TerminalText(
                    text = "Build.",
                    style = typography.xxl.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
                TerminalText(
                    text = "Deploy.",
                    style = typography.xxl.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
                TerminalText(
                    text = "Scale.",
                    style = typography.xxl.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    color = colors.accent,
                )
            }

            // Feature bullets
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                FeatureBullet(
                    icon = "\u26A1",
                    title = "Lightning fast",
                    description = "Sub-millisecond response times with global edge deployment.",
                )
                FeatureBullet(
                    icon = "\u26E8",
                    title = "Secure by default",
                    description = "Enterprise-grade security with zero-trust architecture.",
                )
                FeatureBullet(
                    icon = "\u2316",
                    title = "Global scale",
                    description = "Deploy to 42 regions with automatic failover.",
                )
            }
        }

        // Bottom spacer (SpaceBetween handles this)
        Spacer(modifier = Modifier.height(1.dp))
    }
}

@Composable
private fun FeatureBullet(
    icon: String,
    title: String,
    description: String,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TerminalText(
            text = icon,
            style = typography.md,
            color = colors.accent,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TerminalText(
                text = title,
                style = typography.sm.copy(fontWeight = FontWeight.SemiBold),
                color = colors.text,
            )
            TerminalText(
                text = description,
                style = typography.xs,
                color = colors.textDim,
            )
        }
    }
}

// endregion

// region Mobile Layout

@Composable
private fun RegisterMobileLayout(
    state: RegisterModel,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onLogin: () -> Unit,
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
            text = "$ create_account --new",
            style = typography.xs,
            color = colors.textDim,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title block
        TerminalText(
            text = "Create your account",
            style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
            color = colors.text,
        )
        Spacer(modifier = Modifier.height(4.dp))
        TerminalText(
            text = "Initialize your workspace",
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(colors.accent),
            )
            Column(modifier = Modifier.padding(24.dp)) {
                RegisterFormFields(
                    state = state,
                    onFirstNameChange = onFirstNameChange,
                    onLastNameChange = onLastNameChange,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onConfirmPasswordChange = onConfirmPasswordChange,
                    onTermsAcceptedChange = onTermsAcceptedChange,
                    onRegisterClick = onRegisterClick,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Social buttons
        SocialButtonsRow(
            onGoogleClick = onGoogleClick,
            onAppleClick = onAppleClick,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Login link
        LoginLinkRow(onLogin = onLogin)
    }
}

// endregion

// region Shared Form Content

@Composable
private fun RegisterFormContent(
    state: RegisterModel,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onLogin: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Title block
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TerminalText(
                text = "$ create_account",
                style = typography.xxl.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )
            TerminalText(
                text = "// initialize your workspace",
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

        // Card with accent bar containing form fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(colors.accent),
            )
            Column(
                modifier = Modifier.padding(24.dp),
            ) {
                RegisterFormFields(
                    state = state,
                    onFirstNameChange = onFirstNameChange,
                    onLastNameChange = onLastNameChange,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onConfirmPasswordChange = onConfirmPasswordChange,
                    onTermsAcceptedChange = onTermsAcceptedChange,
                    onRegisterClick = onRegisterClick,
                )
            }
        }

        // Divider + social buttons
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

        SocialButtonsRow(
            onGoogleClick = onGoogleClick,
            onAppleClick = onAppleClick,
        )

        // Login link
        LoginLinkRow(onLogin = onLogin)
    }
}

@Composable
private fun RegisterFormFields(
    state: RegisterModel,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
) {
    val firstNameError = state.fieldErrors["firstName"]
    val lastNameError = state.fieldErrors["lastName"]
    val emailError = state.fieldErrors["email"]
    val passwordError = state.fieldErrors["password"]
    val confirmPasswordError = state.fieldErrors["confirmPassword"]
    val termsError = state.fieldErrors["terms"]

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Name row: firstName + lastName side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TerminalInput(
                value = state.firstName,
                onValueChange = onFirstNameChange,
                modifier = Modifier.weight(1f),
                label = "first_name",
                placeholder = "John",
                isError = firstNameError != null,
                errorMessage = firstNameError,
            )
            TerminalInput(
                value = state.lastName,
                onValueChange = onLastNameChange,
                modifier = Modifier.weight(1f),
                label = "last_name",
                placeholder = "Doe",
                isError = lastNameError != null,
                errorMessage = lastNameError,
            )
        }

        // Email
        TerminalInput(
            value = state.email,
            onValueChange = onEmailChange,
            label = "email",
            placeholder = "user@example.com",
            isError = emailError != null,
            errorMessage = emailError,
        )

        // Password
        TerminalPasswordInput(
            value = state.password,
            onValueChange = onPasswordChange,
            label = "password",
            placeholder = "min. 8 characters",
            isError = passwordError != null,
            errorMessage = passwordError,
        )

        // Confirm password
        TerminalPasswordInput(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "confirm_password",
            placeholder = "re-enter password",
            isError = confirmPasswordError != null,
            errorMessage = confirmPasswordError,
        )

        // Terms checkbox
        Column {
            TerminalCheckbox(
                checked = state.termsAccepted,
                onCheckedChange = onTermsAcceptedChange,
                label = "I accept the terms and conditions",
            )
            if (termsError != null) {
                val colors = TerminalTheme.colors
                val typography = TerminalTheme.typography
                Spacer(modifier = Modifier.height(4.dp))
                BasicText(
                    text = termsError,
                    style = typography.xs.copy(color = colors.error),
                )
            }
        }

        // Register button
        TerminalButton(
            text = if (state.isLoading) "$ registering..." else "$ register()",
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Default,
            enabled = !state.isLoading,
        )
    }
}

@Composable
private fun LoginLinkRow(onLogin: () -> Unit) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        TerminalText(
            text = "// already have an account?",
            style = typography.sm,
            color = colors.textDim,
        )
        BasicText(
            text = "$ login()",
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onLogin,
            ),
            style = typography.sm.copy(
                color = colors.accent,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

// endregion
