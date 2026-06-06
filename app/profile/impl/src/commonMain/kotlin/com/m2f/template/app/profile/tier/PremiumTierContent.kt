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
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.AuraAlert
import com.m2f.template.designsystem.components.feedback.AuraBadge
import com.m2f.template.designsystem.theme.AuraTheme
import org.jetbrains.compose.resources.stringResource
import template.app.profile.generated.resources.Res
import template.app.profile.generated.resources.tier_premium_active_badge
import template.app.profile.generated.resources.tier_premium_alert_message
import template.app.profile.generated.resources.tier_premium_alert_title
import template.app.profile.generated.resources.tier_premium_api_keys_desc
import template.app.profile.generated.resources.tier_premium_api_keys_title
import template.app.profile.generated.resources.tier_premium_feature_api_keys
import template.app.profile.generated.resources.tier_premium_feature_priority_support
import template.app.profile.generated.resources.tier_premium_feature_team_access
import template.app.profile.generated.resources.tier_premium_feature_unlimited_api
import template.app.profile.generated.resources.tier_premium_feature_webhooks
import template.app.profile.generated.resources.tier_premium_features_title
import template.app.profile.generated.resources.tier_premium_response_time
import template.app.profile.generated.resources.tier_premium_response_time_val
import template.app.profile.generated.resources.tier_premium_status_active
import template.app.profile.generated.resources.tier_premium_status_paused
import template.app.profile.generated.resources.tier_premium_support_channel
import template.app.profile.generated.resources.tier_premium_support_channel_val
import template.app.profile.generated.resources.tier_premium_support_title
import template.app.profile.generated.resources.tier_premium_table_created
import template.app.profile.generated.resources.tier_premium_table_endpoint
import template.app.profile.generated.resources.tier_premium_table_events
import template.app.profile.generated.resources.tier_premium_table_key
import template.app.profile.generated.resources.tier_premium_table_last_used
import template.app.profile.generated.resources.tier_premium_table_name
import template.app.profile.generated.resources.tier_premium_table_status
import template.app.profile.generated.resources.tier_premium_tickets_open
import template.app.profile.generated.resources.tier_premium_tickets_open_val
import template.app.profile.generated.resources.tier_premium_webhook_desc
import template.app.profile.generated.resources.tier_premium_webhook_title

/**
 * Premium tier profile content showing full features, webhook configuration,
 * priority support card, and API key display.
 *
 * All data is static/mock -- demonstrates what a premium tier user would see.
 *
 * @param state The current profile state.
 * @param modifier Modifier for the content root.
 */
