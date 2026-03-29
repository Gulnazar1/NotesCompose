package com.startupapps.notescompose.domain

import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.data.NoteHistoryEntity
import com.startupapps.notescompose.data.TaskEntity

interface NoteRepository {
    // Notes
    suspend fun getAllNotes(): List<NoteEntity>
    suspend fun getArchivedNotes(): List<NoteEntity> // ✅
    suspend fun getTrashNotes(): List<NoteEntity>
    suspend fun insertNote(note: NoteEntity)
    suspend fun updateNote(note: NoteEntity)
    suspend fun deleteNote(note: NoteEntity)
    suspend fun clearNotesTrash()
    suspend fun deleteOldNotes(timestamp: Long)

    // History
    suspend fun insertHistory(history: NoteHistoryEntity)
    suspend fun getHistoryForNote(noteId: Int): List<NoteHistoryEntity>
    suspend fun deleteHistoryForNote(noteId: Int)

    // Tasks
    suspend fun getAllTasks(): List<TaskEntity>
    suspend fun getTrashTasks(): List<TaskEntity>
    suspend fun insertTask(task: TaskEntity)
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(task: TaskEntity)
    suspend fun clearTasksTrash()
    suspend fun deleteOldTasks(timestamp: Long)
}
