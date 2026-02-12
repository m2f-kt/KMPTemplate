package com.m2f.template.designsystem.components.data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m2f.template.designsystem.theme.TerminalPreview
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * A themed table component that renders a header row followed by content rows.
 *
 * The table container has a 4dp corner radius, 1px border, and surface fill matching
 * the Pencil design spec. The header row uses [TerminalTheme.colors] table tokens
 * for background and text styling.
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
    val borders = TerminalTheme.borders
    val radius = TerminalTheme.radius

    val shape = RoundedCornerShape(radius.sm)

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
                .background(colors.tableHeaderBg)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            headers.forEach { header ->
                BasicText(
                    text = header,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(
                        fontFamily = typography.fontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        color = colors.tableHeaderText,
                    ),
                )
            }
        }

        // Content rows
        content()
    }
}

/**
 * A single row in a [TerminalTable] with composable content.
 *
 * This is the primary overload that accepts a composable lambda scoped to [RowScope],
 * allowing callers to use `Modifier.weight(1f)` on individual cells and mix
 * [TerminalTableCell] components with custom composables.
 *
 * Row padding is 12dp vertical and 16dp horizontal per Pencil spec. A bottom border
 * is shown by default; set [showBottomBorder] to false for the last row.
 *
 * @param modifier Modifier for the row.
 * @param showBottomBorder Whether to show a bottom border. Set to false for the last row.
 * @param content Composable content scoped to [RowScope].
 */
@Composable
fun TerminalTableRow(
    modifier: Modifier = Modifier,
    showBottomBorder: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = TerminalTheme.colors
    val borders = TerminalTheme.borders

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            content = content,
        )

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

/**
 * A single row in a [TerminalTable] using a list of string cell values.
 *
 * This is a convenience overload that renders each cell as equally-weighted text
 * using [TerminalTheme.colors.tableRowTextPrimary]. For mixed primary/secondary
 * text or custom composables, use the composable content overload instead.
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

    TerminalTableRow(
        modifier = modifier,
        showBottomBorder = showBottomBorder,
    ) {
        cells.forEach { cell ->
            BasicText(
                text = cell,
                modifier = Modifier.weight(1f),
                style = typography.sm.copy(color = colors.tableRowTextPrimary),
            )
        }
    }
}

/**
 * A helper composable for rendering text cells within a composable [TerminalTableRow].
 *
 * Uses [TerminalTheme.typography.sm] (12sp) and differentiates between primary and
 * secondary text colors per Pencil spec. Each cell takes equal weight in the row.
 *
 * @param text The cell text content.
 * @param modifier Modifier for the cell.
 * @param secondary If true, uses [TerminalTheme.colors.tableRowTextSecondary] instead
 *   of [TerminalTheme.colors.tableRowTextPrimary].
 */
@Composable
fun RowScope.TerminalTableCell(
    text: String,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    BasicText(
        text = text,
        modifier = modifier.weight(1f),
        style = typography.sm.copy(
            color = if (secondary) colors.tableRowTextSecondary else colors.tableRowTextPrimary,
        ),
    )
}

@TerminalPreview
@Composable
private fun TerminalTablePreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // List<String> convenience API
            TerminalTable(headers = listOf("Process", "PID", "CPU")) {
                TerminalTableRow(cells = listOf("node", "1234", "12.5%"))
                TerminalTableRow(cells = listOf("gradle", "5678", "45.2%"))
                TerminalTableRow(
                    cells = listOf("chrome", "9012", "8.1%"),
                    showBottomBorder = false,
                )
            }

            // Composable content API with primary/secondary differentiation
            TerminalTable(headers = listOf("Name", "Role", "Status")) {
                TerminalTableRow {
                    TerminalTableCell(text = "alice")
                    TerminalTableCell(text = "admin", secondary = true)
                    TerminalTableCell(text = "active")
                }
                TerminalTableRow(showBottomBorder = false) {
                    TerminalTableCell(text = "bob")
                    TerminalTableCell(text = "user", secondary = true)
                    TerminalTableCell(text = "pending")
                }
            }
        }
    }
}
