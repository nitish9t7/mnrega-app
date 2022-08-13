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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.galaxius.mnrega.composeapp.component.action.DeleteAction
import dev.galaxius.mnrega.composeapp.component.action.ShareAction
import dev.galaxius.mnrega.composeapp.component.action.ShareActionItem
import dev.galaxius.mnrega.composeapp.component.action.ShareDropdown
import dev.galaxius.mnrega.composeapp.component.dialog.ConfirmationDialog
import dev.galaxius.mnrega.composeapp.component.scaffold.MnregaScaffold
import dev.galaxius.mnrega.composeapp.component.scaffold.MnregaTopAppBar
import dev.galaxius.mnrega.composeapp.component.text.NoteField
import dev.galaxius.mnrega.composeapp.component.text.NoteTitleField
import dev.galaxius.mnrega.composeapp.utils.collectState
import dev.galaxius.mnrega.utils.saveBitmap
import dev.galaxius.mnrega.utils.share.shareImage
import dev.galaxius.mnrega.utils.share.shareNoteText
import dev.galaxius.mnrega.view.viewmodel.NoteDetailViewModel
import dev.shreyaspatil.capturable.Capturable
import dev.shreyaspatil.capturable.controller.CaptureController
import dev.shreyaspatil.capturable.controller.rememberCaptureController

@Composable
fun NoteDetailsScreen(
    viewModel: NoteDetailViewModel,
    onNavigateUp: () -> Unit
) {
    val state by viewModel.collectState()
    val context = LocalContext.current

    var showDeleteNoteConfirmation by remember { mutableStateOf(false) }

    NoteDetailContent(
        title = state.title ?: "",
        attendance = state.attendance ?: "",
        error = state.error,
        showSaveButton = state.showSave,
        onTitleChange = viewModel::setTitle,
        onNoteChange = viewModel::setNote,
        onSaveClick = viewModel::save,
        onDeleteClick = { showDeleteNoteConfirmation = true },
        onNavigateUp = onNavigateUp,
        onShareNoteAsText = { context.shareNoteText(state.title ?: "", state.attendance ?: "") },
        onShareNoteAsImage = { bitmap ->
            val uri = saveBitmap(context, bitmap.asAndroidBitmap())
            if (uri != null) {
                context.shareImage(uri)
            }
        }
    )

    DeleteNoteConfirmation(
        show = showDeleteNoteConfirmation,
        onConfirm = viewModel::delete,
        onDismiss = { showDeleteNoteConfirmation = false }
    )

    LaunchedEffect(state.finished) {
        if (state.finished) {
            onNavigateUp()
        }
    }
}

@Composable
fun NoteDetailContent(
    title: String,
    attendance: String,
    error: String?,
    showSaveButton: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onNavigateUp: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareNoteAsText: () -> Unit,
    onShareNoteAsImage: (ImageBitmap) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val captureController = rememberCaptureController()

    MnregaScaffold(
        error = error,
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable(true),
        mnregaTopAppBar = {
            MnregaTopAppBar(
                onNavigateUp = onNavigateUp,
                actions = {
                    NoteDetailActions(
                        onDeleteClick = onDeleteClick,
                        onShareNoteAsTextClick = onShareNoteAsText,
                        onShareNoteAsImageClick = {
                            focusRequester.requestFocus()
                            captureController.capture()
                        }
                    )
                }
            )
        },
        content = {
            NoteDetailBody(
                captureController = captureController,
                onCaptured = onShareNoteAsImage,
                title = title,
                onTitleChange = onTitleChange,
                attendance = attendance,
                onNoteChange = onNoteChange
            )
        },
        floatingActionButton = {
            if (showSaveButton) {
                ExtendedFloatingActionButton(
                    text = { Text("Save", color = Color.White) },
                    icon = { Icon(Icons.Filled.Done, "Save", tint = Color.White) },
                    onClick = onSaveClick,
                    backgroundColor = MaterialTheme.colors.primary
                )
            }
        }
    )
}

@Composable
private fun NoteDetailActions(
    onDeleteClick: () -> Unit,
    onShareNoteAsTextClick: () -> Unit,
    onShareNoteAsImageClick: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    DeleteAction(onClick = onDeleteClick)
    ShareAction(onClick = { dropdownExpanded = true })
    ShareDropdown(
        expanded = dropdownExpanded,
        onDismissRequest = { dropdownExpanded = false },
        shareActions = listOf(
            ShareActionItem(
                label = "Text",
                onActionClick = onShareNoteAsTextClick
            ),
            ShareActionItem(
                label = "Image",
                onActionClick = onShareNoteAsImageClick
            ),
        )
    )
}

@Composable
private fun NoteDetailBody(
    captureController: CaptureController,
    onCaptured: (ImageBitmap) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    attendance: String,
    onNoteChange: (String) -> Unit
) {
    Capturable(
        controller = captureController,
        onCaptured = { bitmap, _ -> bitmap?.let(onCaptured) }
    ) {
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
                onTextChange = onTitleChange
            )

            NoteField(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 32.dp)
                    .background(MaterialTheme.colors.background),
                value = attendance,
                onTextChange = onNoteChange
            )
        }
    }
}

@Composable
fun DeleteNoteConfirmation(show: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    if (show) {
        ConfirmationDialog(
            title = "Delete?",
            message = "Sure want to delete this attendance?",
            onConfirmedYes = onConfirm,
            onConfirmedNo = onDismiss,
            onDismissed = onDismiss
        )
    }
}
