package com.m2f.template.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.m2f.template.app.auth.ForgotPasswordScreen
import com.m2f.template.app.auth.ForgotPasswordViewModel
import com.m2f.template.app.auth.LoginScreen
import com.m2f.template.app.auth.LoginViewModel
import com.m2f.template.app.auth.RegisterScreen
import com.m2f.template.app.auth.RegisterViewModel
import com.m2f.template.app.dashboard.DashboardScreen
import com.m2f.template.app.dashboard.DashboardViewModel
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.card.CardVariant
import com.m2f.template.designsystem.components.card.TerminalCard
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.theme.TerminalTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
    ) {
        composable<LoginRoute> {
            val viewModel = koinViewModel<LoginViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            LoginScreen(
                state = state,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onRememberMeChange = viewModel::onRememberMeChange,
                onLoginClick = viewModel::login,
                onGoogleClick = { /* Implemented in 05-07 via OAuthHandler */ },
                onAppleClick = { /* Implemented in 05-07 via OAuthHandler */ },
                onForgotPassword = { navController.navigate(ForgotPasswordRoute) },
                onRegister = { navController.navigate(RegisterRoute) },
            )
            LaunchedEffect(state.loginSuccess) {
                if (state.loginSuccess) {
                    navController.navigate(DashboardRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                }
            }
        }

        composable<RegisterRoute> {
            val viewModel = koinViewModel<RegisterViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            RegisterScreen(
                state = state,
                onFirstNameChange = viewModel::onFirstNameChange,
                onLastNameChange = viewModel::onLastNameChange,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                onTermsAcceptedChange = viewModel::onTermsAcceptedChange,
                onRegisterClick = viewModel::register,
                onGoogleClick = { /* Implemented in 05-07 via OAuthHandler */ },
                onAppleClick = { /* Implemented in 05-07 via OAuthHandler */ },
                onLogin = { navController.popBackStack() },
            )
            LaunchedEffect(state.registerSuccess) {
                if (state.registerSuccess) {
                    navController.navigate(DashboardRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                }
            }
        }

        composable<DashboardRoute> {
            val viewModel = koinViewModel<DashboardViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            DashboardScreen(
                state = state,
                onNavItemSelected = viewModel::selectNavItem,
                onProfileClick = { navController.navigate(ProfileRoute) },
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToProcesses = { navController.navigate(ProcessesRoute) },
                onNavigateToLogs = { navController.navigate(LogsRoute) },
                onNavigateToDeployments = { navController.navigate(DeploymentsRoute) },
                onNavigateToSettings = { navController.navigate(SettingsRoute) },
            )
        }

        composable<ProfileRoute> {
            PlaceholderScreen(
                title = "> profile",
                buttons = listOf(
                    "Back" to { navController.popBackStack() },
                ),
            )
        }

        composable<ForgotPasswordRoute> {
            val viewModel = koinViewModel<ForgotPasswordViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            ForgotPasswordScreen(
                state = state,
                onEmailChange = viewModel::onEmailChange,
                onSubmit = viewModel::submit,
                onBackToLogin = { navController.popBackStack() },
            )
        }

        composable<ProcessesRoute> {
            PlaceholderScreen(
                title = "> processes",
                buttons = listOf(
                    "Back" to { navController.popBackStack() },
                ),
            )
        }

        composable<LogsRoute> {
            PlaceholderScreen(
                title = "> logs",
                buttons = listOf(
                    "Back" to { navController.popBackStack() },
                ),
            )
        }

        composable<DeploymentsRoute> {
            PlaceholderScreen(
                title = "> deployments",
                buttons = listOf(
                    "Back" to { navController.popBackStack() },
                ),
            )
        }

        composable<SettingsRoute> {
            PlaceholderScreen(
                title = "> settings",
                buttons = listOf(
                    "Back" to { navController.popBackStack() },
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
        TerminalCard(
            title = title,
            description = "// under construction",
            variant = CardVariant.Default,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(spacing.xl),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TerminalBadge(
                    text = "status: pending",
                    variant = BadgeVariant.Warning,
                    icon = "\u25D0",
                )

                Spacer(modifier = Modifier.height(spacing.sm))

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
}
