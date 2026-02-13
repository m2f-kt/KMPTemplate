package com.m2f.template.designsystem.components.data

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m2f.template.designsystem.theme.TerminalPreview
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.designsystem.theme.rememberTerminalRipple

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

// -- Selectable table components --

/**
 * A 16dp inline checkbox for use within table cells.
 *
 * Renders a tri-state checkbox at 16dp size with proportionally smaller icon canvas.
 * Uses the same Pencil color tokens as [com.m2f.template.designsystem.components.selection.TerminalCheckbox]
 * but scaled down for inline table use.
 *
 * @param state The tri-state value.
 * @param onClick Callback invoked when clicked.
 * @param modifier Modifier for the outer 32dp touch-target box.
 */
@Composable
private fun TerminalTableCheckbox(
    state: ToggleableState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val borders = TerminalTheme.borders
    val radius = TerminalTheme.radius

    val boxSize = 16.dp
    val shape = RoundedCornerShape(radius.sm)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.size(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(boxSize)
                .clip(shape)
                .triStateToggleable(
                    state = state,
                    interactionSource = interactionSource,
                    indication = rememberTerminalRipple(bounded = true),
                    role = Role.Checkbox,
                    onClick = onClick,
                )
                .then(
                    when (state) {
                        ToggleableState.Off -> Modifier
                            .background(colors.checkboxBg)
                            .border(borders.default, colors.border, shape)
                        ToggleableState.On,
                        ToggleableState.Indeterminate -> Modifier
                            .background(colors.btnPrimaryBg)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                ToggleableState.On -> {
                    val checkColor = colors.btnPrimaryText
                    Canvas(modifier = Modifier.size(10.dp)) {
                        val strokeWidth = 1.5.dp.toPx()
                        drawLine(
                            color = checkColor,
                            start = Offset(size.width * 0.2f, size.height * 0.5f),
                            end = Offset(size.width * 0.4f, size.height * 0.75f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = checkColor,
                            start = Offset(size.width * 0.4f, size.height * 0.75f),
                            end = Offset(size.width * 0.8f, size.height * 0.25f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round,
                        )
                    }
                }
                ToggleableState.Indeterminate -> {
                    val dashColor = colors.btnPrimaryText
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawLine(
                            color = dashColor,
                            start = Offset(size.width * 0.2f, size.height * 0.5f),
                            end = Offset(size.width * 0.8f, size.height * 0.5f),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
                ToggleableState.Off -> { /* empty */ }
            }
        }
    }
}

/**
 * A themed selectable table with a checkbox column for row selection.
 *
 * Renders a header row with a leading checkbox (reflecting aggregate selection state)
 * followed by content rows. The header checkbox toggles between select-all and
 * deselect-all. Selected rows use [TerminalTheme.colors.tableRowSelectedBg] background.
 *
 * @param headers The list of column header labels.
 * @param selectedRows The set of currently selected row indices.
 * @param onSelectionChange Callback with the new selection set.
 * @param rowCount Total number of data rows.
 * @param modifier Modifier for the outer container.
 * @param content Content lambda receiving an `isSelected` predicate for each row index.
 */
@Composable
fun TerminalSelectableTable(
    headers: List<String>,
    selectedRows: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
    rowCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(isSelected: (Int) -> Boolean) -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val borders = TerminalTheme.borders
    val radius = TerminalTheme.radius

    val shape = RoundedCornerShape(radius.sm)

    val headerState = when {
        selectedRows.size == rowCount && rowCount > 0 -> ToggleableState.On
        selectedRows.isEmpty() -> ToggleableState.Off
        else -> ToggleableState.Indeterminate
    }

    Column(
        modifier = modifier
            .clip(shape)
            .border(borders.thin, colors.border, shape)
            .background(colors.surface),
    ) {
        // Header row with checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.tableHeaderBg)
                .padding(end = 16.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TerminalTableCheckbox(
                state = headerState,
                onClick = {
                    if (selectedRows.size == rowCount) {
                        onSelectionChange(emptySet())
                    } else {
                        onSelectionChange((0 until rowCount).toSet())
                    }
                },
            )
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
        content { index -> index in selectedRows }
    }
}

/**
 * A single row in a [TerminalSelectableTable] with a leading checkbox.
 *
 * Renders a per-row checkbox followed by composable content. When [selected] is true,
 * the row background uses [TerminalTheme.colors.tableRowSelectedBg].
 *
 * @param index The row index (used for identification).
 * @param selected Whether this row is currently selected.
 * @param onSelectedChange Callback invoked when the row checkbox is toggled.
 * @param modifier Modifier for the row.
 * @param showBottomBorder Whether to show a bottom border. Set to false for the last row.
 * @param content Composable content scoped to [RowScope].
 */
@Composable
fun TerminalSelectableTableRow(
    index: Int,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showBottomBorder: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = TerminalTheme.colors
    val borders = TerminalTheme.borders

    val rowBg = if (selected) colors.tableRowSelectedBg else Color.Transparent

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBg)
                .padding(end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TerminalTableCheckbox(
                state = if (selected) ToggleableState.On else ToggleableState.Off,
                onClick = { onSelectedChange(!selected) },
            )
            content()
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

            // Selectable table with checkbox column
            val (selection, setSelection) = remember { mutableStateOf(setOf(0, 2)) }
            val rows = listOf(
                listOf("node", "1234", "12.5%"),
                listOf("gradle", "5678", "45.2%"),
                listOf("chrome", "9012", "8.1%"),
                listOf("docker", "3456", "3.2%"),
            )

            TerminalSelectableTable(
                headers = listOf("Process", "PID", "CPU"),
                selectedRows = selection,
                onSelectionChange = setSelection,
                rowCount = rows.size,
            ) { isSelected ->
                rows.forEachIndexed { index, cells ->
                    TerminalSelectableTableRow(
                        index = index,
                        selected = isSelected(index),
                        onSelectedChange = { checked ->
                            val newSelection = selection.toMutableSet()
                            if (checked) newSelection.add(index) else newSelection.remove(index)
                            setSelection(newSelection)
                        },
                        showBottomBorder = index < rows.lastIndex,
                    ) {
                        TerminalTableCell(text = cells[0])
                        TerminalTableCell(text = cells[1], secondary = true)
                        TerminalTableCell(text = cells[2])
                    }
                }
            }
        }
    }
}
