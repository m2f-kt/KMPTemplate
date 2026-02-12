package com.m2f.template.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1C)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BasicText(
                text = title,
                style = TextStyle(
                    color = Color(0xFFD4D4D4),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            buttons.forEach { (label, onClick) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF6BAF8A))
                        .clickable(onClick = onClick)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = label,
                        style = TextStyle(
                            color = Color(0xFF1A1A1C),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }
    }
}
