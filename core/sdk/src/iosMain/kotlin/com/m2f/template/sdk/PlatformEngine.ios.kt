package com.m2f.template.sdk

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun platformEngine(): HttpClientEngineFactory<*> = Darwin
