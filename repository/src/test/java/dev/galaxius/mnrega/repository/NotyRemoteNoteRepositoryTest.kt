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

import com.squareup.moshi.adapter
import dev.galaxius.mnrega.core.model.Note
import dev.galaxius.mnrega.core.repository.Either
import dev.galaxius.mnrega.core.utils.moshi
import dev.galaxius.mnrega.data.remote.api.MnregaService
import dev.galaxius.mnrega.data.remote.model.request.NoteRequest
import dev.galaxius.mnrega.data.remote.model.response.NoteResponse
import dev.galaxius.mnrega.data.remote.model.response.NotesResponse
import dev.galaxius.mnrega.data.remote.model.response.State
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MnregaRemoteNoteRepositoryTest : BehaviorSpec({
    val service: FakeMnregaService = spyk(FakeMnregaService())
    val repository = MnregaRemoteNoteRepository(service)

    Given("The attendances") {
        When("Notes are retrieved") {
            And("Operation is successful") {
                service.returnSuccessOnGetAllNotes = true
                val response = repository.getAllNotes().first()

                Then("Notes should be requested") {
                    coVerify { service.getAllNotes() }
                }

                Then("Valid response should be returned") {
                    val attendances = (response as Either.Success).data
                    attendances shouldHaveSize 1
                    attendances.first() shouldBe Note("1111", "Lorem Ipsum", "Hey there", 0)
                }
            }

            And("Operation is unsuccessful") {
                service.returnSuccessOnGetAllNotes = false
                val response = repository.getAllNotes().first()

                Then("Notes should be requested") {
                    coVerify { service.getAllNotes() }
                }

                Then("Error response should be returned") {
                    val message = (response as Either.Error).message
                    message shouldBe "Failed to perform operation"
                }
            }
        }

        When("Note is added") {
            And("Inputs are valid") {
                val response = repository.addNote("Lorem Ipsum", "Hey there!")

                Then("Note addition should be requested") {
                    coVerify { service.addNote(NoteRequest("Lorem Ipsum", "Hey there!")) }
                }

                Then("Valid response should be returned") {
                    val id = (response as Either.Success).data
                    id shouldBe "1111"
                }
            }

            And("Inputs are invalid") {
                val response = repository.addNote("Test attendance", "Hey there!")

                Then("Note addition should be requested") {
                    coVerify { service.addNote(NoteRequest("Test attendance", "Hey there!")) }
                }

                Then("Error response should be returned") {
                    val message = (response as Either.Error).message
                    message shouldBe "Failed to perform operation"
                }
            }
        }

        When("Note is updated") {
            And("Inputs are valid") {
                val response = repository.updateNote(
                    attendanceId = "1111",
                    title = "Lorem Ipsum",
                    attendance = "Hey there!"
                )

                Then("Note update should be requested") {
                    coVerify {
                        service.updateNote(
                            attendanceId = "1111",
                            attendanceRequest = NoteRequest("Lorem Ipsum", "Hey there!")
                        )
                    }
                }

                Then("Valid response should be returned") {
                    val id = (response as Either.Success).data
                    id shouldBe "1111"
                }
            }

            And("Inputs are invalid") {
                val response = repository.updateNote(
                    attendanceId = "2222",
                    title = "Lorem Ipsum",
                    attendance = "Hey there!"
                )

                Then("Note update should be requested") {
                    coVerify {
                        service.updateNote(
                            attendanceId = "2222",
                            attendanceRequest = NoteRequest("Lorem Ipsum", "Hey there!")
                        )
                    }
                }

                Then("Error response should be returned") {
                    val message = (response as Either.Error).message
                    message shouldBe "Failed to perform operation"
                }
            }
        }

        When("Note is deleted") {
            And("Inputs are valid") {
                val response = repository.deleteNote(attendanceId = "1111")

                Then("Note deletion should be requested") {
                    coVerify { service.deleteNote(attendanceId = "1111") }
                }

                Then("Valid response should be returned") {
                    val id = (response as Either.Success).data
                    id shouldBe "1111"
                }
            }

            And("Inputs are invalid") {
                val response = repository.deleteNote(attendanceId = "2222")

                Then("Note deletion should be requested") {
                    coVerify {
                        service.deleteNote(attendanceId = "2222")
                    }
                }

                Then("Error response should be returned") {
                    val message = (response as Either.Error).message
                    message shouldBe "Failed to perform operation"
                }
            }
        }
    }
})

class FakeMnregaService : MnregaService {
    var returnSuccessOnGetAllNotes: Boolean = true

    override suspend fun getAllNotes(): Response<NotesResponse> {
        return fakeNotesResponse()
    }

    override suspend fun addNote(attendanceRequest: NoteRequest): Response<NoteResponse> {
        return if (attendanceRequest.title == "Lorem Ipsum") {
            fakeNoteResponse(true)
        } else {
            fakeNoteResponse(false)
        }
    }

    override suspend fun updateNote(
        attendanceId: String,
        attendanceRequest: NoteRequest
    ): Response<NoteResponse> {
        return if (attendanceId == "1111") {
            fakeNoteResponse(true)
        } else {
            fakeNoteResponse(false)
        }
    }

    override suspend fun deleteNote(attendanceId: String): Response<NoteResponse> {
        return if (attendanceId == "1111") {
            fakeNoteResponse(true)
        } else {
            fakeNoteResponse(false)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun fakeNoteResponse(success: Boolean): Response<NoteResponse> {
        return if (success) {
            Response.success(NoteResponse(State.SUCCESS, "Success", "1111"))
        } else {
            val response = NoteResponse(State.FAILED, "Failed to perform operation", null)
            val body = ResponseBody.create(
                MediaType.parse("application/json"),
                moshi.adapter<NoteResponse>().toJson(response)
            )
            Response.error(400, body)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun fakeNotesResponse(): Response<NotesResponse> {
        return if (returnSuccessOnGetAllNotes) {
            Response.success(
                NotesResponse(
                    status = State.SUCCESS,
                    message = "Success",
                    attendances = listOf(Note("1111", "Lorem Ipsum", "Hey there", 0))
                )
            )
        } else {
            val response = NotesResponse(State.FAILED, "Failed to perform operation", emptyList())
            val body = ResponseBody.create(
                MediaType.parse("application/json"),
                moshi.adapter<NotesResponse>().toJson(response)
            )
            Response.error(400, body)
        }
    }
}
