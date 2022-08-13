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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dev.galaxius.mnrega.core.model.Note
import dev.galaxius.mnrega.core.model.MnregaTask
import dev.galaxius.mnrega.core.repository.MnregaNoteRepository
import dev.galaxius.mnrega.core.task.MnregaTaskManager
import dev.galaxius.mnrega.di.LocalRepository
import dev.galaxius.mnrega.utils.validator.NoteValidator
import dev.galaxius.mnrega.view.state.NoteDetailState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class NoteDetailViewModel @AssistedInject constructor(
    private val mnregaTaskManager: MnregaTaskManager,
    @LocalRepository private val attendanceRepository: MnregaNoteRepository,
    @Assisted private val attendanceId: String
) : BaseViewModel<NoteDetailState>(initialState = NoteDetailState()) {

    private var job: Job? = null
    private lateinit var currentNote: Note

    init {
        loadNote()
    }

    fun setTitle(title: String) {
        setState { state -> state.copy(title = title) }
        validateNote()
    }

    fun setNote(attendance: String) {
        setState { state -> state.copy(attendance = attendance) }
        validateNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            setState { state -> state.copy(isLoading = true) }
            val attendance = attendanceRepository.getNoteById(attendanceId).firstOrNull()
            if (attendance != null) {
                currentNote = attendance
                setState { state ->
                    state.copy(isLoading = false, title = attendance.title, attendance = attendance.attendance)
                }
            } else {
                setState { state -> state.copy(isLoading = false, finished = true) }
            }
        }
    }

    fun save() {
        val title = currentState.title?.trim() ?: return
        val attendance = currentState.attendance?.trim() ?: return

        job?.cancel()
        job = viewModelScope.launch {
            setState { state -> state.copy(isLoading = true) }

            val response = attendanceRepository.updateNote(attendanceId, title, attendance)

            setState { state -> state.copy(isLoading = false) }

            response.onSuccess { attendanceId ->
                if (MnregaNoteRepository.isTemporaryNote(attendanceId)) {
                    scheduleNoteCreate(attendanceId)
                } else {
                    scheduleNoteUpdate(attendanceId)
                }
                setState { state -> state.copy(finished = true) }
            }.onFailure { message ->
                setState { state -> state.copy(error = message) }
            }
        }
    }

    fun delete() {
        job?.cancel()
        job = viewModelScope.launch {
            setState { state -> state.copy(isLoading = true) }

            val response = attendanceRepository.deleteNote(attendanceId)

            setState { state -> state.copy(isLoading = false) }

            response.onSuccess { attendanceId ->
                if (!MnregaNoteRepository.isTemporaryNote(attendanceId)) {
                    scheduleNoteDelete(attendanceId)
                }
                setState { state -> state.copy(finished = true) }
            }.onFailure { message ->
                setState { state -> state.copy(error = message) }
            }
        }
    }

    private fun validateNote() {
        try {
            val oldTitle = currentNote.title
            val oldNote = currentNote.attendance

            val title = currentState.title
            val attendance = currentState.attendance

            val isValid = title != null && attendance != null && NoteValidator.isValidNote(title, attendance)
            val areOldAndUpdatedNoteSame = oldTitle == title?.trim() && oldNote == attendance?.trim()

            setState { state -> state.copy(showSave = isValid && !areOldAndUpdatedNoteSame) }
        } catch (error: Throwable) {
        }
    }

    private fun scheduleNoteCreate(attendanceId: String) =
        mnregaTaskManager.scheduleTask(MnregaTask.create(attendanceId))

    private fun scheduleNoteUpdate(attendanceId: String) =
        mnregaTaskManager.scheduleTask(MnregaTask.update(attendanceId))

    private fun scheduleNoteDelete(attendanceId: String) =
        mnregaTaskManager.scheduleTask(MnregaTask.delete(attendanceId))

    @AssistedFactory
    interface Factory {
        fun create(attendanceId: String): NoteDetailViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            attendanceId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(attendanceId) as T
            }
        }
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
interface AssistedInjectModule
