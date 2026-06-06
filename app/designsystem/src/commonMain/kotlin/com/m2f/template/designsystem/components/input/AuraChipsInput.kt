package com.m2f.template.designsystem.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * A glossary token / chips input (handoff §B3 `.at-chips`): existing terms render as mono pills with
 * an `×` to remove, and a transparent inline text field adds a term on Enter or a trailing comma.
 * The container is `bg-elev-1` (`surface`) with a `border` hairline, radius `md`. Foundation only.
 *
 * @param terms the current glossary terms (unique, order-preserving).
 * @param onTermsChange invoked with the new list on add/remove (deduped, blanks dropped).
 * @param placeholder hint shown in the inline add field when there are no terms.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuraChipsInput(
    terms: List<String>,
    onTermsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Add term…",
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val borders = AuraTheme.borders
    val radius = AuraTheme.radius

    var draft by remember { mutableStateOf("") }
    val containerShape = RoundedCornerShape(radius.md)

    fun commit(raw: String) {
        val term = raw.trim().trimEnd(',').trim()
        if (term.isNotEmpty() && terms.none { it.equals(term, ignoreCase = true) }) {
            onTermsChange(terms + term)
        }
        draft = ""
    }

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .clip(containerShape)
            .background(colors.surface)
            .border(borders.thin, colors.border, containerShape)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        terms.forEach { term ->
            TermChip(term = term, onRemove = { onTermsChange(terms - term) })
        }
        BasicTextField(
            value = draft,
            onValueChange = { next ->
                // Commit on a typed comma; otherwise keep editing the draft.
                if (next.endsWith(",")) commit(next) else draft = next
            },
            textStyle = typography.mono.copy(color = colors.text),
            cursorBrush = SolidColor(colors.neonCyan),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { commit(draft) }),
            modifier = Modifier.widthIn(min = 80.dp),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (draft.isEmpty() && terms.isEmpty()) {
                        BasicText(text = placeholder, style = typography.mono.copy(color = colors.textDim))
                    }
                    inner()
                }
            },
        )
    }
}

@Composable
private fun TermChip(term: String, onRemove: () -> Unit) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val borders = AuraTheme.borders
    val shape = RoundedCornerShape(AuraTheme.radius.sm)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(colors.inset)
            .border(borders.thin, colors.border, shape)
            .padding(start = 9.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(text = term, style = typography.mono.copy(color = colors.textMuted))
        BasicText(
            text = "×",
            style = typography.mono.copy(color = colors.textDim),
            modifier = Modifier.clip(RoundedCornerShape(AuraTheme.radius.full)).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onRemove,
            ).padding(horizontal = 2.dp),
        )
    }
}
