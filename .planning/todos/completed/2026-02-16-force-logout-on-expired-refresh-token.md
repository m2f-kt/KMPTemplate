---
created: 2026-02-16T00:54:46.023Z
title: Force logout on expired refresh token
area: auth
files:
  - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt
  - composeApp/src/commonMain/kotlin/com/m2f/template/app/navigation/AppNavHost.kt
---

## Problem

When both the access token and refresh token are expired, the app stays in an error state instead of redirecting to login. For example, the profile screen shows an auth error message. While a transient auth error is acceptable (token refresh in progress), when the refresh token itself is expired and cannot be renewed, the user should be logged out and sent back to the login screen automatically.

Currently the AuthInterceptor attempts refresh and fails, but the failure is returned as a ClientError.Unauthorized to the calling ViewModel, which just displays it as an error state. No mechanism triggers a full logout + navigation reset.

## Solution

- In AuthInterceptor: when refresh fails with 401, clear stored tokens and signal a "session expired" event
- Add an app-level observer (Flow/Channel) for session expiry events
- When session expiry fires, navigate to LoginRoute with `popUpTo(0) { inclusive = true }` to clear the entire back stack
- Consider a snackbar/toast: "Session expired. Please log in again."
