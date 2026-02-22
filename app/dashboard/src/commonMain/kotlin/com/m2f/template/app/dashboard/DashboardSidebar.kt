package com.m2f.template.app.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.display.TerminalAvatar
import com.m2f.template.designsystem.theme.TerminalTheme
import org.jetbrains.compose.resources.stringResource
import template.app.dashboard.generated.resources.Res
import template.app.dashboard.generated.resources.common_brand_name
import template.app.dashboard.generated.resources.common_brand_prompt
import template.app.dashboard.generated.resources.dashboard_logout
import template.app.dashboard.generated.resources.nav_admin
import template.app.dashboard.generated.resources.nav_dashboard
import template.app.dashboard.generated.resources.nav_deployments
import template.app.dashboard.generated.resources.nav_logs
import template.app.dashboard.generated.resources.nav_processes
import template.app.dashboard.generated.resources.nav_settings

/**
 * Desktop sidebar navigation for the dashboard.
 *
 * Shows brand logo, navigation items (dashboard, processes, logs, deployments, settings,
 * and conditionally "admin" for users with admin/owner group role),
 * and a bottom user row with email and logout link.
 *
 * @param selectedItem The currently selected navigation item key.
 * @param userName The logged-in user's email or display name.
 * @param avatarUrl The URL of the user's avatar image, or null to show initials.
 * @param isAdmin Whether the current user has admin/owner role in a group.
 * @param onNavItemSelected Callback when a nav item is clicked.
 * @param onAdminClick Callback when the admin nav item is clicked.
 * @param onProfileClick Callback when the user row is clicked.
 * @param onLogout Callback when the logout action is triggered.
 * @param modifier Modifier for the sidebar root.
 */
@Composable
fun DashboardSidebar(
    selectedItem: String,
    userName: String,
    avatarUrl: String?,
    isAdmin: Boolean,
    onNavItemSelected: (String) -> Unit,
    onAdminClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val spacing = TerminalTheme.spacing
    val radius = TerminalTheme.radius

    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(colors.surface)
            .drawBehind {
                // Right border
                drawLine(
                    color = colors.border,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = spacing.xl, vertical = spacing.lg),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top section: brand + nav items
        Column {
            // Brand row
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

            Spacer(modifier = Modifier.height(32.dp))

            // Nav items
            val navItems = buildList {
                add("dashboard" to stringResource(Res.string.nav_dashboard))
                add("processes" to stringResource(Res.string.nav_processes))
                add("logs" to stringResource(Res.string.nav_logs))
                add("deployments" to stringResource(Res.string.nav_deployments))
                if (isAdmin) add("admin" to stringResource(Res.string.nav_admin))
                add("settings" to stringResource(Res.string.nav_settings))
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                navItems.forEach { (key, label) ->
                    SidebarNavItem(
                        label = label,
                        isSelected = selectedItem == key,
                        onClick = { if (key == "admin") onAdminClick() else onNavItemSelected(key) },
                    )
                }
            }
        }

        // Bottom section: divider + user row
        Column {
            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.border),
            )

            Spacer(modifier = Modifier.height(spacing.lg))

            // User row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(radius.sm))
                    .clickable(onClick = onProfileClick)
                    .padding(vertical = spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar (image or initials)
                TerminalAvatar(
                    initials = userName.take(1).uppercase(),
                    imageUrl = avatarUrl,
                    size = 32.dp,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    TerminalText(
                        text = userName,
                        style = typography.sm,
                        color = colors.text,
                        maxLines = 1,
                    )
                    TerminalText(
                        text = stringResource(Res.string.dashboard_logout),
                        style = typography.xs,
                        color = colors.textDim,
                        modifier = Modifier.clickable(onClick = onLogout),
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarNavItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val radius = TerminalTheme.radius

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = when {
        isSelected -> colors.accentMuted
        isHovered -> colors.inset
        else -> colors.surface
    }

    val textColor = when {
        isSelected -> colors.accent
        isHovered -> colors.text
        else -> colors.textMuted
    }

    val fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius.sm))
            .background(backgroundColor)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TerminalText(
            text = ">",
            style = typography.sm,
            color = if (isSelected) colors.accent else colors.textDim,
        )
        TerminalText(
            text = label,
            style = typography.sm.copy(fontWeight = fontWeight),
            color = textColor,
        )
    }
}
