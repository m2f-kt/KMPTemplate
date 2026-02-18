package com.m2f.template.core.testing

/**
 * Sealed hierarchy representing queued test actions in the ViewModel test DSL.
 *
 * Each statement type corresponds to an action the test runner will execute
 * sequentially: dispatching intents, asserting model states, or asserting events.
 */
sealed interface Statement<out Intent, out Model, out Event> {

    /**
     * Dispatches an intent to the ViewModel under test.
     */
    data class IntentStatement<Intent>(val intent: Intent) : Statement<Intent, Nothing, Nothing>

    /**
     * Asserts that the next model emission matches [expected].
     */
    data class ModelStatement<Model>(val expected: Model) : Statement<Nothing, Model, Nothing>

    /**
     * Asserts that the next event emission matches [expected].
     */
    data class EventStatement<Event>(val expected: Event) : Statement<Nothing, Nothing, Event>
}
