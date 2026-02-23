package com.m2f.template.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.m2f.template.app.auth.InviteAcceptEvent
import com.m2f.template.app.auth.InviteAcceptIntent
import com.m2f.template.app.auth.InviteAcceptScreen
import com.m2f.template.app.auth.InviteAcceptViewModel
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
fun AppNavHost(
    navController: NavHostController,
    tokenStorage: TokenStorage,
    authInterceptor: AuthInterceptor,
) {
    val oauthHandler = remember { OAuthHandler(serverBaseUrl = defaultBaseUrl()) }

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

            composable<DashboardRoute> { backStackEntry ->
                val dashboardViewModel = koinViewModel<DashboardViewModel>()
                val dashboardState by dashboardViewModel.model.collectAsStateWithLifecycle()

                // Refresh profile data when this screen becomes visible (initial or returning from another screen)
                val lifecycleOwner = backStackEntry
                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                            dashboardViewModel.take(DashboardIntent.RefreshProfile)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

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
                    onImageSelected = { bytes, mimeType ->
                        viewModel.take(ProfileIntent.ImageSelected(bytes, mimeType))
                    },
                    onCropConfirmed = { viewModel.take(ProfileIntent.CropConfirmed) },
                    onCropCancelled = { viewModel.take(ProfileIntent.CropCancelled) },
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
                    val groupId = route.groupId
                    if (groupId != null) {
                        viewModel.take(AdminPanelIntent.LoadAdminPanel(groupId))
                    }
                    // When groupId is null, admin panel shows its initial/empty state
                }

                AdminPanelScreen(
                    state = state,
                    onLoadMore = { viewModel.take(AdminPanelIntent.LoadMoreMembers) },
                    onRegisterMember = { viewModel.take(AdminPanelIntent.RegisterMemberClicked) },
                    onBack = { navController.popBackStack() },
                    onOpenCreateGroup = { viewModel.take(AdminPanelIntent.OpenCreateGroupDialog) },
                    onCloseCreateGroup = { viewModel.take(AdminPanelIntent.CloseCreateGroupDialog) },
                    onCreateGroupNameChange = { viewModel.take(AdminPanelIntent.CreateGroupNameChanged(it)) },
                    onSubmitCreateGroup = { viewModel.take(AdminPanelIntent.SubmitCreateGroup) },
                    onOpenInvite = { viewModel.take(AdminPanelIntent.OpenInviteDialog) },
                    onCloseInvite = { viewModel.take(AdminPanelIntent.CloseInviteDialog) },
                    onInviteEmailChange = { viewModel.take(AdminPanelIntent.InviteEmailChanged(it)) },
                    onSendInvite = { viewModel.take(AdminPanelIntent.SendInvite) },
                )
                LaunchedEffect(Unit) {
                    viewModel.event.collect { event ->
                        when (event) {
                            is AdminPanelEvent.NavigateToRegisterMember -> {
                                navController.navigate(RegisterMemberRoute(groupId = event.groupId))
                            }
                            is AdminPanelEvent.GroupCreated -> {
                                // Navigate to admin panel with new group
                                navController.navigate(AdminPanelRoute(groupId = event.groupId))
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

            composable<InviteAcceptRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<InviteAcceptRoute>()
                val viewModel = koinViewModel<InviteAcceptViewModel>()
                val state by viewModel.model.collectAsStateWithLifecycle()

                LaunchedEffect(route.token) {
                    viewModel.take(InviteAcceptIntent.LoadInvitation(route.token))
                }

                InviteAcceptScreen(
                    state = state,
                    onAccept = { viewModel.take(InviteAcceptIntent.AcceptInvitation) },
                    onGoToLogin = { viewModel.take(InviteAcceptIntent.GoToLogin) },
                    onGoToRegister = { viewModel.take(InviteAcceptIntent.GoToRegister) },
                )

                LaunchedEffect(Unit) {
                    viewModel.event.collect { event ->
                        when (event) {
                            is InviteAcceptEvent.NavigateToGroup -> {
                                navController.navigate(AdminPanelRoute(groupId = event.groupId)) {
                                    popUpTo<DashboardRoute>()
                                }
                            }
                            is InviteAcceptEvent.NavigateToLogin -> {
                                navController.navigate(LoginRoute)
                            }
                            is InviteAcceptEvent.NavigateToRegister -> {
                                navController.navigate(RegisterRoute)
                            }
                        }
                    }
                }
            }
        }
    }
}
