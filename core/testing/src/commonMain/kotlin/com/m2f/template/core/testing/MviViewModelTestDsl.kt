package com.m2f.template.core.testing

import app.cash.turbine.turbineScope
import com.m2f.template.core.mvi.MviViewModel
import io.kotest.matchers.shouldBe
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.test.runTest

/**
 * Extension function on [MviViewModel] for declarative, sequential testing
 * of intent -> model -> event flows using Turbine.
 *
 * The DSL queues [Statement] objects (intent dispatches, model assertions,
 * event assertions) and then executes them sequentially inside a Turbine scope.
 * The initial StateFlow emission is consumed automatically.
 *
 * Usage:
 * ```kotlin
 * viewModel.test {
 *     intent(MyIntent.LoadData)
 *     model(MyModel(loading = true))
 *     model(MyModel(loading = false, data = expectedData))
 * }
 * ```
 *
 * For suspend operations in test setup, use [scopedTest] instead.
 *
 * @param context optional [CoroutineContext] passed to [runTest]
 * @param block DSL block that queues intents, model assertions, and event assertions
 */
fun <Intent, Model, Mutation, Event> MviViewModel<Intent, Model, Mutation, Event>.test(
    context: CoroutineContext = EmptyCoroutineContext,
    block: ViewModelTestContext<Intent, Model, Mutation, Event>.() -> Unit,
) {
    val ctx = ViewModelTestContext<Intent, Model, Mutation, Event>().apply(block)
    runTest(context) {
        turbineScope {
            val modelTurbine = model.testIn(backgroundScope)
            val eventTurbine = event.testIn(backgroundScope)

            // Skip initial StateFlow emission
            modelTurbine.awaitItem()

            for (statement in ctx.statements) {
                when (statement) {
                    is Statement.IntentStatement -> take(statement.intent)
                    is Statement.ModelStatement -> modelTurbine.awaitItem() shouldBe statement.expected
                    is Statement.EventStatement -> eventTurbine.awaitItem() shouldBe statement.expected
                }
            }

            modelTurbine.cancelAndIgnoreRemainingEvents()
            eventTurbine.cancelAndIgnoreRemainingEvents()
        }
    }
}

/**
 * Suspend variant of [test] that allows coroutine operations in the DSL block.
 *
 * Usage:
 * ```kotlin
 * viewModel.scopedTest {
 *     // Can call suspend functions here for setup
 *     intent(MyIntent.LoadData)
 *     model(MyModel(loading = true))
 *     event(MyEvent.DataLoaded)
 * }
 * ```
 *
 * @param context optional [CoroutineContext] passed to [runTest]
 * @param block suspend DSL block that queues intents, model assertions, and event assertions
 */
fun <Intent, Model, Mutation, Event> MviViewModel<Intent, Model, Mutation, Event>.scopedTest(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend ViewModelTestContext<Intent, Model, Mutation, Event>.() -> Unit,
) {
    val ctx = ViewModelTestContext<Intent, Model, Mutation, Event>()
    runTest(context) {
        ctx.block()
        turbineScope {
            val modelTurbine = model.testIn(backgroundScope)
            val eventTurbine = event.testIn(backgroundScope)

            // Skip initial StateFlow emission
            modelTurbine.awaitItem()

            for (statement in ctx.statements) {
                when (statement) {
                    is Statement.IntentStatement -> take(statement.intent)
                    is Statement.ModelStatement -> modelTurbine.awaitItem() shouldBe statement.expected
                    is Statement.EventStatement -> eventTurbine.awaitItem() shouldBe statement.expected
                }
            }

            modelTurbine.cancelAndIgnoreRemainingEvents()
            eventTurbine.cancelAndIgnoreRemainingEvents()
        }
    }
}
