package com.m2f.template.app.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import org.jetbrains.compose.resources.stringResource
import template.app.dashboard.generated.resources.Res
import template.app.dashboard.generated.resources.common_brand_name
import template.app.dashboard.generated.resources.common_brand_prompt
import template.app.dashboard.generated.resources.dashboard_active_processes
import template.app.dashboard.generated.resources.dashboard_deployment_build
import template.app.dashboard.generated.resources.dashboard_deployment_deploy
import template.app.dashboard.generated.resources.dashboard_deployment_status
import template.app.dashboard.generated.resources.dashboard_deployment_subtitle
import template.app.dashboard.generated.resources.dashboard_deployment_tests
import template.app.dashboard.generated.resources.dashboard_nodes_active
import template.app.dashboard.generated.resources.dashboard_recent_activity
import template.app.dashboard.generated.resources.dashboard_status_pending
import template.app.dashboard.generated.resources.dashboard_system_overview
import template.app.dashboard.generated.resources.dashboard_table_cpu
import template.app.dashboard.generated.resources.dashboard_table_memory
import template.app.dashboard.generated.resources.dashboard_table_name
import template.app.dashboard.generated.resources.dashboard_table_pid
import template.app.dashboard.generated.resources.dashboard_table_status
import template.app.dashboard.generated.resources.dashboard_under_construction
import template.app.dashboard.generated.resources.metric_avg_latency
import template.app.dashboard.generated.resources.metric_error_rate
import template.app.dashboard.generated.resources.metric_requests
import template.app.dashboard.generated.resources.metric_uptime
import template.app.dashboard.generated.resources.activity_auto_scaling
import template.app.dashboard.generated.resources.activity_deploy
import template.app.dashboard.generated.resources.activity_memory_alert
import template.app.dashboard.generated.resources.activity_ssl_renewed

/**
 * Responsive dashboard screen with desktop sidebar and mobile bottom nav layouts.
 *
 * Uses BoxWithConstraints to switch between desktop (>840dp) and mobile layouts.
 * Desktop shows a 260dp sidebar + main content area.
 * Mobile shows vertical content with a bottom navigation bar.
 *
 * Content switching is state-based: sidebar/bottom nav items update [DashboardModel.selectedNavItem]
 * and the content area renders the appropriate section inline. The sidebar/bottom nav remain
 * visible at all times.
 *
 * @param state The current dashboard state with mock data.
 * @param onNavItemSelected Callback when a sidebar/bottom nav item is selected.
 * @param onProfileClick Callback to navigate to the standalone ProfileRoute.
 * @param onLogout Callback when the user logs out.
 * @param modifier Modifier for the screen root.
 */
@Composable
fun DashboardScreen(
    state: DashboardModel,
    onNavItemSelected: (String) -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(colors.bg),
    ) {
        if (maxWidth > 840.dp) {
            // Desktop layout
            DesktopDashboard(
                state = state,
                onNavItemSelected = onNavItemSelected,
                onProfileClick = onProfileClick,
                onLogout = onLogout,
                onAdminClick = onAdminClick,
            )
        } else {
            // Mobile layout
            MobileDashboard(
                state = state,
                onTabSelected = onNavItemSelected,
                onProfileClick = onProfileClick,
                onAdminClick = onAdminClick,
            )
        }
    }
}

// -- Desktop Layout --

