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

package dev.galaxius.mnrega.composeapp.fake.service

import dev.galaxius.mnrega.composeapp.testUtil.successResponse
import dev.galaxius.mnrega.data.remote.api.MnregaService
import dev.galaxius.mnrega.data.remote.model.request.NoteRequest
import dev.galaxius.mnrega.data.remote.model.response.NoteResponse
import dev.galaxius.mnrega.data.remote.model.response.NotesResponse
import dev.galaxius.mnrega.data.remote.model.response.State
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Path
import javax.inject.Inject

/**
 * Fake implementation for remote attendance Service.
 */
class FakeMnregaService @Inject constructor() : MnregaService {

    override suspend fun getAllNotes(): Response<NotesResponse> {
        return successResponse(NotesResponse(State.SUCCESS, "", emptyList()))
    }

    override suspend fun addNote(@Body attendanceRequest: NoteRequest): Response<NoteResponse> {
        // Do nothing, just return success
        return successResponse(NoteResponse(State.SUCCESS, "", ""))
    }

    override suspend fun updateNote(
        @Path(value = "attendanceId") attendanceId: String,
        @Body attendanceRequest: NoteRequest
    ): Response<NoteResponse> {
        // Do nothing, just return success
        return successResponse(NoteResponse(State.SUCCESS, "", ""))
    }

    override suspend fun deleteNote(attendanceId: String): Response<NoteResponse> {
        // Do nothing, just return success
        return successResponse(NoteResponse(State.SUCCESS, "", ""))
    }
}
