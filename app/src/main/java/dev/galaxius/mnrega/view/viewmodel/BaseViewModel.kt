/*
 * Copyright 2022 Team GALAXIUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.galaxius.mnrega.view.viewmodel

import androidx.lifecycle.ViewModel
import dev.galaxius.mnrega.view.state.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

/**
 * Base for all the ViewModels
 */
abstract class BaseViewModel<STATE : State>(initialState: STATE) : ViewModel() {

    /**
     * Mutable State of this ViewModel
     */
    private val _state = MutableStateFlow(initialState)

    /**
     * State to be exposed to the UI layer
     */
    val state: StateFlow<STATE> = _state.asStateFlow()

    /**
     * Retrieves the current UI state
     */
    val currentState: STATE get() = state.value

    /**
     * Updates the state of this ViewModel and returns the new state
     *
     * @param update Lambda callback with old state to calculate a new state
     * @return The updated state
     */
    protected fun setState(update: (old: STATE) -> STATE): STATE = _state.updateAndGet(update)
}
