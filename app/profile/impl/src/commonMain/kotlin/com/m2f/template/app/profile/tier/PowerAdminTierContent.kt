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
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.AuraButton
import com.m2f.template.designsystem.components.card.CardVariant
import com.m2f.template.designsystem.components.card.AuraCard
import com.m2f.template.designsystem.components.data.AuraList
import com.m2f.template.designsystem.components.data.AuraListItem
import com.m2f.template.designsystem.components.data.AuraTable
import com.m2f.template.designsystem.components.data.AuraTableCell
import com.m2f.template.designsystem.components.data.AuraTableRow
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.AuraAlert
import com.m2f.template.designsystem.components.feedback.AuraBadge
import com.m2f.template.designsystem.components.feedback.AuraProgress
import com.m2f.template.designsystem.theme.AuraTheme
import org.jetbrains.compose.resources.stringResource
import template.app.profile.generated.resources.Res
import template.app.profile.generated.resources.tier_poweradmin_access_admin_col
import template.app.profile.generated.resources.tier_poweradmin_access_delete_col
import template.app.profile.generated.resources.tier_poweradmin_access_desc
import template.app.profile.generated.resources.tier_poweradmin_access_read_col
import template.app.profile.generated.resources.tier_poweradmin_access_resource_col
import template.app.profile.generated.resources.tier_poweradmin_access_title
import template.app.profile.generated.resources.tier_poweradmin_access_write_col
import template.app.profile.generated.resources.tier_poweradmin_btn_logout_all
import template.app.profile.generated.resources.tier_poweradmin_btn_purge
import template.app.profile.generated.resources.tier_poweradmin_btn_reset
import template.app.profile.generated.resources.tier_poweradmin_danger_alert_message
import template.app.profile.generated.resources.tier_poweradmin_danger_alert_title
import template.app.profile.generated.resources.tier_poweradmin_danger_desc
import template.app.profile.generated.resources.tier_poweradmin_danger_force_logout
import template.app.profile.generated.resources.tier_poweradmin_danger_force_logout_sub
import template.app.profile.generated.resources.tier_poweradmin_danger_purge_cache
import template.app.profile.generated.resources.tier_poweradmin_danger_purge_cache_sub
import template.app.profile.generated.resources.tier_poweradmin_danger_reset_rate
import template.app.profile.generated.resources.tier_poweradmin_danger_reset_rate_sub
import template.app.profile.generated.resources.tier_poweradmin_danger_title
import template.app.profile.generated.resources.tier_poweradmin_directory_desc
import template.app.profile.generated.resources.tier_poweradmin_directory_title
import template.app.profile.generated.resources.tier_poweradmin_identity_access_level
import template.app.profile.generated.resources.tier_poweradmin_identity_admin_id
import template.app.profile.generated.resources.tier_poweradmin_identity_desc
import template.app.profile.generated.resources.tier_poweradmin_identity_last_login_ip
import template.app.profile.generated.resources.tier_poweradmin_identity_mfa_status
import template.app.profile.generated.resources.tier_poweradmin_identity_role
import template.app.profile.generated.resources.tier_poweradmin_identity_session_expires
import template.app.profile.generated.resources.tier_poweradmin_identity_title
import template.app.profile.generated.resources.tier_poweradmin_res_audit_logs
import template.app.profile.generated.resources.tier_poweradmin_res_billing
import template.app.profile.generated.resources.tier_poweradmin_res_infra
import template.app.profile.generated.resources.tier_poweradmin_res_system_config
import template.app.profile.generated.resources.tier_poweradmin_res_users
import template.app.profile.generated.resources.tier_poweradmin_stats_avg_latency
import template.app.profile.generated.resources.tier_poweradmin_stats_desc
import template.app.profile.generated.resources.tier_poweradmin_stats_error_rate
import template.app.profile.generated.resources.tier_poweradmin_stats_health_label
import template.app.profile.generated.resources.tier_poweradmin_stats_requests_today
import template.app.profile.generated.resources.tier_poweradmin_stats_title
import template.app.profile.generated.resources.tier_poweradmin_stats_total_users
import template.app.profile.generated.resources.tier_poweradmin_stats_uptime
import template.app.profile.generated.resources.tier_poweradmin_status_api_gateway
import template.app.profile.generated.resources.tier_poweradmin_status_api_gateway_sub
import template.app.profile.generated.resources.tier_poweradmin_status_cache
import template.app.profile.generated.resources.tier_poweradmin_status_cache_sub
import template.app.profile.generated.resources.tier_poweradmin_status_db
import template.app.profile.generated.resources.tier_poweradmin_status_db_sub
import template.app.profile.generated.resources.tier_poweradmin_status_desc
import template.app.profile.generated.resources.tier_poweradmin_status_healthy
import template.app.profile.generated.resources.tier_poweradmin_status_storage
import template.app.profile.generated.resources.tier_poweradmin_status_storage_sub
import template.app.profile.generated.resources.tier_poweradmin_status_title
import template.app.profile.generated.resources.tier_poweradmin_status_warning
import template.app.profile.generated.resources.tier_poweradmin_status_worker
import template.app.profile.generated.resources.tier_poweradmin_status_worker_sub
import template.app.profile.generated.resources.tier_poweradmin_table_email
import template.app.profile.generated.resources.tier_poweradmin_table_id
import template.app.profile.generated.resources.tier_poweradmin_table_name
import template.app.profile.generated.resources.tier_poweradmin_table_status
import template.app.profile.generated.resources.tier_poweradmin_table_tier

