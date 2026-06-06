package com.m2f.template.designsystem.modifier

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraGlow
import com.m2f.template.designsystem.theme.AuraTheme
import kotlin.math.hypot

private const val AURA_SPIN_PERIOD_MS = 6000
private const val AURA_FULL_TURN_DEG = 360f

/**
 * The brand signature — a steady, constant-width neon ring whose conic hue **rotates** around the
 * perimeter. The stops are a **palindrome**
 * (cyan → violet → magenta → violet → cyan): the cyan→cyan wrap is seamless AND the distribution is
 * symmetric, so the ring reads as a full-perimeter band whose colours turn — NOT an asymmetric bright
 * arc that travels like an indeterminate progress bar. There is no breathe pulse: a single linear
 * rotation, full alpha, constant width. Use sparingly: primary CTAs, focused surfaces, hero cards.
 *
 * @param cornerRadius Corner radius of the bordered element (match the element's own shape).
 * @param width Stroke width of the ring.
 * @param animated When false the ring is static (honor `prefers-reduced-motion` by passing false).
 *   TODO(reduced-motion): no shared `AuraTheme.motion` reduced-motion flag exists yet — callers
 *   must pass `animated = false` explicitly; wire this to a system flag when one lands.
 * @param spinPeriodMs Duration of one full hue rotation when [animated]; lower = faster.
 * @param colors The conic ring stops (palindrome by default for a symmetric, travel-free ring).
 */
@Composable
fun Modifier.auraBorder(
    cornerRadius: Dp = AuraTheme.radius.md,
    width: Dp = 1.5.dp,
    animated: Boolean = true,
    spinPeriodMs: Int = AURA_SPIN_PERIOD_MS,
    colors: List<Color> = listOf(
        AuraTheme.colors.neonCyan,
        AuraTheme.colors.neonViolet,
        AuraTheme.colors.neonMagenta,
        AuraTheme.colors.neonViolet,
        AuraTheme.colors.neonCyan,
    ),
): Modifier {
    val angle: State<Float> = if (animated) {
        val transition = rememberInfiniteTransition(label = "auraBorder")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = AURA_FULL_TURN_DEG,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = spinPeriodMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "auraAngle",
        )
    } else {
        rememberStaticState(0f)
    }
    return this.drawWithCache {
        val center = Offset(size.width / 2f, size.height / 2f)
        val ring = ringPath(size = size, cornerRadiusPx = cornerRadius.toPx(), strokePx = width.toPx())
        val sweep = Brush.sweepGradient(colors = colors, center = center)
        onDrawWithContent {
            drawContent()
            // Steady constant-width ring whose hue rotates — full alpha, no breathe, no traveling arc.
            drawSweepRing(ringPath = ring, brush = sweep, angleDegrees = angle.value, center = center)
        }
    }
}

/**
 * Builds the even-odd ring [Path] for a rounded-rect stroke [strokePx] wide, inset from the outer
 * [size] (outer rounded-rect minus inner rounded-rect). Shared by [auraBorder] and any other
 * multi-tone sweep border so both frame their gradient identically.
 */
internal fun ringPath(size: Size, cornerRadiusPx: Float, strokePx: Float): Path {
    val innerRadiusPx = (cornerRadiusPx - strokePx).coerceAtLeast(0f)
    return Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(Offset.Zero, size),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            ),
        )
        addRoundRect(
            RoundRect(
                rect = Rect(
                    Offset(strokePx, strokePx),
                    Size(size.width - strokePx * 2, size.height - strokePx * 2),
                ),
                cornerRadius = CornerRadius(innerRadiusPx, innerRadiusPx),
            ),
        )
        fillType = PathFillType.EvenOdd
    }
}

/**
 * Clips to [ringPath] and paints [brush] rotated by [angleDegrees] about [center] — the rotating
 * conic-stroke effect. [alpha] lets callers modulate the stroke.
 *
 * The fill rect MUST cover the whole ring at EVERY rotation. A per-axis "3×" rect does NOT for a
 * wide-and-short element (e.g. a wide pill, ~470×46): at ~90° the 3×height side becomes far
 * narrower than the element width, so the gradient stops covering the side edges and the border
 * appears to vanish/re-appear (the long-standing "indeterminate progress" artifact). Instead we paint
 * a SQUARE centred on [center] whose side is the element diagonal (×1.5 margin): its inscribed circle
 * (radius ≥ the diagonal) always contains every ring point, so coverage is rotation-invariant.
 */
internal fun DrawScope.drawSweepRing(
    ringPath: Path,
    brush: Brush,
    angleDegrees: Float,
    center: Offset,
    alpha: Float = 1f,
) {
    val side = hypot(size.width, size.height) * SWEEP_COVER_MARGIN
    clipPath(ringPath) {
        rotate(degrees = angleDegrees, pivot = center) {
            drawRect(
                brush = brush,
                topLeft = Offset(center.x - side / 2f, center.y - side / 2f),
                size = Size(side, side),
                alpha = alpha,
            )
        }
    }
}

/** Margin over the diagonal for [drawSweepRing]'s rotation-invariant covering square. */
private const val SWEEP_COVER_MARGIN = 1.5f

/**
 * A colored neon glow — a soft, omnidirectional outer shadow tinted by [glow]. Replaces (never
 * accompanies) a hard structural shadow on neon-emphasised surfaces.
 */
fun Modifier.auraGlow(glow: AuraGlow, shape: Shape): Modifier =
    this.shadow(
        elevation = glow.blur,
        shape = shape,
        ambientColor = glow.color,
        spotColor = glow.color,
        clip = false,
    )

/**
 * A live/now indicator — a dot wrapped in an expanding, fading ring. Used for "Listening" and
 * "live" states.
 */
@Composable
fun PulseDot(
    modifier: Modifier = Modifier,
    color: Color = AuraTheme.colors.neonCyan,
    dotSize: Dp = 8.dp,
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseProgress",
    )
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(dotSize)
            .drawBehind {
                val r = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    color = color.copy(alpha = (1f - progress) * 0.6f),
                    radius = r + progress * r * 2f,
                    center = center,
                )
                drawCircle(color = color, radius = r, center = center)
            },
    )
}

/** The aura gradient ink (cyan → violet → magenta) for branded headlines. */
@Composable
fun auraBrush(): Brush = Brush.linearGradient(
    colors = listOf(
        AuraTheme.colors.neonCyan,
        AuraTheme.colors.neonViolet,
        AuraTheme.colors.neonMagenta,
    ),
)

private fun rememberStaticState(value: Float): State<Float> = StaticFloatState(value)

private class StaticFloatState(override val value: Float) : State<Float>
