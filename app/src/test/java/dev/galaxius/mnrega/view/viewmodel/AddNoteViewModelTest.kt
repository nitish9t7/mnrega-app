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
import dev.galaxius.mnrega.testUtils.currentStateShouldBe
import dev.galaxius.mnrega.testUtils.withState
import dev.galaxius.mnrega.view.state.AddNoteState
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class AddNoteViewModelTest : ViewModelBehaviorSpec({

    val repository: MnregaNoteRepository = mockk(relaxUnitFun = true)
    val taskManager: MnregaTaskManager = mockk(relaxUnitFun = true) {
        every { scheduleTask(any()) } returns UUID.randomUUID()
    }

    val viewModel = AddNoteViewModel(repository, taskManager)

    Given("The ViewModel") {
        val expectedState = AddNoteState(
            title = "",
            attendance = "",
            showSave = false,
            isAdding = false,
            added = false,
            errorMessage = null
        )

        When("Initialized") {
            Then("Initial state should be valid") {
                viewModel currentStateShouldBe expectedState
            }
        }

        When("The state is reset") {
            viewModel.resetState()

            Then("State should be valid") {
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
    }

    Given("A attendance for addition") {
        val title = "Lorem Ipsum"
        val attendance = "Hey there, this is not content"

        viewModel.setTitle(title)
        viewModel.setNote(attendance)

        And("Note addition is successful") {
            coEvery { repository.addNote(title, attendance) } returns Either.success("attendance-11")

            When("Note is added") {
                viewModel.add()

                Then("Note states should be valid") {
                    viewModel.withState {
                        isAdding shouldBe false
                        added shouldBe true
                        errorMessage shouldBe null
                    }
                }

                Then("Note creation task should be get scheduled") {
                    val actualTask = slot<MnregaTask>()
                    verify { taskManager.scheduleTask(capture(actualTask)) }

                    actualTask.captured.let {
                        it.attendanceId shouldBe "attendance-11"
                        it.action shouldBe MnregaTaskAction.CREATE
                    }
                }
            }
        }

        And("Note addition is failed") {
            clearAllMocks()

            coEvery { repository.addNote(title, attendance) } returns Either.error("Failed")

            When("Note is added") {
                viewModel.add()

                Then("Note states should be valid") {
                    viewModel.withState {
                        println("ThisStateIs: $this")
                        isAdding shouldBe false
                        added shouldBe false
                        errorMessage shouldBe "Failed"
                    }
                }

                Then("Note creation task should NOT be get scheduled") {
                    verify(exactly = 0) { taskManager.scheduleTask(any()) }
                }
            }
        }
    }
})
