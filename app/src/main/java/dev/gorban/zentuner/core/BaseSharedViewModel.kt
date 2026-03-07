package dev.gorban.zentuner.core

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseSharedViewModel<State, Action, Event>(
    initialState: State
) : ViewModel() {

    private val _viewStates = MutableStateFlow(initialState)
    val viewStates: StateFlow<State> = _viewStates.asStateFlow()

    private val _viewActions = MutableStateFlow<Action?>(null)
    val viewActions: StateFlow<Action?> = _viewActions.asStateFlow()

    protected var viewState: State
        get() = _viewStates.value
        set(value) { _viewStates.value = value }

    protected var viewAction: Action?
        get() = _viewActions.value
        set(value) { _viewActions.value = value }

    abstract fun obtainEvent(viewEvent: Event)

    fun clearAction() {
        _viewActions.value = null
    }
}
