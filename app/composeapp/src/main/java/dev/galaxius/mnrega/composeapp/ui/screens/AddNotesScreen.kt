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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.galaxius.mnrega.composeapp.component.scaffold.MnregaScaffold
import dev.galaxius.mnrega.composeapp.component.scaffold.MnregaTopAppBar
import dev.galaxius.mnrega.composeapp.component.text.NoteField
import dev.galaxius.mnrega.composeapp.component.text.NoteTitleField
import dev.galaxius.mnrega.composeapp.utils.collectState
import dev.galaxius.mnrega.view.viewmodel.AddNoteViewModel

@Composable
fun AddNoteScreen(
    viewModel: AddNoteViewModel,
    onNavigateUp: () -> Unit
) {
    val state by viewModel.collectState()

    AddNotesContent(
        isLoading = state.isAdding,
        title = state.title,
        attendance = state.attendance,
        showSaveFab = state.showSave,
        onTitleChange = viewModel::setTitle,
        onNoteChange = viewModel::setNote,
        onClickAddNote = viewModel::add,
        error = state.errorMessage,
        onNavigateUp = onNavigateUp
    )

    LaunchedEffect(state.added) {
        if (state.added) {
            onNavigateUp()
        }
    }
}

@Composable
fun AddNotesContent(
    isLoading: Boolean,
    title: String,
    attendance: String,
    showSaveFab: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onClickAddNote: () -> Unit,
    error: String?,
    onNavigateUp: () -> Unit
) {
    MnregaScaffold(
        isLoading = isLoading,
        error = error,
        mnregaTopAppBar = { MnregaTopAppBar(title = "Add Attendance", onNavigateUp = onNavigateUp) },
        content = {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                NoteTitleField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.background),
                    value = title,
                    onTextChange = onTitleChange,
                )

                NoteField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(top = 32.dp)
                        .background(MaterialTheme.colors.background),
                    value = attendance,
                    onTextChange = onNoteChange
                )
            }
        },
        floatingActionButton = {
            if (showSaveFab) {
                ExtendedFloatingActionButton(
                    text = { Text("Save", color = Color.White) },
                    icon = {
                        Icon(
                            Icons.Filled.Done,
                            "Save",
                            tint = Color.White
                        )
                    },
                    onClick = onClickAddNote,
                    backgroundColor = MaterialTheme.colors.primary
                )
            }
        }
    )
}
