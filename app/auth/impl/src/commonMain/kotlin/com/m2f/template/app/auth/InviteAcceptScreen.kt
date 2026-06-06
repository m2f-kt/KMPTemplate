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
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.AuraButton
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.AuraAlert
import com.m2f.template.designsystem.theme.AuraTheme
import org.jetbrains.compose.resources.stringResource
import template.app.auth.generated.resources.Res
import template.app.auth.generated.resources.common_brand_name
import template.app.auth.generated.resources.common_brand_prompt
import template.app.auth.generated.resources.invite_accept_button
import template.app.auth.generated.resources.invite_accept_button_loading
import template.app.auth.generated.resources.invite_already_accepted
import template.app.auth.generated.resources.invite_auth_prompt
import template.app.auth.generated.resources.invite_expired
import template.app.auth.generated.resources.invite_group_label
import template.app.auth.generated.resources.invite_inviter_label
import template.app.auth.generated.resources.invite_loading
import template.app.auth.generated.resources.invite_login_button
import template.app.auth.generated.resources.invite_register_button
import template.app.auth.generated.resources.invite_role_label
import template.app.auth.generated.resources.invite_subtitle
import template.app.auth.generated.resources.invite_success_message
import template.app.auth.generated.resources.invite_expired_hint
import template.app.auth.generated.resources.invite_request_new
import template.app.auth.generated.resources.invite_revoked
import template.app.auth.generated.resources.invite_revoked_hint
import template.app.auth.generated.resources.invite_title

/**
 * Invitation acceptance screen.
 * Displays invitation details and allows the user to accept or see status.
 */
@Composable
fun InviteAcceptScreen(
    state: InviteAcceptModel,
    onAccept: () -> Unit,
    onGoToLogin: () -> Unit,
    onGoToRegister: () -> Unit,
    onRequestNewInvitation: () -> Unit,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

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
                AuraText(
                    text = stringResource(Res.string.common_brand_prompt),
                    style = typography.md.copy(fontWeight = FontWeight.Bold),
                    color = colors.accent,
                )
                AuraText(
                    text = stringResource(Res.string.common_brand_name),
                    style = typography.md.copy(fontWeight = FontWeight.Medium),
                    color = colors.text,
                )
            }

            // Title
            AuraText(
                text = stringResource(Res.string.invite_title),
                style = typography.xxl.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )

            // Subtitle
            AuraText(
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
                        AuraText(
                            text = stringResource(Res.string.invite_loading),
                            style = typography.sm,
                            color = colors.textDim,
                        )
                    }

                    // Error alert
                    if (state.error != null) {
                        AuraAlert(
                            message = resolveStringKey(state.error),
                            variant = AlertVariant.Error,
                        )
                    }

                    // Expired alert
                    if (state.isExpired) {
                        AuraAlert(
                            message = stringResource(Res.string.invite_expired),
                            variant = AlertVariant.Warning,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AuraText(
                            text = stringResource(Res.string.invite_expired_hint),
                            style = typography.sm,
                            color = colors.textDim,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AuraButton(
                            text = stringResource(Res.string.invite_request_new),
                            onClick = onRequestNewInvitation,
                            modifier = Modifier.fillMaxWidth(),
                            variant = ButtonVariant.Secondary,
                        )
                    }

                    // Revoked alert
                    if (state.isRevoked) {
                        AuraAlert(
                            message = stringResource(Res.string.invite_revoked),
                            variant = AlertVariant.Error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AuraText(
                            text = stringResource(Res.string.invite_revoked_hint),
                            style = typography.sm,
                            color = colors.textDim,
                        )
                    }

                    // Already accepted alert
                    if (state.isAlreadyAccepted) {
                        AuraAlert(
                            message = stringResource(Res.string.invite_already_accepted),
                            variant = AlertVariant.Info,
                        )
                    }

                    // Success alert
                    if (state.acceptSuccess) {
                        AuraAlert(
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

                        // Action buttons (only if not expired, not already accepted, not revoked, and not success)
                        if (!state.isExpired && !state.isAlreadyAccepted && !state.isRevoked && !state.acceptSuccess) {
                            if (state.isLoggedIn) {
                                // Authenticated user: show Accept button
                                AuraButton(
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
                            } else {
                                // Unauthenticated user: show Login/Register options
                                AuraText(
                                    text = stringResource(Res.string.invite_auth_prompt),
                                    style = typography.sm,
                                    color = colors.textDim,
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                AuraButton(
                                    text = stringResource(Res.string.invite_login_button),
                                    onClick = onGoToLogin,
                                    modifier = Modifier.fillMaxWidth(),
                                    variant = ButtonVariant.Default,
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                AuraButton(
                                    text = stringResource(Res.string.invite_register_button),
                                    onClick = onGoToRegister,
                                    modifier = Modifier.fillMaxWidth(),
                                    variant = ButtonVariant.Secondary,
                                )
                            }
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
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuraText(
            text = label,
            style = typography.sm.copy(fontWeight = FontWeight.Medium),
            color = colors.textDim,
        )
        AuraText(
            text = value,
            style = typography.sm.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
        )
    }
}
