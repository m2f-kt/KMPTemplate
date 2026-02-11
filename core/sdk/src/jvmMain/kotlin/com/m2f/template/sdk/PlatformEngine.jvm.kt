package com.m2f.template.sdk

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun platformEngine(): HttpClientEngineFactory<*> = CIO
