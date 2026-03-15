package com.m2f.template.app.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.m2f.template.designsystem.components.selection.TerminalCheckbox
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.models.dto.privacy.ConsentType
import org.jetbrains.compose.resources.stringResource
import template.app.privacy.generated.resources.Res
import template.app.privacy.generated.resources.*

/**
 * Full-screen consent gate that blocks access until required consents are granted.
 *
 * Desktop (>840dp): Centered card with max width ~500dp.
 * Mobile (<=840dp): Full-width with padding.
 */
@Composable
fun ConsentGateScreen(
    state: ConsentGateModel,
    onToggleConsent: (ConsentType) -> Unit,
    onAcceptAll: () -> Unit,
    onViewDocument: (ConsentType) -> Unit,
    onDecline: () -> Unit,
) {
    val colors = TerminalTheme.colors

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        if (maxWidth > 840.dp) {
            // Desktop: centered card
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ConsentGateContent(
                    state = state,
                    onToggleConsent = onToggleConsent,
                    onAcceptAll = onAcceptAll,
                    onViewDocument = onViewDocument,
                    onDecline = onDecline,
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .background(colors.surface)
                        .padding(48.dp),
                )
            }
        } else {
            // Mobile: full-width with padding
            ConsentGateContent(
                state = state,
                onToggleConsent = onToggleConsent,
                onAcceptAll = onAcceptAll,
                onViewDocument = onViewDocument,
                onDecline = onDecline,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            )
        }
    }
}

@Composable
private fun ConsentGateContent(
    state: ConsentGateModel,
    onToggleConsent: (ConsentType) -> Unit,
    onAcceptAll: () -> Unit,
    onViewDocument: (ConsentType) -> Unit,
    onDecline: () -> Unit,
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
            text = stringResource(Res.string.privacy_gate_title),
            style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
            color = colors.text,
        )

        TerminalText(
            text = stringResource(Res.string.privacy_gate_subtitle),
            style = typography.sm,
            color = colors.textDim,
        )

        // Loading state
        if (state.loading) {
            TerminalProgress(
                label = stringResource(Res.string.privacy_gate_loading),
            )
        }

        // Error state
        if (state.error != null) {
            TerminalAlert(
                message = state.error.code,
                variant = AlertVariant.Error,
                title = stringResource(Res.string.privacy_gate_error_title),
            )
        }

        // Consent items
        if (state.consents.isNotEmpty()) {
            TerminalDivider()

            state.consents.forEach { item ->
                ConsentRow(
                    item = item,
                    onToggle = { onToggleConsent(item.type) },
                    onViewDocument = { onViewDocument(item.type) },
                )
            }

            TerminalDivider()

            // Accept All button
            TerminalButton(
                text = stringResource(Res.string.privacy_gate_accept_all),
                onClick = onAcceptAll,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Default,
                enabled = state.allAccepted && !state.loading,
            )

            // Decline button
            TerminalButton(
                text = stringResource(Res.string.privacy_gate_decline),
                onClick = onDecline,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary,
                enabled = !state.loading,
            )
        }
    }
}

@Composable
private fun ConsentRow(
    item: ConsentItem,
    onToggle: () -> Unit,
    onViewDocument: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TerminalCheckbox(
                checked = item.accepted,
                onCheckedChange = { onToggle() },
                label = consentTypeLabel(item.type),
            )
        }

        BasicText(
            text = stringResource(Res.string.privacy_gate_read),
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onViewDocument,
            ),
            style = typography.sm.copy(color = colors.accent),
        )
    }
}

@Composable
internal fun consentTypeLabel(type: ConsentType): String = when (type) {
    ConsentType.PRIVACY_POLICY -> stringResource(Res.string.privacy_consent_type_privacy_policy)
    ConsentType.TERMS_OF_SERVICE -> stringResource(Res.string.privacy_consent_type_terms_of_service)
    ConsentType.MARKETING -> stringResource(Res.string.privacy_consent_type_marketing)
    ConsentType.ANALYTICS -> stringResource(Res.string.privacy_consent_type_analytics)
}
