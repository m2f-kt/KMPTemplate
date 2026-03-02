package com.m2f.template.app.admin

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.TerminalButton
import com.m2f.template.designsystem.components.button.TerminalIconButton
import com.m2f.template.designsystem.components.card.CardVariant
import com.m2f.template.designsystem.components.card.TerminalCard
import com.m2f.template.designsystem.components.data.TerminalTable
import com.m2f.template.designsystem.components.data.TerminalTableCell
import com.m2f.template.designsystem.components.data.TerminalTableRow
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.components.feedback.TerminalTooltip
import com.m2f.template.designsystem.components.input.TerminalInput
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.models.GroupRole
import com.m2f.template.models.dto.InvitationResponse
import com.m2f.template.models.dto.MemberResponse
import com.m2f.template.models.localization.StringKey
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.stringResource
import template.app.admin.generated.resources.Res
import template.app.admin.generated.resources.admin_back
import template.app.admin.generated.resources.admin_create_group
import template.app.admin.generated.resources.admin_create_group_cancel
import template.app.admin.generated.resources.admin_create_group_name_label
import template.app.admin.generated.resources.admin_create_group_name_placeholder
import template.app.admin.generated.resources.admin_create_group_submit
import template.app.admin.generated.resources.admin_create_group_success
import template.app.admin.generated.resources.admin_create_group_title
import template.app.admin.generated.resources.admin_error_description
import template.app.admin.generated.resources.admin_error_title
import template.app.admin.generated.resources.admin_invite_cancel
import template.app.admin.generated.resources.admin_invite_done
import template.app.admin.generated.resources.admin_invite_email_label
import template.app.admin.generated.resources.admin_invite_email_placeholder
import template.app.admin.generated.resources.admin_invite_member
import template.app.admin.generated.resources.admin_invite_send
import template.app.admin.generated.resources.admin_invite_success
import template.app.admin.generated.resources.admin_invite_title
import template.app.admin.generated.resources.admin_load_more
import template.app.admin.generated.resources.admin_loading
import template.app.admin.generated.resources.admin_member_count
import template.app.admin.generated.resources.admin_no_group_description
import template.app.admin.generated.resources.admin_no_group_title
import template.app.admin.generated.resources.admin_remove_button
import template.app.admin.generated.resources.admin_remove_member_cancel
import template.app.admin.generated.resources.admin_remove_member_confirm
import template.app.admin.generated.resources.admin_remove_member_submit
import template.app.admin.generated.resources.admin_remove_member_title
import template.app.admin.generated.resources.admin_resend_button
import template.app.admin.generated.resources.admin_role_admin
import template.app.admin.generated.resources.admin_role_member
import template.app.admin.generated.resources.admin_role_owner
import template.app.admin.generated.resources.admin_slug_prefix
import template.app.admin.generated.resources.admin_table_actions
import template.app.admin.generated.resources.admin_table_email
import template.app.admin.generated.resources.admin_table_joined
import template.app.admin.generated.resources.admin_table_name
import template.app.admin.generated.resources.admin_table_role
import template.app.admin.generated.resources.admin_invitations_accepted
import template.app.admin.generated.resources.admin_invitations_actions
import template.app.admin.generated.resources.admin_invitations_email
import template.app.admin.generated.resources.admin_invitations_expired
import template.app.admin.generated.resources.admin_invitations_expires_days
import template.app.admin.generated.resources.admin_invitations_expires_today
import template.app.admin.generated.resources.admin_invitations_expires_tomorrow
import template.app.admin.generated.resources.admin_invitations_none
import template.app.admin.generated.resources.admin_invitations_revoked
import template.app.admin.generated.resources.admin_invitations_role
import template.app.admin.generated.resources.admin_invitations_status
import template.app.admin.generated.resources.admin_invitations_title
import template.app.admin.generated.resources.admin_revoke_button
import template.app.admin.generated.resources.admin_revoke_cancel
import template.app.admin.generated.resources.admin_revoke_confirm
import template.app.admin.generated.resources.admin_revoke_submit
import template.app.admin.generated.resources.admin_revoke_title
import template.app.admin.generated.resources.admin_title

