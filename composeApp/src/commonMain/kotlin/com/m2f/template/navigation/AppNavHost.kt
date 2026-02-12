package com.m2f.template.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.theme.TerminalTheme

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
    ) {
        composable<LoginRoute> {
            PlaceholderScreen(
                title = "Login Screen",
                buttons = listOf(
                    "Go to Register" to {
                        navController.navigate(RegisterRoute)
                    },
                    "Login (Go to Dashboard)" to {
                        navController.navigate(DashboardRoute) {
                            popUpTo<LoginRoute> { inclusive = true }
                        }
                    },
                ),
            )
        }

        composable<RegisterRoute> {
            PlaceholderScreen(
                title = "Register Screen",
                buttons = listOf(
                    "Back" to {
                        navController.popBackStack()
                    },
                ),
            )
        }

        composable<DashboardRoute> {
            PlaceholderScreen(
                title = "Dashboard",
                buttons = listOf(
                    "Go to Profile" to {
                        navController.navigate(ProfileRoute)
                    },
                ),
            )
        }

        composable<ProfileRoute> {
            PlaceholderScreen(
                title = "Profile",
                buttons = listOf(
                    "Back" to {
                        navController.popBackStack()
                    },
                ),
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    buttons: List<Pair<String, () -> Unit>>,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val radius = TerminalTheme.radius
    val spacing = TerminalTheme.spacing

    Box(
        modifier = Modifier.fillMaxSize().background(colors.bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            TerminalText(
                text = title,
                style = typography.xxl,
            )
            buttons.forEach { (label, onClick) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(radius.sm))
                        .background(colors.accent)
                        .clickable(onClick = onClick)
                        .padding(horizontal = spacing.lg, vertical = spacing.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    TerminalText(
                        text = label,
                        style = typography.base.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.bg,
                    )
                }
            }
        }
    }
}
