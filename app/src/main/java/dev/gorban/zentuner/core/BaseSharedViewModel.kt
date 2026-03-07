package dev.gorban.zentuner.core

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseSharedViewModel<State : Any, Action, Event>(initialState: State) : ViewModel() {

    private val _viewStates = MutableStateFlow(initialState)

    private val _viewActions = Channel<Action>()

    fun viewStates(): StateFlow<State> = _viewStates.asStateFlow()

    fun viewActions(): Flow<Action> = _viewActions.receiveAsFlow()

    protected var viewState: State
        get() = _viewStates.value
        set(value) {
            _viewStates.value = value
        }

    protected var viewAction: Action?
        get() = null
        set(value) {
            value?.let { _viewActions.trySend(it) }
        }

    abstract fun obtainEvent(viewEvent: Event)
}
