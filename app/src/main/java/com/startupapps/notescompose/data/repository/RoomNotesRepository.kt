package com.startupapps.notescompose.data.repository

import com.startupapps.notescompose.data.NoteDao
import com.startupapps.notescompose.data.mapper.toDomain
import com.startupapps.notescompose.data.mapper.toEntity
import com.startupapps.notescompose.domain.model.Note
import com.startupapps.notescompose.domain.model.NoteHistory
import com.startupapps.notescompose.domain.repository.NotesRepository

class RoomNotesRepository(
    private val dao: NoteDao
) : NotesRepository {

    override suspend fun getNotes(): List<Note> =
        dao.getAllNotes().map { it.toDomain() }

    override suspend fun getArchivedNotes(): List<Note> =
        dao.getArchivedNotes().map { it.toDomain() }

    override suspend fun getTrashNotes(): List<Note> =
        dao.getTrashNotes().map { it.toDomain() }

    override suspend fun getNoteById(id: Int): Note? =
        dao.getNoteById(id)?.toDomain()

    override suspend fun insertNote(note: Note) =
        dao.insertNote(note.toEntity())

    override suspend fun updateNote(note: Note) =
        dao.updateNote(note.toEntity())

    override suspend fun deleteNote(note: Note) =
        dao.deleteNote(note.toEntity())

    override suspend fun clearTrash() =
        dao.clearNotesTrash()

    override suspend fun deleteOldNotes(cutoff: Long) =
        dao.deleteOldNotes(cutoff)

    override suspend fun countPinnedNotes(): Int =
        dao.getAllNotes().count { it.isPinned }

    override suspend fun insertHistory(history: NoteHistory) =
        dao.insertHistory(history.toEntity())

    override suspend fun getHistory(noteId: Int): List<NoteHistory> =
        dao.getHistoryForNote(noteId).map { it.toDomain() }

    override suspend fun deleteHistory(noteId: Int) =
        dao.deleteHistoryForNote(noteId)
}
