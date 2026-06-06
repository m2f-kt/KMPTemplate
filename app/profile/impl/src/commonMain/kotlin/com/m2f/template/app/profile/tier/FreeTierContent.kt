package com.m2f.template.app.profile.tier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.app.profile.ProfileModel
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.AuraButton
import com.m2f.template.designsystem.components.card.CardVariant
import com.m2f.template.designsystem.components.card.AuraCard
import com.m2f.template.designsystem.components.data.AuraList
import com.m2f.template.designsystem.components.data.AuraListItem
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.AuraAlert
import com.m2f.template.designsystem.components.feedback.AuraBadge
import com.m2f.template.designsystem.components.feedback.AuraProgress
import com.m2f.template.designsystem.theme.AuraTheme
import org.jetbrains.compose.resources.stringResource
import template.app.profile.generated.resources.Res
import template.app.profile.generated.resources.tier_free_alert_message
import template.app.profile.generated.resources.tier_free_alert_title
import template.app.profile.generated.resources.tier_free_api_calls
import template.app.profile.generated.resources.tier_free_api_calls_value
import template.app.profile.generated.resources.tier_free_locked_api_keys
import template.app.profile.generated.resources.tier_free_locked_api_keys_sub
import template.app.profile.generated.resources.tier_free_locked_badge
import template.app.profile.generated.resources.tier_free_locked_features
import template.app.profile.generated.resources.tier_free_locked_priority_support
import template.app.profile.generated.resources.tier_free_locked_priority_support_sub
import template.app.profile.generated.resources.tier_free_locked_team_access
import template.app.profile.generated.resources.tier_free_locked_team_access_sub
import template.app.profile.generated.resources.tier_free_locked_webhooks
import template.app.profile.generated.resources.tier_free_locked_webhooks_sub
import template.app.profile.generated.resources.tier_free_pref_email_notifications
import template.app.profile.generated.resources.tier_free_pref_email_notifications_val
import template.app.profile.generated.resources.tier_free_pref_language
import template.app.profile.generated.resources.tier_free_pref_language_val
import template.app.profile.generated.resources.tier_free_pref_theme
import template.app.profile.generated.resources.tier_free_pref_theme_val
import template.app.profile.generated.resources.tier_free_preferences_desc
import template.app.profile.generated.resources.tier_free_preferences_title
import template.app.profile.generated.resources.tier_free_storage
import template.app.profile.generated.resources.tier_free_storage_value
import template.app.profile.generated.resources.tier_free_upgrade_button
import template.app.profile.generated.resources.tier_free_upgrade_description
import template.app.profile.generated.resources.tier_free_upgrade_title
import template.app.profile.generated.resources.tier_free_usage_desc
import template.app.profile.generated.resources.tier_free_usage_title

/**
 * Free tier profile content showing usage limits, preferences, locked features, and upgrade CTA.
 *
 * All data is static/mock -- demonstrates what a free tier user would see.
 *
 * @param state The current profile state.
 * @param modifier Modifier for the content root.
 */
@Composable
fun FreeTierContent(
    state: ProfileModel,
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Warning alert
        AuraAlert(
            message = stringResource(Res.string.tier_free_alert_message),
            variant = AlertVariant.Warning,
            title = stringResource(Res.string.tier_free_alert_title),
        )

        // Usage limits card
        AuraCard(
            title = stringResource(Res.string.tier_free_usage_title),
            description = stringResource(Res.string.tier_free_usage_desc),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        AuraText(
                            text = stringResource(Res.string.tier_free_api_calls),
                            style = typography.sm,
                            color = colors.textMuted,
                        )
                        AuraText(
                            text = stringResource(Res.string.tier_free_api_calls_value),
                            style = typography.sm.copy(fontWeight = FontWeight.Medium),
                            color = colors.text,
                        )
                    }
                    AuraProgress(
                        progress = 0.847f,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        AuraText(
                            text = stringResource(Res.string.tier_free_storage),
                            style = typography.sm,
                            color = colors.textMuted,
                        )
                        AuraText(
                            text = stringResource(Res.string.tier_free_storage_value),
                            style = typography.sm.copy(fontWeight = FontWeight.Medium),
                            color = colors.text,
                        )
                    }
                    AuraProgress(
                        progress = 0.468f,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Preferences section
        AuraCard(
            title = stringResource(Res.string.tier_free_preferences_title),
            description = stringResource(Res.string.tier_free_preferences_desc),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PreferenceRow(
                    label = stringResource(Res.string.tier_free_pref_email_notifications),
                    value = stringResource(Res.string.tier_free_pref_email_notifications_val),
                )
                PreferenceRow(
                    label = stringResource(Res.string.tier_free_pref_theme),
                    value = stringResource(Res.string.tier_free_pref_theme_val),
                )
                PreferenceRow(
                    label = stringResource(Res.string.tier_free_pref_language),
                    value = stringResource(Res.string.tier_free_pref_language_val),
                )
            }
        }

        // Locked features (dimmed)
        Column(
            modifier = Modifier.alpha(0.5f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraText(
                text = stringResource(Res.string.tier_free_locked_features),
                style = typography.md.copy(fontWeight = FontWeight.Medium),
                color = colors.textMuted,
            )

            AuraList {
                AuraListItem(
                    text = stringResource(Res.string.tier_free_locked_team_access),
                    subtitle = stringResource(Res.string.tier_free_locked_team_access_sub),
                    trailingContent = { color ->
                        AuraBadge(text = stringResource(Res.string.tier_free_locked_badge), variant = BadgeVariant.Default)
                    },
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_free_locked_webhooks),
                    subtitle = stringResource(Res.string.tier_free_locked_webhooks_sub),
                    trailingContent = { color ->
                        AuraBadge(text = stringResource(Res.string.tier_free_locked_badge), variant = BadgeVariant.Default)
                    },
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_free_locked_api_keys),
                    subtitle = stringResource(Res.string.tier_free_locked_api_keys_sub),
                    trailingContent = { color ->
                        AuraBadge(text = stringResource(Res.string.tier_free_locked_badge), variant = BadgeVariant.Default)
                    },
                )
                AuraListItem(
                    text = stringResource(Res.string.tier_free_locked_priority_support),
                    subtitle = stringResource(Res.string.tier_free_locked_priority_support_sub),
                    trailingContent = { color ->
                        AuraBadge(text = stringResource(Res.string.tier_free_locked_badge), variant = BadgeVariant.Default)
                    },
                )
            }
        }

        // Upgrade CTA
        AuraCard(
            title = stringResource(Res.string.tier_free_upgrade_title),
            variant = CardVariant.Accent,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AuraText(
                    text = stringResource(Res.string.tier_free_upgrade_description),
                    style = typography.sm,
                    color = colors.textMuted,
                )
                AuraButton(
                    text = stringResource(Res.string.tier_free_upgrade_button),
                    onClick = { /* Static demo */ },
                    variant = ButtonVariant.Default,
                )
            }
        }
    }
}

@Composable
private fun PreferenceRow(label: String, value: String) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AuraText(
            text = label,
            style = typography.sm,
            color = colors.textMuted,
        )
        AuraText(
            text = value,
            style = typography.sm.copy(fontWeight = FontWeight.Medium),
            color = colors.text,
        )
    }
}
