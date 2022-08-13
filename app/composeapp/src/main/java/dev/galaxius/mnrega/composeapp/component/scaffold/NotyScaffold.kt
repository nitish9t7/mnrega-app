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

package dev.galaxius.mnrega.composeapp.component.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.galaxius.mnrega.composeapp.component.dialog.FailureDialog
import dev.galaxius.mnrega.composeapp.component.dialog.LoaderDialog

/**
 * Compose's wrapped Scaffold for this project
 */
@Composable
fun MnregaScaffold(
    modifier: Modifier = Modifier,
    mnregaTopAppBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    isLoading: Boolean = false,
    error: String? = null,
) {
    if (isLoading) {
        LoaderDialog()
    }
    if (error != null) {
        FailureDialog(error)
    }
    Scaffold(
        modifier = modifier,
        topBar = mnregaTopAppBar,
        content = content,
        floatingActionButton = floatingActionButton
    )
}
