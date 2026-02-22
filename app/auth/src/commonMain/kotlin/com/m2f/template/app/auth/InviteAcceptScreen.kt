package com.m2f.template.app.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.TerminalButton
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.theme.TerminalTheme
import org.jetbrains.compose.resources.stringResource
import template.app.auth.generated.resources.Res
import template.app.auth.generated.resources.common_brand_name
import template.app.auth.generated.resources.common_brand_prompt
import template.app.auth.generated.resources.invite_accept_button
import template.app.auth.generated.resources.invite_accept_button_loading
import template.app.auth.generated.resources.invite_already_accepted
import template.app.auth.generated.resources.invite_expired
import template.app.auth.generated.resources.invite_group_label
import template.app.auth.generated.resources.invite_inviter_label
import template.app.auth.generated.resources.invite_loading
import template.app.auth.generated.resources.invite_role_label
import template.app.auth.generated.resources.invite_subtitle
import template.app.auth.generated.resources.invite_success_message
import template.app.auth.generated.resources.invite_title

/**
 * Invitation acceptance screen.
 * Displays invitation details and allows the user to accept or see status.
 */
@Composable
fun InviteAcceptScreen(
    state: InviteAcceptModel,
    onAccept: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Brand header
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

            // Title
            TerminalText(
                text = stringResource(Res.string.invite_title),
                style = typography.xxl.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )

            // Subtitle
            TerminalText(
                text = stringResource(Res.string.invite_subtitle),
                style = typography.sm,
                color = colors.textDim,
            )

            // Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface),
            ) {
                // Accent bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(colors.accent),
                )

                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Loading state
                    if (state.isLoadingInvitation) {
                        TerminalText(
                            text = stringResource(Res.string.invite_loading),
                            style = typography.sm,
                            color = colors.textDim,
                        )
                    }

                    // Error alert
                    if (state.error != null) {
                        TerminalAlert(
                            message = resolveStringKey(state.error),
                            variant = AlertVariant.Error,
                        )
                    }

                    // Expired alert
                    if (state.isExpired) {
                        TerminalAlert(
                            message = stringResource(Res.string.invite_expired),
                            variant = AlertVariant.Warning,
                        )
                    }

                    // Already accepted alert
                    if (state.isAlreadyAccepted) {
                        TerminalAlert(
                            message = stringResource(Res.string.invite_already_accepted),
                            variant = AlertVariant.Info,
                        )
                    }

                    // Success alert
                    if (state.acceptSuccess) {
                        TerminalAlert(
                            message = stringResource(Res.string.invite_success_message),
                            variant = AlertVariant.Success,
                        )
                    }

                    // Invitation details (only show if loaded and not loading)
                    if (!state.isLoadingInvitation && state.groupName != null) {
                        InvitationDetailRow(
                            label = stringResource(Res.string.invite_group_label),
                            value = state.groupName,
                        )

                        state.inviterName?.let { inviter ->
                            InvitationDetailRow(
                                label = stringResource(Res.string.invite_inviter_label),
                                value = inviter,
                            )
                        }

                        state.role?.let { role ->
                            InvitationDetailRow(
                                label = stringResource(Res.string.invite_role_label),
                                value = role,
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Accept button (only if not expired, not already accepted, and not success)
                        if (!state.isExpired && !state.isAlreadyAccepted && !state.acceptSuccess) {
                            TerminalButton(
                                text = if (state.isAccepting) {
                                    stringResource(Res.string.invite_accept_button_loading)
                                } else {
                                    stringResource(Res.string.invite_accept_button)
                                },
                                onClick = onAccept,
                                modifier = Modifier.fillMaxWidth(),
                                variant = ButtonVariant.Default,
                                enabled = !state.isAccepting,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvitationDetailRow(
    label: String,
    value: String,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TerminalText(
            text = label,
            style = typography.sm.copy(fontWeight = FontWeight.Medium),
            color = colors.textDim,
        )
        TerminalText(
            text = value,
            style = typography.sm.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
        )
    }
}
