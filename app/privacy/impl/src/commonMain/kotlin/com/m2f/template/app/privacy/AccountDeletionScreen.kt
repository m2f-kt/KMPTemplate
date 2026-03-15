package com.m2f.template.app.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.TerminalButton
import com.m2f.template.designsystem.components.display.TerminalDivider
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.feedback.TerminalProgress
import com.m2f.template.designsystem.components.input.TerminalPasswordInput
import com.m2f.template.designsystem.components.input.TerminalTextarea
import com.m2f.template.designsystem.theme.TerminalTheme
import org.jetbrains.compose.resources.stringResource
import template.app.privacy.generated.resources.Res
import template.app.privacy.generated.resources.*

/**
 * Multi-step account deletion flow screen.
 *
 * Desktop (>840dp): Centered card with max width ~500dp.
 * Mobile (<=840dp): Full-width with padding.
 */
@Composable
fun AccountDeletionScreen(
    state: AccountDeletionModel,
    onProceedToReAuth: () -> Unit,
    onReAuthenticate: (String) -> Unit,
    onSetReason: (String) -> Unit,
    onConfirmDeletion: () -> Unit,
    onCancelDeletion: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = TerminalTheme.colors

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        if (maxWidth > 840.dp) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                AccountDeletionContent(
                    state = state,
                    onProceedToReAuth = onProceedToReAuth,
                    onReAuthenticate = onReAuthenticate,
                    onSetReason = onSetReason,
                    onConfirmDeletion = onConfirmDeletion,
                    onCancelDeletion = onCancelDeletion,
                    onBack = onBack,
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .background(colors.surface)
                        .padding(48.dp),
                )
            }
        } else {
            AccountDeletionContent(
                state = state,
                onProceedToReAuth = onProceedToReAuth,
                onReAuthenticate = onReAuthenticate,
                onSetReason = onSetReason,
                onConfirmDeletion = onConfirmDeletion,
                onCancelDeletion = onCancelDeletion,
                onBack = onBack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            )
        }
    }
}

@Composable
private fun AccountDeletionContent(
    state: AccountDeletionModel,
    onProceedToReAuth: () -> Unit,
    onReAuthenticate: (String) -> Unit,
    onSetReason: (String) -> Unit,
    onConfirmDeletion: () -> Unit,
    onCancelDeletion: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Title
        TerminalText(
            text = stringResource(Res.string.privacy_deletion_title),
            style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
            color = colors.error,
        )

        // Loading
        if (state.loading) {
            TerminalProgress(label = stringResource(Res.string.privacy_deletion_processing))
        }

        // Error
        if (state.error != null) {
            TerminalAlert(
                message = state.error.code,
                variant = AlertVariant.Error,
                title = stringResource(Res.string.privacy_deletion_error_title),
            )
        }

        TerminalDivider()

        // Step content
        when (state.step) {
            DeletionStep.WARNING -> WarningStep(
                onContinue = onProceedToReAuth,
                onBack = onBack,
                loading = state.loading,
            )

            DeletionStep.RE_AUTH -> ReAuthStep(
                onReAuthenticate = onReAuthenticate,
                onBack = onBack,
                loading = state.loading,
            )

            DeletionStep.REASON -> ReasonStep(
                reason = state.reason,
                onSetReason = onSetReason,
                loading = state.loading,
            )

            DeletionStep.CONFIRM -> ConfirmStep(
                onConfirmDeletion = onConfirmDeletion,
                onCancel = onBack,
                loading = state.loading,
            )

            DeletionStep.SCHEDULED -> ScheduledStep(
                scheduledAt = state.pendingDeletion?.scheduledAt,
                onCancelDeletion = onCancelDeletion,
            )
        }
    }
}

// region Steps

@Composable
private fun WarningStep(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    loading: Boolean,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    TerminalAlert(
        message = stringResource(Res.string.privacy_deletion_warning_message),
        variant = AlertVariant.Warning,
        title = stringResource(Res.string.privacy_deletion_warning_title),
    )

    Spacer(modifier = Modifier.height(4.dp))

    TerminalText(
        text = stringResource(Res.string.privacy_deletion_warning_consider),
        style = typography.sm.copy(fontWeight = FontWeight.Medium),
        color = colors.text,
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TerminalText(text = stringResource(Res.string.privacy_deletion_warning_bullet1), style = typography.sm, color = colors.textDim)
        TerminalText(text = stringResource(Res.string.privacy_deletion_warning_bullet2), style = typography.sm, color = colors.textDim)
        TerminalText(text = stringResource(Res.string.privacy_deletion_warning_bullet3), style = typography.sm, color = colors.textDim)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TerminalButton(
            text = stringResource(Res.string.privacy_deletion_go_back),
            onClick = onBack,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Secondary,
            enabled = !loading,
        )
        TerminalButton(
            text = stringResource(Res.string.privacy_deletion_continue),
            onClick = onContinue,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Destructive,
            enabled = !loading,
        )
    }
}

