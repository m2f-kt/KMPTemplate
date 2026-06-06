package com.m2f.template.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
actual fun rememberSystemDarkTheme(): Boolean = isSystemInDarkTheme()
