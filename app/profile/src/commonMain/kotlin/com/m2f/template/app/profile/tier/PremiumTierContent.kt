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
import com.m2f.template.designsystem.components.card.CardVariant
import com.m2f.template.designsystem.components.card.TerminalCard
import com.m2f.template.designsystem.components.data.TerminalList
import com.m2f.template.designsystem.components.data.TerminalListItem
import com.m2f.template.designsystem.components.data.TerminalTable
import com.m2f.template.designsystem.components.data.TerminalTableCell
import com.m2f.template.designsystem.components.data.TerminalTableRow
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.theme.TerminalTheme

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
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Premium success alert
        TerminalAlert(
            message = "All premium features are active. Thank you for your support.",
            variant = AlertVariant.Success,
            title = "premium_active",
        )

        // Webhook configuration
        TerminalCard(
            title = "webhook_configuration",
            description = "// event notifications",
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TerminalTable(
                    headers = listOf("ENDPOINT", "EVENTS", "STATUS"),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TerminalTableRow(showBottomBorder = true) {
                        TerminalTableCell(text = "https://api.example.com/hooks/deploy")
                        TerminalTableCell(text = "deploy.success, deploy.fail", secondary = true)
                        Box(modifier = Modifier.weight(1f)) {
                            TerminalBadge(text = "active", variant = BadgeVariant.Success)
                        }
                    }
                    TerminalTableRow(showBottomBorder = true) {
                        TerminalTableCell(text = "https://api.example.com/hooks/alerts")
                        TerminalTableCell(text = "alert.critical, alert.warning", secondary = true)
                        Box(modifier = Modifier.weight(1f)) {
                            TerminalBadge(text = "active", variant = BadgeVariant.Success)
                        }
                    }
                    TerminalTableRow(showBottomBorder = false) {
                        TerminalTableCell(text = "https://slack.example.com/webhook")
                        TerminalTableCell(text = "all", secondary = true)
                        Box(modifier = Modifier.weight(1f)) {
                            TerminalBadge(text = "paused", variant = BadgeVariant.Warning)
                        }
                    }
                }
            }
        }

        // API keys
        TerminalCard(
            title = "api_keys",
            description = "// manage access tokens",
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TerminalTable(
                    headers = listOf("NAME", "KEY", "CREATED", "LAST USED"),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TerminalTableRow(showBottomBorder = true) {
                        TerminalTableCell(text = "production")
                        TerminalTableCell(text = "tk_prod_***...x8f2", secondary = true)
                        TerminalTableCell(text = "2024-01-15", secondary = true)
                        TerminalTableCell(text = "2 hours ago", secondary = true)
                    }
                    TerminalTableRow(showBottomBorder = true) {
                        TerminalTableCell(text = "staging")
                        TerminalTableCell(text = "tk_stg_***...m4d1", secondary = true)
                        TerminalTableCell(text = "2024-02-20", secondary = true)
                        TerminalTableCell(text = "5 days ago", secondary = true)
                    }
                    TerminalTableRow(showBottomBorder = false) {
                        TerminalTableCell(text = "development")
                        TerminalTableCell(text = "tk_dev_***...q7a9", secondary = true)
                        TerminalTableCell(text = "2024-03-01", secondary = true)
                        TerminalTableCell(text = "never", secondary = true)
                    }
                }
            }
        }

        // Priority support card
        TerminalCard(
            title = "priority_support",
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
                    TerminalText(
                        text = "response_time",
                        style = typography.sm,
                        color = colors.textMuted,
                    )
                    TerminalText(
                        text = "< 4 hours",
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.accent,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TerminalText(
                        text = "support_channel",
                        style = typography.sm,
                        color = colors.textMuted,
                    )
                    TerminalText(
                        text = "email + chat",
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.text,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TerminalText(
                        text = "tickets_open",
                        style = typography.sm,
                        color = colors.textMuted,
                    )
                    TerminalText(
                        text = "0",
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.success,
                    )
                }
            }
        }

        // Full feature list
        TerminalList(title = "active_features") {
            TerminalListItem(
                text = "Unlimited API calls",
                trailingContent = { color ->
                    TerminalBadge(text = "\u2713 active", variant = BadgeVariant.Success)
                },
            )
            TerminalListItem(
                text = "Team access (unlimited seats)",
                trailingContent = { color ->
                    TerminalBadge(text = "\u2713 active", variant = BadgeVariant.Success)
                },
            )
            TerminalListItem(
                text = "Webhook integrations",
                trailingContent = { color ->
                    TerminalBadge(text = "\u2713 active", variant = BadgeVariant.Success)
                },
            )
            TerminalListItem(
                text = "API key management",
                trailingContent = { color ->
                    TerminalBadge(text = "\u2713 active", variant = BadgeVariant.Success)
                },
            )
            TerminalListItem(
                text = "Priority support",
                trailingContent = { color ->
                    TerminalBadge(text = "\u2713 active", variant = BadgeVariant.Success)
                },
            )
        }
    }
}
