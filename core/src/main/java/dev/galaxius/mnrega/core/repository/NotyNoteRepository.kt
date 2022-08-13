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

package dev.galaxius.mnrega.core.repository

import dev.galaxius.mnrega.core.model.Note
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Singleton

/**
 * Repository for attendances.
 */
@Singleton
interface MnregaNoteRepository {

    /**
     * Returns a attendance
     *
     * @param attendanceId A attendance ID.
     */
    fun getNoteById(attendanceId: String): Flow<Note>

    /**
     * Returns all attendances.
     */
    fun getAllNotes(): Flow<Either<List<Note>>>

    /**
     * Adds a new attendance
     *
     * @param title Title of a attendance
     * @param attendance Body of a attendance
     */
    suspend fun addNote(title: String, attendance: String): Either<String>

    /**
     * Adds a list of attendances. Replaces attendances if already exists
     */
    suspend fun addNotes(attendances: List<Note>)

    /**
     * Updates a new attendance having ID [attendanceId]
     *
     * @param attendanceId The Note ID
     * @param title Title of a attendance
     * @param attendance Body of a attendance
     */
    suspend fun updateNote(
        attendanceId: String,
        title: String,
        attendance: String
    ): Either<String>

    /**
     * Deletes a new attendance having ID [attendanceId]
     */
    suspend fun deleteNote(attendanceId: String): Either<String>

    /**
     * Deletes all attendances.
     */
    suspend fun deleteAllNotes()

    /**
     * Updates ID of a attendance
     */
    suspend fun updateNoteId(oldNoteId: String, newNoteId: String)

    companion object {
        private const val PREFIX_TEMP_NOTE_ID = "TMP"
        fun generateTemporaryId() = "$PREFIX_TEMP_NOTE_ID-${UUID.randomUUID()}"
        fun isTemporaryNote(attendanceId: String) = attendanceId.startsWith(PREFIX_TEMP_NOTE_ID)
    }
}