@Composable
private fun ReAuthStep(
    onReAuthenticate: (String) -> Unit,
    onBack: () -> Unit,
    loading: Boolean,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    var password by remember { mutableStateOf("") }

    TerminalText(
        text = stringResource(Res.string.privacy_deletion_reauth_message),
        style = typography.sm,
        color = colors.textDim,
    )

    TerminalPasswordInput(
        value = password,
        onValueChange = { password = it },
        label = stringResource(Res.string.privacy_deletion_reauth_label),
        placeholder = stringResource(Res.string.privacy_deletion_reauth_placeholder),
        enabled = !loading,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TerminalButton(
            text = stringResource(Res.string.privacy_deletion_go_back),
            onClick = onBack,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Secondary,
            enabled = !loading,
        )
        TerminalButton(
            text = stringResource(Res.string.privacy_deletion_reauth_verify),
            onClick = { onReAuthenticate(password) },
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Default,
            enabled = password.isNotBlank() && !loading,
        )
    }
}

@Composable
private fun ReasonStep(
    reason: String,
    onSetReason: (String) -> Unit,
    loading: Boolean,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    var localReason by remember(reason) { mutableStateOf(reason) }

    TerminalText(
        text = stringResource(Res.string.privacy_deletion_reason_message),
        style = typography.sm,
        color = colors.textDim,
    )

    TerminalTextarea(
        value = localReason,
        onValueChange = { localReason = it },
        label = stringResource(Res.string.privacy_deletion_reason_label),
        placeholder = stringResource(Res.string.privacy_deletion_reason_placeholder),
        enabled = !loading,
    )

    TerminalButton(
        text = stringResource(Res.string.privacy_deletion_continue),
        onClick = { onSetReason(localReason) },
        modifier = Modifier.fillMaxWidth(),
        variant = ButtonVariant.Destructive,
        enabled = !loading,
    )
}

@Composable
private fun ConfirmStep(
    onConfirmDeletion: () -> Unit,
    onCancel: () -> Unit,
    loading: Boolean,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    TerminalAlert(
        message = stringResource(Res.string.privacy_deletion_confirm_message),
        variant = AlertVariant.Error,
        title = stringResource(Res.string.privacy_deletion_confirm_title),
    )

    Spacer(modifier = Modifier.height(4.dp))

    TerminalText(
        text = stringResource(Res.string.privacy_deletion_confirm_question),
        style = typography.md.copy(fontWeight = FontWeight.Bold),
        color = colors.error,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TerminalButton(
            text = stringResource(Res.string.privacy_deletion_confirm_cancel),
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Secondary,
            enabled = !loading,
        )
        TerminalButton(
            text = stringResource(Res.string.privacy_deletion_confirm_button),
            onClick = onConfirmDeletion,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Destructive,
            enabled = !loading,
        )
    }
}

@Composable
private fun ScheduledStep(
    scheduledAt: String?,
    onCancelDeletion: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    val message = stringResource(Res.string.privacy_deletion_scheduled_message) +
        if (scheduledAt != null) stringResource(Res.string.privacy_deletion_scheduled_message_date, scheduledAt) else ""

    TerminalAlert(
        message = message,
        variant = AlertVariant.Info,
        title = stringResource(Res.string.privacy_deletion_scheduled_title),
    )

    Spacer(modifier = Modifier.height(4.dp))

    TerminalText(
        text = stringResource(Res.string.privacy_deletion_scheduled_info),
        style = typography.sm,
        color = colors.textDim,
    )

    TerminalButton(
        text = stringResource(Res.string.privacy_deletion_scheduled_cancel),
        onClick = onCancelDeletion,
        modifier = Modifier.fillMaxWidth(),
        variant = ButtonVariant.Default,
    )
}

// endregion
