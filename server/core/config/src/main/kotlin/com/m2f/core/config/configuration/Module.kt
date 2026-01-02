package com.m2f.core.config.configuration

import org.koin.dsl.module

val configurationModule = module {
    single { Configuration() }
}
