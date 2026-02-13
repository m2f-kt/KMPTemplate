package com.m2f.template.app.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.models.UserTier

/**
 * Profile sidebar navigation showing tier-appropriate nav items.
 *
 * Each tier shows a different set of navigation sections:
 * - Free: 3 items + upgrade card
 * - Paid: 6 items + premium upgrade info
 * - Premium: 7 items + premium success alert
 * - Admin: 7 management + 2 tools items
 * - PowerAdmin: CRM (6) + System (5) items
 *
 * @param tier The current user tier.
 * @param userName The user's display name or email.
 * @param selectedItem The currently selected nav item key.
 * @param onNavItemSelected Callback when a nav item is clicked.
 * @param onLogout Callback when logout is triggered.
 * @param modifier Modifier for the sidebar root.
 */
@Composable
fun ProfileSidebar(
    tier: UserTier,
    userName: String,
    selectedItem: String,
    onNavItemSelected: (String) -> Unit,
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
        // Top: brand + tier-specific nav items
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // Brand row
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
                    text = "profile",
                    style = typography.md.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.text,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tier-specific navigation items
            TierNavItems(
                tier = tier,
                selectedItem = selectedItem,
                onNavItemSelected = onNavItemSelected,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tier-specific footer content
            TierFooterContent(tier = tier)
        }

        // Bottom: divider + user row + logout
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.border),
            )

            Spacer(modifier = Modifier.height(spacing.lg))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    TerminalText(
                        text = userName,
                        style = typography.sm,
                        color = colors.text,
                        maxLines = 1,
                    )
                    TerminalText(
                        text = "$ logout",
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
private fun TierNavItems(
    tier: UserTier,
    selectedItem: String,
    onNavItemSelected: (String) -> Unit,
) {
    val navItems = when (tier) {
        is UserTier.Free -> listOf(
            "profile" to "profile",
            "preferences" to "preferences",
            "billing" to "billing",
        )
        is UserTier.Paid -> listOf(
            "profile" to "profile",
            "preferences" to "preferences",
            "team" to "team access",
            "analytics" to "analytics",
            "billing" to "billing",
            "export" to "export",
        )
        is UserTier.Premium -> listOf(
            "profile" to "profile",
            "preferences" to "preferences",
            "team" to "team access",
            "webhooks" to "webhooks",
            "api-keys" to "API keys",
            "billing" to "billing",
            "support" to "priority support",
        )
        is UserTier.Admin -> {
            // Management section + Tools section
            listOf(
                "profile" to "profile",
                "users" to "user management",
                "groups" to "groups",
                "permissions" to "permissions",
                "analytics" to "analytics",
                "audit" to "audit log",
                "settings" to "org settings",
                "tools" to "admin tools",
                "reports" to "reports",
            )
        }
        is UserTier.PowerAdmin -> {
            // CRM section + System section
            listOf(
                "directory" to "user directory",
                "identity" to "admin identity",
                "access" to "access matrix",
                "analytics" to "platform analytics",
                "monitoring" to "monitoring",
                "alerts" to "alerts",
                "system" to "system status",
                "config" to "configuration",
                "maintenance" to "maintenance",
                "logs" to "system logs",
                "danger" to "danger zone",
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        navItems.forEach { (key, label) ->
            ProfileNavItem(
                label = label,
                isSelected = selectedItem == key,
                onClick = { onNavItemSelected(key) },
            )
        }
    }
}

@Composable
private fun TierFooterContent(tier: UserTier) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val radius = TerminalTheme.radius

    when (tier) {
        is UserTier.Free -> {
            // Upgrade card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(radius.md))
                    .background(colors.accentMuted)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TerminalText(
                        text = "> upgrade available",
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.accent,
                    )
                    TerminalText(
                        text = "// unlock team access, webhooks, and more",
                        style = typography.xs,
                        color = colors.textMuted,
                    )
                }
            }
        }
        is UserTier.Paid -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(radius.md))
                    .background(colors.accentMuted)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TerminalText(
                        text = "> premium available",
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.accent,
                    )
                    TerminalText(
                        text = "// get priority support, API keys, webhooks",
                        style = typography.xs,
                        color = colors.textMuted,
                    )
                }
            }
        }
        is UserTier.Premium -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(radius.md))
                    .background(colors.successBg)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TerminalText(
                        text = "\u2713 premium active",
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.success,
                    )
                    TerminalText(
                        text = "// all features unlocked",
                        style = typography.xs,
                        color = colors.textMuted,
                    )
                }
            }
        }
        is UserTier.Admin, is UserTier.PowerAdmin -> {
            // No special footer for admin tiers
        }
    }
}

@Composable
private fun ProfileNavItem(
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
