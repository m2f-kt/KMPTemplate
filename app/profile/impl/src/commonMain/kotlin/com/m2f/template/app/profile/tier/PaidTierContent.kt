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
import com.m2f.template.designsystem.components.card.CardVariant
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
import template.app.profile.generated.resources.tier_paid_analytics_desc
import template.app.profile.generated.resources.tier_paid_analytics_title
import template.app.profile.generated.resources.tier_paid_avg_response_time
import template.app.profile.generated.resources.tier_paid_error_rate
import template.app.profile.generated.resources.tier_paid_export_analytics
import template.app.profile.generated.resources.tier_paid_export_analytics_sub
import template.app.profile.generated.resources.tier_paid_export_audit_log
import template.app.profile.generated.resources.tier_paid_export_audit_log_sub
import template.app.profile.generated.resources.tier_paid_export_available
import template.app.profile.generated.resources.tier_paid_export_desc
import template.app.profile.generated.resources.tier_paid_export_locked
import template.app.profile.generated.resources.tier_paid_export_team_data
import template.app.profile.generated.resources.tier_paid_export_team_data_sub
import template.app.profile.generated.resources.tier_paid_export_title
import template.app.profile.generated.resources.tier_paid_seats_used
import template.app.profile.generated.resources.tier_paid_status_active
import template.app.profile.generated.resources.tier_paid_status_pending
import template.app.profile.generated.resources.tier_paid_table_email
import template.app.profile.generated.resources.tier_paid_table_name
import template.app.profile.generated.resources.tier_paid_table_role
import template.app.profile.generated.resources.tier_paid_table_status
import template.app.profile.generated.resources.tier_paid_team_desc
import template.app.profile.generated.resources.tier_paid_team_title
import template.app.profile.generated.resources.tier_paid_total_requests
import template.app.profile.generated.resources.tier_paid_upgrade_description
import template.app.profile.generated.resources.tier_paid_upgrade_title
import template.app.profile.generated.resources.tier_paid_uptime
import template.app.profile.generated.resources.tier_paid_uptime_label

/**
 * Paid tier profile content showing account info, team access, analytics preview, and export options.
 *
 * All data is static/mock -- demonstrates what a paid tier user would see.
 *
 * @param state The current profile state.
 * @param modifier Modifier for the content root.
 */
@Composable
fun PaidTierContent(
    state: ProfileModel,
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Team access section
        AuraCard(
            title = stringResource(Res.string.tier_paid_team_title),
            description = stringResource(Res.string.tier_paid_team_desc),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AuraTable(
                    headers = listOf(
                        stringResource(Res.string.tier_paid_table_name),
                        stringResource(Res.string.tier_paid_table_email),
                        stringResource(Res.string.tier_paid_table_role),
                        stringResource(Res.string.tier_paid_table_status),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AuraTableRow(showBottomBorder = true) {
                        AuraTableCell(text = state.name.ifBlank { "You" })
                        AuraTableCell(text = state.email, secondary = true)
                        AuraTableCell(text = "owner")
                        Box(modifier = Modifier.weight(1f)) {
                            AuraBadge(text = stringResource(Res.string.tier_paid_status_active), variant = BadgeVariant.Success)
                        }
                    }
                    AuraTableRow(showBottomBorder = true) {
                        AuraTableCell(text = "Alex Chen")
                        AuraTableCell(text = "alex@team.dev", secondary = true)
                        AuraTableCell(text = "editor")
                        Box(modifier = Modifier.weight(1f)) {
                            AuraBadge(text = stringResource(Res.string.tier_paid_status_active), variant = BadgeVariant.Success)
                        }
                    }
                    AuraTableRow(showBottomBorder = false) {
                        AuraTableCell(text = "Sam Rivera")
                        AuraTableCell(text = "sam@team.dev", secondary = true)
                        AuraTableCell(text = "viewer")
                        Box(modifier = Modifier.weight(1f)) {
                            AuraBadge(text = stringResource(Res.string.tier_paid_status_pending), variant = BadgeVariant.Warning)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    AuraText(
                        text = stringResource(Res.string.tier_paid_seats_used),
                        style = typography.sm,
                        color = colors.textMuted,
                    )
                    AuraText(
                        text = "3 / 5",
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.text,
                    )
                }
            }
        }

        // Analytics preview
        AuraCard(
            title = stringResource(Res.string.tier_paid_analytics_title),
            description = stringResource(Res.string.tier_paid_analytics_desc),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnalyticRow(label = stringResource(Res.string.tier_paid_total_requests), value = "12,847")
                AnalyticRow(label = stringResource(Res.string.tier_paid_avg_response_time), value = "142ms")
                AnalyticRow(label = stringResource(Res.string.tier_paid_error_rate), value = "0.12%")
                AnalyticRow(label = stringResource(Res.string.tier_paid_uptime), value = "99.97%")

                AuraProgress(
                    progress = 0.9997f,
                    label = stringResource(Res.string.tier_paid_uptime_label),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Export options
        AuraCard(
            title = stringResource(Res.string.tier_paid_export_title),
            description = stringResource(Res.string.tier_paid_export_desc),
        ) {
            AuraList {
                AuraListItem(
                    text = stringResource(Res.string.tier_paid_export_team_data),
                    subtitle = stringResource(Res.string.tier_paid_export_team_data_sub),
                    trailingContent = { color ->
                        AuraBadge(text = stringResource(Res.string.tier_paid_export_available), variant = BadgeVariant.Accent)
                    },
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_paid_export_analytics),
                    subtitle = stringResource(Res.string.tier_paid_export_analytics_sub),
                    trailingContent = { color ->
                        AuraBadge(text = stringResource(Res.string.tier_paid_export_available), variant = BadgeVariant.Accent)
                    },
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_paid_export_audit_log),
                    subtitle = stringResource(Res.string.tier_paid_export_audit_log_sub),
                    trailingContent = { color ->
                        AuraBadge(text = stringResource(Res.string.tier_paid_export_locked), variant = BadgeVariant.Default)
                    },
                )
            }
        }

        // Premium upgrade info
        AuraCard(
            title = stringResource(Res.string.tier_paid_upgrade_title),
            variant = CardVariant.Accent,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AuraText(
                    text = stringResource(Res.string.tier_paid_upgrade_description),
                    style = typography.sm,
                    color = colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun AnalyticRow(label: String, value: String) {
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
