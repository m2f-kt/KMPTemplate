package com.m2f.template.app.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.TerminalButton
import com.m2f.template.designsystem.components.card.CardVariant
import com.m2f.template.designsystem.components.card.TerminalCard
import com.m2f.template.designsystem.components.data.TerminalTable
import com.m2f.template.designsystem.components.data.TerminalTableCell
import com.m2f.template.designsystem.components.data.TerminalTableRow
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.models.GroupRole

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
 * @param modifier Modifier for the screen root.
 */
@Composable
fun AdminPanelScreen(
    state: AdminPanelModel,
    onLoadMore: () -> Unit,
    onRegisterMember: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
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
                text = "<-",
                onClick = onBack,
            )
            TerminalText(
                text = "> admin_panel",
                style = typography.xxl.copy(fontWeight = FontWeight.Bold),
                color = colors.text,
            )
        }

        // Loading state (initial load)
        if (state.isLoading && state.members.isEmpty()) {
            TerminalText(
                text = "// loading...",
                style = typography.md,
                color = colors.textMuted,
            )
            return@Column
        }

        // Error state (no data loaded)
        if (state.error != null && state.members.isEmpty()) {
            TerminalCard(
                title = "error",
                description = "// failed to load admin panel",
                variant = CardVariant.Default,
            ) {
                TerminalBadge(
                    text = "error: ${state.error.code}",
                    variant = BadgeVariant.Error,
                )
            }
            return@Column
        }

        // Group info card
        if (state.groupName.isNotBlank()) {
            TerminalCard(
                title = state.groupName,
                description = "slug: ${state.groupSlug}",
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
                        text = "${state.memberCount} members",
                        variant = BadgeVariant.Accent,
                    )
                }
            }
        }

        // Members table
        if (state.members.isNotEmpty()) {
            TerminalTable(
                headers = listOf("NAME", "EMAIL", "ROLE", "JOINED"),
                modifier = Modifier.fillMaxWidth(),
            ) {
                state.members.forEachIndexed { index, member ->
                    TerminalTableRow(
                        showBottomBorder = index < state.members.lastIndex,
                    ) {
                        TerminalTableCell(text = member.name)
                        TerminalTableCell(text = member.email, secondary = true)
                        TerminalBadge(
                            text = member.role.value,
                            variant = when (member.role) {
                                is GroupRole.Owner, is GroupRole.Admin -> BadgeVariant.Success
                                is GroupRole.Member -> BadgeVariant.Default
                            },
                        )
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
                    text = "// loading...",
                    style = typography.sm,
                    color = colors.textMuted,
                )
            } else {
                TerminalButton(
                    text = "load_more",
                    onClick = onLoadMore,
                )
            }
        }

        // Register member button
        TerminalButton(
            text = "+ register_member",
            onClick = onRegisterMember,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
