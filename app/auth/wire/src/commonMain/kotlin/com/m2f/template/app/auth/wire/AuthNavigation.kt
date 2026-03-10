package com.m2f.template.app.auth.wire

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.m2f.template.app.auth.ForgotPasswordIntent
import com.m2f.template.app.auth.ForgotPasswordScreen
import com.m2f.template.app.auth.ForgotPasswordViewModel
import com.m2f.template.app.auth.InviteAcceptEvent
import com.m2f.template.app.auth.InviteAcceptIntent
import com.m2f.template.app.auth.InviteAcceptScreen
import com.m2f.template.app.auth.InviteAcceptViewModel
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
import com.m2f.template.app.auth.contract.ForgotPasswordRoute
import com.m2f.template.app.auth.contract.InviteAcceptRoute
import com.m2f.template.app.auth.contract.LoginRoute
import com.m2f.template.app.auth.contract.OAuthCallbackRoute
import com.m2f.template.app.auth.contract.RegisterRoute
import com.m2f.template.app.dashboard.contract.DashboardRoute
import com.m2f.template.navigation.Route
import com.m2f.template.sdk.defaultBaseUrl
import com.m2f.template.storage.TokenStorage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<Route>.authEntries(
    backStack: MutableList<Route>,
) {
    entry<LoginRoute> { route ->
        val viewModel = koinViewModel<LoginViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()
        val oauthHandler = remember { OAuthHandler(serverBaseUrl = defaultBaseUrl()) }

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
        val oauthHandler = remember { OAuthHandler(serverBaseUrl = defaultBaseUrl()) }

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
}
