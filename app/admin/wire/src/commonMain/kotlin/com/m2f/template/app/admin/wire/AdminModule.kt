package com.m2f.template.app.admin.wire

import com.m2f.template.app.admin.AdminPanelViewModel
import com.m2f.template.app.admin.RegisterMemberViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val adminModule = module {
    viewModelOf(::AdminPanelViewModel)
    viewModelOf(::RegisterMemberViewModel)
}
