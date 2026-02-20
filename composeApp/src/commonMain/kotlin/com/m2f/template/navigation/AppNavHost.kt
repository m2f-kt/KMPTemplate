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
import com.m2f.template.app.auth.ForgotPasswordIntent
import com.m2f.template.app.auth.ForgotPasswordScreen
import com.m2f.template.app.auth.ForgotPasswordViewModel
import com.m2f.template.app.auth.LoginEvent
import com.m2f.template.app.auth.LoginIntent
import com.m2f.template.app.auth.LoginScreen
import com.m2f.template.app.auth.LoginViewModel
import com.m2f.template.app.auth.OAuthCallbackHandler
import com.m2f.template.app.auth.OAuthHandler
import com.m2f.template.app.auth.RegisterEvent
import com.m2f.template.app.auth.RegisterIntent
import com.m2f.template.app.auth.RegisterScreen
import com.m2f.template.app.auth.RegisterViewModel
import com.m2f.template.app.auth.checkOAuthCallback
import com.m2f.template.app.dashboard.DashboardEvent
import com.m2f.template.app.dashboard.DashboardIntent
import com.m2f.template.app.dashboard.DashboardScreen
import com.m2f.template.app.dashboard.DashboardViewModel
import com.m2f.template.app.admin.AdminPanelEvent
import com.m2f.template.app.admin.AdminPanelIntent
import com.m2f.template.app.admin.AdminPanelScreen
import com.m2f.template.app.admin.AdminPanelViewModel
import com.m2f.template.app.admin.RegisterMemberEvent
import com.m2f.template.app.admin.RegisterMemberIntent
import com.m2f.template.app.admin.RegisterMemberScreen
import com.m2f.template.app.admin.RegisterMemberViewModel
import com.m2f.template.app.profile.ProfileEvent
import com.m2f.template.app.profile.ProfileIntent
import com.m2f.template.app.profile.ProfileScreen
import com.m2f.template.app.profile.ProfileViewModel
import com.m2f.template.localization.LocalAppLocale
import com.m2f.template.localization.LocaleSelector
import com.m2f.template.sdk.AuthInterceptor
import com.m2f.template.sdk.defaultBaseUrl
import com.m2f.template.storage.PreferencesStorage
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
                val state by viewModel.model.collectAsStateWithLifecycle()
                LoginScreen(
                    state = state,
                    onEmailChange = { viewModel.take(LoginIntent.EmailChanged(it)) },
                    onPasswordChange = { viewModel.take(LoginIntent.PasswordChanged(it)) },
                    onRememberMeChange = { viewModel.take(LoginIntent.RememberMeChanged(it)) },
                    onLoginClick = { viewModel.take(LoginIntent.SubmitLoginClicked) },
                    onGoogleClick = { oauthHandler.startOAuth("google") },
                    onAppleClick = { oauthHandler.startOAuth("apple") },
                    onForgotPassword = { navController.navigate(ForgotPasswordRoute) },
                    onRegister = { navController.navigate(RegisterRoute) },
                )
                LaunchedEffect(Unit) {
                    viewModel.event.collect { event ->
                        when (event) {
                            is LoginEvent.NavigateToDashboard -> {
                                navController.navigate(DashboardRoute) {
                                    popUpTo<LoginRoute> { inclusive = true }
                                }
                            }
                        }
                    }
                }
            }

            composable<RegisterRoute> {
                val viewModel = koinViewModel<RegisterViewModel>()
                val state by viewModel.model.collectAsStateWithLifecycle()
                RegisterScreen(
                    state = state,
                    onFirstNameChange = { viewModel.take(RegisterIntent.FirstNameChanged(it)) },
                    onLastNameChange = { viewModel.take(RegisterIntent.LastNameChanged(it)) },
                    onEmailChange = { viewModel.take(RegisterIntent.EmailChanged(it)) },
                    onPasswordChange = { viewModel.take(RegisterIntent.PasswordChanged(it)) },
                    onConfirmPasswordChange = { viewModel.take(RegisterIntent.ConfirmPasswordChanged(it)) },
                    onTermsAcceptedChange = { viewModel.take(RegisterIntent.TermsAcceptedChanged(it)) },
                    onRegisterClick = { viewModel.take(RegisterIntent.SubmitRegisterClicked) },
                    onGoogleClick = { oauthHandler.startOAuth("google") },
                    onAppleClick = { oauthHandler.startOAuth("apple") },
                    onLogin = { navController.popBackStack() },
                )
                LaunchedEffect(Unit) {
                    viewModel.event.collect { event ->
                        when (event) {
                            is RegisterEvent.NavigateToDashboard -> {
                                navController.navigate(DashboardRoute) {
                                    popUpTo<LoginRoute> { inclusive = true }
                                }
                            }
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
                val dashboardState by dashboardViewModel.model.collectAsStateWithLifecycle()

                DashboardScreen(
                    state = dashboardState,
                    onNavItemSelected = { dashboardViewModel.take(DashboardIntent.NavItemSelected(it)) },
                    onProfileClick = { navController.navigate(ProfileRoute) },
                    onLogout = { dashboardViewModel.take(DashboardIntent.LogoutClicked) },
                    onAdminClick = { dashboardViewModel.take(DashboardIntent.AdminPanelClicked) },
                )
                LaunchedEffect(Unit) {
                    dashboardViewModel.event.collect { event ->
                        when (event) {
                            is DashboardEvent.NavigateToLogin -> {
                                navController.navigate(LoginRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            is DashboardEvent.NavigateToAdmin -> {
                                navController.navigate(AdminPanelRoute(groupId = event.groupId))
                            }
                        }
                    }
                }
            }

            composable<ProfileRoute> {
                val viewModel = koinViewModel<ProfileViewModel>()
                val state by viewModel.model.collectAsStateWithLifecycle()
                val preferencesStorage = koinInject<PreferencesStorage>()
                val currentLocale = LocalAppLocale.current
                ProfileScreen(
                    state = state,
                    onStartEditing = { viewModel.take(ProfileIntent.StartEditing) },
                    onCancelEditing = { viewModel.take(ProfileIntent.CancelEditing) },
                    onEditNameChange = { viewModel.take(ProfileIntent.EditNameChanged(it)) },
                    onEditEmailChange = { viewModel.take(ProfileIntent.EditEmailChanged(it)) },
                    onSaveProfile = { viewModel.take(ProfileIntent.SaveProfileClicked) },
                    onLogout = { viewModel.take(ProfileIntent.LogoutClicked) },
                    onBack = { navController.popBackStack() },
                    localeSelector = {
                        LocaleSelector(
                            currentLocale = currentLocale,
                            onLocaleChanged = { locale ->
                                preferencesStorage.language = locale
                            },
                        )
                    },
                )
                LaunchedEffect(Unit) {
                    viewModel.event.collect { event ->
                        when (event) {
                            is ProfileEvent.NavigateToLogin -> {
                                navController.navigate(LoginRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            }

            composable<ForgotPasswordRoute> {
                val viewModel = koinViewModel<ForgotPasswordViewModel>()
                val state by viewModel.model.collectAsStateWithLifecycle()
                ForgotPasswordScreen(
                    state = state,
                    onEmailChange = { viewModel.take(ForgotPasswordIntent.EmailChanged(it)) },
                    onSubmit = { viewModel.take(ForgotPasswordIntent.SubmitForgotPasswordClicked) },
                    onBackToLogin = { navController.popBackStack() },
                )
            }

            composable<AdminPanelRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<AdminPanelRoute>()
                val viewModel = koinViewModel<AdminPanelViewModel>()
                val state by viewModel.model.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.take(AdminPanelIntent.LoadAdminPanel(route.groupId))
                }

                AdminPanelScreen(
                    state = state,
                    onLoadMore = { viewModel.take(AdminPanelIntent.LoadMoreMembers) },
                    onRegisterMember = { viewModel.take(AdminPanelIntent.RegisterMemberClicked) },
                    onBack = { navController.popBackStack() },
                )
                LaunchedEffect(Unit) {
                    viewModel.event.collect { event ->
                        when (event) {
                            is AdminPanelEvent.NavigateToRegisterMember -> {
                                navController.navigate(RegisterMemberRoute(groupId = event.groupId))
                            }
                        }
                    }
                }
            }

            composable<RegisterMemberRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<RegisterMemberRoute>()
                val viewModel = koinViewModel<RegisterMemberViewModel>()
                val state by viewModel.model.collectAsStateWithLifecycle()
                RegisterMemberScreen(
                    state = state,
                    onEmailChange = { viewModel.take(RegisterMemberIntent.EmailChanged(it)) },
                    onPasswordChange = { viewModel.take(RegisterMemberIntent.PasswordChanged(it)) },
                    onFirstNameChange = { viewModel.take(RegisterMemberIntent.FirstNameChanged(it)) },
                    onLastNameChange = { viewModel.take(RegisterMemberIntent.LastNameChanged(it)) },
                    onRoleChange = { viewModel.take(RegisterMemberIntent.RoleChanged(it)) },
                    onSubmit = { viewModel.take(RegisterMemberIntent.SubmitRegisterMember(route.groupId)) },
                    onBack = { navController.popBackStack() },
                )
                LaunchedEffect(Unit) {
                    viewModel.event.collect { event ->
                        when (event) {
                            is RegisterMemberEvent.RegistrationSuccess -> {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}
