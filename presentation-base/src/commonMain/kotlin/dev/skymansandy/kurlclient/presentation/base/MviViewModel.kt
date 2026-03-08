package dev.skymansandy.kurlclient.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.kurlclient.presentation.base.contract.UiEffect
import dev.skymansandy.kurlclient.presentation.base.contract.UiEvent
import dev.skymansandy.kurlclient.presentation.base.contract.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviViewModel<State : UiState, Event : UiEvent, Effect : UiEffect> : ViewModel() {

    val state: StateFlow<State>
        field = MutableStateFlow(createInitialState())

    val effects: SharedFlow<Effect>
        field = MutableSharedFlow<Effect>()

    val events: SharedFlow<Event>
        field = MutableSharedFlow<Event>()

    fun setState(reducer: State.() -> State) {
        state.update {
            val newState = it.reducer()
            newState
        }
    }

    fun setEffect(effect: Effect) {
        effects.tryEmit(effect)
    }

    fun setEvent(event: Event) {
        viewModelScope.launch {
            events.emit(event)
        }
    }

    abstract fun onEvent(event: Event)

    abstract fun createInitialState(): State

    init {
        viewModelScope.launch {
            events.collect {
                onEvent(it)
            }
        }
    }
}