@Composable
fun PremiumTierContent(
    state: ProfileModel,
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Premium success alert
        AuraAlert(
            message = stringResource(Res.string.tier_premium_alert_message),
            variant = AlertVariant.Success,
            title = stringResource(Res.string.tier_premium_alert_title),
        )

        // Webhook configuration
        AuraCard(
            title = stringResource(Res.string.tier_premium_webhook_title),
            description = stringResource(Res.string.tier_premium_webhook_desc),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AuraTable(
                    headers = listOf(
                        stringResource(Res.string.tier_premium_table_endpoint),
                        stringResource(Res.string.tier_premium_table_events),
                        stringResource(Res.string.tier_premium_table_status),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AuraTableRow(showBottomBorder = true) {
                        AuraTableCell(text = "https://api.example.com/hooks/deploy")
                        AuraTableCell(text = "deploy.success, deploy.fail", secondary = true)
                        Box(modifier = Modifier.weight(1f)) {
                            AuraBadge(text = stringResource(Res.string.tier_premium_status_active), variant = BadgeVariant.Success)
                        }
                    }
                    AuraTableRow(showBottomBorder = true) {
                        AuraTableCell(text = "https://api.example.com/hooks/alerts")
                        AuraTableCell(text = "alert.critical, alert.warning", secondary = true)
                        Box(modifier = Modifier.weight(1f)) {
                            AuraBadge(text = stringResource(Res.string.tier_premium_status_active), variant = BadgeVariant.Success)
                        }
                    }
                    AuraTableRow(showBottomBorder = false) {
                        AuraTableCell(text = "https://slack.example.com/webhook")
                        AuraTableCell(text = "all", secondary = true)
                        Box(modifier = Modifier.weight(1f)) {
                            AuraBadge(text = stringResource(Res.string.tier_premium_status_paused), variant = BadgeVariant.Warning)
                        }
                    }
                }
            }
        }

        // API keys
        AuraCard(
            title = stringResource(Res.string.tier_premium_api_keys_title),
            description = stringResource(Res.string.tier_premium_api_keys_desc),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AuraTable(
                    headers = listOf(
                        stringResource(Res.string.tier_premium_table_name),
                        stringResource(Res.string.tier_premium_table_key),
                        stringResource(Res.string.tier_premium_table_created),
                        stringResource(Res.string.tier_premium_table_last_used),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AuraTableRow(showBottomBorder = true) {
                        AuraTableCell(text = "production")
                        AuraTableCell(text = "tk_prod_***...x8f2", secondary = true)
                        AuraTableCell(text = "15/01/2024", secondary = true)
                        AuraTableCell(text = "2 hours ago", secondary = true)
                    }
                    AuraTableRow(showBottomBorder = true) {
                        AuraTableCell(text = "staging")
                        AuraTableCell(text = "tk_stg_***...m4d1", secondary = true)
                        AuraTableCell(text = "20/02/2024", secondary = true)
                        AuraTableCell(text = "5 days ago", secondary = true)
                    }
                    AuraTableRow(showBottomBorder = false) {
                        AuraTableCell(text = "development")
                        AuraTableCell(text = "tk_dev_***...q7a9", secondary = true)
                        AuraTableCell(text = "01/03/2024", secondary = true)
                        AuraTableCell(text = "never", secondary = true)
                    }
                }
            }
        }

        // Priority support card
        AuraCard(
            title = stringResource(Res.string.tier_premium_support_title),
            variant = CardVariant.Highlighted,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    AuraText(
                        text = stringResource(Res.string.tier_premium_response_time),
                        style = typography.sm,
                        color = colors.textMuted,
                    )
                    AuraText(
                        text = stringResource(Res.string.tier_premium_response_time_val),
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.accent,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    AuraText(
                        text = stringResource(Res.string.tier_premium_support_channel),
                        style = typography.sm,
                        color = colors.textMuted,
                    )
                    AuraText(
                        text = stringResource(Res.string.tier_premium_support_channel_val),
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.text,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    AuraText(
                        text = stringResource(Res.string.tier_premium_tickets_open),
                        style = typography.sm,
                        color = colors.textMuted,
                    )
                    AuraText(
                        text = stringResource(Res.string.tier_premium_tickets_open_val),
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.success,
                    )
                }
            }
        }

        // Full feature list
        AuraList(title = stringResource(Res.string.tier_premium_features_title)) {
            AuraListItem(
                text = stringResource(Res.string.tier_premium_feature_unlimited_api),
                trailingContent = { color ->
                    AuraBadge(text = stringResource(Res.string.tier_premium_active_badge), variant = BadgeVariant.Success)
                },
            )
            AuraListItem(
                text = stringResource(Res.string.tier_premium_feature_team_access),
                trailingContent = { color ->
                    AuraBadge(text = stringResource(Res.string.tier_premium_active_badge), variant = BadgeVariant.Success)
                },
            )
            AuraListItem(
                text = stringResource(Res.string.tier_premium_feature_webhooks),
                trailingContent = { color ->
                    AuraBadge(text = stringResource(Res.string.tier_premium_active_badge), variant = BadgeVariant.Success)
                },
            )
            AuraListItem(
                text = stringResource(Res.string.tier_premium_feature_api_keys),
                trailingContent = { color ->
                    AuraBadge(text = stringResource(Res.string.tier_premium_active_badge), variant = BadgeVariant.Success)
                },
            )
            AuraListItem(
                text = stringResource(Res.string.tier_premium_feature_priority_support),
                trailingContent = { color ->
                    AuraBadge(text = stringResource(Res.string.tier_premium_active_badge), variant = BadgeVariant.Success)
                },
            )
        }
    }
}
