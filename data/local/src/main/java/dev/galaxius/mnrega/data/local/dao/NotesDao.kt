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

package dev.galaxius.mnrega.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.galaxius.mnrega.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    /**
     * The return type of this method is nullable because internally it throws an error if
     * entity doesn't exist.
     *
     * Official docs says
     *
     * * When the return type is Flow<T>, querying an empty table throws a null pointer exception.
     * * When the return type is Flow<T?>, querying an empty table emits a null value.
     * * When the return type is Flow<List<T>>, querying an empty table emits an empty list.
     *
     * Refer: https://developer.android.com/reference/androidx/room/Query
     */
    @Query("SELECT * FROM attendances WHERE attendanceId = :attendanceId")
    fun getNoteById(attendanceId: String): Flow<NoteEntity?>

    @Query("SELECT * FROM attendances ORDER BY created DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert
    suspend fun addNote(attendance: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNotes(attendances: List<NoteEntity>)

    @Query("UPDATE attendances SET title = :title, attendance = :attendance WHERE attendanceId = :attendanceId")
    suspend fun updateNoteById(attendanceId: String, title: String, attendance: String)

    @Query("DELETE FROM attendances WHERE attendanceId = :attendanceId")
    suspend fun deleteNoteById(attendanceId: String)

    @Query("DELETE FROM attendances")
    suspend fun deleteAllNotes()

    @Query("UPDATE attendances SET attendanceId = :newNoteId WHERE attendanceId = :oldNoteId")
    fun updateNoteId(oldNoteId: String, newNoteId: String)
}
