package com.m2f.template.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Aura motion tokens. One sharp ease-out for entries/exits, one symmetric ease-in-out for
 * state crossings. Durations in milliseconds: quick (hover/micro), base (modals/swaps),
 * slow (page transitions). Honor `prefers-reduced-motion` at the call site.
 */
@Immutable
data class AuraMotion(
    val durQuickMs: Int,
    val durBaseMs: Int,
    val durSlowMs: Int,
    val easeOut: Easing,
    val easeInOut: Easing,
)

val LocalAuraMotion = staticCompositionLocalOf {
    AuraMotionValues
}

val AuraMotionValues = AuraMotion(
    durQuickMs = 140,
    durBaseMs = 280,
    durSlowMs = 520,
    easeOut = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
    easeInOut = CubicBezierEasing(0.65f, 0f, 0.35f, 1f),
)
