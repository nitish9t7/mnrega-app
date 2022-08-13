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
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import dagger.hilt.android.testing.HiltAndroidTest
import dev.galaxius.mnrega.composeapp.MnregaScreenTest
import dev.galaxius.mnrega.core.model.Note
import dev.galaxius.mnrega.core.repository.MnregaNoteRepository
import dev.galaxius.mnrega.core.session.SessionManager
import dev.galaxius.mnrega.di.LocalRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

@DelicateCoroutinesApi
@HiltAndroidTest
class NotesScreenTest : MnregaScreenTest() {

    @LocalRepository
    @Inject
    lateinit var attendanceRepository: MnregaNoteRepository

    @Inject
    lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        inject()
        // Mock fake authentication
        sessionManager.saveToken("Bearer ABCD")
    }

    @Test
    fun navigateToLogin_whenUserNotExistsInSession() = runTest {
        // Remove user from session
        sessionManager.saveToken(null)

        var navigatingToLogin = false
        setMnregaContent {
            NotesScreen(onNavigateToLogin = { navigatingToLogin = true })
        }

        waitForIdle()

        assertTrue(navigatingToLogin)
    }

    @Test
    fun navigateToAbout_onClickAboutIcon() = runTest {
        var navigatingToAbout = false
        setMnregaContent {
            NotesScreen(onNavigateToAbout = { navigatingToAbout = true })
        }

        onNodeWithContentDescription("About").performClick()
        waitForIdle()
        assertTrue(navigatingToAbout)
    }

    @Test
    fun navigateToAddNote_onClickAddIcon() = runTest {
        var navigateToAddNote = false
        setMnregaContent {
            NotesScreen(onNavigateToAddNote = { navigateToAddNote = true })
        }

        onNodeWithContentDescription("Add").performClick()
        waitForIdle()
        assertTrue(navigateToAddNote)
    }

    @Test
    fun navigateToLogin_onClickLogoutIconAndConfirmedLogin() = runTest {
        var navigatingToLogin = false
        setMnregaContent {
            NotesScreen(onNavigateToLogin = { navigatingToLogin = true })
        }

        val logoutNode = onNodeWithContentDescription("Logout")
        logoutNode.performClick()

        // Should show confirmation dialog
        onNodeWithText("Logout?").assertIsDisplayed()
        onNodeWithText("Sure want to logout?").assertIsDisplayed()

        // Confirm logout
        onNodeWithText("Yes").performClick()
        waitForIdle()
        assertTrue(navigatingToLogin)
    }

    @Test
    fun shouldNotNavigateToLogin_onClickLogoutIconAndDeniedLogin() = runTest {
        var navigatingToLogin = false
        setMnregaContent {
            NotesScreen(onNavigateToLogin = { navigatingToLogin = true })
        }

        val logoutNode = onNodeWithContentDescription("Logout")
        logoutNode.performClick()

        // Should show confirmation dialog
        onNodeWithText("Logout?").assertIsDisplayed()
        onNodeWithText("Sure want to logout?").assertIsDisplayed()

        // Confirm logout
        onNodeWithText("No").performClick()
        waitForIdle()
        assertFalse(navigatingToLogin)
    }

    @Test
    fun showNotes_whenNotesAreLoaded() = runTest {
        setMnregaContent { NotesScreen() }
        registerIdlingResource(prefillNotes())

        waitForIdle()
        onNodeWithTag("attendancesList").performScrollToIndex(0)
        waitForIdle()

        val attendances = attendances()
        onNodeWithText(attendances.first().title).assertExists()
        onNodeWithText(attendances.first().attendance).assertExists()

        // Since it's LazyColumn, last attendance should not exist
        onNodeWithText(attendances.last().title).assertDoesNotExist()
        onNodeWithText(attendances.last().attendance).assertDoesNotExist()

        // Perform scrolling till end
        onNodeWithTag("attendancesList").performScrollToIndex(49)

        waitForIdle()

        // After scrolling, last item should be displayed
        onNodeWithText(attendances.last().title).assertIsDisplayed()
        onNodeWithText(attendances.last().attendance).assertIsDisplayed()
    }

    @Test
    fun showNoteOnRealtime_whenNewNoteIsAddedOrDeleted() = runTest {
        setMnregaContent { NotesScreen() }

        registerIdlingResource(prefillNotes())
        registerIdlingResource(addNote(title = "New Note", attendance = "Hey there!"))

        waitForIdle()
        onNodeWithTag("attendancesList").performScrollToIndex(0)
        waitForIdle()

        // Newly added attendance should be displayed on UI
        onNodeWithText("New Note").assertIsDisplayed()
        onNodeWithText("Hey there!").assertIsDisplayed()

        registerIdlingResource(deleteNote("1"))

        // Deleted attendance should not exist
        onNodeWithText("Lorem Ipsum 1").assertDoesNotExist()
        onNodeWithText("Hello World 1").assertDoesNotExist()
    }

    @Test
    fun navigateToNoteDetail_onClickingNoteContent() = runTest {
        var navigateToNoteId: String? = null

        setMnregaContent { NotesScreen(onNavigateToNoteDetail = { navigateToNoteId = it }) }
        registerIdlingResource(prefillNotes())

        onNodeWithText("Lorem Ipsum 1").performClick()
        waitForIdle()
        assertEquals("1", navigateToNoteId)

        onNodeWithText("Hello World 2").performClick()
        waitForIdle()
        assertEquals("2", navigateToNoteId)
    }

    @Composable
    private fun NotesScreen(
        onNavigateToAbout: () -> Unit = {},
        onNavigateToAddNote: () -> Unit = {},
        onNavigateToNoteDetail: (String) -> Unit = {},
        onNavigateToLogin: () -> Unit = {}
    ) {
        NotesScreen(
            viewModel = viewModel(),
            onNavigateToAbout = onNavigateToAbout,
            onNavigateToAddNote = onNavigateToAddNote,
            onNavigateToNoteDetail = onNavigateToNoteDetail,
            onNavigateToLogin = onNavigateToLogin
        )
    }

    private fun attendances(): List<Note> {
        val title = "Lorem Ipsum"
        val attendance = "Hello World"
        val currentTime = System.currentTimeMillis()

        return (1..50).map {
            Note(
                id = it.toString(),
                title = "$title $it",
                attendance = "$attendance $it",
                created = currentTime - it.toLong()
            )
        }
    }

    @After
    fun tearDown() = runBlocking { attendanceRepository.deleteAllNotes() }

    private fun prefillNotes() = addNotes(attendances())

    @Suppress("SameParameterValue")
    private fun addNote(title: String, attendance: String) = object : IdlingResource {
        override var isIdleNow: Boolean = false

        init {
            GlobalScope.launch {
                attendanceRepository.addNote(title, attendance)
                delay(1_000)
                isIdleNow = true
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun deleteNote(id: String) = object : IdlingResource {
        override var isIdleNow: Boolean = false

        init {
            GlobalScope.launch {
                attendanceRepository.deleteNote(id)
                delay(1_000)
                isIdleNow = true
            }
        }
    }

    private fun addNotes(attendances: List<Note>) = object : IdlingResource {
        override var isIdleNow: Boolean = false

        init {
            GlobalScope.launch {
                attendanceRepository.addNotes(attendances)
                delay(1000)
                isIdleNow = true
            }
        }
    }
}
