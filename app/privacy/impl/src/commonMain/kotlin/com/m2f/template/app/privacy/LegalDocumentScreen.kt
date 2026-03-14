package com.m2f.template.app.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.TerminalButton
import com.m2f.template.designsystem.components.display.TerminalDivider
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.components.feedback.TerminalProgress
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * Legal document viewer screen showing privacy policy, terms of service, etc.
 *
 * Desktop (>840dp): Centered column with max width ~700dp.
 * Mobile (<=840dp): Full-width with padding.
 */
@Composable
fun LegalDocumentScreen(
    state: LegalDocumentModel,
    onSwitchLocale: (String) -> Unit,
    onBack: () -> Unit,
) {
    val colors = TerminalTheme.colors

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        val isDesktop = maxWidth > 840.dp
        val contentModifier = if (isDesktop) {
            Modifier
                .fillMaxSize()
                .padding(48.dp)
        } else {
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        }

        Column(
            modifier = contentModifier,
            horizontalAlignment = if (isDesktop) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            LegalDocumentContent(
                state = state,
                onSwitchLocale = onSwitchLocale,
                onBack = onBack,
                modifier = if (isDesktop) {
                    Modifier.widthIn(max = 700.dp)
                } else {
                    Modifier.fillMaxWidth()
                },
            )
        }
    }
}

@Composable
private fun LegalDocumentContent(
    state: LegalDocumentModel,
    onSwitchLocale: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header row: back button + title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TerminalButton(
                text = "Back",
                onClick = onBack,
                variant = ButtonVariant.Ghost,
            )

            if (state.document != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TerminalBadge(
                        text = "v${state.document.version}",
                        variant = BadgeVariant.Default,
                    )
                }
            }
        }

        // Loading
        if (state.loading) {
            TerminalProgress(label = "Loading document...")
        }

        // Error
        if (state.error != null) {
            TerminalAlert(
                message = state.error.code,
                variant = AlertVariant.Error,
                title = "Error",
            )
        }

        // Document content
        if (state.document != null) {
            // Document type title
            TerminalText(
                text = consentTypeLabel(state.document.type),
                style = typography.xxl.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
                color = colors.text,
            )

            // Metadata row: published date + locale switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TerminalText(
                    text = "Published: ${state.document.publishedAt}",
                    style = typography.xs,
                    color = colors.textDim,
                )

                // Locale switcher
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    LocaleButton(
                        locale = "en",
                        isActive = state.document.locale == "en",
                        onClick = { onSwitchLocale("en") },
                    )
                    LocaleButton(
                        locale = "es",
                        isActive = state.document.locale == "es",
                        onClick = { onSwitchLocale("es") },
                    )
                }
            }

            TerminalDivider()

            // Scrollable content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                TerminalText(
                    text = state.document.content,
                    style = typography.sm,
                    color = colors.text,
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LocaleButton(
    locale: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    TerminalButton(
        text = locale.uppercase(),
        onClick = onClick,
        variant = if (isActive) ButtonVariant.Default else ButtonVariant.Secondary,
    )
}
