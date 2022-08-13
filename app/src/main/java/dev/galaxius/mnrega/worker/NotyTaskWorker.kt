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

package dev.galaxius.mnrega.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.galaxius.mnrega.core.model.Note
import dev.galaxius.mnrega.core.model.MnregaTaskAction
import dev.galaxius.mnrega.core.repository.MnregaNoteRepository
import dev.galaxius.mnrega.core.repository.Either
import dev.galaxius.mnrega.di.LocalRepository
import dev.galaxius.mnrega.di.RemoteRepository
import dev.galaxius.mnrega.utils.ext.getEnum
import kotlinx.coroutines.flow.first

@HiltWorker
class MnregaTaskWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @RemoteRepository private val remoteNoteRepository: MnregaNoteRepository,
    @LocalRepository private val localNoteRepository: MnregaNoteRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RUN_ATTEMPTS) return Result.failure()

        val attendanceId = getNoteId()

        return when (getTaskAction()) {
            MnregaTaskAction.CREATE -> addNote(attendanceId)
            MnregaTaskAction.UPDATE -> updateNote(attendanceId)
            MnregaTaskAction.DELETE -> deleteNote(attendanceId)
        }
    }

    private suspend fun addNote(tempNoteId: String): Result {
        val attendance = fetchLocalNote(tempNoteId)
        val response = remoteNoteRepository.addNote(attendance.title, attendance.attendance)
        return if (response is Either.Success) {
            // `response.data` will be a attendanceId received from API.
            localNoteRepository.updateNoteId(tempNoteId, response.data)
            Result.success()
        } else Result.retry()
    }

    private suspend fun updateNote(attendanceId: String): Result {
        val attendance = fetchLocalNote(attendanceId)
        val response = remoteNoteRepository.updateNote(attendance.id, attendance.title, attendance.attendance)
        return if (response is Either.Success) Result.success() else Result.retry()
    }

    private suspend fun deleteNote(attendanceId: String): Result {
        val response = remoteNoteRepository.deleteNote(attendanceId)
        return if (response is Either.Success) Result.success() else Result.retry()
    }

    private suspend fun fetchLocalNote(attendanceId: String): Note =
        localNoteRepository.getNoteById(attendanceId).first()

    private fun getNoteId(): String = inputData.getString(KEY_NOTE_ID)
        ?: throw IllegalStateException("$KEY_NOTE_ID should be provided as input data.")

    private fun getTaskAction(): MnregaTaskAction = inputData.getEnum<MnregaTaskAction>(KEY_TASK_TYPE)
        ?: throw IllegalStateException("$KEY_TASK_TYPE should be provided as input data.")

    companion object {
        const val MAX_RUN_ATTEMPTS = 3
        const val KEY_NOTE_ID = "attendance_id"
        const val KEY_TASK_TYPE = "mnrega_task_type"
    }
}
