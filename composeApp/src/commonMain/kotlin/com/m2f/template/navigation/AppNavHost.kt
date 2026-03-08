package com.m2f.template.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
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
import com.m2f.template.app.documents.DocumentsEvent
import com.m2f.template.app.documents.DocumentsIntent
import com.m2f.template.app.documents.DocumentsScreen
import com.m2f.template.app.documents.DocumentsViewModel
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
    backStack: MutableList<Route>,
    tokenStorage: TokenStorage,
    authInterceptor: AuthInterceptor,
) {
    val oauthHandler = remember { OAuthHandler(serverBaseUrl = defaultBaseUrl()) }

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<LoginRoute> { route ->
                    val viewModel = koinViewModel<LoginViewModel>()
                    val state by viewModel.model.collectAsStateWithLifecycle()

                    LaunchedEffect(route.invitationToken, route.invitationEmail) {
                        route.invitationToken?.let { token ->
                            viewModel.take(LoginIntent.SetInvitationToken(token))
                        }
                        route.invitationEmail?.let { email ->
                            viewModel.take(LoginIntent.SetInvitationEmail(email))
                        }
                    }

                    LoginScreen(
                        state = state,
                        onEmailChange = { viewModel.take(LoginIntent.EmailChanged(it)) },
                        onPasswordChange = { viewModel.take(LoginIntent.PasswordChanged(it)) },
                        onRememberMeChange = { viewModel.take(LoginIntent.RememberMeChanged(it)) },
                        onLoginClick = { viewModel.take(LoginIntent.SubmitLoginClicked) },
                        onGoogleClick = { oauthHandler.startOAuth("google") },
                        onAppleClick = { oauthHandler.startOAuth("apple") },
                        onForgotPassword = { backStack.add(ForgotPasswordRoute) },
                        onRegister = { backStack.add(RegisterRoute()) },
                    )
                    LaunchedEffect(Unit) {
                        viewModel.event.collect { event ->
                            when (event) {
                                is LoginEvent.NavigateToDashboard,
                                is LoginEvent.NavigateToGroup -> {
                                    backStack.clear()
                                    backStack.add(DashboardRoute)
                                }
                            }
                        }
                    }
                }

                entry<RegisterRoute> { route ->
                    val viewModel = koinViewModel<RegisterViewModel>()
                    val state by viewModel.model.collectAsStateWithLifecycle()

                    LaunchedEffect(route.invitationToken, route.invitationEmail) {
                        route.invitationToken?.let { token ->
                            viewModel.take(RegisterIntent.SetInvitationToken(token))
                        }
                        route.invitationEmail?.let { email ->
                            viewModel.take(RegisterIntent.SetInvitationEmail(email))
                        }
                    }

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
                        onLogin = { backStack.removeLastOrNull() },
                    )
                    LaunchedEffect(Unit) {
                        viewModel.event.collect { event ->
                            when (event) {
                                is RegisterEvent.NavigateToDashboard,
                                is RegisterEvent.NavigateToGroup -> {
                                    backStack.clear()
                                    backStack.add(DashboardRoute)
                                }
                            }
                        }
                    }
                }

                entry<OAuthCallbackRoute> { route ->
                    val tokenStorage = koinInject<TokenStorage>()
                    OAuthCallbackHandler(
                        accessToken = route.accessToken,
                        refreshToken = route.refreshToken,
                        tokenStorage = tokenStorage,
                        onSuccess = {
                            backStack.clear()
                            backStack.add(DashboardRoute)
                        },
                        onError = {
                            backStack.clear()
                            backStack.add(LoginRoute())
                        },
                    )
                }

                entry<DashboardRoute> {
                    val dashboardViewModel = koinViewModel<DashboardViewModel>()
                    val dashboardState by dashboardViewModel.model.collectAsStateWithLifecycle()

                    // Refresh profile data when this screen enters composition
                    // (initial load or returning from another screen)
                    LaunchedEffect(Unit) {
                        dashboardViewModel.take(DashboardIntent.RefreshProfile)
                    }

                    DashboardScreen(
                        state = dashboardState,
                        onNavItemSelected = { dashboardViewModel.take(DashboardIntent.NavItemSelected(it)) },
                        onProfileClick = { backStack.add(ProfileRoute) },
                        onLogout = { dashboardViewModel.take(DashboardIntent.LogoutClicked) },
                        onAdminClick = { dashboardViewModel.take(DashboardIntent.AdminPanelClicked) },
                    )
                    LaunchedEffect(Unit) {
                        dashboardViewModel.event.collect { event ->
                            when (event) {
                                is DashboardEvent.NavigateToLogin -> {
                                    backStack.clear()
                                    backStack.add(LoginRoute())
                                }
                                is DashboardEvent.NavigateToAdmin -> {
                                    backStack.add(AdminPanelRoute(groupId = event.groupId))
                                }
                            }
                        }
                    }
                }

                entry<ProfileRoute> {
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
                        onBack = { backStack.removeLastOrNull() },
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
                                    backStack.clear()
                                    backStack.add(LoginRoute())
                                }
                            }
                        }
                    }
                }

                entry<ForgotPasswordRoute> {
                    val viewModel = koinViewModel<ForgotPasswordViewModel>()
                    val state by viewModel.model.collectAsStateWithLifecycle()
                    ForgotPasswordScreen(
                        state = state,
                        onEmailChange = { viewModel.take(ForgotPasswordIntent.EmailChanged(it)) },
                        onSubmit = { viewModel.take(ForgotPasswordIntent.SubmitForgotPasswordClicked) },
                        onBackToLogin = { backStack.removeLastOrNull() },
                    )
                }

                entry<AdminPanelRoute> { route ->
                    val viewModel = koinViewModel<AdminPanelViewModel>()
                    val state by viewModel.model.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        val groupId = route.groupId
                        if (groupId != null) {
                            viewModel.take(AdminPanelIntent.LoadAdminPanel(groupId))
                        }
                    }

                    AdminPanelScreen(
                        state = state,
                        onLoadMore = { viewModel.take(AdminPanelIntent.LoadMoreMembers) },
                        onRegisterMember = { viewModel.take(AdminPanelIntent.RegisterMemberClicked) },
                        onBack = { backStack.removeLastOrNull() },
                        onOpenCreateGroup = { viewModel.take(AdminPanelIntent.OpenCreateGroupDialog) },
                        onCloseCreateGroup = { viewModel.take(AdminPanelIntent.CloseCreateGroupDialog) },
                        onCreateGroupNameChange = { viewModel.take(AdminPanelIntent.CreateGroupNameChanged(it)) },
                        onSubmitCreateGroup = { viewModel.take(AdminPanelIntent.SubmitCreateGroup) },
                        onOpenInvite = { viewModel.take(AdminPanelIntent.OpenInviteDialog) },
                        onCloseInvite = { viewModel.take(AdminPanelIntent.CloseInviteDialog) },
                        onInviteEmailChange = { viewModel.take(AdminPanelIntent.InviteEmailChanged(it)) },
                        onSendInvite = { viewModel.take(AdminPanelIntent.SendInvite) },
                        onConfirmRevoke = { viewModel.take(AdminPanelIntent.ConfirmRevokeInvitation(it)) },
                        onCancelRevoke = { viewModel.take(AdminPanelIntent.CancelRevoke) },
                        onExecuteRevoke = { viewModel.take(AdminPanelIntent.ExecuteRevoke) },
                        onResend = { viewModel.take(AdminPanelIntent.ResendInvitation(it)) },
                        onConfirmRemoveMember = { viewModel.take(AdminPanelIntent.ConfirmRemoveMember(it)) },
                        onCancelRemoveMember = { viewModel.take(AdminPanelIntent.CancelRemoveMember) },
                        onExecuteRemoveMember = { viewModel.take(AdminPanelIntent.ExecuteRemoveMember) },
                    )
                    LaunchedEffect(Unit) {
                        viewModel.event.collect { event ->
                            when (event) {
                                is AdminPanelEvent.NavigateToRegisterMember -> {
                                    backStack.add(RegisterMemberRoute(groupId = event.groupId))
                                }
                                is AdminPanelEvent.GroupCreated -> {
                                    backStack.add(AdminPanelRoute(groupId = event.groupId))
                                }
                            }
                        }
                    }
                }

                entry<RegisterMemberRoute> { route ->
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
                        onBack = { backStack.removeLastOrNull() },
                    )
                    LaunchedEffect(Unit) {
                        viewModel.event.collect { event ->
                            when (event) {
                                is RegisterMemberEvent.RegistrationSuccess -> {
                                    backStack.removeLastOrNull()
                                }
                            }
                        }
                    }
                }

                entry<InviteAcceptRoute> { route ->
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
                        onRequestNewInvitation = { viewModel.take(InviteAcceptIntent.RequestNewInvitation) },
                    )

                    LaunchedEffect(Unit) {
                        viewModel.event.collect { event ->
                            when (event) {
                                is InviteAcceptEvent.NavigateToGroup -> {
                                    backStack.clear()
                                    backStack.add(DashboardRoute)
                                }
                                is InviteAcceptEvent.NavigateToLogin -> {
                                    backStack.clear()
                                    backStack.add(LoginRoute(invitationToken = event.token, invitationEmail = event.email))
                                }
                                is InviteAcceptEvent.NavigateToRegister -> {
                                    backStack.clear()
                                    backStack.add(RegisterRoute(invitationToken = event.token, invitationEmail = event.email))
                                }
                                is InviteAcceptEvent.RequestedNewInvitation -> {
                                    backStack.clear()
                                    backStack.add(LoginRoute())
                                }
                            }
                        }
                    }
                }

                entry<DocumentsRoute> { route ->
                    val viewModel = koinViewModel<DocumentsViewModel>()
                    val state by viewModel.model.collectAsStateWithLifecycle()
                    var showUploadSuccess by remember { mutableStateOf(false) }

                    LaunchedEffect(route.groupId) {
                        viewModel.take(DocumentsIntent.LoadDocuments(route.groupId))
                    }

                    LaunchedEffect(Unit) {
                        viewModel.event.collect { event ->
                            when (event) {
                                is DocumentsEvent.UploadSuccess -> {
                                    showUploadSuccess = true
                                }
                            }
                        }
                    }

                    DocumentsScreen(
                        state = state,
                        onUploadClick = {
                            // File picker integration is platform-specific;
                            // placeholder for now - will be wired per-platform.
                        },
                        onDeleteDocument = { documentId ->
                            viewModel.take(DocumentsIntent.DeleteDocument(documentId))
                        },
                        onBack = { backStack.removeLastOrNull() },
                        showUploadSuccess = showUploadSuccess,
                    )
                }
            },
        )
    }
}
