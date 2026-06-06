package com.m2f.template.app.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.theme.AuraTheme
import com.m2f.template.designsystem.theme.rememberAuraRipple
import org.jetbrains.compose.resources.stringResource
import template.app.dashboard.generated.resources.Res
import template.app.dashboard.generated.resources.bottom_nav_admin
import template.app.dashboard.generated.resources.bottom_nav_home
import template.app.dashboard.generated.resources.bottom_nav_logs
import template.app.dashboard.generated.resources.bottom_nav_processes
import template.app.dashboard.generated.resources.bottom_nav_settings

/**
 * Mobile bottom navigation bar for the dashboard.
 *
 * Displays tabs (Home, Processes, Logs, conditionally Admin, Settings) in a 64dp tall bar
 * with a top border. Active tab uses accent color, inactive uses textDim.
 *
 * @param selectedTab The currently selected tab key.
 * @param isAdmin Whether the current user has admin/owner role in a group.
 * @param onTabSelected Callback when a tab is clicked.
 * @param onAdminClick Callback when the admin tab is clicked.
 * @param modifier Modifier for the bottom nav root.
 */
@Composable
fun DashboardBottomNav(
    selectedTab: String,
    isAdmin: Boolean,
    onTabSelected: (String) -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors

    val tabs = buildList {
        add(BottomTab("dashboard", "~", stringResource(Res.string.bottom_nav_home)))
        add(BottomTab("processes", "#", stringResource(Res.string.bottom_nav_processes)))
        add(BottomTab("logs", "$", stringResource(Res.string.bottom_nav_logs)))
        if (isAdmin) add(BottomTab("admin", "@", stringResource(Res.string.bottom_nav_admin)))
        add(BottomTab("settings", "%", stringResource(Res.string.bottom_nav_settings)))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(colors.surface)
            .drawBehind {
                // Top border
                drawLine(
                    color = colors.border,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            BottomNavTab(
                tab = tab,
                isSelected = selectedTab == tab.key,
                onClick = { if (tab.key == "admin") onAdminClick() else onTabSelected(tab.key) },
            )
        }
    }
}

private data class BottomTab(
    val key: String,
    val icon: String,
    val label: String,
)

@Composable
private fun BottomNavTab(
    tab: BottomTab,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    val color = if (isSelected) colors.accent else colors.textDim
    val fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = rememberAuraRipple(bounded = false),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AuraText(
            text = tab.icon,
            style = typography.md.copy(fontWeight = FontWeight.Bold),
            color = color,
        )
        Spacer(modifier = Modifier.height(2.dp))
        AuraText(
            text = tab.label,
            style = typography.xs.copy(fontWeight = fontWeight),
            color = color,
        )
    }
}
