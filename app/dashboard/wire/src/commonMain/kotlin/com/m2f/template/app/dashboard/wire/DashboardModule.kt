package com.m2f.template.app.dashboard.wire

import com.m2f.template.app.dashboard.DashboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule = module {
    viewModelOf(::DashboardViewModel)
}
