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

/**
 * Responsive dashboard screen with desktop sidebar and mobile bottom nav layouts.
 *
 * Uses BoxWithConstraints to switch between desktop (>840dp) and mobile layouts.
 * Desktop shows a 260dp sidebar + main content area.
 * Mobile shows vertical content with a bottom navigation bar.
 *
 * @param state The current dashboard state with mock data.
 * @param onNavItemSelected Callback when a sidebar/bottom nav item is selected.
 * @param onProfileClick Callback when the user profile is clicked.
 * @param onLogout Callback when the user logs out.
 * @param onNavigateToProcesses Callback to navigate to the processes screen.
 * @param onNavigateToLogs Callback to navigate to the logs screen.
 * @param onNavigateToDeployments Callback to navigate to the deployments screen.
 * @param onNavigateToSettings Callback to navigate to the settings screen.
 * @param modifier Modifier for the screen root.
 */
@Composable
fun DashboardScreen(
    state: DashboardState,
    onNavItemSelected: (String) -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToProcesses: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToDeployments: () -> Unit,
    onNavigateToSettings: () -> Unit,
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
                onNavItemSelected = { item ->
                    onNavItemSelected(item)
                    when (item) {
                        "processes" -> onNavigateToProcesses()
                        "logs" -> onNavigateToLogs()
                        "deployments" -> onNavigateToDeployments()
                        "settings" -> onNavigateToSettings()
                    }
                },
                onProfileClick = onProfileClick,
                onLogout = onLogout,
            )
        } else {
            // Mobile layout
            MobileDashboard(
                state = state,
                onTabSelected = { tab ->
                    onNavItemSelected(tab)
                    when (tab) {
                        "processes" -> onNavigateToProcesses()
                        "logs" -> onNavigateToLogs()
                        "settings" -> onNavigateToSettings()
                    }
                },
                onProfileClick = onProfileClick,
            )
        }
    }
}

// -- Desktop Layout --

@Composable
private fun DesktopDashboard(
    state: DashboardState,
    onNavItemSelected: (String) -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        DashboardSidebar(
            selectedItem = state.selectedNavItem,
            userName = state.userName,
            onNavItemSelected = onNavItemSelected,
            onProfileClick = onProfileClick,
            onLogout = onLogout,
        )

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
            text = "$ system_overview",
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
    state: DashboardState,
    onTabSelected: (String) -> Unit,
    onProfileClick: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(modifier = Modifier.fillMaxSize()) {
        // Scrollable content
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
                        text = ">_",
                        style = typography.md.copy(fontWeight = FontWeight.Bold),
                        color = colors.accent,
                    )
                    TerminalText(
                        text = "terminal",
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

            // Title block
            Column {
                TerminalText(
                    text = "$ system_overview",
                    style = typography.xxl.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
                Spacer(modifier = Modifier.height(4.dp))
                TerminalText(
                    text = "[4 nodes active]",
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

        // Bottom nav bar
        DashboardBottomNav(
            selectedTab = state.selectedNavItem,
            onTabSelected = onTabSelected,
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
    TerminalList(title = "active_processes", count = processes.size) {
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
        title = metric.label,
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
            text = "active_processes",
            style = typography.md.copy(fontWeight = FontWeight.Medium),
            color = colors.text,
        )

        TerminalTable(
            headers = listOf("PID", "NAME", "CPU", "MEMORY", "STATUS"),
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
        title = "deployment_status",
        description = "// pipeline progress",
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TerminalProgress(
                progress = deployment.build,
                label = "Build",
            )
            TerminalProgress(
                progress = deployment.tests,
                label = "Tests",
            )
            TerminalProgress(
                progress = deployment.deploy,
                label = "Deploy",
            )
        }
    }
}

@Composable
private fun ActivityList(activities: List<DashboardMockData.ActivityItem>) {
    TerminalList(title = "recent_activity", count = activities.size) {
        activities.forEachIndexed { index, activity ->
            TerminalListItem(
                text = activity.title,
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
