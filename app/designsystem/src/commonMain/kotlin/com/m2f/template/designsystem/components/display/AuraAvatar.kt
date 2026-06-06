package com.m2f.template.designsystem.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import com.m2f.template.designsystem.theme.AuraPreview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * A circular avatar element displaying an image or 1-2 character initials as fallback.
 *
 * @param initials The initials to display (typically 1-2 characters) when no image is available.
 * @param modifier Modifier for the container.
 * @param imageUrl Optional URL of the avatar image. If provided and loads successfully, displays the image.
 * @param size The diameter of the avatar circle.
 */
@Composable
fun AuraAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: Dp = 36.dp,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val circleShape = RoundedCornerShape(50)

    // If imageUrl provided, try to load it with fallback to initials on error
    if (imageUrl != null) {
        var showFallback by remember(imageUrl) { mutableStateOf(false) }
        if (!showFallback) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Avatar",
                modifier = modifier.size(size).clip(circleShape),
                contentScale = ContentScale.Crop,
                onError = { showFallback = true },
            )
            return
        }
    }

    // Fallback to initials
    Box(
        modifier = modifier
            .size(size)
            .clip(circleShape)
            .background(colors.accentMuted),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = initials.take(2).uppercase(),
            style = typography.sm.copy(
                color = colors.accent,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@AuraPreview
@Composable
private fun AuraAvatarPreview() {
    AuraTheme {
        Row(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuraAvatar("AB")
            AuraAvatar("JD", size = 48.dp)
            AuraAvatar("X")
            // Avatar with image URL (shows fallback if URL is invalid)
            AuraAvatar("U", imageUrl = "https://example.com/avatar.jpg", size = 48.dp)
        }
    }
}
