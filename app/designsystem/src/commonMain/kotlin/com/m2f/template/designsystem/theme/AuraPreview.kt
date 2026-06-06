package com.m2f.template.designsystem.theme

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview

/**
 * A custom multi-mode preview annotation that generates Light, Dark, and Desktop-wide
 * previews for any composable.
 *
 * Use this instead of bare `@Preview` on all designsystem preview functions to ensure
 * every component is always previewed in all three modes consistently.
 */
@Preview(
    name = "Light Mode",
    uiMode = AndroidUiModes.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark Mode",
    uiMode = AndroidUiModes.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "Desktop",
    widthDp = 1024,
)
@Preview(
    name = "Desktop",
    uiMode = AndroidUiModes.UI_MODE_NIGHT_YES,
    widthDp = 1024,
)
annotation class AuraPreview
