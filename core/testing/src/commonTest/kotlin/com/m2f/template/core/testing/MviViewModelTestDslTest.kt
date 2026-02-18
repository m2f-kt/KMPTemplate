package com.m2f.template.core.testing

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import kotlin.test.Test
import kotlinx.coroutines.launch

class MviViewModelTestDslTest : ViewModelTest() {

    // Minimal MviViewModel for testing the DSL itself
    private sealed interface CounterIntent {
        data object Increment : CounterIntent
        data object Decrement : CounterIntent
        data object NotifyDone : CounterIntent
    }

    private data class CounterModel(val count: Int = 0)

    private sealed interface CounterMutation {
        data class SetCount(val count: Int) : CounterMutation
    }

    private sealed interface CounterEvent {
        data object Done : CounterEvent
    }

    private class CounterViewModel : MviViewModel<CounterIntent, CounterModel, CounterMutation, CounterEvent>(
        initialState = CounterModel()
    ) {
        override fun take(intent: CounterIntent) {
            viewModelScope.launch {
                when (intent) {
                    CounterIntent.Increment -> sendMutation(CounterMutation.SetCount(model.value.count + 1))
                    CounterIntent.Decrement -> sendMutation(CounterMutation.SetCount(model.value.count - 1))
                    CounterIntent.NotifyDone -> sendEvent(CounterEvent.Done)
                }
            }
        }

        override suspend fun reduce(model: CounterModel, mutation: CounterMutation): CounterModel =
            when (mutation) {
                is CounterMutation.SetCount -> model.copy(count = mutation.count)
            }
    }

    @Test
    fun `test DSL dispatches intents and asserts model states`() {
        val viewModel = CounterViewModel()
        viewModel.test {
            intent(CounterIntent.Increment)
            model(CounterModel(count = 1))

            intent(CounterIntent.Increment)
            model(CounterModel(count = 2))

            intent(CounterIntent.Decrement)
            model(CounterModel(count = 1))
        }
    }

    @Test
    fun `test DSL asserts events`() {
        val viewModel = CounterViewModel()
        viewModel.test {
            intent(CounterIntent.NotifyDone)
            event(CounterEvent.Done)
        }
    }

    @Test
    fun `test DSL handles mixed model and event assertions`() {
        val viewModel = CounterViewModel()
        viewModel.test {
            intent(CounterIntent.Increment)
            model(CounterModel(count = 1))

            intent(CounterIntent.NotifyDone)
            event(CounterEvent.Done)

            intent(CounterIntent.Decrement)
            model(CounterModel(count = 0))
        }
    }
}
