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
import dev.galaxius.mnrega.core.repository.MnregaNoteRepository
import dev.galaxius.mnrega.core.repository.Either
import dev.galaxius.mnrega.core.task.MnregaTaskManager
import dev.galaxius.mnrega.core.task.TaskState
import dev.galaxius.mnrega.di.LocalRepository
import dev.galaxius.mnrega.di.RemoteRepository
import kotlinx.coroutines.flow.first
import java.util.*

@HiltWorker
class MnregaSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @RemoteRepository private val remoteNoteRepository: MnregaNoteRepository,
    @LocalRepository private val localNoteRepository: MnregaNoteRepository,
    private val mnregaTaskManager: MnregaTaskManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return syncNotes()
    }

    private suspend fun syncNotes(): Result {
        return try {
            // Fetches all attendances from remote.
            // If task of any attendance is still pending, skip it.
            val attendances = fetchRemoteNotes().filter { attendance -> shouldReplaceNote(attendance.id) }

            // Add/Replace attendances locally.
            localNoteRepository.addNotes(attendances)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun fetchRemoteNotes(): List<Note> {
        return when (val response = remoteNoteRepository.getAllNotes().first()) {
            is Either.Success -> response.data
            is Either.Error -> throw Exception(response.message)
        }
    }

    private fun shouldReplaceNote(attendanceId: String): Boolean {
        val taskId = mnregaTaskManager.getTaskIdFromNoteId(attendanceId).toUUID()
        val state = mnregaTaskManager.getTaskState(taskId)

        return (state == null || state != TaskState.SCHEDULED)
    }

    private fun String.toUUID(): UUID = UUID.fromString(this)
}
