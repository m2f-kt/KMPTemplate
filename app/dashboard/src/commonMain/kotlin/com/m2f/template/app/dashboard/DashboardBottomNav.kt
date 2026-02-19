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
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.designsystem.theme.rememberTerminalRipple

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
    val colors = TerminalTheme.colors

    val tabs = buildList {
        add(BottomTab("dashboard", "~", "home"))
        add(BottomTab("processes", "#", "procs"))
        add(BottomTab("logs", "$", "logs"))
        if (isAdmin) add(BottomTab("admin", "@", "admin"))
        add(BottomTab("settings", "%", "config"))
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
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    val color = if (isSelected) colors.accent else colors.textDim
    val fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = rememberTerminalRipple(bounded = false),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TerminalText(
            text = tab.icon,
            style = typography.md.copy(fontWeight = FontWeight.Bold),
            color = color,
        )
        Spacer(modifier = Modifier.height(2.dp))
        TerminalText(
            text = tab.label,
            style = typography.xs.copy(fontWeight = fontWeight),
            color = color,
        )
    }
}
