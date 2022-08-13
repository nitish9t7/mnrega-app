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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dev.galaxius.mnrega.composeapp.component.ConnectivityStatus
import dev.galaxius.mnrega.composeapp.component.action.AboutAction
import dev.galaxius.mnrega.composeapp.component.action.LogoutAction
import dev.galaxius.mnrega.composeapp.component.action.ThemeSwitchAction
import dev.galaxius.mnrega.composeapp.component.dialog.ConfirmationDialog
import dev.galaxius.mnrega.composeapp.component.attendance.NotesList
import dev.galaxius.mnrega.composeapp.component.scaffold.MnregaScaffold
import dev.galaxius.mnrega.composeapp.component.scaffold.MnregaTopAppBar
import dev.galaxius.mnrega.composeapp.utils.collectState
import dev.galaxius.mnrega.core.model.Note
import dev.galaxius.mnrega.view.viewmodel.NotesViewModel

@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onNavigateToAbout: () -> Unit,
    onNavigateToAddNote: () -> Unit,
    onNavigateToNoteDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.collectState()

    val isInDarkMode = isSystemInDarkTheme()

    var showLogoutConfirmation by remember { mutableStateOf(false) }

    NotesContent(
        isLoading = state.isLoading,
        attendances = state.attendances,
        isConnectivityAvailable = state.isConnectivityAvailable,
        onRefresh = viewModel::syncNotes,
        onToggleTheme = { viewModel.setDarkMode(!isInDarkMode) },
        onAboutClick = onNavigateToAbout,
        onAddNoteClick = onNavigateToAddNote,
        onLogoutClick = { showLogoutConfirmation = true },
        onNavigateToNoteDetail = onNavigateToNoteDetail
    )

    LogoutConfirmation(
        show = showLogoutConfirmation,
        onConfirm = viewModel::logout,
        onDismiss = { showLogoutConfirmation = false }
    )

    val isUserLoggedIn = state.isUserLoggedIn
    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn == false) {
            onNavigateToLogin()
        }
    }
}

@Composable
fun NotesContent(
    isLoading: Boolean,
    attendances: List<Note>,
    isConnectivityAvailable: Boolean?,
    error: String? = null,
    onRefresh: () -> Unit,
    onToggleTheme: () -> Unit,
    onAboutClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToNoteDetail: (String) -> Unit
) {
    MnregaScaffold(
        error = error,
        mnregaTopAppBar = {
            MnregaTopAppBar(
                actions = {
                    ThemeSwitchAction(onToggleTheme)
                    AboutAction(onAboutClick)
                    LogoutAction(onLogout = onLogoutClick)
                }
            )
        },
        content = {
            SwipeRefresh(
                modifier = Modifier.fillMaxSize(),
                state = rememberSwipeRefreshState(isLoading),
                onRefresh = onRefresh,
                swipeEnabled = isConnectivityAvailable == true
            ) {
                Column {
                    if (isConnectivityAvailable != null) {
                        ConnectivityStatus(isConnectivityAvailable)
                    }
                    NotesList(attendances) { attendance -> onNavigateToNoteDetail(attendance.id) }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(
                    Icons.Filled.Add,
                    "Add",
                    tint = Color.White
                )
            }
        }
    )
}

@Composable
fun LogoutConfirmation(show: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    if (show) {
        ConfirmationDialog(
            title = "Logout?",
            message = "Sure want to logout?",
            onConfirmedYes = onConfirm,
            onConfirmedNo = onDismiss,
            onDismissed = onDismiss
        )
    }
}
