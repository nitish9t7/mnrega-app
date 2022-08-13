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
import dev.galaxius.mnrega.core.connectivity.ConnectionState
import dev.galaxius.mnrega.core.connectivity.ConnectivityObserver
import dev.galaxius.mnrega.core.model.Note
import dev.galaxius.mnrega.core.preference.PreferenceManager
import dev.galaxius.mnrega.core.repository.Either
import dev.galaxius.mnrega.core.repository.MnregaNoteRepository
import dev.galaxius.mnrega.core.session.SessionManager
import dev.galaxius.mnrega.core.task.MnregaTaskManager
import dev.galaxius.mnrega.core.task.TaskState
import dev.galaxius.mnrega.fakes.attendance
import dev.galaxius.mnrega.testUtils.currentStateShouldBe
import dev.galaxius.mnrega.testUtils.withState
import dev.galaxius.mnrega.view.state.NotesState
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import java.util.*

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest : ViewModelBehaviorSpec({
    val fakeNotesFlow = MutableSharedFlow<Either<List<Note>>>(replay = 1)

    val repository: MnregaNoteRepository = mockk(relaxUnitFun = true) {
        every { getAllNotes() } returns fakeNotesFlow
    }
    val sessionManager: SessionManager = mockk(relaxUnitFun = true) {
        every { getToken() } returns "TEST_TOKEN"
    }
    val preferenceManager: PreferenceManager = mockk(relaxUnitFun = true)
    val taskManager: MnregaTaskManager = mockk(relaxUnitFun = true)
    val connectivityObserver = spyk(FakeConnectivityObserver())

    val viewModel = NotesViewModel(
        mnregaNoteRepository = repository,
        sessionManager = sessionManager,
        preferenceManager = preferenceManager,
        mnregaTaskManager = taskManager,
        connectivityObserver = connectivityObserver
    )

    Given("The ViewModel") {
        val initialNotes = listOf(Note("NOTE_ID", "Lorem Ipsum", "Note text", 0))
        fakeNotesFlow.emit(Either.success(initialNotes))

        When("Initialized") {
            val expectedState = NotesState(
                isLoading = false,
                attendances = initialNotes,
                error = null,
                isUserLoggedIn = true,
                isConnectivityAvailable = true
            )

            Then("Initial state should be valid") {
                viewModel currentStateShouldBe expectedState
            }

            Then("Current session should be get checked") {
                verify { sessionManager.getToken() }
            }

            Then("Notes should be get synced") {
                verify { taskManager.syncNotes() }
            }
        }
    }

    Given("The list of attendances") {
        val attendances = listOf(attendance("1"), attendance("2"), attendance("3"))

        When("The attendances are updated successfully") {
            fakeNotesFlow.emit(Either.success(attendances))

            Then("Notes should be updated in the state") {
                viewModel.withState {
                    this.attendances shouldBe attendances
                    isLoading shouldBe false
                }
            }
        }

        When("The attendances are updated with failure") {
            fakeNotesFlow.emit(Either.error("Error occurred"))

            Then("Notes should be updated in the state") {
                viewModel.withState {
                    this.error shouldBe "Error occurred"
                    isLoading shouldBe false
                }
            }
        }
    }

    Given("The connectivity") {
        When("The connectivity is available") {
            connectivityObserver.fakeConnectionFlow.value = ConnectionState.Available

            Then("The UI state should have connectivity state updated") {
                viewModel.withState {
                    isConnectivityAvailable shouldBe true
                }
            }
        }

        When("The connectivity is unavailable") {
            connectivityObserver.fakeConnectionFlow.value = ConnectionState.Unavailable

            Then("The UI state should have connectivity state updated") {
                viewModel.withState {
                    isConnectivityAvailable shouldBe false
                }
            }
        }
    }

    Given("Notes available for syncing") {
        When("Sync for attendances is requested") {

            And("Sync is successful") {
                val taskId = UUID.randomUUID()
                every { taskManager.syncNotes() } returns taskId
                every { taskManager.observeTask(taskId) } returns flowOf(
                    TaskState.SCHEDULED,
                    TaskState.COMPLETED
                )

                viewModel.syncNotes()

                Then("UI state should be get updated") {
                    viewModel.withState { isLoading shouldBe false }
                }
            }

            And("Sync is failed") {
                val taskId = UUID.randomUUID()
                every { taskManager.syncNotes() } returns taskId
                every { taskManager.observeTask(taskId) } returns flowOf(
                    TaskState.SCHEDULED,
                    TaskState.FAILED
                )

                viewModel.syncNotes()

                Then("UI state should be get updated") {
                    viewModel.withState {
                        isLoading shouldBe false
                        error shouldBe "Failed to sync attendances"
                    }
                }
            }
        }
    }

    Given("A UI mode") {
        val expectedUiMode = true

        coEvery { preferenceManager.uiModeFlow } returns flowOf(expectedUiMode)

        When("UI mode is changed") {
            viewModel.setDarkMode(expectedUiMode)

            Then("Preference should be get saved") {
                coVerify { preferenceManager.setDarkMode(expectedUiMode) }
            }
        }

        When("Current UI mode is retrieved") {
            val actualUiMode = viewModel.isDarkModeEnabled()

            Then("Correct UI mode should be get returned") {
                actualUiMode shouldBe expectedUiMode
            }
        }
    }

    Given("User session") {
        When("Session is cleared") {
            viewModel.logout()

            Then("Token should get reset") {
                verify { sessionManager.saveToken(null) }
            }

            Then("All scheduled tasks should be get aborted") {
                verify { taskManager.abortAllTasks() }
            }

            Then("All attendances should be get cleared") {
                coVerify { repository.deleteAllNotes() }
            }

            Then("User logged in state should be changed to not logged in") {
                viewModel.withState { isUserLoggedIn shouldBe false }
            }
        }
    }
})

class FakeConnectivityObserver : ConnectivityObserver {
    val fakeConnectionFlow = MutableStateFlow<ConnectionState>(ConnectionState.Available)

    override val connectionState: Flow<ConnectionState> = fakeConnectionFlow
    override var currentConnectionState: ConnectionState = ConnectionState.Available
}
