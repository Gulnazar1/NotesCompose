package com.startupapps.notescompose.domain.repository

import com.startupapps.notescompose.domain.model.Note
import com.startupapps.notescompose.domain.model.NoteHistory

interface NotesRepository {
    suspend fun getNotes(): List<Note>
    suspend fun getArchivedNotes(): List<Note>
    suspend fun getTrashNotes(): List<Note>
    suspend fun getNoteById(id: Int): Note?
    suspend fun insertNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun clearTrash()
    suspend fun deleteOldNotes(cutoff: Long)
    suspend fun countPinnedNotes(): Int
    suspend fun insertHistory(history: NoteHistory)
    suspend fun getHistory(noteId: Int): List<NoteHistory>
    suspend fun deleteHistory(noteId: Int)
}
