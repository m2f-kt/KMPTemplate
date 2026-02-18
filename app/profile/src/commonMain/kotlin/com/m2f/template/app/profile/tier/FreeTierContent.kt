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
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.TerminalButton
import com.m2f.template.designsystem.components.card.CardVariant
import com.m2f.template.designsystem.components.card.TerminalCard
import com.m2f.template.designsystem.components.data.TerminalList
import com.m2f.template.designsystem.components.data.TerminalListItem
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.components.feedback.TerminalProgress
import com.m2f.template.designsystem.theme.TerminalTheme

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
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Warning alert
        TerminalAlert(
            message = "You are on the free tier. Some features are restricted.",
            variant = AlertVariant.Warning,
            title = "free_tier",
        )

        // Usage limits card
        TerminalCard(
            title = "usage_limits",
            description = "// current period",
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
                        TerminalText(
                            text = "API calls",
                            style = typography.sm,
                            color = colors.textMuted,
                        )
                        TerminalText(
                            text = "847 / 1,000",
                            style = typography.sm.copy(fontWeight = FontWeight.Medium),
                            color = colors.text,
                        )
                    }
                    TerminalProgress(
                        progress = 0.847f,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TerminalText(
                            text = "Storage",
                            style = typography.sm,
                            color = colors.textMuted,
                        )
                        TerminalText(
                            text = "234 MB / 500 MB",
                            style = typography.sm.copy(fontWeight = FontWeight.Medium),
                            color = colors.text,
                        )
                    }
                    TerminalProgress(
                        progress = 0.468f,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Preferences section
        TerminalCard(
            title = "preferences",
            description = "// notification settings",
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PreferenceRow(label = "email_notifications", value = "enabled")
                PreferenceRow(label = "theme", value = "system")
                PreferenceRow(label = "language", value = "en-US")
            }
        }

        // Locked features (dimmed)
        Column(
            modifier = Modifier.alpha(0.5f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TerminalText(
                text = "locked_features",
                style = typography.md.copy(fontWeight = FontWeight.Medium),
                color = colors.textMuted,
            )

            TerminalList {
                TerminalListItem(
                    text = "Team access",
                    subtitle = "// requires paid tier",
                    trailingContent = { color ->
                        TerminalBadge(text = "locked", variant = BadgeVariant.Default)
                    },
                )
                TerminalListItem(
                    text = "Webhook configuration",
                    subtitle = "// requires premium tier",
                    trailingContent = { color ->
                        TerminalBadge(text = "locked", variant = BadgeVariant.Default)
                    },
                )
                TerminalListItem(
                    text = "API key management",
                    subtitle = "// requires premium tier",
                    trailingContent = { color ->
                        TerminalBadge(text = "locked", variant = BadgeVariant.Default)
                    },
                )
                TerminalListItem(
                    text = "Priority support",
                    subtitle = "// requires premium tier",
                    trailingContent = { color ->
                        TerminalBadge(text = "locked", variant = BadgeVariant.Default)
                    },
                )
            }
        }

        // Upgrade CTA
        TerminalCard(
            title = "upgrade_plan",
            variant = CardVariant.Accent,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TerminalText(
                    text = "Unlock team access, webhooks, API keys, and priority support.",
                    style = typography.sm,
                    color = colors.textMuted,
                )
                TerminalButton(
                    text = "> upgrade to paid",
                    onClick = { /* Static demo */ },
                    variant = ButtonVariant.Default,
                )
            }
        }
    }
}

@Composable
private fun PreferenceRow(label: String, value: String) {
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
