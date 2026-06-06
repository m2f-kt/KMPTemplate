package com.m2f.template.app.privacy

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.AuraButton
import com.m2f.template.designsystem.components.card.CardVariant
import com.m2f.template.designsystem.components.card.AuraCard
import com.m2f.template.designsystem.components.display.AuraDivider
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.AuraAlert
import com.m2f.template.designsystem.components.feedback.AuraProgress
import com.m2f.template.designsystem.components.input.AuraPasswordInput
import com.m2f.template.designsystem.components.input.AuraTextarea
import com.m2f.template.designsystem.theme.AuraTheme
import com.m2f.template.designsystem.util.toDisplayDate
import org.jetbrains.compose.resources.stringResource
import template.app.privacy.generated.resources.Res
import template.app.privacy.generated.resources.*

/**
 * Multi-step account deletion flow screen.
 *
 * Mobile (<=840dp): Single column, full-width with padding.
 * Desktop (>840dp): Two-column layout -- left (main flow) + right (480dp contextual panel).
 */
@Composable
fun AccountDeletionScreen(
    state: AccountDeletionModel,
    onProceedToReAuth: () -> Unit,
    onReAuthenticate: (String) -> Unit,
    onSetReason: (String) -> Unit,
    onSkipReason: () -> Unit,
    onConfirmDeletion: () -> Unit,
    onCancelDeletion: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = AuraTheme.colors

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        if (maxWidth > 840.dp) {
            // Desktop: Two-column layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Left column -- main flow
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(48.dp)
                        .widthIn(max = 800.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    StepContent(
                        state = state,
                        onProceedToReAuth = onProceedToReAuth,
                        onReAuthenticate = onReAuthenticate,
                        onSetReason = onSetReason,
                        onSkipReason = onSkipReason,
                        onConfirmDeletion = onConfirmDeletion,
                        onCancelDeletion = onCancelDeletion,
                        onLogout = onLogout,
                        onBack = onBack,
                    )
                }

                // Right column -- contextual panel
                val borderColor = colors.border
                Column(
                    modifier = Modifier
                        .width(480.dp)
                        .fillMaxHeight()
                        .drawBehind {
                            drawLine(
                                color = borderColor,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }
                        .background(colors.bg)
                        .verticalScroll(rememberScrollState())
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    DesktopRightPanel(step = state.step)
                }
            }
        } else {
            // Mobile: Single column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepContent(
                    state = state,
                    onProceedToReAuth = onProceedToReAuth,
                    onReAuthenticate = onReAuthenticate,
                    onSetReason = onSetReason,
                    onSkipReason = onSkipReason,
                    onConfirmDeletion = onConfirmDeletion,
                    onCancelDeletion = onCancelDeletion,
                    onLogout = onLogout,
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun StepContent(
    state: AccountDeletionModel,
    onProceedToReAuth: () -> Unit,
    onReAuthenticate: (String) -> Unit,
    onSetReason: (String) -> Unit,
    onSkipReason: () -> Unit,
    onConfirmDeletion: () -> Unit,
    onCancelDeletion: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    if (state.loading) {
        AuraProgress(label = stringResource(Res.string.privacy_deletion_processing))
    }

    when (state.step) {
        DeletionStep.WARNING -> WarningStep(
            onContinue = onProceedToReAuth,
            onCancel = onBack,
            loading = state.loading,
        )

        DeletionStep.RE_AUTH -> ReAuthStep(
            error = state.error,
            onReAuthenticate = onReAuthenticate,
            onBack = onBack,
            loading = state.loading,
        )

        DeletionStep.REASON -> ReasonStep(
            reason = state.reason,
            onSetReason = onSetReason,
            onSkipReason = onSkipReason,
            onBack = onBack,
            loading = state.loading,
        )

        DeletionStep.CONFIRM -> ConfirmStep(
            userEmail = state.userEmail,
            onConfirmDeletion = onConfirmDeletion,
            onCancel = onBack,
            loading = state.loading,
        )

        DeletionStep.SCHEDULED -> ScheduledStep(
            scheduledAt = state.pendingDeletion?.scheduledAt,
            onCancelDeletion = onCancelDeletion,
            onLogout = onLogout,
        )
    }
}

// region Steps

@Composable
private fun WarningStep(
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    loading: Boolean,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    // Step label
    AuraText(
        text = stringResource(Res.string.privacy_deletion_step_1),
        style = typography.xs.copy(fontSize = 10.sp, letterSpacing = 2.sp),
        color = colors.textDim,
    )

    // Title
    AuraText(
        text = stringResource(Res.string.privacy_deletion_title),
        style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
        color = colors.text,
    )

    // Danger alert
    AuraAlert(
        message = stringResource(Res.string.privacy_deletion_warning_danger_message),
        variant = AlertVariant.Error,
        title = "[${stringResource(Res.string.privacy_deletion_warning_danger_title)}]",
    )

    // Scope label
    AuraText(
        text = stringResource(Res.string.privacy_deletion_warning_scope_label),
        style = typography.xs.copy(fontSize = 10.sp, letterSpacing = 2.sp),
        color = colors.textDim,
    )

    // Scope card
    AuraCard(
        title = stringResource(Res.string.privacy_deletion_warning_scope_card_title),
        description = stringResource(Res.string.privacy_deletion_warning_scope_card_desc),
        variant = CardVariant.Default,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val items = listOf(
                stringResource(Res.string.privacy_deletion_warning_scope_item1),
                stringResource(Res.string.privacy_deletion_warning_scope_item2),
                stringResource(Res.string.privacy_deletion_warning_scope_item3),
                stringResource(Res.string.privacy_deletion_warning_scope_item4),
            )
            items.forEach { item ->
                AuraText(
                    text = "▸ $item",
                    style = typography.sm.copy(lineHeight = (typography.sm.fontSize.value * 1.8).sp),
                    color = colors.text,
                )
            }
        }
    }

    // Grace period text
    AuraText(
        text = "▸ ${stringResource(Res.string.privacy_deletion_warning_grace)}",
        style = typography.sm.copy(fontSize = 13.sp),
        color = colors.textDim,
    )

    // Buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AuraButton(
            text = stringResource(Res.string.privacy_deletion_warning_understand),
            onClick = onContinue,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Destructive,
            enabled = !loading,
        )
        AuraButton(
            text = stringResource(Res.string.privacy_deletion_cancel),
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Ghost,
            enabled = !loading,
        )
    }
}

