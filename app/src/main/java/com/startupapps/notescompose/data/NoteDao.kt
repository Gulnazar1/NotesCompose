package com.startupapps.notescompose.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao{
    // --- Заметки ---
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, id DESC")
    suspend fun getAllNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE isDeleted = 1")
    suspend fun getTrashNotes(): List<NoteEntity>

    @Insert
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun clearNotesTrash()

    @Query("DELETE FROM notes WHERE isDeleted = 1 AND deletedAt < :timestamp")
    suspend fun deleteOldNotes(timestamp: Long)

    // --- История заметок ---
    @Insert
    suspend fun insertHistory(history: NoteHistoryEntity)

    @Query("SELECT * FROM note_history WHERE noteId = :noteId ORDER BY timestamp DESC")
    suspend fun getHistoryForNote(noteId: Int): List<NoteHistoryEntity>

    @Query("DELETE FROM note_history WHERE noteId = :noteId")
    suspend fun deleteHistoryForNote(noteId: Int)

    // --- Задачи ---
    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY id DESC")
    suspend fun getAllTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isDeleted = 1")
    suspend fun getTrashTasks(): List<TaskEntity>

    @Insert
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE isDeleted = 1")
    suspend fun clearTasksTrash()

    @Query("DELETE FROM tasks WHERE isDeleted = 1 AND deletedAt < :timestamp")
    suspend fun deleteOldTasks(timestamp: Long)
}