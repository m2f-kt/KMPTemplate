package com.m2f.template.app.profile.tier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.app.profile.ProfileModel
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.card.TerminalCard
import com.m2f.template.designsystem.components.data.TerminalList
import com.m2f.template.designsystem.components.data.TerminalListItem
import com.m2f.template.designsystem.components.data.TerminalTable
import com.m2f.template.designsystem.components.data.TerminalTableCell
import com.m2f.template.designsystem.components.data.TerminalTableRow
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.components.feedback.TerminalProgress
import com.m2f.template.designsystem.theme.TerminalTheme
import org.jetbrains.compose.resources.stringResource
import template.app.profile.generated.resources.Res
import template.app.profile.generated.resources.tier_admin_active_sessions
import template.app.profile.generated.resources.tier_admin_analytics_desc
import template.app.profile.generated.resources.tier_admin_analytics_title
import template.app.profile.generated.resources.tier_admin_api_calls_today
import template.app.profile.generated.resources.tier_admin_audit_api_key
import template.app.profile.generated.resources.tier_admin_audit_desc
import template.app.profile.generated.resources.tier_admin_audit_group_created
import template.app.profile.generated.resources.tier_admin_audit_role_changed
import template.app.profile.generated.resources.tier_admin_audit_title
import template.app.profile.generated.resources.tier_admin_audit_user_invited
import template.app.profile.generated.resources.tier_admin_audit_webhook_added
import template.app.profile.generated.resources.tier_admin_group_engineering
import template.app.profile.generated.resources.tier_admin_group_engineering_sub
import template.app.profile.generated.resources.tier_admin_group_operations
import template.app.profile.generated.resources.tier_admin_group_operations_sub
import template.app.profile.generated.resources.tier_admin_group_security
import template.app.profile.generated.resources.tier_admin_group_security_sub
import template.app.profile.generated.resources.tier_admin_groups_desc
import template.app.profile.generated.resources.tier_admin_groups_title
import template.app.profile.generated.resources.tier_admin_mfa_required
import template.app.profile.generated.resources.tier_admin_org_name
import template.app.profile.generated.resources.tier_admin_org_plan
import template.app.profile.generated.resources.tier_admin_org_seats_total
import template.app.profile.generated.resources.tier_admin_org_seats_used
import template.app.profile.generated.resources.tier_admin_org_sso_enabled
import template.app.profile.generated.resources.tier_admin_perm_admin
import template.app.profile.generated.resources.tier_admin_perm_delete
import template.app.profile.generated.resources.tier_admin_perm_read
import template.app.profile.generated.resources.tier_admin_perm_write
import template.app.profile.generated.resources.tier_admin_perms_desc
import template.app.profile.generated.resources.tier_admin_perms_title
import template.app.profile.generated.resources.tier_admin_settings_desc
import template.app.profile.generated.resources.tier_admin_settings_title
import template.app.profile.generated.resources.tier_admin_status_active
import template.app.profile.generated.resources.tier_admin_status_inactive
import template.app.profile.generated.resources.tier_admin_status_pending
import template.app.profile.generated.resources.tier_admin_storage_label
import template.app.profile.generated.resources.tier_admin_storage_used
import template.app.profile.generated.resources.tier_admin_table_email
import template.app.profile.generated.resources.tier_admin_table_last_active
import template.app.profile.generated.resources.tier_admin_table_name
import template.app.profile.generated.resources.tier_admin_table_perm
import template.app.profile.generated.resources.tier_admin_table_role
import template.app.profile.generated.resources.tier_admin_table_status
import template.app.profile.generated.resources.tier_admin_tier_admin_col
import template.app.profile.generated.resources.tier_admin_tier_free_col
import template.app.profile.generated.resources.tier_admin_tier_paid_col
import template.app.profile.generated.resources.tier_admin_tier_premium_col
import template.app.profile.generated.resources.tier_admin_total_users
import template.app.profile.generated.resources.tier_admin_users_desc
import template.app.profile.generated.resources.tier_admin_users_title

/**
 * Admin tier profile content showing user management table, groups, permissions matrix,
 * analytics, audit log, and org settings.
 *
 * All data is static/mock -- demonstrates what an admin user would see.
 *
 * @param state The current profile state.
 * @param modifier Modifier for the content root.
 */
