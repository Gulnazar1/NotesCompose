package com.startupapps.notescompose.feature.notes.store

import com.arkivanov.mvikotlin.core.store.Store
import com.startupapps.notescompose.domain.model.Note
import com.startupapps.notescompose.domain.model.NoteHistory

interface NotesStore : Store<NotesStore.Intent, NotesStore.State, Nothing> {

    sealed class Intent {
        data object Load : Intent()
        data class Add(
            val title: String,
            val text: String,
            val label: String = "",
            val color: Int = 0xFFFFFFFF.toInt(),
            val imageUri: String? = null
        ) : Intent()

        data class Update(
            val note: Note,
            val title: String,
            val text: String,
            val label: String = "",
            val color: Int = 0xFFFFFFFF.toInt(),
            val imageUri: String? = null
        ) : Intent()

        data class MoveToTrash(val note: Note) : Intent()
        data class Restore(val note: Note) : Intent()
        data class DeleteForever(val note: Note) : Intent()
        data class TogglePin(val note: Note) : Intent()
        data class ToggleArchive(val note: Note) : Intent()
        data object ClearTrash : Intent()
        data class LoadHistory(val noteId: Int) : Intent()
        data class RestoreVersion(val history: NoteHistory) : Intent()
        data object DismissPremiumDialog : Intent()
    }

    data class State(
        val notes: List<Note> = emptyList(),
        val archivedNotes: List<Note> = emptyList(),
        val trashNotes: List<Note> = emptyList(),
        val noteHistory: List<NoteHistory> = emptyList(),
        val showPremiumDialog: Boolean = false
    )
}