/**
 * Stateless admin panel screen displaying group info and a paginated member table.
 *
 * Uses design system components (TerminalCard, TerminalTable, TerminalBadge, TerminalButton)
 * to render group details and member list with cursor-based pagination.
 *
 * @param state Current admin panel state with group info, members, and loading/error flags.
 * @param onLoadMore Callback to load the next page of members.
 * @param onRegisterMember Callback to navigate to the register-member form.
 * @param onBack Callback to navigate back.
 * @param onOpenCreateGroup Callback to open the create group dialog.
 * @param onCloseCreateGroup Callback to close the create group dialog.
 * @param onCreateGroupNameChange Callback when the group name input changes.
 * @param onSubmitCreateGroup Callback to submit the create group form.
 * @param onOpenInvite Callback to open the invite member dialog.
 * @param onCloseInvite Callback to close the invite member dialog.
 * @param onInviteEmailChange Callback when the invite email input changes.
 * @param onSendInvite Callback to send the invitation.
 * @param modifier Modifier for the screen root.
 */
@Composable
fun AdminPanelScreen(
    state: AdminPanelModel,
    onLoadMore: () -> Unit,
    onRegisterMember: () -> Unit,
    onBack: () -> Unit,
    onOpenCreateGroup: () -> Unit,
    onCloseCreateGroup: () -> Unit,
    onCreateGroupNameChange: (String) -> Unit,
    onSubmitCreateGroup: () -> Unit,
    onOpenInvite: () -> Unit,
    onCloseInvite: () -> Unit,
    onInviteEmailChange: (String) -> Unit,
    onSendInvite: () -> Unit,
    onConfirmRevoke: (InvitationResponse) -> Unit,
    onCancelRevoke: () -> Unit,
    onExecuteRevoke: () -> Unit,
    onResend: (InvitationResponse) -> Unit,
    onConfirmRemoveMember: (MemberResponse) -> Unit,
    onCancelRemoveMember: () -> Unit,
    onExecuteRemoveMember: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isMobile = maxWidth < 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(if (isMobile) 16.dp else 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Top bar: back button + title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TerminalButton(
                    text = stringResource(Res.string.admin_back),
                    onClick = onBack,
                )
                TerminalText(
                    text = stringResource(Res.string.admin_title),
                    style = typography.xxl.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
            }

            // Loading state (initial load)
            if (state.isLoading && state.members.isEmpty()) {
                TerminalText(
                    text = stringResource(Res.string.admin_loading),
                    style = typography.md,
                    color = colors.textMuted,
                )
                return@Column
            }

            // Success message after group creation
            if (state.createGroupSuccess) {
                TerminalAlert(
                    message = stringResource(Res.string.admin_create_group_success),
                    variant = AlertVariant.Success,
                )
            }

            // No group selected — system admin without group memberships
            if (state.groupId.isBlank() && !state.isLoading && state.error == null) {
                TerminalCard(
                    title = stringResource(Res.string.admin_no_group_title),
                    description = stringResource(Res.string.admin_no_group_description),
                    variant = CardVariant.Default,
                ) {}

                // Create Group button (for system admins without a group)
                TerminalButton(
                    text = stringResource(Res.string.admin_create_group),
                    onClick = onOpenCreateGroup,
                    variant = ButtonVariant.Secondary,
                )
                return@Column
            }

            // Error state (no data loaded)
            if (state.error != null && state.members.isEmpty()) {
                TerminalCard(
                    title = stringResource(Res.string.admin_error_title),
                    description = stringResource(Res.string.admin_error_description),
                    variant = CardVariant.Default,
                ) {
                    TerminalBadge(
                        text = "error: ${resolveStringKey(state.error)}",
                        variant = BadgeVariant.Error,
                    )
                }
                return@Column
            }

            // Group info card
            if (state.groupName.isNotBlank()) {
                TerminalCard(
                    title = state.groupName,
                    description = stringResource(Res.string.admin_slug_prefix, state.groupSlug),
                    variant = CardVariant.Default,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.groupDescription.isNotBlank()) {
                            TerminalText(
                                text = state.groupDescription,
                                style = typography.sm,
                                color = colors.textMuted,
                            )
                        }
                        TerminalBadge(
                            text = stringResource(Res.string.admin_member_count, state.memberCount),
                            variant = BadgeVariant.Accent,
                        )
                    }
                }
            }

            // Action buttons row: Invite Member
            // Note: Create Group is intentionally hidden when a group exists.
            // Multi-group support deferred to future iteration.
            TerminalButton(
                text = stringResource(Res.string.admin_invite_member),
                onClick = onOpenInvite,
                variant = ButtonVariant.Secondary,
            )

            // Members table
            if (state.members.isNotEmpty()) {
                val memberHeaders = buildList {
                    add(stringResource(Res.string.admin_table_name))
                    if (!isMobile) add(stringResource(Res.string.admin_table_email))
                    add(stringResource(Res.string.admin_table_role))
                    if (!isMobile) add(stringResource(Res.string.admin_table_joined))
                    add(stringResource(Res.string.admin_table_actions))
                }
                TerminalTable(
                    headers = memberHeaders,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.members.forEachIndexed { index, member ->
                        TerminalTableRow(
                            showBottomBorder = index < state.members.lastIndex,
                        ) {
                            TerminalTableCell(text = member.name)
                            if (!isMobile) {
                                TerminalTableCell(text = member.email, secondary = true)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                TerminalBadge(
                                    text = when (member.role) {
                                        is GroupRole.Owner -> stringResource(Res.string.admin_role_owner)
                                        is GroupRole.Admin -> stringResource(Res.string.admin_role_admin)
                                        is GroupRole.Member -> stringResource(Res.string.admin_role_member)
                                    },
                                    variant = when (member.role) {
                                        is GroupRole.Owner, is GroupRole.Admin -> BadgeVariant.Success
                                        is GroupRole.Member -> BadgeVariant.Default
                                    },
                                )
                            }
                            if (!isMobile) {
                                TerminalTableCell(
                                    text = formatJoinedDate(member.joinedAt),
                                    secondary = true,
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                if (member.role != GroupRole.Owner) {
                                    if (isMobile) {
                                        TerminalTooltip(text = stringResource(Res.string.admin_remove_button)) {
                                            TerminalIconButton(
                                                onClick = { onConfirmRemoveMember(member) },
                                                variant = ButtonVariant.Destructive,
                                            ) {
                                                TerminalText(
                                                    "\u2715",
                                                    style = TerminalTheme.typography.sm,
                                                    color = TerminalTheme.colors.btnDestructiveText,
                                                )
                                            }
                                        }
                                    } else {
                                        TerminalButton(
                                            text = stringResource(Res.string.admin_remove_button),
                                            onClick = { onConfirmRemoveMember(member) },
                                            variant = ButtonVariant.Destructive,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Load more button
            if (state.hasMoreMembers) {
                if (state.isLoadingMore) {
                    TerminalText(
                        text = stringResource(Res.string.admin_loading),
                        style = typography.sm,
                        color = colors.textMuted,
                    )
                } else {
                    TerminalButton(
                        text = stringResource(Res.string.admin_load_more),
                        onClick = onLoadMore,
                    )
                }
            }

            // Pending invitations section
            if (state.groupId.isNotBlank()) {
                InvitationsSection(
                    invitations = state.invitations,
                    isLoading = state.isLoadingInvitations,
                    isMobile = isMobile,
                    onConfirmRevoke = onConfirmRevoke,
                    onResend = onResend,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Create Group Dialog overlay
        if (state.showCreateGroupDialog) {
            CreateGroupDialog(
                groupName = state.createGroupName,
                isCreating = state.isCreatingGroup,
                error = state.createGroupError,
                onNameChange = onCreateGroupNameChange,
                onSubmit = onSubmitCreateGroup,
                onCancel = onCloseCreateGroup,
            )
        }

        // Invite Member Dialog overlay
        if (state.showInviteDialog) {
            InviteDialog(
                email = state.inviteEmail,
                isSending = state.isSendingInvite,
                error = state.inviteError,
                success = state.inviteSuccess,
                onEmailChange = onInviteEmailChange,
                onSend = onSendInvite,
                onClose = onCloseInvite,
            )
        }

        // Revoke Invitation Confirmation Dialog overlay
        if (state.showRevokeDialog && state.revokeTarget != null) {
            RevokeDialog(
                email = state.revokeTarget.email,
                isRevoking = state.isRevoking,
                onRevoke = onExecuteRevoke,
                onCancel = onCancelRevoke,
            )
        }

        // Remove Member Confirmation Dialog overlay
        if (state.showRemoveMemberDialog && state.removeMemberTarget != null) {
            RemoveMemberDialog(
                memberName = state.removeMemberTarget.name,
                isRemoving = state.isRemovingMember,
                onRemove = onExecuteRemoveMember,
                onCancel = onCancelRemoveMember,
            )
        }
    }
}

/**
 * Modal dialog for creating a new group.
 *
 * Displays a dark overlay with a centered card containing the group name input,
 * validation error display, and submit/cancel buttons.
 */
@Composable
private fun CreateGroupDialog(
    groupName: String,
    isCreating: Boolean,
    error: StringKey?,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCancel,
            ),
        contentAlignment = Alignment.Center,
    ) {
        TerminalCard(
            title = stringResource(Res.string.admin_create_group_title),
            modifier = Modifier
                .widthIn(max = 400.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}, // Prevent click through to overlay
                ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (error != null) {
                    TerminalAlert(
                        message = resolveStringKey(error),
                        variant = AlertVariant.Error,
                    )
                }

                TerminalInput(
                    value = groupName,
                    onValueChange = onNameChange,
                    label = stringResource(Res.string.admin_create_group_name_label),
                    placeholder = stringResource(Res.string.admin_create_group_name_placeholder),
                    enabled = !isCreating,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TerminalButton(
                        text = stringResource(Res.string.admin_create_group_cancel),
                        onClick = onCancel,
                        variant = ButtonVariant.Ghost,
                        enabled = !isCreating,
                    )
                    TerminalButton(
                        text = if (isCreating) "..." else stringResource(Res.string.admin_create_group_submit),
                        onClick = onSubmit,
                        enabled = !isCreating && groupName.isNotBlank(),
                    )
                }
            }
        }
    }
}

/**
 * Modal dialog for inviting a new member via email.
 *
 * Displays a dark overlay with a centered card containing the email input,
 * validation error display, send button, and success state message.
 * Note: Invitation link is only sent via email for security.
 */
@Composable
private fun InviteDialog(
    email: String,
    isSending: Boolean,
    error: StringKey?,
    success: Boolean,
    onEmailChange: (String) -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose,
            ),
        contentAlignment = Alignment.Center,
    ) {
        TerminalCard(
            title = stringResource(Res.string.admin_invite_title),
            modifier = Modifier
                .widthIn(max = 400.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}, // Prevent click through to overlay
                ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (success) {
                    // Success state - link was emailed to the invitee
                    TerminalAlert(
                        message = stringResource(Res.string.admin_invite_success),
                        variant = AlertVariant.Success,
                    )
                    TerminalButton(
                        text = stringResource(Res.string.admin_invite_done),
                        onClick = onClose,
                    )
                } else {
                    // Input state
                    if (error != null) {
                        TerminalAlert(
                            message = resolveStringKey(error),
                            variant = AlertVariant.Error,
                        )
                    }

                    TerminalInput(
                        value = email,
                        onValueChange = onEmailChange,
                        label = stringResource(Res.string.admin_invite_email_label),
                        placeholder = stringResource(Res.string.admin_invite_email_placeholder),
                        enabled = !isSending,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TerminalButton(
                            text = stringResource(Res.string.admin_invite_cancel),
                            onClick = onClose,
                            variant = ButtonVariant.Ghost,
                            enabled = !isSending,
                        )
                        TerminalButton(
                            text = if (isSending) "..." else stringResource(Res.string.admin_invite_send),
                            onClick = onSend,
                            enabled = !isSending && email.isNotBlank(),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section displaying pending group invitations in a table.
 *
 * Shows a TerminalCard with header "Pending Invitations" and a table with
 * Email, Role, Status, and Actions columns. On mobile, the Role column is hidden
 * and action text buttons become icon buttons with tooltips.
 */
@Composable
private fun InvitationsSection(
    invitations: List<InvitationResponse>,
    isLoading: Boolean,
    isMobile: Boolean,
    onConfirmRevoke: (InvitationResponse) -> Unit,
    onResend: (InvitationResponse) -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    TerminalCard(
        title = stringResource(Res.string.admin_invitations_title),
        variant = CardVariant.Default,
    ) {
        if (isLoading) {
            TerminalText(
                text = stringResource(Res.string.admin_loading),
                style = typography.sm,
                color = colors.textMuted,
            )
        } else if (invitations.isEmpty()) {
            TerminalText(
                text = stringResource(Res.string.admin_invitations_none),
                style = typography.sm,
                color = colors.textMuted,
            )
        } else {
            val invitationHeaders = buildList {
                add(stringResource(Res.string.admin_invitations_email))
                if (!isMobile) add(stringResource(Res.string.admin_invitations_role))
                add(stringResource(Res.string.admin_invitations_status))
                add(stringResource(Res.string.admin_invitations_actions))
            }
            TerminalTable(
                headers = invitationHeaders,
                modifier = Modifier.fillMaxWidth(),
            ) {
                invitations.forEachIndexed { index, invitation ->
                    TerminalTableRow(
                        showBottomBorder = index < invitations.lastIndex,
                    ) {
                        TerminalTableCell(text = invitation.email)
                        if (!isMobile) {
                            TerminalTableCell(
                                text = when (invitation.role.uppercase()) {
                                    "OWNER" -> stringResource(Res.string.admin_role_owner)
                                    "ADMIN" -> stringResource(Res.string.admin_role_admin)
                                    else -> stringResource(Res.string.admin_role_member)
                                },
                                secondary = true,
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            when {
                                invitation.isAccepted -> TerminalBadge(
                                    text = stringResource(Res.string.admin_invitations_accepted),
                                    variant = BadgeVariant.Success,
                                )
                                invitation.isRevoked -> TerminalBadge(
                                    text = stringResource(Res.string.admin_invitations_revoked),
                                    variant = BadgeVariant.Error,
                                )
                                invitation.isExpired -> TerminalBadge(
                                    text = stringResource(Res.string.admin_invitations_expired),
                                    variant = BadgeVariant.Warning,
                                )
                                else -> {
                                    val daysLeft = computeDaysUntilExpiry(invitation.expiresAt)
                                    val expiryText = when {
                                        daysLeft <= 0 -> stringResource(Res.string.admin_invitations_expires_today)
                                        daysLeft == 1 -> stringResource(Res.string.admin_invitations_expires_tomorrow)
                                        else -> stringResource(Res.string.admin_invitations_expires_days, daysLeft)
                                    }
                                    TerminalBadge(
                                        text = expiryText,
                                        variant = BadgeVariant.Accent,
                                    )
                                }
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!invitation.isAccepted && !invitation.isRevoked && !invitation.isExpired) {
                                    if (isMobile) {
                                        TerminalTooltip(text = stringResource(Res.string.admin_revoke_button)) {
                                            TerminalIconButton(
                                                onClick = { onConfirmRevoke(invitation) },
                                                variant = ButtonVariant.Destructive,
                                            ) {
                                                TerminalText(
                                                    "\u2715",
                                                    style = TerminalTheme.typography.sm,
                                                    color = TerminalTheme.colors.btnDestructiveText,
                                                )
                                            }
                                        }
                                    } else {
                                        TerminalButton(
                                            text = stringResource(Res.string.admin_revoke_button),
                                            onClick = { onConfirmRevoke(invitation) },
                                            variant = ButtonVariant.Destructive,
                                        )
                                    }
                                }
                                if (invitation.isExpired || invitation.isRevoked) {
                                    if (isMobile) {
                                        TerminalTooltip(text = stringResource(Res.string.admin_resend_button)) {
                                            TerminalIconButton(
                                                onClick = { onResend(invitation) },
                                                variant = ButtonVariant.Secondary,
                                            ) {
                                                TerminalText(
                                                    "\u21BB",
                                                    style = TerminalTheme.typography.sm,
                                                    color = TerminalTheme.colors.btnSecondaryText,
                                                )
                                            }
                                        }
                                    } else {
                                        TerminalButton(
                                            text = stringResource(Res.string.admin_resend_button),
                                            onClick = { onResend(invitation) },
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
    }
}

/**
 * Modal confirmation dialog for revoking an invitation.
 *
 * Displays a dark overlay with a centered card containing a confirmation message
 * and Revoke/Cancel buttons.
 */
@Composable
private fun RevokeDialog(
    email: String,
    isRevoking: Boolean,
    onRevoke: () -> Unit,
    onCancel: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCancel,
            ),
        contentAlignment = Alignment.Center,
    ) {
        TerminalCard(
            title = stringResource(Res.string.admin_revoke_title),
            modifier = Modifier
                .widthIn(max = 400.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}, // Prevent click through to overlay
                ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TerminalText(
                    text = stringResource(Res.string.admin_revoke_confirm, email),
                    style = TerminalTheme.typography.sm,
                    color = TerminalTheme.colors.text,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TerminalButton(
                        text = stringResource(Res.string.admin_revoke_cancel),
                        onClick = onCancel,
                        variant = ButtonVariant.Ghost,
                        enabled = !isRevoking,
                    )
                    TerminalButton(
                        text = if (isRevoking) "..." else stringResource(Res.string.admin_revoke_submit),
                        onClick = onRevoke,
                        variant = ButtonVariant.Destructive,
                        enabled = !isRevoking,
                    )
                }
            }
        }
    }
}

/**
 * Modal confirmation dialog for removing a member from the group.
 *
 * Displays a dark overlay with a centered card containing a confirmation message
 * and Remove/Cancel buttons.
 */
@Composable
private fun RemoveMemberDialog(
    memberName: String,
    isRemoving: Boolean,
    onRemove: () -> Unit,
    onCancel: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCancel,
            ),
        contentAlignment = Alignment.Center,
    ) {
        TerminalCard(
            title = stringResource(Res.string.admin_remove_member_title),
            modifier = Modifier
                .widthIn(max = 400.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}, // Prevent click through to overlay
                ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TerminalText(
                    text = stringResource(Res.string.admin_remove_member_confirm, memberName),
                    style = TerminalTheme.typography.sm,
                    color = TerminalTheme.colors.text,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TerminalButton(
                        text = stringResource(Res.string.admin_remove_member_cancel),
                        onClick = onCancel,
                        variant = ButtonVariant.Ghost,
                        enabled = !isRemoving,
                    )
                    TerminalButton(
                        text = if (isRemoving) "..." else stringResource(Res.string.admin_remove_member_submit),
                        onClick = onRemove,
                        variant = ButtonVariant.Destructive,
                        enabled = !isRemoving,
                    )
                }
            }
        }
    }
}

/**
 * Formats an ISO-8601 date string (e.g. "2026-03-02T12:34:56") as "DD/MM/YYYY".
 * Returns "-" if the string is blank or parsing fails.
 */
private fun formatJoinedDate(isoDate: String): String {
    if (isoDate.isBlank()) return "-"
    return try {
        val dateTime = LocalDateTime.parse(isoDate)
        val day = dateTime.day.toString().padStart(2, '0')
        val month = dateTime.monthNumber.toString().padStart(2, '0')
        val year = dateTime.year.toString()
        "$day/$month/$year"
    } catch (_: Exception) {
        "-"
    }
}

/**
 * Computes the number of days until the given expiry date string.
 *
 * Parses the ISO-8601 [LocalDateTime] string (assumed UTC) and returns the
 * number of whole days remaining. Returns 0 if the date has already passed
 * or if parsing fails.
 */
private fun computeDaysUntilExpiry(expiresAt: String): Int {
    return try {
        val expiryInstant = LocalDateTime.parse(expiresAt).toInstant(TimeZone.UTC)
        val now = Clock.System.now()
        val diffMs = expiryInstant.toEpochMilliseconds() - now.toEpochMilliseconds()
        if (diffMs <= 0L) 0 else (diffMs / (1000L * 60 * 60 * 24)).toInt()
    } catch (_: Exception) {
        0
    }
}
