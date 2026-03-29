package com.startupapps.notescompose.data

import com.startupapps.notescompose.domain.NoteRepository

class NoteRepositoryImpl(
    private val dao: NoteDao
) : NoteRepository {
    
    // Notes
    override suspend fun getAllNotes(): List<NoteEntity> = dao.getAllNotes()
    override suspend fun getArchivedNotes(): List<NoteEntity> = dao.getArchivedNotes() // ✅
    override suspend fun getTrashNotes(): List<NoteEntity> = dao.getTrashNotes()
    override suspend fun insertNote(note: NoteEntity) = dao.insertNote(note)
    override suspend fun updateNote(note: NoteEntity) = dao.updateNote(note)
    override suspend fun deleteNote(note: NoteEntity) = dao.deleteNote(note)
    override suspend fun clearNotesTrash() = dao.clearNotesTrash()
    override suspend fun deleteOldNotes(timestamp: Long) = dao.deleteOldNotes(timestamp)

    // History
    override suspend fun insertHistory(history: NoteHistoryEntity) = dao.insertHistory(history)
    override suspend fun getHistoryForNote(noteId: Int): List<NoteHistoryEntity> = dao.getHistoryForNote(noteId)
    override suspend fun deleteHistoryForNote(noteId: Int) = dao.deleteHistoryForNote(noteId)

    // Tasks
    override suspend fun getAllTasks(): List<TaskEntity> = dao.getAllTasks()
    override suspend fun getTrashTasks(): List<TaskEntity> = dao.getTrashTasks()
    override suspend fun insertTask(task: TaskEntity) = dao.insertTask(task)
    override suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    override suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)
    override suspend fun clearTasksTrash() = dao.clearTasksTrash()
    override suspend fun deleteOldTasks(timestamp: Long) = dao.deleteOldTasks(timestamp)
}
