package com.m2f.template.designsystem.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Reads the host OS "reduce motion" accessibility preference (WCAG 2.1 — honour
 * `prefers-reduced-motion`). When `true`, callers should drop or shorten motion-heavy animations.
 *
 * Actuals:
 *  - jvm (desktop): on macOS reads `NSWorkspace.accessibilityDisplayShouldReduceMotion` via a
 *    self-contained AppKit JNA call; off macOS / on any failure returns `false`.
 *  - ios: reads `UIAccessibility.isReduceMotionEnabled`.
 *  - android: conservative `false` (no `Context` plumbing in the design system layer).
 *  - wasmJs: `false`.
 *
 * Read once and cached per composition via [rememberPrefersReducedMotion] — the setting changes
 * rarely, so we don't observe live changes.
 */
internal expect fun prefersReducedMotion(): Boolean

/** Composable accessor for [prefersReducedMotion], cached for the lifetime of the composition. */
@Composable
fun rememberPrefersReducedMotion(): Boolean = remember { prefersReducedMotion() }
