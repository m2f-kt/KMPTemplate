package com.m2f.core.config.configuration

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


class Configuration(
    val io: CoroutineDispatcher = Dispatchers.IO,
    val default: CoroutineDispatcher = Dispatchers.Default,
    val maxDatabaseAttempts: Int = 3,
    val env: Env = Env(),
)
