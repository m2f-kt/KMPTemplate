package com.m2f.template.app.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.components.feedback.TerminalProgress
import com.m2f.template.designsystem.components.selection.TerminalSwitch
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.models.dto.privacy.ConsentStatus
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.dto.privacy.ExportStatus
import org.jetbrains.compose.resources.stringResource
import template.app.privacy.generated.resources.Res
import template.app.privacy.generated.resources.*

/**
 * Privacy settings screen allowing users to manage consents, data exports,
 * and account deletion.
 *
 * Desktop (>840dp): Centered column with max width ~600dp.
 * Mobile (<=840dp): Full-width with padding.
 */
@Composable
fun PrivacySettingsScreen(
    state: PrivacySettingsModel,
    onRequestExport: () -> Unit,
    onDownloadExport: () -> Unit,
    onRequestDeletion: () -> Unit,
    onViewDocument: (ConsentType) -> Unit,
    onWithdrawConsent: (ConsentType) -> Unit,
    onBack: () -> Unit,
) {
    val colors = TerminalTheme.colors

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        val isDesktop = maxWidth > 840.dp
        val contentModifier = if (isDesktop) {
            Modifier
                .fillMaxSize()
                .padding(48.dp)
        } else {
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        }

        Column(
            modifier = contentModifier,
            horizontalAlignment = if (isDesktop) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            PrivacySettingsContent(
                state = state,
                onRequestExport = onRequestExport,
                onDownloadExport = onDownloadExport,
                onRequestDeletion = onRequestDeletion,
                onViewDocument = onViewDocument,
                onWithdrawConsent = onWithdrawConsent,
                onBack = onBack,
                modifier = if (isDesktop) {
                    Modifier.widthIn(max = 600.dp)
                } else {
                    Modifier.fillMaxWidth()
                },
            )
        }
    }
}

@Composable
fun PrivacySettingsContent(
    state: PrivacySettingsModel,
    onRequestExport: () -> Unit,
    onDownloadExport: () -> Unit,
    onRequestDeletion: () -> Unit,
    onViewDocument: (ConsentType) -> Unit,
    onWithdrawConsent: (ConsentType) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TerminalText(
                text = stringResource(Res.string.privacy_settings_title),
                style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
                color = colors.text,
            )

            TerminalButton(
                text = stringResource(Res.string.privacy_settings_back),
                onClick = onBack,
                variant = ButtonVariant.Ghost,
            )
        }

        // Loading
        if (state.loading) {
            TerminalProgress(label = stringResource(Res.string.privacy_settings_loading))
        }

        // Error
        if (state.error != null) {
            TerminalAlert(
                message = state.error.code,
                variant = AlertVariant.Error,
                title = stringResource(Res.string.privacy_settings_error_title),
            )
        }

        // Section: Your Consents
        SectionHeader(title = stringResource(Res.string.privacy_settings_consents_header))

        state.activeConsents.forEach { consent ->
            ConsentStatusRow(
                consent = consent,
                onViewDocument = { onViewDocument(consent.type) },
                onWithdraw = { onWithdrawConsent(consent.type) },
            )
        }

        TerminalDivider()

        // Section: Data Export
        SectionHeader(title = stringResource(Res.string.privacy_settings_export_header))

        TerminalText(
            text = stringResource(Res.string.privacy_settings_export_desc),
            style = typography.sm,
            color = colors.textDim,
        )

        if (state.exportStatus != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TerminalText(
                    text = stringResource(Res.string.privacy_settings_export_status),
                    style = typography.sm,
                    color = colors.text,
                )
                TerminalBadge(
                    text = state.exportStatus.status.name,
                    variant = when (state.exportStatus.status) {
                        ExportStatus.COMPLETED -> BadgeVariant.Success
                        ExportStatus.PENDING, ExportStatus.PROCESSING -> BadgeVariant.Warning
                        ExportStatus.FAILED -> BadgeVariant.Error
                        ExportStatus.EXPIRED -> BadgeVariant.Default
                    },
                )
            }

            if (state.exportStatus.status == ExportStatus.COMPLETED && state.exportStatus.downloadUrl != null) {
                TerminalButton(
                    text = stringResource(Res.string.privacy_settings_export_download),
                    onClick = onDownloadExport,
                    variant = ButtonVariant.Success,
                )
            }
        } else {
            TerminalButton(
                text = stringResource(Res.string.privacy_settings_export_request),
                onClick = onRequestExport,
                variant = ButtonVariant.Secondary,
                enabled = !state.loading,
            )
        }

        TerminalDivider()

        // Section: Delete Account
        SectionHeader(title = stringResource(Res.string.privacy_settings_delete_header))

        TerminalText(
            text = stringResource(Res.string.privacy_settings_delete_desc),
            style = typography.sm,
            color = colors.textDim,
        )

        TerminalButton(
            text = stringResource(Res.string.privacy_settings_delete_button),
            onClick = onRequestDeletion,
            variant = ButtonVariant.Destructive,
            enabled = !state.loading,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    TerminalText(
        text = "> $title",
        style = typography.md.copy(fontWeight = FontWeight.Bold),
        color = colors.accent,
    )
}

@Composable
private fun ConsentStatusRow(
    consent: ConsentStatus,
    onViewDocument: () -> Unit,
    onWithdraw: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val isOptional = consent.type == ConsentType.MARKETING || consent.type == ConsentType.ANALYTICS

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            TerminalText(
                text = consentTypeLabel(consent.type),
                style = typography.sm.copy(fontWeight = FontWeight.Medium),
                color = colors.text,
            )
            if (consent.grantedAt != null) {
                TerminalText(
                    text = stringResource(Res.string.privacy_consent_granted_prefix, consent.grantedAt ?: ""),
                    style = typography.xs,
                    color = colors.textDim,
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TerminalButton(
                text = stringResource(Res.string.privacy_consent_view),
                onClick = onViewDocument,
                variant = ButtonVariant.Ghost,
            )

            if (isOptional) {
                TerminalSwitch(
                    checked = consent.granted,
                    onCheckedChange = { onWithdraw() },
                )
            } else {
                TerminalBadge(
                    text = stringResource(Res.string.privacy_consent_required_badge),
                    variant = BadgeVariant.Default,
                )
            }
        }
    }
}
