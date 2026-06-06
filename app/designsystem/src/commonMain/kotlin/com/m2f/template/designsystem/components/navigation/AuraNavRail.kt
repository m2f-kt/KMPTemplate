package com.m2f.template.designsystem.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.theme.AuraPreview
import com.m2f.template.designsystem.theme.AuraTheme

/** WCAG 2.1 AA minimum interactive hit-target height (>=44pt). */
private val NAV_RAIL_MIN_HIT_TARGET = 44.dp
private val NAV_RAIL_WIDTH = 248.dp
private val NAV_ITEM_ICON_SIZE = 18.dp
private val ACTIVE_MARKER_WIDTH = 2.dp

/** Fraction of the rail height over which the top edge feathers up from the app background. */
private const val RAIL_FADE_TOP = 0.16f

/** Fraction at which the bottom edge begins feathering back down into the app background. */
private const val RAIL_FADE_BOTTOM = 0.84f

/**
 * A single navigation entry in an [AuraNavRail] section.
 *
 * @param key stable identifier matched against [AuraNavRail]'s `selectedKey`.
 * @param label the visible nav label.
 * @param icon optional leading glyph slot; receives the resolved tint so the icon tracks the
 *   item's selected/hover/idle state.
 */
@Immutable
data class AuraNavItem(
    val key: String,
    val label: String,
    val icon: (@Composable (Color) -> Unit)? = null,
)

/**
 * A grouped block of nav items with an UPPER-mono eyebrow heading (e.g. `WORKSPACE`).
 *
 * @param eyebrow the section heading; rendered with [AuraTheme]'s `eyebrow` mono style.
 * @param items the entries in this section, in display order.
 */
@Immutable
data class AuraNavSection(
    val eyebrow: String,
    val items: List<AuraNavItem>,
)

/**
 * The persistent left navigation rail — a generic, token-only Aura surface. An optional caller-
 * supplied brand row sits at the top, followed by eyebrow-labelled [sections] of nav items, with
 * an optional [footer] pinned to the bottom.
 *
 * Visual treatment: selected = `accentMuted` fill + `accent` text with an accent inner edge marker;
 * hover = `inset` fill + bright text; idle = muted text on the surface. All colours/spacing/
 * typography resolve to [AuraTheme] — no literals.
 *
 * @param sections grouped nav items rendered top-to-bottom.
 * @param selectedKey the [AuraNavItem.key] of the active entry.
 * @param onSelect invoked with an item's key when it is clicked.
 * @param modifier modifier for the rail root.
 * @param brand optional brand slot at the top (caller-supplied; defaults to none).
 * @param footer optional slot pinned to the bottom (e.g. an account row).
 */
@Composable
fun AuraNavRail(
    sections: List<AuraNavSection>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    brand: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    val colors = AuraTheme.colors
    val spacing = AuraTheme.spacing
    val gap = AuraTheme.gap

    Column(
        modifier = modifier
            .width(NAV_RAIL_WIDTH)
            .fillMaxHeight()
            // Symmetric vertical feather so the rail dissolves into the app background at BOTH the
            // top and bottom edges: `bg` at each end, holding elevated `surface` through the middle.
            .background(
                Brush.verticalGradient(
                    0f to colors.bg,
                    RAIL_FADE_TOP to colors.surface,
                    RAIL_FADE_BOTTOM to colors.surface,
                    1f to colors.bg,
                ),
            )
            .drawBehind {
                // The right-edge divider feathers on the same axis so the seam softens with the fill.
                drawLine(
                    brush = Brush.verticalGradient(
                        0f to Color.Transparent,
                        RAIL_FADE_TOP to colors.border,
                        RAIL_FADE_BOTTOM to colors.border,
                        1f to Color.Transparent,
                    ),
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = spacing.lg, vertical = spacing.xl),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(gap.xl),
        ) {
            if (brand != null) {
                Box(modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xs)) {
                    brand()
                }
            }
            sections.forEach { section ->
                NavRailSection(
                    section = section,
                    selectedKey = selectedKey,
                    onSelect = onSelect,
                )
            }
        }
        if (footer != null) {
            Spacer(modifier = Modifier.height(spacing.lg))
            footer()
        }
    }
}

@Composable
private fun NavRailSection(
    section: AuraNavSection,
    selectedKey: String,
    onSelect: (String) -> Unit,
) {
    val colors = AuraTheme.colors
    val gap = AuraTheme.gap
    Column(verticalArrangement = Arrangement.spacedBy(gap.xs)) {
        AuraText(
            text = section.eyebrow.uppercase(),
            style = AuraTheme.typography.eyebrow,
            color = colors.textFaint,
            modifier = Modifier.padding(start = AuraTheme.spacing.md, bottom = AuraTheme.spacing.xs),
        )
        section.items.forEach { item ->
            NavRailItem(
                item = item,
                isSelected = item.key == selectedKey,
                onClick = { onSelect(item.key) },
            )
        }
    }
}

@Composable
private fun NavRailItem(
    item: AuraNavItem,
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
        else -> Color.Transparent
    }
    val contentColor = when {
        isSelected -> colors.accent
        isHovered -> colors.text
        else -> colors.textMuted
    }
    val shape = RoundedCornerShape(radius.md)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = NAV_RAIL_MIN_HIT_TARGET)
            .clip(shape)
            .background(backgroundColor)
            // Selected: an accent inner-edge marker.
            .drawBehind {
                if (isSelected) {
                    val inset = 8.dp.toPx()
                    drawLine(
                        color = colors.accent,
                        start = Offset(0f, inset),
                        end = Offset(0f, size.height - inset),
                        strokeWidth = ACTIVE_MARKER_WIDTH.toPx(),
                    )
                }
            }
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .semantics {
                selected = isSelected
                role = Role.Tab
                contentDescription = item.label
            }
            .padding(horizontal = AuraTheme.spacing.md, vertical = AuraTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AuraTheme.gap.md),
    ) {
        item.icon?.let { icon ->
            Box(modifier = Modifier.size(NAV_ITEM_ICON_SIZE), contentAlignment = Alignment.Center) {
                icon(contentColor)
            }
        }
        AuraText(
            text = item.label,
            style = typography.bodySm,
            color = contentColor,
        )
    }
}

@Suppress("UnusedPrivateMember")
@AuraPreview
@Composable
private fun AuraNavRailPreview() {
    AuraTheme {
        AuraNavRail(
            sections = listOf(
                AuraNavSection(
                    eyebrow = "Workspace",
                    items = listOf(
                        AuraNavItem(key = "home", label = "Home"),
                        AuraNavItem(key = "library", label = "Library"),
                    ),
                ),
                AuraNavSection(
                    eyebrow = "You",
                    items = listOf(
                        AuraNavItem(key = "settings", label = "Settings"),
                        AuraNavItem(key = "privacy", label = "Privacy"),
                    ),
                ),
            ),
            selectedKey = "home",
            onSelect = {},
            modifier = Modifier.fillMaxHeight().background(AuraTheme.colors.bg),
        )
    }
}
