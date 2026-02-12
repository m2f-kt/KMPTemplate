package com.m2f.template.designsystem.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * A circular avatar element displaying 1-2 character initials.
 *
 * @param initials The initials to display (typically 1-2 characters).
 * @param modifier Modifier for the container.
 * @param size The diameter of the avatar circle.
 */
@Composable
fun TerminalAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    val circleShape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .size(size)
            .clip(circleShape)
            .background(colors.accentMuted),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = initials.take(2).uppercase(),
            style = typography.sm.copy(
                color = colors.accent,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Preview
@Composable
private fun TerminalAvatarPreview() {
    TerminalTheme {
        Row(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TerminalAvatar("AB")
            TerminalAvatar("JD", size = 48.dp)
            TerminalAvatar("X")
        }
    }
}
