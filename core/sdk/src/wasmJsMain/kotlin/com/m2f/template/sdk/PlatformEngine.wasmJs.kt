package com.m2f.template.sdk

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

actual fun platformEngine(): HttpClientEngineFactory<*> = Js