/**
 * PowerAdmin tier profile content showing platform stats, user directory,
 * admin identity card, access matrix, system status, and danger zone.
 *
 * All data is static/mock -- demonstrates what a power admin would see.
 *
 * @param state The current profile state.
 * @param modifier Modifier for the content root.
 */
@Composable
fun PowerAdminTierContent(
    state: ProfileModel,
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Platform stats
        AuraCard(
            title = stringResource(Res.string.tier_poweradmin_stats_title),
            description = stringResource(Res.string.tier_poweradmin_stats_desc),
            variant = CardVariant.Highlighted,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PlatformStatRow(label = stringResource(Res.string.tier_poweradmin_stats_uptime), value = "99.99%")
                PlatformStatRow(label = stringResource(Res.string.tier_poweradmin_stats_total_users), value = "12,847")
                PlatformStatRow(label = stringResource(Res.string.tier_poweradmin_stats_requests_today), value = "2.4M")
                PlatformStatRow(label = stringResource(Res.string.tier_poweradmin_stats_avg_latency), value = "23ms")
                PlatformStatRow(label = stringResource(Res.string.tier_poweradmin_stats_error_rate), value = "0.002%")

                AuraProgress(
                    progress = 0.9999f,
                    label = stringResource(Res.string.tier_poweradmin_stats_health_label),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // User directory
        AuraCard(
            title = stringResource(Res.string.tier_poweradmin_directory_title),
            description = stringResource(Res.string.tier_poweradmin_directory_desc),
        ) {
            AuraTable(
                headers = listOf(
                    stringResource(Res.string.tier_poweradmin_table_id),
                    stringResource(Res.string.tier_poweradmin_table_name),
                    stringResource(Res.string.tier_poweradmin_table_email),
                    stringResource(Res.string.tier_poweradmin_table_tier),
                    stringResource(Res.string.tier_poweradmin_table_status),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = "#001", secondary = true)
                    AuraTableCell(text = state.name.ifBlank { "Root Admin" })
                    AuraTableCell(text = state.email, secondary = true)
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = "power_admin", variant = BadgeVariant.Accent)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = "online", variant = BadgeVariant.Success)
                    }
                }
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = "#002", secondary = true)
                    AuraTableCell(text = "Sarah Admin")
                    AuraTableCell(text = "sarah@platform.dev", secondary = true)
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = "admin", variant = BadgeVariant.Accent)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = "online", variant = BadgeVariant.Success)
                    }
                }
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = "#003", secondary = true)
                    AuraTableCell(text = "Mike Premium")
                    AuraTableCell(text = "mike@company.io", secondary = true)
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = "premium", variant = BadgeVariant.Success)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = "online", variant = BadgeVariant.Success)
                    }
                }
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = "#047", secondary = true)
                    AuraTableCell(text = "Ana Paid")
                    AuraTableCell(text = "ana@startup.co", secondary = true)
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = "paid", variant = BadgeVariant.Default)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = "offline", variant = BadgeVariant.Warning)
                    }
                }
                AuraTableRow(showBottomBorder = false) {
                    AuraTableCell(text = "#128", secondary = true)
                    AuraTableCell(text = "New User")
                    AuraTableCell(text = "new@example.com", secondary = true)
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = "free", variant = BadgeVariant.Default)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AuraBadge(text = "offline", variant = BadgeVariant.Warning)
                    }
                }
            }
        }

        // Admin identity card
        AuraCard(
            title = stringResource(Res.string.tier_poweradmin_identity_title),
            description = stringResource(Res.string.tier_poweradmin_identity_desc),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlatformStatRow(label = stringResource(Res.string.tier_poweradmin_identity_admin_id), value = state.userId.take(8) + "...")
                PlatformStatRow(label = stringResource(Res.string.tier_poweradmin_identity_role), value = "power_admin")
                PlatformStatRow(label = stringResource(Res.string.tier_poweradmin_identity_mfa_status), value = "enabled")
                PlatformStatRow(label = stringResource(Res.string.tier_poweradmin_identity_session_expires), value = "23h 45m")
                PlatformStatRow(label = stringResource(Res.string.tier_poweradmin_identity_last_login_ip), value = "192.168.1.xxx")
                PlatformStatRow(label = stringResource(Res.string.tier_poweradmin_identity_access_level), value = "FULL")
            }
        }

        // Access matrix
        AuraCard(
            title = stringResource(Res.string.tier_poweradmin_access_title),
            description = stringResource(Res.string.tier_poweradmin_access_desc),
        ) {
            AuraTable(
                headers = listOf(
                    stringResource(Res.string.tier_poweradmin_access_resource_col),
                    stringResource(Res.string.tier_poweradmin_access_read_col),
                    stringResource(Res.string.tier_poweradmin_access_write_col),
                    stringResource(Res.string.tier_poweradmin_access_delete_col),
                    stringResource(Res.string.tier_poweradmin_access_admin_col),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = stringResource(Res.string.tier_poweradmin_res_users))
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                }
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = stringResource(Res.string.tier_poweradmin_res_billing))
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                }
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = stringResource(Res.string.tier_poweradmin_res_infra))
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                }
                AuraTableRow(showBottomBorder = true) {
                    AuraTableCell(text = stringResource(Res.string.tier_poweradmin_res_audit_logs))
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2717")
                    AuraTableCell(text = "\u2713")
                }
                AuraTableRow(showBottomBorder = false) {
                    AuraTableCell(text = stringResource(Res.string.tier_poweradmin_res_system_config))
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                    AuraTableCell(text = "\u2713")
                }
            }
        }

        // System status
        AuraCard(
            title = stringResource(Res.string.tier_poweradmin_status_title),
            description = stringResource(Res.string.tier_poweradmin_status_desc),
        ) {
            AuraList {
                AuraListItem(
                    text = stringResource(Res.string.tier_poweradmin_status_api_gateway),
                    subtitle = stringResource(Res.string.tier_poweradmin_status_api_gateway_sub),
                    trailingContent = { color ->
                        AuraBadge(text = stringResource(Res.string.tier_poweradmin_status_healthy), variant = BadgeVariant.Success)
                    },
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_poweradmin_status_db),
                    subtitle = stringResource(Res.string.tier_poweradmin_status_db_sub),
                    trailingContent = { color ->
                        AuraBadge(text = stringResource(Res.string.tier_poweradmin_status_healthy), variant = BadgeVariant.Success)
                    },
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_poweradmin_status_cache),
                    subtitle = stringResource(Res.string.tier_poweradmin_status_cache_sub),
                    trailingContent = { color ->
                        AuraBadge(text = stringResource(Res.string.tier_poweradmin_status_healthy), variant = BadgeVariant.Success)
                    },
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_poweradmin_status_worker),
                    subtitle = stringResource(Res.string.tier_poweradmin_status_worker_sub),
                    trailingContent = { color ->
                        AuraBadge(text = stringResource(Res.string.tier_poweradmin_status_healthy), variant = BadgeVariant.Success)
                    },
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_poweradmin_status_storage),
                    subtitle = stringResource(Res.string.tier_poweradmin_status_storage_sub),
                    trailingContent = { color ->
                        AuraBadge(text = stringResource(Res.string.tier_poweradmin_status_warning), variant = BadgeVariant.Warning)
                    },
                )
            }
        }

        // Danger zone
        AuraAlert(
            message = stringResource(Res.string.tier_poweradmin_danger_alert_message),
            variant = AlertVariant.Error,
            title = stringResource(Res.string.tier_poweradmin_danger_alert_title),
        )

        AuraCard(
            title = stringResource(Res.string.tier_poweradmin_danger_title),
            description = stringResource(Res.string.tier_poweradmin_danger_desc),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AuraText(
                            text = stringResource(Res.string.tier_poweradmin_danger_purge_cache),
                            style = typography.sm.copy(fontWeight = FontWeight.Medium),
                            color = colors.text,
                        )
                        AuraText(
                            text = stringResource(Res.string.tier_poweradmin_danger_purge_cache_sub),
                            style = typography.xs,
                            color = colors.textMuted,
                        )
                    }
                    AuraButton(
                        text = stringResource(Res.string.tier_poweradmin_btn_purge),
                        onClick = { /* Static demo */ },
                        variant = ButtonVariant.Destructive,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AuraText(
                            text = stringResource(Res.string.tier_poweradmin_danger_reset_rate),
                            style = typography.sm.copy(fontWeight = FontWeight.Medium),
                            color = colors.text,
                        )
                        AuraText(
                            text = stringResource(Res.string.tier_poweradmin_danger_reset_rate_sub),
                            style = typography.xs,
                            color = colors.textMuted,
                        )
                    }
                    AuraButton(
                        text = stringResource(Res.string.tier_poweradmin_btn_reset),
                        onClick = { /* Static demo */ },
                        variant = ButtonVariant.Destructive,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AuraText(
                            text = stringResource(Res.string.tier_poweradmin_danger_force_logout),
                            style = typography.sm.copy(fontWeight = FontWeight.Medium),
                            color = colors.text,
                        )
                        AuraText(
                            text = stringResource(Res.string.tier_poweradmin_danger_force_logout_sub),
                            style = typography.xs,
                            color = colors.textMuted,
                        )
                    }
                    AuraButton(
                        text = stringResource(Res.string.tier_poweradmin_btn_logout_all),
                        onClick = { /* Static demo */ },
                        variant = ButtonVariant.Destructive,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlatformStatRow(label: String, value: String) {
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
