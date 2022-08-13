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

package dev.galaxius.mnrega.composeapp.component.attendance

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.galaxius.mnrega.core.model.Note

@Composable
fun NotesList(attendances: List<Note>, onClick: (Note) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = Modifier.testTag("attendancesList")
    ) {
        items(
            items = attendances,
            itemContent = { attendance ->
                NoteCard(
                    title = attendance.title,
                    attendance = attendance.attendance,
                    onNoteClick = { onClick(attendance) }
                )
            },
            key = { Triple(it.id, it.title, it.attendance) }
        )
    }
}
