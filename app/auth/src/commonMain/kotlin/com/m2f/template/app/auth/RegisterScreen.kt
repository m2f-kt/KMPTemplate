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
import org.jetbrains.compose.resources.stringResource
import template.app.auth.generated.resources.Res
import template.app.auth.generated.resources.common_brand_name
import template.app.auth.generated.resources.common_brand_prompt
import template.app.auth.generated.resources.common_or
import template.app.auth.generated.resources.login_brand_prompt
import template.app.auth.generated.resources.register_brand_feature_fast_desc
import template.app.auth.generated.resources.register_brand_feature_fast_title
import template.app.auth.generated.resources.register_brand_feature_scale_desc
import template.app.auth.generated.resources.register_brand_feature_scale_title
import template.app.auth.generated.resources.register_brand_feature_secure_desc
import template.app.auth.generated.resources.register_brand_feature_secure_title
import template.app.auth.generated.resources.register_brand_tagline_1
import template.app.auth.generated.resources.register_brand_tagline_2
import template.app.auth.generated.resources.register_brand_tagline_3
import template.app.auth.generated.resources.register_button
import template.app.auth.generated.resources.register_button_loading
import template.app.auth.generated.resources.register_command
import template.app.auth.generated.resources.register_confirm_password_label
import template.app.auth.generated.resources.register_confirm_password_placeholder
import template.app.auth.generated.resources.register_email_label
import template.app.auth.generated.resources.register_email_placeholder
import template.app.auth.generated.resources.register_first_name_label
import template.app.auth.generated.resources.register_first_name_placeholder
import template.app.auth.generated.resources.register_form_subtitle
import template.app.auth.generated.resources.register_form_title
import template.app.auth.generated.resources.register_last_name_label
import template.app.auth.generated.resources.register_last_name_placeholder
import template.app.auth.generated.resources.register_login_link
import template.app.auth.generated.resources.register_login_prompt
import template.app.auth.generated.resources.register_password_label
import template.app.auth.generated.resources.register_password_placeholder
import template.app.auth.generated.resources.register_subtitle
import template.app.auth.generated.resources.register_terms_text
import template.app.auth.generated.resources.register_title

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
                text = stringResource(Res.string.login_brand_prompt),
                style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                color = colors.accent,
            )
            TerminalText(
                text = stringResource(Res.string.common_brand_name),
                style = typography.md.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                color = colors.text,
            )
        }

        // Middle: Tagline + feature bullets
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            // Tagline
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TerminalText(
                    text = stringResource(Res.string.register_brand_tagline_1),
                    style = typography.xxl.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
                TerminalText(
                    text = stringResource(Res.string.register_brand_tagline_2),
                    style = typography.xxl.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
                TerminalText(
                    text = stringResource(Res.string.register_brand_tagline_3),
                    style = typography.xxl.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    color = colors.accent,
                )
            }

            // Feature bullets
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                FeatureBullet(
                    icon = "\u26A1",
                    title = stringResource(Res.string.register_brand_feature_fast_title),
                    description = stringResource(Res.string.register_brand_feature_fast_desc),
                )
                FeatureBullet(
                    icon = "\u26E8",
                    title = stringResource(Res.string.register_brand_feature_secure_title),
                    description = stringResource(Res.string.register_brand_feature_secure_desc),
                )
                FeatureBullet(
                    icon = "\u2316",
                    title = stringResource(Res.string.register_brand_feature_scale_title),
                    description = stringResource(Res.string.register_brand_feature_scale_desc),
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
                text = stringResource(Res.string.common_brand_prompt),
                style = typography.md.copy(fontWeight = FontWeight.Bold),
                color = colors.accent,
            )
            TerminalText(
                text = stringResource(Res.string.common_brand_name),
                style = typography.md.copy(fontWeight = FontWeight.Medium),
                color = colors.text,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        TerminalText(
            text = stringResource(Res.string.register_command),
            style = typography.xs,
            color = colors.textDim,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title block
        TerminalText(
            text = stringResource(Res.string.register_title),
            style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
            color = colors.text,
        )
        Spacer(modifier = Modifier.height(4.dp))
        TerminalText(
            text = stringResource(Res.string.register_subtitle),
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
                text = stringResource(Res.string.register_form_title),
                style = typography.xxl.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )
            TerminalText(
                text = stringResource(Res.string.register_form_subtitle),
                style = typography.sm,
                color = colors.textDim,
            )
        }

        // Server error alert
        if (state.serverError != null) {
            TerminalAlert(
                message = resolveStringKey(state.serverError),
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
                text = stringResource(Res.string.common_or),
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
                label = stringResource(Res.string.register_first_name_label),
                placeholder = stringResource(Res.string.register_first_name_placeholder),
                isError = firstNameError != null,
                errorMessage = firstNameError?.let { resolveStringKey(it) },
            )
            TerminalInput(
                value = state.lastName,
                onValueChange = onLastNameChange,
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.register_last_name_label),
                placeholder = stringResource(Res.string.register_last_name_placeholder),
                isError = lastNameError != null,
                errorMessage = lastNameError?.let { resolveStringKey(it) },
            )
        }

        // Email
        TerminalInput(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(Res.string.register_email_label),
            placeholder = stringResource(Res.string.register_email_placeholder),
            isError = emailError != null,
            errorMessage = emailError?.let { resolveStringKey(it) },
        )

        // Password
        TerminalPasswordInput(
            value = state.password,
            onValueChange = onPasswordChange,
            label = stringResource(Res.string.register_password_label),
            placeholder = stringResource(Res.string.register_password_placeholder),
            isError = passwordError != null,
            errorMessage = passwordError?.let { resolveStringKey(it) },
        )

        // Confirm password
        TerminalPasswordInput(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = stringResource(Res.string.register_confirm_password_label),
            placeholder = stringResource(Res.string.register_confirm_password_placeholder),
            isError = confirmPasswordError != null,
            errorMessage = confirmPasswordError?.let { resolveStringKey(it) },
        )

        // Terms checkbox
        Column {
            TerminalCheckbox(
                checked = state.termsAccepted,
                onCheckedChange = onTermsAcceptedChange,
                label = stringResource(Res.string.register_terms_text),
            )
            if (termsError != null) {
                val colors = TerminalTheme.colors
                val typography = TerminalTheme.typography
                Spacer(modifier = Modifier.height(4.dp))
                BasicText(
                    text = resolveStringKey(termsError),
                    style = typography.xs.copy(color = colors.error),
                )
            }
        }

        // Register button
        TerminalButton(
            text = if (state.isLoading) stringResource(Res.string.register_button_loading) else stringResource(Res.string.register_button),
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
            text = stringResource(Res.string.register_login_prompt),
            style = typography.sm,
            color = colors.textDim,
        )
        BasicText(
            text = stringResource(Res.string.register_login_link),
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
