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
import com.m2f.template.app.profile.ProfileState
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.TerminalButton
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
import com.m2f.template.designsystem.components.feedback.TerminalProgress
import com.m2f.template.designsystem.theme.TerminalTheme

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
    state: ProfileState,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Platform stats
        TerminalCard(
            title = "platform_stats",
            description = "// real-time metrics",
            variant = CardVariant.Highlighted,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PlatformStatRow(label = "uptime", value = "99.99%")
                PlatformStatRow(label = "total_users", value = "12,847")
                PlatformStatRow(label = "requests_today", value = "2.4M")
                PlatformStatRow(label = "avg_latency", value = "23ms")
                PlatformStatRow(label = "error_rate", value = "0.002%")

                TerminalProgress(
                    progress = 0.9999f,
                    label = "System health",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // User directory
        TerminalCard(
            title = "user_directory",
            description = "// all platform users",
        ) {
            TerminalTable(
                headers = listOf("ID", "NAME", "EMAIL", "TIER", "STATUS"),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "#001", secondary = true)
                    TerminalTableCell(text = state.name.ifBlank { "Root Admin" })
                    TerminalTableCell(text = state.email, secondary = true)
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "power_admin", variant = BadgeVariant.Accent)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "online", variant = BadgeVariant.Success)
                    }
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "#002", secondary = true)
                    TerminalTableCell(text = "Sarah Admin")
                    TerminalTableCell(text = "sarah@platform.dev", secondary = true)
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "admin", variant = BadgeVariant.Accent)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "online", variant = BadgeVariant.Success)
                    }
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "#003", secondary = true)
                    TerminalTableCell(text = "Mike Premium")
                    TerminalTableCell(text = "mike@company.io", secondary = true)
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "premium", variant = BadgeVariant.Success)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "online", variant = BadgeVariant.Success)
                    }
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "#047", secondary = true)
                    TerminalTableCell(text = "Ana Paid")
                    TerminalTableCell(text = "ana@startup.co", secondary = true)
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "paid", variant = BadgeVariant.Default)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "offline", variant = BadgeVariant.Warning)
                    }
                }
                TerminalTableRow(showBottomBorder = false) {
                    TerminalTableCell(text = "#128", secondary = true)
                    TerminalTableCell(text = "New User")
                    TerminalTableCell(text = "new@example.com", secondary = true)
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "free", variant = BadgeVariant.Default)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(text = "offline", variant = BadgeVariant.Warning)
                    }
                }
            }
        }

        // Admin identity card
        TerminalCard(
            title = "admin_identity",
            description = "// your admin credentials",
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlatformStatRow(label = "admin_id", value = state.userId.take(8) + "...")
                PlatformStatRow(label = "role", value = "power_admin")
                PlatformStatRow(label = "mfa_status", value = "enabled")
                PlatformStatRow(label = "session_expires", value = "23h 45m")
                PlatformStatRow(label = "last_login_ip", value = "192.168.1.xxx")
                PlatformStatRow(label = "access_level", value = "FULL")
            }
        }

        // Access matrix
        TerminalCard(
            title = "access_matrix",
            description = "// system permissions",
        ) {
            TerminalTable(
                headers = listOf("RESOURCE", "READ", "WRITE", "DELETE", "ADMIN"),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "Users")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "Billing")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "Infrastructure")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                }
                TerminalTableRow(showBottomBorder = true) {
                    TerminalTableCell(text = "Audit logs")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2717")
                    TerminalTableCell(text = "\u2713")
                }
                TerminalTableRow(showBottomBorder = false) {
                    TerminalTableCell(text = "System config")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                    TerminalTableCell(text = "\u2713")
                }
            }
        }

        // System status
        TerminalCard(
            title = "system_status",
            description = "// service health",
        ) {
            TerminalList {
                TerminalListItem(
                    text = "API Gateway",
                    subtitle = "// latency: 12ms | throughput: 847 req/s",
                    trailingContent = { color ->
                        TerminalBadge(text = "healthy", variant = BadgeVariant.Success)
                    },
                )
                TerminalListItem(
                    text = "Database Primary",
                    subtitle = "// connections: 42/100 | replication: sync",
                    trailingContent = { color ->
                        TerminalBadge(text = "healthy", variant = BadgeVariant.Success)
                    },
                )
                TerminalListItem(
                    text = "Cache Layer",
                    subtitle = "// hit rate: 94.2% | memory: 2.1GB/4GB",
                    trailingContent = { color ->
                        TerminalBadge(text = "healthy", variant = BadgeVariant.Success)
                    },
                )
                TerminalListItem(
                    text = "Worker Queue",
                    subtitle = "// pending: 23 | processing: 4 | failed: 0",
                    trailingContent = { color ->
                        TerminalBadge(text = "healthy", variant = BadgeVariant.Success)
                    },
                )
                TerminalListItem(
                    text = "Storage Service",
                    subtitle = "// usage: 847GB/2TB | iops: 1.2k",
                    trailingContent = { color ->
                        TerminalBadge(text = "warning", variant = BadgeVariant.Warning)
                    },
                )
            }
        }

        // Danger zone
        TerminalAlert(
            message = "Destructive actions below. These operations cannot be undone.",
            variant = AlertVariant.Error,
            title = "danger_zone",
        )

        TerminalCard(
            title = "destructive_actions",
            description = "// irreversible operations",
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
                        TerminalText(
                            text = "Purge all cache",
                            style = typography.sm.copy(fontWeight = FontWeight.Medium),
                            color = colors.text,
                        )
                        TerminalText(
                            text = "// clear all cached data across nodes",
                            style = typography.xs,
                            color = colors.textMuted,
                        )
                    }
                    TerminalButton(
                        text = "purge",
                        onClick = { /* Static demo */ },
                        variant = ButtonVariant.Destructive,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        TerminalText(
                            text = "Reset rate limits",
                            style = typography.sm.copy(fontWeight = FontWeight.Medium),
                            color = colors.text,
                        )
                        TerminalText(
                            text = "// reset all user rate limit counters",
                            style = typography.xs,
                            color = colors.textMuted,
                        )
                    }
                    TerminalButton(
                        text = "reset",
                        onClick = { /* Static demo */ },
                        variant = ButtonVariant.Destructive,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        TerminalText(
                            text = "Force logout all users",
                            style = typography.sm.copy(fontWeight = FontWeight.Medium),
                            color = colors.text,
                        )
                        TerminalText(
                            text = "// invalidate all active sessions",
                            style = typography.xs,
                            color = colors.textMuted,
                        )
                    }
                    TerminalButton(
                        text = "logout all",
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
