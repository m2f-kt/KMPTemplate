package com.m2f.template.sdk

import io.ktor.client.engine.HttpClientEngineFactory

expect fun platformEngine(): HttpClientEngineFactory<*>