@Composable
private fun DesktopDashboard(
    state: DashboardModel,
    onNavItemSelected: (String) -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    onAdminClick: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        DashboardSidebar(
            selectedItem = state.selectedNavItem,
            userName = state.userName,
            isAdmin = state.isAdmin,
            onNavItemSelected = onNavItemSelected,
            onAdminClick = onAdminClick,
            onProfileClick = onProfileClick,
            onLogout = onLogout,
        )

        // State-based content switching
        when (state.selectedNavItem) {
            "dashboard" -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    // Header row
                    DesktopHeader(userName = state.userName, onProfileClick = onProfileClick)

                    // Metrics row
                    MetricsRow(metrics = state.metrics)

                    // Columns row: process table + deployment/activity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        // Left: process table
                        Column(modifier = Modifier.weight(1f)) {
                            ProcessTable(processes = state.processes)
                        }

                        // Right: deployment + activity
                        Column(
                            modifier = Modifier.width(340.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            DeploymentCard(deployment = state.deployment)
                            ActivityList(activities = state.activities)
                        }
                    }
                }
            }
            "processes" -> {
                PlaceholderContent(
                    title = "> processes",
                    modifier = Modifier.weight(1f),
                )
            }
            "logs" -> {
                PlaceholderContent(
                    title = "> logs",
                    modifier = Modifier.weight(1f),
                )
            }
            "deployments" -> {
                PlaceholderContent(
                    title = "> deployments",
                    modifier = Modifier.weight(1f),
                )
            }
            "settings" -> {
                PlaceholderContent(
                    title = "> settings",
                    modifier = Modifier.weight(1f),
                )
            }
            else -> {
                PlaceholderContent(
                    title = "> ${state.selectedNavItem}",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DesktopHeader(
    userName: String,
    onProfileClick: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TerminalText(
            text = stringResource(Res.string.dashboard_system_overview),
            style = typography.xxl.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
        )

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onProfileClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TerminalText(
                text = userName,
                style = typography.sm,
                color = colors.textMuted,
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.accentMuted),
                contentAlignment = Alignment.Center,
            ) {
                TerminalText(
                    text = userName.take(1).uppercase(),
                    style = typography.sm.copy(fontWeight = FontWeight.Medium),
                    color = colors.accent,
                )
            }
        }
    }
}

// -- Mobile Layout --

@Composable
private fun MobileDashboard(
    state: DashboardModel,
    onTabSelected: (String) -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(modifier = Modifier.fillMaxSize()) {
        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Header: brand + avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TerminalText(
                        text = stringResource(Res.string.common_brand_prompt),
                        style = typography.md.copy(fontWeight = FontWeight.Bold),
                        color = colors.accent,
                    )
                    TerminalText(
                        text = stringResource(Res.string.common_brand_name),
                        style = typography.md.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.text,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.accentMuted)
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center,
                ) {
                    TerminalText(
                        text = state.userName.take(1).uppercase(),
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.accent,
                    )
                }
            }

            // State-based content switching
            when (state.selectedNavItem) {
                "dashboard" -> {
                    // Title block
                    Column {
                        TerminalText(
                            text = stringResource(Res.string.dashboard_system_overview),
                            style = typography.xxl.copy(fontWeight = FontWeight.Bold),
                            color = colors.text,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TerminalText(
                            text = stringResource(Res.string.dashboard_nodes_active),
                            style = typography.xs,
                            color = colors.textDim,
                        )
                    }

                    // Metrics: 2x2 grid
                    MobileMetricsGrid(metrics = state.metrics)

                    // Process list (simplified)
                    MobileProcessList(processes = state.processes)

                    // Deployment card
                    DeploymentCard(deployment = state.deployment)

                    // Activity list
                    ActivityList(activities = state.activities)
                }
                "processes" -> {
                    MobilePlaceholderContent(title = "> processes")
                }
                "logs" -> {
                    MobilePlaceholderContent(title = "> logs")
                }
                "settings" -> {
                    MobilePlaceholderContent(title = "> settings")
                }
                else -> {
                    MobilePlaceholderContent(title = "> ${state.selectedNavItem}")
                }
            }
        }

        // Bottom nav bar (always visible)
        DashboardBottomNav(
            selectedTab = state.selectedNavItem,
            isAdmin = state.isAdmin,
            onTabSelected = onTabSelected,
            onAdminClick = onAdminClick,
        )
    }
}

@Composable
private fun MobileMetricsGrid(metrics: List<DashboardMockData.MetricItem>) {
    // 2x2 grid of metric cards
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (rowIndex in metrics.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                for (colIndex in rowIndex until minOf(rowIndex + 2, metrics.size)) {
                    MetricCard(
                        metric = metrics[colIndex],
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileProcessList(processes: List<DashboardMockData.ProcessItem>) {
    TerminalList(title = stringResource(Res.string.dashboard_active_processes), count = processes.size) {
        processes.forEachIndexed { index, process ->
            TerminalListItem(
                text = process.name,
                subtitle = "PID: ${process.pid} | CPU: ${process.cpu} | ${process.memory}",
                trailingContent = { color ->
                    TerminalBadge(
                        text = process.status,
                        variant = BadgeVariant.Success,
                    )
                },
            )
        }
    }
}

// -- Placeholder Content --

@Composable
private fun PlaceholderContent(
    title: String,
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
        TerminalText(
            text = title,
            style = typography.xxl.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
        )
        TerminalCard(
            title = title.removePrefix("> "),
            description = stringResource(Res.string.dashboard_under_construction),
            variant = CardVariant.Default,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TerminalBadge(
                    text = stringResource(Res.string.dashboard_status_pending),
                    variant = BadgeVariant.Warning,
                    icon = "\u25D0",
                )
            }
        }
    }
}

@Composable
private fun MobilePlaceholderContent(title: String) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    TerminalText(
        text = title,
        style = typography.xxl.copy(fontWeight = FontWeight.Bold),
        color = colors.text,
    )
    TerminalCard(
        title = title.removePrefix("> "),
        description = stringResource(Res.string.dashboard_under_construction),
        variant = CardVariant.Default,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TerminalBadge(
                text = stringResource(Res.string.dashboard_status_pending),
                variant = BadgeVariant.Warning,
                icon = "\u25D0",
            )
        }
    }
}

