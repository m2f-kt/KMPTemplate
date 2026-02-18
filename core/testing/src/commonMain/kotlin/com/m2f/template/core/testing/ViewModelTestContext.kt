package com.m2f.template.core.testing

/**
 * DSL receiver for building ViewModel test scenarios.
 *
 * Collects a sequence of [Statement] objects that the test runner
 * will execute in order against the ViewModel under test.
 */
@ViewModelTestDsl
class ViewModelTestContext<Intent, Model, Mutation, Event> {

    internal val statements = mutableListOf<Statement<Intent, Model, Event>>()

    /**
     * Queues an intent to be dispatched to the ViewModel.
     */
    fun intent(intent: Intent) {
        statements.add(Statement.IntentStatement(intent))
    }

    /**
     * Queues a model assertion. The test runner will await the next model
     * emission and assert it equals [expected].
     */
    fun model(expected: Model) {
        statements.add(Statement.ModelStatement(expected))
    }

    /**
     * Queues an event assertion. The test runner will await the next event
     * emission and assert it equals [expected].
     */
    fun event(expected: Event) {
        statements.add(Statement.EventStatement(expected))
    }
}
