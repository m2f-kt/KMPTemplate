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
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.components.card.AuraCard
import com.m2f.template.designsystem.components.data.AuraList
import com.m2f.template.designsystem.components.data.AuraListItem
import com.m2f.template.designsystem.components.data.AuraTable
import com.m2f.template.designsystem.components.data.AuraTableCell
import com.m2f.template.designsystem.components.data.AuraTableRow
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.AuraBadge
import com.m2f.template.designsystem.components.feedback.AuraProgress
import com.m2f.template.designsystem.theme.AuraTheme
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
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // User management table
        AuraCard(
            title = stringResource(Res.string.tier_admin_users_title),
            description = stringResource(Res.string.tier_admin_users_desc),
        ) {
            AuraTable(
                headers = listOf(
                    stringResource(Res.string.tier_admin_table_name),
                    stringResource(Res.string.tier_admin_table_email),
                    stringResource(Res.string.tier_admin_table_role),
                    stringResource(Res.string.tier_admin_table_status),
                    stringResource(Res.string.tier_admin_table_last_active),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = state.name.ifBlank { "Admin" })
                    AuraTableCell(text = state.email, secondary = true)
                    AuraTableCell(text = "admin")
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = stringResource(Res.string.tier_admin_status_active), variant = BadgeVariant.Success)
                    }
                    AuraTableCell(text = "now", secondary = true)
                }
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = "Jordan Lee")
                    AuraTableCell(text = "jordan@org.dev", secondary = true)
                    AuraTableCell(text = "premium")
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = stringResource(Res.string.tier_admin_status_active), variant = BadgeVariant.Success)
                    }
                    AuraTableCell(text = "2h ago", secondary = true)
                }
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = "Riley Kim")
                    AuraTableCell(text = "riley@org.dev", secondary = true)
                    AuraTableCell(text = "paid")
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = stringResource(Res.string.tier_admin_status_active), variant = BadgeVariant.Success)
                    }
                    AuraTableCell(text = "1d ago", secondary = true)
                }
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = "Casey Morgan")
                    AuraTableCell(text = "casey@org.dev", secondary = true)
                    AuraTableCell(text = "free")
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = stringResource(Res.string.tier_admin_status_inactive), variant = BadgeVariant.Warning)
                    }
                    AuraTableCell(text = "14d ago", secondary = true)
                }
                AuraTableRow(showBottomBorder = false) {
                    AuraTableCell(text = "Drew Park")
                    AuraTableCell(text = "drew@org.dev", secondary = true)
                    AuraTableCell(text = "viewer")
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = stringResource(Res.string.tier_admin_status_pending), variant = BadgeVariant.Warning)
                    }
                    AuraTableCell(text = "never", secondary = true)
                }
            }
        }

        // Groups section
        AuraCard(
            title = stringResource(Res.string.tier_admin_groups_title),
            description = stringResource(Res.string.tier_admin_groups_desc),
        ) {
            AuraList {
                AuraListItem(
                    text = stringResource(Res.string.tier_admin_group_engineering),
                    subtitle = stringResource(Res.string.tier_admin_group_engineering_sub),
                    trailingContent = { color ->
                        AuraBadge(text = "8", variant = BadgeVariant.Accent)
                    },
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_admin_group_operations),
                    subtitle = stringResource(Res.string.tier_admin_group_operations_sub),
                    trailingContent = { color ->
                        AuraBadge(text = "4", variant = BadgeVariant.Accent)
                    },
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_admin_group_security),
                    subtitle = stringResource(Res.string.tier_admin_group_security_sub),
                    trailingContent = { color ->
                        AuraBadge(text = "3", variant = BadgeVariant.Accent)
                    },
                )
            }
        }

        // Permissions matrix
        AuraCard(
            title = stringResource(Res.string.tier_admin_perms_title),
            description = stringResource(Res.string.tier_admin_perms_desc),
        ) {
            AuraTable(
                headers = listOf(
                    stringResource(Res.string.tier_admin_table_perm),
                    stringResource(Res.string.tier_admin_tier_free_col),
                    stringResource(Res.string.tier_admin_tier_paid_col),
                    stringResource(Res.string.tier_admin_tier_premium_col),
                    stringResource(Res.string.tier_admin_tier_admin_col),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = stringResource(Res.string.tier_admin_perm_read))
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                }
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = stringResource(Res.string.tier_admin_perm_write))
                    AuraTableCell(text = "\u2717")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                }
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = stringResource(Res.string.tier_admin_perm_admin))
                    AuraTableCell(text = "\u2717")
                    AuraTableCell(text = "\u2717")
                    AuraTableCell(text = "\u2717")
                    AuraTableCell(text = "\u2713")
                }
                AuraTableRow(showBottomBorder = false) {
                    AuraTableCell(text = stringResource(Res.string.tier_admin_perm_delete))
                    AuraTableCell(text = "\u2717")
                    AuraTableCell(text = "\u2717")
                    AuraTableCell(text = "\u2717")
                    AuraTableCell(text = "\u2713")
                }
            }
        }

        // Analytics
        AuraCard(
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

                AuraProgress(
                    progress = 0.248f,
                    label = stringResource(Res.string.tier_admin_storage_label),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Audit log
        AuraCard(
            title = stringResource(Res.string.tier_admin_audit_title),
            description = stringResource(Res.string.tier_admin_audit_desc),
        ) {
            AuraList {
                AuraListItem(
                    text = stringResource(Res.string.tier_admin_audit_role_changed),
                    subtitle = "// 2 hours ago | by ${state.email}",
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_admin_audit_api_key),
                    subtitle = "// 5 hours ago | by jordan@org.dev",
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_admin_audit_group_created),
                    subtitle = "// 1 day ago | by ${state.email}",
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_admin_audit_user_invited),
                    subtitle = "// 2 days ago | by ${state.email}",
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_admin_audit_webhook_added),
                    subtitle = "// 3 days ago | by riley@org.dev",
                )
            }
        }

        // Org settings
        AuraCard(
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
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AuraText(
            text = label,
            style = typography.sm,
            color = colors.textMuted,
        )
        AuraText(
            text = value,
            style = typography.sm.copy(fontWeight = FontWeight.Medium),
            color = colors.text,
        )
    }
}
