package com.m2f.template.designsystem.accessibility

/**
 * Android `actual` for [prefersReducedMotion]. Conservative `false`: animations stay on. A real
 * implementation would read `Settings.Global.TRANSITION_ANIMATION_SCALE` /
 * `ANIMATOR_DURATION_SCALE == 0` via a `Context`, which the design system layer does not carry.
 */
internal actual fun prefersReducedMotion(): Boolean = false