@Composable
fun AdminTierContent(
    state: ProfileModel,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // User management table
        TerminalCard(
            title = stringResource(Res.string.tier_admin_users_title),
            description = stringResource(Res.string.tier_admin_users_desc),
        ) {
            TerminalTable(
                headers = listOf(
                    stringResource(Res.string.tier_admin_table_name),
                    stringResource(Res.string.tier_admin_table_email),
                    stringResource(Res.string.tier_admin_table_role),
                    stringResource(Res.string.tier_admin_table_status),
                    stringResource(Res.string.tier_admin_table_last_active),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = state.name.ifBlank { "Admin" })
                    TerminalTableCell(text = state.email, secondary = true)
                    TerminalTableCell(text = "admin")
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = stringResource(Res.string.tier_admin_status_active), variant = BadgeVariant.Success)
                    }
                    TerminalTableCell(text = "now", secondary = true)
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "Jordan Lee")
                    TerminalTableCell(text = "jordan@org.dev", secondary = true)
                    TerminalTableCell(text = "premium")
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = stringResource(Res.string.tier_admin_status_active), variant = BadgeVariant.Success)
                    }
                    TerminalTableCell(text = "2h ago", secondary = true)
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "Riley Kim")
                    TerminalTableCell(text = "riley@org.dev", secondary = true)
                    TerminalTableCell(text = "paid")
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = stringResource(Res.string.tier_admin_status_active), variant = BadgeVariant.Success)
                    }
                    TerminalTableCell(text = "1d ago", secondary = true)
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "Casey Morgan")
                    TerminalTableCell(text = "casey@org.dev", secondary = true)
                    TerminalTableCell(text = "free")
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = stringResource(Res.string.tier_admin_status_inactive), variant = BadgeVariant.Warning)
                    }
                    TerminalTableCell(text = "14d ago", secondary = true)
                }
                TerminalTableRow(showBottomBorder = false) {
                    TerminalTableCell(text = "Drew Park")
                    TerminalTableCell(text = "drew@org.dev", secondary = true)
                    TerminalTableCell(text = "viewer")
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = stringResource(Res.string.tier_admin_status_pending), variant = BadgeVariant.Warning)
                    }
                    TerminalTableCell(text = "never", secondary = true)
                }
            }
        }

        // Groups section
        TerminalCard(
            title = stringResource(Res.string.tier_admin_groups_title),
            description = stringResource(Res.string.tier_admin_groups_desc),
        ) {
            TerminalList {
                TerminalListItem(
                    text = stringResource(Res.string.tier_admin_group_engineering),
                    subtitle = stringResource(Res.string.tier_admin_group_engineering_sub),
                    trailingContent = { color ->
                        TerminalBadge(text = "8", variant = BadgeVariant.Accent)
                    },
                )
                TerminalListItem(
                    text = stringResource(Res.string.tier_admin_group_operations),
                    subtitle = stringResource(Res.string.tier_admin_group_operations_sub),
                    trailingContent = { color ->
                        TerminalBadge(text = "4", variant = BadgeVariant.Accent)
                    },
                )
                TerminalListItem(
                    text = stringResource(Res.string.tier_admin_group_security),
                    subtitle = stringResource(Res.string.tier_admin_group_security_sub),
                    trailingContent = { color ->
                        TerminalBadge(text = "3", variant = BadgeVariant.Accent)
                    },
                )
            }
        }

        // Permissions matrix
        TerminalCard(
            title = stringResource(Res.string.tier_admin_perms_title),
            description = stringResource(Res.string.tier_admin_perms_desc),
        ) {
            TerminalTable(
                headers = listOf(
                    stringResource(Res.string.tier_admin_table_perm),
                    stringResource(Res.string.tier_admin_tier_free_col),
                    stringResource(Res.string.tier_admin_tier_paid_col),
                    stringResource(Res.string.tier_admin_tier_premium_col),
                    stringResource(Res.string.tier_admin_tier_admin_col),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = stringResource(Res.string.tier_admin_perm_read))
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = stringResource(Res.string.tier_admin_perm_write))
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = stringResource(Res.string.tier_admin_perm_admin))
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2713")
                }
                TerminalTableRow(showBottomBorder = false) {
                    TerminalTableCell(text = stringResource(Res.string.tier_admin_perm_delete))
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2713")
                }
            }
        }

        // Analytics
        TerminalCard(
            title = stringResource(Res.string.tier_admin_analytics_title),
            description = stringResource(Res.string.tier_admin_analytics_desc),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AdminMetricRow(label = stringResource(Res.string.tier_admin_total_users), value = "24")
                AdminMetricRow(label = stringResource(Res.string.tier_admin_active_sessions), value = "12")
                AdminMetricRow(label = stringResource(Res.string.tier_admin_api_calls_today), value = "45,230")
                AdminMetricRow(label = stringResource(Res.string.tier_admin_storage_used), value = "12.4 GB / 50 GB")

                TerminalProgress(
                    progress = 0.248f,
                    label = stringResource(Res.string.tier_admin_storage_label),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Audit log
        TerminalCard(
            title = stringResource(Res.string.tier_admin_audit_title),
            description = stringResource(Res.string.tier_admin_audit_desc),
        ) {
            TerminalList {
                TerminalListItem(
                    text = stringResource(Res.string.tier_admin_audit_role_changed),
                    subtitle = "// 2 hours ago | by ${state.email}",
                )
                TerminalListItem(
                    text = stringResource(Res.string.tier_admin_audit_api_key),
                    subtitle = "// 5 hours ago | by jordan@org.dev",
                )
                TerminalListItem(
                    text = stringResource(Res.string.tier_admin_audit_group_created),
                    subtitle = "// 1 day ago | by ${state.email}",
                )
                TerminalListItem(
                    text = stringResource(Res.string.tier_admin_audit_user_invited),
                    subtitle = "// 2 days ago | by ${state.email}",
                )
                TerminalListItem(
                    text = stringResource(Res.string.tier_admin_audit_webhook_added),
                    subtitle = "// 3 days ago | by riley@org.dev",
                )
            }
        }

        // Org settings
        TerminalCard(
            title = stringResource(Res.string.tier_admin_settings_title),
            description = stringResource(Res.string.tier_admin_settings_desc),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AdminMetricRow(label = stringResource(Res.string.tier_admin_org_name), value = "Terminal Corp")
                AdminMetricRow(label = stringResource(Res.string.tier_admin_org_plan), value = "Enterprise")
                AdminMetricRow(label = stringResource(Res.string.tier_admin_org_seats_total), value = "50")
                AdminMetricRow(label = stringResource(Res.string.tier_admin_org_seats_used), value = "24")
                AdminMetricRow(label = stringResource(Res.string.tier_admin_org_sso_enabled), value = "true")
                AdminMetricRow(label = stringResource(Res.string.tier_admin_mfa_required), value = "true")
            }
        }
    }
}

@Composable
private fun AdminMetricRow(label: String, value: String) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TerminalText(
            text = label,
            style = typography.sm,
            color = colors.textMuted,
        )
        TerminalText(
            text = value,
            style = typography.sm.copy(fontWeight = FontWeight.Medium),
            color = colors.text,
        )
    }
}
