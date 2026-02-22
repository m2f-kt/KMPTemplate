package com.m2f.template.app.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
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
import com.m2f.template.designsystem.components.card.CardVariant
import com.m2f.template.designsystem.components.card.TerminalCard
import com.m2f.template.designsystem.components.data.TerminalTable
import com.m2f.template.designsystem.components.data.TerminalTableCell
import com.m2f.template.designsystem.components.data.TerminalTableRow
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.components.input.TerminalInput
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.models.GroupRole
import com.m2f.template.models.localization.StringKey
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
import template.app.admin.generated.resources.admin_invite_link_label
import template.app.admin.generated.resources.admin_invite_member
import template.app.admin.generated.resources.admin_invite_send
import template.app.admin.generated.resources.admin_invite_success
import template.app.admin.generated.resources.admin_invite_title
import template.app.admin.generated.resources.admin_load_more
import template.app.admin.generated.resources.admin_loading
import template.app.admin.generated.resources.admin_member_count
import template.app.admin.generated.resources.admin_no_group_description
import template.app.admin.generated.resources.admin_no_group_title
import template.app.admin.generated.resources.admin_register_member_button
import template.app.admin.generated.resources.admin_slug_prefix
import template.app.admin.generated.resources.admin_table_email
import template.app.admin.generated.resources.admin_table_joined
import template.app.admin.generated.resources.admin_table_name
import template.app.admin.generated.resources.admin_table_role
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
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
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

            // Action buttons row: Invite Member + Register Member
            // Note: Create Group is intentionally hidden when a group exists.
            // Multi-group support deferred to future iteration.
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TerminalButton(
                    text = stringResource(Res.string.admin_invite_member),
                    onClick = onOpenInvite,
                    variant = ButtonVariant.Secondary,
                )
                TerminalButton(
                    text = stringResource(Res.string.admin_register_member_button),
                    onClick = onRegisterMember,
                )
            }

            // Members table
            if (state.members.isNotEmpty()) {
                TerminalTable(
                    headers = listOf(
                        stringResource(Res.string.admin_table_name),
                        stringResource(Res.string.admin_table_email),
                        stringResource(Res.string.admin_table_role),
                        stringResource(Res.string.admin_table_joined),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.members.forEachIndexed { index, member ->
                        TerminalTableRow(
                            showBottomBorder = index < state.members.lastIndex,
                        ) {
                            TerminalTableCell(text = member.name)
                            TerminalTableCell(text = member.email, secondary = true)
                            Box(modifier = Modifier.weight(1f)) {
                                TerminalBadge(
                                    text = member.role.value,
                                    variant = when (member.role) {
                                        is GroupRole.Owner, is GroupRole.Admin -> BadgeVariant.Success
                                        is GroupRole.Member -> BadgeVariant.Default
                                    },
                                )
                            }
                            TerminalTableCell(
                                text = member.joinedAt.ifBlank { "-" },
                                secondary = true,
                            )
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
                inviteLink = state.inviteLink,
                onEmailChange = onInviteEmailChange,
                onSend = onSendInvite,
                onClose = onCloseInvite,
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
 * validation error display, send button, and success state with shareable link.
 */
@Composable
private fun InviteDialog(
    email: String,
    isSending: Boolean,
    error: StringKey?,
    success: Boolean,
    inviteLink: String?,
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
                if (success && inviteLink != null) {
                    // Success state
                    TerminalAlert(
                        message = stringResource(Res.string.admin_invite_success),
                        variant = AlertVariant.Success,
                    )
                    TerminalText(
                        text = stringResource(Res.string.admin_invite_link_label),
                        style = typography.sm,
                        color = colors.textMuted,
                    )
                    // Selectable link text
                    SelectionContainer {
                        TerminalText(
                            text = inviteLink,
                            style = typography.sm,
                            color = colors.accent,
                        )
                    }
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
