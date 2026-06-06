package com.m2f.template.designsystem.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.m2f.template.designsystem.theme.AuraPreview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * A simple horizontal divider line using theme border color.
 *
 * @param modifier Modifier for the divider.
 */
@Composable
fun AuraDivider(
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors
    val borders = AuraTheme.borders

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(borders.thin)
            .background(colors.border),
    )
}

@AuraPreview
@Composable
private fun AuraDividerPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
        ) {
            AuraText("Above")
            AuraDivider(modifier = Modifier.padding(vertical = 8.dp))
            AuraText("Below")
        }
    }
}
