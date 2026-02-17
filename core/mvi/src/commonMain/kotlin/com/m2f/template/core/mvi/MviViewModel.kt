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
    modelSharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5_000),
) : ViewModel() {

    private val pipeline = MutableSharedFlow<Either<Event, Mutation>>(extraBufferCapacity = 64)

    val model: StateFlow<Model> by lazy {
        pipeline
            .filterIsInstance<Either.Right<Mutation>>()
            .map { it.value }
            .scan(initialState) { model, mutation -> reduce(model, mutation) }
            .stateIn(viewModelScope, modelSharingStarted, initialState)
    }

    val event: SharedFlow<Event> by lazy {
        pipeline
            .filterIsInstance<Either.Left<Event>>()
            .map { it.value }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())
    }

    abstract fun take(intent: Intent)

    abstract suspend fun reduce(model: Model, mutation: Mutation): Model

    protected suspend fun sendEvent(event: Event) {
        pipeline.emit(Either.Left(event))
    }

    protected suspend fun sendMutation(mutation: Mutation) {
        pipeline.emit(Either.Right(mutation))
    }

    protected suspend fun sendStatement(statement: Either<Event, Mutation>) {
        pipeline.emit(statement)
    }
}
