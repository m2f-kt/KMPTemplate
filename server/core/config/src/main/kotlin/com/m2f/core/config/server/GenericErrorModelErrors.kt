package com.m2f.core.config.server

import kotlinx.serialization.Serializable

@Serializable
data class GenericErrorModelErrors(val body: List<String>)
