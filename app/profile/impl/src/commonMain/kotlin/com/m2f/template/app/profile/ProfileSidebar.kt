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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.theme.AuraTheme
import com.m2f.template.models.UserTier
import org.jetbrains.compose.resources.stringResource
import template.app.profile.generated.resources.Res
import template.app.profile.generated.resources.sidebar_brand_name
import template.app.profile.generated.resources.sidebar_brand_prompt
import template.app.profile.generated.resources.sidebar_logout
import template.app.profile.generated.resources.sidebar_nav_free_profile
import template.app.profile.generated.resources.sidebar_nav_free_preferences
import template.app.profile.generated.resources.sidebar_nav_free_billing
import template.app.profile.generated.resources.sidebar_nav_paid_profile
import template.app.profile.generated.resources.sidebar_nav_paid_preferences
import template.app.profile.generated.resources.sidebar_nav_paid_team_access
import template.app.profile.generated.resources.sidebar_nav_paid_analytics
import template.app.profile.generated.resources.sidebar_nav_paid_billing
import template.app.profile.generated.resources.sidebar_nav_paid_export
import template.app.profile.generated.resources.sidebar_nav_premium_profile
import template.app.profile.generated.resources.sidebar_nav_premium_preferences
import template.app.profile.generated.resources.sidebar_nav_premium_team_access
import template.app.profile.generated.resources.sidebar_nav_premium_webhooks
import template.app.profile.generated.resources.sidebar_nav_premium_api_keys
import template.app.profile.generated.resources.sidebar_nav_premium_billing
import template.app.profile.generated.resources.sidebar_nav_premium_priority_support
import template.app.profile.generated.resources.sidebar_nav_admin_profile
import template.app.profile.generated.resources.sidebar_nav_admin_user_management
import template.app.profile.generated.resources.sidebar_nav_admin_groups
import template.app.profile.generated.resources.sidebar_nav_admin_permissions
import template.app.profile.generated.resources.sidebar_nav_admin_analytics
import template.app.profile.generated.resources.sidebar_nav_admin_audit_log
import template.app.profile.generated.resources.sidebar_nav_admin_org_settings
import template.app.profile.generated.resources.sidebar_nav_admin_admin_tools
import template.app.profile.generated.resources.sidebar_nav_admin_reports
import template.app.profile.generated.resources.sidebar_nav_poweradmin_user_directory
import template.app.profile.generated.resources.sidebar_nav_poweradmin_admin_identity
import template.app.profile.generated.resources.sidebar_nav_poweradmin_access_matrix
import template.app.profile.generated.resources.sidebar_nav_poweradmin_platform_analytics
import template.app.profile.generated.resources.sidebar_nav_poweradmin_monitoring
import template.app.profile.generated.resources.sidebar_nav_poweradmin_alerts
import template.app.profile.generated.resources.sidebar_nav_poweradmin_system_status
import template.app.profile.generated.resources.sidebar_nav_poweradmin_configuration
import template.app.profile.generated.resources.sidebar_nav_poweradmin_maintenance
import template.app.profile.generated.resources.sidebar_nav_poweradmin_system_logs
import template.app.profile.generated.resources.sidebar_nav_poweradmin_danger_zone
import template.app.profile.generated.resources.sidebar_nav_privacy
import template.app.profile.generated.resources.sidebar_footer_free_title
import template.app.profile.generated.resources.sidebar_footer_free_subtitle
import template.app.profile.generated.resources.sidebar_footer_paid_title
import template.app.profile.generated.resources.sidebar_footer_paid_subtitle
import template.app.profile.generated.resources.sidebar_footer_premium_title
import template.app.profile.generated.resources.sidebar_footer_premium_subtitle

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
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val spacing = AuraTheme.spacing
    val radius = AuraTheme.radius

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
                AuraText(
                    text = stringResource(Res.string.sidebar_brand_prompt),
                    style = typography.md.copy(fontWeight = FontWeight.Bold),
                    color = colors.accent,
                )
                AuraText(
                    text = stringResource(Res.string.sidebar_brand_name),
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
                Column(modifier = Modifier.weight(1f)) {
                    AuraText(
                        text = userName,
                        style = typography.sm,
                        color = colors.text,
                        maxLines = 1,
                    )
                    AuraText(
                        text = stringResource(Res.string.sidebar_logout),
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
            "profile" to stringResource(Res.string.sidebar_nav_free_profile),
            "preferences" to stringResource(Res.string.sidebar_nav_free_preferences),
            "privacy" to stringResource(Res.string.sidebar_nav_privacy),
            "billing" to stringResource(Res.string.sidebar_nav_free_billing),
        )
        is UserTier.Paid -> listOf(
            "profile" to stringResource(Res.string.sidebar_nav_paid_profile),
            "preferences" to stringResource(Res.string.sidebar_nav_paid_preferences),
            "team" to stringResource(Res.string.sidebar_nav_paid_team_access),
            "analytics" to stringResource(Res.string.sidebar_nav_paid_analytics),
            "privacy" to stringResource(Res.string.sidebar_nav_privacy),
            "billing" to stringResource(Res.string.sidebar_nav_paid_billing),
            "export" to stringResource(Res.string.sidebar_nav_paid_export),
        )
        is UserTier.Premium -> listOf(
            "profile" to stringResource(Res.string.sidebar_nav_premium_profile),
            "preferences" to stringResource(Res.string.sidebar_nav_premium_preferences),
            "team" to stringResource(Res.string.sidebar_nav_premium_team_access),
            "webhooks" to stringResource(Res.string.sidebar_nav_premium_webhooks),
            "api-keys" to stringResource(Res.string.sidebar_nav_premium_api_keys),
            "privacy" to stringResource(Res.string.sidebar_nav_privacy),
            "billing" to stringResource(Res.string.sidebar_nav_premium_billing),
            "support" to stringResource(Res.string.sidebar_nav_premium_priority_support),
        )
        is UserTier.Admin -> {
            // Management section + Tools section
            listOf(
                "profile" to stringResource(Res.string.sidebar_nav_admin_profile),
                "users" to stringResource(Res.string.sidebar_nav_admin_user_management),
                "groups" to stringResource(Res.string.sidebar_nav_admin_groups),
                "permissions" to stringResource(Res.string.sidebar_nav_admin_permissions),
                "analytics" to stringResource(Res.string.sidebar_nav_admin_analytics),
                "audit" to stringResource(Res.string.sidebar_nav_admin_audit_log),
                "settings" to stringResource(Res.string.sidebar_nav_admin_org_settings),
                "privacy" to stringResource(Res.string.sidebar_nav_privacy),
                "tools" to stringResource(Res.string.sidebar_nav_admin_admin_tools),
                "reports" to stringResource(Res.string.sidebar_nav_admin_reports),
            )
        }
        is UserTier.PowerAdmin -> {
            // CRM section + System section
            listOf(
                "directory" to stringResource(Res.string.sidebar_nav_poweradmin_user_directory),
                "identity" to stringResource(Res.string.sidebar_nav_poweradmin_admin_identity),
                "access" to stringResource(Res.string.sidebar_nav_poweradmin_access_matrix),
                "analytics" to stringResource(Res.string.sidebar_nav_poweradmin_platform_analytics),
                "monitoring" to stringResource(Res.string.sidebar_nav_poweradmin_monitoring),
                "alerts" to stringResource(Res.string.sidebar_nav_poweradmin_alerts),
                "system" to stringResource(Res.string.sidebar_nav_poweradmin_system_status),
                "config" to stringResource(Res.string.sidebar_nav_poweradmin_configuration),
                "maintenance" to stringResource(Res.string.sidebar_nav_poweradmin_maintenance),
                "logs" to stringResource(Res.string.sidebar_nav_poweradmin_system_logs),
                "privacy" to stringResource(Res.string.sidebar_nav_privacy),
                "danger" to stringResource(Res.string.sidebar_nav_poweradmin_danger_zone),
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
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val radius = AuraTheme.radius

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
                    AuraText(
                        text = stringResource(Res.string.sidebar_footer_free_title),
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.accent,
                    )
                    AuraText(
                        text = stringResource(Res.string.sidebar_footer_free_subtitle),
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
                    AuraText(
                        text = stringResource(Res.string.sidebar_footer_paid_title),
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.accent,
                    )
                    AuraText(
                        text = stringResource(Res.string.sidebar_footer_paid_subtitle),
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
                    AuraText(
                        text = stringResource(Res.string.sidebar_footer_premium_title),
                        style = typography.sm.copy(fontWeight = FontWeight.Medium),
                        color = colors.success,
                    )
                    AuraText(
                        text = stringResource(Res.string.sidebar_footer_premium_subtitle),
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
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val radius = AuraTheme.radius

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
        AuraText(
            text = ">",
            style = typography.sm,
            color = if (isSelected) colors.accent else colors.textDim,
        )
        AuraText(
            text = label,
            style = typography.sm.copy(fontWeight = fontWeight),
            color = textColor,
        )
    }
}
