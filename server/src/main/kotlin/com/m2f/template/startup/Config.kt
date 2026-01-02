package com.m2f.template.startup

import arrow.fx.coroutines.ResourceScope
import com.m2f.core.config.configuration.Configuration

context(_: ResourceScope)
inline fun config(
    config: Configuration = Configuration(),
    block: context(Configuration)() -> Unit
) = context(config) {
    block()
}