@Composable
private fun ReAuthStep(
    error: com.m2f.template.models.localization.StringKey?,
    onReAuthenticate: (String) -> Unit,
    onBack: () -> Unit,
    loading: Boolean,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    var password by remember { mutableStateOf("") }

    // Step label
    AuraText(
        text = stringResource(Res.string.privacy_deletion_step_2),
        style = typography.xs.copy(fontSize = 10.sp, letterSpacing = 2.sp),
        color = colors.textDim,
    )

    // Title
    AuraText(
        text = stringResource(Res.string.privacy_deletion_reauth_title),
        style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
        color = colors.text,
    )

    // Subtitle
    AuraText(
        text = stringResource(Res.string.privacy_deletion_reauth_subtitle),
        style = typography.sm.copy(fontSize = 13.sp),
        color = colors.textDim,
    )

    // Password input
    AuraPasswordInput(
        value = password,
        onValueChange = { password = it },
        label = stringResource(Res.string.privacy_deletion_reauth_label),
        placeholder = stringResource(Res.string.privacy_deletion_reauth_placeholder),
        enabled = !loading,
    )

    // Error alert
    if (error != null) {
        AuraAlert(
            message = stringResource(Res.string.privacy_deletion_reauth_error_message),
            variant = AlertVariant.Error,
            title = "[${stringResource(Res.string.privacy_deletion_reauth_error_title)}]",
        )
    }

    // Buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AuraButton(
            text = stringResource(Res.string.privacy_deletion_reauth_verify),
            onClick = { onReAuthenticate(password) },
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Destructive,
            enabled = password.isNotBlank() && !loading,
        )
        AuraButton(
            text = stringResource(Res.string.privacy_deletion_back),
            onClick = onBack,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Ghost,
            enabled = !loading,
        )
    }
}

