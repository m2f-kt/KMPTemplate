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
            title = "user_management",
            description = "// organization members",
        ) {
            TerminalTable(
                headers = listOf("NAME", "EMAIL", "ROLE", "STATUS", "LAST ACTIVE"),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = state.name.ifBlank { "Admin" })
                    TerminalTableCell(text = state.email, secondary = true)
                    TerminalTableCell(text = "admin")
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "active", variant = BadgeVariant.Success)
                    }
                    TerminalTableCell(text = "now", secondary = true)
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "Jordan Lee")
                    TerminalTableCell(text = "jordan@org.dev", secondary = true)
                    TerminalTableCell(text = "premium")
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "active", variant = BadgeVariant.Success)
                    }
                    TerminalTableCell(text = "2h ago", secondary = true)
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "Riley Kim")
                    TerminalTableCell(text = "riley@org.dev", secondary = true)
                    TerminalTableCell(text = "paid")
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "active", variant = BadgeVariant.Success)
                    }
                    TerminalTableCell(text = "1d ago", secondary = true)
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "Casey Morgan")
                    TerminalTableCell(text = "casey@org.dev", secondary = true)
                    TerminalTableCell(text = "free")
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "inactive", variant = BadgeVariant.Warning)
                    }
                    TerminalTableCell(text = "14d ago", secondary = true)
                }
                TerminalTableRow(showBottomBorder = false) {
                    TerminalTableCell(text = "Drew Park")
                    TerminalTableCell(text = "drew@org.dev", secondary = true)
                    TerminalTableCell(text = "viewer")
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "pending", variant = BadgeVariant.Warning)
                    }
                    TerminalTableCell(text = "never", secondary = true)
                }
            }
        }

        // Groups section
        TerminalCard(
            title = "groups",
            description = "// team organization",
        ) {
            TerminalList {
                TerminalListItem(
                    text = "Engineering",
                    subtitle = "// 8 members",
                    trailingContent = { color ->
                        TerminalBadge(text = "8", variant = BadgeVariant.Accent)
                    },
                )
                TerminalListItem(
                    text = "Operations",
                    subtitle = "// 4 members",
                    trailingContent = { color ->
                        TerminalBadge(text = "4", variant = BadgeVariant.Accent)
                    },
                )
                TerminalListItem(
                    text = "Security",
                    subtitle = "// 3 members",
                    trailingContent = { color ->
                        TerminalBadge(text = "3", variant = BadgeVariant.Accent)
                    },
                )
            }
        }

        // Permissions matrix
        TerminalCard(
            title = "permissions_matrix",
            description = "// role-based access",
        ) {
            TerminalTable(
                headers = listOf("PERMISSION", "FREE", "PAID", "PREMIUM", "ADMIN"),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "read")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "write")
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "admin")
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2713")
                }
                TerminalTableRow(showBottomBorder = false) {
                    TerminalTableCell(text = "delete")
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2713")
                }
            }
        }

        // Analytics
        TerminalCard(
            title = "org_analytics",
            description = "// platform metrics",
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AdminMetricRow(label = "total_users", value = "24")
                AdminMetricRow(label = "active_sessions", value = "12")
                AdminMetricRow(label = "api_calls_today", value = "45,230")
                AdminMetricRow(label = "storage_used", value = "12.4 GB / 50 GB")

                TerminalProgress(
                    progress = 0.248f,
                    label = "Storage",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Audit log
        TerminalCard(
            title = "audit_log",
            description = "// recent actions",
        ) {
            TerminalList {
                TerminalListItem(
                    text = "User casey@org.dev role changed to free",
                    subtitle = "// 2 hours ago | by ${state.email}",
                )
                TerminalListItem(
                    text = "API key tk_prod_*** rotated",
                    subtitle = "// 5 hours ago | by jordan@org.dev",
                )
                TerminalListItem(
                    text = "Group 'Security' created",
                    subtitle = "// 1 day ago | by ${state.email}",
                )
                TerminalListItem(
                    text = "User drew@org.dev invited",
                    subtitle = "// 2 days ago | by ${state.email}",
                )
                TerminalListItem(
                    text = "Webhook endpoint added",
                    subtitle = "// 3 days ago | by riley@org.dev",
                )
            }
        }

        // Org settings
        TerminalCard(
            title = "org_settings",
            description = "// organization configuration",
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AdminMetricRow(label = "org_name", value = "Terminal Corp")
                AdminMetricRow(label = "plan", value = "Enterprise")
                AdminMetricRow(label = "seats_total", value = "50")
                AdminMetricRow(label = "seats_used", value = "24")
                AdminMetricRow(label = "sso_enabled", value = "true")
                AdminMetricRow(label = "mfa_required", value = "true")
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
