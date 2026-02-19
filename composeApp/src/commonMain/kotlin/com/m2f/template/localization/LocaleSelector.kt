package com.m2f.template.localization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.data.TerminalDropdownMenu
import com.m2f.template.designsystem.components.data.TerminalDropdownMenuItem
import com.m2f.template.designsystem.theme.TerminalTheme
import org.jetbrains.compose.resources.stringResource
import template.composeapp.generated.resources.Res
import template.composeapp.generated.resources.locale_english
import template.composeapp.generated.resources.locale_label
import template.composeapp.generated.resources.locale_spanish

/**
 * A terminal-styled locale selector that lets the user pick their preferred language.
 *
 * Renders a row with a language label and a dropdown showing English and Español.
 * On selection change, calls [onLocaleChanged] with the BCP-47 language tag.
 *
 * @param currentLocale The currently active locale tag (e.g. "en", "es").
 * @param onLocaleChanged Callback invoked with the selected language tag.
 * @param modifier Modifier applied to the root row.
 */
@Composable
fun LocaleSelector(
    currentLocale: String,
    onLocaleChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    val label = stringResource(Res.string.locale_label)
    val englishLabel = stringResource(Res.string.locale_english)
    val spanishLabel = stringResource(Res.string.locale_spanish)

    val locales = remember {
        listOf(
            "en" to englishLabel,
            "es" to spanishLabel,
        )
    }

    val currentLabel = when (currentLocale) {
        "es" -> spanishLabel
        else -> englishLabel
    }

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TerminalText(
            text = "$label:",
            style = typography.sm,
            color = colors.textMuted,
        )

        Box {
            TerminalText(
                text = "> $currentLabel",
                style = typography.sm,
                color = colors.accent,
                modifier = Modifier
                    .clickable { expanded = true }
                    .background(colors.inset)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )

            TerminalDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                locales.forEach { (tag, name) ->
                    TerminalDropdownMenuItem(
                        text = name,
                        onClick = {
                            expanded = false
                            if (tag != currentLocale) {
                                onLocaleChanged(tag)
                            }
                        },
                    )
                }
            }
        }
    }
}
