package com.m2f.template

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform