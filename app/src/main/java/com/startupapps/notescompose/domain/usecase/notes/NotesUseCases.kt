package com.startupapps.notescompose.domain.usecase.notes

import com.startupapps.notescompose.domain.model.Note
import com.startupapps.notescompose.domain.model.NoteHistory
import com.startupapps.notescompose.domain.repository.NotesRepository
import java.util.concurrent.TimeUnit

data class NotesUseCases(
    val addNote: AddNoteUseCase,
    val updateNote: UpdateNoteUseCase,
    val deleteNote: DeleteNoteUseCase,
    val getNotes: GetNotesUseCase,
    val getArchivedNotes: GetArchivedNotesUseCase,
    val getTrashNotes: GetTrashNotesUseCase,
    val moveToTrashNote: MoveToTrashNoteUseCase,
    val restoreNote: RestoreNoteUseCase,
    val toggleArchiveNote: ToggleArchiveNoteUseCase,
    val togglePinNote: TogglePinNoteUseCase,
    val clearTrash: ClearNotesTrashUseCase,
    val cleanupOldNotes: CleanupOldNotesUseCase,
    val getNoteHistory: GetNoteHistoryUseCase,
    val restoreNoteVersion: RestoreNoteVersionUseCase
)

fun createNotesUseCases(repository: NotesRepository): NotesUseCases =
    NotesUseCases(
        addNote = AddNoteUseCase(repository),
        updateNote = UpdateNoteUseCase(repository),
        deleteNote = DeleteNoteUseCase(repository),
        getNotes = GetNotesUseCase(repository),
        getArchivedNotes = GetArchivedNotesUseCase(repository),
        getTrashNotes = GetTrashNotesUseCase(repository),
        moveToTrashNote = MoveToTrashNoteUseCase(repository),
        restoreNote = RestoreNoteUseCase(repository),
        toggleArchiveNote = ToggleArchiveNoteUseCase(repository),
        togglePinNote = TogglePinNoteUseCase(repository),
        clearTrash = ClearNotesTrashUseCase(repository),
        cleanupOldNotes = CleanupOldNotesUseCase(repository),
        getNoteHistory = GetNoteHistoryUseCase(repository),
        restoreNoteVersion = RestoreNoteVersionUseCase(repository)
    )

class AddNoteUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(
        title: String,
        text: String,
        label: String = "",
        color: Int = 0xFFFFFFFF.toInt(),
        imageUri: String? = null
    ) {
        repository.insertNote(
            Note(
                title = title,
                text = text,
                label = label,
                color = color,
                imageUri = imageUri
            )
        )
    }
}

class UpdateNoteUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(
        note: Note,
        title: String,
        text: String,
        label: String = "",
        color: Int = 0xFFFFFFFF.toInt(),
        imageUri: String? = null
    ) {
        val hasTextChanges =
            note.title != title ||
                note.text != text ||
                note.label != label ||
                note.color != color ||
                note.imageUri != imageUri

        if (hasTextChanges) {
            repository.insertHistory(
                NoteHistory(
                    noteId = note.id,
                    title = note.title,
                    text = note.text
                )
            )
        }

        repository.updateNote(
            note.copy(
                title = title,
                text = text,
                label = label,
                color = color,
                imageUri = imageUri
            )
        )
    }
}

class DeleteNoteUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.deleteNote(note)
        repository.deleteHistory(note.id)
    }
}

class GetNotesUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(): List<Note> =
        repository.getNotes()
}

class GetArchivedNotesUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(): List<Note> =
        repository.getArchivedNotes()
}

class GetTrashNotesUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(): List<Note> =
        repository.getTrashNotes()
}

class MoveToTrashNoteUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.updateNote(
            note.copy(
                isDeleted = true,
                deletedAt = System.currentTimeMillis()
            )
        )
    }
}

class RestoreNoteUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.updateNote(note.copy(isDeleted = false, deletedAt = null))
    }
}

class ToggleArchiveNoteUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.updateNote(note.copy(isArchived = !note.isArchived))
    }
}

class TogglePinNoteUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(note: Note): Boolean {
        val isPinning = !note.isPinned
        if (isPinning && repository.countPinnedNotes() >= MAX_PINNED_NOTES) {
            return false
        }

        repository.updateNote(note.copy(isPinned = isPinning))
        return true
    }

    private companion object {
        const val MAX_PINNED_NOTES = 5
    }
}

class ClearNotesTrashUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke() {
        repository.clearTrash()
    }
}

class CleanupOldNotesUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(now: Long = System.currentTimeMillis()) {
        val cutoff = now - TimeUnit.DAYS.toMillis(TRASH_TTL_DAYS)
        repository.deleteOldNotes(cutoff)
    }

    private companion object {
        const val TRASH_TTL_DAYS = 30L
    }
}

class GetNoteHistoryUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(noteId: Int): List<NoteHistory> =
        repository.getHistory(noteId)
}

class RestoreNoteVersionUseCase(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(history: NoteHistory): Int? {
        val note = repository.getNoteById(history.noteId) ?: return null

        repository.insertHistory(
            NoteHistory(
                noteId = note.id,
                title = note.title,
                text = note.text
            )
        )
        repository.updateNote(
            note.copy(
                title = history.title,
                text = history.text
            )
        )

        return note.id
    }
}
