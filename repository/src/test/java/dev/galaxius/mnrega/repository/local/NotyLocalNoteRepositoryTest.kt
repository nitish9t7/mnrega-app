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
import dev.galaxius.mnrega.core.repository.Either.Error
import dev.galaxius.mnrega.core.repository.Either.Success
import dev.galaxius.mnrega.data.local.dao.NotesDao
import dev.galaxius.mnrega.data.local.entity.NoteEntity
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import java.util.*

class MnregaLocalNoteRepositoryTest : BehaviorSpec({
    val attendancesDao: NotesDao = mockk(relaxUnitFun = true)
    val repository = MnregaLocalNoteRepository(attendancesDao)

    Given("Notes for addition") {
        val attendance = Note(
            id = "UNIQUE_ID",
            title = "Lorem Ipsum",
            attendance = "This is body of a attendance!",
            created = Date().time
        )

        val expectedEntity = NoteEntity(attendance.id, attendance.title, attendance.attendance, attendance.created)

        When("Note is added") {
            And("DAO can add attendance") {
                val response = repository.addNote(attendance.title, attendance.attendance)
                val attendanceId = (response as Success).data

                Then("Temporary attendance ID should be returned") {
                    attendanceId shouldStartWith "TMP"
                }

                Then("Note should be get added in DAO") {
                    val actualNoteEntity = slot<NoteEntity>()

                    coVerify { attendancesDao.addNote(capture(actualNoteEntity)) }

                    with(actualNoteEntity.captured) {
                        this.title shouldBe expectedEntity.title
                        this.attendance shouldBe expectedEntity.attendance
                    }
                }
            }

            And("DAO cannot add attendance") {
                coEvery { attendancesDao.addNote(any()) } throws Exception("")

                val response = repository.addNote(attendance.title, attendance.attendance)

                Then("Error response should be returned") {
                    (response as Error).message shouldBe "Unable to create a new attendance"
                }
            }
        }

        When("Notes are added in bulk") {
            repository.addNotes(listOf(attendance))

            Then("Notes should be get added in DAO") {
                coVerify { attendancesDao.addNotes(listOf(expectedEntity)) }
            }
        }
    }

    Given("A attendance") {
        val attendanceEntity = NoteEntity(
            attendanceId = "UNIQUE_ID",
            title = "Lorem Ipsum",
            attendance = "This is body of a attendance!",
            created = Date().time
        )

        When("Note is observed") {
            coEvery { attendancesDao.getNoteById(attendanceEntity.attendanceId) } returns flowOf(attendanceEntity)

            val actualNote = repository.getNoteById(attendanceEntity.attendanceId)

            Then("Note should be returned") {
                with(actualNote.first()) {
                    this.id shouldBe attendanceEntity.attendanceId
                    this.title shouldBe attendanceEntity.title
                    this.attendance shouldBe attendanceEntity.attendance
                    this.created shouldBe attendanceEntity.created
                }
            }
        }

        When("Note is updated") {
            val newTitle = "New title"
            val newNote = "New attendance body"

            And("DAO can update attendance") {
                coEvery { attendancesDao.updateNoteById(any(), any(), any()) } just Runs

                repository.updateNote(attendanceEntity.attendanceId, newTitle, newNote)

                Then("Note should be get updated in DAO") {
                    coVerify { attendancesDao.updateNoteById(attendanceEntity.attendanceId, newTitle, newNote) }
                }
            }

            And("DAO can NOT update attendance") {
                coEvery { attendancesDao.updateNoteById(any(), any(), any()) } throws Exception()

                val response = repository.updateNote(attendanceEntity.attendanceId, newTitle, newNote)

                Then("Error response should be returned") {
                    (response as Error).message shouldBe "Unable to update a attendance"
                }
            }
        }

        When("Note ID is updated") {
            val newNoteId = "NEW_NOTE_ID"
            repository.updateNoteId(oldNoteId = attendanceEntity.attendanceId, newNoteId = newNoteId)

            Then("Note ID should be get updated in DAO") {
                coVerify { attendancesDao.updateNoteId(attendanceEntity.attendanceId, newNoteId) }
            }
        }

        When("Note is deleted") {
            And("DAO can delete attendance") {
                coEvery { attendancesDao.deleteNoteById(any()) } just Runs

                repository.deleteNote(attendanceEntity.attendanceId)

                Then("Note should be get deleted in DAO") {
                    coVerify { attendancesDao.deleteNoteById(attendanceEntity.attendanceId) }
                }
            }

            And("DAO can NOT delete attendance") {
                coEvery { attendancesDao.deleteNoteById(any()) } throws Exception()

                val response = repository.deleteNote(attendanceEntity.attendanceId)

                Then("Error response should be returned") {
                    (response as Error).message shouldBe "Unable to delete a attendance"
                }
            }
        }
    }

    Given("All attendances") {
        val attendance = NoteEntity("ID", "Title", "Note", 0)
        val attendanceEntities = listOf(attendance.copy(attendanceId = "1"), attendance.copy(attendanceId = "2"))

        When("Notes are observed") {
            coEvery { attendancesDao.getAllNotes() } returns flowOf(attendanceEntities)

            val attendances = repository.getAllNotes().first()

            Then("All attendances should be retrieved") {
                (attendances as Success).data shouldBe attendanceEntities.map {
                    Note(it.attendanceId, it.title, it.attendance, it.created)
                }
            }
        }

        When("Notes are deleted in bulk") {
            repository.deleteAllNotes()

            Then("All attendances should be get deleted in DAO") {
                coVerify { attendancesDao.deleteAllNotes() }
            }
        }
    }
})
