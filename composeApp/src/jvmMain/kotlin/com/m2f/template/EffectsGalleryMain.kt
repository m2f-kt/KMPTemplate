package com.m2f.template

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.m2f.template.designsystem.preview.AuraEffectsGallery
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * Standalone desktop window that renders the [AuraEffectsGallery] — every Aura brand-signature
 * effect/modifier + net-new component, live, so they're visible without wiring them into a screen.
 *
 * Run it:  ./gradlew :composeApp:hotRunJvm --mainClass=com.m2f.template.EffectsGalleryMainKt
 * (Hot Reload on, so tweaking AuraEffectsGallery.kt updates the window instantly.)
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Aura — effects gallery",
        state = rememberWindowState(size = DpSize(720.dp, 900.dp)),
    ) {
        AuraTheme(darkTheme = true) {
            AuraEffectsGallery()
        }
    }
}
