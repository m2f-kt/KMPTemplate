package com.m2f.template.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import template.app.designsystem.generated.resources.JetBrainsMono_Bold
import template.app.designsystem.generated.resources.JetBrainsMono_Regular
import template.app.designsystem.generated.resources.JetBrainsMono_SemiBold
import template.app.designsystem.generated.resources.Manrope_Bold
import template.app.designsystem.generated.resources.Manrope_ExtraBold
import template.app.designsystem.generated.resources.Manrope_Medium
import template.app.designsystem.generated.resources.Manrope_Regular
import template.app.designsystem.generated.resources.Manrope_SemiBold
import template.app.designsystem.generated.resources.Res
import template.app.designsystem.generated.resources.SpaceGrotesk_Bold
import template.app.designsystem.generated.resources.SpaceGrotesk_Medium
import template.app.designsystem.generated.resources.SpaceGrotesk_Regular
import template.app.designsystem.generated.resources.SpaceGrotesk_SemiBold
import org.jetbrains.compose.resources.Font

@Immutable
data class AuraTypography(
    // Families
    val displayFamily: FontFamily,
    val bodyFamily: FontFamily,
    val monoFamily: FontFamily,
    // Default family (= body) — kept for back-compat with existing call sites.
    val fontFamily: FontFamily,
    // Aura scale
    val display1: TextStyle,
    val display2: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val body: TextStyle,
    val bodySm: TextStyle,
    val caption: TextStyle,
    val eyebrow: TextStyle,
    val mono: TextStyle,
    // Legacy style names (re-pointed onto the Aura families) — kept so existing
    // screens/components compile and re-skin without edits.
    val chartAxis: TextStyle,
    val xxs: TextStyle,
    val xs: TextStyle,
    val sm: TextStyle,
    val base: TextStyle,
    val md: TextStyle,
    val xxl: TextStyle,
)

val LocalAuraTypography = staticCompositionLocalOf {
    AuraTypography(
        displayFamily = FontFamily.SansSerif,
        bodyFamily = FontFamily.SansSerif,
        monoFamily = FontFamily.Monospace,
        fontFamily = FontFamily.SansSerif,
        display1 = TextStyle.Default,
        display2 = TextStyle.Default,
        h1 = TextStyle.Default,
        h2 = TextStyle.Default,
        h3 = TextStyle.Default,
        body = TextStyle.Default,
        bodySm = TextStyle.Default,
        caption = TextStyle.Default,
        eyebrow = TextStyle.Default,
        mono = TextStyle.Default,
        chartAxis = TextStyle.Default,
        xxs = TextStyle.Default,
        xs = TextStyle.Default,
        sm = TextStyle.Default,
        base = TextStyle.Default,
        md = TextStyle.Default,
        xxl = TextStyle.Default,
    )
}

@Composable
fun auraTypography(): AuraTypography {
    val display = FontFamily(
        Font(Res.font.SpaceGrotesk_Regular, FontWeight.Normal),
        Font(Res.font.SpaceGrotesk_Medium, FontWeight.Medium),
        Font(Res.font.SpaceGrotesk_SemiBold, FontWeight.SemiBold),
        Font(Res.font.SpaceGrotesk_Bold, FontWeight.Bold),
    )
    val body = FontFamily(
        Font(Res.font.Manrope_Regular, FontWeight.Normal),
        Font(Res.font.Manrope_Medium, FontWeight.Medium),
        Font(Res.font.Manrope_SemiBold, FontWeight.SemiBold),
        Font(Res.font.Manrope_Bold, FontWeight.Bold),
        Font(Res.font.Manrope_ExtraBold, FontWeight.ExtraBold),
    )
    val mono = FontFamily(
        Font(Res.font.JetBrainsMono_Regular, FontWeight.Normal),
        Font(Res.font.JetBrainsMono_SemiBold, FontWeight.SemiBold),
        Font(Res.font.JetBrainsMono_Bold, FontWeight.Bold),
    )
    return AuraTypography(
        displayFamily = display,
        bodyFamily = body,
        monoFamily = mono,
        fontFamily = body,
        display1 = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 64.sp,
            lineHeight = 65.sp,
            letterSpacing = (-0.03).em,
        ),
        display2 = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 44.sp,
            lineHeight = 46.sp,
            letterSpacing = (-0.025).em,
        ),
        h1 = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 34.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.02).em,
        ),
        h2 = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
            lineHeight = 31.sp,
            letterSpacing = (-0.015).em,
        ),
        h3 = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 25.sp,
            letterSpacing = (-0.01).em,
        ),
        body = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 25.sp,
        ),
        bodySm = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 21.sp,
        ),
        caption = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            letterSpacing = 0.01.em,
        ),
        eyebrow = TextStyle(
            fontFamily = mono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            letterSpacing = 0.18.em,
        ),
        mono = TextStyle(
            fontFamily = mono,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 20.sp,
        ),
        // Legacy names → Aura families (sizes preserved to avoid layout shift).
        chartAxis = TextStyle(fontFamily = mono, fontSize = 9.sp, fontWeight = FontWeight.Normal),
        xxs = TextStyle(fontFamily = body, fontSize = 10.sp, fontWeight = FontWeight.Normal),
        xs = TextStyle(fontFamily = body, fontSize = 11.sp, fontWeight = FontWeight.Normal),
        sm = TextStyle(fontFamily = body, fontSize = 12.sp, fontWeight = FontWeight.Normal),
        base = TextStyle(fontFamily = body, fontSize = 13.sp, fontWeight = FontWeight.Normal),
        md = TextStyle(fontFamily = body, fontSize = 14.sp, fontWeight = FontWeight.Medium),
        xxl = TextStyle(fontFamily = display, fontSize = 32.sp, fontWeight = FontWeight.Bold),
    )
}
