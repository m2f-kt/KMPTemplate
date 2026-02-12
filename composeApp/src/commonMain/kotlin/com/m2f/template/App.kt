package com.m2f.template

import androidx.compose.runtime.Composable
import com.m2f.template.di.allAppModules
import com.m2f.template.navigation.AppNavHost
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = {
        modules(allAppModules)
    }) {
        AppNavHost()
    }
}
