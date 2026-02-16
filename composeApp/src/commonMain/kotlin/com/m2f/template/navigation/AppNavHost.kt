package com.m2f.template.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.m2f.template.app.auth.ForgotPasswordScreen
import com.m2f.template.app.auth.ForgotPasswordViewModel
import com.m2f.template.app.auth.LoginScreen
import com.m2f.template.app.auth.LoginViewModel
import com.m2f.template.app.auth.OAuthCallbackHandler
import com.m2f.template.app.auth.OAuthHandler
import com.m2f.template.app.auth.RegisterScreen
import com.m2f.template.app.auth.RegisterViewModel
import com.m2f.template.app.auth.checkOAuthCallback
import com.m2f.template.app.dashboard.DashboardScreen
import com.m2f.template.app.dashboard.DashboardViewModel
import com.m2f.template.app.profile.ProfileScreen
import com.m2f.template.app.profile.ProfileViewModel
import com.m2f.template.sdk.AuthInterceptor
import com.m2f.template.sdk.defaultBaseUrl
import com.m2f.template.storage.TokenStorage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val tokenStorage = koinInject<TokenStorage>()
    val authInterceptor = koinInject<AuthInterceptor>()
    val oauthHandler = remember { OAuthHandler(serverBaseUrl = defaultBaseUrl()) }

    // Check for existing auth tokens on startup
    LaunchedEffect(Unit) {
        // Clear tokens from sessions where rememberMe was false
        tokenStorage.clearSessionTokens()

        // If tokens survived (rememberMe was true), skip login
        val accessToken = tokenStorage.getAccessToken()
        if (accessToken != null) {
            navController.navigate(DashboardRoute) {
                popUpTo(LoginRoute) { inclusive = true }
            }
        }
    }

    // Check for OAuth callback on startup (WASM: browser URL params)
    LaunchedEffect(Unit) {
        val callback = checkOAuthCallback()
        if (callback != null) {
            navController.navigate(
                OAuthCallbackRoute(
                    accessToken = callback.first,
                    refreshToken = callback.second,
                ),
            ) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Navigate to login when session expires (refresh token failed)
    LaunchedEffect(Unit) {
        authInterceptor.sessionExpired.collect {
            navController.navigate(LoginRoute) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
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
                    onGoogleClick = { oauthHandler.startOAuth("google") },
                    onAppleClick = { oauthHandler.startOAuth("apple") },
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
                    onGoogleClick = { oauthHandler.startOAuth("google") },
                    onAppleClick = { oauthHandler.startOAuth("apple") },
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

            composable<OAuthCallbackRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<OAuthCallbackRoute>()
                val tokenStorage = koinInject<TokenStorage>()
                OAuthCallbackHandler(
                    accessToken = route.accessToken,
                    refreshToken = route.refreshToken,
                    tokenStorage = tokenStorage,
                    onSuccess = {
                        navController.navigate(DashboardRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onError = {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            composable<DashboardRoute> {
                val dashboardViewModel = koinViewModel<DashboardViewModel>()
                val dashboardState by dashboardViewModel.state.collectAsStateWithLifecycle()

                DashboardScreen(
                    state = dashboardState,
                    onNavItemSelected = dashboardViewModel::selectNavItem,
                    onProfileClick = { navController.navigate(ProfileRoute) },
                    onLogout = {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            composable<ProfileRoute> {
                val viewModel = koinViewModel<ProfileViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                ProfileScreen(
                    state = state,
                    onStartEditing = viewModel::startEditing,
                    onCancelEditing = viewModel::cancelEditing,
                    onEditNameChange = viewModel::onEditNameChange,
                    onEditEmailChange = viewModel::onEditEmailChange,
                    onSaveProfile = viewModel::saveProfile,
                    onLogout = viewModel::logout,
                    onBack = { navController.popBackStack() },
                )
                // Handle logout navigation
                LaunchedEffect(state.logoutTriggered) {
                    if (state.logoutTriggered) {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
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
        }
    }
}
