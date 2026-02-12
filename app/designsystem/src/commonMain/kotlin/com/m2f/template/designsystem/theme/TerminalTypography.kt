package com.m2f.template.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import template.app.designsystem.generated.resources.JetBrainsMono_Bold
import template.app.designsystem.generated.resources.JetBrainsMono_Regular
import template.app.designsystem.generated.resources.JetBrainsMono_SemiBold
import template.app.designsystem.generated.resources.Res

@Immutable
data class TerminalTypography(
    val fontFamily: FontFamily,
    val xs: TextStyle,
    val sm: TextStyle,
    val base: TextStyle,
    val md: TextStyle,
    val xxl: TextStyle,
)

val LocalTerminalTypography = staticCompositionLocalOf {
    TerminalTypography(
        fontFamily = FontFamily.Monospace,
        xs = TextStyle.Default,
        sm = TextStyle.Default,
        base = TextStyle.Default,
        md = TextStyle.Default,
        xxl = TextStyle.Default,
    )
}

@Composable
fun terminalTypography(): TerminalTypography {
    val fontFamily = FontFamily(
        Font(Res.font.JetBrainsMono_Regular, FontWeight.Normal),
        Font(Res.font.JetBrainsMono_SemiBold, FontWeight.SemiBold),
        Font(Res.font.JetBrainsMono_Bold, FontWeight.Bold),
    )
    return TerminalTypography(
        fontFamily = fontFamily,
        xs = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Normal),
        sm = TextStyle(fontFamily = fontFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal),
        base = TextStyle(fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal),
        md = TextStyle(fontFamily = fontFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal),
        xxl = TextStyle(fontFamily = fontFamily, fontSize = 32.sp, fontWeight = FontWeight.Bold),
    )
}
