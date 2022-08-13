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

package dev.galaxius.mnrega.composeapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.IdlingResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidTest
import dev.galaxius.mnrega.composeapp.MnregaScreenTest
import dev.galaxius.mnrega.composeapp.utils.assistedViewModel
import dev.galaxius.mnrega.core.model.Note
import dev.galaxius.mnrega.core.repository.MnregaNoteRepository
import dev.galaxius.mnrega.di.LocalRepository
import dev.galaxius.mnrega.view.viewmodel.NoteDetailViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class NoteDetailsScreenTest : MnregaScreenTest() {

    @LocalRepository
    @Inject
    lateinit var attendanceRepository: MnregaNoteRepository

    @Before
    fun setUp() {
        inject()
        composeTestRule.registerIdlingResource(prepopulateNote())
    }

    @Test
    fun navigateUp_onClickBackIcon() = runTest {
        var navigatingUp = false
        setMnregaContent { NoteDetailScreen(onNavigateUp = { navigatingUp = true }) }

        onNodeWithContentDescription("Back").performClick()

        assertTrue(navigatingUp)
    }

    @Test
    fun hideSaveButton_onInvalidNoteContentInput() = runTest {
        setMnregaContent { NoteDetailScreen() }
        waitForIdle()

        // We only show save button when title as at least has 4 characters
        onNodeWithText("Lorem Ipsum").performTextClearance()
        waitForIdle()
        onNodeWithText("Title").performTextInput("Hi")

        waitForIdle()
        onNodeWithText("Hey there").performTextClearance()

        onNodeWithText("Save").assertDoesNotExist()
    }

    @Test
    fun hideSaveButton_whenEditedContentIsSameAsPreviouslySavedContent() = runTest {
        setMnregaContent { NoteDetailScreen() }
        waitForIdle()

        onNodeWithText("Lorem Ipsum").performTextClearance()
        waitForIdle()

        onNodeWithText("Hey there").performTextClearance()
        waitForIdle()

        onNodeWithText("Title").performTextInput("Lorem Ipsum")
        onNodeWithText("Write attendance here").performTextInput("Hey there")

        onNodeWithText("Save").assertDoesNotExist()
    }

    @Test
    fun showSaveButton_whenEditedContentIsNotSameAsPreviouslySavedContent() = runTest {
        setMnregaContent { NoteDetailScreen() }
        waitForIdle()

        onNodeWithText("Lorem Ipsum").performTextClearance()
        waitForIdle()

        onNodeWithText("Hey there").performTextClearance()
        waitForIdle()

        onNodeWithText("Title").performTextInput("Lorem Ipsum Edited")
        onNodeWithText("Write attendance here").performTextInput("Hey there, this is edited")

        waitForIdle()

        onNodeWithText("Save").assertIsDisplayed()
    }

    @Test
    fun navigateUp_whenNoteIsUpdatedSuccessfully() = runTest {
        var navigatingUp = false
        setMnregaContent { NoteDetailScreen(onNavigateUp = { navigatingUp = true }) }
        waitForIdle()

        onNodeWithText("Lorem Ipsum").performTextInput("Hey there")
        waitForIdle()

        onNodeWithText("Hey there").performTextInput("Lorem Ipsum")
        waitForIdle()

        onNodeWithText("Save").performClick()
        waitForIdle()

        assertTrue(navigatingUp)
    }

    @Composable
    private fun NoteDetailScreen(onNavigateUp: () -> Unit = {}) {
        NoteDetailsScreen(
            viewModel = assistedViewModel {
                NoteDetailViewModel.provideFactory(attendanceDetailViewModelFactory(), "1")
            },
            onNavigateUp = onNavigateUp
        )
    }

    private fun prepopulateNote() = object : IdlingResource {
        override var isIdleNow: Boolean = false

        init {
            val attendance = Note(
                id = "1",
                title = "Lorem Ipsum",
                attendance = "Hey there",
                created = System.currentTimeMillis()
            )
            GlobalScope.launch {
                attendanceRepository.addNotes(listOf(attendance))
                isIdleNow = true
            }
        }
    }
}
