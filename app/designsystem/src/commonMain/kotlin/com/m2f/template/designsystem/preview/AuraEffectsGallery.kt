package com.m2f.template.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.AuraGradientText
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.components.button.AuraButton
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.display.AuraKbdCombo
import com.m2f.template.designsystem.components.feedback.AuraModeBadge
import com.m2f.template.designsystem.components.feedback.ModeBadgeVariant
import com.m2f.template.designsystem.components.input.AuraRangeSlider
import com.m2f.template.designsystem.components.selection.AuraSegmentedControl
import com.m2f.template.designsystem.modifier.PulseDot
import com.m2f.template.designsystem.modifier.auraBorder
import com.m2f.template.designsystem.modifier.auraBrush
import com.m2f.template.designsystem.modifier.auraGlow
import com.m2f.template.designsystem.modifier.auraStaticBorder
import com.m2f.template.designsystem.modifier.selectedAccent
import com.m2f.template.designsystem.theme.AuraPreview
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * A live showcase of every Aura brand-signature effect/modifier and the net-new components that
 * the feature screens don't (yet) opt into — `auraBorder`, `auraGlow`, `selectedAccent`,
 * `auraStaticBorder`, `PulseDot`, `auraBrush`, `ButtonVariant.Aura`, `AuraSegmentedControl`,
 * `AuraModeBadge`, `AuraKbdCombo`, `AuraRangeSlider`, `AuraGradientText`.
 *
 * Render it from an IDE preview ([AuraEffectsGalleryPreview]) or live in a window via the
 * `com.m2f.template.EffectsGalleryMainKt` desktop entry point. Assumes it is already wrapped in
 * [AuraTheme] by the caller.
 */
@Composable
fun AuraEffectsGallery() {
    val colors = AuraTheme.colors
    Column(
        modifier = Modifier
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        AuraGradientText("Aura effects", style = AuraTheme.typography.h1)
        AuraText(
            "Brand signatures + net-new components not yet wired into screens.",
            style = AuraTheme.typography.bodySm,
            color = colors.textMuted,
        )

        Section("auraBorder — animated conic ring (cyan→violet→magenta)") {
            Box(
                modifier = Modifier
                    .size(width = 220.dp, height = 64.dp)
                    .clip(RoundedCornerShape(AuraTheme.radius.lg))
                    .auraBorder(cornerRadius = AuraTheme.radius.lg)
                    .background(colors.surface),
                contentAlignment = Alignment.Center,
            ) { AuraText("Hero / focused CTA", style = AuraTheme.typography.body) }
        }

        Section("auraStaticBorder — same stroke, no rotation") {
            Box(
                modifier = Modifier
                    .size(width = 220.dp, height = 64.dp)
                    .clip(RoundedCornerShape(AuraTheme.radius.lg))
                    .auraStaticBorder(cornerRadius = AuraTheme.radius.lg)
                    .background(colors.surface),
                contentAlignment = Alignment.Center,
            ) { AuraText("Static gradient stroke", style = AuraTheme.typography.body) }
        }

        Section("auraGlow — neon glow tokens") {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                GlowSwatch("cyan") { Modifier.auraGlow(AuraTheme.glows.cyan, CircleShape) }
                GlowSwatch("violet") { Modifier.auraGlow(AuraTheme.glows.violet, CircleShape) }
                GlowSwatch("magenta") { Modifier.auraGlow(AuraTheme.glows.magenta, CircleShape) }
            }
        }

        Section("selectedAccent — selected-row treatment (with glow)") {
            val shape = RoundedCornerShape(AuraTheme.radius.md)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RowChip("Selected + glow", Modifier.selectedAccent(active = true, shape = shape, glow = true))
                RowChip("Selected", Modifier.selectedAccent(active = true, shape = shape))
                RowChip("Not selected", Modifier.selectedAccent(active = false, shape = shape))
            }
        }

        Section("PulseDot — animated status pulse") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                PulseDot()
                PulseDot(color = colors.neonViolet, dotSize = 12.dp)
                PulseDot(color = colors.neonMagenta, dotSize = 6.dp)
            }
        }

        Section("auraBrush — gradient ink for headlines") {
            AuraGradientText("Speak. The keys vanish.", style = AuraTheme.typography.h2)
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(width = 220.dp, height = 10.dp)
                    .clip(RoundedCornerShape(AuraTheme.radius.pill))
                    .background(auraBrush()),
            )
        }

        Section("ButtonVariant.Aura — signature CTA (animated border)") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AuraButton("Try voice", onClick = {}, variant = ButtonVariant.Aura)
                AuraButton("Default", onClick = {}, variant = ButtonVariant.Default)
            }
        }

        Section("AuraSegmentedControl — glowing selection") {
            var idx by remember { mutableIntStateOf(0) }
            AuraSegmentedControl(
                options = listOf("Cloud", "On-device", "Auto"),
                selectedIndex = idx,
                onSelect = { idx = it },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Section("AuraModeBadge") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AuraModeBadge(code = "MODE.CLD", label = "Online", mode = ModeBadgeVariant.Primary)
                AuraModeBadge(code = "MODE.DEV", label = "Offline", mode = ModeBadgeVariant.Secondary)
            }
        }

        Section("AuraKbdCombo") {
            AuraKbdCombo(keys = listOf("⌘", "⇧", "K"))
        }

        Section("AuraRangeSlider") {
            var v by remember { mutableFloatStateOf(0.4f) }
            AuraRangeSlider(
                value = v,
                onValueChange = { v = it },
                startLabel = "Fast",
                endLabel = "Accurate",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AuraText(title, style = AuraTheme.typography.eyebrow, color = AuraTheme.colors.neonCyan)
        content()
    }
}

@Composable
private fun GlowSwatch(label: String, glowModifier: @Composable () -> Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .then(glowModifier())
                .clip(CircleShape)
                .background(AuraTheme.colors.surface),
        )
        AuraText(label, style = AuraTheme.typography.caption, color = AuraTheme.colors.textMuted)
    }
}

@Composable
private fun RowChip(label: String, modifier: Modifier) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(44.dp)
            .then(modifier)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) { AuraText(label, style = AuraTheme.typography.body) }
}

@AuraPreview
@Composable
private fun AuraEffectsGalleryPreview() {
    AuraTheme { AuraEffectsGallery() }
}
