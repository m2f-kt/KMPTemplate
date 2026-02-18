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
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.components.feedback.TerminalProgress
import com.m2f.template.designsystem.theme.TerminalTheme

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
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Team access section
        TerminalCard(
            title = "team_access",
            description = "// members and roles",
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TerminalTable(
                    headers = listOf("NAME", "EMAIL", "ROLE", "STATUS"),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TerminalTableRow(showBottomBorder = true) {
                        TerminalTableCell(text = state.name.ifBlank { "You" })
                        TerminalTableCell(text = state.email, secondary = true)
                        TerminalTableCell(text = "owner")
                        Box(modifier = Modifier.weight(1f)) {
                            TerminalBadge(text = "active", variant = BadgeVariant.Success)
                        }
                    }
                    TerminalTableRow(showBottomBorder = true) {
                        TerminalTableCell(text = "Alex Chen")
                        TerminalTableCell(text = "alex@team.dev", secondary = true)
                        TerminalTableCell(text = "editor")
                        Box(modifier = Modifier.weight(1f)) {
                            TerminalBadge(text = "active", variant = BadgeVariant.Success)
                        }
                    }
                    TerminalTableRow(showBottomBorder = false) {
                        TerminalTableCell(text = "Sam Rivera")
                        TerminalTableCell(text = "sam@team.dev", secondary = true)
                        TerminalTableCell(text = "viewer")
                        Box(modifier = Modifier.weight(1f)) {
                            TerminalBadge(text = "pending", variant = BadgeVariant.Warning)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TerminalText(
                        text = "seats_used",
                        style = typography.sm,
                        color = colors.textMuted,
                    )
                    TerminalText(
                        text = "3 / 5",
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.text,
                    )
                }
            }
        }

        // Analytics preview
        TerminalCard(
            title = "analytics_preview",
            description = "// last 30 days",
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnalyticRow(label = "total_requests", value = "12,847")
                AnalyticRow(label = "avg_response_time", value = "142ms")
                AnalyticRow(label = "error_rate", value = "0.12%")
                AnalyticRow(label = "uptime", value = "99.97%")

                TerminalProgress(
                    progress = 0.9997f,
                    label = "Uptime",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Export options
        TerminalCard(
            title = "data_export",
            description = "// download your data",
        ) {
            TerminalList {
                TerminalListItem(
                    text = "Export team data",
                    subtitle = "// CSV format",
                    trailingContent = { color ->
                        TerminalBadge(text = "available", variant = BadgeVariant.Accent)
                    },
                )
                TerminalListItem(
                    text = "Export analytics",
                    subtitle = "// JSON format",
                    trailingContent = { color ->
                        TerminalBadge(text = "available", variant = BadgeVariant.Accent)
                    },
                )
                TerminalListItem(
                    text = "Export audit log",
                    subtitle = "// requires premium",
                    trailingContent = { color ->
                        TerminalBadge(text = "locked", variant = BadgeVariant.Default)
                    },
                )
            }
        }

        // Premium upgrade info
        TerminalCard(
            title = "upgrade_to_premium",
            variant = CardVariant.Accent,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TerminalText(
                    text = "Unlock webhooks, API keys, priority support, and full audit log.",
                    style = typography.sm,
                    color = colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun AnalyticRow(label: String, value: String) {
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
