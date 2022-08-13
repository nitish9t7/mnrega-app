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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidTest
import dev.galaxius.mnrega.composeapp.MnregaScreenTest
import org.junit.Assert.assertTrue
import org.junit.Test

@HiltAndroidTest
class AddNotesScreenTest : MnregaScreenTest() {

    @Test
    fun navigateUp_onClickBackIcon() = runTest {
        var navigatingUp = false
        setMnregaContent { AddNoteScreen(onNavigateUp = { navigatingUp = true }) }

        onNodeWithContentDescription("Back").performClick()

        waitForIdle()

        assertTrue(navigatingUp)
    }

    @Test
    fun doNotShowAddButton_onInvalidNoteContentInput() = runTest {
        setMnregaContent { AddNoteScreen() }

        // We only show save button when title as at least has 4 characters
        onNodeWithText("Title").performTextInput("Hi")

        onNodeWithText("Save").assertDoesNotExist()
    }

    @Test
    fun showAddButton_onValidNoteContentInput() = runTest {
        setMnregaContent { AddNoteScreen() }

        onNodeWithText("Title").performTextInput("Hi there")
        onNodeWithText("Write attendance here").performTextInput("Hi there, this is a attendance")

        onNodeWithText("Save").assertIsDisplayed()
    }

    @Test
    fun navigateUp_onSuccessfullyAddingNote() = runTest {
        var navigatingUp = false
        setMnregaContent { AddNoteScreen(onNavigateUp = { navigatingUp = true }) }

        onNodeWithText("Title").performTextInput("Hi there")
        onNodeWithText("Write attendance here").performTextInput("Hi there, this is a attendance")

        waitForIdle()
        onNodeWithText("Save").performClick()
        waitForIdle()

        assertTrue(navigatingUp)
    }

    @Composable
    private fun AddNoteScreen(onNavigateUp: () -> Unit = {}) {
        AddNoteScreen(
            viewModel = viewModel(),
            onNavigateUp = onNavigateUp
        )
    }
}
