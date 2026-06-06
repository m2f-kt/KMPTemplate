package com.m2f.template.designsystem.components.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * A modal sheet over the content area (handoff §A6 `.at-modal` / §B3 `.at-modal--wide`): a dimming
 * **scrim** (`rgba(7,8,12,0.62)`) that dismisses on backdrop tap, and a centered sheet that **rises
 * in** (opacity + `translateY(12px) scale(0.98)` → none, `240ms ease-out` — handoff `at-rise`). The
 * sheet is `bg-elev-1` (`surface`), a `borderStrong` hairline, radius `xl`.
 *
 * Render it as the LAST child of the screen's root `Box` so it overlays the page. Caller owns
 * [visible]; tapping the sheet itself is swallowed (only backdrop taps dismiss).
 *
 * Note: a true CSS `backdrop-filter: blur` of the content behind isn't portable across Compose
 * targets, so the heavy `0.62`-alpha scrim stands in for it — the dim, not a live blur.
 *
 * @param maxWidth sheet max width (`480dp` picker / `720dp` editor).
 */
@Composable
fun AuraModalSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    maxWidth: Dp = 480.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AuraTheme.colors
    val borders = AuraTheme.borders
    val radius = AuraTheme.radius
    val motion = AuraTheme.motion
    val rise = with(LocalDensity.current) { 12.dp.roundToPx() }

    // Scrim: rgba(7,8,12,0.62) over the neon `--bg`.
    val scrim = Color(0x9E07080C)
    val sheetShape = RoundedCornerShape(radius.xl)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(motion.durBaseMs, easing = motion.easeOut)),
        exit = fadeOut(tween(motion.durQuickMs)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrim)
                // Backdrop tap dismisses (the sheet below swallows its own taps).
                .pointerInput(Unit) { detectTapGestures { onDismiss() } }
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(240, easing = motion.easeOut)) +
                    slideInVertically(tween(240, easing = motion.easeOut)) { rise } +
                    scaleIn(tween(240, easing = motion.easeOut), initialScale = 0.98f),
                exit = fadeOut(tween(motion.durQuickMs)) +
                    slideOutVertically { rise } +
                    scaleOut(targetScale = 0.98f),
            ) {
                Column(
                    modifier = modifier
                        .widthIn(max = maxWidth)
                        .fillMaxWidth()
                        .clip(sheetShape)
                        .background(colors.surface)
                        .border(borders.thin, colors.borderStrong, sheetShape)
                        // Swallow taps so clicking the sheet doesn't reach the backdrop dismiss.
                        .pointerInput(Unit) { detectTapGestures { } },
                    content = content,
                )
            }
        }
    }
}
