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

import dev.galaxius.mnrega.base.ViewModelBehaviorSpec
import dev.galaxius.mnrega.core.model.MnregaTask
import dev.galaxius.mnrega.core.model.MnregaTaskAction
import dev.galaxius.mnrega.core.repository.Either
import dev.galaxius.mnrega.core.repository.MnregaNoteRepository
import dev.galaxius.mnrega.core.task.MnregaTaskManager
import dev.galaxius.mnrega.fakes.attendance
import dev.galaxius.mnrega.testUtils.currentStateShouldBe
import dev.galaxius.mnrega.testUtils.withState
import dev.galaxius.mnrega.view.state.NoteDetailState
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import java.util.*

class NoteDetailViewModelTest : ViewModelBehaviorSpec({
    val attendance = attendance("attendance-1234")
    val repository: MnregaNoteRepository = mockk {
        coEvery { getNoteById("attendance-1234") } returns flowOf(attendance)
    }

    val scheduledTasks = mutableListOf<MnregaTask>()

    val taskManager: MnregaTaskManager = mockk {
        every { scheduleTask(capture(scheduledTasks)) } returns UUID.randomUUID()
    }

    val attendanceId = "attendance-1234"

    val viewModel = NoteDetailViewModel(taskManager, repository, attendanceId)

    Given("The ViewModel") {
        val expectedState = NoteDetailState(
            isLoading = false,
            title = "Lorem Ipsum",
            attendance = "Hey there! This is attendance content",
            showSave = false,
            finished = false,
            error = null
        )

        When("Initialized") {
            Then("Initial state should be valid") {
                viewModel currentStateShouldBe expectedState
            }
        }
    }

    Given("Note contents") {
        And("Note contents are invalid") {
            val title = "hi"
            val attendance = ""

            When("When attendance contents are set") {
                viewModel.setTitle(title)
                viewModel.setNote(attendance)

                Then("UI state should have validation details") {
                    viewModel.withState {
                        this.title shouldBe title
                        this.attendance shouldBe attendance
                        showSave shouldBe false
                    }
                }
            }
        }

        And("Note contents are valid") {
            val title = "Hey there"
            val attendance = "This is body"

            When("When attendance contents are set") {
                viewModel.setTitle(title)
                viewModel.setNote(attendance)

                Then("UI state should have validation details") {
                    viewModel.withState {
                        this.title shouldBe title
                        this.attendance shouldBe attendance
                        showSave shouldBe true
                    }
                }
            }
        }

        And("Note contents are same as existing attendance contents") {
            val title = attendance.title
            val attendance = attendance.attendance

            When("When attendance contents are set") {
                viewModel.setTitle(title)
                viewModel.setNote(attendance)

                Then("UI state should have validation details") {
                    viewModel.withState {
                        this.title shouldBe title
                        this.attendance shouldBe attendance
                        showSave shouldBe false
                    }
                }
            }
        }
    }

    Given("A attendance for updating") {
        val title = "Lorem Ipsum"
        val attendance = "Updated body of a attendance"

        viewModel.setTitle(title)
        viewModel.setNote(attendance)

        And("Note is not yet synced") {
            coEvery { repository.updateNote(attendanceId, title, attendance) } returns Either.success(
                data = "TMP_$attendanceId"
            )

            When("Note is saved") {
                viewModel.save()

                Then("Note should be get updated") {
                    coVerify { repository.updateNote(attendanceId, title, attendance) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState {
                        isLoading shouldBe false
                        finished shouldBe true
                    }
                }

                Then("Note creation should be get scheduled") {
                    scheduledTasks.last().let {
                        it.attendanceId shouldBe "TMP_$attendanceId"
                        it.action shouldBe MnregaTaskAction.CREATE
                    }
                }
            }
        }

        And("Note is synced") {
            coEvery { repository.updateNote(attendanceId, title, attendance) } returns Either.success(
                data = attendanceId
            )

            When("Note is updated") {
                viewModel.save()

                Then("Note should be get updated") {
                    coVerify { repository.updateNote(attendanceId, title, attendance) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState {
                        isLoading shouldBe false
                        finished shouldBe true
                    }
                }

                Then("Note update should be get scheduled") {
                    scheduledTasks.last().let {
                        it.attendanceId shouldBe attendanceId
                        it.action shouldBe MnregaTaskAction.UPDATE
                    }
                }
            }
        }

        And("Error occurs") {
            coEvery { repository.updateNote(attendanceId, title, attendance) } returns Either.error(
                message = "Error occurred"
            )

            When("Note is updated") {
                viewModel.save()

                Then("Note should be get updated") {
                    coVerify { repository.updateNote(attendanceId, title, attendance) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState { error shouldBe "Error occurred" }
                }
            }
        }
    }

    Given("A attendance for deletion") {
        And("Note is not yet synced") {
            coEvery { repository.deleteNote(attendanceId) } returns Either.success("TMP_$attendanceId")

            When("Note is deleted") {
                viewModel.delete()

                Then("Note should be get deleted") {
                    coVerify { repository.deleteNote(attendanceId) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState { finished shouldBe true }
                }

                Then("Note deletion should NOT be get scheduled") {
                    scheduledTasks.find {
                        it.attendanceId == "TMP_$attendanceId" && it.action == MnregaTaskAction.DELETE
                    } shouldBe null
                }
            }
        }

        And("Note is synced") {
            coEvery { repository.deleteNote(attendanceId) } returns Either.success(attendanceId)

            When("Note is deleted") {
                viewModel.delete()

                Then("Note should be get deleted") {
                    coVerify { repository.deleteNote(attendanceId) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState { finished shouldBe true }
                }

                Then("Note deletion should be get scheduled") {
                    scheduledTasks.last().let {
                        it.attendanceId shouldBe attendanceId
                        it.action shouldBe MnregaTaskAction.DELETE
                    }
                }
            }
        }

        And("Error occurs") {
            coEvery { repository.deleteNote(attendanceId) } returns Either.error("Error occurred")

            When("Note is deleted") {
                viewModel.delete()

                Then("Note should be get deleted") {
                    coVerify { repository.deleteNote(attendanceId) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState { error shouldBe "Error occurred" }
                }
            }
        }
    }
})
