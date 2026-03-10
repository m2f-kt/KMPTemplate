package com.m2f.template.app.documents.wire

import com.m2f.template.app.documents.DocumentsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val documentsModule = module {
    viewModelOf(::DocumentsViewModel)
}
