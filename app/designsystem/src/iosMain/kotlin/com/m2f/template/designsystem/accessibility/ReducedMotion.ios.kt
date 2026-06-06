package com.m2f.template.designsystem.accessibility

import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

/** iOS `actual` for [prefersReducedMotion] — reads the UIKit accessibility flag directly. */
internal actual fun prefersReducedMotion(): Boolean = UIAccessibilityIsReduceMotionEnabled()
