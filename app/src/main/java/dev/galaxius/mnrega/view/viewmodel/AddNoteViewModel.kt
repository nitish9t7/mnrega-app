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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.galaxius.mnrega.core.model.MnregaTask
import dev.galaxius.mnrega.core.repository.MnregaNoteRepository
import dev.galaxius.mnrega.core.task.MnregaTaskManager
import dev.galaxius.mnrega.di.LocalRepository
import dev.galaxius.mnrega.utils.validator.NoteValidator
import dev.galaxius.mnrega.view.state.AddNoteState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    @LocalRepository private val attendanceRepository: MnregaNoteRepository,
    private val mnregaTaskManager: MnregaTaskManager
) : BaseViewModel<AddNoteState>(initialState = AddNoteState()) {

    private var job: Job? = null

    fun setTitle(title: String) {
        setState { state -> state.copy(title = title) }
        validateNote()
    }

    fun setNote(attendance: String) {
        setState { state -> state.copy(attendance = attendance) }
        validateNote()
    }

    fun add() {
        job?.cancel()
        job = viewModelScope.launch {
            val title = state.value.title.trim()
            val attendance = state.value.attendance.trim()

            setState { state -> state.copy(isAdding = true) }

            val result = attendanceRepository.addNote(title, attendance)

            result.onSuccess { attendanceId ->
                scheduleNoteCreate(attendanceId)
                setState { state -> state.copy(isAdding = false, added = true) }
            }.onFailure { message ->
                setState { state ->
                    state.copy(isAdding = false, added = false, errorMessage = message)
                }
            }
        }
    }

    private fun scheduleNoteCreate(attendanceId: String) =
        mnregaTaskManager.scheduleTask(MnregaTask.create(attendanceId))

    private fun validateNote() {
        val isValid = NoteValidator.isValidNote(currentState.title, currentState.attendance)
        setState { state -> state.copy(showSave = isValid) }
    }

    /**
     * In simpleapp module, ViewModel's instance is created using Hilt NavGraph ViewModel so it
     * doesn't clears the ViewModel when the Fragment's onDestroy() lifecycle is invoked and
     * thus it holds the stale state when the same fragment is relaunched. So this method is
     * simply a way for Fragment to ask ViewModel to reset the state.
     */
    fun resetState() {
        setState { AddNoteState() }
    }
}
