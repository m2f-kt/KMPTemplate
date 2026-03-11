package com.m2f.server.files.wire

import com.m2f.server.files.di.fileModule
import org.koin.dsl.module

val fileWireModule = module {
    includes(fileModule)
}
