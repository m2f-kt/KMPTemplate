package com.m2f.template.startup

import arrow.continuations.ktor.server
import arrow.fx.coroutines.ResourceScope
import com.m2f.core.config.configuration.Configuration
import io.ktor.server.application.Application
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineFactory

context(config: Configuration, scope: ResourceScope)
suspend fun <
        TEngine : ApplicationEngine,
        TConfiguration : ApplicationEngine.Configuration,
        > startServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    module: Application.() -> Unit = {}
) {
    scope.server(
        factory = factory,
        host = config.env.http.host,
        port = config.env.http.port,
        preWait = config.env.serverConfig.preWait,
        grace = config.env.serverConfig.grace,
        timeout = config.env.serverConfig.timeout,
        module = module
    )
}
