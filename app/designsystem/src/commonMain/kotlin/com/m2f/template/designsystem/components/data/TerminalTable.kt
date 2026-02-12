package com.m2f.template.designsystem.components.data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.m2f.template.designsystem.theme.TerminalPreview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * A themed table component that renders a header row followed by content rows.
 *
 * @param headers The list of column header labels.
 * @param modifier Modifier for the outer container.
 * @param content Content lambda to add [TerminalTableRow] entries.
 */
@Composable
fun TerminalTable(
    headers: List<String>,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val spacing = TerminalTheme.spacing
    val borders = TerminalTheme.borders
    val radius = TerminalTheme.radius

    val shape = RoundedCornerShape(radius.md)

    Column(
        modifier = modifier
            .clip(shape)
            .border(borders.thin, colors.border, shape)
            .background(colors.surface),
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.sm, vertical = spacing.xs),
        ) {
            headers.forEach { header ->
                BasicText(
                    text = header,
                    modifier = Modifier.weight(1f),
                    style = typography.xs.copy(
                        color = colors.textMuted,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }

        // Header bottom border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(borders.thin)
                .background(colors.border),
        )

        // Content rows
        content()
    }
}

/**
 * A single row in a [TerminalTable].
 *
 * @param cells The list of cell text values (should match header count).
 * @param modifier Modifier for the row.
 * @param showBottomBorder Whether to show a bottom border. Set to false for the last row.
 */
@Composable
fun TerminalTableRow(
    cells: List<String>,
    modifier: Modifier = Modifier,
    showBottomBorder: Boolean = true,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val spacing = TerminalTheme.spacing
    val borders = TerminalTheme.borders

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.sm, vertical = spacing.xs),
        ) {
            cells.forEach { cell ->
                BasicText(
                    text = cell,
                    modifier = Modifier.weight(1f),
                    style = typography.sm.copy(color = colors.text),
                )
            }
        }

        if (showBottomBorder) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(borders.thin)
                    .background(colors.border),
            )
        }
    }
}

@TerminalPreview
@Composable
private fun TerminalTablePreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
        ) {
            TerminalTable(headers = listOf("Name", "Role", "Status")) {
                TerminalTableRow(cells = listOf("Alice", "Admin", "Active"))
                TerminalTableRow(cells = listOf("Bob", "User", "Pending"))
                TerminalTableRow(cells = listOf("Charlie", "User", "Inactive"), showBottomBorder = false)
            }
        }
    }
}
