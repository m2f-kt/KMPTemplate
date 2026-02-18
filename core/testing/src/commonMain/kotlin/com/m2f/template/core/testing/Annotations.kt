package com.m2f.template.core.testing

/**
 * DSL marker annotation for ViewModel test DSL scope.
 * Prevents accidental scope leaking when nesting DSL blocks.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS)
annotation class ViewModelTestDsl

/**
 * DSL marker annotation for fake SDK builder scope.
 * Used by fake builders to prevent scope leaking.
 */
@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class FakeSDKDsl