@Composable
private fun ReasonStep(
    reason: String,
    onSetReason: (String) -> Unit,
    onSkipReason: () -> Unit,
    onBack: () -> Unit,
    loading: Boolean,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    var localReason by remember(reason) { mutableStateOf(reason) }

    // Step label
    AuraText(
        text = stringResource(Res.string.privacy_deletion_step_3),
        style = typography.xs.copy(fontSize = 10.sp, letterSpacing = 2.sp),
        color = colors.textDim,
    )

    // Title
    AuraText(
        text = stringResource(Res.string.privacy_deletion_reason_title),
        style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
        color = colors.text,
    )

    // Subtitle
    AuraText(
        text = stringResource(Res.string.privacy_deletion_reason_subtitle),
        style = typography.sm.copy(fontSize = 13.sp),
        color = colors.textDim,
    )

    // Textarea
    AuraTextarea(
        value = localReason,
        onValueChange = { localReason = it },
        label = stringResource(Res.string.privacy_deletion_reason_label),
        placeholder = stringResource(Res.string.privacy_deletion_reason_placeholder),
        enabled = !loading,
    )

    // Buttons
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AuraButton(
            text = stringResource(Res.string.privacy_deletion_reason_continue),
            onClick = { onSetReason(localReason) },
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Destructive,
            enabled = !loading,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuraButton(
                text = stringResource(Res.string.privacy_deletion_reason_skip),
                onClick = onSkipReason,
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.Ghost,
                enabled = !loading,
            )
            AuraButton(
                text = stringResource(Res.string.privacy_deletion_back),
                onClick = onBack,
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.Ghost,
                enabled = !loading,
            )
        }
    }
}

@Composable
private fun ConfirmStep(
    userEmail: String,
    onConfirmDeletion: () -> Unit,
    onCancel: () -> Unit,
    loading: Boolean,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    val scheduledDate = "7 days"

    // Step label
    AuraText(
        text = stringResource(Res.string.privacy_deletion_confirm_step),
        style = typography.xs.copy(fontSize = 10.sp, letterSpacing = 2.sp),
        color = colors.textDim,
    )

    // Title
    AuraText(
        text = stringResource(Res.string.privacy_deletion_confirm_title),
        style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
        color = colors.text,
    )

    // Subtitle
    AuraText(
        text = stringResource(Res.string.privacy_deletion_confirm_subtitle),
        style = typography.xs.copy(fontSize = 12.sp),
        color = colors.textDim,
    )

    // Deletion summary card
    AuraCard(
        title = stringResource(Res.string.privacy_deletion_confirm_card_title),
        description = stringResource(Res.string.privacy_deletion_confirm_card_desc),
        variant = CardVariant.Accent,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AuraText(
                text = stringResource(Res.string.privacy_deletion_confirm_account, userEmail),
                style = typography.sm,
                color = colors.text,
            )
            AuraText(
                text = stringResource(Res.string.privacy_deletion_confirm_scheduled, scheduledDate),
                style = typography.sm,
                color = colors.text,
            )
            AuraText(
                text = stringResource(Res.string.privacy_deletion_confirm_grace),
                style = typography.sm,
                color = colors.text,
            )
        }
    }

    // Warning alert
    AuraAlert(
        message = stringResource(Res.string.privacy_deletion_confirm_warn, scheduledDate),
        variant = AlertVariant.Warning,
        title = "[WARN]",
    )

    // Buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AuraButton(
            text = stringResource(Res.string.privacy_deletion_confirm_button),
            onClick = onConfirmDeletion,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Destructive,
            enabled = !loading,
        )
        AuraButton(
            text = stringResource(Res.string.privacy_deletion_cancel),
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Ghost,
            enabled = !loading,
        )
    }
}

@Composable
private fun ScheduledStep(
    scheduledAt: String?,
    onCancelDeletion: () -> Unit,
    onLogout: () -> Unit,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    val displayDate = scheduledAt?.toDisplayDate() ?: ""

    // Step label
    AuraText(
        text = stringResource(Res.string.privacy_deletion_scheduled_step),
        style = typography.xs.copy(fontSize = 10.sp, letterSpacing = 2.sp),
        color = colors.textDim,
    )

    // Success alert
    AuraAlert(
        message = stringResource(Res.string.privacy_deletion_scheduled_success_message),
        variant = AlertVariant.Success,
        title = "[SUCCESS] ${stringResource(Res.string.privacy_deletion_scheduled_success_title)}",
    )

    // Info card
    AuraCard(
        title = stringResource(Res.string.privacy_deletion_scheduled_card_title),
        description = stringResource(Res.string.privacy_deletion_scheduled_card_desc),
        variant = CardVariant.Info,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AuraText(
                text = stringResource(Res.string.privacy_deletion_scheduled_card_line1, displayDate),
                style = typography.sm,
                color = colors.text,
            )
            AuraText(
                text = stringResource(Res.string.privacy_deletion_scheduled_card_line2),
                style = typography.sm,
                color = colors.text,
            )
            AuraText(
                text = stringResource(Res.string.privacy_deletion_scheduled_card_line3),
                style = typography.sm,
                color = colors.text,
            )
        }
    }

    // Buttons
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AuraButton(
            text = stringResource(Res.string.privacy_deletion_scheduled_cancel),
            onClick = onCancelDeletion,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Default,
        )
        AuraButton(
            text = stringResource(Res.string.privacy_deletion_scheduled_logout),
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Ghost,
        )
    }
}

// endregion

// region Desktop Right Panel

@Composable
private fun DesktopRightPanel(step: DeletionStep) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    when (step) {
        DeletionStep.WARNING -> {
            PanelHeader(stringResource(Res.string.privacy_deletion_desktop_step1_header))
            PanelTitle(stringResource(Res.string.privacy_deletion_desktop_step1_title))
            AuraText(
                text = stringResource(Res.string.privacy_deletion_desktop_step1_desc),
                style = typography.sm,
                color = colors.textDim,
            )
            AuraDivider()
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step1_bullet1))
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step1_bullet2))
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step1_bullet3))
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step1_bullet4))
            Spacer(modifier = Modifier.height(8.dp))
            AuraText(
                text = stringResource(Res.string.privacy_deletion_desktop_step1_note),
                style = typography.xs,
                color = colors.textDim,
            )
        }

        DeletionStep.RE_AUTH -> {
            PanelHeader(stringResource(Res.string.privacy_deletion_desktop_step2_header))
            PanelTitle(stringResource(Res.string.privacy_deletion_desktop_step2_title))
            AuraText(
                text = stringResource(Res.string.privacy_deletion_desktop_step2_desc),
                style = typography.sm,
                color = colors.textDim,
            )
            AuraDivider()
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step2_bullet1))
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step2_bullet2))
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step2_bullet3))
        }

        DeletionStep.REASON -> {
            PanelHeader(stringResource(Res.string.privacy_deletion_desktop_step3_header))
            PanelTitle(stringResource(Res.string.privacy_deletion_desktop_step3_title))
            AuraText(
                text = stringResource(Res.string.privacy_deletion_desktop_step3_desc),
                style = typography.sm,
                color = colors.textDim,
            )
            AuraDivider()
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step3_bullet1))
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step3_bullet2))
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step3_bullet3))
        }

        DeletionStep.CONFIRM -> {
            PanelHeader(stringResource(Res.string.privacy_deletion_desktop_step4_header))
            AuraDivider()
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step4_bullet1))
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step4_bullet2))
            PanelBullet(stringResource(Res.string.privacy_deletion_desktop_step4_bullet3))
            Spacer(modifier = Modifier.height(8.dp))
            AuraText(
                text = stringResource(Res.string.privacy_deletion_desktop_step4_note),
                style = typography.xs,
                color = colors.textDim,
            )
        }

        DeletionStep.SCHEDULED -> {
            PanelHeader(stringResource(Res.string.privacy_deletion_desktop_step5_header))
            AuraDivider()
            AuraText(
                text = stringResource(Res.string.privacy_deletion_desktop_step5_step1),
                style = typography.sm,
                color = colors.text,
            )
            AuraText(
                text = stringResource(Res.string.privacy_deletion_desktop_step5_step2),
                style = typography.sm,
                color = colors.text,
            )
            AuraText(
                text = stringResource(Res.string.privacy_deletion_desktop_step5_step3),
                style = typography.sm,
                color = colors.text,
            )
            Spacer(modifier = Modifier.height(8.dp))
            AuraText(
                text = stringResource(Res.string.privacy_deletion_desktop_step5_cancel),
                style = typography.xs,
                color = colors.textDim,
            )
        }
    }
}

@Composable
private fun PanelStepIndicator(text: String) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    AuraText(
        text = text,
        style = typography.xs.copy(fontSize = 10.sp, letterSpacing = 2.sp),
        color = colors.textDim,
    )
}

@Composable
private fun PanelHeader(text: String) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    AuraText(
        text = text,
        style = typography.xs.copy(fontSize = 10.sp, letterSpacing = 2.sp),
        color = colors.accent,
    )
}

@Composable
private fun PanelTitle(text: String) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    AuraText(
        text = text,
        style = typography.md.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
        color = colors.text,
    )
}

@Composable
private fun PanelBullet(text: String) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    AuraText(
        text = "▸ $text",
        style = typography.sm,
        color = colors.text,
    )
}

// endregion
