package com.startupapps.notescompose.feature.notes.component

import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.startupapps.notescompose.domain.model.Note
import com.startupapps.notescompose.domain.model.NoteHistory
import com.startupapps.notescompose.feature.notes.store.NotesStore
import com.startupapps.notescompose.feature.settings.store.SettingsStore
import kotlinx.coroutines.flow.StateFlow

interface NotesListComponent {
    val state: StateFlow<NotesStore.State>
    val settings: StateFlow<SettingsStore.State>
    fun onAddNote()
    fun onOpenArchive()
    fun onOpenTrash()
    fun onClickNote(id: Int)
    fun onTogglePin(note: Note)
    fun onToggleArchive(note: Note)
    fun onDeleteNote(note: Note)
    fun onDismissPremiumDialog()
}

interface NotesArchiveComponent {
    val state: StateFlow<NotesStore.State>
    val settings: StateFlow<SettingsStore.State>
    fun onClickNote(id: Int)
    fun onTogglePin(note: Note)
    fun onToggleArchive(note: Note)
    fun onDeleteNote(note: Note)
    fun onBack()
}

interface NotesTrashComponent {
    val state: StateFlow<NotesStore.State>
    fun onRestoreNote(note: Note)
    fun onDeleteNoteForever(note: Note)
    fun onClearAll()
    fun onBack()
}

interface NoteEditorComponent {
    fun onSave(title: String, text: String, label: String, color: Int, imageUri: String?)
    fun onBack()
}

interface NoteDetailComponent {
    val noteId: Int
    val state: StateFlow<NotesStore.State>
    fun onLoadHistory()
    fun onRestoreVersion(history: NoteHistory)
    fun onSave(title: String, text: String, label: String, color: Int, imageUri: String?)
    fun onDelete()
    fun onBack()
}

class DefaultNotesListComponent(
    private val store: NotesStore,
    settingsStore: SettingsStore,
    private val onAddNoteRequested: () -> Unit,
    private val onArchiveRequested: () -> Unit,
    private val onTrashRequested: () -> Unit,
    private val onDetailRequested: (Int) -> Unit
) : NotesListComponent {

    override val state: StateFlow<NotesStore.State> = store.stateFlow
    override val settings: StateFlow<SettingsStore.State> = settingsStore.stateFlow

    override fun onAddNote() = onAddNoteRequested()

    override fun onOpenArchive() = onArchiveRequested()

    override fun onOpenTrash() = onTrashRequested()

    override fun onClickNote(id: Int) = onDetailRequested(id)

    override fun onTogglePin(note: Note) = store.accept(NotesStore.Intent.TogglePin(note))

    override fun onToggleArchive(note: Note) = store.accept(NotesStore.Intent.ToggleArchive(note))

    override fun onDeleteNote(note: Note) = store.accept(NotesStore.Intent.MoveToTrash(note))

    override fun onDismissPremiumDialog() = store.accept(NotesStore.Intent.DismissPremiumDialog)
}

class DefaultNotesArchiveComponent(
    private val store: NotesStore,
    settingsStore: SettingsStore,
    private val onBackRequested: () -> Unit,
    private val onDetailRequested: (Int) -> Unit
) : NotesArchiveComponent {

    override val state: StateFlow<NotesStore.State> = store.stateFlow
    override val settings: StateFlow<SettingsStore.State> = settingsStore.stateFlow

    override fun onClickNote(id: Int) = onDetailRequested(id)

    override fun onTogglePin(note: Note) = store.accept(NotesStore.Intent.TogglePin(note))

    override fun onToggleArchive(note: Note) = store.accept(NotesStore.Intent.ToggleArchive(note))

    override fun onDeleteNote(note: Note) = store.accept(NotesStore.Intent.MoveToTrash(note))

    override fun onBack() = onBackRequested()
}

class DefaultNotesTrashComponent(
    private val store: NotesStore,
    private val onBackRequested: () -> Unit
) : NotesTrashComponent {

    override val state: StateFlow<NotesStore.State> = store.stateFlow

    override fun onRestoreNote(note: Note) = store.accept(NotesStore.Intent.Restore(note))

    override fun onDeleteNoteForever(note: Note) = store.accept(NotesStore.Intent.DeleteForever(note))

    override fun onClearAll() = store.accept(NotesStore.Intent.ClearTrash)

    override fun onBack() = onBackRequested()
}

class DefaultNoteEditorComponent(
    private val store: NotesStore,
    private val onBackRequested: () -> Unit
) : NoteEditorComponent {

    override fun onSave(title: String, text: String, label: String, color: Int, imageUri: String?) {
        store.accept(NotesStore.Intent.Add(title, text, label, color, imageUri))
        onBackRequested()
    }

    override fun onBack() = onBackRequested()
}

class DefaultNoteDetailComponent(
    private val noteStore: NotesStore,
    override val noteId: Int,
    private val onBackRequested: () -> Unit
) : NoteDetailComponent {

    override val state: StateFlow<NotesStore.State> = noteStore.stateFlow

    override fun onLoadHistory() {
        noteStore.accept(NotesStore.Intent.LoadHistory(noteId))
    }

    override fun onRestoreVersion(history: NoteHistory) {
        noteStore.accept(NotesStore.Intent.RestoreVersion(history))
    }

    override fun onSave(title: String, text: String, label: String, color: Int, imageUri: String?) {
        val note = noteStore.state.notes.find { it.id == noteId }
            ?: noteStore.state.archivedNotes.find { it.id == noteId }
            ?: return

        noteStore.accept(
            NotesStore.Intent.Update(
                note = note,
                title = title,
                text = text,
                label = label,
                color = color,
                imageUri = imageUri
            )
        )
        onBackRequested()
    }

    override fun onDelete() {
        val note = noteStore.state.notes.find { it.id == noteId }
            ?: noteStore.state.archivedNotes.find { it.id == noteId }
            ?: return

        noteStore.accept(NotesStore.Intent.MoveToTrash(note))
        onBackRequested()
    }

    override fun onBack() = onBackRequested()
}
