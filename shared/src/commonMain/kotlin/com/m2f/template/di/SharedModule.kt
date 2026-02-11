package com.m2f.template.di

import com.m2f.template.sdk.di.sdkModule
import com.m2f.template.storage.di.storageModule
import org.koin.dsl.module

/**
 * Shared DI module available to all KMP targets.
 * Includes storageModule and sdkModule for transitive DI graph resolution.
 */
val sharedModule = module {
    includes(storageModule, sdkModule)
}
