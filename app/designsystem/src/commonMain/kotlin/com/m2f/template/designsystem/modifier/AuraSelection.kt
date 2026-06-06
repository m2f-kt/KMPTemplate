package com.m2f.template.designsystem.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * The single **"active = accent"** selection recipe used everywhere Aura signals
 * "this is chosen / active": a **flat translucent accent fill + an accent hairline border
 * (+ optional accent glow)**. It is deliberately **NOT a gradient** — the animated aura conic
 * stroke is reserved for hero/CTA surfaces. Implement once, reuse: dropdown active item, chips,
 * override buttons, focused inputs.
 *
 * Apply to a surface you also `.clip(shape)`; the fill + border trace the same [shape].
 *
 * @param active when false the modifier is a no-op (returns the receiver unchanged).
 * @param accent the selection tint (defaults to the theme accent / neon cyan).
 */
@Composable
fun Modifier.selectedAccent(
    active: Boolean,
    shape: Shape,
    accent: Color = AuraTheme.colors.neonCyan,
    fillAlpha: Float = 0.08f,
    borderAlpha: Float = 0.22f,
    borderWidth: Dp = 1.dp,
    glow: Boolean = false,
): Modifier {
    if (!active) return this
    return this
        .then(if (glow) Modifier.auraGlow(AuraTheme.glows.cyan, shape) else Modifier)
        .background(accent.copy(alpha = fillAlpha), shape)
        .border(borderWidth, accent.copy(alpha = borderAlpha), shape)
}

/**
 * The brand conic stroke (cyan → violet → magenta) drawn **statically** — no rotation. The
 * **animated** [auraBorder] is reserved for hero/CTA surfaces; reach for this static variant
 * anywhere the gradient stroke should sit still. A thin wrapper so call sites read intent, not a flag.
 */
@Composable
fun Modifier.auraStaticBorder(
    cornerRadius: Dp = AuraTheme.radius.md,
    width: Dp = 1.dp,
): Modifier = this.auraBorder(cornerRadius = cornerRadius, width = width, animated = false)
