package com.m2f.template.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A neon glow specification — a soft, colored, omnidirectional outer shadow used for
 * emphasis. Glow replaces (never accompanies) a hard structural shadow on neon-emphasised
 * surfaces. Draw via [com.m2f.template.designsystem.modifier.auraGlow].
 */
@Immutable
data class AuraGlow(
    val color: Color,
    val blur: Dp,
    val spread: Dp,
)

@Immutable
data class AuraGlows(
    val cyan: AuraGlow,
    val violet: AuraGlow,
    val magenta: AuraGlow,
)

val LocalAuraGlows = staticCompositionLocalOf {
    AuraGlowValues
}

val AuraGlowValues = AuraGlows(
    cyan = AuraGlow(color = Color(0x7300E5FF), blur = 22.dp, spread = 2.dp),
    violet = AuraGlow(color = Color(0x808B5CF6), blur = 22.dp, spread = 2.dp),
    magenta = AuraGlow(color = Color(0x73FF3DC9), blur = 22.dp, spread = 2.dp),
)
