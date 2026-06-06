package com.m2f.template.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

abstract class MviViewModel<Intent, Model, Mutation, Event>(
    initialState: Model,
    modelSharingStarted: SharingStarted = SharingStarted.Eagerly,
) : ViewModel() {

    private val pipeline = MutableSharedFlow<Either<Event, Mutation>>(extraBufferCapacity = 64)

    val model: StateFlow<Model> =
        pipeline
            .filterIsInstance<Either.Right<Mutation>>()
            .map { it.value }
            .scan(initialState) { model, mutation -> reduce(model, mutation) }
            .stateIn(viewModelScope, modelSharingStarted, initialState)

    // Eagerly (symmetric with `model` above): the relay collects the pipeline from construction, so a
    // one-shot event emitted immediately after a subscriber attaches is not dropped during the
    // WhileSubscribed start-up latency. replay = 0 is retained, so one-shot semantics are unchanged
    // (events emitted with no active subscriber are still not delivered to a later subscriber).
    val event: SharedFlow<Event> =
        pipeline
            .filterIsInstance<Either.Left<Event>>()
            .map { it.value }
            .shareIn(viewModelScope, SharingStarted.Eagerly)

    abstract fun take(intent: Intent)

    abstract suspend fun reduce(model: Model, mutation: Mutation): Model

    protected suspend fun sendEvent(event: Event) {
        pipeline.emit(Either.Left(event))
    }

    protected suspend fun sendMutation(mutation: Mutation) {
        pipeline.emit(Either.Right(mutation))
    }
}
