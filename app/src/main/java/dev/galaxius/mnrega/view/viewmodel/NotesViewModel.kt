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

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.galaxius.mnrega.core.connectivity.ConnectionState.Available
import dev.galaxius.mnrega.core.connectivity.ConnectivityObserver
import dev.galaxius.mnrega.core.preference.PreferenceManager
import dev.galaxius.mnrega.core.repository.MnregaNoteRepository
import dev.galaxius.mnrega.core.session.SessionManager
import dev.galaxius.mnrega.core.task.MnregaTaskManager
import dev.galaxius.mnrega.core.task.TaskState
import dev.galaxius.mnrega.di.LocalRepository
import dev.galaxius.mnrega.view.state.NotesState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    @LocalRepository private val mnregaNoteRepository: MnregaNoteRepository,
    private val sessionManager: SessionManager,
    private val preferenceManager: PreferenceManager,
    private val mnregaTaskManager: MnregaTaskManager,
    private val connectivityObserver: ConnectivityObserver
) : BaseViewModel<NotesState>(initialState = NotesState()) {

    private var syncJob: Job? = null

    init {
        checkUserSession()
        observeNotes()
        syncNotes()
        observeConnectivity()
    }

    fun syncNotes() {
        if (syncJob?.isActive == true) return

        syncJob = viewModelScope.launch {
            val taskId = mnregaTaskManager.syncNotes()

            try {
                mnregaTaskManager.observeTask(taskId).collect { taskState ->
                    when (taskState) {
                        TaskState.SCHEDULED -> setState { state ->
                            state.copy(isLoading = true)
                        }
                        TaskState.COMPLETED, TaskState.CANCELLED -> setState { state ->
                            state.copy(isLoading = false)
                        }
                        TaskState.FAILED -> setState { state ->
                            state.copy(isLoading = false, error = "Failed to sync attendances")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Can't find work by ID '$taskId'")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.saveToken(null)
            mnregaTaskManager.abortAllTasks()
            mnregaNoteRepository.deleteAllNotes()
            setState { state -> state.copy(isUserLoggedIn = false) }
        }
    }

    suspend fun isDarkModeEnabled() = preferenceManager.uiModeFlow.first()

    fun setDarkMode(enable: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDarkMode(enable)
        }
    }

    private fun checkUserSession() {
        setState { it.copy(isUserLoggedIn = sessionManager.getToken() != null) }
    }

    private fun observeNotes() {
        mnregaNoteRepository.getAllNotes()
            .distinctUntilChanged()
            .onEach { response ->
                response.onSuccess { attendances ->
                    setState { state -> state.copy(isLoading = false, attendances = attendances) }
                }.onFailure { message ->
                    setState { state -> state.copy(isLoading = false, error = message) }
                }
            }.onStart { setState { state -> state.copy(isLoading = true) } }
            .launchIn(viewModelScope)
    }

    private fun observeConnectivity() {
        connectivityObserver.connectionState
            .distinctUntilChanged()
            .map { it === Available }
            .onEach { setState { state -> state.copy(isConnectivityAvailable = it) } }
            .launchIn(viewModelScope)
    }

    companion object {
        private const val TAG = "NotesViewModel"
    }
}