// -- Shared Components --

@Composable
private fun MetricsRow(metrics: List<DashboardMockData.MetricItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        metrics.forEach { metric ->
            MetricCard(
                metric = metric,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetricCard(
    metric: DashboardMockData.MetricItem,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    val variant = if (metric.isHighlighted) CardVariant.Highlighted else CardVariant.Default

    TerminalCard(
        title = stringResource(metric.labelRes),
        variant = variant,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TerminalText(
                text = metric.value,
                style = typography.xxl.copy(fontWeight = FontWeight.Bold),
                color = if (metric.isHighlighted) colors.accent else colors.text,
            )
            TerminalBadge(
                text = metric.change,
                variant = if (metric.isUp) BadgeVariant.Success else BadgeVariant.Warning,
                icon = if (metric.isUp) "\u2191" else "\u2193",
            )
        }
    }
}

@Composable
private fun ProcessTable(processes: List<DashboardMockData.ProcessItem>) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TerminalText(
            text = stringResource(Res.string.dashboard_active_processes),
            style = typography.md.copy(fontWeight = FontWeight.Medium),
            color = colors.text,
        )

        TerminalTable(
            headers = listOf(
                stringResource(Res.string.dashboard_table_pid),
                stringResource(Res.string.dashboard_table_name),
                stringResource(Res.string.dashboard_table_cpu),
                stringResource(Res.string.dashboard_table_memory),
                stringResource(Res.string.dashboard_table_status),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            processes.forEachIndexed { index, process ->
                TerminalTableRow(
                    showBottomBorder = index < processes.lastIndex,
                ) {
                    TerminalTableCell(text = process.pid.toString(), secondary = true)
                    TerminalTableCell(text = process.name)
                    TerminalTableCell(text = process.cpu, secondary = true)
                    TerminalTableCell(text = process.memory, secondary = true)
                    Box(modifier = Modifier.weight(1f)) {
                        TerminalBadge(
                            text = process.status,
                            variant = BadgeVariant.Success,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeploymentCard(deployment: DashboardMockData.DeploymentStatus) {
    TerminalCard(
        title = stringResource(Res.string.dashboard_deployment_status),
        description = stringResource(Res.string.dashboard_deployment_subtitle),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TerminalProgress(
                progress = deployment.build,
                label = stringResource(Res.string.dashboard_deployment_build),
            )
            TerminalProgress(
                progress = deployment.tests,
                label = stringResource(Res.string.dashboard_deployment_tests),
            )
            TerminalProgress(
                progress = deployment.deploy,
                label = stringResource(Res.string.dashboard_deployment_deploy),
            )
        }
    }
}

@Composable
private fun ActivityList(activities: List<DashboardMockData.ActivityItem>) {
    TerminalList(title = stringResource(Res.string.dashboard_recent_activity), count = activities.size) {
        activities.forEachIndexed { index, activity ->
            TerminalListItem(
                text = stringResource(activity.titleRes),
                subtitle = "${activity.location} \u2022 ${activity.time}",
                leadingContent = { iconColor ->
                    ActivityIcon(icon = activity.icon, color = iconColor)
                },
            )
        }
    }
}

@Composable
private fun ActivityIcon(icon: String, color: androidx.compose.ui.graphics.Color) {
    val symbol = when (icon) {
        "git-commit-horizontal" -> "\u25CF"
        "triangle-alert" -> "\u26A0"
        "circle-check" -> "\u2713"
        "arrow-up-right" -> "\u2197"
        else -> ">"
    }
    TerminalText(
        text = symbol,
        style = TerminalTheme.typography.sm,
        color = color,
    )
}
