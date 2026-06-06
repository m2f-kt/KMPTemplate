package com.m2f.template.designsystem.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * A custom ripple indication for the Aura design system.
 *
 * Implements [IndicationNodeFactory] (the modern Indication API) to draw a circular
 * ripple expanding from the press point. The ripple expands over 300ms and fades out
 * over 200ms on release. Uses Foundation primitives only -- no Material dependency.
 *
 * @param bounded Whether the ripple is clipped to component bounds. Use `true` for
 *   buttons, cards, list items; `false` for small touch targets like checkbox, switch, radio.
 * @param color The ripple color. If [Color.Unspecified], resolved at draw time from theme defaults.
 */
class AuraRippleIndication(
    private val bounded: Boolean = true,
    private val color: Color = Color.Unspecified,
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode =
        AuraRippleNode(interactionSource, bounded, color)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AuraRippleIndication) return false
        return bounded == other.bounded && color == other.color
    }

    override fun hashCode(): Int = 31 * bounded.hashCode() + color.hashCode()
}

private class AuraRippleNode(
    private val interactionSource: InteractionSource,
    private val bounded: Boolean,
    private val rippleColor: Color,
) : Modifier.Node(), DrawModifierNode {

    private val radius = Animatable(0f)
    private val alpha = Animatable(0f)
    private var pressPosition: Offset = Offset.Zero
    private var targetRadius: Float = 0f
    private var collectJob: Job? = null

    override fun onAttach() {
        collectJob = coroutineScope.launch {
            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        pressPosition = interaction.pressPosition
                        // Reset for new press
                        radius.snapTo(0f)
                        alpha.snapTo(0.12f)
                        // Animate radius expansion
                        launch {
                            radius.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 300),
                            )
                        }
                    }
                    is PressInteraction.Release -> {
                        // Fade out
                        alpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 200),
                        )
                    }
                    is PressInteraction.Cancel -> {
                        // Fade out
                        alpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 200),
                        )
                    }
                }
            }
        }
    }

    override fun onDetach() {
        collectJob?.cancel()
        collectJob = null
    }

    override fun ContentDrawScope.draw() {
        drawContent()

        val currentAlpha = alpha.value
        if (currentAlpha <= 0f) return

        // Compute target radius to cover the entire component from the press point
        targetRadius = sqrt(size.width * size.width + size.height * size.height)

        val currentRadius = radius.value * targetRadius
        val color = if (rippleColor == Color.Unspecified) {
            // Fallback: use a neutral gray with the animated alpha
            Color(0xFF808080).copy(alpha = currentAlpha)
        } else {
            rippleColor.copy(alpha = currentAlpha)
        }

        if (bounded) {
            clipRect(0f, 0f, size.width, size.height) {
                drawCircle(
                    color = color,
                    radius = currentRadius,
                    center = pressPosition,
                )
            }
        } else {
            drawCircle(
                color = color,
                radius = currentRadius,
                center = pressPosition,
            )
        }
    }
}

/**
 * Creates and remembers a [AuraRippleIndication] configured for the current theme.
 *
 * @param bounded Whether the ripple clips to component bounds. Default `true`.
 * @param color Ripple color. If [Color.Unspecified], uses [AuraTheme.colors.text] at 0.12f alpha.
 * @return A themed [AuraRippleIndication] instance.
 */
@Composable
fun rememberAuraRipple(
    bounded: Boolean = true,
    color: Color = Color.Unspecified,
): AuraRippleIndication {
    val resolvedColor = if (color == Color.Unspecified) {
        AuraTheme.colors.text.copy(alpha = 0.12f)
    } else {
        color
    }
    return remember(bounded, resolvedColor) { AuraRippleIndication(bounded, resolvedColor) }
}
