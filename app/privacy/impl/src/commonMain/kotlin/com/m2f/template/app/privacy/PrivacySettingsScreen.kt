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

/**
 * Privacy settings screen allowing users to manage consents, data exports,
 * processing restrictions, and account deletion.
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
    onToggleRestriction: () -> Unit,
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
                onToggleRestriction = onToggleRestriction,
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
private fun PrivacySettingsContent(
    state: PrivacySettingsModel,
    onRequestExport: () -> Unit,
    onDownloadExport: () -> Unit,
    onRequestDeletion: () -> Unit,
    onToggleRestriction: () -> Unit,
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
                text = "Privacy Settings",
                style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
                color = colors.text,
            )

            TerminalButton(
                text = "Back",
                onClick = onBack,
                variant = ButtonVariant.Ghost,
            )
        }

        // Loading
        if (state.loading) {
            TerminalProgress(label = "Loading privacy settings...")
        }

        // Error
        if (state.error != null) {
            TerminalAlert(
                message = state.error.code,
                variant = AlertVariant.Error,
                title = "Error",
            )
        }

        // Section: Your Consents
        SectionHeader(title = "Your Consents")

        state.activeConsents.forEach { consent ->
            ConsentStatusRow(
                consent = consent,
                onViewDocument = { onViewDocument(consent.type) },
                onWithdraw = { onWithdrawConsent(consent.type) },
            )
        }

        TerminalDivider()

        // Section: Data Export
        SectionHeader(title = "Data Export")

        TerminalText(
            text = "Request a copy of all your personal data.",
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
                    text = "Export status:",
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
                    text = "Download Export",
                    onClick = onDownloadExport,
                    variant = ButtonVariant.Success,
                )
            }
        } else {
            TerminalButton(
                text = "Request Data Export",
                onClick = onRequestExport,
                variant = ButtonVariant.Secondary,
                enabled = !state.loading,
            )
        }

        TerminalDivider()

        // Section: Processing Restriction
        SectionHeader(title = "Processing Restriction")

        TerminalText(
            text = "Restrict the processing of your personal data. Some features may be limited.",
            style = typography.sm,
            color = colors.textDim,
        )

        TerminalSwitch(
            checked = state.isRestricted,
            onCheckedChange = { onToggleRestriction() },
            label = if (state.isRestricted) "Processing restricted" else "Processing active",
            enabled = !state.loading,
        )

        TerminalDivider()

        // Section: Delete Account
        SectionHeader(title = "Delete Account")

        TerminalText(
            text = "Permanently delete your account and all associated data. This action cannot be undone.",
            style = typography.sm,
            color = colors.textDim,
        )

        TerminalButton(
            text = "Delete My Account",
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
                    text = "Granted: ${consent.grantedAt}",
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
                text = "View",
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
                    text = "Required",
                    variant = BadgeVariant.Default,
                )
            }
        }
    }
}
