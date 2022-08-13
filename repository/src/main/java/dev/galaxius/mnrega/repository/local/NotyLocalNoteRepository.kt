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

package dev.galaxius.mnrega.repository.local

import dev.galaxius.mnrega.core.model.Note
import dev.galaxius.mnrega.core.repository.MnregaNoteRepository
import dev.galaxius.mnrega.core.repository.Either
import dev.galaxius.mnrega.data.local.dao.NotesDao
import dev.galaxius.mnrega.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

/**
 * Source of data of attendances from from local database
 */
class MnregaLocalNoteRepository @Inject constructor(
    private val attendancesDao: NotesDao
) : MnregaNoteRepository {

    override fun getNoteById(attendanceId: String): Flow<Note> = attendancesDao.getNoteById(attendanceId)
        .filterNotNull()
        .map { Note(it.attendanceId, it.title, it.attendance, it.created) }

    override fun getAllNotes(): Flow<Either<List<Note>>> = attendancesDao.getAllNotes()
        .map { attendances -> attendances.map { Note(it.attendanceId, it.title, it.attendance, it.created) } }
        .transform { attendances -> emit(Either.success(attendances)) }
        .catch { emit(Either.success(emptyList())) }

    override suspend fun addNote(
        title: String,
        attendance: String
    ): Either<String> = runCatching {
        val tempNoteId = MnregaNoteRepository.generateTemporaryId()
        attendancesDao.addNote(
            NoteEntity(
                tempNoteId,
                title,
                attendance,
                System.currentTimeMillis()
            )
        )
        Either.success(tempNoteId)
    }.getOrDefault(Either.error("Unable to create a new attendance"))

    override suspend fun addNotes(attendances: List<Note>) = attendances.map {
        NoteEntity(it.id, it.title, it.attendance, it.created)
    }.let {
        attendancesDao.addNotes(it)
    }

    override suspend fun updateNote(
        attendanceId: String,
        title: String,
        attendance: String
    ): Either<String> = runCatching {
        attendancesDao.updateNoteById(attendanceId, title, attendance)
        Either.success(attendanceId)
    }.getOrDefault(Either.error("Unable to update a attendance"))

    override suspend fun deleteNote(attendanceId: String): Either<String> = runCatching {
        attendancesDao.deleteNoteById(attendanceId)
        Either.success(attendanceId)
    }.getOrDefault(Either.error("Unable to delete a attendance"))

    override suspend fun deleteAllNotes() = attendancesDao.deleteAllNotes()

    override suspend fun updateNoteId(oldNoteId: String, newNoteId: String) =
        attendancesDao.updateNoteId(oldNoteId, newNoteId)
}
