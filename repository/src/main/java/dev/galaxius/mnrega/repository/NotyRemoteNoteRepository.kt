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

package dev.galaxius.mnrega.repository

import dev.galaxius.mnrega.core.model.Note
import dev.galaxius.mnrega.core.repository.MnregaNoteRepository
import dev.galaxius.mnrega.core.repository.Either
import dev.galaxius.mnrega.data.remote.api.MnregaService
import dev.galaxius.mnrega.data.remote.model.request.NoteRequest
import dev.galaxius.mnrega.data.remote.model.response.State
import dev.galaxius.mnrega.data.remote.util.getResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Source of data of attendances from network
 */
@Singleton
class MnregaRemoteNoteRepository @Inject internal constructor(
    private val mnregaService: MnregaService
) : MnregaNoteRepository {

    override fun getAllNotes(): Flow<Either<List<Note>>> = flow {
        val attendancesResponse = mnregaService.getAllNotes().getResponse()

        val state = when (attendancesResponse.status) {
            State.SUCCESS -> Either.success(attendancesResponse.attendances)
            else -> Either.error(attendancesResponse.message)
        }

        emit(state)
    }.catch { emit(Either.error("Can't sync latest attendances")) }

    override suspend fun addNote(title: String, attendance: String): Either<String> {
        return runCatching {
            val attendancesResponse = mnregaService.addNote(NoteRequest(title, attendance)).getResponse()

            when (attendancesResponse.status) {
                State.SUCCESS -> Either.success(attendancesResponse.attendanceId!!)
                else -> Either.error(attendancesResponse.message)
            }
        }.getOrElse {
            it.printStackTrace()
            (Either.error("Something went wrong!"))
        }
    }

    override suspend fun updateNote(
        attendanceId: String,
        title: String,
        attendance: String
    ): Either<String> {
        return runCatching {
            val attendancesResponse = mnregaService.updateNote(
                attendanceId,
                NoteRequest(title, attendance)
            ).getResponse()

            when (attendancesResponse.status) {
                State.SUCCESS -> Either.success(attendancesResponse.attendanceId!!)
                else -> Either.error(attendancesResponse.message)
            }
        }.getOrDefault(Either.error("Something went wrong!"))
    }

    override suspend fun deleteNote(attendanceId: String): Either<String> {
        return runCatching {
            val attendancesResponse = mnregaService.deleteNote(attendanceId).getResponse()

            when (attendancesResponse.status) {
                State.SUCCESS -> Either.success(attendancesResponse.attendanceId!!)
                else -> Either.error(attendancesResponse.message)
            }
        }.getOrDefault(Either.error("Something went wrong!"))
    }

    /** Not needed (NO-OP) **/
    override fun getNoteById(attendanceId: String): Flow<Note> = emptyFlow()

    /** Not needed (NO-OP) **/
    override suspend fun addNotes(attendances: List<Note>) {}

    /** Not needed (NO-OP) **/
    override suspend fun deleteAllNotes() {}

    /** Not needed (NO-OP) **/
    override suspend fun updateNoteId(oldNoteId: String, newNoteId: String) {}
}
