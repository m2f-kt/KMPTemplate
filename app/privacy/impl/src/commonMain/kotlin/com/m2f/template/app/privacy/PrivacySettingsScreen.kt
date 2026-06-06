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
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.AuraButton
import com.m2f.template.designsystem.components.display.AuraDivider
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.AuraAlert
import com.m2f.template.designsystem.components.feedback.AuraBadge
import com.m2f.template.designsystem.components.feedback.AuraProgress
import com.m2f.template.designsystem.components.selection.AuraSwitch
import com.m2f.template.designsystem.theme.AuraTheme
import com.m2f.template.designsystem.util.toDisplayDateTime
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
    onToggleConsent: (ConsentStatus) -> Unit,
    onBack: () -> Unit,
) {
    val colors = AuraTheme.colors

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
                onToggleConsent = onToggleConsent,
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
    onToggleConsent: (ConsentStatus) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

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
            AuraText(
                text = stringResource(Res.string.privacy_settings_title),
                style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
                color = colors.text,
            )

            AuraButton(
                text = stringResource(Res.string.privacy_settings_back),
                onClick = onBack,
                variant = ButtonVariant.Ghost,
            )
        }

        // Loading
        if (state.loading) {
            AuraProgress(label = stringResource(Res.string.privacy_settings_loading))
        }

        // Error
        if (state.error != null) {
            AuraAlert(
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
                onToggle = { onToggleConsent(consent) },
            )
        }

        AuraDivider()

        // Section: Data Export
        SectionHeader(title = stringResource(Res.string.privacy_settings_export_header))

        AuraText(
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
                AuraText(
                    text = stringResource(Res.string.privacy_settings_export_status),
                    style = typography.sm,
                    color = colors.text,
                )
                AuraBadge(
                    text = exportStatusLabel(state.exportStatus.status),
                    variant = when (state.exportStatus.status) {
                        ExportStatus.COMPLETED -> BadgeVariant.Success
                        ExportStatus.PENDING, ExportStatus.PROCESSING -> BadgeVariant.Warning
                        ExportStatus.FAILED -> BadgeVariant.Error
                        ExportStatus.EXPIRED -> BadgeVariant.Default
                    },
                )
            }

            if (state.exportStatus.status == ExportStatus.COMPLETED) {
                AuraButton(
                    text = stringResource(Res.string.privacy_settings_export_download),
                    onClick = onDownloadExport,
                    variant = ButtonVariant.Success,
                )
            }
        } else {
            AuraButton(
                text = stringResource(Res.string.privacy_settings_export_request),
                onClick = onRequestExport,
                variant = ButtonVariant.Secondary,
                enabled = !state.loading,
            )
        }

        AuraDivider()

        // Section: Delete Account
        SectionHeader(title = stringResource(Res.string.privacy_settings_delete_header))

        AuraText(
            text = stringResource(Res.string.privacy_settings_delete_desc),
            style = typography.sm,
            color = colors.textDim,
        )

        AuraButton(
            text = stringResource(Res.string.privacy_settings_delete_button),
            onClick = onRequestDeletion,
            variant = ButtonVariant.Destructive,
            enabled = !state.loading,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun exportStatusLabel(status: ExportStatus): String = when (status) {
    ExportStatus.PENDING -> stringResource(Res.string.privacy_export_status_pending)
    ExportStatus.PROCESSING -> stringResource(Res.string.privacy_export_status_processing)
    ExportStatus.COMPLETED -> stringResource(Res.string.privacy_export_status_completed)
    ExportStatus.FAILED -> stringResource(Res.string.privacy_export_status_failed)
    ExportStatus.EXPIRED -> stringResource(Res.string.privacy_export_status_expired)
}

@Composable
private fun SectionHeader(title: String) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    AuraText(
        text = "> $title",
        style = typography.md.copy(fontWeight = FontWeight.Bold),
        color = colors.accent,
    )
}

@Composable
private fun ConsentStatusRow(
    consent: ConsentStatus,
    onViewDocument: () -> Unit,
    onToggle: () -> Unit,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
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
            AuraText(
                text = consentTypeLabel(consent.type),
                style = typography.sm.copy(fontWeight = FontWeight.Medium),
                color = colors.text,
            )
            if (consent.grantedAt != null) {
                AuraText(
                    text = stringResource(Res.string.privacy_consent_granted_prefix, consent.grantedAt?.toDisplayDateTime() ?: ""),
                    style = typography.xs,
                    color = colors.textDim,
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuraButton(
                text = stringResource(Res.string.privacy_consent_view),
                onClick = onViewDocument,
                variant = ButtonVariant.Ghost,
            )

            if (isOptional) {
                AuraSwitch(
                    checked = consent.granted,
                    onCheckedChange = { onToggle() },
                )
            } else {
                AuraBadge(
                    text = stringResource(Res.string.privacy_consent_required_badge),
                    variant = BadgeVariant.Default,
                )
            }
        }
    }
}
